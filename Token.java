public class Token {
    public TokenType type; // the category like TokenType.number
    public String lexeme;// the actual text like 100
    public int line;// which line it appeared on

    public Token(TokenType type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        return "[" + type + " | \"" + lexeme + "\" | line " + line + "]";
    }
}
