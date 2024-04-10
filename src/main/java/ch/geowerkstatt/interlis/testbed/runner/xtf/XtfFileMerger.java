package ch.geowerkstatt.interlis.testbed.runner.xtf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
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
    private static final String DELETE_ATTRIBUTE = "DELETE";
    private static final String DELETE_ATTRIBUTE_LOWERCASE = DELETE_ATTRIBUTE.toLowerCase();
    private static final String INTERLIS24_NAMESPACE = "http://www.interlis.ch/xtf/2.4/INTERLIS";

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
            LOGGER.info("Merging {} with {} into {}", baseFile, patchFile, outputFile);
            var documentBuilder = factory.newDocumentBuilder();

            var baseDocument = documentBuilder.parse(baseFile.toFile());
            var patchDocument = documentBuilder.parse(patchFile.toFile());

            var baseBaskets = findBaskets(baseDocument);
            if (baseBaskets.isEmpty()) {
                LOGGER.error("No baskets found in base file {}.", baseFile);
                return false;
            }

            var patchBaskets = findBaskets(patchDocument);
            if (patchBaskets.isEmpty()) {
                LOGGER.error("No baskets found in patch file {}.", patchFile);
                return false;
            }

            if (!mergeBaskets(baseDocument, baseBaskets.get(), patchBaskets.get())) {
                return false;
            }

            writeMergedFile(baseDocument, outputFile);
            LOGGER.info("Successfully merged files into {}", outputFile);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to merge files.", e);
            return false;
        }
    }

    private static boolean mergeBaskets(Document document, Map<String, Basket> baseBaskets, Map<String, Basket> patchBaskets) {
        var isValid = true;

        for (var patchBasketEntry : patchBaskets.entrySet()) {
            var basketId = patchBasketEntry.getKey();
            var patchBasket = patchBasketEntry.getValue();

            var originalBasket = baseBaskets.get(basketId);
            if (originalBasket == null) {
                LOGGER.error("Basket {} not found in base file.", basketId);
                isValid = false;
                continue;
            }

            if (hasDeleteAttribute(patchBasket.element())) {
                originalBasket.removeBasketNode();
                continue;
            }

            for (var patchEntry : patchBasket.objects().entrySet()) {
                var entryId = patchEntry.getKey();
                var element = patchEntry.getValue();

                if (hasDeleteAttribute(element)) {
                    if (!originalBasket.removeChildNode(entryId)) {
                        LOGGER.error("Could not remove entry {} from basket {} as it does not exist.", entryId, basketId);
                        isValid = false;
                    }
                } else {
                    var importedNode = document.importNode(element, true);
                    originalBasket.addOrReplaceChildNode(entryId, importedNode);
                }
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
                    var hasId = hasInterlisAttribute(e, BASKET_ID);
                    if (!hasId) {
                        LOGGER.warn("Basket without {} found.", BASKET_ID);
                    }
                    return hasId;
                })
                .collect(Collectors.toMap(e -> getInterlisAttribute(e, BASKET_ID), XtfFileMerger::collectBasket));
        return Optional.of(baskets);
    }

    private static Basket collectBasket(Element basket) {
        var objects = streamChildElementNodes(basket)
                .filter(e -> {
                    var hasId = hasInterlisAttribute(e, OBJECT_ID);
                    if (!hasId) {
                        LOGGER.warn("Entry without {} found in basket {}.", OBJECT_ID, basket.getAttribute(BASKET_ID));
                    }
                    return hasId;
                })
                .collect(Collectors.toMap(e -> getInterlisAttribute(e, OBJECT_ID), e -> e));
        return new Basket(basket, objects);
    }

    private static boolean hasDeleteAttribute(Element element) {
        return element.hasAttribute(DELETE_ATTRIBUTE) || element.hasAttribute(DELETE_ATTRIBUTE_LOWERCASE);
    }

    private static boolean hasInterlisAttribute(Element element, String attributeName) {
        return getInterlisAttribute(element, attributeName) != null;
    }

    private static String getInterlisAttribute(Element element, String attributeName) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }

        var ili24Name = attributeName.toLowerCase();
        if (element.hasAttributeNS(INTERLIS24_NAMESPACE, ili24Name)) {
            return element.getAttributeNS(INTERLIS24_NAMESPACE, ili24Name);
        }
        return null;
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
