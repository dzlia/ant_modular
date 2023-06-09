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
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;

public class MockCallTargetTask extends Ant
{
    // Allows tests verify isolation between module targets.
    public Map<String, Object> propertiesToSet;
    
    public Throwable exception;
    public boolean executed;
    
    public MockProject ownProject;
    
    public boolean inheritAll;
    public boolean inheritRefs;
    public String target;
    
    private final ArrayList<Reference> references = new ArrayList<Reference>();
    private final ArrayList<Property> params = new ArrayList<Property>();
    private final ArrayList<PropertySet> propertySets = new ArrayList<PropertySet>();
    
    private boolean initialised = false;
    private String antFile;
    private String expectedAntFile;
    
    public MockCallTargetTask(final Project project)
    {
        setProject(project);
        ownProject = new MockProject();
        // The target project inherits basedir from the invoker's project.
        ownProject.setBaseDir(project.getBaseDir());
        expectedAntFile = project.getProperty("ant.file");
    }
    
    @Override
    public void init()
    {
        // Initialisation is mocked and performed in the constructor.
        initialised = true;
    }
    
    @Override
    public void setAntfile(String antFile)
    {
        this.antFile = antFile;
        super.setAntfile(antFile);
    }
    
    @Override
    public void execute()
    {
        Assert.assertTrue(initialised);
        Assert.assertNotNull(antFile);
        Assert.assertEquals(expectedAntFile, antFile);
        Assert.assertFalse(executed);
        executed = true;
        
        // lightweight execute; filling own project properties with those overridden via params and property sets
        for (final Property param : params) {
            param.setProject(ownProject);
            param.execute();
        }
        
        final PropertyHelper helper = PropertyHelper.getPropertyHelper(ownProject);
        
        for (final PropertySet propSet : propertySets) {
            for (final Map.Entry<Object, Object> prop : propSet.getProperties().entrySet()) {
                helper.setProperty((String) prop.getKey(), prop.getValue(), false);
            }
        }
        
        if (inheritAll)
        {
            for (final Map.Entry<String, Object> prop :
                    (Set<Map.Entry<String, Object>>) getProject().getProperties().entrySet()) {
                final String propertyName = (String) prop.getKey();
                if (propertyName.equals(MagicNames.PROJECT_BASEDIR)) {
                    continue;
                }
                helper.setNewProperty("", propertyName, prop.getValue());
            }
        }
        
        // lightwight adding references to the own project.
        if (inheritAll || inheritRefs) {
            for (final Map.Entry<String, Object> ref :
                    (Set<Map.Entry<String, Object>>) getProject().getReferences().entrySet()) {
                final String refName = (String) ref.getKey();
                if (refName.equals(MagicNames.REFID_PROPERTY_HELPER) ||
                        refName.equals(ComponentHelper.COMPONENT_HELPER_REFERENCE)) {
                    continue;
                }
                if (!ownProject.getReferences().containsKey(refName)) {
                    ownProject.addReference(refName, ref.getValue());
                }
            }
        }
        for (final Reference ref : references) {
            Assert.assertNotNull(ref.getProject());
            final String refId = ref.getRefId();
            if (!ownProject.getReferences().containsKey(refId)) {
                ownProject.addReference(refId, ref.getReferencedObject(null));
            }
        }
        
        if (propertiesToSet != null)
        {
            for (final Map.Entry<String, Object> prop : propertiesToSet.entrySet()) {
                helper.setNewProperty("", (String) prop.getKey(), prop.getValue());
            }
        }
        
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }
        if (exception instanceof Error) {
            throw (Error) exception;
        }
    }
    
    @Override
    public void setTarget(final String target)
    {
        this.target = target;
        super.setTarget(target);
    }
    
    @Override
    public void setInheritAll(final boolean inheritAll)
    {
        this.inheritAll = inheritAll;
        super.setInheritAll(inheritAll);
    }
    
    @Override
    public void setInheritRefs(final boolean inheritRefs)
    {
        this.inheritRefs = inheritRefs;
        super.setInheritRefs(inheritRefs);
    }
    
    @Override
    public void addReference(final Reference reference)
    {
        references.add(reference);
        super.addReference(reference);
    }
    
    @Override
    public Property createProperty()
    {
        final Property param = super.createProperty();
        param.setProject(ownProject);
        params.add(param);
        return param;
    }
    
    @Override
    public void addPropertyset(final PropertySet propertySet)
    {
        propertySets.add(propertySet);
        super.addPropertyset(propertySet);
    }
    
    @Override
    protected Project getNewProject()
    {
        return ownProject;
    }
}
