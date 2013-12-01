/* Copyright (c) 2013, Dźmitry Laŭčuk
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
package afc.ant.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

public class ModuleInfoTest extends TestCase
{
    private MockModuleLoader moduleLoader;
    
    @Override
    protected void setUp()
    {
        moduleLoader = new MockModuleLoader();
    }
    
    @Override
    protected void tearDown()
    {
        moduleLoader = null;
    }
    
    public void testConstructWithNullPath()
    {
        try {
            new ModuleInfo(null, moduleLoader);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
    }
    
    public void testConstructWithNullModuleLoader()
    {
        try {
            new ModuleInfo("foo/bar", null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("moduleLoader", ex.getMessage());
        }
    }
    
    public void testConstruct_NormalisedPathIsNull()
    {
        moduleLoader.normalisedPaths.put("some/path", null);
        
        try {
            new ModuleInfo("some/path", moduleLoader);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("The normalised path that corresponds to the path 'some/path' is null.", ex.getMessage());
        }
    }
    
    public void testConstructWithEmptyPath()
    {
        final ModuleInfo m = new ModuleInfo("", moduleLoader);
        
        assertEquals("/", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testConstructWithNonEmptyPath()
    {
        final ModuleInfo m = new ModuleInfo("a/b/c", moduleLoader);
        
        assertEquals("a/b/c/", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testConstructWithNormalisedPath()
    {
        final ModuleInfo m = new ModuleInfo("a/b/c/", moduleLoader);
        
        assertEquals("a/b/c/", m.getPath());
        assertEquals(Collections.emptySet(), m.getDependencies());
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetValidDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        final ArrayList<String> deps = new ArrayList<String>();
        deps.add("bar/");
        deps.add("baz");
        
        final HashSet<String> expectedDeps = TestUtil.set("bar/", "baz/");
        m.setDependencies(deps);
        assertEquals("foo/", m.getPath());
        assertEquals(expectedDeps, m.getDependencies());
        assertEquals(Arrays.asList("bar/", "baz"), deps);
        
        deps.clear();
        assertEquals(expectedDeps, m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testReSetValidDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);

        m.setDependencies(Arrays.asList("bar"));
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        m.setDependencies(Arrays.asList("baz", "quux"));
        assertEquals(TestUtil.set("baz/", "quux/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetValidDependencies_InputCollectionDoesNotSupportNullElements()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        final TreeSet<String> deps = new TreeSet<String>();
        deps.add("bar");
        deps.add("baz");
        
        final HashSet<String> expectedDeps = TestUtil.set("bar/", "baz/");
        m.setDependencies(deps);
        assertEquals("foo/", m.getPath());
        assertEquals(expectedDeps, m.getDependencies());
        assertEquals(TestUtil.set("bar", "baz"), deps);
        
        deps.clear();
        assertEquals(expectedDeps, m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testTryAddDependencyViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.getDependencies().add("quux");
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testTryAddNullDependencyViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.getDependencies().add(null);
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testTryClearDependenciesViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);

        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.getDependencies().clear();
            fail();
        }
        catch (RuntimeException ex) {
            // expected
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetNullDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar", "baz"));
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.setDependencies(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies", ex.getMessage());
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetDependenciesWithNullElement()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar", "baz/"));
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.setDependencies(Arrays.asList("quux", null));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependencies contains null dependency.", ex.getMessage());
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetDependencies_SomeNormalisedDependencyIsNull()
    {
        moduleLoader.normalisedPaths.put("flux", null);
        
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar", "baz/"));
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        try {
            m.setDependencies(Arrays.asList("quux", "flux"));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("The normalised path that corresponds to the path 'flux' is null.", ex.getMessage());
        }
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_NoOtherDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.addDependency("bar");
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_ToExistingDependencies()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Arrays.asList("bar"));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        m.addDependency("baz");
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_NullDependency()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Collections.singletonList("bar"));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        try {
            m.addDependency(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("dependency", ex.getMessage());
        }
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_NormalisedDependencyIsNull()
    {
        moduleLoader.normalisedPaths.put("baz", null);
        
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Collections.singletonList("bar"));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        try {
            m.addDependency("baz");
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("The normalised path that corresponds to the path 'baz' is null.", ex.getMessage());
        }
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_AddItselfAsDependency_NonNormalisedDependency()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Collections.singletonList("bar"));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        try {
            m.addDependency(new String("foo"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
            
        m.addDependency("baz");
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testAddDependency_AddItselfAsDependency_NormalisedDependency()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.setDependencies(Collections.singletonList("bar"));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        try {
            m.addDependency(new String("foo/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
            
        m.addDependency("baz");
        assertEquals("foo/", m.getPath());
        assertEquals(TestUtil.set("bar/", "baz/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    /**
     * <p>Test description: though discouraged using different instances of ModuleInfo with the same path is allowed.
     * Such instances must be treat as different modules.</p>
     */
    public void testEquals()
    {
        final ModuleInfo m1 = new ModuleInfo("foo", moduleLoader);
        final ModuleInfo m2 = new ModuleInfo("bar", moduleLoader);
        final ModuleInfo m3 = new ModuleInfo("foo", moduleLoader);
        
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(new Object()));
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(m2));
        assertFalse(m1.equals(m3));
        
        m1.addDependency("bar");
        assertTrue(m1.equals(m1));
        
        m1.addAttribute("baz", "quux");
        assertTrue(m1.equals(m1));
    }
    
    public void testSetDependencyToItselfByMeansOfSetDependencies_NonNormalisedValue()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.addDependency("bar");
        
        try {
            m.setDependencies(Arrays.asList("baz", "foo"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetDependencyToItselfByMeansOfSetDependencies_NormalisedValue()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
        m.addDependency("bar");
        
        try {
            m.setDependencies(Arrays.asList("baz", "foo/"));
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Cannot add itself as a dependency.", ex.getMessage());
        }
        
        assertEquals(Collections.singleton("bar/"), m.getDependencies());
        
        assertEquals(Collections.emptyMap(), m.getAttributes());
    }
    
    public void testSetAttributes()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("bar", new Object());
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertEquals("foo/", m.getPath());
        assertEquals(expectedAttributes, m.getAttributes());
        assertEquals(Collections.emptySet(), m.getDependencies());
        assertEquals(expectedAttributes, attributes); // ensure input attributes are not modified by #setAttributes
        
        attributes.clear();
        assertEquals(expectedAttributes, m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testReSetAttributes()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
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
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        m.addDependency("zoo");
        
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
        
        assertEquals(Collections.singleton("zoo/"), m.getDependencies());
    }
    
    public void testSetAttributes_InputMapDoesNotSupportNullKeys()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        m.addDependency("zoo");
        
        final TreeMap<String, Object> attributes = new TreeMap<String, Object>();
        final Object o = new Object();
        attributes.put("bar", o);
        attributes.put("baz", Integer.valueOf(2000));
        attributes.put("quux", null);
        
        final HashMap<String, Object> expectedAttributes = new HashMap<String, Object>(attributes);
        m.setAttributes(attributes);
        assertEquals(expectedAttributes, m.getAttributes());
        
        assertEquals(Collections.singleton("zoo/"), m.getDependencies());
    }
    
    public void testTryAddAttributeViaGetter()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
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
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
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
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        
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
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        final Object val = new Object();
        
        m.addAttribute("bar", val);
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        assertEquals(Collections.emptySet(), m.getDependencies());
    }
    
    public void testAddAttribute_ToExistingAttributes()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        m.addDependency("quux");
        
        final Object val = new Object();
        
        m.setAttributes(Collections.singletonMap("bar", val));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        m.addAttribute("baz", "444");
        assertEquals("foo/", m.getPath());
        
        final HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("bar", val);
        attributes.put("baz", "444");
        assertEquals(attributes, m.getAttributes());
        
        assertEquals(Collections.singleton("quux/"), m.getDependencies());
    }
    
    public void testAddAttribute_NullAttribute()
    {
        final ModuleInfo m = new ModuleInfo("foo", moduleLoader);
        m.addDependency("quux");
        
        final Object val = new Object();
        
        m.setAttributes(Collections.singletonMap("bar", val));
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        try {
            m.addAttribute(null, "123");
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("attributeName", ex.getMessage());
        }
        assertEquals("foo/", m.getPath());
        assertEquals(Collections.singletonMap("bar", val), m.getAttributes());
        
        assertEquals(Collections.singleton("quux/"), m.getDependencies());
    }
}
