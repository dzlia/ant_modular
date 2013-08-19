package afc.ant.modular;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

import junit.framework.TestCase;

// TODO add more tests
public class ManifestModuleLoaderTest extends TestCase
{
    private Project project;
    private File baseDir;
    private ManifestModuleLoader loader;
    
    @Override
    protected void setUp()
    {
        baseDir = new File("test/data/ManifestModuleLoader");
        project = new Project();
        project.setBaseDir(baseDir);
        loader = new ManifestModuleLoader();
    }
    
    public void testLoadModule_NoDependencies_NoAttributes() throws Exception
    {
        loader.setProject(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("NoDeps_NoAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("NoDeps_NoAttributes/", moduleInfo.getPath());
        assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
    
    public void testLoadModule_EmptyDependencies_NoAttributes() throws Exception
    {
        loader.setProject(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("EmptyDeps_NoAttributes/");
        
        assertNotNull(moduleInfo);
        assertEquals("EmptyDeps_NoAttributes/", moduleInfo.getPath());
        assertEquals(Collections.emptySet(), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
    
    public void testLoadModule_WithDependencies_NoAttributes() throws Exception
    {
        loader.setProject(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_NoAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_NoAttributes/", moduleInfo.getPath());
        assertEquals(TestUtil.set("foo/", "\u0142aska/", "baz/quux/"), moduleInfo.getDependencies());
        assertEquals(Collections.emptyMap(), moduleInfo.getAttributes());
    }
    
    public void testLoadModule_WithDependencies_WithAttributes_NoClasspathAttributes() throws Exception
    {
        loader.setProject(project);
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_WithAttributes/", moduleInfo.getPath());
        assertEquals(TestUtil.set("foo/", "bar/baz/"), moduleInfo.getDependencies());
        assertEquals(TestUtil.map("Attrib1", "", "Attrib2", "a b  cc/%C5%81/e", "Attrib3", "hello, world!", "aTTRIB4", "12345"),
                moduleInfo.getAttributes());
    }
    
    public void testLoadModule_WithDependencies_WithAttributes_SingleClasspathAttribute() throws Exception
    {
        final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
        
        loader.setProject(project);
        loader.createClasspathAttribute().setName("ATTRIB2"); // manifest attribute names are case-insensitive
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_WithAttributes/", moduleInfo.getPath());
        assertEquals(TestUtil.set("foo/", "bar/baz/"), moduleInfo.getDependencies());
        assertEquals(TestUtil.set("Attrib1", "ATTRIB2", "Attrib3", "aTTRIB4"), moduleInfo.getAttributes().keySet());
        assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
        assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
        assertEquals("12345", moduleInfo.getAttributes().get("aTTRIB4"));
        
        assertPath(moduleInfo.getAttributes().get("ATTRIB2"),
                new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "cc/\u0141/e"));
    }
    
    public void testLoadModule_WithDependencies_WithAttributes_MultipleClasspathAttribute() throws Exception
    {
        final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
        
        loader.setProject(project);
        loader.createClasspathAttribute().setName("Attrib2");
        loader.createClasspathAttribute().setName("Attrib4"); // manifest attribute names are case-insensitive
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_WithAttributes/", moduleInfo.getPath());
        assertEquals(TestUtil.set("foo/", "bar/baz/"), moduleInfo.getDependencies());
        assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "Attrib4"), moduleInfo.getAttributes().keySet());
        assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
        assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
        
        assertPath(moduleInfo.getAttributes().get("Attrib2"),
                new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "cc/\u0141/e"));
        
        assertPath(moduleInfo.getAttributes().get("Attrib4"), new File(moduleDir, "12345"));
    }
    
    public void testLoadModule_WithDependencies_WithAttributes_ClasspathAttributeNotFound() throws Exception
    {
        final File moduleDir = new File(baseDir, "WithDeps_WithAttributes");
        
        loader.setProject(project);
        loader.createClasspathAttribute().setName("Attrib2");
        loader.createClasspathAttribute().setName("NoSuchAttribute");
        
        final ModuleInfo moduleInfo = loader.loadModule("WithDeps_WithAttributes");
        
        assertNotNull(moduleInfo);
        assertEquals("WithDeps_WithAttributes/", moduleInfo.getPath());
        assertEquals(TestUtil.set("foo/", "bar/baz/"), moduleInfo.getDependencies());
        assertEquals(TestUtil.set("Attrib1", "Attrib2", "Attrib3", "aTTRIB4"), moduleInfo.getAttributes().keySet());
        assertEquals("", moduleInfo.getAttributes().get("Attrib1"));
        assertEquals("hello, world!", moduleInfo.getAttributes().get("Attrib3"));
        assertEquals("12345", moduleInfo.getAttributes().get("aTTRIB4"));
        
        assertPath(moduleInfo.getAttributes().get("Attrib2"),
                new File(moduleDir, "a"), new File(moduleDir, "b"), new File(moduleDir, "cc/\u0141/e"));
    }
    
    public void testLoadModule_ClasspathAttributeHasNoName() throws Exception
    {
        loader.setProject(project);
        loader.createClasspathAttribute().setName("Attrib2");
        
        try {
            loader.createClasspathAttribute(); // no name is set
            loader.loadModule("WithDeps_NoAttributes");
            
            fail();
        }
        catch (BuildException ex) {
            assertEquals("A 'classpathAttribute' element with undefined name is encountered.", ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleDoesNotExist() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("NoSuchModule");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'NoSuchModule' ('" +
                    new File(baseDir, "NoSuchModule").getAbsolutePath() + "') does not exist.", ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleDoesNotHaveManifest() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("ModuleWithNoManifest");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'ModuleWithNoManifest' does not have the manifest ('" +
                    new File(baseDir, "ModuleWithNoManifest/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                    ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleDoesNotHaveMETAINFDirectory() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("ModuleWithNoMetainf");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'ModuleWithNoMetainf' does not have the manifest ('" +
                    new File(baseDir, "ModuleWithNoMetainf/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                    ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleManifestIsNotAFile() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("ModuleWithManifestAsDir");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'ModuleWithManifestAsDir' has the manifest that is not a file ('" +
                    new File(baseDir, "ModuleWithManifestAsDir/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                    ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleManifestIsMalformed() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("ModuleWithMalformedManifest");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            /* The message is not the beast for a malformed manifest. It is caused by the fact that
               java.util.jar.Manifest#read does not distinguish real I/O errors and malformed format errors. */
            assertEquals("An I/O error is encountered while loading the manifest of " +
                    "the module 'ModuleWithMalformedManifest' ('" +
                    new File(baseDir, "ModuleWithMalformedManifest/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                    ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModuleManifestDoesNotHaveBuildSection() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("NoBuildSection");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module 'NoBuildSection' does not have the 'Build' section in its manifest ('" +
                    new File(baseDir, "NoBuildSection/META-INF/MANIFEST.MF").getAbsolutePath() + "').",
                    ex.getMessage());
        }
    }
    
    public void testCannotLoadModule_ModulePathIsNotADirectory() throws Exception
    {
        loader.setProject(project);
        
        try {
            loader.loadModule("ModuleIsNotADirectory");
            
            fail();
        }
        catch (ModuleNotLoadedException ex) {
            assertEquals("The module path 'ModuleIsNotADirectory' ('" +
                    new File(baseDir, "ModuleIsNotADirectory").getAbsolutePath() + "') is not a directory.",
                    ex.getMessage());
        }
    }
    
    private static void assertPath(final Object pathObject, final File... expectedElements)
    {
        assertNotNull(pathObject);
        assertTrue(pathObject instanceof Path);
        final Path path = (Path) pathObject;
        assertEquals(expectedElements.length, path.size());
        final Iterator i = path.iterator();
        for (final File element : expectedElements) {
            assertEquals(element.getAbsolutePath(), ((FileResource) i.next()).getFile().getAbsolutePath());
        }
        assertFalse(i.hasNext()); // sanity check
    }
}