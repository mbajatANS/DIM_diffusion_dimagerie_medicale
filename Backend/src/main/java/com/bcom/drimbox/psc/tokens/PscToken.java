/*
 *  PscToken.java - DRIMBox
 *
 * N°IDDN : IDDN.FR.001.020012.000.S.C.2023.000.30000
 *
 * MIT License
 *
 * Copyright (c) 2022 b<>com
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bcom.drimbox.psc.tokens;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

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
