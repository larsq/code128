package larsq.barcode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SymbolFactoryTest {
    @Test
    @DisplayName("Code C contains 100 character symbols")
    void codeset_C_should_contain_100_character_symbols() {
        List<Symbol> symbols = Codeset.C.alphabet();

        assertAll(
                () -> assertTrue(symbols.stream().allMatch(s -> s.encoding().length() == 2),
                        "encodings should be of size 2"),
                () -> assertEquals(100, symbols.size(),
                        "should contain 100 symbols"),
                () -> assertTrue(symbols.stream().allMatch(s -> s.category() == SymbolClass.NORMAL),
                        "all symbols should be characters"));
    }

    @Test
    @DisplayName("Code A contains 64 character symbols and 32 control symbols")
    void codesetA_should_contain_64_character_symobols_and_32_control_symbols() {
        Map<SymbolClass, List<Symbol>> group = Codeset.A.alphabet().stream().collect(Collectors.groupingBy(Symbol::category));

        Assertions.assertAll(
                () -> assertEquals(64, group.get(SymbolClass.NORMAL).size(), "should contain 64 character symbols"),
                () -> assertEquals(32, group.get(SymbolClass.CONTROL).size(), "should contain 32 control symbols")
        );
    }

    @Test
    @DisplayName("Code B contains 95 character symbols and 1 control symbols")
    void codesetA_should_contain_95_character_symobols_and_1_control_symbols() {
        Map<SymbolClass, List<Symbol>> group = Codeset.B.alphabet().stream().collect(Collectors.groupingBy(Symbol::category));

        Assertions.assertAll(
                () -> assertEquals(95, group.get(SymbolClass.NORMAL).size(), "should contain 96 character symbols"),
                () -> assertEquals(1, group.get(SymbolClass.CONTROL).size(), "should contain 1 control symbols")
        );
    }

}