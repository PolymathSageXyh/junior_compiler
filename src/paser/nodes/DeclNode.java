package paser.nodes;

import lexer.SyntaxType;
import lightllr.AstVisitor;

public class DeclNode extends Node{

    @Override
    public StringBuilder getPaserLog() {
        for (Node child : children) {
            this.paserLog.append(child.getPaserLog());
        }
        return this.paserLog;
    }

    public void accept(AstVisitor astVisitor) {
        for (Node child : this.getChildren()) {
            if (child.getType() == SyntaxType.CONST_DECL) {
                ((ConstDeclNode)child).accept(astVisitor);
            }
            if (child.getType() == SyntaxType.VAR_DECL) {
                ((VarDeclNode)child).accept(astVisitor);
            }
        }
    }

}
