/* Copyright (c) 2013-2016, Dźmitry Laŭčuk
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;

import junit.framework.Assert;

public class TestUtil
{
    /**
     * <p>Creates a {@link HashSet} populated with given elements. All duplicate
     * elements, if any, are removed by the set. It is useful for creating sets
     * of few elements in a single line.</p>
     *  
     * @param elements the elements to initialise a set with.
     * 
     * @param <T> the type of the elements.
     * 
     * @return a set that is populated with the given elements.
     */
    public static <T> HashSet<T> set(final T... elements)
    {
        return new HashSet<T>(Arrays.asList(elements));
    }
    
    public static <K, V> HashMap<K, V> map(final Object... parts)
    {
        Assert.assertTrue(parts.length % 2 == 0);
        final HashMap<K, V> map = new HashMap<K, V>();
        for (int i = 0; i < parts.length;) {
            @SuppressWarnings("unchecked") // the caller is responsible for passing correct objects
            final K key = (K) parts[i++];
            @SuppressWarnings("unchecked") // the caller is responsible for passing correct objects
            final V value = (V) parts[i++];
            map.put(key, value);
        }
        return map;
    }
    
    public static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs, final String moduleProperty,
            final ModuleInfo proto, final Map<String, Object> properties)
    {
        assertCallTargetState(task, executed, target, inheritAll, inheritRefs, moduleProperty, proto,
                properties, Collections.<String, Object>emptyMap());
    }
    
    public static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs, final String moduleRefId,
            final ModuleInfo proto, final Map<String, Object> properties, final Map<String, Object> references)
    {
        Assert.assertEquals(executed, task.executed);
        Assert.assertEquals(target, task.target);
        Assert.assertEquals(inheritAll, task.inheritAll);
        Assert.assertEquals(inheritRefs, task.inheritRefs);
        
        final Object moduleObj = task.ownProject.getReference(moduleRefId);
        Assert.assertTrue(moduleObj instanceof Module);
        final Module module = (Module) moduleObj;
        Assert.assertEquals(proto.getPath(), module.getPath());
        Assert.assertEquals(proto.getAttributes(), module.getAttributes());
        final HashSet<String> depPaths = new HashSet<String>();
        for (final Module dep : module.getDependencies()) {
            Assert.assertTrue(depPaths.add(dep.getPath()));
        }
        Assert.assertEquals(proto.getDependencies(), depPaths);
        
        final Map<?, ?> actualProperties = task.ownProject.getProperties();
        actualProperties.remove(MagicNames.PROJECT_BASEDIR);
        Assert.assertEquals(properties, actualProperties);
        
        // merging module property into the properties passed. The module object is not freely available
        final HashMap<String, Object> actualRefs = new HashMap<String, Object>(references);
        actualRefs.put(moduleRefId, module);
        
        assertReferences(task.ownProject, actualRefs);
    }
    
    public static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs,
            final Map<String, Object> properties)
    {
        assertCallTargetState(task, executed, target, inheritAll, inheritRefs,
                properties, Collections.<String, Object>emptyMap());
    }
    
    public static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs,
            final Map<String, Object> properties, final Map<String, Object> references)
    {
        Assert.assertEquals(executed, task.executed);
        Assert.assertEquals(target, task.target);
        Assert.assertEquals(inheritAll, task.inheritAll);
        Assert.assertEquals(inheritRefs, task.inheritRefs);
        
        final Map<?, ?> actualProperties = task.ownProject.getProperties();
        actualProperties.remove(MagicNames.PROJECT_BASEDIR);
        Assert.assertEquals(properties, actualProperties);
        
        assertReferences(task.ownProject, references);
    }
    
    private static void assertReferences(final Project project, final Map<String, Object> expectedReferences) 
    {
        final Map<?, ?> actualReferences = project.getReferences();
        for (final Iterator<?> i = actualReferences.keySet().iterator(); i.hasNext();) {
            if (((String) i.next()).startsWith("ant.")) {
                i.remove();
            }
        }
        Assert.assertEquals(expectedReferences, actualReferences);
    }
    
    public static String getModulePath(final Project project, final String moduleRefId)
    {
        Assert.assertNotNull(project);
        final Object module = project.getReference(moduleRefId);
        Assert.assertNotNull(module);
        Assert.assertTrue(module instanceof Module);
        return ((Module) module).getPath();
    }
}
