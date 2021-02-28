package de.bibbuddy;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * The IsbnRetriever class connects to the API and returns metadata for an ISBN.
 *
 * @author Luis Moßburger
 */
public class AuthorRetriever {

  private final String autApiUrl = "https://d-nb.info/gnd/";
  private final String autApiParam = "/about/marcxml";

  private static Document loadXmlFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));

    return builder.parse(is);
  }

  public void extractAuthors(NodeList authors) {
    String url;
    Element autEl;
    Thread thread;
    ApiReader apiReader;
    Document xmlMetadata = null;

    System.out.println("autoren aufgerufen");

    if (authors.getLength() > 0) {
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
            System.out.println(metadata);
            xmlMetadata = loadXmlFromString(metadata);
            extractAuthorName(xmlMetadata);
          } catch (Exception e) {
            System.out.println(e);
          }
        }
      }
    }
  }

  private String extractAuthorName(Document xmlMetadata) {
    String authorName = "";
    XPath xpath = XPathFactory.newInstance().newXPath();

    try {
      // in MARCXML, datafield "100" (suggested name for the person) is only present once
      // (cataloguing rules for libraries)
      XPathExpression expr = xpath.compile("//datafield[@tag=\"100\"]//subfield[@code=\"a\"]");
      Object exprResult = expr.evaluate(xmlMetadata, XPathConstants.NODESET);
      NodeList authorNameWrapper = (NodeList) exprResult;
      String aut = authorNameWrapper.item(0).getTextContent();
      authorName = aut;
    } catch (Exception e) {
      System.out.println(e);
    }

    return authorName;
  }

}
