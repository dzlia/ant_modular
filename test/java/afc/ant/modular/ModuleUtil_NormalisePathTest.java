package afc.ant.modular;

import java.io.File;

import junit.framework.TestCase;

/**
 * <p>Unit tests for {@link ModuleUtil#normalisePath(String, File)}.</p>
 * 
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleUtil_NormalisePathTest extends TestCase
{
    /**
     * <p>Tests that {@link ModuleUtil#normalisePath(String, File)} throws
     * {@link NullPointerException} if a {@code null} base directory is passed in.</p>
     */
    public void testNormalisePath_BaseDirIsCurrentDir_NullBaseDir()
    {
        try {
            ModuleUtil.normalisePath("foo/bar", null);
            fail();
        }
        catch (NullPointerException ex) {
            assertEquals("baseDir", ex.getMessage());
        }
    }
    
    /**
     * <p>Tests that {@link ModuleUtil#normalisePath(String, File)} throws
     * {@link NullPointerException} if a {@code null} path is passed in.</p>
     */
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
    
    /**
     * <p>Tests that {@link ModuleUtil#normalisePath(String, File)} converts
     * an empty string as a path to '.' that necessarily points to
     * the base directory.</p>
     */
    public void testNormalisePath_BaseDirIsCurrentDir_EmptyPath()
    {
        assertEquals(".", ModuleUtil.normalisePath("", new File("")));
    }
    
    /**
     * <p>Tests that {@link ModuleUtil#normalisePath(String, File)} normalises
     * a direct directory within the base directory to itself (without the
     * tailing separator).</p>
     */
    public void testNormalisePath_BaseDirIsCurrentDir_DirectChildPathInNormalisedForm()
    {
        assertEquals("foo", ModuleUtil.normalisePath("foo", new File("")));
    }
    
    /**
     * <p>Tests that {@link ModuleUtil#normalisePath(String, File)} normalises
     * a direct directory with tailing path separator within the base directory
     * to itself but without the tailing separator.</p>
     */
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
    
    public void testNormalisePath_BaseDirIsNotCurrentDir_PathGoesOutsideBaseDirAndPartiallyGoesBack_BackAndForce()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("..", ModuleUtil.normalisePath("../../hello/universe/..", baseDir));
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
    
    public void testNormalisePath_AbsolutePath_PointsToBaseDir_DirectMatch()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals(".", ModuleUtil.normalisePath("/hello/world", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToBaseDir_DirectMatch_WithSeparator()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals(".", ModuleUtil.normalisePath("/hello/world", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToBaseDir_GoingBackAndForth()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals(".", ModuleUtil.normalisePath("/hello/../../hello/world/foo/..", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToParentDir()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("..", ModuleUtil.normalisePath("/hello", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToRoot()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("../..", ModuleUtil.normalisePath("/", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToSubDir()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("foo", ModuleUtil.normalisePath("/hello/world/foo", baseDir));
    }
    
    public void testNormalisePath_AbsolutePath_PointsToSubSubDir()
    {
        final File baseDir = new File("/hello/world/");
        
        assertEquals("foo/bar", ModuleUtil.normalisePath("/hello/world/foo/./bar/baz/..", baseDir));
    }
    
    public void testNormalisePath_NonNormalisedRelativeBaseDir_RelativePath()
    {
        final File baseDir = new File("hello/./../world");
        
        assertEquals("bar", ModuleUtil.normalisePath("foo/../bar/baz/..", baseDir));
    }
    
    public void testNormalisePath_NonNormalisedAbsoluteBaseDir_RelativePath()
    {
        final File baseDir = new File("/hello/./world");
        
        assertEquals("foo/bar", ModuleUtil.normalisePath("/hello/world/foo/./bar/baz/..", baseDir));
    }
    
    public void testNormalisePath_NonNormalisedAbsoluteBaseDirThatGoesBeyondRoot()
    {
        final File baseDir = new File("/hello/./../../world");
        
        assertEquals("foo/bar", ModuleUtil.normalisePath("/world/foo/./bar/baz/..", baseDir));
    }
    
    public void testNormalisePath_NonNormalisedAbsoluteBaseDir_AbsolutePath()
    {
        final File baseDir = new File("/hello/./world");
        
        assertEquals("foo/bar", ModuleUtil.normalisePath("/hello/world/foo/./bar/baz/..", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsRoot()
    {
        final File baseDir = new File("/");
        
        assertEquals("baz", ModuleUtil.normalisePath("foo/bar/../../../baz", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsRoot_PathGoesBeyondRootFromStart()
    {
        final File baseDir = new File("/");
        
        assertEquals("foo/baz", ModuleUtil.normalisePath("../../../foo/bar/../baz", baseDir));
    }
    
    public void testNormalisePath_BaseDirIsRoot_WholePathGoesBeyondRoot()
    {
        final File baseDir = new File("/");
        
        assertEquals(".", ModuleUtil.normalisePath("../../..", baseDir));
    }
    
    public void testNormalisePath_BaseDirEndsWithGoToParentElementsAndGoesBeyondRoot()
    {
        final File baseDir = new File("/hello/world/../../../");
        
        assertEquals("baz", ModuleUtil.normalisePath("foo/bar/../../../baz", baseDir));
    }
    
    public void testNormalisePath_PathGoesUpAndDownAndDoesNotRepeatBaseDir()
    {
        final File baseDir = new File("/hello/world");
        
        assertEquals("../../foo/bar", ModuleUtil.normalisePath("../../foo/bar", baseDir));
    }
    
    public void testNormalisePath_PathGoesUpAndDownAndDoesNotRepeatBaseDir_NamesMatchAtSomeLevel()
    {
        final File baseDir = new File("/hello/world/baz");
        
        assertEquals("../../../foo/world/baz", ModuleUtil.normalisePath("../../../foo/world/../world/baz", baseDir));
    }
}
