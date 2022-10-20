/*
 *  request.component.ts - DRIMBox
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

