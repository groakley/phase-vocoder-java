import edu.geo4.duke.processing.operators.ICallee;
import edu.geo4.duke.processing.operators.PassThroughCallee;
import edu.geo4.duke.processing.players.WavPlayer;


public class Main {

    /**
     * @param args
     */
    public static void main (String[] args) {
        ICallee op = new PassThroughCallee();
        WavPlayer pv = new WavPlayer("res/sample_music/coldplay.wav", op);
        pv.start();
    }

}
