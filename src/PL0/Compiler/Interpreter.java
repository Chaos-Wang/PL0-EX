package PL0.Compiler;

import PL0.PL0Handler;
import PL0.Utils.Global;
import PL0.Utils.Error;

/**
 * 类P-Code指令类型
 */
enum Fct {
    LIT, OPR, LOD, STO, CAL, INT, JMP, JPC
}

class Instruction {
    /**
     * 虚拟机代码指令
     */
    public Fct f;

    /**
     * 引用层与声明层的层次差
     */
    public int l;

    /**
     * 指令参数
     */
    public int a;
}

/**
 *　　代码解释器
 */
public class Interpreter {
    // 解释执行时使用的栈大小
    final int stacksize = 500;

    /**
     * 虚拟机代码指针，取值范围[0, Global.MAX_VIRTUAL_CODE]
     */
    public int index = 0;

    /**
     * 存放虚拟机代码的数组
     */
    public Instruction[] code = new Instruction[Global.MAX_VIRTUAL_CODE];

    /**
     * 生成虚拟机代码
     * @param x instruction.f
     * @param y instruction.l
     * @param z instruction.a
     */
    public void gen(Fct x, int y, int z) {
        if (index >= Global.MAX_VIRTUAL_CODE) {
            Error.report(1);
            throw new java.lang.Error("Program too long");
        }

        code[index] = new Instruction();
        code[index].f = x;
        code[index].l = y;
        code[index].a = z;
        index ++;
    }

    /**
     * 输出目标代码清单
     * @param start 开始输出的位置
     */
    public void listcode(int start) {
        if (Global.SHOW_CODE) {
            for (int i=start; i<index; i++) {
                String msg = i + " " + code[i].f + " " + code[i].l + " " + code[i].a;
                System.out.println(msg);
                PL0Handler.codePrinter.println(msg);
            }
        }
    }

    /**
     * 解释程序
     */
    public void interpret() {
        int p, b, t;						// 指令指针，指令基址，栈顶指针
        Instruction i;						// 存放当前指令
        int[] s = new int[stacksize];		// 栈

        System.out.println("Run " + PL0Handler.file.getName());
        t = b = p = 0;
        s[0] = s[1] = s[2] = 0;
        do {
            i = code[p];					// 读当前指令
            p ++;
            switch (i.f) {
                case LIT:				// 将a的值取到栈顶
                    s[t] = i.a;
                    t++;
                    break;
                case OPR:				// 数学、逻辑运算
                    switch (i.a)
                    {
                        case 0:
                            t = b;
                            p = s[t+2];
                            b = s[t+1];
                            break;
                        case 1:
                            s[t-1] = -s[t-1];
                            break;
                        case 2:
                            t--;
                            s[t-1] = s[t-1]+s[t];
                            break;
                        case 3:
                            t--;
                            s[t-1] = s[t-1]-s[t];
                            break;
                        case 4:
                            t--;
                            s[t-1] = s[t-1]*s[t];
                            break;
                        case 5:
                            t--;
                            s[t-1] = s[t-1]/s[t];
                            break;
                        case 6:
                            s[t-1] = s[t-1]%2;
                            break;
                        case 8:
                            t--;
                            s[t-1] = (s[t-1] == s[t] ? 1 : 0);
                            break;
                        case 9:
                            t--;
                            s[t-1] = (s[t-1] != s[t] ? 1 : 0);
                            break;
                        case 10:
                            t--;
                            s[t-1] = (s[t-1] < s[t] ? 1 : 0);
                            break;
                        case 11:
                            t--;
                            s[t-1] = (s[t-1] >= s[t] ? 1 : 0);
                            break;
                        case 12:
                            t--;
                            s[t-1] = (s[t-1] > s[t] ? 1 : 0);
                            break;
                        case 13:
                            t--;
                            s[t-1] = (s[t-1] <= s[t] ? 1 : 0);
                            break;
                        case 14:
                            System.out.print(s[t-1]);
                            PL0Handler.resultPrinter.print(s[t-1]);
                            t--;
                            break;
                        case 15:
                            System.out.println();
                            PL0Handler.resultPrinter.println();
                            break;
                        case 16:
                            System.out.print("?");
                            PL0Handler.resultPrinter.print("?");
                            s[t] = 0;
                            try {
                                s[t] = Integer.parseInt(PL0Handler.stdin.readLine());
                            } catch (Exception e) {}
                            PL0Handler.resultPrinter.println(s[t]);
                            t++;
                            break;
                        case 17:
                            s[t-1] = s[t-1]+1;
                            break;
                        case 18:
                            s[t-1] = s[t-1]-1;
                            break;
                    }
                    break;
                case LOD:				// 取相对当前过程的数据基地址为a的内存的值到栈顶
                    s[t] = s[base(i.l,s,b)+i.a];
                    t++;
                    break;
                case STO:				// 栈顶的值存到相对当前过程的数据基地址为a的内存
                    t--;
                    s[base(i.l, s, b) + i.a] = s[t];
                    break;
                case CAL:				// 调用子过程
                    s[t] = base(i.l, s, b); 	// 将静态作用域基地址入栈
                    s[t+1] = b;					// 将动态作用域基地址入栈
                    s[t+2] = p;					// 将当前指令指针入栈
                    b = t;  					// 改变基地址指针值为新过程的基地址
                    p = i.a;   					// 跳转
                    break;
                case INT:			// 分配内存
                    t += i.a;
                    break;
                case JMP:				// 直接跳转
                    p = i.a;
                    break;
                case JPC:				// 条件跳转（当栈顶为0的时候跳转）
                    t--;
                    if (s[t] == 0)
                        p = i.a;
                    break;
            }
        } while (p != 0);
    }

    /**
     * 通过给定的层次差来获得该层的堆栈帧基地址
     * @param l 目标层次与当前层次的层次差
     * @param s 运行栈
     * @param b 当前层堆栈帧基地址
     * @return 目标层次的堆栈帧基地址
     */
    private int base(int l, int[] s, int b) {
        int b1 = b;
        while (l > 0) {
            b1 = s[b1];
            l --;
        }
        return b1;
    }
}
