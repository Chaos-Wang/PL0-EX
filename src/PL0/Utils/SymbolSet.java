package PL0.Utils;

import PL0.Symbol.Symbol;

import java.util.BitSet;

public class SymbolSet extends BitSet {

    /**
     * 构造一个符号集合
     * @param nbits 集合的容量
     */
    public SymbolSet(int nbits) {
        super(nbits);
    }

    /**
     * 把一个符号放到集合中
     * @param s 要放置的符号
     */
    public void set(Symbol s) {
        set(s.ordinal());
    }

    /**
     * 检查一个符号是否在集合中
     * @param s 要检查的符号
     * @return 若符号在集合中
     */
    public boolean contain(Symbol s) {
        return get(s.ordinal());
    }

}
