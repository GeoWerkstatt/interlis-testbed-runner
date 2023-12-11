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
    public void addOrReplaceChildNode(String entryId, Node node) {
        var originalEntry = objects().get(entryId);
        if (originalEntry == null) {
            element().appendChild(node);
        } else {
            element().replaceChild(node, originalEntry);
        }
    }

    /**
     * Removes the basket node from the XML document.
     */
    public void removeBasketNode() {
        element().getParentNode().removeChild(element());
    }

    /**
     * Removes the child node with the given entry ID.
     *
     * @param entryId the ID of the entry to remove
     * @return {@code true} if the entry was removed, {@code false} if the entry was not found
     */
    public boolean removeChildNode(String entryId) {
        var originalEntry = objects().get(entryId);
        if (originalEntry == null) {
            return false;
        }

        element().removeChild(originalEntry);
        return true;
    }
}
