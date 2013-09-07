package afc.ant.modular;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import junit.framework.TestCase;

public class ModuleUtilTest extends TestCase
{
    public void testIsModule_NullValue()
    {
        assertFalse(ModuleUtil.isModule(null));
    }
    
    public void testIsModule_NotModule()
    {
        assertFalse(ModuleUtil.isModule(new Object()));
    }
    
    public void testIsModule_ModuleInfo()
    {
        assertFalse(ModuleUtil.isModule(new ModuleInfo("foo")));
    }
    
    public void testIsModule_Module()
    {
        assertTrue(ModuleUtil.isModule(new Module("foo")));
    }
    
    public void testIsModule_Module_DifferentClassLoader() throws Exception
    {
        final Object module = createModuleWithDifferentClassLoader("foo/");
        
        assertTrue(ModuleUtil.isModule(module));
    }
    
    private static Object createModuleWithDifferentClassLoader(final String path) throws Exception
    {
        final Class c = new ModuleClassLoader().loadClass(Module.class.getName());
        
        assertNotSame(c, Module.class);
        
        final Constructor constructor = c.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        
        return constructor.newInstance(path);
    }
    
    private static class ModuleClassLoader extends ClassLoader
    {
        public ModuleClassLoader() throws IOException
        {
            /* Defining Module and its nested classes explicitly so that
             * this class loader does not ask the parent class loader to load them,
             * and a brand new classes are associated with this class loader.
             */ 
            defineClass(Module.class.getName());
            // This code is consistent: the binary name of the class ArrayListSet is used.
            defineClass(Module.class.getName() + "$ArrayListSet");
        }
        
        private void defineClass(final String className) throws IOException
        {
            final InputStream in = Module.class.getClassLoader().getResourceAsStream(
                    className.replace('.', '/') + ".class");
            
            try {
                final BufferedInputStream bufferedIn = new BufferedInputStream(in);
                final ByteArrayOutputStream buf = new ByteArrayOutputStream();
                
                int b;
                while ((b = bufferedIn.read()) >= 0) {
                    buf.write(b);
                }
                
                defineClass(className, buf.toByteArray(), 0, buf.size());
            }
            finally {
                in.close();
            }
        }
    }
}
