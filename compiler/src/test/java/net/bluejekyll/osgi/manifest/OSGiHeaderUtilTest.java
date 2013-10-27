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

package net.bluejekyll.osgi.manifest;

import net.bluejekyll.osgi.manifest.OSGiHeaderUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * OSGiHeaderUtilTest
 *
 * @author bfry
 */
public class OSGiHeaderUtilTest {
    @DataProvider
    public Object[][] provider() {
        return new Object[][]{
                {"",
                        new String[]{},
                        true
                },
                {"com.force.amazonaws.ec2._2011_11_01",
                        new String[]{"com.force.amazonaws.ec2._2011_11_01"},
                        true
                },
                {"com.force.amazonaws.ec2._2011_11_01;uses:=\"javax.xml.bind.annotation,javax.xml.datatype,javax.xml.bind,javax.xml.namespace\";version=\"1.0.0.SNAPSHOT\",com.force.raidennet.api.ec2.action;version=\"1.0.0.SNAPSHOT\",com.force.raidennet.api.ec2.error;uses:=\"javax.xml.bind.annotation\";version=\"1.0.0.SNAPSHOT\"",
                        new String[]{"com.force.amazonaws.ec2._2011_11_01","com.force.raidennet.api.ec2.action","com.force.raidennet.api.ec2.error"},
                        true
                },
                {"com.force.amazonaws.ec2._2011_11_01;version=\"[1.0,2)\",com.force.raidennet.api.ec2.error;version=\"[1.0,2)\",com.force.raidennet.services.compute;version=\"[1.0,2)\",com.sun.jersey.spi.container.servlet,com.sun.jersey.spi.resource,javax.crypto,javax.crypto.spec,javax.servlet;version=\"[2.6,3)\",javax.servlet.http;version=\"[2.6,3)\",javax.ws.rs,javax.ws.rs.core,javax.ws.rs.ext,javax.xml.bind,javax.xml.bind.annotation,javax.xml.datatype,javax.xml.namespace,org.apache.log4j;version=\"[1.2,2)\",org.osgi.framework;version=\"[1.6,2)\"",
                        new String[]{"com.force.amazonaws.ec2._2011_11_01","com.force.raidennet.api.ec2.error","com.force.raidennet.services.compute","com.sun.jersey.spi.container.servlet","com.sun.jersey.spi.resource","javax.crypto","javax.crypto.spec","javax.servlet","javax.servlet.http","javax.ws.rs","javax.ws.rs.core","javax.ws.rs.ext","javax.xml.bind","javax.xml.bind.annotation","javax.xml.datatype","javax.xml.namespace","org.apache.log4j","org.osgi.framework"},
                        true
                },
                {"javax.imageio.stream,javax.mail;resolution:=optional,javax.imageio",
                        new String[]{"javax.imageio.stream","javax.imageio"},
                        true
                },
                {"javax.imageio.stream,javax.mail;resolution:=optional,javax.imageio",
                        new String[]{"javax.imageio.stream","javax.mail","javax.imageio"},
                        false
                },
                {"org.codehaus.jackson.format;uses:=\"org.codehaus.jackson.io,org.codehaus.jackson\";version=\"1.9.4\",org.codehaus.jackson.io;uses:=\"org.codehaus.jackson.util,org.codehaus.jackson\";version=\"1.9.4\",org.codehaus.jackson.sym;uses:=\"org.codehaus.jackson.util\";version=\"1.9.4\",org.codehaus.jackson.util;uses:=\"org.codehaus.jackson.io,org.codehaus.jackson.impl,org.codehaus.jackson\";version=\"1.9.4\",org.codehaus.jackson.annotate;version=\"1.9.4\",org.codehaus.jackson.impl;uses:=\"org.codehaus.jackson.format,org.codehaus.jackson.io,org.codehaus.jackson.sym,org.codehaus.jackson.util,org.codehaus.jackson\";version=\"1.9.4\",org.codehaus.jackson;uses:=\"org.codehaus.jackson.format,org.codehaus.jackson.sym,org.codehaus.jackson.annotate,org.codehaus.jackson.impl,org.codehaus.jackson.type,org.codehaus.jackson.io,org.codehaus.jackson.util\";version=\"1.9.4\",org.codehaus.jackson.type;version=\"1.9.4\"",
                        new String[]{"org.codehaus.jackson.format","org.codehaus.jackson.io","org.codehaus.jackson.sym","org.codehaus.jackson.util","org.codehaus.jackson.annotate","org.codehaus.jackson.impl","org.codehaus.jackson","org.codehaus.jackson.type"},
                        true
                },
                {"com.force.raidennet.kernel;version=\"[1.0,2)\",com.force.raidennet.osgi.test,org.apache.log4j;version=\"[1.2,2)\",org.osgi.framework;version=\"[1.6,2)\",org.testng;version=\"[6.3,7)\"",
                        new String[]{"com.force.raidennet.kernel","com.force.raidennet.osgi.test","org.apache.log4j","org.osgi.framework","org.testng"},
                        true
                }
        };
    }
    
    @Test(dataProvider = "provider")
    public void testParse(String stringToParse, String[] expectedResult, boolean excludeOptional) {
        OSGiHeaderUtil util = new OSGiHeaderUtil(stringToParse);
        Set<String> parsed = util.parse(excludeOptional);
        Set<String> expectedParse = new HashSet<String>(Arrays.asList(expectedResult));

        Assert.assertEquals(parsed, expectedParse);
    }
}
