/* Copyright (c) 2013-2015, Dźmitry Laŭčuk
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met: 

   1. Redistributions of source code must retain the above copyright notice, this
      list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
   ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package antmodular;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import antmodular.Module;

import junit.framework.TestCase;

/**
 * <p>Unit tests for {@link Module}.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleTest extends TestCase
{
    public void testConstructWithEmptyPath()
    {
        final Module m = module("");
        
        assertSame("", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testConstructWithNonEmptyPath()
    {
        final Module m = module("a/b/c");
        
        assertSame("a/b/c", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testSetValidDependencies()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        final ArrayList<Module> deps = new ArrayList<Module>();
        deps.add(m2);
        deps.add(m3);
        m2.setDependencies(new Module[]{m3});
        
        final HashSet<Module> expectedDeps = TestUtil.set(m2, m3);
        m.setDependencies(new Module[]{m2, m3});
        assertSame("foo", m.getPath());
        assertEquals(expectedDeps, m.getDependencies());
        assertEquals(Arrays.asList(m2, m3), deps);
        
        deps.clear();
        assertEquals(expectedDeps, m.getDependencies());
        
        // ensure deps of m2 and m3 are not modified
        assertEquals(Collections.singleton(m3), m2.getDependencies());
        assertEquals(Collections.emptySet(), m3.getDependencies());
    }
    
    public void testSetValidDependencies_InputCollectionDoesNotSupportNullElements()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
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
        m2.setDependencies(new Module[]{m3});
        
        final HashSet<Module> expectedDeps = TestUtil.set(m2, m3);
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().add(module("quux"));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryAddNullDependencyViaGetter()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
        
        try {
            m.getDependencies().addAll(Arrays.asList(module("quux")));
            fail();
        }
        catch (UnsupportedOperationException ex) {
            // expected
        }
        assertEquals(TestUtil.set(m2, m3), m.getDependencies());
    }
    
    public void testTryRemoveADependencyViaIterator()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("baz");
        
        m.setDependencies(new Module[]{m2, m3});
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
    
    public void testSetDependencies_NoOtherDependencies()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        
        m.setDependencies(new Module[]{m2});
        assertSame("foo", m.getPath());
        assertEquals(Collections.singleton(m2), m.getDependencies());
    }
    
    /**
     * <p>Test description: though discouraged using different instances of Module with the same path is allowed.
     * Such instances must be treat as different modules.</p>
     */
    public void testAddDependency_AddModuleWithTheSamePathAsDependency()
    {
        final Module m = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("foo");
        
        m.setDependencies(new Module[]{m2, m3});
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
        final Module m1 = module("foo");
        final Module m2 = module("bar");
        final Module m3 = module("foo");
        
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(new Object()));
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m3));
        
        m1.setDependencies(new Module[]{m2});
        assertTrue(m1.equals(m1));
    }
    
    public void testSetAttributes()
    {
        final Module m = module("foo");
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
        final Module m = module("foo");
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
        final Module m = module("foo");
        final Module m2 = module("zoo");
        m.setDependencies(new Module[]{m2});
        
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
        final Module m = module("foo");
        final Module m2 = module("zoo");
        m.setDependencies(new Module[]{m2});
        
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
        final Module m = module("foo");
        
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
        final Module m = module("foo");
        
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
        final Module m = module("foo");
        
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        
        try {
            m.setAttributes(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("attributes", ex.getMessage());
        }
        m.setAttributes(Collections.<String, Object>singletonMap("bar", "baz"));
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testAddAttribute_NoOtherAttributes()
    {
        final Module m = module("foo");
        final Object val = new Object();
        
        m.addAttribute("bar", val);
        assertSame("foo", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testAddAttribute_ToExistingAttributes()
    {
        final Module m = module("foo");
        final Module m2 = module("zoo");
        m.setDependencies(new Module[]{m2});
        
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
        final Module m = module("foo");
        final Module m2 = module("quux");
        m.setDependencies(new Module[]{m2});
        
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
    
    public void testGetDependencies_BasicOperations()
    {
        final Module m1 = module("foo/");
        final Module m2 = module("bar/");
        final Module m3 = module("baz/");
        final Module m4 = module("quux/");
        m1.setDependencies(new Module[]{m2, m3});
        
        final Set<Module> deps = m1.getDependencies();
        
        assertNotNull(deps);
        assertFalse(deps.isEmpty());
        assertEquals(2, deps.size());
        assertFalse(deps.contains(m1));
        assertTrue(deps.contains(m2));
        assertTrue(deps.contains(m3));
        assertFalse(deps.contains(m4));
        assertEquals(TestUtil.set(m2, m3), deps);
        
        final Set<Module> deps2 = m2.getDependencies();
        
        assertNotNull(deps2);
        assertTrue(deps2.isEmpty());
        assertEquals(0, deps2.size());
        assertFalse(deps2.contains(m1));
        assertFalse(deps2.contains(m2));
        assertFalse(deps2.contains(m3));
        assertFalse(deps2.contains(m4));
        assertEquals(Collections.emptySet(), deps2);
    }
    
    private Module module(final String path)
    {
        final Module result = new Module(path);
        result.setDependencies(new Module[0]);
        return result;
    }
}
