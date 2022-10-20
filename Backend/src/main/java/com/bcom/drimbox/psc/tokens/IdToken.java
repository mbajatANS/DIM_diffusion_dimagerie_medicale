/*
 *  IdToken.java - DRIMBox
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

public class IdToken extends PscToken {


	private String aud;
	private String atHash;
	private String subjectNameID;
	private Long authTime;
	private String acr;
	private Boolean emailVerified;
	private String preferredUsername;

	public IdToken(String token) {
		super(token);
		JsonObject jsonObject = decodeJWTToJsonObject(token);
		this.aud = jsonObject.getString("aud", "");
		this.atHash = jsonObject.getString("at_hash", "");
		this.subjectNameID = jsonObject.getString("family_name", "");
		this.authTime = jsonObject.getJsonNumber("auth_time").longValue();
		this.acr = jsonObject.getString("acr", "");
		this.emailVerified = jsonObject.getBoolean("email_verified", false);
		this.preferredUsername = jsonObject.getString("preferred_username", "");

	}

	public String getAud() {
		return aud;
	}

	public void setAud(String aud) {
		this.aud = aud;
	}

	public String getAtHash() {
		return atHash;
	}

	public void setAtHash(String atHash) {
		this.atHash = atHash;
	}

	public String getSubjectNameID() {
		return subjectNameID;
	}

	public void setSubjectNameID(String subjectNameID) {
		this.subjectNameID = subjectNameID;
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

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getPreferredUsername() {
		return preferredUsername;
	}

	public void setPreferredUsername(String preferredUsername) {
		this.preferredUsername = preferredUsername;
	}
}
