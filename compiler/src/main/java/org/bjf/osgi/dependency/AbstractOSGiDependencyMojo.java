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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * AbstractOSGiDependencyMojo
 *
 * @author bfry
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public abstract class AbstractOSGiDependencyMojo extends AbstractMojo {
    /**
     * This is the set of system packages provided by the jdk
     * @parameter default-value="javax.accessibility,javax.activation,javax.activity,javax.annotation,javax.annotation.processing,javax.crypto,javax.crypto.interfaces,javax.crypto.spec,javax.imageio,javax.imageio.event,javax.imageio.metadata,javax.imageio.plugins.bmp,javax.imageio.plugins.jpeg,javax.imageio.spi,javax.imageio.stream,javax.jws,javax.jws.soap,javax.lang.model,javax.lang.model.element,javax.lang.model.type,javax.lang.model.util,javax.management,javax.management.loading,javax.management.modelmbean,javax.management.monitor,javax.management.openmbean,javax.management.relation,javax.management.remote,javax.management.remote.rmi,javax.management.timer,javax.naming,javax.naming.directory,javax.naming.event,javax.naming.ldap,javax.naming.spi,javax.net,javax.net.ssl,javax.print,javax.print.attribute,javax.print.attribute.standard,javax.print.event,javax.rmi,javax.rmi.CORBA,javax.rmi.ssl,javax.script,javax.security.auth,javax.security.auth.callback,javax.security.auth.kerberos,javax.security.auth.login,javax.security.auth.spi,javax.security.auth.x500,javax.security.cert,javax.security.sasl,javax.sound.midi,javax.sound.midi.spi,javax.sound.sampled,javax.sound.sampled.spi,javax.sql,javax.sql.rowset,javax.sql.rowset.serial,javax.sql.rowset.spi,javax.swing,javax.swing.border,javax.swing.colorchooser,javax.swing.event,javax.swing.filechooser,javax.swing.plaf,javax.swing.plaf.basic,javax.swing.plaf.metal,javax.swing.plaf.multi,javax.swing.plaf.synth,javax.swing.table,javax.swing.text,javax.swing.text.html,javax.swing.text.html.parser,javax.swing.text.rtf,javax.swing.tree,javax.swing.undo,javax.tools,javax.transaction,javax.transaction.xa,javax.xml,javax.xml.bind,javax.xml.bind.annotation,javax.xml.bind.annotation.adapters,javax.xml.bind.attachment,javax.xml.bind.helpers,javax.xml.bind.util,javax.xml.crypto,javax.xml.crypto.dom,javax.xml.crypto.dsig,javax.xml.crypto.dsig.dom,javax.xml.crypto.dsig.keyinfo,javax.xml.crypto.dsig.spec,javax.xml.datatype,javax.xml.namespace,javax.xml.parsers,javax.xml.soap,javax.xml.stream,javax.xml.stream.events,javax.xml.stream.util,javax.xml.transform,javax.xml.transform.dom,javax.xml.transform.sax,javax.xml.transform.stax,javax.xml.transform.stream,javax.xml.validation,javax.xml.ws,javax.xml.ws.handler,javax.xml.ws.handler.soap,javax.xml.ws.http,javax.xml.ws.soap,javax.xml.ws.spi,javax.xml.xpath,org.ietf.jgss,org.omg.CORBA,org.omg.CORBA.DynAnyPackage,org.omg.CORBA.ORBPackage,org.omg.CORBA.TypeCodePackage,org.omg.CORBA.portable,org.omg.CORBA_2_3,org.omg.CORBA_2_3.portable,org.omg.CosNaming,org.omg.CosNaming.NamingContextExtPackage,org.omg.CosNaming.NamingContextPackage,org.omg.Dynamic,org.omg.DynamicAny,org.omg.DynamicAny.DynAnyFactoryPackage,org.omg.DynamicAny.DynAnyPackage,org.omg.IOP,org.omg.IOP.CodecFactoryPackage,org.omg.IOP.CodecPackage,org.omg.Messaging,org.omg.PortableInterceptor,org.omg.PortableInterceptor.ORBInitInfoPackage,org.omg.PortableServer,org.omg.PortableServer.CurrentPackage,org.omg.PortableServer.POAManagerPackage,org.omg.PortableServer.POAPackage,org.omg.PortableServer.ServantLocatorPackage,org.omg.PortableServer.portable,org.omg.SendingContext,org.omg.stub.java.rmi,org.w3c.dom,org.w3c.dom.bootstrap,org.w3c.dom.css,org.w3c.dom.events,org.w3c.dom.html,org.w3c.dom.ls,org.w3c.dom.ranges,org.w3c.dom.stylesheets,org.w3c.dom.traversal,org.w3c.dom.views,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers"
     */
    private String osgiSystemPackages;

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * These are additional packages to ignore...
     * @parameter
     */
    private List<String> ignorePackages;

    /**
     * This should be a list of <groupId>:<artifactId>
     * @parameter
     */
    private Set<String> ignoreArtifacts;

    protected MavenProject getProject() {
        return project;
    }
    
    public Map<String, Artifact> getRootRequiredImports(Artifact artifact, OSGiManifestUtil manifestUtil, Set<String> importsFound)  throws MojoExecutionException, MojoFailureException {
        importsFound.addAll(manifestUtil.getExports()); // add all the packages this project exports too.
        if (ignorePackages != null) importsFound.addAll(ignorePackages);
        
        Map<String, Artifact> osgiImports = getOsgiImports(manifestUtil.getImportsMinusExports(), artifact);
        osgiImports.keySet().removeAll(importsFound);
        
        return osgiImports;
    }
    
    public Set<Artifact> getOSGiDependencies(Artifact artifact, OSGiManifestUtil manifestUtil) throws MojoExecutionException, MojoFailureException {
        // initializing the imports found to the osgi system packages, i.e. what's in the jdk
        Set<String> importsFound = getSystemPackages();
        Map<String, Artifact> osgiImports = getRootRequiredImports(artifact, manifestUtil, importsFound);
        return computeOSGiDependencies(importsFound, osgiImports);
    }

    protected Set<Artifact> computeOSGiDependencies(Set<String> importsFound, Map<String, Artifact> osgiRequiredImports) throws MojoFailureException, MojoExecutionException {
        Set<Artifact> requiredArtifacts = new HashSet<Artifact>();
        Set<Artifact> dependencies = getDependencies();
        Map<Artifact, Set<String>> dependencyImports = new HashMap<Artifact, Set<String>>();
        Map<Artifact, Set<String>> dependencyExports = new HashMap<Artifact, Set<String>>();
        Map<String, Set<Artifact>> osgiExportsByArtifact = new HashMap<String, Set<Artifact>>();

        if (getLog().isDebugEnabled()) {
            getLog().debug("beginning set of imports: " + osgiRequiredImports.keySet());
            getLog().debug("all dependencies: " + dependencies);
        }

        // find all the imports and exports of all the dependencies
        for (Artifact dependency: dependencies) {
            if (ignoreArtifacts != null &&
                    ignoreArtifacts.contains(dependency.getGroupId() + ":" + dependency.getArtifactId())) {
                continue;
            }

            OSGiManifestUtil dependencyManifestUtil = getOSGiManifestUtil(dependency);
            if (dependencyManifestUtil != null && dependencyManifestUtil.isBundle()) {
                Set<String> thisDependencyExports = Collections.unmodifiableSet(dependencyManifestUtil.getExports());

                // collect all the exports by this dependency
                for (String export: thisDependencyExports) {
                    Set<Artifact> artifacts = osgiExportsByArtifact.get(export);
                    if (artifacts == null) {
                        artifacts = new HashSet<Artifact>();
                        osgiExportsByArtifact.put(export, artifacts);
                    }
                    artifacts.add(dependency);
                }
                
                dependencyImports.put(dependency, dependencyManifestUtil.getImportsMinusExports());
                dependencyExports.put(dependency, thisDependencyExports);
                
                if (getLog().isDebugEnabled()) getLog().debug("exports for " + dependency + ": " + thisDependencyExports);
            }
        }
        
        if (getLog().isDebugEnabled()) getLog().debug("all exports: " + osgiExportsByArtifact.keySet());

        // find all the artifacts we need
        Deque<String> requiredImports = new LinkedList<String>(osgiRequiredImports.keySet());
        while(!requiredImports.isEmpty()) {
            String osgiImport = requiredImports.pop();
            
            Set<Artifact> artifacts = osgiExportsByArtifact.get(osgiImport);
            if (artifacts!= null) for (Artifact requiredArtifact: artifacts) {
                // we'll need this artifact now...
                requiredArtifacts.add(requiredArtifact);

                // we need to add all the imports for this 
                Set<String> newRequiredImports = dependencyImports.get(requiredArtifact);
                if (newRequiredImports == null) throw new MojoFailureException("imports should not be null for: " + requiredArtifact);
                requiredImports.addAll(newRequiredImports);
                
                // we just found all it's exports...
                Set<String> requiredExports = dependencyExports.get(requiredArtifact);
                if (requiredExports == null) throw new MojoFailureException("exports should not be null for: " + requiredArtifact);
                importsFound.addAll(requiredExports);
            } else {
                if (!importsFound.contains(osgiImport)) getLog().warn("no artifact found that provides: " + osgiImport);
            }
        }
        
        // remove all the found imports from the imports
        osgiRequiredImports.keySet().removeAll(importsFound);

        if (!osgiRequiredImports.isEmpty()) {
            for (Map.Entry<String, Artifact> entry: osgiRequiredImports.entrySet()) {
                getLog().error("Import: " + entry.getKey() + " required by: " + entry.getValue() + " not found");
            }
            throw new MojoExecutionException("Missing imports, see output");
        }

        return requiredArtifacts;
    }

    protected Set<String> getSystemPackages() {
        String[] systemPackages = osgiSystemPackages.split(",");

        // initializing the imports found to the osgi system packages, i.e. what's in the jdk
        return new HashSet<String>(Arrays.asList(systemPackages));
    }
    
    private Set<Artifact> getDependencies() {
        // this doesn't work with scope==provided, not sure why...
        Set dependencies = project.getArtifacts();

        Set<Artifact> result = new HashSet<Artifact>();
        for (Object artifactObj: dependencies) {
            result.add((Artifact) artifactObj);
        }

        return result;
    }

    private Map<String, Artifact> getOsgiImports(Set<String> imports, Artifact projectArtifact) throws MojoFailureException {
        Map<String, Artifact> result = new HashMap<String, Artifact>();
        for (String packageImport: imports) {
            result.put(packageImport, projectArtifact);
        }

        return result;
    }

    protected OSGiManifestUtil getOSGiManifestUtil(Artifact projectArtifact) throws MojoFailureException {
        File projectFile = projectArtifact.getFile();

        try {
            JarFile jar = new JarFile(projectFile);
            Manifest manifest = jar.getManifest();
            
            if (manifest == null) {
                getLog().warn("null manifest for " + projectArtifact.getGroupId() + ":" + projectArtifact.getArtifactId());
                return null;
            }
            
            return new OSGiManifestUtil(manifest);
        } catch (IOException e) {
            throw new MojoFailureException("could not read jar file: " + projectFile, e);
        }
    }
}
