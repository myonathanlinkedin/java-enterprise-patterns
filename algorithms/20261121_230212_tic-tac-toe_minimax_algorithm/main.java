package tictactoe;

import tictactoe.Engine;
import tictactoe.Types.Board;
import tictactoe.Types.Coord;
import tictactoe.Types.Player;

/**
 * Driver with exhaustive unit‑tests for the Tic‑Tac‑Toe Minimax implementation.
 */
public final class Main {

    private Main() {
        // Prevent instantiation.
    }

    public static void main(String[] args) {
        testEmptyBoardFirstMove();
        testImmediateWin();
        testBlockOpponentWin();
        testOptimalDraw();
        System.out.println("All Tic-Tac-Toe Minimax tests passed.");
    }

    /** X should take the center on an empty board (optimal opening). */
    private static void testEmptyBoardFirstMove() {
        Board board = new Board();
        Coord best = Engine.getBestMove(board, Player.X);
        assert best.row == 1 && best.col == 1 : "Expected center (1,1) but got " + best;
    }

    /** X can win immediately; engine must select that move. */
    private static void testImmediateWin() {
        Board board = new Board();
        // X X _
        // O O _
        // _ _ _
        board.makeMove(new Coord(0, 0), Player.X);
        board.makeMove(new Coord(0, 1), Player.X);
        board.makeMove(new Coord(1, 0), Player.O);
        board.makeMove(new Coord(1, 1), Player.O);
        Coord best = Engine.getBestMove(board, Player.X);
        assert best.row == 0 && best.col == 2 : "X should win at (0,2) but got " + best;
    }

    /** O must block X's winning threat. */
    private static void testBlockOpponentWin() {
        Board board = new Board();
        // X _ _
        // _ X _
        // O _ _
        board.makeMove(new Coord(0, 0), Player.X);
        board.makeMove(new Coord(1, 1), Player.X);
        board.makeMove(new Coord(2, 0), Player.O);
        Coord best = Engine.getBestMove(board, Player.O);
        // O must play (2,2) to block diagonal.
        assert best.row == 2 && best.col == 2 : "O should block at (2,2) but got " + best;
    }

    /** Verify that the engine chooses a draw when no win is possible. */
    private static void testOptimalDraw() {
        Board board = new Board();
        // X O X
        // X O O
        // O X _
        board.makeMove(new Coord(0, 0), Player.X);
        board.makeMove(new Coord(0, 1), Player.O);
        board.makeMove(new Coord(0, 2), Player.X);
        board.makeMove(new Coord(1, 0), Player.X);
        board.makeMove(new Coord(1, 1), Player.O);
        board.makeMove(new Coord(1, 2), Player.O);
        board.makeMove(new Coord(2, 0), Player.O);
        board.makeMove(new Coord(2, 1), Player.X);
        // Only move left is (2,2); result is a draw.
        Coord best = Engine.getBestMove(board, Player.X);
        assert best.row == 2 && best.col == 2 : "Expected last move (2,2) but got " + best;
        board.makeMove(best, Player.X);
        assert board.checkWinner() == null : "Game should be a draw.";
        assert board.isFull() : "Board should be full after last move.";
    }
}
