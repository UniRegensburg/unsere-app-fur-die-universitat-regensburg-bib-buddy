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
 * The AuthorRetriever class connects to the API and returns authors for a given book.
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
   * Extracts authors from the given xmlDocument and gathers their names from an API.
   *
   * @param xmlMetadata XML metadata about a book, contains URL to author information
   * @return list of authors/relevant persons for this book
   */
  public List<Author> extractAuthors(Document xmlMetadata) {
    NodeList[] relevantPersons = {
        xmlMetadata.getElementsByTagName("marcrel:aut"), // "authors"
        xmlMetadata.getElementsByTagName("dcterms:contributor"), // "contributors"
        xmlMetadata.getElementsByTagName("dcterms:creator"), // "creator"
        xmlMetadata.getElementsByTagName("marcrel:cmp") // "creator"
    };

    NodeList authorList = null;
    for (NodeList persons : relevantPersons) {
      if (persons.getLength() > 0) {
        authorList = persons;
      }
    }

    return makeAuthorList(authorList);
  }

  private List<Author> makeAuthorList(NodeList authors) {
    List<Author> authorArray = new ArrayList<>();
    Document xmlMetadata;

    if (authors == null) {
      return authorArray;
    }

    for (int i = 0; i < authors.getLength(); i++) {

      // read from API with isbn (Thread)
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

      // retrieve metadata that was saved
      String metadata = apiReader.getMetadata();
      if (metadata != null) {
        // parse xml
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
