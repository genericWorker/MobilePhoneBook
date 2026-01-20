package edu.dccc.mobilephonebook;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DoublyLinkedList<E> implements Iterable<E> {
    private Node<E> head;
    private Node<E> tail;
    private int size = 0;

    private static class Node<E> {
        E data;
        Node<E> next;
        Node<E> prev;

        Node(E data) { this.data = data; }
    }

    // Standard Add (to the tail)
    public void add(E element) {
        Node<E> newNode = new Node<>(element);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    // NEW: Remove method for your Delete button
    public boolean remove(E element) {
        Node<E> current = head;
        while (current != null) {
            if (current.data.equals(element)) {
                if (current.prev != null) current.prev.next = current.next;
                else head = current.next;

                if (current.next != null) current.next.prev = current.prev;
                else tail = current.prev;

                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // FORWARD ITERATOR (Default)
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> current = head;
            @Override
            public boolean hasNext() { return current != null; }
            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                E data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    // NEW: BACKWARD ITERATOR (The "Tail First" Proof)
    public Iterable<E> backwards() {
        return () -> new Iterator<E>() {
            private Node<E> current = tail; // Starts at the Tail!
            @Override
            public boolean hasNext() { return current != null; }
            @Override
            public E next() {
                if (!hasNext()) throw new NoSuchElementException();
                E data = current.data;
                current = current.prev; // Moves backward!
                return data;
            }
        };
    }

    public boolean isEmpty() {
        return head == null;
        // or return size == 0; if you maintain a size variable
    }

    public int size() { return size; }
}