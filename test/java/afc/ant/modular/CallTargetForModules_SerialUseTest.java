package afc.ant.modular;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo, TestUtil.map());
    }
    
    public void testSerialRun_SingleModule_ModulePropertyUndefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, TestUtil.map());
    }
    
    private static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs, final String moduleProperty,
            final ModuleInfo proto, final Map<String, Object> properties)
    {
        assertEquals(executed, task.executed);
        assertEquals(target, task.target);
        assertEquals(inheritAll, task.inheritAll);
        assertEquals(inheritRefs, task.inheritRefs);
        
        final Object moduleObj = task.ownProject.getProperties().get(moduleProperty);
        assertTrue(moduleObj instanceof Module);
        final Module module = (Module) moduleObj;
        assertEquals(proto.getPath(), module.getPath());
        assertEquals(proto.getAttributes(), module.getAttributes());
        final HashSet<String> depPaths = new HashSet<String>();
        for (final Module dep : module.getDependencies()) {
            assertTrue(depPaths.add(dep.getPath()));
        }
        assertEquals(proto.getDependencies(), depPaths);
        
        // merging module property into the properties passed. The module object is not freely available
        final HashMap<String, Object> propsWithModule = new HashMap<String, Object>(properties);
        propsWithModule.put(moduleProperty, module);
        assertEquals(propsWithModule, task.ownProject.getProperties());
    }
    
    private static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs,
            final Map<String, Object> properties)
    {
        assertEquals(executed, task.executed);
        assertEquals(target, task.target);
        assertEquals(inheritAll, task.inheritAll);
        assertEquals(inheritRefs, task.inheritRefs);
        
        assertEquals(properties, task.ownProject.getProperties());
    }
}
