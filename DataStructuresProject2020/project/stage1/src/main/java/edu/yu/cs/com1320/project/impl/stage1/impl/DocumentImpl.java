package edu.yu.cs.com1320.project.impl.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;
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

import static org.apache.pdfbox.io.IOUtils.toByteArray;

public class DocumentImpl implements Document {
    private URI uri;
    private int hashCode;
    private String text;
    private byte[] pdfContent;

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

    }
    public DocumentImpl(URI uri,  String txt, int txtHash, byte[] pdfBytes){
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

    public boolean equals (DocumentImpl doc){
        if(this.uri.equals(doc.getKey()) && this.hashCode == doc.getDocumentTextHashCode()){
            return true;
        }
        return false;
    }
    protected byte [] textToPdf(String text){
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
}
