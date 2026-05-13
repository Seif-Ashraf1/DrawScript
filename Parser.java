import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    // ── entry point ───────────────────────────────────────

    public ProgramNode parseProgram() {
        List<ASTNode> stmts = new ArrayList<>();

        while (!isAtEnd()) {
            skipNewlines();
            if (isAtEnd()) break;
            ASTNode stmt = parseStatement();
            if (stmt != null) stmts.add(stmt);
        }

        return new ProgramNode(stmts);
    }

    // ── statement ─────────────────────────────────────────
    // statement → command | assignment

    private ASTNode parseStatement() {
        Token t = current();

        // Commands
        if (t.type == TokenType.COMMAND) {
            return parseCommand();
        }

        // Assignment: identifier := expression
        if (t.type == TokenType.IDENTIFIER && lookAhead().type == TokenType.ASSIGN) {
            return parseAssignment();
        }

        // Unknown command: identifier followed by ( — looks like a command call but not recognized
        if (t.type == TokenType.IDENTIFIER && lookAhead().type == TokenType.LPAREN) {
            System.out.println("SYNTAX ERROR: Unknown command '" + t.lexeme + "' at line " + t.line
                    + ". Valid commands are: move, line, circle, color");
            // skip the whole statement: name ( ... ) ;
            advance(); // skip identifier
            advance(); // skip (
            // skip everything until ) or ; or newline or EOF
            while (!isAtEnd()
                    && current().type != TokenType.RPAREN
                    && current().type != TokenType.SEMICOLON
                    && current().type != TokenType.NEWLINE) {
                advance();
            }
            if (current().type == TokenType.RPAREN) advance();   // skip )
            if (current().type == TokenType.SEMICOLON) advance(); // skip ;
            return null;
        }

        // Unknown statement
        System.out.println("SYNTAX ERROR: Unexpected token '" + t.lexeme + "' at line " + t.line);
        advance(); // skip and recover
        return null;
    }

    // ── command ───────────────────────────────────────────
    // command → COMMAND ( expression , expression , ... ) ;

    private CommandNode parseCommand() {
        Token cmdToken = advance(); // consume command keyword
        String cmdName = cmdToken.lexeme;

        expect(TokenType.LPAREN, "(");

        List<ASTNode> args = new ArrayList<>();

        // Parse comma-separated argument list
        if (current().type != TokenType.RPAREN) {
            args.add(parseExpression());
            while (current().type == TokenType.COMMA) {
                advance(); // consume comma
                args.add(parseExpression());
            }
        }

        expect(TokenType.RPAREN, ")");
        expectSemicolonOrNewline();

        return new CommandNode(cmdName, args);
    }

    // ── assignment ────────────────────────────────────────
    // assignment → IDENTIFIER := expression ;

    private AssignNode parseAssignment() {
        Token nameToken = advance();   // identifier
        advance();                     // :=
        ASTNode value = parseExpression();
        expectSemicolonOrNewline();
        return new AssignNode(nameToken.lexeme, value);
    }

    // ── expression (addition / subtraction) ──────────────
    // expression → term (('+' | '-') term)*

    private ASTNode parseExpression() {
        ASTNode left = parseTerm();

        while (current().type == TokenType.OP || current().type == TokenType.OP) {
            String op = advance().lexeme;
            ASTNode right = parseTerm();
            left = new BinaryNode(op, left, right);
        }

        return left;
    }

    // ── term (multiplication / division) ─────────────────
    // term → factor (('*' | '/') factor)*

    private ASTNode parseTerm() {
        ASTNode left = parseFactor();

        while (current().type == TokenType.OP || current().type == TokenType.OP) {
            String op = advance().lexeme;
            ASTNode right = parseFactor();
            left = new BinaryNode(op, left, right);
        }

        return left;
    }

    // ── factor ────────────────────────────────────────────
    // factor → NUMBER | IDENTIFIER | '(' expression ')'

    private ASTNode parseFactor() {
        Token t = current();

        if (t.type == TokenType.NUMBER) {
            advance();
            return new NumberNode(Integer.parseInt(t.lexeme));
        }

        if (t.type == TokenType.IDENTIFIER) {
            advance();
            return new IdentifierNode(t.lexeme);
        }

        if (t.type == TokenType.LPAREN) {
            advance(); // consume (
            ASTNode expr = parseExpression();
            expect(TokenType.RPAREN, ")");
            return expr;
        }

        // Error recovery
        System.out.println("SYNTAX ERROR: Expected expression but found '" + t.lexeme + "' at line " + t.line);
        advance();
        return new NumberNode(0); // placeholder to keep going
    }

    // ── helpers ───────────────────────────────────────────

    private Token current() {
        return tokens.get(pos);
    }

    private Token lookAhead() {
        if (pos + 1 < tokens.size()) return tokens.get(pos + 1);
        return tokens.get(tokens.size() - 1);
    }

    private Token advance() {
        Token t = tokens.get(pos);
        if (pos < tokens.size() - 1) pos++;
        return t;
    }

    private boolean isAtEnd() {
        return current().type == TokenType.EOF;
    }

    private void skipNewlines() {
        while (current().type == TokenType.NEWLINE) advance();
    }

    private void expect(TokenType type, String what) {
        if (current().type == type) {
            advance();
        } else {
            System.out.println("SYNTAX ERROR: Expected '" + what +
                    "' but found '" + current().lexeme + "' at line " + current().line);
        }
    }

    // Accept either ; or newline as statement terminator
    private void expectSemicolonOrNewline() {
        if (current().type == TokenType.SEMICOLON) {
            advance();
        } else if (current().type == TokenType.NEWLINE) {
            advance();
        } else if (current().type == TokenType.EOF) {
            // fine — last statement with no terminator
        } else {
            System.out.println("SYNTAX ERROR: Expected ';' or newline but found '" +
                    current().lexeme + "' at line " + current().line);
        }
    }
}