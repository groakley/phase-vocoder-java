package edu.geo4.duke.processing.players;

import edu.geo4.duke.processing.operators.ICallee;

public interface ICaller {
    
    public void answer(ICallee callee, int jobID, byte[] reply);

}
