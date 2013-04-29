package edu.geo4.duke.processing.operators;

public class PassThroughOperator implements ByteOperator {

    @Override
    public byte[] process (byte[] data) {
        byte[] output = data.clone();
        return output;
    }

}
