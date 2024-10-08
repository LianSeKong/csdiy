package deque;
import org.junit.Test;

import static org.junit.Assert.*;



/** Performs some basic linked list tests. */
public class LinkedListDequeTest {

    @Test
    public void myTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
       // assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst(9);

        assertEquals(9, (int) lld1.removeLast());
        lld1.addFirst(3);
        assertEquals(3, (int) lld1.removeLast());
        lld1.printDeque();
    }

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

		assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
		lld1.addFirst("front");

		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

		lld1.addLast("middle");
		assertEquals(2, lld1.size());

		lld1.addLast("back");
		assertEquals(3, lld1.size());

		System.out.println("Printing out deque: ");
		lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
		assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

		lld1.addFirst(10);
		assertFalse("lld1 should contain 1 item", lld1.isEmpty());
		lld1.removeFirst();
		assertTrue("lld1 should be empty after removal", lld1.isEmpty());

        lld1.addFirst(12);
        lld1.removeLast();
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());

        lld1.addLast(13);
        lld1.removeLast();
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());

        lld1.addLast(14);
        lld1.removeFirst();
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {

        LinkedListDeque<String>  lld1 = new LinkedListDeque<String>();
        LinkedListDeque<Double>  lld2 = new LinkedListDeque<Double>();
        LinkedListDeque<Boolean> lld3 = new LinkedListDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

        System.out.println(s);
        System.out.println(b);
        System.out.println(d);

        lld1.addLast("string");
        lld2.addLast(3.14159);
        lld3.addLast(true);

         s = lld1.removeLast();
         d = lld2.removeLast();
         b = lld3.removeLast();

        System.out.println(s);
        System.out.println(b);
        System.out.println(d);

    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        assertNull("Should return null when removeFirst is called on an empty Deque,", lld1.removeFirst());
        assertNull("Should return null when removeLast is called on an empty Deque,", lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest2() {

        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }
        for (int i = 0; i < 1000000; i++) {
            lld1.addFirst(i);
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    public void iteratorTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 1000; i++) {
            lld1.addLast(i);
        }
        int i = 0;
        for (int item : lld1){
            assertEquals("Should have the same value", i, item, 0);
            i += 1;
        }
    }

    @Test
    public void equalsTest(){
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        LinkedListDeque<Integer> lld2 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 5; i++) {
            lld1.addLast(i);
            lld2.addLast(i);
        }
        assertEquals("size and value equals", lld1, lld2);
        for (int i = 0; i < 5; i++) {
            lld1.addLast(i);
        }
        assertNotEquals("size not equals", lld1, lld2);

        for (int i = 5; i < 10; i++) {
            lld2.addLast(i);
        }
        assertNotEquals("size  equals but value not equals", lld1, lld2);
    }

    @Test
    public void getRecTest() {
        LinkedListDeque<Integer> lld1 = new LinkedListDeque<Integer>();
        for (int i = 0; i < 500; i++) {
            lld1.addLast(i);
        }
        for (int i = 0; i < 500; i++) {
            assertEquals("Should have the same value", i, lld1.getRecursive(i), 0);
        }
    }
    public static void main(String[] args) {
        jh61b.junit.TestRunner.runTests("all", LinkedListDequeTest.class);
    }
}
