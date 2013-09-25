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

/**
 * <p>An interface of an Ant type that loads meta information about a module.
 * This information is stored in {@link afc.ant.modular.ModuleInfo}
 * objects as attributes in the form of {@code key->value}.
 * An implementation of this interface is free to choose any representation of
 * module meta information, except the following:</p>
 * <ul>
 *  <li>the module path is stored in the
 *      {@link afc.ant.modular.ModuleInfo#getPath() path} property</li>
 *  <li>the module dependee modules are stored as their paths in the
 *      {@link afc.ant.modular.ModuleInfo#getDependencies() dependencies}
 *      property</li>
 *  <li>for the sake of inter-operability, it is recommended (though not required)
 *      that the module classpath attributes are stored as
 *      {@link org.apache.tools.ant.types.Path} objects</li>
 * </ul>
 * <p>The dependee modules should not be resolved recursively.</p>
 * 
 * <p>Additional notes:<p>
 * <ul>
 *  <li>{@code ModuleLoader} instance are used in the single threaded execution model</li>
 *  <li>no caching of meta information is generally needed. Each module is loaded
 *      only once</li>
 *  <li>implementations of this interface are used by the task
 *      {@link afc.ant.modular.CallTargetForModules} as pluggable components
 *      to define a way in which module meta information is to be loaded</li>
 * </ul>
 *
 * @author D&#378;mitry La&#365;&#269;uk
 */
public interface ModuleLoader
{
    /**
     * <p>Loads meta information of the module with the given path. If the module information
     * cannot be loaded then an exception {@link afc.ant.modular.ModuleNotLoadedException}
     * is thrown.</p>
     * 
     * @param path the module path. It is a path relative to the directory that is the root
     *      for all modules (generally an Ant project base directory).
     *      It must not be {@code null}.
     * 
     * @return a {@link afc.ant.modular.ModuleInfo} object that is initialised
     *      with the module path, dependencies and attributes. It is never {@code null}.
     * 
     * @throws ModuleNotLoadedException if the module meta information cannot be loaded.
     */
    ModuleInfo loadModule(String path) throws ModuleNotLoadedException;
}
