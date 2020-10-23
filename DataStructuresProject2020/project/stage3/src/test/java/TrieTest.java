import edu.yu.cs.com1320.project.impl.TrieImpl;
import org.junit.*;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.stage3.impl.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TrieTest {
    TrieImpl<Integer> trie;
    Comparator<Integer> comparator= new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    };


    @Test
    public void putTest(){
        this.trie  = new TrieImpl<>();
        this.trie.put("test", 0);
        this.trie.put("test", 2);
        this.trie.put("test", 3);
        this.trie.put("test", 1);
        this.trie.put("test", 4);
        this.trie.put("testing", 5);
        this.trie.put("tester", 6);
        this.trie.put("ABC", 123);
        this.trie.put("Dovid", 21);
        this.trie.put("Gelbtuch", 23);
    }
    @Test
    public void getAllTest(){
        putTest();
        List<Integer> list = this.trie.getAllSorted("test", this.comparator);
        assertEquals(5, list.size());
        for (int i = 0; i < list.size() ; i++) {
            assertEquals( (Integer) i, list.get(i));
        }
    }
    @Test
    public void getAllPrefixTest(){
        putTest();
        List<Integer> list = this.trie.getAllWithPrefixSorted("t", this.comparator);
        assertEquals(7, list.size());
        for (int i = 0; i < list.size() ; i++) {
            assertEquals( (Integer) i, list.get(i));
        }
    }
    @Test
    public void deleteAllTest(){
        putTest();
        Set<Integer> set = this.trie.deleteAll("test");
        assertEquals(5, set.size());
        for (int i = 0; i < set.size() ; i++) {
            assertTrue(set.contains(i));
        }
        List<Integer> list = this.trie.getAllWithPrefixSorted("t", comparator);
        assertEquals(2, list.size());
        assertEquals((Integer) 5, list.get(0));
        assertEquals((Integer) 6, list.get(1));
    }
    @Test
    public void deleteAllPrefixTest(){
        putTest();
        Set<Integer> set = this.trie.deleteAllWithPrefix("test");
        assertEquals(7, set.size());
        for (int i = 0; i < set.size() ; i++) {
            assertTrue(set.contains(i));
        }
        List<Integer> list = this.trie.getAllWithPrefixSorted("t", comparator);
        assertEquals(0, list.size());
    }
    @Test
    public void deleteTest(){
        putTest();
        this.trie.delete("test", 3);
        List<Integer> list = this.trie.getAllSorted("test", comparator);
        assertEquals(4, list.size());
        for (int i = 0; i < list.size() ; i++) {
            if(i == 3){
                assertFalse(list.contains(3));
                continue;
            }
            assertEquals( (Integer) i, list.get(i));
        }

    }
    @Test
    public void nothing(){
        this.trie = new TrieImpl<>();
        List<Integer> list = this.trie.getAllSorted("hello",comparator);
        List<Integer> listPref = this.trie.getAllWithPrefixSorted("ee",comparator);
        Set<Integer> set = this.trie.deleteAll("nothing");
        Set<Integer> setPref = this.trie.deleteAllWithPrefix("qu");
        assertEquals(0, list.size());
        assertEquals(0, listPref.size());
        assertEquals(0, set.size());
        assertEquals(0, setPref.size());
    }
}
