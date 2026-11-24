import java.util.*;

public class Main {
    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void testInitialBestMove() {
        TicTacToe game = new TicTacToe();
        int move = game.getBestMove(TicTacToe.X);
        // Any corner or center is optimal; algorithm returns first available (0)
        assertTrue(move == 0, "Initial best move should be 0, got " + move);
    }

    private static void testWinningMove() {
        TicTacToe game = new TicTacToe();
        // X at positions 0 and 1, O elsewhere
        game.makeMove(0, TicTacToe.X);
        game.makeMove(1, TicTacToe.X);
        game.makeMove(4, TicTacToe.O);
        int move = game.getBestMove(TicTacToe.X);
        assertTrue(move == 2, "X should win by playing 2, got " + move);
    }

    private static void testBlockingMove() {
        TicTacToe game = new TicTacToe();
        // O about to win at positions 3,4; X must block at 5
        game.makeMove(3, TicTacToe.O);
        game.makeMove(4, TicTacToe.O);
        game.makeMove(0, TicTacToe.X);
        game.makeMove(1, TicTacToe.X);
        int move = game.getBestMove(TicTacToe.X);
        assertTrue(move == 5, "X should block at 5, got " + move);
    }

    private static void testDrawDetection() {
        TicTacToe game = new TicTacToe();
        // Fill board to a draw state
        char[] drawBoard = {
            TicTacToe.X, TicTacToe.O, TicTacToe.X,
            TicTacToe.X, TicTacToe.O, TicTacToe.O,
            TicTacToe.O, TicTacToe.X, TicTacToe.X
        };
        for (int i = 0; i < 9; i++) game.makeMove(i, drawBoard[i]);
        assertTrue(game.isDraw(), "Board should be a draw");
        assertTrue(!game.isWin(TicTacToe.X) && !game.isWin(TicTacToe.O), "No player should have won");
    }

    private static void benchmarkEmptyBoard() {
        TicTacToe game = new TicTacToe();
        long start = System.nanoTime();
        int best = game.getBestMove(TicTacToe.X);
        long elapsed = System.nanoTime() - start;
        System.out.println("Benchmark: best move on empty board = " + best + " (time " + (elapsed/1_000_000) + " ms)");
    }

    public static void main(String[] args) {
        testInitialBestMove();
        testWinningMove();
        testBlockingMove();
        testDrawDetection();
        System.out.println("All unit tests passed.");
        benchmarkEmptyBoard();
    }
}
