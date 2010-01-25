
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import net.simon04.jelementtree.ElementTree;
import org.junit.Assert;
import org.junit.Test;

public class TestFoo {

	@Test
	public void testTail() {
		ElementTree t = ElementTree.fromString("<a>1<b/>2<c>3</c>4<d/>5</a>");
		System.out.println(t);
	}

	@Test
	public void testKml() {
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
		Assert.assertEquals(xml, xml2);
		System.out.println(xml2);
	}

	@Test
	public void testGeocoding() throws Exception {
		ElementTree geo = ElementTree.fromStream(new URI("http://nominatim.openstreetmap.org/search?format=xml&q=innsbruck").
				toURL().
				openStream());
		System.out.println(geo.toXML() + "\n");
		for (ElementTree i : geo.findAll("//place")) {
			System.out.println(i.getAttribute("display_name"));
			System.out.println("\t" + i.getAttribute("lon") + "," + i.getAttribute("lat"));
		}
	}
}
