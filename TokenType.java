public enum TokenType {
    // Commands
    MOVE, LINE, CIRCLE, COLOR,

    // Literals & Identifiers
    NUMBER, IDENTIFIER, STRING,

    // Operators
    PLUS, MINUS, STAR, SLASH, ASSIGN,

    // Punctuation
    LPAREN, RPAREN, SEMICOLON, COMMA, NEWLINE,

    // Special
    EOF, UNKNOWN
}
