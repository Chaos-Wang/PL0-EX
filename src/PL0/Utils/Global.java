package PL0.Utils;

/**
 * 这个类用于保存全局编译器设置和错误代码
 */
public class Global {
    public static int MAX_VIRTUAL_CODE=500;         //最多虚拟机代码数
    public static int MAX_NUM = 2048;               //数字最大值
    public static int MAX_DIGIT = 14;               //数字最大位数
    public static int MAX_TABLE = 100;              //名字表最大大小
    public static boolean SHOW_TABLE = true;        //是否显示名字表
    public static boolean SHOW_CODE = true;         //是否显示中间代码
    public static boolean SHOW_PL0CODE = true;      //是否显示原代码


    public static String[] ERROR_TEXT = {
            "","缺少符号.","代码表过长",
            "缺少符号,或;","procedure后应为标识符", "缺少符号;",
            "需要:=而不是=","常量需要在定义时赋值", "常量声明后需要=",
            "const关键字后应该是标识符", "var关键字后应该是标识符","while关键字后需要do关键字",
            "缺少分号","缺少分号或end","if关键字后需要then",
            "未找到过程","call后标识符应为过程","all后应为标识符",
            "write()中应为完整表达式","read()中应是声明过的变量名","read()中的标识符不是变量",
            "格式错误，应是左括号","格式错误，应是右括号","缺少赋值符号",
            "赋值语句格式错误","变量未找到","不能为过程",
            "标识符未声明","数字过大","缺少右括号",
            "语法错误","数字位数超限","数字过大",
            "for关键字后需要to、downto","to、downto关键字后需要do","for关键字后需要变量赋值表达式"
    };
}
