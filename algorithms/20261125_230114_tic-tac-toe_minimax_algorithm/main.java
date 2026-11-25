public class Main {
    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void testInitialBestMove() {
        TicTacToe game = new TicTacToe();
        int move = game.bestMove();
        assertTrue(move == 4, "Empty board best move should be center (4), got " + move);
    }

    private static void testWinningMove() {
        TicTacToe game = new TicTacToe();
        // X at 0,1; O at 3,4; X to move and win at 2
        char[] board = {
            TicTacToe.X, TicTacToe.X, TicTacToe.EMPTY,
            TicTacToe.O, TicTacToe.O, TicTacToe.EMPTY,
            TicTacToe.EMPTY, TicTacToe.EMPTY, TicTacToe.EMPTY
        };
        game.setBoard(board, TicTacToe.X);
        int move = game.bestMove();
        assertTrue(move == 2, "X should win at position 2, got " + move);
    }

    private static void testBlockOpponent() {
        TicTacToe game = new TicTacToe();
        // O threatens win at 8, X must block
        char[] board = {
            TicTacToe.X, TicTacToe.EMPTY, TicTacToe.EMPTY,
            TicTacToe.EMPTY, TicTacToe.O, TicTacToe.EMPTY,
            TicTacToe.EMPTY, TicTacToe.EMPTY, TicTacToe.O
        };
        game.setBoard(board, TicTacToe.X);
        int move = game.bestMove();
        assertTrue(move == 8, "X should block O at position 8, got " + move);
    }

    private static void testDrawDetection() {
        TicTacToe game = new TicTacToe();
        // Full board with no winner
        char[] board = {
            TicTacToe.X, TicTacToe.O, TicTacToe.X,
            TicTacToe.X, TicTacToe.O, TicTacToe.O,
            TicTacToe.O, TicTacToe.X, TicTacToe.X
        };
        game.setBoard(board, TicTacToe.X);
        int move = game.bestMove();
        assertTrue(move == -1, "No moves left, should return -1, got " + move);
        assertTrue(game.checkWinner() == TicTacToe.EMPTY, "No winner expected");
    }

    private static void testFullGameSimulation() {
        TicTacToe game = new TicTacToe();
        while (true) {
            int move = game.bestMove();
            if (move == -1) break;
            game.makeMove(move);
            char winner = game.checkWinner();
            if (winner != TicTacToe.EMPTY) {
                assertTrue(winner == TicTacToe.X, "Optimal play should end with X win or draw");
                break;
            }
        }
    }

    public static void main(String[] args) {
        testInitialBestMove();
        testWinningMove();
        testBlockOpponent();
        testDrawDetection();
        testFullGameSimulation();
        System.out.println("All Tic-Tac-Toe Minimax tests passed.");
    }
}
