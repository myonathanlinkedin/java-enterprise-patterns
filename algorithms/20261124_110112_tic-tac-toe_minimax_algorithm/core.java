package tictactoe;

import java.util.ArrayList;
import java.util.List;

/**
 * Core implementation of Tic-Tac-Toe with Minimax (alpha‑beta) algorithm.
 * Board cells contain 'X', 'O' or ' ' (space) for empty.
 */
public final class TicTacToe {
    public static final int SIZE = 3;
    private final char[][] board;
    private static final char EMPTY = ' ';
    private static final char PLAYER_X = 'X';
    private static final char PLAYER_O = 'O';

    public TicTacToe() {
        board = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    /** Copy constructor used for testing immutability. */
    private TicTacToe(char[][] src) {
        board = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(src[i], 0, board[i], 0, SIZE);
        }
    }

    /** Returns a deep copy of the current game state. */
    public TicTacToe copy() {
        return new TicTacToe(board);
    }

    /** Places a mark for the given player at (row, col). Returns true if successful. */
    public boolean makeMove(int row, int col, char player) {
        if (!isValidPlayer(player) || !inBounds(row, col) || board[row][col] != EMPTY) {
            return false;
        }
        board[row][col] = player;
        return true;
    }

    /** Removes a mark from (row, col). Used by the minimax recursion. */
    public void undoMove(int row, int col) {
        if (inBounds(row, col)) {
            board[row][col] = EMPTY;
        }
    }

    /** Returns a list of empty cells as int[2] arrays. */
    public List<int[]> emptyCells() {
        List<int[]> cells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    cells.add(new int[]{i, j});
                }
            }
        }
        return cells;
    }

    /** Checks if the board is full. */
    public boolean isFull() {
        return emptyCells().isEmpty();
    }

    /** Determines if the given player has won. */
    public boolean hasWon(char player) {
        if (!isValidPlayer(player)) return false;
        // rows & columns
        for (int i = 0; i < SIZE; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) return true;
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) return true;
        }
        // diagonals
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) return true;
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) return true;
        return false;
    }

    /** Returns true if the game is over (win or draw). */
    public boolean isGameOver() {
        return hasWon(PLAYER_X) || hasWon(PLAYER_O) || isFull();
    }

    /** Evaluates terminal board: +10 for X win, -10 for O win, 0 otherwise. */
    private int evaluate() {
        if (hasWon(PLAYER_X)) return +10;
        if (hasWon(PLAYER_O)) return -10;
        return 0;
    }

    /** Minimax with alpha‑beta pruning. Returns score from X perspective. */
    private int minimax(boolean maximizingPlayer, int depth, int alpha, int beta) {
        int score = evaluate();
        if (score != 0 || isFull()) {
            // Return score adjusted by depth to prefer faster wins / slower losses.
            return score - (maximizingPlayer ? depth : -depth);
        }

        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] cell : emptyCells()) {
                makeMove(cell[0], cell[1], PLAYER_X);
                int eval = minimax(false, depth + 1, alpha, beta);
                undoMove(cell[0], cell[1]);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // beta cut‑off
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] cell : emptyCells()) {
                makeMove(cell[0], cell[1], PLAYER_O);
                int eval = minimax(true, depth + 1, alpha, beta);
                undoMove(cell[0], cell[1]);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // alpha cut‑off
            }
            return minEval;
        }
    }

    /**
     * Returns the optimal move for the given player as int[2] {row, col}.
     * If multiple moves have equal value, the first encountered is returned.
     */
    public int[] bestMove(char player) {
        if (!isValidPlayer(player) || isGameOver()) {
            return null;
        }
        int bestScore = (player == PLAYER_X) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int[] bestCell = null;
        for (int[] cell : emptyCells()) {
            makeMove(cell[0], cell[1], player);
            int moveScore = minimax(player == PLAYER_O, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            undoMove(cell[0], cell[1]);

            if (player == PLAYER_X) {
                if (moveScore > bestScore) {
                    bestScore = moveScore;
                    bestCell = cell;
                }
            } else {
                if (moveScore < bestScore) {
                    bestScore = moveScore;
                    bestCell = cell;
                }
            }
        }
        return bestCell;
    }

    /** Utility validation methods. */
    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    private static boolean isValidPlayer(char p) {
        return p == PLAYER_X || p == PLAYER_O;
    }

    /** Returns a printable representation of the board. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            sb.append("|");
            for (int j = 0; j < SIZE; j++) {
                sb.append(board[i][j] == EMPTY ? ' ' : board[i][j]).append("|");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
