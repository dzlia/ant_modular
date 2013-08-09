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
import java.util.HashSet;
import java.util.IdentityHashMap;

public class SerialDependencyResolver implements DependencyResolver
{
    private HashSet<Node> nodes;
    
    public void init(final Collection<ModuleInfo> modules) throws CyclicDependenciesDetectedException
    {
        if (modules == null) {
            throw new NullPointerException("modules");
        }
        final IdentityHashMap<ModuleInfo, Node> registry = new IdentityHashMap<ModuleInfo, Node>();
        nodes = new HashSet<Node>();
        int i = 0;
        // TODO check that there are no missing modules and all nodes are initialised fully
        for (final ModuleInfo module : modules) {
            if (module == null) {
                throw new NullPointerException("modules contains null elements.");
            }
            final Node node = resolveNode(module, registry);
            // linking nodes in the same way as modules are linked
            for (final ModuleInfo dep : module.getDependencies()) {
                final Node depNode = resolveNode(dep, registry);
                node.dependencies.add(depNode);
                depNode.dependencyOf.add(node);
            }
            nodes.add(node);
        }
        // TODO add cyclic dependency checking
    }
    
    private static Node resolveNode(final ModuleInfo module, final IdentityHashMap<ModuleInfo, Node> registry)
    {
        Node node = registry.get(module);
        if (node == null) {
            node = new Node(module);
            registry.put(module, node);
        }
        return node;
    }
    
    // TODO prevent repeated calls of #getFreeModule without calling moduleProcessed
    // returns a module that does not have dependencies
    public ModuleInfo getFreeModule()
    {
        if (nodes.isEmpty()) {
            return null;
        }
        for (final Node node : nodes) {
            if (node.dependencies.size() == 0) {
                return node.module;
            }
        }
        throw new IllegalStateException(); // cyclic dependency detection does not work properly.
    }
    
    public void moduleProcessed(final ModuleInfo module)
    {
        if (module == null) {
            throw new NullPointerException("module");
        }
        
        // TODO improve performance
        for (final Node node : nodes) {
            if (node.module == module) {
                for (final Node depOf : node.dependencyOf) {
                    depOf.dependencies.remove(node);
                }
                nodes.remove(node);
                return;
            }
        }
        
        throw new IllegalArgumentException("Alien module is passed.");
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
}
