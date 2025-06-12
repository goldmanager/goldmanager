package com.my.goldmanager.entity.util;

import java.util.List;
import java.util.Map;

public class TestDataConfig {

	private int itemStorageCount = 100;
	private int historySize = 3000;
	private List<Float> defaultOzSizes = List.of(0.25f, 0.5f, 1f, 1.5f, 2.0f);
	private List<Float> defaultGrammSizes = List.of(10f, 1f, 5f, 20f, 50f, 100f);
	private Map<String, String> userLogins = Map.of("user1", "Test1245", "user2", "Testagaghgha6677");

	public int getItemStorageCount() {
		return itemStorageCount;
	}

	public void setItemStorageCount(int itemStorageCount) {
		this.itemStorageCount = itemStorageCount;
	}

	public int getHistorySize() {
		return historySize;
	}

	public void setHistorySize(int historySize) {
		this.historySize = historySize;
	}

	public List<Float> getDefaultOzSizes() {
		return defaultOzSizes;
	}

	public void setDefaultOzSizes(List<Float> defaultOzSizes) {
		this.defaultOzSizes = defaultOzSizes;
	}

	public List<Float> getDefaultGrammSizes() {
		return defaultGrammSizes;
	}

	public void setDefaultGrammSizes(List<Float> defaultGrammSizes) {
		this.defaultGrammSizes = defaultGrammSizes;
	}

	public Map<String, String> getUserLogins() {
		return userLogins;
	}

	public void setUserLogins(Map<String, String> userLogins) {
		this.userLogins = userLogins;
	}


}
