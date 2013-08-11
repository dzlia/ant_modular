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
    private HashSet<Node> nodes;
    private ModuleInfo moduleAcquired;
    
    public void init(final Collection<ModuleInfo> rootModules) throws CyclicDependenciesDetectedException
    {
        if (rootModules == null) {
            throw new NullPointerException("rootModules");
        }
        for (final ModuleInfo module : rootModules) {
            if (module == null) {
                throw new NullPointerException("rootModules contains null element.");
            }
        }
        ensureNoLoops(rootModules);
        nodes = buildNodeGraph(rootModules);
        moduleAcquired = null;
    }
    
    // returns a module that does not have dependencies
    public ModuleInfo getFreeModule()
    {
        ensureInitialised();
        if (moduleAcquired != null) {
            throw new IllegalStateException("#getFreeModule() is called when there is a module being processed.");
        }
        if (nodes.isEmpty()) {
            return null;
        }
        final Iterator<Node> i = nodes.iterator();
        do {
            final Node node = i.next();
            if (node.dependencies.size() == 0) {
                moduleAcquired = node.module;
                
                /* Removing node from the graph here instead of #moduleProcessed to avoid another
                 * search for the node when #moduleProcessed is invoked. All consistency and input validity
                 * checks are performed so that the caller must follow the correct workflow.
                 */
                for (final Node depOf : node.dependencyOf) {
                    depOf.dependencies.remove(node);
                }
                i.remove(); // means nodes.remove(node);
                
                return node.module;
            }
        } while (i.hasNext());
        throw new IllegalStateException(); // cyclic dependency detection does not work properly.
    }
    
    public void moduleProcessed(final ModuleInfo module)
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
        if (nodes == null) {
            throw new IllegalStateException("Resolver is not initialised.");
        }
    }
    
    private static class Node
    {
        private Node(final ModuleInfo module)
        {
            this.module = module;
            dependencies = new HashSet<Node>();
            dependencyOf = new HashSet<Node>();
        }
        
        private final ModuleInfo module;
        private final HashSet<Node> dependencies;
        private final HashSet<Node> dependencyOf;
    }
    
    private static HashSet<Node> buildNodeGraph(final Collection<ModuleInfo> rootModules)
    {
        final IdentityHashMap<ModuleInfo, Node> registry = new IdentityHashMap<ModuleInfo, Node>();
        final HashSet<Node> nodeGraph = new HashSet<Node>();
        for (final ModuleInfo module : rootModules) {
            addNodeDeep(module, nodeGraph, registry);
        }
        return nodeGraph;
    }
    
    private static void addNodeDeep(final ModuleInfo module, final HashSet<Node> nodeGraph,
            final IdentityHashMap<ModuleInfo, Node> registry)
    {
        if (registry.containsKey(module)) {
            return; // the module is already processed 
        }
        
        final Node node = new Node(module);
        if (registry.put(node.module, node) != null) { // registering node assuming that each node could be registered only once.
            throw new IllegalStateException("Node already exists.");
        }
        
        // linking nodes in the same way as modules are linked
        for (final ModuleInfo dep : module.getDependencies()) {
            addNodeDeep(dep, nodeGraph, registry);
            final Node depNode = registry.get(dep);
            assert depNode != null;
            node.dependencies.add(depNode);
            depNode.dependencyOf.add(node);
        }
        nodeGraph.add(node);
    }
    
    private static void ensureNoLoops(final Collection<ModuleInfo> modules) throws CyclicDependenciesDetectedException
    {
        // TODO re-write the code to re-use node graph created for dependency resolution
        final HashSet<Node> graph = buildNodeGraph(modules);
        /* Remove nodes without dependencies from the graph while this is possible.
           Non-empty resulting graph means there are loops in there. */
        boolean stuck;
        do {
            stuck = true;
            for (final Iterator<Node> i = graph.iterator(); i.hasNext();) {
                final Node node = i.next();
                if (node.dependencies.size() == 0) {
                    for (final Node depOf : node.dependencyOf) {
                        depOf.dependencies.remove(node);
                    }
                    i.remove(); // means graph.remove(node);
                    stuck = false;
                }
            }
        } while (!stuck);
        
        if (graph.isEmpty()) {
            return;
        }
        
        /* There are cycling dependencies detected. Obtaining here a single loop to report to the invoker.
         * 
         * The following holds:
         *  - each node has at least a single dependency
         *  - walking in any direction from any starting node within the graph will lead to a loop detected
         *  - the loop detected does not necessarily end with the starting node, some leading nodes could be truncated
         */
        final LinkedHashSet<Node> path = new LinkedHashSet<Node>();
        for (Node node = anyNode(graph);; node = anyNode(node.dependencies)) {
            if (!path.add(node)) { // the node is already visited so the loop is detected
                int loopSize = path.size();
                final Iterator<Node> it = path.iterator();
                while (it.next() != node) {
                    // skipping all leading nodes that are outside the loop
                    --loopSize;
                }
                final ArrayList<ModuleInfo> loop = new ArrayList<ModuleInfo>(loopSize);
                loop.add(node.module);
                while (it.hasNext()) {
                    loop.add(it.next().module);
                }
                assert loopSize == loop.size();
                throw new CyclicDependenciesDetectedException(loop);
            }
        }
    }
    
    private static Node anyNode(final HashSet<Node> nodes)
    {
        return nodes.iterator().next();
    }
}
