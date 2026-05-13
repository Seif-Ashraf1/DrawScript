import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("  DrawScript Compiler Front-End");
        System.out.println("==============================================\n");

        // ── Test 1: Simple valid commands ─────────────────
        runTest("Test 1 — Simple Valid Commands",
            "ci(50)\n"
        );

        // ── Test 2: Variable assignment + expression args ──
        runTest("Test 1 — Simple Valid Commands",
                "move(100, 100);\n" +
                        "line(200, 100);\n" +
                        "circle(50);\n"
        );

        // ── Test 3: Color + mixed commands ────────────────
        runTest("Test 3 — Color & Mixed Commands",
            "color(255);\n" +
            "move(80, 80);\n" +
            "line(120, 80);\n" +
            "line(120, 120);\n"
        );

        // ── Test 4: Nested arithmetic expressions ─────────
        runTest("Test 4 — Nested Arithmetic in Arguments",
            "move((10 + 5) * 2, 100);\n" +
            "circle(3 * (-5));\n"
        );

        // ── Test 5: INVALID — missing closing paren ───────
        runTest("Test 5 — INVALID: Missing ')'",
            "move(100, 200;\n"
        );

        // ── Test 6: INVALID — unknown command ─────────────
        runTest("Test 6 — INVALID: Unknown Command",
            "draw(100, 200);\n"
        );

        // ── Test 7: INVALID — bad expression ──────────────
        runTest("Test 7 — INVALID: Bad Expression",
            "circle(50 + );\n"
        );
    }

    // ─────────────────────────────────────────────────────
    //  Runs one test: scan → print tokens → parse → print AST
    // ─────────────────────────────────────────────────────
    static void runTest(String title, String source) {
        System.out.println("──────────────────────────────────────────────");
        System.out.println("  " + title);
        System.out.println("──────────────────────────────────────────────");
        System.out.println("Source:\n" + source);

        // ── SCANNING ──
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();

        System.out.println("Token Stream:");
        for (Token t : tokens) {
            System.out.println("  " + t);
        }

        // ── PARSING ──
        System.out.println("\nAST:");
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parseProgram();
        System.out.print(program.toTree("  "));

        System.out.println();
    }
}
