package com.sharingapples.threading;

import org.junit.Test;

import java.util.Random;

/**
 * Created by ranjan on 1/26/16.
 */
public class PoolTestCase {

  private final Random random = new Random();

  @Test
  public void testThreadPool() {
    ThreadPool pool = new ThreadPool(1000);

    // Let's put some mundane tasks that sleeps for some time
    for(int i=0; i<10000; ++i) {
      final int idx = i;
      pool.push(new Task() {
        public String toString() {
          return "Mundane-" + idx;
        }

        public void execute() {
          try {
            Thread.sleep(random.nextInt(500) + 10);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
    }

    // Start the pool
    pool.start();

    // Draw out the initial status of the pool
    System.out.println(pool.getStatus(null));

    pool.join(pool::stop, ()-> {
      // A user task run in short intervals, use this to update status
      System.out.println(pool.getStatus(null));
    });

  }
}
