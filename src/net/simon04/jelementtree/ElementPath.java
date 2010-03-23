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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementPath {

	private List<Operator> ops = new LinkedList<Operator>();
	private PathType type;
	/** @see <a href="http://www.w3.org/TR/REC-xml/#NT-NameStartChar">http://www.w3.org/TR/REC-xml/#NT-NameStartChar</a> */
	private static String TAGPATTERN;

	static {
		StringBuilder patternFirst = new StringBuilder(1024).
				append("[").
				append(":").
				append("[A-Z]").
				append("_").
				append("[a-z]").
				append("[\\xC0-\\xD6]").
				append("[\\xD8-\\xF6]").
				append("[\\xF8-\\u02FF]").
				append("[\\u0370-\\u037D]").
				append("[\\u037F-\\u1FFF]").
				append("[\\u200C-\\u200D]").
				append("[\\u2070-\\u218F]").
				append("[\\u2C00-\\u2FEF]").
				append("[\\u3001-\\uD7FF]").
				append("[\\uF900-\\uFDCF]").
				append("[\\uFDF0-\\uFFFD]").
				append("[\\u10000-\\uEFFFF]").
				append("]");
		
		StringBuilder patternFollow = new StringBuilder(1024).
				append("[").
				append(patternFirst).
				append("\\-").
				append("\\.").
				append("\\xB7").
				append("[\\u0300-\\u036F]").
				append("[\\u203F-\\u2040]").
				append("]+");

		TAGPATTERN = patternFirst.append(patternFollow).toString();
	}

	private interface Operator {

		List<ElementTree> convert(ElementTree e);
	}

	private enum PathType {

		ROOT, ELEMENT, CHILDREN
	}

	ElementPath(String xpath) {
		type = xpath.startsWith("/") ? PathType.ROOT : (xpath.startsWith("\\.") ? PathType.ELEMENT : PathType.CHILDREN);
		while (!xpath.isEmpty()) {
			Matcher m;
			if ((m = Pattern.compile("^/?\\.\\.").matcher(xpath)).find()) {
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						return Arrays.asList(e.getParent());
					}
				});
			} else if ((m = Pattern.compile("^/?\\.").matcher(xpath)).find()) {
				// do nothing
			} else if ((m = Pattern.compile("^//").matcher(xpath)).find()) {
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						LinkedList<ElementTree> result = new LinkedList<ElementTree>();
						result.add(e);
						for (ElementTree i : e.getChildren()) {
							result.addAll(convert(i));
						}
						return result;
					}
				});
			} else if ((m = Pattern.compile("^\\*").matcher(xpath)).find()) {
				// do nothing
			} else if ((m = Pattern.compile("^(" + TAGPATTERN + "+)").matcher(xpath)).find()) {
				final String tag = m.group(1);
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						if (e.getTag().equals(tag)) {
							return Arrays.asList(e);
						} else {
							return Arrays.asList();
						}
					}
				});
			} else if ((m = Pattern.compile("^\\[@(" + TAGPATTERN + "+)(=('" + TAGPATTERN + "+'))?\\]").matcher(xpath)).find()) {
				final String key = m.group(1);
				final String value = m.group(3).isEmpty() ? null : m.group(3);
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						if ((value == null && e.getAttribute(key) != null)
								|| (value != null && e.getAttribute(key).equals(value))) {
							return Arrays.asList(e);
						} else {
							return Arrays.asList();
						}
					}
				});
			} else if ((m = Pattern.compile("^\\[(" + TAGPATTERN + "+)\\]").matcher(xpath)).find()) {
				final String tag = m.group(1);
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						boolean hasTag = false;
						for (ElementTree j : e.getChildren()) {
							if (j.getTag().equals(tag)) {
								hasTag = true;
								break;
							}
						}
						if (hasTag) {
							return Arrays.asList(e);
						} else {
							return Arrays.asList();
						}
					}
				});
			} else if ((m = Pattern.compile("^/").matcher(xpath)).find()) {
				ops.add(new Operator() {

					@Override
					public List<ElementTree> convert(ElementTree e) {
						return e.getChildren();
					}
				});
			} else {
				throw new IllegalArgumentException("Invalid pattern specified; error at " + xpath);
			}
			xpath = m.replaceFirst("");
		}
	}

	List<ElementTree> findAll(ElementTree e) {
		HashSet<ElementTree> lst = new LinkedHashSet<ElementTree>();
		switch (type) {
			case ELEMENT:
				lst.add(e);
				break;
			case CHILDREN:
				lst.addAll(e.getChildren());
				break;
			case ROOT:
				lst.add(e.getRoot());
				break;
		}
		for (Operator op : ops) {
			LinkedHashSet<ElementTree> lstNew = new LinkedHashSet<ElementTree>();
			for (ElementTree i : lst) {
				lstNew.addAll(op.convert(i));
			}
			lst = lstNew;
		}
		return new LinkedList<ElementTree>(lst);
	}
}
