package ch.geowerkstatt.interlis.testbed.runner.xtf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class XtfFileMerger implements XtfMerger {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String BASKET_ID = "BID";
    private static final String OBJECT_ID = "TID";

    private final DocumentBuilderFactory factory;

    /**
     * Creates a new instance of the XtfFileMerger class.
     */
    public XtfFileMerger() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
    }

    @Override
    public boolean merge(Path baseFile, Path patchFile, Path outputFile) {
        try {
            LOGGER.info("Merging " + baseFile + " with " + patchFile + " into " + outputFile);
            var documentBuilder = createDocumentBuilder();

            var baseDocument = documentBuilder.parse(baseFile.toFile());
            var patchDocument = documentBuilder.parse(patchFile.toFile());

            var baseBaskets = findBaskets(baseDocument);
            if (baseBaskets.isEmpty()) {
                LOGGER.error("No baskets found in base file " + baseFile + ".");
                return false;
            }

            var patchBaskets = findBaskets(patchDocument);
            if (patchBaskets.isEmpty()) {
                LOGGER.error("No baskets found in patch file " + patchFile + ".");
                return false;
            }

            if (!mergeBaskets(baseDocument, baseBaskets.get(), patchBaskets.get())) {
                return false;
            }

            writeMergedFile(baseDocument, outputFile);
            LOGGER.info("Successfully merged files into " + outputFile);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to merge files.", e);
            return false;
        }
    }

    DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        return factory.newDocumentBuilder();
    }

    private static boolean mergeBaskets(Document document, Map<String, Basket> baseBaskets, Map<String, Basket> patchBaskets) {
        var isValid = true;

        for (var patchBasket : patchBaskets.entrySet()) {
            var basketId = patchBasket.getKey();

            var originalBasket = baseBaskets.get(basketId);
            if (originalBasket == null) {
                LOGGER.error("Basket " + basketId + " not found in base file.");
                isValid = false;
                continue;
            }

            for (var patchEntry : patchBasket.getValue().objects().entrySet()) {
                var entryId = patchEntry.getKey();

                var importedNode = document.importNode(patchEntry.getValue(), true);
                originalBasket.addOrReplaceChild(entryId, importedNode);
            }
        }

        return isValid;
    }

    private static void writeMergedFile(Document document, Path outputFile) throws IOException, TransformerException {
        Files.createDirectories(outputFile.getParent());

        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();
        var source = new DOMSource(document);
        var result = new StreamResult(outputFile.toFile());
        transformer.transform(source, result);
    }

    static Optional<Map<String, Basket>> findBaskets(Document document) {
        var dataSection = findDataSection(document);
        if (dataSection.isEmpty()) {
            return Optional.empty();
        }

        var baskets = streamChildElementNodes(dataSection.get())
                .filter(e -> {
                    var hasId = e.hasAttribute(BASKET_ID);
                    if (!hasId) {
                        LOGGER.warn("Basket without " + BASKET_ID + " found.");
                    }
                    return hasId;
                })
                .collect(Collectors.toMap(e -> e.getAttribute(BASKET_ID), XtfFileMerger::collectBasket));
        return Optional.of(baskets);
    }

    private static Basket collectBasket(Element basket) {
        var objects = streamChildElementNodes(basket)
                .filter(e -> {
                    var hasId = e.hasAttribute(OBJECT_ID);
                    if (!hasId) {
                        LOGGER.warn("Entry without " + OBJECT_ID + " found in basket " + basket.getAttribute(BASKET_ID) + ".");
                    }
                    return hasId;
                })
                .collect(Collectors.toMap(e -> e.getAttribute(OBJECT_ID), e -> e));
        return new Basket(basket, objects);
    }

    private static Optional<Element> findDataSection(Document document) {
        var transfer = document.getFirstChild();
        return streamChildElementNodes(transfer)
                .filter(n -> n.getLocalName().equalsIgnoreCase("datasection"))
                .findFirst();
    }

    private static Stream<Element> streamChildElementNodes(Node node) {
        var childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength())
                .mapToObj(childNodes::item)
                .filter(n -> n instanceof Element)
                .map(n -> (Element) n);
    }
}
