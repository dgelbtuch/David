import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;
public class UndoTrieTest {
    DocumentStoreImpl store;
    public URI makeDoc(String string, DocumentStore store){
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
    @Test(expected = IllegalStateException.class)
    public void exception() throws IllegalStateException{
        this.store = new DocumentStoreImpl();
        this.store.undo();
    }

    @Test
    public void undoPutTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("CompSci 2020", this.store);
        URI uri3 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.search("is");
        assertEquals("This is test1", list.get(0));
        this.store.undo(uri1);
        list = this.store.search("is");
        assertTrue(list.isEmpty());
    }
    @Test
    public void undoDeleteTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("CompSci 2020", this.store);
        URI uri3 = makeDoc("Dovid Gelbtuch", this.store);
        this.store.deleteDocument(uri1);
        List<String> list = this.store.search("is");
        assertTrue(list.isEmpty());
        this.store.undo();
        list = this.store.search("is");
        assertEquals("This is test1", list.get(0));
    }
    @Test
    public void undoOverwriteTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        List<String> list = this.store.search("is");
        assertEquals("This is test1", list.get(0));

        InputStream stream1 = new ByteArrayInputStream("replacement test1".getBytes());
        this.store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        list = this.store.search("test1");
        assertEquals(1, list.size());
        assertEquals("replacement test1", list.get(0));

        this.store.undo();
        list = this.store.search("test1");
        assertEquals("This is test1", list.get(0));
    }
    @Test
    public void undoDeleteAllTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is is test2", this.store);
        URI uri3 = makeDoc("This is is is test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.search("is");
        assertEquals(3, list.size());

        this.store.deleteAll("is");
        list = this.store.search("is");
        assertEquals(0, list.size());

        this.store.undo();
        list = this.store.search("is");
        assertEquals(3, list.size());
        assertEquals("This is is is test3", list.get(0));
        assertEquals("This is is test2", list.get(1));
        assertEquals("This is test1", list.get(2));
    }
    @Test
    public void undoDeleteAllPrefixTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is isis test2", this.store);
        URI uri3 = makeDoc("This is isis island test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.searchByPrefix("is");
        assertEquals(3, list.size());

        this.store.deleteAllWithPrefix("is");
        list = this.store.searchByPrefix("is");
        assertEquals(0, list.size());

        this.store.undo();
        list = this.store.searchByPrefix("is");
        assertEquals(3, list.size());
        assertEquals("This is isis island test3", list.get(0));
        assertEquals("This is isis test2", list.get(1));
        assertEquals("This is test1", list.get(2));
    }
    @Test
    public void undoSpecificAfterDeleteAllTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is isis test2", this.store);
        URI uri3 = makeDoc("This is isis island test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.searchByPrefix("is");
        assertEquals(3, list.size());

        this.store.deleteAllWithPrefix("is");
        list = this.store.searchByPrefix("is");
        assertEquals(0, list.size());

        this.store.undo(uri2);
        list = this.store.searchByPrefix("is");
        assertEquals(1, list.size());
        assertEquals("This is isis test2", list.get(0));
    }
    @Test
    public void undoMultipleTest(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is is test2", this.store);
        URI uri3 = makeDoc("This is is is test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.search("is");
        assertEquals(3, list.size());

        this.store.deleteDocument(uri2) ;
        list = this.store.search("is");
        assertEquals(2, list.size());

        this.store.deleteAll("is");
        list = this.store.search("is");
        assertEquals(0, list.size());

        this.store.undo();
        list = this.store.search("is");
        assertEquals(2, list.size());
        assertEquals("This is is is test3", list.get(0));
        assertEquals("This is test1", list.get(1));

        this.store.undo(uri2);
        list = this.store.search("is");
        assertEquals(3, list.size());
        assertEquals("This is is is test3", list.get(0));
        assertEquals("This is is test2", list.get(1));
        assertEquals("This is test1", list.get(2));
    }
    @Test
    public void undoSpecificThenGeneral(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is is test2", this.store);
        URI uri3 = makeDoc("This is is is test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);
        List<String> list = this.store.search("is");
        assertEquals(3, list.size());

        this.store.undo(uri1);
        this.store.undo(uri3);
        this.store.undo(uri2);

        list = this.store.search("is");
        assertEquals(0, list.size());
        list = this.store.search("Dovid");
        assertEquals(1, list.size());

        this.store.undo();
        list = this.store.search("Dovid");
        assertEquals(0, list.size());
    }
    @Test
    public void undoSetThenGeneral(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is is test2", this.store);
        URI uri3 = makeDoc("This is is is test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);

        this.store.deleteDocument(uri4);
        this.store.deleteAll("is");

        this.store.undo(uri1);
        this.store.undo(uri3);
        this.store.undo(uri2);
        this.store.undo();
        List<String> list = this.store.search("is");
        assertEquals(3, list.size());
        list = this.store.search("Dovid");
        assertEquals(1, list.size());
    }
    @Test
    public void undoGeneralThenSet(){
        this.store = new DocumentStoreImpl();
        URI uri1 = makeDoc("This is test1", this.store);
        URI uri2 = makeDoc("This is is test2", this.store);
        URI uri3 = makeDoc("This is is is test3", this.store);
        URI uri4 = makeDoc("Dovid Gelbtuch", this.store);

        this.store.deleteAll("is");
        this.store.deleteDocument(uri4);


        this.store.undo();
        this.store.undo();

        List<String> list = this.store.search("is");
        assertEquals(3, list.size());
        list = this.store.search("Dovid");
        assertEquals(1, list.size());
    }
    @Test
    public void undoNothingTest(){
        this.store = new DocumentStoreImpl();
        this.store.deleteAll("hello");
        this.store.undo();

        this.store = new DocumentStoreImpl();
        this.store.deleteAllWithPrefix("hello");
        this.store.undo();
    }
}
