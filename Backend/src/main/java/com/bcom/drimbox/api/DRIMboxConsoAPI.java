/*
 *  DRIMboxConsoAPI.java - DRIMBox
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

package com.bcom.drimbox.api;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import com.bcom.drimbox.dmp.auth.WebTokenAuth;
import com.bcom.drimbox.pacs.PacsCache;
import com.bcom.drimbox.utils.RequestHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Future;

import static com.bcom.drimbox.utils.PrefixConstants.*;


@Path("/")
public class DRIMboxConsoAPI {

	@Inject
	WebTokenAuth webTokenAuth;

	@Inject
	RequestHelper requestHelper;


	/**
	 * Http protocol (may be changed later to https://)
	 */
	public static final String HTTP_PROTOCOL = "http://";

	@Inject
	PacsCache pacsCache;

	/**
	 * This will contain the cookieID in the form of "Bearer cookieID"
	 */
	@HeaderParam("Authorization")
	@DefaultValue("")
	String authHeader;

	@ConfigProperty(name = "debug.noAuth", defaultValue="false")
	Boolean noAuth;


	@GET
	@Path("wado/{drimboxSourceURL}")
	@Produces("application/dicom")
	public Uni<RestResponse<byte[]>> wadoRequest(String drimboxSourceURL, @Context UriInfo uriInfo) {

		if (!checkAuthorization())
			return Uni.createFrom().item(requestHelper.getDeniedFileResponse());

		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
		String studyUID = params.get("studyUID").get(0);
		String seriesUID = params.get("seriesUID").get(0);
		String instanceUID = params.get("objectUID").get(0);

		Log.info("[WADO] Request : " + instanceUID);

		try {
			Future<byte[]> future = pacsCache.getDicomFile(studyUID, seriesUID, instanceUID);
			return Uni.createFrom().future(future).onItem().transform(
					item -> {
						Log.info("[WADO] Response : " + instanceUID);
						return RestResponse.ResponseBuilder.ok(item).build();
					}
					);
		} catch (Exception e) {
			// TODO : Is this really fatal ?
			Log.fatal("Can't get file from cache");
			return Uni.createFrom().item(requestHelper.getDeniedFileResponse());
		}
	}

	@GET
	@Path("rs/{drimboxSourceURL}/studies/{studyUID}")
	public RestResponse<byte[]> studyRequest(String drimboxSourceURL, String studyUID) {
		return seriesRequest(drimboxSourceURL, studyUID, "");
	}


	@GET
	@Path("rs/{drimboxSourceURL}/studies/{studyUID}/series/{seriesUID}")
	//@Produces(MediaType.MULTIPART_FORM_DATA)
	public RestResponse<byte[]> seriesRequest(String drimboxSourceURL, String studyUID, String seriesUID) {
		String url = HTTP_PROTOCOL + drimboxSourceURL + "/" + DRIMBOX_PREFIX + "/" + STUDIES_PREFIX + "/" + studyUID;
		if (!seriesUID.isEmpty()) {
			url += "/series/" + seriesUID;
		}

		return requestHelper.fileRequest(url, this::getDrimboxConnection);
	}

	@GET
	@Path("rs/{drimboxSourceURL}/studies/{studyUID}/series")
	@Produces("application/dicom+json")
	public RestResponse<String> seriesListRequest(String drimboxSourceURL, String studyUID) {
		String url = HTTP_PROTOCOL + drimboxSourceURL + "/" + DRIMBOX_PREFIX + "/" + STUDIES_PREFIX + "/" + studyUID + "/" + SERIES_PREFIX;
		return requestHelper.stringRequest(url, this::getDrimboxConnection);
	}

	@GET
	@Path("rs/{drimboxSourceURL}/studies/{studyUID}/series/{seriesUID}/metadata")
	@Produces("application/dicom+json")
	public RestResponse<String> metadataRequest(String drimboxSourceURL, String studyUID, String seriesUID) {
		String url = HTTP_PROTOCOL + drimboxSourceURL + "/" + DRIMBOX_PREFIX + "/" + STUDIES_PREFIX + "/" + studyUID;
		if (!seriesUID.isEmpty()) {
			url += "/series/" + seriesUID;
		}

		// This is non-blocking operation
		pacsCache.addNewEntry(drimboxSourceURL, studyUID, seriesUID);

		return requestHelper.stringRequest(url + "/" + METADATA_PREFIX, this::getDrimboxConnection);
	}

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("ohifmetadata/{drimboxSourceUrl}")
	public String getOHIFMetadata(@Context UriInfo uriInfo, String drimboxSourceUrl) {

		JsonObjectBuilder root = Json.createObjectBuilder();
		JsonObjectBuilder servers = Json.createObjectBuilder();
		JsonArrayBuilder dicomWebArray = Json.createArrayBuilder();
		JsonObjectBuilder dicomWebObject =  Json.createObjectBuilder();

		dicomWebObject.add("name", "DCM4CHEE");

		dicomWebObject.add("wadoUriRoot", uriInfo.getBaseUri() + "wado/" + drimboxSourceUrl);
		dicomWebObject.add("wadoRoot", uriInfo.getBaseUri() + "rs/" + drimboxSourceUrl);
		dicomWebObject.add("qidoRoot", uriInfo.getBaseUri() + "rs/" + drimboxSourceUrl);

		// Those are here for debug purpose in case something is wrong with the forwarding
		//        dicomWebObject.add("wadoUriRoot", "http://localhost:8012/dcm4chee-arc/aets/AS_RECEIVED/wado");
		//        dicomWebObject.add("qidoRoot", "http://localhost:8012/dcm4chee-arc/aets/AS_RECEIVED/rs");
		//        dicomWebObject.add("wadoRoot", "http://localhost:8012/dcm4chee-arc/aets/AS_RECEIVED/rs");

		// Todo : Test wadoRS for imageRendering
		dicomWebObject.add("imageRendering", "wadouri");
		dicomWebObject.add("thumbnailRendering", "wadouri");
		dicomWebObject.add("enableStudyLazyLoad", true);
		dicomWebObject.add("qidoSupportsIncludeField", true);

		dicomWebArray.add(dicomWebObject);
		servers.add("dicomWeb", dicomWebArray);
		root.add("servers", servers);

		return root.build().toString();
	}

	private Boolean checkAuthorization() {
		if(noAuth)
			return true;

		// Check auth header
		// We expect something like "Bearer cookieID" from OHIF
		if (authHeader == null || authHeader.isEmpty())
			return false;

		return webTokenAuth.getUsersMap().containsKey(getCookieID());
	}

	private String getCookieID() {
		// Remove Bearer prefix (the space after is important)
		return authHeader.replace("Bearer ", "");
	}

	private HttpURLConnection getDrimboxConnection(String drimboxUrl) throws Exception {

		Log.info("Check auth with cookie ID...");
		if (!checkAuthorization()) {
			throw new Exception("Cookie ID is not valid");
		}
		Log.info("Auth is ok.");

		final URL url = new URL(drimboxUrl);

		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		if(!noAuth)
			connection.setRequestProperty("Authorization",webTokenAuth.getUsersMap().get(getCookieID()).getAccessToken().getRawAccessToken());

		int responseCode = connection.getResponseCode();

		if (responseCode != 200 && responseCode != 206)
			throw new Exception("Drimbox request failed with error code " + responseCode);


		return connection;
	}
}

