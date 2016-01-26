package com.sharingapples.threading;

/**
 * An exception thrown when a task is executing
 *
 * Created by ranjan on 1/26/16.
 */
public class TaskException extends Exception {
  private final Task task;

  public TaskException(Task task, Throwable cause) {
    super("Error executing task - " + task, cause);

    this.task = task;
  }

  public Task getTask() {
    return task;
  }
}
