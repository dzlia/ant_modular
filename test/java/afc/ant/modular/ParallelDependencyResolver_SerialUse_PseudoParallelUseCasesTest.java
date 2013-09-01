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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
        final Module module1 = new Module("foo");
        final Module module2 = new Module("bar");
        
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
        final Module module1 = new Module("foo");
        final Module module2 = new Module("bar");
        final Module module3 = new Module("baz");
        module1.addDependency(module2);
        module3.addDependency(module2);
        
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
        final Module module1 = new Module("foo");
        final Module module2 = new Module("bar");
        final Module module3 = new Module("baz");
        module2.addDependency(module1);
        module2.addDependency(module3);
        
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
        final Module module1 = new Module("foo");
        final Module module2 = new Module("bar");
        final Module module3 = new Module("baz");
        module2.addDependency(module1);
        module2.addDependency(module3);
        
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
}
