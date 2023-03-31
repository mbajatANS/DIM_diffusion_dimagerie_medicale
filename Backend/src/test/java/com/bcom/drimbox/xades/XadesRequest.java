/*
 *  XadesRequest.java - DRIMBox
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

package com.bcom.drimbox.xades;

import com.bcom.drimbox.dmp.xades.file.CDAFile;
import com.bcom.drimbox.dmp.xades.file.KOSFile;
import com.bcom.drimbox.dmp.xades.request.ProvideAndRegisterRequest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.bcom.drimbox.xades.CDAParsing.CDA_TEST_FILE;

@QuarkusTest
public class XadesRequest {

    @Test
    public void testRequest() {
        CDAFile c = new CDAFile(new File(ClassLoader.getSystemResource(CDA_TEST_FILE).getPath()));
        KOSFile k = new KOSFile(new File(ClassLoader.getSystemResource("kos.dcm").getPath()));
        ProvideAndRegisterRequest r = new ProvideAndRegisterRequest(c, k);
    }
}
