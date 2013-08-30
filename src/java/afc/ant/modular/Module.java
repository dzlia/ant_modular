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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Module
{
    private final String path;
    
    /* A set of dependencies upon other modules is emulated via ArrayList which is memory-efficient
     * and has fast #add and #iterator#next operations. Uniqueness of modules is ensured by
     * implementation of ModuleRegistry and ModuleInfo. The latter does not allow the same path
     * to be added to the dependencies twice. Clients of Module see the module dependencies as a Set
     * object that guarantees uniqueness of its elements.
     * 
     * This field has the package-level access. This is used by dependency resolvers to avoid
     * overhead associated with iterators (virtual function call, longer dereference chain, etc.).
     */
    final ArrayList<Module> dependencies = new ArrayList<Module>();
    private final Set<Module> dependenciesView = Collections.unmodifiableSet(new ArrayListSet<Module>(dependencies));
    
    private final HashMap<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, Object> attributesView = Collections.unmodifiableMap(attributes);
    
    // path is assumed to end with '/'
    Module(final String path)
    {
        if (path == null) {
            throw new NullPointerException("path");
        }
        this.path = path;
    }
    
    public String getPath()
    {
        return path;
    }
    
    void addDependency(final Module dependency)
    {
        /* Add dependency is not public/protected. The package developer is responsible
           for passing valid dependencies. */
        assert dependency != null;
        assert dependency != this;
        assert !dependencies.contains(dependency);
        dependencies.add(dependency);
    }
    
    /**
     * <p>Replaces the dependencies of this {@code Module} with given {@code Module} objects.
     * The new dependencies become visible immediately via a set returned by
     * {@link #getDependencies()}.</p>
     * 
     * <p>The input collection is not modified by this function and ownership over it is not
     * passed to this {@code Module}.</p>
     * 
     * @param dependencies {@code Module} objects that this {@code Module} is to depend upon.
     *      This collection and all its elements are to be non-{@code null}. This collection must not
     *      contain this {@code Module}.
     * 
     * @throws NullPointerException if <i>dependencies</i> or any its element is {@code null}.
     *      This {@code Module} instance is not modified in this case.
     * @throws IllegalArgumentException if <i>dependencies</i> contains this {@code Module}.
     *      This {@code Module} instance is not modified in this case.
     */
    void setDependencies(final Collection<Module> dependencies)
    {
        if (dependencies == null) {
            throw new NullPointerException("dependencies");
        }
        // Iteration is used instead of Collection#contains because not all collections support null elements.
        for (final Module dependency : dependencies) {
            if (dependency == null) {
                throw new NullPointerException("dependencies contains null dependency.");
            }
            if (dependency == this) {
                throw new IllegalArgumentException("Cannot add itself as a dependency.");
            }
        }
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }
    
    /**
     * <p>Returns a set of modules which this {@code Module} depends upon. The {@code Module}
     * objects returned are necessarily non-{@code null}. The set returned is unmodifiable.
     * In addition, any further modification of this module's dependencies is immediately
     * visible in the set returned.</p>
     * 
     * <p><strong>Note</strong>: the cost of the {@code Set#contains(Object)} operation
     * of the set returned is {@code O(n)}.</p>
     * 
     * @return an unmodifiable set of this module's dependee modules.
     */
    public Set<Module> getDependencies()
    {
        return dependenciesView;
    }
    
    public void addAttribute(final String attributeName, final Object value)
    {
        if (attributeName == null) {
            throw new NullPointerException("attributeName");
        }
        attributes.put(attributeName, value);
    }
    
    /**
     * <p>Replaces the attributes of this {@code Module} with given attributes.
     * The new attributes become visible immediately via a set returned by
     * {@link #getAttributes()}.</p>
     * 
     * <p>The input map is not modified by this function and ownership over it is not
     * passed to this {@code Module}.</p>
     * 
     * @param attributes the new attributes to be assigned to this {@code Module}.
     *      This map must be non-{@code null}.
     * 
     * @throws NullPointerException if <i>attributes</i> is {@code null}.
     *      This {@code Module} instance is not modified in this case.
     */
    public void setAttributes(final Map<String, Object> attributes)
    {
        if (attributes == null) {
            throw new NullPointerException("attributes");
        }
        // Iteration is used instead of Map#containsKey because not all maps support null keys.
        for (final String attributeName : attributes.keySet()) {
            if (attributeName == null) {
                throw new NullPointerException("attributes contains an attribute with null name.");
            }
        }
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }
    
    /**
     * <p>Returns this {@code Module}'s attributes. The map returned is necessarily
     * non-{@code null} and unmodifiable. In addition, any further modification of this
     * {@code Module}'s attributes by means of the {@link #addAttribute(String, Object)}
     * and {@link #setAttributes(Map)} operations is immediately visible in the map returned.</p>
     * 
     * @return an unmodifiable map of this {@code Module}'s attributes.
     */
    public Map<String, Object> getAttributes()
    {
        return attributesView;
    }
    
    /* An adaptor from ArrayList to Set. Lists without duplicate elements are supported only.
     * It must be used with an unmodifiable wrapper.
     */
    private static class ArrayListSet<T> extends AbstractSet<T>
    {
        private final ArrayList<T> list;
        
        public ArrayListSet(final ArrayList<T> list)
        {
            assert list != null;
            assert new HashSet<T>(list).size() == list.size();
            this.list = list;
        }

        @Override
        public Iterator<T> iterator()
        {
            return list.iterator();
        }
        
        @Override
        public int size()
        {
            return list.size();
        }
        
        @Override
        public boolean contains(final Object o)
        {
            return list.contains(o);
        }
    }
}
