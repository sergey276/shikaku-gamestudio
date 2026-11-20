package sk.tuke.gamestudio.game.shikaku.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PuzzleFactory {

    private final Random random = new Random();

    public Board createSampleBoard() {
        return createBoard(4, 4);
    }

    public Board createBoard(int rows, int columns) {
        List<Rect> rectangles = generateRectangles(rows, columns);
        List<Clue> clues = new ArrayList<>();

        for (Rect rect : rectangles) {
            int clueRow = rect.top + random.nextInt(rect.height);
            int clueColumn = rect.left + random.nextInt(rect.width);
            clues.add(new Clue(new Position(clueRow, clueColumn), rect.area()));
        }

        return new Board(rows, columns, clues);
    }

    private List<Rect> generateRectangles(int rows, int columns) {
        List<Rect> rectangles = new ArrayList<>();
        rectangles.add(new Rect(0, 0, rows, columns));

        int maxArea = getMaxRectangleArea(rows, columns);

        boolean changed;
        do {
            changed = false;
            List<Rect> next = new ArrayList<>();

            for (Rect rect : rectangles) {
                if (shouldSplit(rect, maxArea)) {
                    next.addAll(splitRectangle(rect));
                    changed = true;
                } else {
                    next.add(rect);
                }
            }

            rectangles = next;
        } while (changed);

        return rectangles;
    }

    private int getMaxRectangleArea(int rows, int columns) {
        int size = Math.max(rows, columns);

        if (size <= 4) {
            return 4;
        }
        if (size <= 6) {
            return 6;
        }
        return 8;
    }

    private boolean shouldSplit(Rect rect, int maxArea) {
        if (rect.area() <= maxArea) {
            return false;
        }

        return rect.width > 1 || rect.height > 1;
    }

    private List<Rect> splitRectangle(Rect rect) {
        List<Rect> result = new ArrayList<>();

        boolean canSplitHorizontally = rect.height > 1;
        boolean canSplitVertically = rect.width > 1;

        if (!canSplitHorizontally && !canSplitVertically) {
            result.add(rect);
            return result;
        }

        boolean splitHorizontally;
        if (canSplitHorizontally && canSplitVertically) {
            splitHorizontally = random.nextBoolean();
        } else {
            splitHorizontally = canSplitHorizontally;
        }

        if (splitHorizontally) {
            int split = 1 + random.nextInt(rect.height - 1);

            result.add(new Rect(rect.top, rect.left, split, rect.width));
            result.add(new Rect(rect.top + split, rect.left, rect.height - split, rect.width));
        } else {
            int split = 1 + random.nextInt(rect.width - 1);

            result.add(new Rect(rect.top, rect.left, rect.height, split));
            result.add(new Rect(rect.top, rect.left + split, rect.height, rect.width - split));
        }

        return result;
    }

    private static class Rect {
        private final int top;
        private final int left;
        private final int height;
        private final int width;

        private Rect(int top, int left, int height, int width) {
            this.top = top;
            this.left = left;
            this.height = height;
            this.width = width;
        }

        private int area() {
            return height * width;
        }
    }
}