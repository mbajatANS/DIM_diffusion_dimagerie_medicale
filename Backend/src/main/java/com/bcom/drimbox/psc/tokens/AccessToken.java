/*
 *  AccessToken.java - DRIMBox
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

package com.bcom.drimbox.psc.tokens;

import javax.json.JsonObject;

public class AccessToken extends PscToken{

	private String rawAccessToken;
	private Long authTime;
	private String acr;
	private Boolean emailVerified;
	private String preferredUsername;
	private String scope;

	public AccessToken(String token) {
		super(token);
		this.setRawAccessToken(token);
		JsonObject jsonObject = decodeJWTToJsonObject(token);
		this.acr = jsonObject.getString("acr", "");
		this.emailVerified = jsonObject.getBoolean("email_verified", false);
		this.preferredUsername = jsonObject.getString("preferred_username", "");
		this.authTime = jsonObject.getJsonNumber("auth_time").longValue();
		this.scope = jsonObject.getString("scope", "");
	}

	public Long getAuthTime() {
		return authTime;
	}

	public void setAuthTime(Long authTime) {
		this.authTime = authTime;
	}

	public String getAcr() {
		return acr;
	}

	public void setAcr(String acr) {
		this.acr = acr;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setemailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getPreferredUsername() {
		return preferredUsername;
	}

	public void setPreferredUsername(String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getRawAccessToken() {
		return rawAccessToken;
	}

	public void setRawAccessToken(String rawAccessToken) {
		this.rawAccessToken = rawAccessToken;
	}

}
