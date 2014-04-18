package edu.geo4.duke.processing.players;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import edu.geo4.duke.processing.operators.ICallee;
import edu.geo4.duke.processing.operators.TimeStretchOperator;

public class Mp3Player implements ICaller {

  private WavPlayer myWavPlayer;

  public Mp3Player(File mp3file, ICallee operator) {
    testPlay(mp3file);
  }

  public void testPlay(File file) {
    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(file);
      AudioInputStream din = null;
      AudioFormat baseFormat = in.getFormat();
      AudioFormat decodedFormat =
          new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
              baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
              false);
      din = AudioSystem.getAudioInputStream(decodedFormat, in);

      myWavPlayer = new WavPlayer(din, new TimeStretchOperator(1.0f, false));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void answer(ICallee callee, int jobID, float[] reply) {
    // Assumes the WavPlayer successfully initialized.
    myWavPlayer.answer(callee, jobID, reply);
  }

  @Override
  public void run() {
    myWavPlayer.run();
  }

}
