package com.my.goldmanager.service.dataexpimp;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;

import com.my.goldmanager.entity.Item;
import com.my.goldmanager.entity.ItemStorage;
import com.my.goldmanager.entity.ItemType;
import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.MaterialHistory;
import com.my.goldmanager.entity.TestData;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.service.entity.ExportEntities;

public class ExportDataAssertions {
	private ExportDataAssertions() {

	}

	public static void assertEquals(TestData expected, ExportEntities actual, boolean expectSimplyfied) {
		assertMaterials(expected.getMetals(), actual.getMetals());
		assertMaterialHistories(expected.getMaterialHistories(), actual.getMaterialHistories(), expectSimplyfied);
		assertUnits(expected.getUnits(), actual.getUnits());
		assertItemStorages(expected.getItemStorages(), actual.getItemStorages());
		assertItemTypes(expected.getItemTypes(), actual.getItemTypes(), expectSimplyfied);
		assertItems(expected.getItems(), actual.getItems(), expectSimplyfied);
	}

	public static void assertMaterials(List<Material> expected, List<Material> actual) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (Material expectedMaterial : expected) {
			Optional<Material> found = actual.stream().filter(m -> m.getId().equals(expectedMaterial.getId()))
					.distinct().findFirst();
			Assertions.assertTrue(found.isPresent(), "No matching Material found for id " + expectedMaterial.getId());
			Material actualMaterial = found.get();
			Assertions.assertEquals(expectedMaterial.getPrice(), actualMaterial.getPrice());
			Assertions.assertEquals(expectedMaterial.getName(), actualMaterial.getName());
			Assertions.assertEquals(expectedMaterial.getEntryDate().getTime(), actualMaterial.getEntryDate().getTime());
		}
	}

	public static void assertItemTypes(List<ItemType> expected, List<ItemType> actual, boolean expectSimplyfied) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (ItemType expectedEntity : expected) {
			Optional<ItemType> found = actual.stream().filter(item -> item.getId().equals(expectedEntity.getId()))
					.distinct().findFirst();
			Assertions.assertTrue(found.isPresent(), "No matching ItemType found for id " + expectedEntity.getId());

			ItemType actualEntity = found.get();
			Assertions.assertEquals(expectedEntity.getModifier(), actualEntity.getModifier());
			Assertions.assertEquals(expectedEntity.getName(), actualEntity.getName());
			Assertions.assertEquals(expectedEntity.getMaterial().getId(), actualEntity.getMaterial().getId());

			if (expectSimplyfied) {
				Assertions.assertNull(actualEntity.getMaterial().getName(), "Expected simplified Material name");
			}
		}
	}

	public static void assertItems(List<Item> expected, List<Item> actual, boolean expectSimplyfied) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (Item expectedEntity : expected) {
			Optional<Item> found = actual.stream().filter(item -> item.getId().equals(expectedEntity.getId()))
					.distinct().findFirst();
			Assertions.assertTrue(found.isPresent(), "No matching Item found for id " + expectedEntity.getId());

			Item actualEntity = found.get();
			Assertions.assertEquals(expectedEntity.getAmount(), actualEntity.getAmount());
			Assertions.assertEquals(expectedEntity.getItemCount(), actualEntity.getItemCount());
			Assertions.assertEquals(expectedEntity.getName(), actualEntity.getName());
			if (expectedEntity.getItemStorage() == null) {
				Assertions.assertNull(actualEntity.getItemStorage());
			} else {
				Assertions.assertEquals(expectedEntity.getItemStorage().getId(), actualEntity.getItemStorage().getId());
				if (expectSimplyfied) {
					Assertions.assertNull(actualEntity.getItemStorage().getName(),
							"Expected simplified ItemStorage name");
					Assertions.assertNull(actualEntity.getItemStorage().getDescription(),
							"Expected simplified ItemStorage description");
				}
			}
			Assertions.assertEquals(expectedEntity.getUnit().getName(), actualEntity.getUnit().getName());
			Assertions.assertEquals(expectedEntity.getItemType().getId(), actualEntity.getItemType().getId());
			if (expectSimplyfied) {
				Assertions.assertNull(actualEntity.getItemType().getMaterial(),
						"Expected simplified ItemType Material");
				Assertions.assertNull(actualEntity.getItemType().getName(), "Expected simplified ItemType Name");
			}
		}
	}

	public static void assertUnits(List<Unit> expected, List<Unit> actual) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (Unit expectedEntity : expected) {
			Optional<Unit> found = actual.stream().filter(entity -> entity.getName().equals(expectedEntity.getName()))
					.distinct().findFirst();
			Assertions.assertTrue(found.isPresent(), "No matching Unit found for " + expectedEntity.getName());
			Unit actualEntity = found.get();
			Assertions.assertEquals(expectedEntity.getFactor(), actualEntity.getFactor());
		}
	}

	public static void assertItemStorages(List<ItemStorage> expected, List<ItemStorage> actual) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (ItemStorage expectedEntity : expected) {
			Optional<ItemStorage> found = actual.stream()
					.filter(entity -> entity.getName().equals(expectedEntity.getName())).distinct().findFirst();
			Assertions.assertTrue(found.isPresent(), "No matching ItemStorage found for " + expectedEntity.getName());
			ItemStorage actualEntity = found.get();
			Assertions.assertEquals(expectedEntity.getName(), actualEntity.getName());
			Assertions.assertEquals(expectedEntity.getDescription(), actualEntity.getDescription());
		}
	}

	public static void assertMaterialHistories(List<MaterialHistory> expected, List<MaterialHistory> actual,
			boolean expectSimplyfied) {
		Assertions.assertEquals(expected.size(), actual.size());
		for (MaterialHistory expectedMaterialHistory : expected) {
			Optional<MaterialHistory> found = actual.stream()
					.filter(m -> m.getId().equals(expectedMaterialHistory.getId())).distinct().findFirst();
			Assertions.assertTrue(found.isPresent(),
					"No matching MaterialHistory found for id " + expectedMaterialHistory.getId());
			MaterialHistory actualMaterialHistory = found.get();
			Assertions.assertEquals(expectedMaterialHistory.getPrice(), actualMaterialHistory.getPrice());
			Assertions.assertEquals(expectedMaterialHistory.getMaterial().getId(),
					actualMaterialHistory.getMaterial().getId());
			Assertions.assertEquals(expectedMaterialHistory.getEntryDate().getTime(),
					actualMaterialHistory.getEntryDate().getTime());
			if (expectSimplyfied) {
				Assertions.assertNull(actualMaterialHistory.getMaterial().getName(),
						"Expected simplified Material name");
			}
		}
	}

}
