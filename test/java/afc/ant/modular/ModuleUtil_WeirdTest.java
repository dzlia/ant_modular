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

import java.lang.reflect.Constructor;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

/**
 * <p>Contains unit tests for {@link ModuleUtil} that verify its behaviour when invalid
 * {@link Module} objects are passed to its functions.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleUtil_WeirdTest extends TestCase
{
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getPath(Object)}
     * if a {@link Module} instance is passed that does not have the member function
     * {@code #getPath()}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetPath_NoSuchMethod() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_empty.class");
        
        try {
            ModuleUtil.getPath(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module instance does not have the function 'getPath()'.", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getDependencies(Object)}
     * if a {@link Module} instance is passed that does not have the member function
     * {@code #getDependencies()}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetDependencies_NoSuchMethod() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_empty.class");
        
        try {
            ModuleUtil.getDependencies(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module instance does not have the function 'getDependencies()'.", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getAttributes(Object)}
     * if a {@link Module} instance is passed that does not have the member function
     * {@code #getAttributes()}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetAttributes_NoSuchMethod() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_empty.class");
        
        try {
            ModuleUtil.getAttributes(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module instance does not have the function 'getAttributes()'.", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getPath(Object)}
     * if a {@link Module} instance is passed whose {@code #getPath()} member function is private
     * thus not invocable outside.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetPath_PrivateFunction() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_private.class");
        
        try {
            ModuleUtil.getPath(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Unable to invoke module#getPath().", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getDependencies(Object)}
     * if a {@link Module} instance is passed whose {@code #getDependencies()} member function is
     * private thus not invocable outside.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetDependencies_PrivateFunction() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_private.class");
        
        try {
            ModuleUtil.getDependencies(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Unable to invoke module#getDependencies().", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getAttributes(Object)}
     * if a {@link Module} instance is passed whose {@code #getAttributes()} member function is
     * private thus not invocable outside.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetAttributes_PrivateFunction() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_private.class");
        
        try {
            ModuleUtil.getAttributes(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Unable to invoke module#getAttributes().", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getPath(Object)}
     * if {@link Module#getPath()} throws an exception.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetPath_FunctionThrowsException() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_exception.class");
        
        try {
            ModuleUtil.getPath(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("module#getPath() has thrown an exception.", ex.getMessage());
            assertNotNull(ex.getCause());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getDependencies(Object)}
     * if {@link Module#getDependencies()} throws an exception.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetDependencies_FunctionThrowsException() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_exception.class");
        
        try {
            ModuleUtil.getDependencies(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("module#getDependencies() has thrown an exception.", ex.getMessage());
            assertNotNull(ex.getCause());
        }
    }
    
    /**
     * <p>Tests that a {@link BuildException} is thrown by {@link ModuleUtil#getAttributes(Object)}
     * if {@link Module#getAttributes()} throws an exception.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetAttributes_FunctionThrowsException() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader("test/data/ModuleUtil/Module_exception.class");
        
        try {
            ModuleUtil.getAttributes(wrongModule);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("module#getAttributes() has thrown an exception.", ex.getMessage());
            assertNotNull(ex.getCause());
        }
    }
    
    /**
     * <p>Tests that a {@link ClassCastException} is thrown by {@link ModuleUtil#getPath(Object)}
     * if a {@link Module} instance is passed whose {@code #getPath()} member function does not
     * return {@link String}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetPath_WrongReturnType() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader(
                "test/data/ModuleUtil/Module_wrong_types.class");
        
        try {
            ModuleUtil.getPath(wrongModule);
            fail();
        }
        catch (ClassCastException ex) {
            // expected
        }
    }
    
    /**
     * <p>Tests that a {@link ClassCastException} is thrown by {@link ModuleUtil#getDependencies(Object)}
     * if a {@link Module} instance is passed whose {@code #getDependencies()} member function does not
     * return {@link java.util.Set}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetDependencies_WrongReturnType() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader(
                "test/data/ModuleUtil/Module_wrong_types.class");
        
        try {
            ModuleUtil.getDependencies(wrongModule);
            fail();
        }
        catch (ClassCastException ex) {
            // expected
        }
    }
    
    /**
     * <p>Tests that a {@link ClassCastException} is thrown by {@link ModuleUtil#getAttributes(Object)}
     * if a {@link Module} instance is passed whose {@code #getDependencies()} member function does not
     * return {@link java.util.Map}.</p>
     * 
     * @throws Exception if this test fails with an exception.
     */
    public void testGetAttributes_WrongReturnType() throws Exception
    {
        final Object wrongModule = createModuleWithDifferentClassLoader(
                "test/data/ModuleUtil/Module_wrong_types.class");
        
        try {
            ModuleUtil.getAttributes(wrongModule);
            fail();
        }
        catch (ClassCastException ex) {
            // expected
        }
    }
    
    private static Object createModuleWithDifferentClassLoader(final String pathToModuleClass) throws Exception
    {
        final Class<?> c = new ModuleClassLoader(pathToModuleClass).loadClass(Module.class.getName());
        
        assertNotSame(c, Module.class);
        
        final Constructor<?> constructor = c.getDeclaredConstructor();
        
        return constructor.newInstance();
    }
}
