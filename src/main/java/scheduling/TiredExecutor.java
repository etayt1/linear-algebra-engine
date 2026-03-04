package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new TiredThread(i + 1, 0.5 + Math.random());
            idleMinHeap.add(workers[i]);
            workers[i].start();
        }
    }

    public void submit(Runnable task) {
        try {
            TiredThread worker = idleMinHeap.take();
            inFlight.incrementAndGet();
            worker.newTask(()-> {
                try {
                    long start = System.nanoTime();
                    worker.setBusy(true);
                    worker.stopIdling();
                    task.run();
                    worker.addToTimeUsed(System.nanoTime() - start);
                    worker.resetIdleTimeStart();
                    worker.setBusy(false);
                } finally {
                    inFlight.decrementAndGet();
                    idleMinHeap.add(worker);
                    synchronized (this) {
                        notifyAll();
                    }
                }
            });
        } catch (InterruptedException e) {
            try{
                shutdown();
            } catch(InterruptedException ex) {
                throw new IllegalStateException("Executor failed to shutdown");
            }
            throw new IllegalStateException("take has failed!");
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks){
            submit(task);}
        
        synchronized (this) {
            while (inFlight.get() > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Executor was interrupted!");
                }
            }
        }
        // submit tasks one by one and wait until all finish
    }

    public void shutdown() throws InterruptedException {
        for (int i = 0; i < workers.length; i++) {
            workers[i].shutdown();
        }
        for (int i = 0; i < workers.length; i++) {
            workers[i].join();
        }
    }

    public synchronized String getWorkerReport() {
        // return readable statistics for each worker
        String workerReport = "\n=== WorkerReport ===\n";
        for (int i = 0; i < workers.length; i++)
            workerReport = workerReport.concat("  Worker " + workers[i].getWorkerId() + ":\n" + 
                                               "  - Busy: " + workers[i].isBusy() + "\n" +
                                               "  - Fatigue: " + workers[i].getFatigue() + "\n" + 
                                               "  - Time used: " + workers[i].getTimeUsed() + "\n" +
                                               "  - Time idle: " + workers[i].getTimeIdle() + "\n");
        workerReport = workerReport.concat("====================\n");
        return workerReport;
    }
}
