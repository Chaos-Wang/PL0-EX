package PL0;

import PL0.Compiler.Interpreter;
import PL0.Compiler.Parser;
import PL0.Compiler.Scanner;
import PL0.Table.Table;
import PL0.Utils.Error;
import PL0.Utils.Global;

import java.io.*;

public class PL0Handler {
    public static Scanner lex;                      //词法分析器
    public static Parser parser;                    //语法分析器
    public static Interpreter interp;               //解释器
    public static Table table;                      //名字表

    public static PrintStream tablePrinter;         //输出名字表
    public static PrintStream codePrinter;          //输出中间代码
    public static PrintStream resultPrinter;        //输出结果
    public static PrintStream midPrinter;	        // 输出源文件及其各行对应的首地址

    public static BufferedReader stdin;             //标准输入
    public static File file;                               //源码文件
    public String filePath;                         //文件地址
    public Boolean isCompiled = false;              //是否编译

    public PL0Handler(String filePath) throws IOException {

        file = new File(filePath);
        stdin = new BufferedReader(new FileReader(file), 4096);
        this.filePath = file.getParentFile().getAbsolutePath();

        midPrinter = new PrintStream(this.filePath + "\\" + file.getName() + "_MidCode.tmp");
        midPrinter.println("Input pl/0 file?   " + file.getAbsolutePath());

        table = new Table();
        interp = new Interpreter();
        lex = new Scanner(stdin);
        parser = new Parser(lex, table, interp);
    }

    public PL0Handler() throws IOException {
        stdin = new BufferedReader(new InputStreamReader(System.in));

        // 输入文件名
        filePath = "";
        System.out.print("Input pl/0 file?   ");
        while (filePath.equals(""))
            filePath = stdin.readLine();
        file = new File(filePath);
        stdin = new BufferedReader(new FileReader(file), 4096);
        filePath = file.getParentFile().getAbsolutePath();

        midPrinter = new PrintStream(filePath + "\\" + file.getName() + "_MidCode.tmp");
        midPrinter.println("Input pl/0 file?   " + file.getAbsolutePath());

        table = new Table();
        interp = new Interpreter();
        lex = new Scanner(stdin);
        parser = new Parser(lex, table, interp);
    }

    /**
     * 执行编译动作
     * @return 是否编译成功
     */
    private boolean compile() {
        boolean abort = false;

        try {
            codePrinter = new PrintStream(filePath + "\\" + file.getName() +"_Code.tmp");
            tablePrinter = new PrintStream(filePath + "\\" + file.getName() +"_Table.tmp");
            parser.nextSym();		// 前瞻分析需要预先读入一个符号
            parser.parse();			// 开始语法分析过程（连同语法检查、目标代码生成）
        } catch (java.lang.Error e) {
            // 如果是发生严重错误则直接中止
            abort = true;
        } catch (Exception e) {
        } finally {
            codePrinter.close();
            midPrinter.close();
            tablePrinter.close();
        }
        if (abort)
            System.exit(0);

        if(Error.err == 0)
            isCompiled = true;

        // 编译成功是指完成编译过程并且没有错误
        return (Error.err == 0);
    }

    /**
     *  执行编译成功的代码
     * @throws FileNotFoundException
     */
    private void run() throws FileNotFoundException {
        // 如果成功编译则接着解释运行
        resultPrinter = new PrintStream(filePath + "\\" + file.getName() + "_Result.tmp");
        interp.interpret();
        resultPrinter.close();
    }

    public void start() throws FileNotFoundException {
        if (compile()) {
            // 如果成功编译则接着解释运行
            run();
        } else {
            System.out.print("Errors in pl/0 program");
        }
    }

    public static void newStart() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        PL0Handler pl0 = null;
        System.out.println("**************************");
        System.out.println("file + 文件名  设置源文件");
        System.out.println("compile  编译");
        System.out.println("run  运行");
        System.out.println("compile&run  编译并运行");
        System.out.println("show table  显示变量表");
        System.out.println("show code  显示中间代码");
        System.out.println("show pl0code  显示源代码");
        System.out.println("hide table  不显示变量表");
        System.out.println("hide code  不显示中间代码");
        System.out.println("hide pl0code  不显示源代码");
        System.out.println("help  打印帮助");
        System.out.println("exit 退出");
        System.out.println("**************************");


        System.out.print(">");
        String line = bufferedReader.readLine();
        while(!line.equals("exit")) {

            if(line.contains("file")){
                line = line.split(" ")[1];
                if(!new File(line).exists())
                {
                    System.out.println("文件不存在");
                }else{
                    pl0 = new PL0Handler(line);
                }
            }
            else if(line.equals("compile")){
                if(pl0 == null){
                    System.out.println("未导入文件");
                }else if (pl0.isCompiled){
                    System.out.println("已编译");
                }
                else if(pl0.compile()){
                    System.out.println("编译成功");
                }else{
                    System.out.println("编译失败");
                    System.exit(1);
                }
            }
            else if(line.equals("run")){
                if (pl0 == null || PL0Handler.tablePrinter == null) {
                    System.out.println("未导入文件或未编译");
                }else{
                    pl0.run();
                }

            }
            else if(line.equals("compile&run")){
                if(pl0 == null){
                    System.out.println("未导入文件");
                }else if (pl0.isCompiled){
                    System.out.println("已编译");
                }
                else if(pl0.compile()){
                    System.out.println("编译成功");
                }else{
                    System.out.println("编译失败");
                    System.exit(1);
                }
                pl0.run();
            }
            else if(line.equals("show table")){
                Global.SHOW_TABLE = true;
                System.out.println(Global.SHOW_TABLE);
            }
            else if(line.equals("show code")){
                Global.SHOW_CODE = true;
                System.out.println(Global.SHOW_CODE);

            }
            else if(line.equals("show pl0code")){
                Global.SHOW_PL0CODE = true;
                System.out.println(Global.SHOW_PL0CODE);

            }
            else if(line.equals("hide table")){
                Global.SHOW_TABLE = false;
                System.out.println(Global.SHOW_TABLE);
            }
            else if(line.equals("hide code")){
                Global.SHOW_CODE = false;
                System.out.println(Global.SHOW_CODE);

            }
            else if(line.equals("hide pl0code")){
                Global.SHOW_PL0CODE = false;
                System.out.println(Global.SHOW_PL0CODE);
            }
            else if(line.equals("help")){
                System.out.println("**************************");
                System.out.println("file + 文件名  设置源文件");
                System.out.println("compile  编译");
                System.out.println("run  运行");
                System.out.println("compile&run  编译并运行");
                System.out.println("show table  显示变量表");
                System.out.println("show code  显示中间代码");
                System.out.println("show pl0code  显示源代码");
                System.out.println("hide table  不显示变量表");
                System.out.println("hide code  不显示中间代码");
                System.out.println("hide pl0code  不显示源代码");
                System.out.println("help  打印帮助");
                System.out.println("exit 退出");
                System.out.println("**************************");
            }
            else{
                System.out.println("未知指令");
            }

            System.out.print(">");
            line = bufferedReader.readLine();
        }
        System.out.println("System exited.");
    }

}
