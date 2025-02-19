import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.statement.StatementKernel;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Yifan Yao, Yueyi Hua
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        return Condition.valueOf(c.replace('-', '_').toUpperCase());
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        // Take "IF" out
        tokens.dequeue();

        // Take Condition out
        String s1 = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(s1),
                "Error: CONDITION expected, found: \"" + s1 + "\"");
        StatementKernel.Condition c = parseCondition(s1);

        // Take "THEN" out
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError(s1.equals("THEN"),
                "Error: Keyword \"THEN\" expected, found: \"" + s1 + "\"");

        // Process first Statement
        Statement stat1 = s.newInstance();
        stat1.parseBlock(tokens);

        // Take "ELSE" or "END" out
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError((s1.equals("END")) || (s1.equals("ELSE")),
                "Error: Keywords \"END\" or \"ELSE\" expected, found: \"" + s1
                        + "\"");

        if (s1.equals("END")) {
            s.assembleIf(c, stat1);
        } else {
            // Process second Statement (iff "ELSE" exist)
            Statement stat2 = s.newInstance();
            stat2.parseBlock(tokens);
            s.assembleIfElse(c, stat1, stat2);

            // Take "END" out
            s1 = tokens.dequeue();
        }
        Reporter.assertElseFatalError(s1.equals("END"),
                "Error: Keyword \"END\" expected, found: \"" + s1 + "\"");

        // Take "IF" out
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError(s1.equals("IF"),
                "Error: Keyword \"IF\" expected, found: \"" + s1 + "\"");
    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        // Take "WHILE" out
        tokens.dequeue();

        // Take Condition out
        String s1 = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(s1),
                "Error: CONDITION expected, found: \"" + s1 + "\"");
        StatementKernel.Condition c = parseCondition(s1);

        // Take "DO" out
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError(s1.equals("DO"),
                "Error: Keyword \"DO\" expected, found: \"" + s1 + "\"");

        // Process Statement
        Statement stat = s.newInstance();
        stat.parseBlock(tokens);
        s.assembleWhile(c, stat);

        // Take "END" out
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError(s1.equals("END"),
                "Error: Keyword \"END\" expected, found: \"" + s1 + "\"");
        s1 = tokens.dequeue();
        Reporter.assertElseFatalError(s1.equals("WHILE"),
                "Error: Keyword \"WHILE\" expected, found: \"" + s1 + "\"");
    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";

        s.assembleCall(tokens.dequeue());
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        if (tokens.front().equals("IF")) {
            parseIf(tokens, this);
        } else if (tokens.front().equals("WHILE")) {
            parseWhile(tokens, this);
        } else {
            Reporter.assertElseFatalError(
                    Tokenizer.isIdentifier(tokens.front()),
                    "Violation of: identifier string is proper prefix of tokens");
            parseCall(tokens, this);
        }
    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        this.clear();

        while ((tokens.front().equals("IF")) || (tokens.front().equals("WHILE"))
                || (Tokenizer.isIdentifier(tokens.front()))) {
            // Process Statement
            Statement stat = this.newInstance();

            stat.parse(tokens);
            this.addToBlock(this.lengthOfBlock(), stat);
        }
    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
