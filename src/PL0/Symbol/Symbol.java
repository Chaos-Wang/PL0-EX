package PL0.Symbol;

public enum Symbol{
        //类型
        pl0null,pl0ident,pl0number,
        //运算
        pl0plus,pl0minus, pl0times,pl0slash,
        //关系
        pl0eql,pl0neql,pl0less, pl0lesseql, pl0more,pl0moreql,
        //其他符号
        pl0lparen,pl0rparen,pl0comma,pl0period,pl0semicolon,
        //赋值标记
        pl0becomes,
        //保留字
        beginsym,callsym,constsym,dosym,endsym,ifsym,oddsym,
        proceduresym, readsym,thensym,varsym,whilesym,writesym,
        elsesym,untilsym,forsym,downtosym,tosym
}

