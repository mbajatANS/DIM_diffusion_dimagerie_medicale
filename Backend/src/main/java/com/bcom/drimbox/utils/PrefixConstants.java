/*
 *  PrefixConstants.java - DRIMBox
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

/**
 * Collection of constants that are defined in path of Pacs and Drimbox requests
 */
public final class PrefixConstants {

    private PrefixConstants() {}

    /**
     * Drimbox requests prefix
     */
    public static final String DRIMBOX_PREFIX = "drimbox";
    /**
     * Studies prefix (as defined in the dicom standard)
     */
    public static final String STUDIES_PREFIX = "studies";
    /**
     * Series prefix (as defined in the dicom standard)
     */
    public static final String SERIES_PREFIX = "series";
    /**
     * Metadata prefix (as defined in the dicom standard)
     */
    public static final String METADATA_PREFIX = "metadata";

}
