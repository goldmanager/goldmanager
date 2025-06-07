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
package com.my.goldmanager.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.my.goldmanager.entity.Item;

public class PriceCalculatorUtil {

	private PriceCalculatorUtil() {
		// Static class
	}

	/**
	 * Calculate Single price of the provided Item
	 * 
	 * @param item
	 * @param materialPrice
	 * @return
	 */
	public static float calculateSingleItemPrice(Item item, float materialPrice) {
		BigDecimal price = new BigDecimal(
				item.getAmount() * item.getUnit().getFactor() * item.getItemType().getModifier() * materialPrice)
				.setScale(2, RoundingMode.HALF_DOWN);
		return price.floatValue();
	}

	/**
	 * Calculates the price regarding the itemCount
	 * @param item
	 * @param materialPrice
	 * @return
	 */
	public static float calculateTotalItemPrice(Item item, float materialPrice) {
		return new BigDecimal(Float.valueOf(item.getItemCount())* calculateSingleItemPrice(item, materialPrice))
				.setScale(2, RoundingMode.HALF_DOWN).floatValue();
	}
}
