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

import java.util.Arrays;

import junit.framework.TestCase;

public class SerialDependencyResolver_InvalidUseCasesTest extends TestCase
{
    private SerialDependencyResolver resolver;
    
    @Override
    protected void setUp()
    {
        resolver = new SerialDependencyResolver();
    }
    
    public void testNullModuleList() throws Exception
    {
        try {
            resolver.init(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules", ex.getMessage());
        }
    }
    
    public void testNullModuleInTheRootModuleList() throws Exception
    {
        try {
            resolver.init(Arrays.asList(new ModuleInfo("foo"), null, new ModuleInfo("bar")));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("rootModules contains null element.", ex.getMessage());
        }
    }
}
