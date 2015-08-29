/* Copyright (c) 2013-2015, Dźmitry Laŭčuk
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

/**
 * <p>A {@link DependencyResolver} that supports multi-threaded {@link Module module}
 * processing. That is, at the moment multiple modules could be acquired for processing
 * (by different threads). An attempt to acquire a module if there are no modules with
 * no unprocessed dependencies will block the thread until such a module appears (other
 * threads must mark at least a single module as processed for this) or this
 * {@code ParallelDependencyResolver} is {@link #abort() aborted}. Refer to the class
 * description of {@code DependencyResolver} for more details.</p>
 * 
 * <p>As against {@link SerialDependencyResolver}, {@code ParallelDependencyResolver} is
 * much less efficient with respect to both processor and memory footprint which is
 * compensated by allowing for parallel processing or independent modules.</p>
 * 
 * <p>{@code ParallelDependencyResolver} is thread-safe.</p>
 * 
 * @see SerialDependencyResolver
 * @see CallTargetForModules
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ParallelDependencyResolver implements DependencyResolver
{
    private ArrayList<Node> shortlist;
    private IdentityHashMap<Module, Node> modulesAcquired;
    private int remainingModuleCount;
    
    /**
     * <p>Initialises this {@code ParallelDependencyResolver} with a set of {@link Module modules}
     * to process. The resulting set includes these root modules and all their direct and indirect
     * {@link Module#getDependencies() dependee modules}. If this {@code ParallelDependencyResolver}
     * is already initialised with another set of modules then its state is reset so that the new
     * set of modules is being used.</p>
     * 
     * @param rootModules the modules that constitute with their direct and indirect dependee
     *      modules a set of modules. The order of processing of modules in this set is to be
     *      resolved by this {@code ParallelDependencyResolver}. This collection and all of
     *      its elements must be non-{@code null}.
     * 
     * @throws CyclicDependenciesDetectedException if there are cyclic dependencies between
     *      the modules.
     * @throws NullPointerException if either <em>rootModules</em> or any of its elements
     *      is {@code null}.
     */
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
            final ArrayList<Node> newShortlist = new ArrayList<Node>();
            /* If buildNodeGraph() throws an exception then the state is not changed
               so that this ParallelDependencyResolver instance could be used as if
               this init() were not invoked. */
            remainingModuleCount = buildNodeGraph(rootModules, newShortlist);
            shortlist = newShortlist;
            modulesAcquired = new IdentityHashMap<Module, Node>();
        }
    }
    
    /**
     * <p>Returns a {@link Module module} that does not have {@link Module#getDependencies()
     * dependencies} unprocessed. If all modules are already processed then {@code null} is returned.
     * This {@code ParallelDependencyResolver} must be initialised before this function can be used.
     * Each module that is successfully processed must be released by invoking
     * {@link #moduleProcessed(Module)} with this module passed as a parameter. If this rule is not
     * followed then module processing could get stuck.</p>
     * 
     * <p>If this function is invoked when there are no modules with no unprocessed dependencies then
     * it blocks this thread waiting for a free module to appear. A different thread must report some
     * module previously acquired as processed for this function to return. Another way for this
     * function to return control is to invoke {@link #abort()}. In this case {@code null} is
     * returned.</p>
     * 
     * @return a module which has no unprocessed dependee modules, or {@code null} if all modules
     *      are already processed or this {@code ParallelDependencyResolver} is aborted.
     * 
     * @throws IllegalStateException if this {@code ParallelDependencyResolver} is not initialised.
     * @throws IllegalStateException if this function is waiting for a free module and the current
     *      thread is interrupted. The <em>interrupted status</em> of this thread is not reset in
     *      this case.
     */
    public synchronized Module getFreeModule()
    {
        ensureInitialised();
        
        try {
            while (shortlist.isEmpty()) {
                if (remainingModuleCount <= 0) {
                    // Either all modules are processed or #abort() has been called.
                    return null;
                }
                wait();
            }
            final Node node = shortlist.remove(shortlist.size()-1);
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
    
    /**
     * <p>Marks a given {@link Module module} as processed, so that the modules that depend upon
     * this module have one less unprocessed dependency. The modules for which this module is
     * the last unprocessed dependency become available for processing and can be acquired by
     * invoking {@link #getFreeModule()}. All threads waiting for a free module are notified
     * by this function.</p>
     * 
     * @param module the module to be marked as processed. It must belong to the set of modules
     *      this {@code ParallelDependencyResolver} is initialised with. It must be acquired for
     *      processing by invoking {@code getFreeModule()} before it is released by this function.
     *      It must be non-{@code null}.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if the given module does not belong to the modules this
     *      {@code ParallelDependencyResolver} is initialised with or if this module is not
     *      acquired for processing by {@code getFreeModule()}. This does not happen if this
     *      {@code ParallelDependencyResolver} is aborted.
     * @throws IllegalStateException if this {@code ParallelDependencyResolver} is not initialised.
     */
    public synchronized void moduleProcessed(final Module module)
    {
        ensureInitialised();
        if (module == null) {
            throw new NullPointerException("module");
        }
        if (remainingModuleCount < 0) {
            // #abort() has been called.
            return;
        }
        final Node node = modulesAcquired.remove(module);
        if (node == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The module ''{0}'' is not being processed.", module.getPath()));
        }
        for (int i = 0, n = node.dependencyOf.size(); i < n; ++i) {
            final Node depOf = node.dependencyOf.get(i);
            if (--depOf.dependencyCount == 0) {
                // all modules with no dependencies go to the shortlist
                shortlist.add(depOf);
            }
        }
        /* Notifying all threads after the module is removed and its dependencies are processed
           so that each thread waiting can either get a free module or finish execution. */
        notifyAll();
    }
    
    /**
     * <p>Aborts the module processing routine associated with this
     * {@code ParallelDependencyResolver} so that:</p>
     * <ul>
     *  <li>all threads waiting for a free module within {@link #getFreeModule()} are
     *      notified and {@code null} is returned by them to indicate that there are no
     *      more modules to process</li>
     *  <li>any subsequent invocation of {@code getFreeModule()} returns {@code null}</li>
     *  <li>any subsequent invocation of {@link #moduleProcessed(Module)} accepts any
     *      non-{@code null} module and just returns</li>
     * </ul>
     * <p>It is supposed that all threads that perform parallel module processing
     * will finish it rapidly after {@code abort()} is invoked.</p>
     * 
     * <p>It is safe to invoke {@code abort()} for an already aborted
     * {@code ParallelDependencyResolver}. Successful {@link #init(Collection)
     * re-initialisation} of a {@code ParallelDependencyResolver} resets its
     * <em>aborted status</em>.
     */
    public synchronized void abort()
    {
        ensureInitialised();
        shortlist.clear();
        modulesAcquired.clear(); // Tracking the acquired modules does not make sense anymore.
        remainingModuleCount = -1; // Indicates that #abort() has been called.
        notifyAll();
    }
    
    private void ensureInitialised()
    {
        if (shortlist == null) {
            throw new IllegalStateException("Resolver is not initialised.");
        }
    }
    
    private static class Node
    {
        Node(final Module module)
        {
            this.module = module;
            dependencyCount = module.getDependencies().size();
            dependencyOf = new ArrayList<Node>();
        }
        
        final Module module;
        /* Knowing just dependency count is enough to detect the moment
           when this node has no dependencies remaining. */
        int dependencyCount;
        final ArrayList<Node> dependencyOf;
    }
    
    /*
     * Builds a DAG which nodes hold modules and arcs that represent inverted module dependencies.
     * The list of nodes returned via shortlist contains the starting vertices of the graph.
     * The modules that are bound to these vertices do not have dependencies on other modules
     * and are used as modules to start unwinding dependencies from.
     * 
     * @returns the total number of modules.
     */
    private static int buildNodeGraph(final Collection<Module> rootModules, ArrayList<Node> shortlist)
            throws CyclicDependenciesDetectedException
    {
        final IdentityHashMap<Module, Node> registry = new IdentityHashMap<Module, Node>();
        final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
        for (final Module module : rootModules) {
            addNodeDeep(module, shortlist, registry, path);
        }
        // the number of nodes in the graph
        return registry.size();
    }
    
    // TODO reduce the number of parameters in this function (see SerialDependencyResolver#addModuleDeep).
    private static Node addNodeDeep(final Module module, final ArrayList<Node> shortlist,
            final IdentityHashMap<Module, Node> registry, final LinkedHashSet<Module> path)
            throws CyclicDependenciesDetectedException
    {
        Node node = registry.get(module);
        if (node != null) {
            return node; // the module is already processed
        }
        
        if (path.add(module)) {
            node = new Node(module);
            
            final Module[] deps = module.dependencies;
            if (deps.length == 0) {
                shortlist.add(node);
            } else {
                // inverted dependencies are assigned
                for (int i = 0, n = deps.length; i < n; ++i) {
                    final Module dep = deps[i];
                    final Node depNode = addNodeDeep(dep, shortlist, registry, path);
                    depNode.dependencyOf.add(node);
                }
            }
            
            registry.put(module, node);
            path.remove(module);
            return node;
        }
        
        /* A loop is detected. It does not necessarily end with the starting node,
         * some leading path elements could be truncated.
         * 
         * it.remove() has non-optional performance: just skipping to the module's
         * position and then copy the remaining modules to a list works faster.
         * However, this implementation is simpler and for an error case
         * the minor difference in performance does not matter.
         */
        final Iterator<Module> it = path.iterator();
        while (it.next() != module) {
            // skipping all leading modules that are outside the loop
            it.remove();
        }
        throw new CyclicDependenciesDetectedException(new ArrayList<Module>(path));
    }
}
