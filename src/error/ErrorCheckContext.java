package error;

public class ErrorCheckContext {
    public int loopLevel;
    public boolean afterFuncDef;
    public boolean isConst;
    public boolean isLVal;

    public boolean isConstExp;
    public boolean isConstInitVal;

    public ErrorCheckContext() {
        loopLevel = 0;
        afterFuncDef =false;
        isConst = false;
        isLVal = false;
        isConstExp = false;
        isConstInitVal = false;
    }
}
