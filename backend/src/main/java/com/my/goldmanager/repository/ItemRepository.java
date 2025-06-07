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
package com.my.goldmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.my.goldmanager.entity.Item;

public interface ItemRepository extends JpaRepository<Item, String> {

	@Query(value = "Select item FROM Item item JOIN item.itemType it JOIN it.material m WHERE m.id = :materialId")
	List<Item> findByMaterialId(@Param("materialId") String materialId);

	@Query(value = "Select item FROM Item item JOIN item.itemStorage storage WHERE storage.id = :storageId")
	List<Item> findByItemStorageId(@Param("storageId") String storageId);
}
