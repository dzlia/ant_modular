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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

public class ModuleRegistryTest extends TestCase
{
    private MockModuleLoader loader;
    private ModuleRegistry registry;
    
    @Override
    protected void setUp()
    {
        loader = new MockModuleLoader();
        registry = new ModuleRegistry(loader);
    }
    
    @Override
    protected void tearDown()
    {
        registry = null;
        loader = null;
    }
    
    public void testCreateSingleModuleWithADependency_NoAttributes() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("bar");
        module.addDependency("bar");
        loader.results.put("foo/", module);
        loader.results.put("bar/", dep);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar/");
        
        assertModule(m1, "foo/", m2);
        assertModule(m2, "bar/");
        
        assertSame(m1, registry.resolveModule("foo"));
        
        assertEquals(2, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateSingleModuleWithADependency_WithAttributes() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        module.addAttribute("1", "2");
        module.addAttribute("3", "4");
        final ModuleInfo dep = new ModuleInfo("bar");
        final Object val = new Object();
        dep.addAttribute("5", val);
        module.addDependency("bar");
        loader.results.put("foo/", module);
        loader.results.put("bar/", dep);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        
        assertModule(m1, "foo/", TestUtil.<String, Object>map("1", "2", "3", "4"), m2);
        assertModule(m2, "bar/", TestUtil.<String, Object>map("5", val));
        
        assertSame(m1, registry.resolveModule("foo/"));
        
        assertEquals(2, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoIndependentModules() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo module2 = new ModuleInfo("bar");
        module2.addAttribute("qqq", "www");
        loader.results.put("foo/", module);
        loader.results.put("bar/", module2);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        final Module m3 = registry.resolveModule("foo");
        final Module m4 = registry.resolveModule("bar");
        
        assertEquals(2, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/"), new HashSet<String>(loader.paths));
        
        assertSame(m1, m3);
        assertSame(m2, m4);
        
        assertModule(m1, "foo/");
        assertModule(m2, "bar/", TestUtil.<String, Object>map("qqq", "www"));
        
        assertEquals(2, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModulesAndDirectDependencies() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("baz");
        final ModuleInfo dep2 = new ModuleInfo("quux");
        final ModuleInfo module2 = new ModuleInfo("bar");
        module.addDependency("baz");
        module.addDependency("quux");
        loader.results.put("foo/", module);
        loader.results.put("bar/", module2);
        loader.results.put("baz/", dep);
        loader.results.put("quux/", dep2);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        final Module m3 = registry.resolveModule("foo");
        final Module m4 = registry.resolveModule("bar");
        
        assertEquals(4, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "baz/", "quux/"), new HashSet<String>(loader.paths));
        
        assertSame(m1, m3);
        assertSame(m2, m4);
        
        final Module m5 = registry.resolveModule("baz");
        final Module m6 = registry.resolveModule("quux");
        
        assertModule(m1, "foo/", m5, m6);
        assertModule(m2, "bar/");
        assertModule(m5, "baz/");
        assertModule(m6, "quux/");
        
        assertEquals(4, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "baz/", "quux/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModulesAndDeepDependencies() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("baz");
        final ModuleInfo dep2 = new ModuleInfo("quux");
        final ModuleInfo dep3 = new ModuleInfo("zoo");
        final ModuleInfo module2 = new ModuleInfo("bar");
        module.addDependency("baz");
        module.addDependency("quux");
        dep2.addDependency("zoo");
        loader.results.put("foo/", module);
        loader.results.put("bar/", module2);
        loader.results.put("baz/", dep);
        loader.results.put("quux/", dep2);
        loader.results.put("zoo/", dep3);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        final Module m3 = registry.resolveModule("foo");
        final Module m4 = registry.resolveModule("bar");
        
        assertEquals(5, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "baz/", "quux/", "zoo/"), new HashSet<String>(loader.paths));
        
        assertSame(m1, m3);
        assertSame(m2, m4);
        
        final Module m5 = registry.resolveModule("baz");
        final Module m6 = registry.resolveModule("quux");
        final Module m7 = registry.resolveModule("zoo");
        final Module m8 = registry.resolveModule("zoo");
        
        assertModule(m1, "foo/", m5, m6);
        assertModule(m2, "bar/");
        assertModule(m5, "baz/");
        assertModule(m6, "quux/", m7);
        assertModule(m7, "zoo/");
        
        assertSame(m7, m8);
        
        assertEquals(5, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "baz/", "quux/", "zoo/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_ModuleNotLoadedException() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("quux");
        module.addDependency("quux");
        loader.results.put("foo/", module);
        loader.results.put("quux/", dep);
        
        final ModuleNotLoadedException exception = new ModuleNotLoadedException();
        loader.results.put("bar/", exception);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertSame(exception, ex);
        }
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            // the normalised path is expected in the exception message
            assertEquals("bar/", ex.getMessage());
        }
        
        final Module m3 = registry.resolveModule("quux");
        
        assertModule(m1, "foo/", m3);
        assertSame(m1, m2);
        assertModule(m3, "quux/");
        
        assertEquals(3, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "quux/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_RuntimeException() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("quux");
        module.addDependency("quux");
        final ModuleInfo module2 = new ModuleInfo("bar");
        loader.results.put("foo/", module);
        loader.results.put("quux/", dep);
        
        final RuntimeException exception = new RuntimeException();
        loader.results.put("bar/", exception);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (RuntimeException ex) {
            assertSame(exception, ex);
        }
        
        loader.results.put("bar/", module2);
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        final Module m4 = registry.resolveModule("quux");
        
        assertModule(m1, "foo/", m4);
        assertSame(m1, m2);
        assertModule(m3, "bar/");
        assertModule(m4, "quux/");
        
        assertEquals(4, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "quux/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_Error() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("quux");
        module.addDependency("quux");
        final ModuleInfo module2 = new ModuleInfo("bar");
        loader.results.put("foo/", module);
        loader.results.put("quux/", dep);
        
        final Error exception = new Error();
        loader.results.put("bar/", exception);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (Error ex) {
            assertSame(exception, ex);
        }
        
        loader.results.put("bar/", module2);
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        final Module m4 = registry.resolveModule("quux");
        
        assertModule(m1, "foo/", m4);
        assertSame(m1, m2);
        assertModule(m3, "bar/");
        assertModule(m4, "quux/");
        
        assertEquals(4, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "quux/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_NullIsReturned() throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo");
        final ModuleInfo dep = new ModuleInfo("quux");
        moduleInfo.addDependency("quux");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar");
        loader.results.put("foo/", moduleInfo);
        loader.results.put("quux/", dep);
        loader.results.put("bar/", null);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("Module loader returned null for the path 'bar/'.", ex.getMessage());
        }
        
        loader.results.put("bar/", moduleInfo2);
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        final Module m4 = registry.resolveModule("quux");
        
        assertModule(m1, "foo/", m4);
        assertSame(m1, m2);
        assertModule(m3, "bar/");
        assertModule(m4, "quux/");
        
        assertEquals(4, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "quux/"), new HashSet<String>(loader.paths));
    }
    
    public void testNullPath() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        loader.results.put("foo/", module);
        
        try {
            registry.resolveModule(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
        
        final Module m = registry.resolveModule("foo");
        
        assertModule(m, "foo/");
        
        assertEquals(Collections.singletonList("foo/"), loader.paths);
    }
    
    public void testCreateResolverWithNullModuleLoader()
    {
        try {
            new ModuleRegistry(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("moduleLoader", ex.getMessage());
        }
    }
    
    public void testCreateTwoModules_CyclicDependency() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        module.addDependency("bar");
        final ModuleInfo module2 = new ModuleInfo("bar");
        module2.addDependency("foo");
        loader.results.put("foo/", module);
        loader.results.put("bar/", module2);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        
        assertModule(m1, "foo/", m2);
        assertModule(m2, "bar/", m1);
        
        assertSame(m1, registry.resolveModule("foo"));
        assertSame(m2, registry.resolveModule("bar"));
        
        assertEquals(2, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/"), new HashSet<String>(loader.paths));
    }
    
    public void testCreateThreeModules_CyclicDependency() throws Exception
    {
        final ModuleInfo module = new ModuleInfo("foo");
        module.addDependency("bar");
        final ModuleInfo module2 = new ModuleInfo("bar");
        module2.addDependency("baz");
        final ModuleInfo module3 = new ModuleInfo("baz");
        module3.addDependency("foo");
        loader.results.put("foo/", module);
        loader.results.put("bar/", module2);
        loader.results.put("baz/", module3);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        final Module m3 = registry.resolveModule("baz");
        
        assertModule(m1, "foo/", m2);
        assertModule(m2, "bar/", m3);
        assertModule(m3, "baz/", m1);
        
        assertSame(m1, registry.resolveModule("foo"));
        assertSame(m2, registry.resolveModule("bar"));
        assertSame(m3, registry.resolveModule("baz"));
        
        assertEquals(3, loader.paths.size());
        assertEquals(TestUtil.set("foo/", "bar/", "baz/"), new HashSet<String>(loader.paths));
    }
    
    private static void assertModule(final Module module, final String path, final Module... dependencies)
    {
        assertModule(module, path, Collections.<String, Object>emptyMap(), dependencies);
    }
    
    private static void assertModule(final Module module, final String path, final Map<String, Object> attributes,
            final Module... dependencies)
    {
        assertNotNull(module);
        assertEquals(path, module.getPath());
        assertEquals(attributes, module.getAttributes());
        assertEquals(TestUtil.set(dependencies), module.getDependencies());
    }
    
    private static class MockModuleLoader implements ModuleLoader
    {
        public final ArrayList<String> paths = new ArrayList<String>();
        public final HashMap<String, Object> results = new HashMap<String, Object>();
        
        public void init(Project project)
        {
            throw new UnsupportedOperationException();
        }
        
        public ModuleInfo loadModule(final String path) throws ModuleNotLoadedException
        {
            assertNotNull(path);
            paths.add(path);
            assertTrue(results.containsKey(path));
            final Object result = results.remove(path);
            if (result instanceof ModuleNotLoadedException) {
                throw (ModuleNotLoadedException) result;
            }
            if (result instanceof RuntimeException) {
                throw (RuntimeException) result;
            }
            if (result instanceof Error) {
                throw (Error) result;
            }
            return (ModuleInfo) result;
        }
    }
}
