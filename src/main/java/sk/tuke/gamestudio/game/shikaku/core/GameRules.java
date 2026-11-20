package sk.tuke.gamestudio.game.shikaku.core;

import java.util.Map;

public class GameRules {

    public boolean canPlace(Board board, Clue clue, RectangleArea rectangleArea) {

        if (rectangleArea.area() != clue.getValue()) {
            return false;
        }

        if (!rectangleArea.contains(clue.getPosition())) {
            return false;
        }

        for (Clue other : board.getClues()) {

            if (other != clue && rectangleArea.contains(other.getPosition())) {
                return false;
            }
        }

        for (Map.Entry<Clue, RectangleArea> entry : board.getPlacements().entrySet()) {

            if (entry.getKey() != clue && rectangleArea.overlaps(entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    public boolean isSolved(Board board) {

        if (board.getPlacements().size() != board.getClues().size()) {
            return false;
        }

        for (int row = 0; row < board.getRowCount(); row++) {

            for (int col = 0; col < board.getColumnCount(); col++) {

                Position p = new Position(row, col);

                boolean covered = false;

                for (RectangleArea r : board.getPlacements().values()) {

                    if (r.contains(p)) {
                        covered = true;
                        break;
                    }
                }

                if (!covered) {
                    return false;
                }
            }
        }

        return true;
    }
}
