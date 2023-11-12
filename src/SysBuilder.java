import lexer.SyntaxType;
import lightllr.*;
import lightllr.Module;
import paser.nodes.*;

import java.util.ArrayList;
import java.util.Stack;

import static java.lang.System.exit;

public class SysBuilder implements AstVisitor {
    private IrBuilder builder;
    private Scope scope;
    private Module module;
    private Value ret = null;
    private int return_flag = 0;       //全局变量标识当前模块是否已经有return语句
    private IrType secondDim = null;
    private Stack<BasicBlock> breakOutOfForLoop = new Stack<>();
    private Stack<BasicBlock> continueOutOfForLoop = new Stack<>();

    private ArrayList<IrType> Ints = new ArrayList<>();
    private boolean isRequireLval = false;
    private int addFlag = 0;
// types
    private IrType VOID_T;
    private IrType INT1_T;
    private IrType INT32_T;
    private IrType INT32PTR_T;

    public SysBuilder() {
        this.module = new Module("Sys_module");
        builder = new IrBuilder(null, module);
        scope = new Scope();
        IrType tyVoid = IrType.getVoidType(module);
        IntegerType tyInt32 = IrType.getInt32Type(module);
        PointerType pp = PointerType.getPointerType(new IntegerType(8, module));

        ArrayList<IrType> getIntParams = new ArrayList<>();
        FunctionType getInt = FunctionType.get(tyInt32, getIntParams);
        Function getIntFunc = Function.create(getInt, "getint", module);

        ArrayList<IrType> putIntParams = new ArrayList<>();
        putIntParams.add(tyInt32);
        FunctionType putInt = FunctionType.get(tyVoid, putIntParams);
        Function putIntFunc = Function.create(putInt, "putint", module);

        ArrayList<IrType> putchParams = new ArrayList<>();
        putchParams.add(tyInt32);
        FunctionType putCh = FunctionType.get(tyVoid, putchParams);
        Function putChFunc = Function.create(putCh, "putch", module);

        ArrayList<IrType> putStrParams = new ArrayList<>();
        putStrParams.add(pp);
        FunctionType putStr = FunctionType.get(tyVoid, putStrParams);
        Function putStrFunc = Function.create(putStr, "putstr", module);

        scope.enter();
        scope.push("getint", getIntFunc);
        scope.push("putint", putIntFunc);
        scope.push("putch", putChFunc);
        scope.push("putstr", putStrFunc);
    }

    public Module getModule() { return module; }


    @Override
    public void visit(CompUnitNode compUnitNode) {
        VOID_T = IrType.getVoidType(module);
        INT1_T = IrType.getInt1Type(module);
        INT32_T = IrType.getInt32Type(module);
        INT32PTR_T = IrType.getInt32PtrType(module);
        ArrayList<Node> children = compUnitNode.getChildren();
        for (Node child : children) {
            if (child.getType() == SyntaxType.DECL) {
                ((DeclNode)child).accept(this);
            } else if (child.getType() == SyntaxType.FUNC_DEF) {
                ((FuncDefNode)child).accept(this);
            } else {
                ((MainFuncDefNode)child).accept(this);
            }
        }
    }

    public ConstantArray loadInitsForArray(ArrayList<Integer> initVals) {
        ArrayList<Constant> temp = new ArrayList<>();
        IrType TyInt32 = IrType.getInt32Type(module);
        for (int item : initVals) {
            temp.add(ConstantInt.get(item, module));
        }
        return ConstantArray.get(ArrayType.get(TyInt32, initVals.size()), temp);
    }

    public ConstantArray loadInitsForArray(ArrayList<Integer> initVals, ArrayList<Integer> dimention) {
        ArrayList<Constant> result = new ArrayList<>();
        IrType TyInt32 = IrType.getInt32Type(module);
        int k = dimention.get(1), n = dimention.get(0);
        ArrayType arrayType = ArrayType.get(TyInt32, k);
        for (int i = 0; i < n; i++) {
            ArrayList<Constant> temp = new ArrayList<>();
            for (int j = 0; j < k; j++) {
                int idx = i * k + j;
                temp.add(ConstantInt.get(initVals.get(idx), module));
            }
            result.add(ConstantArray.get(arrayType, temp));
        }
        return ConstantArray.get(ArrayType.get(arrayType, n), result);

    }

    @Override
    public void visit(AddExpNode addExpNode){
        if(return_flag > 0) return;
        Value AdditiveExpression;
        Value Term;
        Value icmp;
        if (addExpNode.op == null) {
            if (addExpNode.isPrintNeZero && ((MulExpNode)addExpNode.lvar).op == null && (((MulExpNode)addExpNode.lvar).lvar).getChildren().get(0).getType() == SyntaxType.UNARY_OP && (((MulExpNode)addExpNode.lvar).lvar).getChildren().get(0).getChildren().get(0).getType() == SyntaxType.NOT) {
                addExpNode.isPrintNeZero = false;
            }
            ((MulExpNode)addExpNode.lvar).accept(this);
        } else {
            ((AddExpNode)addExpNode.lvar).accept(this);
            AdditiveExpression = ret;
            ((MulExpNode)addExpNode.rvar).accept(this);
            Term = ret;
            if (((IntegerType)AdditiveExpression.getIrType()).getNumBits() != ((IntegerType)Term.getIrType()).getNumBits()) {
                if (((IntegerType)AdditiveExpression.getIrType()).isI1()) {
                    AdditiveExpression = builder.createZext(AdditiveExpression, INT32_T);
                }
                if (((IntegerType)Term.getIrType()).isI1()) {
                    Term = builder.createZext(Term, INT32_T);
                }
            }
            if (addExpNode.op == SyntaxType.PLUS) {
                icmp = builder.createIadd(AdditiveExpression, Term);
            } else {
                icmp = builder.createIsub(AdditiveExpression, Term);
            }
            ret = icmp;
        }
        if (addExpNode.isPrintNeZero) {
            if (((IntegerType)ret.getIrType()).getNumBits() != 32) {
                ret = builder.createZext(ret, INT32_T);
            }
            ret = builder.createIcmpNe(ret, ConstantInt.get(0, module));
        }
    }

    @Override
    public void visit(BlockNode blockNode) {
        if (return_flag > 0) return;
        scope.enter();
        for (Node child : blockNode.getChildren()) {
            if (child.getType() == SyntaxType.DECL) {
                ((DeclNode)child).accept(this);
            } else if(child.getType() == SyntaxType.STMT) {
                ((StmtNode)child).accept(this);
            } else {
                //System.out.println("未定义语法");
            }
        }
        scope.exit();
    }

    @Override
    public void visit(BreakStmtNode breakStmtNode) {
        if (return_flag > 0) return;
        builder.createBr(breakOutOfForLoop.peek());
    }

    @Override
    public void visit(CondNode condNode) {
        if (return_flag > 0) return;
        this.visit((LOrExpNode) condNode.getChildren().get(0));
    }

    @Override
    public void visit(ConstDeclNode constDeclNode) {
        if (return_flag > 0) return;
        for (Node child : constDeclNode.getChildren()) {
            if (child.getType() == SyntaxType.CONST_DEF) {
                this.visit((ConstDefNode)child);
            }
        }
    }

    @Override
    public void visit(ConstDefNode constDefNode) {
        if (return_flag > 0) return;
        IrType TyInt32 = IrType.getInt32Type(module);
        if (!scope.inGlobal()) {  //局部
            if (constDefNode.dimention.size() == 1) { //数组
                ArrayType arrayType = ArrayType.get(TyInt32, constDefNode.dimention.get(0));
                AllocaInstr Local_IntArrayAlloca = builder.createAlloca(arrayType); //为数组分配空间
                scope.push(constDefNode.nameOfIdent, Local_IntArrayAlloca);
                //数组赋值，待定
                for (int i= 0; i < constDefNode.initVals.size(); i++) {
                    ArrayList<Value> tmp = new ArrayList<>();
                    tmp.add(ConstantInt.get(0, module));
                    tmp.add(ConstantInt.get(i, module));
                    GetElementPtrInstr gg = builder.createGep(Local_IntArrayAlloca, tmp);
                    StoreInstr ss = builder.createStore(ConstantInt.get(constDefNode.initVals.get(i), module), gg);
                }

            } else if(constDefNode.dimention.size() == 2) {
                ArrayType arrayType1 = ArrayType.get(TyInt32, constDefNode.dimention.get(1));
                ArrayType arrayType2 = ArrayType.get(arrayType1, constDefNode.dimention.get(0));
                AllocaInstr Local_IntArrayAlloca = builder.createAlloca(arrayType2);
                scope.push(constDefNode.nameOfIdent, Local_IntArrayAlloca);
                for (int i = 0 ; i < constDefNode.dimention.get(0); i++) {
                    for (int j = 0; j < constDefNode.dimention.get(1); j++) {
                        ArrayList<Value> tmp = new ArrayList<>();
                        tmp.add(ConstantInt.get(0, module));
                        tmp.add(ConstantInt.get(i, module));
                        tmp.add(ConstantInt.get(j, module));
                        GetElementPtrInstr gg = builder.createGep(Local_IntArrayAlloca, tmp);
                        StoreInstr ss = builder.createStore(ConstantInt.get(constDefNode.initVals.get(i*constDefNode.dimention.get(1)+j), module), gg);
                    }
                }
            }
            else {
                AllocaInstr Local_IntAlloca = builder.createAlloca(TyInt32); //为变量分配空间
                scope.push(constDefNode.nameOfIdent, Local_IntAlloca);
                //数组赋值，待定
                StoreInstr ss = builder.createStore(ConstantInt.get(constDefNode.initVals.get(0), module), Local_IntAlloca);
            }
        }
        else {
            if (constDefNode.dimention.size() == 1) {
                if (constDefNode.initVals.size() == 0) {
                    ArrayType arrayType = ArrayType.get(TyInt32, constDefNode.dimention.get(0));
                    ConstantZero initializer = ConstantZero.get(arrayType, module);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(constDefNode.nameOfIdent, module, arrayType, true, initializer); //为数组分配空间

                    scope.push(constDefNode.nameOfIdent, Globle_IntArrayAlloca);
                } else {
                    ConstantArray cc = loadInitsForArray(constDefNode.initVals);
                    ArrayType arrayType = ArrayType.get(TyInt32, constDefNode.dimention.get(0));
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(constDefNode.nameOfIdent, module, arrayType, true, cc); //为数组分配空间
                    //System.out.println(Globle_IntArrayAlloca.print());
                    scope.push(constDefNode.nameOfIdent, Globle_IntArrayAlloca);
                }
            } else if (constDefNode.dimention.size() == 2) {
                if (constDefNode.initVals.size() == 0) {
                    ArrayType arrayType1 = ArrayType.get(TyInt32, constDefNode.dimention.get(1));
                    ArrayType arrayType2 = ArrayType.get(arrayType1, constDefNode.dimention.get(0));
                    ConstantZero initializer = ConstantZero.get(arrayType2, module);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(constDefNode.nameOfIdent, module, arrayType2, true, initializer); //为数组分配空间
                    scope.push(constDefNode.nameOfIdent, Globle_IntArrayAlloca);
                } else {
                    ArrayType arrayType1 = ArrayType.get(TyInt32, constDefNode.dimention.get(1));
                    ArrayType arrayType2 = ArrayType.get(arrayType1, constDefNode.dimention.get(0));
                    ConstantArray cc = loadInitsForArray(constDefNode.initVals, constDefNode.dimention);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(constDefNode.nameOfIdent, module, arrayType2, true, cc); //为数组分配空间
                    //System.out.println(Globle_IntArrayAlloca.print());
                    scope.push(constDefNode.nameOfIdent, Globle_IntArrayAlloca);
                }
            } else {
                int temp = constDefNode.initVals.size() == 0 ? 0 : constDefNode.initVals.get(0);
                ConstantInt initializer = ConstantInt.get(temp, module);
                GlobalVariable Globle_IntAlloca = GlobalVariable.create(constDefNode.nameOfIdent, module, TyInt32, true, initializer); //为变量分配空间
                //System.out.println(Globle_IntAlloca.print());
                scope.push(constDefNode.nameOfIdent, Globle_IntAlloca);
            }
        }
    }

    @Override
    public void visit(ConstExpNode constExpNode) {

    }

    @Override
    public void visit(ContinueStmtNode continueStmtNode) {
        if (return_flag > 0) return;
        builder.createBr(continueOutOfForLoop.peek());
    }

    @Override
    public void visit(DeclNode declNode) {

    }

    @Override
    public void visit(EqExpNode eqExpNode) {
        if(return_flag > 0) return;
        int y = addFlag++;
        Value tmp1;
        Value tmp2;
        Value icmp;
        if (eqExpNode.op == null) {
            if (y == 0) {
                RelExpNode rr = ((RelExpNode)eqExpNode.lvar);
                if (rr.op == null) {
                    AddExpNode aa = ((AddExpNode)rr.lvar);
                    aa.isPrintNeZero = true;
                }
                rr.accept(this);
            } else {
                ((RelExpNode)eqExpNode.lvar).accept(this);
            }
        } else {
            ((EqExpNode)eqExpNode.lvar).accept(this);
            tmp1 = ret;
            ((RelExpNode)eqExpNode.rvar).accept(this);
            tmp2 = ret;
            if (((IntegerType)tmp1.getIrType()).getNumBits() != ((IntegerType)tmp2.getIrType()).getNumBits()) {
                if (((IntegerType)tmp1.getIrType()).isI1()) {
                    tmp1 = builder.createZext(tmp1, INT32_T);
                }
                if (((IntegerType)tmp2.getIrType()).isI1()) {
                    tmp2 = builder.createZext(tmp2, INT32_T);
                }
            }
            if (eqExpNode.op == SyntaxType.EQL) {
                icmp = builder.createIcmpEq(tmp1, tmp2);
            } else {
                icmp = builder.createIcmpNe(tmp1, tmp2);
            }
            ret = icmp;
        }
    }

    @Override
    public void visit(ForLoopStmtNode forLoopStmtNode) {
        if(return_flag > 0) return;
        Function currentFunc = builder.getInsertBlock().getParent();
        if (forLoopStmtNode.forstmt1 != null) {
            ((ForStmtNode)forLoopStmtNode.forstmt1).accept(this);
        }
        if (forLoopStmtNode.cond != null && forLoopStmtNode.forstmt2 != null) {
            BasicBlock loopJudge = BasicBlock.create(module, "", currentFunc);
            BasicBlock loopBody = BasicBlock.create(module, "", currentFunc);
            BasicBlock change = BasicBlock.create(module, "", currentFunc);
            BasicBlock out = BasicBlock.create(module, "", currentFunc);
            breakOutOfForLoop.push(out);
            continueOutOfForLoop.push(change);
            builder.createBr(loopJudge);

            builder.setInsertPoint(loopJudge);
            CondNode cc = (CondNode)forLoopStmtNode.cond;
            cc.setTrueBlock(loopBody);
            cc.setFalseBlock(out);
            cc.accept(this);

            builder.setInsertPoint(loopBody);
            ((StmtNode)forLoopStmtNode.stmt).accept(this);
            builder.createBr(change);
            builder.setInsertPoint(change);
            ((ForStmtNode)forLoopStmtNode.forstmt2).accept(this);
            builder.createBr(loopJudge);

            builder.setInsertPoint(out);

        } else if (forLoopStmtNode.cond != null) {
            BasicBlock loopJudge = BasicBlock.create(module, "", currentFunc);
            BasicBlock loopBody = BasicBlock.create(module, "", currentFunc);
            BasicBlock out = BasicBlock.create(module, "", currentFunc);

            breakOutOfForLoop.push(out);
            continueOutOfForLoop.push(loopJudge);
            builder.createBr(loopJudge);

            builder.setInsertPoint(loopJudge);
            CondNode cc = (CondNode)forLoopStmtNode.cond;
            cc.setTrueBlock(loopBody);
            cc.setFalseBlock(out);
            cc.accept(this);

            builder.setInsertPoint(loopBody);
            ((StmtNode)forLoopStmtNode.stmt).accept(this);
            builder.createBr(loopJudge);

            builder.setInsertPoint(out);

        } else if (forLoopStmtNode.forstmt2 != null) {
            BasicBlock loopBody = BasicBlock.create(module, "", currentFunc);
            BasicBlock change = BasicBlock.create(module, "", currentFunc);
            BasicBlock out = BasicBlock.create(module, "", currentFunc);

            breakOutOfForLoop.push(out);
            continueOutOfForLoop.push(change);
            builder.createBr(loopBody);

            builder.setInsertPoint(loopBody);
            ((StmtNode)forLoopStmtNode.stmt).accept(this);
            builder.createBr(change);

            builder.setInsertPoint(change);
            ((ForStmtNode)forLoopStmtNode.forstmt2).accept(this);
            builder.createBr(loopBody);

            builder.setInsertPoint(out);

        } else {
            BasicBlock loopBody = BasicBlock.create(module, "", currentFunc);
            BasicBlock out = BasicBlock.create(module, "", currentFunc);
            breakOutOfForLoop.push(out);
            continueOutOfForLoop.push(loopBody);
            builder.createBr(loopBody);
            builder.setInsertPoint(loopBody);
            ((StmtNode)forLoopStmtNode.stmt).accept(this);
            builder.createBr(loopBody);

            builder.setInsertPoint(out);
        }
        breakOutOfForLoop.pop();
        continueOutOfForLoop.pop();
        return_flag = 0;
    }

    @Override
    public void visit(ForStmtNode forStmtNode) {
        if(return_flag > 0) return;
        isRequireLval = true;
        this.visit((LValNode)forStmtNode.lval);
        Value var = ret;
        ((ExpNode)forStmtNode.exp).accept(this);
        builder.createStore(ret, var);
    }

    @Override
    public void visit(FuncDefNode funcDefNode) {
        scope.enter(); //进入函数的作用域
        IrType TYPE32 = IrType.getInt32Type(module);
        IrType TYPEV = IrType.getVoidType(module);
        IrType TYPEARRAY_32 = PointerType.getInt32PtrType(module);
        IrType funType;

        //判断新声明的function的返回值类型
        if(funcDefNode.isRetVoid) {
            funType = TYPEV;
        } else {
            funType = TYPE32;
        }

        if (funcDefNode.params != null) {
            for (Node param : funcDefNode.params.getChildren()) {
                if (param.getType() == SyntaxType.FUNC_F_PARAM) {
                    ((FuncFParamNode)param).accept(this); //得到参数类型
                }
            }
            ArrayList<IrType> tmp = new ArrayList<>(Ints);
            Function fun = Function.create(FunctionType.get(funType, tmp), funcDefNode.funcName, module); //由函数类型定义函数
            BasicBlock bb = BasicBlock.create(module, "entry", fun); // BB的名字在生成中无所谓,但是可以方便阅读
            builder.setInsertPoint(bb);

            scope.exit(); //先退出当前作用域
            scope.push(funcDefNode.funcName, fun); //函数名放进作用域
            scope.enter(); //再次进入函数的作用域

            for (Node param : funcDefNode.params.getChildren()) {
                if (param.getType() == SyntaxType.FUNC_F_PARAM) {
                    if (((FuncFParamNode) param).dim == 0) {
                        AllocaInstr pAlloca = builder.createAlloca(TYPE32);
                        //System.out.println(pAlloca.print());
                        scope.push(((FuncFParamNode)param).name, pAlloca);
                    } else if (((FuncFParamNode) param).dim == 1) {
                        AllocaInstr pAlloca = builder.createAlloca(TYPEARRAY_32); //在内存中分配空间
                        scope.push(((FuncFParamNode)param).name, pAlloca);
                        //System.out.println(pAlloca.print());
                    } else {
                        IrType DUAL_INT_32 = PointerType.getPointerType(ArrayType.get(TYPE32, ((FuncFParamNode) param).len));
                        AllocaInstr pAlloca = builder.createAlloca(DUAL_INT_32); //在内存中分配空间
                        scope.push(((FuncFParamNode)param).name, pAlloca);
                        //System.out.println(pAlloca.print());
                    }
                }
            }
            // * 号运算符是从迭代器中取出迭代器当前指向的元素
            ArrayList<Value> args = new ArrayList<>(fun.getArguments());
            int i = 0;
            for (Node param : funcDefNode.params.getChildren()) {
                if (param.getType() == SyntaxType.FUNC_F_PARAM) {
                    Value pAlloca = scope.find(((FuncFParamNode)param).name);
                    if (pAlloca == null)
                        exit(0);
                    else {
                        StoreInstr ss = builder.createStore(args.get(i), pAlloca);
                        //System.out.println(ss.print());
                        i++;
                    }
                    Ints.remove(Ints.size()-1); //清空向量
                }
            }
        }
        else {
            Function fun = Function.create(FunctionType.get(funType, Ints), funcDefNode.funcName, module); //由函数类型定义函数
            BasicBlock bb = BasicBlock.create(module, "entry", fun); // BB的名字在生成中无所谓,但是可以方便阅读
            builder.setInsertPoint(bb);
            scope.exit(); //先退出当前作用域
            scope.push(funcDefNode.funcName, fun); //函数名放进作用域
            scope.enter(); //再次进入函数的作用域
        }

        ((BlockNode)funcDefNode.block).acccept(this); //执行compound-stmt
        if(return_flag == 0) {
            IrType return_type = builder.getInsertBlock().getParent().getReturnType();
            if(return_type.isVoidType())
                builder.createVoidRet();
            else if(return_type.isIntegerType())
                builder.createRet(ConstantInt.get(0, module));
        }
        return_flag = 0;
        scope.exit();
    }

    @Override
    public void visit(FuncFParamNode funcFParamNode) {
        if (return_flag > 0) return;
        IrType TYPE32 = IrType.getInt32Type(module);
        IrType TYPEARRAY_INT_32 = PointerType.getInt32PtrType(module);
        //返回参数类型并分配空间
        if (funcFParamNode.dim == 1) {
            Ints.add(TYPEARRAY_INT_32);
        } else if (funcFParamNode.dim == 0) {
            Ints.add(TYPE32);
        } else {
           IrType DUAL_INT_32 = PointerType.getPointerType(ArrayType.get(TYPE32, funcFParamNode.len));
           secondDim = DUAL_INT_32;
           Ints.add(DUAL_INT_32);
        }
    }

    @Override
    public void visit(IfStmtNode ifStmtNode) {
        if(return_flag > 0) return;
        CondNode cond = ((CondNode)ifStmtNode.cond);
        Function currentFunc = builder.getInsertBlock().getParent();
        BasicBlock trueBB = BasicBlock.create(module, "", currentFunc);
        BasicBlock falseBB;
        BasicBlock nextBB = null;
        int insertedflag = 0;
        if (ifStmtNode.else_stmt != null) //有else
        {
            falseBB = BasicBlock.create(module, "", currentFunc);
            cond.setFalseBlock(falseBB);
            cond.setTrueBlock(trueBB);
            cond.accept(this);
            builder.setInsertPoint(falseBB);
            ((StmtNode)ifStmtNode.else_stmt).accept(this);
            if (builder.getInsertBlock().get_terminator() == null) {
                insertedflag = 1;
                nextBB = BasicBlock.create(module, "", currentFunc);
                builder.createBr(nextBB);
            }
            return_flag = 0;
            //tureBB
            builder.setInsertPoint(trueBB);
            ((StmtNode)ifStmtNode.if_stmt).accept(this);
            if (builder.getInsertBlock().get_terminator() == null) {
                if (insertedflag == 0) {
                    insertedflag = 1;
                    nextBB = BasicBlock.create(module, "", currentFunc);
                }
                builder.createBr(nextBB);
            }
            return_flag = insertedflag == 0 ? 1 : 0;
            if (insertedflag == 1) {
                builder.setInsertPoint(nextBB);
            }
        }
        else {
            nextBB = BasicBlock.create(module, "", currentFunc);
            cond.setFalseBlock(nextBB);
            cond.setTrueBlock(trueBB);
            cond.accept(this);
            builder.setInsertPoint(trueBB);
            ((StmtNode)ifStmtNode.if_stmt).accept(this);
            if (return_flag == 0 && builder.getInsertBlock().get_terminator() == null) {
                builder.createBr(nextBB);
            }
            return_flag = 0;
            builder.setInsertPoint(nextBB);
        }
    }

    @Override
    public void visit(InitValNode initValNode){

    }

    @Override
    public void visit(LAndExpNode lAndExpNode) {
        if(return_flag > 0) return;
        if (lAndExpNode.op == null) {
            addFlag = 0;
            EqExpNode ee = ((EqExpNode)lAndExpNode.lvar);
            ee.setTrueBlock(lAndExpNode.getTrueBlock());
            ee.setFalseBlock(lAndExpNode.getFalseBlock());
            ee.accept(this);
            builder.createCondBr(ret, ee.getTrueBlock(), ee.getFalseBlock());
        } else {
            LAndExpNode la = ((LAndExpNode)lAndExpNode.lvar);
            EqExpNode ee = ((EqExpNode)lAndExpNode.rvar);
            Function currentFunc = builder.getInsertBlock().getParent();
            BasicBlock bb = BasicBlock.create(module, "", currentFunc);
            la.setTrueBlock(bb);
            la.setFalseBlock(lAndExpNode.getFalseBlock());
            la.accept(this);
            return_flag = 0;
            builder.setInsertPoint(bb);
            ee.setFalseBlock(lAndExpNode.getFalseBlock());
            ee.setTrueBlock(lAndExpNode.getTrueBlock());
            addFlag = 0;
            ee.accept(this);
            builder.createCondBr(ret, ee.getTrueBlock(), ee.getFalseBlock());
        }
    }

    @Override
    public void visit(LOrExpNode lOrExpNode) {
        if(return_flag > 0) return;
        if (lOrExpNode.op == null) {
            LAndExpNode la = (LAndExpNode)lOrExpNode.lvar;
            la.setFalseBlock(lOrExpNode.getFalseBlock());
            la.setTrueBlock(lOrExpNode.getTrueBlock());
            la.accept(this);
        } else {
            LOrExpNode lo = (LOrExpNode)lOrExpNode.lvar;
            LAndExpNode la = (LAndExpNode)lOrExpNode.rvar;
            Function currentFunc =  builder.getInsertBlock().getParent();
            BasicBlock bb = BasicBlock.create(module, "", currentFunc);
            lo.setTrueBlock(lOrExpNode.getTrueBlock());
            lo.setFalseBlock(bb);
            lo.accept(this);
            la.setTrueBlock(lOrExpNode.getTrueBlock());
            la.setFalseBlock(lOrExpNode.getFalseBlock());
            return_flag = 0;
            builder.setInsertPoint(bb);
            la.accept(this);
        }
    }

    @Override
    public void visit(LValNode lValNode) {
        if(return_flag > 0) return;
        boolean should_return_lvalue = isRequireLval;
        isRequireLval = false;
        Value var = scope.find(lValNode.name);
        boolean is_int = var.getIrType().getPointerElementType().isIntegerType();
        boolean is_ptr = var.getIrType().getPointerElementType().isPointerType();
        boolean is_Array = var.getIrType().getPointerElementType().isArrayType();
        IrType hhh = var.getIrType().getPointerElementType();
        if (var != null) {
            if (lValNode.offset.size() != 0) {
                if (lValNode.offset.size() == 1) {
                    ((ExpNode)lValNode.offset.get(0)).accept(this);
                    Value num = ret;
                    if (is_Array) {
                        if (((ArrayType)hhh).getDim() == 1) {
                            ArrayList<Value> tmp = new ArrayList<>();
                            tmp.add(ConstantInt.get(0, module));
                            tmp.add(num);
                            var = builder.createGep(var, tmp);
                            if (should_return_lvalue) {
                                ret = var;
                                isRequireLval = false;
                            } else {
                                ret = builder.createLoad(var);
                            }
                        } else {
                            ArrayList<Value> tmp = new ArrayList<>();
                            tmp.add(ConstantInt.get(0, module));
                            tmp.add(num);
                            tmp.add(ConstantInt.get(0, module));
                            var = builder.createGep(var, tmp);
                            ret = var;
                        }
                    } else if (is_ptr) {
                        LoadInstr var_load = builder.createLoad(var);
                        ArrayList<Value> tmp = new ArrayList<>();
                        if (!hhh.getPointerElementType().isArrayType()) {
                            tmp.add(num);
                            var = builder.createGep(var_load, tmp);
                            if (should_return_lvalue) {
                                ret = var;
                                isRequireLval = false;
                            } else {
                                ret = builder.createLoad(var);
                            }
                        } else {
                            tmp.add(num);
                            tmp.add(ConstantInt.get(0, module));
                            var = builder.createGep(var_load, tmp);
                            ret = var;
                        }
                    }
                } else {
                    ((ExpNode)lValNode.offset.get(0)).accept(this);
                    Value num1 = ret;
                    ((ExpNode)lValNode.offset.get(1)).accept(this);
                    Value num2 = ret;
                    if (is_Array) {
                        ArrayList<Value> tmp = new ArrayList<>();
                        tmp.add(ConstantInt.get(0, module));
                        tmp.add(num1);
                        tmp.add(num2);
                        var = builder.createGep(var, tmp);
                    } else if (is_ptr) {
                        LoadInstr var_load = builder.createLoad(var);
                        ArrayList<Value> tmp = new ArrayList<>();
                        tmp.add(num1);
                        tmp.add(num2);
                        var = builder.createGep(var_load, tmp);
                    }
                    if (should_return_lvalue) {
                        ret = var;
                        isRequireLval = false;
                    } else {
                        ret = builder.createLoad(var);
                    }
                }
            } else {
                if (should_return_lvalue) {
                    ret = var;
                    isRequireLval = false;
                } else {
                    if (is_int || is_ptr) {
                        ret = builder.createLoad(var);
                    } else if (is_Array) {
                        int dim = ((ArrayType)var.getIrType().getPointerElementType()).getDim();
                        if (dim == 1) {
                            ArrayList<Value> tmp = new ArrayList<>();
                            tmp.add(ConstantInt.get(0, module));
                            tmp.add(ConstantInt.get(0, module));
                            ret = builder.createGep(var, tmp);
                        } else {
                            ArrayList<Value> tmp = new ArrayList<>();
                            tmp.add(ConstantInt.get(0, module));
                            tmp.add(ConstantInt.get(0, module));
                            ret = builder.createGep(var, tmp);
                        }
                    } else {
                        System.out.println("lval error");
                    }
                }
            }
        }
        else {
            System.out.println("cannot find the var\n");
        }
    }

    @Override
    public void visit(MainFuncDefNode mainFuncDefNode) {
        scope.enter(); //进入函数的作用域
        IrType TYPE32 = IrType.getInt32Type(module);
        IrType funType;
        funType = TYPE32;
        Function fun = Function.create(FunctionType.get(funType, Ints), "main", module); //由函数类型定义函数
        BasicBlock bb = BasicBlock.create(module, "entry", fun); // BB的名字在生成中无所谓,但是可以方便阅读
        builder.setInsertPoint(bb);
        scope.exit(); //先退出当前作用域
        scope.push("main", fun); //函数名放进作用域
        scope.enter(); //再次进入函数的作用域
        ((BlockNode)mainFuncDefNode.block).acccept(this); //执行compound-stmt
        if(return_flag == 0) {
            IrType return_type = builder.getInsertBlock().getParent().getReturnType();
            if(return_type.isVoidType())
                builder.createVoidRet();
            else if(return_type.isIntegerType())
                builder.createRet(ConstantInt.get(0, module));
        }
        return_flag = 0;
        scope.exit();
    }

    @Override
    public void visit(MulExpNode mulExpNode) {
        if(return_flag > 0) return;
        Value AdditiveExpression;
        Value Term;
        Value icmp;
        if (mulExpNode.op == null) {
            ((UnaryExpNode)mulExpNode.lvar).accept(this);
        } else {
            ((MulExpNode)mulExpNode.lvar).accept(this);
            AdditiveExpression = ret;
            ((UnaryExpNode)mulExpNode.rvar).accept(this);
            Term = ret;
            if (((IntegerType)AdditiveExpression.getIrType()).getNumBits() != ((IntegerType)Term.getIrType()).getNumBits()) {
                if (((IntegerType)AdditiveExpression.getIrType()).isI1()) {
                    AdditiveExpression = builder.createZext(AdditiveExpression, INT32_T);
                }
                if (((IntegerType)Term.getIrType()).isI1()) {
                    Term = builder.createZext(Term, INT32_T);
                }
            }
            if (mulExpNode.op == SyntaxType.MULT) {
                icmp = builder.createImul(AdditiveExpression, Term);
            } else if (mulExpNode.op == SyntaxType.DIV){
                icmp = builder.createIsdiv(AdditiveExpression, Term);
            } else {
                Value tmp1 = builder.createIsdiv(AdditiveExpression, Term);
                Value tmp2 = builder.createImul(Term, tmp1);
                icmp = builder.createIsub(AdditiveExpression, tmp2);
            }
            ret = icmp;
        }
    }

    @Override
    public void visit(NumberNode numberNode) {
        if(return_flag > 0) return;
        ret = ConstantInt.get(numberNode.truth, module);
    }

    @Override
    public void visit(PrimaryExpNode primaryExpNode) {

    }

    @Override
    public void visit(PrintfStmtNode printfStmtNode) {
        if (return_flag > 0) return;
        int cnt = 0;
        Value putint = scope.find("putint");
        Value putch = scope.find("putch");
        for (int i = 0; i < printfStmtNode.constr.length(); i++) {
            if (printfStmtNode.constr.charAt(i) == '%') {
                i++;
                ArrayList<Value> args = new ArrayList<>();
                ((ExpNode)printfStmtNode.exp.get(cnt)).accept(this);
                args.add(ret);
                builder.createCall(putint, args);
                cnt++;
            } else if (printfStmtNode.constr.charAt(i) == '\\' && printfStmtNode.constr.charAt(i + 1) == 'n') {
                i++;
                ArrayList<Value> args = new ArrayList<>();
                args.add(ConstantInt.get(10, module));
                builder.createCall(putch, args);
            } else {
                ArrayList<Value> args = new ArrayList<>();
                args.add(ConstantInt.get((printfStmtNode.constr.charAt(i)), module));
                builder.createCall(putch, args);
            }
        }
    }

    @Override
    public void visit(RelExpNode relExpNode) {
        if(return_flag > 0) return;
        Value tmp1;
        Value tmp2;
        Value icmp;
        if (relExpNode.op == null) {
            ((AddExpNode)relExpNode.lvar).accept(this);
        } else {
            ((RelExpNode)relExpNode.lvar).accept(this);
            tmp1 = ret;
            ((AddExpNode)relExpNode.rvar).accept(this);
            tmp2 = ret;
            if (((IntegerType)tmp1.getIrType()).getNumBits() != ((IntegerType)tmp2.getIrType()).getNumBits()) {
                if (((IntegerType)tmp1.getIrType()).isI1()) {
                    tmp1 = builder.createZext(tmp1, INT32_T);
                }
                if (((IntegerType)tmp2.getIrType()).isI1()) {
                    tmp2 = builder.createZext(tmp2, INT32_T);
                }
            }
            if (relExpNode.op == SyntaxType.GEQ) {
                icmp = builder.createIcmpGe(tmp1, tmp2);
            } else if (relExpNode.op == SyntaxType.GRE) {
                icmp = builder.createIcmpGt(tmp1, tmp2);
            } else if (relExpNode.op == SyntaxType.LEQ) {
                icmp = builder.createIcmpLe(tmp1, tmp2);
            } else {
                icmp = builder.createIcmpLt(tmp1, tmp2);
            }
            ret = icmp;
        }
    }

    @Override
    public void visit(ReturnStmtNode returnStmtNode) {
        if(return_flag > 0) return;
        IrType return_type = builder.getInsertBlock().getParent().getReturnType();
        if (returnStmtNode.exp == null) //空指针
        {
            if (!return_type.isVoidType())
                System.out.println("return_type is not void, but expression is empty\n");
            builder.createVoidRet();
        }
        else {
            ((ExpNode)returnStmtNode.exp).accept(this);
            if (return_type.isVoidType()) //void型
            {
                System.out.println("return_type is void, but expression is not empty\n");
                builder.createVoidRet();
                return;
            }
            builder.createRet(ret);
        }
        return_flag = 1;
    }

    @Override
    public void visit(StmtNode stmtNode) {
        if(return_flag > 0) return;
        isRequireLval = true;
        this.visit((LValNode)stmtNode.lval);
        Value var = ret;
        if (stmtNode.exp != null) {
            ((ExpNode)stmtNode.exp).accept(this);
            builder.createStore(ret, var);
        } else {
            Value value = scope.find("getint");
            ArrayList<Value> func = new ArrayList<>();
            ret = builder.createCall(value, func);
            builder.createStore(ret, var);
        }
    }

    @Override
    public void visit(UnaryExpNode unaryExpNode) {
        if (return_flag > 0) return;
        if (unaryExpNode.getChildren().get(0).getType() == SyntaxType.UNARY_OP) {
            SyntaxType operand = ((UnaryOpNode)unaryExpNode.getChildren().get(0)).getContent();
            this.visit(((UnaryExpNode)unaryExpNode.getChildren().get(1)));
            if (operand == SyntaxType.MINU) {
                Value tmp = ret;
                if (((IntegerType)tmp.getIrType()).getNumBits() != 32) {
                    tmp = builder.createZext(tmp, INT32_T);
                }
                ret = builder.createIsub(ConstantInt.get(0, module), tmp);
            } else if (operand == SyntaxType.NOT) {
                Value tmp = ret;
                if (((IntegerType)tmp.getIrType()).getNumBits() != 32) {
                    tmp = builder.createZext(tmp, INT32_T);
                }
                ret = builder.createIcmpEq(tmp, ConstantInt.get(0, module));
            }
        } else if (unaryExpNode.getChildren().get(0).getType() == SyntaxType.PRIMARY_EXP){
                //this.visit(((PrimaryExpNode)unaryExpNode.getChildren().get(0)));
                ((PrimaryExpNode)unaryExpNode.getChildren().get(0)).accept(this);
        } else {
            Value value;
            value = scope.find(unaryExpNode.name); //调用scope.find()找ID对应的值
            if (value == null) {
                System.out.println("cannot find the fun\n");
                return;
            }
            IrType fun = value.getIrType();
            if (!fun.isFunctionType()) return;
            FunctionType callfun = ((FunctionType)fun);
            Value value_args;
            int i = 0;
            ArrayList<Value> function = new ArrayList<>();
            for (Node Args : unaryExpNode.args) {
                i++;
                ((ExpNode)Args).accept(this);
                value_args = ret;
                function.add(value_args);
            }
            if (i != callfun.getNumOfArgs())
            {
                System.out.println("\t the num of arg error\n");
                return;
            }
            ret = builder.createCall(value, function);
        }
    }

    @Override
    public void visit(VarDeclNode varDeclNode) {
        if (return_flag > 0) return;
        for (Node child : varDeclNode.getChildren()) {
            if (child.getType() == SyntaxType.VAR_DEF) {
                this.visit((VarDefNode) child);
            }
        }
    }

    @Override
    public void visit(VarDefNode varDefNode) {
        if (return_flag > 0) return;
        IrType TyInt32 = IrType.getInt32Type(module);
        if (!scope.inGlobal()) {
            if (varDefNode.dimention.size() == 1) { //数组
                ArrayType arrayType = ArrayType.get(TyInt32, varDefNode.dimention.get(0));
                AllocaInstr Local_IntArrayAlloca = builder.createAlloca(arrayType); //为数组分配空间
                scope.push(varDefNode.nameOfIdent, Local_IntArrayAlloca);
                //数组赋值，待定
                for (int i= 0; i < varDefNode.initVal.size(); i++) {
                    ArrayList<Value> tmp = new ArrayList<>();
                    tmp.add(ConstantInt.get(0, module));
                    tmp.add(ConstantInt.get(i, module));
                    GetElementPtrInstr gg = builder.createGep(Local_IntArrayAlloca, tmp);
                    ((ExpNode)varDefNode.initVal.get(i)).accept(this);
                    StoreInstr ss = builder.createStore(ret, gg);
                }

            } else if(varDefNode.dimention.size() == 2) {
                ArrayType arrayType1 = ArrayType.get(TyInt32, varDefNode.dimention.get(1));
                ArrayType arrayType2 = ArrayType.get(arrayType1, varDefNode.dimention.get(0));
                AllocaInstr Local_IntArrayAlloca = builder.createAlloca(arrayType2);
                scope.push(varDefNode.nameOfIdent, Local_IntArrayAlloca);
                if (varDefNode.initVal.size() > 0) {
                    for (int i = 0 ; i < varDefNode.dimention.get(0); i++) {
                        for (int j = 0; j < varDefNode.dimention.get(1); j++) {
                            ArrayList<Value> tmp = new ArrayList<>();
                            tmp.add(ConstantInt.get(0, module));
                            tmp.add(ConstantInt.get(i, module));
                            tmp.add(ConstantInt.get(j, module));
                            GetElementPtrInstr gg = builder.createGep(Local_IntArrayAlloca, tmp);
                            ((ExpNode)varDefNode.initVal.get(i*varDefNode.dimention.get(1)+j)).accept(this);
                            StoreInstr ss = builder.createStore(ret, gg);
                        }
                    }
                }

            }
            else {
                AllocaInstr Local_IntAlloca = builder.createAlloca(TyInt32); //为变量分配空间
                scope.push(varDefNode.nameOfIdent, Local_IntAlloca);
                if (varDefNode.initVal.size() > 0) {
                    ((ExpNode)varDefNode.initVal.get(0)).accept(this);
                    //数组赋值，待定
                    StoreInstr ss = builder.createStore(ret, Local_IntAlloca);
                }
            }
        }
        else {
            if (varDefNode.dimention.size() == 1) {
                if (varDefNode.nums.size() == 0) {
                    ArrayType arrayType = ArrayType.get(TyInt32, varDefNode.dimention.get(0));
                    ConstantZero initializer = ConstantZero.get(arrayType, module);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(varDefNode.nameOfIdent, module, arrayType, false, initializer); //为数组分配空间

                    scope.push(varDefNode.nameOfIdent, Globle_IntArrayAlloca);
                } else {
                    ConstantArray cc = loadInitsForArray(varDefNode.nums);
                    ArrayType arrayType = ArrayType.get(TyInt32, varDefNode.dimention.get(0));
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(varDefNode.nameOfIdent, module, arrayType, false, cc); //为数组分配空间
                    //System.out.println(Globle_IntArrayAlloca.print());
                    scope.push(varDefNode.nameOfIdent, Globle_IntArrayAlloca);
                }
            } else if (varDefNode.dimention.size() == 2) {
                if (varDefNode.nums.size() == 0) {
                    ArrayType arrayType1 = ArrayType.get(TyInt32, varDefNode.dimention.get(1));
                    ArrayType arrayType2 = ArrayType.get(arrayType1, varDefNode.dimention.get(0));
                    ConstantZero initializer = ConstantZero.get(arrayType2, module);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(varDefNode.nameOfIdent, module, arrayType2, false, initializer); //为数组分配空间
                    scope.push(varDefNode.nameOfIdent, Globle_IntArrayAlloca);
                } else {
                    ArrayType arrayType1 = ArrayType.get(TyInt32, varDefNode.dimention.get(1));
                    ArrayType arrayType2 = ArrayType.get(arrayType1, varDefNode.dimention.get(0));
                    ConstantArray cc = loadInitsForArray(varDefNode.nums, varDefNode.dimention);
                    GlobalVariable Globle_IntArrayAlloca = GlobalVariable.create(varDefNode.nameOfIdent, module, arrayType2, false, cc); //为数组分配空间
                    //System.out.println(Globle_IntArrayAlloca.print());
                    scope.push(varDefNode.nameOfIdent, Globle_IntArrayAlloca);
                }
            } else {
                int temp = varDefNode.nums.size() == 0 ? 0 : varDefNode.nums.get(0);
                ConstantInt initializer = ConstantInt.get(temp, module);
                GlobalVariable Globle_IntAlloca = GlobalVariable.create(varDefNode.nameOfIdent, module, TyInt32, false, initializer); //为变量分配空间
                //System.out.println(Globle_IntAlloca.print());
                scope.push(varDefNode.nameOfIdent, Globle_IntAlloca);
            }
        }
    }
}
