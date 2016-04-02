/* Copyright (c) 2013-2016, Dźmitry Laŭčuk
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

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.Reference;

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
    
    public void testNoModuleRefId()
    {
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'moduleRefId' is undefined.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
    }
    
    public void testNoOutputRefId()
    {
        final Module module = new Module("foo");
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'outputRefId' is undefined.", ex.getMessage());
        }
        
        assertSame(module, project.getReference("in"));
    }
    
    public void testNoModuleRef()
    {
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Reference in not found.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
        assertSame(null, project.getReference("in"));
    }
    
    public void testNoModuleUnderTheRef()
    {
        task.setModuleRefId(new Reference(project, "in")
        {
            @Override
            public Object getReferencedObject()
            {
                return null;
            }
        });
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("No module is found via the reference 'in'.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
        assertSame(null, project.getReference("in"));
    }
    
    public void testInvalidModuleType()
    {
        final Object invalidModule = Integer.valueOf(0);
        project.addReference("in", invalidModule);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Invalid module type is found under the property 'in'. " +
                    "Expected: 'afc.ant.modular.Module', found: 'java.lang.Integer'.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
        assertSame(invalidModule, project.getReference("in"));
    }
    
    public void testSuccessfulExecution()
    {
        final Module module = new Module("foo");
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        task.execute();
        
        assertEquals("foo", project.getReference("out"));
        assertSame(module, project.getReference("in"));
    }
    
    public void testOutputRefAlreadyDefined()
    {
        project.setProperty("out", "bar");
        
        final Module module = new Module("foo");
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        task.execute();
        
        assertSame("foo", project.getReference("out"));
        assertSame(module, project.getReference("in"));
    }
    
    public void testNullModulePath() throws Exception
    {
        // This class loader loads the class afc.ant.modular.Module that returns null path.
        final ModuleClassLoader cl = new ModuleClassLoader("test/data/GetModulePath/Module_null.class");
        final Class<?> moduleClass = cl.loadClass(Module.class.getName());
        
        final Object module = moduleClass.newInstance();
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module path is undefined.", ex.getMessage());
        }
        
        assertNull(project.getReference("out"));
        assertFalse(project.getReferences().containsKey("out"));
        assertSame(module, project.getReference("in"));
    }
    
    public void testNullModulePath_OutputRefAlreadyDefined() throws Exception
    {
        project.addReference("out", "bar");
        
        // This class loader loads the class afc.ant.modular.Module that returns null path.
        final ModuleClassLoader cl = new ModuleClassLoader("test/data/GetModulePath/Module_null.class");
        final Class<?> moduleClass = cl.loadClass(Module.class.getName());
        
        final Object module = moduleClass.newInstance();
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The module path is undefined.", ex.getMessage());
        }
        
        assertSame("bar", project.getReference("out"));
        assertSame(module, project.getReference("in"));
    }
}
