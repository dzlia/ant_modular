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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;

import antmodular.CallTargetForModules;
import antmodular.ModuleInfo;
import antmodular.CallTargetForModules.ModuleElement;
import antmodular.CallTargetForModules.ParamElement;

public class CallTargetForModules_SerialUseTest extends TestCase
{
    private CallTargetForModules task;
    private MockProject project;
    private MockModuleLoader moduleLoader;
    
    @Override
    protected void setUp()
    {
        project = new MockProject();
        project.setProperty(MagicNames.ANT_FILE, "ant_file");
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
    
    public void testSerialRun_SingleModule_ModuleRefIdDefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map());
    }
    
    public void testSerialRun_SingleModule_ModuleRefIdUndefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, TestUtil.<String, Object>map());
    }
    
    public void testSerialRun_SingleModule_WithReferences_InheritAll_InheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Object val1 = new Object();
        final Object val2 = new Object();
        final Object val3 = new Object();
        project.addReference("ref1", val1);
        project.addReference("ref2", val2);
        project.addReference("ref3", val3);
        
        final Reference ref1 = new Reference();
        ref1.setProject(project);
        ref1.setRefId("ref1");
        task.addReference(ref1);
        final Reference ref2 = new Reference();
        ref2.setProject(project);
        ref2.setRefId("ref2");
        task.addReference(ref2);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.setInheritAll(true);
        task.setInheritRefs(true);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, true, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_InheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Object val1 = new Object();
        final Object val2 = new Object();
        final Object val3 = new Object();
        project.addReference("ref1", val1);
        project.addReference("ref2", val2);
        project.addReference("ref3", val3);
        
        final Reference ref1 = new Reference();
        ref1.setProject(project);
        ref1.setRefId("ref1");
        task.addReference(ref1);
        final Reference ref2 = new Reference();
        ref2.setProject(project);
        ref2.setRefId("ref2");
        task.addReference(ref2);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.setInheritAll(true);
        task.setInheritRefs(false);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_DoNotInheritAll_InheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Object val1 = new Object();
        final Object val2 = new Object();
        final Object val3 = new Object();
        project.addReference("ref1", val1);
        project.addReference("ref2", val2);
        project.addReference("ref3", val3);
        
        final Reference ref1 = new Reference();
        ref1.setProject(project);
        ref1.setRefId("ref1");
        task.addReference(ref1);
        final Reference ref2 = new Reference();
        ref2.setProject(project);
        ref2.setRefId("ref2");
        task.addReference(ref2);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.setInheritAll(false);
        task.setInheritRefs(true);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, true, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_DoNotInheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Object val1 = new Object();
        final Object val2 = new Object();
        final Object val3 = new Object();
        project.addReference("ref1", val1);
        project.addReference("ref2", val2);
        project.addReference("ref3", val3);
        
        final Reference ref1 = new Reference();
        ref1.setProject(project);
        ref1.setRefId("ref1");
        task.addReference(ref1);
        final Reference ref2 = new Reference();
        ref2.setProject(project);
        ref2.setRefId("ref2");
        task.addReference(ref2);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.setInheritAll(false);
        task.setInheritRefs(false);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2));
    }
    
    public void testSerialRun_SingleModule_NoReferencesPassed_DoNotInheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Object val1 = new Object();
        final Object val2 = new Object();
        final Object val3 = new Object();
        project.addReference("ref1", val1);
        project.addReference("ref2", val2);
        project.addReference("ref3", val3);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.setInheritAll(false);
        task.setInheritRefs(false);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                Collections.<String, Object>emptyMap());
    }
    
    public void testSerialRun_SingleModule_WithUserParams()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesForced()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(true);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamLocation()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setLocation(new File("a/b/c"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", new File("a/b/c").getAbsolutePath()));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFileWithPrefix_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrl_AndInheritedPropertiesByDefault() throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        
        project.setProperty("123", "456");
        project.setProperty("hello", "world"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrl_PropertiesNotInherited()
            throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrlWithPrefix_AndInheritedPropertiesByDefault()
            throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspath_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path = new Path(project);
        path.setLocation(new File("test/data/"));
        param1.setClasspath(path);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspath_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path = new Path(project);
        path.setLocation(new File("test/data/"));
        param1.setClasspath(path);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathAndPrefix_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path = new Path(project);
        path.setLocation(new File("test/data/"));
        param1.setClasspath(path);
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_FirstCreateThenSet()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = param1.createClasspath();
        path1.setLocation(new File("test/"));
        final Path path2 = new Path(project);
        path2.setLocation(new File("test/data/"));
        param1.setClasspath(path2);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_FirstSetThenCreate()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = new Path(project);
        path1.setLocation(new File("test/data/"));
        param1.setClasspath(path1);
        final Path path2 = param1.createClasspath();
        path2.setLocation(new File("test/"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_MultipleCreate()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = param1.createClasspath();
        path1.setLocation(new File("test/data/"));
        final Path path2 = param1.createClasspath();
        path2.setLocation(new File("test/"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_MultipleCreate_WithPrefix()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = param1.createClasspath();
        path1.setLocation(new File("test/data/"));
        final Path path2 = param1.createClasspath();
        path2.setLocation(new File("test/"));
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_RefContainsResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Path cpRefPath = new Path(project);
        cpRefPath.setLocation(new File("test/data/"));
        project.addReference("cpRef", cpRefPath);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Reference classpathRef = new Reference();
        classpathRef.setProject(project);
        classpathRef.setRefId("cpRef");
        param1.setClasspathRef(classpathRef);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_FirstSetThenSetRef_RefContainsResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Path cpRefPath = new Path(project);
        cpRefPath.setLocation(new File("test/data/"));
        project.addReference("cpRef", cpRefPath);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = new Path(project);
        path1.setLocation(new File("test/"));
        param1.setClasspath(path1);
        final Reference classpathRef = new Reference();
        classpathRef.setProject(project);
        classpathRef.setRefId("cpRef");
        param1.setClasspathRef(classpathRef);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_FirstSetThenSetRef_RefDoesNotContainResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Path cpRefPath = new Path(project);
        cpRefPath.setLocation(new File("test/"));
        project.addReference("cpRef", cpRefPath);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/CallTargetForModules/params_for_test.properties");
        final Path path1 = new Path(project);
        path1.setLocation(new File("test/data/"));
        param1.setClasspath(path1);
        final Reference classpathRef = new Reference();
        classpathRef.setProject(project);
        classpathRef.setRefId("cpRef");
        param1.setClasspathRef(classpathRef);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesForced()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(true);
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final PropertySet propSet1 = new PropertySet();
        propSet1.setProject(project);
        propSet1.appendName("123");
        propSet1.appendName("hello");
        propSet1.appendName("no_such_property");
        final PropertySet propSet2 = new PropertySet();
        propSet2.setProject(project);
        propSet2.appendName("qwerty");
        task.addPropertyset(propSet1);
        task.addPropertyset(propSet2);
        
        project.setProperty("123", "456");
        project.setProperty("12345", "45678");
        project.setProperty("hello", "universe");
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_IncludingParamReference()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        project.addReference("cpRef", "ref_value");
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        final Reference ref = new Reference();
        ref.setProject(project);
        ref.setRefId("cpRef");
        param2.setName("someRef");
        param2.setRefid(ref);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "someRef", "ref_value", "123", "456"),
                Collections.<String, Object>singletonMap("cpRef", "ref_value"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_IncludingParamReference_ModuleRefIdUndefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        project.addReference("cpRef", "ref_value");
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        final Reference ref = new Reference();
        ref.setProject(project);
        ref.setRefId("cpRef");
        param2.setName("someRef");
        param2.setRefid(ref);
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false,
                TestUtil.<String, Object>map("hello", "world", "someRef", "ref_value", "123", "456"),
                Collections.<String, Object>singletonMap("cpRef", "ref_value"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_IncludingParamEnvironment()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        project.addReference("cpRef", "ref_value");
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        task.createParam().setEnvironment("env");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param with the same name
        
        task.perform();
        
        // adding all system environment variables prefixed with 'env.'
        final HashMap<String, Object> properties = new HashMap<String, Object>();
        for (final Map.Entry<String, String> e : System.getenv().entrySet()) {
            properties.put("env." + e.getKey(), e.getValue());
        }
        properties.putAll(TestUtil.<String, Object>map("hello", "world", "123", "456"));
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo, properties,
                Collections.<String, Object>singletonMap("cpRef", "ref_value"));
    }
    
    public void testSerialRun_ModuleWithDeps_ModuleRefIdDefined()
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
        dep2.addDependency("quux/");
        final ModuleInfo dep3 = new ModuleInfo("quux/", moduleLoader);
        dep3.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        moduleLoader.modules.put("quux/", dep3);
        
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
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleRef", dep3,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "moduleRef", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false, "moduleRef", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testSerialRun_ModuleWithDeps_ModuleRefIdUndefined()
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
        dep2.addDependency("quux/");
        final ModuleInfo dep3 = new ModuleInfo("quux/", moduleLoader);
        dep3.addAttribute("z", "x");
        
        moduleLoader.modules.put("foo/", moduleInfo);
        moduleLoader.modules.put("bar/", dep1);
        moduleLoader.modules.put("baz/", dep2);
        moduleLoader.modules.put("quux/", dep3);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        final MockCallTargetTask task2 = new MockCallTargetTask(project);
        project.tasks.add(task2);
        final MockCallTargetTask task3 = new MockCallTargetTask(project);
        project.tasks.add(task3);
        final MockCallTargetTask task4 = new MockCallTargetTask(project);
        project.tasks.add(task4);
        
        task.init();
        task.setTarget("testTarget");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "testTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "testTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "testTarget", true, false,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testSerialRun_MultipleModulesWithDeps_RelatedHierarchies_ModuleRefIdDefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_RelatedHierarchies_ModuleRefIdUndefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_UnrelatedHierarchies_ModuleRefIdDefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_UnrelatedHierarchies_ModuleRefIdUndefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_ThreadCountSetToOne()
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
        task.setThreadCount(1); // indicates serial execution
        
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
    
    public void testSerialRun_ModuleWithDeps_BuildFailure()
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
    
    public void testSerialRun_ModuleWithDeps_RuntimeExceptionInATarget()
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
    
    public void testSerialRun_MultipleModulesWithDeps_CustomTarget_ModuleRefIdDefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_CustomTarget_ModuleRefIdUndefined()
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
    
    public void testSerialRun_MultipleModulesWithDeps_RepeatedModuleElement_UnambiguousTarget()
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
    
    public void testSerialRun_MultipleModulesWithDeps_RepeatedModuleElement_AmbiguousNonDefaultTargets()
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
    
    public void testSerialRun_MultipleModulesWithDeps_RepeatedModuleElement_AmbiguousTargets_WithDefaultTarget()
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
    
    public void testSerialRun_BuildFailure_ProjectCreateTargetThrowsRuntimeException()
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
    
    public void testSerialRun_BuildFailure_ProjectCreateTargetThrowsError()
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
    
    public void testSerialRun_BuildFailure_BuildListenerThrowsRuntimeException()
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
    
    public void testSerialRun_BuildFailure_BuildListenerThrowsError()
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
    
    /**
     * <p>Tests that the {@code <param>} elements of the task {@code <callTargetForModules}
     * which are configured with paths resolve resources correctly if the project basedir
     * is set non-default.</p>
     */
    public void testSerialRun_SingleModule_ParamsLoadedFromResource_NonDefaultProjectBasedir()
    {
        project.setBaseDir(new File("test/data/"));
        
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/params_for_test.properties");
        final Path path1 = param1.createClasspath();
        path1.setPath("CallTargetForModules/");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    /**
     * <p>Tests that {@code ModuleRefId} overrides {@code <reference>} elements if both define
     * reference with the same name.</p>
     */
    public void testThatModuleRefIdOverridesRefElements()
    {
        project.setBaseDir(new File("test/data/"));
        project.addReference("moduleRef", new Object());
        
        final Object ref2Object = new Object();
        project.addReference("anotherRef", ref2Object);
        
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("moduleRef");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final Reference ref = new Reference();
        ref.setProject(project);
        ref.setRefId("moduleRef");
        task.addReference(ref);
        
        final ParamElement param1 = task.createParam();
        param1.setResource("/params_for_test.properties");
        final Path path1 = param1.createClasspath();
        path1.setPath("CallTargetForModules/");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleRef", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                TestUtil.<String, Object>map("anotherRef", ref2Object));
    }
    
    /**
     * <p>Tests that an empty string passed to
     * {@link CallTargetForModules#setModuleRefId(String)} is considered
     * a defined property.</p>
     */
    public void testThatEmptyModuleRefIdIsConsideredADefinedReference()
    {
        project.setBaseDir(new File("test/data/"));
        
        final ModuleInfo moduleInfo = new ModuleInfo("foo/", moduleLoader);
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleRefId("");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        project.setProperty("123", "456");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "", moduleInfo,
                Collections.<String, Object>singletonMap("123", "456"));
    }
}
