import java.util.ArrayList;
import java.util.List;

public class Parser {

    private List<Token> tokens;
    private int pos;
    private boolean hasError;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.hasError = false;
    }

    // ============================================================
    // ENTRY POINT
    // ============================================================

    public ProgramNode parseProgram() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            skipNewlines();
            if (isAtEnd()) break;
            ASTNode stmt = parseStatement();
            if (stmt != null) stmts.add(stmt);
            skipNewlines();
        }
        return new ProgramNode(stmts);
    }

    // ============================================================
    // STATEMENT PARSING
    // ============================================================

    private ASTNode parseStatement() {
        Token t = current();

        if (t.type == TokenType.COMMAND) {
            return parseCommand();
        }

        if (t.type == TokenType.IDENTIFIER && lookAhead().type == TokenType.ASSIGN) {
            return parseAssignment();
        }

        System.out.println("SYNTAX ERROR: Unknown statement '"
                + t.lexeme + "' at line " + t.line);
        advance();
        return null;
    }

    // ============================================================
    // STEP 5 — parseCommand() WITH validateCommandArgs() CALL
    // ============================================================

    private ASTNode parseCommand() {
        Token cmdToken = advance();
        String cmdName = cmdToken.lexeme;
        int cmdLine = cmdToken.line;        // ← save line for error reporting

        expect(TokenType.LPAREN, "(");

        List<ASTNode> args = new ArrayList<>();
        if (current().type != TokenType.RPAREN) {
            args.add(parseExpression());
            while (current().type == TokenType.COMMA) {
                advance();
                args.add(parseExpression());
            }
        }

        expect(TokenType.RPAREN, ")");
        expectSemicolon();

        validateCommandArgs(cmdName, args, cmdLine);   // ← STEP 5: call validation here

        return new CommandNode(cmdName, args);
    }

    // ============================================================
    // parseAssignment() — NO CHANGES NEEDED HERE
    // ============================================================

    private ASTNode parseAssignment() {
        Token nameToken = advance();
        advance(); // consume :=
        ASTNode value = parseExpression();
        expectSemicolon();
        return new AssignNode(nameToken.lexeme, value);
    }

    // ============================================================
    // EXPRESSION PARSING — NO CHANGES
    // ============================================================

    private ASTNode parseExpression() {
        ASTNode left = parseTerm();
        while (current().type == TokenType.OP
                || current().type == TokenType.OP) {
            String op = advance().lexeme;
            ASTNode right = parseTerm();
            left = new BinaryNode(op, left, right);
        }
        return left;
    }

    private ASTNode parseTerm() {
        ASTNode left = parseFactor();
        while (current().type == TokenType.OP
                || current().type == TokenType.OP) {
            String op = advance().lexeme;
            ASTNode right = parseFactor();
            left = new BinaryNode(op, left, right);
        }
        return left;
    }

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
            advance();
            ASTNode expr = parseExpression();
            expect(TokenType.RPAREN, ")");
            return expr;
        }

        System.out.println("SYNTAX ERROR: Unexpected token '"
                + t.lexeme + "' at line " + t.line);
        advance();
        return new NumberNode(0);
    }

    // ============================================================
    // STEP 3 — validateCommandArgs()
    // ADD THIS AFTER parseCommand()
    // ============================================================

    private void validateCommandArgs(String commandName,
                                     List<ASTNode> args,
                                     int line) {
        for (int i = 0; i < args.size(); i++) {
            ASTNode arg = args.get(i);

            // Skip check if arg contains a variable — we don't know its value yet
            if (containsIdentifier(arg)) continue;

            int result = evaluate(arg);

            if (result < 0) {
                System.out.println("SEMANTIC ERROR: Argument " + (i + 1)
                        + " of '" + commandName
                        + "' evaluates to negative value (" + result + ")"
                        + " at line " + line
                        + " — drawing coordinates must be non-negative.");
                hasError = true;
            }
        }
    }

    // ============================================================
    // STEP 1 — evaluate()
    // ADD THIS AFTER validateCommandArgs()
    // ============================================================

    private int evaluate(ASTNode node) {

        // Base case: plain number — just return its value
        if (node instanceof NumberNode) {
            return ((NumberNode) node).value;
        }

        // Binary operation: evaluate left and right, then apply operator
        if (node instanceof BinaryNode) {
            BinaryNode bin = (BinaryNode) node;
            int left  = evaluate(bin.left);
            int right = evaluate(bin.right);

            switch (bin.op) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/":
                    if (right == 0) {
                        System.out.println("SEMANTIC ERROR: Division by zero"
                                + " at line " + currentLine());
                        hasError = true;
                        return 0;
                    }
                    return left / right;
            }
        }

        // Identifier: unknown value at parse time, return 0 and skip check
        if (node instanceof IdentifierNode) {
            return 0;
        }

        return 0;
    }

    // ============================================================
    // STEP 4 — containsIdentifier()
    // ADD THIS AFTER evaluate()
    // ============================================================

    private boolean containsIdentifier(ASTNode node) {

        // If the node itself is an identifier → true
        if (node instanceof IdentifierNode) {
            return true;
        }

        // If it's a binary node, check both children recursively
        if (node instanceof BinaryNode) {
            BinaryNode bin = (BinaryNode) node;
            return containsIdentifier(bin.left)
                || containsIdentifier(bin.right);
        }

        // NumberNode has no identifier inside it
        return false;
    }

    // ============================================================
    // STEP 2 — currentLine()
    // ADD THIS AFTER containsIdentifier()
    // ============================================================

    private int currentLine() {
        return tokens.get(pos).line;
    }

    // ============================================================
    // HELPER METHODS — NO CHANGES
    // ============================================================

    private void expect(TokenType type, String symbol) {
        if (current().type == type) {
            advance();
        } else {
            System.out.println("SYNTAX ERROR: Expected '" + symbol
                    + "' but found '" + current().lexeme
                    + "' at line " + current().line);
        }
    }

    private void expectSemicolon() {
        if (current().type == TokenType.SEMICOLON) {
            advance();
        } else {
            System.out.println("SYNTAX ERROR: Expected ';' at end of statement"
                    + " but found '" + current().lexeme
                    + "' at line " + current().line);
        }
    }

    private void skipNewlines() {
        while (current().type == TokenType.NEWLINE) {
            advance();
        }
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token lookAhead() {
        if (pos + 1 < tokens.size())
            return tokens.get(pos + 1);
        return tokens.get(tokens.size() - 1);
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private boolean isAtEnd() {
        return current().type == TokenType.EOF;
    }

    // ============================================================
    // ERROR STATUS QUERY
    // ============================================================

    public boolean hasErrors() {
        return hasError;
    }
}