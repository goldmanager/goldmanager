package com.my.goldmanager.pricecollector;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.mockito.MockitoAnnotations;
import tools.jackson.databind.ObjectMapper;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.repository.MaterialHistoryRepository;
import com.my.goldmanager.repository.MaterialRepository;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.exception.ValidationException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetalPriceCollectorTest {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	@Autowired
	private MaterialService materialService;

	@Autowired
	private MaterialRepository materialRepository;

	@Autowired
	private MaterialHistoryRepository materialHistoryRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private AutoCloseable mocks;

	@Mock
	private HttpClient httpClient;

	@Mock
	private HttpClient.Builder httpClientBuilder;

	@Mock
	private HttpResponse<String> httpResponse;

	@BeforeEach
	void initMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@Test
	void testInitWithoutHistory() throws ValidationException, IOException, InterruptedException {
		Material gold = new Material();
		gold.setEntryDate(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
		gold.setName("Gold");
		gold.setPrice(100f);

		materialService.store(gold);

		Material silver = new Material();
		silver.setEntryDate(Date.from(Instant.now().minus(4, ChronoUnit.DAYS)));
		silver.setName("Silver");
		silver.setPrice(10f);

		materialService.store(silver);

		MetalPriceCollector collector = new MetalPriceCollector();

		collector.setMaterialHistoryRepository(materialHistoryRepository);
		collector.setMaterialRepository(materialRepository);
		collector.setMaterialService(materialService);
		collector.setWebClientBuilder(httpClientBuilder);
		collector.setObjectMapper(objectMapper);

		collector.setApiKey("apikey");
		collector.setEndpoint("http://test/api");
		collector.setCurrency("EUR");
		collector.setMappings("Gold=XAU,Silver=XAG");

		collector.setFetchPeriodMinutes(5);
		collector.setFetchHistoryDays(0);

		collector.init();

		Assertions.assertEquals(1, materialHistoryRepository.findByMaterial(gold.getId()).size());

		Assertions.assertEquals(1, materialHistoryRepository.findByMaterial(silver.getId()).size());
	}

	@Test
	void testNotInitialized() throws ValidationException, IOException, InterruptedException {
		Material gold = new Material();
		gold.setEntryDate(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
		gold.setName("Gold");
		gold.setPrice(100f);

		materialService.store(gold);

		Material silver = new Material();
		silver.setEntryDate(Date.from(Instant.now().minus(4, ChronoUnit.DAYS)));
		silver.setName("Silver");
		silver.setPrice(10f);

		materialService.store(silver);

		MetalPriceCollector collector = new MetalPriceCollector();

		collector.setMaterialHistoryRepository(materialHistoryRepository);
		collector.setMaterialRepository(materialRepository);
		collector.setMaterialService(materialService);
		collector.setWebClientBuilder(httpClientBuilder);
		collector.setObjectMapper(objectMapper);

		collector.setApiKey("apikey");
		collector.setEndpoint("http://test/api");
		collector.setCurrency("EUR");
		collector.setMappings("Gold=XAU,Silver=XAG");

		collector.setFetchPeriodMinutes(5);
		collector.setFetchHistoryDays(0);

		collector.getCurrentPrices();

		Assertions.assertEquals(1, materialHistoryRepository.findByMaterial(gold.getId()).size());
		Assertions.assertEquals(1, materialHistoryRepository.findByMaterial(silver.getId()).size());

	}

	@Test
	void testUpdatePrices() throws ValidationException, IOException, InterruptedException {
		Material gold = new Material();
		gold.setEntryDate(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
		gold.setName("Gold");
		gold.setPrice(100f);

		materialService.store(gold);

		Material silver = new Material();
		silver.setEntryDate(Date.from(Instant.now().minus(4, ChronoUnit.DAYS)));
		silver.setName("Silver");
		silver.setPrice(10f);

		Date expecteddate = Date
				.from(Instant.ofEpochSecond(Instant.now().minus(5, ChronoUnit.SECONDS).getEpochSecond()));
		materialService.store(silver);

		when(httpClientBuilder.build()).thenReturn(httpClient);

		when(httpClient.send(any(), any())).thenAnswer(new Answer<HttpResponse<String>>() {

			@Override
			public HttpResponse<String> answer(InvocationOnMock invocation) throws Throwable {
				String url = invocation.getArgument(0).toString();
				URI uri = URI.create(url.substring(0, url.length() - " GET".length()));
				Map<String, String> queryString = parseQueryString(uri.getQuery());
				LatestPrices latestPrices = new LatestPrices();

				latestPrices.setBase(queryString.get("base"));

				latestPrices.setTimestamp(expecteddate.toInstant().getEpochSecond());

				latestPrices.setRates(new TreeMap<String, Float>());
				latestPrices.getRates().put("XAU", 3650f);
				latestPrices.getRates().put("XAG", 365f);
				latestPrices.getRates().put(latestPrices.getBase() + "XAU", 3650f);
				latestPrices.getRates().put(latestPrices.getBase() + "XAG", 365f);
				latestPrices.setSuccess(true);
				when(httpResponse.statusCode()).thenReturn(200);
				when(httpResponse.body()).thenReturn(objectMapper.writeValueAsString(latestPrices));
				return httpResponse;
			}
		});

		MetalPriceCollector collector = new MetalPriceCollector();

		collector.setMaterialHistoryRepository(materialHistoryRepository);
		collector.setMaterialRepository(materialRepository);
		collector.setMaterialService(materialService);
		collector.setWebClientBuilder(httpClientBuilder);
		collector.setObjectMapper(objectMapper);

		collector.setApiKey("apikey");
		collector.setEndpoint("http://test/api");
		collector.setCurrency("EUR");
		collector.setMappings("Gold=XAU,Silver=XAG");

		collector.setFetchPeriodMinutes(5);
		collector.setFetchHistoryDays(0);

		collector.init();

		collector.getCurrentPrices();


		Assertions.assertEquals(2, materialHistoryRepository.findByMaterial(gold.getId()).size());
		Assertions.assertEquals(2, materialHistoryRepository.findByMaterial(silver.getId()).size());
		Assertions.assertEquals(3650f, materialService.getById(gold.getId()).get().getPrice());
		Assertions.assertEquals(365f, materialService.getById(silver.getId()).get().getPrice());
		Assertions.assertEquals(expecteddate.toInstant().toEpochMilli(),
				materialService.getById(gold.getId()).get().getEntryDate().toInstant().toEpochMilli());
		Assertions.assertEquals(expecteddate.toInstant().toEpochMilli(),
				materialService.getById(silver.getId()).get().getEntryDate().toInstant().toEpochMilli());
	}

	@Test
	void testInitWithHistory() throws ValidationException, IOException, InterruptedException {
		Material gold = new Material();
		gold.setEntryDate(Date.from(Instant.now().minus(5, ChronoUnit.DAYS)));
		gold.setName("Gold");
		gold.setPrice(100f);

		materialService.store(gold);

		Material silver = new Material();
		silver.setEntryDate(Date.from(Instant.now().minus(4, ChronoUnit.DAYS)));
		silver.setName("Silver");
		silver.setPrice(10f);

		materialService.store(silver);

		
		when(httpClientBuilder.build()).thenReturn(httpClient);
		
		when(httpClient.send(any(), any())).thenAnswer(new Answer<HttpResponse<String>>() {

			@Override
			public HttpResponse<String> answer(InvocationOnMock invocation) throws Throwable {
				String url = invocation.getArgument(0).toString();
				URI uri = URI.create(url.substring(0, url.length() - " GET".length()));
				Map<String, String> queryString = parseQueryString(uri.getQuery());
				Timeframe timeFrame = new Timeframe();
				timeFrame.setBase(queryString.get("base"));
				timeFrame.setStart_date(queryString.get("start_date"));

				timeFrame.setEnd_date(queryString.get("end_date"));

				Date startdate = dateFormat.parse(timeFrame.getStart_date());
				Date endDate = dateFormat.parse(timeFrame.getEnd_date());

				List<String> dates = new LinkedList<>();
				timeFrame.setRates(new TreeMap<String, TreeMap<String, Float>>());
				while (endDate.after(startdate)) {
					dates.add(dateFormat.format(endDate));
					endDate = Date.from(endDate.toInstant().minus(1, ChronoUnit.DAYS));
				}

				float price = 365f;
				if ("XAU".equals(queryString.get("currencies"))) {
					price = 3650;
				}
				for (String d : dates.reversed()) {

					TreeMap<String, Float> rate = new TreeMap<String, Float>();
					rate.put(queryString.get("currencies"), price);
					rate.put(timeFrame.getBase() + queryString.get("currencies"), price);
					timeFrame.getRates().put(d, rate);
					price = price - 1;
				}
				timeFrame.setSuccess(true);
				when(httpResponse.statusCode()).thenReturn(200);
				when(httpResponse.body()).thenReturn(objectMapper.writeValueAsString(timeFrame));
				return httpResponse;
			}
		});

		MetalPriceCollector collector = new MetalPriceCollector();

		collector.setMaterialHistoryRepository(materialHistoryRepository);
		collector.setMaterialRepository(materialRepository);
		collector.setMaterialService(materialService);
		collector.setWebClientBuilder(httpClientBuilder);
		collector.setObjectMapper(objectMapper);

		collector.setApiKey("apikey");
		collector.setEndpoint("http://test/api");
		collector.setCurrency("EUR");
		collector.setMappings("Gold=XAU,Silver=XAG");

		collector.setFetchPeriodMinutes(5);
		collector.setFetchHistoryDays(5);

		collector.init();

		Assertions.assertEquals(6, materialHistoryRepository.findByMaterial(gold.getId()).size());

		Assertions.assertEquals(5, materialHistoryRepository.findByMaterial(silver.getId()).size());
	}

	@AfterEach
	public void cleanUp() {
		materialHistoryRepository.deleteAll();
		materialRepository.deleteAll();
		if (mocks != null) {
			try {
				mocks.close();
			} catch (Exception ignored) {
			}
		}

	}

	private Map<String, String> parseQueryString(String queryString) {
		Map<String, String> result = new HashMap<>();
		String[] pairs = queryString.split("[&]");
		for (String pair : pairs) {
			String[] entry = pair.split("[=]");
			if (entry.length == 2) {
				result.put(entry[0], entry[1]);
			}
		}
		return result;
	}
}
