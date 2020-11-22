package larsq.barcode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class StreamSupport {
    public static class Index<T> {
        public final int index;
        public final T item;

        private Index(int index, T item) {
            this.index = index;
            this.item = item;
        }
    }

    public static <T> Stream<Index<T>> withIndex(Stream<T> stream) {
        AtomicInteger index = new AtomicInteger(0);

        return stream.map(s -> new Index<>(index.getAndIncrement(), s));
    }
}
