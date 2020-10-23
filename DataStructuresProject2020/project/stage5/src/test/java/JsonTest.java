import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class JsonTest {
    @Test
    public void json() throws URISyntaxException, IOException {
        File file = new File(System.getProperty("user.dir"));
        DocumentPersistenceManager manager = new DocumentPersistenceManager(file);
        URI uri = new URI("/src/testing/file/test");
        String test = "test";
        DocumentImpl document = new DocumentImpl(uri, test,test.hashCode());
        manager.serialize(document.getKey(), document);
        DocumentImpl doc = (DocumentImpl) manager.deserialize(uri);
        assertEquals("test", doc.getDocumentAsTxt());
        assertEquals((Integer) 1, doc.getWordMap().get("TEST"));
    }
    @Test
    public void nonEmptyDir() throws URISyntaxException, IOException {
        File file = new File(System.getProperty("user.dir"));
        DocumentPersistenceManager manager = new DocumentPersistenceManager(file);

        URI uri = new URI("/src/testing/file/test");
        String test = "test";
        DocumentImpl document = new DocumentImpl(uri, test,test.hashCode());

        URI uri2 = new URI("/src/testing/file/test2");
        String test2 = "test2";
        DocumentImpl document2 = new DocumentImpl(uri2, test2,test2.hashCode());

        manager.serialize(uri,document);
        manager.serialize(uri2,document2);

        manager.deserialize(uri);
        String path = "/src/testing/file/";
        path = path.replaceAll("http://", File.separator);
        path = path.replaceAll("//", File.separator);
        File file2 = new File(System.getProperty("user.dir") + path);
        assertTrue(file2.list().length == 1);
        manager.deserialize(uri2);
    }
    @Test
    public void pathTest() throws URISyntaxException, IOException {
        File file = new File(System.getProperty("user.dir"));
        DocumentPersistenceManager manager = new DocumentPersistenceManager(file);
        URI uri = new URI("http://src/testing/file/test");
        String test = "test";
        DocumentImpl document = new DocumentImpl(uri, test,test.hashCode());
        manager.serialize(document.getKey(), document);
        DocumentImpl doc = (DocumentImpl) manager.deserialize(uri);
        assertEquals("test", doc.getDocumentAsTxt());
        assertEquals((Integer) 1, doc.getWordMap().get("TEST"));
    }
    @Test
    public void nothingThere() throws URISyntaxException, IOException {
        File file = new File(System.getProperty("user.dir"));
        DocumentPersistenceManager manager = new DocumentPersistenceManager(file);
        URI uri = new URI("/src/testing/file/test");
        assertNull(manager.deserialize(uri));
    }
//    @Test
//    public void test() throws URISyntaxException {
//        URI uri = new URI("http://www.yu.edu/documents/doc1");
//        System.out.println(uri.getAuthority());
//        System.out.println(uri.getRawAuthority()+uri.getRawPath());
//    }


}
