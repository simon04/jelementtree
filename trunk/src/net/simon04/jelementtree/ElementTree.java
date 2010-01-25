/*
 * Copyright (C) 2010 Simon Legner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.simon04.jelementtree;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * {@code ElementTree} is the main class for XML handling. It represents one XML tag including attributes, references to child nodes and to its parent.
 *
 * Use the constructor {@link #ElementTree} to instanciate a new root element and {@link #createChild} to go down in the XML hierarchy.
 * Various getter and setter methods are available whereas the setters return an instance of this element to allow chained method calls.
 * The static methods {@link #fromString}, {@link #fromReader}, {@link #fromStream} allow parsing of an existing XML document and obtaining a new {@code ElementTree}.
 * The method {@link #toXML} is provided to obtain an XML string representation out of this instance.
 *
 * @author Simon Legner
 * @see "The idea and paradigm originate from Python's package ElementTree, see the <a href="http://effbot.org/zone/element-index.htm">homepage of ElementTree</a>."
 */
public class ElementTree implements Cloneable, Serializable {

	private static final long serialVersionUID = 24L;
	private String tag;
	private Map<String, String> attributes = new TreeMap<String, String>();
	private ElementTree parent;
	private List<ElementTree> children = new LinkedList<ElementTree>();
	private String text, tail;

	/**
	 * Instanciates a new XML element representation with the specified {@code tag} name.
	 * This is the one and only constructor as entrance point to this class.
	 * The tag name must comply with the <a href="http://www.w3.org/TR/2008/REC-xml-20081126/#NT-NameStartChar">specification</a>.
	 * @param tag the tag name of this XML element.
	 */
	public ElementTree(String tag) {
		this.tag = tag;
	}

	/**
	 * Adds/overrides the attribute with the name {@code key} and sets its value to {@code value}.
	 * Think of an attribute as the {@code href}-part in {@code <a href="http://...">...</a>}.
	 * @param key the attribute name/identifier.
	 * @param value the attribute value.
	 * @return a reference to this element.
	 */
	public ElementTree setAttribute(String key, String value) {
		this.attributes.put(key, value);
		return this;
	}

	/**
	 * Appends the given {@code child} as last child node to this instance.
	 * @param child child instance.
	 * @return a reference to this element.
	 */
	public ElementTree addChild(ElementTree child) {
		child.parent = this;
		this.children.add(child);
		return this;
	}

	/**
	 * Inserts the given {@code child} as child node at the specified position.
	 * @param index index at which the child is to be inserted.
	 * @param child child instance.
	 * @return a reference to this element.
	 */
	public ElementTree addChild(int index, ElementTree child) {
		child.parent = this;
		this.children.add(index, child);
		return this;
	}

	/**
	 * Instanciates a new {@code ElementTree} and appends it as last child node to this instance.
	 * @param tag the tag name of child.
	 * @return a reference to the newly created child element.
	 */
	public ElementTree createChild(String tag) {
		ElementTree child = new ElementTree(tag);
		addChild(child);
		return child;
	}

	/**
	 * Sets the text before the first child.
	 * Think of the text as {@code text} or {@code bold} in {@code <root>text<b>bold</b>after</root>}.
	 * @param text the text.
	 * @return a reference to this element.
	 */
	public ElementTree setText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * Sets the tail of this element, that is the text after the closing tag of this element.
	 * Think of the tail as {@code after} in {@code <root>text<b>bold</b>after</root>}.
	 * @param tail the tail.
	 * @return a reference to this element.
	 */
	public ElementTree setTail(String tail) {
		this.tail = tail;
		return this;
	}

	/**
	 * Sets the namespace of this element by adding a {@code xmlns} attribute.
	 * For instance {@code new ElementTree("kml").setNamespace("http://earth.google.com/kml/2.2").toXML()} yields {@code <kml xmlns="http://earth.google.com/kml/2.2">}.
	 * @param namespace namespace url.
	 * @return a reference to this element.
	 */
	public ElementTree setNamespace(String namespace) {
		return setNamespace(null, namespace);
	}

	/**
	 * Sets the namespace for elements with {@code prefix} by adding a {@code xmlns:prefix} attribute.
	 * For instance use this to set the namespace for the prefix {@code edi} in {@code <x xmlns:edi="http://ecommerce.example.org/schema"><edi:price units='Euro'>32.18</edi:price></x>}.
	 * @param prefix the prefix.
	 * @param namespace namespace url.
	 * @return a reference to this element.
	 */
	public ElementTree setNamespace(String prefix, String namespace) {
		String attr = prefix == null ? "xmlns" : ("xmlns:" + prefix);
		return setAttribute(attr, namespace);
	}

	/**
	 * Returns an unmodifiable list of the children. Use this e.g. for iteration purposes.
	 * @return an unmodifiable list of the children.
	 */
	public List<ElementTree> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Returns the child at position {@index}. For negative values the list is considered in reversed order.
	 * @param index index of the child satisfying {@code -n <= index < +n} where {@code n} is the number of children.
	 * @return
	 */
	public ElementTree getChild(int index) {
		if (index < 0) {
			index += children.size();
		}
		return children.get(index);
	}

	/**
	 * Returns the first child.
	 * @return the first child.
	 */
	public ElementTree getFirstChild() {
		return children.isEmpty() ? null : children.get(0);
	}

	/**
	 * Returns the last child.
	 * @return the last child.
	 */
	public ElementTree getLastChild() {
		return children.isEmpty() ? null : children.get(children.size() - 1);
	}

	/**
	 * Returns the associated value for the attribute with the name {@code key} or {@code null} if none is set.
	 * @param key the attribute name/identifier.
	 * @return the attribute value or {@code null} if none is set.
	 */
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Returns the tag name.
	 * @return the tag name.
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Returns the tail of this element, that is the text after the closing tag of this element. See {@link #setTail} for an example.
	 * @return the tail.
	 */
	public String getTail() {
		return tail;
	}

	/**
	 * Returns the text before the first child. See {@link #setText} for an example.
	 * @return the text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns an unmodifiable map of the attributes whereas the attribute names are the keys in the map.
	 * @return an unmodifiable map of the attributes.
	 */
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	/**
	 * Returns the parent element of this instance or null if none exists.
	 * @return the parent element of this instance or null if none exists.
	 */
	public ElementTree getParent() {
		return parent;
	}

	/**
	 * Returns the root element, that is the one element with no parent.
	 * @return the root element.
	 */
	public ElementTree getRoot() {
		ElementTree root = this;
		while (root.getParent() != null) {
			root = root.getParent();
		}
		return root;
	}

	@Override
	public ElementTree clone() {
		ElementTree r = new ElementTree(this.tag);
		r.attributes.putAll(this.attributes);
		r.children.addAll(this.children);
		r.text = this.text;
		r.tail = this.tail;
		return r;
	}

	/**
	 * Returns an XML string representation of the XML tree starting with this instance.
	 * @return the XML string.
	 */
	public String toXML() {
		StringBuilder s = new StringBuilder();
		if (parent == null) {
			s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		}
		s.append("<");
		s.append(tag);
		for (Entry<String, String> a : attributes.entrySet()) {
			s.append(" ");
			s.append(a.getKey());
			s.append("=\"");
			s.append(escapeText(a.getValue()));
			s.append("\"");
		}
		if (text == null && children.isEmpty()) {
			s.append("/>");
		} else {
			s.append(">");
			if (!children.isEmpty()) {
				s.append("\n");
			}
			if (text != null) {
				s.append(escapeText(text));
			}
			for (ElementTree c : children) {
				s.append(c.toXML());
			}
			s.append("</");
			s.append(tag);
			s.append(">\n");
		}
		if (tail != null) {
			s.append(tail);
		}
		return s.toString();
	}

	/**
	 * Finds elements via the given {@code xpath} string. You can provide a simplified version of <a href="http://www.w3.org/TR/xpath">XPath</a>:
	 * {@code ( . | .. | / | // | * | tag | [tag] | [@attr] | [@attr='value'] )+}
	 * @param xpath the xpath string.
	 * @return list of elements for xpath.
	 */
	public List<ElementTree> findAll(String xpath) {
		return new ElementPath(xpath).findAll(this);
	}

	/**
	 * Returns the first element for the {@code xpath}, that is the first of {@link #findAll} or {@code null}.
	 * @param xpath the xpath string.
	 * @return first element for xpath.
	 */
	public ElementTree find(String xpath) {
		List<ElementTree> l = findAll(xpath);
		return l.size() == 0 ? null : l.get(0);
	}

	/**
	 * Returns the text of the first element for the {@code xpath}, that is the same as {@link #find}.{@link #getText}.
	 * @param xpath the xpath string.
	 * @return text of first element for xpath.
	 */
	public String findText(String xpath) {
		ElementTree e = find(xpath);
		return e == null ? null : e.getText();
	}

	private static String escapeText(String text) {
		return text.replaceAll("&", "&amp;").
				replaceAll("<", "&lt;").
				replaceAll(">", "&gt;").
				replaceAll("\"", "&quot;").
				replaceAll("\n", "&#10;");
	}

	@Override
	public String toString() {
		return this.toXML();
	}

	public static ElementTree fromString(String xml) {
		return fromReader(new StringReader(xml));
	}

	public static ElementTree fromReader(Reader reader) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(reader);
			return fromParser(parser);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static ElementTree fromStream(InputStream stream) {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader parser = factory.createXMLStreamReader(stream);
			return fromParser(parser);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static ElementTree fromParser(XMLStreamReader parser) throws XMLStreamException {
		ElementTree current = null;
		while (parser.hasNext()) {
			switch (parser.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					String name = parser.getName().
							getLocalPart();
					String prefix = parser.getName().
							getPrefix();
					if (prefix != null && !prefix.isEmpty()) {
						name = prefix + ":" + name;
					}
					if (current == null) {
						current = new ElementTree(name);
					} else {
						current = current.createChild(name);
					}
					for (int i = 0; i < parser.getAttributeCount(); i++) {
						current.setAttribute(parser.getAttributeLocalName(i), parser.getAttributeValue(i));
					}
					for (int i = 0; i < parser.getNamespaceCount(); i++) {
						current.setNamespace(parser.getNamespacePrefix(i), parser.getNamespaceURI(i));
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					if (current != null && current.parent != null) {
						current = current.parent;
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					String text = parser.getText();
					if (text != null && !text.trim().
							isEmpty() && current != null) {
						if (current.children.isEmpty()) {
							current.text = text.trim();
						} else {
							current.getLastChild().tail = text.trim();
						}
					}
			}
			parser.next();
		}
		return current;
	}
}
