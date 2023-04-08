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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;

public class MockBuildListener implements BuildListener
{
    private final Task task;
    private final Throwable exceptionToThrow;
    
    public MockBuildListener(final Task task, final Throwable exceptionToThrow)
    {
        this.task = task;
        this.exceptionToThrow = exceptionToThrow;
    }
    
    public void buildFinished(final BuildEvent event)
    {
    }

    public void buildStarted(final BuildEvent event)
    {
    }

    public void messageLogged(final BuildEvent event)
    {
    }

    public void targetFinished(final BuildEvent event)
    {
    }

    public void targetStarted(final BuildEvent event)
    {
    }

    public void taskFinished(final BuildEvent event)
    {
    }

    public void taskStarted(final BuildEvent event)
    {
        if (event.getTask() != task) {
            return;
        }
        if (exceptionToThrow instanceof RuntimeException) {
            throw (RuntimeException) exceptionToThrow;
        }
        if (exceptionToThrow instanceof Error) {
            throw (Error) exceptionToThrow;
        }
    }
}
