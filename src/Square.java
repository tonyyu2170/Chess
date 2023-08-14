import java.util.Objects;

public class Square {

    private final int row;
    private final int col;
    private final String strNotation;

    public Square() {
        row = col = -1;
        strNotation = null;
    }

    public Square(int row, int col) {
        this.row = row;
        this.col = col;
        strNotation = indexToNotation(row, col);
    }

    public Square(Square other) {
        this.row = other.row;
        this.col = other.col;
        this.strNotation = other.strNotation;
    }

    // returns notation of a square given the row and column
    public String indexToNotation(int row, int col) {

        String columnIndex = String.valueOf(Character.toChars(col + 97));
        int rowIndex = 8 - row;

        return columnIndex + rowIndex;

    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String toString() {
        return strNotation;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;

        Square other = (Square) obj;

        return this.row == other.row && this.col == other.col && this.strNotation.equals(other.strNotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, strNotation);
    }

}
