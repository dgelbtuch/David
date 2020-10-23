package edu.yu.cs.com1320.project.impl.stage1.impl;


import edu.yu.cs.com1320.project.impl.HashTableImpl;

import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;


import static org.apache.pdfbox.pdmodel.PDDocument.load;

public class DocumentStoreImpl implements DocumentStore {
    private HashTableImpl<URI, DocumentImpl> hashTable = new HashTableImpl<URI, DocumentImpl>();

    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        byte [] contents = null;
        String text = null;
        DocumentImpl document = null;
        if(uri == null || format == null){ throw new IllegalArgumentException();}
        if(input == null){
            if (exists(uri) == false){ return 0; }
            int deletedHashCode = getDocumentAsTxt(uri).hashCode();
            deleteDocument(uri);
            return deletedHashCode;
        }
        try {
            contents = streamToByte(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        text = extractText(contents, format);
        if(format == DocumentFormat.TXT){ document = new DocumentImpl(uri, text, text.hashCode()); }
        if(format == DocumentFormat.PDF){ document = new DocumentImpl(uri, text, text.hashCode(), contents); }
        if(exists(uri) == true){
            if(document.getDocumentTextHashCode() == this.hashTable.get(uri).getDocumentTextHashCode()){return document.getDocumentTextHashCode();}
            int replaceHash = getDocumentAsTxt(uri).hashCode();
            this.hashTable.put(uri, document);
            return replaceHash;
        }
        this.hashTable.put(uri, document);
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
            return false;
        }
        this.hashTable.put(uri, null);
        return true;
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
    private byte[] streamToByte(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384];
        int len = input.read(data);
        while (len != -1) {
            buffer.write(data, 0, len);
            len = input.read(data);
        }
        buffer.flush();
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
