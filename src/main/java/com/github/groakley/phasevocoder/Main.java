package com.github.groakley.phasevocoder;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import edu.geo4.duke.gui.ApplicationFrame;
import edu.geo4.duke.processing.operators.TimeStretchOperator;
import edu.geo4.duke.processing.players.ICaller;
import edu.geo4.duke.processing.players.Mp3Player;
import edu.geo4.duke.processing.players.WavPlayer;


public class Main {

  public static void main(String[] args) {
    TimeStretchOperator op = new TimeStretchOperator(1.0f, false);
    ICaller pv;
    // if (selection == 0) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose a WAVE File");
    chooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav, *.mp3)", "wav", "mp3"));
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      if (chooser.getSelectedFile().getName().endsWith(".wav")) {
        pv = new WavPlayer(chooser.getSelectedFile(), op);
      } else if (chooser.getSelectedFile().getName().endsWith(".mp3")) {
        pv = new Mp3Player(chooser.getSelectedFile(), op);
      } else {
        throw new RuntimeException("Unsupported file type. Must be of type .wav or .mp3");
      }
    } else {
      pv = null;
      System.exit(0);
    }
    new ApplicationFrame(op);
    new Thread(pv).start();
  }
}
