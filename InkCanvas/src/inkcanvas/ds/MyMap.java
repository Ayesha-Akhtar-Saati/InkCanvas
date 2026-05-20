package inkcanvas.ds;

/**
 * Manual hash-map (separate chaining) — no java.util collections used.
 */
public class MyMap<K, V> {

    private static class Entry<K, V> {
        K key; V value; Entry<K, V> next;
        Entry(K key, V value) { this.key = key; this.value = value; }
    }

    private static final int CAPACITY = 64;
    private final Object[] buckets;
    private int size;

    public MyMap() {
        buckets = new Object[CAPACITY];
        size = 0;
    }

    private int index(K key) {
        int h = key.hashCode() % CAPACITY;
        return h < 0 ? h + CAPACITY : h;
    }

    public void put(K key, V value) {
        int i = index(key);
        @SuppressWarnings("unchecked")
        Entry<K, V> cur = (Entry<K, V>) buckets[i];
        while (cur != null) {
            if (cur.key.equals(key)) { cur.value = value; return; }
            cur = cur.next;
        }
        @SuppressWarnings("unchecked")
        Entry<K, V> head = (Entry<K, V>) buckets[i];
        Entry<K, V> node = new Entry<>(key, value);
        node.next = head;
        buckets[i] = node;
        size++;
    }

    public V get(K key) {
        int i = index(key);
        @SuppressWarnings("unchecked")
        Entry<K, V> cur = (Entry<K, V>) buckets[i];
        while (cur != null) {
            if (cur.key.equals(key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    public boolean containsKey(K key) { return get(key) != null; }

    public void remove(K key) {
        int i = index(key);
        @SuppressWarnings("unchecked")
        Entry<K, V> cur = (Entry<K, V>) buckets[i];
        Entry<K, V> prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) buckets[i] = cur.next;
                else prev.next = cur.next;
                size--;
                return;
            }
            prev = cur; cur = cur.next;
        }
    }

    /** Return all values as MyList */
    public MyList<V> values() {
        MyList<V> list = new MyList<>();
        for (Object bucket : buckets) {
            @SuppressWarnings("unchecked")
            Entry<K, V> cur = (Entry<K, V>) bucket;
            while (cur != null) { list.add(cur.value); cur = cur.next; }
        }
        return list;
    }

    /** Return all keys as MyList */
    public MyList<K> keys() {
        MyList<K> list = new MyList<>();
        for (Object bucket : buckets) {
            @SuppressWarnings("unchecked")
            Entry<K, V> cur = (Entry<K, V>) bucket;
            while (cur != null) { list.add(cur.key); cur = cur.next; }
        }
        return list;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }
}
