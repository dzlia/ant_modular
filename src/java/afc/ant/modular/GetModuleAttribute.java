package afc.ant.modular;

import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;

public class GetModuleAttribute extends Task
{
    private String moduleProperty;
    private String outputProperty;
    private String name;
    
    @Override
    public void execute()
    {
        if (moduleProperty == null) {
            throw new BuildException("'moduleProperty' is undefined.");
        }
        if (outputProperty == null) {
            throw new BuildException("'outputProperty' is undefined.");
        }
        if (name == null) {
            throw new BuildException("'name' is undefined.");
        }
        final Project project = getProject();
        final PropertyHelper propHelper = PropertyHelper.getPropertyHelper(project);
        final Object moduleObject = propHelper.getProperty(moduleProperty);
        if (moduleObject == null) {
            throw new BuildException(MessageFormat.format(
                    "No module is found under the property ''{0}''.", moduleProperty));
        }
        
        /* This task is invoked from within a target that is called by CallTargetForModules.
         * A new project is created for this target and therefore a new class loader.
         * Multiple instances of the Module class could be created: one for the module instance
         * that is put by CallTargetForModules and one that is loaded by this class' class loader.
         * 
         * Reflection is used to handle the original module object with any configuration of
         * Ant class loader hierarchy.
         */
        if (!moduleObject.getClass().getName().equals(Module.class.getName())) {
            throw new BuildException(MessageFormat.format(
                    "Invalid module type is found under the property ''{0}''. Expected: ''{1}'', found: ''{2}''.",
                    moduleProperty, Module.class.getName(), moduleObject.getClass().getName()));
        }
        
        propHelper.setProperty(outputProperty, ModuleUtil.getAttributes(moduleObject).get(name), false);
    }
    
    public void setModuleProperty(final String moduleProperty)
    {
        this.moduleProperty = moduleProperty;
    }
    
    public void setOutputProperty(final String name)
    {
        outputProperty = name;
    }
    
    public void setName(final String name)
    {
        this.name = name;
    }
}
