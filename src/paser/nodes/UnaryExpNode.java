package paser.nodes;

import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.SyntaxType;
import paser.Mypair;
import symbol.FuncSymbol;
import symbol.SymbolTable;

import java.util.ArrayList;

public class UnaryExpNode extends Node {

    public void checkError(ArrayList<Mypair<ErrorType, Integer>> errorList, ErrorCheckContext ctx, ErrorCheckReturn ret) {
        if (children.get(0).getType() == SyntaxType.IDENFR && children.get(1).getType() == SyntaxType.LPARENT) {
            FuncSymbol funcSymbol = SymbolTable.getInstance().getFunc(((TokenNode)children.get(0)).getValue());
            boolean flag = false;
            int line = children.get(0).endLine;
            if (funcSymbol == null) {
                errorList.add(Mypair.of(ErrorType.UNDEFINED_IDENT, children.get(0).endLine));
            } else if (funcSymbol.isVoid()) {
                ret.dimension = -1;
            }
            for (int i = 2; i < children.size(); i++) {
                ErrorCheckReturn ret1 = new ErrorCheckReturn();
                children.get(i).checkError(errorList, ctx, ret1);
                if (funcSymbol != null &&
                        (children.get(i).getType() == SyntaxType.FUNC_R_PARAMS ||
                                (!flag && children.get(i).getType() == SyntaxType.RPARENT))) {
                    flag = true;
                    ErrorType kind = funcSymbol.tryMatch(ret1.params);
                    if (kind != ErrorType.CORRECT) {
                        errorList.add(Mypair.of(kind, line));
                    }
                }
            }
        } else {
            for (Node child : children) {
                child.checkError(errorList, ctx, ret);
            }
        }
    }

}
