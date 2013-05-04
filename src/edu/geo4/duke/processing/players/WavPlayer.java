package edu.geo4.duke.processing.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import edu.geo4.duke.processing.operators.ICallee;


public class WavPlayer extends Thread implements ICaller {

    File myInput;
    ICallee myOperator;
    private Vector<BlockingQueue<Byte>> outChannels;
    private Vector<ICallee> myChannelOperators;
    private int sampleSizeInBytes;

    public WavPlayer (String filePath, ICallee operator) {
        this(new File(filePath), operator);
    }

    public WavPlayer (File file, ICallee operator) {
        myInput = file;
        myOperator = operator;
        outChannels = new Vector<BlockingQueue<Byte>>();
        myChannelOperators = new Vector<ICallee>();
    }

    @Override
    public void start () {
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(myInput);
            AudioFormat inputFormat = audioInputStream.getFormat();
            sampleSizeInBytes = inputFormat.getSampleSizeInBits() / Byte.SIZE;
            int bytesPerFrame = inputFormat.getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                // some audio formats may have unspecified frame size
                // in that case we may read any amount of bytes
                bytesPerFrame = 1;
            }
            // Set an arbitrary buffer size of 1024 frames.
            int numBytes = 1024 * bytesPerFrame;
            // int numBytes = 2048 * bytesPerFrame;
            byte[] audioBytes = new byte[numBytes];

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, inputFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(inputFormat);
            sourceDataLine.start();

            for (int i = 0; i < inputFormat.getChannels(); i++) {
                outChannels.add(new LinkedBlockingQueue<Byte>(16384 * sampleSizeInBytes));

            }

            for (int i = 0; i < inputFormat.getChannels(); i++) {
                ICallee op;
                if (i > 0) {
                    op = myOperator.getNewInstance();
                }
                else {
                    op = myOperator;
                }
                myChannelOperators.add(op);
                new Thread(op).start();
            }

            // Try to read numBytes bytes from the file.
            boolean readThisIteration = true;
            int readResult = 0;
            while (readResult != -1) {
                if (readThisIteration) {
                    readResult = audioInputStream.read(audioBytes);
                    ArrayList<byte[]> channels = new ArrayList<byte[]>();
                    for (int i = 0; i < inputFormat.getChannels(); i++) {
                        channels.add(extractChannel(audioBytes, inputFormat.getSampleSizeInBits(),
                                                    inputFormat.getChannels(), i));
                    }
                    for (int i = channels.size() - 1; i >= 0; i--) {
                        byte[] data = channels.get(i);
                        myChannelOperators.get(i).call(this, i,
                                                       byteToFloat(data, sampleSizeInBytes));
                    }
                }

                // Check if enough data is in outChannels
                int outSegmentLength = 1024 * (sampleSizeInBytes);
                boolean enoughData = true;
                for (BlockingQueue<Byte> bq : outChannels) {
                    if (bq.size() < outSegmentLength) {
                        enoughData = false;
                    }
                }

                if (enoughData) {
                    ArrayList<byte[]> outputSegments = new ArrayList<byte[]>();
                    for (BlockingQueue<Byte> bq : outChannels) {
                        byte[] segment = new byte[outSegmentLength];
                        for (int i = 0; i < segment.length; i++) {
                            segment[i] = bq.take();
                        }
                        outputSegments.add(segment);
                    }
                    byte[] interleavedChannels =
                            interleaveChannels(outputSegments, inputFormat.getSampleSizeInBits());
                    sourceDataLine.write(interleavedChannels, 0, interleavedChannels.length);
                }

                readThisIteration = true;
                for (ICallee callee : myChannelOperators) {
                    if (callee.remainingCapacity() < (audioBytes.length / myChannelOperators.size()) /
                                                     sampleSizeInBytes) {
                        readThisIteration = false;
                    }
                }

            }
        }
        catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            for (ICallee op : myChannelOperators) {
                op.stop();
            }
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            sourceDataLine = null;
        }
        System.exit(0);
    }

    @Override
    public void answer (ICallee callee, int jobID, float[] reply) {
        BlockingQueue<Byte> queue = outChannels.get(jobID);
        try {
            for (Byte b : floatToByte(reply, sampleSizeInBytes)) {
                queue.put(b);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static float[] byteToFloat (byte[] bytes, int bytesPerSample) {
        float[] output = new float[bytes.length / bytesPerSample];
        for (int i = 0; i < output.length; i++) {
            byte[] frames = new byte[8];
            for (int j = 0; j < bytesPerSample; j++) {
                frames[j] = bytes[bytesPerSample * i + j];
            }
            long composite =
                    (long) ((frames[7] << 56) | (frames[6] << 48) | frames[5] << 40 |
                            frames[4] << 32 | frames[3] << 24 | frames[2] << 16 | frames[1] << 8 | (frames[0] & 0xFF));
            float fComposite = (float) composite;
            fComposite = (float) (fComposite / Math.pow(2, bytesPerSample * 8 - 1));
            output[i] = fComposite;
        }
        return output;
    }

    private static byte[] floatToByte (float[] floats, int bytesPerSample) {
        byte[] output = new byte[floats.length * bytesPerSample];
        for (int i = 0; i < output.length - bytesPerSample + 1; i = i + bytesPerSample) {
            float clipped = Math.max(-1.0f, floats[i / bytesPerSample]);
            clipped = Math.min(1.0f, clipped);
            long composite = (long) (clipped * Math.pow(2, bytesPerSample * 8 - 1));
            for (int j = 0; j < bytesPerSample; j++) {
                output[i + j] = (byte) ((composite >> 8 * j) & 0xFF);
            }
        }
        return output;
    }

    /**
     * Returns the bytes from a Wav format byte stream corresponding to the
     * specified channel number.
     * 
     * @param sampleSizeInBits Number of bits in each sample of the input stream
     * @param numChannels Total number of channels present in the input stream
     *        data
     * @param outChannelNum Desired output channel (0-indexed)
     * @return The byte array of just a single channel from a wav file with
     *         multiple channels.
     */
    private static byte[] extractChannel (byte[] data, int sampleSizeInBits, int numChannels,
                                          int outChannelNum) {
        byte[] output = new byte[data.length / numChannels];
        int numBytesPerSample = sampleSizeInBits / Byte.SIZE;
        int bytePosition = 0;
        int outIndex = 0;
        for (int i = 0; i < data.length; i++) {
            if (bytePosition >= outChannelNum * numBytesPerSample &&
                bytePosition < (outChannelNum + numChannels - 1) * numBytesPerSample) {
                output[outIndex] = data[i];
                outIndex++;
            }
            bytePosition++;
            if (bytePosition == numChannels * numBytesPerSample) {
                bytePosition = 0;
            }
        }
        return output;
    }

    private static byte[] interleaveChannels (ArrayList<byte[]> channels, int sampleSizeInBits) {
        // Check if channels are the same length
        int first = channels.get(0).length;
        for (byte[] chan : channels) {
            if (chan.length != first) { throw new RuntimeException("Channel lengths were different"); }
        }

        // Interleave channels
        byte[] output = new byte[first * channels.size()];
        int numBytesPerSample = sampleSizeInBits / Byte.SIZE;
        int outIdx = 0;
        int chanIdx = 0;
        while (chanIdx + numBytesPerSample <= first) {
            for (int i = 0; i < channels.size(); i++) {
                for (int j = 0; j < numBytesPerSample; j++) {
                    output[outIdx + j] = channels.get(i)[chanIdx + j];
                }
                outIdx += numBytesPerSample;
            }
            chanIdx += numBytesPerSample;
        }
        return output;
    }

}
