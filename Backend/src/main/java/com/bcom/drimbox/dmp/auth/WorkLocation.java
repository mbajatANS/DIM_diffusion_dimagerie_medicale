/*
 *  WorkLocation.java - DRIMBox
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

		if(cookieSession != null && webTokenAuth.getUsersMap().containsKey(cookieSession.getName())) {
			String sectActivite;
			JsonArray jArr = webTokenAuth.getUsersMap().get(cookieSession.getName()).getUserInfo().getJsonObject("SubjectRefPro").getJsonArray("exercices").getJsonObject(0).getJsonArray("activities");
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

		if(cookieSession != null && webTokenAuth.getUsersMap().containsKey(cookieSession.getName())) {		
			webTokenAuth.getUsersMap().get(cookieSession.getName()).setSecteurActivite(workLocation);
			return Response.ok("Success").build();
		}

		else return Response.status(401).build();
	}
}
