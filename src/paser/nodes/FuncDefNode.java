package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;
import java.util.Objects;

public class FuncDefNode extends Node{
    public String funcName = null;
    public boolean isRetVoid = true;
    public Node params = null;
    public Node block = null;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = -1;
        for (Node child : children) {
            if (child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                funcName = name;
                line = child.endLine;
            } else if (child.getType() == SyntaxType.FUNC_TYPE) {
                isRetVoid = Objects.equals(((TokenNode)child.getChildren().get(0)).getValue(), "void");
            } else if (child.getType() == SyntaxType.LPARENT) {
                SymbolTable.getInstance().startScope();
            } else if (child.getType() == SyntaxType.FUNC_F_PARAMS) {
                params = child;
            } else if (child.getType() == SyntaxType.BLOCK) {
                block = child;
                if (!SymbolTable.getInstance().tryAddFunc(ret.params, name, ret.isVoid)) {
                    errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
                }
                ctx.afterFuncDef = true;
                ret.params.clear();
            }
            child.checkError(errorList, ctx, ret);
        }
        if(!ret.isReturn && !ret.isVoid) {
            errorList.add(Mypair.of(ErrorType.INTFUNC_MISS_RETURN,endLine));
        }
        ret.reset();
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }
}
