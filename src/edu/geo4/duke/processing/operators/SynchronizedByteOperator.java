package edu.geo4.duke.processing.operators;

/**
 * This interface describes a thread that operate on streams of bytes. Its
 * intended application is in DSP, where it is intended to provide a general
 * interface for "black-box" signal processing units.
 * 
 * @author Grant Oakley
 * 
 */
public interface SynchronizedByteOperator extends Runnable {

    /**
     * Inputs data to be processed. If the operator's buffer is full, this
     * command blocks, and will not execute into there is room in the buffer.
     * 
     * @param data data to be processed as an array of bytes
     */
    public void put (byte[] data) throws InterruptedException;

    /**
     * Retrieves the data input by the put method post-processing. If there is
     * still data that has been input to the operator that has not been
     * processed, this operation blocks until the output has been generated.
     * 
     * @param size size of the output byte array. If this exceeds the length of
     *        the number of bytes left to be output, the remainder of the output
     *        array is filled with zeros.
     * @return data that has been processed by the operator
     */
    public byte[] take (int size) throws InterruptedException;

    /**
     * Finds the number of bytes that can currently be taken from the output
     * buffer.
     * 
     * @return an integer that is the number of bytes than can be taken from the
     *         operator
     */
    public int bytesRemaining ();

    /**
     * Call to stop the thread and drain any resources.
     */
    public void close ();

}
