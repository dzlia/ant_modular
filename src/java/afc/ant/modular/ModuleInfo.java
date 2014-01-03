/* Copyright (c) 2013-2014, Dźmitry Laŭčuk
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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>An entity that serves as a prototype of a {@link Module}. As against {@code Module}
 * objects, {@code ModuleInfo} instances hold references to their dependee module by their
 * paths, not as instances of {@code ModuleInfo} or {@code Module}. This allows metadata
 * to be loaded for all modules before the modules are linked to each other, which is
 * a non-trivial task if there are cyclic dependencies between them.</p>
 * 
 * <p>Each module is identified by its {@link #getPath() path} relative to the root directory
 * of this environment. In addition, each module has metadata associated with it.
 * This metadata consists of {@link #getDependencies() dependencies} and
 * {@link #getAttributes() attributes}.</p>
 * 
 * <p>The module dependencies is a set of modules which this module depends upon.
 * Typically the dependee modules should be processed before this module can be
 * processed. A {@code ModuleInfo} holds references to its dependee modules by their
 * {@link ModuleLoader#normalisePath(String) normalised paths}.</p>
 * 
 * <p>The module attributes are named pieces of data of free format. Attribute names
 * are case-sensitive. The {@code null} name is not allowed. An attribute value can be
 * any object or {@code null}.</p>
 * 
 * <p>A typical workflow of module metadata processing is the following. A {@link ModuleLoader}
 * creates {@code ModuleInfo} instances that hold information about modules. These
 * {@code ModuleInfo}s are then converted into {@code Module}s by {@link ModuleRegistry}.
 * The dependency paths defined in the {@code ModuleInfo} are converted into {@code Module}
 * instances and the attributes are copied with no transformation.</p>
 * 
 * <p>{@code ModuleInfo} is not thread-safe. Its instances are expected to be processed
 * by a single thread.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public final class ModuleInfo
{
    private final String path;
    private final HashSet<String> dependencies = new HashSet<String>();
    private final Set<String> dependenciesView = Collections.unmodifiableSet(dependencies);
    private final HashMap<String, Object> attributes = new HashMap<String, Object>();
    private final Map<String, Object> attributesView = Collections.unmodifiableMap(attributes);
    private final ModuleLoader moduleLoader;
    
    /**
     * <p>Creates a {@code ModuleInfo} with a given path. The normalised path is assigned.
     * The {@code ModuleInfo} instance created has neither dependencies nor attributes.</p>
     * 
     * <p>The {@link ModuleLoader} passed is expected to be the {@code ModuleLoader} that
     * creates this {@code ModuleInfo}. Its function
     * {@link ModuleLoader#normalisePath(String) normalisePath(String)} is used
     * to normalise module paths (the path of this {@code ModuleInfo} and the paths of
     * the dependee modules).</p>
     * 
     * @param path the module path. It must be non-{@code null}.
     * @param moduleLoader the module loader that creates this {@code ModuleInfo}.
     *      It must be non-{@code null}.
     * 
     * @throws NullPointerException if <em>path</em> or the normalised path or
     *      <em>moduleLoader</em> is {@code null}.
     */
    public ModuleInfo(final String path, final ModuleLoader moduleLoader)
    {
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (moduleLoader == null) {
            throw new NullPointerException("moduleLoader");
        }
        this.path = moduleLoader.normalisePath(path);
        if (this.path == null) {
            throw new NullPointerException(MessageFormat.format(
                    "The normalised path that corresponds to the path ''{0}'' is null.", path));
        }
        this.moduleLoader = moduleLoader;
    }
    
    /**
     * <p>Returns the {@link ModuleLoader#normalisePath(String) normalised} path of this
     * {@code ModuleInfo}. It is a path relative to the root directory of the environment
     * that is associated with this {@code ModuleInfo}.</p>
     * 
     * @return the module path that is normalised by this {@code ModuleInfo}'s
     *      {@code ModuleLoader}. It is necessarily non-{@code null}.
     */
    public String getPath()
    {
        return path;
    }
    
    /**
     * <p>Assigns a given module path as a dependency. The path is normalised before it is
     * assigned. The given module path in its normalised form must be not equal to the path
     * of this {@code ModuleInfo}. In addition, it must be non-{@code null}. The new dependency
     * becomes visible immediately via a set returned by {@link #getDependencies()}.</p>
     * 
     * @param dependency the module path to be assigned as a dependency.
     *      It must be non-{@code null}.
     * 
     * @throws NullPointerException if <em>dependency</em> or the dependency in the normalised
     *      form is {@code null}.
     * @throws IllegalArgumentException if <em>dependency</em> in the normalised form is equal
     *      to this {@code ModuleInfo}'s path.
     */
    public void addDependency(final String dependency)
    {
        if (dependency == null) {
            throw new NullPointerException("dependency");
        }
        final String normalisedDependency = moduleLoader.normalisePath(dependency);
        if (normalisedDependency == null) {
            throw new NullPointerException(MessageFormat.format(
                    "The normalised path that corresponds to the path ''{0}'' is null.", dependency));
        }
        if (normalisedDependency.equals(path)) {
            throw new IllegalArgumentException("Cannot add itself as a dependency.");
        }
        dependencies.add(normalisedDependency);
    }
    
    /**
     * <p>Replaces the dependencies of this {@code ModuleInfo} with given module paths.
     * The dependencies assigned are normalised, if needed. The new dependencies become
     * visible immediately via a set returned by {@link #getDependencies()}.</p>
     * 
     * <p>The input collection is not modified by this function and ownership over it is not
     * passed to this {@code ModuleInfo}.</p>
     * 
     * @param dependencies module paths that this {@code ModuleInfo} is to depend upon.
     *      This collection and all its elements are to be non-{@code null}. This collection
     *      must not contain this {@code ModuleInfo}'s path (the normalised paths are compared).
     * 
     * @throws NullPointerException if <em>dependencies</em> or any its element or any dependency
     *      in the normalised form is {@code null}. This {@code ModuleInfo} instance is not
     *      modified in this case.
     * @throws IllegalArgumentException if <em>dependencies</em> contains this {@code ModuleInfo}'s
     *      path (normalised or non-normalised). This {@code ModuleInfo} instance is not modified
     *      in this case.
     */
    public void setDependencies(final Collection<String> dependencies)
    {
        if (dependencies == null) {
            throw new NullPointerException("dependencies");
        }
        
        /* Normalisation could be an expensive operation. Storing all normalised paths here
         * to avoid normalising same paths twice.
         */
        final String[] normalisedPaths = new String[dependencies.size()];
        int i = 0;
        for (final String dependency : dependencies) {
            if (dependency == null) {
                throw new NullPointerException("dependencies contains null dependency.");
            }
            final String normalisedDependency = moduleLoader.normalisePath(dependency);
            if (normalisedDependency == null) {
                throw new NullPointerException(MessageFormat.format(
                        "The normalised path that corresponds to the path ''{0}'' is null.", dependency));
            }
            if (normalisedDependency.equals(path)) {
                throw new IllegalArgumentException("Cannot add itself as a dependency.");
            }
            normalisedPaths[i++] = normalisedDependency;
        }
        
        // The new dependencies are valid. Replacing the existing dependencies with the new ones.
        this.dependencies.clear();
        for (final String normalisedPath : normalisedPaths) {
            this.dependencies.add(normalisedPath);
        }
    }
    
    /**
     * <p>Returns a set of {@link ModuleLoader#normalisePath(String) normalised} module paths
     * which this module depends upon. The set returned and the paths it contains are necessarily
     * non-{@code null}. The set returned is unmodifiable. In addition, any further modification
     * of this {@code ModuleInfo}'s dependencies by means of the {@link #addDependency(String)}
     * and {@link #setDependencies(Collection)} operations is immediately visible in the set
     * returned.</p>
     * 
     * @return an unmodifiable set of this module's dependee module paths that are normalised
     *      by this {@code ModuleInfo}'s {@code ModuleLoader}.
     */
    public Set<String> getDependencies()
    {
        return dependenciesView;
    }
    
    /**
     * <p>Sets a given attribute to this {@code ModuleInfo}. If the attribute with the given name
     * already exists then its value is replaced with the new value. The new attribute becomes
     * visible immediately via a set returned by {@link #getAttributes()}.</p>
     * 
     * @param attributeName the name of the attribute. It must not be {@code null}.
     *      Attribute names are case-sensitive.
     * @param value the attribute value. It can be {@code null}.
     * 
     * @throws NullPointerException if <em>attributeName</em> is {@code null}.
     *      This {@code ModuleInfo} instance is not modified in this case.
     */
    public void addAttribute(final String attributeName, final Object value)
    {
        if (attributeName == null) {
            throw new NullPointerException("attributeName");
        }
        attributes.put(attributeName, value);
    }
    
    /**
     * <p>Replaces the attributes of this {@code ModuleInfo} with given attributes.
     * The new attributes become visible immediately via a set returned by
     * {@link #getAttributes()}.</p>
     * 
     * <p>The input map is not modified by this function and ownership over it is not
     * passed to this {@code ModuleInfo}.</p>
     * 
     * @param attributes the new attributes to be assigned to this {@code ModuleInfo}.
     *      This map must be non-{@code null}.
     * 
     * @throws NullPointerException if <em>attributes</em> is {@code null}.
     *      This {@code ModuleInfo} instance is not modified in this case.
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
     * <p>Returns this module's attributes. The map returned is necessarily non-{@code null} and
     * unmodifiable. In addition, any further modification of this {@code ModuleInfo}'s attributes
     * by means of the {@link #addAttribute(String, Object)} and {@link #setAttributes(Map)}
     * operations is immediately visible in the map returned.</p>
     * 
     * @return an unmodifiable map of this module's attributes.
     */
    public Map<String, Object> getAttributes()
    {
        return attributesView;
    }
}
