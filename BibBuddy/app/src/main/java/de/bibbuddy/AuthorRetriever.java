package de.bibbuddy;

import android.util.Log;
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
 * AuthorRetriever connects to the GND API and returns author data for a given book.
 *
 * @author Luis Moßburger
 */
public class AuthorRetriever {

  private static final String TAG = AuthorRetriever.class.getSimpleName();

  private static Document loadXmlFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource inputSource = new InputSource(new StringReader(xml));

    return builder.parse(inputSource);
  }

  /**
   * Extracts authors from a xmlDocument and gathers their data from an API.
   *
   * @param xmlMetadata about a book
   * @return list of most relevant persons for this book
   */
  public List<Author> extractAuthors(Document xmlMetadata) {
    NodeList[] relevantPersons = {
        xmlMetadata.getElementsByTagName("marcrel:aut"), // "authors"
        xmlMetadata.getElementsByTagName("dcterms:contributor"), // "contributors"
        xmlMetadata.getElementsByTagName("dcterms:creator"), // "creator"
        xmlMetadata.getElementsByTagName("marcrel:cmp") // "composer"
    };

    NodeList authorList = null;
    for (NodeList persons : relevantPersons) {
      if (persons.getLength() > 0) {
        authorList = persons;
        break;
      }
    }

    return makeAuthorList(authorList);
  }

  /**
   * Creates a list of authors.
   *
   * @param authors wrapped in xmlNodes
   * @return list of extracted author objects
   */
  private List<Author> makeAuthorList(NodeList authors) {
    List<Author> authorArray = new ArrayList<>();
    Document xmlMetadata;

    if (authors == null) {
      return authorArray;
    }

    for (int i = 0; i < authors.getLength(); i++) {

      // Reads from API with isbn (Thread)
      Element authorEl = (Element) authors.item(i);
      String url = authorEl.getAttribute("rdf:resource");
      String autApiUrl = "https://d-nb.info/gnd/";
      String autApiParam = "/about/marcxml";
      ApiReader apiReader = new ApiReader(autApiUrl + url.split("/gnd/")[1] + autApiParam);
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
        // Parses xml
        try {
          xmlMetadata = loadXmlFromString(metadata);
          authorArray.add(constructAuthor(xmlMetadata));
        } catch (Exception ex) {
          Log.e(TAG, ex.toString(), ex);
        }
      }
    }

    return authorArray;
  }

  /**
   * Creates author object from XML data.
   *
   * @param xmlMetadata about a person
   * @return author object for this person
   */
  private Author constructAuthor(Document xmlMetadata) {
    Author author = null;
    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      // In MARCXML, datafield "100" (suggested name for the person) is only present once
      // (cataloguing rules for libraries)
      XPathExpression expr = xpath.compile("//datafield[@tag=\"100\"]//subfield[@code=\"a\"]");
      Object exprResult = expr.evaluate(xmlMetadata, XPathConstants.NODESET);
      NodeList authorNameWrapper = (NodeList) exprResult;
      String authorName = authorNameWrapper.item(0).getTextContent();

      // MARCXML datafield "100" subfield code "a" normally is in this form: Lastname, First Name
      // In some cases, the person has a "Eigenname", like "Marc Aurel" and therefore no comma
      // In that case, we split by the last space in the name
      if (authorName.contains(",")) {
        author = new Author(authorName.split(",")[1].trim(), authorName.split(",")[0].trim());
      } else if (authorName.contains(" ")) {
        int posOfLastSpace = authorName.lastIndexOf(" ");
        author = new Author(authorName.substring(0, posOfLastSpace).trim(),
                            authorName.substring(posOfLastSpace).trim());
      } else {
        author = new Author(authorName, " ");
      }
    } catch (Exception ex) {
      Log.e(TAG, ex.toString(), ex);
    }

    return author;
  }

}
