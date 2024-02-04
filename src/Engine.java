import java.util.Set;

public class Engine {

    private final Game game;
    private final MoveGenerator moveGenerator;
    private final Evaluator evaluator;
    private Move bestMove;

    public Engine() {

        game = new Game();
        moveGenerator = new MoveGenerator();
        evaluator = new Evaluator();
        bestMove = null;

    }

    public Engine(String fen) {

        game = new Game(fen);
        moveGenerator = new MoveGenerator();
        evaluator = new Evaluator();
        bestMove = null;

    }

    public int getBestMove(int depth) {

        int bestScore = 0;

        if (depth == 0) {
            return evaluator.getScore(game.getBoard());
        }

        Set<Move> legalMoves = moveGenerator.generateLegalMoves(game.getBoard());

        for (Move move : legalMoves) {

            game.move(move);

            int bestEval = getBestMove(depth-1);
            if (bestEval > bestScore) {
                bestScore = bestEval;
                bestMove = move;
            }
            game.unMove();
        }

        return bestScore;

    }

    public Move getBest() {
        return bestMove;
    }

}
