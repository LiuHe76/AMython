package entity;

/**
 *  Token class appending with extra information(line No., column No.) for error reporting.
 */
public class Token {
    private String name;
    private Object value;
    private int lineNo;
    private int columnNo;

    public Token(String name, Object value, int lineNo, int columnNo) {
        this.name = name;
        this.value = value;
        this.lineNo = lineNo;
        this.columnNo = columnNo;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public int getLineNo() {
        return lineNo;
    }

    public int getColumnNo() {
        return columnNo;
    }

    public String toString() {
        return "Token<" + name + ">:<" + value + ">[Line: " + lineNo + "][Colume: " + columnNo + "]";
    }
}