package paser;

public class Mypair<U, V> {
    public final U first;       // the first field of a pair
    public final V second;      // the second field of a pair

    // Constructs a new pair with specified values
    private Mypair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    @Override
    public int hashCode() { return 37 * first.hashCode() + second.hashCode(); }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Mypair<?, ?> pair = (Mypair<?, ?>) o;
        if (!first.equals(pair.first)) { return false; }
        return second.equals(pair.second);
    }

    public static <U, V> Mypair <U, V> of(U a, V b) {
        return new Mypair<>(a, b);
    }

}
