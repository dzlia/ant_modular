package afc.ant.modular;

import java.util.ArrayList;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;

import junit.framework.Assert;

public class MockCallTargetTask extends CallTarget
{
    public Throwable exception;
    public boolean executed;
    
    public final MockProject ownProject;
    
    public boolean inheritAll;
    public boolean inheritRefs;
    
    private final ArrayList<Property> params = new ArrayList<Property>();
    private final ArrayList<PropertySet> propertySets = new ArrayList<PropertySet>();
    
    public MockCallTargetTask(final Project project)
    {
        setProject(project);
        ownProject = new MockProject();
    }
    
    @Override
    public void execute()
    {
        Assert.assertFalse(executed);
        executed = true;
        
        // lightweight execute; filling own project properties with those overridden via params and property sets
        for (final Property param : params) {
            param.setProject(ownProject);
            param.execute();
        }
        for (final PropertySet propSet : propertySets) {
            for (final Map.Entry<Object, Object> prop : propSet.getProperties().entrySet()) {
                PropertyHelper.setNewProperty(ownProject, (String) prop.getKey(), prop.getValue());
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
    public Property createParam()
    {
        final Property param = super.createParam();
        params.add(param);
        return param;
    }
    
    @Override
    public void addPropertyset(final PropertySet propertySet)
    {
        propertySets.add(propertySet);
        super.addPropertyset(propertySet);
    }
}
