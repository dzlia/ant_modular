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
package antmodular;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import antmodular.Module;
import antmodular.ParallelDependencyResolver;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * <p>Tests cases when {@link ParallelDependencyResolver} is used by multiple threads.</p>
 *
 * @author @author D&#378;mitry La&#365;&#269;uk
 */
public class ParallelDependencyResolver_ParalleUseTest extends TestCase
{
    private ParallelDependencyResolver resolver;
    
    @Override
    protected void setUp()
    {
        resolver = new ParallelDependencyResolver();
    }
    
    @Override
    protected void tearDown()
    {
        resolver = null;
    }
    
    /**
     * <p>The module depend upon others in such a way that at the moment there is only
     * a single module that could be processed.</p>
     */
    public void testManyModulesAndThreads_DenseModuleGraph() throws Exception
    {
        final int n = 100;
        final ArrayList<Module> modules = new ArrayList<Module>(n);
        
        for (int i = 0; i < n; ++i) {
            final Module m = new Module("does_not_matter");
            m.setDependencies(modules.toArray(new Module[i]));
            modules.add(m);
        }
        
        resolver.init(modules);
        
        final Queue<Module> order = executeConcurrently(resolver, 10);
        
        assertNotNull(order);
        assertEquals(modules, new ArrayList<Module>(order));
    }
    
    /**
     * <p>The module depend upon others in such a way that at the moment there could be
     * many modules that could be processed in parallel.</p>
     */
    public void testManyModulesAndThreads_SparseModuleGraph() throws Exception
    {
        final int n = 100;
        final ArrayList<Module> modules = new ArrayList<Module>(n);
        
        // The seed value makes the module graph the same for different runs of the test in the same JVM.
        final Random rand = new Random(25);
        for (int i = 0; i < n; ++i) {
            final Module m = new Module("does_not_matter");
            // j is assigned with some pseudo-random value so that different modules are used as dependencies.
            final ArrayList<Module> deps = new ArrayList<Module>();
            for (int s = modules.size(), j = Math.min(s, rand.nextInt(10)); j < s; j += 5) {
                deps.add(modules.get(j));
            }
            m.setDependencies(deps.toArray(new Module[deps.size()]));
            modules.add(m);
        }
        
        resolver.init(modules);
        
        final Queue<Module> order = executeConcurrently(resolver, 10);
        
        assertNotNull(order);
        final ArrayList<Module> list = new ArrayList<Module>(order);
        assertEquals(n, list.size());
        
        for (int i = 0; i < n; ++i) {
            final Module module = list.get(i);
            for (final Module dep : module.dependencies) {
                final int depPos = list.indexOf(dep);
                assertTrue(depPos >= 0);
                assertTrue(i > depPos);
            }
        }
    }
    
    private static Queue<Module> executeConcurrently(final ParallelDependencyResolver resolver, final int threadCount)
            throws Exception
    {
        final ConcurrentLinkedQueue<Module> order = new ConcurrentLinkedQueue<Module>();
        final Thread[] threads = new Thread[threadCount];
        final CyclicBarrier barrier = new CyclicBarrier(threadCount);
        final AtomicBoolean failure = new AtomicBoolean();
        final AtomicReference<Throwable> failureCause = new AtomicReference<Throwable>();
        
        for (int i = 0; i < threadCount; ++i) {
            final Thread t = new Thread() {
                @Override
                public void run()
                {
                    try {
                        barrier.await();
                        
                        Module m;
                        while (!failure.get() && (m = resolver.getFreeModule()) != null) {
                            order.add(m);
                            resolver.moduleProcessed(m);
                        }
                    }
                    catch (Throwable ex) {
                        failure.set(true);
                        failureCause.set(ex);
                    }
                }
            };
            threads[i] = t;
            t.start();
        }
        
        for (int i = 0; i < threadCount; ++i) {
            // TODO The test could hand up here. Resolve this.
            threads[i].join();
        }
        
        if (failure.get()) { // failing the test if not all threads has executed successfully.
            final AssertionFailedError ex = new AssertionFailedError();
            ex.initCause(failureCause.get());
            throw ex;
        }
        
        return order;
    }
}
