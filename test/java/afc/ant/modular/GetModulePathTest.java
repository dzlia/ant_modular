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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

import junit.framework.TestCase;

public class GetModulePathTest extends TestCase
{
    private GetModulePath task;
    private Project project;
    
    @Override
    protected void setUp()
    {
        task = new GetModulePath();
        project = new Project();
        task.setProject(project);
    }
    
    @Override
    protected void tearDown()
    {
        project = null;
        task = null;
    }
    
    public void testNoModuleProperty()
    {
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'moduleProperty' is undefined.", ex.getMessage());
        }
        
        assertEquals(null, PropertyHelper.getProperty(project, "out"));
    }
    
    public void testNoOutputProperty()
    {
        final Module module = new Module("foo");
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'outputProperty' is undefined.", ex.getMessage());
        }
        
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testNoModuleUnderThePropery()
    {
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("No module is found under the property 'in'.", ex.getMessage());
        }
        
        assertEquals(null, PropertyHelper.getProperty(project, "out"));
        assertSame(null, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testInvalidModuleType()
    {
        final Object invalidModule = Integer.valueOf(0);
        PropertyHelper.setProperty(project, "in", invalidModule);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Invalid module type is found under the property 'in'. " +
                    "Expected: 'afc.ant.modular.Module', found: 'java.lang.Integer'.", ex.getMessage());
        }
        
        assertEquals(null, PropertyHelper.getProperty(project, "out"));
        assertSame(invalidModule, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testSuccessfulExecution()
    {
        final Module module = new Module("foo");
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        task.execute();
        
        assertEquals("foo", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testOutputPropertyAlreadyDefined()
    {
        project.setProperty("out", "bar");
        
        final Module module = new Module("foo");
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        task.execute();
        
        assertSame("bar", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testNullModulePath() throws Exception
    {
        // This class loader loads the class afc.ant.modular.Module that returns null path.
        final ModuleClassLoader cl = new ModuleClassLoader("test/data/GetModulePath/Module_null.class");
        final Class<?> moduleClass = cl.loadClass(Module.class.getName());
        
        final Object module = moduleClass.newInstance();
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module path is undefined.", ex.getMessage());
        }
        
        assertNull(PropertyHelper.getProperty(project, "out"));
        assertFalse(project.getProperties().contains("out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
    
    public void testNullModulePath_OutputPropertyAlreadyDefined() throws Exception
    {
        project.setProperty("out", "bar");
        
        // This class loader loads the class afc.ant.modular.Module that returns null path.
        final ModuleClassLoader cl = new ModuleClassLoader("test/data/GetModulePath/Module_null.class");
        final Class<?> moduleClass = cl.loadClass(Module.class.getName());
        
        final Object module = moduleClass.newInstance();
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module path is undefined.", ex.getMessage());
        }
        
        assertSame("bar", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
}
