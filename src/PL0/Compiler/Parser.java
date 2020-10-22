package PL0.Compiler;

import PL0.Symbol.Symbol;
import PL0.Table.*;
import PL0.Utils.Global;
import PL0.Utils.SymbolSet;
import PL0.Utils.Error;

/**
 *   语法分析器
 */
public class Parser {
    private Scanner lex;            //词法分析器引用
    private Table table;            //符号表的引用
    private Interpreter interp;		//目标代码生成器的引用

    private SymbolSet declbegsys;   //声明开始集合
    private SymbolSet statbegsys;   //语句开始集合
    private SymbolSet facbegsys;    //因子开始集合

    private Symbol sym;             //当前符号
    private int index = 0;
    private final int symnum = Symbol.values().length;

    /**
     *  语法分析器初始化
     * @param l 词法分析器
     * @param t 符号表
     */
    public Parser(Scanner l, Table t, Interpreter ip){
        lex = l;
        table = t;
        interp = ip;

        //设置声明开始符号集
        declbegsys = new SymbolSet(symnum);
        declbegsys.set(Symbol.constsym);
        declbegsys.set(Symbol.varsym);
        declbegsys.set(Symbol.proceduresym);

        //设置语句开始符号集合
        statbegsys = new SymbolSet(symnum);
        statbegsys.set(Symbol.beginsym);
        statbegsys.set(Symbol.callsym);
        statbegsys.set(Symbol.ifsym);
        statbegsys.set(Symbol.whilesym);
        statbegsys.set(Symbol.readsym);
        statbegsys.set(Symbol.writesym);

        statbegsys.set(Symbol.elsesym);
        statbegsys.set(Symbol.untilsym);
        statbegsys.set(Symbol.dosym);
        statbegsys.set(Symbol.forsym);
        statbegsys.set(Symbol.downtosym);
        statbegsys.set(Symbol.tosym);


        //设置因子开始符号集
        facbegsys = new SymbolSet(symnum);
        facbegsys.set(Symbol.pl0ident);
        facbegsys.set(Symbol.pl0number);
        facbegsys.set(Symbol.pl0lparen);

    }

    public void parse() throws Exception {
        SymbolSet nxtlev = new SymbolSet(symnum);
        nxtlev.or(declbegsys);
        nxtlev.or(statbegsys);
        nxtlev.set(Symbol.pl0period);
        parseBlock(0,nxtlev);

        if(sym != Symbol.pl0period){
            Error.report(1);        //程序末尾需要.
        }
    }

    /**
     * 获得下一个语法符号
     */
    public void nextSym() {
        lex.getSym();
        sym =lex.sym;
    }

    /**
     * 测试当前符号是否合法
     *
     * @param s1 我们需要的符号
     * @param s2 如果不是我们需要的，则需要一个补救用的集合
     * @param errcode 错误号
     */
    void test(SymbolSet s1, SymbolSet s2, int errcode) {
        // 在某一部分（如一条语句，一个表达式）将要结束时时我们希望下一个符号属于某集合
        //（该部分的后跟符号），test负责这项检测，并且负责当检测不通过时的补救措施，程
        // 序在需要检测时指定当前需要的符号集合和补救用的集合（如之前未完成部分的后跟符
        // 号），以及检测不通过时的错误号。
        if (!s1.contain(sym)) {
            Error.report(errcode);
            // 当检测不通过时，不停获取符号，直到它属于需要的集合或补救的集合
            while (!s1.contain(sym) && !s2.contain(sym))
                nextSym();
        }
    }

    /**
     * 分析<分程序>
     *
     * @param lev 当前分程序所在层
     * @param fsys 当前模块后跟符号集
     */
    public void parseBlock(int lev, SymbolSet fsys) throws Exception {
        // <分程序> := [<常量说明部分>][<变量说明部分>][<过程说明部分>]<语句>

        int dx0, tx0, cx0;				// 保留初始dx，tx和cx
        SymbolSet nxtlev = new SymbolSet(symnum);

        dx0 = index;						// 记录本层之前的数据量（以便恢复）
        index = 3;
        tx0 = table.index;					// 记录本层名字的初始位置（以便恢复）

        if(table.get(tx0)==null)
            table.enter(Pl0Object.procedure,lev,0);
        table.getProcedure(table.index).address = interp.index;

        interp.gen(Fct.JMP, 0, 0);

        if (lev > Global.MAX_VIRTUAL_CODE)
            Error.report(2);       //超过最大代码数

        // 分析<说明部分>
        do {
            // <常量说明部分>
            if (sym == Symbol.constsym) {
                nextSym();

                parseConstDeclaration(lev);
                while (sym == Symbol.pl0comma) {
                    nextSym();
                    parseConstDeclaration(lev);
                }

                if (sym == Symbol.pl0semicolon)
                    nextSym();
                else
                    Error.report(3);				// 漏掉了逗号或者分号
            }

            // <变量说明部分>
            if (sym == Symbol.varsym) {
                nextSym();
                parseVarDeclaration(lev);
                while (sym == Symbol.pl0comma)
                {
                    nextSym();
                    parseVarDeclaration(lev);
                }

                if (sym == Symbol.pl0semicolon)
                    nextSym();
                else
                    Error.report(3);				// 漏掉了逗号或者分号
            }

            // <过程说明部分>
            while (sym == Symbol.proceduresym) {
                nextSym();
                if (sym == Symbol.pl0ident) {
                    table.enter(Pl0Object.procedure, lev, index);
                    nextSym();
                } else {
                    Error.report(4);		// procedure后应为标识符
                }

                if (sym == Symbol.pl0semicolon)
                    nextSym();
                else
                    Error.report(5);				// 漏掉了分号

                nxtlev = (SymbolSet) fsys.clone();
                nxtlev.set(Symbol.pl0semicolon);
                parseBlock(lev+1, nxtlev);

                if (sym == Symbol.pl0semicolon) {
                    nextSym();
                    nxtlev = (SymbolSet) statbegsys.clone();
                    nxtlev.set(Symbol.pl0ident);
                    nxtlev.set(Symbol.proceduresym);
                    test(nxtlev, fsys, 30);
                } else {
                    Error.report(5);				// 漏掉了分号
                }
            }

            nxtlev = (SymbolSet) statbegsys.clone();
            nxtlev.set(Symbol.pl0ident);
            test(nxtlev, declbegsys, 30);
        } while (declbegsys.contain(sym));		// 直到没有声明符号

        // 开始生成当前过程代码
        Pl0Procedure item = table.getProcedure(tx0);
        interp.code[item.address].a = interp.index;
        item.address = interp.index;		// 当前过程代码地址
        item.size = index;					// 声明部分中每增加一条声明都会给dx增加1，
        // 声明部分已经结束，dx就是当前过程的堆栈帧大小
        cx0 = interp.index;
        interp.gen(Fct.INT, 0, index);   // 生成分配内存代码

        table.printTable(tx0);

        // 分析<语句>
        nxtlev = (SymbolSet) fsys.clone();		// 每个后跟符号集和都包含上层后跟符号集和，以便补救
        nxtlev.set(Symbol.pl0semicolon);		// 语句后跟符号为分号或end
        nxtlev.set(Symbol.endsym);
        parseStatement(nxtlev, lev);
        interp.gen(Fct.OPR, 0, 0);		// 每个过程出口都要使用的释放数据段指令

        nxtlev = new SymbolSet(symnum);	// 分程序没有补救集合
        test(fsys, nxtlev, 30);				// 检测后跟符号正确性

        interp.listcode(cx0);

        index = dx0;							// 恢复堆栈帧计数器
        table.index = tx0;						// 回复名字表位置
    }

    /**
     * 分析<常量说明部分>
     * @param lev 当前所在的层次
     */
    void parseConstDeclaration(int lev) {
        if (sym == Symbol.pl0ident) {
            nextSym();
            if (sym == Symbol.pl0eql || sym == Symbol.pl0becomes) {
                if (sym == Symbol.pl0becomes)
                    Error.report(6);			// 把 = 写成了 :=
                nextSym();
                if (sym == Symbol.pl0number) {
                    table.enter(Pl0Object.constant, lev, index);
                    nextSym();
                } else {
                    Error.report(7);			// 常量说明 = 后应是数字
                }
            } else {
                Error.report(8);				// 常量说明标识后应是 =
            }
        } else {
            Error.report(9);					// const 后应是标识符
        }
    }

    /**
     * 分析<变量说明部分>
     * @param lev 当前层次
     */
    void parseVarDeclaration(int lev) {
        if (sym == Symbol.pl0ident) {
            // 填写名字表并改变堆栈帧计数器
            table.enter(Pl0Object.variable, lev, index);
            index ++;
            nextSym();
        } else {
            Error.report(10);					// var 后应是标识
        }
    }

    /**
     * 分析<语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    void parseStatement(SymbolSet fsys, int lev) throws Exception {
        SymbolSet nxtlev;
        switch (sym) {
            case pl0ident:
                parseAssignStatement(fsys, lev);
                break;
            case readsym:
                parseReadStatement(fsys, lev);
                break;
            case writesym:
                parseWriteStatement(fsys, lev);
                break;
            case callsym:
                parseCallStatement(fsys, lev);
                break;
            case ifsym:
                parseIfStatement(fsys, lev);
                break;
            case beginsym:
                parseBeginStatement(fsys, lev);
                break;
            case whilesym:
                parseWhileStatement(fsys, lev);
                break;

            case dosym:
                parseDoStatement(fsys, lev);
                break;
            case forsym:
                parseForStatement(fsys, lev);
                break;

            default:
                nxtlev = new SymbolSet(symnum);
                test(fsys, nxtlev, 30);
                break;
        }
    }

    /**
     * 分析<直到循环语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseDoStatement(SymbolSet fsys, int lev) throws Exception {
        int cx1, cx2;
        SymbolSet nxtlev;

        nextSym();
        nxtlev = (SymbolSet) fsys.clone();
        nxtlev.set(Symbol.whilesym);		    // 后跟符号为while

        if (sym == Symbol.whilesym)
            nextSym();
        else
            Error.report(18);		    // 缺少while

        cx1 = interp.index;                    //执行语句块的开始
        parseStatement(nxtlev, lev);           //分析<语句>


        nextSym();
        nxtlev.set(Symbol.untilsym);           //后跟符号为until
        if (sym == Symbol.untilsym)
            nextSym();
        else
            Error.report(18);		    // 缺少until

        parseCondition(nxtlev, lev);           //分析<条件>

        cx2 = interp.index;                    // 条件判断语句位置
        interp.gen(Fct.JPC, 0, 0);      // 生成条件跳转，但跳出循环的地址未知
        interp.gen(Fct.JMP, 0, cx1);       // 跳转到语句块开始

        interp.code[cx2].a = interp.index;    //回填当前地址


    }

    /**
     * 分析<复合语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseBeginStatement(SymbolSet fsys, int lev) throws Exception {
        SymbolSet nxtlev;

        nextSym();
        nxtlev = (SymbolSet) fsys.clone();
        nxtlev.set(Symbol.pl0semicolon);
        nxtlev.set(Symbol.endsym);
        parseStatement(nxtlev, lev);
        // 循环分析{; <语句>}，直到下一个符号不是语句开始符号或收到end
        while (statbegsys.contain(sym) || sym == Symbol.pl0semicolon) {
            if (sym == Symbol.pl0semicolon)
                nextSym();
            else
                Error.report(12);					// 缺少分号
            parseStatement(nxtlev, lev);
        }
        if (sym == Symbol.endsym)
            nextSym();
        else
            Error.report(13);						// 缺少end或分号
    }

    /**
     * 分析<过程调用语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseCallStatement(SymbolSet fsys, int lev) {
        int i;
        nextSym();
        if (sym == Symbol.pl0ident) {
            i = table.position(lex.id);
            if (i == 0) {
                Error.report(16);					// 过程未找到
            } else {
                Pl0Procedure item = table.getProcedure(i);
                if (item.kind == Pl0Object.procedure)
                    interp.gen(Fct.CAL, lev - item.level, item.address);
                else
                    Error.report(17);				// call后标识符应为过程
            }
            nextSym();
        } else {
            Error.report(18);						// call后应为标识符
        }
    }

    /**
     * 分析<写语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseWriteStatement(SymbolSet fsys, int lev) {
        SymbolSet nxtlev;

        nextSym();
        if (sym == Symbol.pl0lparen) {
            do {
                nextSym();
                nxtlev = (SymbolSet) fsys.clone();
                nxtlev.set(Symbol.pl0rparen);
                nxtlev.set(Symbol.pl0comma);
                parseExpression(nxtlev, lev);
                interp.gen(Fct.OPR, 0, 14);
            } while (sym == Symbol.pl0comma);

            if (sym == Symbol.pl0rparen)
                nextSym();
            else
                Error.report(18);				// write()中应为完整表达式
        }
        interp.gen(Fct.OPR, 0, 15);
    }

    /**
     * 分析<读语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseReadStatement(SymbolSet fsys, int lev) {
        int i;

        nextSym();
        if (sym == Symbol.pl0lparen) {
            do {
                nextSym();
                if (sym == Symbol.pl0ident)
                    i = table.position(lex.id);
                else
                    i = 0;

                if (i == 0) {
                    Error.report(19);			// read()中应是声明过的变量名
                } else {
                    Pl0Variable item = table.getVariable(i);
                    if (item.kind != Pl0Object.variable) {
                        Error.report(20);		// read()中的标识符不是变量, thanks to amd
                    } else {
                        interp.gen(Fct.OPR, 0, 16);
                        interp.gen(Fct.STO, lev-item.level, item.address);
                    }
                }

                nextSym();
            } while (sym == Symbol.pl0comma);
        } else {
            Error.report(21);					// 格式错误，应是左括号
        }

        if (sym == Symbol.pl0rparen) {
            nextSym();
        } else {
            Error.report(22);					// 格式错误，应是右括号
            while (!fsys.contain(sym))
                nextSym();
        }
    }

    /**
     * 分析<赋值语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseAssignStatement(SymbolSet fsys, int lev) {
        int i;
        SymbolSet nxtlev;

        i = table.position(lex.id);
        if (i > 0) {
            Pl0Variable item = table.getVariable(i);
            if (item.kind == Pl0Object.variable) {
                nextSym();
                if (sym == Symbol.pl0becomes)
                    nextSym();
                else
                    Error.report(23);					// 没有检测到赋值符号
                nxtlev = (SymbolSet) fsys.clone();
                parseExpression(nxtlev, lev);
                // parseExpression将产生一系列指令，但最终结果将会保存在栈顶，执行sto命令完成赋值
                interp.gen(Fct.STO, lev - item.level, item.address);
            } else {
                Error.report(24);						// 赋值语句格式错误
            }
        } else {
            Error.report(25);							// 变量未找到
        }
    }

    /**
     * 分析<表达式>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseExpression(SymbolSet fsys, int lev) {
        Symbol addop;
        SymbolSet nxtlev;

        // 分析[+|-]<项>
        if (sym == Symbol.pl0plus || sym == Symbol.pl0minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymbolSet) fsys.clone();
            nxtlev.set(Symbol.pl0plus);
            nxtlev.set(Symbol.pl0minus);
            parseTerm(nxtlev, lev);
            if (addop == Symbol.pl0minus)
                interp.gen(Fct.OPR, 0, 1);
        } else {
            nxtlev = (SymbolSet) fsys.clone();
            nxtlev.set(Symbol.pl0plus);
            nxtlev.set(Symbol.pl0minus);
            parseTerm(nxtlev, lev);
        }

        // 分析{<加法运算符><项>}
        while (sym == Symbol.pl0plus || sym == Symbol.pl0minus) {
            addop = sym;
            nextSym();
            nxtlev = (SymbolSet) fsys.clone();
            nxtlev.set(Symbol.pl0plus);
            nxtlev.set(Symbol.pl0minus);
            parseTerm(nxtlev, lev);
            if (addop == Symbol.pl0plus)
                interp.gen(Fct.OPR, 0, 2);
            else
                interp.gen(Fct.OPR, 0, 3);
        }
    }

    /**
     * 分析<项>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseTerm(SymbolSet fsys, int lev) {
        Symbol mulop;
        SymbolSet nxtlev;

        // 分析<因子>
        nxtlev = (SymbolSet) fsys.clone();
        nxtlev.set(Symbol.pl0times);
        nxtlev.set(Symbol.pl0slash);
        parseFactor(nxtlev, lev);

        // 分析{<乘法运算符><因子>}
        while (sym == Symbol.pl0times || sym == Symbol.pl0slash) {
            mulop = sym;
            nextSym();
            parseFactor(nxtlev, lev);
            if (mulop == Symbol.pl0times)
                interp.gen(Fct.OPR, 0, 4);
            else
                interp.gen(Fct.OPR, 0, 5);
        }
    }

    /**
     * 分析<因子>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseFactor(SymbolSet fsys, int lev) {
        SymbolSet nxtlev;

        test(facbegsys, fsys, 30);			// 检测因子的开始符号

        if (facbegsys.contain(sym)) {
            if (sym == Symbol.pl0ident) {			// 因子为常量或变量
                int i = table.position(lex.id);
                if (i > 0) {
                    TableItem item = table.get(i);
                    switch (item.kind) {
                        case constant:			// 名字为常量
                            interp.gen(Fct.LIT, 0, table.getConst(i).val);
                            break;
                        case variable:			// 名字为变量
                            interp.gen(Fct.LOD, lev - table.getVariable(i).level, table.getVariable(i).address);
                            break;
                        case procedure:			// 名字为过程
                            Error.report(26);				// 不能为过程
                            break;
                    }
                } else {
                    Error.report(27);					// 标识符未声明
                }
                nextSym();
            } else if (sym == Symbol.pl0number) {	// 因子为数
                int num = lex.value;
                if (num > Global.MAX_NUM) {
                    Error.report(28);
                    num = 0;
                }
                interp.gen(Fct.LIT, 0, num);
                nextSym();
            } else if (sym == Symbol.pl0lparen) {	// 因子为表达式
                nextSym();
                nxtlev = (SymbolSet) fsys.clone();
                nxtlev.set(Symbol.pl0rparen);
                parseExpression(nxtlev, lev);
                if (sym == Symbol.pl0rparen)
                    nextSym();
                else
                    Error.report(29);					// 缺少右括号
            } else {
                // 做补救措施
                test(fsys, facbegsys, 30);
            }
        }
    }

    /**
     * 分析<条件>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseCondition(SymbolSet fsys, int lev) {
        Symbol relop;
        SymbolSet nxtlev;

        if (sym == Symbol.oddsym) {
            // 分析 ODD<表达式>
            nextSym();
            parseExpression(fsys, lev);
            interp.gen(Fct.OPR, 0, 6);
        } else {
            // 分析<表达式><关系运算符><表达式>
            nxtlev = (SymbolSet) fsys.clone();
            nxtlev.set(Symbol.pl0eql);
            nxtlev.set(Symbol.pl0neql);
            nxtlev.set(Symbol.pl0less);
            nxtlev.set(Symbol.pl0lesseql);
            nxtlev.set(Symbol.pl0more);
            nxtlev.set(Symbol.pl0moreql);
            parseExpression(nxtlev, lev);
            if (sym == Symbol.pl0eql || sym == Symbol.pl0neql
                    || sym == Symbol.pl0less || sym == Symbol.pl0lesseql
                    || sym == Symbol.pl0more || sym == Symbol.pl0moreql) {
                relop = sym;
                nextSym();
                parseExpression(fsys, lev);
                switch (relop) {
                    case pl0eql:
                        interp.gen(Fct.OPR, 0, 8);
                        break;
                    case pl0neql:
                        interp.gen(Fct.OPR, 0, 9);
                        break;
                    case pl0less:
                        interp.gen(Fct.OPR, 0, 10);
                        break;
                    case pl0moreql:
                        interp.gen(Fct.OPR, 0, 11);
                        break;
                    case pl0more:
                        interp.gen(Fct.OPR, 0, 12);
                        break;
                    case pl0lesseql:
                        interp.gen(Fct.OPR, 0, 13);
                        break;
                }
            } else {
                Error.report(30);
            }
        }
    }


    /**
     * 扩充PL/0
     */


    /**
     * 分析<条件语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseIfStatement(SymbolSet fsys, int lev) throws Exception {
        int cx1, cx2;
        SymbolSet nxtlev;

        nextSym();
        nxtlev = (SymbolSet) fsys.clone();
        nxtlev.set(Symbol.thensym);
        nxtlev.set(Symbol.dosym);
        parseCondition(nxtlev, lev);			// 分析<条件>

        if (sym == Symbol.thensym)
            nextSym();
        else
            Error.report(15);			// 缺少then

        cx1 = interp.index;						// 保存当前指令地址
        interp.gen(Fct.JPC, 0, 0);		// 生成条件跳转指令，跳转地址未知，暂时写0
        parseStatement(fsys, lev);				// 处理then后的语句

        Scanner tmpLex = lex.clone();

        nextSym();
        nxtlev.set(Symbol.elsesym);

        if(sym == Symbol.elsesym){

            cx2 = interp.index;
            interp.gen(Fct.JMP, 0, 0);       // 生产跳转命令，地址未知

            nextSym();

            interp.code[cx1].a = interp.index;
            parseStatement(fsys, lev);

            interp.code[cx2].a = interp.index;  //回填if执行完毕跳转地址
        }else{
            lex = tmpLex;
            sym = lex.sym;
            interp.code[cx1].a = interp.index;	// 经statement处理后，index为then后语句执行
        }

    }

    /**
     * 分析<当型循环语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseWhileStatement(SymbolSet fsys, int lev) throws Exception {
        int cx1, cx2;
        SymbolSet nxtlev;

        cx1 = interp.index;                        // 保存判断条件操作的位置
        nextSym();
        nxtlev = (SymbolSet) fsys.clone();

        nxtlev.set(Symbol.dosym);               // 后跟符号为do
        parseCondition(nxtlev, lev);            // 分析<条件>

        cx2 = interp.index;                     // 保存循环体的结束的下一个位置
        interp.gen(Fct.JPC, 0, 0);       // 生成条件跳转，但跳出循环的地址未知

        if (sym == Symbol.dosym)
            nextSym();
        else
            Error.report(11);           // while后缺少do或begin

        parseStatement(fsys, lev);              // 分析<语句>
        interp.gen(Fct.JMP, 0, cx1);         // 回头重新判断条件
        interp.code[cx2].a = interp.index;       // 反填跳出循环的地址，与<条件语句>类似}

    }

    /**
     * 分析<for循环语句>
     * @param fsys 后跟符号集
     * @param lev 当前层次
     */
    private void parseForStatement(SymbolSet fsys, int lev) throws Exception {
        int cx1 = 0,cx2;
        SymbolSet nxtlev;

        nextSym();
        nxtlev = (SymbolSet) fsys.clone();

        int i = table.position(lex.id);                             //循环控制变量位置
        TableItem item = table.get(i);
        if(item.kind == Pl0Object.variable) {

            Pl0Variable variable = table.getVariable(i);

            parseStatement(fsys,lev);

            nxtlev.set(Symbol.tosym);
            nxtlev.set(Symbol.downtosym);                           //后跟to、downto
            Symbol mode = sym;

            cx1 = interp.index;
            interp.gen(Fct.LOD, lev-variable.level,variable.address);       //读取循环控制变量
            //根据不同的模式，有不同的循环判断条件
            if(sym == Symbol.tosym){
                nextSym();
                parseFactor(fsys,lev);                  //读取循环边界
                interp.gen(Fct.OPR, 0, 13);
            }else if(sym == Symbol.downtosym){
                nextSym();
                parseFactor(fsys,lev);                  //读取循环边界
                interp.gen(Fct.OPR, 0, 11);
            }else{
                Error.report(33);               //for关键字后需要to、downto
            }
            cx2 = interp.index;
            interp.gen(Fct.JPC,0,0);               //条件判断

            nxtlev.set(Symbol.dosym);
            if(sym == Symbol.dosym){
                nextSym();
                parseStatement(fsys,lev);               //执行语句块
            }else{
                Error.report(34);               //to、downto关键字后需要do
            }

            interp.gen(Fct.LOD, lev-variable.level, variable.address);          //读取循环判断变量
            //根据不同模式循环判断变量增大或减小
            if(mode == Symbol.tosym){
                interp.gen(Fct.OPR,0,17);
            }else if(mode == Symbol.downtosym) {
                interp.gen(Fct.OPR,0,18);
            }
            //存储循环判断变量
            interp.gen(Fct.STO, lev-variable.level, variable.address);
            //跳转到循环判断语句
            interp.gen(Fct.JMP, 0, cx1);
            interp.code[cx2].a = interp.index;

        }else{
            Error.report(35);       //for关键字后需要变量赋值表达式
        }
    }
}
