/*
 *  WebTokenAuth.java - DRIMBox
 *  Copyright 2022 b<>com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bcom.drimbox.dmp.auth;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class WebTokenAuth {

	private Map<String, UserData> usersMap;

	WebTokenAuth(){
		this.setUsersMap(new HashMap<>());
	}

	public Map<String, UserData> getUsersMap() {
		return usersMap;
	}

	public void setUsersMap(Map<String, UserData> usersMap) {
		this.usersMap = usersMap;
	}
}
