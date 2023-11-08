package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class LValNode extends Node {
    public String name = null;
    public ArrayList<Node> offset = new ArrayList<>();

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        TokenNode temp = null;
        int cnt = 0;
        ArrayList<Integer> offset = new ArrayList<>();
        for (Node child : children) {
            if(child.getType() == SyntaxType.IDENFR) {
                temp = (TokenNode)child;
                name = temp.getValue();
                if (SymbolTable.getInstance().getVar(temp.getValue()) == null) {
                    errorList.add(Mypair.of(ErrorType.UNDEFINED_IDENT, temp.endLine));
                    return;
                } else if (ctx.isLVal && SymbolTable.getInstance().getVar(temp.getValue()).isConst()) {
                    errorList.add(Mypair.of(ErrorType.CONSTANT_ASSIGNING, temp.endLine));
                    return;
                }
                ctx.isLVal = false;
            } else if(child.getType() ==SyntaxType.EXP) {
                this.offset.add(child);
                cnt++;
                if (ctx.isConstExp) {
                    ret.val = 0;
                    child.checkError(errorList, ctx, ret);
                    offset.add(ret.val);
                    ret.val = 0;
                } else {
                    child.checkError(errorList, ctx, ret);
                }
            } else {
                child.checkError(errorList, ctx, ret);
            }
        }
        if (temp != null) {
            ret.dimension = SymbolTable.getInstance().getVar(temp.getValue()).getArrayDim() - cnt;
        }
        if (ctx.isConstExp) {
            if (cnt == 0) {
                assert temp != null;
                ret.val = SymbolTable.getInstance().getVar(temp.getValue()).getVVV();
            } else {
                assert temp != null;
                ret.val = SymbolTable.getInstance().getVar(temp.getValue()).getVVV(offset);
            }
        }
    }

}
