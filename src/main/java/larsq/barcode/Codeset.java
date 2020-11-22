package larsq.barcode;

import java.util.List;

public enum Codeset {
    A(1), B(1), C(2);

    final int length;

    Codeset(int length) {
        this.length = length;
    }

    public List<Symbol> alphabet() {
        return SymbolFactory.alphabet(this);
    }


}
