package com.my.goldmanager.entity;

import java.util.List;

public class TestData {

	private List<Unit> units;

	private List<Material> metals;

	private List<UserLogin> users;

	private List<ItemStorage> itemStorages;

	private List<MaterialHistory> materialHistories;

	private List<ItemType> itemTypes;

	private List<Item> items;

	public List<Unit> getUnits() {
		return units;
	}

	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	public List<Material> getMetals() {
		return metals;
	}

	public void setMetals(List<Material> metals) {
		this.metals = metals;
	}

	public List<UserLogin> getUsers() {
		return users;
	}

	public void setUsers(List<UserLogin> users) {
		this.users = users;
	}

	public List<ItemStorage> getItemStorages() {
		return itemStorages;
	}

	public void setItemStorages(List<ItemStorage> itemStorages) {
		this.itemStorages = itemStorages;
	}

	public List<MaterialHistory> getMaterialHistories() {
		return materialHistories;
	}

	public void setMaterialHistories(List<MaterialHistory> materialHistories) {
		this.materialHistories = materialHistories;
	}

	public List<ItemType> getItemTypes() {
		return itemTypes;
	}

	public void setItemTypes(List<ItemType> itemTypes) {
		this.itemTypes = itemTypes;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}
