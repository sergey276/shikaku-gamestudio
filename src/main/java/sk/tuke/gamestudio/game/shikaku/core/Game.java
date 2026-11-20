package sk.tuke.gamestudio.game.shikaku.core;

public class Game {

    private Board board;

    private GameRules rules;

    private GameState state;

    public Game(Board board) {

        this.board = board;

        this.rules = new GameRules();

        this.state = GameState.NEW;
    }

    public Board getBoard() {
        return board;
    }

    public GameState getState() {
        return state;
    }

    public boolean placeRectangle(Position cluePosition, RectangleArea rectangleArea) {

        Clue clue = board.findClueAt(cluePosition);

        if (clue == null) {
            return false;
        }

        if (!rules.canPlace(board, clue, rectangleArea)) {
            return false;
        }

        board.setPlacement(clue, rectangleArea);

        state = rules.isSolved(board) ? GameState.SOLVED : GameState.RUNNING;

        return true;
    }

    public boolean removeRectangle(Position cluePosition) {

        Clue clue = board.findClueAt(cluePosition);

        if (clue == null) {
            return false;
        }

        board.removePlacement(clue);

        state = board.getPlacements().isEmpty()
                ? GameState.NEW
                : GameState.RUNNING;

        return true;
    }
}