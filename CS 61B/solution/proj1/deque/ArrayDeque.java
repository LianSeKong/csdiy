package deque;

import java.util.Iterator;
import static java.lang.Math.floorMod;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private static final int CAPACITY = 2;
    private static final double ZOOM_FACTOR = 0.25;
    private static final int INITIAL_SIZE = 8;
    private T[] items;
    private int length;
    private int front;
    private int back;

    public ArrayDeque() {
        this.length = 0;
        this.items = (T[]) new Object[INITIAL_SIZE];
        front = items.length / 2;
        back = front + 1;
    }

    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];  // Create a new size items.
        int newFront = capacity / 4;   // new item's begin index
        for (int i = 0; i < this.size(); i++) {
            newItems[newFront + i] = get(i);  // Copy elements of old items
        }
        this.items = newItems;
        this.front = floorMod(newFront - 1, newItems.length);
        this.back = newFront + this.length;
    }


    public void addFirst(T item) {
        if (this.length == this.items.length) {
            resize(this.items.length * CAPACITY);
        }
        this.length += 1;
        this.items[this.front] = item;
        this.front = floorMod(this.front - 1, this.items.length);
    }

    public void addLast(T item) {
        if (this.length == this.items.length) {
            resize(this.items.length * CAPACITY);
        }
        this.length += 1;
        this.items[this.back] = item;
        this.back = floorMod(this.back + 1, this.items.length);
    }

    private void reducedCapacity() {
        int capacity = (int) (this.items.length * ZOOM_FACTOR);
        boolean flag = this.items.length >= 16 && this.length <= capacity;
        if (flag) {
            resize(capacity * 2);
        }
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        reducedCapacity();
        this.length -= 1;
        int index = floorMod(this.front + 1, this.items.length);
        T returnVal = this.items[index];
        this.items[index] = null; // 不再引用，利于垃圾回收
        this.front = index;
        return returnVal;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        reducedCapacity();
        this.length -= 1;
        int index = floorMod(this.back - 1, this.items.length);
        T returnVal = this.items[index];
        this.items[index] = null;
        this.back = index;
        return returnVal;
    }

    public T get(int index) {
        if (index < 0 || index >= this.length) {
            return null;
        }
        int firstIndex = floorMod(this.front + 1, this.items.length);
        int targetIndex = floorMod(firstIndex + index, this.items.length);
        return this.items[targetIndex];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int index;

        ArrayDequeIterator() {
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return this.index < length;
        }

        public T next() {
            this.index += 1;
            return get(this.index - 1);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Deque)) {
            return false;
        }
        Deque<T> dq = (Deque<T>) obj;
        if (dq.size() != this.size()) {
            return false;
        }

        for (int i = 0; i < dq.size(); i++) {
            if (!dq.get(i).equals(this.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < this.length; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    public int size() {
        return this.length;
    }
}
