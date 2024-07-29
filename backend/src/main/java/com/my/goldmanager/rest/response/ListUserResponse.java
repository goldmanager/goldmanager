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
package com.my.goldmanager.rest.response;

import java.util.List;

import com.my.goldmanager.rest.entity.UserInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * UserResponse
 */
public class ListUserResponse {
 
	@Getter
	@Setter
	private List<UserInfo> userInfos;
	
}