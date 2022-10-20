/*
 *  check-access.service.ts - DRIMBox
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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class CheckAccessService {

  constructor(private readonly http: HttpClient) { }

  /**
   * Verify if patient dmp is open and user has right to access to it
   * @param ins of the patient user wants to access
   * @param consent value 'yes' or 'no' -> give access if user don't have the right
   */
  async td0_2(ins: string, consent: string): Promise<string> {

    const response = await this.http.get('/api/verify/' + ins, { responseType: 'text' }).toPromise();

    const verify = response.toString().split("AUTORISATION")[1].split("code=\"")[1].split("\"")[0];
    // If VALIDE, user has right to access to patient dmp
    if (verify === "VALIDE") {
      return "ok";
    }
    else if (consent === "yes") {
      // If content : yes, we add right for the user to access patient dmp
      return this.td0_3(ins);
    }
    else return "no access";
  }

  /**
   * Give right to the user to access to patient dmp
   * @param ins of the patient
   */
  async td0_3(ins: string): Promise<string> {
    await this.http.get('/api/grant/' + ins, { responseType: 'text' }).toPromise();
    return "ok";
  }
}
