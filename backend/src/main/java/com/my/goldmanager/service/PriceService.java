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
package com.my.goldmanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.repository.ItemRepository;
import com.my.goldmanager.rest.entity.Price;
import com.my.goldmanager.rest.entity.PriceGroup;
import com.my.goldmanager.rest.entity.PriceGroupList;
import com.my.goldmanager.rest.entity.PriceList;
import com.my.goldmanager.service.util.PriceCalculatorUtil;

/**
 * Calculates the current Prices
 */
@Service
public class PriceService {

	@Autowired
	private ItemRepository itemRepository;

	@Transactional(readOnly = true)
	public PriceList listAll() {
		return createPriceList(itemRepository.findAll());
	}

	@Transactional(readOnly = true)
	public PriceGroupList groupByMaterial() {
		return groupBy(item -> item.getItemType().getMaterial().getName());
	}

	@Transactional(readOnly = true)
	public PriceGroupList groupByItemType() {
		return groupBy(item -> item.getItemType().getName());
	}

	@Transactional(readOnly = true)
	public Optional<PriceList> listForMaterial(String materialId) {
		List<Item> items = itemRepository.findByMaterialId(materialId);
		return CollectionUtils.isEmpty(items) ? Optional.empty() : Optional.of(createPriceList(items));
	}

	@Transactional(readOnly = true)
	public Optional<PriceList> listForStorage(String storageId) {
		List<Item> items = itemRepository.findByItemStorageId(storageId);
		return CollectionUtils.isEmpty(items) ? Optional.empty() : Optional.of(createPriceList(items));
	}

	@Transactional(readOnly = true)
	public Optional<Price> getPriceofItem(String itemId) {
		return itemRepository.findById(itemId).map(this::calculatePrice);
	}

	private PriceGroupList groupBy(java.util.function.Function<Item, String> groupKeyExtractor) {
		Map<String, PriceGroup> result = new TreeMap<>();
		List<Item> items = itemRepository.findAll();
		items.forEach(item -> {
			String key = groupKeyExtractor.apply(item);
			PriceGroup priceGroup = result.computeIfAbsent(key, k -> {
				PriceGroup newGroup = new PriceGroup();
				newGroup.setGroupName(k);
				newGroup.setPrices(new LinkedList<>());
				return newGroup;
			});
			addItemToPriceGroup(item, priceGroup);
		});
		PriceGroupList priceGroupList = new PriceGroupList();
		priceGroupList.setPriceGroups(new LinkedList<>(result.values()));
		return priceGroupList;
	}

	private void addItemToPriceGroup(Item item, PriceGroup priceGroup) {
		Price price = calculatePrice(item);
		priceGroup.getPrices().add(price);
		BigDecimal totalPrice = new BigDecimal(priceGroup.getTotalPrice() + price.getPriceTotal()).setScale(2,
				RoundingMode.HALF_DOWN);
		priceGroup.setTotalPrice(totalPrice.floatValue());
		BigDecimal amount = new BigDecimal(
				priceGroup.getAmount() + (item.getItemCount() * item.getAmount() * item.getUnit().getFactor()))
				.setScale(2, RoundingMode.HALF_DOWN);
		priceGroup.setAmount(amount.floatValue());
	}

	private PriceList createPriceList(List<Item> items) {
		PriceList result = new PriceList();
		result.setPrices(new LinkedList<>());
		items.forEach(item -> result.getPrices().add(calculatePrice(item)));
		calculateSummaryPrice(result);
		return result;
	}

	private static void calculateSummaryPrice(PriceList priceList) {
		priceList.getPrices().forEach(p -> priceList.setTotalPrice(priceList.getTotalPrice() + p.getPriceTotal()));
		priceList.setTotalPrice(
				new BigDecimal(priceList.getTotalPrice()).setScale(2, RoundingMode.HALF_DOWN).floatValue());
	}

	private Price calculatePrice(Item item) {
		Price result = new Price();
		if (item != null) {
			result.setItem(item);
			result.setPrice(
					PriceCalculatorUtil.calculateSingleItemPrice(item, item.getItemType().getMaterial().getPrice()));
			result.setPriceTotal(
					PriceCalculatorUtil.calculateTotalItemPrice(item, item.getItemType().getMaterial().getPrice()));
		}
		return result;
	}
}
