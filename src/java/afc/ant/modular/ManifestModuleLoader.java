/* Copyright (c) 2013, Dźmitry Laŭčuk
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met: 

   1. Redistributions of source code must retain the above copyright notice, this
      list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
   ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package afc.ant.modular;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.launch.Locator;

// TODO parse Class-Path and other attributes
public class ManifestModuleLoader extends ProjectComponent implements ModuleLoader
{
    private static final Name ATTRIB_DEPENDENCIES = new Name("Depends");
    
    private static final Pattern dependenciesPattern = Pattern.compile("\\S+");
    
    public ModuleInfo loadModule(final String path) throws ModuleNotLoadedException
    {
        final Attributes attributes = readManifestBuildSection(path);
        final ModuleInfo moduleInfo = new ModuleInfo(path);
        
        addDependencies(attributes, moduleInfo);
        
        return moduleInfo;
    }
    
    private static void addDependencies(final Attributes attributes, final ModuleInfo moduleInfo)
    {
        final String deps = (String) attributes.remove(ATTRIB_DEPENDENCIES);
        if (deps == null) {
            return;
        }
        final Matcher m = dependenciesPattern.matcher(deps);
        while (m.find()) {
            final String path = decodeUri(m.group());
            moduleInfo.addDependency(path);
        }
    }
    
    private Attributes readManifestBuildSection(final String path) throws ModuleNotLoadedException
    {
        final File moduleDir = new File(getProject().getBaseDir(), path);
        if (!moduleDir.exists()) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "The module with path ''{0}'' (''{1}'') does not exist.", path, moduleDir.getAbsolutePath()));
        }
        if (!moduleDir.isDirectory()) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "The module path ''{0}'' (''{1}'') is not a directory.", path, moduleDir.getAbsolutePath()));
        }
        
        final File manifestFile = new File(moduleDir, "META-INF/MANIFEST.MF");
        if (!manifestFile.exists()) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "The module with path ''{0}'' does not have the manifest (''{1}'').",
                    path, manifestFile.getAbsolutePath()));
        }
        if (!manifestFile.isFile()) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "The module with path ''{0}'' have the manifest that is not a file (''{1}'').",
                    path, manifestFile.getAbsolutePath()));
        }
        
        try {
            final FileInputStream in = new FileInputStream(manifestFile);
            try {
                final Manifest manifest = new Manifest(in);
                final String buildSectionName = "Build";
                final Attributes buildAttributes = manifest.getAttributes(buildSectionName);
                if (buildAttributes == null) {
                    throw new ModuleNotLoadedException(MessageFormat.format(
                            "The module ''{0}'' does not have the ''{2}'' section in its manifest (''{1}'').",
                            path, manifestFile.getAbsolutePath(), buildSectionName));
                }
                return buildAttributes;
            }
            finally {
                in.close();
            }
        }
        catch (IOException ex) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "An I/O error is encountered while loading the module with path ''{0}'' (''{1}'').",
                    path, manifestFile.getAbsolutePath()), ex);
        }
    }
    
    private static String decodeUri(final String str)
    {
        try {
            return Locator.decodeUri(str);
        }
        catch (UnsupportedEncodingException ex) {
            throw new BuildException("Unable to decode URI.", ex);
        }
    }
}
