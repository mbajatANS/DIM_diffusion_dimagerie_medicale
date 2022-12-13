
/*
 *  DmpAPI.java - DRIMBox
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
	 * @return DMP response with code 200 if all is going well. 401 if there is a failure in auth, 500 if VIHF could not be created.
	 */
	private Response dmpRequest(BaseRequest request, String ins, Cookie cookieSession, returnType returnObject) {
		if(cookieSession != null) {
			String cookieID = cookieSession.getValue();

			if ( webTokenAuth.clientRegistered(cookieID)) {

				Boolean result = request.createVIHF(
						webTokenAuth.getUserInfo(cookieID),
						ins,
						webTokenAuth.getSecteurActivite(cookieID));

				if (!result)
					return Response.status(500).build();

				if (returnObject == returnType.STRING) {
					DMPConnect.DMPResponse response = dmpConnect.sendRequest(request);
					return Response.ok(response.message).build();
				}
				// To retrieve a file (cda or kos), we need to have it in byteArray format to not lose information
				if (returnObject == returnType.RAWBYTES) {
					DMPResponseBytes response = dmpConnect.sendKOSRequest(request);
					return Response.ok(response.rawMessage).build();
				}
			}
		}

		return Response.status(401).build();
	}
}
