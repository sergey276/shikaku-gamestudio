package sk.tuke.gamestudio.game.shikaku.core;

import java.util.*;

public class Board {

    private int rowCount;
    private int columnCount;

    private List<Clue> clues;

    private Map<Clue, RectangleArea> placements;

    public Board(int rowCount, int columnCount, List<Clue> clues) {

        this.rowCount = rowCount;
        this.columnCount = columnCount;

        this.clues = new ArrayList<>(clues);
        this.placements = new HashMap<>();
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public List<Clue> getClues() {
        return clues;
    }

    public Map<Clue, RectangleArea> getPlacements() {
        return placements;
    }

    public void setPlacement(Clue clue, RectangleArea rectangleArea) {
        placements.put(clue, rectangleArea);
    }

    public void removePlacement(Clue clue) {
        placements.remove(clue);
    }

    public RectangleArea getPlacement(Clue clue) {
        return placements.get(clue);
    }

    public Clue findClueAt(Position position) {

        for (Clue clue : clues) {

            if (clue.getPosition().equals(position)) {
                return clue;
            }
        }

        return null;
    }

    public boolean isInside(Position position) {

        return position.getRow() >= 0
                && position.getRow() < rowCount
                && position.getColumn() >= 0
                && position.getColumn() < columnCount;
    }
}