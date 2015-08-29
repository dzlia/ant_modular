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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class SerialDependencyResolver_ValidUseCasesTest extends TestCase
{
    private SerialDependencyResolver resolver;
    
    @Override
    protected void setUp()
    {
        resolver = new SerialDependencyResolver();
    }
    
    @Override
    protected void tearDown()
    {
        resolver = null;
    }
    
    public void testNoModules() throws Exception
    {
        resolver.init(Collections.<Module>emptyList());
        
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testSingleModule() throws Exception
    {
        final Module module = module("foo");
        
        resolver.init(Collections.singleton(module));
        
        assertSame(module, resolver.getFreeModule());
        resolver.moduleProcessed(module);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testTwoLinkedModules() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testTwoUnrelatedModules() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        
        resolver.init(Arrays.asList(module1, module2));
        
        final HashSet<Module> returnedModules = new HashSet<Module>();
        final Module m1 = resolver.getFreeModule();
        returnedModules.add(m1);
        resolver.moduleProcessed(m1);
        final Module m2 = resolver.getFreeModule();
        returnedModules.add(m2);
        resolver.moduleProcessed(m2);
        assertSame(null, resolver.getFreeModule());
        
        assertEquals(TestUtil.set(module1, module2), returnedModules);
    }
    
    public void testThreeModules_Chain() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module3.setDependencies(new Module[]{module1});
        
        resolver.init(Collections.singletonList(module3));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testThreeModules_Chain_AllModulesAreRoot() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module3.setDependencies(new Module[]{module1});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testThreeModules_TwoDependUponOne() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module3.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2, module3));

        final ArrayList<Module> order = flushModules(resolver, 3);
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

        final ArrayList<Module> order = flushModules(resolver, 3);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.indexOf(module1) < order.indexOf(module2));
        assertTrue(order.indexOf(module3) < order.indexOf(module2));
    }
    
    public void testFourModules_Diamond() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module2.setDependencies(new Module[]{module1, module3});
        module1.setDependencies(new Module[]{module4});
        module3.setDependencies(new Module[]{module4});
        
        resolver.init(Arrays.asList(module1, module2, module3, module4));

        final ArrayList<Module> order = flushModules(resolver, 4);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.contains(module4));
        assertTrue(order.indexOf(module1) < order.indexOf(module2));
        assertTrue(order.indexOf(module3) < order.indexOf(module2));
        assertTrue(order.indexOf(module4) < order.indexOf(module1));
        assertTrue(order.indexOf(module4) < order.indexOf(module3));
    }
    
    public void testFourModules_Branch() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module1, module2});
        module2.setDependencies(new Module[]{module4});
        
        resolver.init(Arrays.asList(module1, module2, module3, module4));

        final ArrayList<Module> order = flushModules(resolver, 4);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.contains(module4));
        assertTrue(order.indexOf(module1) < order.indexOf(module3));
        assertTrue(order.indexOf(module2) < order.indexOf(module3));
        assertTrue(order.indexOf(module4) < order.indexOf(module2));
    }
    
    public void testFourModules_ThreeAndSingle() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module1, module2});
        
        resolver.init(Arrays.asList(module1, module2, module3, module4));

        final ArrayList<Module> order = flushModules(resolver, 4);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.contains(module4));
        assertTrue(order.indexOf(module1) < order.indexOf(module3));
        assertTrue(order.indexOf(module2) < order.indexOf(module3));
    }
    
    public void testLoop_TwoModules()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module1});
        
        try {
            resolver.init(Arrays.asList(module1, module2));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->bar->]") + '|' + Pattern.quote("[->bar->foo->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testLoop_ThreeModules_AllInLoop()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module1});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz->bar->]") + '|' +
                    Pattern.quote("[->baz->bar->foo->]") + '|' +
                    Pattern.quote("[->bar->foo->baz->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testLoop_ThreeModules_TwoInLoop()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module1});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz->]") + '|' +
                    Pattern.quote("[->baz->foo->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testLoop_FourModules_AllInLoop()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module1.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module1});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz->bar->quux->]") + '|' +
                    Pattern.quote("[->quux->foo->baz->bar->]") + '|' +
                    Pattern.quote("[->bar->quux->foo->baz->]") + '|' +
                    Pattern.quote("[->baz->bar->quux->foo->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testLoop_FourModules_ThreeInLoop()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module1.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module1});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz->bar->]") + '|' +
                    Pattern.quote("[->bar->foo->baz->]") + '|' +
                    Pattern.quote("[->baz->bar->foo->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testLoop_FourModules_TwoLoops()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module1.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module1});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module2});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->foo->baz->]") + '|' +
                    Pattern.quote("[->baz->foo->]") + '|' +
                    Pattern.quote("[->bar->quux->]") + '|' +
                    Pattern.quote("[->quux->bar->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testNonRootModulesInDependencies_NoLoops() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        final Module module5 = module("flux");
        module3.setDependencies(new Module[]{module1, module2});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module5});
        
        resolver.init(Arrays.asList(module1, module3, module4));

        final ArrayList<Module> order = flushModules(resolver, 5);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.contains(module4));
        assertTrue(order.contains(module5));
        assertTrue(order.indexOf(module1) < order.indexOf(module3));
        assertTrue(order.indexOf(module2) < order.indexOf(module3));
        assertTrue(order.indexOf(module4) < order.indexOf(module2));
        assertTrue(order.indexOf(module5) < order.indexOf(module4));
    }
    
    public void testNonRootModulesInDependencies_Loop() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        final Module module5 = module("flux");
        module3.setDependencies(new Module[]{module1, module2});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module5});
        module5.setDependencies(new Module[]{module2});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->bar->quux->flux->]") + '|' +
                    Pattern.quote("[->flux->bar->quux->]") + '|' +
                    Pattern.quote("[->quux->flux->bar->]") + ")\\.", ex.getMessage()));
        }
    }
    
    /**
     * <p>Test description: though discouraged using different instances of Module with the same path is allowed.
     * SerialDependencyResolver must treat them as different modules.</p>
     */
    public void testDifferentModulesWithTheSamePath_NoLoops() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("bar");
        final Module module4 = module("bar");
        final Module module5 = module("flux");
        module3.setDependencies(new Module[]{module1, module2});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module5});
        
        resolver.init(Arrays.asList(module1, module3, module4));

        final ArrayList<Module> order = flushModules(resolver, 5);
        assertTrue(order.contains(module1));
        assertTrue(order.contains(module2));
        assertTrue(order.contains(module3));
        assertTrue(order.contains(module4));
        assertTrue(order.contains(module5));
        assertTrue(order.indexOf(module1) < order.indexOf(module3));
        assertTrue(order.indexOf(module2) < order.indexOf(module3));
        assertTrue(order.indexOf(module4) < order.indexOf(module2));
        assertTrue(order.indexOf(module5) < order.indexOf(module4));
    }
    
    /**
     * <p>Test description: though discouraged using different instances of Module with the same path is allowed.
     * SerialDependencyResolver must treat them as different modules.</p>
     */
    public void testDifferentModulesWithTheSamePath_Loop() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("bar");
        final Module module4 = module("bar");
        final Module module5 = module("flux");
        module3.setDependencies(new Module[]{module1, module2});
        module2.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module5});
        module5.setDependencies(new Module[]{module2});
        
        try {
            resolver.init(Arrays.asList(module1, module2, module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->bar->bar->flux->]") + '|' +
                    Pattern.quote("[->flux->bar->bar->]") + '|' +
                    Pattern.quote("[->bar->flux->bar->]") + ")\\.", ex.getMessage()));
        }
    }
    
    public void testReInit_InTheMiddle_ModuleNotAcquired() throws Exception
    {
        final Module module1 = module("foo/");
        final Module module2 = module("bar/");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        
        final Module module3 = module("baz/");
        final Module module4 = module("quux/");
        module3.setDependencies(new Module[]{module4});
        
        resolver.init(Arrays.asList(module3, module4));
        
        assertSame(module4, resolver.getFreeModule());
        resolver.moduleProcessed(module4);
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(null, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module2);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'bar/' is not being processed.", ex.getMessage());
        }
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInit_InTheMiddle_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo/");
        final Module module2 = module("bar/");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        
        final Module module3 = module("baz/");
        final Module module4 = module("quux/");
        module3.setDependencies(new Module[]{module4});
        
        resolver.init(Arrays.asList(module3, module4));
        
        assertSame(module4, resolver.getFreeModule());
        resolver.moduleProcessed(module4);
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(null, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module1);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo/' is not being processed.", ex.getMessage());
        }
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInit_InTheEnd_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo/");
        final Module module2 = module("bar/");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        
        final Module module3 = module("baz/");
        final Module module4 = module("quux/");
        module3.setDependencies(new Module[]{module4});
        
        resolver.init(Arrays.asList(module3, module4));
        
        assertSame(module4, resolver.getFreeModule());
        resolver.moduleProcessed(module4);
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(null, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module1);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo/' is not being processed.", ex.getMessage());
        }
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInitFailed_InTheMiddle_ModuleNotAcquired() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module3});
        
        try {
            resolver.init(Arrays.asList(module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->quux->baz->]") + '|' +
                    Pattern.quote("[->baz->quux->]") + ")\\.", ex.getMessage()));
        }
        
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInitFailed_InTheMiddle_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module3});
        
        try {
            resolver.init(Arrays.asList(module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->quux->baz->]") + '|' +
                    Pattern.quote("[->baz->quux->]") + ")\\.", ex.getMessage()));
        }
        
        try {
            resolver.moduleProcessed(module1);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo' is not being processed.", ex.getMessage());
        }
        resolver.moduleProcessed(module2);
        
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInitDueToNullModule_InTheMiddle_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module3});
        
        try {
            resolver.init(Arrays.asList(module3, null));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules contains null element.", ex.getMessage());
        }
        
        try {
            resolver.moduleProcessed(module1);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo' is not being processed.", ex.getMessage());
        }
        resolver.moduleProcessed(module2);
        
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInitDueToNullRootModules_InTheMiddle_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        
        try {
            resolver.init(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules", ex.getMessage());
        }
        
        try {
            resolver.moduleProcessed(module1);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo' is not being processed.", ex.getMessage());
        }
        resolver.moduleProcessed(module2);
        
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testReInitFailed_InTheEnd_ModuleAcquired() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module3.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module3});
        
        try {
            resolver.init(Arrays.asList(module3, module4));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->quux->baz->]") + '|' +
                    Pattern.quote("[->baz->quux->]") + ")\\.", ex.getMessage()));
        }
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testLoop_RootModuleIsNotIncluded()
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        final Module module4 = module("quux");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        module3.setDependencies(new Module[]{module4});
        module4.setDependencies(new Module[]{module3});
        
        try {
            resolver.init(Arrays.asList(module1));
            fail();
        }
        catch (CyclicDependenciesDetectedException ex) {
            assertTrue(ex.getMessage(), Pattern.matches("Cyclic dependencies detected: (?:" +
                    Pattern.quote("[->baz->quux->]") + '|' +
                    Pattern.quote("[->quux->baz->]") + ")\\.", ex.getMessage()));
        }
    }
    
    private static ArrayList<Module> flushModules(final SerialDependencyResolver resolver, final int moduleCount)
    {
        final ArrayList<Module> result = new ArrayList<Module>();
        for (int i = moduleCount; i > 0; --i) {
            final Module module = resolver.getFreeModule();
            result.add(module);
            resolver.moduleProcessed(module);
        }
        assertSame(null, resolver.getFreeModule());
        assertEquals(moduleCount, result.size());
        return result;
    }
    
    private Module module(final String path)
    {
        final Module result = new Module(path);
        result.setDependencies(new Module[0]);
        return result;
    }
}
