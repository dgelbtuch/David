package edu.yu.cs.com1320.project.stage5.impl;


import edu.yu.cs.com1320.project.CommandSet;
import edu.yu.cs.com1320.project.GenericCommand;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.Undoable;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static org.apache.pdfbox.pdmodel.PDDocument.load;

public class DocumentStoreImpl implements DocumentStore {
    private BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
    private StackImpl<Undoable> stack = new StackImpl<>();
    private TrieImpl<URI> trie = new TrieImpl<>();
    private MinHeapImpl<DURI> heap = new MinHeapImpl<>();
    private int docNumber = 0;
    private int byteNumber = 0;
    private int docLimit = -1;
    private int byteLimit = -1;
    private File baseDir;

    public DocumentStoreImpl(){
        this.baseDir = new File(System.getProperty("user.dir"));
        DocumentPersistenceManager pm = new DocumentPersistenceManager(baseDir);
        this.bTree.setPersistenceManager(pm);
        URI sentinel = null;
        try {
            sentinel = new URI("");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.bTree.put(sentinel, null);
    }
    public DocumentStoreImpl(File baseDir){
        this.baseDir = baseDir;
        if(baseDir == null){
            this.baseDir = new File(System.getProperty("user.dir"));
        }
        DocumentPersistenceManager pm = new DocumentPersistenceManager(this.baseDir);
        this.bTree.setPersistenceManager(pm);
        URI sentinel = null;
        try {
            sentinel = new URI("");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.bTree.put(sentinel, null);
    }


    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl document = null;
        if(uri == null || format == null){ throw new IllegalArgumentException();}
        if(input == null){
            if (!onDisk(uri) && !exists(uri)){
                this.stack.push(nothing(uri));
                return 0; }
            if(onDisk(uri)){
                this.heap.insert(new DURI(uri));
            }
            int deletedHashCode = this.bTree.get(uri).getDocumentTextHashCode();
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
        this.checkOnDisk(uri);
        DocumentImpl get = (DocumentImpl) this.bTree.get(uri);
        if(get == null){
            return null;
        }
        get.setLastUseTime(System.nanoTime());
        this.heap.reHeapify(new DURI((uri)));
        this.checkStorage();
        return get.getDocumentAsPdf();
    }

    public String getDocumentAsTxt(URI uri) {
        this.checkOnDisk(uri);
        DocumentImpl get = (DocumentImpl) this.bTree.get(uri);
        if(get == null){
            return null;
        }
        get.setLastUseTime(System.nanoTime());
        this.heap.reHeapify(new DURI(uri));
        this.checkStorage();
        return get.getDocumentAsTxt();
    }

    public boolean deleteDocument(URI uri) {
        if(onDisk(uri)){
            this.heap.insert(new DURI(uri));
        }
        if(this.bTree.get(uri) == null){
            this.stack.push(nothing(uri));
            return false;
        }
        this.bTree.get(uri).setLastUseTime(Long.MIN_VALUE);
        this.heap.reHeapify(new DURI(uri));
        this.heap.removeMin();
        deletedStorage(uri);
        this.stack.push(deleteCommand(uri, (DocumentImpl) this.bTree.get(uri)));
        this.removeFromTrie(this.bTree.get(uri));
        this.bTree.put(uri, null);
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
        List<URI> uris = this.trie.getAllSorted(keyword, (URI o1, URI o2) -> o1.compareTo(o2));
        List<DocumentImpl> docs = new ArrayList<>();
        List<String> list = new ArrayList<String>();
        long currentTime = System.nanoTime();
        if(uris == null){
            return list;
        }
        for (URI doc: uris) {
            this.checkOnDisk(doc);
            this.bTree.get(doc).setLastUseTime(currentTime);
            this.heap.reHeapify(new DURI(doc));
            docs.add((DocumentImpl) this.bTree.get(doc));
        }
        docs.sort((DocumentImpl o1, DocumentImpl o2) -> o2.wordCount(key) - o1.wordCount(key));
        for (DocumentImpl doc: docs) {
            list.add(doc.getDocumentAsTxt());
        }
        this.checkStorage();
        return list;
    }

    @Override
    public List<byte[]> searchPDFs(String keyword) {
        keyword = keyword.trim();
        keyword = keyword.replaceAll("\\W", "");
        keyword = keyword.toUpperCase();
        String key = keyword;
        List<URI> uris = this.trie.getAllSorted(keyword, Comparator.naturalOrder());
        List<DocumentImpl> docs = new ArrayList<>();
        List<byte[]> list = new ArrayList<byte[]>();
        long currentTime = System.nanoTime();
        for (URI doc: uris) {
            this.checkOnDisk(doc);
            this.bTree.get(doc).setLastUseTime(currentTime);
            this.heap.reHeapify(new DURI(doc));
            docs.add((DocumentImpl) this.bTree.get(doc));
        }
        docs.sort((DocumentImpl o1, DocumentImpl o2) -> o2.wordCount(key) - o1.wordCount(key));
        for (DocumentImpl doc: docs) {
            list.add(doc.getDocumentAsPdf());
        }
        this.checkStorage();
        return list;
    }

    @Override
    public List<String> searchByPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        String pref = prefix;
        List<URI> uris = this.trie.getAllWithPrefixSorted(prefix, Comparator.naturalOrder());
        List<DocumentImpl> docs = new ArrayList<>();
        List<String> list = new ArrayList<String>();
        long currentTime = System.nanoTime();
        if(uris == null){
            return list;
        }
        for (URI doc: uris) {
            this.checkOnDisk(doc);
            this.bTree.get(doc).setLastUseTime(currentTime);
            this.heap.reHeapify(new DURI(doc));
            docs.add((DocumentImpl) this.bTree.get(doc));
        }
        docs.sort((DocumentImpl o1, DocumentImpl o2) -> getPrefixCount(o2, pref) - getPrefixCount(o1, pref));
        for (DocumentImpl doc: docs) {
            list.add(doc.getDocumentAsTxt());
        }
        this.checkStorage();
        return list;
    }

    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        String pref = prefix;
        List<URI> uris = this.trie.getAllWithPrefixSorted(prefix, Comparator.naturalOrder());
        List<DocumentImpl> docs = new ArrayList<>();
        List<byte[]> list = new ArrayList<byte[]>();
        long currentTime = System.nanoTime();
        for (URI doc: uris) {
            this.checkOnDisk(doc);
            this.bTree.get(doc).setLastUseTime(currentTime);
            this.heap.reHeapify(new DURI(doc));
            docs.add((DocumentImpl) this.bTree.get(doc));
        }
        docs.sort((DocumentImpl o1, DocumentImpl o2) -> getPrefixCount(o2, pref) - getPrefixCount(o1, pref));
        for (DocumentImpl doc: docs) {
            list.add(doc.getDocumentAsPdf());
        }
        this.checkStorage();
        return list;
    }

    @Override
    public Set<URI> deleteAll(String key) {
        key = key.trim();
        key = key.replaceAll("\\W", "");
        key = key.toUpperCase();
        Set<URI> docs = this.trie.deleteAll(key);
        Set<URI> deletedDocs = new HashSet<>();
        CommandSet<URI> uriCommandSet = new CommandSet<>();
        for (URI doc: docs) {
            if(onDisk(doc)){
                this.heap.insert(new DURI(doc));
            }
            deletedDocs.add(this.bTree.get(doc).getKey());
            uriCommandSet.addCommand(deleteCommand(this.bTree.get(doc).getKey(), (DocumentImpl) this.bTree.get(doc)));
            removeFromTrie(this.bTree.get(doc));
            this.bTree.get(doc).setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(new DURI(doc));
            this.heap.removeMin();
            this.deletedStorage(doc);
            this.bTree.put(doc, null);
        }
        this.stack.push(uriCommandSet);
        return deletedDocs;
    }

    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        prefix = prefix.trim();
        prefix = prefix.replaceAll("\\W", "");
        prefix = prefix.toUpperCase();
        Set<URI> docs = this.trie.deleteAllWithPrefix(prefix);
        Set<URI> deletedDocs = new HashSet<>();
        CommandSet<URI> uriCommandSet = new CommandSet<>();
        for (URI doc: docs) {
            if(onDisk(doc)){
                this.heap.insert(new DURI(doc));
            }
            deletedDocs.add(this.bTree.get(doc).getKey());
            uriCommandSet.addCommand(deleteCommand(doc, (DocumentImpl) this.bTree.get(doc)));
            removeFromTrie(this.bTree.get(doc));
            this.bTree.get(doc).setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(new DURI(doc));
            this.heap.removeMin();
            this.deletedStorage(doc);
            this.bTree.put(doc, null);
        }
        this.stack.push(uriCommandSet);
        return deletedDocs;
    }

    @Override
    public void setMaxDocumentCount(int limit) {
        this.docLimit = limit;
        while((docLimit < docNumber) && (docLimit > -1)){
           this.removeFromMemory();
        }
    }

    @Override
    public void setMaxDocumentBytes(int limit) {
        this.byteLimit = limit;
        while((byteLimit < byteNumber) && (byteLimit > -1)){
            this.removeFromMemory();
        }
    }

    /**
     * @return the Document object stored at that URI, or null if there is no such
    Document
     */
    protected Document getDocument(URI uri){
        String path = this.uriPath(uri);
        File directory = new File(this.baseDir + path + ".json");
        if(directory.exists()){
            return null;
        }
        if(this.bTree.get(uri) == null){
            return null;
        }
        return this.bTree.get(uri);
    }



    /********************************************************************************
     * MY METHODS
     ********************************************************************************
     */

    private String uriPath(URI uri){
        String path = "";
        if(uri.getRawAuthority() != null){
            path = File.separator + uri.getRawAuthority();
        }
        path += uri.getRawPath();
        if(!path.startsWith(File.separator)){
            path = File.separator + path;
        }
        return path;
    }

    /*
     * MIN HEAP METHODS
     */
    private void addedStorage(URI uri){
        this.docNumber++;
        this.byteNumber += (this.bTree.get(uri).getDocumentAsTxt().getBytes().length + this.bTree.get(uri).getDocumentAsPdf().length);
    }
    private void deletedStorage(URI uri){
        this.docNumber--;
        this.byteNumber -= (this.bTree.get(uri).getDocumentAsTxt().getBytes().length + this.bTree.get(uri).getDocumentAsPdf().length);
    }
    private void checkStorage(){
        while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
            this.removeFromMemory();
        }
    }
    private void checkOnDisk(URI uri){
        String path = this.uriPath(uri);
        File directory = new File(this.baseDir + path + ".json");
        if(directory.exists()){
            addedStorage(uri);
            this.bTree.get(uri).setLastUseTime(System.nanoTime());
            this.heap.insert(new DURI(uri));
        }
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
    private void addToTrie(Document document){
        String text = document.getDocumentAsTxt();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].trim();
            words[i] = words[i].replaceAll("\\W", "");
            words[i] = words[i].toUpperCase();
            this.trie.put(words[i], document.getKey());
        }
    }
    private void removeFromTrie(Document document) {
        String text = document.getDocumentAsTxt();
        String[] words = text.split(" ");
        for (String word : words) {
            word = word.trim();
            word = word.replaceAll("\\W", "");
            word = word.toUpperCase();
            this.trie.delete(word, document.getKey());
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
            if(onDisk(uri)){
                this.heap.insert(new DURI(uri));
            }
            this.removeFromTrie(doc);
            deletedStorage(uri);
            doc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(new DURI(uri));
            this.heap.removeMin();
            this.bTree.put(uri, null);
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> putOverwriteCommand(URI uriKey, DocumentImpl oldDoc, DocumentImpl newDoc){
        Function<URI, Boolean> undo = uri -> {
            if(onDisk(uri)){
                this.heap.insert(new DURI(uri));
                this.docNumber++;
            }
            this.byteNumber -= (this.bTree.get(uri).getDocumentAsTxt().getBytes().length + this.bTree.get(uri).getDocumentAsPdf().length);
            this.removeFromTrie(newDoc);
            this.bTree.put(uri, oldDoc);
            this.addToTrie(oldDoc);
            this.byteNumber += (this.bTree.get(uri).getDocumentAsTxt().getBytes().length + this.bTree.get(uri).getDocumentAsPdf().length);
            newDoc.setLastUseTime(Long.MIN_VALUE);
            this.heap.reHeapify(new DURI(uri));
            this.heap.removeMin();
            oldDoc.setLastUseTime(System.nanoTime());
            while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
                this.removeFromMemory();
            }
            this.heap.insert(new DURI(uri));
            return true;
        };
        return new GenericCommand<URI>(uriKey,undo);
    }
    private GenericCommand<URI> deleteCommand(URI uriKey, DocumentImpl document){
        Function<URI, Boolean> undo = uri -> {
            this.bTree.put(uri, document);
            this.addToTrie(document);
            addedStorage(uri);
            document.setLastUseTime(System.nanoTime());
            while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
                this.removeFromMemory();
            }
            this.heap.insert(new DURI(uri));
            return true;
        };
        return new GenericCommand<URI>(uriKey, undo);
    }
    private GenericCommand<URI> nothing(URI uriKey){
        Function<URI, Boolean> undo = uri -> { return true; };
        return new GenericCommand<URI>(uriKey, undo);
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
        Document get = this.getDocument(uri);
        if(get == null){
            return false;
        }
        return true;
    }

    /*
     * PUT/DELETE METHODS
     */

    private int replace(DocumentImpl document, URI uri){
        if(onDisk(uri)){
            this.heap.insert(new DURI(uri));
        }
        if(document.getDocumentTextHashCode() == this.bTree.get(uri).getDocumentTextHashCode()){

            this.stack.push(nothing(uri));
            this.bTree.get(uri).setLastUseTime(System.nanoTime());
            this.heap.reHeapify(new DURI(uri));
            return document.getDocumentTextHashCode();
        }
        int replaceHash = this.bTree.get(uri).getDocumentTextHashCode();
        this.byteNumber -= (this.bTree.get(uri).getDocumentAsTxt().getBytes().length + this.bTree.get(uri).getDocumentAsPdf().length);
        this.stack.push(putOverwriteCommand(uri, (DocumentImpl) this.bTree.get(uri), document));
        removeFromTrie(this.bTree.get(uri));
        addToTrie(document);
        this.bTree.get(uri).setLastUseTime(Long.MIN_VALUE);
        this.heap.reHeapify(new DURI(uri));
        this.heap.removeMin();
        this.bTree.put(uri, document);
        this.byteNumber += (document.getDocumentAsTxt().getBytes().length + document.getDocumentAsPdf().length);
        document.setLastUseTime(System.nanoTime());
        while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
            this.removeFromMemory();
        }
        this.heap.insert(new DURI(uri));
        return replaceHash;
    }
    private void addToStore(URI uri, DocumentImpl document) {
        this.bTree.put(uri, document);
        addToTrie(document);
        this.stack.push(putNewCommand(uri, document));
        document.setLastUseTime(System.nanoTime());
        addedStorage(uri);
        while((docLimit < docNumber && docLimit > -1 )|| (byteLimit < byteNumber && byteLimit > -1)){
            URI lastUsed = this.heap.removeMin().uri;
            deletedStorage(lastUsed);
            try {
                this.bTree.moveToDisk(lastUsed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.heap.insert(new DURI(uri));
    }
    private boolean onDisk(URI uri){
        String path = this.uriPath(uri);
        File directory = new File(this.baseDir + path + ".json");
        if(directory.exists()){
            return true;
        }
        return false;
    }
    private void removeFromMemory(){
        URI lastUsed = this.heap.removeMin().uri;
        deletedStorage(lastUsed);
        try {
            this.bTree.moveToDisk(lastUsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DURI implements Comparable<DURI>{
        protected URI uri;
        protected DURI (URI uri){
            this.uri = uri;
        }
        @Override
        public int compareTo(DURI o) {
            return bTree.get(this.uri).compareTo(bTree.get(o.uri));
        }
        @Override
        public boolean equals(Object o){
            return this.uri.hashCode() == ((DURI) o).uri.hashCode();
        }
        @Override
        public int hashCode(){
            return this.uri.hashCode();
        }
    }



}
