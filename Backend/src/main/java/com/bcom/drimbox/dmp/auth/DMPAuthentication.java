/*
 *  DMPAuthentication.java - DRIMBox
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

package com.bcom.drimbox.dmp.auth;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bcom.drimbox.psc.ProSanteConnect;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api")
public class DMPAuthentication {

	@Inject
    ProSanteConnect proSanteConnect;

	@Inject
	WebTokenAuth webTokenAuth;

	@ConfigProperty(name = "quarkus.oidc.client-id")
	String clientID;

	@ConfigProperty(name="quarkus.oidc.authorization-path")
	String baseURL;

	@ConfigProperty(name="quarkus.oidc.authentication.scopes")
	String scopes;

	@ConfigProperty(name="quarkus.oidc.authentication.extra-params.acr_values")
	String acrValues;

	@ConfigProperty(name="ris.host")
	String risHost;

	/**
	 * Redirect to RIS page after PSC authentication
	 *
	 * @param code Code value sent from PSC to authenticate
	 * @param cookieSession Cookie sent from front app
	 * @param state Query param sent from PSC (used for security)
	 * @return 301 redirection
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getLandingPage(@QueryParam("code") String code, @CookieParam("SessionToken") Cookie cookieSession, @QueryParam("state") String state) {

		if(cookieSession != null && webTokenAuth.getUsersMap().containsKey(cookieSession.getName())) {
			String cookieName = cookieSession.getName();
			if(code != null && state != null && state.equals(webTokenAuth.getUsersMap().get(cookieName).getState().toString())) {
				Boolean tokenCreated = proSanteConnect.createAuthToken(code, cookieName);

				if(!tokenCreated && webTokenAuth.getUsersMap().containsKey(cookieSession.getName()))
					webTokenAuth.getUsersMap().remove(cookieSession.getName());
			}
		}
		try {
			URI uri = new URI(risHost + "/ris");
			return Response.temporaryRedirect(uri).build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}


	/**
	 * Check if user is already connected or need to be redirected to PSC
	 *
	 * @param cookieSession Cookie sent from front app
	 * @return 200 ok with connected state
	 * @throws Exception
	 */
	@Path("/auth")
	@GET
	public Response authCheck(@CookieParam("SessionToken") Cookie cookieSession, @QueryParam("ins") String ins) throws Exception {
		if (cookieSession == null)
			return Response.serverError().build();

		if(webTokenAuth.getUsersMap().containsKey(cookieSession.getName())) {
			JsonObject temp = webTokenAuth.getUsersMap().get(cookieSession.getName()).getUserInfo().getJsonObject("SubjectRefPro").getJsonArray("exercices").getJsonObject(0);
			String name = temp.getString("prenomDexercice") + " " + temp.getString("nomDexercice");
			Long epoch = webTokenAuth.getUsersMap().get(cookieSession.getName()).getAccessToken().getAuthTime();
			String date = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date (epoch*1000));

			if(webTokenAuth.getUsersMap().get(cookieSession.getName()).getSecteurActivite() != "empty")
				return Response.ok("connected").build();

			else
				return Response.ok("connected but no structure : &" + name + "&" + date).build();
		}
		else {
			var responseType = "code";
			UUID state = UUID.randomUUID();
			var nonce = UUID.randomUUID();

			UserData authentServ = new UserData();
			authentServ.setNonce(nonce);
			authentServ.setState(state);
			webTokenAuth.getUsersMap().put(cookieSession.getName(), authentServ);

			String redirectURI = risHost + "/api";
			var url = baseURL + "?response_type=" + responseType + "&client_id=" + clientID + "&redirect_uri=" + redirectURI + "&scope=openid " + scopes + "&acr_values=" + acrValues + "&state=" + state + "&nonce=" + nonce;
			return Response.ok("no connected : " + url).build();

		}
	}
}







