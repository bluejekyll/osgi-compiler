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

package org.bjf.osgi.dependency;

import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * OSGiManifestUtil
 *
 * @author bfry
 * @since RaidenNet 0.1
 */
public class OSGiManifestUtil {
    private static final Attributes.Name IMPORT_PACKAGE = new Attributes.Name("Import-Package");
    private static final Attributes.Name EXPORT_PACKAGE = new Attributes.Name("Export-Package");
    private static final Attributes.Name IGNORE_PACKAGE = new Attributes.Name("Ignore-Package");
    private static final Attributes.Name BUNDLE_NAME = new Attributes.Name("Bundle-Name");
    
    private final Manifest manifest;
    
    public OSGiManifestUtil(File manifestFile) throws MojoFailureException {
        if (manifestFile == null ||
                !manifestFile.exists() ||
                !manifestFile.isFile()) throw new MojoFailureException("manifestFile not found:" + manifestFile);

        try {
            InputStream is = new FileInputStream(manifestFile);
            this.manifest = new Manifest(is);
        } catch (IOException e) {
            throw new MojoFailureException("could not read Manifest: " + manifestFile, e);
        }
    }

    public OSGiManifestUtil(Manifest manifest) {
        if (manifest == null) throw new NullPointerException();
        this.manifest = manifest;
    }
    
    public Set<String> getImports() throws IOException {
        return getPackages(IMPORT_PACKAGE);
    }
    
    public Set<String> getImportsMinusExports() throws IOException {
        Set<String> imports = getImports();
        Set<String> exports = getExports();
        Set<String> ignores = getIgnores();
        
        imports.removeAll(exports);
        imports.removeAll(ignores);
        return imports;
    }
    
    public Set<String> getExports() throws IOException {
        return getPackages(EXPORT_PACKAGE);
    }

    public Set<String> getIgnores() throws IOException {
        return getPackages(IGNORE_PACKAGE);
    }

    public String getBundleName() throws IOException {
        return getAttribute(BUNDLE_NAME);
    }
    
    public boolean isBundle() throws IOException {
        return getBundleName() != null;
    }

    private String getAttribute(Attributes.Name param) throws IOException {
        Attributes attributes = manifest.getMainAttributes();

        Object obj = attributes.get(param);

        if (obj != null) {
            if (!(obj instanceof String)) throw new IOException("expected a string for: " + param + " " + obj.getClass().getName());
            return (String)obj;
        }
        return null;
    }
     
    private Set<String> getPackages(Attributes.Name packageType) throws IOException {
        String value = getAttribute(packageType);
        if (value != null) {
            OSGiHeaderUtil util = new OSGiHeaderUtil(value);
            return util.parse(true);
        } 
        
        return Collections.emptySet();
    }
}
