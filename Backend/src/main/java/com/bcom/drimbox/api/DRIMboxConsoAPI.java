/*
 *  DRIMboxConsoAPI.java - DRIMBox
 *
 * N°IDDN : IDDN.FR.001.020012.000.S.C.2023.000.30000
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

import com.bcom.drimbox.dmp.xades.file.KOSFile;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import com.bcom.drimbox.dmp.auth.WebTokenAuth;
import com.bcom.drimbox.pacs.PacsCache;
import com.bcom.drimbox.utils.RequestHelper;
import org.dcm4che3.data.Tag;
import org.dcm4che3.util.TagUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;

import javax.inject.Inject;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.function.Function;

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
	public static final String DICOM_FILE_PREFIX = "dicomfile";
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
		pacsCache.addNewEntry(drimboxSourceURL, getAccessToken(), studyUID, seriesUID);

		return requestHelper.stringRequest(url + "/" + METADATA_PREFIX, this::getDrimboxConnection);
	}

	private String getAccessToken() {
		String accessToken = "noAuthDebugOnly";
		if (!noAuth) {
			accessToken = webTokenAuth.getAccessToken(getCookieID()).getRawAccessToken();
		}
		return accessToken;
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



	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("ohifv3metadata/{studyUID}/{seriesUID}")
	public String getOHIFv3Metadata(@Context UriInfo uriInfo, String studyUID, String seriesUID) {
		String drimboxSourceURL = "";
		// get drimbox source url from KOS
		KOSFile kos = DmpAPI.getKOS(studyUID);
		if (kos == null) {
			Log.error("can't find KOS associated with study UID " + studyUID);
			return "";
		}

		String seriesURL = kos.getSeriesURL().get(seriesUID);
		if (seriesURL == null) {
			Log.error("Can't find series " + seriesUID + "in KOS " + studyUID);
			return "";
		}

		// Get drimboxSource url
		try {
			URL u = new URL(seriesURL);
			String protocol = u.getProtocol();
			String authority = u.getAuthority();
			drimboxSourceURL = String.format("%s://%s", protocol, authority);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "";
		}

		// Note : This will also populate the cache
		return getOHIFMetadata(uriInfo.getBaseUri(), studyUID, seriesUID, drimboxSourceURL);
	}

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("ohifv3metadata/{drimboxSourceURL}/{studyUID}/{seriesUID}")
	public String getOHIFv3Metadata(@Context UriInfo uriInfo, String drimboxSourceURL, String studyUID, String seriesUID) {
		// Note : This will also populate the cache
		return getOHIFMetadata(uriInfo.getBaseUri(), studyUID, seriesUID, drimboxSourceURL);
	}


	// Note : this method create a JSON for OHIF AND populate the cache
	private String getOHIFMetadata(URI drimboxConsoBaseURI, String studyUID, String seriesUID, String drimboxSourceURL) {
		// First see if we can get the metadata from the source
		// TODO : check auth
		String url = HTTP_PROTOCOL + drimboxSourceURL + "/" + DRIMBOX_PREFIX + "/" + STUDIES_PREFIX + "/" + studyUID;
		if (!seriesUID.isEmpty()) {
			url += "/series/" + seriesUID;
		}
		var response = requestHelper.stringRequest(url + "/" + METADATA_PREFIX, this::getDrimboxConnection);

		// TODO : better handling of errors
		if (response.getStatus() != 200) {
			return "";
		}

		// Add series to the cache
		// This is non-blocking operation
		pacsCache.addNewEntry(drimboxSourceURL, getAccessToken(), studyUID, seriesUID);


		// Read metadata from server
		var jsonReader = Json.createReader(new StringReader(response.getEntity()));
		JsonArray dicomMetadata = jsonReader.readArray();


		JsonObjectBuilder root = Json.createObjectBuilder();
		JsonArrayBuilder studiesArray = Json.createArrayBuilder();
		JsonArrayBuilder seriesArray = Json.createArrayBuilder();
		JsonArrayBuilder instancesArray = Json.createArrayBuilder();

		JsonObjectBuilder seriesObject = Json.createObjectBuilder();
		seriesObject.add("SeriesInstanceUID", seriesUID);

		for(Object dicomInstanceMetadata : dicomMetadata) {
			JsonObject currentInstanceJson = (JsonObject) dicomInstanceMetadata;
			Function<Integer, String> getStringField = (var tag) ->  currentInstanceJson.getJsonObject(TagUtils.toHexString(tag)).getJsonArray("Value").getString(0);
			Function<Integer, Integer> getIntField = (var tag) ->  currentInstanceJson.getJsonObject(TagUtils.toHexString(tag)).getJsonArray("Value").getInt(0);

			JsonObjectBuilder metadata = Json.createObjectBuilder();
			String instanceUID = getStringField.apply(Tag.SOPInstanceUID);
			metadata.add("SOPInstanceUID", instanceUID);
			metadata.add("SeriesInstanceUID", getStringField.apply(Tag.SeriesInstanceUID));
			metadata.add("StudyInstanceUID", getStringField.apply(Tag.StudyInstanceUID));
			metadata.add("SOPClassUID", getStringField.apply(Tag.SOPClassUID));
			metadata.add("Modality", getStringField.apply(Tag.Modality));
			metadata.add("Columns", getIntField.apply(Tag.Columns));
			metadata.add("Rows", getIntField.apply(Tag.Rows));
			metadata.add("PixelRepresentation", getIntField.apply(Tag.PixelRepresentation));
			metadata.add("BitsAllocated", getIntField.apply(Tag.BitsAllocated));
			metadata.add("BitsStored", getIntField.apply(Tag.BitsStored));
			metadata.add("SamplesPerPixel", getIntField.apply(Tag.SamplesPerPixel));
			metadata.add("HighBit", getIntField.apply(Tag.HighBit));
			metadata.add("PhotometricInterpretation", getStringField.apply(Tag.PhotometricInterpretation));
			metadata.add("InstanceNumber", getStringField.apply(Tag.InstanceNumber));



			// Add to the instance list
			instancesArray.add(Json.createObjectBuilder()
					.add("metadata", metadata)
					.add("url", "dicomweb:" + drimboxConsoBaseURI + DICOM_FILE_PREFIX + "/" + studyUID + "/" + seriesUID + "/" + instanceUID )
			);
		}
		seriesObject.add("instances", instancesArray);
		seriesObject.add("Modality", "CT");
		seriesArray.add(seriesObject);

		JsonObjectBuilder study = Json.createObjectBuilder();
		study.add("StudyInstanceUID", studyUID);
		study.add("series", seriesArray);
		study.add("NumInstances", dicomMetadata.size());

		study.add("StudyDate", "20000101");
		study.add("StudyTime", "");
		study.add("PatientName", "");
		study.add("PatientID", "LOL");
		study.add("AccessionNumber", "");
		study.add("PatientAge", "");
		study.add("PatientSex", "");
		study.add("StudyDescription", "");
		// TODO handle modalities (maybe not necessary ?)
		study.add("Modalities", "CT");

		studiesArray.add(study);

		root.add("studies", studiesArray);

		return root.build().toString();
	}

	@GET
	@Path(DICOM_FILE_PREFIX + "/{studyUID}/{seriesUID}/{instanceUID}")
	//@Produces("application/dicom")
	public Uni<RestResponse<byte[]>> getDicomFile(String studyUID, String seriesUID, String instanceUID) {
		if (!checkAuthorization())
			return Uni.createFrom().item(requestHelper.getDeniedFileResponse());

		try {
			Future<byte[]> future = pacsCache.getDicomFile(studyUID, seriesUID, instanceUID);
			return Uni.createFrom().future(future).onItem().transform(
					item -> {
						Log.info("[dicomfile] Response : " + instanceUID);
						return RestResponse.ResponseBuilder.ok(item).header("Accept-Ranges", "bytes").build();
					}
			);
		} catch (Exception e) {
			// TODO : Is this really fatal ?
			Log.fatal("Can't get file from cache");
			return Uni.createFrom().item(requestHelper.getDeniedFileResponse());
		}
	}

	private Boolean checkAuthorization() {
		if(noAuth)
			return true;

		// Check auth header
		// We expect something like "Bearer cookieID" from OHIF
		if (authHeader == null || authHeader.isEmpty())
			return false;

		return webTokenAuth.clientRegistered(getCookieID());
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
			connection.setRequestProperty("Authorization", webTokenAuth.getAccessToken(getCookieID()).getRawAccessToken());

		int responseCode = connection.getResponseCode();

		if (responseCode != 200 && responseCode != 206)
			throw new Exception("Drimbox request failed with error code " + responseCode);


		return connection;
	}
}

