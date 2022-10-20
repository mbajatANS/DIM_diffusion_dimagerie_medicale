/*
 *  StowRs.java - DRIMBox
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

package com.bcom.drimbox.pacs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.dcm4che3.util.StreamUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;

@Path("/api")
public class StowRs {

	// Stow url (e.g. http://localhost:8080/dcm4chee-arc/aets/DCM4CHEE/rs/studies)
	@ConfigProperty(name = "pacs.stowUrl")
	String stowUrl;

	// Cache of instance datas
	@Inject PacsCache pacsCache;

	// Boundary for multipart request
	private static final String BOUNDARY = "myBoundary";


	/**
	 * 
	 * @param studyUID for the study to stow
	 * @param serieUID if present to only stow this serie
	 * @throws Exception
	 */
	@Path("/stow")
	public void stow(@QueryParam("studyUID") String studyUID, @QueryParam("serieUID") String serieUID) throws Exception {

		// List of instances from the study to stow
		List<byte[]> listFiles = new ArrayList<>();

		// If serieUID and studyUID in url and exist on cache, load only instances from the serie given
		if(serieUID != null && studyUID != null && pacsCache.dicomCache.get(studyUID).get(serieUID) != null) {

			pacsCache.dicomCache.get(studyUID).get(serieUID).dicomFiles.forEach((instance, valueByte) 
					-> listFiles.add(valueByte));
		}

		// studyUID in url and exist on cache, load all instances from the study given
		else if(studyUID != null && pacsCache.dicomCache.get(studyUID) != null) {
			pacsCache.dicomCache.get(studyUID).forEach((serie,dicomCacheInstance)
					-> dicomCacheInstance.dicomFiles.forEach((instance, valueByte) 
							-> listFiles.add(valueByte)));
		}


		// Open connection with Pacs
		URLConnection urlConnection = new URL(stowUrl).openConnection();
		final HttpURLConnection connection = (HttpURLConnection) urlConnection;

		// Initialize header parameters
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Content-Type",
				"multipart/related;type=\"application/dicom\";boundary=" + BOUNDARY);
		requestProperties.put("Accept", "application/dicom+json");
		requestProperties.put("Connection", "keep-alive");
		requestProperties.forEach(connection::setRequestProperty);

		try (OutputStream out = connection.getOutputStream()) {
			// Creating multipart request with all instance datas
			for (int i = 0; i < listFiles.size(); i++) {
				InputStream targetStream = new ByteArrayInputStream(listFiles.get(i));
				try {
					if (i != 0) {
						out.write(("\r\n").getBytes());
					}
					out.write(("--" + BOUNDARY + "\r\nContent-Disposition: form-data;name=\"file\";filename=\"IM-0001-000" + i + ".dcm\"\r\n"
							+ "Content-Type: application/dicom\r\n\r\n").getBytes());
					StreamUtils.copy(targetStream, out);
					if (i == listFiles.size() - 1)
						out.write(("\r\n--" + BOUNDARY + "--").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					throw new RuntimeException(e);
				}
			}

			out.flush();
			Log.info(connection.getResponseMessage());
			Log.info(connection.getErrorStream());
			Log.info(connection.getResponseCode());
			connection.connect();

			connection.disconnect();

		}
	}

}
