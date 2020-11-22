package larsq.barcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Code128 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Code128.class);

    public String encode(String value) {
        EncodedString encodedString = encoded(value);

        if (encodedString == null) {
            return null;
        }

        return encodedString.characters();
    }

    private EncodedString encoded(String message) {
        SortedMap<Integer, List<EncodedString>> pathsToExplore = initialPaths();
        Map<String, Set<Symbol>> explored = new LinkedHashMap<>();

        while (!pathsToExplore.isEmpty()) {
            List<EncodedString> cheapestPaths = pathsToExplore.remove(pathsToExplore.firstKey());

            for (EncodedString current : cheapestPaths) {
                ExploreResult result = explore(explored, message, current);

                if (result.encodingComplete()) {
                    LOGGER.debug("encoding found: " + result.complete);
                    return result.complete;
                }
                addInitialPaths(pathsToExplore, result.partialEncodings);
            }

        }
        return null;
    }


    private ExploreResult explore(Map<String, Set<Symbol>> explored, String message, EncodedString encoded) {
        Set<Symbol> alreadyExploredEncodings = explored.computeIfAbsent(generateKey(encoded), __ -> new LinkedHashSet<>());
        Set<Symbol> nextSymbols = extractNextSymbols(message, encoded, alreadyExploredEncodings);

        List<EncodedString> partialResults = new LinkedList<>();
        for (Symbol nextSymbol : nextSymbols) {
            EncodedString updated = encoded.withSymbol(nextSymbol);

            if (updated.encodedMessage().equals(message)) {
                return ExploreResult.complete(updated);
            }

            partialResults.add(updated);
        }

        alreadyExploredEncodings.addAll(nextSymbols);
        return ExploreResult.intermediaryResult(partialResults);
    }

    private static String generateKey(EncodedString encoded) {
        return encoded.currentCodeSet().name() + "::" + encoded.encodedMessage();
    }

    private static Set<Symbol> extractNextSymbols(String message, EncodedString encoded, Set<Symbol> alreadyExplored) {
        Set<Symbol> nextSymbols = new LinkedHashSet<>();

        if (message.length() - encoded.length() >= encoded.currentCodeSet().length) {
            int start = encoded.encodedMessage().length();
            int stop = start + encoded.currentCodeSet().length;

            Symbol nextSymbol = Symbol.fromCodeset(message.substring(start, stop), encoded.currentCodeSet());

            Optional.ofNullable(nextSymbol).ifPresent(nextSymbols::add);
        }

        if (!encoded.lastSymbol().isSwitchSymbol()) {
            nextSymbols.addAll(SymbolFactory.switchSymbolsOf(encoded.currentCodeSet()));
        }

        nextSymbols.removeAll(alreadyExplored);

        return nextSymbols;
    }

    private static class ExploreResult {
        final EncodedString complete;
        final List<EncodedString> partialEncodings;

        private ExploreResult(EncodedString complete, List<EncodedString> partialEncodings) {
            this.complete = complete;
            this.partialEncodings = partialEncodings;
        }

        boolean encodingComplete() {
            return complete != null;
        }

        static ExploreResult complete(EncodedString encodedString) {
            return new ExploreResult(encodedString, null);
        }

        static ExploreResult intermediaryResult(List<EncodedString> partialEncodings) {
            return new ExploreResult(null, partialEncodings);
        }
    }

    private static SortedMap<Integer, List<EncodedString>> initialPaths() {
        SortedMap<Integer, List<EncodedString>> pathsToExplore = new TreeMap<>();
        addInitialPaths(pathsToExplore, Arrays.asList(
                EncodedString.create(SymbolFactory.START_SYMBOL_B),
                EncodedString.create(SymbolFactory.START_SYMBOL_A),
                EncodedString.create(SymbolFactory.START_SYMBOL_C)));
        return pathsToExplore;
    }

    private static void addInitialPaths(SortedMap<Integer, List<EncodedString>> weights, List<EncodedString> encodedStrings) {
        for (EncodedString encodedString : encodedStrings) {
            List<EncodedString> sameWeight = weights.computeIfAbsent(encodedString.weight(), __ -> new ArrayList<>());
            sameWeight.add(encodedString);
        }
    }


}
