package edu.geo4.duke.processing.operators;

/**
 * An interface defining any LTI DSP system. It needs only take an input array
 * of bytes and output another array of bytes.
 * 
 * @author Grant Oakley
 * 
 */
public interface ByteOperator {

    /**
     * Process the incoming array of bytes, and then output a new array of
     * bytes.
     * 
     * @param data data to be processed
     * @return processed data in a new array (does not operate in place)
     */
    public byte[] process (byte[] data);

}
