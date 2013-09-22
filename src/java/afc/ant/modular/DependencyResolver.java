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

/**
 * <p>Resolves dependencies between {@link Module modules}, that is it defines an order
 * in which a given set of modules is to be processed so that each module is processed
 * after all modules it depends upon are processed. The order of processing of independent
 * modules is undefined.</p>
 * 
 * <p>The lifecycle of a {@code DependencyResolver} instance is the following:
 * <ol type="1">
 *  <li>{@link #init(Collection)} is invoked with a collection of root modules
 *      passed it. All direct and indirect dependee modules of these root modules
 *      are involved into the dependency resolution process</li>
 *  <li>the caller invokes {@link #getFreeModule()} to acquire the next module which
 *      does not have its dependee modules unprocessed</li>
 *  <li>the caller executes the module processing routine on the module acquired</li>
 *  <li>when the processing is finished the caller invokes {@link #moduleProcessed(Module)}
 *      to report to this {@code DependencyResolver} that this module is processed so that
 *      the modules that depend upon this module have one less unprocessed dependency</li>
 *  <li>the steps <tt>2-4</tt> are repeated until there are no modules unprocessed, that is
 *      until {@link #getFreeModule()} returns {@code null}</li>
 * </ol>
 * If there are cyclic dependencies between modules (so that the order of module processing
 * is undefined) then a {@link CyclicDependenciesDetectedException} is thrown at the
 * step <tt>1</tt>.</p>
 * 
 * <p>In Java code the above described workflow could be written as follows:
 * <pre>
 * final Collection&lt;Module&gt; rootModules = getRootModules();
 * final DependencyResolver depResolver = createDependencyResolver();
 * try {
 *     depResolver.init(rootModules);
 *     
 *     Module module;
 *     while ((module = depResolver.getFreeModule()) != null) {
 *         processModule(module);
 *         
 *         depResolver.moduleProcessed(module);
 *     }
 * }
 * catch (CyclicDependenciesDetectedException ex) {
 *     // there are cyclic dependencies detected between modules
 * }</pre></p>
 * 
 * <p>Note that an implementation of {@code DependencyResolver} can allow multiple modules
 * with no dependencies unprocessed to be acquired simultaneously, to allow for their
 * processing in parallel. {@link ParallelDependencyResolver} is such an implementation, while
 * {@link SerialDependencyResolver} is not.</p>
 * 
 * @see SerialDependencyResolver
 * @see ParallelDependencyResolver
 * @see CallTargetForModules
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public interface DependencyResolver
{
    /**
     * <p>Initialises this {@code DependencyResolver} with a set of {@link Module modules} to
     * process. The resulting set includes these root modules and all their direct and indirect
     * {@link Module#getDependencies() dependee modules}. If this {@code DependencyResolver} is
     * already initialised with another set of modules then its state is reset so that the new set
     * of modules is being used.</p>
     * 
     * @param rootModules the modules that constitute with their direct and indirect dependee
     *      modules a set of modules. The order of processing of modules in this set is to be
     *      resolved by this {@code DependencyResolver}. This collection and all of its elements
     *      must be non-{@code null}.
     * 
     * @throws CyclicDependenciesDetectedException if there are cyclic dependencies between
     *      the modules.
     * @throws NullPointerException if either <em>rootModules</em> or any of its elements
     *      is {@code null}.
     */
    void init(Collection<Module> rootModules) throws CyclicDependenciesDetectedException;
    
    /**
     * <p>Returns a {@link Module module} that does not have {@link Module#getDependencies()
     * dependencies} unprocessed. If all modules are already processed then {@code null} is returned.
     * This {@code DependencyResolver} must be initialised before this function can be used.
     * Each module that is successfully processed must be released by invoking
     * {@link #moduleProcessed(Module)} with this module passed as a parameter.</p>
     * 
     * <p>Optionally, an implementation of {@code DependencyResolver} can support blocking waiting
     * for a free module if there are no such modules at the moment. In this case a different thread
     * must report some module previously acquired as processed for this function to return.</p>
     * 
     * @return a module which has no unprocessed dependee modules, or {@code null} if all modules
     *      are already processed.
     * 
     * @throws IllegalStateException if this {@code DependencyResolver} is not initialised or
     *      is in inconsistent state.
     * @throws IllegalStateException if this function is invoked when there are modules
     *      to be processed but: each module has unprocessed dependencies and this implementation
     *      does not support waiting for free modules, or a limit is reached on number of modules
     *      that can be processed at the same time.
     * @throws IllegalStateException if it is a blocking implementation and the current thread
     *      is interrupted while waiting for a free module. The <em>interrupted status</em>
     *      of this thread is not reset in this case.
     */
    Module getFreeModule();
    
    /**
     * <p>Marks a given {@link Module module} as processed, so that the modules that depend upon
     * this module have one less unprocessed dependency. The modules for which this module is
     * the last unprocessed dependency become available for processing and can be acquired by
     * invoking {@link #getFreeModule()}.</p>
     * 
     * @param module the module to be marked as processed. It must belong to the set of modules
     *      this {@code DependencyResolver} is initialised with. It must be acquired for processing
     *      by invoking {@code getFreeModule()} before it is released by this function. It must
     *      be non-{@code null}.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if the given module does not belong to the modules this
     *      {@code DependencyResolver} is initialised with or if this module is not acquired
     *      for processing by {@code getFreeModule()}.
     * @throws IllegalStateException if this {@code DependencyResolver} is not initialised or
     *      is inconsistent state.
     */
    void moduleProcessed(Module module);
}
