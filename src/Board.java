import java.util.*;

public class Board implements Piece {

    // 2d array of squares
    private int [][] board;
    // 1 if white's turn, -1 if black's turn
    private int turn;
    // Arraylist of all the moves played
    private final Stack<Move> moves;
    // if castling is available. Index 0 for white, index 1 for black : Index 0 for King side, index 1 for queen side
    private boolean [][] castlingRights;
    // total value of pieces, Index 0 for white, index 1 for black
    private int [] value;
    // piece locations in int notation. First index: white or black. Second index: Piece index
    private Set<Square>[][] pieceLoc;
    // counts how many moves without pawn move or capture
    private int fiftyMoveCount;

    // black ansi code
    private static final String BLACK_COL = "\u001B[90m";
    // white ansi code
    private static final String WHITE_COL = "\u001B[97m";
    // ansi reset color code
    private static final String RESET_COL = "\u001B[0m";


    public Board() {

        board = new int [8][8];
        setStartingBoard();

        turn = 1;

        moves = new Stack<>();

        castlingRights = new boolean[2][2];
        castlingRights[0][0] = castlingRights[0][1] = castlingRights[1][0] = castlingRights[1][1] = true;

        value = new int [2];
        setStartingValue();

        pieceLoc = new HashSet[2][6];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                pieceLoc[i][j] = new HashSet<>();
            }
        }
        setStartingPieceLoc();

        fiftyMoveCount = 0;

    }

    public Board(Board other) {

        this.board = new int[8][8];
        for (int i = 0; i < 8; i++) {
            this.board[i] = other.board[i].clone();
        }

        this.turn = other.turn;

        this.moves = new Stack<>();
        this.moves.addAll(other.moves);

        this.castlingRights = new boolean[2][2];
        for (int i = 0; i < 2; i++) {
            this.castlingRights[i] = other.castlingRights[i].clone();
        }

        this.value = other.value.clone();

        this.pieceLoc = new HashSet[2][6];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                this.pieceLoc[i][j] = new HashSet<>();
                for (Square square: other.pieceLoc[i][j]) {
                    this.pieceLoc[i][j].add(new Square(square));
                }
            }
        }

        this.fiftyMoveCount = other.fiftyMoveCount;

    }

    public void printBoard() {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

        for (int i = 0; i < board.length; i++) {

            System.out.print(7 - i + 1 + " ");

            for (int j = 0; j < board[0].length; j++) {

                String squareColor;
                String piece;
                String pieceColor;

                // determines color of the square
                if ((i + j) % 2 == 0) {squareColor = WHITE_COL;}
                else {squareColor = BLACK_COL;}

                // determines what type of piece
                piece = String.valueOf(numToLetter(board[i][j]));

                // determines the color of the piece
                if (isPieceWhite(i, j)) {pieceColor = WHITE_COL;}
                else {pieceColor = BLACK_COL;}

                System.out.print(squareColor + "[" + pieceColor + piece + squareColor + "]" + RESET_COL);
            }

            System.out.println();
        }

        System.out.println("   a  b  c  d  e  f  g  h ");
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }

    // sets up pieces on the board
    public void setStartingBoard() {

        // first index for white, second index for black
        int [] multiplier = {1, -1};
        int [] pawnStartRow = {6, 1};
        int [] pieceStartRow = {7, 0};

        // first iteration sets white pieces, second iteration sets black pieces
        for (int i = 0; i < 2; i++) {

            for (int j = 0; j < 8; j++) {
                board[pawnStartRow[i]][j] = pawnNum * multiplier[i];
            }

            board[pieceStartRow[i]][2] = board[pieceStartRow[i]][5] = bishopNum * multiplier[i];
            board[pieceStartRow[i]][1] = board[pieceStartRow[i]][6] = knightNum * multiplier[i];
            board[pieceStartRow[i]][0] = board[pieceStartRow[i]][7] = rookNum * multiplier[i];
            board[pieceStartRow[i]][3] = queenNum * multiplier[i];
            board[pieceStartRow[i]][4] = kingNum * multiplier[i];

        }

    }

    // sets piece locations in int notation in pieceLoc
    public void setStartingPieceLoc() {

        // first index for white, second index for black
        int [] pawnStartRow = {6, 1};
        int [] pieceStartRow = {7, 0};

        // first iteration sets white piece locations, second iteration sets black piece locations
        for (int i = 0; i < 2; i++) {

            for (int j = 0; j < 8; j++) {
                pieceLoc[i][pawnIndex].add(new Square(pawnStartRow[i], j));
            }

            pieceLoc[i][bishopIndex].add(new Square(pieceStartRow[i], 2));
            pieceLoc[i][bishopIndex].add(new Square(pieceStartRow[i], 5));
            pieceLoc[i][knightIndex].add(new Square(pieceStartRow[i], 1));
            pieceLoc[i][knightIndex].add(new Square(pieceStartRow[i], 6));
            pieceLoc[i][rookIndex].add(new Square(pieceStartRow[i], 0));
            pieceLoc[i][rookIndex].add(new Square(pieceStartRow[i], 7));
            pieceLoc[i][queenIndex].add(new Square(pieceStartRow[i], 3));
            pieceLoc[i][kingIndex].add(new Square(pieceStartRow[i], 4));

        }

    }

    // sets values of value array
    public void setStartingValue() {

        value[0] = value[1] = 39;

    }

    // performs all castling functionality
    public void castle(Move move) {

        int row = calcStartKingRow();
        int colorIndex = turnToColorIndex();
        int [][] pieceCol = {{6, 2}, {7, 0}, {5, 3}};
        int pieceColIndex;

        if (move.getType() == MoveType.CASTLE_KING) { // king side castle
            KSCastle();
            pieceColIndex = 0;
        }
        else { // queen side castle
            QSCastle();
            pieceColIndex = 1;
        }

        // update 50 move count
        fiftyMoveCount++;
        // update castling rights
        castlingRights[colorIndex][0] = false;
        castlingRights[colorIndex][1] = false;
        // changes pieceLoc for king
        pieceLoc[colorIndex][kingIndex].remove(new Square(row, 4));
        pieceLoc[colorIndex][kingIndex].add(new Square(row, pieceCol[0][pieceColIndex]));
        // changes pieceLoc for rook
        pieceLoc[colorIndex][rookIndex].remove(new Square(row, pieceCol[1][pieceColIndex]));
        pieceLoc[colorIndex][rookIndex].add(new Square(row, pieceCol[2][pieceColIndex]));

    }

    // castle king side
    public void KSCastle() {

        int row = calcStartKingRow();

        board[row][6] = board[row][4]; // moves king
        board[row][5] = board[row][7]; // moves rook
        board[row][4] = board[row][7] = noneNum; // sets og squares back to normal

    }

    // castle queen side
    public void QSCastle() {

        int row = calcStartKingRow();

        board[row][2] = board[row][4]; // moves king
        board[row][3] = board[row][0]; // moves rook
        board[row][4] = board[row][0] = noneNum; // sets og squares back to normal

    }

    // performs all En Passant functionality
    public void enPassant(Move move) {

        int startRow = move.getStartRow();
        int startCol = move.getStartCol();
        int targetRow = move.getTargetRow();
        int targetCol = move.getTargetCol();
        int colorIndex = turnToColorIndex();
        int oppIndex = turnToOppColorIndex();

        // updates value array
        value[oppIndex]--;

        // moves pieces on the board
        board[targetRow][targetCol] = board[startRow][startCol];
        board[startRow][startCol] = board[startRow][targetCol] = noneNum;

        // removes opponents pieceLoc
        pieceLoc[oppIndex][pawnIndex].remove(new Square(startRow, targetCol));
        // changes pieceLoc for pawn that did en passant
        pieceLoc[colorIndex][pawnIndex].remove(new Square(startRow, startCol));
        pieceLoc[colorIndex][pawnIndex].add(new Square(targetRow, targetCol));

    }

    // performs all promotion functionality
    public void promote(Move move) {

        int promotionPiece = move.getType().getValue() * turn;
        int startRow = move.getStartRow();
        int startCol = move.getStartCol();
        int targetRow = move.getTargetRow();
        int targetCol = move.getTargetCol();
        int endSquare = board[targetRow][targetCol];
        int colorIndex = turnToColorIndex();
        int oppIndex = turnToOppColorIndex();

        // updates value array
        value[colorIndex] += pieceNumToValue(promotionPiece) - pieceNumToValue(pawnNum);
        value[oppIndex] -= pieceNumToValue(endSquare);

        // updates castling rights if opponents rook is taken
        if (Math.abs(endSquare) == rookNum) {

            if (targetCol == 0) {castlingRights[oppIndex][1] = false;}
            else if (targetCol == 7) {castlingRights[oppIndex][0] = false;}

        }

        // updates pieceLoc
        if (move.isCapture()) {
            // removes opponents pieceLoc
            pieceLoc[oppIndex][Math.abs(endSquare) - 1].remove(new Square(targetRow, targetCol));
        }
        // removes old position of your piece
        pieceLoc[colorIndex][pawnIndex].remove(new Square(startRow, startCol));
        // adds your piece to the end
        pieceLoc[colorIndex][Math.abs(promotionPiece) - 1].add(new Square(targetRow, targetCol));

        // moves piece on the board
        board[targetRow][targetCol] = promotionPiece;
        board[startRow][startCol] = noneNum;

    }

    // performs all non En Passant and Castling functionality
    public void normalMove(Move move) {

        int startRow = move.getStartRow();
        int startCol = move.getStartCol();
        int targetRow = move.getTargetRow();
        int targetCol = move.getTargetCol();
        int startSquare = board[startRow][startCol];
        int endSquare = board[targetRow][targetCol];
        int colorIndex = turnToColorIndex();
        int oppIndex = turnToOppColorIndex();

        // checks if count should be incremented bc no capture or pawn move (counting for 50 move draw rule)
        if (!move.isCapture() && Math.abs(startSquare) != pawnNum) {
            fiftyMoveCount++;
        }

        // checks if castling rights have changed and changes the tracker of which square the king is on
        if (Math.abs(startSquare) == kingNum) {

            castlingRights[colorIndex][0] = false;
            castlingRights[colorIndex][1] = false;

        } else if (Math.abs(startSquare) == rookNum) {

            if (startCol == 0) {castlingRights[colorIndex][1] = false;}
            else if (startCol == 7) {castlingRights[colorIndex][0] = false;}

        }

        // updates castling rights if opponents rook is taken
        if (Math.abs(endSquare) == rookNum && targetRow == calcPromoteRow()) {

            if (targetCol == 0) {castlingRights[oppIndex][1] = false;}
            else if (targetCol == 7) {castlingRights[oppIndex][0] = false;}

        }

        // updates value array
        value[oppIndex] -= pieceNumToValue(endSquare);

        // updates pieceLoc
        if (move.isCapture()) {
            // removes opponents pieceLoc
            pieceLoc[oppIndex][Math.abs(endSquare) - 1].remove(new Square(targetRow, targetCol));
        }
        // removes old position of your piece
        pieceLoc[colorIndex][Math.abs(startSquare) - 1].remove(new Square(startRow, startCol));
        // adds your piece to the end
        pieceLoc[colorIndex][Math.abs(startSquare) - 1].add(new Square(targetRow, targetCol));

        // moves piece on the board
        board[targetRow][targetCol] = startSquare;
        board[startRow][startCol] = noneNum;

    }

    // checks if a square is in bounds of the board 2d array
    public boolean isInBounds(int targetRow, int targetCol) {

        return targetRow >= 0 && targetRow <= 7 && targetCol >= 0 && targetCol <= 7;

    }

    // checks if piece is white
    public boolean isPieceWhite(int row, int col) {

        return board[row][col] > 0;

    }

    // checks if piece is black
    public boolean isPieceBlack(int row, int col) {

        return board[row][col] < 0;

    }

    // checks if the piece on a square is the right color for whose turn it is
    public boolean isPieceTurn(int row, int col) {

        return turn * board[row][col] > 0;

    }

    // checks if the piece is the wrong color for whose turn it is
    public boolean isNotPieceTurn(int row, int col) {

        return !isPieceTurn(row, col) && board[row][col] != 0;

    }

    // returns true if there are no pawns left on the board
    public boolean noPawnsLeft() {

        return pieceLoc[0][pawnIndex].size() == 0 && pieceLoc[1][pawnIndex].size() == 0;

    }

    // returns if move is a normal move
    public boolean isMoveNormal(Move move) {

        return move.getType() == MoveType.NORMAL;

    }


    // returns the row that the pawn starts on depending on whose turn it is
    public int calcStartPawnRow() {

        if (turn == 1) {
            return 6;
        }
        return 1;

    }

    // returns the row that the pawn starts on depending on whose turn it is
    public int calcOppStartPawnRow() {

        if (turn == 1) {
            return 1;
        }
        return 6;

    }

    // returns the row that the pawn promotes on depending on whose turn it is
    public int calcPromoteRow() {

        if (turn == 1) {
            return 0;
        }
        return 7;

    }

    // returns the row that the king starts on depending on whose turn it is
    public int calcStartKingRow() {

        if (turn == 1) {
            return 7;
        }
        return 0;

    }

    // returns the row that your pawn has to be in for en passant
    public int calcEPRow() {

        if (turn == 1) {
            return 3;
        }
        return 4;

    }

    // returns index of the castling arrays and value arrays based on whose turn it is
    public int turnToColorIndex() {

        if (turn == 1) {
            return 0;
        }
        return 1;

    }

    // returns index of opposite color stuff
    public int turnToOppColorIndex() {

        if (turn == 1) {
            return 1;
        }
        return 0;

    }

    // returns index based on color of piece
    public int getPieceIndex(int row, int col) {

        if (isPieceWhite(row, col)) {
            return 0;
        } else if (isPieceBlack(row, col)) {
            return 1;
        }
        return -1;

    }

    // returns value of piece
    public int pieceNumToValue(int num) {

        switch (Math.abs(num)) {

            case pawnNum -> {return 1;}
            case bishopNum, knightNum -> {return 3;}
            case rookNum -> {return 5;}
            case queenNum -> {return 9;}
            default -> {return 0;}

        }

    }

    // returns piece letter from piece number
    public char numToLetter(int pieceNum) {

        char letter = switch (Math.abs(pieceNum)) {
            case noneNum -> noneChar;
            case pawnNum -> pawnChar;
            case bishopNum -> bishopChar;
            case knightNum -> knightChar;
            case rookNum -> rookChar;
            case queenNum -> queenChar;
            default -> kingChar;
        };

        if (pieceNum > 0) {
            return letter;
        }
        return Character.toLowerCase(letter);

    }

    // returns piece number from piece letter
    public int letterToNum(char pieceLetter) {

        int num = switch (Character.toLowerCase(pieceLetter)) {
            case 'p' -> pawnNum;
            case 'b' -> bishopNum;
            case 'n' -> knightNum;
            case 'r' -> rookNum;
            case 'q' -> queenNum;
            case 'k' -> kingNum;
            default -> noneNum;
        };

        if (Character.isUpperCase(pieceLetter)) {
            return num;
        }
        return num * -1;

    }

    // Getter methods:
    public int[][] getBoard() {
        return board;
    }

    public int getTurn() {
        return turn;
    }

    public Stack<Move> getMoves() {
        return moves;
    }

    public Move getLastMove() {
        return moves.peek();
    }

    public boolean[][] getCastlingRights() {
        return castlingRights;
    }

    public int[] getValue() {
        return value;
    }

    public Set<Square>[][] getPieceLoc() {
        return pieceLoc;
    }

    public int getFiftyMoveCount() {
        return fiftyMoveCount;
    }

    // Setter methods:
    public void setBoard(int[][] board) {
        this.board = board;
    }

    public void setSquare(int row, int col, int pieceNum) {
        board[row][col] = pieceNum;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void incrementTurn() {
        turn *= -1;
    }

    public void addMove(Move move) {
        moves.push(move);
    }

    public void setCastlingRights(boolean[][] castlingRights) {
        this.castlingRights = castlingRights;
    }

    public void setCastlingRight(int colorIndex, int sideIndex, boolean canCastle) {
        castlingRights[colorIndex][sideIndex] = canCastle;
    }

    public void setValue(int[] value) {
        this.value = value;
    }

    public void addValue(int colorIndex, int value) {
        this.value[colorIndex] += value;
    }


    public void addPieceLoc(int colorIndex, int pieceIndex, int row, int col) {
        pieceLoc[colorIndex][pieceIndex].add(new Square(row, col));
    }

    public void setFiftyMoveCount(int fiftyMoveCount) {
        this.fiftyMoveCount = fiftyMoveCount;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        printBoard();
        sb.append("Turn: ").append(turn).append("\n");
        sb.append("Castling Rights (White King Side): ").append(castlingRights[0][0]).append("\n");
        sb.append("Castling Rights (White Queen Side): ").append(castlingRights[0][1]).append("\n");
        sb.append("Castling Rights (Black King Side): ").append(castlingRights[1][0]).append("\n");
        sb.append("Castling Rights (Black Queen Side): ").append(castlingRights[1][1]).append("\n");
        sb.append("Value (White): ").append(value[0]).append("\n");
        sb.append("Value (Black): ").append(value[1]).append("\n");
        sb.append("Fifty Move Count: ").append(fiftyMoveCount).append("\n");

        return sb.toString();
    }

}