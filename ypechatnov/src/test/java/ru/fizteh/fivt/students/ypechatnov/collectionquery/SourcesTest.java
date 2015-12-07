package ru.fizteh.fivt.students.ypechatnov.collectionquery;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.fizteh.fivt.students.ypechatnov.collectionquery.Sources;
import org.mockito.*;
import org.mockito.runners.*;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ura on 04.12.15.
 */
public class SourcesTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testList() {
        assertEquals(Sources.list(1, 2, 3), Arrays.asList(1, 2, 3));
    }
    @Test
    public void testSet() {
        assertEquals(Sources.set(1, 2, 3), new HashSet<Integer>(Sources.list(1, 2, 3)));
    }

    @Test
    public void testLines() throws Exception {
        thrown.expect(NoSuchMethodException.class);
        Sources.lines(mock(System.in.getClass()));
        Sources.lines(Paths.get("out.txt"));
    }

    @Test
    // Required existing out.txt
    public void testLines1() throws Exception {
        thrown.expect(NoSuchMethodException.class);
        Sources.lines(Paths.get("out.txt"));
    }

    @Test
    public void testLines2() throws Exception {
        thrown.expect(IOException.class);
        Sources.lines(Paths.get("no_such_file_i_think_ahaha"));
    }




}
