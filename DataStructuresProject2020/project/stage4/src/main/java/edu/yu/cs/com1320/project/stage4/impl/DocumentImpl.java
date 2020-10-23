package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.Document;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;

public class DocumentImpl implements Document {
    private URI uri;
    private int hashCode;
    private String text;
    private byte[] pdfContent;
    private HashMap<String, Integer> hashMap = new HashMap<>();
    private long lastTime;


    public DocumentImpl(URI uri, String txt, int txtHash){
        if(uri == null){
            throw new IllegalArgumentException();
        }
        if(txt == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.text = txt;
        this.hashCode = txtHash;
        this.setHashMap();

    }
    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes){
        if(uri == null){
            throw new IllegalArgumentException();
        }
        if(txt == null){
            throw new IllegalArgumentException();
        }
        this.uri = uri;
        this.pdfContent = pdfBytes;
        this.text = txt;
        this.hashCode = txtHash;
        this.setHashMap();
    }


    public byte[] getDocumentAsPdf() {
        if(this.pdfContent != null){
            return this.pdfContent;
        }
        return textToPdf(this.text);
    }

    public String getDocumentAsTxt() {
        return this.text;
    }

    public int getDocumentTextHashCode() {
        return this.hashCode;
    }

    public URI getKey() {
        return this.uri;
    }

    public int wordCount(String word) {
        word = word.trim();
        word = word.replaceAll("\\W", "");
        word = word.toUpperCase();
        if(this.hashMap.containsKey(word)){
            return this.hashMap.get(word);
        }
        return 0;
    }

    @Override
    public long getLastUseTime() {
        return this.lastTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastTime = timeInNanoseconds;
    }

    /********************************************************************************
     * MY METHODS
     ********************************************************************************
     */
    private boolean equals (DocumentImpl doc){
        if(this.uri.equals(doc.getKey()) && this.hashCode == doc.getDocumentTextHashCode()){
            return true;
        }
        return false;
    }

    private String[] getWords(){
        String text = this.getDocumentAsTxt();
        return text.split(" ");
    }
    private void setHashMap(){
        String[] wordArray = this.getWords();
        for (int i = 0; i < wordArray.length; i++) {
            wordArray[i] = wordArray[i].trim();
            wordArray[i] = wordArray[i].replaceAll("\\W", "");
            wordArray[i] = wordArray[i].toUpperCase();
            if(this.hashMap.containsKey(wordArray[i])){
                this.hashMap.put(wordArray[i], this.hashMap.get(wordArray[i]) + 1);
            }
            else{
                this.hashMap.put(wordArray[i], 1);
            }
        }
    }

    private byte [] textToPdf(String text){
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        PDPageContentStream contents = null;
        PDFont font = PDType1Font.TIMES_ROMAN;
        byte[] info = null;
        try {
            contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 12);
            contents.newLineAtOffset(100, 700);
            contents.showText(text);
            contents.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        doc.addPage(page);
        try {
            contents.close();
            info = docToByte(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pdfContent = info;
        return info;
    }

    private byte[] docToByte(PDDocument doc) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        doc.save(byteArrayOutputStream);
        doc.close();
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int compareTo(Document o) {
        if(this.getLastUseTime() > o.getLastUseTime()){
            return 1;
        }
        else if(this.getLastUseTime() < o.getLastUseTime()){
            return -1;
        }
        return 0;
    }
}
