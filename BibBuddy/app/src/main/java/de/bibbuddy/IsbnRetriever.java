package de.bibbuddy;

import java.io.StringReader;
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
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
  }

  private Book createRecord(Document xmlMetadata) {
    long id = 0L;
    String isbn = xmlMetadata.getElementsByTagName("bibo:isbn").item(0).getTextContent();
    String title = xmlMetadata.getElementsByTagName("dc:title").item(0).getTextContent();
    String subttle = xmlMetadata.getElementsByTagName("dc:title").item(0).getTextContent();
    int pubYear = Integer
        .parseInt(xmlMetadata.getElementsByTagName("dcterms:issued").item(0).getTextContent());
    String publisher =
        xmlMetadata.getElementsByTagName("dcterms:publisher").item(0).getTextContent();
    String volume = "";
    String edition = "";
    String addInfos = "";
    int createDate = 0;
    int modDate = 0;

    Book book = new Book(id, isbn, title, subttle, pubYear, publisher, volume, edition, addInfos,
        createDate, modDate);
    return book;
  }

  /**
   * Main method for the Runnable to start - read from API and resolve to metadata.
   *
   * @author Luis Moßburger
   */
  public void run() {
    //initialize variables
    Thread thread;
    ApiReader apiReader;
    String metadata = "";
    Document xmlMetadata = null;
    //Read from API with isbn (Thread)
    apiReader = new ApiReader(String.format(this.isbnApi, this.isbn));
    thread = new Thread(apiReader);
    thread.start();
    try {
      thread.join();
    } catch (Exception e) {
      System.out.println(e);
    }
    //Retrieve metadata that was saved
    metadata = apiReader.getMetadata();
    if (metadata != null) {
      //parse xml
      try {
        xmlMetadata = loadXmlFromString(metadata);
      } catch (Exception e) {
        System.out.println(e);
      }

      //extract url
      Node sameAsNode = xmlMetadata.getElementsByTagName("owl:sameAs").item(0);
      Element sameAsEl = (Element) sameAsNode;
      String sameAsUrl = sameAsEl.getAttribute("rdf:resource");
      String endUrl = this.apiUrl + "data/" + sameAsUrl.split(".de/")[1] + apiXmlParameter;

      //Read from API with bv-nr
      apiReader = new ApiReader(endUrl);
      thread = new Thread(apiReader);
      thread.start();
      try {
        thread.join();
      } catch (Exception e) {
        System.out.println(e);
      }
      //Retrieve metadata that was saved
      metadata = apiReader.getMetadata();
      if (metadata != null) {
        //parse xml
        try {
          xmlMetadata = loadXmlFromString(metadata);
        } catch (Exception e) {
          System.out.println(e);
        }
        //create a record
        book = createRecord(xmlMetadata);
      }
    }
  }

  public Book getBook() {
    return book;
  }

}
