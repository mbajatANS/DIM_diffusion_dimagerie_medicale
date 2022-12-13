/*
 *  PacsCache.java - DRIMBox
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

package com.bcom.drimbox.pacs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.bcom.drimbox.api.DRIMboxConsoAPI;
import com.bcom.drimbox.utils.PrefixConstants;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.mime.MultipartInputStream;
import org.dcm4che3.mime.MultipartParser;

import io.quarkus.logging.Log;
import io.vertx.core.Vertx;

@Singleton
public class PacsCache {
	static class DicomCacheInstance {
		// Instance UID => file
		Map<String, byte[]> dicomFiles = new HashMap<>();
		Boolean complete = false;
	}

	// StudyUID =>
	//  - SeriesUID
	//      - InstanceUID
	//      - InstanceUID
	//  - SeriesUID
	//      - InstanceUID
	// StudyUID =>
	//  - SeriesUID
	//  - SeriesUID
	Map<String, Map<String, DicomCacheInstance>> dicomCache = new HashMap<>();
	
	// Boundary for multipart request
	private static final String BOUNDARY = "myBoundary";
	
	private final Vertx vertx;

	@Inject
	public PacsCache(Vertx vertx) {
		this.vertx = vertx;
	}


	/**
	 * Add new entry to the cache. It will fetch all instances of given study and
	 * series UID to store it in the cache.
	 *
	 * This function is non-blocking and will be executed in another thread
	 *
	 * @param drimboxSourceURL Source drimbox URL
	 * @param studyUID Study UID
	 * @param seriesUID Series UID
	 */
	public void addNewEntry(String drimboxSourceURL, String studyUID, String seriesUID) {
		// Do not rebuild if already here
		if (dicomCache.containsKey(studyUID) && dicomCache.get(studyUID).containsKey(seriesUID))
			return;

		vertx.executeBlocking(promise -> {
			Log.info("Starting cache build...");
			Log.info("Starting WADO (series) request : " + seriesUID);

			Map<String, DicomCacheInstance> map = new HashMap<>();
			map.put(seriesUID, new DicomCacheInstance());

			if(dicomCache.containsKey(studyUID)) {
				dicomCache.get(studyUID).put(seriesUID, new DicomCacheInstance());
			}
			else {
				dicomCache.put(studyUID, map);
			}
			buildEntry(drimboxSourceURL, studyUID, seriesUID);

			promise.complete();
		}, res -> 
		Log.info("Cache built")
				);
	}

	// TODO : handle study/series not present

	private DicomCacheInstance getCacheInstance(String studyUID, String seriesUID) {
		return dicomCache.get(studyUID).get(seriesUID);
	}


	/**
	 * Get dicom file in cache.
	 *
	 * If it is not available it will wait for the buildCache to emit the requested
	 * file.
	 *
	 * @param studyUID Study UID
	 * @param seriesUID Series UID
	 * @param instanceUID Instance UID
	 *
	 * @return Dicom file corresponding to the UIDs.
	 * It may not be available right away as the cache can take some time to be built.
	 */
	Map<String, CompletableFuture<byte[]>> waitingFutures = new HashMap<>();
	public Future<byte[]> getDicomFile(String studyUID, String seriesUID, String instanceUID) {
		CompletableFuture<byte[]> completableFuture = new CompletableFuture<>();

		DicomCacheInstance instance = getCacheInstance(studyUID, seriesUID);

		if (instance.dicomFiles.containsKey(instanceUID)) {
			//Log.info("[CACHE] Available " + instanceUID);
			completableFuture.complete(instance.dicomFiles.get(instanceUID));
		} else {
			Log.info("[CACHE] Waiting for : " + instanceUID);
			waitingFutures.put(instanceUID, completableFuture);
			//			vertx.eventBus().consumer(instanceUID).handler( m-> {
			//				Log.info("[CACHE] Sending image : " + instanceUID);
			//				completableFuture.complete((byte[]) m.body());
			//			}
			//					);
			//
		}

		return completableFuture;
	}

	private interface BoundaryFunc { String getBoundary(String contentType); }
	private void buildEntry(String drimboxSourceURL, String studyUID, String seriesUID) {
		String serviceURL = DRIMboxConsoAPI.HTTP_PROTOCOL + drimboxSourceURL + "/" + PrefixConstants.DRIMBOX_PREFIX + "/" + PrefixConstants.STUDIES_PREFIX + "/" + studyUID + "/series/" + seriesUID;
		//String serviceURL = "http://localhost:8081/dcm4chee-arc/aets/AS_RECEIVED/rs"  + "/" + STUDIES_PREFIX + "/" + studyUID + "/series/" + seriesUID;
		try {
			Map<String, String> transferSyntaxes = Map.of(UID.JPEGBaseline8Bit, "0.9",
					UID.JPEGExtended12Bit, "0.9",
					UID.JPEGLosslessSV1, "0.6",
					UID.JPEGLSLossless, "0.5",
					UID.RLELossless, "0.5",
					UID.MPEG2MPML, "0.5",
					UID.MPEG2MPHL, "0.5",
					UID.MPEG4HP41, "0.5",
					UID.MPEG4HP41BD, "0.5",
					UID.ExplicitVRLittleEndian, "0.4");
			final URL url = new URL(serviceURL);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    for (Map.Entry<String, String> entry : transferSyntaxes.entrySet()) {
				connection.addRequestProperty("Accept", "multipart/related; type=\"application/dicom\";transfer-syntax="+entry.getKey()+";q="+entry.getValue()+";boundary=" + BOUNDARY);
		    }

			BoundaryFunc boundaryManager = (String contentType) -> {
				String[] respContentTypeParams = contentType.split(";");
				for (String respContentTypeParam : respContentTypeParams)
					if (respContentTypeParam.replace(" ", "").startsWith("boundary="))
						return respContentTypeParam
								.substring(respContentTypeParam.indexOf('=') + 1)
								.replaceAll("\"", "");

				return null;
			};

			String boundary = boundaryManager.getBoundary(connection.getContentType());
			if (boundary == null) {
				Log.fatal("Invalid response. Unpacking of parts not possible.");
				throw new RuntimeException();
			}

			DicomCacheInstance dc = getCacheInstance(studyUID, seriesUID);

			new MultipartParser(boundary).parse(new BufferedInputStream(connection.getInputStream()), new MultipartParser.Handler() {
				@Override
				public void bodyPart(int partNumber, MultipartInputStream multipartInputStream) throws IOException {
					Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
					try {
						byte[] rawDicomFile = multipartInputStream.readAllBytes();
						DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(rawDicomFile));
						Attributes dataSet = dis.readDataset();
						String instanceUID = dataSet.getString(Tag.SOPInstanceUID);

						//Log.info("[CACHE] Received file " + instanceUID);
						dc.dicomFiles.put(instanceUID, rawDicomFile);

						if (waitingFutures.containsKey(instanceUID)) {
							//Log.info("[CACHE] Publish file " + instanceUID);
							waitingFutures.get(instanceUID).complete(rawDicomFile);
							waitingFutures.remove(instanceUID);
						}

					} catch (Exception e) {
						Log.fatal("Failed to process Part #" + partNumber + headerParams, e);
					}
				}
			});

			dc.complete = true;
			Log.info("[CACHE] Complete");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}



	}

}
