package afc.ant.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

public class ModuleTest extends TestCase
{
    public void testConstructWithNullPath()
    {
        try {
            new Module(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
    }
    
    public void testConstructWithEmptyPath()
    {
        final Module m = new Module("");
        
        assertSame("", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testConstructWithNonEmptyPath()
    {
        final Module m = new Module("a/b/c");
        
        assertSame("a/b/c", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testSetValidDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        final ArrayList<Module> deps = new ArrayList<Module>();
        deps.add(m2);
        deps.add(m3);
        m2.setDependencies(Collections.singleton(m3));
        
        final HashSet<Module> expectedDeps = new HashSet<Module>(Arrays.asList(m2, m3));
        m.setDependencies(deps);
        assertSame("foo", m.getPath());
        assertEquals(expectedDeps, m.getDependencies());
        assertEquals(Arrays.asList(m2, m3), deps);
        
        deps.clear();
        assertEquals(expectedDeps, m.getDependencies());
        
        // ensure deps of m2 and m3 are not modified
        assertEquals(Collections.singleton(m3), m2.getDependencies());
        assertEquals(Collections.emptySet(), m3.getDependencies());
    }
    
    public void testReSetValidDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        final Module m4 = new Module("quux");

        m.setDependencies(Arrays.asList(m2));
        assertEquals(new HashSet<Module>(Arrays.asList(m2)), m.getDependencies());
        
        m.setDependencies(Arrays.asList(m3, m4));
        assertEquals(new HashSet<Module>(Arrays.asList(m3, m4)), m.getDependencies());
    }
    
    public void testTryAddDependencyViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        
        try {
            m.getDependencies().add(new Module("quux"));
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testTryAddNullDependencyViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        
        try {
            m.getDependencies().add(null);
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testTryClearDependenciesViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        
        try {
            m.getDependencies().clear();
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testSetNullDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        
        try {
            m.setDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies", ex.getMessage());
        }
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testSetDependenciesWithNullElement()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        
        try {
            m.setDependencies(Arrays.asList(new Module("quux"), null));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies contains null dependency.", ex.getMessage());
        }
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testAddDependency_NoOtherDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        
        m.addDependency(m2);
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2)), m.getDependencies());
    }
    
    public void testAddDependency_ToExistingDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2)), m.getDependencies());
        
        m.addDependency(m3);
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    public void testAddDependency_NullDependency()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        
        m.setDependencies(Collections.singletonList(m2));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Collections.singletonList(m2)), m.getDependencies());
        
        try {
            m.addDependency(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependency", ex.getMessage());
        }
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Collections.singletonList(m2)), m.getDependencies());
    }
    
    public void testAddDependency_AddItselfAsDependency()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Collections.singletonList(m2));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Collections.singletonList(m2)), m.getDependencies());
        
        try {
            m.addDependency(m);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Collections.singletonList(m2)), m.getDependencies());
            
        m.addDependency(m3);
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
    }
    
    /**
     * <p>Test description: though discouraged using different instances of Module with the same path is allowed.
     * Such instances must be treat as different modules.</p>
     */
    public void testAddDependency_AddModuleWithTheSamePathAsDependency()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("foo");
        
        m.setDependencies(Collections.singletonList(m2));
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Collections.singletonList(m2)), m.getDependencies());
        
        m.addDependency(m3);
        assertSame("foo", m.getPath());
        assertEquals(new HashSet<Module>(Arrays.asList(m2, m3)), m.getDependencies());
        assertEquals(Collections.emptySet(), m3.getDependencies());
    }
    
    /**
     * <p>Test description: though discouraged using different instances of Module with the same path is allowed.
     * Such instances must be treat as different modules.</p>
     */
    public void testEquals()
    {
        final Module m1 = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("foo");
        
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(new Object()));
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m3));
        
        m1.addDependency(m2);
        assertTrue(m1.equals(m1));
    }
    
    public void testSetItselfByMeansOfSetDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        
        m.addDependency(m2);
        
        try {
            m.setDependencies(Arrays.asList(new Module("baz"), m));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
}
