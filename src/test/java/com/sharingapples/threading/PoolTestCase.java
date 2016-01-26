package com.sharingapples.threading;

import com.sharingapples.logging.StatusBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

/**
 * Created by ranjan on 1/26/16.
 */
public class PoolTestCase {

  private final Random random = new Random();
  private StatusBuilder statusBuilder;
  @Test
  public void testThreadPool() throws IOException {
    ThreadPool pool = new ThreadPool(100);
    statusBuilder = new StatusBuilder(pool);

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
    statusBuilder.dump();


    pool.join(pool::stop, statusBuilder::dump);
  }
}
