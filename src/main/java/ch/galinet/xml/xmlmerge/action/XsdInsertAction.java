package ch.galinet.xml.xmlmerge.action;

import ch.galinet.xml.xmlmerge.AbstractXmlMergeException;
import ch.galinet.xml.xsdhierarchy.MapNamespaceContext;
import ch.galinet.xml.xsdhierarchy.XsdHelper;
import ch.galinet.xml.xsdhierarchy.XsdParser;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by aellenn on 28.04.2017.
 */
public class XsdInsertAction extends AbstractMergeAction {

    private static Logger s_logger = LoggerFactory.getLogger(XsdInsertAction.class);

    public void perform(Element originalElement, Element patchElement,
                        Element outputParentElement) throws AbstractXmlMergeException {
        try {

            Element element;

            if (originalElement != null) {
                element = (Element) originalElement.clone();
            } else {
                element = (Element) patchElement.clone();
            }

            // List existing elements in output parent element
            List existingChildren = outputParentElement.getContent();//Children();

            if (existingChildren.size() == 0) {
                // This is the first child
                outputParentElement.addContent(element);
            } else {
                XsdParser parser = new XsdParser(getXsd(outputParentElement.getNamespaceURI()));
                Pair<String, MapNamespaceContext> scdPath = XsdHelper.generateScdPath(outputParentElement);
                List<String> orderedXsdElements = parser.getChildren(scdPath.getLeft(), scdPath.getRight()).stream().map(e -> e.getName()).collect(Collectors.toList());

                int indexOfNewElementInDtd = orderedXsdElements.indexOf(element.getName());
                s_logger.debug("index of element " + element.getName() + ": " + indexOfNewElementInDtd);

                int pos = existingChildren.size();

                // Calculate the position in the parent where we insert the
                // element
                for (int i = 0; i < existingChildren.size(); i++) {
                    if (existingChildren.get(i) instanceof Element) {
                        String elementName = ((Element) existingChildren.get(i)).getName();
                        s_logger.debug("index of child " + elementName + ": " + orderedXsdElements.indexOf(elementName));
                        if (orderedXsdElements.indexOf(elementName) > indexOfNewElementInDtd) {
                            pos = i;
                            break;
                        }
                    }
                }

                s_logger.debug("adding element " + element.getName() + " add in pos " + pos);
                outputParentElement.addContent(pos, element);
            }
        } catch (SAXException e) {
            throw new RuntimeException("Error during analyzing XSD document", e);
        }
    }

    private InputStream getXsd(String namespaceURI) {
        try {
            Properties prop = new Properties();

            prop.load(this.getClass().getClassLoader().getResourceAsStream(System.getProperty("xsdConfigPropertyFile", "xsdConfig.properties")));

            String value = prop.getProperty(String.format("namespace.%s", namespaceURI.replace("http://", "")), null);
            if (value == null || value.length() == 0) {
                throw new RuntimeException(String.format("XSD not defined in property file for namespace : %s", namespaceURI));
            }

            InputStream result = this.getClass().getClassLoader().getResourceAsStream(value);
            if (result == null) {
                throw new RuntimeException(String.format("XSD file not found in resources : %s", value));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load property file", e);
        }
    }
}
