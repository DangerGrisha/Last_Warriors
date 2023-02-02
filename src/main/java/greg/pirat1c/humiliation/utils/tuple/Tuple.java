package greg.pirat1c.humiliation.utils.tuple;

public class Tuple <L,R> {
    private L left;
    private R right;

    public static final Object EMPTY = new Object();

    private static Object EMPTY_OBJ = new Object();

    private Tuple (L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Tuple<L, R> of (L left, R right) {
        return new Tuple<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
