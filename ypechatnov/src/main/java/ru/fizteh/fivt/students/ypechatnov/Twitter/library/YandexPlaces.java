package main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library;

/**
 * Created by ura on 27.09.15.
 */

import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.exceptions.PlaceNotFoundException;
import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import twitter4j.JSONObject;
import twitter4j.Place;


public class YandexPlaces {
    private Document doc;

    public double[] calcCoord() throws PlaceNotFoundException {
        try {
            String p = doc.getElementsByTagName("pos").item(0).getTextContent();
            double[] coord = {Double.parseDouble(p.split(" ")[0]), Double.parseDouble(p.split(" ")[1])};
            return coord;
        } catch (Exception e) {
            System.err.println(e);
            throw new PlaceNotFoundException();
        }
    }

    public Double calcRadiusKm() throws PlaceNotFoundException {
        try {
            String lowerCorner, upperCorner;
            lowerCorner = doc.getElementsByTagName("lowerCorner").item(0).getTextContent();
            upperCorner = doc.getElementsByTagName("upperCorner").item(0).getTextContent();

            // https://ru.wikipedia.org/wiki/%D0%9E%D1%80%D1%82%D0%BE%D0%B4%D1%80%D0%BE%D0%BC%D0%B8%D1%8F
            final Double r = new Double(6371);
            Double a1, a2, p1, p2, d;
            p1 = Math.toRadians(Double.parseDouble(lowerCorner.split(" ")[0]));
            a1 = Math.toRadians(Double.parseDouble(lowerCorner.split(" ")[1]));
            p2 = Math.toRadians(Double.parseDouble(upperCorner.split(" ")[0]));
            a2 = Math.toRadians(Double.parseDouble(upperCorner.split(" ")[1]));
            d = Math.acos(Math.sin(p1) * Math.sin(p2)
                    + StrictMath.cos(p1) * Math.cos(p2) * Math.cos(a2 - a1));
            return d * r / new Double(2.0);
        } catch (Exception e) {
            System.err.println(e);
            throw new PlaceNotFoundException();
        }
    }

    public double[][] calcBounds() throws PlaceNotFoundException {
        try {
            String b1, b2;
            b1 = doc.getElementsByTagName("lowerCorner").item(0).getTextContent();
            b2 = doc.getElementsByTagName("upperCorner").item(0).getTextContent();
            double[][] bnds = {{Double.parseDouble(b1.split(" ")[0]), Double.parseDouble(b1.split(" ")[1])},
                    {Double.parseDouble(b2.split(" ")[0]), Double.parseDouble(b2.split(" ")[1])}};
            return bnds;
        } catch (Exception e) {
            System.err.println(e);
            throw new PlaceNotFoundException();
        }
    }

    public YandexPlaces() {
    }

    public YandexPlaces setPlaceQuery(String place) throws PlaceNotFoundException {
        if (place.equals("nearby")) {
            place = findSelfLocation();
        }
        return setPlaceQueryByGeoCodeXML(retrieveGeoCodeXML(place));
    }

    public YandexPlaces setPlaceQueryByGeoCodeXML(String geoCode) throws PlaceNotFoundException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(geoCode)));
            return this;
        } catch (Exception e) {
            System.err.println(e);
            throw new PlaceNotFoundException();
        }
    }

    public String retrieveGeoCodeXML(String place) throws PlaceNotFoundException {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL("https://geocode-maps.yandex.ru/1.x/?&geocode=" + place + "&results=1");
            System.err.println("Query to YandexMaps: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line + "\n");
            }
            rd.close();
            return result.toString();
        } catch (Exception e) {
            throw new PlaceNotFoundException();
        }
    }

    // not a Yandex, but.....
    public String findSelfLocation() throws PlaceNotFoundException {
        try {
            JSONObject jsonObject;
            String url = "http://ipinfo.io/json";
            return new JSONObject(IOUtils.toString(new URL(url))).getString("city");
        } catch (Exception e) {
            System.err.println(e);
            throw new PlaceNotFoundException();
        }
    }
}
