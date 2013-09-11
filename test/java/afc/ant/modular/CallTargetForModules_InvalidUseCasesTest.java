package afc.ant.modular;

import org.apache.tools.ant.BuildException;

import junit.framework.TestCase;

public class CallTargetForModules_InvalidUseCasesTest extends TestCase
{
    private CallTargetForModules task;
    private MockProject project;
    private MockModuleLoader moduleLoader;
    
    @Override
    protected void setUp()
    {
        project = new MockProject();
        task = new CallTargetForModules();
        task.setProject(project);
        moduleLoader = new MockModuleLoader();
    }
    
    @Override
    protected void tearDown()
    {
        moduleLoader = null;
        task = null;
        project = null;
    }
    
    public void testMissingTarget()
    {
        task.init();
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The attribute 'target' is undefined.", ex.getMessage());
        }
    }
    
    public void testMissingModuleLoader()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("No module loader is defined.", ex.getMessage());
        }
    }
    
    public void testAttemptToConfigureMoreThanOneModuleLoader()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.addConfigured(new MockModuleLoader());
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Only a single module loader element is allowed.", ex.getMessage());
        }
    }
}
