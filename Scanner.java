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
                scanIdentifierOrKeyword();
                continue;
            }

            // Operators and punctuation
            switch (c) {
                case '+': tokens.add(new Token(TokenType.PLUS,      "+", line)); pos++; break;
                case '-': tokens.add(new Token(TokenType.MINUS,     "-", line)); pos++; break;
                case '*': tokens.add(new Token(TokenType.STAR,      "*", line)); pos++; break;
                case '/': tokens.add(new Token(TokenType.SLASH,     "/", line)); pos++; break;
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

    private void scanNumber() {
        int start = pos;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;
        String num = source.substring(start, pos);
        tokens.add(new Token(TokenType.NUMBER, num, line));
    }

    private void scanIdentifierOrKeyword() {
        int start = pos;
        while (pos < source.length() &&
               (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        String word = source.substring(start, pos);

        // Match keywords (commands)
        switch (word.toLowerCase()) {
            case "move":   tokens.add(new Token(TokenType.MOVE,   word, line)); break;
            case "line":   tokens.add(new Token(TokenType.LINE,   word, line)); break;
            case "circle": tokens.add(new Token(TokenType.CIRCLE, word, line)); break;
            case "color":  tokens.add(new Token(TokenType.COLOR,  word, line)); break;
            default:       tokens.add(new Token(TokenType.IDENTIFIER, word, line)); break;
        }
    }
}
