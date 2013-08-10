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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ModuleInfo
{
    private final String path;
    private final HashSet<ModuleInfo> dependencies = new HashSet<ModuleInfo>();
    private final Set<ModuleInfo> dependenciesView = Collections.unmodifiableSet(dependencies);
    
    public ModuleInfo(final String path)
    {
        if (path == null) {
            throw new NullPointerException("path");
        }
        this.path = path;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public void addDependency(final ModuleInfo dependency)
    {
        if (dependency == null) {
            throw new NullPointerException("dependency");
        }
        if (dependency == this) {
            throw new IllegalArgumentException("Cannot add itself as a dependency.");
        }
        dependencies.add(dependency);
    }
    
    public void setDependencies(final Collection<ModuleInfo> dependencies)
    {
        if (dependencies == null) {
            throw new NullPointerException("dependencies");
        }
        for (final ModuleInfo dependency : dependencies) {
            if (dependency == null) {
                throw new NullPointerException("dependencies contains null dependency.");
            }
        }
        // TODO check each dependency to be non-null
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }
    
    public Set<ModuleInfo> getDependencies()
    {
        return dependenciesView;
    }
}
