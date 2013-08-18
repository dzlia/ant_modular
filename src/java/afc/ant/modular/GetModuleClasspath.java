package afc.ant.modular;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

public class GetModuleClasspath extends Task
{
    private String moduleProperty;
    private final ArrayList<SourceAttribute> sourceAttributes = new ArrayList<SourceAttribute>();
    private String outputProperty;
    private boolean includeDependencies;
    
    @Override
    public void execute()
    {
        if (moduleProperty == null) {
            throw new BuildException("'moduleProperty' is undefined.");
        }
        if (sourceAttributes.isEmpty()) {
            throw new BuildException("Source attributes are not defined.");
        }
        if (outputProperty == null) {
            throw new BuildException("'outputProperty' is undefined.");
        }
        for (final SourceAttribute sourceAttribute : sourceAttributes) {
            if (sourceAttribute.name == null) {
                throw new BuildException("A source attribute with undefined name is specified.");
            }
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
        
        final Path classpath = new Path(project);
        appendClasspathElements(moduleObject, classpath, new LinkedHashSet<Object>());
        
        propHelper.setProperty(outputProperty, classpath, false);
    }
    
    private void appendClasspathElements(final Object /*Module*/ module, final Path classpath,
            final LinkedHashSet<Object> processedModules)
    {
        if (processedModules.contains(module)) {
            /* Each module is processed only once. In addition, if there are
               cyclic dependencies then there is no infinite recursion. */
            return;
        }
        processedModules.add(module);
        
        appendElements(module, classpath);
        
        if (includeDependencies) {
            for (final Object /*Module*/ dep : ModuleUtil.getDependencies(module)) {
                appendClasspathElements(dep, classpath, processedModules);
            }
        }
    }
    
    private void appendElements(final Object /*Module*/ module, final Path classpath)
    {
        final Map<String, Object> attributes = ModuleUtil.getAttributes(module);
        for (final SourceAttribute sourceAttribute : sourceAttributes) {
            final Object o = attributes.get(sourceAttribute.name);
            if (o == null) {
                continue;
            }
            if (!(o instanceof Path)) {
                throw new BuildException(MessageFormat.format(
                        "The attribute ''{0}'' of the module ''{1}'' is not an Ant path.",
                        sourceAttribute.name, ModuleUtil.getPath(module)));
            }
            classpath.add((Path) o);
        }
    }
    
    public void setModuleProperty(final String moduleProperty)
    {
        this.moduleProperty = moduleProperty;
    }
    
    public void setSourceAttribute(final String name)
    {
        createSourceAttribute().setName(name);
    }
    
    public SourceAttribute createSourceAttribute()
    {
        final SourceAttribute o = new SourceAttribute();
        sourceAttributes.add(o);
        return o;
    }
    
    private static class SourceAttribute
    {
        private String name;
        
        public void setName(final String name)
        {
            this.name = name;
        }
    }
    
    public void setOutputProperty(final String name)
    {
        outputProperty = name;
    }
    
    public void setIncludeDependencies(final boolean option)
    {
        includeDependencies = option;
    }
}
