import java.util.Set;

public class Perft {

    private final String fen;
    private final int depth;
    private final Game game;
    private final MoveGenerator moveGenerator;

    public Perft(String fen, int depth) {

        this.fen = fen;
        this.depth = depth;
        game = new Game(fen);
        moveGenerator = new MoveGenerator();

    }

    public void printNodeCount() {

        for (int i = 1; i <= depth; i++) {
            long currentTime = System.currentTimeMillis();
            System.out.println("Depth: " + i + " ply\tResult: " + getNodes(i) + " positions     Time: " + (System.currentTimeMillis() - currentTime) + " milliseconds");
        }

    }

    public int getNodes(int depth) {

        if (depth == 1) {
            return moveGenerator.generateLegalMoves(game.getBoard()).size();
        }

        int numPos = 0;
        Set<Move> legalMoves = moveGenerator.generateLegalMoves(game.getBoard());

        for (Move move : legalMoves) {

            game.move(move);
            numPos += getNodes(depth-1);
            game.unMove();
        }

        return numPos;

    }


    public String getStartingFen() {
        return fen;
    }

    public int getDepth() {
        return depth;
    }

}
