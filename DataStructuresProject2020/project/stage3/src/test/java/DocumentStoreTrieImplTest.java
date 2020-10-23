import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;

public class DocumentStoreTrieImplTest {
    DocumentStoreImpl store;

    @Before
    public void setUp() throws URISyntaxException {
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1 ", this.store);
        URI uri2 = makeDoc("This is is is test2", this.store);
        URI uri3 = makeDoc("This is is isis island test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
    }
    public URI makeDoc(String string, DocumentStore store) throws URISyntaxException {
        String test1 = string;
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        URI uri1 = new URI(test1.replaceAll(" ", ""));
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
    @Test
    public void searchTest() throws URISyntaxException {
        URI uriTest = makeDoc("This isis isis test4", this.store);
        //This one won't show up
        List<String> list = this.store.search("is");
        assertEquals(3, list.size());
        assertEquals("This is is is test2", list.get(0));
        assertEquals("This is is isis island test3", list.get(1));
        assertEquals("This is test1", list.get(2));
    }
    @Test
    public void searchPdfTest() throws URISyntaxException {
        List<byte[]> pdfList = this.store.searchPDFs("is");
        assertEquals(3, pdfList.size());
        List<String> list = new ArrayList<>();
        for (byte[] bytes: pdfList) {
            list.add(extractText(bytes));
        }
        assertEquals("This is is is test2", list.get(0));
        assertEquals("This is is isis island test3", list.get(1));
        assertEquals("This is test1", list.get(2));
    }
    @Test
    public void searchTestEmpty(){
        Set<URI> uri = store.deleteAll("is");
        List<String> list = this.store.search("is");
        List<byte[]> listPdf = this.store.searchPDFs("is");
        assertEquals(0, list.size());
        assertEquals(0, listPdf.size());
    }
    @Test
    public void prefixSearchTest() throws URISyntaxException {
        URI uriTest = makeDoc("This isis isis test4", this.store);
        List<String> list = this.store.searchByPrefix("is");
        assertEquals(4, list.size());
        assertEquals("This is is isis island test3", list.get(0));
        assertEquals("This is is is test2", list.get(1));
        assertEquals("This isis isis test4", list.get(2));
        assertEquals("This is test1", list.get(3));
    }
    @Test
    public void prefixSearchPDFTest() throws URISyntaxException {
        URI uriTest = makeDoc("This isis isis test4", this.store);
        List<byte[]> pdfList = this.store.searchPDFsByPrefix("is");
        assertEquals(4, pdfList.size());
        List<String> list = new ArrayList<>();
        for (byte[] bytes: pdfList) {
            list.add(extractText(bytes));
        }
        assertEquals("This is is isis island test3", list.get(0));
        assertEquals("This is is is test2", list.get(1));
        assertEquals("This isis isis test4", list.get(2));
        assertEquals("This is test1", list.get(3));
    }
    @Test
    public void prefixSearchEmptyTest(){
        Set<URI> uri = store.deleteAllWithPrefix("is");
        List<String> list = this.store.searchByPrefix("is");
        List<byte[]> listPdf = this.store.searchPDFsByPrefix("is");
        assertEquals(0, list.size());
        assertEquals(0, listPdf.size());
    }
    @Test
    public void deleteAllTest(){
        Set<URI> uris = store.deleteAll("is");
        List<String> list = this.store.search("is");
        List<String> listPrefix = this.store.searchByPrefix("is");
        assertEquals(0, list.size());
        assertEquals(0, listPrefix.size());
        for (URI deleted: uris) {
            assertEquals(null, store.getDocumentAsTxt(deleted));
        }
    }
    @Test
    public void deleteAllNothingTest(){
        Set<URI> uris = store.deleteAll("is");
        Set<URI> nothing = store.deleteAll("is");
        assertEquals(0, nothing.size());
    }
    @Test
    public void deleteAllPrefixTest(){
        Set<URI> uris = store.deleteAllWithPrefix("is");
        List<String> list = this.store.search("is");
        List<String> listPrefix = this.store.searchByPrefix("is");
        assertEquals(0, list.size());
        assertEquals(0, listPrefix.size());
        for (URI deleted: uris) {
            assertEquals(null, store.getDocumentAsTxt(deleted));
        }
    }
    @Test
    public void deleteAllPrefixNothingTest(){
        Set<URI> uris = store.deleteAllWithPrefix("is");
        Set<URI> nothing = store.deleteAllWithPrefix("is");
        assertEquals(0, nothing.size());
    }
    @Test
    public void nonLetterDoc() throws URISyntaxException {
        URI uri1 = makeDoc("hi! ho!w are you!", this.store);
        URI uri2 = makeDoc("hi! ho!w h?ow are you!", this.store);
        URI uri3 = makeDoc("hi! ho!w how h?o@w are you!", this.store);
        List<String> list = this.store.search("how");
        assertEquals("hi! ho!w are you!", list.get(2));
        assertEquals("hi! ho!w h?ow are you!", list.get(1));
        assertEquals("hi! ho!w how h?o@w are you!", list.get(0));
    }
    @Test
    public void deleteDocFromTrieTest() throws URISyntaxException {
        URI uri  = new URI("DovidGelbtuch");
        //make sure that it's in the trie
        List<String> dovid = this.store.search("Dovid");
        List<String> gelbtuch = this.store.search("Gelbtuch");
        List<String> d = this.store.searchByPrefix("D");
        List<String> g = this.store.searchByPrefix("G");
        assertEquals(dovid, gelbtuch);
        assertEquals(d, g);
        assertEquals(d, g);

        this.store.deleteDocument(uri);
        dovid = this.store.search("Dovid");
        gelbtuch = this.store.search("Gelbtuch");
        d = this.store.searchByPrefix("D");
        g = this.store.searchByPrefix("G");
        assertTrue(dovid.isEmpty());
        assertTrue(gelbtuch.isEmpty());
        assertTrue(d.isEmpty());
        assertTrue(g.isEmpty());
    }
}

