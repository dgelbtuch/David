package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;
import edu.yu.cs.com1320.project.stage3.Document;

import javax.print.Doc;
import java.util.*;

public class TrieImpl<Value> implements Trie<Value>{
    private final int alphabetSize; // extended ASCII
    private Node root; // root of trie
    private Value type;

    public TrieImpl(){
        this.alphabetSize = 256;
        this.root = new Node<>();
    }


    private class Node<Value>
    {
        //protected Value val;
        protected Set<Value> set = new HashSet<>();
        protected Node[] links = new Node[alphabetSize];
    }

    public void put(String key, Value val) {
        if (val == null || key == null)
        {
            return;
        }
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        put(this.root, key, val, 0);
    }


    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        List<Value> list = new ArrayList<>();
        if (key == null || comparator == null) { return list; }
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        Node<Value> x = this.get(this.root, key, 0);
        if (x == null)
        {
            return list;
        }
        for (Value val: x.set) {
            list.add(val);
        }
        list.sort(comparator);
        return list;
    }


    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        List<Value> list = new ArrayList<>();
        if (prefix == null || comparator == null) { return list;}
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        Node<Value> x = this.get(this.root, prefix, 0);
        if (x == null)
        {
            return list;
        }
        for (Value val: this.getAllFromNode(x)) {
            list.add(val);
        }
        list.sort(comparator);
        return list;
    }


    public Set<Value> deleteAllWithPrefix(String prefix) {
        Set<Value> setReturn = new HashSet<>();
        if (prefix == null) { return setReturn;}
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        Node<Value> x = this.get(this.root, prefix, 0);
        Set<Value> deleted = this.getAllFromNode(x);
        if(deleted.isEmpty()){
            return deleted;
        }
        for (Value val: deleted) {
            setReturn.add(val);
        }
        this.deleteAllFromNode(x);
        this.deleteAll(this.root, prefix,0);
        return deleted;
    }

    public Set<Value> deleteAll(String key) {
        Set<Value> setReturn = new HashSet<>();
        if (key == null) { return setReturn;}
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        Node<Value> x = this.get(this.root, key, 0);
        if(x == null){
            return setReturn;
        }
        for (Value val: x.set) {
            setReturn.add(val);
        }
        deleteAll(this.root, key, 0);
        return setReturn;
    }
    public Value delete(String key, Value val) {
        if (key == null) { return null;}
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        return this.deleteSpecificValue(this.root, key, 0, val);
    }



    /*
    MY METHODS/////////////////////////////////////////////////////
     */

    private Node get(Node x, String key, int d)
    {
        if (x == null)
        {
            return null;
        }
        if (d == key.length())
        {
            return x;
        }
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }
    private Node put(Node x, String key, Value val, int d)
    {
        if (x == null)
        {
            x = new Node();
        }
        if (d == key.length())
        {
            x.set.add(val);
            return x;
        }
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }
    private Node deleteAll(Node x, String key, int d) {
        if (x == null)
        {
            return null;
        }
        if (d == key.length())
        {
            x.set.clear();
        }
        else
        {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1);
        }
        if (!(x.set.isEmpty()))
        {
            return x;
        }
        for (int c = 0; c < this.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return x; //not empty
            }
        }
        return null;
    }
    private Set<Value> getAllFromNode(Node x){
        Set<Value> set = new HashSet<>();
        if (x == null)
        {
            return set;
        }
        set.addAll(x.set);
        for (int c = 0; c < this.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                set.addAll(x.links[c].set);
                set.addAll(getAllFromNode(x.links[c]));
            }
        }
        return set;
    }
    private Value deleteSpecificValue(Node x, String key, int d, Value val){
        if (x == null) { return null; }
        if (d == key.length())
        {
            if(x.set.contains(val)){ x.set.remove(val); }
            else{return null;}
        }
        else
        {
            char c = key.charAt(d);
            this.deleteSpecificValue(x.links[c], key, d + 1, val);
        }
        if(!(hasChildren(x)) && x.set.isEmpty()){
            deleteAll(this.root, key, 0);
        }
        return val;
    }
    private Node<Value> deleteAllFromNode(Node x){
        for (int c = 0; c < this.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                x.links[c] = deleteAllFromNode(x.links[c]);
            }
        }
        return null;
    }
    private Set<Value> getValues(Node x){
        return x.set;
    }
    private boolean hasChildren(Node x){
        if(x == null){return false;}
        for (int c = 0; c < this.alphabetSize; c++)
        {
            if (x.links[c] != null)
            {
                return true; //not empty
            }
        }
        return false;
    }


}
