import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private String source;
    private int pos;
    private int line;
    private List<Token> tokens;

    public Scanner(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.tokens = new ArrayList<>();
    }

    public List<Token> scan() {
        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) break;

            char c = source.charAt(pos);

            // Skip comments (// ...)
            if (c == '/' && peek() == '/') {
                skipLineComment();
                continue;
            }

            // Newline
            if (c == '\n') {
                if (!checkSemicolonBeforeNewline()) {
                    break;  // Stop scanning on missing semicolon error
                }
                tokens.add(new Token(TokenType.NEWLINE, "\\n", line));

                line++;
                pos++;
                continue;
            }

            // Numbers
            if (Character.isDigit(c)) {
                scanNumber();
                continue;
            }

            // Identifiers and keywords
            if (Character.isLetter(c) || c == '_') {
                if (!scanIdentifierOrKeyword()) {
                    break;
                }
                continue;
            }

            // Operators and punctuation
            switch (c) {
                case '+': tokens.add(new Token(TokenType.OP,      "+", line)); pos++; break;
                case '-': tokens.add(new Token(TokenType.OP,     "-", line)); pos++; break;
                case '*': tokens.add(new Token(TokenType.OP,      "*", line)); pos++; break;
                case '/': tokens.add(new Token(TokenType.OP,     "/", line)); pos++; break;
                case '(': tokens.add(new Token(TokenType.LPAREN,    "(", line)); pos++; break;
                case ')': tokens.add(new Token(TokenType.RPAREN,    ")", line)); pos++; break;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";", line)); pos++; break;
                case ',': tokens.add(new Token(TokenType.COMMA,     ",", line)); pos++; break;
                case ':':
                    if (peek() == '=') {
                        tokens.add(new Token(TokenType.ASSIGN, ":=", line));
                        pos += 2;
                    } else {
                        tokens.add(new Token(TokenType.UNKNOWN, ":", line));
                        pos++;
                    }
                    break;
                default:
                    System.out.println("LEXICAL ERROR: Unknown character '" + c + "' at line " + line);
                    tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(c), line));
                    pos++;
            }
        }

        tokens.add(new Token(TokenType.EOF, "EOF", line));
        return tokens;
    }

    // ── helpers ──────────────────────────────────────────

    private char peek() {
        if (pos + 1 < source.length()) return source.charAt(pos + 1);
        return '\0';
    }

    private void skipWhitespace() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\r') pos++;
            else break;
        }
    }

    private void skipLineComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') pos++;
    }

    private boolean checkSemicolonBeforeNewline() {
    // Walk backwards through already-collected tokens
    // Skip any NEWLINE tokens we already recorded
    for (int i = tokens.size() - 1; i >= 0; i--) {
        Token t = tokens.get(i);

        if (t.type == TokenType.NEWLINE) continue; // skip previous newlines

        // First real token found going backwards
        if (t.type == TokenType.SEMICOLON) {
            return true; // ✅ last real token was ; → fine
        } else if (t.type == TokenType.EOF) {
            return true; // beginning of file, nothing to check
        } else {
            // ❌ last real token was NOT ; → missing semicolon
            System.out.println("LEXICAL ERROR: Missing ';' after '"
                    + t.lexeme + "' at line " + t.line);
            return false; // Terminate scanning
        }
    }
    return true;
}

    private void scanNumber() {
        int start = pos;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;
        String num = source.substring(start, pos);
        tokens.add(new Token(TokenType.NUMBER, num, line));
    }

    private boolean scanIdentifierOrKeyword() {
        int start = pos;
        while (pos < source.length() &&
               (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String word = source.substring(start, pos);

        // Match keywords (commands)
        switch (word) {
            case "move":   tokens.add(new Token(TokenType.COMMAND,   word, line)); return true;
            case "line":   tokens.add(new Token(TokenType.COMMAND,   word, line)); return true;
            case "circle": tokens.add(new Token(TokenType.COMMAND, word, line)); return true;
            case "color":  tokens.add(new Token(TokenType.COMMAND,  word, line)); return true;
            default:
                int lookahead = pos;
                while (lookahead < source.length() 
                       && source.charAt(lookahead) == ' ') {
                    lookahead++;
                }
                if (lookahead + 1 < source.length()
                        && source.charAt(lookahead) == ':'
                        && source.charAt(lookahead + 1) == '=') {
                    tokens.add(new Token(TokenType.IDENTIFIER, word, line));
                    return true;
                } else {
                    System.out.println("LEXICAL ERROR: invalid syntax (unknown token '"
                            + word + "') at line " + line);
                    tokens.add(new Token(TokenType.UNKNOWN, word, line));
                    return false;
        }
    }
    }
}
