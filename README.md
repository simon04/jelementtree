# JElementTree
JElementTree is an easy-to-use Java library for working with XML in a pythonic way. A big advantage is its powerful, but minimalistic implementation. There is one class, ElementTree, you'll be confronted with.

## Features
* no additional library required
* hierarchical representation of XML tree
* lightweight namespace support
* simple XPath support
* parsing XML documents (currently based on StAX from JDK 1.6)
* method chaining like Java's StringBuilder

## Origin
The concept originates from the ElementTree package of Python which can be found on http://effbot.org/zone/element-index.htm

## Examples
### Example 1: construction of KML document
The following two different codes will a tiny KML document.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.2">
  <Document>
    <name>World</name>
    <Folder>
      <name>Austria</name>
      <Placemark>
        <name>Innsbruck</name>
        <Point>
          <coordinates>11.40,47.26</coordinates>
        </Point>
      </Placemark>
    </Folder>
  </Document>
</kml>
```

```java
String xml = new ElementTree("kml").setNamespace("http://earth.google.com/kml/2.2").
                createChild("Document").
                createChild("name").
                setText("World").
                getParent().
                createChild("Folder").
                createChild("name").
                setText("Austria").
                getParent().
                createChild("Placemark").
                createChild("name").
                setText("Innsbruck").
                getParent().
                createChild("Point").
                createChild("coordinates").
                setText("11.40,47.26").
                getRoot().
                toXML();
```
```java
String xml2 = new ElementTree("kml").setNamespace("http://earth.google.com/kml/2.2").
                createChild("Document").
                addChild(new ElementTree("name").setText("World")).
                createChild("Folder").
                addChild(new ElementTree("name").setText("Austria")).
                createChild("Placemark").
                addChild(new ElementTree("name").setText("Innsbruck")).
                createChild("Point").
                addChild(new ElementTree("coordinates").setText("11.40,47.26")).
                getRoot().
                toXML();
```

### Example 2: geocoding with OpenStreetMap
```java
ElementTree geo = ElementTree.fromStream(new URI("http://nominatim.openstreetmap.org/search?format=xml&q=innsbruck").toURL().openStream());
for (ElementTree i : geo.findAll("//place")) {
        System.out.println(i.getAttribute("display_name"));
        System.out.println("\t" + i.getAttribute("lon") + "," + i.getAttribute("lat"));
}
```

```
Innsbruck, Innsbruck-Stadt, Tirol, Österreich, Europe
        11.3936859,47.2656269
Innsbruck, Innsbruck-Stadt, Tirol, Österreich
        11.3936828,47.26565
Innsbruck, Carroll, United States of America
        -71.1338615946084,43.5341678362733
Innsbruck, Aspenhoff, Warren, Missouri, United States of America
        -91.1552550683042,38.6957456083531
```
