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

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

public class SerialDependencyResolver_InvalidUseCasesTest extends TestCase
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
    
    public void testNullModuleList() throws Exception
    {
        try {
            resolver.init(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules", ex.getMessage());
        }
    }
    
    public void testNullModuleInTheRootModuleList() throws Exception
    {
        try {
            resolver.init(Arrays.asList(module("foo"), null, module("bar")));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules contains null element.", ex.getMessage());
        }
    }
    
    public void testUseNonInitialisedResolver_GetFreeModule()
    {
        try {
            resolver.getFreeModule();
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("Resolver is not initialised.", ex.getMessage());
        }
    }
    
    public void testUseNonInitialisedResolver_ModuleProcessed()
    {
        try {
            resolver.moduleProcessed(module("foo"));
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("Resolver is not initialised.", ex.getMessage());
        }
    }
    
    public void testRepeatedCallGetFreeModule_ThereAreUnprocessedModules_NonLastModuleIsBeingProcessed() throws Exception
    {
        resolver.init(Arrays.asList(module("foo"), module("bar")));
        resolver.getFreeModule();
        
        try {
            resolver.getFreeModule();
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("#getFreeModule() is called when there is a module being processed.", ex.getMessage());
        }
    }
    
    public void testRepeatedCallGetFreeModule_ThereAreUnprocessedModules_LastModuleIsBeingProcessed() throws Exception
    {
        resolver.init(Arrays.asList(module("foo"), module("bar")));
        resolver.moduleProcessed(resolver.getFreeModule());
        resolver.getFreeModule();
        
        try {
            resolver.getFreeModule();
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("#getFreeModule() is called when there is a module being processed.", ex.getMessage());
        }
    }
    
    public void testRepeatedCallGetFreeModule_ThereAreNoUnprocessedModules() throws Exception
    {
        resolver.init(Arrays.asList(module("foo"), module("bar")));
        resolver.moduleProcessed(resolver.getFreeModule());
        resolver.moduleProcessed(resolver.getFreeModule());
        
        assertEquals(null, resolver.getFreeModule());
        assertEquals(null, resolver.getFreeModule()); // repeated #getFreeModule invocation
    }
    
    public void testRepeatedCallGetFreeModule_EmptyModuleList() throws Exception
    {
        resolver.init(Collections.<Module>emptyList());
        
        assertEquals(null, resolver.getFreeModule());
        assertEquals(null, resolver.getFreeModule()); // repeated #getFreeModule invocation
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_AlienModule_NoModuleWasEverProcessed() throws Exception
    {
        resolver.init(Arrays.asList(module("foo/"), module("bar/")));
        
        try {
            resolver.moduleProcessed(module("baz/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'baz/' is not being processed.", ex.getMessage());
        }
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_AlienModule_SomeModulesWereProcessed() throws Exception
    {
        resolver.init(Arrays.asList(module("foo/"), module("bar/")));
        resolver.moduleProcessed(resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module("baz/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'baz/' is not being processed.", ex.getMessage());
        }
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_NativeModule_NoModuleWasEverProcessed() throws Exception
    {
        final Module module1 = module("foo/");
        final Module module2 = module("bar/");
        final Module module3 = module("baz/");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        try {
            resolver.moduleProcessed(module3); // module3 is processed first because of dependencies
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'baz/' is not being processed.", ex.getMessage());
        }
        
        assertSame(module3, resolver.getFreeModule());
        resolver.moduleProcessed(module3);
        assertSame(module2, resolver.getFreeModule());
        resolver.moduleProcessed(module2);
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_NativeModule_SomeModulesWereProcessed() throws Exception
    {
        final Module module1 = module("foo/");
        final Module module2 = module("bar/");
        final Module module3 = module("baz/");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        resolver.moduleProcessed(resolver.getFreeModule());
        resolver.moduleProcessed(resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module1); // module1 is processed last because of dependencies
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'foo/' is not being processed.", ex.getMessage());
        }
        
        assertSame(module1, resolver.getFreeModule());
        resolver.moduleProcessed(module1);
        assertSame(null, resolver.getFreeModule());
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_NullModule_ThereAreModulesInQueue() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        resolver.moduleProcessed(resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
        
        assertSame(module2, resolver.getFreeModule());
    }
    
    public void testCallModuleProcessed_NoModuleIsBeingProcessed_NullModule_ThereAreNoModulesInQueue() throws Exception
    {
        final Module module1 = module("foo");
        
        resolver.init(Arrays.asList(module1));
        
        resolver.moduleProcessed(resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
        
        assertNull(resolver.getFreeModule());
    }
    
    public void testCallModuleProcessed_SomeModuleIsBeingProcessed_AlienModule() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        module1.setDependencies(new Module[]{module2});
        resolver.init(Arrays.asList(module1, module2));
        
        assertSame(module2, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module("baz"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'baz' is not being processed.", ex.getMessage());
        }
    }
    
    public void testCallModuleProcessed_SomeModuleIsBeingProcessed_WrongNativeModule() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        assertSame(module3, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(module2);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("The module 'bar' is not being processed.", ex.getMessage());
        }
        
        resolver.moduleProcessed(module3);
        assertSame(module2, resolver.getFreeModule());
    }
    
    public void testCallModuleProcessed_SomeModuleIsBeingProcessed_NullModule_ThereAreModulesInQueue() throws Exception
    {
        final Module module1 = module("foo");
        final Module module2 = module("bar");
        final Module module3 = module("baz");
        module1.setDependencies(new Module[]{module2});
        module2.setDependencies(new Module[]{module3});
        
        resolver.init(Arrays.asList(module1, module2, module3));
        
        assertSame(module3, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
    }
    
    public void testCallModuleProcessed_SomeModuleIsBeingProcessed_NullModule_ThereAreNoModulesInQueue() throws Exception
    {
        final Module module1 = module("foo");
        
        resolver.init(Arrays.asList(module1));
        
        assertSame(module1, resolver.getFreeModule());
        
        try {
            resolver.moduleProcessed(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
    }
    
    private Module module(final String path)
    {
        final Module result = new Module(path);
        result.setDependencies(new Module[0]);
        return result;
    }
}
