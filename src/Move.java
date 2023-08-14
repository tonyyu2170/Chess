import java.util.Objects;

public class Move {

    // square piece is moving from
    private final Square startSquare;
    // square piece is moving to
    private final Square targetSquare;
    // type of move
    private final MoveType type;
    // if move is a capture or not
    private final boolean isCapture;

    // constructor given square indices
    public Move(int row, int col, int targetRow, int targetCol, MoveType type, boolean isCapture) {

        startSquare = new Square(row, col);
        targetSquare = new Square(targetRow, targetCol);
        this.type = type;
        this.isCapture = isCapture;

    }

    public Square getStartSquare() {
        return startSquare;
    }

    public Square getTargetSquare() {
        return targetSquare;
    }

    public int getStartRow() {
        return startSquare.getRow();
    }

    public int getStartCol() {
        return startSquare.getCol();
    }

    public int getTargetRow() {
        return targetSquare.getRow();
    }

    public int getTargetCol() {
        return targetSquare.getCol();
    }

    public String getTargetNotation() {
        return targetSquare.toString();
    }

    public MoveType getType() {
        return type;
    }

    public boolean isCapture() {
        return isCapture;
    }

    public String toString() {

        String promotionChar = switch (type) {
            case PROMOTE_QUEEN -> "q";
            case PROMOTE_ROOK -> "r";
            case PROMOTE_KNIGHT -> "n";
            case PROMOTE_BISHOP -> "b";
            default -> "";
        };

        return startSquare.toString() + targetSquare + promotionChar;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;

        Move other = (Move) obj;

        return this.startSquare.equals(other.getStartSquare()) && this.targetSquare.equals(other.getTargetSquare()) && this.type == other.type && this.isCapture == other.isCapture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startSquare, targetSquare, type, isCapture);
    }

}