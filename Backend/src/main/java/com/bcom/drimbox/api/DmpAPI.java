
/*
 *  DmpAPI.java - DRIMBox
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

package com.bcom.drimbox.api;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bcom.drimbox.dmp.request.BaseRequest;
import com.bcom.drimbox.dmp.request.FindAllDocumentRequest;
import com.bcom.drimbox.dmp.auth.WebTokenAuth;
import com.bcom.drimbox.dmp.DMPConnect;
import com.bcom.drimbox.dmp.DMPConnect.DMPResponseBytes;
import com.bcom.drimbox.dmp.request.FindFilterDocumentRequest;
import com.bcom.drimbox.dmp.request.GiveAuthorizationRequest;
import com.bcom.drimbox.dmp.request.RetrieveDocumentRequest;
import com.bcom.drimbox.dmp.request.VerifyAuthorizationRequest;

// Todo : find a prefix for those request, /dmp maybe ?
@Path("/api")
public class DmpAPI {

	@Inject
	DMPConnect dmpConnect;

	@Inject
	WebTokenAuth webTokenAuth;

	private enum returnType {
		   STRING, RAWBYTES;
		}
	
	@GET
	@Path("/grant/{ins}")
	@Produces(MediaType.TEXT_XML)
	public Response auth(String ins, @CookieParam("SessionToken") Cookie cookieSession)  {
		GiveAuthorizationRequest request = new GiveAuthorizationRequest();
		return dmpRequest(request, ins, cookieSession, returnType.STRING);
	}


	@GET
	@Path("/query/{ins}")
	@Produces(MediaType.TEXT_XML)
	public Response query(String ins, @CookieParam("SessionToken") Cookie cookieSession, @QueryParam("modality") List<String> modalities,
			@QueryParam("region") List<String> regions, @QueryParam("start") String start, @QueryParam("stop") String stop)  {

		if(!modalities.isEmpty() || !regions.isEmpty() || start != null || stop != null) {
			FindFilterDocumentRequest request = new FindFilterDocumentRequest(ins, modalities, regions, start, stop);
			return dmpRequest(request, ins, cookieSession, returnType.STRING);

		}
		else {
			FindAllDocumentRequest request = new FindAllDocumentRequest(ins);
			return dmpRequest(request, ins, cookieSession, returnType.STRING);
		}
	}

	@GET
	@Path("/retrieve/{ins}")
	@Produces(MediaType.TEXT_XML)
	public Response retrieve(String ins, @QueryParam("repositoryId") String repositoryId, @QueryParam("uniqueId") String uniqueId, @CookieParam("SessionToken") Cookie cookieSession)  {
		RetrieveDocumentRequest request = new RetrieveDocumentRequest(repositoryId, uniqueId);
		return dmpRequest(request, ins, cookieSession, returnType.RAWBYTES);
	}

	@GET
	@Path("/verify/{ins}")
	@Produces(MediaType.TEXT_XML)
	public Response verify(String ins, @CookieParam("SessionToken") Cookie cookieSession)  {
		VerifyAuthorizationRequest request = new VerifyAuthorizationRequest(ins);
		return dmpRequest(request, ins, cookieSession, returnType.STRING);
	}


	/**
	 * Make a request to the DMP
	 *
	 * @param request Request to make
	 * @param ins Patient INS
	 * @param cookieSession Cookie session gathered from the backend request. If null it will return a 401 error code.
	 * @param byteArray response in byteArray format
	 * @return DMP response with code 200 if all is going well. 401 if there is a failure in auth, 500 if VIHF could not be created.
	 */
	private Response dmpRequest(BaseRequest request, String ins, Cookie cookieSession, returnType returnObject) {
		if(cookieSession != null && webTokenAuth.getUsersMap().containsKey(cookieSession.getName())) {

			Boolean result = request.createVIHF(webTokenAuth.getUsersMap().get(cookieSession.getName()).getUserInfo(), ins, webTokenAuth.getUsersMap().get(cookieSession.getName()).getSecteurActivite());
			if (!result)
				return Response.status(500).build();
			
			if(returnObject == returnType.STRING) {
				DMPConnect.DMPResponse response = dmpConnect.sendRequest(request);
				return Response.ok(response.message).build();
			}
			// To retrieve a file (cda or kos), we need to have it in byteArray format to not lose information
			if(returnObject == returnType.RAWBYTES) {
				DMPResponseBytes response = dmpConnect.sendKOSRequest(request);
				return Response.ok(response.rawMessage).build();
			}
		}

		return Response.status(401).build();
	}
}
