import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Scanner;

public class Game {

    // How game ends
    private GameEndType gameEnd;
    // Board
    private Board board;
    // All FEN functionality
    private final FEN fenctionality;
    // Move generator
    private final MoveGenerator moveGenerator;
    // Evaluator
    private final Evaluator evaluator;

    // black ansi code
    private static final String BLACK_COL = "\u001B[90m";
    // white ansi code
    private static final String WHITE_COL = "\u001B[97m";
    // ansi reset color code
    private static final String RESET_COL = "\u001B[0m";

    // starts game from the starting position
    public Game() {

        board = new Board();
        fenctionality = new FEN();
        moveGenerator = new MoveGenerator();
        evaluator = new Evaluator();

    }

    // starts game from a specific FEN string position
    public Game(String fen) {

        fenctionality = new FEN(fen);
        board = fenctionality.setFen(fen);
        moveGenerator = new MoveGenerator();
        evaluator = new Evaluator();

    }

    public void printRules() {

        System.out.println("Welcome to Chess!");
        System.out.println("The rules of the game are as follows: ");
        System.out.println("The 2 players, " + WHITE_COL + "White" + RESET_COL + " and " + BLACK_COL + "Black" + RESET_COL + ", alternate moves with " + WHITE_COL + "White" + RESET_COL + " going first");
        System.out.println("The format of a normal move is the starting square and then the ending square. ex: e2e4");
        System.out.println("The format of castling is the king starting square to the king end square");
        System.out.println("The format of en passant is the pawn starting square to the pawn end square");
        System.out.println("The format of promotion is the pawn starting square to the pawn end square, followed by the piece");
        System.out.println("For example, to promote a pawn to a queen from a7 to a8, type 'a7a8q'");
        System.out.println("To resign, type 'i resign :('. To offer a draw, type 'draw?'. To accept a draw, type 'yes'");
        System.out.println("Have fun!");

    }

    public void play() {

        printRules();
        Scanner input = new Scanner(System.in);

        while (gameEnd == null) {

            board.printBoard();
            System.out.println("Eval: " + evaluator.getScore(board));
            if (moveGenerator.isInCheck()) {
                System.out.println("check!");
            }
            System.out.println("It is now " + turnToPlayer() + "'s turn to move");

            String userInput = input.nextLine();

            switch (userInput.toLowerCase()) {
                case "i resign :(" -> gameEnd = turnToResignType();
                case "draw?" -> {
                    Scanner agreement = new Scanner(System.in);
                    if (agreement.nextLine().equals("yes")) {
                        gameEnd = GameEndType.AGREEMENT;
                    } else {
                        System.out.println("no draw");
                    }
                }
                default -> {
                    Move move = isValidMove(userInput);
                    if (move != null) {
                        move(move);
                    } else {
                        System.out.println("not a valid move");
                    }
                }
            }

            isCheckmate();
            isDraw();

        }

        board.printBoard();
        System.out.println(gameEnd.getValue());

    }

    public void move(Move move) {

        switch (move.getType()) {
            case NORMAL -> // normal moves
                board.normalMove(move);
            case CASTLE_KING, CASTLE_QUEEN -> // castling
                board.castle(move);
            case EN_PASSANT -> // en passant
                board.enPassant(move);
            case PROMOTE_BISHOP, PROMOTE_KNIGHT, PROMOTE_ROOK, PROMOTE_QUEEN -> // pawn promotion
                board.promote(move);
        }

        board.incrementTurn();
        board.addMove(move);
        fenctionality.addFen(fenctionality.getFen(board));

    }

    public void unMove() {

        fenctionality.removeLastFen();
        board = fenctionality.setFen(fenctionality.getLastFen());

    }

    // returns move if true, returns null if false
    public Move isValidMove(String input) {

        Set<Move> legalMoves = moveGenerator.generateLegalMoves(board);

        for (Move move : legalMoves) {
            if (move.toString().equals(input)) {
                return move;
            }
        }

        return null;

    }

    // checks if one side has been checkmated
    public boolean isCheckmate() {

        boolean noLegalMoves = moveGenerator.generateLegalMoves(board).isEmpty();
        boolean isCheck = moveGenerator.isInCheck();

        if (noLegalMoves && isCheck) {
            gameEnd = turnToCheckmateType();
            return true;
        }

        return false;

    }

    // checks if game is a draw by stalemated, draw by repetition, insufficient material, or 50 move rule
    public boolean isDraw() {

        return drawByStalemate() || drawByRepetition() || drawByMaterial() || drawBy50MoveRule();

    }

    // checks draw by stalemate
    public boolean drawByStalemate() {

        boolean noLegalMoves = moveGenerator.generateLegalMoves(board).isEmpty();
        boolean isCheck = moveGenerator.isInCheck();

        if (noLegalMoves && !isCheck) {
            gameEnd = GameEndType.STALEMATE;
            return true;
        }

        return false;

    }

    // checks draw by repetition
    public boolean drawByRepetition() {

        Stack<String> cutFenList = fenctionality.getCutFenList();

        class MutableInt {
            int value = 1;
            public void increment() {value++;}
            public int get() {return value;}
        }

        Map<String, MutableInt> fenCount = new HashMap<>(cutFenList.size());

        for (String cutFen: cutFenList) {

            MutableInt count = fenCount.get(cutFen);
            if (count == null) {
                fenCount.put(cutFen, new MutableInt());
            } else if (count.get() == 2) {
                gameEnd = GameEndType.REPETITION;
                return true;
            } else {
                count.increment();
            }

        }

        return false;

    }

    // checks draw by insufficient material
    public boolean drawByMaterial() {

        if (board.noPawnsLeft() && board.getValue()[0] < 4 && board.getValue()[1] < 4) {
            gameEnd = GameEndType.INSUFFICIENT_MATERIAL;
            return true;
        }

        return false;

    }

    // checks draw by 50 move rule
    public boolean drawBy50MoveRule() {

        if (board.getFiftyMoveCount() == 50) {
            gameEnd = GameEndType.FIFTY_MOVE_RULE;
            return true;
        }

        return false;

    }

    public GameEndType turnToResignType() {
        if (board.getTurn() == 1) {
            return GameEndType.BLACK_RESIGN;
        }
        return GameEndType.WHITE_RESIGN;
    }

    public GameEndType turnToTimeOutType() {
        if (board.getTurn() == 1) {
            return GameEndType.BLACK_TIMEOUT;
        }
        return GameEndType.WHITE_TIMEOUT;
    }

    public GameEndType turnToCheckmateType() {
        if (board.getTurn() == 1) {
            return GameEndType.BLACK_CHECKMATE;
        }
        return GameEndType.WHITE_CHECKMATE;
    }

    public String turnToPlayer() {
        if (board.getTurn() == 1) {
            return "White";
        }
        return "Black";
    }

    // Getter methods:
    public Board getBoard() {
        return board;
    }

    public Evaluator getEvaluator() { return evaluator; }

}
