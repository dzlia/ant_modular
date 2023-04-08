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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import antmodular.CyclicDependenciesDetectedException;
import antmodular.Module;

import junit.framework.TestCase;

public class CyclicDependenciesDetectedExceptionTest extends TestCase
{
    public void testNullLoop()
    {
        try {
            new CyclicDependenciesDetectedException(null);
            fail();
        }
        catch (NullPointerException ex) {
            // expected
        }
    }
    
    public void testEmptyLoop()
    {
        final ArrayList<Module> loop = new ArrayList<Module>(0);
        final CyclicDependenciesDetectedException ex = new CyclicDependenciesDetectedException(loop);
        assertSame(loop, ex.getLoop());
        assertEquals(Collections.emptyList(), ex.getLoop());
        assertEquals("Cyclic dependencies detected: [].", ex.getMessage());
    }
    
    public void testSingleModuleInLoop()
    {
        final Module m1 = new Module("foo");
        final ArrayList<Module> loop = new ArrayList<Module>(0);
        loop.add(m1);
        final CyclicDependenciesDetectedException ex = new CyclicDependenciesDetectedException(loop);
        assertSame(loop, ex.getLoop());
        assertEquals(Collections.singletonList(m1), ex.getLoop());
        assertEquals("Cyclic dependencies detected: [->foo->].", ex.getMessage());
    }
    
    public void testTwoModulesInLoop()
    {
        final Module m1 = new Module("foo");
        final Module m2 = new Module("bar");
        final ArrayList<Module> loop = new ArrayList<Module>(0);
        loop.add(m1);
        loop.add(m2);
        final CyclicDependenciesDetectedException ex = new CyclicDependenciesDetectedException(loop);
        assertSame(loop, ex.getLoop());
        assertEquals(Arrays.asList(m1, m2), ex.getLoop());
        assertEquals("Cyclic dependencies detected: [->foo->bar->].", ex.getMessage());
    }
    
    public void testThreeModulesInLoop()
    {
        final Module m1 = new Module("foo");
        final Module m2 = new Module("bar");
        final Module m3 = new Module("baz");
        final ArrayList<Module> loop = new ArrayList<Module>(0);
        loop.add(m1);
        loop.add(m2);
        loop.add(m3);
        final CyclicDependenciesDetectedException ex = new CyclicDependenciesDetectedException(loop);
        assertSame(loop, ex.getLoop());
        assertEquals(Arrays.asList(m1, m2, m3), ex.getLoop());
        assertEquals("Cyclic dependencies detected: [->foo->bar->baz->].", ex.getMessage());
    }
    
    public void testNullModuleInLoop()
    {
        final Module m1 = new Module("foo");
        final Module m2 = null;
        final Module m3 = new Module("baz");
        final ArrayList<Module> loop = new ArrayList<Module>(0);
        loop.add(m1);
        loop.add(m2);
        loop.add(m3);
        try {
            new CyclicDependenciesDetectedException(loop);
            fail();
        }
        catch (NullPointerException ex) {
            // expected
        }
    }
}
