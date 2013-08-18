package afc.ant.modular;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;

public class ModuleUtil
{
    // Uses reflection to call the getter to support Module objects loaded by different class loaders.
    public static String getPath(final Object module)
    {
        validateModule(module);
        return (String) callFunction(module, "getPath");
    }
    
    // Uses reflection to call the getter to support Module objects loaded by different class loaders.
    public static Set<?> getDependencies(final Object module)
    {
        validateModule(module);
        return (Set<?>) callFunction(module, "getDependencies");
    }
    
    // Uses reflection to call the getter to support Module objects loaded by different class loaders.
    public static Map<String, Object> getAttributes(final Object module)
    {
        validateModule(module);
        return (Map<String, Object>) callFunction(module, "getAttributes");
    }
    
    private static void validateModule(final Object module)
    {
        if (module == null) {
            throw new NullPointerException("module");
        }
        final String className = module.getClass().getName();
        if (!className.equals(Module.class.getName())) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unsupported module type. Expected: ''{0}'', was: ''{1}''",
                    Module.class.getName(), className));
        }
    }
    
    private static Object callFunction(final Object module, final String functionName)
    {
        try {
            return module.getClass().getMethod(functionName).invoke(module);
        }
        catch (IllegalAccessException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
        }
        catch (NoSuchMethodException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
        }
        catch (InvocationTargetException ex) {
            throw new BuildException("Unable to get module attributes.", ex);
        }
    }
}
