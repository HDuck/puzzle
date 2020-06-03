import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Board {
    private final int[][] boardTiles;
    private int[][] cachedTwinTiles;

    // create a board from an n-by-n array of tiles,
    // where tiles[row][col] = tile at (row, col)
    public Board(int[][] tiles) {
        boardTiles = copy(tiles);
    }

    // string representation of this board
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(dimension()).append("\n");
        for (int row = 0; row < dimension(); row++) {
            for (int col = 0; col < dimension(); col++) {
                result.append(" ").append(boardTiles[row][col]);
            }
            result.append("\n");
        }
        return result.toString();
    }

    // board dimension n
    public int dimension() {
        return boardTiles.length;
    }

    // number of tiles out of place
    public int hamming() {
        int hamming = 0;
        for (int row = 0; row < dimension(); row++) {
            for (int col = 0; col < dimension(); col++) {
                int tile = boardTiles[row][col];
                if (tile == 0) {
                    continue;
                }

                int expectedTile = col + 1 + row * dimension();
                // row = tile / dimension()
                // col = tile % dimension() - 1
                if (tile != expectedTile) {
                    hamming++;
                }
            }
        }
        return hamming;
    }

    // sum of Manhattan distances between tiles and goal
    public int manhattan() {
        int manhattan = 0;
        for (int row = 0; row < dimension(); row++) {
            for (int col = 0; col < dimension(); col++) {
                int tile = boardTiles[row][col];
                if (tile == 0) {
                    continue;
                }

                int round = tile / dimension();
                int offset = tile % dimension();
                int expectedRow = offset == 0 ? round - 1 : round;
                int expectedCol = offset == 0 ? dimension() - 1 : offset - 1;

                if (expectedRow != row) {
                    manhattan += Math.abs(expectedRow - row);
                }

                if (expectedCol != col) {
                    manhattan += Math.abs(expectedCol - col);
                }
            }
        }
        return manhattan;
    }

    // is this board the goal board?
    public boolean isGoal() {
        return hamming() == 0;
    }

    // does this board equal y?
    public boolean equals(Object y) {
        if (y == null) {
            return false;
        }
        return toString().equals(y.toString());
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        return new NeighborBoards();
    }

    private class NeighborBoards implements Iterable<Board> {
        private Board[] neighbors;
        private int neighborsCount = 0;

        public NeighborBoards() {
            neighbors = new Board[4];
            int[] emptyTileCoords = getEmptyTileCoords();
            int row = emptyTileCoords[0];
            int col = emptyTileCoords[1];
            if (row + 1 != dimension()) {
                neighbors[neighborsCount++] = createBottomNeighbor(row, col);
            }
            if (row != 0) {
                neighbors[neighborsCount++] = createTopNeighbor(row, col);
            }
            if (col + 1 != dimension()) {
                neighbors[neighborsCount++] = createRightNeighbor(row, col);
            }
            if (col != 0) {
                neighbors[neighborsCount++] = createLeftNeighbor(row, col);
            }
        }

        public Iterator<Board> iterator() {
            return new NeighborIterator();
        }

        private class NeighborIterator implements Iterator<Board> {
            int iteratedCount = 0;

            public boolean hasNext() {
                return iteratedCount < neighborsCount;
            }

            public Board next() {
                Board neighbor = neighbors[iteratedCount++];
                if (neighbor == null) {
                    throw new NoSuchElementException();
                }
                return neighbor;
            }
        }

        private Board createTopNeighbor(int zeroRow, int zeroCol) {
            int[][] neighborTiles = copy(boardTiles);
            int tile = boardTiles[zeroRow - 1][zeroCol];
            neighborTiles[zeroRow - 1][zeroCol] = 0;
            neighborTiles[zeroRow][zeroCol] = tile;
            return new Board(neighborTiles);
        }

        private Board createRightNeighbor(int zeroRow, int zeroCol) {
            int[][] neighborTiles = copy(boardTiles);
            int tile = boardTiles[zeroRow][zeroCol + 1];
            neighborTiles[zeroRow][zeroCol + 1] = 0;
            neighborTiles[zeroRow][zeroCol] = tile;
            return new Board(neighborTiles);
        }

        private Board createBottomNeighbor(int zeroRow, int zeroCol) {
            int[][] neighborTiles = copy(boardTiles);
            int tile = boardTiles[zeroRow + 1][zeroCol];
            neighborTiles[zeroRow + 1][zeroCol] = 0;
            neighborTiles[zeroRow][zeroCol] = tile;
            return new Board(neighborTiles);
        }

        private Board createLeftNeighbor(int zeroRow, int zeroCol) {
            int[][] neighborTiles = copy(boardTiles);
            int tile = boardTiles[zeroRow][zeroCol - 1];
            neighborTiles[zeroRow][zeroCol - 1] = 0;
            neighborTiles[zeroRow][zeroCol] = tile;
            return new Board(neighborTiles);
        }

        private int[] getEmptyTileCoords() {
            int[] result = new int[2];
            result[0] = -1;
            result[1] = 1;
            for (int row = 0; row < dimension(); row++) {
                for (int col = 0; col < dimension(); col++) {
                    int tile = boardTiles[row][col];
                    if (tile == 0) {
                        result[0] = row;
                        result[1] = col;
                        break;
                    }
                }
                if (result[0] != -1) {
                    break;
                }
            }
            return result;
        }
    }

    private int[][] copy(int[][] tiles) {
        int n = tiles.length;
        int[][] result = new int[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                result[row][col] = tiles[row][col];
            }
        }
        return result;
    }

    // a board that is obtained by exchanging any pair of tiles
    public Board twin() {
        if (cachedTwinTiles == null) {
            int[] tileExceptions = new int[2];
            tileExceptions[0] = 0;

            int[] firstTileCoords = getRandomizedTileCoords(tileExceptions);
            int firstTileRow = firstTileCoords[0];
            int firstTileCol = firstTileCoords[1];
            int firstTile = boardTiles[firstTileRow][firstTileCol];

            tileExceptions[1] = firstTile;

            int[] secondTileCoords = getRandomizedTileCoords(tileExceptions);
            int secondTileRow = secondTileCoords[0];
            int secondTileCol = secondTileCoords[1];
            int secondTile = boardTiles[secondTileRow][secondTileCol];

            cachedTwinTiles = copy(boardTiles);
            cachedTwinTiles[firstTileRow][firstTileCol] = secondTile;
            cachedTwinTiles[secondTileRow][secondTileCol] = firstTile;
        }

        return new Board(cachedTwinTiles);
    }

    private int[] getRandomizedTileCoords(int[] tileExceptions) {
        int[] result = new int[2];
        boolean isUnique = false;
        int randomRow = -1;
        int randomCol = -1;

        while (!isUnique) {
            randomRow = StdRandom.uniform(dimension());
            randomCol = StdRandom.uniform(dimension());

            for (int tile : tileExceptions) {
                isUnique = tile != boardTiles[randomRow][randomCol];
                if (!isUnique) {
                    break;
                }
            }
        }

        result[0] = randomRow;
        result[1] = randomCol;
        return result;
    }

    // unit testing (not graded)
    public static void main(String[] args) {
        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board board = new Board(tiles);
        Board board2 = new Board(tiles);

        StdOut.println(board);
        StdOut.println(board2);
        StdOut.println(board.hamming());
        StdOut.println(board.manhattan());
        int index = 1;
        for (Board neighbor : board.neighbors()) {
            StdOut.println("neighbor" + index++);
            StdOut.println(neighbor);
        }
    }
}
