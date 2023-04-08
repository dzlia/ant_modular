/* Copyright (c) 2013-2023, Dźmitry Laŭčuk
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
package antmodular;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * <p>Resolves dependencies between {@link Module modules}, that is it defines an order
 * in which a given set of modules is to be processed so that each module is processed
 * after all modules it depends upon are processed. The order of processing of independent
 * modules is undefined.</p>
 * 
 * <p>The lifecycle of a {@code SerialDependencyResolver} instance is the following:</p>
 * <ol type="1">
 *  <li>{@link #init(Collection)} is invoked with a collection of root modules
 *      passed it. All direct and indirect dependee modules of these root modules
 *      are involved into the dependency resolution process</li>
 *  <li>the caller invokes {@link #getFreeModule()} to acquire the next module which
 *      does not have its dependee modules unprocessed</li>
 *  <li>the caller executes the module processing routine on the module acquired</li>
 *  <li>when the processing is finished the caller invokes {@link #moduleProcessed(Module)}
 *      to report to this {@code SerialDependencyResolver} that this module is processed so that
 *      the modules that depend upon this module have one less unprocessed dependency</li>
 *  <li>the steps <tt>2-4</tt> are repeated until there are no modules unprocessed, that is
 *      until {@link #getFreeModule()} returns {@code null}</li>
 * </ol>
 * <p>If there are cyclic dependencies between modules (so that the order of module processing
 * is undefined) then a {@link CyclicDependenciesDetectedException} is thrown at the
 * step <tt>1</tt>.</p>
 * 
 * <p>This dependency resolver supports only single-threaded {@link Module module} processing.
 * That is, at the moment at most a single module could be acquired for processing.
 * An attempt to acquire more than a single module leads to an {@link IllegalStateException}
 * thrown by {@link #getFreeModule()}.</p>
 * 
 * <p>As against {@link ParallelDependencyResolver}, {@code SerialDependencyResolver} is
 * much more efficient with respect to both processor and memory footprint.</p>
 * 
 * <p>{@code SerialDependencyResolver} is not thread-safe. It is expected to be used
 * within a single thread only.</p>
 * 
 * @see ParallelDependencyResolver
 * @see CallTargetForModules
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class SerialDependencyResolver
{
    private ArrayList<Module> moduleOrder;
    private Module moduleAcquired;
    private int pos;
    
    /**
     * <p>Initialises this {@code SerialDependencyResolver} with a set of {@link Module modules} to
     * process. The resulting set includes these root modules and all their direct and indirect
     * {@link Module#getDependencies() dependee modules}. If this {@code SerialDependencyResolver} is
     * already initialised with another set of modules then its state is reset so that the new set
     * of modules is being used.</p>
     * 
     * @param rootModules the modules that constitute with their direct and indirect dependee
     *      modules a set of modules. The order of processing of modules in this set is to be
     *      resolved by this {@code SerialDependencyResolver}. This collection and all of its
     *      elements must be non-{@code null}.
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
        moduleOrder = orderModules(rootModules);
        pos = 0;
        moduleAcquired = null;
    }
    
    /**
     * <p>Returns a {@link Module module} that does not have {@link Module#getDependencies()
     * dependencies} unprocessed. If all modules are already processed then {@code null} is returned.
     * This {@code SerialDependencyResolver} must be initialised before this function can be used.
     * Each module that is successfully processed must be released by invoking
     * {@link #moduleProcessed(Module)} with this module passed as a parameter. At most a single module
     * could be acquired for processing at the moment. An attempt to acquire more than a single module
     * leads to an {@link IllegalStateException} thrown.</p>
     * 
     * @return a module which has no unprocessed dependee modules, or {@code null} if all modules
     *      are already processed.
     * 
     * @throws IllegalStateException if this {@code SerialDependencyResolver} is not initialised.
     * @throws IllegalStateException if this function is invoked when there is a module acquired.
     */
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
    
    /**
     * <p>Marks a given {@link Module module} as processed, so that the modules that depend upon
     * this module have one less unprocessed dependency. The modules for which this module is
     * the last unprocessed dependency become available for processing and can be acquired by
     * invoking {@link #getFreeModule()}.</p>
     * 
     * @param module the module to be marked as processed. It must belong to the set of modules
     *      this {@code SerialDependencyResolver} is initialised with. It must be acquired for
     *      processing by invoking {@code getFreeModule()} before it is released by this function.
     *      It must be non-{@code null}.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if the given module does not belong to the modules this
     *      {@code SerialDependencyResolver} is initialised with or if this module is not
     *      acquired for processing by {@code getFreeModule()}.
     * @throws IllegalStateException if this {@code SerialDependencyResolver} is not initialised.
     */
    public void moduleProcessed(final Module module)
    {
        ensureInitialised();
        if (module == null) {
            throw new NullPointerException("module");
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
            throws CyclicDependenciesDetectedException
    {
        final Context ctx = new Context();
        for (final Module module : rootModules) {
            addModuleDeep(module, ctx);
        }
        return ctx.moduleOrder;
    }
    
    /* Data that is used by addModuleDeep. These objects are the same at each step of the recursion
     * so there is no need to pass them again and again thus wasting stack space. The instance of
     * Context being used is likely to be in the processor cache so that its fields are accessed
     * with little overhead.
     */
    private static class Context
    {
        final IdentityHashMap<Module, ?> registry = new IdentityHashMap<Module, Object>();
        final LinkedHashSet<Module> path = new LinkedHashSet<Module>();
        final ArrayList<Module> moduleOrder = new ArrayList<Module>();
        
        // Prevents uncontrollable synthetic code (including classes) to be generated by a java compiler.
        Context()
        {
        }
    }
    
    /* TODO think if path could be used as an array-based stack and the status of the modules
       is set as registry module. However, the current implementation shows itself to be slightly faster.*/
    private static void addModuleDeep(final Module module, final Context ctx)
            throws CyclicDependenciesDetectedException
    {
        if (ctx.registry.containsKey(module)) {
            return; // the module is already processed
        }
        
        if (ctx.path.add(module)) {
            // the dependee modules are added before this module
            final Module[] deps = module.dependencies;
            for (int i = 0, n = deps.length; i < n; ++i) {
                final Module dep = deps[i];
                addModuleDeep(dep, ctx);
            }
            ctx.path.remove(module);
            ctx.registry.put(module, null);
            ctx.moduleOrder.add(module);
            return;
        }
        
        /* A loop is detected. It does not necessarily end with the starting node,
         * some leading path elements could be truncated.
         * 
         * it.remove() has non-optional performance: just skipping to the module's
         * position and then copy the remaining modules to a list works faster.
         * However, this implementation is simpler and for an error case
         * the minor difference in performance does not matter.
         */
        final Iterator<Module> it = ctx.path.iterator();
        while (it.next() != module) {
            // skipping all leading modules that are outside the loop
            it.remove();
        }
        throw new CyclicDependenciesDetectedException(new ArrayList<Module>(ctx.path));
    }
}
