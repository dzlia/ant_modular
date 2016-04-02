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

import java.util.Collections;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;

public class GetModuleAttributeTest extends TestCase
{
    private GetModuleAttribute task;
    private Project project;
    
    @Override
    protected void setUp()
    {
        task = new GetModuleAttribute();
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
        task.setName("attrib");
        
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
        module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setName("attrib");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'outputRefId' is undefined.", ex.getMessage());
        }
        
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testNoNameAttibute()
    {
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'name' is undefined.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testNoModuleRef()
    {
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
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
    
    public void testNoModuleViaTheRef()
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
        task.setName("attrib");
        
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
        task.setName("attrib");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Invalid module type is found via the reference 'in'. " +
                    "Expected: 'afc.ant.modular.Module', found: 'java.lang.Integer'.", ex.getMessage());
        }
        
        assertEquals(null, project.getReference("out"));
        assertSame(invalidModule, project.getReference("in"));
    }
    
    public void testSuccessfulExecution_AttributeExists()
    {
        final Object attributeValue = new Object();
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.<String, Object>map("attrib", attributeValue, "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(attributeValue, project.getReference("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib", attributeValue, "attrib2", "3"), module.getAttributes());
    }
    
    public void testSuccessfulExecution_AttributeDoesNotExist()
    {
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.<String, Object>map("attrib1", "1", "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(null, project.getReference("out"));
        assertFalse(project.getReferences().containsKey("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib1", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testSuccessfulExecution_NoAttributes()
    {
        final Module module = new Module("foo");
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(null, project.getReference("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(Collections.emptyMap(), module.getAttributes());
    }
    
    public void testOutputRefAlreadyDefined()
    {
        project.addReference("out", "bar");
        
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
        task.execute();
        
        assertEquals("1", project.getReference("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testOutputRefAlreadyDefined_AttributeDoesNotExist()
    {
        project.addReference("out", "bar");
        
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.<String, Object>map("attrib1", "1", "attrib2", "3"));
        project.addReference("in", module);
        
        task.setModuleRefId(new Reference(project, "in"));
        task.setOutputRefId("out");
        task.setName("attrib");
        
        task.execute();
        
        assertNull(project.getReference("out"));
        assertSame(module, project.getReference("in"));
        assertEquals(TestUtil.<String, Object>map("attrib1", "1", "attrib2", "3"), module.getAttributes());
    }
}
