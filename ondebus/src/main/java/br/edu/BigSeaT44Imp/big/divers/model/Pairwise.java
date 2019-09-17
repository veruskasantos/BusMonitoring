package br.edu.BigSeaT44Imp.big.divers.model;

public class Pairwise<T1, T2> {

    public final T1 left;
    public final T2 right;

    public Pairwise(T1 left, T2 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public final int hashCode() {
        int hashCode = 31 + (left == null ? 0 : left.hashCode());
        return 31 * hashCode + (right == null ? 0 : right.hashCode());
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Pairwise)) {
            return false;
        }
        Pairwise<?, ?> that = (Pairwise<?, ?>) o;
        // handles nulls properly
        return (equal(left, that.left) && equal(right, that.right)) || (equal(left, that.right) && equal(right, that.left));
    }

    // From Apache Licensed guava:
    private boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }


    @Override
    public String toString() {
        return "(" + left + "," + right + ")";
    }

    public static <X, Y> Pairwise<X, Y> create(X x, Y y) {
        return new Pairwise<X, Y>(x, y);
    }
}
