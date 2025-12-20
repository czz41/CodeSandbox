import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
public class Main{
    public static void main(String[] args) {
        Integer a = Integer.parseInt(args[0]);
        Integer b = Integer.parseInt(args[1]);
        // 1. 定义文件路径（当前目录下的test1.txt）
        String filePath = "test1.txt";
        // 要写入的内容
        String content = "Hello, 这是用FileWriter写入的内容！\n第二行内容\n";

        // 2. 写入文件（try-with-resources自动关闭流，避免资源泄漏）
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8, true)) {
            // 参数说明：
            // - filePath：文件路径，不存在则自动创建
            // - StandardCharsets.UTF_8：指定编码（JDK11+支持，低版本可改用"UTF-8"字符串）
            // - true：追加写入（false则覆盖原有内容）
            writer.write(content);
            System.out.println("文件写入成功（FileWriter）：" + filePath);
        } catch (IOException e) {
            System.err.println("文件写入失败：" + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(args[0]+"+"+args[1]+"="+(a+b));
    }
}