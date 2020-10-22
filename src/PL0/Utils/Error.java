package PL0.Utils;

import PL0.PL0Handler;
/**
 * 错误计数器
 */

public class Error {
    //错误数目统计
    public static int err = 0;

    /**
     * 返回错误信息
     * @param errCode   错误编码
     */
    public static void report(int errCode){
        String tmp = "**** Error:"+ Global.ERROR_TEXT[errCode] + " on line " + PL0Handler.lex.liIndex
                + " near \"" + PL0Handler.lex.id + "\"";

        System.out.println(tmp);
        PL0Handler.midPrinter.println(tmp);
        err ++;
    }
}
