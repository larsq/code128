package larsq.barcode;

import java.util.Optional;

import static larsq.barcode.SymbolClass.*;
import static larsq.barcode.SymbolClass.NORMAL;

public class Symbol {
    private final String token;
    private final SymbolClass category;
    private final int checksumValue;
    private final Codeset nextCodeset;
    private final String encoding;

    public Symbol(String token, SymbolClass category, int checksumValue, Codeset nextCodeset) {
        this.token = token;
        this.category = category;
        this.checksumValue = checksumValue;
        this.nextCodeset = nextCodeset;
        this.encoding = category == NORMAL ? token : "";
    }

    public SymbolClass category() {
        return category;
    }

    public Codeset nextCodeset() {
        return nextCodeset;
    }

    public int checksumValue() {
        return checksumValue;
    }

    public String encoding() {
        return encoding;
    }

    int weight() {
        return 1 - encoding.length();
    }

    public static Symbol fromCodeset(String symbol, Codeset codeset) {
        return codeset.alphabet().stream()
                .filter(sym -> sym.token.equals(symbol)).findAny()
                .orElse(null);
    }

    String name() {
        if (category() == CONTROL) {
            return Encoding.name(token.charAt(0));
        }

        if (category() == NORMAL) {
            return token;
        }

        return Optional.ofNullable(nextCodeset).map(Enum::name).orElse("") + ":" + category.name();
    }

    @Override
    public String toString() {
        return "Symbol{" + name() + "}";
    }

    public boolean isSwitchSymbol() {
        return category == CODE || category == SHIFT;
    }

    public boolean isShiftSymbol() {
        return category == SHIFT;
    }
}
