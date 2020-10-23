import edu.yu.cs.com1320.project.impl.BTreeImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class BTreeTest {
    @Test
    public void putAndDeleteTest(){
        BTreeImpl<Integer, String> tree = new BTreeImpl<>();

        assertEquals(null,tree.put(1, "1"));
        assertEquals(null,tree.put(2, "2"));
        assertEquals(null,tree.put(3, "3"));

        assertEquals("1", tree.get(1));
        assertEquals("2", tree.get(2));
        assertEquals("3", tree.get(3));

        tree.put(2, null);
        assertEquals("1", tree.get(1));
        assertEquals(null, tree.get(2));
        assertEquals("3", tree.get(3));

    }
    @Test
    public void overwriteTest(){
        BTreeImpl<Integer, String> tree = new BTreeImpl<>();

        assertNull(tree.put(1, "1"));
        assertEquals("1", tree.put(1, "2"));

    }
}
