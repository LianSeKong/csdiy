package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> c;
    public MaxArrayDeque(Comparator<T> cNew) {
        super();
        c = cNew;
    }
    public T max() {
        return max(c);
    }
    public T max(Comparator<T> cNew) {
        if (isEmpty()) {
            return null;
        } else {
            T maxT = get(0);
            for (int i = 1; i < size(); i++) {
                if (cNew.compare(get(i), maxT) > 0) {
                    maxT = get(i);
                }
            }
            return maxT;
        }
    }
}
