package afc.ant.modular;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import junit.framework.Assert;

public class MockProject extends Project
{
    public final ArrayList<Task> tasks = new ArrayList<Task>();
    public int tasksReturned = 0;
    
    @Override
    public Task createTask(final String taskType) throws BuildException
    {
        Assert.assertNotNull(taskType);
        Assert.assertFalse(taskType.length() == 0);
        Assert.assertTrue(tasksReturned < tasks.size());
        return tasks.get(tasksReturned++);
    }
}
