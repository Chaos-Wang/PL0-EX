package PL0.Symbol;

public enum ReservedSymbol{
    beginsym,callsym,constsym,dosym,endsym,ifsym,oddsym,
    proceduresym, readsym,thensym,varsym,whilesym,writesym,
    elsesym,untilsym,forsym,downtosym,tosym;

    public static boolean contain(String str){
        for(ReservedSymbol tmp: ReservedSymbol.values()){
            if(tmp.name().equals(str)){
                return true;
            }
        }
        return false;
    }
}

