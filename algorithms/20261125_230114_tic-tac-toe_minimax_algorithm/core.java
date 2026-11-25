import java.util.ArrayList;
import java.util.List;

public class TicTacToe {
    public static final int SIZE = 9;
    public static final char EMPTY = ' ';
    public static final char X = 'X';
    public static final char O = 'O';

    private final char[] board;
    private char currentPlayer;

    public TicTacToe() {
        board = new char[SIZE];
        for (int i = 0; i < SIZE; i++) board[i] = EMPTY;
        currentPlayer = X;
    }

    // copy constructor
    private TicTacToe(char[] board, char currentPlayer) {
        this.board = board.clone();
        this.currentPlayer = currentPlayer;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public char[] getBoard() {
        return board.clone();
    }

    public void setBoard(char[] newBoard, char player) {
        if (newBoard.length != SIZE) throw new IllegalArgumentException();
        System.arraycopy(newBoard, 0, board, 0, SIZE);
        currentPlayer = player;
    }

    public List<Integer> availableMoves() {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            if (board[i] == EMPTY) moves.add(i);
        }
        return moves;
    }

    public boolean makeMove(int idx) {
        if (idx < 0 || idx >= SIZE || board[idx] != EMPTY) return false;
        board[idx] = currentPlayer;
        currentPlayer = (currentPlayer == X) ? O : X;
        return true;
    }

    public void undoMove(int idx) {
        if (idx < 0 || idx >= SIZE) return;
        board[idx] = EMPTY;
        currentPlayer = (currentPlayer == X) ? O : X;
    }

    public char checkWinner() {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
        };
        for (int[] l : lines) {
            if (board[l[0]] != EMPTY &&
                board[l[0]] == board[l[1]] &&
                board[l[1]] == board[l[2]]) {
                return board[l[0]];
            }
        }
        return EMPTY;
    }

    public boolean isFull() {
        for (char c : board) if (c == EMPTY) return false;
        return true;
    }

    // Minimax with alpha-beta, returns score from X perspective
    private int minimax(int depth, int alpha, int beta, boolean maximizing) {
        char winner = checkWinner();
        if (winner == X) return 10 - depth;
        if (winner == O) return depth - 10;
        if (isFull()) return 0;

        if (maximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int move : availableMoves()) {
                makeMove(move);
                int eval = minimax(depth + 1, alpha, beta, false);
                undoMove(move);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int move : availableMoves()) {
                makeMove(move);
                int eval = minimax(depth + 1, alpha, beta, true);
                undoMove(move);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    // Public API: best move for current player
    public int bestMove() {
        List<Integer> moves = availableMoves();
        int bestScore = (currentPlayer == X) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int bestMove = -1;
        for (int move : moves) {
            makeMove(move);
            int score = minimax(0, Integer.MIN_VALUE, Integer.MAX_VALUE, currentPlayer == O);
            undoMove(move);
            if (currentPlayer == X) {
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

    // Utility for debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SIZE; i++) {
            sb.append(board[i] == EMPTY ? '-' : board[i]);
            if (i % 3 == 2 && i != SIZE - 1) sb.append('\n');
        }
        return sb.toString();
    }
}
