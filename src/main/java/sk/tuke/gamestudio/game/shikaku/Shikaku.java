package sk.tuke.gamestudio.game.shikaku;

import sk.tuke.gamestudio.game.shikaku.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.shikaku.core.Board;
import sk.tuke.gamestudio.game.shikaku.core.Game;
import sk.tuke.gamestudio.game.shikaku.core.PuzzleFactory;

public class Shikaku {

    public static void main(String[] args) {
        PuzzleFactory puzzleFactory = new PuzzleFactory();
        Board board = puzzleFactory.createSampleBoard();
        Game game = new Game(board);

        ConsoleUI consoleUI = new ConsoleUI();
        consoleUI.play(game);
    }
}




















/*Правильные адреса теперь
Правильные адреса теперь
🔹 Комментарии
http://localhost:8081/api/comment/shikaku
🔹 Score
http://localhost:8081/api/score/shikaku
🔹 Rating
http://localhost:8081/api/rating/shikaku*/