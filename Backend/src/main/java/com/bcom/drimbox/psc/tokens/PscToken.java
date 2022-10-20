/*
 *  PscToken.java - DRIMBox
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class PscToken {
	protected Long exp;
	protected Long iat;
	protected String jti;
	protected String iss;
	protected String sub;
	protected String typ;
	protected String azp;
	protected String nonce;
	protected String sessionState;
	protected String sid;


	PscToken(String token){
		JsonObject jsonObject = decodeJWTToJsonObject(token);
		this.exp = jsonObject.getJsonNumber("exp").longValue();
		this.iat = jsonObject.getJsonNumber("iat").longValue();
		this.jti = jsonObject.getString("jti");
		this.iss = jsonObject.getString("iss");
		this.sub = jsonObject.getString("sub");
		this.typ = jsonObject.getString("typ");
		this.azp = jsonObject.getString("azp");
		this.nonce = jsonObject.getString("nonce");
		this.sessionState = jsonObject.getString("session_state");
		this.sid = jsonObject.getString("sid");
	}

	public Long getExp() {
		return exp;
	}
	public void setExp(Long exp) {
		this.exp = exp;
	}
	public Long getIat() {
		return iat;
	}
	public void setIat(Long iat) {
		this.iat = iat;
	}
	public String getJti() {
		return jti;
	}
	public void setJti(String jti) {
		this.jti = jti;
	}
	public String getIss() {
		return iss;
	}
	public void setIss(String iss) {
		this.iss = iss;
	}
	public String getSub() {
		return sub;
	}
	public void setSub(String sub) {
		this.sub = sub;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getAzp() {
		return azp;
	}
	public void setAzp(String azp) {
		this.azp = azp;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public String getSessionState() {
		return sessionState;
	}
	public void setSessionState(String sessionState) {
		this.sessionState = sessionState;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}

	public JsonObject decodeJWTToJsonObject(String token) {
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String[] chunks = token.split("\\.");
		String payload = new String(decoder.decode(chunks[1]));
		InputStream targetStream = new ByteArrayInputStream(payload.getBytes());
		JsonReader rdr = Json.createReader(targetStream);
		return rdr.readObject();
	}

}
