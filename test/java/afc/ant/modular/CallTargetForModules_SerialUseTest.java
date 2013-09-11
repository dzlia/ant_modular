package afc.ant.modular;

import java.util.HashSet;

import junit.framework.TestCase;

public class CallTargetForModules_SerialUseTest extends TestCase
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
    
    public void testSerialRun_SingleModule_ModulePropertyDefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        task.perform();
        
        assertCallTargetState(task1, true, true, false, "moduleProp", moduleInfo);
    }
    
    private static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final boolean inheritAll, final boolean inheritRefs, final String moduleProperty, final ModuleInfo proto)
    {
        assertEquals(executed, task.executed);
        assertEquals(inheritAll, task.inheritAll);
        assertEquals(inheritRefs, task.inheritRefs);
        
        final Object moduleObj = task.ownProject.getProperties().get("moduleProp");
        assertTrue(moduleObj instanceof Module);
        final Module module = (Module) moduleObj;
        assertEquals(proto.getPath(), module.getPath());
        assertEquals(proto.getAttributes(), module.getAttributes());
        final HashSet<String> depPaths = new HashSet<String>();
        for (final Module dep : module.getDependencies()) {
            assertTrue(depPaths.add(dep.getPath()));
        }
        assertEquals(proto.getDependencies(), depPaths);
    }
}
