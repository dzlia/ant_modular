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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant.Reference;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;

import afc.ant.modular.CallTargetForModules.ParamElement;

import junit.framework.TestCase;

public class CallTargetForModules_SerialUseTest extends TestCase
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
    
    public void testSerialRun_SingleModule_ModulePropertyDefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map());
    }
    
    public void testSerialRun_SingleModule_ModulePropertyUndefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
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
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, true, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_InheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_DoNotInheritAll_InheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, true, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2, "ref3", val3));
    }
    
    public void testSerialRun_SingleModule_WithReferences_DoNotInheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                TestUtil.<String, Object>map("ref1", val1, "ref2", val2));
    }
    
    public void testSerialRun_SingleModule_NoReferencesPassed_DoNotInheritAll_DoNotInheritRefs()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"),
                Collections.<String, Object>emptyMap());
    }
    
    public void testSerialRun_SingleModule_WithUserParams()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setName("hello");
        param1.setValue("world");
        final ParamElement param2 = task.createParam();
        param2.setName("John");
        param2.setValue("Smith");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_AndInheritedPropertiesForced()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamLocation()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", new File("a/b/c").getAbsolutePath()));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFile_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromFileWithPrefix_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setFile(new File("test/data/CallTargetForModules/params_for_test.properties"));
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrl_AndInheritedPropertiesByDefault() throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        
        project.setProperty("123", "456");
        project.setProperty("hello", "world"); // must be overridden by the param property with the same name
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrl_PropertiesNotInherited()
            throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        task.setInheritAll(false);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromUrlWithPrefix_AndInheritedPropertiesByDefault()
            throws Exception
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param1 = task.createParam();
        param1.setUrl(new File("test/data/CallTargetForModules/params_for_test.properties").toURI().toURL());
        param1.setPrefix("afc");
        
        project.setProperty("123", "456");
        project.setProperty("hello", "universe");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspath_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspath_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("John", "Smith", "qwerty", "board", "hello", "world"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathAndPrefix_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_FirstCreateThenSet()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_FirstSetThenCreate()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_MultipleCreate()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithMultiClasspath_MultipleCreate_WithPrefix()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("afc.hello", "world", "afc.John", "Smith", "123", "456",
                        "afc.qwerty", "board", "hello", "universe"));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_RefContainsResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_FirstSetThenSetRef_RefContainsResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithUserParamsFromResourceWithClasspathRef_FirstSetThenSetRef_RefDoesNotContainResource()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"),
                Collections.<String, Object>singletonMap("cpRef", cpRefPath));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesByDefault()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_AndInheritedPropertiesForced()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithPropertySets_PropertiesNotInherited()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("123", "456", "hello", "universe", "qwerty", "board"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_IncludingParamReference()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("hello", "world", "someRef", "ref_value", "123", "456"),
                Collections.<String, Object>singletonMap("cpRef", "ref_value"));
    }
    
    public void testSerialRun_SingleModule_WithUserParams_IncludingParamReference_ModulePropertyUndefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
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
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleLoader.modules.put("foo/", moduleInfo);
        
        final MockCallTargetTask task1 = new MockCallTargetTask(project);
        project.tasks.add(task1);
        
        task.init();
        task.setTarget("testTarget");
        task.setModuleProperty("moduleProp");
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
        TestUtil.assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo, properties,
                Collections.<String, Object>singletonMap("cpRef", "ref_value"));
    }
    
    public void testSerialRun_ModuleWithDeps_ModulePropertyDefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/");
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        dep2.addDependency("quux/");
        final ModuleInfo dep3 = new ModuleInfo("quux/");
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
        task.setModuleProperty("moduleProp");
        task.createModule().setPath("foo");
        task.addConfigured(moduleLoader);
        
        final ParamElement param = task.createParam();
        param.setName("p");
        param.setValue("o");
        
        project.setProperty("qwerty", "board");
        
        task.perform();
        
        TestUtil.assertCallTargetState(task1, true, "someTarget", true, false, "moduleProp", dep3,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task2, true, "someTarget", true, false, "moduleProp", dep2,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task3, true, "someTarget", true, false, "moduleProp", dep1,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
        TestUtil.assertCallTargetState(task4, true, "someTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.<String, Object>map("qwerty", "board", "p", "o"));
    }
    
    public void testSerialRun_ModuleWithDeps_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        dep1.addDependency("baz/");
        final ModuleInfo dep2 = new ModuleInfo("baz/");
        dep2.addAttribute("qq", "ww");
        dep2.addAttribute("aa", "ss");
        dep2.addDependency("quux/");
        final ModuleInfo dep3 = new ModuleInfo("quux/");
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
    
    public void testSerialRun_MultipleModulesWithDeps_RelatedHierarchies_ModulePropertyDefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/");
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/");
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
        task.setModuleProperty("mProp");
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
    
    public void testSerialRun_MultipleModulesWithDeps_RelatedHierarchies_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/");
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/");
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
    
    public void testSerialRun_MultipleModulesWithDeps_UnrelatedHierarchies_ModulePropertyDefined()
    {
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/");
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/");
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
        task.setModuleProperty("mProp");
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
    
    public void testSerialRun_MultipleModulesWithDeps_UnrelatedHierarchies_ModulePropertyUndefined()
    {
        // Unambiguous order of module processing is selected for the sake of simplicity.
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/");
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/");
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
        final ModuleInfo moduleInfo = new ModuleInfo("foo/");
        moduleInfo.addAttribute("1", "2");
        moduleInfo.addDependency("bar/");
        moduleInfo.addDependency("baz/");
        final ModuleInfo dep1 = new ModuleInfo("bar/");
        dep1.addDependency("baz/");
        final ModuleInfo moduleInfo2 = new ModuleInfo("baz/");
        moduleInfo2.addAttribute("qq", "ww");
        moduleInfo2.addAttribute("aa", "ss");
        moduleInfo2.addDependency("quux/");
        final ModuleInfo dep2 = new ModuleInfo("quux/");
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
        task.setModuleProperty("mProp");
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
}
