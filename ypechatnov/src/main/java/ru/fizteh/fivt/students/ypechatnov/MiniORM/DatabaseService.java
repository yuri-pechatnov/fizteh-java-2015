package ru.fizteh.fivt.students.ypechatnov.MiniORM;

import ru.fizteh.fivt.students.ypechatnov.MiniORM.exceptions.DatabaseException;
import ru.fizteh.fivt.students.ypechatnov.MiniORM.exceptions.InternalDatabaseError;
import ru.fizteh.fivt.students.ypechatnov.MiniORM.exceptions.NoSuchRowException;

import java.lang.reflect.Field;
import java.rmi.NoSuchObjectException;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.h2.tools.DeleteDbFiles;

/**
 * Created by ura on 13.12.15.
 */
public class DatabaseService<T, K> {

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (Exception e) {
            throw new NoClassDefFoundError();
        }
    }

    static final String databaseName = "database";

    final Class<T> itemClass;
    final Class<K> primaryKeyClass;
    final DataConverter<T> dataConverter;
    final Field primaryKeyField;

    public T queryById(K key) throws java.sql.SQLException,
            IllegalAccessException, InstantiationException, DatabaseException {
        List<T> list = calculateResultOfQuery(dataConverter.generateSelectOneByPrimaryKeyCommand(key));
        if (list.size() == 0) {
            return null;
        }
        if (list.size() > 1) {
            throw new InternalDatabaseError();
        }
        return list.get(0);
    }

    public Iterable<T> queryForAll() throws java.sql.SQLException,
            IllegalAccessException, InstantiationException {
        return calculateResultOfQuery(dataConverter.generateSelectAllCommand());
    }

    protected List<T> calculateResultOfQuery(String command) throws java.sql.SQLException,
            IllegalAccessException, InstantiationException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/" + databaseName)) {
            try (Statement stmt = conn.createStatement()) {
                List<T> list = new ArrayList<T>();
                try (ResultSet set = stmt.executeQuery(command)) {
                    while (set.next()) {
                        T item = itemClass.newInstance();
                        dataConverter.writeRow(set, item);
                        list.add(item);
                    }
                }
                return list;
            }
        }
    }

    public void insert(T item) throws java.sql.SQLException, DatabaseException {
        execute(dataConverter.generateInsertCommand(item));
    }

    public void update(T item) throws java.sql.SQLException, DatabaseException {
        execute(dataConverter.generateUpdateCommand(item));
    }

    public void delete(T item) throws java.sql.SQLException, DatabaseException {
        execute(dataConverter.generateDeleteCommand(item));
    }

    public void createTable() throws java.sql.SQLException, DatabaseException {
        execute(dataConverter.generateCreateCommand());
    }

    public void dropTable() throws java.sql.SQLException {
        execute(dataConverter.generateDropCommand());
    }


    public void execute(String command) throws java.sql.SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:~/" + databaseName)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(command);
            }
        }
    }

    public static <T> DatabaseService of(Class<T> clazz) throws DatabaseException {
        DataConverter<T> dataConverter = new DataConverter<T>(clazz);
        return of(clazz, dataConverter.getPrimaryKeyField().getType(), dataConverter);
    }

    protected static <T, K> DatabaseService<T, K> of(Class<T> itemClass,
                                                     Class<K> primaryKeyClass, DataConverter<T> dataConverter) {
        try {
            return new DatabaseService<T, K>(itemClass, primaryKeyClass, dataConverter);
        } catch (IllegalAccessException e) {
            throw new InternalDatabaseError();
        }
    }

    public static void deleteDatabase() {
        DeleteDbFiles.execute("~", databaseName, true);
    }

    protected DatabaseService(Class<T> itemClass, Class<K> primaryKeyClass, DataConverter<T> dataConverter)
            throws IllegalAccessException{
        this.itemClass = itemClass;
        this.primaryKeyClass = primaryKeyClass;
        this.dataConverter = dataConverter;
        this.primaryKeyField = dataConverter.getPrimaryKeyField();
    }
}
