public enum TokenType {
    // Commands - the drawing
    COMMAND,

    // Literals & Identifiers
    NUMBER, // 100 ,42
    IDENTIFIER, // size , x
    STRING,

    // Operators
    OP, ASSIGN, 

    // Punctuation
    LPAREN, // (
    RPAREN,// )
    SEMICOLON,// ;
    COMMA, // ,
    NEWLINE,// end of line

    // Special
    EOF, UNKNOWN
}
