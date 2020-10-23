import edu.yu.cs.com1320.project.impl.stage1.impl.*;
import edu.yu.cs.com1320.project.stage1.DocumentStore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Before;
import org.junit.Test;


import static org.apache.pdfbox.pdmodel.PDDocument.load;
import static org.junit.Assert.*;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class DocTest {
    DocumentStoreImpl store;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        this.store = new DocumentStoreImpl();

        String testString1 = "TEST_STRING_1";
        String testString2 = "TEST_STRING_2";
        String testString3 = "TEST_STRING_3";
        String testString4 = "TEST_STRING_4";
        String testString5 = "TEST_STRING_5";
        String testString6 = "TEST_STRING_6";
        PDDocument testDoc = makePdf("This is a test pdf");
        testDoc.save(System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test" + File.separator + "testDoc.pdf");
        InputStream in  = docToStream(testDoc);
        URI pdfUri = new URI("pdf");
        URI uri1 = new URI(testString1);
        URI uri2 = new URI(testString2);
        URI uri3 = new URI(testString3);
        URI uri4 = new URI(testString4);
        URI uri5 = new URI(testString5);
        URI uri6 = new URI(testString6);
        InputStream targetStream1 = new ByteArrayInputStream(testString1.getBytes());
        InputStream targetStream2 = new ByteArrayInputStream(testString2.getBytes());
        InputStream targetStream3 = new ByteArrayInputStream(testString3.getBytes());
        InputStream targetStream4 = new ByteArrayInputStream(testString4.getBytes());
        InputStream targetStream5 = new ByteArrayInputStream(testString5.getBytes());
        InputStream targetStream6 = new ByteArrayInputStream(testString6.getBytes());
        store.putDocument(in, pdfUri, DocumentStore.DocumentFormat.PDF);
        store.putDocument(targetStream1, uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(targetStream2, uri2, DocumentStore.DocumentFormat.TXT);
        store.putDocument(targetStream3, uri3, DocumentStore.DocumentFormat.TXT);
        store.putDocument(targetStream4, uri4, DocumentStore.DocumentFormat.TXT);
        store.putDocument(targetStream5, uri5, DocumentStore.DocumentFormat.TXT);
        store.putDocument(targetStream6, uri6, DocumentStore.DocumentFormat.TXT);
    }
    @Test
    public void testGetTxt() throws URISyntaxException {
        String compare1 = store.getDocumentAsTxt(new URI("TEST_STRING_1"));
        String compare2 = store.getDocumentAsTxt(new URI("TEST_STRING_2"));
        String compare3 = store.getDocumentAsTxt(new URI("TEST_STRING_3"));
        String compare4 = store.getDocumentAsTxt(new URI("TEST_STRING_4"));
        String compare5 = store.getDocumentAsTxt(new URI("TEST_STRING_5"));
        String compare6 = store.getDocumentAsTxt(new URI("TEST_STRING_6"));
        assertEquals("TEST_STRING_1", compare1);
        assertEquals("TEST_STRING_2", compare2);
        assertEquals("TEST_STRING_3", compare3);
        assertEquals("TEST_STRING_4", compare4);
        assertEquals("TEST_STRING_5", compare5);
        assertEquals("TEST_STRING_6", compare6);
    }
    @Test
    public void testGetPdfAndText() throws URISyntaxException, IOException {
        byte[] pdfByte = store.getDocumentAsPdf(new URI("pdf"));
        File file = new File(System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test"+ File.separator + "testDoc.pdf");
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        String fileExpect = extractText(bytes, DocumentStore.DocumentFormat.PDF).trim();
        String expectedString = store.getDocumentAsTxt(new URI("pdf"));
        String resultString = extractText(pdfByte, DocumentStore.DocumentFormat.PDF).trim();
        assertEquals(expectedString, resultString);
        assertEquals(expectedString, fileExpect);
    }
    @Test
    public void newValue() throws URISyntaxException {
        String testString1 = "TEST_STRING_1";
        URI uri1 = new URI(testString1);
        assertEquals(testString1, store.getDocumentAsTxt(uri1));

        String changedText = "This is the changed text";
        InputStream targetStream1 = new ByteArrayInputStream(changedText.getBytes());
        store.putDocument(targetStream1, uri1, DocumentStore.DocumentFormat.TXT);
        assertEquals(changedText, store.getDocumentAsTxt(uri1));
    }
    @Test
    public void testDelete() throws URISyntaxException {
        String compare5 = store.getDocumentAsTxt(new URI("TEST_STRING_5"));
        String compare6 = store.getDocumentAsTxt(new URI("TEST_STRING_6"));
        assertEquals("TEST_STRING_5", compare5);
        assertEquals("TEST_STRING_6", compare6);
        store.putDocument(null, new URI("TEST_STRING_6"), DocumentStore.DocumentFormat.TXT);
        store.deleteDocument(new URI("TEST_STRING_5"));
        int deleteNothing = store.putDocument(null, new URI("nothing"), DocumentStore.DocumentFormat.TXT);
        String delete6 = store.getDocumentAsTxt(new URI("TEST_STRING_6"));
        String delete5 = store.getDocumentAsTxt(new URI("TEST_STRING_5"));
        //assertEquals(null, delete5);
        assertEquals(null, delete6);
        assertEquals(0, deleteNothing);

    }
    @Test
    public void testGetTextAsPdf() throws IOException, URISyntaxException {
        //This test take a TXT file turns it into a pdf, a rewrites an old PDF file
        PDDocument questionDoc = makePdf("What is your favorite flavor of pringles?");
        questionDoc.save(System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test" + File.separator + "questionDoc.pdf");
        InputStream in  = docToStream(questionDoc);

        String answer = "Mine is Barbecue!";
        InputStream answerStream = new ByteArrayInputStream(answer.getBytes());
        store.putDocument(answerStream, new URI("answer"), DocumentStore.DocumentFormat.TXT);
        store.putDocument(in, new URI("question"), DocumentStore.DocumentFormat.PDF);


        String answerText = store.getDocumentAsTxt(new URI("answer"));
        String questionText = store.getDocumentAsTxt(new URI("question"));
        assertEquals("Mine is Barbecue!", answerText);
        assertEquals("What is your favorite flavor of pringles?", questionText);

        PDDocument reWrittenDoc = load(store.getDocumentAsPdf(new URI("answer")));
        reWrittenDoc.save(System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test" + File.separator + "questionDoc.pdf");
        reWrittenDoc.close();
        File rewrittenFile = new File(System.getProperty("user.dir") + File.separator + "src"+ File.separator + "test" + File.separator + "questionDoc.pdf");
        FileInputStream rewrittenStream = new FileInputStream(rewrittenFile);
        store.putDocument(rewrittenStream, new URI("rewrite"), DocumentStore.DocumentFormat.PDF);
        assertEquals("Mine is Barbecue!", store.getDocumentAsTxt(new URI("rewrite")));

    }
    @Test
    public void putReturnValues() throws URISyntaxException {
        String testString7 = "TEST_STRING_7";
        URI uri7 = new URI(testString7);
        InputStream targetStream7 = new ByteArrayInputStream(testString7.getBytes());
        int resultNew = store.putDocument(targetStream7, uri7, DocumentStore.DocumentFormat.TXT);
        assertEquals(0, resultNew);

        int resultDelete = store.putDocument(null, uri7, DocumentStore.DocumentFormat.TXT);
        assertEquals(resultDelete, testString7.hashCode());

        String testString6 = "TEST_STRING_6";
        URI uri6 = new URI(testString6);
        InputStream targetStream6 = new ByteArrayInputStream(testString6.getBytes());
        int resultOld = store.putDocument(targetStream6, uri6, DocumentStore.DocumentFormat.TXT);
        assertEquals(testString6.hashCode(), resultOld);

        String replace = "replace";
        InputStream targetReplace = new ByteArrayInputStream(replace.getBytes());
        int replaceHash = store.putDocument(targetReplace, uri6, DocumentStore.DocumentFormat.TXT);
        assertEquals(testString6.hashCode(), replaceHash);

        int nothing = store.putDocument(null, new URI("nothing"), DocumentStore.DocumentFormat.TXT);
        assertEquals(0, nothing);



    }

    private String extractText (byte[] contents, DocumentStore.DocumentFormat format) {
        String text = null;
        if (format == DocumentStore.DocumentFormat.TXT) {
            text = new String(contents);
        }
        if (format == DocumentStore.DocumentFormat.PDF) {
            try {
                PDDocument doc = load(contents);
                PDFTextStripper strip = new PDFTextStripper();
                text = strip.getText(doc);
                doc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    private PDDocument makePdf(String text) throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        PDPageContentStream contents = null;
        PDFont font = PDType1Font.TIMES_ROMAN;

        contents = new PDPageContentStream(doc, page);
        contents.beginText();
        contents.setFont(font, 12);
        contents.newLineAtOffset(100, 700);
        contents.showText(text);
        contents.endText();

        doc.addPage(page);
        contents.close();
        return doc;
    }
    public byte[] getByteArray(PDDocument doc) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        doc.save(byteArrayOutputStream);
        doc.close();
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return byteArrayOutputStream.toByteArray();
    }
    public InputStream docToStream(PDDocument doc) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        doc.save(byteArrayOutputStream);
        doc.close();
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return inputStream;
    }
}
