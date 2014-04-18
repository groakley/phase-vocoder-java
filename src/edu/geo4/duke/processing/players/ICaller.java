package edu.geo4.duke.processing.players;

import edu.geo4.duke.processing.operators.ICallee;

public interface ICaller extends Runnable {

  public void answer(ICallee callee, int jobID, float[] reply);

}
