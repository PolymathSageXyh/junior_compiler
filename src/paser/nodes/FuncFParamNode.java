package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class FuncFParamNode extends Node{
    public int dim = 0;
    public int len = 0;

    public String name = null;

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = 0;
        int dim = 0;
        ArrayList<Integer> temp = new ArrayList<>();
        for (Node child : children) {
            if (child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                this.name = name;
                line = child.endLine;
            } else if (child.getType() == SyntaxType.LBRACK) {
                dim++;
            } else if (child.getType() == SyntaxType.CONST_EXP) {
                ctx.isConstExp = true;
                ret.val = 0;
                child.checkError(errorList, ctx, ret);
                len = ret.val;
                ctx.isConstExp = false;
            } else {
                child.checkError(errorList, ctx, ret);
            }
        }
        ret.dimension = dim;
        this.dim = dim;
        for(int i = 0; i < dim; i++) {
            temp.add(0); //占位
        }
        if (!SymbolTable.getInstance().addVar(false, temp, name)) {
            errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
