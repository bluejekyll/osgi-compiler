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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OSGiDependencyCopyMojo
 *
 * This plugin will collect all required bundles which are referenced from the calling projects bundle.
 *
 * @phase package
 * @goal copy-bundle-dependencies
 * @requiresDependencyResolution runtime
 * @requiresDependencyCollection runtime
 * 
 * @author bfry
 * @since RaidenNet 0.1
 */
@SuppressWarnings("JavaDoc")
public class OSGiDependencyCopyMojo extends AbstractOSGiDependencyMojo {
    /**
     * The path to place all the collected bundles into.
     * @parameter
     * @required
     */
    private File bundleDirectory;

    /**
     * The scope of dependencies to copy to the destination path, this will not work with the provided scope right now.
     * @parameter default-value="compile+runtime"
     */
    private String scope;

    /**
     * @parameter default="false"
     */
    private boolean skipCopy;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skipCopy) {
            Set<Artifact> artifactSet = getProject().getDependencyArtifacts();
            Set<Artifact> dependenciesToCopy = new HashSet<Artifact>();
            Set<String> importsFound = getSystemPackages();
            Map<String, Artifact> requiredImports = new HashMap<String, Artifact>();
            
            for (Artifact dependency: artifactSet) {
                if (scope.contains(dependency.getScope())) {
                    OSGiManifestUtil util = getOSGiManifestUtil(dependency);
                    if (util != null && util.isBundle()) {
                        dependenciesToCopy.add(dependency);
                        requiredImports.putAll(getRootRequiredImports(dependency, util, importsFound));
                    }
                } else {
                    if (getLog().isDebugEnabled()) getLog().debug("ignoring: " + dependency);
                }
            }
            
            if (getLog().isDebugEnabled()) getLog().debug("direct dependencies: " + dependenciesToCopy);
            
            dependenciesToCopy.addAll(computeOSGiDependencies(importsFound, requiredImports));
            
            for (Artifact requiredArtifact: dependenciesToCopy) {
                File jarFile = requiredArtifact.getFile();
                
                if (jarFile != null && jarFile.exists() && jarFile.isFile()) {
                    File groupDir = new File(bundleDirectory, requiredArtifact.getGroupId());
                    if (!groupDir.exists() && !groupDir.mkdirs()) throw new MojoFailureException("could not create directory: " + groupDir);
                    
                    File destinationFile = new File(groupDir, jarFile.getName());
                    getLog().info("copying: " + jarFile + " to: " + destinationFile);
                    copyTo(jarFile, destinationFile);
                } else {
                    getLog().warn("file does not exist: " + requiredArtifact.getGroupId() + ":" + requiredArtifact.getArtifactId() + " file: " + jarFile);
                }
                
            }
        } else {
            getLog().info("skipped, skipCopy set to true");
        }
    }
    
    private void copyTo(File srcFile, File targetFile) throws MojoFailureException {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(srcFile));
            try {
                File tmpFile = new File(targetFile.getAbsolutePath() + ".tmp");
                OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile)); 
                try {
                    for (int bite = is.read(); bite > -1; bite = is.read()) {
                        os.write(bite);
                    }
                    
                    os.flush();
                    tmpFile.renameTo(targetFile);
                } finally {
                    os.close();
                    tmpFile.delete();
                }
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new MojoFailureException("error copying jar", e);
        } 
    }
}
