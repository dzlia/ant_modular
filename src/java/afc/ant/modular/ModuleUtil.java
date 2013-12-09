/* Copyright (c) 2013, Dźmitry Laŭčuk
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
package afc.ant.modular;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;

/**
 * <p>Various utilities that are used primarily to read module meta information
 * by Ant tasks that are used in targets invoked for a specific module.</p>
 *
 * @author D&#378;mitry La&#365;&#269;uk
 */
public class ModuleUtil
{
    // prohibits having instances of ModuleUtil
    private ModuleUtil()
    {
    }
    
    /**
     * <p>Returns {@code true} if the given object is an instance of {@link Module}
     * regardless of what class loader this class is loaded by. {@code null} is considered
     * as not an instance of {@code Module}.</p>
     * 
     * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
     * new types by a new class loader. {@link CallTargetForModules} executes the target configured
     * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
     * anonymous class loader then it is loaded each time the target is invoked for another module.
     * Therefore, the JVM considers the class of the module objects passed by
     * {@code CallTargetForModules} and the class {@code Module} available in this child project
     * as different types. This function handles {@code Module} objects that are loaded by
     * an arbitrary class loader.</p>
     * 
     * @param object the object to be tested.
     * 
     * @return {@code true} if <em>object</em> is non-{@code null} and its class
     *      is {@link Module}; {@code false} is returned otherwise.
     * 
     * @see #getPath(Object)
     * @see #getDependencies(Object)
     * @see #getAttributes(Object)
     */
    public static boolean isModule(final Object object)
    {
        if (object == null) {
            return false;
        }
        return object.getClass().getName().equals(Module.class.getName());
    }
    
    /**
     * <p>Works as an equivalent of {@link Module#getPath() getPath()} invoked for
     * <em>module</em> if the given object's class is {@link Module} regardless of
     * what class loader loaded it.</p>
     * 
     * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
     * new types by a new class loader. {@link CallTargetForModules} executes the target configured
     * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
     * anonymous class loader then it is loaded each time the target is invoked for another module.
     * Therefore, the JVM considers the class of the module objects passed by
     * {@code CallTargetForModules} and the class {@code Module} available in this child project
     * as different types. This function handles {@code Module} objects that are loaded by
     * an arbitrary class loader.</p>
     * 
     * @param module the module object. It must be non-{@code null}.
     * 
     * @return the same result as if {@code module.getPath()} were invoked.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
     * @throws ClassCastException if <em>module</em>'s property {@code path} is not
     *      an instance of {@link String}.
     * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
     * 
     * @see #isModule(Object)
     * @see #getDependencies(Object)
     * @see #getAttributes(Object)
     */
    public static String getPath(final Object module)
    {
        validateModule(module);
        return (String) callFunction(module, "getPath");
    }
    
    /**
     * <p>Works as an equivalent of {@link Module#getDependencies() getDependencies()} invoked for
     * <em>module</em> if the given object's class is {@link Module} regardless of
     * what class loader loaded it.</p>
     * 
     * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
     * new types by a new class loader. {@link CallTargetForModules} executes the target configured
     * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
     * anonymous class loader then it is loaded each time the target is invoked for another module.
     * Therefore, the JVM considers the class of the module objects passed by
     * {@code CallTargetForModules} and the class {@code Module} available in this child project
     * as different types. This function handles {@code Module} objects that are loaded by
     * an arbitrary class loader.</p>
     * 
     * @param module the module object. It must be non-{@code null}.
     * 
     * @return the same result as if {@code module.getDependencies()} were invoked.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
     * @throws ClassCastException if <em>module</em>'s property {@code dependencies} is not
     *      an instance of {@link Set}.
     * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
     * 
     * @see #isModule(Object)
     * @see #getPath(Object)
     * @see #getAttributes(Object)
     */
    public static Set<?> getDependencies(final Object module)
    {
        validateModule(module);
        return (Set<?>) callFunction(module, "getDependencies");
    }
    
    /**
     * <p>Works as an equivalent of {@link Module#getAttributes() getAttributes()} invoked for
     * <em>module</em> if the given object's class is {@link Module} regardless of
     * what class loader loaded it.</p>
     * 
     * <p>Rationale: by default Ant's {@code <typedef/>} and {@code <taskdef/>} tasks load
     * new types by a new class loader. {@link CallTargetForModules} executes the target configured
     * in a new Ant project for each module. If the Ant Modular tag library is loaded by an
     * anonymous class loader then it is loaded each time the target is invoked for another module.
     * Therefore, the JVM considers the class of the module objects passed by
     * {@code CallTargetForModules} and the class {@code Module} available in this child project
     * as different types. This function handles {@code Module} objects that are loaded by
     * an arbitrary class loader.</p>
     * 
     * <p>Note that if an attribute value's class is not in the class path of the current
     * class loader then an attempt to use it could still cause {@link ClassCastException}
     * exceptions.</p>
     * 
     * @param module the module object. It must be non-{@code null}.
     * 
     * @return the same result as if {@code module.getAttributes()} were invoked.
     * 
     * @throws NullPointerException if <em>module</em> is {@code null}.
     * @throws IllegalArgumentException if <em>module</em>'s class is not {@code Module}.
     * @throws ClassCastException if <em>module</em>'s property {@code attributes} is not
     *      an instance of {@link Map}.
     * @throws BuildException if invocation of {@code module.getPath()} did not succeed.
     * 
     * @see #isModule(Object)
     * @see #getPath(Object)
     * @see #getDependencies(Object)
     */
    public static Map<String, Object> getAttributes(final Object module)
    {
        validateModule(module);
        @SuppressWarnings("unchecked")
        final Map<String, Object> attribs = (Map<String, Object>) callFunction(module, "getAttributes");
        return attribs;
    }
    
    private static void validateModule(final Object module)
    {
        if (module == null) {
            throw new NullPointerException("module");
        }
        final String className = module.getClass().getName();
        if (!className.equals(Module.class.getName())) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unsupported module type. Expected: ''{0}'', was: ''{1}''.",
                    Module.class.getName(), className));
        }
    }
    
    private static Object callFunction(final Object module, final String functionName)
    {
        try {
            return module.getClass().getDeclaredMethod(functionName).invoke(module);
        }
        catch (IllegalAccessException ex) {
            throw new BuildException(MessageFormat.format(
                    "Unable to invoke module#{0}().", functionName));
        }
        catch (NoSuchMethodException ex) {
            throw new BuildException(MessageFormat.format(
                    "The module instance does not have the function ''{0}()''.", functionName));
        }
        catch (InvocationTargetException ex) {
            throw new BuildException(MessageFormat.format(
                    "module#{0}() has thrown an exception.", functionName), ex.getCause());
        }
    }
    
    /**
     * <p>Normalises a given path relative to a given base directory as
     * {@link #normalisePath(String, File, boolean)} does but necessarily with <em>no</em> path
     * letter case normalisation.</p>
     * 
     * @param path the path to normalise. It must be non-{@code null}. It does not need
     *      to point to an existing file.
     * @param baseDir the base directory to normalise the path against. It must be
     *      non-{@code null}. It does not need to point to an existing directory. If it
     *      points to an existing file then this file can be a non-directory. Only the path of
     *      <em>baseDir</em> is used for normalisation.
     *      
     * @return the normalised path. It is necessarily non-{@code null}.
     * 
     * @throws NullPointerException if either <em>path</em> or <em>baseDir</em> is {@code null}.
     */
    public static String normalisePath(final String path, final File baseDir)
    {
        return normalisePath(path, baseDir, false);
    }
    
    // TODO document me.
    // TODO make this code readable.
    // TODO improve performance.
    public static String normalisePath(final String path, final File baseDir, final boolean normaliseCase)
    {
        if (baseDir == null) {
            throw new NullPointerException("baseDir");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        if (path.length() == 0) {
            // path refers to baseDir.
            return ".";
        }
        
        // Adding path elements in the reverse order.
        final ArrayList<String> parts = new ArrayList<String>();
        final File pathFile = new File(path);
        for (File f = pathFile; f != null; f = f.getParentFile()) {
            parts.add(f.getName());
        }
        
        final ArrayList<String> resultParts;
        
        /* Indicates what is the depth of the current path element in the file system hierarchy
         * given that the depth of the baseDir is zero.
         */
        int depth = 0;
        int baseDirCommonCursor = 0;
        ArrayList<String> baseDirParts;
        
        if (pathFile.isAbsolute()) {
            baseDirParts = baseDirElements(baseDir);
            
            /* Initialising the destination path with necessary .. elements to reach
             * the position 'above' root directory so that the root directory is the
             * first path element to start resolving with.
             */
            final int levelsUp = baseDirParts.size();
            baseDirCommonCursor = depth = -levelsUp;
            resultParts = new ArrayList<String>(parts.size() + levelsUp);
            resultParts.addAll(Collections.nCopies(levelsUp, ".."));
        } else {
            // It is unknown if baseDirParts will be used later so leaving it non-initialised.
            baseDirParts = null;
            
            // The path is relative. baseDir is a starting point to resolve path elements against.
            baseDirCommonCursor = depth = 0;
            resultParts = new ArrayList<String>(parts.size());
        }
        
        // Going through path elements from parents to children resolving '.' and '..'.
        for (int i = parts.size() - 1; i >= 0; --i) {
            final String part = parts.get(i);
            if (part.equals(".")) {
                continue;
            } else if (part.equals("..")) {
                if (baseDirParts == null) {
                    // Lazy init.
                    baseDirParts = baseDirElements(baseDir);
                }
                
                if (depth <= 0 && -(depth - 1) == baseDirParts.size()) {
                    /* There is nothing to do since the root directory is reached and
                     * the parent of the root directory is the root directory itself.
                     * 
                     * If depth is equal to zero here then baseDir is the root directory.
                     */
                    assert depth == baseDirCommonCursor;
                    continue;
                }
                
                final int size = resultParts.size();
                if (size == 0 || resultParts.get(size - 1).equals("..")) {
                    // The current path element points to the direct or an indirect parent directory of the baseDir.
                    resultParts.add("..");
                    
                    // Moving the cursor one level up.
                    --baseDirCommonCursor;
                } else {
                    resultParts.remove(size - 1);
                }
                --depth;
            } else {
                if (depth < 0 && baseDirCommonCursor == depth) {
                    // The current sub-path points to a parent of baseDir.
                    assert baseDirParts != null;
                    
                    /* If the current path element points to a parent of baseDir then
                     * just removing the previous '..'.
                     */
                    if (part.equals(baseDirParts.get(-depth - 1))) {
                        assert resultParts.get(resultParts.size() - 1).equals("..");
                        
                        resultParts.remove(resultParts.size() - 1);
                        ++depth;
                        ++baseDirCommonCursor;
                        continue;
                    }
                }
                resultParts.add(part);
                ++depth;
            }
        }
        if (resultParts.isEmpty()) {
            // path refers to baseDir.
            return ".";
        }
        
        final String normalisedPath = join(resultParts, File.separatorChar);
        // The system default locale is used for casting to the lower case.
        return normaliseCase ? normalisedPath.toLowerCase() : normalisedPath;
    }
    
    /*
     * Normalises a given file path by removing all '.' elements and resolving
     * the '..' elements. Symbolic links are not resolved.
     * 
     * This function is used by #normalisePath(String) to normalise base directories.
     * 
     * @param baseDir the base directory to be normalised. It must be non-null.
     * 
     * @return the path elements of the normalised path in the reverse order.
     */
    private static ArrayList<String> baseDirElements(final File baseDir)
    {
        // Base directory path elements in the reverse order.
        final ArrayList<String> baseDirParts = new ArrayList<String>();
        // The number of '..' elements that still can discard some directory path elements.
        int parentDirElementCount = 0;
        File parent;
        for (File f = baseDir.isAbsolute() ? baseDir : baseDir.getAbsoluteFile(); f != null; f = parent) {
            parent = f.getParentFile();
            
            final String e = f.getName();
            if (e.equals(".")) {
                continue;
            } else if (e.equals("..")) {
                // Remembering this '..' element to discard parent paths that go before.
                ++parentDirElementCount;
            } else {
                if (parentDirElementCount > 0 && parent != null) {
                    /* The current element is not the root directory and there is
                     * a '..' element that discards it. Throwing away both the current
                     * element and the correspondent '..' element.
                     */
                    --parentDirElementCount;
                } else {
                    baseDirParts.add(e);
                }
            }
        }
        return baseDirParts;
    }
    
    /*
     * Composes a string out of given strings placing a given separator between them.
     * For example: join(["foo", "bar", "baz"], '.') produces the string "foo.bar.baz".
     * 
     * @param parts the list of the strings to be joined. It must be non-null.
     * @param separator the separator to be placed between the string elements.
     * 
     * @return the string composed. It is never null.
     */
    private static String join(final ArrayList<String> parts, final char separator)
    {
        final int size = parts.size();
        
        // Initialising destination string size with the number of separators.
        int destSize = size - 1;
        for (int i = 0; i < size; ++i) {
            destSize += parts.get(i).length();
        }
        
        // The buffer is created with the necessary capacity.
        final StringBuilder buf = new StringBuilder(destSize);
        int i = 0;
        for (final int n = size - 1; i < n; ++i) {
            buf.append(parts.get(i)).append(separator);
        }
        buf.append(parts.get(i));
        
        assert buf.length() == destSize;
        
        return buf.toString();
    }
}
