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

package org.bjf.osgi.compiler;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.tools.*;

import org.apache.log4j.Logger;

import org.bjf.osgi.dependency.OSGiManifestUtil;

/**
 * OSGiCompiler
 *
 * @author bfry
 * @since RaidenNet 0.1
 */
public class OSGiCompiler {
    private final Logger logger = Logger.getLogger(OSGiCompiler.class);
    private final URL fileToCompile;
    private final Set<String> systemPackages;
    private final Set<String> bootDelegation;

    public OSGiCompiler(URL fileToCompile, Set<String> systemPackages, Set<String> bootDelegation) {
        this.fileToCompile = fileToCompile;
        this.systemPackages = systemPackages;
        this.bootDelegation = bootDelegation;
    }
    
    public void compile() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        logger.info(compiler.getClass());
        logger.info(compiler.getSourceVersions());
        
        DiagnosticListener<JavaFileObject> diagnosticListener = new DiagnosticListener<JavaFileObject>() {
            @Override
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                logger.info(diagnostic.toString());
            }
        };

        Map<String, Set<JarFile>> exportsToJars = buildMapOfExportsToJars();
        StandardJavaFileManager fileManager = new OSGiJavaFileManager(compiler.getStandardFileManager(diagnosticListener, null, null), exportsToJars, systemPackages, bootDelegation);
        
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(fileToCompile.getFile());
        
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticListener, null, null, fileObjects);
        
        if (!task.call()) {
            throw new RuntimeException("compilation failed");
        }
    }
    
    private Map<String, Set<JarFile>> buildMapOfExportsToJars() {
        String classPath = System.getProperty("java.class.path");
        String pathSeparator = System.getProperty("path.separator");

        Map<String, Set<JarFile>> exportsToJars = new HashMap<String, Set<JarFile>>();
        
        String[] classPaths = classPath.split(pathSeparator);
        for (String path: classPaths) {
            if (path.endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(path);
                    Manifest manifest = jarFile.getManifest();
                    
                    if (manifest != null) {
                        OSGiManifestUtil manifestUtil = new OSGiManifestUtil(manifest);
                        if (manifestUtil.isBundle()) {
                            Set<String> exports = manifestUtil.getExports();
                            
                            for (String export: exports) {
                                Set<JarFile> jars = exportsToJars.get(export);
                                if (jars == null) {
                                    jars = new LinkedHashSet<JarFile>(); // keep the order the same as on the classpath
                                    exportsToJars.put(export, jars);
                                }
                                
                                jars.add(jarFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.warn(String.format("could not read jar file: %s", path), e);
                }
            }
        }
        
        return exportsToJars;
    }
}
