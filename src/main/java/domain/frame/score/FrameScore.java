package domain.frame.score;

import domain.frame.Frame;
import domain.frame.pin.Pin;
import domain.frame.result.CannotCalcException;
import domain.frame.status.FrameStatus;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FrameScore {
    private FrameStatus status;
    private List<Pin> pins = new ArrayList<>();
    private int leftNumber;

    public FrameScore(int leftNumber) {
        status = FrameStatus.getInitStatus();
        this.leftNumber = leftNumber;
    }

    public void roll(int num) throws IllegalArgumentException {
        if (isOverRecordPin(num)) {
            throw new IllegalArgumentException(Pin.MAX + "개 까지만 입력가능합니다.");
        }

        if (!isBonusFinish()) {
            pins.add(new Pin(num));
            leftNumber--;
        }
        changeFrameStatus();
    }

    private void changeFrameStatus() {
        if (!status.isFinish()) {
            status = status.changeStatus(getTotalScore(), leftNumber);
        }
    }

    public boolean isOverRecordPin(int num) {
        return !status.isFinish() && pins.size() != 0 && pins.get(0).isOverRecordPin(num);
    }

    public FrameScore getScore() throws CannotCalcException {
        if (!isBonusFinish()) {
            throw new CannotCalcException("종료되지않은 프레임의 합계점수는 구할 수 없습니다.");
        }
        return this;
    }

    public String makeScoreMessage(Frame frame) {
        if (!frame.isLast() || !status.isBonus()) {
            return status.convertScore(pins);
        }
        return status.convertScore(pins) + ScoreMessage.getMessage(ScoreMessage.MODIFIER) + makeLastFrameBonusMessage();
    }

    private String makeLastFrameBonusMessage() {
        if (!status.isStrike()) {
            return pins.stream().filter(pin -> pins.indexOf(pin) >= Frame.REGULAR_COUNT).map(pin -> ScoreMessage.convertMessage(pin.getNum())).collect(joining(ScoreMessage.getMessage(ScoreMessage.MODIFIER)));
        }

        Pin firstBonusPin = pins.get(Frame.REGULAR_COUNT - 1);
        if (!isBonusFinish() || firstBonusPin.isMax()) {
            return pins.stream().filter(pin -> pins.indexOf(pin) >= Frame.REGULAR_COUNT - 1).map(pin -> ScoreMessage.convertMessage(pin.getNum())).collect(joining(ScoreMessage.getMessage(ScoreMessage.MODIFIER)));
        }
        return makeLastFrameBonusSpareMessage(firstBonusPin);
    }

    private String makeLastFrameBonusSpareMessage(Pin firstBonusPin) {
        Pin lastBonusPin = pins.get(pins.size() - 1);
        if (firstBonusPin.isMax(lastBonusPin)) {
            return ScoreMessage.convertMessage(firstBonusPin.getNum()) + ScoreMessage.getMessage(ScoreMessage.MODIFIER) + ScoreMessage.getMessage(ScoreMessage.SPARE);
        }
        return pins.stream().filter(pin -> pins.indexOf(pin) >= Frame.REGULAR_COUNT - 1).map(pin -> ScoreMessage.convertMessage(pin.getNum())).collect(joining(ScoreMessage.getMessage(ScoreMessage.MODIFIER)));
    }

    public int getTotalScore() {
        return pins.stream().mapToInt(Pin::getNum).sum();
    }

    public boolean isRegularFinish() {
        return status.isFinish();
    }

    public boolean isBonusFinish() {
        return leftNumber == 0;
    }

    public void increaseLeftCount() {
        if (status.isBonus()) {
            leftNumber += 1;
        }
    }

    public boolean isBeforeBonusRoll() {
        if (!status.isBonus()) {
            return true;
        }
        return status.isBonus() && status.isRightThrewNum(pins.size());
    }

    public void rollBonusPins(Frame askFrame, Frame frameWithScores) {
        if (frameWithScores.isLast()) {
            List<Integer> pins = frameWithScores.getRecentlyPinNums(askFrame, leftNumber);
            recordLastFrameBonusPin(pins);
            return;
        }

        List<Integer> pins = frameWithScores.getRecentlyPinNums(askFrame, leftNumber);
        if (pins.size() == leftNumber) {
            pins.forEach(this::roll);
        }
    }

    private void recordLastFrameBonusPin(List<Integer> pins) {
        if (pins.isEmpty()) {
            return;
        }
        pins.forEach(this::roll);
    }

    public boolean isPossibleGettingPins(Frame nextFrame, int amount) {
        return isPossibleGettingPins(amount) || checkNextFrame(nextFrame, amount);
    }

    public boolean isPossibleGettingPins(int amount) {
        return pins.size() == amount;
    }

    private boolean checkNextFrame(Frame nextFrame, int amount) {
        if (nextFrame == null) {
            return false;
        }
        int diffNum = amount - pins.size();
        return nextFrame.haveTried(diffNum);
    }

    public boolean isWithinRollNum(int rollNum) {
        return pins.size() >= rollNum;
    }

    public List<Integer> getBonusPins(Frame nextFrame, int amount) {
        List<Integer> bonusPins = pins.stream().filter(pin -> pins.indexOf(pin) < pins.size()).map(Pin::getNum).collect(toList());
        if (bonusPins.size() != amount) {
            int diffNum = amount - bonusPins.size();
            bonusPins.addAll(nextFrame.getPins(diffNum));
        }
        return bonusPins;
    }

    public List<Integer> getPins(int amount) {
        List<Integer> pinNums = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Pin pin = pins.get(i);
            pinNums.add(pin.getNum());
        }
        return pinNums;
    }
}
