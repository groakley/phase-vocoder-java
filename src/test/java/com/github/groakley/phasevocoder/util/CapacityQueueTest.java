package com.github.groakley.phasevocoder.util;

import junit.framework.Assert;
import org.junit.Test;

public class CapacityQueueTest {

  @Test
  public void testAdd() throws Exception {
    final int cap = 4;
    CapacityQueue<Boolean> queue = new CapacityQueue<Boolean>(cap);
    queue.add(new Boolean(false));
    queue.add(new Boolean(false));
    queue.add(new Boolean(false));
    queue.add(new Boolean(false));
    queue.add(new Boolean(false));
    Assert.assertEquals(cap, queue.size());
  }

}
