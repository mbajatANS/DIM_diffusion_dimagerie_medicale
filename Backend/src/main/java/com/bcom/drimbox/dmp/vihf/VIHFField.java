/*
 *  VIHFField.java - DRIMBox
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

package com.bcom.drimbox.dmp.vihf;


/**
 * Enum des champs du VIHF
 *
 */

public enum VIHFField {
    // Version du VIHF utilise
    VIHF_VERSION("VIHF_Version"),
    // Profession ou future profession de l'utilisateur
    ROLE("urn:oasis:names:tc:xacml:2.0:subject:role"),
    // Secteur d'activite dans lequel exerce l'utilisateur
    SECTEUR_ACTIVITE("Secteur_Activite"),


    // Ressource visee par l'utilisateur
    RESSOURCE_URN("Ressource_URN"),
    // Mode d'accès demandé (normal , bris de glace, ...) */
    PURPOSE_OF_USE("urn:oasis:names:tc:xspa:1.0:subject:purposeofuse"),
    // Identite de l'utilisateur (nom, prenom et / ou service, ...) */
    SUBJECT_ID("urn:oasis:names:tc:xspa:1.0:subject:subject-id"),
    // Identifiant de l'etablissement */
    IDENTIFIANT_STRUCTURE("Identifiant_Structure"),
    // Numero de serie ou identifiant de l'installation du logiciel
    LPS_ID("LPS_ID"),
    // Numero de serie ou identifiant de l'installation du logiciel
    LPS_NOM("LPS_Nom"),
    // Numero de serie ou identifiant de l'installation du logiciel
    LPS_HOMOLOGATION_DMP("LPS_ID_HOMOLOGATION_DMP"),

    //Nom et version du logiciel utilise (facultatif)
    LPS_VERSION("LPS_Version"),
    //Identifiant patient vis (facultatif)
    RESSOURCE_ID("urn:oasis:names:tc:xacml:2.0:resource:resource-id"),


    // Requis en mode AIR : cst "INDIRECT_RENFORCEE"*/
    AUTHENTIFICATION_MODE("Authentification_Mode"),
    // Requis si la fonctionnalité est activée et si demande de connexion secréte au DMP
    CONFIDENTIALITY_CODE("urn:oasis:names:tc:xspa:1.0:resource:patient:hl7:confidentiality-code");

    private String fieldName;

    private VIHFField(String fieldName){
        this.fieldName = fieldName;
    }

    public boolean isOptionnal(){
        switch (this) {
            case VIHF_VERSION:
            case ROLE:
            case SECTEUR_ACTIVITE:
            case RESSOURCE_URN:
            case PURPOSE_OF_USE:
            case SUBJECT_ID:
            case AUTHENTIFICATION_MODE:
            case IDENTIFIANT_STRUCTURE:
            case LPS_HOMOLOGATION_DMP:
            case LPS_NOM:
                return false;
            case CONFIDENTIALITY_CODE:
            default:
                return true;
        }
    }

    private VIHFField(){

    }
    @Override
    public String toString(){
        return this.fieldName;
    }

}
