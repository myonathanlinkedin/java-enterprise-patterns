package tictactoe;

import java.util.Arrays;

/**
 * Simple test harness for TicTacToe Minimax implementation.
 * Uses Java assert statements; run with -ea to enable.
 */
public final class Main {
    public static void main(String[] args) {
        testInitialBestMove();
        testWinningMove();
        testBlockingMove();
        testDrawDetection();
        testFullGameSimulation();
        System.out.println("All tests passed.");
    }

    private static void testInitialBestMove() {
        TicTacToe game = new TicTacToe();
        int[] move = game.bestMove('X');
        // Center is optimal for first player.
        assert move != null : "Best move should not be null on empty board";
        assert move[0] == 1 && move[1] == 1 : "First move should be center (1,1)";
    }

    private static void testWinningMove() {
        TicTacToe game = new TicTacToe();
        // X has two in a row, can win with (0,2)
        game.makeMove(0, 0, 'X');
        game.makeMove(0, 1, 'X');
        game.makeMove(1, 0, 'O');
        game.makeMove(1, 1, 'O');
        int[] move = game.bestMove('X');
        assert move != null && move[0] == 0 && move[1] == 2 : "X should win at (0,2)";
    }

    private static void testBlockingMove() {
        TicTacToe game = new TicTacToe();
        // O is about to win; X must block at (2,2)
        game.makeMove(0, 0, 'X');
        game.makeMove(1, 1, 'O');
        game.makeMove(0, 1, 'X');
        game.makeMove(0, 2, 'O');
        game.makeMove(2, 0, 'X');
        // O threatens (2,2) via diagonal
        int[] move = game.bestMove('X');
        assert move != null && move[0] == 2 && move[1] == 2 : "X must block O at (2,2)";
    }

    private static void testDrawDetection() {
        TicTacToe game = new TicTacToe();
        // Fill board to a draw state
        char[][] drawBoard = {
            {'X','O','X'},
            {'X','O','O'},
            {'O','X','X'}
        };
        for (int i = 0; i < TicTacToe.SIZE; i++) {
            for (int j = 0; j < TicTacToe.SIZE; j++) {
                game.makeMove(i, j, drawBoard[i][j]);
            }
        }
        assert game.isGameOver() : "Game should be over (draw)";
        assert !game.hasWon('X') && !game.hasWon('O') : "No player should have won";
        assert game.bestMove('X') == null : "No best move when game is over";
    }

    private static void testFullGameSimulation() {
        TicTacToe game = new TicTacToe();
        char current = 'X';
        while (!game.isGameOver()) {
            int[] move = game.bestMove(current);
            assert move != null : "Best move should exist while game not over";
            boolean ok = game.makeMove(move[0], move[1], current);
            assert ok : "Move should be legal";
            current = (current == 'X') ? 'O' : 'X';
        }
        // X should never lose with optimal play.
        assert !game.hasWon('O') : "Optimal play should prevent O from winning";
        // X may win or draw.
        System.out.println("Final board after optimal play:\n" + game);
    }
}
