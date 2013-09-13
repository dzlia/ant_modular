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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;

import junit.framework.TestCase;

public class ModuleUtilTest extends TestCase
{
    public void testIsModule_NullValue()
    {
        assertFalse(ModuleUtil.isModule(null));
    }
    
    public void testIsModule_NotModule()
    {
        assertFalse(ModuleUtil.isModule(new Object()));
    }
    
    public void testIsModule_ModuleInfo()
    {
        assertFalse(ModuleUtil.isModule(new ModuleInfo("foo")));
    }
    
    public void testIsModule_Module()
    {
        assertTrue(ModuleUtil.isModule(new Module("foo/")));
    }
    
    public void testIsModule_Module_DifferentClassLoader() throws Exception
    {
        final Object module = createModuleWithDifferentClassLoader("foo/", new ModuleClassLoader());
        
        assertTrue(ModuleUtil.isModule(module));
    }
    
    public void testGetPath_NullModule()
    {
        try {
            ModuleUtil.getPath(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
    }
    
    public void testGetPath_NonModule()
    {
        try {
            ModuleUtil.getPath(new Object());
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + Object.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetPath_ModuleInfo()
    {
        try {
            ModuleUtil.getPath(new ModuleInfo("foo/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + ModuleInfo.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetPath_Module()
    {
        assertSame("foo/", ModuleUtil.getPath(new Module("foo/")));
    }
    
    public void testGetPath_Module_DifferentClassLoader() throws Exception
    {
        assertSame("foo/", ModuleUtil.getPath(createModuleWithDifferentClassLoader("foo/", new ModuleClassLoader())));
    }
    
    public void testGetDependencies_NullModule()
    {
        try {
            ModuleUtil.getDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
    }
    
    public void testGetDependencies_NonModule()
    {
        try {
            ModuleUtil.getDependencies(new Object());
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + Object.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetDependencies_ModuleInfo()
    {
        try {
            ModuleUtil.getDependencies(new ModuleInfo("foo/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + ModuleInfo.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetDependencies_Module()
    {
        final Module module = new Module("foo/");
        final Module dep1 = new Module("bar/");
        final Module dep2 = new Module("baz/");
        module.addDependency(dep1);
        module.addDependency(dep2);
        
        assertSame(module.getDependencies(), ModuleUtil.getDependencies(module));
        assertEquals(TestUtil.set(dep1, dep2), ModuleUtil.getDependencies(module));
    }
    
    public void testGetDependencies_Module_DifferentClassLoader() throws Exception
    {
        final ModuleClassLoader classLoader = new ModuleClassLoader();
        final Object module = createModuleWithDifferentClassLoader("foo/", classLoader);
        final Object dep = createModuleWithDifferentClassLoader("bar/", classLoader);
        // invoking module.addDependency(dep) via reflection
        final Method addDependency = module.getClass().getDeclaredMethod("addDependency", module.getClass());
        addDependency.setAccessible(true);
        addDependency.invoke(module, dep);
        
        assertEquals(Collections.singleton(dep), ModuleUtil.getDependencies(module));
    }
    
    public void testGetAttributes_NullModule()
    {
        try {
            ModuleUtil.getAttributes(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("module", ex.getMessage());
        }
    }
    
    public void testGetAttributes_NonModule()
    {
        try {
            ModuleUtil.getAttributes(new Object());
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + Object.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetAttributes_ModuleInfo()
    {
        try {
            ModuleUtil.getAttributes(new ModuleInfo("foo/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Unsupported module type. Expected: '" + Module.class.getName() +
                    "', was: '" + ModuleInfo.class.getName() + "'.", ex.getMessage());
        }
    }
    
    public void testGetAttributes_Module()
    {
        final Module module = new Module("foo/");
        module.addAttribute("123", "222");
        module.addAttribute("444", "111");
        
        assertSame(module.getAttributes(), ModuleUtil.getAttributes(module));
        assertEquals(TestUtil.map("123", "222", "444", "111"), ModuleUtil.getAttributes(module));
    }
    
    public void testGetAttributes_Module_DifferentClassLoader() throws Exception
    {
        final Object module = createModuleWithDifferentClassLoader("foo/", new ModuleClassLoader());
        // invoking module.addDependency(dep) via reflection
        final Method addDependency = module.getClass().getDeclaredMethod("addAttribute", String.class, Object.class);
        addDependency.setAccessible(true);
        addDependency.invoke(module, "123", "456");
        
        assertEquals(Collections.singletonMap("123", "456"), ModuleUtil.getAttributes(module));
    }
    
    private static Object createModuleWithDifferentClassLoader(final String path, final ClassLoader cl) throws Exception
    {
        final Class<?> c = cl.loadClass(Module.class.getName());
        
        assertNotSame(c, Module.class);
        
        final Constructor<?> constructor = c.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        
        return constructor.newInstance(path);
    }
    
    private static class ModuleClassLoader extends ClassLoader
    {
        public ModuleClassLoader() throws IOException
        {
            /* Defining Module and its nested classes explicitly so that
             * this class loader does not ask the parent class loader to load them,
             * and a brand new classes are associated with this class loader.
             */ 
            defineClass(Module.class.getName());
            // This code is consistent: the binary name of the class ArrayListSet is used.
            defineClass(Module.class.getName() + "$ArrayListSet");
        }
        
        private void defineClass(final String className) throws IOException
        {
            final InputStream in = Module.class.getClassLoader().getResourceAsStream(
                    className.replace('.', '/') + ".class");
            
            try {
                final BufferedInputStream bufferedIn = new BufferedInputStream(in);
                final ByteArrayOutputStream buf = new ByteArrayOutputStream();
                
                int b;
                while ((b = bufferedIn.read()) >= 0) {
                    buf.write(b);
                }
                
                defineClass(className, buf.toByteArray(), 0, buf.size());
            }
            finally {
                in.close();
            }
        }
    }
}
