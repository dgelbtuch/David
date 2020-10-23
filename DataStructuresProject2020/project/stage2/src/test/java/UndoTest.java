import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


public class UndoTest {
    @Test
    public void undoPut() throws URISyntaxException {
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.undo();

        assertEquals(null, store.getDocumentAsTxt(uri2));
        store.undo();
        assertEquals(null, store.getDocumentAsTxt(uri1));
    }
    @Test
    public void undoPutUri() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.undo(uri1);
        assertEquals(null, store.getDocumentAsTxt(uri1));
        assertEquals("test2", store.getDocumentAsTxt(uri2));
    }
    @Test
    public void undoOverwrite() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        String test3 = "test3";
        String test4 = "test4";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        InputStream stream3 = new ByteArrayInputStream(test3.getBytes());
        InputStream stream4 = new ByteArrayInputStream(test4.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        URI uri3 = new URI(test3);
        URI uri4 = new URI(test4);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream3, uri3, DocumentStore.DocumentFormat.TXT);

        store.putDocument(stream2, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream4, uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals("test2", store.getDocumentAsTxt(uri1));
        assertEquals("test4", store.getDocumentAsTxt(uri3));
        store.undo();
        assertEquals("test3", store.getDocumentAsTxt(uri3));
        store.undo();
        assertEquals("test1", store.getDocumentAsTxt(uri1));

    }
    @Test
    public void undoOverwriteUri() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        String test3 = "test3";
        String test4 = "test4";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        InputStream stream3 = new ByteArrayInputStream(test3.getBytes());
        InputStream stream4 = new ByteArrayInputStream(test4.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        URI uri3 = new URI(test3);
        URI uri4 = new URI(test4);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream3, uri3, DocumentStore.DocumentFormat.TXT);

        store.putDocument(stream2, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream4, uri3, DocumentStore.DocumentFormat.TXT);
        assertEquals("test2", store.getDocumentAsTxt(uri1));
        assertEquals("test4", store.getDocumentAsTxt(uri3));
        store.undo(uri1);
        assertEquals("test1", store.getDocumentAsTxt(uri1));
        store.undo(uri3);
        assertEquals("test3", store.getDocumentAsTxt(uri3));
    }
    @Test
    public void undoDelete() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.undo();
        assertEquals("test2", store.getDocumentAsTxt(uri2));
        store.undo();
        assertEquals("test1", store.getDocumentAsTxt(uri1));
    }
    @Test
    public void undoDeleteUri() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        String test2 = "test2";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test2.getBytes());
        URI uri1 = new URI(test1);
        URI uri2 = new URI(test2);
        store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(stream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(uri1);
        store.deleteDocument(uri2);
        store.undo(uri1);
        assertEquals("test1", store.getDocumentAsTxt(uri1));
        store.undo(uri2);
        assertEquals("test2", store.getDocumentAsTxt(uri2));
    }
    @Test
    public void nothingUndo() throws URISyntaxException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        String test1 = "test1";
        InputStream stream1 = new ByteArrayInputStream(test1.getBytes());
        InputStream stream2 = new ByteArrayInputStream(test1.getBytes());
        URI uri1 = new URI(test1);
        boolean nothing = store.deleteDocument(uri1);
        assertEquals(false, nothing);
        store.undo();
        int newDoc  = store.putDocument(stream1, uri1, DocumentStore.DocumentFormat.TXT);
        int oldDoc  = store.putDocument(stream2, uri1, DocumentStore.DocumentFormat.TXT);
        assertFalse(newDoc == oldDoc);
        store.undo();
        assertEquals(test1, store.getDocumentAsTxt(uri1));

    }
    @Test(expected = IllegalStateException.class)
    public void exception() throws IllegalStateException{
        DocumentStoreImpl store = new DocumentStoreImpl();
        store.undo();
    }


}
