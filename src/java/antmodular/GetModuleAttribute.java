/* Copyright (c) 2013-2023, Dźmitry Laŭčuk
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

import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * <p>An Ant task that sets an {@link Module#getAttributes() attribute} of a {@link Module} object
 * as an Ant {@link Project project} reference. The attribute is resolved by its name. If the attribute
 * with the given name is undefined for this {@code Module} or set to {@code null} then the reference
 * is made undefined. If the reference already exists then it is updated with the new value.</p>
 * 
 * <p>This task accepts {@code Module} objects that are loaded by any class loader. It is only
 * required that the class name of the object is exactly {@code antmodular.Module} and it has
 * the public member function {@link Module#getAttributes()} that returns {@link java.util.Map}.
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
 *  <tr><td>outputProperty</td>
 *      <td>yes</td>
 *      <td>The ID of the reference where the module attribute value is to be set.</td></tr>
 *  <tr><td>name</td>
 *      <td>yes</td>
 *      <td>The name of the attribute.</td></tr>
 * </tbody>
 * </table>
 * 
 * <h3>Usage example</h3>
 * <pre>{@literal <getModuleAttribute name="Module-Version" moduleRefId="project.module" outputRefId="project.module.path"/>}</pre>
 * 
 * <p>Here, the module is expected to be set to the reference named <em>project.module</em>. After
 * the task executes the module attribute <em>Module-Version</em> is assigned to the reference
 * <em>project.module.path</em>. If the reference <em>project.module.path</em> is already defined
 * then it is updated with the new value.</p>
 * 
 * @see Module#getAttributes()
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class GetModuleAttribute extends Task
{
    private Reference moduleRef;
    private String outputRefId;
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
        if (moduleRef == null) {
            throw new BuildException("The attribute 'moduleRefId' is undefined.");
        }
        if (outputRefId == null) {
            throw new BuildException("The attribute 'outputRefId' is undefined.");
        }
        if (name == null) {
            throw new BuildException("The attribute 'name' is undefined.");
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
        
        final Object value = ModuleUtil.getAttributes(moduleObject).get(name);
        if (value != null) {
            getProject().addReference(outputRefId, value);
        } else {
            getProject().getReferences().remove(outputRefId);
        }
    }
    
    /**
     * <p>Sets the reference which holds the module whose attribute is to be obtained.</p>
     * 
     * @param ref the reference to the module. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} is
     *      thrown by {@link #execute()}.
     */
    public void setModuleRefId(final Reference ref)
    {
        moduleRef = ref;
    }
    
    /**
     * <p>Sets the ID of the reference to which the module attribute is to be set.</p>
     * 
     * @param refId the ID of the output reference. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} is
     *      thrown by {@link #execute()}.
     */
    public void setOutputRefId(final String refId)
    {
        outputRefId = refId;
    }
    
    /**
     * <p>Sets the name of the module attribute which is to be set as an Ant
     * {@link Project project} reference.</p>
     * 
     * @param name the name of the attribute. It must be not {@code null}.
     *      Otherwise an {@link BuildException org.apache.tools.ant.BuildException} is
     *      thrown by {@link #execute()}.
     */
    public void setName(final String name)
    {
        this.name = name;
    }
}
