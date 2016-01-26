package com.sharingapples.threading;

/**
 * The pool thread implementation
 *
 * Created by ranjan on 1/25/16.
 */
final class Worker extends Thread {

  private final ThreadPool pool;          // The pool to which this worker belongs

  private int taskCount;         // The number of tasks that this worker has done
  private int errorCount;        // The number of tasks on which error has occurred
  private Throwable lastError;   // The last error that occurred while running a task
  private Task currentTask;      // The current task being run

  Worker(ThreadPool pool) {
    this.pool = pool;
    this.setDaemon(false);
  }

  @Override
  public void run() {
    do {
      try {
        synchronized (pool) {
          // See if there is a task to work on
          this.currentTask = pool.take();

          while (this.currentTask == null) {
            // no task, so we will wait
            pool.waitingThreads += 1;
            pool.wait();
            pool.waitingThreads -= 1;
            this.currentTask = pool.take();
          }
        }

      } catch (InterruptedException e) {
        // Look like we are stopping the pool, the thread has been interrupted
        // First update the status
        synchronized (pool) {
          pool.waitingThreads -= 1;
        }

        // And exit
        break;
      }

      try {
        // run the task handling any exception that may occur
        this.currentTask.execute();
        synchronized (pool) {
          this.taskCount += 1;
        }
      } catch(RuntimeException e) {
        // if there was a run time exception, we simply ignore the task, but keep a log
        System.err.println("ERROR " + this.currentTask + " " + e.getMessage());
        e.printStackTrace();
        synchronized (pool) {
          errorCount += 1;
          this.lastError = e;
        }
      } catch(Exception e) {
        System.err.println("ERROR-WILL-RETRY " + this.currentTask + " " + e.getMessage());
        e.printStackTrace();
        synchronized (pool) {
          this.errorCount += 1;
          this.lastError = e;
        }
        // We will put this task in the retry list, for execution at the end
        pool.push(this.currentTask);
      }

    } while(true);
  }

  int getErrorCount() {
    return errorCount;
  }

  String getLastError() {
    return lastError == null ? "-" : lastError.getMessage();
  }

  int getTaskCount() {
    return taskCount;
  }

  String getCurrentTask() {
    return currentTask == null ? "-" : currentTask.toString();
  }
}
