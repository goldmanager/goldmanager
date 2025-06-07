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
package com.my.goldmanager.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.my.goldmanager.service.AuthKeyInfoService;


/**
 * This Component Cleans up the expired Authentication Keys all 15 minutes;
 */
@Component
@Profile({ "default", "dev" })
public class CleanUpExpiredKeyIds {

	@Autowired
	private AuthKeyInfoService authKeyInfoService;

	@Scheduled(fixedRate = 1800000, initialDelay = 900000) // every 30 minutes
	@Profile({ "default", "dev" })
	public void doCleaUp() {

		authKeyInfoService.cleanUpExpiredKeyInfos();
	}
}
