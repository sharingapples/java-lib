package com.sharingapples.threading;

/**
 * Task to be implemented
 *
 * Created by ranjan on 1/25/16.
 */
public interface Task {
  /**
   * Execute the task
   *
   * @throws TaskException
   */
  void execute() throws TaskException;
}
