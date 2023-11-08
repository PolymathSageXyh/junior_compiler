package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class ConstDefNode extends Node{
    public String nameOfIdent = null;
    public ArrayList<Integer> dimention = new ArrayList<>();
    public ArrayList<Integer> initVals = new ArrayList<>();

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        String name = "";
        int line = 0;
        ArrayList<Integer> dim = new ArrayList<>();
        for (Node child : children) {
            if(child.getType() == SyntaxType.IDENFR) {
                name = ((TokenNode)child).getValue();
                this.nameOfIdent = name;
                line = child.getEndLine();
                child.checkError(errorList, ctx, ret);
            } else if(child.getType() == SyntaxType.CONST_EXP){
                ctx.isConstExp = true;
                child.checkError(errorList, ctx, ret);
                ctx.isConstExp = false;
                dim.add(ret.val); //站位
                ret.val = 0;
            } else if (child.getType() == SyntaxType.CONST_INIT_VAL){
                ctx.isConstInitVal = true;
                child.checkError(errorList, ctx, ret);
            } else {
                child.checkError(errorList, ctx, ret);
            }
        }
        this.dimention = dim;
        if (!ctx.isConstInitVal) {
            if(!SymbolTable.getInstance().addVar(ctx.isConst, dim, name)) {
                errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
            }
        } else {
            ctx.isConstInitVal = false;
            ArrayList<Integer> temp = new ArrayList<>(ret.inits);
            this.initVals = temp;
            ret.inits.clear();
            if(!SymbolTable.getInstance().addVar(ctx.isConst, dim, name, temp)) {
                errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
            }
        }

    }

}
