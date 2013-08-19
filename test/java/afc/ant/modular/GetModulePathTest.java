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
        
        assertEquals("bar", PropertyHelper.getProperty(project, "out"));
        assertSame(module, PropertyHelper.getProperty(project, "in"));
    }
}
