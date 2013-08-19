package afc.ant.modular;

import java.util.Collections;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

import junit.framework.TestCase;

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
    
    public void testNoModuleProperty()
    {
        task.setOutputProperty("out");
        task.setName("attrib");
        
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
        module.setAttributes(TestUtil.map("attrib", "1", "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setName("attrib");
        
        try {
            task.execute();
            fail();
        }
        catch (BuildException ex)
        {
            assertEquals("The attribute 'outputProperty' is undefined.", ex.getMessage());
        }
        
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testNoModuleUnderThePropery()
    {
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
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
        task.setName("attrib");
        
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
    
    public void testSuccessfulExecution_AttributeExists()
    {
        final Object attributeValue = new Object();
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.map("attrib", attributeValue, "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(attributeValue, PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib", attributeValue, "attrib2", "3"), module.getAttributes());
    }
    
    public void testSuccessfulExecution_AttributeDoesNotExist()
    {
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.map("attrib1", "1", "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(null, PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib1", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testSuccessfulExecution_NoAttributes()
    {
        final Module module = new Module("foo");
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame(null, PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(Collections.emptyMap(), module.getAttributes());
    }
    
    public void testOutputPropertyAlreadyDefined()
    {
        project.setProperty("out", "bar");
        
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.map("attrib", "1", "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
        task.execute();
        
        assertEquals("bar", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib", "1", "attrib2", "3"), module.getAttributes());
    }
    
    public void testOutputPropertyAlreadyDefined_AttributeDoesNotExist()
    {
        project.setProperty("out", "bar");
        
        final Module module = new Module("foo");
        module.setAttributes(TestUtil.map("attrib1", "1", "attrib2", "3"));
        PropertyHelper.setProperty(project, "in", module);
        
        task.setModuleProperty("in");
        task.setOutputProperty("out");
        task.setName("attrib");
        
        task.execute();
        
        assertSame("bar", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
        assertEquals(TestUtil.map("attrib1", "1", "attrib2", "3"), module.getAttributes());
    }
}
