/*
 *  request.component.ts - DRIMBox
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

import { Component, OnInit, ElementRef, AfterViewInit } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CheckAccessService } from './check-access.service';
import { DocumentsService } from './documents.service';
import { DatePipe } from '@angular/common';
import { MAT_DATE_FORMATS } from '@angular/material/core';

// See the Moment.js docs for the meaning of these formats:
// https://momentjs.com/docs/#/displaying/format/
export const MY_FORMATS = {
  parse: {
    dateInput: 'DD/MM/YYYY',
  },
  display: {
    dateInput: 'DD/MM/YYYY',
    monthYearLabel: 'MMMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY'
  },
};

@Component({
  selector: 'app-request',
  templateUrl: './request.component.html',
  styleUrls: ['./request.component.scss'],
  providers: [
    // `MomentDateAdapter` can be automatically provided by importing `MomentDateModule` in your
    // application's root module. We provide it at the component level here, due to limitations of
    // our example generation script.
    { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },
  ],
})
export class RequestComponent implements OnInit, AfterViewInit {

  // boolean to display error message
  accessError = false;

  // consent value retrieve in url parameter
  consent: string;
  // ins retrieved in url parameter
  ins: string;

  regions = new UntypedFormControl('');
  regionsList: string[] = ['Abdomen + pelvis', 'Corps entier', 'Membres superieurs', 'Membres inferieurs', 'Tete', 'Thorax'];

  modalites = new UntypedFormControl('');
  modalitesList: string[] = ['CT', 'US', 'MR', 'PT'];

  startDate = new UntypedFormControl();
  stopDate = new UntypedFormControl();

  // table headers
  headElements = ['Examens', 'Date', 'CR', 'PACS', 'Visionneuse'];
  headElementsPatient = ['Nom', 'Prenom', 'Sexe', 'Date de naissance', 'INS'];
  headSerieElemKOS = ['Description', 'Localisation', 'Modalité', 'Nombre d\images'];

  /**
   * Change background color to black on ins page
   * */
  ngAfterViewInit() {
    this.elementRef.nativeElement.ownerDocument.body.style.backgroundColor = '#000';
    this.elementRef.nativeElement.ownerDocument.body.style.color = '#fff';
  }

  /**
   *
   * @param route to manage angular routes
   * @param checkAccessService to check-access component
   * @param documentsService to documents component
   * @param elementRef to change css
   */
  constructor(private readonly route: ActivatedRoute, private readonly checkAccessService: CheckAccessService,
    public documentsService: DocumentsService, private readonly elementRef: ElementRef) {
  }

  /**
   * On Init, retrieve url param and check auth and right to access
   * */
  async ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.ins = params['ins'];
      this.consent = params['consent'];
    });

    if (this.ins !== undefined) {
      // Check access to patient dmp
      const auth = await this.checkAccessService.td0_2(this.ins, this.consent);
      if (auth === "ok") {
        this.documentsService.td3_1(this.ins);
      }
      else if (auth === "no access") {
        this.accessError = true;
      }
    }
  }

  /**
   * Check filters after research button click
   * */
  filterSearch() {
    const startDateFormat = new DatePipe('en').transform(this.startDate.value, 'yyyyMMdd');
    const stopDateFormat = new DatePipe('en').transform(this.stopDate.value, 'yyyyMMdd');

    // Check if filters selected, if not classic request
    if (this.modalites.value.length === 0 && this.regions.value.length === 0 && startDateFormat === null && stopDateFormat === null) {
      this.documentsService.td3_1(this.ins);
    }
    // Request with filters
    else {
      this.documentsService.td3_1Filters(this.ins, this.modalites.value, this.regions.value, startDateFormat, stopDateFormat);
    }
  }

}

