/*
 * Copyright (c) 2013, Benjamin J. Fry
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Benjamin J. Fry nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.bluejekyll.osgi.compiler;


import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * OSGiCompilerTest
 *
 * @author bfry
 */
public class OSGiCompilerTest {
    Pattern testBundlePattern = Pattern.compile(".*test.bundle-[a-zA-Z0-9.-]+jar.*");

    private static final String TEST_ARTIFACT = "test.bundle";
    private static final String VERSION = "1.0-SNAPSHOT";

    @DataProvider
    Object[][] testCompileProvider() {
        return new Object[][]{
                {"TestBasic.java", true},
                {"TestSuccess.java", true},
                {"TestFailure.java", false},
        };
    }

    @Test(dataProvider = "testCompileProvider")
    public void testCompile(String javaFile, boolean pass) throws Exception {
        boolean hasTestJar = false;
        if (!testBundlePattern.matcher(System.getProperty("java.class.path")).matches()) {
            // maven does this correctly by default, IDE's on the other hand do not...
            String path = String.format("%s/.m2/repository/net/bluejekyll/osgi/%s/%s/%s-%s.jar",
                    System.getProperty("user.home"), TEST_ARTIFACT, VERSION, TEST_ARTIFACT, VERSION);

            File file = new File(path);
            if (file.exists()) {
                String tmpClassPath = String.format("%s%s%s", System.getProperty("java.class.path"), System.getProperty("path.separator"), file.getAbsolutePath());
                System.setProperty("java.class.path", tmpClassPath);
                hasTestJar = true;
            } else {
                hasTestJar = false;
            }
        } else {
            hasTestJar = true;
        }

        Assert.assertTrue(hasTestJar, "test.bundle-{version}.jar not on classpath");

        URL testJava = OSGiCompilerTest.class.getResource(javaFile);

        Assert.assertNotNull(testJava);

        try {
            OSGiCompiler compiler = new OSGiCompiler(testJava, Collections.singleton("java.lang"), Collections.singleton("java.*"));
            compiler.compile();
            Assert.assertTrue(pass, "compile should have failed");
        } catch (Exception e) {
            Assert.assertFalse(pass, String.format("compile should have succeeded: %s", e.getMessage()));
        }
        
        //Class testClazz = Class.forName("com.force.osgi.compiler.TestSuccess");
        //Object obj = testClazz.newInstance();
        //Assert.assertEquals(obj.toString(), "deadbeef");
    }
}
