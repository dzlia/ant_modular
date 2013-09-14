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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo, TestUtil.map());
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, TestUtil.map());
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456"));
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
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("John", "Smith", "qwerty", "board", "hello", "world"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("afc.hello", "world", "afc.John", "Smith", "123", "456", "afc.qwerty", "board",
                        "hello", "universe"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("John", "Smith", "qwerty", "board", "hello", "world"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("afc.hello", "world", "afc.John", "Smith", "123", "456", "afc.qwerty", "board",
                        "hello", "universe"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("John", "Smith", "qwerty", "board", "hello", "world"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("afc.hello", "world", "afc.John", "Smith", "123", "456", "afc.qwerty", "board",
                        "hello", "universe"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("hello", "world", "John", "Smith", "123", "456", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("afc.hello", "world", "afc.John", "Smith", "123", "456", "afc.qwerty", "board",
                        "hello", "universe"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", true, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "12345", "45678", "hello", "universe", "qwerty", "board"));
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
        
        assertCallTargetState(task1, true, "testTarget", false, false, "moduleProp", moduleInfo,
                TestUtil.map("123", "456", "hello", "universe", "qwerty", "board"));
    }
    
    private static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs, final String moduleProperty,
            final ModuleInfo proto, final Map<String, Object> properties)
    {
        assertEquals(executed, task.executed);
        assertEquals(target, task.target);
        assertEquals(inheritAll, task.inheritAll);
        assertEquals(inheritRefs, task.inheritRefs);
        
        final Object moduleObj = task.ownProject.getProperties().get(moduleProperty);
        assertTrue(moduleObj instanceof Module);
        final Module module = (Module) moduleObj;
        assertEquals(proto.getPath(), module.getPath());
        assertEquals(proto.getAttributes(), module.getAttributes());
        final HashSet<String> depPaths = new HashSet<String>();
        for (final Module dep : module.getDependencies()) {
            assertTrue(depPaths.add(dep.getPath()));
        }
        assertEquals(proto.getDependencies(), depPaths);
        
        // merging module property into the properties passed. The module object is not freely available
        final HashMap<String, Object> propsWithModule = new HashMap<String, Object>(properties);
        propsWithModule.put(moduleProperty, module);
        assertEquals(propsWithModule, task.ownProject.getProperties());
    }
    
    private static void assertCallTargetState(final MockCallTargetTask task, final boolean executed,
            final String target, final boolean inheritAll, final boolean inheritRefs,
            final Map<String, Object> properties)
    {
        assertEquals(executed, task.executed);
        assertEquals(target, task.target);
        assertEquals(inheritAll, task.inheritAll);
        assertEquals(inheritRefs, task.inheritRefs);
        
        assertEquals(properties, task.ownProject.getProperties());
    }
}
