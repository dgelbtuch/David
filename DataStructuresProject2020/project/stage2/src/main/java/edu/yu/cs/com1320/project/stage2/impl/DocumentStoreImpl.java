package edu.yu.cs.com1320.project.stage2.impl;


import edu.yu.cs.com1320.project.Command;
import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

import static org.apache.pdfbox.pdmodel.PDDocument.load;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>();
    private StackImpl<Command> stack = new StackImpl<>();

    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        DocumentImpl document = null;
        if(uri == null || format == null){ throw new IllegalArgumentException();}
        if(input == null){
            if (!exists(uri)){
                this.stack.push(nothing(uri));
                return 0;
            }
            int deletedHashCode = getDocumentAsTxt(uri).hashCode();
            deleteDocument(uri);
            return deletedHashCode;
        }
        byte [] contents = streamToByte(input);
        String text = extractText(contents, format);
        if(format == DocumentFormat.TXT){ document = new DocumentImpl(uri, text, text.hashCode()); }
        if(format == DocumentFormat.PDF){ document = new DocumentImpl(uri, text, text.hashCode(), contents); }
        if(exists(uri)){
            if(document.getDocumentTextHashCode() == this.hashTable.get(uri).getDocumentTextHashCode()){
                this.stack.push(nothing(uri));
                return document.getDocumentTextHashCode();}
            int replaceHash = getDocumentAsTxt(uri).hashCode();
            this.stack.push(putOverwriteCommand(uri, (DocumentImpl) getDocument(uri), document));
            this.hashTable.put(uri, document);
            return replaceHash;
        }
        this.hashTable.put(uri, document);
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
        StackImpl<Command> temp = new StackImpl<>();
        for (int i = 0; i <= this.stack.size(); i++) {
            if (this.stack.peek().getUri().hashCode() == uri.hashCode()) {
                this.stack.pop().undo();
                break;
            }
            temp.push(this.stack.pop());
        }
        for(int i = 0; i <= temp.size(); i++) {
            this.stack.push(temp.pop());
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
        return this.hashTable.get(uri);
    }

    /********************************************************************************
     * MY METHODS
     ********************************************************************************
     */

    private Command putNewCommand(URI uriKey, DocumentImpl doc){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, null);
            return true;
        };
        return new Command(uriKey,undo);
    }
    private Command putOverwriteCommand(URI uriKey, DocumentImpl oldDoc, DocumentImpl newDoc){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, oldDoc);
            return true;
        };
        return new Command(uriKey,undo);
    }
    private Command deleteCommand(URI uriKey, Document document){
        Function<URI, Boolean> undo = uri -> {
            this.hashTable.put(uri, (DocumentImpl) document);
            return true;
        };
        return new Command(uriKey, undo);
    }
    private Command nothing(URI uriKey){
        Function<URI, Boolean> undo = uri -> { return true; };
        return new Command(uriKey, undo);
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
        Document get = this.hashTable.get(uri);
        if(get == null){
            return false;
        }
        return true;
    }

}
