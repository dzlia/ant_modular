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
    }
    
    public void testInvalidModuleType()
    {
        PropertyHelper.setProperty(project, "in", Integer.valueOf(0));
        
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
