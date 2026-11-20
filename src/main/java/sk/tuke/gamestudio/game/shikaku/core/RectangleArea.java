package sk.tuke.gamestudio.game.shikaku.core;

public class RectangleArea {

    private Position topLeft;
    private Position bottomRight;

    public RectangleArea(Position first, Position second) {

        int topRow = Math.min(first.getRow(), second.getRow());
        int leftColumn = Math.min(first.getColumn(), second.getColumn());

        int bottomRow = Math.max(first.getRow(), second.getRow());
        int rightColumn = Math.max(first.getColumn(), second.getColumn());

        this.topLeft = new Position(topRow, leftColumn);
        this.bottomRight = new Position(bottomRow, rightColumn);
    }

    public int getWidth() {
        return bottomRight.getColumn() - topLeft.getColumn() + 1;
    }

    public int getHeight() {
        return bottomRight.getRow() - topLeft.getRow() + 1;
    }

    public int area() {
        return getWidth() * getHeight();
    }

    public boolean contains(Position position) {
        return position.getRow() >= topLeft.getRow()
                && position.getRow() <= bottomRight.getRow()
                && position.getColumn() >= topLeft.getColumn()
                && position.getColumn() <= bottomRight.getColumn();
    }

    public boolean overlaps(RectangleArea other) {

        boolean separated =
                bottomRight.getRow() < other.topLeft.getRow()
                        || topLeft.getRow() > other.bottomRight.getRow()
                        || bottomRight.getColumn() < other.topLeft.getColumn()
                        || topLeft.getColumn() > other.bottomRight.getColumn();

        return !separated;
    }
}
