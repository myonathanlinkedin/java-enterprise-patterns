import java.util.*;

public class TicTacToe {
    public static final char EMPTY = ' ';
    public static final char X = 'X';
    public static final char O = 'O';
    private final char[] board; // 0-8 indices

    public TicTacToe() {
        board = new char[9];
        Arrays.fill(board, EMPTY);
    }

    // Copy constructor for testing immutability
    private TicTacToe(char[] src) {
        board = src.clone();
    }

    public char[] getBoardCopy() {
        return board.clone();
    }

    public boolean makeMove(int idx, char player) {
        if (idx < 0 || idx >= 9 || board[idx] != EMPTY) return false;
        board[idx] = player;
        return true;
    }

    public void undoMove(int idx) {
        board[idx] = EMPTY;
    }

    public List<Integer> availableMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) moves.add(i);
        }
        return moves;
    }

    public boolean isWin(char player) {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8}, // rows
            {0,3,6},{1,4,7},{2,5,8}, // cols
            {0,4,8},{2,4,6}          // diags
        };
        for (int[] l : lines) {
            if (board[l[0]] == player && board[l[1]] == player && board[l[2]] == player)
                return true;
        }
        return false;
    }

    public boolean isDraw() {
        return !isWin(X) && !isWin(O) && availableMoves().isEmpty();
    }

    private int evaluate() {
        if (isWin(X)) return +10;
        if (isWin(O)) return -10;
        return 0;
    }

    // Minimax with depth to prefer faster wins / slower losses
    private int minimax(char player, int depth, boolean maximizing) {
        int score = evaluate();
        if (score != 0) return score - depth * (score > 0 ? 1 : -1);
        if (availableMoves().isEmpty()) return 0; // draw

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int move : availableMoves()) {
                makeMove(move, player);
                int val = minimax(O, depth + 1, false);
                undoMove(move);
                best = Math.max(best, val);
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int move : availableMoves()) {
                makeMove(move, player);
                int val = minimax(X, depth + 1, true);
                undoMove(move);
                best = Math.min(best, val);
            }
            return best;
        }
    }

    public int getBestMove(char player) {
        int bestMove = -1;
        int bestScore = (player == X) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        boolean maximizing = (player == X);
        for (int move : availableMoves()) {
            makeMove(move, player);
            int score = minimax(player == X ? O : X, 0, !maximizing);
            undoMove(move);
            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    // Utility for pretty printing (optional)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            sb.append(board[i] == EMPTY ? '-' : board[i]);
            if (i % 3 == 2 && i != 8) sb.append('\n');
        }
        return sb.toString();
    }
}
