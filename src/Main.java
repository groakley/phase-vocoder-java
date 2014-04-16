import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import edu.geo4.duke.gui.ApplicationFrame;
import edu.geo4.duke.processing.operators.TimeStretchOperator;
import edu.geo4.duke.processing.players.WavPlayer;
import edu.geo4.duke.res.ResourceGetter;


public class Main {

  public static void main(String[] args) {
    // Object[] options = { "Choose WAVE File", "Use Default" };
    // int selection =
    // JOptionPane
    // .showOptionDialog(null,
    // "Would you like to use the default file excerpt, or select your own?",
    // "Choose File", JOptionPane.YES_NO_CANCEL_OPTION,
    // JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

    // if (selection != JOptionPane.CLOSED_OPTION) {
    TimeStretchOperator op = new TimeStretchOperator(1.0f, false);
    WavPlayer pv;
    // if (selection == 0) {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose a WAVE File");
    chooser.setFileFilter(new FileNameExtensionFilter("WAVE File (*.wav)", "wav"));
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      pv = new WavPlayer(chooser.getSelectedFile(), op);
    } else {
      pv = null;
      System.exit(0);
    }
    // }
    // else {
    // pv =
    // new WavPlayer(new ResourceGetter()
    // .getURL("coldplay_shortened.wav").getFile(),
    // op);
    // }
    new ApplicationFrame(op);
    pv.start();
    // }

  }
}
