package afc.ant.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

public class ModuleInfoTest extends TestCase
{
    public void testConstructWithNullPath()
    {
        try {
            new ModuleInfo(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
    }
    
    public void testConstructWithEmptyPath()
    {
        final ModuleInfo m = new ModuleInfo("");
        
        assertSame("", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testConstructWithNonEmptyPath()
    {
        final ModuleInfo m = new ModuleInfo("a/b/c");
        
        assertSame("a/b/c", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testSetValidDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        final ArrayList<String> deps = new ArrayList<String>();
        deps.add("bar");
        deps.add("baz");
        
        final HashSet<String> expectedDeps = new HashSet<String>(Arrays.asList("bar", "baz"));
        m.setDependencies(deps);
        assertSame("foo", m.getPath());
        assertEquals(expectedDeps, m.getDependencies());
        assertEquals(Arrays.asList("bar", "baz"), deps);
        
        deps.clear();
        assertEquals(expectedDeps, m.getDependencies());
    }
    
    public void testReSetValidDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");

        m.setDependencies(Arrays.asList("bar"));
        assertEquals(new HashSet<String>(Arrays.asList("bar")), m.getDependencies());
        
        m.setDependencies(Arrays.asList("baz", "quux"));
        assertEquals(new HashSet<String>(Arrays.asList("baz", "quux")), m.getDependencies());
    }
    
    public void testTryAddDependencyViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
        
        try {
            m.getDependencies().add("quux");
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testTryAddNullDependencyViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
        
        try {
            m.getDependencies().add(null);
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testTryClearDependenciesViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo");

        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
        
        try {
            m.getDependencies().clear();
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testSetNullDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
        
        try {
            m.setDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies", ex.getMessage());
        }
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testSetDependenciesWithNullElement()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
        
        try {
            m.setDependencies(Arrays.asList("quux", null));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies contains null dependency.", ex.getMessage());
        }
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testAddDependency_NoOtherDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.addDependency("bar");
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar")), m.getDependencies());
    }
    
    public void testAddDependency_ToExistingDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Arrays.asList("bar"));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar")), m.getDependencies());
        
        m.addDependency("baz");
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    public void testAddDependency_NullDependency()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Collections.singletonList("bar"));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Collections.singletonList("bar")), m.getDependencies());
        
        try {
            m.addDependency(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependency", ex.getMessage());
        }
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Collections.singletonList("bar")), m.getDependencies());
    }
    
    public void testAddDependency_AddItselfAsDependency()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.setDependencies(Collections.singletonList("bar"));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Collections.singletonList("bar")), m.getDependencies());
        
        try {
            m.addDependency(new String("foo"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Collections.singletonList("bar")), m.getDependencies());
            
        m.addDependency("baz");
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<String>(Arrays.asList("bar", "baz")), m.getDependencies());
    }
    
    /**
     * <p>Test description: though discouraged using different instances of ModuleInfo with the same path is allowed.
     * Such instances must be treat as different modules.</p>
     */
    public void testEquals()
    {
        final ModuleInfo m1 = new ModuleInfo("foo");
        final ModuleInfo m2 = new ModuleInfo("bar");
        final ModuleInfo m3 = new ModuleInfo("foo");
        
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(new Object()));
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m3));
        
        m1.addDependency("bar");
        assertTrue(m1.equals(m1));
    }
    
    public void testSetDependencyToItselfByMeansOfSetDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo");
        
        m.addDependency("bar");
        
        try {
            m.setDependencies(Arrays.asList("baz", "foo"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        
        assertEquals(Collections.singleton("bar"), m.getDependencies());
    }
}
