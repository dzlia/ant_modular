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
 * <p>Indicates that a module meta information is not loaded by a {@link ModuleLoader}
 * implementation. Optionally, it provides an explanatory message and/or
 * a cause exception.</p>
 * 
 * <p>Since the process of module resolution is recursive it is possible that
 * the caller sees {@code ModuleNotLoadedException} that is related to a module
 * is different from the module that was requested. This is possible for clients of
 * {@link ModuleRegistry}.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 * 
 * @see ModuleLoader
 * @see ModuleRegistry
 */
public class ModuleNotLoadedException extends Exception
{
    /**
     * <p>Creates an instance of {@code ModuleNotLoadedException} with undefined
     * explanatory message and cause exception.</p>
     */
    public ModuleNotLoadedException()
    {
        this(null, null);
    }
    
    /**
     * <p>Creates an instance of {@code ModuleNotLoadedException} with the given
     * explanatory message and undefined cause exception.</p>
     * 
     * @param message the explanatory message. It is recommended that the message
     *      contains information about the path of the module that is not loaded.
     *      {@code null} is allowed and indicates an undefined message.
     */
    public ModuleNotLoadedException(final String message)
    {
        this(message, null);
    }
    
    /**
     * <p>Creates an instance of {@code ModuleNotLoadedException} with the given
     * explanatory message and cause exception. It is recommended that the message
     * contains information about the path of the module that is not loaded.</p>
     * 
     * @param message the explanatory message. It is recommended that the message
     *      contains information about the path of the module that is not loaded.
     *      {@code null} is allowed and indicates an undefined message.
     * @param cause the cause exception. {@code null} is allowed and indicates
     *      no cause exception.
     */
    public ModuleNotLoadedException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
