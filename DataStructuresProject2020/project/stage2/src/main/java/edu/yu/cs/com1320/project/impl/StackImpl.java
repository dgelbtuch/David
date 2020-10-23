package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {
    private LinkedList<T> list;
    private int stackLength;

    public StackImpl(){
        this.stackLength = 0;
        this.list = new LinkedList<>();
    }

    public void push(T element) {
        list.add(element);
        this.stackLength ++;
    }


    public T pop() {
        T popped = list.remove();
        this.stackLength --;
        return popped;
    }


    public T peek() {
        return this.list.head.data;
    }


    public int size() {
        return this.stackLength;
    }

    private class Node<T> {
        private T data;
        private Node<T> next;
    }
    private class LinkedList<T> {
        private T data;
        private Node<T> head;
        private int length = 0;

        private LinkedList() {
            this.head = null;
        }

        private void add(T value) {
            Node<T> object = new Node<T>();
            object.data = value;
            if(this.head == null){
                this.head = object;
                this.head.next = null;
            }else {
                object.next = this.head;
                this.head = object;
                length++;
            }
        }
        private T remove(){
            if(this.head == null){
                return null;
            }
            T object = this.head.data;
            this.head = this.head.next;
            length --;
            return object;
        }
    }
}
