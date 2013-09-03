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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.PropertySet;

public class CallTargetForModules extends Task
{
    private ArrayList<ModuleElement> moduleElements;
    private ModuleLoader moduleLoader;
    // If defined then the correspondent Module object is set to this property for each module being processed.
    private String moduleProperty;
    
    private String target;
    private boolean targetSet;
    private final ArrayList<ParamElement> params = new ArrayList<ParamElement>();
    private final ArrayList<Reference> references = new ArrayList<Reference>();
    private final PropertySet propertySet = new PropertySet();
    
    // Default values match antcall's defaults.
    private boolean inheritAll = true;
    private boolean inheritRefs = false;
    
    @Override
    public void init() throws BuildException
    {
        moduleProperty = null;
        targetSet = false;
        moduleElements = new ArrayList<ModuleElement>();
    }
    
    @Override
    public void execute() throws BuildException
    {
        if (!targetSet) {
            throw new BuildException("Target is not set.");
        }
        if (moduleLoader == null) {
            throw new BuildException("The module loader element is required.");
        }
        
        final ModuleRegistry registry = new ModuleRegistry(moduleLoader);
        
        try {
            final ArrayList<Module> modules = new ArrayList<Module>(moduleElements.size());
            for (final ModuleElement moduleParam : moduleElements) {
                if (moduleParam.path == null) {
                    throw new BuildException("Module path is undefined.");
                }
                modules.add(registry.resolveModule(moduleParam.path));
            }
            
            // TODO make dependency resolver configurable
            final DependencyResolver dependencyResolver = new SerialDependencyResolver();
            dependencyResolver.init(modules);
            
            Module module;
            // TODO add support for parallelism
            while ((module = dependencyResolver.getFreeModule()) != null) {
                callTarget(module);
                
                dependencyResolver.moduleProcessed(module);
            }
        }
        catch (ModuleNotLoadedException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
        catch (CyclicDependenciesDetectedException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
    }
    
    private void callTarget(final Module module)
    {
        final CallTarget antcall = (CallTarget) getProject().createTask("antcall");
        antcall.init();
        
        if (moduleProperty != null) {
            final Property moduleParam = antcall.createParam();
            moduleParam.setName(moduleProperty);
            moduleParam.setValue(module);
        }
        for (int i = 0, n = params.size(); i < n; ++i) {
            final ParamElement param = params.get(i);
            param.populate(antcall.createParam());
        }
        for (int i = 0, n = references.size(); i < n; ++i) {
            antcall.addReference(references.get(i));
        }
        antcall.addPropertyset(propertySet);
        antcall.setInheritAll(inheritAll);
        antcall.setInheritRefs(inheritRefs);
        antcall.setTarget(target);
        
        antcall.perform();
    }
    
    public ModuleElement createModule()
    {
        final ModuleElement module = new ModuleElement();
        moduleElements.add(module);
        return module;
    }
    
    public void addConfigured(final ModuleLoader loader)
    {
        if (moduleLoader != null) {
            throw new BuildException("Only a single module loader element is allowed.");
        }
        moduleLoader = loader;
    }
    
    public void setModuleProperty(final String propertyName)
    {
        moduleProperty = propertyName;
    }
    
    public void setTarget(final String target)
    {
        this.target = target;
        targetSet = true;
    }
    
    public void setInheritAll(final boolean inheritAll)
    {
        this.inheritAll = inheritAll;
    }
    
    public void setInheritRefs(final boolean inheritRefs)
    {
        this.inheritRefs = inheritRefs;
    }
    
    public ParamElement createParam()
    {
        final ParamElement param = new ParamElement();
        params.add(param);
        return param;
    }
    
    public void addReference(final Reference reference)
    {
        references.add(reference);
    }
    
    public void addPropertyset(final PropertySet propertySet)
    {
        this.propertySet.addPropertyset(propertySet);
    }
    
    // TODO support defining modules using regular expressions
    public static class ModuleElement
    {
        private String path;
        
        public void setPath(final String path)
        {
            if (path == null) {
                throw new BuildException("Module path is undefined.");
            }
            this.path = path;
        }
    }
    
    // TODO add all configuration that is supported by the Ant Property type.
    public static class ParamElement
    {
        private String name;
        private String value;
        
        private boolean nameSet;
        private boolean valueSet;
        
        public void setName(final String name)
        {
            this.name = name;
            nameSet = true;
        }
        
        public void setValue(final String value)
        {
            this.value = value;
            valueSet = true;
        }
        
        public void populate(final Property property)
        {
            if (nameSet) {
                property.setName(name);
            }
            if (valueSet) {
                property.setValue(value);
            }
        }
    }
}
