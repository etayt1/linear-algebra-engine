package scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TiredThreadTest {
    @Test
    void testOverflowRejection() throws InterruptedException {
        // worker with small fatigue
        TiredThread worker = new TiredThread(1, 1.0);
        worker.start();

        // 1. task A: sleep long, force worker busy
        worker.newTask(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        });
        
        // wait bit, let worker take task A
        Thread.sleep(50);

        // 2. task B: fill queue (size 1)
        worker.newTask(() -> {});

        // 3. task C: overflow
        // queue full + worker busy = crash
        assertThrows(IllegalStateException.class, () -> {
            worker.newTask(() -> {});
        });

        worker.shutdown();
    }

    @Test
    void testFatigueCalculation() {
        // factor 2.5
        TiredThread worker = new TiredThread(1, 2.5);
        
        // fake work time 100ns
        worker.addToTimeUsed(100);

        // 100 * 2.5 = 250
        assertEquals(250.0, worker.getFatigue(), 0.001);
    }
}
