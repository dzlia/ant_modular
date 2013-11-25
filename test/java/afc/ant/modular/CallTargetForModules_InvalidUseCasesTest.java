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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;

import junit.framework.TestCase;

public class CallTargetForModules_InvalidUseCasesTest extends TestCase
{
    private CallTargetForModules task;
    private MockProject project;
    private MockModuleLoader moduleLoader;
    
    @Override
    protected void setUp()
    {
        project = new MockProject();
        task = new CallTargetForModules();
        task.setProject(project);
        moduleLoader = new MockModuleLoader();
    }
    
    @Override
    protected void tearDown()
    {
        moduleLoader = null;
        task = null;
        project = null;
    }
    
    public void testMissingTarget()
    {
        task.init();
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("The attribute 'target' is undefined.", ex.getMessage());
        }
    }
    
    public void testMissingModuleLoader()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("No module loader is defined.", ex.getMessage());
        }
    }
    
    public void testAttemptToConfigureMoreThanOneModuleLoader()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.addConfigured(new MockModuleLoader());
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Only a single module loader element is allowed.", ex.getMessage());
        }
    }
    
    public void testModuleElementWithNoPath_SingleModule()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule();
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("There is a <module> element with the attribute 'path' undefined.", ex.getMessage());
        }
    }
    
    public void testModuleElementWithNoPath_MultipleModules()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.createModule();
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("There is a <module> element with the attribute 'path' undefined.", ex.getMessage());
        }
    }
    
    public void testNoModuleElements()
    {
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("At least one <module> element is required.", ex.getMessage());
        }
    }
    
    public void testTwoRootModules_CyclicDependency()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("foo/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertTrue(ex.getCause() instanceof CyclicDependenciesDetectedException);
            assertEquals(ex.getMessage(), ex.getCause().getMessage());
        }
    }
    
    public void testRootModuleWithDependency_CyclicDependency_TaskLocationDefined()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("foo/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Location location = new Location("some_file");
        task.setLocation(location);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertTrue(ex.getCause() instanceof CyclicDependenciesDetectedException);
            assertEquals(ex.getMessage(), ex.getCause().getMessage());
            assertSame(location, ex.getLocation());
        }
    }
    
    public void testRootModuleWithDependency_CyclicDependency_TaskLocationUndefined()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("foo/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertTrue(ex.getCause() instanceof CyclicDependenciesDetectedException);
            assertEquals(ex.getMessage(), ex.getCause().getMessage());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
    }
    
    public void testMissingModule_TaskLocationDefined()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("baz/"); // this dependency will not be resolved
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        final ModuleNotLoadedException exception = new ModuleNotLoadedException("test_msg");
        moduleLoader.modules.put("baz/", exception);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Location location = new Location("some_file");
        task.setLocation(location);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertSame(exception, ex.getCause());
            assertEquals(ex.getMessage(), "test_msg");
            assertSame(location, ex.getLocation());
        }
    }
    
    public void testMissingModule_TaskLocationUnefined()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleInfo2.addDependency("baz/"); // this dependency will not be resolved
        moduleLoader.modules.put("foo/", moduleInfo1);
        moduleLoader.modules.put("bar/", moduleInfo2);
        
        final ModuleNotLoadedException exception = new ModuleNotLoadedException("test_msg");
        moduleLoader.modules.put("baz/", exception);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertSame(exception, ex.getCause());
            assertEquals(ex.getMessage(), "test_msg");
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
    }
    
    public void testThreadCountIsInvalid_ZeroValue()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.setThreadCount(0);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Invalid thread count: '0'. It must be a positive value.", ex.getMessage());
        }
    }
    
    public void testThreadCountIsInvalid_NegativeValue()
    {
        final ModuleInfo moduleInfo1 = new ModuleInfo("foo/", moduleLoader);
        moduleInfo1.addDependency("bar/");
        moduleLoader.modules.put("foo/", moduleInfo1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        try {
            task.setThreadCount(-1);
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Invalid thread count: '-1'. It must be a positive value.", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that {@code NullPointerException} is thrown if {@code null} module loader is passed
     * to {@code CallTargetForModules}.</p>
     */
    public void testNullModuleLoaderIsPassed()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        
        try {
            task.addConfigured(null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("moduleLoader", ex.getMessage());
        }
    }
}
