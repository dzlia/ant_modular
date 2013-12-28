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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;

/**
 * <p>An Ant task that sets an {@link Module#getAttributes() attribute} of a {@link Module} object
 * as an Ant {@link Project project} property. The attribute is resolved by its name. If the attribute
 * with the given name is undefined for this {@code Module} or set to {@code null} then the property
 * is not set. If the property is already created then it is <em>not</em> updated.</p>
 * 
 * <p>This task works with {@code Module} objects that are loaded by any class loader. It is only
 * required that the class name of the object is exactly {@code afc.ant.modular.Module} and it has
 * the public member function {@link Module#getAttributes()} that returns {@link java.util.Map}.
 * Incompatible module objects passed cause an exception raised by this task.</p>
 * 
 * <h3>Task input</h3>
 * <table border="1">
 * <thead>
 *  <tr><th>Attribute</th>
 *      <th>Required?</th>
 *      <th>Description</th></tr>
 * </thead>
 * <tbody>
 *  <tr><td>moduleProperty</td>
 *      <td>yes</td>
 *      <td>The name of the property which holds the module object.</td></tr>
 *  <tr><td>outputProperty</td>
 *      <td>yes</td>
 *      <td>The name of the property where the module's attribute value is to be set.</td></tr>
 *  <tr><td>name</td>
 *      <td>yes</td>
 *      <td>The name of the attribute.</td></tr>
 * </tbody>
 * </table>
 * 
 * <h3>Usage example</h3>
 * <p>
 * <pre>{@literal <getModuleAttribute name="Module-Version" moduleProperty="project.module" outputProperty="project.module.path"/>}</pre>
 * 
 * Here, the module is expected to be set to the property named <em>project.module</em>. After
 * the task executes the module's attribute <em>Module-Version</em> is assigned to the property
 * named <em>project.module.path</em>. Note that the latter property must be undefined and the
 * attribute must be defined and not set to {@code null}. Otherwise {@code <getModuleAttribute>}
 * does not assign the new value to this property.</p>
 * 
 * @see Module#getAttributes()
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class GetModuleAttribute extends Task
{
    private String moduleProperty;
    private String outputProperty;
    private String name;
    
    /**
     * <p>Executes this task. See the {@link GetModuleAttribute class description} for the
     * details.</p>
     * 
     * @throws BuildException if the task is configured incorrectly or if the module object
     *      specified is not a well-formed {@link Module} instance.
     */
    @Override
    public void execute()
    {
        if (moduleProperty == null) {
            throw new BuildException("The attribute 'moduleProperty' is undefined.");
        }
        if (outputProperty == null) {
            throw new BuildException("The attribute 'outputProperty' is undefined.");
        }
        if (name == null) {
            throw new BuildException("The attribute 'name' is undefined.");
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
        
        final Object value = ModuleUtil.getAttributes(moduleObject).get(name);
        if (value != null) { // null value must not be passed to #setNewProperty.
            propHelper.setNewProperty((String) null, outputProperty, value);
        }
    }
    
    /**
     * <p>Sets the name of the property which holds the module whose attribute is to be obtained.</p>
     * 
     * @param moduleProperty the name of the property. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setModuleProperty(final String moduleProperty)
    {
        this.moduleProperty = moduleProperty;
    }
    
    /**
     * <p>Sets the name of the property to which the module's attribute is to be set. This property
     * should be undefined. Otherwise this task will not assign the new value to it.</p>
     * 
     * @param propertyName the name of the output property. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setOutputProperty(final String propertyName)
    {
        outputProperty = propertyName;
    }
    
    /**
     * <p>Sets the name of the module's attribute which is to be set as an Ant
     * {@link Project project} property. The attribute with the given name must be defined and
     * not set to {@code null} for this task to have effect.</p>
     * 
     * @param name the name of the attribute. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} will be
     *      thrown by {@link #execute()}.
     */
    public void setName(final String name)
    {
        this.name = name;
    }
}
