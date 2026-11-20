package sk.tuke.gamestudio.game.shikaku.core;

public class Clue {

    private Position position;
    private int value;

    public Clue(Position position, int value) {
        this.position = position;
        this.value = value;
    }

    public Position getPosition() {
        return position;
    }

    public int getValue() {
        return value;
    }
}
