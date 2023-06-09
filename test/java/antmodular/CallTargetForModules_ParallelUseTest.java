/* Copyright (c) 2013-2016, Dźmitry Laŭčuk
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
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;

import antmodular.CallTargetForModules;
import antmodular.ModuleInfo;
import antmodular.CallTargetForModules.ModuleElement;
import antmodular.CallTargetForModules.ParamElement;

import junit.framework.TestCase;

// TODO add high load tests
public class CallTargetForModules_ParallelUseTest extends TestCase
{
    private CallTargetForModules task;
    private MockProject project;
    private MockModuleLoader moduleLoader;
    
    @Override
    protected void setUp()
    {
        project = new MockProject();
        project.setProperty(MagicNames.ANT_FILE, "test_ant_file");
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
    
    public void testParallelRun_TwoThreads_MultipleModulesWithDeps_RelatedHierarchies_ModulePropertyDefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("baz");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "mProp", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "mProp", moduleInfo2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false, "mProp", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "mProp", moduleInfo,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testParallelRun_TwoThreads_MultipleModulesWithDeps_RelatedHierarchies_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.createModule().setPath("foo");
        task.createModule().setPath("baz");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testParallelRun_TwoThreads_MultipleModulesWithDeps_UnrelatedHierarchies_ModulePropertyDefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        /* Verifying that module-specific targets to not see the changes of each other.
           Support of MockCallTargetTask is needed for correct emulation. */
        task1.propertiesToSet = Collections.<String, Object>singletonMap("task1", "prop1");
        task2.propertiesToSet = Collections.<String, Object>singletonMap("task2", "prop2");
        task3.propertiesToSet = Collections.<String, Object>singletonMap("task3", "prop3");
        task4.propertiesToSet = Collections.<String, Object>singletonMap("task4", "prop4");
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("baz");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        final Map<String, ModuleInfo> moduleInfos = TestUtil.<String, ModuleInfo>map(
                moduleInfo.getPath(), moduleInfo,
                moduleInfo2.getPath(), moduleInfo2,
                dep1.getPath(), dep1,
                dep2.getPath(), dep2);
        final ArrayList<String> modulePaths = new ArrayList<String>();
        modulePaths.add(TestUtil.getModulePath(task1.ownProject, "mProp"));
        modulePaths.add(TestUtil.getModulePath(task2.ownProject, "mProp"));
        modulePaths.add(TestUtil.getModulePath(task3.ownProject, "mProp"));
        modulePaths.add(TestUtil.getModulePath(task4.ownProject, "mProp"));
        
        assertTrue(modulePaths.contains("foo/"));
        assertTrue(modulePaths.contains("bar/"));
        assertTrue(modulePaths.contains("baz/"));
        assertTrue(modulePaths.contains("quux/"));
        assertTrue(modulePaths.indexOf("foo/") > modulePaths.indexOf("bar/"));
        assertTrue(modulePaths.indexOf("baz/") > modulePaths.indexOf("quux/"));
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "mProp",
                moduleInfos.get(modulePaths.get(0)),
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task1", "prop1"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "mProp",
                moduleInfos.get(modulePaths.get(1)),
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task2", "prop2"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false, "mProp",
                moduleInfos.get(modulePaths.get(2)),
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task3", "prop3"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "mProp",
                moduleInfos.get(modulePaths.get(3)),
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task4", "prop4"));
    }
    
    public void testParallelRun_TwoThreads_MultipleModulesWithDeps_UnrelatedHierarchies_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        /* Verifying that module-specific targets to not see the changes of each other.
           Support of MockCallTargetTask is needed for correct emulation. */
        task1.propertiesToSet = Collections.<String, Object>singletonMap("task1", "prop1");
        task2.propertiesToSet = Collections.<String, Object>singletonMap("task2", "prop2");
        task3.propertiesToSet = Collections.<String, Object>singletonMap("task3", "prop3");
        task4.propertiesToSet = Collections.<String, Object>singletonMap("task4", "prop4");
        
        task.init();
        task.setTarget("someTarget");
        task.createModule().setPath("foo");
        task.createModule().setPath("baz");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        /* Since no module is passed to the targets there is not way to determine
           what targets were called for what modules. */
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task1", "prop1"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task2", "prop2"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task3", "prop3"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o", "task4", "prop4"));
    }
    
    public void testParallelRun_ModuleWithDeps_BuildFailure()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        final Location location = new Location("some_file", 10, 20);
        final BuildException exception = new BuildException("test_failure_msg", location);
        task2.exception = exception;
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Module 'bar/': test_failure_msg", ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(location, ex.getLocation());
            assertTrue(Arrays.equals(exception.getStackTrace(), ex.getStackTrace()));
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "moduleRef", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task3.executed);
    }
    
    /**
     * <p>Tests that a misconfiguration of or a bug in a module task that leads to
     * {@link BuildException} thrown with {@code null} location leads to
     * {@link Location#UNKNOWN_LOCATION} reported to the caller.</p>
     */
    public void testParallelRun_ModuleWithDeps_BuildFailure_BuildExceptionHasNullLocation()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        final BuildException exception = new BuildException("test_failure_msg", (Location) null);
        task2.exception = exception;
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Module 'bar/': test_failure_msg", ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
            assertTrue(Arrays.equals(exception.getStackTrace(), ex.getStackTrace()));
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "moduleRef", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task3.executed);
    }
    
    public void testParallelRun_ModuleWithDeps_RuntimeExceptionInATarget()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        final RuntimeException exception = new RuntimeException("test_failure_msg");
        task2.exception = exception;
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Module 'bar/': " + ex.getCause().getMessage(), ex.getMessage());
            assertNotNull(ex.getLocation());
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "moduleRef", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task3.executed);
    }
    
    public void testParallelRun_MultipleModulesWithDeps_CustomTarget_ModulePropertyDefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        final ModuleElement moduleElem = task.createModule();
        moduleElem.setPath("baz");
        moduleElem.setTarget("customTarget");
        final ModuleElement moduleElem2 = task.createModule();
        moduleElem2.setPath("bar");
        moduleElem2.setTarget("customTarget2");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "mProp", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "customTarget", true, false, "mProp", moduleInfo2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "customTarget2", true, false, "mProp", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "mProp", moduleInfo,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testParallelRun_MultipleModulesWithDeps_CustomTarget_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.createModule().setPath("foo");
        final ModuleElement moduleElem = task.createModule();
        moduleElem.setPath("baz");
        moduleElem.setTarget("customTarget");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "customTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testParallelRun_MultipleModulesWithDeps_RepeatedModuleElement_UnambiguousTarget()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        final ModuleElement moduleElem = task.createModule();
        moduleElem.setPath("baz");
        moduleElem.setTarget("customTarget");
        final ModuleElement moduleElem2 = task.createModule();
        moduleElem2.setPath("baz");
        moduleElem2.setTarget("customTarget");
        task.createModule().setPath("foo/");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "mProp", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "customTarget", true, false, "mProp", moduleInfo2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false, "mProp", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "mProp", moduleInfo,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testParallelRun_MultipleModulesWithDeps_RepeatedModuleElement_AmbiguousNonDefaultTargets()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        final ModuleElement moduleElem = task.createModule();
        moduleElem.setPath("baz");
        moduleElem.setTarget("customTarget");
        final ModuleElement moduleElem2 = task.createModule();
        moduleElem2.setPath("baz");
        moduleElem2.setTarget("anotherCustomTarget");
        task.createModule().setPath("foo/");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Ambiguous choice of the target to be invoked for the module 'baz/'. " +
                    "At least the targets 'customTarget' and 'anotherCustomTarget' are configured.", ex.getMessage());
        }
        
        assertFalse(task1.executed);
        assertFalse(task2.executed);
        assertFalse(task3.executed);
        assertFalse(task4.executed);
    }
    
    public void testParallelRun_MultipleModulesWithDeps_RepeatedModuleElement_AmbiguousTargets_WithDefaultTarget()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/", moduleLoader);
        dep2.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", moduleInfo2);
        moduleLoader.modules.put("quux/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        final ModuleElement moduleElem = task.createModule();
        moduleElem.setPath("baz");
        moduleElem.setTarget("customTarget");
        task.createModule().setPath("baz/");
        task.createModule().setPath("foo/");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertEquals("Ambiguous choice of the target to be invoked for the module 'baz/'. " +
                    "At least the targets 'customTarget' and 'someTarget' are configured.", ex.getMessage());
        }
        
        assertFalse(task1.executed);
        assertFalse(task2.executed);
        assertFalse(task3.executed);
        assertFalse(task4.executed);
    }
    
    /**
     * <p>Tests that:</p>
     * <ul>
     *  <li>the build thread is interruptible if a helper thread does not respond</li>
     *  <li>all the helper threads are interrupted if the build thread is interrupted</li>
     * </ul>
     */
    public void testParallelRun_MultipleModules_BuildThreadIsInterrupted() throws Throwable
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        final ModuleInfo moduleInfo3 = new ModuleInfo("baz/", moduleLoader);
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", moduleInfo2);
        moduleLoader.modules.put("baz/", moduleInfo3);
        
        final CyclicBarrier hangBarrier = new CyclicBarrier(4);
        
        final AtomicReference<Throwable> failureCause = new AtomicReference<Throwable>();
        
        final Thread buildThread = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    try {
                        task.perform();
                        fail();
                    }
                    catch (BuildException ex) {
                        // expected
                        assertEquals("The build thread was interrupted.", ex.getMessage());
                        assertNull(ex.getCause());
                    }
                    
                    assertTrue(Thread.currentThread().isInterrupted());
                }
                catch (Throwable ex) {
                    failureCause.set(ex);
                }
            }
        };
        
        final HangingMockCallTargetTask task1 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task1);
        final HangingMockCallTargetTask task2 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task2);
        final HangingMockCallTargetTask task3 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task3);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.createModule().setPath("baz");
        task.addConfigured(moduleLoader);
        task.setThreadCount(3);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        buildThread.start();
        
        hangBarrier.await();
        
        assertNotNull(task1.hangingThread);
        assertNotNull(task2.hangingThread);
        assertNotNull(task3.hangingThread);
        final Map<Thread, HangingMockCallTargetTask> threadToTaskMap = TestUtil.map(
                task1.hangingThread, task1, task2.hangingThread, task2, task3.hangingThread, task3);
        assertEquals(3, threadToTaskMap.size());
        
        final HangingMockCallTargetTask buildThreadTask = threadToTaskMap.remove(buildThread);
        assertNotNull(buildThreadTask);
        
        buildThreadTask.hang = false;
        synchronized (buildThreadTask) {
            buildThreadTask.notify();
        }
        
        buildThread.interrupt();
        
        buildThread.join();
        
        for (final Map.Entry<Thread, HangingMockCallTargetTask> entry : threadToTaskMap.entrySet()) {
            entry.getKey().join(); // reasonable timeout
            
            assertTrue(entry.getValue().interrupted);
        }
        
        if (failureCause.get() != null) {
            throw failureCause.get();
        }
        
        // the other checks are performed thoroughly in other tests.
        assertTrue(task1.executed);
        assertTrue(task2.executed);
        assertTrue(task3.executed);
    }
    
    /**
     * <p>Tests that:</p>
     * <ul>
     *  <li>the build thread is interruptible while it processes modules</li>
     *  <li>all the helper threads are interrupted if the build thread is interrupted</li>
     * </ul>
     */
    public void testParallelRun_MultipleModules_BuildThreadIsInterruptedWhileProcessingNotBlocked() throws Throwable
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        final ModuleInfo moduleInfo2 = new ModuleInfo("bar/", moduleLoader);
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        final ModuleInfo moduleInfo3 = new ModuleInfo("baz/", moduleLoader);
        final ModuleInfo moduleInfo4 = new ModuleInfo("quux/", moduleLoader);
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", moduleInfo2);
        moduleLoader.modules.put("baz/", moduleInfo3);
        moduleLoader.modules.put("quux/", moduleInfo4);
        
        final CyclicBarrier hangBarrier = new CyclicBarrier(4);
        
        final AtomicReference<Throwable> failureCause = new AtomicReference<Throwable>();
        
        final Thread buildThread = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    try {
                        task.perform();
                        fail();
                    }
                    catch (BuildException ex) {
                        // expected
                        assertEquals("The build thread was interrupted.", ex.getMessage());
                        assertNull(ex.getCause());
                    }
                    
                    assertTrue(Thread.currentThread().isInterrupted());
                }
                catch (Throwable ex) {
                    failureCause.set(ex);
                }
            }
        };
        
        final HangingMockCallTargetTask task1 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task1);
        final HangingMockCallTargetTask task2 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task2);
        final HangingMockCallTargetTask task3 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task3);
        final HangingMockCallTargetTask task4 = new HangingMockCallTargetTask(project, hangBarrier, failureCause);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("mProp");
        task.createModule().setPath("foo");
        task.createModule().setPath("bar");
        task.createModule().setPath("baz");
        task.createModule().setPath("quux");
        task.addConfigured(moduleLoader);
        task.setThreadCount(3);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        buildThread.start();
        
        hangBarrier.await();
        
        // One of the tasks is not started and its hanging thread is undefined.
        final Map<Thread, HangingMockCallTargetTask> threadToTaskMap = TestUtil.map(
                task1.hangingThread, task1, task2.hangingThread, task2, task3.hangingThread, task3,
                task4.hangingThread, task4);
        assertEquals(4, threadToTaskMap.size());
        assertTrue(threadToTaskMap.containsKey(null));
        
        final HangingMockCallTargetTask buildThreadTask = threadToTaskMap.remove(buildThread);
        assertNotNull(buildThreadTask);
        
        buildThreadTask.selfInterrupt = true;
        buildThreadTask.hang = false;
        synchronized (buildThreadTask) {
            buildThreadTask.notify();
        }
        
        buildThread.join();
        
        assertTrue(buildThreadTask.executed);
        
        final HangingMockCallTargetTask unprocessedTask = threadToTaskMap.remove(null);
        assertNotNull(unprocessedTask);
        
        for (final Map.Entry<Thread, HangingMockCallTargetTask> entry : threadToTaskMap.entrySet()) {
            entry.getKey().join(); // reasonable timeout
            
            assertTrue(entry.getValue().interrupted);
            assertTrue(entry.getValue().executed);
        }
        
        assertFalse(unprocessedTask.executed);
        
        if (failureCause.get() != null) {
            throw failureCause.get();
        }
    }
    
    public void testParallelRun_BuildFailure_ProjectCreateTargetThrowsRuntimeException()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final RuntimeException exception = new RuntimeException("test_msg");
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        project.tasks.add(exception); // task2 fails to be created
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertNotNull(ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task3.executed);
    }
    
    public void testParallelRun_BuildFailure_ProjectCreateTargetThrowsError()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final Error exception = new Error("test_msg");
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        project.tasks.add(exception); // task2 fails to be created
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertNotNull(ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task3.executed);
    }
    
    public void testParallelRun_BuildFailure_BuildListenerThrowsRuntimeException()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        final RuntimeException exception = new RuntimeException("test_msg");
        project.addBuildListener(new MockBuildListener(task2, exception));
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertNotNull(ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task2.executed);
        assertFalse(task3.executed);
    }
    
    public void testParallelRun_BuildFailure_BuildListenerThrowsError()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/", moduleLoader);
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/", moduleLoader);
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        
        final Error exception = new Error("test_msg");
        project.addBuildListener(new MockBuildListener(task2, exception));
        
        task.init();
        task.setTarget("someTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setThreadCount(2);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        try {
            task.perform();
            fail();
        }
        catch (BuildException ex) {
            assertNotNull(ex.getMessage());
            assertSame(exception, ex.getCause());
            assertSame(Location.UNKNOWN_LOCATION, ex.getLocation());
        }
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        assertFalse(task2.executed);
        assertFalse(task3.executed);
    }
}
