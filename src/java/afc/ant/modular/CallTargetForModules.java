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

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;

public class CallTargetForModules extends Task
{
    private ArrayList<ModuleElement> moduleElements = new ArrayList<ModuleElement>();
    private ModuleLoader moduleLoader;
    // If defined then the correspondent Module object is set to this property for each module being processed.
    private String moduleProperty;
    
    private String target;
    private final ArrayList<ParamElement> params = new ArrayList<ParamElement>();
    private final ArrayList<Reference> references = new ArrayList<Reference>();
    private final PropertySet propertySet = new PropertySet();
    
    // Default values match antcall's defaults.
    private boolean inheritAll = true;
    private boolean inheritRefs = false;
    
    // The number of threads used to build modules.
    private int threadCount = 1;
    
    @Override
    public void execute() throws BuildException
    {
        if (target == null) {
            throw new BuildException("The attribute 'target' is undefined.");
        }
        if (moduleLoader == null) {
            throw new BuildException("No module loader is defined.");
        }
        
        final int moduleCount = moduleElements.size();
        if (moduleCount == 0) {
            throw new BuildException("At least one <module> element is required.");
        }
        
        for (int i = 0; i < moduleCount; ++i) {
            final ModuleElement moduleParam = moduleElements.get(i);
            if (moduleParam.path == null) {
                throw new BuildException("There is a <module> element with the attribute 'path' undefined.");
            }
        }
        
        final ModuleRegistry registry = new ModuleRegistry(moduleLoader);
        
        try {
            final ArrayList<Module> modules = new ArrayList<Module>(moduleCount);
            // These targets will be invoked for these modules despite of the default target name.
            final IdentityHashMap<Module, String> overriddenTargets =
                    new IdentityHashMap<Module, String>(modules.size());
            
            for (int i = 0, n = moduleCount; i < n; ++i) {
                final ModuleElement moduleParam = moduleElements.get(i);
                
                final Module module = registry.resolveModule(moduleParam.path);
                modules.add(module);
                
                /* Resolving the name of the target to be invoked for this module. If the choice
                 * if ambiguous (i.e. there are multiple <module> elements that define the same
                 * module whose target name configured is different) then a BuildException
                 * is thrown to terminate the build.
                 */
                String moduleTarget = moduleParam.target == null ? target : moduleParam.target;
                final String oldTarget = overriddenTargets.put(module, moduleTarget);
                if (oldTarget != null && !oldTarget.equals(moduleTarget)) {
                    throw new BuildException(MessageFormat.format(
                            "Ambiguous choice of the target to be invoked for the module ''{0}''. " +
                            "At least the targets ''{1}'' and ''{2}'' are configured.",
                            module.getPath(), oldTarget, moduleTarget));
                }
            }
            
            /* Params that define the property with the name equal to the moduleProperty value
             * (if the latter is set) must be removed so that moduleProperty overrides them
             * in module-specific projects.
             */
            deleteModulePropertyParams();
            
            if (threadCount == 1) {
                processModulesSerial(modules, overriddenTargets);
            } else {
                processModulesParallel(modules, overriddenTargets);
            }
        }
        catch (ModuleNotLoadedException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
        catch (CyclicDependenciesDetectedException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
    }
    
    private void callTarget(final Module module, final String target)
    {
        final CallTarget antcall = (CallTarget) getProject().createTask("antcall");
        antcall.init();
        
        /* moduleProperty is expected to override properties with the same name
         * defined in params. That's why it goes first.
         */
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
        
        try {
            antcall.perform();
        }
        catch (RuntimeException ex) {
            throw buildExceptionForModule(ex, module);
        }
    }
    
    private BuildException buildExceptionForModule(final Throwable cause, final Module module)
    {
        final BuildException ex = new BuildException(MessageFormat.format(
                "Module ''{0}'': {1}", module.getPath(), cause.getMessage()), cause);
        
        // Gathering all information available from the original build exception.
        if (cause instanceof BuildException) {
            /* The thread that generated this exception is either the current thread or
               a dead thread so synchronisation is not needed to read location. */
            final Location location = ((BuildException) cause).getLocation();
            ex.setLocation(location == null ? Location.UNKNOWN_LOCATION : location);
            ex.setStackTrace(cause.getStackTrace());
        }
        
        return ex;
    }
    
    // Removes all params which define a property with the name equal to what is set to moduleProperty.
    private void deleteModulePropertyParams()
    {
        if (moduleProperty == null) {
            return;
        }
        for (int i = params.size() - 1; i >= 0; --i) {
            final String paramName = params.get(i).name;
            if (paramName != null && moduleProperty.equals(paramName)) {
                params.remove(i);
            }
        }
    }
    
    private void processModulesSerial(final ArrayList<Module> modules,
            final IdentityHashMap<Module, String> overriddenTargets) throws CyclicDependenciesDetectedException
    {
        final SerialDependencyResolver dependencyResolver = new SerialDependencyResolver();
        dependencyResolver.init(modules);
        
        Module module;
        while ((module = dependencyResolver.getFreeModule()) != null) {
            String target = overriddenTargets.get(module);
            if (target == null) {
                target = this.target;
            }
            
            callTarget(module, target);
            
            dependencyResolver.moduleProcessed(module);
        }
    }
    
    private void processModulesParallel(final ArrayList<Module> modules,
            final IdentityHashMap<Module, String> overriddenTargets) throws CyclicDependenciesDetectedException
    {
        final ParallelDependencyResolver dependencyResolver = new ParallelDependencyResolver();
        dependencyResolver.init(modules);
        
        final AtomicBoolean buildFailed = new AtomicBoolean(false);
        final AtomicReference<Throwable> buildFailureException = new AtomicReference<Throwable>();
        
        /* A stateless worker to process modules using ParallelDependencyResolver.
         * This instance can be used by multiple threads simultaneously.
         * 
         * It does not throw an exception outside run() and preserves the
         * interrupted status of the thread it is executed in.
         */
        final Runnable parallelBuildWorker = new Runnable()
        {
            public void run()
            {
                try {
                    do {
                        final Module module = dependencyResolver.getFreeModule();
                        if (module == null) {
                            /* Either all modules are processed or the build has failed and
                             * the resolver was aborted. Finishing execution.
                             */
                            return;
                        }
                        
                        String target = overriddenTargets.get(module);
                        if (target == null) {
                            target = CallTargetForModules.this.target;
                        }
                        
                        /* Do not call dependencyResolver#moduleProcessed in case of exception!
                         * This could make the modules that depend upon this module
                         * (whose processing has failed!) free for acquisition, despite of
                         * their dependee module did not succeed.
                         * 
                         * Instead, dependencyResolver#abort() is called.
                         */
                        callTarget(module, target);
                        
                        // Reporting this module as processed if no error is encountered.
                        dependencyResolver.moduleProcessed(module);
                    } while (!Thread.currentThread().isInterrupted());
                }
                catch (Throwable ex) {
                    buildFailed.set(true);
                    buildFailureException.set(ex);
                    /* Ensure that other threads will stop module processing right after
                       their current module is processed. */
                    dependencyResolver.abort();
                }
            }
        };
        
        // The current thread will be the last thread to process modules.
        final int threadsToCreate = threadCount-1;
        final Thread[] threads = new Thread[threadsToCreate];
        for (int i = 0; i < threadsToCreate; ++i) {
            final Thread t = new Thread(parallelBuildWorker);
            threads[i] = t;
            t.start();
        }
        
        try {
            // The current thread is one of the threads that process the modules.
            parallelBuildWorker.run();
        }
        finally {
            /* Waiting for all worker threads to finish even if the build fails on
             * a module that was being processed by this thread. This will allow the
             * 'build failed' message to be the last one.
             */
            joinThreads(threads);
        }
        
        if (buildFailed.get()) {
            /* buildFailureException could contain either RuntimeException or Error
               because this is what could be thrown in thread#run(). */
            final Throwable ex = buildFailureException.get();
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex; // includes properly initialised BuildException
            } else {
                throw (Error) ex;
            }
        }
    }
    
    private static void joinThreads(final Thread[] threads)
    {
        /* parallelBuildWorker preserves the interrupted status of the current thread
         * so an InterruptedException is thrown if this thread starts waiting
         * for a helper thread to join. This leads to all helper threads interrupted
         * so that the build finished rapidly and gracefully.
         */
        try {
            for (final Thread t : threads) {
                t.join();
            }
        }
        catch (InterruptedException ex) {
            /* Interrupting all the activities so that the build finishes as quick as possible.
             * This is fine if an already finished thread is interrupted.
             */
            for (final Thread t : threads) {
                t.interrupt();
            }
            
            Thread.currentThread().interrupt();
            throw new BuildException("The build thread was interrupted.");
        }
    }
    
    /**
     * <p>Creates a new {@link ModuleElement ModuleElement} container that backs the
     * nested element {@code <module>} of this {@code <callTargetForModules>} task.
     * The module it refers to will be built by this {@code <callTargetForModules>} along
     * with all modules it depends upon (directly or indirectly).</p>
     * 
     * <p>At least one {@code <module>} element must be specified. Otherwise the build
     * process fails.</p>
     * 
     * @return the {@code ModuleElement} created. It is never {@code null}.
     * 
     * @see #setTarget(String)
     */
    public ModuleElement createModule()
    {
        final ModuleElement module = new ModuleElement();
        moduleElements.add(module);
        return module;
    }
    
    /**
     * <p>Sets a {@link ModuleLoader} that is to be used by this {@code <callTargetForModules>}
     * task. One and only one module loader must be defined for a {@code <callTargetForModules>}
     * task. The name of the nested element is defined by the name of the Ant type used to pass
     * this instance of {@code ModuleLoader}.</p>
     * 
     * @param moduleLoader the {@code ModuleLoader} instance to be used by this
     *      {@code <callTargetForModules>} task. {@code null} value is not allowed.
     * 
     * @throws BuildException if more than one {@code ModuleLoader} is defined for this
     *      {@code <callTargetForModules>} task.
     * @throws NullPointerException if <em>moduleLoader</em> is {@code null}.
     */
    public void addConfigured(final ModuleLoader moduleLoader)
    {
        if (moduleLoader == null) {
            throw new NullPointerException("moduleLoader");
        }
        if (this.moduleLoader != null) {
            throw new BuildException("Only a single module loader element is allowed.");
        }
        this.moduleLoader = moduleLoader;
    }
    
    /**
     * <p>Sets the number of threads to be used by this {@code <callTargetForModules>}
     * task to build independent modules in parallel. If <em>1</em> is passed then
     * modules are built sequentally. By default, the number of threads used is
     * <em>1</em>.</p>
     * 
     * <p>This setter is accessible via the attribute {@code threadCount} of this
     * {@code <callTargetForModules>} task.</p>
     * 
     * @param threadCount the number of threads to be set. It must be a positive value.
     * 
     * @throws BuildException if <em>threadCount</em> is non-positive.
     * 
     * @see SerialDependencyResolver
     * @see ParallelDependencyResolver
     */
    public void setThreadCount(final int threadCount)
    {
        if (threadCount <= 0) {
            throw new BuildException(MessageFormat.format(
                    "Invalid thread count: ''{0}''. It must be a positive value.",
                    String.valueOf(threadCount)));
        }
        this.threadCount = threadCount;
    }
    
    /**
     * <p>Sets the name of the Ant property in a module-specific project that is assigned
     * with the {@link Module} instance that is associated with this module. If it is not
     * set then the {@code Module} is not passed to that project. This property value overrides
     * the property with the same name passed with {@link #createParam() &lt;param&gt;} elements.
     * If the property with the same name is defined in the caller project or in the module
     * project then it is overridden regardless of what is set to the
     * {@link #setInheritAll(boolean) inheritAll} attribute. However, the user-defined property
     * with the same name is not overridden.</p>
     * 
     * <p>It is used to extract module metadata in module-specific Ant projects. In addition,
     * it could be used to pass information between module-specific projects via module
     * attributes (which is possible if two modules are linked one to the other).</p>
     * 
     * @param propertyName the name of the property to set {@code Module} instances to.
     *      An empty string is considered a defined property.
     * 
     * @see Module
     * @see GetModuleAttribute
     * @see GetModulePath
     * @see GetModuleClasspath
     */
    public void setModuleProperty(final String propertyName)
    {
        moduleProperty = propertyName;
    }
    
    /**
     * <p>Sets the name of the target to be invoked by this {@code <callTargetForModules>}
     * for modules involved in the build process by default. The target is expected to be
     * defined in the current Ant project. If at least one module uses this target and the
     * target itself is undefined in the current Ant project then the build fails.</p>
     * 
     * <p>A non-default target could be defined for a module by means of the attribute
     * {@link ModuleElement#setTarget(String) target} of the nested element
     * {@link #createModule() &lt;module&gt;}. </p>
     * 
     * <p>The attribute {@code target} is required. It must be defined even if all modules
     * involved have custom targets.</p>
     * 
     * @param target the name of the target to invoke for modules by default.
     */
    public void setTarget(final String target)
    {
        this.target = target;
    }
    
    public void setInheritAll(final boolean inheritAll)
    {
        this.inheritAll = inheritAll;
    }
    
    public void setInheritRefs(final boolean inheritRefs)
    {
        this.inheritRefs = inheritRefs;
    }
    
    /**
     * <p>Creates a new {@link ParamElement ParamElement} container that backs the nested
     * element {@code <param>} of this {@code <callTargetForModules>} task. Multiple
     * nested {@code <param>} elements are allowed.</p>
     * 
     * <p>This element represents a property set that is passed to the Ant project
     * created for each module or any project created in that project regardless of what
     * is set to {@link #setInheritAll(boolean) inheritAll}. This property set overrides the
     * properties with the same name defined in the project it is passed to. However, it does
     * not override the user-defined properties with the same name. Nor it overrides the
     * property defined by the attribute {@link #setModuleProperty(String) moduleProperty}.</p>
     * 
     * <p>This allows you to parameterise targets that are invoked for modules.</p>
     * 
     * @return the {@code ParamElement} created. It is never {@code null}.
     */
    public ParamElement createParam()
    {
        final ParamElement param = new ParamElement();
        param.setProject(getProject());
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
    
    /**
     * <p>Serves as the nested element {@code <module>} of the task
     * {@link CallTargetForModules &lt;callTargetForModules&gt;} and defines the root modules
     * to start build with. All modules this module depend upon (directly or indirectly)
     * are included into the build process.</p>
     * 
     * <h3>Attributes</h3>
     * <table border="1">
     * <thead>
     *  <tr><th>Attribute</th>
     *      <th>Required?</th>
     *      <th>Description</th></tr>
     * </thead>
     * <tbody>
     *  <tr><td>path</td>
     *      <td>yes</td>
     *      <td>The path of the module to be included. Non-normalised paths are allowed.</td></tr>
     *  <tr><td>target</td>
     *      <td>no</td>
     *      <td>Specifies the name of the target that must be invoked for this module by
     *          {@code <callTargetForModules>}. If this attribute is undefined then the
     *          target defined in {@code <callTargetForModules>} is used.
     *          This target name is not propagated to the dependee modules.</td></tr>
     * </tbody>
     * </table>
     */
    public static class ModuleElement
    {
        private String path;
        private String target;
        
        /**
         * <p>Sets the path of the module to be included into the build process.</p>
         * 
         * @param path the module path. Non-normalised paths are allowed.
         *      {@code null} should not be set because this leads to build failure.
         */
        public void setPath(final String path)
        {
            this.path = path;
        }
        
        /**
         * <p>Sets the target to be invoked for the module defined by this
         * {@code <module>} element. If the module-specific target is undefined then
         * the target defined in {@code <callTargetForModules>} is used.</p>
         * 
         * @param target the name of the target to be invoked.
         */
        public void setTarget(final String target)
        {
            this.target = target;
        }
    }
    
    public static class ParamElement extends ProjectComponent
    {
        private String name;
        private String value;
        private File location;
        private File file;
        private URL url;
        private String resource;
        private Path classpathAttribute;
        private Path classpath;
        private Reference classpathRef;
        private String environment;
        private Reference reference;
        private String prefix;
        // relative and basedir introduced in Ant 1.8.0 are not available because Ant 1.6+ is supported.
        // prefixValues introduced in Ant 1.8.2 is not available because Ant 1.6+ is supported.
        
        private boolean nameSet;
        private boolean valueSet;
        private boolean locationSet;
        private boolean fileSet;
        private boolean urlSet;
        private boolean resourceSet;
        private boolean classpathAttributeSet;
        private boolean classpathRefSet;
        private boolean environmentSet;
        private boolean referenceSet;
        private boolean prefixSet;
        
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
        
        public void setLocation(final File location)
        {
            this.location = location;
            locationSet = true;
        }
        
        public void setFile(final File file)
        {
            this.file = file;
            fileSet = true;
        }
        
        public void setUrl(final URL url)
        {
            this.url = url;
            urlSet = true;
        }
        
        public void setResource(final String resource)
        {
            this.resource = resource;
            resourceSet = true;
        }
        
        public void setClasspath(final Path classpath)
        {
            classpathAttribute = classpath;
            classpathAttributeSet = true;
        }
        
        public Path createClasspath()
        {
            if (classpath == null) {
                return classpath = new Path(getProject());
            } else {
                return classpath.createPath();
            }
        }
        
        public void setClasspathRef(final Reference reference)
        {
            classpathRef = reference;
            classpathRefSet = true;
        }
        
        public void setEnvironment(final String environment)
        {
            this.environment = environment;
            environmentSet = true;
        }
        
        public void setRefid(final Reference reference)
        {
            this.reference = reference;
            referenceSet = true;
        }
        
        public void setPrefix(final String prefix)
        {
            this.prefix = prefix;
            prefixSet = true;
        }
        
        private void populate(final Property property)
        {
            if (nameSet) {
                property.setName(name);
            }
            if (valueSet) {
                property.setValue(value);
            }
            if (locationSet) {
                property.setLocation(location);
            }
            if (fileSet) {
                property.setFile(file);
            }
            if (urlSet) {
                property.setUrl(url);
            }
            if (resourceSet) {
                property.setResource(resource);
            }
            if (classpathAttributeSet) {
                property.setClasspath(classpathAttribute);
            }
            if (classpath != null) {
                property.createClasspath().add(classpath);
            }
            if (classpathRefSet) {
                property.setClasspathRef(classpathRef);
            }
            if (environmentSet) {
                property.setEnvironment(environment);
            }
            if (referenceSet) {
                property.setRefid(reference);
            }
            if (prefixSet) {
                property.setPrefix(prefix);
            }
        }
    }
}
