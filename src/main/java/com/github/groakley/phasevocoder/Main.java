package com.github.groakley.phasevocoder;

import com.github.groakley.phasevocoder.gui.ApplicationFrame;
import com.github.groakley.phasevocoder.processing.operators.TimeStretchOperator;
import com.github.groakley.phasevocoder.processing.players.ICaller;
import com.github.groakley.phasevocoder.processing.players.Mp3Player;
import com.github.groakley.phasevocoder.processing.players.WavPlayer;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

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
