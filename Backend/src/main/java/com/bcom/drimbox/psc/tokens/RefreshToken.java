/*
 *  RefreshToken.java - DRIMBox
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

public class RefreshToken extends PscToken {
	private String aud;
	private String scope;

	public RefreshToken(String token) {
		super(token);
		JsonObject jsonObject = decodeJWTToJsonObject(token);

		this.aud = jsonObject.getString("aud");
		this.scope = jsonObject.getString("scope");
	}

	public String getAud() {
		return aud;
	}
	public void setAud(String aud) {
		this.aud = aud;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
}
