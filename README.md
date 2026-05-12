# 🎨 DrawScript
### Compiler Front-End | Variant 4 | Java

DrawScript is a professional compiler front-end designed to process a custom domain-specific language for drawing commands. This project implements a high-performance **Handwritten Scanner** and a **Recursive-Descent Parser** to transform raw drawing scripts into a structured Abstract Syntax Tree (AST).

---

## 🚀 Overview
The DrawScript compiler pipeline is built for clarity and efficiency, focusing on the lexical and syntactical analysis of drawing-based instructions. It supports variable assignments, complex arithmetic expressions with operator precedence, and robust error recovery.

### Key Features
- **Custom DSL**: Specifically designed for geometric and movement commands.
- **Handwritten Lexer**: Tokenizes source code into categories like commands, literals, and operators[cite: 3].
- **Recursive-Descent Parser**: Builds a deep AST following strict context-free grammar rules[cite: 2, 33].
- **Arithmetic Engine**: Supports standard operator precedence (`*`, `/` > `+`, `-`) and nested parentheses.
- **Error Recovery**: Gracefully handles syntax errors, reporting line numbers and continuing analysis.

---

## 📂 Project Structure
The source code is modularly organized for maintainability:

| File | Role | Responsibility |
| :--- | :--- | :--- |
| `Scanner.java` | Lexer | Converts source characters into a stream of tokens[cite: 3]. |
| `Parser.java` | Parser | Validates syntax and constructs the AST[cite: 3]. |
| `ASTNode.java` | Model | Defines the hierarchy of tree nodes (Command, Assign, BinaryOp, etc.)[cite: 3]. |
| `TokenType.java` | Schema | Enum defining all valid language tokens[cite: 3]. |
| `Token.java` | Data | Stores token metadata: type, lexeme, and line number[cite: 3]. |
| `Main.java` | Entry | Orchestrates test cases and visualizes the AST output[cite: 3]. |

---

## 🛠️ Language Specification

### Supported Commands
- `move(x, y)`: Sets the current drawing position.
- `line(x, y)`: Draws a line to the specified coordinates.
- `circle(radius)`: Draws a circle with a given radius.
- `color(value)`: Sets the stroke color using numeric arguments.

### Syntax Example
```drawscript
// Variable assignment
size := 40;

// Command with arithmetic expression
move(10 + size, 20 * 2);

// Nested expressions
circle(3 * (4 + 6));
