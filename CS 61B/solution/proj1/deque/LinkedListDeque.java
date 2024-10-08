package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    private final Node<T> sentinel; // 哨兵
    private int length;          // Caching Size

    public LinkedListDeque() {
        // 循环哨兵节点：prev指向列表的尾节点, next指向列表的头节点
        this.sentinel = new Node<>(null, null, null);
        this.sentinel.prev = this.sentinel;
        this.sentinel.next = this.sentinel;
        this.length = 0;
    }

    public void addFirst(T item) {
        this.length += 1;
        // prev为当前列表的尾节点, next为当前列表的头节点
        Node<T> p = new Node<>(item, null, null);
        p.prev = this.sentinel;
        p.next = this.sentinel.next;
        // p节点为当前列表的头节点
        this.sentinel.next.prev = p;
        this.sentinel.next = p;
    }

    public void addLast(T item) {
        this.length += 1;
        Node<T> p = new Node<>(item, null, null); // prev为当前列表的尾节点, next为当前列表的头节点
        p.next = this.sentinel;  // p节点为当前列表的尾节点
        p.prev = this.sentinel.prev;
        this.sentinel.prev.next = p;
        this.sentinel.prev = p;
    }

    public T removeFirst() {
        if (this.length == 0) {
            return null;
        }
        this.length -= 1;
        Node<T> firstNode = this.sentinel.next;
        firstNode.next.prev = this.sentinel;
        this.sentinel.next = firstNode.next;
        return firstNode.item;
    }

    public T removeLast() {
        if (this.length == 0) {
            return null;
        }
        this.length -= 1;
        Node<T> lastNode = this.sentinel.prev;
        lastNode.prev.next = this.sentinel;
        this.sentinel.prev = lastNode.prev;
        return lastNode.item;
    }

    @Override
    public T get(int index) {
        if (this.length == 0 || index < 0 || index >= this.length) {
            return null;
        }
        Node<T> node = this.sentinel.next;
        while (index > 0) {
            node = node.next;
            index -= 1;
        }
        return node.item;
    }

    private T getRecursiveHelper(Node<T> p, int index) {
        if (index == 0) {
            return p.item;
        }
        return getRecursiveHelper(p.next, index - 1);
    }

    public T getRecursive(int index) {
        if (isEmpty()) {
            return null;
        }
        return getRecursiveHelper(this.sentinel.next, index);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> lld = (Deque<T>) o;
        if (lld.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!lld.get(i).equals(get(i))) {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return this.length;
    }

    @Override
    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        Node<T> p = this.sentinel.next;
        for (int i = 0; i < this.length; i++) {
            sb.append(p.item);
            sb.append(" ");
            p = p.next;
        }
        System.out.println(sb);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        private Node<T> nodes;
        private int index;

        LinkedListDequeIterator() {
            nodes = sentinel.next;
            index = 0;
        }

        public boolean hasNext() {
            return index < length;
        }

        public T next() {
            T item = nodes.item;
            nodes = nodes.next;
            index += 1;
            return item;
        }
    }

    public static class Node<T> {
        private T item;
        private Node<T> next;
        private Node<T> prev;

        public Node(T item, Node<T> prev, Node<T> next) {
            this.next = next;
            this.prev = prev;
            this.item = item;
        }
    }
}
