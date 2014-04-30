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

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;

/**
 * <p>An Ant task that executes a target for each module specified and all their dependee modules.
 * An order in which the modules' targets are executed is such that for each module all
 * the dependee modules are processed before this module is processed. An order in which
 * independent modules are executed is undefined.</p>
 * 
 * <p>To provide isolation, the target invoked to process the module is executed in its own
 * Ant {@link Project project}. However, there is a way to pass data from the parent Ant project to
 * the module-specific project. The attributes {@link #setInheritAll(boolean) inheritAll},
 * {@link #setInheritRefs(boolean) inheritRefs}, and the nested elements {@link #createParam()
 * &lt;createParam&gt;}, {@link #addPropertyset(PropertySet) &lt;propertyset&gt;},
 * {@link #addReference(org.apache.tools.ant.taskdefs.Ant.Reference) &lt;reference&gt;} can be used
 * for this. If the attribute {@link #setModuleProperty(String) moduleProperty} is defined
 * then a {@link Module} instance that contains module metadata is passed to the module-specific
 * project with the given property. In addition, all user-defined properties (i.e. the properties
 * defined by the command line) are passed to each module-specific project. Note that there is no
 * explicit way to pass data between module-specific Ant projects.</p>
 * 
 * <p>Module metadata are loaded by a {@link ModuleLoader} specified by the nested element
 * whose type is a descendant of {@code ModuleLoader}. One and only one such element must
 * be specified.</p>
 * 
 * <p>For the sake of performance, modules could be processed in parallel. That is, independent
 * modules could be processed simultaneously, each within its own thread. There is no way to
 * perform parallel processing of modules that are dependent, directly or indirectly, one upon
 * another. The number of threads to be used is defined by the attribute
 * {@link #setThreadCount(int) threadCount}.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
// TODO document task input and usage example.
public class CallTargetForModules extends Task
{
    private ArrayList<ModuleElement> moduleElements = new ArrayList<ModuleElement>();
    private ModuleLoader moduleLoader;
    // If defined then the correspondent Module object is set to this property for each module being processed.
    private String moduleProperty;
    
    private String target;
    private final ArrayList<ParamElement> params = new ArrayList<ParamElement>();
    private final ArrayList<Ant.Reference> references = new ArrayList<Ant.Reference>();
    private final PropertySet propertySet = new PropertySet();
    
    // Default values match antcall's defaults.
    private boolean inheritAll = true;
    private boolean inheritRefs = false;
    
    // The number of threads used to build modules.
    private int threadCount = 1;
    
    /**
     * <p>Executes this {@code <callTargetForModules>} task. See the
     * {@link CallTargetForModules class description} for the details.</p>
     * 
     * @throws BuildException if this task is configured incorrectly or
     *      if the build of some module involved fails.
     */
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
    
    /**
     * <p>Sets the flag whether or not the properties of the current Ant {@link Project project}
     * are to be passed to the Ant projects created to process {@link Module modules}.
     * If {@code true} is set then all the properties from the current project are passed to
     * each module-specific Ant project. If {@code false} is set then only the user properties
     * (i.e. the properties defined in the command line) and the properties defined by the elements
     * {@link #createParam() &lt;param&gt;} and {@link #addPropertyset(PropertySet) &lt;propertyset&gt;}
     * are passed. {@code true} is the default value.</p>
     * 
     * <p>In either case the properties defined within the module-specific project are overridden
     * by the correspondent properties passed from the current project.</p>
     * 
     * @param inheritAll the flag value to be set.
     */
    public void setInheritAll(final boolean inheritAll)
    {
        this.inheritAll = inheritAll;
    }
    
    /**
     * <p>Sets the flag whether or not the references of the current Ant {@link Project project}
     * are to be passed to the Ant projects created to process {@link Module modules}. If
     * {@code true} is set then all the references are passed to each module-specific Ant project.
     * If {@code false} is set then the references are not passed. {@code false} is the default
     * value.</p>
     * 
     * <p>The references defined in the module-specific Ant project are not overridden by
     * the current project's references even if <em>inheritRefs</em> is set to {@code true}.
     * Use the {@link #addReference(Ant.Reference) &lt;reference&gt;} nested element for this.</p>
     * 
     * @param inheritRefs the flag value to be set.
     */
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
    
    /**
     * <p>Adds a new {@link org.apache.tools.ant.taskdefs.Ant.Reference org.apache.tools.ant.taskdefs.Ant.Reference}
     * container that backs the nested element {@code <reference>} of this
     * {@code <callTargetForModules>} task. Multiple nested {@code <reference>} elements are
     * allowed.</p>
     * 
     * <p>This element defines a reference to be inherited by the Ant {@link Project projects}
     * created to process {@link Module modules}. If there is a reference with the ID requested
     * defined in the module-specific Ant project then it is overridden by this reference.
     * However, a reference with the ID requested defined within a target is not overridden.</p>
     * 
     * <p>Use the task attribute {@link #setInheritRefs(boolean) inheritRefs} set to {@code true}
     * to pass all references defined within the Ant project of this {@code <callTargetForModules>}
     * to the module-specific projects. Note that no references are overridden in this case except
     * those that are specified by the {@code <reference>} elements.</p>
     * 
     * @param reference the {@code <reference>} element to be added. It must be not {@code null}.
     */
    public void addReference(final Ant.Reference reference)
    {
        references.add(reference);
    }
    
    /**
     * <p>Adds a new {@link PropertySet org.apache.tools.ant.types.PropertySet}
     * container that backs the nested element {@code <propertyset>} of this
     * {@code <callTargetForModules>} task. Multiple nested {@code <propertyset>} elements are
     * allowed.</p>
     * 
     * <p>This element defines a set of Ant project properties that are to be passed to the Ant
     * {@link Project projects} created to process {@link Module modules}. Each of these properties
     * overrides the property with the same name defined in a module-specific project. However,
     * these properties are overridden by the user-defined properties (i.e. those passed with
     * the command line) and {@link #createParam() params}.</p>
     * 
     * <p>Use the task attribute {@link #setInheritAll(boolean) inheritAll} set to {@code true}
     * to pass all properties defined within the current Ant project to the module-specific
     * projects.</p>
     * 
     * @param propertySet the {@code <propertyset>} element to be added. It must be
     *      not {@code null}.
     */
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
    
    /**
     * <p>Serves as the nested element {@code <param>} of the task
     * {@link CallTargetForModules &lt;callTargetForModules&gt;} and defines the parameters to be
     * passed to the module-specific Ant projects. Refer to
     * {@link CallTargetForModules#createParam()} for more details.</p>
     * 
     * <h3>Attributes</h3>
     * <p>The attributes and nested elements defined by {@code ParamElement} have the same
     * semantics as the correspondent attributes and nested elements of the task
     * {@link org.apache.tools.ant.taskdefs.Property &lt;property&gt;} have.</p>
     * 
     * @see Property org.apache.tools.ant.taskdefs.Property
     */
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
        private Ant.Reference classpathRef;
        private String environment;
        private Ant.Reference reference;
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
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setName(String)} have.</p>
         * 
         * @param name the value to be set.
         */
        public void setName(final String name)
        {
            this.name = name;
            nameSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setValue(String)} have.</p>
         * 
         * @param value the value to be set.
         */
        public void setValue(final String value)
        {
            this.value = value;
            valueSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setLocation(File)} have.</p>
         * 
         * @param location the value to be set.
         */
        public void setLocation(final File location)
        {
            this.location = location;
            locationSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setFile(File)} have.</p>
         * 
         * @param file the value to be set.
         */
        public void setFile(final File file)
        {
            this.file = file;
            fileSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setUrl(URL)} have.</p>
         * 
         * @param url the value to be set.
         */
        public void setUrl(final URL url)
        {
            this.url = url;
            urlSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setResource(String)} have.</p>
         * 
         * @param resource the value to be set.
         */
        public void setResource(final String resource)
        {
            this.resource = resource;
            resourceSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setClasspath(Path)} have.</p>
         * 
         * @param classpath the value to be set.
         */
        public void setClasspath(final Path classpath)
        {
            classpathAttribute = classpath;
            classpathAttributeSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#createClasspath()} have.</p>
         * 
         * @return the element created. It is never {@code null}.
         */
        public Path createClasspath()
        {
            if (classpath == null) {
                return classpath = new Path(getProject());
            } else {
                return classpath.createPath();
            }
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setClasspathRef(
         * org.apache.tools.ant.types.Reference)} have.</p>
         * 
         * @param reference the value to be set.
         */
        public void setClasspathRef(final Ant.Reference reference)
        {
            classpathRef = reference;
            classpathRefSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setEnvironment(String)} have.</p>
         * 
         * @param environment the value to be set.
         */
        public void setEnvironment(final String environment)
        {
            this.environment = environment;
            environmentSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setRefid(
         * org.apache.tools.ant.types.Reference)} have.</p>
         * 
         * @param reference the value to be set.
         */
        public void setRefid(final Ant.Reference reference)
        {
            this.reference = reference;
            referenceSet = true;
        }
        
        /**
         * <p>Has the same semantics as
         * {@link org.apache.tools.ant.taskdefs.Property#setPrefix(String)} have.</p>
         * 
         * @param prefix the value to be set.
         */
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
