import java.util.Set;
import java.util.Stack;

public class FEN implements Piece {

    // list of all fen strings
    private final Stack<String> fenList;
    // list of all cut fen string to determine 3 move repetition
    private final Stack<String> cutFenList;

    // FEN of chess starting position
    public static final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    // cut FEN of chess starting position
    public static final String startCutFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq";

    public FEN() {

        fenList = new Stack<>();
        cutFenList = new Stack<>();
        setFen(startFen);
        fenList.push(startFen);
        cutFenList.push(startCutFen);

    }

    public FEN(String fen) {

        fenList = new Stack<>();
        cutFenList = new Stack<>();
        fenList.push(fen);
        cutFenList.push(getCutFen(fen));

    }

    public String getFen(Board board) {

        String EPTargetSquare = " ";
        String halfMoveClock = " ";
        String fullMoveNum = " ";

        // getting en passant FEN
        if (board.getMoves().isEmpty() || !board.isMoveNormal(board.getLastMove())) {
            EPTargetSquare += "-";
        }
        else {

            Move lastMove = board.getLastMove();

            if (Math.abs(board.getBoard()[lastMove.getTargetRow()][lastMove.getTargetCol()]) == pawnNum && Math.abs(lastMove.getStartRow() - lastMove.getTargetRow()) == 2) {
                EPTargetSquare += lastMove.getTargetNotation().substring(0,1) + (Integer.parseInt(lastMove.getTargetNotation().substring(1)) + board.getTurn());
            } else {
                EPTargetSquare += "-";
            }

        }

        // getting fifty move count FEN
        halfMoveClock += board.getFiftyMoveCount();

        // getting total amount of full moves  FEN
        fullMoveNum += board.getMoves().size() / 2 + 1;

        return getCutFen(board) + EPTargetSquare + halfMoveClock + fullMoveNum;

    }

    public String getCutFen(Board board) {

        StringBuilder piecePlace = new StringBuilder();
        String activeCol = " ";
        String canCastle = " ";

        // getting board FEN
        for (int[] ints : board.getBoard()) {

            int counter = 0;

            for (int anInt : ints) {

                if (anInt != noneNum) {
                    if (counter > 0) {
                        piecePlace.append(counter);
                        counter = 0;
                    }
                    piecePlace.append(board.numToLetter(anInt));
                } else {
                    counter++;
                }

            }

            if (counter > 0) {
                piecePlace.append(counter);
            }

            piecePlace.append("/");

        }

        piecePlace = new StringBuilder(piecePlace.substring(0, piecePlace.length() - 1));

        // getting turn FEN
        if (board.getTurn() == 1) {
            activeCol += "w";
        } else {
            activeCol += "b";
        }

        // getting castling rights FEN
        if (board.getCastlingRights()[0][0]) {canCastle += "K";}
        if (board.getCastlingRights()[0][1]) {canCastle += "Q";}
        if (board.getCastlingRights()[1][0]) {canCastle += "k";}
        if (board.getCastlingRights()[1][1]) {canCastle += "q";}
        if (canCastle.equals(" ")) {canCastle += "-";}

        return piecePlace + activeCol + canCastle;

    }

    public String getCutFen(String fen) {

        int pos = fen.indexOf(" ");

        for (int i = 0; i < 2; i++) {
            pos = fen.indexOf(" ", pos + 1);
        }

        return fen.substring(0, pos);

    }

    public Board setFen(String fen) {

        Board board = new Board();

        // Index 0: piece placement. Index 1: active color. Index 2: castling availability. Index 3: En Passant target square. Index 4: Half move clock. Index 5: Full move number
        String [] fenParts = fen.split(" ");

        // clear board
        board.setBoard(new int[8][8]);

        // new board has new piece locations
        for (Set<Square>[] hashSets : board.getPieceLoc()) {
            for (int j = 0; j < board.getPieceLoc()[0].length; j++) {
                hashSets[j].clear();
            }
        }

        // new board has new value totals
        board.setValue(new int[] {0,0});

        // setting board
        String [] boardFen = fenParts[0].split("/");
        for (int i = 0; i < boardFen.length; i++) {

            char [] chars = boardFen[i].toCharArray();
            int counter = 0;

            for (char aChar : chars) {

                if (Character.isLetter(aChar)) {

                    int pieceNum = board.letterToNum(aChar);

                    // sets board position
                    board.getBoard()[i][counter] = pieceNum;

                    int colorIndex = board.getPieceIndex(i, counter);

                    // sets value total
                    board.addValue(colorIndex, board.pieceNumToValue(pieceNum));
                    // sets pieceLoc
                    board.addPieceLoc(colorIndex, Math.abs(pieceNum) - 1, i, counter);
                    // increments counter
                    counter++;

                } else {
                    counter += Character.getNumericValue(aChar);
                }

            }

        }

        // setting turn
        if (fenParts[1].equals("w")) {
            board.setTurn(1);
        } else {
            board.setTurn(-1);
        }

        // setting castling rights
        String canCastle = fenParts[2];
        board.setCastlingRights(new boolean [][] {{false, false}, {false, false}});
        for (int i = 0; i < canCastle.length(); i++) {

            if (canCastle.charAt(i) == 'K') {board.setCastlingRight(0, 0, true);}
            else if (canCastle.charAt(i) == 'Q') {board.setCastlingRight(0, 1, true);}
            else if (canCastle.charAt(i) == 'k') {board.setCastlingRight(1, 0, true);}
            else if (canCastle.charAt(i) == 'q') {board.setCastlingRight(1, 1, true);}

        }

        // setting 50 move count
        board.setFiftyMoveCount(Integer.parseInt(fenParts[4]));

        return board;

    }

    public void addFen(String fen) {
        fenList.push(fen);
        cutFenList.push(getCutFen(fen));
    }

    public void removeLastFen() {
        fenList.pop();
        cutFenList.pop();
    }

    public String getLastFen() {
        return fenList.peek();
    }

    public Stack<String> getCutFenList() {
        return cutFenList;
    }

}
