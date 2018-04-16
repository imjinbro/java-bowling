package domain.frame;

import domain.frame.pin.Pin;
import domain.frame.result.Board;
import domain.frame.result.FrameResult;
import domain.frame.score.FrameScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LastFrame extends Frame {
    private List<Pin> bonusPins = new ArrayList<>();
    private int bonusRecordCount = 0;

    public LastFrame(int frameNum) {
        super(frameNum);
    }

    @Override
    Frame doRecord(FrameScore score, int num) throws IllegalArgumentException {
        recordPin(score, num);
        if (score.isRegularFinish() && score.isBeforeBonusRoll() && bonusPins.isEmpty()) {
            score.increaseLeftCount();
        }
        return this;
    }

    private void recordPin(FrameScore score, int num) throws IllegalArgumentException {
        if (!score.isRegularFinish()) {
            score.roll(num);
            return;
        }
        recordBonusPin(num);
    }

    public void recordBonusPin(int num) {
        if (bonusPins.isEmpty() || isMaxFirstBonusPin()) {
            bonusPins.add(new Pin(num));
        }

        Pin firstPin = getFirstBonusPin();
        if (firstPin.isOverRecordPin(num)) {
            throw new IllegalArgumentException(Pin.MAX + "개 까지만 입력가능합니다.");
        }
        bonusPins.add(new Pin(num));
    }

    private boolean isMaxFirstBonusPin() {
        Pin firstBonus = getFirstBonusPin();
        return firstBonus.isMax();
    }

    private Pin getFirstBonusPin() {
        return bonusPins.get(0);
    }

    @Override
    void doRefreshPinNum(Frame currentFrame, FrameScore score) {
        score.rollBonusPins(this, this);
    }

    @Override
    List<Integer> doGetRecentlyPinNums(Frame askFrame, FrameScore score, int amount) {
        if (!askFrame.isLast()) {
            return buildRecentlyPinNumsForNormal(score, amount);
        }

        if (bonusPins.isEmpty()) {
            return Collections.emptyList();
        }
        return buildRecentlyPinNumsForLast();
    }

    private List<Integer> buildRecentlyPinNumsForNormal(FrameScore score, int amount) {
        if (!score.isPossibleGettingPins(amount) ) {
            return Collections.emptyList();
        }
        return score.getPins(amount);
    }

    private List<Integer> buildRecentlyPinNumsForLast() {
        Pin bonusPin = bonusPins.get(bonusRecordCount++);
        return Collections.singletonList(bonusPin.getNum());
    }

    @Override
    boolean doCheckFinish(FrameScore score) {
        return score.isRegularFinish() && score.isBonusFinish();
    }

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    void addFrameResult(Board board, int baseScore) {
        FrameResult result = getResult(baseScore);
        board.addResult(result);
    }
}
