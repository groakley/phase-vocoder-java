import edu.geo4.duke.processing.operators.ICallee;
import edu.geo4.duke.processing.operators.PassThroughCallee;
import edu.geo4.duke.processing.operators.TimeStretchOperator;
import edu.geo4.duke.processing.players.WavPlayer;


public class Main {

    /**
     * @param args
     */
    public static void main (String[] args) {
//        ICallee op = new PassThroughCallee();
        ICallee op = new TimeStretchOperator(1.0f);
        WavPlayer pv = new WavPlayer("res/sample_music/coldplay.wav", op);
        pv.start();
    }

}
