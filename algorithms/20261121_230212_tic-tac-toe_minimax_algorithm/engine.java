package tictactoe;

import tictactoe.Types.Board;
import tictactoe.Types.Coord;
import tictactoe.Types.Player;

/**
 * Core Minimax engine for Tic-Tac-Toe.
 */
public final class Engine {

    private Engine() {
        // Prevent instantiation.
    }

    /**
     * Returns the optimal move for the given player on the current board.
     *
     * @param board  current board (will not be mutated)
     * @param player player to move
     * @return coordinate of the best move
     */
    public static Coord getBestMove(Board board, Player player) {
        int bestScore = (player == Player.X) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Coord bestMove = null;

        for (Coord move : board.emptyCells()) {
            board.makeMove(move, player);
            int score = minimax(board, 0, player.opponent(), player);
            board.undoMove(move);

            if (player == Player.X) {
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

        if (bestMove == null) {
            throw new IllegalStateException("No moves available");
        }
        return bestMove;
    }

    /**
     * Minimax recursion.
     *
     * @param board   current board
     * @param depth   recursion depth (used for tie-breaking)
     * @param current player whose turn it is in this node
     * @param maximizer original player who wants to maximize the score
     * @return evaluation score
     */
    private static int minimax(Board board, int depth, Player current, Player maximizer) {
        Player winner = board.checkWinner();
        if (winner != null) {
            return evaluateWinner(winner, maximizer, depth);
        }
        if (board.isFull()) {
            return 0; // draw
        }

        if (current == maximizer) {
            int maxEval = Integer.MIN_VALUE;
            for (Coord move : board.emptyCells()) {
                board.makeMove(move, current);
                int eval = minimax(board, depth + 1, current.opponent(), maximizer);
                board.undoMove(move);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Coord move : board.emptyCells()) {
                board.makeMove(move, current);
                int eval = minimax(board, depth + 1, current.opponent(), maximizer);
                board.undoMove(move);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }

    /**
     * Scoring function: +10 for X win, -10 for O win, adjusted by depth to prefer faster wins.
     *
     * @param winner    player who won
     * @param maximizer player who initiated the search
     * @param depth     depth at which win occurred
     * @return integer score
     */
    private static int evaluateWinner(Player winner, Player maximizer, int depth) {
        int baseScore = (winner == Player.X) ? 10 : -10;
        // If maximizer is O, invert the sign to keep perspective consistent.
        if (maximizer == Player.O) {
            baseScore = -baseScore;
        }
        // Prefer quicker victories and delayed defeats.
        return baseScore - depth;
    }
}
