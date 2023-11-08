package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.SymbolTable;

import java.util.ArrayList;

public class InitValNode extends Node {
    public ArrayList<Node> inits = new ArrayList<>();
    public ArrayList<Integer> nums = new ArrayList<>();

    @Override
    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (!SymbolTable.getInstance().isGlobal()) {
            if (children.size() == 1) {
                inits.add(children.get(0));
                children.get(0).checkError(errorList, ctx, ret);
            } else {
                for (Node child : children) {
                    if (child.getType() == SyntaxType.INIT_VAL) {
                        child.checkError(errorList, ctx, ret);
                        this.inits.addAll(((InitValNode)child).inits);
                    }
                }
            }
        } else {
            if (children.size() == 1) {
                ctx.isConstExp = true;
                children.get(0).checkError(errorList, ctx, ret);
                ctx.isConstExp = false;
                nums.add(ret.val);
            } else {
                for (Node child : children) {
                    if (child.getType() == SyntaxType.INIT_VAL) {
                        child.checkError(errorList, ctx, ret);
                        this.nums.addAll(((InitValNode)child).nums);
                    }
                }
            }
        }
    }

}
