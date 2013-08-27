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
import java.util.Set;

public class SerialDependencyResolver implements DependencyResolver
{
    private ArrayList<Node> shortlist;
    private Module moduleAcquired;
    
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
        shortlist = buildNodeGraph(rootModules);
        moduleAcquired = null;
    }
    
    // returns a module that does not have dependencies
    public Module getFreeModule()
    {
        ensureInitialised();
        if (moduleAcquired != null) {
            throw new IllegalStateException("#getFreeModule() is called when there is a module being processed.");
        }
        if (shortlist.isEmpty()) {
            return null;
        }
        /* Removing node from the graph here instead of #moduleProcessed to avoid searching
         * for the node when #moduleProcessed is invoked. All consistency and input validity
         * checks are performed so that the caller must follow the correct workflow.
         */
        final Node node = shortlist.remove(shortlist.size()-1);
        
        for (int j = 0, n = node.dependencyOf.size(); j < n; ++j) {
            final Node depOf = node.dependencyOf.get(j);
            if (--depOf.dependencyCount == 0) {
                // all modules with no dependencies go to the shortlist
                shortlist.add(depOf);
            }
        }
        return moduleAcquired = node.module;
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
        moduleAcquired = null;
    }
    
    private void ensureInitialised()
    {
        if (shortlist == null) {
            throw new IllegalStateException("Resolver is not initialised.");
        }
    }
    
    private static class Node
    {
        private Node(final Module module)
        {
            this.module = module;
            dependencyCount = module.getDependencies().size();
            dependencyOf = new ArrayList<Node>();
        }
        
        private final Module module;
        /* Knowing just dependency count is enough to detect the moment
           when this node has no dependencies remaining. */
        private int dependencyCount;
        private final ArrayList<Node> dependencyOf;
    }
    
    private static ArrayList<Node> buildNodeGraph(final Collection<Module> rootModules)
    {
        /* TODO it is known that there are no loops in the dependency graph.
           Use it knowledge to eliminate unnecessary checks OR merge buildNodeGraph() with
           ensureNoLoops() so that loops are checked for while the node graph is being built. */
        final IdentityHashMap<Module, Node> registry = new IdentityHashMap<Module, Node>();
        // shortlist stores the leaves of the inverted dependency graph.
        final ArrayList<Node> shortlist = new ArrayList<Node>();
        for (final Module module : rootModules) {
            addNodeDeep(module, shortlist, registry);
        }
        return shortlist;
    }
    
    private static void addNodeDeep(final Module module, final ArrayList<Node> shortlist,
            final IdentityHashMap<Module, Node> registry)
    {
        if (registry.containsKey(module)) {
            return; // the module is already processed 
        }
        
        final Node node = new Node(module);
        if (registry.put(node.module, node) != null) { // registering node assuming that each node could be registered only once.
            throw new IllegalStateException("Node already exists.");
        }
        
        final Set<Module> deps = module.getDependencies();
        if (deps.isEmpty()) {
            shortlist.add(node);
            return;
        }
        // inverted dependencies are assigned
        for (final Module dep : module.getDependencies()) {
            addNodeDeep(dep, shortlist, registry);
            final Node depNode = registry.get(dep);
            assert depNode != null;
            depNode.dependencyOf.add(node);
        }
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
