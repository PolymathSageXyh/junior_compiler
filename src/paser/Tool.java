package paser;
import lexer.*;

import java.util.ArrayList;

public class Tool {
    private final ArrayList<Token> tokens;
    private int pointer;
    public Tool(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.pointer = 0;
    }

    public void setPointer(int pp) {
        pointer = pp;
    }

    public int getPointer() {
        return pointer;
    }

    public boolean isEnd() {
        return pointer >= tokens.size();
    }

    public void move() {
        pointer += 1;
    }

    public int lastTokenLine() {
        if (pointer == 0) return 0;
        else return lookAhead(-1).getEmergeLine();
    }

    public Token lookAhead(int step) {
        if (pointer + step < tokens.size() && pointer + step >= 0) {
            return tokens.get(pointer + step);
        }
        else {
            return null;
        }
    }

    public boolean isConstDecl() {
        return lookAhead(0).equalType(SyntaxType.CONSTTK);
    }

    public boolean isVarDecl() {
        return lookAhead(0).equalType(SyntaxType.INTTK) && !lookAhead(2).equalType(SyntaxType.LPARENT);
    }

    public boolean isDecl() {
        return isConstDecl() || isVarDecl();
    }

    public boolean isFuncType() {
        return lookAhead(0).equalType(SyntaxType.VOIDTK) || lookAhead(0).equalType(SyntaxType.INTTK);
    }

    public boolean isFuncDef() {
        return isFuncType() && !lookAhead(1).equalType(SyntaxType.MAINTK);
    }

    public boolean isMain() {
        return lookAhead(0).equalType(SyntaxType.INTTK) && lookAhead(1).equalType(SyntaxType.MAINTK) &&
                lookAhead(2).equalType(SyntaxType.LPARENT) && lookAhead(3).equalType(SyntaxType.RPARENT);
    }

    public boolean isLBrack() {
        return lookAhead(0).equalType(SyntaxType.LBRACK);
    }

    public boolean isRBrack() {
        return lookAhead(0).equalType(SyntaxType.RBRACK);
    }

    public boolean isLBrace() {
        return lookAhead(0).equalType(SyntaxType.LBRACE);
    }

    public boolean isRBrace() {
        return lookAhead(0).equalType(SyntaxType.RBRACE);
    }

    public boolean isLParent() {
        return lookAhead(0).equalType(SyntaxType.LPARENT);
    }

    public boolean isRParent() {
        return lookAhead(0).equalType(SyntaxType.RPARENT);
    }

    public boolean isComma() {
        return lookAhead(0).equalType(SyntaxType.COMMA);
    }

    public boolean isSemicn() {
        return lookAhead(0).equalType(SyntaxType.SEMICN);
    }

    public boolean isInt() {
        return lookAhead(0).equalType(SyntaxType.INTTK);
    }

    public boolean isVoid() {
        return lookAhead(0).equalType(SyntaxType.VOIDTK);
    }

    public boolean isPlusOrMinus() {
        return lookAhead(0).equalType(SyntaxType.PLUS) || lookAhead(0).equalType(SyntaxType.MINU);
    }

    public boolean isMulorModorDiv() {
        return lookAhead(0).equalType(SyntaxType.MULT) || lookAhead(0).equalType(SyntaxType.MOD) || lookAhead(0).equalType(SyntaxType.DIV);
    }

    public boolean isOr() {
        return lookAhead(0).equalType(SyntaxType.OR);
    }

    public boolean isAnd() {
        return lookAhead(0).equalType(SyntaxType.AND);
    }

    public boolean isEqualOrNotEqual() {
        return lookAhead(0).equalType(SyntaxType.EQL) || lookAhead(0).equalType(SyntaxType.NEQ);
    }

    public boolean isCompare() {
        return lookAhead(0).equalType(SyntaxType.LEQ) || lookAhead(0).equalType(SyntaxType.GEQ) ||
                lookAhead(0).equalType(SyntaxType.GRE) || lookAhead(0).equalType(SyntaxType.LSS);
    }

    public boolean isIdent() {
        return lookAhead(0).equalType(SyntaxType.IDENFR);
    }

    public boolean isNumber() {
        return lookAhead(0).equalType(SyntaxType.INTCON);
    }

    public boolean isUnaryOp() {
        return lookAhead(0).equalType(SyntaxType.PLUS) || lookAhead(0).equalType(SyntaxType.MINU) ||
                lookAhead(0).equalType(SyntaxType.NOT);
    }

    public SyntaxType getSyntaxTypeOfFirstToken() {
        return lookAhead(0).getType();
    }

    public boolean isAssign() {
        return lookAhead(0).equalType(SyntaxType.ASSIGN);
    }

    public boolean isFor() {
        return lookAhead(0).equalType(SyntaxType.FORTK);
    }

    public boolean isPrintf() {
        return lookAhead(0).equalType(SyntaxType.PRINTFTK);
    }

    public boolean isBreak() {
        return lookAhead(0).equalType(SyntaxType.BREAKTK);
    }

    public boolean isContinue() {
        return lookAhead(0).equalType(SyntaxType.CONTINUETK);
    }

    public boolean isReturn() {
        return lookAhead(0).equalType(SyntaxType.RETURNTK);
    }

    public boolean isIf() {
        return lookAhead(0).equalType(SyntaxType.IFTK);
    }

    public boolean isElse() {
        return lookAhead(0).equalType(SyntaxType.ELSETK);
    }

    public boolean isNot() {
        return lookAhead(0).equalType(SyntaxType.NOT);
    }

    public boolean isGetInt() {
        return lookAhead(0).equalType(SyntaxType.GETINTTK);
    }





}
