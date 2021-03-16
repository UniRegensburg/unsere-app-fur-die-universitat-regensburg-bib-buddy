package de.bibbuddy;

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
  private final String apiUrl = "https://lod.b3kat.de/";
  private final String apiXmlParameter = "?output=xml";
  private final String isbnApi = apiUrl + "data/isbn/%s" + apiXmlParameter;

  private final String isbn;
  private Book book = null;
  private List<Author> authors = new ArrayList<Author>();

  /**
   * The IsbnRetriever class connects to the API and returns metadata for an ISBN.
   *
   * @param isbn ISBN from which metadata should be returned
   * @author Luis Moßburger
   */
  IsbnRetriever(String isbn) {
    this.isbn = isbn;
  }

  private static Document loadXmlFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource inputSource = new InputSource(new StringReader(xml));

    return builder.parse(inputSource);
  }

  private String getField(Document xmlMetadata, String fieldName) {
    String value = "";

    try {
      value = xmlMetadata.getElementsByTagName(fieldName).item(0).getTextContent();
    } catch(Exception e) {
      // exception for publication year, which needs an int
      if (fieldName == "dcterms:issued") {
        value = "0";
      }
      System.out.println("Value of field '" + fieldName + "' could not be extracted.");
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
    List<Author> authors = new ArrayList<Author>();
    AuthorRetriever authorRetriever = new AuthorRetriever();
    authors = authorRetriever.extractAuthors(xmlMetadata);
    return authors;
  }

  /**
   * Main method for the Runnable to start - read from API and resolve to metadata.
   *
   * @author Luis Moßburger
   */
  public void run() {
    // initialize variables
    Thread thread;
    ApiReader apiReader;
    Document xmlMetadata = null;
    // read from API with isbn (Thread)
    apiReader = new ApiReader(String.format(this.isbnApi, this.isbn));
    thread = new Thread(apiReader);
    thread.start();

    try {
      thread.join();
    } catch (Exception e) {
      System.out.println(e);
    }

    // retrieve metadata that was saved
    String metadata = apiReader.getMetadata();
    if (metadata != null) {
      // parse xml
      try {
        xmlMetadata = loadXmlFromString(metadata);
      } catch (Exception e) {
        System.out.println(e);
      }

      // extract url
      Node sameAsNode = xmlMetadata.getElementsByTagName("owl:sameAs").item(0);
      Element sameAsEl = (Element) sameAsNode;
      String sameAsUrl = sameAsEl.getAttribute("rdf:resource");
      String endUrl = this.apiUrl + "data/" + sameAsUrl.split(".de/")[1] + apiXmlParameter;

      // read from API with bv-nr
      apiReader = new ApiReader(endUrl);
      thread = new Thread(apiReader);
      thread.start();

      try {
        thread.join();
      } catch (Exception e) {
        System.out.println(e);
      }

      // retrieve metadata that was saved
      metadata = apiReader.getMetadata();
      if (metadata != null) {
        // parse xml
        try {
          xmlMetadata = loadXmlFromString(metadata);
        } catch (Exception e) {
          System.out.println(e);
        }

        // create record & authors
        authors = createAuthors(xmlMetadata);
        book = createRecord(xmlMetadata);
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
