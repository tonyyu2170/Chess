public enum GameEndType {

    // game endings with someone winning and other losing
    WHITE_CHECKMATE("White wins by checkmate!"), BLACK_CHECKMATE("Black wins by checkmate!"),
    WHITE_RESIGN("White wins by resignation!"), BLACK_RESIGN("White wins by resignation!"),
    WHITE_TIMEOUT("White wins by timeout!"), BLACK_TIMEOUT("White wins by timeout!"),

    // game endings with draw
    STALEMATE("Draw by stalemate"), INSUFFICIENT_MATERIAL("Draw by insufficient material"), FIFTY_MOVE_RULE("Draw by 50 move rule"), REPETITION("Draw by repetition"), AGREEMENT("Draw by agreement");

    private final String endMessage;

    GameEndType(String endMessage) {
        this.endMessage = endMessage;
    }

    public String getValue() {
        return endMessage;
    }

}
