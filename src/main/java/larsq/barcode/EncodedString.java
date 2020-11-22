package larsq.barcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

public class EncodedString {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedString.class);

    private final List<Symbol> symbols;
    private final String encodedMessage;
    private final int weight;
    private final Codeset currentCodeSet;

    private EncodedString(List<Symbol> symbols, String encodedMessage, int weight) {
        this.symbols = unmodifiableList(symbols);
        this.encodedMessage = encodedMessage;
        this.weight = weight;
        this.currentCodeSet = deriveCurrentCodeSet();
    }

    private EncodedString(Symbol symbol) {
        this.symbols = singletonList(symbol);
        this.encodedMessage = deriveFromSymbols(symbols);
        this.weight = calculateWeight(symbols, this.encodedMessage);
        this.currentCodeSet = Objects.requireNonNull(symbol.nextCodeset(), "Symbol must set nexCodeSet");
    }

    int weight() {
        return weight;
    }

    String encodedMessage() {
        return encodedMessage;
    }

    EncodedString withSymbol(Symbol symbol) {
        List<Symbol> symbols = new ArrayList<>(this.symbols);
        symbols.add(symbol);

        String updatedMessage = this.encodedMessage + symbol.encoding();
        int oldSymbolChange = this.weight - this.encodedMessage.length();
        int weight = updatedMessage.length() + oldSymbolChange + symbol.weight();

        LOGGER.debug("adding {}, message={}, new weight={}", symbol.name(), updatedMessage, weight);

        return new EncodedString(symbols, updatedMessage, weight);
    }

    Symbol lastSymbol() {
        return symbols.get(symbols.size() - 1);
    }

    private Symbol beforeLastSymbolIfAny() {
        return symbols.size() > 2 ? symbols.get(symbols.size() - 2) : null;
    }

    private Codeset deriveCurrentCodeSet(boolean excludeLast) {
        int stopAt = excludeLast ? symbols.size() - 2 : symbols.size();
        List<Symbol> symbolsToConsider = this.symbols.subList(1, stopAt);

        return symbolsToConsider
                .stream().map(Symbol::nextCodeset)
                .reduce(symbols.get(0).nextCodeset(), (oldCs, newCs) -> newCs != null ? newCs : oldCs);
    }

    private Codeset deriveCurrentCodeSet() {
        Symbol beforeLast = beforeLastSymbolIfAny();

        if (beforeLast != null && beforeLast.isShiftSymbol()) {
            return deriveCurrentCodeSet(true);
        } else {
            return deriveCurrentCodeSet(false);
        }
    }


    String characters() {
        StringBuffer stringBuffer = new StringBuffer();

        symbols.forEach(symbol -> stringBuffer.append(Encoding.of(symbol)));
        stringBuffer.append(Encoding.of(calculateChecksum()));
        stringBuffer.append(Encoding.STOP_CHAR);

        return stringBuffer.toString();
    }

    private int calculateChecksum() {
        int sum = StreamSupport.withIndex(symbols.stream())
                .mapToInt(element -> Math.max(1, element.index) * element.item.checksumValue()).sum();

        if (LOGGER.isDebugEnabled()) {
            String message = StreamSupport.withIndex(symbols.stream())
                    .map(s -> String.format("%d:%s:%d", s.index, s.item.name(), s.item.checksumValue()))
                    .collect(Collectors.joining(" "));

            LOGGER.debug("checksum of: {}: {} {}", message, sum, sum % 103);
        }

        return sum % 103;
    }

    private static String deriveFromSymbols(List<Symbol> symbols) {
        StringBuffer stringBuffer = new StringBuffer();
        symbols.forEach(symbol -> stringBuffer.append(symbol.encoding()));

        return stringBuffer.toString();
    }

    private static int calculateWeight(List<Symbol> symbols, String value) {
        return value.length() + symbols.stream().mapToInt(Symbol::weight).sum();
    }


    @Override
    public String toString() {
        return MessageFormat.format("Encoding'{'{0}:{1}:{2}'}'",
                weight, currentCodeSet().name(),
                symbols.stream().map(Symbol::toString).collect(Collectors.joining(" ")));
    }

    static EncodedString create(Symbol symbol) {
        return new EncodedString(symbol);
    }

    public Codeset currentCodeSet() {
        return currentCodeSet;
    }

    public int length() {
        return encodedMessage.length();
    }
}
