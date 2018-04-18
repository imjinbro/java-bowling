package domain.frame.result;

import domain.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class Boards {
    private Map<Player, Board> boards;

    public Boards(Map<Player, Board> boards) {
        this.boards = boards;
    }

    public List<String> getPlayerNameList() {
        return boards.keySet().stream().map(Player::getName).collect(toList());
    }

    public List<String> getResultMessage(String name) {
        return findBoardByName(name).getResultMessage();
    }

    public List<Integer> getResultScore(String name) {
        return findBoardByName(name).getResultScore();
    }

    private Board findBoardByName(String name) {
        Optional<Player> searchPlayer = boards.keySet().stream().filter(player -> player.isSameName(name)).findFirst();
        if (!searchPlayer.isPresent()) {
            throw new NullPointerException("오류에 의해 사용자를 찾을 수 없습니다.");
        }
        return boards.get(searchPlayer.get());
    }
}
