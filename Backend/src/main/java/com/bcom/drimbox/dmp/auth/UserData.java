/*
 *  UserData.java - DRIMBox
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

import java.util.UUID;

import javax.json.JsonObject;

import com.bcom.drimbox.psc.tokens.AccessToken;
import com.bcom.drimbox.psc.tokens.IdToken;
import com.bcom.drimbox.psc.tokens.RefreshToken;

public class UserData {

	private AccessToken accessToken;
	private IdToken idToken;
	private RefreshToken refreshToken;
	private JsonObject userInfo;
	private String secteurActivite;
	private UUID nonce;
	private UUID state;

	public AccessToken getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(AccessToken accessToken) {
		this.accessToken = accessToken;
	}

	public IdToken getIdToken() {
		return idToken;
	}

	public void setIdToken(IdToken idToken) {
		this.idToken = idToken;
	}
	
	public RefreshToken getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(RefreshToken refreshToken) {
		this.refreshToken = refreshToken;
	} 

	public JsonObject getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(JsonObject userInfo) {
		this.userInfo = userInfo;
	}

	public String getSecteurActivite() {
		return secteurActivite;
	}

	public void setSecteurActivite(String secteurActivite) {
		this.secteurActivite = secteurActivite;
	}

	public UUID getNonce() {
		return nonce;
	}

	public void setNonce(UUID nonce) {
		this.nonce = nonce;
	}

	public UUID getState() {
		return state;
	}

	public void setState(UUID state) {
		this.state = state;
	}
}
