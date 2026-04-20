import java.util.List;

// ─────────────────────────────────────────────
//  Base AST node
// ─────────────────────────────────────────────
abstract class ASTNode {
    public abstract String toTree(String indent);
}

// ─────────────────────────────────────────────
//  Program = list of statements
// ─────────────────────────────────────────────
class ProgramNode extends ASTNode {
    public List<ASTNode> statements;

    public ProgramNode(List<ASTNode> statements) {
        this.statements = statements;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Program\n");
        for (ASTNode s : statements) {
            sb.append(s.toTree(indent + "  "));
        }
        return sb.toString();
    }
}

// ─────────────────────────────────────────────
//  Command node: move / line / circle / color
// ─────────────────────────────────────────────
class CommandNode extends ASTNode {
    public String command;
    public List<ASTNode> args;

    public CommandNode(String command, List<ASTNode> args) {
        this.command = command;
        this.args = args;
    }

    @Override
    public String toTree(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Command: ").append(command.toUpperCase()).append("\n");
        for (ASTNode arg : args) {
            sb.append(arg.toTree(indent + "  "));
        }
        return sb.toString();
    }
}

// ─────────────────────────────────────────────
//  Assignment: size := 40
// ─────────────────────────────────────────────
class AssignNode extends ASTNode {
    public String name;
    public ASTNode value;

    public AssignNode(String name, ASTNode value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toTree(String indent) {
        return indent + "Assign: " + name + "\n" +
               value.toTree(indent + "  ");
    }
}

// ─────────────────────────────────────────────
//  Binary expression: left OP right
// ─────────────────────────────────────────────
class BinaryNode extends ASTNode {
    public String op;
    public ASTNode left;
    public ASTNode right;

    public BinaryNode(String op, ASTNode left, ASTNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toTree(String indent) {
        return indent + "BinaryOp: " + op + "\n" +
               left.toTree(indent + "  ") +
               right.toTree(indent + "  ");
    }
}

// ─────────────────────────────────────────────
//  Number literal
// ─────────────────────────────────────────────
class NumberNode extends ASTNode {
    public int value;

    public NumberNode(int value) {
        this.value = value;
    }

    @Override
    public String toTree(String indent) {
        return indent + "Number: " + value + "\n";
    }
}

// ─────────────────────────────────────────────
//  Identifier (variable name)
// ─────────────────────────────────────────────
class IdentifierNode extends ASTNode {
    public String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    @Override
    public String toTree(String indent) {
        return indent + "Identifier: " + name + "\n";
    }
}
