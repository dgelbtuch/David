package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl <E extends Comparable> extends MinHeap <E> {


    public MinHeapImpl(){
        this.elements = (E[]) new Comparable[5];
        this.count = 0;
        this.elementsToArrayIndex = new HashMap<>();
    }

    @Override
    public void reHeapify(E element) {

        if(getArrayIndex(element) == 1){
            if(this.count == 1){
                return;
            }
            this.downHeap(getArrayIndex(element));
            return;
        }
        if(this.isGreater(getArrayIndex(element)/2, getArrayIndex(element))){
            this.upHeap(getArrayIndex(element));
        }
        else {
            this.downHeap(getArrayIndex(element));
        }

    }

    @Override
    protected int getArrayIndex(E element) {
        return this.elementsToArrayIndex.get(element);
    }

    @Override
    protected void doubleArraySize() {
        E[] newArray = (E[]) new Comparable [this.elements.length * 2];
        for (int i = 0; i < this.elements.length; i++) {
            newArray[i] = this.elements[i];
        }
        this.elements = newArray;
    }

    @Override
    protected void swap(int i, int j) {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        this.elementsToArrayIndex.put(this.elements[i], i);
        this.elementsToArrayIndex.put(this.elements[j], j);
    }

    @Override
    protected void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k))
        {
            this.swap(k, k / 2);
            k = k / 2;
        }

    }

    @Override
    protected void downHeap(int k) {
        while (2 * k <= this.count)
        {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1))
            {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j))
            {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }

    @Override
    public void insert(E x) {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1)
        {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        this.elementsToArrayIndex.put(x, this.count);
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }

    @Override
    public E removeMin() {
        if (isEmpty())
        {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elementsToArrayIndex.remove(this.elements[this.count + 1]);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        return min;
    }
    /*
   MY METHODS/////////////////////////////////////////////////////
    */
}
