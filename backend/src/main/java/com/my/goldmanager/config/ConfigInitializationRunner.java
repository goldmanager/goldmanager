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
package com.my.goldmanager.config;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.my.goldmanager.entity.Material;
import com.my.goldmanager.entity.Unit;
import com.my.goldmanager.pricecollector.MetalPriceCollector;
import com.my.goldmanager.service.MaterialService;
import com.my.goldmanager.service.UnitService;
import com.my.goldmanager.service.UserService;

@Component
@Profile({ "default", "dev" })
public class ConfigInitializationRunner implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(ConfigInitializationRunner.class);
	public static final String ENV_DEFAULTUSER = "APP_DEFAULT_USER";
	public static final String ENV_DEFAULTPASSWORD = "APP_DEFAULT_PASSWORD";

	@Autowired
	private UserService userService;
	@Autowired
	private MaterialService materialService;
	@Autowired
	private UnitService unitService;

	@Autowired(required = false)
	private MetalPriceCollector metalsMetalApisPriceCollector;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String defaultUser = SystemEnvUtil.readVariable(ENV_DEFAULTUSER);
		String defaultPassword = SystemEnvUtil.readVariable(ENV_DEFAULTPASSWORD);
		if (defaultUser != null && defaultPassword != null) {
			if (userService.countUsers() > 0) {
				logger.info("Users existing, skipping creation of default user");
			} else {
				logger.info("Creating default user {}...", defaultUser);
				userService.create(defaultUser, defaultPassword);
				logger.info("Created default user {}.", defaultUser);
			}
		}
		if (materialService.list().isEmpty()) {
			Material material = new Material();
			material.setName("Gold");
			material.setPrice(1);
			material.setEntryDate(new Date());
			materialService.store(material);
			logger.info("Created initial Material 'Gold'");
		} else {
			logger.info("Materials existing. Skipping creation of material 'Gold'");
		}

		if (unitService.listAll().isEmpty()) {
			Unit unit = new Unit();
			unit.setFactor(1);
			unit.setName("Oz");
			unitService.save(unit);
			logger.info("Created initial Unit 'Oz'");
		} else {
			logger.info("Units existing. Skipping creation of unit 'Oz'");
		}

		if (metalsMetalApisPriceCollector != null) {
			metalsMetalApisPriceCollector.init();
		}

	}

}
