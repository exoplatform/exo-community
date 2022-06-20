package org.exoplatform.welcomescreens.service.utils;

import org.exoplatform.services.wcm.core.NodeLocation;

import javax.jcr.Node;

public class NodeUtils {

    private final static String NODE_REPOSITORY = "repository";
    private final static String NODE_WORKSPACE = "collaboration";

    public static Node findCollaborationFile(String webContentUrl) {
        Node nodeByExpression = NodeLocation.getNodeByLocation(new NodeLocation(NODE_REPOSITORY, NODE_WORKSPACE, webContentUrl, null, true));
        return nodeByExpression;
    }

    public static String getWebContentContentFromUrl(String webContentUrl) {
        try {
            Node node = NodeUtils.findCollaborationFile(webContentUrl);
            return node.getNode("default.html/jcr:content").getProperty("jcr:data").getString();
        } catch (Exception e) {
            return null;
        }
    }
}
