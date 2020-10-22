package PL0.Table;

import PL0.PL0Handler;
import PL0.Utils.Global;
import PL0.Utils.Error;

public class Table {
    //名字表
    private TableItem[] table = new TableItem[Global.MAX_TABLE];
    //当前表项索引
    public int index = 0;

    /**
     * 获取表项
     * @param i     表中的位置
     * @return      位置i处的内容
     */
    public TableItem get(int i){

        return table[i];
    }

    /**
     * 获取Const
     * @param i     表中的位置
     * @return      位置i处的内容
     */
    public Pl0Const getConst(int i){
        return (Pl0Const) table[i];
    }

    /**
     * 获取Procedure
     * @param i     表中的位置
     * @return      位置i处的内容
     */
    public Pl0Procedure getProcedure(int i){
        return (Pl0Procedure) table[i];
    }

    /**
     * 获取Variable
     * @param i     表中的位置
     * @return      位置i处的内容
     */
    public Pl0Variable getVariable(int i){
        return (Pl0Variable)table[i];
    }

    /**
     * 把某个符号插入名字表
     * @param k     符号类型
     * @param lev   名字所在层次
     * @param dx    当前
     */
    public void enter(Pl0Object k, int lev, int dx){
        if(index != 0||table[0] != null){
            index ++;
        }

        TableItem item;

        switch (k){
            //常量
            case constant:
                item = new Pl0Const();
                if(PL0Handler.lex.value > Global.MAX_NUM){
                    Error.report(31);
                    ((Pl0Const) item).val = 0;
                }else{
                    ((Pl0Const) item).val = PL0Handler.lex.value;
                }
                break;
            //变量
            case variable:
                item = new Pl0Variable();
                ((Pl0Variable) item).level = lev;
                ((Pl0Variable) item).address = dx;
                break;
            //过程
            default:
                item = new Pl0Procedure();
                ((Pl0Procedure) item).level = lev;
                break;
        }
        item.name = PL0Handler.lex.id;
        item.kind = k;

        table[index] = item;
    }

    /**
     * 打印符号表内容
     * @param start 当前作用域符号表区间左端
     */
    public void printTable(int start){
        if(!Global.SHOW_TABLE){return;}
        System.out.println("TABLE:");

        if(start >= index){
            System.out.println("    NULL");
        }

        for(int i = start + 1; i <= index; ++i){
            String msg = "OOPS! UNKNOWN TABLE ITEM!";
            switch (table[i].kind) {
                case constant:
                    msg = "    " + i + " const " + table[i].name + " val=" + ((Pl0Const)table[i]).val;
                    break;
                case variable:
                    msg = "    " + i + " var   " + table[i].name + " lev=" + ((Pl0Variable)table[i]).level + " addr=" + ((Pl0Variable)table[i]).address;
                    break;
                case procedure:
                    msg = "    " + i + " proc  " + table[i].name + " lev=" + ((Pl0Procedure)table[i]).level + " addr=" + ((Pl0Procedure)table[i]).address + " size=" + ((Pl0Procedure)table[i]).size;
                    break;
            }
            System.out.println(msg);
            PL0Handler.tablePrinter.println(msg);
        }
    }

    /**
     * 在名字表中查找某个名字位置
     * @param name  要查找的名字
     * @return  找到则返回名字项下标，否则返回0
     */
    public int position(String name){
        for (int i = index; i > 0; i--)
            if (get(i).name.equals(name))
                return i;

        return 0;
    }

}
