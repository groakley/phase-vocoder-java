package edu.geo4.duke.processing.operators;

import edu.geo4.duke.processing.players.ICaller;


/**
 * This interface describes a thread that operate on streams of bytes. Its
 * intended application is in DSP, where it is intended to provide a general
 * interface for "black-box" signal processing units.
 * 
 * @author Grant Oakley
 * 
 */
public interface ICallee extends Runnable {

    /**
     * Inputs data to be processed. If the operator's buffer is full, this
     * command blocks, and will not execute into there is room in the buffer.
     * Will not work if the thread has not been started.
     * 
     * @param caller process to report back to
     * @param jobID integer identifying the called process
     * @param data data to be processed as an array of bytes
     */
    public void answer (ICaller caller, int jobID, byte[] data);

    /**
     * Stops the thread and releases any of its resources.
     */
    public void stop ();

    /**
     * Initializes and returns a copy an instance of this class with the same
     * input parameters it was given in its constructor. This is used to get
     * copies of the initial instance for multiple audio channels.
     * 
     * @return copy of this instance as it was originally created
     */
    public ICallee getNewInstance ();

}
