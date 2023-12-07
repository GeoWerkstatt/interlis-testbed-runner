package ch.geowerkstatt.interlis.testbed.runner.xtf;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

public record Basket(Element element, Map<String, Element> objects) {
    /**
     * Adds or replaces the child node with the given entry ID.
     *
     * @param entryId the entry ID
     * @param node    the node to add or replace
     */
    public void addOrReplaceChild(String entryId, Node node) {
        var originalEntry = objects().get(entryId);
        if (originalEntry == null) {
            element().appendChild(node);
        } else {
            element().replaceChild(node, originalEntry);
        }
    }
}
