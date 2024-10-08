package deque;
import org.junit.Test;
import static org.junit.Assert.*;
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     *  finally printing the results.
     *
     * */

    public void addIsEmptySizeTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", ad1.isEmpty());
        ad1.addFirst("1");

        assertEquals(1, ad1.size());
        assertFalse("ad1 should now contain 1 item", ad1.isEmpty());

        ad1.addLast("2");
        assertEquals(2, ad1.size());

        System.out.println("Printing out deque: ");
        ad1.printDeque();
    }
    @Test
    public void addTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        for (int i = 0; i < 15; i++) {
            int num = (int) Math.floor(Math.random() * (3 - 1) + 1);
            if (num % 2 == 0) {
                ad.addFirst(i);
            }
            else {
                ad.addLast(i);
            }
        }
        assertEquals("Size are 1000!", 15, ad.size(), 0);
    }
    @Test
    public void addTestMany() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        for (int i = 0; i < 10000; i++) {
            int num = (int) Math.floor(Math.random() * (3 - 1) + 1);
            if (num % 2 == 0) {
                ad.addFirst(i);
            }
            else {
                ad.addLast(i);
            }
        }
        assertEquals("Size are 10000!", 10000, ad.size(), 0);
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("ad1 should be empty upon initialization", ad1.isEmpty());

        ad1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", ad1.isEmpty());

        ad1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", ad1.isEmpty());
    }

    @Test
    /* Adds an item, then get an item.*/
    public void addAndGetLastTest(){
        ArrayDeque<Integer> adi = new ArrayDeque<Integer>();
        adi.addLast((Integer) 3);
        assertEquals((Integer) 3,adi.get(0));

        adi.addLast((Integer) 4);
        assertEquals((Integer) 4,adi.get(1));

        adi.addFirst((Integer) 5);
        assertEquals((Integer) 5,adi.get(0));

        adi.addFirst((Integer) 6);
        assertEquals((Integer) 6,adi.get(0));

        assertEquals(4,adi.size(),0);
        System.out.println("Printing out deque: ");
        adi.printDeque();
    }

    @Test

    public void printDequeTest(){
        ArrayDeque<Integer> adi = new ArrayDeque<Integer>();
        for (int i = 0; i < 4; i++) {
            adi.addLast((Integer) i);
        }
        for (int i = 7; i > 3; i--) {
            adi.addFirst((Integer) i);
        }
        adi.addFirst((Integer) 10);
        System.out.println("Printing out deque: ");
        adi.printDeque();

    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {


        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addFirst(10);

        ad1.removeLast();
        ad1.removeFirst();
        ad1.removeLast();
        ad1.removeFirst();

        int size = ad1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create LinkedListDeques with different parameterized types*/
    public void multipleParamTest() {
        ArrayDeque<String> ad1 = new ArrayDeque<String>();
        ArrayDeque<Double> ad2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> ad3 = new ArrayDeque<Boolean>();
        ad1.addFirst("string");
        ad2.addFirst(3.14159);
        ad3.addFirst(true);

        String s = ad1.removeFirst();
        double d = ad2.removeFirst();
        boolean b = ad3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty LinkedListDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, ad1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, ad1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 100; i++) {
            ad1.addLast(i);
        }

        for (double i = 0; i < 50; i++) {

            assertEquals("Should have the same value", i, (double) ad1.removeFirst(), 0.0);
        }

        for (double i = 99; i > 50; i--) {
            assertEquals("Should have the same value", i, (double) ad1.removeLast(), 0.0);
        }
    }


    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void getTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();

        for (int i = 0; i < 8; i++) {
            ad1.addFirst(i);
        }
        assertEquals("Should have the same value", 7, ad1.get(0), 0);
        assertEquals("Should have the same value", 0, ad1.get(7), 0);
        assertEquals("Should have the same value", null, ad1.get(8));
        for (int i = 0; i < 8; i++) {
            ad1.addLast(i);
        }
        assertEquals("Should have the same value", 7, ad1.get(0), 0);
        assertEquals("Should have the same value", 7, ad1.get(15), 0);
        assertEquals("Should have the same value",4 , ad1.get(12), 0);
        assertEquals("Should have the same value",3 , ad1.get(11), 0);
    }

    @Test
    public void removeTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ad1.addFirst((Integer) 1);
        assertEquals("Should have the same value", 1,  ad1.removeFirst(),0);
        ad1.addLast(99);
        assertEquals("Should have the same value", 99,  ad1.get(0),0);
        ad1.addLast(36);
        assertEquals("Should have the same value", 99,  ad1.get(0),0);
        assertEquals("Should have the same value", 36,  ad1.removeLast(),0);
        ad1.addLast(100);
        assertEquals("Should have the same value", 100,  ad1.get(ad1.size() - 1),0);
        assertEquals("Should have the same value", 2,  ad1.size(),0);
    }
    @Test

    public void iteratorTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 8; i++) {
            ad1.addFirst(i);
        }
        for (Integer i : ad1);
        ArrayDeque<Integer> ad2 = new ArrayDeque<Integer>();
        for (int i = 0; i < 80; i++) {
            ad2.addLast(i);
        }
        for (Integer i : ad2){
            assertEquals("iterator Equals ", i,ad2.get(i));
        };
    }
    @Test
    public void EqualsTest(){
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<Integer>();
        for (int i = 0; i < 50; i++) {
            ad1.addFirst(i);
        }
        for (int i = 0; i < 50; i++) {
            ad2.addFirst(i);
        }
        assertTrue("EqualsTest, ad1 equal ad2",ad1.equals(ad2));
        ad2.addFirst(1);
        assertFalse("EqualsTest, ad1 NOT equal ad2",ad1.equals(ad2));
        ad1.addFirst(1);
        assertTrue("EqualsTest, ad1 equal ad2",ad1.equals(ad2));
    }
    public static void main(String[] args) {
        jh61b.junit.TestRunner.runTests("all", ArrayDequeTest.class);
    }
}
