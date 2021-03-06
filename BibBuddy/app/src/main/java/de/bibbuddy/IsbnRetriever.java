package de.bibbuddy;

import android.util.Log;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class IsbnRetriever implements Runnable {

  private static final String TAG = IsbnRetriever.class.getSimpleName();

  private final String isbn;

  private Book book;
  private List<Author> authors = new ArrayList<>();

  /**
   * IsbnRetriever connects to the API and returns metadata for an ISBN.
   *
   * @param isbn for which metadata should be returned
   * @author Luis Moßburger
   */
  IsbnRetriever(String isbn) {
    this.isbn = isbn.replaceAll("-", "").replaceAll("\\s", "");
  }

  private static Document loadXmlFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource inputSource = new InputSource(new StringReader(xml));

    return builder.parse(inputSource);
  }

  /**
   * Extracts a field from a xmlDocument.
   *
   * @param xmlMetadata which contains the field
   * @param fieldName   that should be extracted
   */
  private String getField(Document xmlMetadata, String fieldName) {
    String value = "";

    try {
      value = xmlMetadata.getElementsByTagName(fieldName).item(0).getTextContent();
    } catch (Exception ex) {
      // Exception for publication year, which needs an int
      if (fieldName.equals("dcterms:issued")) {
        value = "0";
      }
    }

    return value;
  }

  private Book createRecord(Document xmlMetadata) {
    return new Book(getField(xmlMetadata, "bibo:isbn"), // isbn
                    getField(xmlMetadata, "dc:title"), // title
                    getField(xmlMetadata, "isbd:P1006"), // subtitle
                    Integer.parseInt(getField(xmlMetadata, "dcterms:issued")), // pubYear
                    getField(xmlMetadata, "dcterms:publisher"), // publisher
                    "", // volume
                    getField(xmlMetadata, "bibo:edition"), // edition
                    getField(xmlMetadata, "dcterms:extent")); // addInfos
  }

  private List<Author> createAuthors(Document xmlMetadata) {
    AuthorRetriever authorRetriever = new AuthorRetriever();

    return authorRetriever.extractAuthors(xmlMetadata);
  }

  /**
   * Reads from API and resolve to metadata.
   */
  public void run() {

    Document xmlMetadata = null;
    String apiXmlParameter = "?output=xml";
    String apiUrl = "https://lod.b3kat.de/";
    String isbnApi = apiUrl + "data/isbn/%s" + apiXmlParameter;

    // Reads from API with ISBN
    ApiReader apiReader = new ApiReader(String.format(isbnApi, this.isbn));
    Thread thread = new Thread(apiReader);
    thread.start();

    try {
      thread.join();
    } catch (Exception ex) {
      Log.e(TAG, ex.toString(), ex);
    }

    // Retrieves metadata that was saved
    String metadata = apiReader.getMetadata();
    if (metadata != null) {
      try {
        xmlMetadata = loadXmlFromString(metadata);
      } catch (Exception ex) {
        Log.e(TAG, ex.toString(), ex);
      }

      if (xmlMetadata != null) {
        // Extracts url
        Node sameAsNode = xmlMetadata.getElementsByTagName("owl:sameAs").item(0);
        Element sameAsEl = (Element) sameAsNode;
        String sameAsUrl = sameAsEl.getAttribute("rdf:resource");
        String endUrl = apiUrl + "data/" + sameAsUrl.split(".de/")[1] + apiXmlParameter;

        // Reads from API with "BV-Nummer" (internal ID)
        apiReader = new ApiReader(endUrl);
        thread = new Thread(apiReader);
        thread.start();

        try {
          thread.join();
        } catch (Exception ex) {
          Log.e(TAG, ex.toString(), ex);
        }

        // Retrieves metadata that was saved
        metadata = apiReader.getMetadata();
        if (metadata != null) {
          try {
            xmlMetadata = loadXmlFromString(metadata);
          } catch (Exception ex) {
            Log.e(TAG, ex.toString(), ex);
          }

          authors = createAuthors(xmlMetadata);
          book = createRecord(xmlMetadata);
        }
      }
    }
  }

  public Book getBook() {
    return book;
  }

  public List<Author> getAuthors() {
    return authors;
  }

}
