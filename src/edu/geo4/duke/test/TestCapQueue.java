package edu.geo4.duke.test;

import junit.framework.Assert;
import org.junit.Test;

import edu.geo4.duke.util.CapacityQueue;


public class TestCapQueue {

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
