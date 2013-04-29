import edu.geo4.duke.processing.operators.ByteOperator;
import edu.geo4.duke.processing.operators.PassThroughOperator;
import edu.geo4.duke.processing.players.WavPlayer;


public class Main {

    /**
     * @param args
     */
    public static void main (String[] args) {
        ByteOperator op = new PassThroughOperator();
        WavPlayer pv = new WavPlayer("res/sample_music/coldplay.wav", op);
        pv.start();
    }

}
