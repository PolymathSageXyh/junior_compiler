package lexer;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private final String sourceCode;
    private final int sourceCodeLength;
    private int linePointer;
    private int chrPointer;
    private static final HashMap<String, SyntaxType> reserveWords = new HashMap<>();
    private static final HashMap<String, SyntaxType> demarcations = new HashMap<>();
    private final ArrayList<Token> tokens;

    static
    {
        reserveWords.put("main", SyntaxType.MAINTK);
        reserveWords.put("const", SyntaxType.CONSTTK);
        reserveWords.put("int", SyntaxType.INTTK);
        reserveWords.put("break", SyntaxType.BREAKTK);
        reserveWords.put("continue", SyntaxType.CONTINUETK);
        reserveWords.put("if", SyntaxType.IFTK);
        reserveWords.put("else", SyntaxType.ELSETK);
        reserveWords.put("for", SyntaxType.FORTK);
        reserveWords.put("getint", SyntaxType.GETINTTK);
        reserveWords.put("printf", SyntaxType.PRINTFTK);
        reserveWords.put("return", SyntaxType.RETURNTK);
        reserveWords.put("void", SyntaxType.VOIDTK);
        demarcations.put("+", SyntaxType.PLUS);
        demarcations.put("-", SyntaxType.MINU);
        demarcations.put("*", SyntaxType.MULT);
        demarcations.put("%", SyntaxType.MOD);
        demarcations.put(",", SyntaxType.COMMA);
        demarcations.put(";", SyntaxType.SEMICN);
        demarcations.put("(", SyntaxType.LPARENT);
        demarcations.put(")", SyntaxType.RPARENT);
        demarcations.put("[", SyntaxType.LBRACK);
        demarcations.put("]", SyntaxType.RBRACK);
        demarcations.put("{", SyntaxType.LBRACE);
        demarcations.put("}", SyntaxType.RBRACE);
        demarcations.put("<", SyntaxType.LSS);
        demarcations.put(">", SyntaxType.GRE);
        demarcations.put("=", SyntaxType.ASSIGN);
        demarcations.put("!", SyntaxType.NOT);
        demarcations.put("<=", SyntaxType.LEQ);
        demarcations.put(">=", SyntaxType.GEQ);
        demarcations.put("==", SyntaxType.EQL);
        demarcations.put("!=", SyntaxType.NEQ);
        demarcations.put("&&", SyntaxType.AND);
        demarcations.put("||", SyntaxType.OR);
    }
     public Lexer(String sourceCode) {
         this.sourceCode = sourceCode + "\0";
         this.tokens = new ArrayList<>();
         this.sourceCodeLength = sourceCode.length();
         this.chrPointer = 0;
         this.linePointer = 1;
     }

    public ArrayList<Token> run() {
        while (!isFileEnd()) {
            Token token = getToken();
            if (token == null) {
                System.out.println("bug");
            }
            else if (token.getType() != SyntaxType.SPACE) {
                tokens.add(token);
            }
        }
        tokens.add(new Token(SyntaxType.EOF, 0, null));
        return tokens;
    }

    public ArrayList<Token> getTokens() {
        return this.tokens;
    }

    public void skipBlank() {
        char ch = sourceCode.charAt(chrPointer);
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            if (ch == '\n') { linePointer++; }
            chrPointer++;
            ch = sourceCode.charAt(chrPointer);
        }
    }

    public boolean isFileEnd() {
        return sourceCode.charAt(chrPointer) == '\0';
    }

    public char peek() {
        return sourceCode.charAt(chrPointer + 1);
    }

    public char getChar() {
        chrPointer++;
        return sourceCode.charAt(chrPointer);
    }

    public boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    public Token identOrReserve() {
        StringBuilder ss = new StringBuilder();
        ss.append(sourceCode.charAt(chrPointer));
        while (true) {
            char temp = getChar();
            if (isLetter(temp) || temp == '_' || isDigit(temp)) {
                ss.append(temp);
            } else {
                break;
            }
        }
        String str = ss.toString();
        return (new Token(reserveWords.getOrDefault(str, SyntaxType.IDENFR), linePointer, str));
    }

    public Token constInt() {
        StringBuilder ss = new StringBuilder();
        ss.append(sourceCode.charAt(chrPointer));
        while (true) {
            char temp = getChar();
            if (isDigit(temp)) {
                ss.append(temp);
            } else {
                break;
            }
        }
        String str = ss.toString();
        if (str.length() > 1 && str.charAt(0) == '0') {
            System.out.println("bug");
            return null;
        } else {
            return new Token(SyntaxType.INTCON, linePointer, str);
        }
    }

    public boolean isValidchr(char ch) {
        return ((int) ch >= 40 && (int) ch <= 126) || (int) ch == 32 || (int) ch == 33 || ch == '%';
    }

    public Token formatStr() {
        StringBuilder ss = new StringBuilder();
        ss.append(sourceCode.charAt(chrPointer));
        char temp;
        do {
            temp = getChar();
            ss.append(temp);
            if (temp == '%') {
                char cc = peek();
                if(cc != 'd') {
                    System.out.println("bug");
                    return null;
                }
            } else if (temp == '\\') {
                char cc = peek();
                if(cc != 'n') {
                    System.out.println("bug");
                    return null;
                }
            }
        } while(isValidchr(temp));
        if (temp == '"') {
            getChar();
            return new Token(SyntaxType.STRCON, linePointer, ss.toString());
        } else {
            System.out.println("bug");
            return null;
        }
    }

    public Token singleAnnotation() {
        char temp = getChar();
        while (temp != '\n' && !isFileEnd()) {
            temp = getChar();
        }
        return new Token(SyntaxType.SPACE, linePointer, "");
    }

    public Token multiAnnotation() {
        boolean flag = false;
        getChar();
        char temp;
        do {
            temp = getChar();
            if (temp == '\n') {
                linePointer++;
            } else if (flag && temp == '/') {
                getChar();
                return new Token(SyntaxType.SPACE, linePointer, "");
            } else flag = temp == '*';
        } while (!isFileEnd());
        System.out.println("bug");
        return null;
    }


    public Token getToken() {
        skipBlank();
        char ch = sourceCode.charAt(chrPointer);
        if (ch == '\0') {
            return new Token(SyntaxType.SPACE, linePointer, "\0");
        }
        else if (isLetter(ch) || ch == '_') {
            return identOrReserve();
        } else if (isDigit(ch)) {
            return constInt();
        } else if (ch == '"') {
            return formatStr();
        } else if (ch == '/') {
            char temp = peek();
            if (temp == '/') {
                return singleAnnotation();
            } else if (temp == '*') {
                return multiAnnotation();
            } else {
                getChar();
                return new Token(SyntaxType.DIV, linePointer, "/");
            }
        } else if (ch == '+' || ch == '*' || ch == '-' || ch == '%'
                || ch == '(' || ch == ')' || ch == '[' || ch == ']'
                || ch == '{' || ch == '}' || ch == ';' || ch == ',') {
            getChar();
            return new Token(demarcations.get(String.valueOf(ch)), linePointer, String.valueOf(ch));
        } else if (ch == '<' || ch == '>' || ch == '=' || ch == '!') {
            StringBuilder ss = new StringBuilder();
            ss.append(ch);
            char temp = peek();
            if (temp == '=') {
                getChar();
                ss.append('=');
            }
            getChar();
            return new Token(demarcations.get(ss.toString()), linePointer, ss.toString());
        } else if (ch == '&' || ch == '|') {
            char temp = peek();
            if (temp != ch) {
                System.out.println("bug");
                return null;
            } else {
                getChar();
                String ss = ch + String.valueOf(ch);
                getChar();
                return new Token(demarcations.get(ss), linePointer, ss);
            }
        } else {
            System.out.println("bug");
            return null;
        }
    }

    public String display() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tokens.size() - 1; i++) {
            stringBuilder.append(tokens.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }
}
