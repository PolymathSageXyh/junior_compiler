package paser;

import error.ErrorType;
import lexer.SyntaxType;
import lexer.Token;
import paser.nodes.*;

import java.util.ArrayList;

public class Parser {
    private Tool tool;
    private TreeBuilder treeBuilder;

    public Parser(ArrayList<Token> tokens) {
        this.tool = new Tool(tokens);
        this.treeBuilder = new TreeBuilder();
    }

    public void terminalSymbol() {
        treeBuilder.terminalSymbol(tool.lookAhead(0).getType(), tool.lookAhead(0).getEmergeLine(), tool.lookAhead(0).getValue());
        tool.move();
    }

    public Node parseAll() {
        parseCompUnit();
        return treeBuilder.root();
    }

    public void parseCompUnit() {
        treeBuilder.buildNode(SyntaxType.COMP_UNIT);
        SyntaxType type = tool.lookAhead(0).getType();
        while (type != SyntaxType.EOF) {
            if (tool.isDecl()) {
                parseDecl();
            } else if (tool.isFuncDef()) {
                parseFuncDef();
            } else if (tool.isMain()) {
                parseMainFuncDef();
            } else {
                System.out.println("CompUnit error");
                return;
            }
            type = tool.lookAhead(0).getType();
        }
        treeBuilder.finishNode(new CompUnitNode());
    }

    public void parseDecl() {
        treeBuilder.buildNode(SyntaxType.DECL);
        if (tool.isConstDecl()) {
            parseConstDecl();
        } else {
            parseVarDecl();
        }
        treeBuilder.finishNode(new DeclNode());
    }

    public void parseFuncDef() {
        treeBuilder.buildNode(SyntaxType.FUNC_DEF);
        parseFuncType();
        terminalSymbol(); //ident
        terminalSymbol(); //(
        while (tool.isInt()) {
            parseFuncFParams();
        }
        //terminalSymbol(); //)
        checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
        parseBlock();
        treeBuilder.finishNode(new FuncDefNode());
    }

    public void parseMainFuncDef() {
        treeBuilder.buildNode(SyntaxType.MAINFUNC_DEF);
        terminalSymbol(); //int
        terminalSymbol(); //main
        terminalSymbol(); //(
        //terminalSymbol(); //)
        checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
        parseBlock();
        treeBuilder.finishNode(new MainFuncDefNode());
    }

    public void parseConstDecl() {
        treeBuilder.buildNode(SyntaxType.CONST_DECL);
        terminalSymbol(); //const
        terminalSymbol(); // int
        parseConstDef();
        while (tool.isComma()) {
            terminalSymbol(); //,
            parseConstDef();
        }
        //terminalSymbol();//;
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new ConstDeclNode());
    }


    public void parseConstDef() {
        treeBuilder.buildNode(SyntaxType.CONST_DEF);
        terminalSymbol(); //ident
        while (tool.isLBrack()) {
            terminalSymbol(); // [
            parseConstExp();
            //terminalSymbol(); //]
            checkToken(SyntaxType.RBRACK, ErrorType.RBRACK_MISSING);
        }
        terminalSymbol();// =
        parseConstInitVal();
        treeBuilder.finishNode(new ConstDefNode());
    }

    public void parseConstInitVal() {
        treeBuilder.buildNode(SyntaxType.CONST_INIT_VAL);
        if (SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) {       //ConstExp == Exp
            parseConstExp();
        } else if (tool.isLBrace()) {
            terminalSymbol();
            if (tool.isLBrace() || SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) {
                parseConstInitVal();
                while (tool.isComma()) {
                    terminalSymbol();
                    parseConstInitVal();
                }
                terminalSymbol();
            } else if (tool.isRBrace()) {
                terminalSymbol();
            } else {
                System.out.println("ConstInitVal error");
                return;
            }
        } else {
            System.out.println("ConstInitVal error");
            return;
        }
        treeBuilder.finishNode(new ConstInitValNode());
    }

    public void parseConstExp() {
        treeBuilder.buildNode(SyntaxType.CONST_EXP);
        parseAddExp();
        treeBuilder.finishNode(new ConstExpNode());
    }

    public void parseVarDecl() {
        treeBuilder.buildNode(SyntaxType.VAR_DECL);
        terminalSymbol(); //int
        parseVarDef();
        while (tool.isComma()) {
            terminalSymbol(); //,
            parseVarDef();
        }
        //terminalSymbol(); //;
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new VarDeclNode());
    }

    public void parseVarDef() {
        treeBuilder.buildNode(SyntaxType.VAR_DEF);
        terminalSymbol(); //ident
        while (tool.isLBrack()) {
            terminalSymbol(); //[
            parseConstExp();
            //terminalSymbol();
            checkToken(SyntaxType.RBRACK, ErrorType.RBRACK_MISSING);
        }
        if (tool.isAssign()) {
            terminalSymbol();
            parseInitVal();
        }
        treeBuilder.finishNode(new VarDefNode());
    }

    public void parseInitVal() {
        treeBuilder.buildNode(SyntaxType.INIT_VAL);
        if (SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) {
            parseExp();
        } else if (tool.isLBrace()) {
            terminalSymbol();
            if (tool.isLBrace() || SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) {
                parseInitVal();
                while (tool.isComma()) {
                    terminalSymbol();
                    parseInitVal();
                }
                terminalSymbol();
            } else if (tool.isRBrace()) {
                terminalSymbol();
            } else {
                System.out.println("Initval error");
                return;
            }
        } else {
            System.out.println("Initval error");
            return;
        }
        treeBuilder.finishNode(new InitValNode());
    }

    public void parseFuncType() {
        treeBuilder.buildNode(SyntaxType.FUNC_TYPE);
        terminalSymbol(); //void or int
        treeBuilder.finishNode(new FuncTypeNode());
    }

    public void parseFuncFParams() {
        treeBuilder.buildNode(SyntaxType.FUNC_F_PARAMS);
        parseFuncFParam();
        while(tool.isComma()) {
            terminalSymbol();
            parseFuncFParam();
        }
        treeBuilder.finishNode(new FuncFParamsNode());
    }

    public void parseFuncFParam() {
        treeBuilder.buildNode(SyntaxType.FUNC_F_PARAM);
        terminalSymbol(); //BType
        terminalSymbol(); //ident
        if(tool.isLBrack()){
            terminalSymbol(); //[
            //terminalSymbol(); //]
            checkToken(SyntaxType.RBRACK, ErrorType.RBRACK_MISSING);
            while(tool.isLBrack()) {
                terminalSymbol();
                parseConstExp();
                //terminalSymbol();
                checkToken(SyntaxType.RBRACK, ErrorType.RBRACK_MISSING);
            }
        }
        treeBuilder.finishNode(new FuncFParamNode());
    }

    public void parseAddExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.ADD_EXP);
        parseMulExp();
        while(tool.isPlusOrMinus()){
            treeBuilder.buildNodeAt(point, SyntaxType.ADD_EXP);
            treeBuilder.finishNode(new AddExpNode());
            terminalSymbol();
            parseMulExp();
        }
        treeBuilder.finishNode(new AddExpNode());
    }

    public void parseMulExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.MUL_EXP);
        parseUnaryExp();
        while(tool.isMulorModorDiv()) {
            treeBuilder.buildNodeAt(point, SyntaxType.MUL_EXP);
            treeBuilder.finishNode(new MulExpNode());
            terminalSymbol();
            parseUnaryExp();
        }
        treeBuilder.finishNode(new MulExpNode());
    }

    public void parseLOrExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.LOR_EXP);
        parseLAndExp();
        while(tool.isOr()) {
            treeBuilder.buildNodeAt(point, SyntaxType.LOR_EXP);
            treeBuilder.finishNode(new LOrExpNode());
            terminalSymbol();
            parseLAndExp();
        }
        treeBuilder.finishNode(new LOrExpNode());
    }

    public void parseLAndExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.LAND_EXP);
        parseEqExp();
        while(tool.isAnd()) {
            treeBuilder.buildNodeAt(point, SyntaxType.LAND_EXP);
            treeBuilder.finishNode(new LAndExpNode());
            terminalSymbol();
            parseEqExp();
        }
        treeBuilder.finishNode(new LAndExpNode());
    }

    public void parseEqExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.EQ_EXP);
        parseRelExp();
        while(tool.isEqualOrNotEqual()) {
            treeBuilder.buildNodeAt(point, SyntaxType.EQ_EXP);
            treeBuilder.finishNode(new EqExpNode());
            terminalSymbol();
            parseRelExp();
        }
        treeBuilder.finishNode(new EqExpNode());
    }

    public void parseRelExp() {
        int point = treeBuilder.getChildrenSize();
        treeBuilder.buildNode(SyntaxType.REAL_EXP);
        parseAddExp();
        while(tool.isCompare()) {
            treeBuilder.buildNodeAt(point, SyntaxType.REAL_EXP);
            treeBuilder.finishNode(new RelExpNode());
            terminalSymbol();
            parseAddExp();
        }
        treeBuilder.finishNode(new RelExpNode());
    }

    public void parseCond() {
        treeBuilder.buildNode(SyntaxType.COND);
        parseLOrExp();
        treeBuilder.finishNode(new CondNode());
    }

    public void parseForStmt() {
        treeBuilder.buildNode(SyntaxType.FOR_STMT);
        parseLVal();
        terminalSymbol(); //=
        parseExp();
        treeBuilder.finishNode(new ForStmtNode());
    }

    public void parseExp() {
        treeBuilder.buildNode(SyntaxType.EXP);
        parseAddExp();
        treeBuilder.finishNode(new ExpNode());
    }

    public void parseLVal() {
        treeBuilder.buildNode(SyntaxType.LVAL);
        terminalSymbol(); //ident
        while (tool.isLBrack()) {
            terminalSymbol(); //[
            parseExp();
            //terminalSymbol(); //]
            checkToken(SyntaxType.RBRACK, ErrorType.RBRACK_MISSING);
        }
        treeBuilder.finishNode(new LValNode());
    }

    public void parseNumber() {
        treeBuilder.buildNode(SyntaxType.NUMBER);
        terminalSymbol(); //number
        treeBuilder.finishNode(new NumberNode());
    }

    public void parsePrimaryExp() {
        treeBuilder.buildNode(SyntaxType.PRIMARY_EXP);
        if (tool.isLParent()) {
            terminalSymbol();
            parseExp();
            terminalSymbol();
            treeBuilder.finishNode(new PrimaryExpNode());
        } else if (tool.isIdent()) {
            parseLVal();
            treeBuilder.finishNode(new PrimaryExpNode());
        } else if (tool.isNumber()) {
            parseNumber();
            treeBuilder.finishNode(new PrimaryExpNode());
        } else {
            System.out.println("parimary error");
        }
    }

    public void parseUnaryExp() {
        treeBuilder.buildNode(SyntaxType.UNARY_EXP);
        if (tool.isUnaryOp()) {
            parseUnaryOp();
            parseUnaryExp();
        } else if (tool.isLParent() || tool.isNumber()) {
            parsePrimaryExp();
        } else if (tool.isIdent()) {
            if (tool.lookAhead(1).equalType(SyntaxType.LPARENT)) {
                terminalSymbol();// ident
                terminalSymbol();// (
                if (tool.isRParent()) {
                    terminalSymbol();
                } else if (SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) {
                    parseFuncRParams();
                    //terminalSymbol(); //)
                    checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
                } else {
                    //System.out.println("Unary error");
                    checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
                }
            } else {
                parsePrimaryExp();
            }
        } else {
            System.out.println("Unary error");
        }
        treeBuilder.finishNode(new UnaryExpNode());
    }

    public void parseFuncRParams() {
        treeBuilder.buildNode(SyntaxType.FUNC_R_PARAMS);
        parseExp();
        while (tool.isComma()) {
            terminalSymbol();
            parseExp();
        }
        treeBuilder.finishNode(new FuncRParamsNode());
    }

    public void parseUnaryOp() {
        treeBuilder.buildNode(SyntaxType.UNARY_OP);
        terminalSymbol();
        treeBuilder.finishNode(new UnaryOpNode());
    }

    public void parseBlock() {
        treeBuilder.buildNode(SyntaxType.BLOCK);
        terminalSymbol(); //{
        while(true){
            if (tool.isDecl()) {
                parseDecl();
            } else if (tool.isRBrace()) {
                break;
            } else {
                parseStmt();
            }
        }
        terminalSymbol(); //}
        treeBuilder.finishNode(new BlockNode());
    }

    public void parseForLoopStmt() {
        treeBuilder.buildNode(SyntaxType.FOR_LOOP_STMT);
        terminalSymbol();// for
        terminalSymbol();// (
        if (tool.isIdent()) { parseForStmt(); }
        if (tool.isSemicn()) { terminalSymbol(); }
        else { System.out.println("for loop error"); }
        if (SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) { parseCond(); }
        if (tool.isSemicn()) { terminalSymbol(); }
        else { System.out.println("for loop error"); }
        if (tool.isIdent()) { parseForStmt(); }
        if (tool.isRParent()) { terminalSymbol(); }
        else { System.out.println("for loop error"); }
        parseStmt();
        treeBuilder.finishNode(new ForLoopStmtNode());
    }

    public void parseReturnStmt() {
        treeBuilder.buildNode(SyntaxType.RETURN_STMT);
        terminalSymbol(); //return
        if (SyntaxType.expFirst(tool.getSyntaxTypeOfFirstToken())) { parseExp(); }
        //if (tool.isSemicn()) { terminalSymbol(); }
        //else {
        //    System.out.println("return error");
        //}
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new ReturnStmtNode());
    }

    public void parseBreakStmt() {
        treeBuilder.buildNode(SyntaxType.BREAK_STMT);
        terminalSymbol();
        //terminalSymbol();
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new BreakStmtNode());
    }

    public void parseContinueStmt() {
        treeBuilder.buildNode(SyntaxType.CONTINUE_STMT);
        terminalSymbol();
        //terminalSymbol();
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new ContinueStmtNode());
    }

    public void parseIfStmt() {
        treeBuilder.buildNode(SyntaxType.IF_STMT);
        terminalSymbol(); //if
        terminalSymbol(); //(
        parseCond();
        //terminalSymbol(); //)
        checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
        parseStmt();
        if(tool.isElse()){
            terminalSymbol();
            parseStmt();
        }
        treeBuilder.finishNode(new IfStmtNode());
    }

    public void parsePrintfStmt() {
        treeBuilder.buildNode(SyntaxType.PRINTF_STMT);
        terminalSymbol();
        terminalSymbol();
        terminalSymbol();
        while(tool.isComma()){
            terminalSymbol();
            parseExp();
        }
        //terminalSymbol(); //)
        //terminalSymbol(); //;
        checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        treeBuilder.finishNode(new PrintfStmtNode());
    }

    public void parseStmt() {
        SyntaxType type = null;
        treeBuilder.buildNode(SyntaxType.STMT);
        if (tool.isFor()) {
            type = SyntaxType.FOR_LOOP_STMT;
            parseForLoopStmt();
        } else if (tool.isReturn()) {
            type = SyntaxType.RETURN_STMT;
            parseReturnStmt();
        } else if (tool.isBreak()) {
            type = SyntaxType.BREAK_STMT;
            parseBreakStmt();
        } else if (tool.isContinue()) {
            type = SyntaxType.CONTINUE_STMT;
            parseContinueStmt();
        } else if (tool.isPrintf()) {
            type = SyntaxType.PRINTF_STMT;
            parsePrintfStmt();
        } else if (tool.isIf()) {
            type = SyntaxType.IF_STMT;
            parseIfStmt();
        } else if (tool.isSemicn()) {
            terminalSymbol(); //;
        } else if (tool.isLBrace()) {
            type = SyntaxType.BLOCK;
            parseBlock();
        } else if (tool.isRBrace()) {
            checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
        } else {
            if (tool.isNumber() || tool.isPlusOrMinus() || tool.isNot() || tool.isLParent()) {
                type = SyntaxType.EXP;
                parseExp();
                //terminalSymbol(); //;
                checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
            } else if (tool.isIdent()) {
                int point = tool.getPointer();
                parseLVal();
                if (tool.lookAhead(0).equalType(SyntaxType.ASSIGN)) {
                    type = SyntaxType.ASSIGN;
                    terminalSymbol();
                    if (tool.isGetInt()) {
                        terminalSymbol();
                        terminalSymbol();
                        //terminalSymbol();
                        checkToken(SyntaxType.RPARENT, ErrorType.RPARENT_MISSING);
                        //terminalSymbol();
                        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
                    } else {
                        parseExp();
                        //terminalSymbol();
                        checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
                    }
                } else {
                    treeBuilder.children.pop();
                    tool.setPointer(point);
                    type = SyntaxType.EXP;
                    parseExp();
                    //terminalSymbol();
                    checkToken(SyntaxType.SEMICN, ErrorType.SEMICOLON_MISSING);
                }
            } else {
                System.out.println("Stmt error");
                return;
            }
        }
        treeBuilder.finishNode(new StmtNode(type));
    }

    private void checkToken(SyntaxType syntaxType, ErrorType errorType){
        if(tool.getSyntaxTypeOfFirstToken() == syntaxType){
            terminalSymbol();
        } else {
            treeBuilder.addErrorNode(errorType, tool.lastTokenLine());
        }
    }

}
