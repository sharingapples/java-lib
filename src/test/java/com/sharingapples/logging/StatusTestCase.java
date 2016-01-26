package com.sharingapples.logging;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by ranjan on 1/26/16.
 */
public class StatusTestCase {
  @Test
  public void testStatus() throws IOException, InterruptedException {
    StatusBuilder statusBuilder = new StatusBuilder(status -> {
      status.append("Current time is ");
      status.append(System.currentTimeMillis());
    });

    for(int i=0; i<10; ++i) {
      statusBuilder.dump();
      Thread.sleep(1000);
    }
  }
}
