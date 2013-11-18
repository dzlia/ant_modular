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
import java.util.HashMap;

/**
 * <p>Manages {@link Module} instances in scope of a single {@link CallTargetForModules}
 * task. In particular, it allows the client to get a {@code Module} instance that
 * corresponds to a given module path, and this instance is the same for all such invocations
 * (see {@link #resolveModule(String)}). Metadata for each {@code Module} is loaded by a
 * {@link ModuleLoader} with which the {@code ModuleRegistry} is created. Module's dependee
 * modules are loaded by the {@code ModuleRegistry} itself.</p>
 * 
 * <p>Guarantees provided by {@code ModuleRegistry}:</p>
 * <ul>
 *  <li>{@code ModuleRegistry} is not thread-safe</li>
 *  <li>for each module path (after it is normalised) {@link ModuleLoader#loadModule(String)}
 *      is invoked at most once</li>
 *  <li>each module path is associated with at most a single {@code Module} instance</li>
 *  <li>the module's dependee modules are assigned to this {@code Module} instance before
 *      it is returned outside {@code ModuleRegistry}</li>
 * </ul>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleRegistry
{
    private static final Object moduleNotLoaded = new Object();
    
    private final HashMap<String, Object> modules; // values are either ModuleInfo instances of 'moduleNotLoaded'
    private final ModuleLoader moduleLoader;
    
    /**
     * <p>Creates an instance of {@code ModuleRegistry} that uses given {@link ModuleLoader}
     * to obtain module metadata.</p>
     * 
     * @param moduleLoader the {@code ModuleLoader} to be used by the {@code ModuleRegistry}
     *      created. It must not be {@code null}.
     * 
     * @throws NullPointerException if <em>moduleLoader</em> is {@code null}.
     */
    public ModuleRegistry(final ModuleLoader moduleLoader)
    {
        if (moduleLoader == null) {
            throw new NullPointerException("moduleLoader");
        }
        this.moduleLoader = moduleLoader;
        this.modules = new HashMap<String, Object>();
    }
    
    /**
     * <p>Returns a {@link Module} that is associated with a given module path, or throws
     * {@link ModuleNotLoadedException} if no module is associated with this path or if the
     * module metadata cannot be loaded. If {@code resolveModule(String)} is invoked with paths
     * that correspond to the same module then the same {@code Module} instance is returned for
     * these invocations. The path that is assigned to the {@code Module} returned is a
     * normalised path, which could be different from the given path.</p>
     * 
     * <p>The {@code ModuleLoader} this {@code ModuleRegistry} was created with is used to
     * load module metadata. The metadata (in form of {@link ModuleInfo}) loaded is converted
     * to a {@code Module} object. In particular, the dependency paths defined in the
     * {@code ModuleInfo} are converted into correspondent {@code Module} instances by
     * resolving them with this {@code ModuleRegistry}, and the attributes are copied with
     * no transformation.</p>
     * 
     * <p>For each given normalised path {@link ModuleLoader#loadModule(String)} is invoked
     * at most once.</p>
     * 
     * @param path the path of the module to be resolved. It must not be {@code null}.
     *      The module path is normalised, so the path of the module returned could
     *      differ from this path.
     * 
     * @return the {@code Module} object that corresponds to the given path.
     *      It is never {@code null}.
     * 
     * @throws NullPointerException if <em>path</em> is {@code null}.
     * @throws ModuleNotLoadedException if there is no module associated with the given path
     *      or if the module metadata cannot be loaded.
     */
    public Module resolveModule(final String path) throws ModuleNotLoadedException
    {
        if (path == null) {
            throw new NullPointerException("path");
        }
        
        final String normalisedPath = ModuleInfo.normalisePath(path);
        final Object cachedModule = modules.get(normalisedPath);
        if (cachedModule == moduleNotLoaded) {
            throw new ModuleNotLoadedException(normalisedPath);
        }
        if (cachedModule != null) {
            return (Module) cachedModule;
        }
        try {
            final ModuleInfo moduleInfo = moduleLoader.loadModule(normalisedPath);
            if (moduleInfo == null) {
                throw new NullPointerException(MessageFormat.format(
                        "Module loader returned null for the path ''{0}''.", normalisedPath));
            }
            final Module module = new Module(normalisedPath);
            module.setAttributes(moduleInfo.getAttributes());
            /* The module under construction is put into the registry to prevent infinite
               module loading in case of cyclic dependencies, which causes stack overflow. */
            modules.put(normalisedPath, module);
            for (final String depPath : moduleInfo.getDependencies()) {
                module.addDependency(resolveModule(depPath));
            }
            return module;
        }
        catch (ModuleNotLoadedException ex) {
            modules.put(normalisedPath, moduleNotLoaded);
            throw ex;
        }
    }
}
