package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import lightllr.AstVisitor;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class VarDefNode extends Node {
    public String nameOfIdent = null;
    public ArrayList<Integer> dimention = new ArrayList<>();
    public ArrayList<Node> initVal = new ArrayList<>();
    public ArrayList<Integer> nums = new ArrayList<>();

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
            } else if (child.getType() == SyntaxType.CONST_EXP){
                ctx.isConstExp = true;
                ret.val = 0;
                child.checkError(errorList, ctx, ret);
                dim.add(ret.val); //站位
                ctx.isConstExp = false;
            } else if (child.getType() == SyntaxType.INIT_VAL){
                child.checkError(errorList, ctx, ret);
                if (!SymbolTable.getInstance().isGlobal()) {
                    this.initVal.addAll(((InitValNode)child).inits);
                } else {
                    this.nums.addAll(((InitValNode)child).nums);
                }

            }
        }
        this.dimention = dim;
        if(!SymbolTable.getInstance().addVar(ctx.isConst, dim, name)) {
            errorList.add(Mypair.of(ErrorType.REDEFINED_IDENT, line));
        }
    }

    public void accept(AstVisitor astVisitor) {
        astVisitor.visit(this);
    }

}
