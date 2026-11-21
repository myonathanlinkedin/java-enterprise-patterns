package tictactoe;

import java.util.Objects;

/**
 * Domain types for Tic-Tac-Toe.
 */
public final class Types {

    /** Players in the game. */
    public enum Player {
        X('X'), O('O');

        private final char symbol;

        Player(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        /** Returns the opponent of the current player. */
        public Player opponent() {
            return this == X ? O : X;
        }
    }

    /** Immutable coordinate on the board. */
    public static final class Coord {
        public final int row;
        public final int col;

        public Coord(int row, int col) {
            if (row < 0 || row > 2 || col < 0 || col > 2) {
                throw new IllegalArgumentException("Coordinates must be in range [0,2]");
            }
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Coord)) return false;
            Coord coord = (Coord) o;
            return row == coord.row && col == coord.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "Coord(" + row + "," + col + ")";
        }
    }

    /** Representation of the 3x3 board. */
    public static final class Board {
        private final char[][] cells; // 'X', 'O', or ' '

        public Board() {
            cells = new char[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    cells[i][j] = ' ';
                }
            }
        }

        /** Copy constructor for safe recursion. */
        public Board(Board other) {
            cells = new char[3][3];
            for (int i = 0; i < 3; i++) {
                System.arraycopy(other.cells[i], 0, this.cells[i], 0, 3);
            }
        }

        /** Places a player's symbol at the given coordinate. */
        public void makeMove(Coord c, Player p) {
            if (cells[c.row][c.col] != ' ') {
                throw new IllegalArgumentException("Cell already occupied: " + c);
            }
            cells[c.row][c.col] = p.getSymbol();
        }

        /** Removes a symbol from the given coordinate (undo). */
        public void undoMove(Coord c) {
            cells[c.row][c.col] = ' ';
        }

        /** Returns true if the board is full. */
        public boolean isFull() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (cells[i][j] == ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        /** Returns the winner player if any, otherwise null. */
        public Player checkWinner() {
            // rows & columns
            for (int i = 0; i < 3; i++) {
                if (cells[i][0] != ' ' && cells[i][0] == cells[i][1] && cells[i][1] == cells[i][2]) {
                    return playerFromSymbol(cells[i][0]);
                }
                if (cells[0][i] != ' ' && cells[0][i] == cells[1][i] && cells[1][i] == cells[2][i]) {
                    return playerFromSymbol(cells[0][i]);
                }
            }
            // diagonals
            if (cells[0][0] != ' ' && cells[0][0] == cells[1][1] && cells[1][1] == cells[2][2]) {
                return playerFromSymbol(cells[0][0]);
            }
            if (cells[0][2] != ' ' && cells[0][2] == cells[1][1] && cells[1][1] == cells[2][0]) {
                return playerFromSymbol(cells[0][2]);
            }
            return null;
        }

        /** Returns a list of empty coordinates. */
        public java.util.List<Coord> emptyCells() {
            java.util.List<Coord> list = new java.util.ArrayList<>(9);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (cells[i][j] == ' ') {
                        list.add(new Coord(i, j));
                    }
                }
            }
            return list;
        }

        /** Returns a printable string of the board. */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                sb.append(" ");
                for (int j = 0; j < 3; j++) {
                    sb.append(cells[i][j] == ' ' ? '_' : cells[i][j]);
                    if (j < 2) sb.append(" | ");
                }
                sb.append('\n');
                if (i < 2) sb.append("---+---+---\n");
            }
            return sb.toString();
        }

        private static Player playerFromSymbol(char sym) {
            return sym == 'X' ? Player.X : Player.O;
        }
    }
}
