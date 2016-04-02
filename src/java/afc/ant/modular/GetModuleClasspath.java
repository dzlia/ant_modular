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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * <p>An Ant task that generates a classpath for a given {@link Module module} and sets it as
 * an {@link Path org.apache.tools.ant.types.Path} instance as an Ant {@link Project project}
 * reference. If the reference already exists then it is updated with the new value.</p>
 * 
 * <p>The resulting classpath is built from the module attributes that are specified by
 * the {@link #setClasspathAttribute(String) attribute}/{@link #createClasspathAttribute() elements}
 * {@code classpathAttribute}. If the attribute with the given name is undefined for a module
 * then it is ignored. If the attribute
 * {@link #setIncludeDependencies(boolean) includeDependencies} is set to {@code true} then
 * the attribute with the given name of each dependee module is used as a contributor to
 * the resulting classpath.</p>
 * 
 * <p>At least one {@code classpathAttribute} attribute/element must be defined. Each classpath
 * attribute, if defined, must be an instance of {@link Path org.apache.tools.ant.types.Path}.
 * Otherwise an {@link BuildException org.apache.tools.ant.BuildException} is thrown by
 * {@link #execute()}.</p>
 * 
 * <p>The resulting classpath is built as follows:</p>
 * <ol type="1">
 *  <li>An empty resulting {@code Path} is created relative to the Ant project of this task.</li>
 *  <li>The attributes of the {@link #setModuleRefId(Reference) primary module} are iterated
 *      through in the order they are declared and the {@code Path} objects stored are appended
 *      to the resulting {@code Path}. The attribute {@code classpathAttribute}, if defined,
 *      always precedes the elements {@code classpathAttribute}, if any.</li>
 *  <li>If {@code includeDependencies} is set to {@code true} then the step {@code 2} is repeated
 *      for each dependee module (direct and indirect). The order in which the dependee modules
 *      are processed is undefined.</li>
 * </ol>
 * 
 * <p>This task accepts {@code Module} objects that are loaded by any class loader.
 * It is only required that the class name of the object is exactly {@code afc.ant.modular.Module}
 * and it has the public member function {@link Module#getAttributes()} that returns a {@link Map}.
 * Incompatible module objects passed cause an exception raised by this task.</p>
 * 
 * <h3>Task input</h3>
 * <h4>Attributes</h4>
 * <table border="1">
 * <thead>
 *  <tr><th>Attribute</th>
 *      <th>Required?</th>
 *      <th>Description</th></tr>
 * </thead>
 * <tbody>
 *  <tr><td>moduleRefId</td>
 *      <td>yes</td>
 *      <td>The ID of the reference which holds the module object.</td></tr>
 *  <tr><td>outputProperty (an attribute)</td>
 *      <td>yes</td>
 *      <td>The ID of the reference where the resulting classpath is to be set.</td></tr>
 *  <tr><td>classpathAttribute</td>
 *      <td>no (at least one {@code classpathAttribute} attribute/element must be defined)</td>
 *      <td>The name of a module attribute that is to be used (probably, with other classpath
 *          attributes) to build up the resulting classpath.</td></tr>
 *  <tr><td>includeDependencies</td>
 *      <td>no</td>
 *      <td>The flag whether or not the classpath elements of the dependee modules (direct and
 *          indirect) of the primary module are to be included into the resulting classpath.
 *          If {@code true} is set then the dependee modules are taken into account;
 *          if {@code false} is set then they are ignored. {@code false} is the default value.</td></tr>
 * </tbody>
 * </table>
 * <h4>Elements</h4>
 * <h5>classpathAttribute</h5>
 * <p>Defines the name of a module attribute that is to be used (probably, with other classpath
 * attributes) to build up the resulting classpath. This element is optional, but can be used
 * multiple times. At least one {@code classpathAttribute} attribute/element must be defined.</p>
 * <table border="1">
 * <thead>
 *  <tr><th>Attribute</th>
 *      <th>Required?</th>
 *      <th>Description</th></tr>
 * </thead>
 * <tbody>
 *  <tr><td>name</td>
 *      <td>yes</td>
 *      <td>The name of the module classpath attribute.</td></tr>
 * </tbody>
 * </table>
 * 
 * <h3>Usage example</h3>
 * <pre> {@literal <getModuleClasspath moduleRefId="project.module" outputRefId="project.module.classpath" classpathAttribute="testClasspath">
 *      <classpathAttribute name="runtimeClasspath"/>
 *      <classpathAttribute name="compileClasspath"/>
 * </getModuleClasspath>}</pre>
 * 
 * <p>Here, the module is expected to be set to the reference <em>project.module</em>.
 * The resulting classpath is built up from the classpath attributes {@code testClasspath},
 * {@code runtimeClasspath}, {@code compileClasspath}, exactly in this order. After the task
 * executes the resulting classpath is assigned to the reference <em>project.module.classpath</em>.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class GetModuleClasspath extends Task
{
    private Reference moduleRef;
    private final ArrayList<ClasspathAttribute> classpathAttributes = new ArrayList<ClasspathAttribute>();
    private String outputRefId;
    private boolean includeDependencies = false;
    
    /**
     * <p>Executes this task. See the {@link GetModuleClasspath class description} for the
     * details.</p>
     * 
     * 
     * @throws BuildException in any of these cases:
     *      <ul>
     *          <li>this task is configured incorrectly</li>
     *          <li>the {@link #setModuleRefId(Reference) module object} specified is not a
     *              well-formed {@link Module} instance</li>
     *          <li>{@link #setIncludeDependencies(boolean) includeDependencies} is set to
     *              {@code true} and any of the dependee modules is not a well-formed
     *              {@link Module} instance</li>
     *          <li>some classpath attribute configured is not an instance of
     *              {@link Path org.apache.tools.ant.types.Path} for some module</li>
     *      </ul>
     */
    @Override
    public void execute()
    {
        if (moduleRef == null) {
            throw new BuildException("The attribute 'moduleRefId' is undefined.");
        }
        if (outputRefId == null) {
            throw new BuildException("The attribute 'outputRefId' is undefined.");
        }
        if (classpathAttributes.isEmpty()) {
            throw new BuildException("No classpath attributes are defined.");
        }
        for (final ClasspathAttribute classpathAttribute : classpathAttributes) {
            if (classpathAttribute.name == null) {
                throw new BuildException("A classpath attribute with the undefined name is specified.");
            }
        }
        final Object moduleObject = moduleRef.getReferencedObject();
        if (moduleObject == null) {
            throw new BuildException(MessageFormat.format(
                    "No module is found via the reference ''{0}''.", moduleRef.getRefId()));
        }
        
        /* This task is invoked from within a target that is called by CallTargetForModules.
         * A new project is created for this target and therefore this tag library could be
         * re-defined and loaded by a new class loader.
         * 
         * ModuleUtil is used to handle the original module object with any configuration of
         * Ant class loader hierarchy.
         */
        if (!ModuleUtil.isModule(moduleObject)) {
            throw new BuildException(MessageFormat.format(
                    "Invalid module type is found via the reference ''{0}''. Expected: ''{1}'', found: ''{2}''.",
                    moduleRef.getRefId(), Module.class.getName(), moduleObject.getClass().getName()));
        }
        
        final Path classpath = new Path(getProject());
        appendClasspathElements(moduleObject, classpath, new LinkedHashSet<Object>());
        
        getProject().addReference(outputRefId, classpath);
    }
    
    private void appendClasspathElements(final Object /*Module*/ module, final Path classpath,
            final LinkedHashSet<Object> processedModules)
    {
        if (processedModules.contains(module)) {
            /* Each module is processed only once. In addition, if there are
               cyclic dependencies then there is no infinite recursion. */
            return;
        }
        processedModules.add(module);
        
        appendElements(module, classpath);
        
        if (includeDependencies) {
            for (final Object /*Module*/ dep : ModuleUtil.getDependencies(module)) {
                appendClasspathElements(dep, classpath, processedModules);
            }
        }
    }
    
    private void appendElements(final Object /*Module*/ module, final Path classpath)
    {
        final Map<String, Object> attributes = ModuleUtil.getAttributes(module);
        for (final ClasspathAttribute classpathAttribute : classpathAttributes) {
            final Object o = attributes.get(classpathAttribute.name);
            if (o == null) {
                continue;
            }
            if (!(o instanceof Path)) {
                throw new BuildException(MessageFormat.format(
                        "The attribute ''{0}'' of the module ''{1}'' is not an Ant path.",
                        classpathAttribute.name, ModuleUtil.getPath(module)));
            }
            classpath.add((Path) o);
        }
    }
    
    /**
     * <p>Sets the reference which holds the module whose classpath is to be calculated.</p>
     * 
     * @param ref the reference to the module. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setModuleRefId(final Reference ref)
    {
        moduleRef = ref;
    }
    
    /**
     * <p>Sets the name of a module attribute that is to be used (probably, with other
     * classpath attributes) to build up the resulting classpath. If the attribute with the
     * given name is undefined for a module then it is ignored. If the attribute
     * {@link #setIncludeDependencies(boolean) includeDependencies} is set to {@code true}
     * then the attribute with the given name of each dependee module is used as a
     * contributor to the resulting classpath.</p>
     * 
     * <p>This classpath attribute, if defined, must be an instance of
     * {@link Path org.apache.tools.ant.types.Path}. Otherwise an
     * {@link BuildException org.apache.tools.ant.BuildException} is thrown by
     * {@link #execute()}.</p>
     * 
     * <p>Multiple classpath attributes could be added by using the elements
     * {@link #createClasspathAttribute() &lt;classpathAttribute&gt;}. The attribute and
     * the elements {@code classpathAttribute} can be used simultaneously. At least one
     * {@code classpathAttribute} attribute/element must be defined. Otherwise an
     * {@link BuildException org.apache.tools.ant.BuildException} is thrown by
     * {@link #execute()}.</p>
     * 
     * @param name the name of the module attribute. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} is
     *      thrown by {@link #execute()}.
     */
    public void setClasspathAttribute(final String name)
    {
        final ClasspathAttribute o = new ClasspathAttribute();
        o.setName(name);
        
        /* The attribute 'classpathAttribute' precedes the elements with the same name.
         * 
         * This implementation is not optimal: it costs O(n). This is fine since the total
         * number of the elements 'classpathAttribute' is unlikely to be large.
         */
        classpathAttributes.add(0, o);
    }
    
    /**
     * <p>Creates a new {@link ClasspathAttribute ClasspathAttribute} container that backs
     * the nested element {@code <classpathAttribute>} of this {@code <getModuleClasspath>}
     * task.</p>
     * 
     * <p>This element defines the name of a module attribute that is to be used (probably,
     * with other classpath attributes) to build up the resulting classpath. If the attribute
     * with the given name is undefined for a module then it is ignored. If the attribute
     * {@link #setIncludeDependencies(boolean) includeDependencies} is set to {@code true}
     * then the attribute with the given name of each dependee module is used as a contributor
     * to the resulting classpath.</p>
     * 
     * <p>This classpath attribute, if defined, must be an instance of
     * {@link Path org.apache.tools.ant.types.Path}. Otherwise an
     * {@link BuildException org.apache.tools.ant.BuildException} is thrown by
     * {@link #execute()}.</p>
     * 
     * <p>Multiple nested {@code <classpathAttribute>} elements are allowed. The
     * {@link #setClasspathAttribute(String) attribute} and the elements
     * {@code classpathAttribute} can be used simultaneously. At least one
     * {@code classpathAttribute} attribute/element must be defined. Otherwise an
     * {@link BuildException org.apache.tools.ant.BuildException} is thrown by
     * {@link #execute()}.</p>
     * 
     * @return the {@code ClasspathAttribute} created. It is never {@code null}.
     */
    public ClasspathAttribute createClasspathAttribute()
    {
        final ClasspathAttribute o = new ClasspathAttribute();
        classpathAttributes.add(o);
        return o;
    }
    
    /**
     * <p>Serves as the nested element {@code <classpathAttribute>} or the attribute
     * {@code "classpathAttribute"} of the task
     * {@link GetModuleClasspath &lt;getModuleClasspath&gt;}. This element defines a
     * module attribute that is to be used (probably, with other classpath attributes) to
     * build up the resulting classpath. The name of the classpath attribute is defined
     * by the element attribute {@link #setName(String) &quot;name&quot;}.</p>
     * 
     * <p>The classpath attribute value must be either {@code null} or an instance of
     * {@link Path org.apache.tools.ant.types.Path}. The parent task
     * {@code GetModuleClasspath} throws a
     * {@link BuildException org.apache.tools.ant.BuildException} otherwise.</p>
     * 
     * <h3>Attributes</h3>
     * <table border="1">
     * <thead>
     *  <tr><th>Attribute</th>
     *      <th>Required?</th>
     *      <th>Description</th></tr>
     * </thead>
     * <tbody>
     *  <tr><td>name</td>
     *      <td>yes</td>
     *      <td>The name of the classpath attribute.</td></tr>
     * </tbody>
     * </table>
     */
    public static class ClasspathAttribute
    {
        private String name;
        
        /**
         * <p>Sets the name of the classpath attribute to be used to build up the resulting
         * classpath.</p>
         * 
         * @param name the name to be set. It must be not {@code null}. The parent
         *      {@link GetModuleClasspath} throws a {@link BuildException} otherwise.
         */
        public void setName(final String name)
        {
            this.name = name;
        }
    }
    
    /**
     * <p>Sets the ID of the reference to which the classpath calculated (an instance of
     * {@link Path  org.apache.tools.ant.types.Path}) is to be set.</p>
     * 
     * @param refId the ID of the reference. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setOutputRefId(final String refId)
    {
        outputRefId = refId;
    }
    
    /**
     * <p>Sets the flag whether or not the classpath elements of the dependee modules
     * (direct and indirect) of the {@link #setModuleRefId(Reference) primary module} are to
     * be included into the resulting classpath. If {@code true} is set then the dependee
     * modules are taken into account; if {@code false} is set then they are ignored. The
     * latter case is default. The classpath attributes that are used for the dependee modules
     * are defined by the {@code classpathAttribute} attribute/elements, which is the same as
     * for the primary module.</p>
     * 
     * <p>It is guaranteed that the dependee modules' classpath elements are placed after
     * all classpath elements of the primary module. However, the relative order of the
     * classpath elements of the dependee modules is undefined.</p>
     * 
     * @param flag the flag value to be set.
     */
    public void setIncludeDependencies(final boolean flag)
    {
        includeDependencies = flag;
    }
}
