package edu.yu.cs.com1320.project.stage3.impl;



import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;

import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Function;

import static org.apache.pdfbox.pdmodel.PDDocument.load;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>();
    private StackImpl<Undoable> stack = new StackImpl<>();
    private TrieImpl<DocumentImpl> trie = new TrieImpl<>();

    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl document = null;
        if(uri == null || format == null){ throw new IllegalArgumentException();}
        if(input == null){
            if (!exists(uri)){
                this.stack.push(nothing(uri));
                return 0; }
            int deletedHashCode = getDocumentAsTxt(uri).hashCode();
            deleteDocument(uri);
            return deletedHashCode;
        }
        byte [] contents = streamToByte(input);
        String text = extractText(contents, format);
        if(format == DocumentFormat.TXT){ document = new DocumentImpl(uri, text, text.hashCode()); }
        if(format == DocumentFormat.PDF){ document = new DocumentImpl(uri, text, text.hashCode(), contents); }
        if(exists(uri)){
            return this.replace(document, uri);
        }
        this.hashTable.put(uri, document);
        addToTrie(document);
        this.stack.push(putNewCommand(uri, document));
        return 0;
    }

    public byte[] getDocumentAsPdf(URI uri) {
        DocumentImpl get = this.hashTable.get(uri);
        if(get == null){
            return null;
        }
        return get.getDocumentAsPdf();
    }

    public String getDocumentAsTxt(URI uri) {
        DocumentImpl get = this.hashTable.get(uri);
        if(get == null){
            return null;
        }
        return get.getDocumentAsTxt();
    }

    public boolean deleteDocument(URI uri) {
        if(this.hashTable.get(uri) == null){
            this.stack.push(nothing(uri));
            return false;
        }
        this.stack.push(deleteCommand(uri, getDocument(uri)));
        this.removeFromTrie(this.hashTable.get(uri));
        this.hashTable.put(uri, null);
        return true;
    }


    public void undo() throws IllegalStateException {
        if(this.stack.size() == 0){
            throw new IllegalStateException("Stack Is Empty");
        }
        this.stack.pop().undo();
    }


    public void undo(URI uri) throws IllegalStateException {
        if(this.stack.size() == 0){
            throw new IllegalStateException("Stack Is Empty");
        }
        StackImpl<Undoable> temp = new StackImpl<>();
        int size = this.stack.size();
        for (int i = 0; i < size; i++) {
            if(this.stack.peek() instanceof CommandSet){
               CommandSet<URI> commandSet = (CommandSet<URI>) this.stack.peek();
               if(removeCommandFromSet(uri, commandSet)) {
                   break;
               }
               temp.push(this.stack.pop());
               continue;
            }
            GenericCommand<URI> top = (GenericCommand<URI>) this.stack.peek();
            if (top.getTarget().hashCode() == uri.hashCode()) {
                this.stack.pop().undo();
                break;
            }
            temp.push(this.stack.pop());
        }
        int tempSize = temp.size();
        for(int i = 0; i < tempSize; i++) {
            this.stack.push(temp.pop());
        }
    }

    @Override
    public List<String> search(String keyword) {
        keyword = keyword.trim();
        keyword = keyword.replaceAll("\\W", "");
        keyword = keyword.toUpperCase();
        String key = keyword;
        List<DocumentImpl> documents = this.trie.getAllSorted(keyword, (DocumentImpl o1, DocumentImpl o2) -> o2.wordCount(key) - o1.wordCount(key));
        List<String> list = new ArrayList<String>();
        if(documents == null){
            return list;
        }
        for (DocumentImpl doc: documents) {
            list.add(doc.getDocumentAsTxt());
        }
        return list;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        keyword = keyword.trim();
        keyword = keyword.replaceAll("\\W", "");
        keyword = keyword.toUpperCase();
        String key = keyword;
        List<DocumentImpl> documents = this.trie.getAllSorted(keyword, (DocumentImpl o1, DocumentImpl o2) -> o2.wordCount(key) - o1.wordCount(key));
        List<byte[]> list = new ArrayList<byte[]>();
        for (DocumentImpl doc: documents) {
            list.add(doc.getDocumentAsPdf());
        }
        return list;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        String pref = prefix;
        List<DocumentImpl> documents = this.trie.getAllWithPrefixSorted(prefix, (DocumentImpl o1, DocumentImpl o2) -> getPrefixCount(o2, pref) - getPrefixCount(o1, pref));
        List<String> list = new ArrayList<String>();
        if(documents == null){
            return list;
        }
        for (DocumentImpl doc: documents) {
            list.add(doc.getDocumentAsTxt());
        }
        return list;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        String pref = prefix;
        List<DocumentImpl> documents = this.trie.getAllWithPrefixSorted(prefix, (DocumentImpl o1, DocumentImpl o2) -> getPrefixCount(o2, pref) - getPrefixCount(o1, pref));
        List<byte[]> list = new ArrayList<byte[]>();
        for (DocumentImpl doc: documents) {
            list.add(doc.getDocumentAsPdf());
        }
        return list;
    }

    @Override
    public Set<URI> deleteAll(String key) {
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        Set<DocumentImpl> docs = this.trie.deleteAll(key);
        Set<URI> deletedDocs = new HashSet<>();
        CommandSet<URI> uriCommandSet = new CommandSet<>();
        for (DocumentImpl doc: docs) {
            deletedDocs.add(doc.getKey());
            uriCommandSet.addCommand(deleteCommand(doc.getKey(), doc));
            removeFromTrie(doc);
            this.hashTable.put(doc.getKey(), null);
        }
        this.stack.push(uriCommandSet);
        return deletedDocs;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        Set<DocumentImpl> docs = this.trie.deleteAllWithPrefix(prefix);
        Set<URI> deletedDocs = new HashSet<>();
        CommandSet<URI> uriCommandSet = new CommandSet<>();
        for (DocumentImpl doc: docs) {
            deletedDocs.add(doc.getKey());
            uriCommandSet.addCommand(deleteCommand(doc.getKey(), doc));
            removeFromTrie(doc);
            this.hashTable.put(doc.getKey(), null);
        }
        this.stack.push(uriCommandSet);
        return deletedDocs;
    }

    /**
     * @return the Document object stored at that URI, or null if there is no such
    Document
     */
    protected Document getDocument(URI uri){
        if(this.hashTable.get(uri) == null){
            return null;
        }
        return (Document) this.hashTable.get(uri);
    }



    /********************************************************************************
     * MY METHODS
     ********************************************************************************
     */

    private int getPrefixCount(DocumentImpl document, String prefix){
        int prefixCount = 0;
        String[] words = this.getDocumentAsTxt(document.getKey()).split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
            words[i] = words[i].replaceAll("\\W", "");
            words[i] = words[i].toUpperCase();
        }
        for (String word: words) {
            if(word.startsWith(prefix)){
                prefixCount ++;
            }
        }
        return prefixCount;
    }
    private boolean removeCommandFromSet(URI uri, CommandSet<URI> set){
        if(set.containsTarget(uri)){
            set.undo(uri);
            if(set.size()==0){
                this.stack.pop();
            }
            return true;
        }
        return false;
    }
    private GenericCommand<URI> putNewCommand(URI uriKey, DocumentImpl doc){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, null);
            this.removeFromTrie(doc);
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> putOverwriteCommand(URI uriKey, DocumentImpl oldDoc, DocumentImpl newDoc){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, oldDoc);
            this.addToTrie(oldDoc);
            this.removeFromTrie(newDoc);
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> deleteCommand(URI uriKey, Document document){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, (DocumentImpl) document);
            this.addToTrie((DocumentImpl) document);
            return true;
        };
        return new GenericCommand<URI>(uriKey, undo);
    }
    private GenericCommand<URI> nothing(URI uriKey){
        Function<URI, Boolean> undo = uri -> { return true; };
        return new GenericCommand<URI>(uriKey, undo);
    }
    private String extractText (byte[] contents, DocumentFormat format){
        String text = null;
        if (format == DocumentFormat.TXT){
            text = new String (contents);
        }
        if (format == DocumentFormat.PDF){
            try {
                PDDocument doc = load(contents);
                PDFTextStripper strip = new PDFTextStripper();
                text = strip.getText(doc);
                doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        text = text.trim();
        text = text.replace("\ufeff", "");
        return text;
    }
    private byte[] streamToByte(InputStream input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384];
        int len = 0;
        try {
            len = input.read(data);
            while (len != -1) {
                buffer.write(data, 0, len);
                len = input.read(data);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toByteArray();
    }
    private boolean exists(URI uri){
        Document get = (Document) this.hashTable.get(uri);
        if(get == null){
            return false;
        }
        return true;
    }
    private void addToTrie(DocumentImpl document){
        String text = document.getDocumentAsTxt();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
            words[i] = words[i].replaceAll("\\W", "");
            words[i] = words[i].toUpperCase();
            this.trie.put(words[i], document);
        }
    }
    private void removeFromTrie(DocumentImpl document) {
        String text = document.getDocumentAsTxt();
        String[] words = text.split(" ");
        for (String word : words) {
            word = word.trim();
            word = word.replaceAll("\\W", "");
            word = word.toUpperCase();
            this.trie.delete(word, document);
        }
    }
    private int replace(DocumentImpl document, URI uri){
        if(document.getDocumentTextHashCode() == this.hashTable.get(uri).getDocumentTextHashCode()){
            this.stack.push(nothing(uri));
            return document.getDocumentTextHashCode();}
        int replaceHash = getDocumentAsTxt(uri).hashCode();
        this.stack.push(putOverwriteCommand(uri, (DocumentImpl) getDocument(uri), document));
        removeFromTrie((DocumentImpl) getDocument(uri));
        addToTrie(document);
        this.hashTable.put(uri, document);
        return replaceHash;
    }
}
