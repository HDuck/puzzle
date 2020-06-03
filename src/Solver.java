import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.StdOut;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Solver {
    private MinPQ<Node> boardTree;
    private MinPQ<Node> pq;
    private MinPQ<Node> twinPQ;
    private boolean isTwinSolved = false;

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException();
        }

        Node initialNode = new Node(initial);
        Board twinBoard = initial.twin();
        Node twinNode = new Node(twinBoard);
        pq = new MinPQ<>();
        twinPQ = new MinPQ<>();
        boardTree = new MinPQ<>();

        boardTree.insert(initialNode);
        pq.insert(initialNode);
        twinPQ.insert(twinNode);

        while (!isTwinSolved && !boardTree.min().board.isGoal()) {
            findSolution(pq);
            if (pq.min().board.isGoal()) {
                break;
            }
            findSolution(twinPQ);
            if (twinPQ.min().board.isGoal()) {
                isTwinSolved = true;
                break;
            }
        }
    }

    private static class Node implements Comparable<Node> {
        public Node previousNode = null;
        public Board board;
        public int move = 0;

        public Node(Board b) {
            board = b;
        }

        public Node(Board b, Node pN, int m) {
            previousNode = pN;
            board = b;
            move = m;
        }

        public int compareTo(Node that) {
            int priority1 = calcPriority(this);
            int priority2 = calcPriority(that);

            if (priority1 > priority2) return 1;
            if (priority1 < priority2) return -1;

            if (this.move > that.move) return -1;
            if (this.move < that.move) return 1;

            return 0;
        }

        private int calcPriority(Node node) {
            return node.board.manhattan() + node.move;
        }
    }

    private void findSolution(MinPQ<Node> pq) {
        Node minNode = pq.delMin();
        for (Board neighbor : minNode.board.neighbors()) {
            Node neighborNode = new Node(neighbor, minNode, minNode.move + 1);
            if (minNode.previousNode != null &&
                neighborNode.board.equals(minNode.previousNode.board)
            ) {
                continue;
            }
            boardTree.insert(neighborNode);
            pq.insert(neighborNode);
        }
    }

    // is the initial board solvable? (see below)
    public boolean isSolvable() {
        return !isTwinSolved;
    }

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves() {
        if (!isSolvable()) {
            return -1;
        }
        return pq.min().move;
    }

    // sequence of boards in a shortest solution; null if unsolvable
    public Iterable<Board> solution() {
        if (!isSolvable()) {
            return null;
        }
        return new SolutionBoards();
    }

    private class SolutionBoards implements Iterable<Board> {
        private Board[] boards;

        public SolutionBoards() {
            boards = new Board[moves() + 1];

            Node currNode = pq.min();

            int i = moves();
            while (currNode != null) {
                boards[i--] = currNode.board;
                currNode = currNode.previousNode;
            }
        }

        public Iterator<Board> iterator() {
            return new SolutionIterator();
        }

        private class SolutionIterator implements Iterator<Board> {
            private int iterationIndex = 0;

            public boolean hasNext() {
                return iterationIndex <= moves();
            }

            public Board next() {
                if (!hasNext()) throw new NoSuchElementException();
                return boards[iterationIndex++];
            }
        }
    }

    // test client (see below)
    public static void main(String[] args) {
        // create initial board from file
        In in = new In(args[0]);
        int n = in.readInt();
        int[][] tiles = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tiles[i][j] = in.readInt();
        Board initial = new Board(tiles);

        // solve the puzzle
        Solver solver = new Solver(initial);

        // print solution to standard output
        if (!solver.isSolvable())
            StdOut.println("No solution possible");
        else {
            StdOut.println("Minimum number of moves = " + solver.moves());
            for (Board board : solver.solution())
                StdOut.println(board);
        }
    }
}
