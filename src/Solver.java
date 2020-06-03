import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.StdOut;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Solver {
    private Board[] solutionBoards;

    // find a solution to the initial board (using the A* algorithm)
    public Solver(Board initial) {
        if (initial == null) {
            throw new IllegalArgumentException();
        }

        Node initialNode = new Node(initial);
        Board twinBoard = initial.twin();
        Node twinNode = new Node(twinBoard);

        MinPQ<Node> pq = new MinPQ<>();
        MinPQ<Node> twinPQ = new MinPQ<>();
        pq.insert(initialNode);
        twinPQ.insert(twinNode);

        boolean isTwinSolved = false;
        while (true) {
            if (pq.min().board.isGoal()) {
                break;
            }
            findSolution(pq);

            if (twinPQ.min().board.isGoal()) {
                isTwinSolved = true;
                break;
            }
            findSolution(twinPQ);
        }

        if (!isTwinSolved) {
            int movesMade = pq.min().move;
            solutionBoards = new Board[movesMade + 1];

            Node currNode = pq.min();
            while (currNode != null) {
                solutionBoards[movesMade--] = currNode.board;
                currNode = currNode.previousNode;
            }
        }
    }

    private static class Node implements Comparable<Node> {
        public int cachedPriority = -1;
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
            if (node.cachedPriority == -1) {
                node.cachedPriority = node.board.manhattan() + node.move;
            }
            return node.cachedPriority;
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
            pq.insert(neighborNode);
        }
    }

    // is the initial board solvable? (see below)
    public boolean isSolvable() {
        return solutionBoards != null;
    }

    // min number of moves to solve initial board; -1 if unsolvable
    public int moves() {
        if (!isSolvable()) {
            return -1;
        }
        return solutionBoards.length - 1;
    }

    // sequence of boards in a shortest solution; null if unsolvable
    public Iterable<Board> solution() {
        if (!isSolvable()) {
            return null;
        }
        return new SolutionBoards();
    }

    private class SolutionBoards implements Iterable<Board> {
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
                return solutionBoards[iterationIndex++];
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
