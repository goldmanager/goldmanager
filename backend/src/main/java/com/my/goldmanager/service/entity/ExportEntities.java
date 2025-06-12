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
package com.my.goldmanager.service.entity;

import java.util.List;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.entity.UserLogin;

import lombok.Getter;
import lombok.Setter;

/**
 * Contains the exported entities
 */
public class ExportEntities {

	@Getter
	@Setter
	private List<Unit> units;

	@Getter
	@Setter
	private List<Material> metals;

	@Getter
	@Setter
	private List<UserLogin> users;

	@Getter
	@Setter
	private List<ItemStorage> itemStorages;

	@Getter
	@Setter
	private List<MaterialHistory> materialHistories;

	@Getter
	@Setter
	private List<ItemType> itemTypes;

	@Getter
	@Setter
	private List<Item> items;

}
