/*
 *  GiveAuthorizationRequest.java - DRIMBox
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

package com.bcom.drimbox.dmp.request;

public class GiveAuthorizationRequest extends BaseRequest {	

	@Override
	protected String actionName() {
		return "urn:si-dmp-habilitations:HabilitationsEndPoint:setAuthorization";
	}

	@Override
	protected String serviceName() {
		return "habilitations";
	}

	public GiveAuthorizationRequest() {
		super();

		var psetAuthorization = soapRequest.createElement("setAuthorization");
		psetAuthorization.setAttribute("xmlns", "urn:si-dmp-habilitations");
		body.appendChild(psetAuthorization);

		var pAction = soapRequest.createElement("action");
		pAction.appendChild(soapRequest.createTextNode("AJOUT"));
		psetAuthorization.appendChild(pAction);
	}


}
