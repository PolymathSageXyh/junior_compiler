package lexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SyntaxType
{
    SINGLE_ANNOTATION, MULTI_ANNOTATION,
    MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK, FORTK, GETINTTK, PRINTFTK, RETURNTK, VOIDTK,
    // delimiter
    NOT, AND, OR, PLUS, MINU, MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ,
    ASSIGN, SEMICN, COMMA, SPACE,
    LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE,
    // IntConst
    INTCON,
    // FormatString
    STRCON,
    // Identifier
    IDENFR,
    EOF, ERROR,

    COMP_UNIT, FUNC_TYPE, FUNC_DEF, MAINFUNC_DEF, FUNC_F_PARAMS, FUNC_F_PARAM, BLOCK, DECL, STMT, VAR_DECL,
    CONST_DECL, VAR_DEF, CONST_DEF, INIT_VAL, CONST_INIT_VAL, EXP, CONST_EXP, ADD_EXP, UNARY_EXP, MUL_EXP,
    UNARY_OP, FUNC_R_PARAMS, PRIMARY_EXP, NUMBER, LVAL, COND, LOR_EXP, LAND_EXP, EQ_EXP, REAL_EXP, FOR_STMT,
    BREAK_STMT, CONTINUE_STMT, IF_STMT, FOR_LOOP_STMT, RETURN_STMT, PRINTF_STMT;


    public static String getLog(SyntaxType kind) {
        Map<SyntaxType,String> KeyWord2String = Stream.of(new Object[][] {
                {SyntaxType.COMP_UNIT, "CompUnit"},
                {SyntaxType.DECL, "Decl"},
                {SyntaxType.FUNC_DEF, "FuncDef"},
                {SyntaxType.MAINFUNC_DEF, "MainFuncDef"},
                {SyntaxType.CONST_DECL, "ConstDecl"},
                {SyntaxType.VAR_DEF, "VarDef"},
                {SyntaxType.CONST_DEF, "ConstDef"},
                {SyntaxType.CONST_EXP, "ConstExp"},
                {SyntaxType.CONST_INIT_VAL, "ConstInitVal"},
                {SyntaxType.VAR_DECL, "VarDecl"},
                {SyntaxType.INIT_VAL, "InitVal"},
                {SyntaxType.FUNC_TYPE, "FuncType"},
                {SyntaxType.FUNC_F_PARAMS, "FuncFParams"},
                {SyntaxType.FUNC_F_PARAM, "FuncFParam"},
                {SyntaxType.BLOCK, "Block"},
                {SyntaxType.STMT, "Stmt"},
                {SyntaxType.EXP, "Exp"},
                {SyntaxType.ADD_EXP, "AddExp"},
                {SyntaxType.UNARY_EXP, "UnaryExp"},
                {SyntaxType.MUL_EXP, "MulExp"},
                {SyntaxType.UNARY_OP, "UnaryOp"},
                {SyntaxType.FUNC_R_PARAMS, "FuncRParams"},
                {SyntaxType.PRIMARY_EXP, "PrimaryExp"},
                {SyntaxType.NUMBER, "Number"},
                {SyntaxType.LVAL, "LVal"},
                {SyntaxType.COND, "Cond"},
                {SyntaxType.LOR_EXP, "LOrExp"},
                {SyntaxType.LAND_EXP, "LAndExp"},
                {SyntaxType.EQ_EXP, "EqExp"},
                {SyntaxType.REAL_EXP, "RelExp"},
                {SyntaxType.FOR_STMT, "ForStmt"}
        }).collect(Collectors.toMap(data -> (SyntaxType) data[0], data -> (String) data[1]));
        return KeyWord2String.get(kind);
    }

    public static boolean expFirst(SyntaxType type) {
        HashSet<SyntaxType> exprFirst = new HashSet<>(Arrays.asList(
                SyntaxType.PLUS,
                SyntaxType.MINU,
                SyntaxType.NOT,
                SyntaxType.IDENFR,
                SyntaxType.LPARENT,
                SyntaxType.INTCON));
        return exprFirst.contains(type);
    }
}

