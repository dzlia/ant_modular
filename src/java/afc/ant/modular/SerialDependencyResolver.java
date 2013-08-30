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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class SerialDependencyResolver implements DependencyResolver
{
    private ArrayList<Module> moduleOrder;
    private Module moduleAcquired;
    private int pos;
    
    public void init(final Collection<Module> rootModules) throws CyclicDependenciesDetectedException
    {
        if (rootModules == null) {
            throw new NullPointerException("rootModules");
        }
        for (final Module module : rootModules) {
            if (module == null) {
                throw new NullPointerException("rootModules contains null element.");
            }
        }
        ensureNoLoops(rootModules);
        moduleOrder = orderModules(rootModules);
        pos = 0;
        moduleAcquired = null;
    }
    
    // returns a module that does not have dependencies
    public Module getFreeModule()
    {
        ensureInitialised();
        if (moduleAcquired != null) {
            throw new IllegalStateException("#getFreeModule() is called when there is a module being processed.");
        }
        if (pos == moduleOrder.size()) {
            return null;
        }
        return moduleAcquired = moduleOrder.get(pos);
    }
    
    public void moduleProcessed(final Module module)
    {
        ensureInitialised();
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (moduleAcquired == null) {
            throw new IllegalStateException("No module is being processed.");
        }
        if (moduleAcquired != module) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The module ''{0}'' is not being processed.", module.getPath()));
        }
        ++pos;
        moduleAcquired = null;
    }
    
    private void ensureInitialised()
    {
        if (moduleOrder == null) {
            throw new IllegalStateException("Resolver is not initialised.");
        }
    }
    
    // Returns modules in the order so that each module's dependee modules are located before this module.
    private static ArrayList<Module> orderModules(final Collection<Module> rootModules)
    {
        /* TODO it is known that there are no loops in the dependency graph.
           Use it knowledge to eliminate unnecessary checks OR merge orderModules() with
           ensureNoLoops() so that loops are checked for while the module order is being built. */
        final IdentityHashMap<Module, Module> registry = new IdentityHashMap<Module, Module>();
        final ArrayList<Module> moduleOrder = new ArrayList<Module>();
        for (final Module module : rootModules) {
            addModuleDeep(module, moduleOrder, registry);
        }
        return moduleOrder;
    }
    
    private static void addModuleDeep(final Module module, final ArrayList<Module> moduleOrder,
            final IdentityHashMap<Module, Module> registry)
    {
        if (registry.containsKey(module)) {
            return; // the module is already processed
        }
        
        registry.put(module, null);
        
        // inverted dependencies are assigned
        for (final Module dep : module.getDependencies()) {
            addModuleDeep(dep, moduleOrder, registry);
        }
        moduleOrder.add(module);
    }
    
    private static void ensureNoLoops(final Collection<Module> modules) throws CyclicDependenciesDetectedException
    {
        final HashSet<Module> cleanModules = new HashSet<Module>();
        final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
        for (final Module module : modules) {
            ensureNoLoops(module, path, cleanModules);
        }
    }
    
    private static void ensureNoLoops(final Module module, final LinkedHashSet<Module> path,
            final HashSet<Module> cleanModules) throws CyclicDependenciesDetectedException
    {
        if (cleanModules.contains(module)) {
            return;
        }
        if (path.add(module)) {
            for (final Module dep : module.getDependencies()) {
                ensureNoLoops(dep, path, cleanModules);
            }
            path.remove(module);
            cleanModules.add(module);
            return;
        }
        
        /* A loop is detected. It does not necessarily end with the starting node,
           some leading nodes could be truncated. */
        int loopSize = path.size();
        final Iterator<Module> it = path.iterator();
        while (it.next() != module) {
            // skipping all leading nodes that are outside the loop
            --loopSize;
        }
        final ArrayList<Module> loop = new ArrayList<Module>(loopSize);
        loop.add(module);
        while (it.hasNext()) {
            loop.add(it.next());
        }
        assert loopSize == loop.size();
        throw new CyclicDependenciesDetectedException(loop);
    }
}
