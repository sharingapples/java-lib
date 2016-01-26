package com.sharingapples.threading;

import com.sharingapples.logging.StatusProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * A ThreadPool provides a mechanism to execute a number of tasks using
 * a fixed set of workers.
 *
 * Created by ranjan on 1/25/16.
 */
public class ThreadPool implements StatusProvider {

  private final List<Worker> workers;       // The threads available on this pool
  private final Queue<Task> tasks;  // The tasks to be performed on this pool

  private long startTime = 0;
  private long stopTime = -1;

  // The number of threads waiting for tasks, this is updated from {@link Worker} class
  int waitingThreads = 0;

  private Task idleTask;            // The task run when the pool becomes idle
  private Task userTask;            // The task that is run periodically on the starter thread
  private int userTimeout = 1000;  // the interval at which the starter thread wakes up to check

  /**
   * Create a new pool of workers with the given size
   *
   * @param size The number of threads to be available
   */
  public ThreadPool(int size) {
    // using  a simple list for storing all the tasks
    tasks = new LinkedList<>();

    List<Worker> workers = new ArrayList<>(size);
    for (int i = 0; i < size; ++i) {
      workers.add(new Worker(this));
    }
    this.workers = Collections.unmodifiableList(workers);
  }

  /**
   * Push a task to be worked out
   *
   * @param task The task to be executed
   */
  public synchronized void push(Task task) {
    assert (task != null);

    // Add a new task
    tasks.add(task);

    // Notify all the waiting threads, if this were the only task on the queue
    if (tasks.size() == 1) {
      this.notify();
    }
  }

  synchronized Task take() throws InterruptedException {
    return tasks.poll();
  }

  /**
   * Get the number of tasks in the queue that need to be executed
   *
   * @return The number of tasks waiting to be executed
   */
  public synchronized int getTasksInQueue() {
    return tasks.size();
  }

  /**
   * Find out if all the workers are done with all the tasks and there
   * aren't any more task to run
   *
   * @return {@code true} if the pool is idle without any tasks
   */
  public synchronized boolean isIdle() {
    return getTasksInQueue() == 0 && waitingThreads == workers.size();
  }

  /**
   * Stop executing all the threads and make sure all the threads have
   * come to a halt
   */
  public void stop() throws InvalidStateException {
    synchronized (this) {
      if (stopTime != 0) {
        throw new InvalidStateException("The pool has not started yet to stop");
      }

      // dump out all the tasks
      if (tasks.size() > 0) {
        System.out.println("Stopping with " + tasks.size() + " tasks in queue");
      }
    }

    // interrupt all the worker threads
    workers.forEach(Worker::interrupt);

    // wait unit all the threads come to a halt
    // if there's an error, we just move on
    workers.stream().filter(Thread::isAlive).forEach(worker -> {
      try {
        worker.join();
      } catch (InterruptedException e) {
        // if there's an error, we just move on
      }
    });

    synchronized (this) {
      stopTime = System.currentTimeMillis();
    }
  }

  public synchronized void setIdleTask(Task task) {
    this.idleTask = task;
  }

  public synchronized void setUserTask(Task task) {
    this.userTask = task;
  }

  public synchronized void setUserTask(Task task, int timeout) {
    this.userTask = task;
    this.userTimeout = timeout;
  }

  public synchronized void start() throws InvalidStateException {

    // Check if the pool is already running
    if (stopTime == 0) {
      throw new InvalidStateException("The thread is already running");
    }

    startTime = System.currentTimeMillis();
    stopTime = 0;


    // Start the thread pool
    workers.forEach(Worker::start);
  }

  public void join(Task idleTask) {
    this.setIdleTask(idleTask);
    join();
  }

  public void join(Task idleTask, Task userTask) {
    this.setIdleTask(idleTask);
    this.setUserTask(userTask);
    join();
  }

  public void join(Task idleTask, Task userTask, int userTimeout) {
    this.setIdleTask(idleTask);
    this.setUserTask(userTask, userTimeout);
    join();
  }

  public void join(Task userTask, int userTimeout) {
    this.setUserTask(userTask, userTimeout);
    join();
  }

  public void join() {

    boolean stopped = false;

    synchronized (this) {
      // If the thread is not started, it is not possible to join it
      stopped = (this.stopTime != 0);
    }

    // Wait on all the threads
    while(!stopped) {
      try {
        Thread.sleep(userTimeout);
      } catch (InterruptedException e) {
        System.err.println("Error while sleeping on starter thread");
        e.printStackTrace();
      }

      // In case the pool is idle, execute a idle task available
      if(isIdle()) {
        Task onIdle;
        synchronized (this) {
          onIdle = this.idleTask;
        }

        // Let's run a idle task
        if (onIdle != null) {
          try {
            onIdle.execute();
          } catch(TaskException | RuntimeException e) {
            System.err.println("Error while running idle task " + idleTask);
            e.printStackTrace();
          }
        }
      }

      Task userTask;
      synchronized (this) {
        userTask = this.userTask;
      }

      if (userTask != null) {
        try {
          userTask.execute();
        } catch(TaskException | RuntimeException e) {
          System.err.println("Error while running user task " + userTask);
          e.printStackTrace();
        }
      }

      // Check if the thread has been stopped
      synchronized (this) {
        stopped = this.stopTime != 0;
      }
    }
  }

  @Override
  public synchronized void updateStatus(StringBuilder status) {
    long runTime = (stopTime == 0 ? System.currentTimeMillis() : stopTime) - startTime;
    status.append("Total run time: ");
    status.append(runTime/1000.0);
    status.append(" seconds.     Status: ");
    //status.append(stopTime == 0 ? "RUNNING" : "STOPPED");
    if (stopTime == 0) {
      status.append("RUNNING   Activity: ");
      status.append(workers.size() - waitingThreads);
      status.append('/');
      status.append(workers.size());
      status.append("   Queue: ");
      status.append(tasks.size());
      status.append(" tasks");
    } else {
      status.append("STOPPED");
    }
    status.append('\n');

    status.append(String.format("%-14s | %5s | %5s | %-20s | %s\n", "Thread", "Tasks", "Errs", "Current", "Last Error"));
    status.append("====================================================================================================\n");
    for(Worker worker:workers) {
      status.append(String.format("%-14s | %5d | %5d | %-20s | %s\n",
              worker.getName(),
              worker.getTaskCount(),
              worker.getErrorCount(),
              left(worker.getCurrentTask(), 20),
              worker.getLastError()));
    }
    status.append("====================================================================================================\n");
  }

  private static String left(String str, int length) {
    if (str.length() > length) {
      return str.substring(0, length);
    } else {
      return str;
    }
  }
}
