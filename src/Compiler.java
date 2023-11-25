import error.ErrorCheckContext;
import error.ErrorCheckReturn;
import error.ErrorType;
import lexer.Lexer;
import lightllr.Module;
import lightllr.optimization.*;
import mips.MipsParser;
import mips.RegAllocator;
import paser.Mypair;
import paser.nodes.CompUnitNode;
import paser.nodes.Node;
import paser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String inFilePath = "src/testfile.txt"; // 文件路径
        String errorFilePath = "src/error.txt";
        String outFilePath = "src/llvm_ir.txt";
        String finalFilePath = "src/mips.txt";
        try {
            //System.out.println(Files.exists(Paths.get(filePath)));
            String fileContent = Files.readString(Path.of(inFilePath));

            // 使用UTF-8字符集读取文件内容，你可以根据文件的实际编码更改Charset
            //Charset charset = Charset.forName("UTF-8");
            //ath path = Paths.get(filePath);
            //String fileContent = new String(Files.readAllBytes(path), charset);
            Lexer lexer = new Lexer(fileContent);
            lexer.run();
            Parser parser = new Parser(lexer.getTokens());
            Node root = parser.parseAll();

            //Files.write(Path.of(outFilePath), root.getPaserLog().toString().getBytes(), StandardOpenOption.CREATE);
            ArrayList<Mypair<ErrorType, Integer>> errorList = new ArrayList<>();
            root.checkError(errorList, new ErrorCheckContext(), new ErrorCheckReturn());
            if (!errorList.isEmpty()) {
                StringBuilder ss = new StringBuilder();
                for(Mypair<ErrorType,Integer> i:errorList){
                    ss.append(i.second).append(" ").append(ErrorType.error2type(i.first)).append("\n");
                }
                Files.write(Path.of(errorFilePath), ss.toString().getBytes(), StandardOpenOption.CREATE);
                return;
            }
            SysBuilder sysBuilder = new SysBuilder();
            sysBuilder.visit((CompUnitNode) root);

            sysBuilder.getModule().setPrintName();
            //String res = sysBuilder.getModule().print();
            PassManager pp = new PassManager(sysBuilder.getModule());
            pp.addPass(new ReplacePutch(sysBuilder.getModule()), false);
            pp.addPass(new Mem2Reg(sysBuilder.getModule()), false);
            pp.addPass(new SideEffect(sysBuilder.getModule()), false);
            pp.addPass(new KillDeadCode(sysBuilder.getModule()), false);
            pp.addPass(new ConstPropagation(sysBuilder.getModule()), false);
            pp.addPass(new LoopInvHoist(sysBuilder.getModule()), false);
            pp.addPass(new ActiveVars(sysBuilder.getModule()), false);
            pp.addPass(new RegAllocator(sysBuilder.getModule()), false);
            //pp.addPass(new RemovePhi(sysBuilder.getModule()), false);

            pp.run();
            //MipsParser mipsParser = new MipsParser(sysBuilder.getModule());
            String res = sysBuilder.getModule().print();
            //mipsParser.parseModule();
            //String finalres = mipsParser.toString();

            //System.out.println(sysBuilder.getModule().print());

            Files.write(Path.of(outFilePath), res.getBytes(), StandardOpenOption.CREATE);
            //Files.write(Path.of(finalFilePath), finalres.getBytes(), StandardOpenOption.CREATE);
            //System.out.print(lexer.display());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
