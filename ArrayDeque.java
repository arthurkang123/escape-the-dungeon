package byog.Core;

public class ArrayDeque<T> {
    private T[] itemArray;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        itemArray = (T[]) new Object[8];
        nextFirst = 3; //Arbitrary number to begin with
        nextLast = 4; //+1 of nextFirst
        size = 0;
    }

    /* Testing for myself to use Java Visualizer
    public static void main(String[] args) {
        ArrayDeque test = new ArrayDeque();
        test.addFirst(3);
        test.addFirst(2);
        test.addFirst(1);
        test.addFirst(0);
        test.addLast(4);
        test.addLast(5);
        test.addLast(6);
        test.addLast(7);
        test.addLast(8);
        test.addFirst(9);
        test.addLast(10);
        test.addLast(11);
        test.addFirst(12);
        test.addFirst(13);
        test.addLast(14);
        test.addFirst(15);
        test.addLast(16);
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeLast();
        test.removeFirst();
        test.removeFirst();
        test.addFirst(3);
        test.addFirst(2);
        test.addLast(4);
        test.addFirst(1);
        test.addFirst(0);
        test.addLast(4);
        test.addLast(5);
        test.addLast(6);
        test.addLast(7);
        test.addLast(8);
        test.addFirst(9);
        test.addLast(10);
        test.addLast(11);
        test.addFirst(12);
        test.addFirst(13);
        test.addLast(14);
        test.addFirst(15);
        test.addLast(16);
        test.size();
        test.printDeque();
    }
    */

    private void resize(int capacity) {
        T[] larger = (T[]) new Object[capacity];
        System.arraycopy(itemArray, 0, larger, 0, nextFirst + 1);
        int secondstartpoint = capacity - (itemArray.length - 1 - nextFirst);
        int secondpart = (itemArray.length - 1 - nextFirst);
        System.arraycopy(itemArray, nextLast, larger, secondstartpoint, secondpart);
        nextLast = nextFirst + 1;
        nextFirst = secondstartpoint - 1;
        itemArray = larger;

    }

    public void addFirst(T item) {
        if (size == itemArray.length) {
            resize(size * 2);
        }
        itemArray[nextFirst] = item;
        nextFirst = Math.floorMod(nextFirst - 1, itemArray.length);
        //Found Java version of Modulo on Stack Overflow as % in Java means remainder
        size += 1;
    }

    public void addLast(T item) {
        if (size == itemArray.length) {
            resize(size * 2);
        }
        itemArray[nextLast] = item;
        nextLast = Math.floorMod(nextLast + 1, itemArray.length);
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (T item : itemArray) {
            System.out.print(item + " ");
        }
    }

    private void resizedown() {
        float R = (size / (float) itemArray.length);
        //Searched how to get float after division on Stack Overflow
        if ((R < 0.25) && (this.itemArray.length >= 18)) {
            int capacity = (itemArray.length / 2);
            T[] smaller = (T[]) new Object[capacity];

            if (Math.floorMod(nextFirst + 1, itemArray.length) == 0) {
                System.arraycopy(itemArray, 0, smaller, 0, size);
                itemArray = smaller;
                nextFirst = itemArray.length - 1;
                nextLast = size;
            } else if (itemArray[0] == null) {
                System.arraycopy(itemArray, nextFirst + 1, smaller, 0, size);
                itemArray = smaller;
                nextFirst = itemArray.length - 1;
                nextLast = size;
            } else {
                System.arraycopy(itemArray, 0, smaller, 0, nextLast);
                int secondstartpoint = smaller.length - (itemArray.length - 1 - nextFirst);
                int secondpart = (itemArray.length - 1 - nextFirst);
                System.arraycopy(itemArray, nextFirst + 1, smaller, secondstartpoint, secondpart);
                nextFirst = secondstartpoint - 1;
                itemArray = smaller;
            }
        }
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        } else {
            resizedown();
            nextFirst = Math.floorMod(nextFirst + 1, itemArray.length);
            T temp = itemArray[nextFirst];
            itemArray[nextFirst] = null;
            size -= 1;
            return temp;
        }
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        } else {
            resizedown();
            nextLast = Math.floorMod(nextLast - 1, itemArray.length);
            T temp = itemArray[nextLast];
            itemArray[nextLast] = null;
            size -= 1;
            return temp;
        }
    }

    public T get(int index) {
        return itemArray[Math.floorMod(nextFirst + 1 + index, itemArray.length)];
    }
}
