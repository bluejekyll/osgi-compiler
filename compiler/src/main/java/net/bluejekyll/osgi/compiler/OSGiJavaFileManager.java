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
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.apache.log4j.Logger;

/**
 * OSGiJavaFileManager
 *
 * @author bfry
 */
public class OSGiJavaFileManager implements StandardJavaFileManager {
    private static final Logger logger = Logger.getLogger(OSGiJavaFileManager.class);
    
    private final StandardJavaFileManager delegate;
    private final Map<String, Set<JarFile>> exportsToJars;
    private final Set<String> systemPackages;
    private final Set<Pattern> bootDelegation;

    public OSGiJavaFileManager(StandardJavaFileManager delegate, Map<String, Set<JarFile>> exportsToJars, Set<String> systemPackages, Set<String> bootDelegation) {
        this.delegate = delegate;
        this.exportsToJars = exportsToJars;
        this.systemPackages = systemPackages;
        
        // todo combine these into a single pattern
        Set<Pattern> bootDelegationTmp = new HashSet<Pattern>();
        for (String bootDelegationPattern: bootDelegation) {
            bootDelegationTmp.add(Pattern.compile(bootDelegationPattern));
        }
        
        this.bootDelegation = Collections.unmodifiableSet(bootDelegationTmp);
    }
    

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        logger.info("isSameFile");
        return delegate.isSameFile(a, b);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        logger.info("getJavaFileObjectsFromFiles");
        return delegate.getJavaFileObjectsFromFiles(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        logger.info("getJavaFileObjects");
        return delegate.getJavaFileObjects(files);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        logger.info("getJavaFileObjectsFromStrings");
        return delegate.getJavaFileObjectsFromStrings(names);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        logger.info("getJavaFileObjects");
        return delegate.getJavaFileObjects(names);
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
        logger.info(String.format("setLocation location: %s path: %s", location, path));
        delegate.setLocation(location, path);
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        Iterable<? extends File> files = delegate.getLocation(location);
        logger.info(String.format("getLocation: location: %s files: %s", location, files));
        return files;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        logger.info(String.format("getClassLoader location: %s isOutput: %b", location.getName(), location.isOutputLocation()));
        return new OSGiClassLoader(delegate.getClassLoader(location));
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (recurse) throw new UnsupportedOperationException("recurse is not currently supported");

        boolean passes = false;
        if (location.isOutputLocation() ||
                StandardLocation.SOURCE_PATH.equals(location)) passes = true;

        for (Pattern pattern: bootDelegation) {
            if (pattern.matcher(packageName).matches()) passes = true; 
        }
        
        if (!passes && 
                (systemPackages.contains(packageName) ||
                        exportsToJars.containsKey(packageName))) {
            passes = true;
        }
        
        if (passes) {
            // we have an exported jar
            return delegate.list(location, packageName, kinds, recurse);
        } else {
            logger.error(String.format("%s is not exported by any OSGi jar", packageName));
            return Collections.emptyList();
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        String binaryName = delegate.inferBinaryName(location, file);
        logger.trace(String.format("inferBinaryName location: %s file: %s binaryName: %s", location, file, binaryName));
        return binaryName;
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        logger.info("handleOption");
        return delegate.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
        boolean hasLocation = delegate.hasLocation(location);
        if (logger.isTraceEnabled()) logger.trace(String.format("hasLocation location: %s", location));
        return hasLocation;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        logger.info(String.format("getJavaFileForInput location: %s className: %s kind: %s", location, className, kind));
        return delegate.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        JavaFileObject fileObject = delegate.getJavaFileForOutput(location, className, kind, sibling);
        logger.info(String.format("getJavaFileForOutput location: %s className: %s kind: %s sibling: %s fileObject: %s", location, className, kind, sibling, fileObject));
        return fileObject;
        
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        FileObject fileObject = delegate.getFileForInput(location, packageName, relativeName);
        logger.info(String.format("getFileForInput fileObject: %s", fileObject));
        return fileObject;
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        FileObject fileObject = delegate.getFileForOutput(location, packageName, relativeName, sibling);
        logger.info(String.format("getFileForOutput fileObject: %s", fileObject));
        return fileObject;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public int isSupportedOption(String option) {
        logger.info("isSupportedOption");
        return delegate.isSupportedOption(option);
    }
}

