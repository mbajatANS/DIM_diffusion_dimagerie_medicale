/*
 *  DRIMboxSourceAPI.java - DRIMBox
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

import static com.bcom.drimbox.utils.PrefixConstants.DRIMBOX_PREFIX;
import static com.bcom.drimbox.utils.PrefixConstants.METADATA_PREFIX;
import static com.bcom.drimbox.utils.PrefixConstants.SERIES_PREFIX;
import static com.bcom.drimbox.utils.PrefixConstants.STUDIES_PREFIX;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.bcom.drimbox.psc.ProSanteConnect;
import com.bcom.drimbox.utils.RequestHelper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;


@Path("/" + DRIMBOX_PREFIX)
public class DRIMboxSourceAPI {

	// Wado url (e.g. http://localhost:8080/dcm4chee-arc/aets/AS_RECEIVED/rs)
	@ConfigProperty(name = "pacs.wadoUrl")
	String wadoUrl;
	// Wado URI url (e.g. http://localhost:8080/dcm4chee-arc/aets/AS_RECEIVED/wado)
	@ConfigProperty(name = "pacs.wadoURIUrl")
	String wadoURIUrl;

	@ConfigProperty(name = "debug.noAuth", defaultValue="false")
	Boolean noAuth;


	@Inject
	RequestHelper requestHelper;

	/**
	 * Bearer token that is in the request. It will be verified with the instropection mechanism of prosanteconnect
	 */
	@HeaderParam("Authorization")
	String bearerToken;
	/**
	 * Pro sante connect service to verify the token
	 */
	@Inject
	ProSanteConnect proSanteConnect;

	@GET
	@Path("/studies/{studyUID}/series/{seriesUID}")
	public RestResponse<byte[]> drimboxMultipartWado(String studyUID, String seriesUID) {
		String url = wadoUrl + "/" + STUDIES_PREFIX + "/" + studyUID + "/" + SERIES_PREFIX + "/" + seriesUID;
		
		return requestHelper.fileRequestCMove(url);
	}

	@GET
	@Path("/studies/{studyUID}/series/{seriesUID}/metadata")
	@Produces("application/dicom+json")
	public RestResponse<String> drimboxMetadataRequest(String studyUID, String seriesUID) {
		String url = wadoUrl + "/" + STUDIES_PREFIX +"/" + studyUID + "/" + SERIES_PREFIX + "/" + seriesUID + "/" + METADATA_PREFIX;

		return requestHelper.stringRequest(url, this::getPacsConnection);
	}

	@GET
	@Path("/studies/{studyUID}/series")
	@Produces("application/dicom+json")
	public RestResponse<String> drimboxSeriesRequest(String studyUID) {
		String url = wadoUrl + "/" + STUDIES_PREFIX + "/" + studyUID + "/" + SERIES_PREFIX;

		return requestHelper.stringRequest(url, this::getPacsConnection);
	}

	@GET
	@Path("/wado")
	@Produces("application/dicom")
	public RestResponse<byte[]> drimboxWadoURI(@Context UriInfo uriInfo) {
		String url = requestHelper.constructUrlWithParam(wadoURIUrl, uriInfo);

		return requestHelper.fileRequest(url, this::getPacsConnection);
	}



	private HttpURLConnection getPacsConnection(String pacsUrl) throws Exception {
		final URL url = new URL(pacsUrl);

		if (noAuth && (bearerToken.isEmpty() || !proSanteConnect.introspectToken(bearerToken))) {
			throw new Exception("Authentication failure");
		}

		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();

		if (responseCode != 200 && responseCode != 206)
			throw new Exception("Pacs request failed with error code " + responseCode);


		return connection;
	}



}
