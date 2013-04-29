package edu.geo4.duke.processing.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import edu.geo4.duke.processing.operators.ByteOperator;


public class WavPlayer extends Thread {

    File myInput;
    ByteOperator myOperator;

    public WavPlayer (String filePath, ByteOperator operator) {
        myInput = new File(filePath);
        myOperator = operator;
    }

    @Override
    public void start () {
        int totalFramesRead = 0;
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine = null;

        try {
            audioInputStream = AudioSystem.getAudioInputStream(myInput);
            AudioFormat inputFormat = audioInputStream.getFormat();
            int bytesPerFrame = inputFormat.getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
                // some audio formats may have unspecified frame size
                // in that case we may read any amount of bytes
                bytesPerFrame = 1;
            }
            // Set an arbitrary buffer size of 1024 frames.
            int numBytes = 1024 * bytesPerFrame;
            byte[] audioBytes = new byte[numBytes];

            DataLine.Info info = new DataLine.Info(SourceDataLine.class, inputFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(inputFormat);
            sourceDataLine.start();

            int numBytesRead = 0;
            int numFramesRead = 0;
            // Try to read numBytes bytes from the file.
            while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
                // Calculate the number of frames actually read.
                numFramesRead = numBytesRead / bytesPerFrame;
                totalFramesRead += numFramesRead;
                // Here, do something useful with the audio data that's
                // now in the audioBytes array...
                // silenceRightChannel(audioBytes);
                // byte[] test =
                // extractChannel(audioBytes, inputFormat.getSampleSizeInBits(),
                // inputFormat.getChannels(), 0);
                ArrayList<byte[]> channels = new ArrayList<byte[]>();
                for (int i = 0; i < inputFormat.getChannels(); i++) {
                    channels.add(extractChannel(audioBytes, inputFormat.getSampleSizeInBits(),
                                                inputFormat.getChannels(), i));
                }
                for (int i = channels.size() - 1; i >= 0; i--) {
                    byte[] data = channels.get(i);
                    channels.remove(i);
                    data = myOperator.process(data);
                    channels.add(i, data);
                }
                byte[] processedData =
                        interleaveChannels(channels, inputFormat.getSampleSizeInBits());
                sourceDataLine.write(processedData, 0, processedData.length);
            }
        }
        catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Cleaned up");
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
            sourceDataLine = null;
        }
        System.out.println("Reached end");
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

//    private void silenceRightChannel (byte[] data) {
//        int bytePosition = 0;
//        for (int i = 0; i < data.length; i++) {
//            if (bytePosition == 2 || bytePosition == 3) {
//                data[i] = 0;
//            }
//            bytePosition++;
//            if (bytePosition == 4) {
//                bytePosition = 0;
//            }
//        }
//    }

    private void printArray (byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (Byte b : data) {
            sb.append(b + ", ");
        }
        System.out.println(sb.toString());
    }

}
