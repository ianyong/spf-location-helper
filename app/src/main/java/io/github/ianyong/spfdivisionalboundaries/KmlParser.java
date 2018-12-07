package io.github.ianyong.spfdivisionalboundaries;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class KmlParser {

    private final static String PLACEMARK = "Placemark";
    private final static String NAME = "name";
    private final static String EXTENDED_DATA = "ExtendedData";
    private final static String SCHEMA_DATA = "SchemaData";
    private final static String SIMPLE_DATA = "SimpleData";

    private XmlPullParser xpp;
    private HashMap<String, KmlPlacemark> placemarks;

    public KmlParser(InputStream inputStream) throws XmlPullParserException {
        placemarks = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        xpp.setInput(inputStream, null);
    }

    public void parseKml() throws XmlPullParserException, IOException {
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                // Process placemark data
                if (xpp.getName().equals(PLACEMARK)) {
                    String placemarkName = xpp.getAttributeValue(null, "id");
                    KmlPlacemark placemark = new KmlPlacemark(placemarkName);
                    eventType = xpp.next(); // Advance to next parsing event for skip() function to work properly
                    while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals(PLACEMARK))) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals(EXTENDED_DATA)) {
                                String propertyKey = null;
                                while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals(EXTENDED_DATA))) {
                                    if (eventType == XmlPullParser.START_TAG) {
                                        if (xpp.getName().equals(SIMPLE_DATA)) {
                                            propertyKey = xpp.getAttributeValue(null, "name");
                                        }
                                    } else if (eventType == XmlPullParser.TEXT && propertyKey != null) {
                                        placemark.setProperty(propertyKey, xpp.getText());
                                        propertyKey = null;
                                    }
                                    eventType = xpp.next();
                                }
                            } else {
                                skip(xpp);
                            }
                        }
                        eventType = xpp.next();
                    }
                    placemarks.put(placemarkName, placemark);
                }
            }
            eventType = xpp.next();
        }
    }

    private void skip(XmlPullParser xpp) throws XmlPullParserException, IOException {
        if (xpp.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (xpp.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public KmlPlacemark getKmlPlacemark(String id) {
        return placemarks.get(id);
    }

}
