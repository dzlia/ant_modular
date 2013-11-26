package afc.ant.modular;

import java.io.File;

import junit.framework.TestCase;

// TODO add tests with absolute paths
public class ModuleUtil_NormalisePathTest extends TestCase
{
    public void testNormalisePath_BaseDirIsCurrentDir_NullPath()
    {
        try {
            ModuleUtil.normalisePath(null, new File(""));
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("path", ex.getMessage());
        }
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_EmptyPath()
    {
        assertEquals(".", ModuleUtil.normalisePath("", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_DirectChildPathInNormalisedForm()
    {
        assertEquals("foo", ModuleUtil.normalisePath("foo", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_DirectChildPathInNonNormalisedForm()
    {
        assertEquals("foo", ModuleUtil.normalisePath("foo/", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_DeepPathInNormalisedForm()
    {
        assertEquals("foo/bar/baz", ModuleUtil.normalisePath("foo/bar/baz", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_DeepPathPathInNonNormalisedForm()
    {
        assertEquals("foo/bar/baz", ModuleUtil.normalisePath("foo/bar/baz/", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathIsDotInNormalisedForm()
    {
        assertEquals(".", ModuleUtil.normalisePath(".", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathIsDotInNonNormalisedForm()
    {
        assertEquals(".", ModuleUtil.normalisePath("./", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathStartsWithDot()
    {
        assertEquals("foo/bar/baz", ModuleUtil.normalisePath("./foo/bar/baz", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathContainsDotElement()
    {
        assertEquals("foo/bar/baz", ModuleUtil.normalisePath("foo/bar/./baz/", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathContainsDoubleDotElement()
    {
        assertEquals("foo/baz", ModuleUtil.normalisePath("foo/bar/../baz/", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsCurrentDir_PathContainsDotsAndDoubleDots()
    {
        assertEquals("foo/baz", ModuleUtil.normalisePath("foo/./bar/.././baz/quux/../", new File("")));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathContainsDotsAndDoubleDots()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("foo/baz", ModuleUtil.normalisePath("foo/./bar/.././baz/quux/../", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDir_SimpleCase()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("../foo", ModuleUtil.normalisePath("../foo/", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDir_ComplicatedCase()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("../foo/baz", ModuleUtil.normalisePath("../foo/bar/../baz/./quux/..", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndPartiallyGoesBack()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("..", ModuleUtil.normalisePath("../../hello", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndPartiallyGoesBack_ComplicatedCase()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("../universe", ModuleUtil.normalisePath("../../hello/universe", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndReturnsToIt_SimpleCase()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("foo", ModuleUtil.normalisePath("../world/foo", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndReturnsToIt_NormalisedPathIsBaseDir()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals(".", ModuleUtil.normalisePath("../world", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndReturnsToIt_ComplicatedCase()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("foo/baz", ModuleUtil.normalisePath(
                "../qwe/../../hello/world/foo/bar/../baz/./quux/..", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesBeyondRoot()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("foo/baz", ModuleUtil.normalisePath(
                "../qwe/../../../hello/world/foo/bar/../baz/./quux/..", baseDir));
    }
}