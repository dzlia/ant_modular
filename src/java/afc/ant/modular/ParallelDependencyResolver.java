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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ParallelDependencyResolver implements DependencyResolver
{
    private LinkedBlockingQueue<Node> shortlist;
    private IdentityHashMap<Module, Node> modulesAcquired;
    private int remainingModuleCount;
    
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
        synchronized (this) {
            shortlist = new LinkedBlockingQueue<Node>();
            remainingModuleCount = buildNodeGraph(rootModules, shortlist);
            modulesAcquired = new IdentityHashMap<Module, Node>();
        }
    }
    
    // returns a module that does not have dependencies
    public synchronized Module getFreeModule()
    {
        ensureInitialised();
        if (remainingModuleCount == 0) {
            return null;
        }
        
        try {
            final Node node = shortlist.take();
            final Module module = node.module;
            modulesAcquired.put(module, node);
            --remainingModuleCount;
            
            return module;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
    }
    
    public synchronized void moduleProcessed(final Module module)
    {
        ensureInitialised();
        if (module == null) {
            throw new NullPointerException("module");
        }
        try {
            final Node node = modulesAcquired.remove(module);
            if (node == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The module ''{0}'' is not being processed.", module.getPath()));
            }
            for (int j = 0, n = node.dependencyOf.size(); j < n; ++j) {
                final Node depOf = node.dependencyOf.get(j);
                if (--depOf.dependencyCount == 0) {
                    // all modules with no dependencies go to the shortlist
                    shortlist.put(depOf);
                }
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
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
    
    /*
     * Builds a DAG which nodes hold modules and arcs that represent inverted module dependencies.
     * The list of nodes returned via shortlist contains the starting vertices of the graph.
     * The modules that are bound to these vertices do not have dependencies on other modules
     * and are used as modules to start unwinding dependencies from.
     * 
     * @returns the total number of modules.
     */
    private static int buildNodeGraph(final Collection<Module> rootModules, LinkedBlockingQueue<Node> shortlist)
            throws CyclicDependenciesDetectedException
    {
        /* TODO it is known that there are no loops in the dependency graph.
           Use it knowledge to eliminate unnecessary checks OR merge buildNodeGraph() with
           ensureNoLoops() so that loops are checked for while the node graph is being built. */
        final IdentityHashMap<Module, Node> registry = new IdentityHashMap<Module, Node>();
        final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
        for (final Module module : rootModules) {
            addNodeDeep(module, shortlist, registry, path);
        }
        // the number 
        return registry.size();
    }
    
    private static Node addNodeDeep(final Module module, final LinkedBlockingQueue<Node> shortlist,
            final IdentityHashMap<Module, Node> registry, final LinkedHashSet<Module> path)
            throws CyclicDependenciesDetectedException
    {
        Node node = registry.get(module);
        if (node != null) {
            return node; // the module is already processed
        }
        
        if (path.add(module)) {
            node = new Node(module);
            registry.put(module, node);
            
            final Set<Module> deps = module.getDependencies();
            if (deps.isEmpty()) {
                shortlist.add(node);
            } else {
                // inverted dependencies are assigned
                for (final Module dep : module.getDependencies()) {
                    final Node depNode = addNodeDeep(dep, shortlist, registry, path);
                    assert depNode != null;
                    depNode.dependencyOf.add(node);
                }
            }
            
            path.remove(module);
            return node;
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
