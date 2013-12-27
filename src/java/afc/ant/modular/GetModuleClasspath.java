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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

public class GetModuleClasspath extends Task
{
    private String moduleProperty;
    private final ArrayList<ClasspathAttribute> classpathAttributes = new ArrayList<ClasspathAttribute>();
    private String outputProperty;
    private boolean includeDependencies = false;
    
    @Override
    public void execute()
    {
        if (moduleProperty == null) {
            throw new BuildException("The attribute 'moduleProperty' is undefined.");
        }
        if (classpathAttributes.isEmpty()) {
            throw new BuildException("Classpath attributes are not defined.");
        }
        if (outputProperty == null) {
            throw new BuildException("The attribute 'outputProperty' is undefined.");
        }
        for (final ClasspathAttribute classpathAttribute : classpathAttributes) {
            if (classpathAttribute.name == null) {
                throw new BuildException("A classpath attribute with undefined name is specified.");
            }
        }
        final Project project = getProject();
        final PropertyHelper propHelper = PropertyHelper.getPropertyHelper(project);
        final Object moduleObject = propHelper.getProperty(moduleProperty);
        if (moduleObject == null) {
            throw new BuildException(MessageFormat.format(
                    "No module is found under the property ''{0}''.", moduleProperty));
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
                    "Invalid module type is found under the property ''{0}''. Expected: ''{1}'', found: ''{2}''.",
                    moduleProperty, Module.class.getName(), moduleObject.getClass().getName()));
        }
        
        final Path classpath = new Path(project);
        appendClasspathElements(moduleObject, classpath, new LinkedHashSet<Object>());
        
        propHelper.setNewProperty((String) null, outputProperty, classpath);
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
     * <p>Sets the name of the property which holds the module whose classpath is to be
     * calculated.</p>
     * 
     * @param moduleProperty the name of the property. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setModuleProperty(final String moduleProperty)
    {
        this.moduleProperty = moduleProperty;
    }
    
    public void setClasspathAttribute(final String name)
    {
        createClasspathAttribute().setName(name);
    }
    
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
     * <p>Sets the name of the property to which the classpath calculated (an instance of
     * {@link Path  org.apache.tools.ant.types.Path}) is to be set. This property should be
     * undefined. Otherwise this task will not assign the new value to it.</p>
     * 
     * @param outputProperty the name of the property. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setOutputProperty(final String outputProperty)
    {
        this.outputProperty = outputProperty;
    }
    
    public void setIncludeDependencies(final boolean option)
    {
        includeDependencies = option;
    }
}
