package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl <Key, Value> implements HashTable<Key, Value> {
    private Key key;
    private Value value;
    private LinkedList<HashTableImpl<Key, Value>>[] array = new LinkedList[5];

    public HashTableImpl() {
        for (int i = 0; i < this.array.length; i++) {
            array[i] = new LinkedList<HashTableImpl<Key, Value>>();
        }
    }

    public Value get(Key k) {
        int hCode = k.hashCode();
        int index = hashFunction(hCode);
        LinkedList<HashTableImpl<Key, Value>> list = this.array[index];
        Node<HashTableImpl<Key, Value>> current = list.head;
        while (current != null) {
            if (current.data.key.hashCode() == k.hashCode()) {
                return current.data.value;
            }
            current = current.next;
        }
        return null;
    }

    public Value put(Key k, Value v) {
        if(v == null){
            delete(k);
            return null;
        }
        int index = hashFunction(k.hashCode());
        LinkedList<HashTableImpl<Key, Value>> list = this.array[index];
        HashTableImpl<Key, Value> object = new HashTableImpl<Key, Value>();
        object.key = k;
        object.value = v;
        Node<HashTableImpl<Key, Value>> current = list.head;

        while (current != null) {
            if (current.data.key.hashCode() == k.hashCode()) {
                Value old = current.data.value;
                if(old.equals(v)){return old;}
                current.data.value = v;
                return old;
            }
            current = current.next;
        }
        list.add(object);
        if(getAverageListSize() >= 4){
            LinkedList<HashTableImpl<Key, Value>>[] oldArray= this.array;
            arrayDouble();
            reHash(oldArray);
            return null;
        }
        return null;
    }

    private void reHash(LinkedList<HashTableImpl<Key, Value>>[] oldArray) {
        for (int i = 0; i < oldArray.length; i++) {
            LinkedList<HashTableImpl<Key, Value>> list = oldArray[i];
            Node<HashTableImpl<Key, Value>> current = list.head;
            while(current != null){
                put(current.data.key, current.data.value);
                current = current.next;
            }
        }
    }

    private void arrayDouble() {
        this.array = new LinkedList[this.array.length * 2];
        for (int i = 0; i < this.array.length; i++) {
            array[i] = new LinkedList<HashTableImpl<Key, Value>>();
        }
    }

    private boolean delete(Key k) {
        int hCode = k.hashCode();
        int index = hashFunction(hCode);
        LinkedList<HashTableImpl<Key, Value>> list = this.array[index];
        Value v = get(k);
        HashTableImpl<Key, Value> object = new HashTableImpl<Key, Value>();
        object.key = k;
        object.value = v;
        Node<HashTableImpl<Key, Value>> current = list.head;
        if(current.data.key.hashCode() == k.hashCode()){
            if(current.next == null){
                list.head = null;
                return true;
            }
            list.head = current.next;
            return true;
        }
        while(current.next != null){
            if(current.next.data.key.hashCode() == k.hashCode()){
                if(current.next.next == null){
                    current.next = null;
                    return true;
                }
                current.next = current.next.next;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    private int hashFunction(int k) {
        return (k & 0x7fffffff) % this.array.length;
    }

    private int getAverageListSize(){
        int average = 0;
        int total = 0;
        for(int i = 0; i < this.array.length; i++){
            total += array[i].getLength();
        }
        average = total/this.array.length;
        return average;
    }


    private class Node<T> {
        private T data;
        private Node<T> next;
    }

    private class LinkedList<T> {
        T data;
        Node<T> head;
        int length = 0;

        private LinkedList() {
            this.head = null;
        }

        private void add(T value) {
            Node<T> object = new Node<T>();
            object.data = value;
            if (this.head == null) {
                this.head = object;
                this.head.next = null;
                this.length ++;

            } else {
                Node<T> current = this.head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = object;
                this.length ++;
            }
        }

        private int getLength(){

            return this.length;
        }

    }
}



