import org.example.LexAnalysis;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * @author Anyan Huang
 * @version 1.0.0
 * @date 2022/12/29
 * @doc 词法分析测试类
 */
public class TestLexAnalysis {
    // 记录标准流的位置
    final InputStream oldIn = System.in;
    final PrintStream oldOut = System.out;
    // 设置输出流
    final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * 验证测试结果
     * @throws IOException
     */
    @Test
    public void resultTest() throws IOException {
        System.setOut(new PrintStream(outContent));
        // 设置输入流
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(this.getClass().getClassLoader().getResource("test.txt").getPath()));
            System.setIn(in);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 定义待测试类的实例
        LexAnalysis t = new LexAnalysis();
        t.analysis();
        //读入预期输出
        File file = new File(this.getClass().getClassLoader().getResource("ans.txt").getPath());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String temp = "";
        while ((temp = bufferedReader.readLine()) != null) {
            //windows输出流的换行为\r\n
            stringBuilder.append(temp + "\r\n");
        }
        bufferedReader.close();
        String text = stringBuilder.toString();
        //由于多添加了一个换行，此处删去
        text = text.substring(0,stringBuilder.lastIndexOf("\r\n"));
        // 在下面测试outContent中是否包含期望的结果
        Assert.assertEquals(text, outContent.toString());
        // 将输入流和输出流都改回标准流
        System.setIn(oldIn);
        System.setOut(oldOut);
    }

}
