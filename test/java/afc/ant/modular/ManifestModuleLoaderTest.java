package afc.ant.modular;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.tools.ant.Project;

import junit.framework.TestCase;

// TODO add more tests
public class ManifestModuleLoaderTest extends TestCase
{
    private Project project;
    private ManifestModuleLoader loader;
    
    @Override
    protected void setUp()
    {
        project = new Project();
        project.setBasedir("test/data/ManifestModuleLoader");
        loader = new ManifestModuleLoader();
    }
    
    public void testLoadModule_NoDependencies_NoAttributes() throws Exception
    {
        loader.init(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("NoDeps_NoAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("NoDeps_NoAttributes", moduleInfo.getPath());
        assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
    
    public void testLoadModule_EmptyDependencies_NoAttributes() throws Exception
    {
        loader.init(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("EmptyDeps_NoAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("EmptyDeps_NoAttributes", moduleInfo.getPath());
        assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
    
    public void testLoadModule_WithDependencies_NoAttributes() throws Exception
    {
        loader.init(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_NoAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_NoAttributes", moduleInfo.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("foo", "bar", "baz/quux")), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
}
