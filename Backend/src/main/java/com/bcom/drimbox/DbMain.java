/*
 *  DbMain.java - DRIMBox
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

package com.bcom.drimbox;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.bcom.drimbox.pacs.CStoreSCP;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.CommandLineArguments;

import java.util.Optional;

@Startup
@Singleton
public class DbMain {

	@ConfigProperty(name="dcm.cstore.AET")
	String calledAET;
	@ConfigProperty(name="dcm.cstore.host")
	String host;
	@ConfigProperty(name="dcm.cstore.port")
	int port;

	@Inject
	@CommandLineArguments
	String[] args;

	// Cache of instance datas
	@Inject
	CStoreSCP cStoreSCP;

	enum DrimBOXMode {
		SOURCE,
		CONSO
	}

	static final String SOURCE_ARG = "source";
	static final String CONSO_ARG = "conso";
	@PostConstruct
	public void checkParams() throws Exception {
		DrimBOXMode mode = DrimBOXMode.CONSO;
		// Also check environment (mainly for docker)
		String envDbMode = Optional.ofNullable(System.getenv("DRIMBOX_MODE")).orElse("");

		if (args.length == 1 && args[0].equals(SOURCE_ARG)
			|| envDbMode.equals(SOURCE_ARG)) {
			mode = DrimBOXMode.SOURCE;
		}

		switch (mode) {
			case SOURCE:
				Log.info("Starting DrimBOX Source");
				cStoreSCP.startCStore(calledAET, host, port);
				break;
			case CONSO:
				Log.info("Starting DrimBOX Conso");
				break;
		}

	}
}