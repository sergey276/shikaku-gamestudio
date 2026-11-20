package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.shikaku.core.Board;
import sk.tuke.gamestudio.game.shikaku.core.Clue;
import sk.tuke.gamestudio.game.shikaku.core.Game;
import sk.tuke.gamestudio.game.shikaku.core.GameState;
import sk.tuke.gamestudio.game.shikaku.core.Position;
import sk.tuke.gamestudio.game.shikaku.core.PuzzleFactory;
import sk.tuke.gamestudio.game.shikaku.core.RectangleArea;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.ScoreService;
import sk.tuke.gamestudio.service.UserAccountException;
import sk.tuke.gamestudio.service.UserAccountService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class ShikakuController {
    private static final String GAME_NAME = "shikaku";
    private static final int TOP_SCORE_LIMIT = 5;

    private final ScoreService scoreService;
    private final CommentService commentService;
    private final RatingService ratingService;
    private final UserAccountService userAccountService;
    private final PuzzleFactory puzzleFactory = new PuzzleFactory();
    private final Map<String, Integer> failedLoginAttempts = new HashMap<>();

    private Game game;
    private String player = "";
    private String message = "Register or sign in with username and password to start.";
    private String authMode = "choice";
    private String passwordResetCandidate = "";
    private boolean authenticated;
    private boolean gameReady;
    private long startedAt;
    private long elapsedSeconds;
    private boolean scoreSaved;
    private Integer victoryRank;
    private Integer victoryPoints;
    private Integer latestSolvedPoints;

    @Autowired
    public ShikakuController(ScoreService scoreService, CommentService commentService, RatingService ratingService,
                             UserAccountService userAccountService) {
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.ratingService = ratingService;
        this.userAccountService = userAccountService;
        startNewGame(4);
        gameReady = false;
    }

    @GetMapping({"/", "/shikaku"})
    public String shikaku(@RequestParam(value = "auth", required = false) String auth, Model model) {
        if (!authenticated && auth != null) {
            authMode = normalizeAuthMode(auth);
        }
        fillModel(model);
        return "shikaku";
    }

    @PostMapping("/shikaku/player")
    public String register(@RequestParam String player, @RequestParam String password) {
        String normalizedPlayer = normalizePlayer(player);
        String normalizedPassword = normalizePassword(password);
        if (normalizedPlayer == null || normalizedPassword == null) {
            authenticated = false;
            gameReady = false;
            message = "Username and password are required.";
            return "redirect:/shikaku";
        }

        try {
            userAccountService.register(normalizedPlayer, normalizedPassword);
            this.player = normalizedPlayer;
            authenticated = true;
            gameReady = false;
            authMode = "choice";
            failedLoginAttempts.remove(normalizedPlayer);
            message = "Account created. Choose difficulty, " + this.player + ".";
        } catch (UserAccountException e) {
            authenticated = false;
            gameReady = false;
            authMode = "register";
            message = e.getMessage();
        }
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/login")
    public String login(@RequestParam String player, @RequestParam String password) {
        String normalizedPlayer = normalizePlayer(player);
        String normalizedPassword = normalizePassword(password);
        if (normalizedPlayer == null || normalizedPassword == null) {
            authenticated = false;
            gameReady = false;
            message = "Username and password are required.";
            return "redirect:/shikaku";
        }

        if (!userAccountService.authenticate(normalizedPlayer, normalizedPassword)) {
            authenticated = false;
            gameReady = false;
            authMode = "login";
            message = buildFailedLoginMessage(normalizedPlayer);
            return "redirect:/shikaku";
        }

        this.player = normalizedPlayer;
        authenticated = true;
        gameReady = false;
        authMode = "choice";
        failedLoginAttempts.remove(normalizedPlayer);
        message = "Welcome back, " + this.player + ". Choose difficulty.";
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/password")
    public String changePassword(@RequestParam String player, @RequestParam String password) {
        String normalizedPlayer = normalizePlayer(player);
        String normalizedPassword = normalizePassword(password);
        if (normalizedPlayer == null || normalizedPassword == null) {
            authenticated = false;
            gameReady = false;
            authMode = "reset";
            message = "Username and new password are required.";
            return "redirect:/shikaku";
        }

        try {
            userAccountService.updatePassword(normalizedPlayer, normalizedPassword);
            failedLoginAttempts.remove(normalizedPlayer);
            passwordResetCandidate = "";
            authMode = "login";
            message = "Password changed. Now sign in with the new password.";
        } catch (UserAccountException e) {
            authMode = "reset";
            message = e.getMessage();
        }
        return "redirect:/shikaku";
    }

    @GetMapping("/shikaku/new")
    public String newGame(@RequestParam(value = "size", required = false, defaultValue = "4") int size) {
        if (!authenticated) {
            message = "Sign in before choosing difficulty.";
            return "redirect:/shikaku";
        }

        int normalizedSize = normalizeSize(size);
        startNewGame(normalizedSize);
        message = "New " + normalizedSize + "x" + normalizedSize + " puzzle is ready.";
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/place")
    public String place(@RequestParam int clueRow,
                        @RequestParam int clueColumn,
                        @RequestParam int row1,
                        @RequestParam int column1,
                        @RequestParam int row2,
                        @RequestParam int column2) {
        if (!canPlay()) {
            message = "Sign in and choose difficulty first.";
            return "redirect:/shikaku";
        }
        if (isSolved()) {
            message = "The puzzle is already solved. Choose another difficulty or exit.";
            return "redirect:/shikaku";
        }

        boolean placed = game.placeRectangle(
                new Position(clueRow, clueColumn),
                new RectangleArea(new Position(row1, column1), new Position(row2, column2))
        );

        if (placed) {
            message = "Rectangle placed.";
            saveScoreIfSolved();
        } else {
            message = "Invalid rectangle. It must contain the clue, have the clue area, and not overlap.";
        }
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/remove")
    public String remove(@RequestParam int clueRow, @RequestParam int clueColumn) {
        if (!canPlay()) {
            message = "Sign in and choose difficulty first.";
            return "redirect:/shikaku";
        }
        if (isSolved()) {
            message = "The puzzle is already solved. Choose another difficulty or exit.";
            return "redirect:/shikaku";
        }

        message = game.removeRectangle(new Position(clueRow, clueColumn))
                ? "Rectangle removed."
                : "No clue exists on the selected cell.";
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/give-up")
    public String giveUp() {
        if (!authenticated) {
            message = "Sign in before choosing difficulty.";
            return "redirect:/shikaku";
        }

        gameReady = false;
        scoreSaved = false;
        victoryRank = null;
        victoryPoints = null;
        latestSolvedPoints = null;
        message = "You gave up. Choose another difficulty, " + player + ".";
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/exit")
    public String logout() {
        resetSessionState();
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/comment")
    public String addComment(@RequestParam String text) {
        if (!authenticated) {
            message = "Sign in before sending comments.";
            return "redirect:/shikaku";
        }
        if (text == null || text.isBlank()) {
            message = "Comment cannot be empty.";
            return "redirect:/shikaku";
        }

        try {
            commentService.addComment(new Comment(GAME_NAME, this.player, text.trim(), new Date()));
            message = "Comment saved.";
        } catch (RuntimeException e) {
            message = "Comment was not saved because the database rejected it.";
        }
        return "redirect:/shikaku";
    }

    @PostMapping("/shikaku/rating")
    public String rate(@RequestParam int rating) {
        if (!authenticated) {
            message = "Sign in before rating the game.";
            return "redirect:/shikaku";
        }
        if (rating < 1 || rating > 5) {
            message = "Rating must be between 1 and 5.";
            return "redirect:/shikaku";
        }

        try {
            ratingService.setRating(GAME_NAME, this.player, rating);
            message = "Rating saved.";
        } catch (RuntimeException e) {
            message = "Rating was not saved because the database rejected it.";
        }
        return "redirect:/shikaku";
    }

    public boolean isCovered(int row, int column) {
        Position position = new Position(row, column);
        for (RectangleArea area : game.getBoard().getPlacements().values()) {
            if (area.contains(position)) {
                return true;
            }
        }
        return false;
    }

    public Clue getClue(int row, int column) {
        return game.getBoard().findClueAt(new Position(row, column));
    }

    private void fillModel(Model model) {
        Board board = game.getBoard();
        model.addAttribute("game", game);
        model.addAttribute("board", board);
        model.addAttribute("rows", range(board.getRowCount()));
        model.addAttribute("columns", range(board.getColumnCount()));
        model.addAttribute("player", player);
        model.addAttribute("message", message);
        model.addAttribute("authMode", authMode);
        model.addAttribute("passwordResetCandidate", passwordResetCandidate);
        model.addAttribute("playerReady", authenticated);
        model.addAttribute("gameReady", gameReady);
        model.addAttribute("solved", gameReady && game.getState() == GameState.SOLVED);
        model.addAttribute("victoryRank", victoryRank);
        model.addAttribute("victoryPoints", victoryPoints);
        model.addAttribute("latestSolvedPoints", latestSolvedPoints);
        model.addAttribute("startedAt", startedAt);
        model.addAttribute("elapsedSeconds", getElapsedSeconds());
        try {
            model.addAttribute("scores", scoreService.getTopScores(GAME_NAME)
                    .stream()
                    .limit(TOP_SCORE_LIMIT)
                    .toList());
        } catch (RuntimeException e) {
            model.addAttribute("scores", List.of());
        }
        try {
            model.addAttribute("comments", commentService.getComments(GAME_NAME));
        } catch (RuntimeException e) {
            model.addAttribute("comments", List.of());
        }
        try {
            model.addAttribute("averageRating", ratingService.getAverageRating(GAME_NAME));
            model.addAttribute("playerRating", ratingService.getRating(GAME_NAME, player));
            model.addAttribute("ratings", ratingService.getRatings(GAME_NAME));
        } catch (RuntimeException e) {
            model.addAttribute("averageRating", 0);
            model.addAttribute("playerRating", 0);
            model.addAttribute("ratings", List.of());
        }
    }

    private List<Integer> range(int count) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(i);
        }
        return values;
    }

    private void startNewGame(int size) {
        game = new Game(puzzleFactory.createBoard(size, size));
        startedAt = System.currentTimeMillis();
        elapsedSeconds = 0;
        scoreSaved = false;
        victoryRank = null;
        victoryPoints = null;
        latestSolvedPoints = null;
        gameReady = true;
    }

    private int normalizeSize(int size) {
        if (size <= 4) {
            return 4;
        }
        if (size <= 6) {
            return 6;
        }
        return 8;
    }

    private boolean canPlay() {
        return authenticated && gameReady;
    }

    private boolean isSolved() {
        return game != null && game.getState() == GameState.SOLVED;
    }

    private void saveScoreIfSolved() {
        if (game.getState() != GameState.SOLVED || scoreSaved) {
            return;
        }

        long seconds = Math.max(1, (System.currentTimeMillis() - startedAt) / 1000);
        elapsedSeconds = seconds;
        String difficulty = getCurrentDifficulty();
        String difficultyLabel = getDifficultyLabel(difficulty);
        int boardArea = game.getBoard().getRowCount() * game.getBoard().getColumnCount();
        int points = Math.max(10, boardArea * 100 - (int) seconds * 5);
        latestSolvedPoints = points;
        victoryPoints = points;
        try {
            Integer bestScore = scoreService.getBestScore(GAME_NAME, player, difficulty);
            if (bestScore != null && bestScore >= points) {
                victoryRank = null;
                message = bestScore == points
                        ? "Congratulations! You solved the " + difficultyLabel + " puzzle. Your best result there is already " + bestScore + " points."
                        : "Congratulations! You solved the " + difficultyLabel + " puzzle, but you already had a better result there: " + bestScore + " points.";
                victoryPoints = bestScore;
                scoreSaved = true;
                return;
            }

            scoreService.addScore(new Score(GAME_NAME, player, difficulty, points, new Date()));
            victoryRank = calculateRank(points, difficulty);
            message = "Congratulations! You solved the " + difficultyLabel + " puzzle.";
        } catch (RuntimeException e) {
            victoryRank = null;
            message = "Congratulations! Database rejected the score, but the game will continue.";
        }
        scoreSaved = true;
    }

    private int calculateRank(int points, String difficulty) {
        int rank = 1;
        for (Score score : scoreService.getTopScores(GAME_NAME)) {
            if (difficulty.equals(score.getDifficulty()) && score.getPoints() > points) {
                rank++;
            }
        }
        return rank;
    }

    private String getCurrentDifficulty() {
        int size = game.getBoard().getRowCount();
        if (size <= 4) {
            return "easy";
        }
        if (size <= 6) {
            return "medium";
        }
        return "hard";
    }

    private String getDifficultyLabel(String difficulty) {
        if ("easy".equals(difficulty)) {
            return "Easy";
        }
        if ("medium".equals(difficulty)) {
            return "Medium";
        }
        return "Hard";
    }

    private long getElapsedSeconds() {
        if (!gameReady) {
            return 0;
        }
        if (isSolved()) {
            return elapsedSeconds;
        }
        return Math.max(0, (System.currentTimeMillis() - startedAt) / 1000);
    }

    private String normalizePlayer(String player) {
        if (player == null) {
            return null;
        }

        String normalized = player.trim();
        if (normalized.isEmpty() || normalized.length() > 32) {
            return null;
        }
        return normalized;
    }

    private String normalizePassword(String password) {
        if (password == null) {
            return null;
        }

        String normalized = password.trim();
        if (normalized.length() < 4 || normalized.length() > 64) {
            return null;
        }
        return normalized;
    }

    private String normalizeAuthMode(String auth) {
        if ("register".equalsIgnoreCase(auth)) {
            return "register";
        }
        if ("login".equalsIgnoreCase(auth)) {
            return "login";
        }
        if ("reset".equalsIgnoreCase(auth)) {
            return "reset";
        }
        return "choice";
    }

    private void resetSessionState() {
        player = "";
        authenticated = false;
        authMode = "choice";
        gameReady = false;
        scoreSaved = false;
        victoryRank = null;
        victoryPoints = null;
        latestSolvedPoints = null;
        elapsedSeconds = 0;
        passwordResetCandidate = "";
        message = "Register or sign in with username and password to start.";
    }

    private String buildFailedLoginMessage(String username) {
        if (!userAccountService.exists(username)) {
            passwordResetCandidate = "";
            return "This account is not registered yet. Please register first.";
        }

        int attempts = failedLoginAttempts.merge(username, 1, Integer::sum);
        if (attempts >= 5) {
            passwordResetCandidate = username;
            return "Invalid password. You entered the wrong password 5 times. You can change the password now.";
        }
        passwordResetCandidate = "";
        return "Invalid password.";
    }
}
