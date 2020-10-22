package PL0.Compiler;

import PL0.PL0Handler;
import PL0.Symbol.*;
import PL0.Utils.Error;
import PL0.Utils.Global;

import java.io.*;
import java.util.ArrayList;

/*
 *词法分析器
 */
public class Scanner implements Cloneable {
    public ArrayList<String> lines;                     //文件行
    public Character ch = ' ';                          //当前字符
    public String line = "";                            //当前行
    public int liIndex = -1;                            //当前行索引
    public int chIndex = 0;                             //当前字符位置
    public Symbol sym;                                  //当前读入符号
    public String id;                                   //标识符名字
    public int value;                                   //标识符数值（有的话）
    private boolean show = true;                        //是否显示代码

    /**
     * 初始化词法分析器
     * @param input
     */
    public Scanner(BufferedReader input) throws IOException {
        lines = new ArrayList<>();
        String tmp;
        while((tmp = input.readLine()) != null){
            lines.add(tmp);
        }
    }

    /**
     * 读取字符(为减少磁盘IO，每次读取一行)
     */
    public void getCh(){
        if(chIndex == line.length()){
            line = "";
            while(line.isEmpty()){
                line = lines.get(++liIndex) + "\n";
            }
            chIndex = 0;

            if(show) {
                PL0Handler.midPrinter.print(PL0Handler.interp.index + " " + line);
                if(Global.SHOW_PL0CODE){
                    System.out.print(PL0Handler.interp.index + " " + line);
                }

            }else{show = !show;}
        }
        ch = line.charAt(chIndex);
        chIndex ++;
    }

    /**
     * 词法分析，获取一个词法符号
     */
    public void getSym(){
        //是空格全跳过
        while(Character.isWhitespace(ch)){
            getCh();
        }
        if(Character.isAlphabetic(ch)){
            matchSymbol();
        }else if(Character.isDigit(ch)){
            matchNumber();
        }else{
            matchOperator();
        }
    }

    /**
     * 分析关键字或标识符
     */
    void matchSymbol(){
        String tmp = "";
        do{
            tmp = tmp + ch;
            getCh();
        }while(Character.isLowerCase(ch)||Character.isDigit(ch));
        id = tmp;

        if(ReservedSymbol.contain(tmp+"sym")){
            //保留字
            sym = Symbol.valueOf(tmp+"sym");
        }else{
            //一般标识符
            sym = Symbol.pl0ident;
        }

    }

    /**
     * 分析数字
     */
    void matchNumber(){
        sym = Symbol.pl0number;
        value = 0;
        int n = 0;
        //获取数字值
        do{
            value = value*10 + Character.digit(ch, 10);
            n++;
            getCh();
        }while(Character.isDigit(ch));
        n--;
        //位数超限
        if( n > Global.MAX_DIGIT){
            Error.report(31);
        }
    }

    /**
     * 分析操作符
     */
    void matchOperator(){
        switch (ch){
            //赋值符号
            case ':':
                getCh();
                if(ch == '='){
                    sym = Symbol.pl0becomes;
                    getCh();
                }else{
                    //不能识别的符号
                    sym = Symbol.pl0null;
                }
                break;
            //小于或者小等
            case '<':
                getCh();
                if (ch == '=') {
                    sym = Symbol.pl0lesseql;
                    getCh();
                }else{
                    sym = Symbol.pl0less;
                }
                break;
            //大于或者大等
            case '>':
                getCh();
                if (ch == '=') {
                    sym = Symbol.pl0moreql;
                    getCh();
                }else{
                    sym = Symbol.pl0more;
                }
                break;
            //其他单字符操作符
            default:
                sym = BasicSymbol.get(ch);
                if(sym != Symbol.pl0period){getCh();}
                break;
        }
    }

    @Override
    protected Scanner clone() throws CloneNotSupportedException{
        Scanner scanner = (Scanner) super.clone();
        scanner.lines = (ArrayList<String>) lines.clone();
        scanner.show = false;
        return scanner;
    }
}
