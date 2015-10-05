package test.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library;



import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.exceptions.PlaceNotFoundException;
import org.junit.*;
import junit.framework.Assert;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import main.java.ru.fizteh.fivt.students.ypechatnov.Twitter.library.YandexPlaces;
import twitter4j.*;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;


public class YandexPlacesTest {

    @Test
    public void testCalculations() throws PlaceNotFoundException{
        YandexPlaces places = new YandexPlaces();
        places.setPlaceQueryByGeoCodeXML(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<ymaps>\n"
                        + "<lowerCorner>83.50535 53.183755</lowerCorner>\n"
                        + "<upperCorner>83.864954 53.452658</upperCorner>\n"
                        + "<pos>83.779875 53.348053</pos>\n"
                        + "</ymaps>\n");
        double coord[] = places.calcCoord(),
                bnds[][] = places.calcBounds(),
                radius = places.calcRadiusKm();
        Assert.assertTrue(new Double(83) < coord[0] && coord[0] < new Double(84));
        Assert.assertTrue(new Double(53) < coord[1] && coord[1] < new Double(54));
        System.err.println(radius);
        Assert.assertTrue(new Double(10) < radius && radius < new Double(300));
        Assert.assertTrue(bnds[0][0] < coord[0] && bnds[0][1] < coord[1]);
        Assert.assertTrue(bnds[1][0] > coord[0] && bnds[1][1] > coord[1]);
    }
}
