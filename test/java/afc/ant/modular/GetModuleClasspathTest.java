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

// TODO add tests for positive cases.
public class GetModuleClasspathTest extends TestCase
{
    private GetModuleClasspath task;
    private Project project;
    
    @Override
    protected void setUp()
    {
        task = new GetModuleClasspath();
        project = new Project();
        task.setProject(project);
    }
    
    public void testNoModuleProperty()
    {
        task.setOutputProperty("out");
        task.setSourceAttribute("cp");
        
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
        task.setSourceAttribute("cp");
        
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
    
    public void testNoClasspathAttributes()
    {
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.map("attrib", "1", "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Source attributes are not defined.", ex.getMessage());
        }
        
        assertEquals(null, PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testNoModuleUnderThePropery()
    {
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setSourceAttribute("cp");
        
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
        final Object invalidModule = Long.valueOf(0);
        PropertyHelper.setProperty(project, "in", invalidModule);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setSourceAttribute("cp");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("Invalid module type is found under the property 'in'. " +
                    "Expected: 'afc.ant.modular.Module', found: 'java.lang.Long'.", ex.getMessage());
        }
        
        assertEquals(null, PropertyHelper.getProperty(project, "out"));
        assertSame(invalidModule, PropertyHelper.getProperty(project, "in"));
    }
}
