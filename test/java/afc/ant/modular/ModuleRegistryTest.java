package afc.ant.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import junit.framework.TestCase;

public class ModuleRegistryTest extends TestCase
{
    private MockModuleLoader loader;
    private ModuleRegistry registry;
    
    @Override
    protected void setUp()
    {
        loader = new MockModuleLoader();
        registry = new ModuleRegistry(loader);
    }
    
    public void testCreateSingleModule() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("bar");
        module.addDependency(dep);
        loader.results.add(module);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("foo");
        
        assertSame(module, m1);
        assertSame(module, m2);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        
        assertEquals(Collections.singletonList("foo"), loader.paths);
    }
    
    public void testCreateTwoModules() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("quux");
        final Module module2 = new Module("test");
        module.addDependency(dep);
        loader.results.add(module);
        loader.results.add(module2);
        
        final Module m1 = registry.resolveModule("foo");
        final Module m2 = registry.resolveModule("bar");
        final Module m3 = registry.resolveModule("foo");
        final Module m4 = registry.resolveModule("bar");
        
        assertSame(module, m1);
        assertSame(module2, m2);
        assertSame(module, m3);
        assertSame(module2, m4);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        assertEquals(Collections.emptySet(), m2.getDependencies());
        
        assertEquals(Arrays.asList("foo", "bar"), loader.paths);
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_ModuleNotLoadedException() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("quux");
        module.addDependency(dep);
        loader.results.add(module);
        
        final ModuleNotLoadedException exception = new ModuleNotLoadedException();
        loader.results.add(exception);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertSame(exception, ex);
        }
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            // expected
        }
        
        assertSame(module, m1);
        assertSame(module, m2);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        
        assertEquals(Arrays.asList("foo", "bar"), loader.paths);
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_RuntimeException() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("quux");
        module.addDependency(dep);
        final Module module2 = new Module("test2");
        loader.results.add(module);
        
        final RuntimeException exception = new RuntimeException();
        loader.results.add(exception);
        
        loader.results.add(module2);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (RuntimeException ex) {
            assertSame(exception, ex);
        }
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        
        assertSame(module, m1);
        assertSame(module, m2);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        assertSame(module2, m3);
        assertEquals(Collections.emptySet(), m3.getDependencies());
        
        assertEquals(Arrays.asList("foo", "bar", "bar"), loader.paths);
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_Error() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("quux");
        module.addDependency(dep);
        final Module module2 = new Module("test2");
        loader.results.add(module);
        
        final Error exception = new Error();
        loader.results.add(exception);
        
        loader.results.add(module2);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (Error ex) {
            assertSame(exception, ex);
        }
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        
        assertSame(module, m1);
        assertSame(module, m2);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        assertSame(module2, m3);
        assertEquals(Collections.emptySet(), m3.getDependencies());
        
        assertEquals(Arrays.asList("foo", "bar", "bar"), loader.paths);
    }
    
    public void testCreateTwoModules_SecondModuleIsNotLoaded_NullIsReturned() throws Exception
    {
        final Module module = new Module("test");
        final Module dep = new Module("quux");
        module.addDependency(dep);
        final Module module2 = new Module("test2");
        loader.results.add(module);
        loader.results.add(null);
        loader.results.add(module2);
        
        final Module m1 = registry.resolveModule("foo");
        
        try {
            registry.resolveModule("bar");
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("Module loader returned null for the path 'bar'.", ex.getMessage());
        }
        
        final Module m2 = registry.resolveModule("foo");
        
        // another attempt to load the module
        final Module m3 = registry.resolveModule("bar");
        
        assertSame(module, m1);
        assertSame(module, m2);
        assertEquals(Collections.singleton(dep), m1.getDependencies());
        assertSame(module2, m3);
        assertEquals(Collections.emptySet(), m3.getDependencies());
        
        assertEquals(Arrays.asList("foo", "bar", "bar"), loader.paths);
    }
    
    public void testNullPath() throws Exception
    {
        final Module module = new Module("test");
        loader.results.add(module);
        
        try {
            registry.resolveModule(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
        
        final Module m = registry.resolveModule("foo");
        
        assertSame(module, m);
        
        assertEquals(Collections.singletonList("foo"), loader.paths);
    }
    
    public void testCreateResolverWithNullModuleLoader()
    {
        try {
            new ModuleRegistry(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("moduleLoader", ex.getMessage());
        }
    }
    
    private static class MockModuleLoader implements ModuleLoader
    {
        public final ArrayList<String> paths = new ArrayList<String>();
        public final LinkedList<Object> results = new LinkedList<Object>();
        
        public Module loadModule(final String path) throws ModuleNotLoadedException
        {
            assertNotNull(path);
            paths.add(path);
            assertFalse(results.isEmpty());
            final Object result = results.remove();
            if (result instanceof ModuleNotLoadedException) {
                throw (ModuleNotLoadedException) result;
            }
            if (result instanceof RuntimeException) {
                throw (RuntimeException) result;
            }
            if (result instanceof Error) {
                throw (Error) result;
            }
            return (Module) result;
        }
    }
}
