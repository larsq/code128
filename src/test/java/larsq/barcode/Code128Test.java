package larsq.barcode;

import larsq.barcode.Code128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Code128Test {
    private Code128 target;

    @BeforeEach
    void setup() {
        target = new Code128();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("encoding")
    void testEncoding(String source, String expected) {
        assertEquals(expected, target.encode(source));
    }

    static Stream<Arguments> encoding() {
        return Stream.of(
                arguments("0123456789", "Í!7McyiÎ"),
                arguments("05552020202034", "Í%W4444BÅÎ"),
                arguments("2020-01-01", "Í44É-01-01LÎ"),
                arguments("123456789", "Ì1Ç7McyÆÎ"),
                arguments(" Hello World", "Ì Hello World6Î"));
    }

}