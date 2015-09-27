package main.java.ru.fizteh.fivt.students.ypechatnov;

/**
 * Created by ura on 27.09.15.
 */


import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import twitter4j.JSONObject;


public class YandexPlaces {

    private boolean connectionError;
    private boolean ioError;
    private boolean strangeError;
    private Document doc;


    public boolean isSmthFailed() {
        return connectionError || ioError || strangeError;
    }

    public double[] calcCoord() {
        String p = doc.getElementsByTagName("pos").item(0).getTextContent();
        double[] coord = {Double.parseDouble(p.split(" ")[0]), Double.parseDouble(p.split(" ")[1])};
        return coord;
    }
    public Double calcRadiusKm() {
        // https://ru.wikipedia.org/wiki/%D0%9E%D1%80%D1%82%D0%BE%D0%B4%D1%80%D0%BE%D0%BC%D0%B8%D1%8F
        String b1, b2;
        b1 = doc.getElementsByTagName("lowerCorner").item(0).getTextContent();
        b2 = doc.getElementsByTagName("upperCorner").item(0).getTextContent();
        Double a1, a2, p1, p2, d;
        final Double r = new Double(6300), k = new Double(180) / Math.PI;
        p1 = Double.parseDouble(b1.split(" ")[0]) / k;
        a1 = Double.parseDouble(b1.split(" ")[1]) / k;
        p2 = Double.parseDouble(b2.split(" ")[0]) / k;
        a2 = Double.parseDouble(b2.split(" ")[1]) / k;
        d = Math.acos(Math.sin(p1) * Math.sin(p2)
                + StrictMath.cos(p1) * Math.cos(p2) * Math.cos(a2 - a1));
        return d * r / new Double(2.0);
    }

    public double[][] calcBounds() {
        String b1, b2;
        b1 = doc.getElementsByTagName("lowerCorner").item(0).getTextContent();
        b2 = doc.getElementsByTagName("upperCorner").item(0).getTextContent();
        double[][] bnds =  {{Double.parseDouble(b1.split(" ")[1]), Double.parseDouble(b1.split(" ")[0])},
                {Double.parseDouble(b2.split(" ")[1]), Double.parseDouble(b2.split(" ")[0])}};
        return bnds;
    }

    YandexPlaces(String place) {
        connectionError = false;
        ioError = false;
        strangeError = false;

        if (place.equals("nearby")) {
            place = findSelf();
        }
        if (place == null) {t
            connectionError = true;
            return;
        }

        StringBuilder result = new StringBuilder();
        try {
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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(result.toString())));
        } catch (MalformedURLException e) {
            connectionError = true;

        } catch (ProtocolException e) {
            connectionError = true;
        } catch (IOException e) {
            ioError = true;
        } catch (ParserConfigurationException e) {
            strangeError = true;
        } catch (SAXException e) {
            strangeError = true;
        }
    }

    // not a Yandex, but.....
    private static String findSelf() {
        JSONObject jsonObject;
        String url = "http://ipinfo.io/json";
        try {
            return new JSONObject(IOUtils.toString(new URL(url))).getString("city");
        } catch (Exception e) {
        }
        return null;

    }
}
