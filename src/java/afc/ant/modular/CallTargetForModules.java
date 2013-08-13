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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.PropertySet;

public class CallTargetForModules extends Task
{
    private CallTarget antcall;
    private ArrayList<ModuleElement> moduleElements;
    private ModuleLoader moduleLoader;
    
    private boolean targetSet;
    private boolean moduleLoaderCreated;
    
    @Override
    public void init() throws BuildException
    {
        targetSet = false;
        moduleElements = new ArrayList<ModuleElement>();
        antcall = (CallTarget) getProject().createTask("antcall");
        antcall.init();
    }
    
    @Override
    public void execute() throws BuildException
    {
        if (!targetSet) {
            throw new BuildException("Target is not set.");
        }
        if (moduleLoader == null) {
            throw new BuildException("Module loader is undefined.");
        }
        
        moduleLoader.init(getProject());
        
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
            throw new BuildException(ex);
        }
        catch (CyclicDependenciesDetectedException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void callTarget(final Module module)
    {
        final Property param = antcall.createParam();
        param.setName("module");
        param.setValue(module);
        antcall.perform();
    }
    
    public ModuleElement createModule()
    {
        final ModuleElement module = new ModuleElement();
        moduleElements.add(module);
        return module;
    }
    
    public ModuleLoaderElement createModuleLoader()
    {
        if (moduleLoaderCreated) {
            throw new BuildException("Only a single 'moduleLoader' element is allowed.");
        }
        moduleLoaderCreated = true;
        return new ModuleLoaderElement();
    }
    
    public void setTarget(final String target)
    {
        antcall.setTarget(target);
        targetSet = true;
    }
    
    public void setInheritAll(final boolean inheritAll)
    {
        antcall.setInheritAll(inheritAll);
    }
    
    public void setInheritRefs(final boolean inheritRefs)
    {
        antcall.setInheritRefs(inheritRefs);
    }
    
    public Property createParam()
    {
        return antcall.createParam();
    }
    
    public void addReference(final Reference reference)
    {
        antcall.addReference(reference);
    }
    
    public void addPropertyset(final PropertySet propertySet)
    {
        antcall.addPropertyset(propertySet);
    }
    
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
    
    public class ModuleLoaderElement
    {
        public void setClassname(final String className)
        {
            if (className == null) {
                throw new BuildException("Module loader class name is undefined.");
            }
            
            try {
                // TODO support custom classpath
                final ClassLoader classLoader = CallTargetForModules.class.getClassLoader();
                final Class<?> moduleLoaderClass = classLoader.loadClass(className);
                if (!ModuleLoader.class.isAssignableFrom(moduleLoaderClass)) {
                    throw new BuildException(MessageFormat.format("''{0}'' is not an subclass of ''{1}''.",
                            moduleLoaderClass.getName(), ModuleLoader.class.getName()));
                }
                moduleLoader = (ModuleLoader) moduleLoaderClass.newInstance();
            }
            catch (ClassNotFoundException ex) {
                throw new BuildException(MessageFormat.format("Unable to load class ''{0}''.", className), ex);
            }
            catch (IllegalAccessException ex) {
                throw new BuildException(MessageFormat.format("Unable to instantiate class ''{0}''.", className), ex);
            }
            catch (InstantiationException ex) {
                throw new BuildException(MessageFormat.format("Unable to instantiate class ''{0}''.", className), ex);
            }
        }
    }
}
