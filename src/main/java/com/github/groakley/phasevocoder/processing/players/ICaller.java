package com.github.groakley.phasevocoder.processing.players;

import com.github.groakley.phasevocoder.processing.operators.ICallee;

public interface ICaller extends Runnable {

  public void answer(ICallee callee, int jobID, float[] reply);

}
