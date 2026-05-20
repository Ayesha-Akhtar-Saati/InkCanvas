package inkcanvas.ds;
/**
 * Manual singly-linked list — no java.util collections used.
 */
public class MyList<T> {

    private static class Node<T> {
        T data;
        Node<T> next;
        Node(T data) { this.data = data; }
    }

    private Node<T> head;
    private int size;

    public MyList() { head = null; size = 0; }

    /** Append to end */
    public void add(T item) {
        Node<T> node = new Node<>(item);
        if (head == null) { head = node; }
        else {
            Node<T> cur = head;
            while (cur.next != null) cur = cur.next;
            cur.next = node;
        }
        size++;
    }

    /** Insert at front */
    public void addFirst(T item) {
        Node<T> node = new Node<>(item);
        node.next = head;
        head = node;
        size++;
    }

    /** Get by index */
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        Node<T> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur.data;
    }

    /** Remove by index */
    public void remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        if (index == 0) { head = head.next; size--; return; }
        Node<T> cur = head;
        for (int i = 0; i < index - 1; i++) cur = cur.next;
        cur.next = cur.next.next;
        size--;
    }

    /** Remove first occurrence matching object via equals */
    public boolean removeItem(T item) {
        if (head == null) return false;
        if (head.data.equals(item)) { head = head.next; size--; return true; }
        Node<T> cur = head;
        while (cur.next != null) {
            if (cur.next.data.equals(item)) { cur.next = cur.next.next; size--; return true; }
            cur = cur.next;
        }
        return false;
    }

    public boolean contains(T item) {
        Node<T> cur = head;
        while (cur != null) {
            if (cur.data.equals(item)) return true;
            cur = cur.next;
        }
        return false;
    }

    public int size()    { return size; }
    public boolean isEmpty() { return size == 0; }

    public void clear() { head = null; size = 0; }

    /** Convert to plain array for use with JTable / JList */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] arr = new Object[size];
        Node<T> cur = head;
        for (int i = 0; i < size; i++) { arr[i] = cur.data; cur = cur.next; }
        return (T[]) arr;
    }
}



