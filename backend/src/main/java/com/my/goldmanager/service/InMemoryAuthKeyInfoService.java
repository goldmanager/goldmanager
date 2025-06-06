/** Copyright 2024 fg12111

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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.my.goldmanager.service.entity.KeyInfo;

import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

@Service
public class InMemoryAuthKeyInfoService implements AuthKeyInfoService {

	@AllArgsConstructor
	private static final class KeyWrapper {
		Key key;
		String userName;
		Date refreshAfter;
		Date expiresAfter;
	}

	private final ConcurrentHashMap<String, KeyWrapper> secretKeys = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, List<String>> userKeyIDMap = new ConcurrentHashMap<>();

	@Value("${com.my.goldmanager.auth.keyvalidity:3600000}") // Default value: 1h
	private long keyValidity;
	@Value("${com.my.goldmanager.auth.keyexpiration:7201000}") // Default value: 7201s = 2h 1s
	private long keyExpiration; // KeyExpiration after end of validity should be at least as long as the JWT
								// Token validity

	@Override
	public KeyInfo getKeyInfoForUserName(String username) {
		synchronized (secretKeys) {

			String keyId = getKeyIdsForUserName(username);
			KeyWrapper secretKey = null;
			if (keyId != null) {
				secretKey = secretKeys.get(keyId);
			}

			if (secretKey == null || new Date().after(secretKey.refreshAfter)) {

				keyId = UUID.randomUUID().toString();
				secretKey = generateNewKey(username);
				secretKeys.put(keyId, secretKey);
				addKeyIdForUserName(username, keyId);
			}

			return new KeyInfo(secretKey.key, username, keyId);

		}

	}

	private KeyWrapper generateNewKey(String username) {
		KeyWrapper secretKey;
		Key newey = Jwts.SIG.HS256.key().build();
		long currentDate = System.currentTimeMillis();
		secretKey = new KeyWrapper(newey, username, new Date(currentDate + keyValidity),
				new Date(currentDate + keyValidity + keyExpiration));
		return secretKey;
	}

	private String getKeyIdsForUserName(String username) {
		synchronized (userKeyIDMap) {
			List<String> keyIds = userKeyIDMap.get(username);
			if (keyIds != null && !keyIds.isEmpty()) {
				return keyIds.getFirst();
			}
			return null;

		}

	}

	private void addKeyIdForUserName(String username, String keyId) {
		synchronized (userKeyIDMap) {
			List<String> keyIds = userKeyIDMap.get(username);

			if (keyIds == null) {

				keyIds = new LinkedList<String>();

				userKeyIDMap.put(username, new LinkedList<String>());

			}
			userKeyIDMap.get(username).addFirst(keyId);
		}

	}

	private void removeKeyIdForUserName(String username, String keyId) {
		synchronized (userKeyIDMap) {
			List<String> keyIds = userKeyIDMap.get(username);

			if (keyIds != null) {

				keyIds.remove(keyId);
			}

		}

	}

	@Override
	public Key getKeyforKeyId(String keyId) {

		KeyWrapper result = secretKeys.get(keyId);
		return result != null ? result.key : null;
	}

	@Override
	public void removeKeyInfosForUserName(String username) {
		synchronized (secretKeys) {
			List<String> keyIds = userKeyIDMap.get(username);
			if (keyIds != null) {
				keyIds.stream().forEach(keyId -> secretKeys.remove(keyId));
			}
			userKeyIDMap.remove(username);
		}

	}

	@Override
	public void clearAll() {
		secretKeys.clear();
		userKeyIDMap.clear();

	}

	@Override
	public void invalidateKeyForUsername(String username) {

		synchronized (secretKeys) {
			List<String> keyIds = userKeyIDMap.get(username);
			if (keyIds != null && !keyIds.isEmpty()) {
				if (secretKeys.get(keyIds.getFirst()) != null) {
					KeyWrapper key = secretKeys.get(keyIds.getFirst());
					key.refreshAfter = new Date(System.currentTimeMillis() - 1);
					secretKeys.put(keyIds.getFirst(), key);
				}
			}
		}

	}

	@Override
	public void cleanUpExpiredKeyInfos() {
		synchronized (secretKeys) {
			final Date expirationDate = new Date(System.currentTimeMillis());
			List<Entry<String, KeyWrapper>> forRemoval = secretKeys.entrySet().stream()
					.filter(entry -> entry.getValue().expiresAfter.before(expirationDate)).collect(Collectors.toList());

                        forRemoval.stream().forEach(entry -> {
                                secretKeys.remove(entry.getKey());
                                removeKeyIdForUserName(entry.getValue().userName, entry.getKey());
                        });
                }

	}

	/**
	 * Set Key validity. Package visibility intended for junit testing
	 * 
	 * @param keyValidity
	 */
	void setKeyValidity(long keyValidity) {
		this.keyValidity = keyValidity;

	}

	/**
	 * Set Key expiration. Package visibility intended for junit testing
	 * 
	 * @param keyExpiration
	 */
	void setKeyExpiration(long keyExpiration) {
		this.keyExpiration = keyExpiration;
	}

}
