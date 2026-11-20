package sk.tuke.gamestudio.game.shikaku.consoleui;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.shikaku.core.*;
import sk.tuke.gamestudio.service.*;

import java.util.*;

public class ConsoleUI {

    private static final String GAME_NAME = "shikaku";

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";

    private Game game;
    private final Scanner scanner = new Scanner(System.in);

    private String playerName;
    private long startTime;
    private long endTime;

    private int currentRows = 4;
    private int currentColumns = 4;

    private final PuzzleFactory puzzleFactory = new PuzzleFactory();

    private final ScoreService scoreService = new ScoreServiceJDBC();
    private final CommentService commentService = new CommentServiceJDBC();
    private final RatingService ratingService = new RatingServiceJDBC();

    private final Deque<Map<Clue, RectangleArea>> undoHistory = new ArrayDeque<>();

    public void play(Game game) {
        this.game = game;
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            System.out.println(PURPLE + "=== SHIKAKU ===" + RESET);
            System.out.println(GREEN + "1" + RESET + " - Start game");
            System.out.println(YELLOW + "2" + RESET + " - Top 5 scores");
            System.out.println("3 - Add comment");
            System.out.println("4 - Show comments");
            System.out.println("5 - Rate game");
            System.out.println("6 - Show rating");
            System.out.println(RED + "0" + RESET + " - Exit");
            System.out.print("> ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        chooseDifficulty();
                        break;
                    case 2:
                        printTopScores();
                        break;
                    case 3:
                        addComment();
                        break;
                    case 4:
                        showComments();
                        break;
                    case 5:
                        addRating();
                        break;
                    case 6:
                        showRating();
                        break;
                    case 0:
                        System.out.println("Bye!");
                        return;
                    default:
                        System.out.println(RED + "Wrong choice!\n" + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Wrong input!\n" + RESET);
                scanner.nextLine();
            }
        }
    }

    private void chooseDifficulty() {
        System.out.println("Choose difficulty:");
        System.out.println("1 - Easy (4x4)");
        System.out.println("2 - Medium (6x6)");
        System.out.println("3 - Hard (8x8)");
        System.out.print("> ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    currentRows = 4;
                    currentColumns = 4;
                    break;
                case 2:
                    currentRows = 6;
                    currentColumns = 6;
                    break;
                case 3:
                    currentRows = 8;
                    currentColumns = 8;
                    break;
                default:
                    System.out.println("Wrong choice!\n");
                    return;
            }

            System.out.print("Enter your name: ");
            playerName = scanner.nextLine();

            createNewGame();
            startGame();

        } catch (Exception e) {
            System.out.println("Wrong input!\n");
            scanner.nextLine();
        }
    }

    private void startGame() {
        startTime = System.currentTimeMillis();

        while (game.getState() != GameState.SOLVED) {
            show();

            boolean cont = handleInput();
            if (!cont) {
                System.out.println(YELLOW + "Back to menu...\n" + RESET);
                return;
            }
        }

        endTime = System.currentTimeMillis();

        show();
        System.out.println(GREEN + "Solved!" + RESET);

        long duration = (endTime - startTime) / 1000;
        saveScore(duration);
        askForCommentAndRating();
    }

    private void createNewGame() {
        Board newBoard = puzzleFactory.createBoard(currentRows, currentColumns);
        game = new Game(newBoard);
        undoHistory.clear();
    }

    private void clearCurrentBoard() {
        game.getBoard().getPlacements().clear();
        undoHistory.clear();
        System.out.println(YELLOW + "Board cleared!\n" + RESET);
    }

    private void saveStateForUndo() {
        undoHistory.push(new HashMap<>(game.getBoard().getPlacements()));
    }

    private void undoLastMove() {
        if (undoHistory.isEmpty()) {
            System.out.println(RED + "Nothing to undo!\n" + RESET);
            return;
        }

        Map<Clue, RectangleArea> prev = undoHistory.pop();
        game.getBoard().getPlacements().clear();
        game.getBoard().getPlacements().putAll(prev);

        System.out.println(CYAN + "Undo!\n" + RESET);
    }

    public void show() {
        Board board = game.getBoard();

        System.out.print("    ");
        for (int c = 0; c < board.getColumnCount(); c++) {
            System.out.printf("%-4d", c);
        }
        System.out.println();

        for (int r = 0; r < board.getRowCount(); r++) {
            System.out.printf("%-4s", (char) ('A' + r));

            for (int c = 0; c < board.getColumnCount(); c++) {
                Position p = new Position(r, c);
                Clue clue = board.findClueAt(p);

                if (clue != null) {
                    System.out.printf("%-4s", clue.getValue());
                } else if (isCovered(p)) {
                    System.out.print(CYAN + "██" + RESET + "  ");
                } else {
                    System.out.print("-   ");
                }
            }
            System.out.println();
        }

        System.out.println("\nState: " + game.getState());
        System.out.println();
    }


    public boolean handleInput() {
        System.out.println("7-exit  8-undo  9-clear");
        System.out.println("Enter move: clueRow clueCol row1 col1 row2 col2");
        System.out.println("Example: 1 2 0 2 3 2");
        System.out.print("> ");

        String input = scanner.nextLine().trim();

        if (input.equals("7")) {
            System.out.print("Exit? (y/n): ");
            return !scanner.nextLine().equalsIgnoreCase("y");
        }

        if (input.equals("8")) {
            undoLastMove();
            return true;
        }

        if (input.equals("9")) {
            clearCurrentBoard();
            return true;
        }

        try {
            String[] p = input.split("\\s+");

            if (p.length != 6) {
                System.out.println(RED + "Wrong format! Use: clueRow clueCol row1 col1 row2 col2\n" + RESET);
                return true;
            }

            int clueRow = Integer.parseInt(p[0]);
            int clueCol = Integer.parseInt(p[1]);
            int row1 = Integer.parseInt(p[2]);
            int col1 = Integer.parseInt(p[3]);
            int row2 = Integer.parseInt(p[4]);
            int col2 = Integer.parseInt(p[5]);

            Position cluePosition = new Position(clueRow, clueCol);
            RectangleArea area = new RectangleArea(
                    new Position(row1, col1),
                    new Position(row2, col2)
            );

            saveStateForUndo();

            boolean ok = game.placeRectangle(cluePosition, area);

            if (!ok) {
                undoHistory.pop();
                System.out.println(RED + "Invalid!" + RESET);
            }

        } catch (Exception e) {
            System.out.println(RED + "Error!" + RESET);
        }

        return true;
    }

    private void saveScore(long duration) {
        int points = Math.max(1000 - (int) duration * 10, 10);

        scoreService.addScore(
                new Score(GAME_NAME, playerName, getCurrentDifficulty(), points, new Date())
        );
    }

    private void printTopScores() {
        for (Score s : scoreService.getTopScores(GAME_NAME)) {
            System.out.println(s.getPlayer() + " - " + s.getDifficultyLabel() + " - " + s.getPoints());
        }
    }

    private String getCurrentDifficulty() {
        if (currentRows <= 4) {
            return "easy";
        }
        if (currentRows <= 6) {
            return "medium";
        }
        return "hard";
    }

    private void addComment() {
        if (playerName == null || playerName.isBlank()) {
            System.out.print("Enter your name first: ");
            playerName = scanner.nextLine();
        }

        System.out.print("Comment: ");
        commentService.addComment(
                new Comment(GAME_NAME, playerName, scanner.nextLine(), new Date())
        );
    }

    private void showComments() {
        for (Comment c : commentService.getComments(GAME_NAME)) {
            System.out.println(c.getPlayer() + ": " + c.getComment());
        }
    }

    private void addRating() {
        if (playerName == null || playerName.isBlank()) {
            System.out.print("Enter your name first: ");
            playerName = scanner.nextLine();
        }

        try {
            System.out.print("Rating: ");
            int rating = Integer.parseInt(scanner.nextLine());

            if (rating < 1 || rating > 5) {
                System.out.println(RED + "Rating must be from 1 to 5!" + RESET);
                return;
            }

            ratingService.setRating(GAME_NAME, playerName, rating);
            System.out.println(GREEN + "Rating saved!" + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Wrong rating!" + RESET);
        }
    }

    private void showRating() {
        System.out.println("Rating: " + ratingService.getAverageRating(GAME_NAME));
    }

    private boolean isCovered(Position p) {
        for (RectangleArea r : game.getBoard().getPlacements().values()) {
            if (r.contains(p)) return true;
        }
        return false;
    }

    private void askForCommentAndRating() {
        try {
            System.out.print("Add comment? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Comment: ");
                commentService.addComment(
                        new Comment(GAME_NAME, playerName, scanner.nextLine(), new Date())
                );
                System.out.println(GREEN + "Comment saved!" + RESET);
            }

            System.out.print("Rate game? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Rating (1-5): ");
                int rating = Integer.parseInt(scanner.nextLine());

                if (rating >= 1 && rating <= 5) {
                    ratingService.setRating(GAME_NAME, playerName, rating);
                    System.out.println(GREEN + "Rating saved!" + RESET);
                } else {
                    System.out.println(RED + "Wrong rating!" + RESET);
                }
            }

            System.out.println();
        } catch (Exception e) {
            System.out.println(RED + "Error saving comment/rating\n" + RESET);
        }
    }
}
