package lightllr;

import paser.nodes.*;

public interface AstVisitor {
    public void visit(CompUnitNode compUnitNode);
    public void visit(AddExpNode addExpNode);
    public void visit(BlockNode blockNode);
    public void visit(BreakStmtNode breakStmtNode);
    public void visit(CondNode condNode);
    public void visit(ConstDeclNode constDeclNode);
    public void visit(ConstDefNode constDefNode);
    public void visit(ConstExpNode constExpNode);
    public void visit(ContinueStmtNode continueStmtNode);
    public void visit(DeclNode declNode);
    public void visit(EqExpNode eqExpNode);
    public void visit(ForLoopStmtNode forLoopStmtNode);
    public void visit(ForStmtNode forStmtNode);
    public void visit(FuncDefNode funcDefNode);
    public void visit(FuncFParamNode funcFParamNode);
    public void visit(IfStmtNode ifStmtNode);
    public void visit(InitValNode initValNode);
    public void visit(LAndExpNode lAndExpNode);
    public void visit(LOrExpNode lOrExpNode);
    public void visit(LValNode lValNode);
    public void visit(MainFuncDefNode mainFuncDefNode);
    public void visit(MulExpNode mulExpNode);
    public void visit(NumberNode numberNode);
    public void visit(PrimaryExpNode primaryExpNode);
    public void visit(PrintfStmtNode printfStmtNode);
    public void visit(RelExpNode relExpNode);
    public void visit(ReturnStmtNode returnStmtNode);
    public void visit(StmtNode stmtNode);
    public void visit(UnaryExpNode unaryExpNode);
    public void visit(VarDeclNode varDeclNode);
    public void visit(VarDefNode varDefNode);

}
