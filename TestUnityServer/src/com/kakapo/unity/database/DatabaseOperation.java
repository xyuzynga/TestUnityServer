/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author gopikrishnan.v <p>This class is used for Database Operations.</p>
 */
public final class DatabaseOperation {

    /**
     *
     */
    static Connection objConnectionMYSQL;
    static DatabaseOperation objDatabaseOperation;

    /**
     * Constructor for the singleton class DatabaseOperation.DatabaseOperation
     * class is used to perform database operations.It contains 2 methods
     * executeUpdate() and executeQuery().
     *
     * @param strConnectionURL - The connection url to the MYSQL database.
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private DatabaseOperation(String strConnectionURL) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        objConnectionMYSQL = DriverManager.getConnection(strConnectionURL);
    }

    /**
     * executeUpdate() method executes the update statements in the database.
     *
     * @param strSqlStatement - SQL update query that which is to be executed.
     * @throws SQLException
     */
    public void executeUpdate(String strSqlStatement) throws SQLException {
        try (PreparedStatement ps = objConnectionMYSQL.prepareStatement(strSqlStatement)) {
            ps.executeUpdate();
        }
    }

    /**
     * executeQuery() method executes an sql query passed to it and returns a
     * ResultSet that contains the result to the query.
     *
     * @param strSqlStatement - the sql query that is to be executed to return a
     * ResultSet from the database.
     * @return ResultSet object which is the result of the sql query(argument to
     * the method)
     * @throws SQLException
     */
    public ResultSet executeQuery(String stringSqlStatement) throws SQLException {
        return objConnectionMYSQL.prepareStatement(stringSqlStatement).executeQuery();
    }

    /**
     * A method to return the singleton object of this class.
     *
     * @param connectionURL - connection URL to the MYSQL database server.
     * @return - The single object of class DatabaseOperation.If the object was
     * not instantiated previously it is instantiated with the received
     * connection parameters.
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static DatabaseOperation getInstance(String strConnectionURL) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (objDatabaseOperation == null) {
            objDatabaseOperation = new DatabaseOperation(strConnectionURL);
        }
        return objDatabaseOperation;
    }

    /**
     * A method to return the singleton object of this class.
     *
     * @return The single object of class DatabaseOperation.If the object was
     * not instantiated previously throws a null pointer exception.
     */
    public static DatabaseOperation getInstance() {
        if (objDatabaseOperation != null) {
            return objDatabaseOperation;
        } else {
            throw new NullPointerException("Database connection not established");
        }
    }
}
