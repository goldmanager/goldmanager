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
package com.my.goldmanager.service;

import java.security.Key;

import com.my.goldmanager.service.entity.KeyInfo;

/**
 * Service interface providing secret Keys for Authentication
 */
public interface AuthKeyInfoService {


	/**
	 * Returns a {@link KeyInfo} for specified username
	 * 
	 * @param username
	 * @return
	 */
	KeyInfo getKeyInfoForUserName(String username);


	/**
	 * Returns a {@link Key} for specified KeyId
	 * 
	 * @param keyId
	 * @return
	 */
	Key getKeyforKeyId(String keyId);


	/**
	 * Forces the private key or specified username to be regenerated on next call
	 * of {@link AuthKeyInfoService#getKeyInfoForUserName(String)}.
	 * 
	 * @param username
	 */
	void invalidateKeyForUsername(String username);

	/**
	 * Remove all KeyInfos
	 */
	void clearAll();

	/**
	 * Remove KeyInfos for user
	 * 
	 * @param username
	 */
	void removeKeyInfosForUserName(String username);

	/**
	 * Clean up expired KeyInfos
	 */
	void cleanUpExpiredKeyInfos();
}
