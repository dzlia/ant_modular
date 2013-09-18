package afc.ant.modular;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tools.ant.Project;

public class HangingMockCallTargetTask extends MockCallTargetTask
{
    public volatile Thread hangingThread;
    private final AtomicBoolean flag;
    private final CyclicBarrier hangBarrier;
    private final AtomicReference<Throwable> failureCause;
    
    public HangingMockCallTargetTask(final Project project, final AtomicBoolean hangWhileFlag,
            final CyclicBarrier hangBarrier, final AtomicReference<Throwable> failureCause)
    {
        super(project);
        flag = hangWhileFlag;
        this.hangBarrier = hangBarrier;
        this.failureCause = failureCause;
    }
    
    @Override
    public synchronized void execute()
    {
        super.execute();
        
        try {
            hangingThread = Thread.currentThread();
            hangBarrier.await();
            while(flag.get()) {
                wait();
            }
        }
        catch (InterruptedException ex) {
            System.out.println("exit");
            Thread.currentThread().interrupt();
            failureCause.set(ex);
            throw new IllegalStateException();
        }
        catch (BrokenBarrierException ex) {
            System.out.println("exit");
            failureCause.set(ex);
            throw new RuntimeException(ex);
        }
    }
}
