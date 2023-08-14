public interface Piece {

    // piece numbers for each piece
    // pieces are white if positive and black if negative
    int noneNum = 0;
    int pawnNum = 1;
    int bishopNum = 2;
    int knightNum = 3;
    int rookNum = 4;
    int queenNum = 5;
    int kingNum = 6;

    // piece chars for each piece
    // pieces are capital if white and lowercase if black
    char noneChar = ' ';
    char pawnChar = 'P';
    char bishopChar = 'B';
    char knightChar = 'N';
    char rookChar = 'R';
    char queenChar = 'Q';
    char kingChar = 'K';

    // piece indices for each piece
    int pawnIndex = 0;
    int bishopIndex = 1;
    int knightIndex = 2;
    int rookIndex = 3;
    int queenIndex = 4;
    int kingIndex = 5;

}
