package es.upm.fi.cig.multictbnc.data.representation;

import java.util.LinkedList;

/**
 * Represents a sliding window data structure. It is a fixed-size window that slides over a sequence of elements,
 * typically used for processing streams of data. The window adds new elements to the end and removes the oldest
 * elements to maintain a constant size.
 *
 * @param <T> the type of elements held in this sliding window
 */
public class SlidingWindow<T> extends LinkedList<T> {
    /**
     * Fixed size of the sliding window.
     */
    private final int size;

    /**
     * Constructs a sliding window with the specified size.
     *
     * @param size fixed size of the sliding window
     */
    public SlidingWindow(int size) {
        this.size = size;
    }

    /**
     * Adds a new object to the sliding window. If the window reaches its maximum size, the oldest element is removed.
     *
     * @param object object to be added to the window
     * @return {@code true} if the object is added, {@code false} otherwise
     */
    public boolean add(T object) {
        if (size > 0) {
            addLast(object);
            if (size() > size) {
                removeFirst();
            }
            return true;
        }
        return false;
    }
}
