import domain.Player;
import domain.frame.result.FrameResults;
import domain.frame.Frames;
import view.InputView;
import view.OutputView;

public class BowlingMain {

    public static void main(String[] args) {
        Player player = InputView.getPlayer();
        FrameResults results = new FrameResults();
        Frames frames = new Frames();
        while (!frames.isFinish()) {
            doGame(player, results, frames);
        }
    }

    private static void doGame(Player player, FrameResults results, Frames frames) {
        try {
            int score = InputView.getScore(frames.getCurrentFrameNum());
            results.addResult(frames.getCurrentFrame(), frames.recordScore(score));
            OutputView.printGameResult(player, results);
        } catch (IllegalArgumentException e) {
            InputView.printMessage(e.getMessage());
            doGame(player, results, frames);
        }
    }
}
