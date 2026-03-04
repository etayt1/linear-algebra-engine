package scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

public class TiredExecutorTest {
    // @Test
    // void submitAll_runsAllTasks() throws InterruptedException {
    //     int numWorkers = 3;
    //     int numTasks = 10;

    //     TiredExecutor executor = new TiredExecutor(numWorkers);

    //     AtomicInteger counter = new AtomicInteger(0);
    //     List<Runnable> tasks = new ArrayList<>();

    //     for (int i = 0; i < numTasks; i++) {
    //         tasks.add(counter::incrementAndGet);
    //     }  

    //     executor.submitAll(tasks);

    //     assertEquals(numTasks, counter.get(),"All tasks should have been executed");
    //     //System.out.println(executor.getWorkerReport());
        
    //     executor.shutdown();
    // }

    private TiredExecutor executor;

    @BeforeEach
    void setUp() {
        // start pool with 2 threads
        executor = new TiredExecutor(2);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // kill threads or jvm hang forever
        executor.shutdown();
    }

    @Test
    void testSubmitAllBlocks() {
        int taskCount = 20;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        // prepare 20 tasks. each sleep a bit
        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(50); // simulate hard work
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                   // ignore
                }
            });
        }

        // execute. current thread must freeze here
        executor.submitAll(tasks);

        // if code reach here, all must be done
        // if fail, barrier is leaky
        assertEquals(taskCount, counter.get(), "barrier broken. executor returned too fast.");
    }

    @Test
    void testLoadBalancingStress() {
        // pool small, 3 threads
        TiredExecutor executor = new TiredExecutor(3);
        AtomicInteger counter = new AtomicInteger(0);
        int totalTasks = 1000;

        List<Runnable> tasks = new ArrayList<>();
        // make 1000 tiny jobs
        for (int i = 0; i < totalTasks; i++) {
            tasks.add(counter::incrementAndGet);
        }

        // send all and block wait
        executor.submitAll(tasks);

        // check if all ran
        assertEquals(totalTasks, counter.get(), "some tasks lost");
        
        // kill threads safe
        try {
             executor.shutdown(); 
        } catch (InterruptedException e) {}
    }

    @Test
    void testShutdownLifecycle() {
        // verify stop not hang
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            
            // 3 threads pool
            TiredExecutor exec = new TiredExecutor(3);
            
            // submit dummy tasks
            for (int i = 0; i < 10; i++) {
                exec.submit(() -> {
                    // do nothing
                    Math.sin(10); 
                });
            }

            // stop and join threads
            exec.shutdown();
        });
    }
}
