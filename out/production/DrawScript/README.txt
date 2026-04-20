==============================================
  DrawScript — Compiler Front-End Project
  Variant 4 | Java | Handwritten Scanner & Parser
==============================================

HOW TO COMPILE AND RUN
-----------------------
  javac *.java
  java Main


FILES
-----
  TokenType.java   → Enum of all token categories
  Token.java       → Token class (type + lexeme + line number)
  Scanner.java     → Handwritten lexer (scanner)
  ASTNode.java     → All AST node classes
  Parser.java      → Handwritten recursive-descent parser
  Main.java        → Entry point with 7 test cases


GRAMMAR (Recursive-Descent Friendly)
--------------------------------------
  program    → statement*

  statement  → command
             | assignment

  command    → ('move' | 'line' | 'circle' | 'color')
               '(' arglist ')' (';' | NEWLINE)

  arglist    → expression (',' expression)*

  assignment → IDENTIFIER ':=' expression (';' | NEWLINE)

  expression → term (('+' | '-') term)*

  term       → factor (('*' | '/') factor)*

  factor     → NUMBER
             | IDENTIFIER
             | '(' expression ')'


TOKEN CATEGORIES
-----------------
  MOVE, LINE, CIRCLE, COLOR    → command keywords
  NUMBER                       → integer literals e.g. 100
  IDENTIFIER                   → variable names e.g. size
  PLUS, MINUS, STAR, SLASH     → arithmetic operators
  ASSIGN                       → :=
  LPAREN, RPAREN               → ( )
  SEMICOLON                    → ;
  COMMA                        → ,
  NEWLINE                      → line separator
  EOF                          → end of input
  UNKNOWN                      → unrecognized character (lexical error)


EXPECTED OUTPUT — Test 1 (Simple Valid Commands)
-------------------------------------------------
Source:
  move(100, 100);
  line(200, 100);
  circle(50);

Token Stream:
  [MOVE | "move" | line 1]
  [LPAREN | "(" | line 1]
  [NUMBER | "100" | line 1]
  [COMMA | "," | line 1]
  [NUMBER | "100" | line 1]
  [RPAREN | ")" | line 1]
  [SEMICOLON | ";" | line 1]
  [NEWLINE | "\n" | line 1]
  [LINE | "line" | line 2]
  ... (continues)

AST:
  Program
    Command: MOVE
      Number: 100
      Number: 100
    Command: LINE
      Number: 200
      Number: 100
    Command: CIRCLE
      Number: 50


EXPECTED OUTPUT — Test 2 (Variable + Expressions)
---------------------------------------------------
Source:
  size := 40;
  move(10 + size, 20 * 2);
  circle(size);

AST:
  Program
    Assign: size
      Number: 40
    Command: MOVE
      BinaryOp: +
        Number: 10
        Identifier: size
      BinaryOp: *
        Number: 20
        Number: 2
    Command: CIRCLE
      Identifier: size


EXPECTED OUTPUT — Test 5 (INVALID: Missing ')')
------------------------------------------------
Source:
  move(100, 200;

Output:
  SYNTAX ERROR: Expected ')' but found ';' at line 1
  (parser recovers and continues)


EXPECTED OUTPUT — Test 6 (INVALID: Unknown Command)
-----------------------------------------------------
Source:
  draw(100, 200);

Output:
  SYNTAX ERROR: Unexpected token 'draw' at line 1
  (parser skips the token and recovers)


EXPECTED OUTPUT — Test 7 (INVALID: Bad Expression)
----------------------------------------------------
Source:
  circle(50 + );

Output:
  SYNTAX ERROR: Expected expression but found ')' at line 1


DESIGN DECISIONS
-----------------
  1. The scanner runs independently — you can print tokens before parsing.
  2. The parser builds a real AST (tree nodes), not just a parse trace.
  3. Expressions support full arithmetic precedence:
       * and / bind tighter than + and -
       Parentheses override precedence normally.
  4. Statements end with ; OR newline — both are accepted.
  5. Comments start with // and are skipped by the scanner.
  6. On syntax errors, the parser prints a message and recovers
     (skips the bad token) to keep parsing the rest of the file.

KNOWN LIMITATIONS
------------------
  - Only integer literals (no floats)
  - Color command accepts numeric arguments only (no named colors)
  - No interpreter / evaluator — this is a front-end only project
