package afc.ant.modular;

import java.util.HashMap;

import junit.framework.Assert;

public class MockModuleLoader implements ModuleLoader
{
    public final HashMap<String, Object> modules = new HashMap<String, Object>();
    
    public ModuleInfo loadModule(final String path) throws ModuleNotLoadedException
    {
        Assert.assertNotNull(path);
        final Object val = modules.get(path);
        Assert.assertNotNull(val);
        if (val instanceof ModuleNotLoadedException) {
            throw (ModuleNotLoadedException) val;
        }
        return (ModuleInfo) val;
    }

}
