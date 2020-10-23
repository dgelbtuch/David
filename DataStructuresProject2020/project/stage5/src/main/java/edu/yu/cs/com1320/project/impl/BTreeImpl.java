package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;
import java.util.Arrays;

public class BTreeImpl <Key extends Comparable<Key>, Value> implements BTree<Key,Value> {
    //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
    private static final int MAX = 4;
    private Node root; //root of the B-tree
    private Node leftMostExternalNode;
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private PersistenceManager<Key, Value> pm;

    public BTreeImpl()
    {
        this.root = new Node(0);
        this.leftMostExternalNode = this.root;
        this.n = 0;
    }

    @Override
    public Value get(Key k) {
        if (k == null)
        {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry<Key,Value> entry = this.get(this.root, k, this.height);
        if(entry != null)
        {
            if(entry.val != null){
                return entry.val;
            }
            try {
                if(this.pm == null){
                    return null;
                }
                entry.val = this.pm.deserialize(k);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(entry.val != null){
                return entry.val;
            }
        }
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        if (k == null)
        {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        Entry<Key,Value> alreadyThere = this.get(this.root, k, this.height);
        if(alreadyThere != null)
        {
            Value value = alreadyThere.getValue();
            if(value == null){
                try {
                    if(this.pm != null) { value = this.pm.deserialize(k); }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            alreadyThere.val = v;
            return value;
        }
        Node newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (newNode == null) { return null; }
        Node newRoot = new Node(2);
        newRoot.entries[0] = new Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;
        this.height++;
        return null;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {
        Value val = this.get(k);
        this.pm.serialize(k, val);
        this.put(k, null);
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {
        this.pm = pm;
    }
    //B-tree node data type
    private class Node
    {
        private int entryCount; // number of entries
        private Entry<Key, Value>[] entries = new Entry[MAX]; // the array of children
        private Node next;
        private Node previous;

        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }

        private void setNext(Node next)
        {
            this.next = next;
        }
        private Node getNext()
        {
            return this.next;
        }
        private void setPrevious(Node previous)
        {
            this.previous = previous;
        }
        private Node getPrevious()
        {
            return this.previous;
        }

        private Entry[] getEntries()
        {
            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }
    private class Entry<Key, Value> {
        private Key key;
        private Value val;
        private Node child;

        private Entry(Key key, Value val, Node child)
        {
            this.key = key;
            this.val = val;
            this.child = child;
        }
        private Value getValue()
        {
            return this.val;
        }
        private Key getKey()
        {
            return this.key;
        }
    }
    private Entry<Key,Value> get(Node currentNode, Key key, int height) {
        Entry<Key, Value>[] entries = currentNode.entries;
        if (height == 0) {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (isEqual(key, entries[j].key)) { return entries[j]; }
            }
            return null;
        }
        else {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            return null;
        }
    }
    private Node put(Node currentNode, Key key, Value val, int height) {
        int j;
        Entry<Key,Value> newEntry = new Entry<Key,Value>(key, val, null);
        if (height == 0) {
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) { break; }
            }
        }
        else {
            for (j = 0; j < currentNode.entryCount; j++) {
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) { return null; }
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX) { return null; }
        else { return this.split(currentNode, height); }
    }

    private Node split(Node currentNode, int height)
    {
        Node newNode = new Node(MAX / 2);
        currentNode.entryCount = MAX / 2;
        for (int j = 0; j < MAX / 2; j++)
        {
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
        }
        //external node
        if (height == 0)
        {
            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }
        return newNode;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Key k1, Key k2)
    {
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Key k1, Key k2)
    {
        return k1.compareTo(k2) == 0;
    }
}
