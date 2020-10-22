package PL0.Symbol;

public enum BasicSymbol {
    //运算
    pl0plus,pl0minus, pl0times,pl0slash,
    //其他符号
    pl0lparen,pl0rparen,pl0eql,pl0comma,
    pl0period,pl0neql,pl0semicolon;


    public static boolean contain(String str){
        for(BasicSymbol tmp: BasicSymbol.values()){
            if(tmp.name().equals(str)){
                return true;
            }
        }
        return false;
    }

    public static Symbol get(Character ch){
        Symbol res = null;
        String[] chs = {
                "+","-","*","/",
                "(",")","=",",",
                ".","#",";"
        };
        for(int i = 0; i != chs.length; ++i){
            if(ch == chs[i].charAt(0)){
                res = Symbol.valueOf((BasicSymbol.values())[i].name());
            }
        }
        return res;
    }

}
