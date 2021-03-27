package de.bibbuddy;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * The AuthorRetriever class connects to the API and returns authors for a given book.
 *
 * @author Luis Moßburger
 */
public class AuthorRetriever {

  private final String autApiUrl = "https://d-nb.info/gnd/";
  private final String autApiParam = "/about/marcxml";

  private static Document loadXmlFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource inputSource = new InputSource(new StringReader(xml));

    return builder.parse(inputSource);
  }

  /**
   * Extracts authors from the given xmlDocument and gathers their names from an API.
   *
   * @param xmlMetadata XML metadata about a book, contains URL to author information
   * @return list of authors/relevant persons for this book
   * @author Luis Moßburger
   */
  public List<Author> extractAuthors(Document xmlMetadata) {
    List<Author> authors;
    NodeList authorList = null;
    NodeList[] relevantPersons = {
        xmlMetadata.getElementsByTagName("marcrel:aut"), // "authors"
        xmlMetadata.getElementsByTagName("dcterms:contributor"), // "contributors"
        xmlMetadata.getElementsByTagName("dcterms:creator"), // "creator"
        xmlMetadata.getElementsByTagName("marcrel:cmp") // "creator"
        };

    for (NodeList persons : relevantPersons) {
      if (persons.getLength() > 0) {
        authorList = persons;
      }
    }

    authors = makeAuthorList(authorList);
    return authors;
  }

  private List<Author> makeAuthorList(NodeList authors) {
    String url;
    Element autEl;
    List<Author> authorArray = new ArrayList<Author>();

    Thread thread;
    ApiReader apiReader;
    Document xmlMetadata = null;

    if (authors != null) {
      for (int i = 0; i < authors.getLength(); i++) {

        // read from API with isbn (Thread)
        autEl = (Element) authors.item(i);
        url = autEl.getAttribute("rdf:resource");
        apiReader = new ApiReader(this.autApiUrl + url.split("/gnd/")[1] + this.autApiParam);
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
            authorArray.add(constructAuthor(xmlMetadata));
          } catch (Exception e) {
            System.out.println(e);
          }
        }
      }
    }

    return authorArray;
  }

  private Author constructAuthor(Document xmlMetadata) {
    Author author = null;
    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      // in MARCXML, datafield "100" (suggested name for the person) is only present once
      // (cataloguing rules for libraries)
      XPathExpression expr = xpath.compile("//datafield[@tag=\"100\"]//subfield[@code=\"a\"]");
      Object exprResult = expr.evaluate(xmlMetadata, XPathConstants.NODESET);
      NodeList authorNameWrapper = (NodeList) exprResult;
      String authorName = authorNameWrapper.item(0).getTextContent();
      // MARCXML datafield "100" subfield code "a" is always in this form: Lastname, First Name
      author = new Author(authorName.split(",")[1].trim(), authorName.split(",")[0].trim());
    } catch (Exception e) {
      System.out.println(e);
    }

    return author;
  }

}
