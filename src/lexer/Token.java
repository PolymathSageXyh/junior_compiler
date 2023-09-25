package lexer;

public class Token {
    private SyntaxType type;
    private int emergeLine;
    private String value;

    public Token(SyntaxType type, int emergeLine, String value) {
        this.type = type;
        this.emergeLine = emergeLine;
        this.value = value;
    }

    public int getEmergeLine() {
        return emergeLine;
    }

    public String getValue() {
        return value;
    }

    public SyntaxType getType() {
        return type;
    }

    @Override
    public String toString()
    {
        return type.toString() + " " + value;
    }
}
