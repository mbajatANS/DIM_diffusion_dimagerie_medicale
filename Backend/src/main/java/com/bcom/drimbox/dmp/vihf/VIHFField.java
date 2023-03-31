/*
 *  VIHFField.java - DRIMBox
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

    VIHFField(String fieldName){
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

    VIHFField(){

    }
    @Override
    public String toString(){
        return this.fieldName;
    }

}
