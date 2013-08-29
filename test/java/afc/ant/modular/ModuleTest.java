package afc.ant.modular;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

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
        
        final HashSet<Module> expectedDeps = TestUtil.set(m2, m3);
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
        assertEquals(Collections.singleton(m2), m.getDependencies());
        
        m.setDependencies(Arrays.asList(m3, m4));
        assertEquals(TestUtil.set(m3, m4), m.getDependencies());
    }
    
    public void testSetValidDependencies_InputCollectionDoesNotSupportNullElements()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        final List<Module> deps = new AbstractList<Module>() {
            private final ArrayList<Module> list = new ArrayList<Module>();
            
            @Override
            public boolean contains(final Object element)
            {
                throw new NullPointerException();
            }
            
            @Override
            public void add(final int index, final Module element)
            {
                list.add(index, element);
            }

            @Override
            public Module get(final int index)
            {
                return list.get(index);
            }
            
            @Override
            public Module remove(final int index)
            {
                return list.remove(index);
            }

            @Override
            public int size()
            {
                return list.size();
            }
        };
        deps.add(m2);
        deps.add(m3);
        m2.setDependencies(Collections.singleton(m3));
        
        final HashSet<Module> expectedDeps = TestUtil.set(m2, m3);
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
    
    public void testTryAddDependencyViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().add(new Module("quux"));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryAddNullDependencyViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().add(null);
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryClearDependenciesViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().clear();
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRemoveADependencyViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().remove(m2);
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRemoveAllViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().removeAll(Arrays.asList(m2));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRemoveAllViaGetter_InputCollectionSizeGreaterThanDependencyCount()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().removeAll(Arrays.asList(m2, m3, m2));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRetainAllViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().retainAll(Arrays.asList(m2, m3, m2));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryAddACollectionOfDependenciesViaGetter()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().addAll(Arrays.asList(new Module("quux")));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRemoveADependencyViaIterator()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");

        m.setDependencies(Arrays.asList(m2, m3));
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        final Iterator<Module> i = m.getDependencies().iterator();
        assertTrue(i.hasNext());
        final Module moduleToRemove = i.next();
        assertTrue(TestUtil.set(m2, m3).contains(moduleToRemove));
        
        try {
            i.remove();
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testSetNullDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertSame("foo", m.getPath());
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.setDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies", ex.getMessage());
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testSetDependenciesWithNullElement()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2, m3));
        assertSame("foo", m.getPath());
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.setDependencies(Arrays.asList(new Module("quux"), null));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies contains null dependency.", ex.getMessage());
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testAddDependency_NoOtherDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        
        m.addDependency(m2);
        assertSame("foo", m.getPath());
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
    
    public void testAddDependency_ToExistingDependencies()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        
        m.setDependencies(Arrays.asList(m2));
        assertSame("foo", m.getPath());
        assertEquals(Collections.singleton(m2), m.getDependencies());
        
        m.addDependency(m3);
        assertSame("foo", m.getPath());
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
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
        assertEquals(Collections.singleton(m2), m.getDependencies());
        
        m.addDependency(m3);
        assertSame("foo", m.getPath());
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
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
    
    public void testSetAttributes()
    {
        final Module m = new Module("foo");
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("bar", new Object());
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertSame("foo", m.getPath());
        assertEquals(expectedAttributes, m.getAttributes());
        assertEquals(Collections.emptySet(), m.getDependencies());
        assertEquals(expectedAttributes, attributes); // ensure input attributes are not modified by #setAttributes
        
        attributes.clear();
        assertEquals(expectedAttributes, m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testReSetAttributes()
    {
        final Module m = new Module("foo");
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        final Object o = new Object();
        attributes.put("bar", o);
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertEquals(expectedAttributes, m.getAttributes());
        
        final HashMap<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put("111", "222");
        attributes2.put("baz", "");
        attributes2.put("quux", new Object());
        
        final HashMap<String, Object> expectedAttributes2 = new HashMap<String, Object>(attributes2);
        m.setAttributes(attributes2);
        assertEquals(expectedAttributes2, m.getAttributes());
        assertEquals(expectedAttributes2, attributes2); // ensure input attributes are not modified by #setAttributes
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testReSetAttributes_WithNullAttributeName_InputMapSupportsNullKeys()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("zoo");
        m.addDependency(m2);
        
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        final Object o = new Object();
        attributes.put("bar", o);
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertEquals(expectedAttributes, m.getAttributes());
        
        final HashMap<String, Object> attributes2 = new HashMap<String, Object>();
        attributes2.put("111", "222");
        attributes2.put(null, "");
        attributes2.put("quux", new Object());
        
        try {
            m.setAttributes(attributes2);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("attributes contains an attribute with null name.", ex.getMessage());
        }
        assertEquals(expectedAttributes, m.getAttributes());
        
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
    
    public void testSetAttributes_InputMapDoesNotSupportNullKeys()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("zoo");
        m.addDependency(m2);
        
        final TreeMap<String, Object> attributes = new TreeMap<String, Object>();
        final Object o = new Object();
        attributes.put("bar", o);
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertEquals(expectedAttributes, m.getAttributes());
        
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
    
    public void testTryAddAttributeViaGetter()
    {
        final Module m = new Module("foo");
        
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        assertEquals(Collections.singletonMap("bar", "baz"), m.getAttributes());
        
        try {
            m.getAttributes().put("quux", "zoo");
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(Collections.singletonMap("bar", "baz"), m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testTryClearAttributesViaGetter()
    {
        final Module m = new Module("foo");
        
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        assertEquals(Collections.singletonMap("bar", "baz"), m.getAttributes());
        
        try {
            m.getAttributes().clear();
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(Collections.singletonMap("bar", "baz"), m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testSetNullAttributes()
    {
        final Module m = new Module("foo");
        
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        
        try {
            m.setDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies", ex.getMessage());
        }
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testAddAttribute_NoOtherAttributes()
    {
        final Module m = new Module("foo");
        final Object val = new Object();
        
        m.addAttribute("bar", val);
        assertSame("foo", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testAddAttribute_ToExistingAttributes()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("zoo");
        m.addDependency(m2);
        
        final Object val = new Object();
        
        m.setAttributes(Collections.singletonMap("bar", val));
        assertSame("foo", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        m.addAttribute("baz", "444");
        assertSame("foo", m.getPath());
        
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("bar", val);
        attributes.put("baz", "444");
        assertEquals(attributes, m.getAttributes());
        
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
    
    public void testAddAttribute_NullAttribute()
    {
        final Module m = new Module("foo");
        final Module m2 = new Module("quux");
        m.addDependency(m2);
        
        final Object val = new Object();
        
        m.setAttributes(Collections.singletonMap("bar", val));
        assertSame("foo", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        try {
            m.addAttribute(null, "123");
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("attributeName", ex.getMessage());
        }
        assertSame("foo", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
}
