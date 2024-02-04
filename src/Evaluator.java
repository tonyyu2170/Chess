import java.util.*;

public class Evaluator implements Piece {

    private int score;

    private final int [] pieceValues = {100, 330, 320, 500, 900, 20000};
    private final int [][] pawnTable = {
            {0,  0,  0,  0,  0,  0,  0,  0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {5,  5, 10, 25, 25, 10,  5,  5},
            {0,  0,  0, 20, 20,  0,  0,  0},
            {5, -5,-10,  0,  0,-10, -5,  5},
            {5, 10, 10,-20,-20, 10, 10,  5},
            {0,  0,  0,  0,  0,  0,  0,  0}
    };
    private final int [][] bishopTable = {
            {-20,-10,-10,-10,-10,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5, 10, 10,  5,  0,-10},
            {-10,  5,  5, 10, 10,  5,  5,-10},
            {-10,  0, 10, 10, 10, 10,  0,-10},
            {-10, 10, 10, 10, 10, 10, 10,-10},
            {-10,  5,  0,  0,  0,  0,  5,-10},
            {-20,-10,-10,-10,-10,-10,-10,-20}
    };
    private final int [][] knightTable = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
    };
    private final int [][] rookTable = {
            {0,  0,  0,  0,  0,  0,  0,  0},
            {5, 10, 10, 10, 10, 10, 10,  5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {0,  0,  0,  5,  5,  0,  0,  0}
    };
    private final int [][] queenTable = {
            {-20,-10,-10, -5, -5,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5,  5,  5,  5,  0,-10},
            {-5,  0,  5,  5,  5,  5,  0, -5},
            {0,  0,  5,  5,  5,  5,  0, -5},
            {-10,  5,  5,  5,  5,  5,  0,-10},
            {-10,  0,  5,  0,  0,  0,  0,-10},
            {-20,-10,-10, -5, -5,-10,-10,-20}
    };
    private final int [][] kingTable = {
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            {20, 20,  0,  0,  0,  0, 20, 20},
            {20, 30, 10,  0,  0, 10, 30, 20}
    };
    private final int [][][] pieceTables = {pawnTable, bishopTable, knightTable, rookTable, queenTable, kingTable};


    public Evaluator() {
        score = 0;
    }

    public int getScore(Board board) {

        score = 0;
        score += materialDiff(board);
        score += pieceActivity(board);
        return score;

    }

    public int materialDiff(Board board) {

        int matDiff = 0;
        Set<Square>[][] pieceLoc = board.getPieceLoc();
        for (int color = 0; color < pieceLoc.length; color++) {
            for (int pieceIndex = 0; pieceIndex < pieceLoc[color].length; pieceIndex++) {

                if (color == 0) {
                    matDiff += pieceLoc[color][pieceIndex].size() * pieceValues[pieceIndex];
                }
                else {
                    matDiff -= pieceLoc[color][pieceIndex].size() * pieceValues[pieceIndex];
                }

            }
        }
        return matDiff;

    }

    public int pieceActivity(Board board) {

        int actDiff = 0;
        Set<Square>[][] pieceLoc = board.getPieceLoc();
        for (int color = 0; color < pieceLoc.length; color++) {
            for (int pieceIndex = 0; pieceIndex < pieceLoc[color].length; pieceIndex++) {
                for (Square currSquare: pieceLoc[color][pieceIndex]) {

                    if (color == 0) {
                        actDiff += pieceTables[pieceIndex][currSquare.getRow()][currSquare.getCol()];
                    }
                    else {
                        actDiff -= pieceTables[pieceIndex][7-currSquare.getRow()][currSquare.getCol()];
                    }

                }
            }
        }

        return actDiff;

    }

    public int getScore() {
        return score;
    }

}
