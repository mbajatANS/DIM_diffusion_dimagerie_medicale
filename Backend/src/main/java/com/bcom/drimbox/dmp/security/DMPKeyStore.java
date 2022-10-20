/*
 *  DMPKeyStore.java - DRIMBox
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

package com.bcom.drimbox.dmp.security;


import io.quarkus.logging.Log;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.security.*;
import java.security.cert.X509Certificate;


/**
 * Class that handles all the certificates used in the TLS connection and for VIHF signature.
 * It also has some helpers function to extract the information you need (private key, issuer, ...)
 *
 * The following resources are mandatory :
 *  - resources/dmp/auth.p12 : auth certificate used for the TLS connection
 *  - resources/dmp/signature.p12 : signature certificate used for the VIHF signature
 *  - resources/dmp/authClient-truststore.jks : java created trust store with the ACI-EL-ORG.crt CA file
 *      - keytool -genkey -dname "cn=CLIENT" -alias trustStoreKey -keyalg RSA -keystore authClient-truststore.jks -keypass mypassword -storepass mypassword
 *      - keytool -import -keystore authClient-truststore.jks -file ACI-EL-ORG.crt
 */
@Singleton
public final class DMPKeyStore {
    // Class variables
    private KeyStore tlsClientStore;
    private KeyStore signStore;
    private KeyStore trustStore;
    private KeyManager[] keyManagers;


    private String password;

    /**
     * @return TLS KeyStore (resources/dmp/auth.p12)
     */
    public KeyStore getTlsClientStore() {
        return tlsClientStore;
    }

    /**
     * @return VIHF signature KeyStore (resources/dmp/auth.p12)
     */
    public KeyStore getSignStore() {
        return signStore;
    }

    /**
     * @return CA trust store (resources/dmp/authClient-truststore.jks)
     */
    public KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * @return KeyManager initialized with getTlsClientStore()
     */
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    /**
     * Get private key that is in VIHF signature KeyStore (resources/dmp/auth.p12)
     * @return Private key used to sign VIHF
     */
    public KeyStore.PrivateKeyEntry privateKeySignature() {
        try {
            return (KeyStore.PrivateKeyEntry) signStore.getEntry(signStore.aliases().nextElement(), new KeyStore.PasswordProtection(password.toCharArray()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Init the DMPKeyStore object with given password. The password will be taken from the application.properties file
     * (field : dmp.auth.certif.password) and will be used to decrypt the following files :
     *  - auth.p12
     *  - signature.p12
     *  - authClient-truststore.jks
     *
     * @apiNote We need to pass the password here otherwise it's not available in the constructor
     *
     */
    @Inject
    DMPKeyStore(@ConfigProperty(name="dmp.auth.certif.password") String password, @ConfigProperty(name="dmp.auth.store.password") String storePassword) {
        this.password = password;
        try {
            tlsClientStore = KeyStore.getInstance("PKCS12");
            tlsClientStore.load(getFileFromResourceAsStream("dmp/auth.p12"), password.toCharArray());

            signStore = KeyStore.getInstance("PKCS12");
            signStore.load(getFileFromResourceAsStream("dmp/signature.p12"), password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(tlsClientStore, password.toCharArray());
            keyManagers = kmf.getKeyManagers();

            trustStore = KeyStore.getInstance("JKS");
            trustStore.load(getFileFromResourceAsStream("dmp/authClient-truststore.jks"), storePassword.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get issuer that will be used to fill the VIHF field "Issuer"
     * @return Issuer inside the dmp/signature.p12 file
     */
    public String getVIHFIssuer() {
        try {
            X509Certificate cert = (X509Certificate) signStore.getCertificate(signStore.aliases().nextElement());
            return cert.getSubjectX500Principal().toString();
        } catch (KeyStoreException e) {
            Log.error(e.getMessage());
        }
        return "";
    }

    /**
     * Get file from the resource folder
     * @param fileName File to load
     * @return Openned file
     */
    private InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
}
