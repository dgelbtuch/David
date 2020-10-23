package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MinHeapImplTest {
    MinHeapImpl<Integer> heap = new MinHeapImpl<>();

    @Test
    public void indexTest(){
        for (int i = 0; i < 10; i++) {
            this.heap.insert(i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(i+1, this.heap.getArrayIndex(i));
        }
    }
    @Test
    public void indexTest2(){
        for (int i = 10; i > 5; i--) {
            this.heap.insert(i);
        }
        assertEquals(1, this.heap.getArrayIndex(6));
        assertEquals(2, this.heap.getArrayIndex(7));
        assertEquals(3, this.heap.getArrayIndex(9));
        assertEquals(4, this.heap.getArrayIndex(10));
        assertEquals(5, this.heap.getArrayIndex(8));

    }
}
