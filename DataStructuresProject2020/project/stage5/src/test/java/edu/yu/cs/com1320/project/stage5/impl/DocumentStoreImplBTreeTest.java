package edu.yu.cs.com1320.project.stage5.impl;
import com.google.gson.JsonIOException;
import edu.yu.cs.com1320.project.stage5.DocumentStore;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;

public class DocumentStoreImplBTreeTest {
    DocumentStoreImpl store = new DocumentStoreImpl();
    File baseDir = new File(System.getProperty("user.dir"));
    DocumentPersistenceManager pm = new DocumentPersistenceManager(baseDir);
    URI uri1, uri2, uri3, uri4;
    int bytes1,bytes2,bytes3, bytes4, bytes;


    @Before
    public void setUp() throws URISyntaxException {
        this.store = new DocumentStoreImpl();
        this.uri1 = makeDoc("This is test1", this.store, "/src/testing/test1");
        this.uri2 = makeDoc("This is is is test2", this.store,"/src/testing/test2");
        this.uri3 = makeDoc("This is is isis island test3", this.store,"/src/testing/test3");
        this.uri4 = makeDoc("Dovid Gelbtuch", this.store,"/src/testing/test4");
        this.bytes1 = this.store.getDocumentAsTxt(uri1).getBytes().length + this.store.getDocumentAsPdf(uri1).length;
        this.bytes2 = this.store.getDocumentAsTxt(uri2).getBytes().length + this.store.getDocumentAsPdf(uri2).length;
        this.bytes3 = this.store.getDocumentAsTxt(uri3).getBytes().length + this.store.getDocumentAsPdf(uri3).length;
        this.bytes4 = this.store.getDocumentAsTxt(uri4).getBytes().length + this.store.getDocumentAsPdf(uri4).length;

        this.bytes = this.bytes1 + this.bytes2 + this.bytes3 + this.bytes4;
    }
    public URI makeDoc(String string, DocumentStore store, String uri) throws URISyntaxException {
        String test1 = string;
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        URI uri1 = new URI(uri);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        return uri1;
    }
    private String uriPath(URI uri){
        String path = File.separator;
        if(uri.getRawAuthority() != null){
            path = File.separator + uri.getRawAuthority();
        }
        path += uri.getRawPath();
        if(!path.startsWith(File.separator)){
            path = File.separator + path;
        }
        return path;
    }
    private boolean checkOnDisk(URI uri){
        String path = this.uriPath(uri);
        File directory = new File(this.baseDir + path + ".json");
        if(directory.exists()){
            return true;
        }
        return false;
    }
    @Test
    public void limitTest() throws IOException {
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        this.pm.deserialize(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void byteLimitTest() throws IOException {
        this.store.setMaxDocumentBytes(bytes-1);
        assertTrue(checkOnDisk(uri1));
        this.pm.deserialize(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void puttingDocThatGoesOverLimit() throws URISyntaxException {
        this.store = new DocumentStoreImpl();
        this.store.setMaxDocumentCount(0);
        this.uri1 = makeDoc("This is test1", this.store, "/src/testing/test1");
        assertEquals(0, this.store.putDocument(null, uri1, DocumentStore.DocumentFormat.TXT));
        assertFalse(checkOnDisk(uri1));
    }

    @Test
    public void getTxtTest() throws IOException {
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));

        assertEquals("This is test1", store.getDocumentAsTxt(uri1));
        assertTrue(checkOnDisk(uri2));
        this.pm.deserialize(uri2);
        assertFalse(checkOnDisk(uri2));
    }
    @Test
    public void getPdfTest() throws IOException{
        //this.store.putDocument(null, uri2, DocumentStore.DocumentFormat.TXT);
        byte[] bytes1 = this.store.getDocumentAsPdf(uri1);
        this.store.getDocumentAsPdf(uri2);
        this.store.getDocumentAsPdf(uri3);
        this.store.getDocumentAsPdf(uri4);
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        assertEquals(bytes1.length, store.getDocumentAsPdf(uri1).length);
        assertTrue(checkOnDisk(uri2));
        this.pm.deserialize(uri2);
        assertFalse(checkOnDisk(uri2));
    }
    @Test
    public void getTextBytesTest() throws IOException {
        this.store.setMaxDocumentBytes(bytes-1);
        assertTrue(checkOnDisk(uri1));

        assertEquals("This is test1", store.getDocumentAsTxt(uri1));
        assertTrue(checkOnDisk(uri2));
        this.pm.deserialize(uri2);
        assertFalse(checkOnDisk(uri2));
    }
    @Test
    public void searchTest() throws IOException {
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertTrue(checkOnDisk(uri3));

        this.store.setMaxDocumentCount(3);

        this.store.search("is");
        assertTrue(checkOnDisk(uri4));
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri1));

        this.pm.deserialize(uri4);

    }
    @Test
    public void searchByBytesTest() throws URISyntaxException {
        this.store.getDocumentAsTxt(uri1);
        this.store.getDocumentAsTxt(uri2);
        this.store.getDocumentAsTxt(uri3);
        this.store.setMaxDocumentBytes(this.bytes - 1);

        assertTrue(checkOnDisk(uri4));
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));

        this.store.search("Dovid");
        assertTrue(checkOnDisk(uri1));
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
        this.store.deleteDocument(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void searchWithMultipleInMemory() throws URISyntaxException {
        this.store = new DocumentStoreImpl();
        this.uri1 = makeDoc("This is test1", this.store, "/src/testing/test1");
        this.uri2 = makeDoc("This is test2", this.store,"/src/testing/test2");
        this.uri3 = makeDoc("Dovid Gelbtuch", this.store,"/src/testing/test3");
        this.uri4 = makeDoc("hello", this.store,"/src/testing/test4");
        this.store.setMaxDocumentCount(2);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri4));
        this.store.search("is");
        assertTrue(checkOnDisk(uri3));
        assertTrue(checkOnDisk(uri4));
        assertFalse(checkOnDisk(uri1));
        assertFalse(checkOnDisk(uri2));

        this.store.deleteDocument(uri3);
        this.store.deleteDocument(uri4);
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri4));
    }
    @Test
    public void searchPdfTest() throws IOException {
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertTrue(checkOnDisk(uri3));

        this.store.setMaxDocumentCount(3);

        this.store.searchPDFs("is");
        assertTrue(checkOnDisk(uri4));
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri1));

        this.pm.deserialize(uri4);
    }
    @Test
    public void searchPrefix() throws IOException {
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertTrue(checkOnDisk(uri3));

        this.store.setMaxDocumentCount(3);

        this.store.searchByPrefix("test");
        assertTrue(checkOnDisk(uri4));
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri1));

        this.pm.deserialize(uri4);
    }
    @Test
    public void searchPrefixPdf() throws IOException {
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertTrue(checkOnDisk(uri3));

        this.store.setMaxDocumentCount(3);

        this.store.searchPDFsByPrefix("test");
        assertTrue(checkOnDisk(uri4));
        assertFalse(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri1));

        this.pm.deserialize(uri4);
    }
    @Test
    public void deleteTest(){
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        assertTrue(this.store.deleteDocument(uri1));
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void deleteNothing(){
        this.store = new DocumentStoreImpl();
        this.store.deleteDocument(uri1);
    }
    @Test
    public void deleteAllTest(){
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        assertTrue(checkOnDisk(uri3));
        assertFalse(checkOnDisk(uri4));

        this.store.deleteAll("is");
        assertFalse(checkOnDisk(uri1));
        assertFalse(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri3));
    }
    @Test
    public void undoPutTest(){
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        this.store.undo(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void undoDeleteTest(){
        this.store.deleteDocument(uri4);
        this.store.setMaxDocumentCount(3);
        this.store.undo();
        assertTrue(checkOnDisk(uri1));
        this.store.deleteDocument(uri1);
    }
    @Test
    public void overwriteTest() throws URISyntaxException {
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        URI uri = makeDoc("overwrite test", this.store,"/src/testing/test1");
        assertTrue(checkOnDisk(uri2));
        assertFalse(checkOnDisk(uri1));
        assertEquals("overwrite test", this.store.getDocumentAsTxt(uri1));
        this.store.deleteDocument(uri2);
    }
    @Test
    public void undoOverwriteTest() throws URISyntaxException {
        URI uri = makeDoc("overwrite test", this.store,"/src/testing/test1");
        assertEquals("overwrite test", this.store.getDocumentAsTxt(uri1));

        this.store.getDocumentAsTxt(uri2);
        this.store.getDocumentAsTxt(uri3);
        this.store.getDocumentAsTxt(uri4);

        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));

        this.store.undo(uri1);

        assertEquals("This is test1", this.store.getDocumentAsTxt(uri1));
        assertFalse(checkOnDisk(uri1));
        assertTrue(checkOnDisk(uri2));
        this.store.deleteDocument(uri2);

    }
    @Test
    public void newBaseDir() throws URISyntaxException, IOException {
        this.baseDir = new File (System.getProperty("user.dir") + "test");
        this.store = new DocumentStoreImpl(this.baseDir);
        this.pm = new DocumentPersistenceManager(this.baseDir);
        this.uri1 = makeDoc("This is test1", this.store, "/src/testing/test1");
        this.uri2 = makeDoc("This is is is test2", this.store,"/src/testing/test2");
        this.uri3 = makeDoc("This is is isis island test3", this.store,"/src/testing/test3");
        this.uri4 = makeDoc("Dovid Gelbtuch", this.store,"/src/testing/test4");
        this.store.setMaxDocumentCount(3);
        assertTrue(checkOnDisk(uri1));
        this.pm.deserialize(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void httpTest() throws URISyntaxException {
        this.store = new DocumentStoreImpl();
        URI uri = makeDoc("http", this.store, "http://www.yu.edu/documents/doc1");
        this.store.setMaxDocumentCount(0);
        File file = new File(System.getProperty("user.dir") + File.separator + "www.yu.edu/documents/doc1.json");
        assertTrue(checkOnDisk(uri));
        assertTrue(file.exists());
        this.store.deleteDocument(uri);
    }
    @Test
    public void httpTestWithBaseDir() throws URISyntaxException {
        this.baseDir = new File(System.getProperty("user.dir") + File.separator + "files");
        this.store = new DocumentStoreImpl(this.baseDir);
        URI uri = makeDoc("http", this.store, "http://www.yu.edu/documents/doc1");
        this.store.setMaxDocumentCount(0);
        File file = new File(this.baseDir.getPath() + File.separator + "www.yu.edu/documents/doc1.json");
        assertTrue(checkOnDisk(uri));
        assertTrue(file.exists());
        this.store.deleteDocument(uri);
    }
    @Test
    public void methodCount() {
        Method[] methods = DocumentStoreImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        System.out.println(publicMethodCount);
        assertTrue(publicMethodCount == 14);
    }
    @Test
    public void subClassCount() {
        @SuppressWarnings("rawtypes")
        Class[] classes = DocumentStoreImpl.class.getClasses();
        assertTrue(classes.length == 0);
    }
   
    @Test
    public void newBaseDir2() throws URISyntaxException {
        File file = new File(System.getProperty("user.home") + File.separator + "DS");
        this.baseDir = file;
        this.store = new DocumentStoreImpl(this.baseDir);
        this.uri1 = makeDoc("This is test1", this.store, "test");
        this.uri2 = makeDoc("This is is is test2", this.store,"test2");
        this.store.setMaxDocumentCount(1);
        assertTrue(checkOnDisk(uri1));
        this.store.deleteDocument(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void nullBase() throws URISyntaxException {
        this.store = new DocumentStoreImpl(null);
        this.uri1 = makeDoc("This is test1", this.store, "test");
        this.store.setMaxDocumentCount(0);

        assertTrue(checkOnDisk(uri1));
        this.store.deleteDocument(uri1);
        assertFalse(checkOnDisk(uri1));
    }
    @Test
    public void nonExistentBase() throws URISyntaxException {
        this.baseDir = new File("NonExistent");
        this.store = new DocumentStoreImpl(baseDir);
        this.uri1 = makeDoc("This is test1", this.store, "test");
        this.store.setMaxDocumentCount(0);

        assertTrue(checkOnDisk(uri1));
        this.store.deleteDocument(uri1);
        assertFalse(checkOnDisk(uri1));
    }






}
