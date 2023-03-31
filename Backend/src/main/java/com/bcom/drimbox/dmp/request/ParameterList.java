
/*
 *  ParameterList.java - DRIMBox
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

package com.bcom.drimbox.dmp.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.bcom.drimbox.dmp.auth.WebTokenAuth;


@Path("/parameters")
@Singleton
public class ParameterList {

	// Map cache with uuid and query parameters associated 
	private final Map<String, Map<String, String>> paramsCache = new HashMap<>();

	// Map of query parameters
	private final Map<String, String> queryParams = new HashMap<>();
 
	@Inject
	WebTokenAuth webTokenAuth;

	/**
	 *  Retrieve params from RIS and adding them to the cache with a uuid 
	 * 
	 * @param requestBody list of params from RIS
	 * @return response 301 to redirect user to frontend
	 * @throws Exception
	 */
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response echo(String requestBody) throws Exception {

		queryParams.clear();
		UUID uuid = UUID.randomUUID();

		// Retrieving query parameters from url
		String[] pairs = requestBody.split("&");
		for (String pair : pairs) {
			String[] keyValuePair = pair.split("=");
			// Adding query params in local map
			this.queryParams.put(keyValuePair[0], keyValuePair[1]);
		}
		// Adding local map with uuid in Cache map
		this.paramsCache.put(uuid.toString(), this.queryParams);

		// Redirect to viewer if studyinstanceUIDs in query params
		if(this.queryParams.containsKey("studyInstanceUID")) {
			String drimboxConso = "localhost:8081";
			String drimboxSource = "localhost:8082";
			String viewerURL = "localhost:3000";
			return Response.ok("http://"+viewerURL+"/viewer?url=http://"+drimboxConso+"/ohifmetadata/"+drimboxSource+"&studyInstanceUIDs=" + this.queryParams.get("studyInstanceUID")).build();
		}		
		else
		{
			return Response //seeOther = 303 redirect
					.seeOther(UriBuilder.fromUri("http://localhost:4200")
							.queryParam("uuid", uuid.toString())
							.build())//build the URL where you want to redirect
					.build();
		}	
	}

	/**
	 * Return ins associated with uuid
	 * 
	 * @param uuid retrieve from url param
	 * @return ins 
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/ins")
	public Response retrieveINS(@QueryParam("uuid") String uuid) {
		if (this.paramsCache.containsKey(uuid)) {
			return Response.ok(this.paramsCache.get(uuid).get("ins")).build();
		}

		else return Response.status(401).build();
	}

	/**
	 * Return consent associated with uuid
	 * 
	 * @param uuid retrieve from url param
	 * @return consent 
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/consent")
	public Response retrieveConsent(@QueryParam("uuid") String uuid) {
		if (this.paramsCache.containsKey(uuid)) {
			return Response.ok(this.paramsCache.get(uuid).get("consent")).build();
		}

		else return Response.status(401).build();
	}

	/**
	 * Return list of filters associated with uuid
	 * 
	 * @param uuid retrieve from url param
	 * @return modalities, anatomic regions and studyDate if present in cache 
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/filter")
	public Response retrieveFilter(@QueryParam("uuid") String uuid) {

		if (this.paramsCache.containsKey(uuid)) {

			String response = "";

			if(this.paramsCache.get(uuid).containsKey("modality")) {
				response += "modality=" + this.paramsCache.get(uuid).get("modality") + "/";
			}
			if(this.paramsCache.get(uuid).containsKey("anatomicRegion")) {
				response += "anatomicRegion=" + this.paramsCache.get(uuid).get("anatomicRegion") + "/";
			}
			if(this.paramsCache.get(uuid).containsKey("studyDate")) {
				response += "studyDate=" + this.paramsCache.get(uuid).get("studyDate") + "/";
			}
			if(this.paramsCache.get(uuid).containsKey("accessionNumber")) {
				response += "accessionNumber=" + this.paramsCache.get(uuid).get("accessionNumber") + "/";
			}
			return Response.ok(response).build();
		}
		else return Response.status(401).build();
	}
	
	/**
	 * Return consent associated with uuid
	 * 
	 * @param uuid retrieve from url param
	 * @return consent 
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/situation")
	public Response retrieveSituation(@CookieParam("SessionToken") Cookie cookieSession, @QueryParam("uuid") String uuid) {
		if (this.paramsCache.containsKey(uuid) && (!Objects.equals(webTokenAuth.getSecteurActivite(cookieSession.getValue()), "empty") || this.paramsCache.get(uuid).containsKey("situation"))) {
			return Response.ok("not empty").build();
		}

		else return Response.ok("empty").build();
	}

	/**
	 * Return situation associated with uuid
	 * 
	 * @param uuid
	 * @return situation if existed, else 'empty' 
	 */
	public String getSituation(String uuid) {
		return this.paramsCache.get(uuid).getOrDefault("situation", "empty");
	}
}













