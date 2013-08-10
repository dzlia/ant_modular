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

import junit.framework.TestCase;

public class SerialDependencyResolver_InvalidUseCasesTest extends TestCase
{
    private SerialDependencyResolver resolver;
    
    @Override
    protected void setUp()
    {
        resolver = new SerialDependencyResolver();
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
            resolver.init(Arrays.asList(new ModuleInfo("foo"), null, new ModuleInfo("bar")));
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
            resolver.moduleProcessed(new ModuleInfo("foo"));
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("Resolver is not initialised.", ex.getMessage());
        }
    }
    
    public void testRepeatedCallGetFreeModule_ThereAreUnprocessedModules_NonLastModuleIsBeingProcessed() throws Exception
    {
        resolver.init(Arrays.asList(new ModuleInfo("foo"), new ModuleInfo("bar")));
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
        resolver.init(Arrays.asList(new ModuleInfo("foo"), new ModuleInfo("bar")));
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
        resolver.init(Arrays.asList(new ModuleInfo("foo"), new ModuleInfo("bar")));
        resolver.moduleProcessed(resolver.getFreeModule());
        resolver.moduleProcessed(resolver.getFreeModule());
        
        assertEquals(null, resolver.getFreeModule());
        assertEquals(null, resolver.getFreeModule()); // repeated #getFreeModule invocation
    }
    
    public void testRepeatedCallGetFreeModule_EmptyModuleList() throws Exception
    {
        resolver.init(Collections.<ModuleInfo>emptyList());
        
        assertEquals(null, resolver.getFreeModule());
        assertEquals(null, resolver.getFreeModule()); // repeated #getFreeModule invocation
    }
}
