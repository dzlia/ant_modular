package afc.ant.modular;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.Assert;

public class TestUtil
{
    public static <T> HashSet<T> set(final T... elements)
    {
        return new HashSet<T>(Arrays.asList(elements));
    }
    
    public static HashMap<String, Object> map(final Object... parts)
    {
        Assert.assertTrue(parts.length % 2 == 0);
        final HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < parts.length; i+=2) {
            map.put((String) parts[i], parts[i+1]);
        }
        return map;
    }
}
