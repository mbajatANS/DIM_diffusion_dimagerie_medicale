/*
 *  RequestHelper.java - DRIMBox
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

package com.bcom.drimbox.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.bcom.drimbox.pacs.CMoveSCU;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.logging.Log;

@Singleton
public class RequestHelper {

	// Cache of instance datas
	@Inject
	CMoveSCU cMoveSCU;

	/**
	 * Extract query params from UriInfo and append it to base URL
	 *  e.g. if your initial request have params like ?param1=12&param2=DOUZE
	 *  with a baseUrl = http://pacs.dcm4che.org/wado it will return :
	 *  http://pacs.dcm4che.org/wado?param1=12&param2=DOUZE
	 *
	 *  We need this function because quarkus doesn't capture parameters in the @Path,
	 *  but it is stored in uriInfo.
	 *
	 * @param baseUrl Base url of the service you want to reach
	 * @param uriInfo UriInfo object obtained in the request (@Context UriInfo uriInfo)
	 *
	 * @return BaseUrl appended with the params in UriInfo
	 */
	public String constructUrlWithParam(String baseUrl, UriInfo uriInfo) {
		// Add ? to base URL (default separator for get params)
		baseUrl += "?";

		MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
		StringBuilder baseUrlBuilder = new StringBuilder(baseUrl);
		for(String str : queryParameters.keySet()){
			baseUrlBuilder.append(str).append("=").append(queryParameters.getFirst(str)).append("&");
		}
		baseUrl = baseUrlBuilder.toString();

		// Remove the last &
		return baseUrl.substring(0, baseUrl.length() - 1);
	}

	/**
	 * Get String representation of connection
	 * @param connection HttpURLConnection of the server
	 * @return Empty string if an error occurred, contents of the input stream otherwise
	 */
	public RestResponse<String> readStringResponse(HttpURLConnection connection) {
		try {
			String response =new BufferedReader(
					new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining(System.lineSeparator()));

			return RestResponse.ResponseBuilder
					.ok(response)
					.header("Content-Type", connection.getContentType())
					.build();

		} catch (Exception e) {
			Log.fatal("error while reading server string response");
		}
		return getDeniedStringResponse();
	}

	public RestResponse<byte[]> readFileResponse(HttpURLConnection connection) {
		try {
			return RestResponse.ResponseBuilder
					.ok(connection.getInputStream().readAllBytes())
					.header("Content-Type", connection.getContentType())
					.build();
		} catch (Exception e) {
			Log.fatal("error while reading server file response");
			return RestResponse.ResponseBuilder.ok(new byte[0]).status(401).build();
		}
	}

	/**
	 * Convenience function that returns 401 RestResponse for String
	 */
	public RestResponse<String> getDeniedStringResponse() {
		return RestResponse.ResponseBuilder.ok("").status(401).build();
	}

	/**
	 * Convenience function that returns 401 RestResponse for files
	 */
	public RestResponse<byte[]> getDeniedFileResponse() {
		return RestResponse.ResponseBuilder.ok(new byte[0]).status(401).build();
	}


	// Todo : add String authToken
	public interface ServiceConnection {
		HttpURLConnection connect(String url) throws Exception ;
	}

	// Todo : see if Response<> can do the work instead to avoid duplicate functions
	public RestResponse<String> stringRequest(String pacsUrl, ServiceConnection service) {
		try {
			HttpURLConnection connection = service.connect(pacsUrl);
			RestResponse<String> response = readStringResponse(connection);

			connection.disconnect();
			return response;
		} catch (Exception e) {
			Log.fatal("Error while doing string request " + e.getMessage());
			return getDeniedStringResponse();
		}
	}


	public RestResponse<byte[]> fileRequest(String pacsUrl, ServiceConnection service) {
		try {
			HttpURLConnection connection = service.connect(pacsUrl);
			RestResponse<byte[]> response = readFileResponse(connection);

			connection.disconnect();
			Log.info("Fin request");
			return response;
		} catch (Exception e) {
			Log.fatal("Error while doing file request " + e.getMessage());
			return getDeniedFileResponse();
		}
	}

	public synchronized RestResponse<byte[]> fileRequestCMove(String pacsUrl) {
		String studyUID = pacsUrl.split("/studies/")[1].split("/")[0];
		String serieUID = pacsUrl.split("/series/")[1].split("/")[0];
		try {
			return RestResponse.ResponseBuilder
					.ok(cMoveSCU.cMove(studyUID, serieUID))
					.header("Content-Type", "multipart/related;start=\"<1@resteasy-multipart>\";transfer-syntax=1.2.840.10008.1.2.1;type=\"application/dicom\"; boundary=myBoundary")
					.build();
		} catch (Exception e) {
			Log.fatal("Error while doing file request " + e.getMessage());
			return getDeniedFileResponse();
		}
	}


}
