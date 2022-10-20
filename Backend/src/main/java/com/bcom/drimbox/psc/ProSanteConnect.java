/*
 *  ProSanteConnect.java - DRIMBox
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

package com.bcom.drimbox.psc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.bcom.drimbox.psc.tokens.AccessToken;
import com.bcom.drimbox.psc.tokens.IdToken;
import com.bcom.drimbox.psc.tokens.RefreshToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.bcom.drimbox.dmp.auth.WebTokenAuth;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;

@Singleton
public class ProSanteConnect {
	@ConfigProperty(name="pcs.baseurl")
	String baseURL;

	@ConfigProperty(name = "quarkus.oidc.credentials.secret")
	String clientSecret;

	@ConfigProperty(name = "quarkus.oidc.client-id")
	String clientID;

	@Inject
	WebTokenAuth webTokenAuth;


	/**
	 * Get user info based on the accessToken
	 *
	 * You can check beforehand if the token is valid with ProSanteConnect.introspectToken
	 *
	 * @param rawAccessToken PCS raw accessToken
	 * @return The UserInfo associated with the AccessToken. If something went wrong with the token the object will contain an error field (e.g. "error" and "error_description"). If something else went wrong the return value can be empty.
	 */
	public JsonObject getUserInfo(String rawAccessToken) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(baseURL + "/userinfo");
			httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawAccessToken);

			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				try (InputStream is = entity.getContent()) {
					JsonReader rdr = Json.createReader(is);

					return rdr.readObject();
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return Json.createObjectBuilder().build();
	}
	/**
	 * Make an introspect request to the Pro Sante Connect endpoint
	 * @param rawAccessToken PSC raw accessToken
	 * @return True if token is valid, false otherwise
	 */
	public Boolean introspectToken(String rawAccessToken) {
		try {
			String encoding = Base64.getEncoder().encodeToString((clientID + ":" + clientSecret).getBytes());


			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(baseURL + "/token/introspect");
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
			List<NameValuePair> params = new ArrayList<>(1);
			params.add(new BasicNameValuePair("token", rawAccessToken));

			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				try (InputStream is = entity.getContent()) {
					JsonReader rdr = Json.createReader(is);
					JsonObject obj = rdr.readObject();

					return obj.getBoolean("active");
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return false;
	}


	/**
	 *  Create authentication token in the backend to re-use later
	 *
	 * @param code Code value send from PSC
	 * @param cookie Cookie ID of the client
	 *
	 * @return True if success, false otherwise
	 */
	public Boolean createAuthToken(String code, String cookie) {
		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(baseURL + "token");
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			List<NameValuePair> params = new ArrayList<>(2);
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			params.add(new BasicNameValuePair("redirect_uri", "https://localhost/api"));
			params.add(new BasicNameValuePair("client_id", clientID));
			params.add(new BasicNameValuePair("client_secret", clientSecret));
			params.add(new BasicNameValuePair("code", code));

			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				try (InputStream is = entity.getContent()) {
					JsonReader rdr = Json.createReader(is);
					JsonObject obj = rdr.readObject();

					String rawAccessToken = obj.getString("access_token", "");
					if (rawAccessToken.isEmpty()) {
						Log.error("Can't get access token from PSC");
						return false;
					}
					AccessToken accessToken = new AccessToken(rawAccessToken);

					String rawIDToken = obj.getString("id_token", "");
					IdToken idToken = new IdToken(rawIDToken);
					if (rawIDToken.isEmpty()) {
						Log.error("Can't get ID token from PSC");
						return false;
					}

					String rawRefreshToken = obj.getString("refresh_token", "");
					RefreshToken refreshToken = new RefreshToken(rawRefreshToken);
					if (rawRefreshToken.isEmpty()) {
						Log.error("Can't get refresh token from PSC");
						return false;
					}

					String nonce = webTokenAuth.getUsersMap().get(cookie).getNonce().toString();

					if(nonce.equals(accessToken.getNonce()) && nonce.equals(idToken.getNonce())) {
						webTokenAuth.getUsersMap().get(cookie).setAccessToken(accessToken);
						webTokenAuth.getUsersMap().get(cookie).setIdToken(idToken);
						webTokenAuth.getUsersMap().get(cookie).setRefreshToken(refreshToken);
						webTokenAuth.getUsersMap().get(cookie).setUserInfo(getUserInfo(rawAccessToken));
						webTokenAuth.getUsersMap().get(cookie).setSecteurActivite("empty");
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return false;
	}
}
