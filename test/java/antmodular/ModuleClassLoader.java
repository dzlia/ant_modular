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
package antmodular;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import antmodular.Module;

/**
 * <p>Loads custom (potentially invalid) implementations of {@link Module}.<p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleClassLoader extends ClassLoader
{
    /**
     * <p>Creates a class loader with a custom (potentially invalid) implementation of
     * {@link Module} defined.</p>
     * 
     * @param pathToModuleClass the path to the Java class file that holds the desired
     *      implementation of {@code antmodular.Module}.
     * 
     * @throws IOException if the class file cannot be read.
     */
    public ModuleClassLoader(final String pathToModuleClass) throws IOException
    {
        /* Defining Module and its nested classes explicitly so that
         * this class loader does not ask the parent class loader to load them,
         * and a brand new classes are associated with this class loader.
         */ 
        final FileInputStream in = new FileInputStream(pathToModuleClass);
        
        try {
            final BufferedInputStream bufferedIn = new BufferedInputStream(in);
            final ByteArrayOutputStream buf = new ByteArrayOutputStream();
            
            int b;
            while ((b = bufferedIn.read()) >= 0) {
                buf.write(b);
            }
            
            defineClass(Module.class.getName(), buf.toByteArray(), 0, buf.size());
        }
        finally {
            in.close();
        }
    }
}