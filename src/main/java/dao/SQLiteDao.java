package dao;

import core.Settings;
import core.Tools;
import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created by muzafar on 6/14/17.
 */
public abstract class SQLiteDao {
    private final static String databaseURL = Settings.getSettings().getDatabaseURL();
    protected static Logger logger = Tools.logger;
    protected Statement s = null;
    protected ResultSet rs = null;
    private Connection c = null;

    protected void sqlOpen() {
        try {
            // create a connection to the database
            c = DriverManager.getConnection(databaseURL);

        } catch (SQLException e) {
            logger.error("Unable to connect to SQLite.");
            showSQLException(e);
        }
        try {
            c.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Unable to enable autocommit.");
            showSQLException(e);
        }
        try {
            s = c.createStatement();
        } catch (SQLException e) {
            logger.error("Unable to create a static SQL statement.");
            showSQLException(e);
            // We can't go much further without a result set, return...
        }
    }

    protected void sqlClose() {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            showSQLException(e);
        }

        try {
            if (s != null) s.close();
        } catch (SQLException e) {
            showSQLException(e);
        }

        try {
            if (c != null) c.close();
        } catch (SQLException e) {
            showSQLException(e);
        }
    }

    protected void showSQLException(SQLException e) {
        // Notice that a SQLException is actually a chain of SQLExceptions,
        // let's not forget to print all of them...
        SQLException next = e;
        while (next != null) {
            logger.error(next.getMessage() + "\n" +
                    "Error Code: " + next.getErrorCode() + "\n" +
                    "SQL State: " + next.getSQLState());
            next = next.getNextException();
        }
    }
}
