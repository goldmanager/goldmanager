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

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.my.goldmanager.entity.MaterialHistory;

public interface MaterialHistoryRepository extends JpaRepository<MaterialHistory, String> {
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId order by mh.entryDate desc")
	List<MaterialHistory> findByMaterial(@Param("materialId") String materialId);
	
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and (mh.entryDate BETWEEN :startDate and :endDate) order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialInRange(@Param("materialId") String materialId, @Param("startDate") Date startDate, @Param("endDate") Date endDate );
	

	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and mh.entryDate <= :startDate order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialStartAt(@Param("materialId") String materialId, @Param("startDate") Date startDate );
	
	@Query(value = "Select mh FROM MaterialHistory mh JOIN mh.material m  WHERE m.id = :materialId and mh.entryDate >=:endDate order by mh.entryDate desc")
	List<MaterialHistory> findByMaterialEndAt(@Param("materialId") String materialId, @Param("endDate") Date endDate );
}
