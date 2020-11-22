package larsq.barcode;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;
import static larsq.barcode.Codeset.*;
import static larsq.barcode.Codeset.C;
import static larsq.barcode.SymbolClass.CODE;
import static larsq.barcode.SymbolClass.SHIFT;

public class SymbolFactory {
    private static final List<Symbol> csA = construct(basicSymbols(), codeAControlSymbols());
    private static final List<Symbol> csB = construct(basicSymbols(), codeBExtendedSymbols());
    private static final List<Symbol> csC = codeCSymbols();

    static final Symbol START_SYMBOL_A = new Symbol("SC", SymbolClass.START, 103, A);
    static final Symbol START_SYMBOL_B = new Symbol("SC", SymbolClass.START, 104, B);
    static final Symbol START_SYMBOL_C = new Symbol("SC", SymbolClass.START, 105, C);

    static final Symbol SHIFT_A = new Symbol("SH_A", SHIFT, 98, A);
    static final Symbol SHIFT_B = new Symbol("SH_B", SHIFT, 98, B);
    static final Symbol CODE_A = new Symbol("C_A", CODE, 101, A);
    static final Symbol CODE_B = new Symbol("C_B", CODE, 100, B);
    static final Symbol CODE_C = new Symbol("C_C", CODE, 99, C);

    // Is not yet supported
    // private static final Symbol FNC_1 = new Symbol("", Symbols.SymbolCategory.FNC_1, 102, null);
    // private static final Symbol FNC_2 = new Symbol("", Symbols.SymbolCategory.FNC_2, 97, null);
    // private static final Symbol FNC_3 = new Symbol("", Symbols.SymbolCategory.FNC_3, 96, null);


    public static List<Symbol> csA() {
        return csA;
    }

    public static List<Symbol> csB() {
        return csB;
    }

    public static List<Symbol> csC() {
        return csC;
    }


    static List<Symbol> switchSymbolsOf(Codeset codeset) {
        switch (codeset) {
            case A:
                return of(CODE_B, CODE_C, SHIFT_B);
            case B:
                return of(CODE_A, CODE_C, SHIFT_A);
            case C:
                return of(CODE_A, CODE_B);
        }

        throw new UnsupportedOperationException("Unsupported codeset: " + codeset);
    }

    static List<Symbol> alphabet(Codeset codeset) {
        switch (codeset) {
            case A:
                return csA();
            case B:
                return csB();
            case C:
                return csC();
        }

        throw new UnsupportedOperationException("Unsupported codeset: " + codeset);
    }

    private static List<Symbol> basicSymbols() {
        Symbol[] symbols = new Symbol[64];
        Arrays.setAll(symbols, index -> new Symbol(String.valueOf(Character.toChars(index + 32)), SymbolClass.NORMAL, index, null));
        return ImmutableList.copyOf(symbols);
    }

    private static List<Symbol> codeAControlSymbols() {
        Symbol[] symbols = new Symbol[32];
        Arrays.setAll(symbols, index -> new Symbol(String.valueOf(Character.toChars(index)), SymbolClass.CONTROL, index + 64, null));
        return ImmutableList.copyOf(symbols);
    }

    private static List<Symbol> codeBExtendedSymbols() {
        Symbol[] symbols = new Symbol[32];
        Arrays.setAll(symbols, index -> new Symbol(String.valueOf(Character.toChars(index + 96)), index == 31 ? SymbolClass.CONTROL : SymbolClass.NORMAL, index + 64, null));

        return ImmutableList.copyOf(symbols);

    }

    private static List<Symbol> codeCSymbols() {
        Symbol[] symbols = new Symbol[100];
        Arrays.setAll(symbols, index -> new Symbol(String.format("%02d", index), SymbolClass.NORMAL, index, null));
        return ImmutableList.copyOf(symbols);
    }

    @SafeVarargs
    private static List<Symbol> construct(List<Symbol>... symbols) {
        return Arrays.stream(symbols).flatMap(Collection::stream).collect(Collectors.toList());
    }

}
