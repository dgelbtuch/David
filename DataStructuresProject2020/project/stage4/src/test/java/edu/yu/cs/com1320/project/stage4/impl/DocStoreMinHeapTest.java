package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.impl.Utils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static edu.yu.cs.com1320.project.stage4.impl.Utils.textToPdfData;
import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;

public class DocStoreMinHeapTest {
    DocumentStoreImpl store;
    URI uri1;
    URI uri2;
    URI uri3;
    URI uri4;
    int bytes;

    @Before
    public void setUp() {
        this.store = new DocumentStoreImpl();
        this.uri1 = makeDoc("This is test1", this.store);
        this.uri2 = makeDoc("This is is is test2", this.store);
        this.uri3 = makeDoc("This is is isis island test3", this.store);
        this.uri4 = makeDoc("Dovid Gelbtuch", this.store);
        this.bytes = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        this.bytes += this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        this.bytes += this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        this.bytes += this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;
    }
    public URI makeDoc(String string, DocumentStore store) {
        String test1 = string;
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        URI uri1 = null;
        try {
            uri1 = new URI(test1.replaceAll(" ", ""));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        return uri1;
    }
    public String extractText(byte[] bytes){
        String text = "";
        try {
            PDDocument doc = load(bytes);
            PDFTextStripper strip = new PDFTextStripper();
            text = strip.getText(doc);
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.trim();
    }
    public int getTotalBytes(URI uri){
        int totalBytes = this.store.getDocumentAsTxt(uri).getBytes().length + this.store.getDocumentAsPdf(uri).length;
        return totalBytes;
    }
    @Test
    public void overDocLimitTest(){
        this.store.setMaxDocumentCount(4);
        this.store.search("is");
        URI uri5 = makeDoc("new", this.store);
        assertEquals("This is test1", this.store.getDocument(uri1).getDocumentAsTxt());
        assertEquals("This is is is test2", this.store.getDocument(uri2).getDocumentAsTxt());
        assertEquals("This is is isis island test3", this.store.getDocument(uri3).getDocumentAsTxt());
        assertEquals("new", this.store.getDocument(uri5).getDocumentAsTxt());
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void overByteLimit() throws IOException {
        int bytes;
        bytes = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        bytes += this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        bytes += this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        bytes += this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;
        String test = "new";
        int testBytes = test.getBytes().length + textToPdfData(test).length;
        this.store.setMaxDocumentBytes(bytes + testBytes - 1);
        this.store.search("is");
        URI uri5 = makeDoc("new", this.store);
        assertEquals("This is test1", this.store.getDocument(uri1).getDocumentAsTxt());
        assertEquals("This is is is test2", this.store.getDocument(uri2).getDocumentAsTxt());
        assertEquals("This is is isis island test3", this.store.getDocument(uri3).getDocumentAsTxt());
        assertEquals("new", this.store.getDocument(uri5).getDocumentAsTxt());
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void overAnyLimit1() throws IOException {
        int bytes;
        bytes = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        bytes += this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        bytes += this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        bytes += this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;
        String test = "new";
        int testBytes = test.getBytes().length + textToPdfData(test).length;
        this.store.setMaxDocumentCount(4);
        this.store.setMaxDocumentBytes(bytes * 2);
        this.store.search("is");
        URI uri5 = makeDoc("new", this.store);
        assertEquals("This is test1", this.store.getDocument(uri1).getDocumentAsTxt());
        assertEquals("This is is is test2", this.store.getDocument(uri2).getDocumentAsTxt());
        assertEquals("This is is isis island test3", this.store.getDocument(uri3).getDocumentAsTxt());
        assertEquals("new", this.store.getDocument(uri5).getDocumentAsTxt());
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void overAnyLimit2() throws IOException {
        int bytes;
        bytes = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        bytes += this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        bytes += this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        bytes += this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;
        String test = "new";
        int testBytes = test.getBytes().length + textToPdfData(test).length;
        this.store.setMaxDocumentCount(5);
        this.store.setMaxDocumentBytes(bytes + testBytes - 1);
        this.store.search("is");
        URI uri5 = makeDoc("new", this.store);
        assertEquals("This is test1", this.store.getDocument(uri1).getDocumentAsTxt());
        assertEquals("This is is is test2", this.store.getDocument(uri2).getDocumentAsTxt());
        assertEquals("This is is isis island test3", this.store.getDocument(uri3).getDocumentAsTxt());
        assertEquals("new", this.store.getDocument(uri5).getDocumentAsTxt());
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void overByteLimitWithLargeDoc() throws IOException {
        int bytes;
        bytes = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        bytes += this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        bytes += this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        bytes += this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;
        String test = "My name is Dovid Gelbtuch, and I will make a Doc that uses a lot of bytes for my test code for stage four. My name is Dovid Gelbtuch, and I will make a Doc that uses a lot of bytes for my test code for stage four. My name is Dovid Gelbtuch, and I will make a Doc that uses a lot of bytes for my test code for stage four.";
        int testBytes = test.getBytes().length + textToPdfData(test).length;
        this.store.setMaxDocumentBytes(bytes);
        this.store.search("is");
        this.store.search("test1");
        this.store.search("test2");
        URI uri5 = makeDoc(test, this.store);
        assertEquals("This is test1", this.store.getDocument(uri1).getDocumentAsTxt());
        assertEquals("This is is is test2", this.store.getDocument(uri2).getDocumentAsTxt());
        //assertEquals("This is is isis island test3", this.store.getDocument(uri3).getDocumentAsTxt());
        assertEquals(test, this.store.getDocument(uri5).getDocumentAsTxt());
        assertNull(this.store.getDocument(this.uri4));
        assertNull(this.store.getDocument(this.uri3));
    }
    @Test
    public void setLimitLowerThanDocuments(){
        this.store.setMaxDocumentCount(0);
        assertNull(this.store.getDocument(this.uri1));
        assertNull(this.store.getDocument(this.uri2));
        assertNull(this.store.getDocument(this.uri3));
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void setLimitLowerThanBytes() {
        this.store.setMaxDocumentBytes(0);
        assertNull(this.store.getDocument(this.uri1));
        assertNull(this.store.getDocument(this.uri2));
        assertNull(this.store.getDocument(this.uri3));
        assertNull(this.store.getDocument(this.uri4));
    }
    @Test
    public void deleteDoc(){
        this.store.deleteDocument(uri1);
        assertNull(this.store.getDocument(uri1));
        this.store.setMaxDocumentCount(2);
        assertNull(this.store.getDocument(uri2));
    }
    @Test
    public void putInTooBigDoc(){
        this.store = new DocumentStoreImpl();
        this.store.setMaxDocumentCount(0);
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri));

        this.store = new DocumentStoreImpl();
        this.store.setMaxDocumentBytes(0);
        URI uri2 = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri2));
    }
    @Test
    public void docPdfUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.getDocumentAsPdf(uri1);
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri2));
    }
    @Test
    public void docTxtUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.getDocumentAsTxt(uri1);
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri2));
    }
    @Test
    public void searchUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.search("is");
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void searchPdfUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.searchPDFs("is");
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void searchPrefixUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.searchByPrefix("test");
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void searchPrefixPdfUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.searchPDFsByPrefix("test");
        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void deleteAllUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.deleteAll("is");
        URI uri5 = makeDoc("test5", this.store);
        URI uri6 = makeDoc("test6", this.store);
        URI uri7 = makeDoc("test7", this.store);
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
        assertEquals(uri5, this.store.getDocument(uri5).getKey());
        assertEquals(uri6, this.store.getDocument(uri6).getKey());
        assertEquals(uri7, this.store.getDocument(uri7).getKey());

        URI uri8 = makeDoc("test8", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void deleteAllPrefixUpdateTime(){
        this.store.setMaxDocumentCount(4);
        this.store.deleteAllWithPrefix("test");
        URI uri5 = makeDoc("test5", this.store);
        URI uri6 = makeDoc("test6", this.store);
        URI uri7 = makeDoc("test7", this.store);
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
        assertEquals(uri5, this.store.getDocument(uri5).getKey());
        assertEquals(uri6, this.store.getDocument(uri6).getKey());
        assertEquals(uri7, this.store.getDocument(uri7).getKey());

        URI uri8 = makeDoc("test8", this.store);
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void multipleActions(){
        //trying to delete uri2
        this.store.setMaxDocumentCount(4);

        this.store.getDocument(uri4);
        this.store.getDocumentAsTxt(uri2);
        this.store.search("test1");
        this.store.getDocumentAsPdf(uri3);

        this.store.getDocumentAsPdf(uri1);
        this.store.searchPDFs("test3");
        this.store.getDocumentAsTxt(uri4);

        URI uri = makeDoc("test", this.store);
        assertNull(this.store.getDocument(uri2));
    }

    @Test
    public void undoPutTime(){
        this.store.setMaxDocumentCount(4);
        this.store.undo();
        URI uri = makeDoc("test", this.store);
        assertEquals(uri1, this.store.getDocument(uri1).getKey());
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri, this.store.getDocument(uri).getKey());
        assertNull(this.store.getDocument(uri4));
    }
    @Test
    public void undoDeleteTime(){
        this.store.deleteDocument(uri4);
        this.store.setMaxDocumentCount(3);

        this.store.undo();
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
        assertNull(this.store.getDocument(uri1));
    }
    @Test (expected = IllegalStateException.class)
    public void removedFromCommandStack(){
        this.store = new DocumentStoreImpl();
        URI uri5 = makeDoc("test5", this.store);
        this.store.setMaxDocumentCount(0);
        this.store.undo();
    }
    @Test (expected = IllegalStateException.class)
    public void removedFromCommandStackSet(){
        this.store = new DocumentStoreImpl();
        URI uri5 = makeDoc("test5", this.store);
        this.store.search("test5");
        this.store.setMaxDocumentCount(0);
        this.store.undo();
    }
    @Test
    public void removedFromCommandStack2() {
        this.store = new DocumentStoreImpl();
        this.store.setMaxDocumentCount(2);
        URI uri5 = makeDoc("test5", this.store);
        URI uri6 = makeDoc("test6", this.store);
        this.store.getDocumentAsTxt(uri6);
        URI uri7 = makeDoc("test7", this.store);
        this.store.undo();
        assertNull(this.store.getDocument(uri7));
        this.store.undo();
        assertNull(this.store.getDocument(uri5));
    }
    @Test(expected = IllegalStateException.class)
    public void removedFromCommandStackSet2(){
        this.store = new DocumentStoreImpl();
        this.uri4 = makeDoc("Dovid Gelbtuch", this.store);
        this.uri1 = makeDoc("This is test1", this.store);
        this.uri2 = makeDoc("This is is is test2", this.store);
        this.store.deleteAll("is");
        String test1 = "This is test1";
        String test2 = "This is is is test2";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());

        store.putDocument(stream1, this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream2, this.uri2, DocumentStore.DocumentFormat.TXT);

        this.store.getDocumentAsTxt(uri4);
        this.store.setMaxDocumentCount(1);
        this.store.setMaxDocumentCount(3);

        this.store.undo();
        this.store.undo();

    }
    @Test
    public void undoOverAmount(){
        this.store.deleteDocument(uri4);
        this.store.setMaxDocumentCount(3);
        this.store.undo();
        assertNull(this.store.getDocument(uri1));
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
    }
    @Test
    public void overwriteDoc() {
        this.store.setMaxDocumentBytes(this.bytes);
        String test = "Dovid Gelbtuch is testing";
        InputStream stream = new ByteArrayInputStream(test.getBytes());
        store.putDocument(stream, this.uri4, DocumentStore.DocumentFormat.TXT);
        assertNull(this.store.getDocument(uri1));
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
    }
    @Test
    public void undoOverwriteDoc() {
        String test = "";
        InputStream stream = new ByteArrayInputStream(test.getBytes());
        store.putDocument(stream, this.uri4, DocumentStore.DocumentFormat.TXT);
        this.store.setMaxDocumentBytes(this.bytes - 1);
        assertEquals(uri1, this.store.getDocument(uri1).getKey());
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
        this.store.undo();
        assertNull(this.store.getDocument(uri1));
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
    }
    @Test
    public void duplicateDocUpdate(){
        String test = "This is test1";
        InputStream stream = new ByteArrayInputStream(test.getBytes());
        store.putDocument(stream, this.uri1, DocumentStore.DocumentFormat.TXT);
        this.store.setMaxDocumentCount(3);
        assertEquals(uri1, this.store.getDocument(uri1).getKey());
        assertNull(this.store.getDocument(uri2));
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
    }
    @Test
    public void overBytesWitOtherDocs(){
        this.store.setMaxDocumentBytes(this.bytes);
        String all = this.store.getDocumentAsTxt(uri1);
        all += this.store.getDocumentAsTxt(uri2);
        all += this.store.getDocumentAsTxt(uri3);
        all += this.store.getDocumentAsTxt(uri4);
        all += "This doc has larger than the limit while there are other docs in the store.";
        for (int i = 0; i < 10; i++) {
            all += all;
        }
        URI uri = makeDoc(all, this.store);
        assertEquals(uri1, this.store.getDocument(uri1).getKey());
        assertEquals(uri2, this.store.getDocument(uri2).getKey());
        assertEquals(uri3, this.store.getDocument(uri3).getKey());
        assertEquals(uri4, this.store.getDocument(uri4).getKey());
    }
}
