package entity;

import exception.SyntaxError;

import java.util.*;

/**
 *  [Singleton]
 *  given source input, supplying token instance each time Parser call getNextToken() method.
 *  jobs:
 *    - do token-level checking.
 *    - parse and specify token type.
 */
public class Lexer {
    private static Map<String, String> KEYWORDS;
    private static Set<Character> WHITESPACE;
    private static Set<Character> NUMBER;
    private static Set<Character> NAME;
    private static Map<String, String> OPERATORS;
    private static Map<Character, String> DELIMITER;

    private static Lexer lexer = new Lexer();

    private int lineNo;
    private int columnNo;
    private String source;
    private int cursor;
    private String[] segs;

    static {
        init();
    }

    private Lexer() {}

    public static Lexer getLexer() { return lexer; }

    /**
     *  get the whole input string of target language source file
     *  do several initial operations
     */
    public void input(String source) {
        this.source = source;
        segs = source.split("\n");
        lineNo = 1;
        columnNo = 1;
        cursor = 0;
    }

    /**
     *  using line No. to index each line for error printing
     */
    private String getSegByNo(int lno) {
        return segs[lno-1];
    }

    /**
     *  print error message in the terminal
     */
    public void generatorErrorMsg(int lno, int cno) {
        String errortitle = "Error occured at line " + lno;
        String errorline = "  " + getSegByNo(lno);
        String prefix = "  " + getSegByNo(lno).substring(0, cno);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < prefix.length()-1; i += 1) {
            sb.append(" ");
        }
        System.out.println(errortitle);
        System.out.println(errorline);
        System.out.println(sb.append("^").toString());
    }

    /**
     *  language elements initialization entry
     */
    private static void init() {
        KeywordInit();
        WhitespaceInit();
        NumberInit();
        NameInit();
        OperatorInit();
        DelimiterInit();
    }

    private static void KeywordInit() {
        KEYWORDS = new HashMap<>();
        String[] keywords = new String[] {"PROGRAM", "while", "def", "if", "else", "elif", "for", "return", "lambda"};
        String[] names = new String[] {"PROGRAM", "WHILE", "DEF", "IF", "ELSE", "ELIF", "FOR", "RETURN", "LAMBDA"};
        for (int i = 0; i < keywords.length; i += 1) {
            KEYWORDS.put(keywords[i], names[i]);
        }
    }

    private static void WhitespaceInit() {
        WHITESPACE = new HashSet<>();
        String set = " \t";
        for (int i = 0; i < set.length(); i += 1) {
            WHITESPACE.add(set.charAt(i));
        }
    }

    private static void NumberInit() {
        NUMBER = new HashSet<>();
        String set = "0123456789.-";
        for (int i = 0; i < set.length(); i += 1) {
            NUMBER.add(set.charAt(i));
        }
    }

    private static void NameInit() {
        NAME = new HashSet<>();
        String set = "_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < set.length(); i += 1) {
            NAME.add(set.charAt(i));
        }
    }

    private static void OperatorInit() {
        OPERATORS = new HashMap<>();
        String[] operators = new String[] {"+", "-", "*", "/", "//", "=", ">", "<", ">=", "<=", "==", "!="};
        String[] names = new String[] {"PLUS", "MINUS", "MULT", "DIV", "TRUEDIV", "ASSIGN", "GT", "LT", "GE", "LE", "EQ", "NE"};
        for (int i = 0; i < operators.length; i += 1) {
            OPERATORS.put(operators[i], names[i]);
        }
    }

    private static void DelimiterInit() {
        DELIMITER = new HashMap<>();
        String set = "(),{}:";
        String[] names = new String[] {"LP", "RP", "COMMA", "LCB", "RCB", "COLON"};
        for (int i = 0; i < set.length(); i += 1) {
            DELIMITER.put(set.charAt(i), names[i]);
        }
    }

    /**
     *  get current character
     *  supporting infinite input abstraction
     */
    private char getCurrentChar() throws SyntaxError {
        if (cursor >= source.length()) {
            throw new SyntaxError("Reaching end of file.");
        }
        return source.charAt(cursor);
    }

    /**
     *  get next character for forward looking without increment global cursor
     */
    private char peek() throws SyntaxError {
        char c;
        try {
            c = source.charAt(cursor+1);
        } catch (Exception e) {
            throw new SyntaxError("Reaching end of file.");
        }
        return c;
    }

    /**
     *  increment cursor by one position in the current line
     */
    private void advance() {
        cursor += 1;
        columnNo += 1;
    }

    /**
     *  increment cursor by one position through moving to next line
     */
    private void jumpToNextLine() {
        cursor += 1;
        columnNo = 1;
        lineNo += 1;
    }

    /**
     *  check if next character exists in the given set passed as parameter
     */
    private boolean peek(Set<Character> set) {
        try {
            char n = peek();
            if (set.contains(n)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *  consume a character sequence and parse it into corresponding number case
     *  reporting error when number is invalid (e.g. 3.1.4)
     */
    private Token getNumericToken() throws SyntaxError {
        StringBuffer sb = new StringBuffer();
        int lno = lineNo;
        int cno = columnNo;
        while (NUMBER.contains(getCurrentChar())) {
            sb.append(getCurrentChar());
            advance();
        }
        String rawNumber = sb.toString();
        try {
            return new Token("CONST_INT", Integer.parseInt(rawNumber), lno, cno);
        } catch (NumberFormatException e1) {
            try {
                return new Token("CONST_DOUBLE", Double.parseDouble(rawNumber), lno, cno);
            } catch (NumberFormatException e2) {
                generatorErrorMsg(lno, cno);
                throw new SyntaxError("Invalid numeric: " + rawNumber + ".");
            }
        }
    }

    /**
     *  consume a character sequence until next character not existing in NAME set
     *  return name in string format
     */
    private String getName() throws SyntaxError {
        StringBuffer sb = new StringBuffer();
        while (NAME.contains(getCurrentChar())) {
            sb.append(getCurrentChar());
            advance();
        }
        return sb.toString();
    }

    /**
     *  return token each time method get called
     *  report error when invalid character caught
     */
    public Token getNextToken() throws SyntaxError {
        gotoChar();
        char c = getCurrentChar();
        if (DELIMITER.containsKey(c)) {
            //  "(),{}:"
            Token token = new Token(DELIMITER.get(c), String.valueOf(c), lineNo, columnNo);
            advance();
            return token;
        } else if (OPERATORS.containsKey(String.valueOf(c)) || c == '!') {
            // "+", "-", "*", "/", "//", "=", ">", "<", ">=", "<=", "==", "!="
            Token token;
            if (c == '/') {
                if (peek() == '/') {
                    token = new Token(OPERATORS.get("//"), "//", lineNo, columnNo);
                    advance();advance();
                } else {
                    token = new Token(OPERATORS.get("/"), "/", lineNo, columnNo);
                    advance();
                }
            } else if (c == '<') {
                if (peek() == '=') {
                    token = new Token(OPERATORS.get("<="), "<=", lineNo, columnNo);
                    advance();advance();
                } else {
                    token = new Token(OPERATORS.get("<"), "<", lineNo, columnNo);
                    advance();
                }
            } else if (c == '>') {
                if (peek() == '=') {
                    token = new Token(OPERATORS.get(">="), ">=", lineNo, columnNo);
                    advance();advance();
                } else {
                    token = new Token(OPERATORS.get(">"), ">", lineNo, columnNo);
                    advance();
                }
            } else if (c == '=') {
                if (peek() == '=') {
                    token = new Token(OPERATORS.get("=="), "==", lineNo, columnNo);
                    advance();advance();
                } else {
                    token = new Token(OPERATORS.get("="), "=", lineNo, columnNo);
                    advance();
                }
            } else if (c == '!') {
                token = new Token(OPERATORS.get("!="), "!=", lineNo, columnNo);
                advance();
                advance();
            } else if (c != '-' || !peek(NUMBER)){
                token = new Token(OPERATORS.get(String.valueOf(c)), String.valueOf(c), lineNo, columnNo);
                advance();
            } else {
                return getNumericToken();
            }
            return token;
        } else if (!NUMBER.contains(c) && NAME.contains(c)) {
            int lno = lineNo;
            int cno = columnNo;
            String name = getName();
            if (KEYWORDS.containsKey(name)) {
                return new Token(KEYWORDS.get(name), name, lno, cno);
            } else if (name.equals("True") || name.equals("False")) {
                if (name.equals("True")) {
                    return new Token("CONST_BOOLEAN", true, lno, cno);
                } else {
                    return new Token("CONST_BOOLEAN", false, lno, cno);
                }
            } else {
                return new Token("ID", name, lno, cno);
            }
        } else if (NUMBER.contains(c)) {
            return getNumericToken();
        } else {
            generatorErrorMsg(lineNo, columnNo);
            throw new SyntaxError("Invalid character: " + c + ".");
        }
    }

    /**
     *  go to next meaningful character through removing whitespace, comment
     */
    private void gotoChar() throws SyntaxError {
        removeWhitespace();
        while (getCurrentChar() == '\n' || getCurrentChar() == '/') {
            if (getCurrentChar() == '\n') {
                jumpToNextLine();
                removeWhitespace();
            } else {
                if (peek() == '*') {
                    removeComment();
                    removeWhitespace();
                } else {
                    break;
                }
            }
        }
    }

    /**
     *  jump through comment within SLASH STAR     STAR  SLASH
     */
    private void removeComment() throws SyntaxError {
        advance();advance();
        while (getCurrentChar() != '*' || peek() != '/') {
            if (getCurrentChar() == '\n') {
                jumpToNextLine();
            } else {
                advance();
            }
        }
        advance();advance();
    }

    /**
     *  remove white space
     */
    private void removeWhitespace() throws SyntaxError {
        while (WHITESPACE.contains(getCurrentChar())) {
            advance();
        }
    }
}
