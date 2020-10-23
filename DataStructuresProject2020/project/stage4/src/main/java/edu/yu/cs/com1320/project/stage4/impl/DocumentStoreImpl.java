package edu.yu.cs.com1320.project.stage4.impl;



import com.sun.jndi.toolkit.url.Uri;
import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.CommandSet;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
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
    private MinHeapImpl<DocumentImpl> heap = new MinHeapImpl<>();
    private int docNumber = 0;
    private int byteNumber = 0;
    private int docLimit = -1;
    private int byteLimit = -1;

    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl document = null;
        if(uri == null || format == null){ throw new IllegalArgumentException();}
        if(input == null){
            if (!exists(uri)){
                this.stack.push(nothing(uri));
                return 0; }
            int deletedHashCode = this.hashTable.get(uri).getDocumentTextHashCode();
            deleteDocument(uri);
            return deletedHashCode;
        }
        byte [] contents = streamToByte(input);
        String text = extractText(contents, format);
        if(format == DocumentFormat.TXT){ document = new DocumentImpl(uri, text, text.hashCode()); }
        if(format == DocumentFormat.PDF){ document = new DocumentImpl(uri, text, text.hashCode(), contents); }
        if(docLimit == 0 || byteLimit == 0){
            return 0;
        }
        if((document.getDocumentAsTxt().getBytes().length + document.getDocumentAsPdf().length > byteLimit) && byteLimit > -1){
            return 0;
        }
        if(exists(uri)){
            return replace(document,uri);
        }
        this.addToStore(uri, document);
        return 0;
    }



    public byte[] getDocumentAsPdf(URI uri) {
        DocumentImpl get = this.hashTable.get(uri);
        if(get == null){
            return null;
        }
        get.setLastUseTime(System.nanoTime());
        this.heap.reHeapify(get);
        return get.getDocumentAsPdf();
    }

    public String getDocumentAsTxt(URI uri) {
        DocumentImpl get = this.hashTable.get(uri);
        if(get == null){
            return null;
        }
        get.setLastUseTime(System.nanoTime());
        this.heap.reHeapify(get);
        return get.getDocumentAsTxt();
    }

    public boolean deleteDocument(URI uri) {
        if(this.hashTable.get(uri) == null){
            this.stack.push(nothing(uri));
            return false;
        }
        this.hashTable.get(uri).setLastUseTime(Long.MIN_VALUE);
        this.heap.reHeapify(this.hashTable.get(uri));
        this.heap.removeMin();
        deletedStorage(uri);
        this.stack.push(deleteCommand(uri, this.hashTable.get(uri)));
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
        long currentTime = System.nanoTime();
        if(documents == null){
            return list;
        }
        for (DocumentImpl doc: documents) {
            doc.setLastUseTime(currentTime);
            this.heap.reHeapify(doc);
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
        long currentTime = System.nanoTime();
        for (DocumentImpl doc: documents) {
            doc.setLastUseTime(currentTime);
            this.heap.reHeapify(doc);
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
        long currentTime = System.nanoTime();
        if(documents == null){
            return list;
        }
        for (DocumentImpl doc: documents) {
            doc.setLastUseTime(currentTime);
            this.heap.reHeapify(doc);
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
        long currentTime = System.nanoTime();
        for (DocumentImpl doc: documents) {
            doc.setLastUseTime(currentTime);
            this.heap.reHeapify(doc);
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
        long currentTime = System.nanoTime();
        for (DocumentImpl doc: docs) {
            deletedDocs.add(doc.getKey());
            uriCommandSet.addCommand(deleteCommand(doc.getKey(), doc));
            removeFromTrie(doc);
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(doc);
            this.heap.removeMin();
            this.deletedStorage(doc.getKey());
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
        long currentTime = System.nanoTime();
        for (DocumentImpl doc: docs) {
            deletedDocs.add(doc.getKey());
            uriCommandSet.addCommand(deleteCommand(doc.getKey(), doc));
            removeFromTrie(doc);
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(doc);
            this.heap.removeMin();
            this.deletedStorage(doc.getKey());
            this.hashTable.put(doc.getKey(), null);
        }
        this.stack.push(uriCommandSet);
        return deletedDocs;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.docLimit = limit;
        while((docLimit < docNumber) && (docLimit > -1)){
            DocumentImpl lastUsed = this.heap.removeMin();
            deletedStorage(lastUsed.getKey());
            this.removeFromStack(lastUsed.getKey());
            this.removeFromTrie(lastUsed);
            this.hashTable.put(lastUsed.getKey(), null);
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.byteLimit = limit;
        while((byteLimit < byteNumber) && (byteLimit > -1)){
            DocumentImpl lastUsed = this.heap.removeMin();
            deletedStorage(lastUsed.getKey());
            this.removeFromStack(lastUsed.getKey());
            this.removeFromTrie(lastUsed);
            this.hashTable.put(lastUsed.getKey(), null);
        }
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


    /*
     * MIN HEAP METHODS
     */
    private void addedStorage(URI uri){
        this.docNumber++;
        this.byteNumber += (this.hashTable.get(uri).getDocumentAsTxt().getBytes().length + this.hashTable.get(uri).getDocumentAsPdf().length);
    }
    private void deletedStorage(URI uri){
        this.docNumber--;
        this.byteNumber -= (this.hashTable.get(uri).getDocumentAsTxt().getBytes().length + this.hashTable.get(uri).getDocumentAsPdf().length);
    }

    /*
     * TRIE METHODS
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

    /*
     * UNDO METHODS
     */

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
            this.removeFromTrie(doc);
            deletedStorage(uri);
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(doc);
            this.heap.removeMin();
            this.hashTable.put(uri, null);
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> putOverwriteCommand(URI uriKey, DocumentImpl oldDoc, DocumentImpl newDoc){
        Function<URI, Boolean> undo = uri -> {
            this.byteNumber -= (this.hashTable.get(uri).getDocumentAsTxt().getBytes().length + this.hashTable.get(uri).getDocumentAsPdf().length);
            this.hashTable.put(uri, oldDoc);
            this.addToTrie(oldDoc);
            this.removeFromTrie(newDoc);
            this.byteNumber += (this.hashTable.get(uri).getDocumentAsTxt().getBytes().length + this.hashTable.get(uri).getDocumentAsPdf().length);
            newDoc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(newDoc);
            this.heap.removeMin();
            oldDoc.setLastUseTime(System.nanoTime());
            while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
                DocumentImpl lastUsed = this.heap.removeMin();
                deletedStorage(lastUsed.getKey());
                this.removeFromStack(lastUsed.getKey());
                this.removeFromTrie(lastUsed);
                this.hashTable.put(lastUsed.getKey(), null);
            }
            this.heap.insert(oldDoc);
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> deleteCommand(URI uriKey, DocumentImpl document){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, (DocumentImpl) document);
            this.addToTrie((DocumentImpl) document);
            addedStorage(uri);
            document.setLastUseTime(System.nanoTime());
            while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
                DocumentImpl lastUsed = this.heap.removeMin();
                deletedStorage(lastUsed.getKey());
                this.removeFromStack(lastUsed.getKey());
                this.removeFromTrie(lastUsed);
                this.hashTable.put(lastUsed.getKey(), null);
            }
            this.heap.insert(document);
            return true;
        };
        return new GenericCommand<URI>(uriKey, undo);
    }
    private GenericCommand<URI> nothing(URI uriKey){
        Function<URI, Boolean> undo = uri -> { return true; };
        return new GenericCommand<URI>(uriKey, undo);
    }
    private void removeFromStack(URI uri) {
        if(this.stack.size() == 0){
            throw new IllegalStateException("Stack Is Empty");
        }
        StackImpl<Undoable> temp = new StackImpl<>();
        int size = this.stack.size();
        int i = 0;
        while (this.stack.size() != 0) {
            if(this.stack.peek() instanceof CommandSet){
                CommandSet<URI> commandSet = (CommandSet<URI>) this.stack.peek();
                deleteFromStack(uri, commandSet,temp);
                continue;
            }
            GenericCommand<URI> top = (GenericCommand<URI>) this.stack.peek();
            if (top.getTarget().hashCode() == uri.hashCode()) {
                this.stack.pop();
                continue;
            }
            temp.push(this.stack.pop());
        }
        int tempSize = temp.size();
        for(int j = 0; j < tempSize; j++) {
            this.stack.push(temp.pop());
        }
    }

    private void deleteFromStack(URI uri, CommandSet commandSet, Stack temp) {
        Iterator commandIterator = commandSet.iterator();
        while(commandIterator.hasNext()){
            GenericCommand<URI> command = (GenericCommand<URI>) commandIterator.next();
            if(command.hashCode() == uri.hashCode()){
                commandIterator.remove();
                if(commandSet.size() == 0){
                    this.stack.pop();
                }
                return;
            }
        }
        temp.push(this.stack.pop());

    }

    /*
     * DOCUMENT METHODS
     */

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

    /*
     * PUT/DELETE METHODS
     */

    private int replace(DocumentImpl document, URI uri){
        if(document.getDocumentTextHashCode() == this.hashTable.get(uri).getDocumentTextHashCode()){
            this.stack.push(nothing(uri));
            this.hashTable.get(uri).setLastUseTime(System.nanoTime());
            this.heap.reHeapify(this.hashTable.get(uri));
            return document.getDocumentTextHashCode();
        }
        int replaceHash = this.hashTable.get(uri).getDocumentTextHashCode();
        this.byteNumber -= (this.hashTable.get(uri).getDocumentAsTxt().getBytes().length + this.hashTable.get(uri).getDocumentAsPdf().length);
        this.stack.push(putOverwriteCommand(uri, this.hashTable.get(uri), document));
        removeFromTrie((DocumentImpl) this.hashTable.get(uri));
        addToTrie(document);
        this.hashTable.get(uri).setLastUseTime(Long.MIN_VALUE);
        this.heap.reHeapify(this.hashTable.get(uri));
        this.heap.removeMin();

        this.hashTable.put(uri, document);
        this.byteNumber += (document.getDocumentAsTxt().getBytes().length + document.getDocumentAsPdf().length);
        document.setLastUseTime(System.nanoTime());
        while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
            DocumentImpl lastUsed = this.heap.removeMin();
            deletedStorage(lastUsed.getKey());
            this.removeFromStack(lastUsed.getKey());
            this.removeFromTrie(lastUsed);
            this.hashTable.put(lastUsed.getKey(), null);
        }
        this.heap.insert(document);
        return replaceHash;
    }

    private void addToStore(URI uri, DocumentImpl document) {
        this.hashTable.put(uri, document);
        addToTrie(document);
        this.stack.push(putNewCommand(uri, document));
        document.setLastUseTime(System.nanoTime());
        addedStorage(uri);
        while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
            DocumentImpl lastUsed = this.heap.removeMin();
            deletedStorage(lastUsed.getKey());
            this.removeFromStack(lastUsed.getKey());
            this.removeFromTrie(lastUsed);
            this.hashTable.put(lastUsed.getKey(), null);
        }
        this.heap.insert(document);
    }


}
