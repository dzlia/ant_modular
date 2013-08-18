package afc.ant.modular;

import java.util.HashMap;

import junit.framework.Assert;

public class TestUtil
{
    public static HashMap<String, Object> map(Object... parts)
    {
        Assert.assertTrue(parts.length % 2 == 0);
        final HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < parts.length; i+=2) {
            map.put((String) parts[i], parts[i+1]);
        }
        return map;
    }
}
