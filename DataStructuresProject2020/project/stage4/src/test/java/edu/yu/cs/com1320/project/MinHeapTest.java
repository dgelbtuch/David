package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage4.impl.Utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static edu.yu.cs.com1320.project.stage4.impl.Utils.textToPdfData;
import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;

public class MinHeapTest {
    MinHeapImpl<Integer> heap = new MinHeapImpl<>();
    MinHeap<MyClass> myHeap = new MinHeapImpl<>();
    public class MyClass implements Comparable<MyClass>{
        public int value;
        public String name;
        public MyClass(int value){
            this.value = value;
            this.name = "" + value;
        }
        public void changeValue(int i){
            this.value = i;
        }
        @Override
        public int compareTo(MyClass o) {
            return this.value - o.value;
        }
    }

    @Test
    public void insert(){
        for (int i = 0; i < 4; i++) {
            this.heap.insert(i);
        }
        assertEquals(4, this.heap.count);
    }
    @Test
    public void doubleArray(){
        for (int i = 0; i < 100; i++) {
            this.heap.insert(i);
        }
        assertEquals(100, this.heap.count);
    }
    @Test
    public void removeMinTest(){
        for (int i = 0; i < 10; i++) {
            this.heap.insert(i);
        }
        assertEquals((Integer) 0, this.heap.removeMin());
    }
    @Test
    public void addSmallTest(){
        for (int i = 1; i < 10; i++) {
            this.heap.insert(i);
        }
        this.heap.insert(0);
        assertEquals((Integer) 0, this.heap.removeMin());
    }
    @Test
    public void reheapifyTest(){
        MyClass obj1 = new MyClass(1);
        MyClass obj2 = new MyClass(2);
        MyClass obj3 = new MyClass(3);
        MyClass obj4 = new MyClass(4);
        this.myHeap.insert(obj1);
        this.myHeap.insert(obj2);
        this.myHeap.insert(obj3);
        this.myHeap.insert(obj4);
        obj4.changeValue(0);
        this.myHeap.reHeapify(obj4);
        assertEquals(obj4, this.myHeap.removeMin());
    }


}
