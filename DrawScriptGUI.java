import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class DrawScriptGUI extends JFrame {

    // ── Colors ────────────────────────────────────────────
    private static final Color BG        = new Color(245, 245, 248);
    private static final Color PANEL_BG  = Color.WHITE;
    private static final Color ACCENT    = new Color(24, 95, 165);
    private static final Color TAB_SEL   = new Color(230, 241, 251);
    private static final Color BORDER_C  = new Color(210, 215, 220);
    private static final Color CODE_BG   = new Color(30,  30,  40);
    private static final Color CODE_FG   = new Color(220, 220, 220);
    private static final Color KW_COLOR  = new Color(100, 200, 100);
    private static final Color NUM_COLOR = new Color(255, 180,  80);
    private static final Color ERR_COLOR = new Color(255,  80,  80);
    private static final Color OK_COLOR  = new Color( 60, 180,  80);

    // ── Widgets ───────────────────────────────────────────
    private JTextPane  codeEditor;
    private JTextArea  tokenOutput;
    private JTree      astTree;
    private JTextArea  errorsArea;
    private DrawCanvas drawCanvas;
    private JLabel     statusLabel;

    // ── Preset test cases ─────────────────────────────────
    private static final String[] TEST_LABELS = {
        "Test 1 – Simple valid commands",
        "Test 2 – Variables & expressions",
        "Test 3 – Color & mixed commands",
        "Test 4 – Nested arithmetic",
        "Test 5 – INVALID: missing ')'",
        "Test 6 – INVALID: unknown command",
        "Test 7 – INVALID: bad expression"
    };
    private static final String[] TEST_SOURCES = {
        "move(100, 100);\nline(200, 100);\ncircle(50);\n",
        "size := 40;\nmove(10 + size, 20 * 2);\ncircle(size);\n",
        "color(255);\nmove(80, 80);\nline(120, 80);\nline(120, 120);\n",
        "move((10 + 5) * 2, 100);\ncircle(3 * (4 + 6));\n",
        "move(100, 200;\n",
        "draw(100, 200);\n",
        "circle(50 + );\n"
    };

    // ─────────────────────────────────────────────────────
    public DrawScriptGUI() {
        super("DrawScript — Compiler Front-End");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        buildUI();
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────
    //  UI CONSTRUCTION
    // ─────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        // load first test by default
        codeEditor.setText(TEST_SOURCES[0]);
        runCompiler();
    }

    // ── Header ────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(ACCENT);
        p.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("DrawScript  Compiler Front-End");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        p.add(title, BorderLayout.WEST);

        // test-case selector
        JComboBox<String> combo = new JComboBox<>(TEST_LABELS);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setMaximumSize(new Dimension(320, 30));
        combo.addActionListener(e -> {
            codeEditor.setText(TEST_SOURCES[combo.getSelectedIndex()]);
            runCompiler();
        });

        JButton runBtn = makeButton("▶  Run", PANEL_BG, ACCENT);
        runBtn.addActionListener(e -> runCompiler());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(new JLabel("Preset:") {{ setForeground(Color.WHITE); }});
        right.add(combo);
        right.add(runBtn);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    // ── Center: left editor + right tabbed output ─────────
    private JSplitPane buildCenter() {
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildEditorPanel(), buildOutputTabs());
        sp.setDividerLocation(380);
        sp.setDividerSize(6);
        sp.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));
        sp.setBackground(BG);
        return sp;
    }

    // ── Left: code editor ─────────────────────────────────
    private JPanel buildEditorPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG);

        JLabel lbl = sectionLabel("Source Code");
        p.add(lbl, BorderLayout.NORTH);

        codeEditor = new JTextPane();
        codeEditor.setBackground(CODE_BG);
        codeEditor.setForeground(CODE_FG);
        codeEditor.setCaretColor(Color.WHITE);
        codeEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        codeEditor.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(codeEditor);
        scroll.setBorder(new LineBorder(BORDER_C));
        p.add(scroll, BorderLayout.CENTER);

        JButton runBtn = makeButton("▶  Compile & Run", ACCENT, Color.WHITE);
        runBtn.addActionListener(e -> runCompiler());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(BG);
        btnRow.add(runBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        return p;
    }

    // ── Right: tabbed output ──────────────────────────────
    private JTabbedPane buildOutputTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabs.setBackground(PANEL_BG);

        // Tab 1 – Tokens
        tokenOutput = new JTextArea();
        tokenOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tokenOutput.setEditable(false);
        tokenOutput.setBackground(new Color(250, 250, 252));
        tabs.addTab("🔤 Tokens", scrollOf(tokenOutput));

        // Tab 2 – AST Tree
        astTree = new JTree(new DefaultMutableTreeNode("(run to see AST)"));
        astTree.setFont(new Font("Monospaced", Font.PLAIN, 13));
        astTree.setBackground(PANEL_BG);
        astTree.setRowHeight(22);
        tabs.addTab("🌳 AST", scrollOf(astTree));

        // Tab 3 – Drawing canvas
        drawCanvas = new DrawCanvas();
        JScrollPane canvasScroll = new JScrollPane(drawCanvas);
        canvasScroll.setBorder(null);
        tabs.addTab("🎨 Drawing", canvasScroll);

        // Tab 4 – Grammar reference
        tabs.addTab("📖 Grammar", buildGrammarPanel());

        // Tab 5 – Token spec
        tabs.addTab("🏷  Token Spec", buildTokenSpecPanel());

        // Tab 6 – Design report
        tabs.addTab("📋 Report", buildReportPanel());

        // Tab 7 – Errors
        errorsArea = new JTextArea();
        errorsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        errorsArea.setEditable(false);
        errorsArea.setForeground(ERR_COLOR);
        errorsArea.setBackground(new Color(20, 10, 10));
        tabs.addTab("⚠ Errors", scrollOf(errorsArea));

        return tabs;
    }

    // ── Status bar ────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(230, 232, 236));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_C));
        statusLabel = new JLabel("  Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        p.add(statusLabel, BorderLayout.WEST);
        return p;
    }

    // ─────────────────────────────────────────────────────
    //  GRAMMAR PANEL
    // ─────────────────────────────────────────────────────
    private JScrollPane buildGrammarPanel() {
        JTextPane tp = new JTextPane();
        tp.setEditable(false);
        tp.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tp.setBackground(new Color(252, 252, 255));

        StyledDocument doc = tp.getStyledDocument();
        Style base  = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style rule  = doc.addStyle("rule",  base); StyleConstants.setForeground(rule,  new Color(24, 95, 165)); StyleConstants.setBold(rule, true);
        Style nt    = doc.addStyle("nt",    base); StyleConstants.setForeground(nt,    new Color(83, 58, 183));
        Style kw    = doc.addStyle("kw",    base); StyleConstants.setForeground(kw,    new Color(59,109, 17));  StyleConstants.setBold(kw, true);
        Style lit   = doc.addStyle("lit",   base); StyleConstants.setForeground(lit,   new Color(153, 60, 29));
        Style norm  = doc.addStyle("norm",  base); StyleConstants.setForeground(norm,  Color.DARK_GRAY);
        Style head  = doc.addStyle("head",  base); StyleConstants.setForeground(head,  ACCENT); StyleConstants.setBold(head, true); StyleConstants.setFontSize(head, 15);

        try {
            doc.insertString(doc.getLength(), "GRAMMAR  (Recursive-Descent Friendly)\n\n", head);

            Object[][] rows = {
                {"<program>",    " ::= ", "<statement>", " | ", "<statement>", " ", "<program>"},
                {"<statement>",  " ::= ", "<command>",   " | ", "<assignment>", null, null},
                {"<command>",    " ::= ", "move", "(", "<arglist>", ")", "  |  line(...)  |  circle(...)  |  color(...)"},
                {"<arglist>",    " ::= ", "<expression>", " | ", "<expression>", " , ", "<arglist>"},
                {"<assignment>", " ::= ", "<identifier>", " := ", "<expression>", " ;", null},
                {"<expression>", " ::= ", "<term>", " | ", "<expression>", " + | - ", "<term>"},
                {"<term>",       " ::= ", "<factor>", " | ", "<term>", " * | / ", "<factor>"},
                {"<factor>",     " ::= ", "<number>", " | ", "<identifier>", " | ( ", "<expression> )"},
                {"<number>",     " ::= ", "<digit>", " | ", "<digit>", null, "<number>"},
                {"<digit>",      " ::= ", "0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9", null, null, null, null},
                {"<identifier>", " ::= ", "<letter>", " | ", "<letter>", null, "<identifier>"},
                {"<letter>",     " ::= ", "a | b | c | ... | z", null, null, null, null},
            };

            for (Object[] row : rows) {
                doc.insertString(doc.getLength(), (String)row[0], rule);
                doc.insertString(doc.getLength(), (String)row[1], norm);
                String r2 = (String)row[2];
                Style s2 = r2.startsWith("<") ? nt : (r2.matches("[a-z].*") ? kw : lit);
                doc.insertString(doc.getLength(), r2, s2);
                if (row[3] != null) {
                    doc.insertString(doc.getLength(), (String)row[3], norm);
                    String r4 = (String)row[4];
                    Style s4 = r4 != null && r4.startsWith("<") ? nt : norm;
                    doc.insertString(doc.getLength(), r4, s4);
                }
                if (row[5] != null) doc.insertString(doc.getLength(), (String)row[5], norm);
                if (row[6] != null) {
                    String r6 = (String)row[6];
                    Style s6 = r6.startsWith("<") ? nt : norm;
                    doc.insertString(doc.getLength(), r6, s6);
                }
                doc.insertString(doc.getLength(), "\n", norm);
            }
        } catch (BadLocationException ignored) {}

        return scrollOf(tp);
    }

    // ─────────────────────────────────────────────────────
    //  TOKEN SPEC PANEL
    // ─────────────────────────────────────────────────────
    private JScrollPane buildTokenSpecPanel() {
        String[][] data = {
            {"MOVE, LINE, CIRCLE, COLOR", "Command keywords",    "move  line  circle  color"},
            {"NUMBER",                    "Integer literal",      "0  42  100  255"},
            {"IDENTIFIER",               "Variable name",        "size  x  radius"},
            {"PLUS",                      "Addition operator",    "+"},
            {"MINUS",                     "Subtraction operator", "-"},
            {"STAR",                      "Multiplication",       "*"},
            {"SLASH",                     "Division",             "/"},
            {"ASSIGN",                    "Assignment operator",  ":="},
            {"LPAREN / RPAREN",           "Parentheses",          "(  )"},
            {"SEMICOLON",                 "Statement terminator", ";"},
            {"COMMA",                     "Argument separator",   ","},
            {"NEWLINE",                   "Line separator",       "\\n"},
            {"EOF",                       "End of input",         "(implicit)"},
            {"UNKNOWN",                   "Lexical error token",  "e.g. @  #  $"},
        };

        String[] cols = {"Token Type", "Description", "Examples"};
        JTable table = new JTable(data, cols);
        table.setFont(new Font("Monospaced", Font.PLAIN, 13));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(220, 232, 248));
        table.setGridColor(BORDER_C);
        table.setIntercellSpacing(new Dimension(8, 4));
        table.setSelectionBackground(TAB_SEL);
        table.setEnabled(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return sp;
    }

    // ─────────────────────────────────────────────────────
    //  DESIGN REPORT PANEL
    // ─────────────────────────────────────────────────────
    private JScrollPane buildReportPanel() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setMargin(new Insets(12, 14, 12, 14));
        ta.setBackground(new Color(252, 252, 255));
        ta.setText(
            "DrawScript — Compiler Front-End  |  Design Report\n" +
            "=======================================================\n\n" +
            "OVERVIEW\n" +
            "--------\n" +
            "DrawScript is a small domain-specific language for issuing 2-D\n" +
            "drawing commands (move, line, circle, color) with full arithmetic\n" +
            "expression support and variable assignment.\n\n" +
            "SCANNER (Scanner.java)\n" +
            "----------------------\n" +
            "• Handwritten character-by-character lexer.\n" +
            "• Skips spaces, tabs, carriage returns and // comments.\n" +
            "• Emits NEWLINE tokens (used as optional statement terminators).\n" +
            "• Classifies identifiers as keywords or IDENTIFIER via a switch.\n" +
            "• Unknown characters produce UNKNOWN tokens with a lexical-error\n" +
            "  message rather than crashing.\n\n" +
            "PARSER (Parser.java)\n" +
            "--------------------\n" +
            "• Handwritten recursive-descent parser matching the BNF grammar.\n" +
            "• Builds a real heterogeneous AST (not just a trace).\n" +
            "• Operator precedence is encoded structurally:\n" +
            "    parseExpression → parseTerm → parseFactor\n" +
            "  so * / bind tighter than + -.\n" +
            "• Parenthesised sub-expressions are handled in parseFactor.\n" +
            "• Statement terminators: accepts ; OR newline OR EOF.\n" +
            "• Error recovery: on unexpected tokens the parser prints a message\n" +
            "  and skips the bad token to continue parsing.\n\n" +
            "AST NODES (ASTNode.java)\n" +
            "------------------------\n" +
            "  ProgramNode   – root; list of statements\n" +
            "  CommandNode   – command keyword + list of argument nodes\n" +
            "  AssignNode    – variable name + value expression\n" +
            "  BinaryNode    – operator string + left + right subtrees\n" +
            "  NumberNode    – integer literal\n" +
            "  IdentifierNode– variable reference\n\n" +
            "GUI (DrawScriptGUI.java)\n" +
            "------------------------\n" +
            "• Swing application; does NOT modify any other class.\n" +
            "• Left panel: editable source code editor (dark theme).\n" +
            "• Tokens tab: formatted token stream after scanning.\n" +
            "• AST tab:    JTree rendering of the parsed AST.\n" +
            "• Drawing tab: live 2-D canvas executing valid commands.\n" +
            "• Grammar / Token Spec / Report tabs for documentation.\n" +
            "• Errors tab: collects all lexical and syntax error messages.\n" +
            "• Preset combo-box loads the 7 standard test cases.\n\n" +
            "KNOWN LIMITATIONS\n" +
            "-----------------\n" +
            "• Integer literals only (no floats).\n" +
            "• color() sets a grayscale value (0-255); no named colors.\n" +
            "• No interpreter for variables — drawing uses raw numbers only.\n" +
            "• No semantic analysis (undefined variables are not checked).\n"
        );
        return scrollOf(ta);
    }

    // ─────────────────────────────────────────────────────
    //  COMPILER PIPELINE  (scan → parse → display)
    // ─────────────────────────────────────────────────────
    private void runCompiler() {
        String source = codeEditor.getText();

        // --- redirect System.out to capture error messages ---
        java.io.ByteArrayOutputStream errBuf = new java.io.ByteArrayOutputStream();
        java.io.PrintStream oldOut = System.out;
        System.setOut(new java.io.PrintStream(errBuf));

        // SCAN
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();

        // PARSE
        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parseProgram();

        // restore stdout
        System.setOut(oldOut);
        String errors = errBuf.toString().trim();

        // ── Display tokens ────────────────────────────────
        StringBuilder tb = new StringBuilder();
        for (Token t : tokens) tb.append(t.toString()).append("\n");
        tokenOutput.setText(tb.toString());

        // ── Display AST as JTree ──────────────────────────
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Program");
        for (ASTNode stmt : program.statements) root.add(toTreeNode(stmt));
        astTree.setModel(new DefaultTreeModel(root));
        expandAllNodes(astTree, 0, astTree.getRowCount());

        // ── Display errors ────────────────────────────────
        errorsArea.setText(errors.isEmpty() ? "✔  No errors detected." : errors);
        errorsArea.setForeground(errors.isEmpty() ? OK_COLOR : ERR_COLOR);

        // ── Execute drawing ───────────────────────────────
        drawCanvas.execute(program.statements);

        // ── Status bar ────────────────────────────────────
        long errCount = Arrays.stream(errors.split("\n"))
                              .filter(l -> l.contains("ERROR")).count();
        if (errors.isEmpty() || errCount == 0) {
            statusLabel.setText("  ✔  Compiled OK — "
                + tokens.size() + " tokens, "
                + program.statements.size() + " statements");
            statusLabel.setForeground(new Color(30, 130, 50));
        } else {
            statusLabel.setText("  ✖  " + errCount + " error(s) found");
            statusLabel.setForeground(ERR_COLOR);
        }
    }

    // ─────────────────────────────────────────────────────
    //  AST → JTree node converter
    // ─────────────────────────────────────────────────────
    private DefaultMutableTreeNode toTreeNode(ASTNode node) {
        if (node instanceof ProgramNode) {
            DefaultMutableTreeNode n = new DefaultMutableTreeNode("Program");
            for (ASTNode s : ((ProgramNode) node).statements) n.add(toTreeNode(s));
            return n;
        }
        if (node instanceof CommandNode) {
            CommandNode c = (CommandNode) node;
            DefaultMutableTreeNode n = new DefaultMutableTreeNode("Command: " + c.command.toUpperCase());
            for (ASTNode a : c.args) n.add(toTreeNode(a));
            return n;
        }
        if (node instanceof AssignNode) {
            AssignNode a = (AssignNode) node;
            DefaultMutableTreeNode n = new DefaultMutableTreeNode("Assign: " + a.name);
            n.add(toTreeNode(a.value));
            return n;
        }
        if (node instanceof BinaryNode) {
            BinaryNode b = (BinaryNode) node;
            DefaultMutableTreeNode n = new DefaultMutableTreeNode("BinaryOp: " + b.op);
            n.add(toTreeNode(b.left));
            n.add(toTreeNode(b.right));
            return n;
        }
        if (node instanceof NumberNode)
            return new DefaultMutableTreeNode("Number: " + ((NumberNode) node).value);
        if (node instanceof IdentifierNode)
            return new DefaultMutableTreeNode("Identifier: " + ((IdentifierNode) node).name);
        return new DefaultMutableTreeNode(node.toString());
    }

    private void expandAllNodes(JTree tree, int start, int rowCount) {
        for (int i = start; i < rowCount; i++) tree.expandRow(i);
        if (tree.getRowCount() != rowCount) expandAllNodes(tree, rowCount, tree.getRowCount());
    }

    // ─────────────────────────────────────────────────────
    //  DRAWING CANVAS
    // ─────────────────────────────────────────────────────
    static class DrawCanvas extends JPanel {
        private final List<DrawCmd> cmds = new ArrayList<>();

        DrawCanvas() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(500, 500));
            setBorder(new LineBorder(BORDER_C));
        }

        void execute(List<ASTNode> stmts) {
            cmds.clear();
            int cx = 0, cy = 0;
            Color color = Color.BLACK;

            for (ASTNode stmt : stmts) {
                if (!(stmt instanceof CommandNode)) continue;
                CommandNode cmd = (CommandNode) stmt;
                List<Integer> vals = new ArrayList<>();
                for (ASTNode arg : cmd.args) {
                    Integer v = evalInt(arg);
                    if (v != null) vals.add(v);
                }

                switch (cmd.command.toLowerCase()) {
                    case "move":
                        if (vals.size() >= 2) { cx = vals.get(0); cy = vals.get(1); }
                        break;
                    case "line":
                        if (vals.size() >= 2) {
                            int nx = vals.get(0), ny = vals.get(1);
                            cmds.add(new DrawCmd("line", cx, cy, nx, ny, 0, color));
                            cx = nx; cy = ny;
                        }
                        break;
                    case "circle":
                        if (!vals.isEmpty())
                            cmds.add(new DrawCmd("circle", cx, cy, 0, 0, vals.get(0), color));
                        break;
                    case "color":
                        if (!vals.isEmpty()) {
                            int g = Math.max(0, Math.min(255, vals.get(0)));
                            color = new Color(g, g, g);
                        }
                        break;
                }
            }
            repaint();
        }

        private Integer evalInt(ASTNode n) {
            if (n instanceof NumberNode)  return ((NumberNode) n).value;
            if (n instanceof BinaryNode) {
                BinaryNode b = (BinaryNode) n;
                Integer l = evalInt(b.left), r = evalInt(b.right);
                if (l == null || r == null) return null;
                switch (b.op) {
                    case "+": return l + r;
                    case "-": return l - r;
                    case "*": return l * r;
                    case "/": return r == 0 ? 0 : l / r;
                }
            }
            return null; // identifiers unknown at this stage
        }

        private int toScreenX(int userX) {
            return getWidth() / 2 + userX;
        }

        private int toScreenY(int userY) {
            return getHeight() / 2 - userY;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f));

            // grid
            g2.setColor(new Color(240, 240, 245));
            for (int i = 0; i < getWidth(); i += 20)  g2.drawLine(i, 0, i, getHeight());
            for (int j = 0; j < getHeight(); j += 20) g2.drawLine(0, j, getWidth(), j);

            for (DrawCmd c : cmds) {
                g2.setColor(c.color);
                if ("line".equals(c.kind)) {
                    g2.drawLine(toScreenX(c.x1), toScreenY(c.y1), toScreenX(c.x2), toScreenY(c.y2));
                } else if ("circle".equals(c.kind)) {
                    int r = c.r;
                    int sx = toScreenX(c.x1);
                    int sy = toScreenY(c.y1);
                    g2.drawOval(sx - r, sy - r, 2*r, 2*r);
                }
            }

            // origin dot
            g2.setColor(new Color(200, 80, 80, 180));
            int originX = getWidth() / 2;
            int originY = getHeight() / 2;
            g2.fillOval(originX - 4, originY - 4, 8, 8);

            if (cmds.isEmpty()) {
                g2.setColor(new Color(180, 180, 200));
                g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
                g2.drawString("Run a program to see the drawing output here.", 30, getHeight()/2);
            }
        }
    }

    static class DrawCmd {
        String kind; int x1, y1, x2, y2, r; Color color;
        DrawCmd(String kind, int x1, int y1, int x2, int y2, int r, Color color) {
            this.kind=kind; this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
            this.r=r; this.color=color;
        }
    }

    // ─────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────
    private static JScrollPane scrollOf(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER_C));
        return sp;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private static JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(fg.darker(), 1, true),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new DrawScriptGUI();
        });
    }
}
