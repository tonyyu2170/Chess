public enum MoveType {

    NORMAL(0), EN_PASSANT(1), CASTLE_KING(6), CASTLE_QUEEN(7),
    PROMOTE_BISHOP(2), PROMOTE_KNIGHT(3), PROMOTE_ROOK(4), PROMOTE_QUEEN(5);

    private final int value;

    MoveType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
