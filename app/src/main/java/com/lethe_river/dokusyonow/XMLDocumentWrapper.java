package com.lethe_river.dokusyonow;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLDocumentWrapper {
	
	private final Document document;
	private Node baseNode;
	
	
	public XMLDocumentWrapper(Document doc) {
		document = doc;
		baseNode = document.getDocumentElement();
	}
	
	public String get(String path) {
		Node node = getNode(path);
		if(node == null)
			return null;
		return node.getFirstChild().getNodeValue();
	}
	
	// add
	public String getCreator(String role) {
		String str = "";
		NodeList nodeList = baseNode.getChildNodes();
		for(int i=0; i < nodeList.getLength(); i++) {
			if(nodeList.item(i).getNodeName().equals("Creator")) {
				Node node = nodeList.item(i);
				// check
				/*NodeList list = nodeList.item(i).getChildNodes();
				for(int j=0; j < list.getLength(); j++) {
					if(list.item(j) != null){
						System.out.println("exist");
						System.out.println(list.item(j).getNodeName());
					}
				}
				System.out.println(nodeList.item(i).getFirstChild().getNodeValue() + "です");
				NamedNodeMap nnm = nodeList.item(i).getAttributes();
				System.out.println("role : " + nnm.getNamedItem("Role").getFirstChild().getNodeValue());*/
				
				str = str + node.getAttributes().getNamedItem("Role").getFirstChild().getNodeValue() + ":" + node.getFirstChild().getNodeValue() + " ";
			}
		}
		return str;
	}
	
	public boolean setBase(String path) {
		baseNode = getNode(path);
		return baseNode != null;
	}
	
	public List<String> getAsList(String path) {
		List<String> list = new ArrayList<>();
		List<Node> nodes = getNodes(path);
		
		for(Node node : nodes) {
			NodeList nl = node.getParentNode().getChildNodes();
			for(int i = 0;i < nl.getLength();i++) {
				Node child = nl.item(i);
				if(child.getNodeName().equals(node.getNodeName()))
					list.add(child.getFirstChild().getNodeValue());
			}
		}
		return list;
	}
	
	private List<Node> getNodes(String path, List<Node> nodes) {
		
		//System.out.println("getNodes");
		//System.out.println("  path:"+path);
		//System.out.print("  nodes:");
		//for(Node node : nodes) {
		//	System.out.print(" "+node.getNodeName());
		//} System.out.println();
		
		List<Node> nexts = new ArrayList<>();
		String nextPath = null;
		if(path.indexOf("/") != -1) {
			nextPath = path.substring(path.indexOf("/")+1);
			path = path.substring(0, path.indexOf("/"));
		}
		
		// pathと名前の付くものすべてを返す
		for(Node node : nodes) {
			NodeList l = node.getChildNodes();
			for(int i = 0;i < l.getLength();i++) {
				Node n = l.item(i);
				if(n.getNodeName().matches(path))
					nexts.add(n);
			}
		}
		
		if(nextPath == null)
			return nexts;
		else {
			return getNodes(nextPath, nexts);
		}
	}
	
	private List<Node> getNodes(String path) {
		List<Node> nodes = new ArrayList<>();
		if(path.startsWith("/")) {
			nodes.add(document.getDocumentElement());
			path = path.substring(1);
		} else if (path.startsWith("./")) {
			nodes.add(baseNode);
			path = path.substring(2);
		} else {
			return null;
		}
		return getNodes(path, nodes);
	}
	
	private Node getNode(String path) {
		Node node = null;
		
		if(path.startsWith("/")) {
			node = document.getDocumentElement();
			path = path.substring(1);
		} else if (path.startsWith("./")) {
			node = baseNode;
			path = path.substring(2);
		} else {
			return null;
		}
		label: for(String name : path.split("/")) {
			
			// 添え字処理
			int index = 0;
			if(name.matches(".+\\[.+\\]")) {
				try {
					index = Integer.parseInt(name.substring(name.indexOf('['), name.lastIndexOf(']')));
					name = name.substring(0, name.indexOf('['));
				} catch (NumberFormatException e1) {
					return null;
				}	
			}
			NodeList list = node.getChildNodes();
			for(int i = 0;i < list.getLength();i++) {
				Node n = list.item(i);
				if(n.getNodeName().equals(name)) {
					if(index == 0) {
						node = n;
						continue label;
					} else {
						index--;
					}
				}
			}
			return null;
		}
		return node;
	}
	
	public String getTree() {
		StringBuilder sb = new StringBuilder();
		getTree(document.getDocumentElement(), 0, sb);
		return sb.toString();
	}
	
	private void getTree(Node node, int indent, StringBuilder sb) {
		for(int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		sb.append(node.getNodeName()).append("[").append(node.getNodeValue()).append("]").append("\n");
		NodeList list = node.getChildNodes();
		for(int i = 0; i < list.getLength();i++) {
			Node next = list.item(i);
				getTree(next, indent+1, sb);
		}
	}
	
	public static void main(String[] args) {
		XMLDocumentWrapper wrapper = new XMLDocumentWrapper(AmazonAPI.getDocument(9784873115726L));
		//XMLDocumentWrapper wrapper = new XMLDocumentWrapper(AmazonAPI.getDocument("やさしいJava"));
		System.out.println(wrapper.getTree());
		System.out.println();
		wrapper.setBase("/Items/Item/ItemAttributes");
		//System.out.println(wrapper.get("/Items/Item/ItemAttributes"));
		
		for(String str : wrapper.getAsList("./.+"))
			System.out.println(str);
		
		//wrapper.setBase("/Items/Item/ItemLinks");
		//for(String str : wrapper.getAsList("./ItemLink/URL"))
		//	System.out.println(str);
	}
}
