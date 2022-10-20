/*
 *  app.component.ts - DRIMBox
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

import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent {
  title = 'ApiDrim';

  // boolean indicates if user is connected
  connected = false;
  // ins retrieved in url parameter
  ins: string;
  // auth date of user
  dateAuth: string;
  // name + surname of user
  authenticator: string;

  /**
   * Constructor : manage local datas, session token and check if user connected
   * @param http for rest requests
   * @param cookieService to manage user cookies
   * @param route to manage angular routes
   */
  constructor(private readonly http: HttpClient, private readonly cookieService: CookieService, private readonly route: ActivatedRoute) {
    this.authenticator = "DOCTEUR RADI";
    this.dateAuth = "04/10/2022 15:42";

    this.route.queryParams.subscribe(params => {
      // Retrieve url parameter (ins)
      this.ins = params['ins'];
      if (this.ins !== undefined) {
        this.connected = true;
      }
      // Generate sessionToken
      if (!cookieService.check("SessionToken")) {
        const uuid = this.generateUUID();
        this.cookieService.set("SessionToken", uuid);
      }
      this.verifyConnection();
    });
  }

  /**
   * Verify with backend if user connected
   * */
  verifyConnection() {
    this.http.get('/api/auth', { responseType: 'text' }).subscribe(data => {
      // Back can answer : connected -- means user is already connected
      //                   connected but no structure : + list structs -- means user is already connected but activity struct no selected, gives list of user structs
      //                   no connected : + url -- means user is not connected, gives url to ProSanteConnect
      if (data.startsWith("no connected : ")) {
        // No connected, redirect to ProSanteconnect window
        const url = data.split(": ")[1];
        window.location.replace(url);
      }
      else if (data.startsWith("connected but no structure : ")) {
        window.location.replace("https://localhost/ris");
      }
    });
  }

  /**
   * Function to generate UUID
   * */
  generateUUID() { // Public Domain/MIT
    let d = new Date().getTime();//Timestamp
    let d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now() * 1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      let r = Math.random() * 16;//random number between 0 and 16
      if (d > 0) {//Use timestamp until depleted
        r = (d + r) % 16 | 0;
        d = Math.floor(d / 16);
      } else {//Use microseconds since page-load if supported
        r = (d2 + r) % 16 | 0;
        d2 = Math.floor(d2 / 16);
      }
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
  }
}
