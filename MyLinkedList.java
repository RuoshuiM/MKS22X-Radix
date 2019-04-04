import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Doubly-linked implementation of a list and a deque. Provides minimalist
 * support for radix sort operations.
 *
 * <p>
 * The only extra operation this list supports over {@link java.util.LinkedList}
 * is that it supports an extend method to quickly link two lists together.
 * </p>
 *
 * @author ruosh
 *
 * @param <E> the type of elements held in this collection
 */
public class MyLinkedList<E> implements Iterable<E> {
    /** size of the list */
    private int size = 0;

    /** used for attempt to prevent concurrent modification on iteration */
    private int modCount = 0;

    /**
     * Pointer to the fist node;
     */
    Node<E> first;

    /**
     * Pointer to the last node;
     */
    Node<E> last;

    /**
     * Makes an empty list
     */
    public MyLinkedList() {
    }

    /**
     * Copy the references to the other LinkedList to this
     * 
     * @param orig
     */
    public void from(MyLinkedList<E> other) {
        first = other.first;
        last = other.last;
        size = other.size;
    }

    public MyLinkedList(Collection<? extends E> c) {
        this();
        for (E e : c) {
            this.push(e);
        }
    }

    /**
     * @return the size of the list
     */
    public int size() {
        return size;
    }

    private void checkIndex(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Add an element to the end of the list
     *
     * @param ele element to be added
     */
    public void addLast(E ele) {
        if (last == null) {
            first = last = new Node<E>(null, ele, null);
        } else {
            Node<E> l = last;
            last = new Node<E>(last, ele, null);
            l.next = last;
        }
        modCount++;
        size++;
    }

    /**
     * Add an element to the front of the list
     *
     * @param ele element to be added
     */
    public void addFirst(E ele) {
        if (first == null) {
            first = last = new Node<E>(null, ele, null);
        } else {
            Node<E> f = first;
            first = new Node<E>(null, ele, first);
            f.prev = first;
        }
        modCount++;
        size++;
    }

    /**
     * Removes the first element from the list and return it
     *
     * @return the first element
     * @throws NoSuchElementException if list has no more elements
     */
    public E removeFirst() {
        if (first == null) {
            throw new NoSuchElementException();
        }
        Node<E> f = first;
        first = first.next;
        f.next = null; // help GC
        modCount++;
        size--;
        return f.item;
    }

    /**
     * Removes the last element from the list and return it
     *
     * @return the last element
     * @throws NoSuchElementException if list has no more elements
     */
    public E removeLast() {
        if (last == null) {
            throw new NoSuchElementException();
        }
        Node<E> l = last;
        last = last.prev;
        l.prev = null; // help GC
        modCount++;
        size--;
        return l.item;
    }

    /**
     * Reset the list to an empty state, breaking link between each node
     */
    public void clear() {
        Node<E> x = first;
        while (x != null) {
            Node<E> f = x;
            first = first.next;
            f.prev = f.next = null;
            f.item = null;
        }

        first = last = null;
        size = 0;
        modCount++;
    }

    /**
     * Reset the list to an empty state by removing references only
     */
    public void minimalClear() {
        first = last = null;
        size = 0;
        modCount++;
    }

    /**
     * Extend current list in constant time. The method merges the linked lists by
     * linking the list in the argument to the back of the current list.
     *
     * @param other The other list
     */
    public void extend(MyLinkedList<E> other) {
        if (other.size() == 0) {
            return;
        } else if (this.size() == 0) {
             first = other.first;
             last = other.last;
             size = other.size;
        } else {
            last.next = other.first;
            other.first.prev = last;
            size += other.size();
            last = other.last;
        }
        modCount++;
    }

    public Object[] toArray() {
        Object[] results = new Object[size];

        int i = 0;
        for (Node<E> p = first; p != null; p = p.next) {
            results[i++] = p.item;
        }

        return results;
    }

    /*
     * Stack operations
     */

    /**
     * Same as {@link addLast}
     *
     * @param ele element to be added
     */
    public void push(E ele) {
        addLast(ele);
    }

    /**
     * Same as {@link removeLast}
     *
     * @return element removed
     */
    public E pop() {
        return removeLast();
    }

    @Override
    public String toString() {
        Iterator<E> iter = iterator();
        if (!iter.hasNext()) {
            return "[]";
        }

        StringBuilder s = new StringBuilder();
        s.append('[');
        s.append(iter.next());

        while (iter.hasNext()) {
            s.append(',').append(' ');
            s.append(iter.next());
        }

        return s.append(']').toString();

    }

    Node<E> node(int index) {
        checkIndex(index);
        Node<E> cur;

        if (index < (size >> 1)) {
            // when index is in the first half
            cur = first;
            for (int i = 0; i < index; i++) {
                cur = cur.next;
            }
        } else {
            cur = last;
            for (int i = size - 1; i > index; i--) {
                cur = cur.prev;
            }
        }

        return cur;
    }

    private static class Node<E> {
        E item;
        Node<E> prev;
        Node<E> next;

        Node(Node<E> prev, E item, Node<E> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * MyLinkedList's general ListIterator. Does not implement the modification
     * methods.
     */
    class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
            if (index != 0) checkIndex(index);
            nextIndex = index;
            next = (index == size) ? null : node(index);
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkForComodification();
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;

            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        private void checkForComodification() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }

    }
    
    /**
     * This iterator iterates through the list by removing first element to free up memory.
     * Do not modify the list while iterating through it.
     * @author ruosh
     *
     */
    class removeItr implements Iterator<E> {
        
        Node<E> next;
        int index;
        
        public removeItr() {
            next = first;
        }
        
        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            Node<E> cur = next;
            E val = cur.item;
            next = next.next;
            cur.prev = null;
            cur.item = null;
            cur.next = null;
            return val;
        }
        
    }

    @Override
    public Iterator<E> iterator() {
        return new ListItr(0);
//        return new removeItr();
    }
    
    

    public static void main(String[] args) {
        MyLinkedList<Integer> ints = new MyLinkedList<>(Arrays.asList(1, 4, 5, 6, 6, 7));
        MyLinkedList<Integer> ints2 = new MyLinkedList<>(Arrays.asList(6, 7, 8, 9, 10));

        System.out.println(ints);
        System.out.println(ints2);
        ints.extend(ints2);
        System.out.println(ints);
    }
}
