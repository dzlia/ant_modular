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
import java.text.MessageFormat;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;

public class ManifestModuleLoader implements ModuleLoader
{
    private static final Name ATTRIB_DEPENDENCIES = new Name("Depends");
    
    private static final Pattern dependenciesPattern = Pattern.compile("\\S+");
    
    private Project project;
    
    public void init(final Project project)
    {
        if (project == null) {
            throw new NullPointerException("project");
        }
        this.project = project;
    }
    
    public ModuleInfo loadModule(final String path) throws ModuleNotLoadedException
    {
        if (project == null) {
            throw new IllegalStateException("This ManifestModuleLoader is not initialised.");
        }
        final Manifest manifest = readManifest(path);
        
        final ModuleInfo moduleInfo = new ModuleInfo(path);
        
        final Attributes attributes = manifest.getMainAttributes();
        addDependencies(attributes, moduleInfo);
        
        return moduleInfo;
    }
    
    private static void addDependencies(final Attributes attributes, final ModuleInfo moduleInfo)
    {
        final String deps = attributes.getValue(ATTRIB_DEPENDENCIES);
        if (deps == null) {
            return;
        }
        final Matcher m = dependenciesPattern.matcher(deps);
        while (m.find()) {
            moduleInfo.addDependency(m.group());
        }
    }
    
    private Manifest readManifest(final String path) throws ModuleNotLoadedException
    {
        final File moduleDir = new File(project.getBaseDir(), path);
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
                return new Manifest(in);
            }
            finally {
                in.close();
            }
        }
        catch (IOException ex) {
            throw new ModuleNotLoadedException(MessageFormat.format(
                    "An I/O error is encountered while loading the module with path ''{0}'' (''{1}'').",
                    path, manifestFile.getAbsolutePath()));
        }
    }
}
