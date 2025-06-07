/** Copyright 2025 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager.pricecollector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.exception.ValidationException;

import lombok.Getter;
import lombok.Setter;

/**
 * Collects current material prices by using
 * https://api.metalpricecollector.com/ rest api
 */
@ConditionalOnProperty(name = "metalpricecollector.enabled", havingValue = "true", matchIfMissing = true)
@Component
@Profile({ "default", "dev" })
public class MetalPriceCollector {
	private static final Logger logger = LoggerFactory.getLogger(MetalPriceCollector.class);
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private AtomicBoolean isInitialized = new AtomicBoolean(false);


	@Autowired
	@Getter
	@Setter
	private HttpClient.Builder webClientBuilder;

	@Autowired
	@Getter
	@Setter
	private ObjectMapper objectMapper;

	@Value("${metalpricecollector.apikey}")
	@Getter
	@Setter
	private String apiKey;

	@Value("${metalpricecollector.endpoint:https://api.metalpriceapi.com/v1}")
	@Getter
	@Setter
	private String endpoint;

	@Value("${metalpricecollector.fetchHistoryDays:0}")
	@Getter
	@Setter
	private int fetchHistoryDays;

	@Value("${metalpricecollector.currency:USD}")
	@Getter
	@Setter
	private String currency;

	@Value("${metalpricecollector.metalmappings:Gold=XAU}")
	@Getter
	@Setter
	private String mappings;

	@Value("${metalpricecollector.fetchIntervalMinutes:60}")
	@Getter
	@Setter
	private long fetchPeriodMinutes;

	@Autowired
	@Getter
	@Setter
	private MaterialService materialService;

	@Autowired
	@Getter
	@Setter
	private MaterialRepository materialRepository;

	@Autowired
	@Getter
	@Setter
	private MaterialHistoryRepository materialHistoryRepository;

	private Map<String, String> getMetalMappings() {
		Map<String, String> result = new HashMap<String, String>();
		String[] mappingsArray = mappings.split("[,]");
		for (String mapping : mappingsArray) {
			String[] mappingArray = mapping.split("[=]");
			if (mappingArray.length == 2) {
				result.put(mappingArray[0], mappingArray[1]);
			}
		}
		return result;
	}

	@Scheduled(timeUnit = TimeUnit.MINUTES, fixedRateString = "${metalpricecollector.fetchIntervalMinutes:60}", initialDelay = 10)
	public void getCurrentPrices() {

		if (isInitialized.get()) {
			logger.debug("Updating prices");
			Map<String, String> mappingSettings = getMetalMappings();
			if (!mappingSettings.isEmpty() && apiKey != null) {
				try (HttpClient httpClient = webClientBuilder.build()) {
					List<Material> materials = materialService.list();

					HttpRequest request = HttpRequest.newBuilder().GET()
							.headers("X-API-KEY", apiKey, "Content-Type", "application/json").uri(URI.create(endpoint
									+ "/latest?base=" + currency + "&currencies=" + getMetalSymbols(mappingSettings)))
							.build();
					HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
					String body = response.body();
					if (response.statusCode() == 200) {
						LatestPrices latestPrices = objectMapper.readValue(body, LatestPrices.class);
						if (latestPrices.isSuccess() && latestPrices.getRates() != null) {

							Date entryDate = Date.from(Instant.ofEpochSecond(latestPrices.getTimestamp()));
							materials.stream().filter(m -> mappingSettings.get(m.getName()) != null
									&& latestPrices.getRates().get(currency + mappingSettings.get(m.getName())) != null)
									.forEach(m -> updateMaterialPrice(m,
											latestPrices.getRates().get(currency + mappingSettings.get(m.getName())),
											entryDate));
						}
					} else {
						logger.error("Could not fetch current prices, response code: {}, body:{}", body);
					}
				} catch (IOException | InterruptedException e) {
					logger.error("Error while retreiving current material price", e);
				}
			}

		}
		else {
			logger.warn("Not yet initialized. Skipping");
		}

	}

	private void updateMaterialPrice(Material m, float price, Date date) {
		if (m.getEntryDate().before(date)) {
			m.setEntryDate(date);
			m.setPrice(price);
			try {
				materialService.update(m.getId(), m);
				logger.info("Updated price for metal {}", m.getName());
			} catch (ValidationException e) {
				logger.error("Can not update metal {}", m.getName(), e);
			}
		} else {
			logger.warn("Skipping Update for metal {}, since the fetched price is older than the currently stored.",
					m.getName());
		}
	}

	private String getMetalSymbols(Map<String, String> mappingSettings) {
		StringBuilder sb = new StringBuilder();
		mappingSettings.values().stream().forEach(material -> sb.append(material).append(","));
		return sb.toString();
	}

	public void init() {
		Map<String, String> mappingSettings = getMetalMappings();
		if (!mappingSettings.isEmpty() && apiKey != null && fetchHistoryDays > 0 && fetchHistoryDays <= 365) {

			try (HttpClient httpClient = webClientBuilder.build()) {

				List<Material> materials = materialService.list();
				for (Entry<String, String> entry : mappingSettings.entrySet()) {
					Optional<Material> optional = materials.stream().filter(m -> m.getName().equals(entry.getKey()))
							.findFirst();
					Date today = new Date();
					if (optional.isPresent()) {

						Material material = optional.get();
						long diffInMillis = Math.abs(today.getTime() - material.getEntryDate().getTime());

						long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

						if (diffInDays > 0) {
							if (diffInDays > fetchHistoryDays) {
								diffInDays = fetchHistoryDays;
							}
							Date dateFrom = new Date(today.getTime() - TimeUnit.DAYS.toMillis(diffInDays));
							HttpRequest request = HttpRequest.newBuilder().GET()
									.headers("X-API-KEY", apiKey, "Content-Type", "application/json")
									.uri(URI.create(endpoint + "/timeframe?base=" + currency + "&start_date="
											+ simpleDateFormat.format(dateFrom) + "&end_date="
											+ simpleDateFormat.format(today) + "&currencies=" + entry.getValue()))
									.build();
							HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
							if (response.statusCode() == 200) {
								String body = response.body();
								Timeframe timeframe = objectMapper.readValue(body, Timeframe.class);

								if (timeframe.isSuccess()) {
									for (Entry<String, TreeMap<String, Float>> rate : timeframe.getRates().entrySet()) {

										if (rate.getValue().get(currency + entry.getValue()) != null) {

											material.setPrice(rate.getValue().get(currency + entry.getValue()));
											Date entryDate = simpleDateFormat.parse(rate.getKey());
											material.setEntryDate(entryDate);
											MaterialHistory mh = new MaterialHistory();

											mh.setMaterial(material);
											mh.setEntryDate(entryDate);
											mh.setPrice(rate.getValue().get(currency + entry.getValue()));
											materialHistoryRepository.save(mh);
											materialRepository.save(material);
										}

									}
								}
								logger.info("Updated PriceHistory for material {}", material.getName());
							} else {
								logger.error("Received ResponseCode: {}, body: {}", response.statusCode(),
										response.body());
							}
						}
					}
				}

			} catch (IOException | InterruptedException | ParseException e) {
				logger.error("Can not parse entry date", e);
			}
		}
		isInitialized.set(true);
	}

}
