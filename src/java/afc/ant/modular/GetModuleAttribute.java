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

public class GetModuleAttribute extends Task
{
    private String moduleProperty;
    private String outputProperty;
    private String name;
    
    @Override
    public void execute()
    {
        if (moduleProperty == null) {
            throw new BuildException("'moduleProperty' is undefined.");
        }
        if (outputProperty == null) {
            throw new BuildException("'outputProperty' is undefined.");
        }
        if (name == null) {
            throw new BuildException("'name' is undefined.");
        }
        final Project project = getProject();
        final PropertyHelper propHelper = PropertyHelper.getPropertyHelper(project);
        final Object moduleObject = propHelper.getProperty(moduleProperty);
        if (moduleObject == null) {
            throw new BuildException(MessageFormat.format(
                    "No module is found under the property ''{0}''.", moduleProperty));
        }
        
        /* This task is invoked from within a target that is called by CallTargetForModules.
         * A new project is created for this target and therefore a new class loader.
         * Multiple instances of the Module class could be created: one for the module instance
         * that is put by CallTargetForModules and one that is loaded by this class' class loader.
         * 
         * Reflection is used to handle the original module object with any configuration of
         * Ant class loader hierarchy.
         */
        if (!ModuleUtil.isModule(moduleObject)) {
            throw new BuildException(MessageFormat.format(
                    "Invalid module type is found under the property ''{0}''. Expected: ''{1}'', found: ''{2}''.",
                    moduleProperty, Module.class.getName(), moduleObject.getClass().getName()));
        }
        
        propHelper.setProperty(outputProperty, ModuleUtil.getAttributes(moduleObject).get(name), false);
    }
    
    public void setModuleProperty(final String moduleProperty)
    {
        this.moduleProperty = moduleProperty;
    }
    
    public void setOutputProperty(final String name)
    {
        outputProperty = name;
    }
    
    public void setName(final String name)
    {
        this.name = name;
    }
}
