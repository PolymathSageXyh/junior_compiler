import lexer.Lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Compiler {
    public static void main(String[] args) {
        String inFilePath = "src/testfile.txt"; // 文件路径
        String outFilePath = "src/output.txt";
        try {
            //System.out.println(Files.exists(Paths.get(filePath)));
            String fileContent = Files.readString(Path.of(inFilePath));

            // 使用UTF-8字符集读取文件内容，你可以根据文件的实际编码更改Charset
            //Charset charset = Charset.forName("UTF-8");
            //ath path = Paths.get(filePath);
            //String fileContent = new String(Files.readAllBytes(path), charset);
            Lexer lexer = new Lexer(fileContent);
            lexer.run();
            // 输出文件内容
            Files.write(Path.of(outFilePath), lexer.display().getBytes(), StandardOpenOption.CREATE);
            //System.out.print(lexer.display());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
