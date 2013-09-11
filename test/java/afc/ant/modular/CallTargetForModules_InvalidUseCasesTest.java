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
    
    public void testModuleElementWithNoPath_SingleModule()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule();
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("There is a <module> element with the attribute 'path' undefined.", ex.getMessage());
        }
    }
    
    public void testModuleElementWithNoPath_MultipleModules()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.createModule();
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("There is a <module> element with the attribute 'path' undefined.", ex.getMessage());
        }
    }
    
    public void testNoModuleElements()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("At least one <module> element is required.", ex.getMessage());
        }
    }
    
    public void testTwoRootModules_CyclicDependency()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/");
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("foo/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertTrue(ex.getCause() instanceof CyclicDependenciesDetectedException);
            assertEquals(ex.getMessage(), ex.getCause().getMessage());
        }
    }
    
    public void testRootModuleWithDependency_CyclicDependency()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/");
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("foo/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertTrue(ex.getCause() instanceof CyclicDependenciesDetectedException);
            assertEquals(ex.getMessage(), ex.getCause().getMessage());
        }
    }
    
    public void testMissingModule()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/");
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("baz/"); // this dependency will not be resolved
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        final ModuleNotLoadedException exception = new ModuleNotLoadedException("test_msg");
        moduleLoader.modules.put("baz/", exception);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertSame(exception, ex.getCause());
            assertEquals("test_msg", ex.getMessage());
        }
    }
}
