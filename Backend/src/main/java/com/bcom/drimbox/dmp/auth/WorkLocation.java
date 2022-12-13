/*
 *  WorkLocation.java - DRIMBox
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

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

@Path("/api")
public class WorkLocation {

	@Inject
	WebTokenAuth webTokenAuth;

	/**
	 * Retrieve all work locations
	 */
	@Path("/locations")
	@GET
	public Response retrieveWorkLocations(@CookieParam("SessionToken") Cookie cookieSession) throws Exception {

		if(cookieSession != null && webTokenAuth.clientRegistered(cookieSession.getValue())) {
			String sectActivite;
			JsonArray jArr = webTokenAuth.getUserInfo(cookieSession.getValue()).getJsonObject("SubjectRefPro").getJsonArray("exercices").getJsonObject(0).getJsonArray("activities");
			StringBuilder sectActiviteBuilder = new StringBuilder();
			for (int i = 0; i < jArr.size(); i++) {
				sectActiviteBuilder.append("/").append(jArr.getJsonObject(i).getString("raisonSocialeSite"));
			}
			sectActivite = sectActiviteBuilder.toString();
			sectActivite = sectActivite.replaceFirst("/", "");
			return Response.ok(sectActivite).build();
		}

		else return Response.status(401).build();
	}

	/**
	 * Set selected location
	 */
	@GET
	@Path("/location")
	public Response setWorkLocation(@QueryParam("workLocation") String workLocation, @CookieParam("SessionToken") Cookie cookieSession) {

		if(cookieSession != null && webTokenAuth.setSecteurActivite(cookieSession.getValue(), workLocation)) {
			return Response.ok("Success").build();
		}

		else return Response.status(401).build();
	}
}
