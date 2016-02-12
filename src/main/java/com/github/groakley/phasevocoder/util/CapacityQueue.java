/*
 * Copyright (c) 2016 Grant Oakley
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.groakley.phasevocoder.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CapacityQueue<E> extends LinkedList<E> {

  private static final long serialVersionUID = 1L;

  private int myCapacity;

  public CapacityQueue(int capacity) {
    setCapacity(capacity);
  }

  public synchronized List<E> setCapacity(int capacity) {
    myCapacity = capacity;
    if (myCapacity < 0) {
      throw new RuntimeException("Queue capcity must be greater than or equal to 0.");
    }
    List<E> excess = new LinkedList<E>();
    while (size() > myCapacity) {
      excess.add(super.remove());
    }
    return excess;
  }

  public synchronized int getCapacity() {
    return myCapacity;
  }

  @Override
  public synchronized boolean add(E o) {
    super.add(o);
    while (size() > myCapacity) {
      super.remove();
    }
    return true;
  }

  @Override
  public synchronized boolean offer(E o) {
    while (size() > myCapacity - 1 && size() >= 0) {
      super.remove();
    }
    return super.offer(o);
  }

  @Override
  public synchronized boolean addAll(Collection<? extends E> c) {
    while (size() > myCapacity - c.size() && size() >= 0) {
      super.remove();
    }
    return super.addAll(c);
  }
}
