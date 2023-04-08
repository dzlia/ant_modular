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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import antmodular.Module;
import antmodular.ParallelDependencyResolver;

import junit.framework.TestCase;

/**
 * <p>Tests cases when {@link ParallelDependencyResolver} is used by a single thread but
 * multiple modules are acquired simultaneously.</p>
 *
 * @author @author D&#378;mitry La&#365;&#269;uk
 */
public class ParallelDependencyResolver_SerialUse_PseudoParallelUseCasesTest extends TestCase
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
    
    public void testTwoUnrelatedModules() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        
        resolver.init(Arrays.asList(module1, module2));
        
        final HashSet<Module> returnedModules = new HashSet<Module>();
        final Module m1 = resolver.getFreeModule();
        final Module m2 = resolver.getFreeModule();
        returnedModules.add(m1);
        returnedModules.add(m2);
        resolver.moduleProcessed(m1);
        resolver.moduleProcessed(m2);
        assertSame(null, resolver.getFreeModule());
        
        assertEquals(TestUtil.set(module1, module2), returnedModules);
    }
    
    public void testThreeModules_TwoDependUponOne() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module3.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        final Module m1 = resolver.getFreeModule();
        resolver.moduleProcessed(m1);
        final Module m2 = resolver.getFreeModule();
        final Module m3 = resolver.getFreeModule();
        resolver.moduleProcessed(m2);
        resolver.moduleProcessed(m3);
        assertEquals(null, resolver.getFreeModule());
        
        final List<Module> order = Arrays.asList(m1, m2, m3);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.indexOf(module2) < order.indexOf(module1));
        assertTrue(order.indexOf(module2) < order.indexOf(module3));
    }
    
    public void testThreeModules_OneDependsUponTwo() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module2.setDependencies(new Module[]{module1, module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));

        final Module m1 = resolver.getFreeModule();
        final Module m2 = resolver.getFreeModule();
        resolver.moduleProcessed(m1);
        resolver.moduleProcessed(m2);
        final Module m3 = resolver.getFreeModule();
        resolver.moduleProcessed(m3);
        assertEquals(null, resolver.getFreeModule());
        
        final List<Module> order = Arrays.asList(m1, m2, m3);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.indexOf(module1) < order.indexOf(module2));
        assertTrue(order.indexOf(module3) < order.indexOf(module2));
    }
    
    public void testThreeModules_OneDependsUponTwo_ReleaseNonAquiredModule() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module2.setDependencies(new Module[]{module1, module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));

        final Module m1 = resolver.getFreeModule();
        final Module m2 = resolver.getFreeModule();
        resolver.moduleProcessed(m1);
        resolver.moduleProcessed(m2);
        
        try {
            resolver.moduleProcessed(module3);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'baz' is not being processed.", ex.getMessage());
        }
        
        final Module m3 = resolver.getFreeModule();
        resolver.moduleProcessed(m3);
        assertEquals(null, resolver.getFreeModule());
        
        final List<Module> order = Arrays.asList(m1, m2, m3);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.indexOf(module1) < order.indexOf(module2));
        assertTrue(order.indexOf(module3) < order.indexOf(module2));
    }
    
    public void testThreeModules_AbortInTheMiddle() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1));
        
        final Module m1 = resolver.getFreeModule();
        
        resolver.abort();
        
        final Module m2 = resolver.getFreeModule();
        
        resolver.moduleProcessed(m1);
        
        final Module m3 = resolver.getFreeModule();
        
        assertSame(module3, m1);
        assertNull(m2);
        assertNull(m3);
    }
    
    public void testThreeModules_MultipleAbortInTheMiddle() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module2.setDependencies(new Module[]{module1, module3});
        
        resolver.init(Arrays.asList(module2));

        final Module m1 = resolver.getFreeModule();
        final Module m2 = resolver.getFreeModule();
        
        resolver.abort();
        resolver.abort();
        
        final Module m3 = resolver.getFreeModule();
        
        assertNull(m3);
        
        resolver.moduleProcessed(m1);
        
        final Module m4 = resolver.getFreeModule();
        
        assertNull(m4);
        
        resolver.moduleProcessed(m2);
        
        final Module m5 = resolver.getFreeModule();
        
        assertNull(m5);
        
        assertEquals(TestUtil.set(module1, module3), TestUtil.set(m1, m2));
    }
    
    /**
     * <p>Tests that execution of resolver#getFreeModule() ends with {@link IllegalStateException}
     * thrown if this thread gets interrupted while it is waiting for a free module.</p>
     */
    public void testModuleProcessingThreadWaitsForFreeModule_ThreadInterrupted() throws Throwable
    {
        final AtomicReference<Throwable> testFailureCause = new AtomicReference<Throwable>();
        final CyclicBarrier b = new CyclicBarrier(2);
        
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    final Module module1 = module("foo");
                    final Module module2 = module("bar");
                    final Module module3 = module("baz");
                    module2.setDependencies(new Module[]{module1, module3});
                    
                    resolver.init(Arrays.asList(module2));
                    resolver.getFreeModule();
                    resolver.getFreeModule();
                    
                    /* Ensures that the code below the barrier is executed before the code in the test thread.
                     * This works because ParallelDependencyResolver#getFreeModule waits against the resolver
                     * itself so that while it waits the resolver's monitor gets released.
                     */
                    synchronized (resolver) {
                        b.await();
                        resolver.getFreeModule(); // hangs up here
                    }
                    fail();
                }
                catch (IllegalStateException ex) {
                    // expected
                }
                catch (Throwable ex) {
                    testFailureCause.set(ex);
                }
            }
        };
        
        t.start();
        
        b.await();
        // Ensures that the code within is executed after the code in the resolver thread.
        synchronized (resolver) {
            t.interrupt();
        }
        t.join();
        
        if (testFailureCause.get() != null) {
            throw testFailureCause.get();
        }
    }
    
    /**
     * <p>Tests that ParallelDependencyResolver#abort() wakes all threads that wait
     * for free modules from this resolver and that these threads get {@code null}
     * even though there were modules to process.</p>
     */
    public void testModuleProcessingThreadWaitsForFreeModule_ResolverIsAborted() throws Throwable
    {
        final AtomicReference<Throwable> testFailureCause = new AtomicReference<Throwable>();
        final CyclicBarrier b = new CyclicBarrier(2);
        
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    final Module module1 = module("foo");
                    final Module module2 = module("bar");
                    final Module module3 = module("baz");
                    module2.setDependencies(new Module[]{module1, module3});
                    
                    resolver.init(Arrays.asList(module2));
                    resolver.getFreeModule();
                    resolver.getFreeModule();
                    
                    final Module m;
                    
                    /* Ensures that the code below the barrier is executed before the code in the test thread.
                     * This works because ParallelDependencyResolver#getFreeModule waits against the resolver
                     * itself so that while it waits the resolver's monitor gets released.
                     */
                    synchronized (resolver) {
                        b.await();
                        m = resolver.getFreeModule(); // hangs up here
                    }
                    
                    assertNull(m);
                    assertNull(resolver.getFreeModule());
                }
                catch (Throwable ex) {
                    testFailureCause.set(ex);
                }
            }
        };
        
        t.start();
        
        b.await();
        // Ensures that the code within is executed after the code in the resolver thread.
        synchronized (resolver) {
            resolver.abort();
        }
        t.join();
        
        if (testFailureCause.get() != null) {
            throw testFailureCause.get();
        }
    }
    
    private Module module(final String path)
    {
        final Module result = new Module(path);
        result.setDependencies(new Module[0]);
        return result;
    }
}
