package ru.fizteh.fivt.students.ypechatnov.MiniORM;

/**
 * Created by ura on 13.12.15.
 */



import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;
import org.mockito.runners.*;


import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import static junit.framework.Assert.assertEquals;

import ru.fizteh.fivt.students.ypechatnov.MiniORM.annotations.*;

public class DatabaseServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() throws Exception {
        DatabaseService.deleteDatabase();
        @Table(name = "cats")
        class Kitten {
            @PrimaryKey
            @Column(name = "ID")
            Integer id;
            @Column(name = "COLOUR")
            Integer colour;
        }
        DatabaseService<Kitten, Integer> service = DatabaseService.of(Kitten.class);
        service.createTable();

    }

    @Test
    public void testDrop() throws Exception {
        DatabaseService.deleteDatabase();
        @Table(name = "cats")
        class Kitten {
            @PrimaryKey
            @Column(name = "ID")
            Integer id;
            @Column(name = "COLOUR")
            Integer colour;
        }
        DatabaseService<Kitten, Integer> service = DatabaseService.of(Kitten.class);
        service.createTable();
        service.dropTable();
    }


    @Test
    public void testInsert() throws Exception {
        DatabaseService.deleteDatabase();
        @Table(name = "cats")
        class Kitten {
            @PrimaryKey
            @Column(name = "ID")
            Integer id;
            @Column(name = "COLOUR")
            Integer colour;
            Kitten() {}
            Kitten(int id, int colour) {
                this.id = id;
                this.colour = colour;
            }
        }
        DatabaseService<Kitten, Integer> service = DatabaseService.of(Kitten.class);
        service.createTable();
        Kitten a = new Kitten(1, 13), b = new Kitten(2, 17);
        service.insert(a);
        service.insert(b);
    }

    @Table(name = "dogs")
    public static class Doggy {
        @PrimaryKey
        @Column(name = "ID")
        public Integer id;
        @Column(name = "COLOUR")
        public Integer colour;
        @Override
        public boolean equals(Object dog) {
            return dog instanceof Doggy && id.equals(((Doggy) dog).id) && colour.equals(((Doggy) dog).colour);
        }
        @Override
        public String toString() {
            return "(" + id + ", " + colour + ")";
        }
        public Doggy() {}
        public Doggy(int id, int colour) {
            this.id = id;
            this.colour = colour;
        }
    }

    @Test
    public void testQueryForAll() throws Exception {
        DatabaseService.deleteDatabase();
        DatabaseService<Doggy, Integer> service = DatabaseService.of(Doggy.class);
        service.createTable();
        Doggy a = new Doggy(1, 13), b = new Doggy(2, 17);
        service.insert(a);
        service.insert(b);
        Iterator<Doggy> iter = service.queryForAll().iterator();
        assertTrue(iter.hasNext());
        assertEquals(a, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(b, iter.next());
    }

    @Test
    public void testQueryOne() throws Exception {
        DatabaseService.deleteDatabase();
        DatabaseService<Doggy, Integer> service = DatabaseService.of(Doggy.class);
        service.createTable();
        Doggy a = new Doggy(1, 13), b = new Doggy(2, 17);
        service.insert(a);
        service.insert(b);
        assertEquals(service.queryById(2), b);
        assertTrue(service.queryById(3) == null);
    }

    @Test
    public void testUpdate() throws Exception {
        DatabaseService.deleteDatabase();
        DatabaseService<Doggy, Integer> service = DatabaseService.of(Doggy.class);
        service.createTable();
        Doggy a = new Doggy(1, 13), b = new Doggy(1, 17);
        service.insert(a);
        service.update(b);
        assertEquals(service.queryById(1), b);
    }

    @Test
    public void testDelete() throws Exception {
        DatabaseService.deleteDatabase();
        DatabaseService<Doggy, Integer> service = DatabaseService.of(Doggy.class);
        service.createTable();
        Doggy a = new Doggy(1, 13), b = new Doggy(1, 17);
        service.insert(a);
        service.delete(b);
        assertTrue(!service.queryForAll().iterator().hasNext());
    }

}
