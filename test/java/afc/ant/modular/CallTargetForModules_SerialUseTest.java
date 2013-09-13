package afc.ant.modular;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.tools.ant.types.PropertySet;

import afc.ant.modular.CallTargetForModules.ParamElement;

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
    
    public void testSerialRun_SingleModule_WithUserParams()
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
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesByDefault()
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
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesForced()
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
        task.setInheritAll(true);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_PropertiesNotInherited()
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
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_AndInheritedPropertiesByDefault()
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
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "universe", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_PropertiesNotInherited()
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
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("John", "Smith", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFileWithPrefix_AndInheritedPropertiesByDefault()
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
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "universe", "afc.John", "Smith", "123", "456", "afc.qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesByDefault()
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
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesForced()
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
        task.setInheritAll(true);
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_PropertiesNotInherited()
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
        task.setInheritAll(false);
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "hello", "universe", "qwerty", "board"));
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
