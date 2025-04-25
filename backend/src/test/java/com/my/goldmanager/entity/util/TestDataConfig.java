package com.my.goldmanager.entity.util;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class TestDataConfig {
	@Getter
	@Setter
	private int itemStorageCount = 100;
	@Getter
	@Setter
	private int historySize = 3000;
	@Getter
	@Setter
	private List<Float> defaultOzSizes = List.of(0.25f, 0.5f, 1f, 1.5f, 2.0f);
	@Getter
	@Setter
	private List<Float> defaultGrammSizes = List.of(10f, 1f, 5f, 20f, 50f, 100f);
	@Getter
	@Setter
	private Map<String, String> userLogins = Map.of("user1", "Test1245", "user2", "Testagaghgha6677");


}
