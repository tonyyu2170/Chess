import java.util.*;
import static java.lang.Math.min;

public class MoveGenerator implements Piece {

    private Set<Move> possibleMoves;
    private Set<Square> enemyTargetedSquares;

    private int friendlyKingRow;
    private int friendlyKingCol;

    private Square slidingCheckFrom;
    private Set<Square> slidingCheckSquares;
    private Square knightPawnCheckFrom;
    private boolean inDoubleCheck;

    private Map<Square, Set<Square>> pinnedSquares;

    public MoveGenerator() {

        possibleMoves = new HashSet<>(218);
        enemyTargetedSquares = new HashSet<>(218);

        friendlyKingRow = friendlyKingCol = -1;

        slidingCheckFrom = null;
        slidingCheckSquares = new HashSet<>(8);
        knightPawnCheckFrom = null;
        inDoubleCheck = false;

        pinnedSquares = new HashMap<>();

    }

    public void setMoveGenerator(Board board) {

        possibleMoves = new HashSet<>(218);
        enemyTargetedSquares = new HashSet<>(218);

        for (Square kingSquare: board.getPieceLoc()[board.turnToColorIndex()][kingIndex]) {
            friendlyKingRow = kingSquare.getRow();
            friendlyKingCol = kingSquare.getCol();
        }

        slidingCheckFrom = null;
        slidingCheckSquares = new HashSet<>(8);
        knightPawnCheckFrom = null;
        inDoubleCheck = false;

        pinnedSquares = new HashMap<>();

    }

    // updates HashSet of legal moves
    public Set<Move> generateLegalMoves(Board board) {

        setMoveGenerator(board);
        generateEnemyTargetedSquares(board);

        // if in double check only possible moves are king moves
        if (inDoubleCheck) {
            getKingMoves(friendlyKingRow, friendlyKingCol, board);
            return possibleMoves;
        }

        if (inSlidingCheck()) {
            slidingCheckSquares = getSquaresBetween(slidingCheckFrom, new Square(friendlyKingRow, friendlyKingCol));
        }

        generateMoves(board);

        return possibleMoves;

    }

    public void generateMoves(Board board) {

        Set<Square>[] pieceLoc = board.getPieceLoc()[board.turnToColorIndex()];

        int [][] diaMultipler = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
        int [][] rectMultiplier = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        // iterates through the correct color's different types of piece arrays
        for (int pieceIndex = 0; pieceIndex < pieceLoc.length; pieceIndex++) {

            // iterates through each type of piece's array to get the individual pieces
            for (Square square: pieceLoc[pieceIndex]) {

                int row = square.getRow();
                int col = square.getCol();

                int [] diaEdgeDist = getDiaDist(row, col);
                int [] rectEdgeDist = {row, 7 - col, 7 - row, col};

                switch (pieceIndex) {
                    case pawnIndex -> getPawnMoves(row, col, board);
                    // edgeDist: 1st index = top right, 2nd index = bottom right, 3rd index = bottom left, 4th index = top left
                    case bishopIndex -> getSlidingMoves(row, col, diaEdgeDist, diaMultipler, board);
                    case knightIndex -> getKnightMoves(row, col, board);
                    // edge Dist: 1st index = top, 2nd index = right, 3rd index = bottom, 4th index = left
                    case rookIndex -> getSlidingMoves(row, col, rectEdgeDist, rectMultiplier, board);
                    // just goes through both bishop and rook processes for queen, same for king but stops after 1 in each direction
                    case queenIndex -> {
                        getSlidingMoves(row, col, diaEdgeDist, diaMultipler, board);
                        getSlidingMoves(row, col, rectEdgeDist, rectMultiplier, board);
                    }
                    case kingIndex -> getKingMoves(row, col, board);
                }

            }

        }

        // castling is represented with 'CA', the side is represented by either 'KS' or 'QS'
        int kingRow = board.calcStartKingRow();
        if (canKSCastle(board)) {possibleMoves.add(new Move(kingRow, 4, kingRow, 6, MoveType.CASTLE_KING, false));}
        if (canQSCastle(board)) {possibleMoves.add(new Move(kingRow, 4, kingRow, 2, MoveType.CASTLE_QUEEN, false));}

    }

    public void getKingMoves(int row, int col, Board board) {

        // all 8 move's changes to the current i and j
        int [][] moveDiffs = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] moveDiff : moveDiffs) {

            int targetRow = row + moveDiff[0];
            int targetCol = col + moveDiff[1];

            // checks if target box is out of bounds and if target box has a friendly piece
            if (board.isInBounds(targetRow, targetCol) && !board.isPieceTurn(targetRow, targetCol) && !enemyTargetedSquares.contains(new Square(targetRow, targetCol))) {
                possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.NORMAL, board.getBoard()[targetRow][targetCol] != 0));
            }

        }

    }

    // returns list of all possible moves from bishops and the diagonal moves for queen
    public void getSlidingMoves(int row, int col, int [] edgeDist, int [][] direction, Board board) {

        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= edgeDist[i]; j++) {

                // location of target square indices
                int targetRow = row + direction[i][0] * j;
                int targetCol = col + direction[i][1] * j;
                boolean isCapture = board.getBoard()[targetRow][targetCol] != 0;

                // checks if target box has a friendly piece or is pinned
                if (board.isPieceTurn(targetRow, targetCol) || isPinned(row, col) && notValidPinMove(row, col, targetRow, targetCol)) {
                    break;
                }

                // checks if in check
                if ((!inSlidingCheck() || inSlidingCheck() && !notBlockSlidingCheck(targetRow, targetCol)) && (!inKnightPawnCheck() || inKnightPawnCheck() && !notTakeKnightPawnChecker(targetRow, targetCol))) {
                    possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.NORMAL, isCapture));
                }

                // if capturing enemy piece
                if (isCapture) {
                    break;
                }

            }
        }

    }

    // returns list of all possible knight moves
    public void getKnightMoves(int row, int col, Board board) {

        if (isPinned(row, col)) {
            return;
        }

        // all 8 move's changes to the current i and j
        int [][] moveDiffs = {{-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}};

        for (int[] moveDiff : moveDiffs) {

            int targetRow = row + moveDiff[0];
            int targetCol = col + moveDiff[1];

            if (!board.isInBounds(targetRow, targetCol) || board.isPieceTurn(targetRow, targetCol) || inSlidingCheck() && notBlockSlidingCheck(targetRow, targetCol) || inKnightPawnCheck() && notTakeKnightPawnChecker(targetRow, targetCol)) {
                continue;
            }

            possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.NORMAL, board.getBoard()[targetRow][targetCol] != 0));

        }

    }

    // returns list of all possible pawn moves
    public void getPawnMoves(int row, int col, Board board) {

        int turn = board.getTurn();
        int targetRow = row - turn;
        boolean canMoveOneSquare = board.getBoard()[targetRow][col] == 0;
        boolean notKnightPawnCheck = !inKnightPawnCheck();

        // checks if pawn can move 1 square up
        if (canMoveOneSquare && notKnightPawnCheck && (!inSlidingCheck() || inSlidingCheck() && !notBlockSlidingCheck(targetRow, col)) && (!isPinned(row, col) || isPinned(row, col) && !notValidPinMove(row, col, targetRow, col))) {

            // promotion
            if (targetRow == board.calcPromoteRow()) {
                getPromotionMoves(row, col, targetRow, col, false);
            }
            // regular 1 square up move
            else {
                possibleMoves.add(new Move(row, col, targetRow, col, MoveType.NORMAL, false));
            }

        }

        targetRow = row - turn * 2;

        boolean canMoveTwoSquare = row == board.calcStartPawnRow() && board.getBoard()[targetRow][col] == 0;

        // checks if pawn can move 2 squares up
        if (canMoveOneSquare && canMoveTwoSquare && notKnightPawnCheck && (!inSlidingCheck() || inSlidingCheck() && !notBlockSlidingCheck(targetRow, col)) && (!isPinned(row, col) || isPinned(row, col) && !notValidPinMove(row, col, targetRow, col))) {
            possibleMoves.add(new Move(row, col, targetRow, col, MoveType.NORMAL, false));
        }


        targetRow = row - turn;
        int [] targetCol = {col + 1, col - 1};

        // checks if pawn can capture any pieces normally, first to the right and then to the left
        for (int i = 0; i < 2; i++) {

            if (!board.isInBounds(targetRow, targetCol[i]) || !board.isNotPieceTurn(targetRow, targetCol[i]) || inSlidingCheck() && notBlockSlidingCheck(targetRow, targetCol[i]) || inKnightPawnCheck() && notTakeKnightPawnChecker(targetRow, targetCol[i]) || isPinned(row, col) && notValidPinMove(row, col, targetRow, targetCol[i])) {
                continue;
            }

            if (targetRow == board.calcPromoteRow()) {
                getPromotionMoves(row, col, targetRow, targetCol[i], true);
                continue;
            }

            possibleMoves.add(new Move(row, col, targetRow, targetCol[i], MoveType.NORMAL, true));

        }

        // EN PASSANT FORCED MOVE
        if (board.getMoves().isEmpty()) {
            return;
        }

        Move lastMove = board.getLastMove();

        if (canEnPassant(row, col, lastMove, board) && isEnPassantLegal(row, col, row - turn, lastMove.getTargetCol(), board)) {
            possibleMoves.add(new Move(row, col, row - turn, lastMove.getTargetCol(), MoveType.EN_PASSANT, true));
        }

    }

    // returns moves promoting to different pieces
    public void getPromotionMoves(int row, int col, int targetRow, int targetCol, boolean isCapture) {

        possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.PROMOTE_BISHOP, isCapture));
        possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.PROMOTE_KNIGHT, isCapture));
        possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.PROMOTE_ROOK, isCapture));
        possibleMoves.add(new Move(row, col, targetRow, targetCol, MoveType.PROMOTE_QUEEN, isCapture));

    }

    // checks if can Queen side castle
    public boolean canKSCastle(Board board) {

        int row = board.calcStartKingRow();

        boolean emptySquares = board.getBoard()[row][5] == noneNum && board.getBoard()[row][6] == noneNum;
        boolean hasPiecesNotMoved = board.getCastlingRights()[board.turnToColorIndex()][0];
        boolean squaresNotAttacked = !enemyTargetedSquares.contains(new Square(row, 4)) && !enemyTargetedSquares.contains(new Square(row, 5)) && !enemyTargetedSquares.contains(new Square(row, 6));

        return emptySquares && hasPiecesNotMoved && squaresNotAttacked;

    }

    // checks if can King side castle
    public boolean canQSCastle(Board board) {

        int row = board.calcStartKingRow();

        boolean emptySquares = board.getBoard()[row][1] == noneNum && board.getBoard()[row][2] == noneNum && board.getBoard()[row][3] == noneNum;
        boolean hasPiecesNotMoved = board.getCastlingRights()[board.turnToColorIndex()][1];
        boolean squaresNotAttacked = !enemyTargetedSquares.contains(new Square(row, 4)) && !enemyTargetedSquares.contains(new Square(row, 3)) && !enemyTargetedSquares.contains(new Square(row, 2));

        return emptySquares && hasPiecesNotMoved && squaresNotAttacked;

    }

    // 1st index = top right, 2nd index = bottom right, 3rd index = bottom left, 4th index = top left
    public int [] getDiaDist(int row, int col) {

        return new int[]{min(row, 7-col), min(7-row, 7-col), min(7-row, col), min(row, col)};

    }

    public void generateEnemyTargetedSquares(Board board) {


        Set<Square>[] pieceLoc = board.getPieceLoc()[board.turnToOppColorIndex()];
        int checkCounter = 0;

        int [][] diaMultipler = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
        int [][] rectMultiplier = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        // iterates through the correct color's different types of piece arrays
        for (int pieceIndex = 0; pieceIndex < pieceLoc.length; pieceIndex++) {

            // iterates through each type of piece's array to get the individual pieces
            for (Square square: pieceLoc[pieceIndex]) {

                int row = square.getRow();
                int col = square.getCol();

                int [] diaEdgeDist = getDiaDist(row, col);
                int [] rectEdgeDist = {row, 7 - col, 7 - row, col};

                switch (pieceIndex) {
                    case pawnIndex -> checkCounter += getPawnAttacks(row, col, board);
                    // edgeDist: 1st index = top right, 2nd index = bottom right, 3rd index = bottom left, 4th index = top left
                    case bishopIndex -> checkCounter += getSlidingAttacks(row, col, diaEdgeDist, diaMultipler, board);
                    case knightIndex -> checkCounter += getKnightAttacks(row, col, board);
                    // edge Dist: 1st index = top, 2nd index = right, 3rd index = bottom, 4th index = left
                    case rookIndex -> checkCounter += getSlidingAttacks(row, col, rectEdgeDist, rectMultiplier, board);
                    // just goes through both bishop and rook processes for queen, same for king but stops after 1 in each direction
                    case queenIndex, kingIndex -> {
                        checkCounter += getSlidingAttacks(row, col, diaEdgeDist, diaMultipler, board);
                        checkCounter += getSlidingAttacks(row, col, rectEdgeDist, rectMultiplier, board);
                    }
                }

            }

        }

        if (checkCounter == 2) {
            inDoubleCheck = true;
        }

    }

    public int getSlidingAttacks(int row, int col, int [] edgeDist, int [][] direction, Board board) {

        int checkCounter = 0;

        for (int i = 0; i < 4; i++) {

            int pieceCounter = 0;
            Square pinnedSquare = new Square();

            for (int j = 0; j < edgeDist[i]; j++) {

                // location of target square indices
                int targetRow = row + direction[i][0] * (j + 1);
                int targetCol = col + direction[i][1] * (j + 1);

                if (pieceCounter == 0) {
                    enemyTargetedSquares.add(new Square(targetRow, targetCol));
                }

                if (board.getBoard()[targetRow][targetCol] == kingNum * board.getTurn()) {

                    if (pieceCounter == 0) {

                        slidingCheckFrom = new Square(row, col);
                        checkCounter++;

                        if (board.isInBounds(row + direction[i][0] * (j + 2), col + direction[i][1] * (j + 2))) {
                            enemyTargetedSquares.add(new Square(row + direction[i][0] * (j + 2), col + direction[i][1] * (j + 2)));
                        }

                    } else {
                        pinnedSquares.put(pinnedSquare, getSquaresBetween(new Square(row, col), new Square(friendlyKingRow, friendlyKingCol)));
                    }

                    pieceCounter++;

                } else if (board.isPieceTurn(targetRow, targetCol)) {
                    pieceCounter++;
                    pinnedSquare = new Square(targetRow, targetCol);
                }

                // breaks this direction if piece is a king or friendly piece
                if (Math.abs(board.getBoard()[row][col]) == kingNum || board.isNotPieceTurn(targetRow, targetCol) || pieceCounter == 2) {
                    break;
                }

            }
        }

        return checkCounter;

    }

    public int getKnightAttacks(int row, int col, Board board) {

        int checkCounter = 0;
        int [][] moveDiffs = {{-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}};

        for (int[] moveDiff : moveDiffs) {

            int targetRow = row + moveDiff[0];
            int targetCol = col + moveDiff[1];

            // checks if target box is out of bounds and if target box has a friendly piece
            if (board.isInBounds(targetRow, targetCol)) {

                enemyTargetedSquares.add(new Square(targetRow, targetCol));

                if (board.getBoard()[targetRow][targetCol] == kingNum * board.getTurn()) {
                    knightPawnCheckFrom = new Square(row, col);
                    checkCounter++;
                }

            }

        }

        return checkCounter;

    }

    public int getPawnAttacks(int row, int col, Board board) {

        int checkCounter = 0;
        int targetRow = row + board.getTurn();
        int [] targetCol = {col + 1, col - 1};

        // checks if pawn can capture any pieces normally, first to the right and then to the left
        for (int i = 0; i < 2; i++) {

            if (board.isInBounds(targetRow, targetCol[i])) {

                enemyTargetedSquares.add(new Square(targetRow, targetCol[i]));

                if (board.getBoard()[targetRow][targetCol[i]] == kingNum * board.getTurn()) {
                    knightPawnCheckFrom = new Square(row, col);
                    checkCounter++;
                }

            }

        }

        return checkCounter;

    }

    public Set<Square> getSquaresBetween(Square start, Square end) {

        Set<Square> squaresBetween = new HashSet<>(7);

        int startRow = start.getRow();
        int startCol = start.getCol();
        int endRow = end.getRow();
        int endCol = end.getCol();

        // Calculate the row and column differences between the two points
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);

        // Calculate the increments for moving along the line
        int rowIncrement = Integer.compare(endRow, startRow);
        int colIncrement = Integer.compare(endCol, startCol);

        // Output the line between the two points
        while (startRow != endRow || startCol != endCol) {

            squaresBetween.add(new Square(startRow, startCol));
            int err = colDiff - rowDiff;

            if (2 * err > -rowDiff) {
                err -= rowDiff;
                startCol += colIncrement;
            }
            if (2 * err < colDiff) {
                startRow += rowIncrement;
            }
        }

        return squaresBetween;

    }

    public boolean notTakeKnightPawnChecker(int targetRow, int targetCol) {
        return !knightPawnCheckFrom.equals(new Square(targetRow, targetCol));
    }

    public boolean notBlockSlidingCheck(int targetRow, int targetCol) {
        return !slidingCheckSquares.contains(new Square(targetRow, targetCol));
    }

    public boolean isPinned(int row, int col) {
        return pinnedSquares.containsKey(new Square(row, col));
    }

    public boolean notValidPinMove(int pinnedRow, int pinnedCol, int targetRow, int targetCol) {
        return !pinnedSquares.get(new Square(pinnedRow, pinnedCol)).contains(new Square(targetRow, targetCol));
    }

    public boolean inKnightPawnCheck() {
        return knightPawnCheckFrom != null;
    }

    public boolean inSlidingCheck() {
        return slidingCheckFrom != null;
    }

    public boolean isInCheck() {
        return slidingCheckFrom != null || knightPawnCheckFrom != null;
    }

    public boolean canEnPassant(int row, int col, Move lastMove, Board board) {
        return board.isMoveNormal(lastMove) && row == board.calcEPRow() && lastMove.getTargetRow() == board.calcEPRow() && Math.abs(board.getBoard()[lastMove.getTargetRow()][lastMove.getTargetCol()]) == pawnNum && Math.abs(col - lastMove.getTargetCol()) == 1 && lastMove.getStartRow() == board.calcOppStartPawnRow();
    }

    public boolean isEnPassantLegal(int row, int col, int targetRow, int targetCol, Board board) {

        Board tempBoard = new Board(board);
        Set<Square>[][] tempPieceLoc = tempBoard.getPieceLoc();
        Set<Move> currentPossibleMoves = possibleMoves;

        tempPieceLoc[tempBoard.turnToOppColorIndex()][pawnIndex].remove(new Square(row, targetCol));

        tempBoard.setSquare(row, col, noneNum);
        tempBoard.setSquare(row, targetCol, noneNum);
        tempBoard.setSquare(targetRow, targetCol, tempBoard.getTurn());

        boolean isLegal = true;

        setMoveGenerator(tempBoard);
        generateEnemyTargetedSquares(tempBoard);
        if (isInCheck()) {
            isLegal = false;
        }

        setMoveGenerator(board);
        generateEnemyTargetedSquares(board);
        possibleMoves = currentPossibleMoves;

        return isLegal;

    }

    @Override
    public String toString() {
        return "possibleMoves: " + possibleMoves +
                ", enemyTargetedSquares: " + enemyTargetedSquares +
                ", friendlyKingRow: " + friendlyKingRow +
                ", friendlyKingCol: " + friendlyKingCol +
                ", slidingCheckFrom: " + slidingCheckFrom +
                ", slidingCheckSquares: " + slidingCheckSquares +
                ", knightPawnCheckFrom: " + knightPawnCheckFrom +
                ", inDoubleCheck: " + inDoubleCheck +
                ", pinnedSquares: " + pinnedSquares;
    }


}
