/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import snaq.db.ConnectionPool;

/**
 *
 * @author gopikrishnan.v
 * <p>This class is used for Database Operation</p>
 */
public class DatabaseOperation {
    /**
     *  To set the driver
     */
private static final String DbDriver   = "com.mysql.jdbc.Driver";  //
/**
 * Connection string 
 */

private static final String strConnection    = "jdbc:mysql://10.10.10.28:3306/test"; // 
/**
 * Username of the database user
 */

private static final String strDbUsername = "test"; // 
/**
 * Password of the database user
 */
private static final String strDbPassword = "test"; // 
/**
 * Connection object to establish the connecting to the database
 */

private static Connection   DatabaseConnection; // 
/**
 *  Resultset to store the information retrieved from the database
 */
private static ResultSet    RsStudent; //
/**
 * Statement object of the database connection
 */
private static PreparedStatement    DatabaseStatement; // 
/**
 * connection pool to hold the MySQL database  connection objects.
 */
static ConnectionPool pool = new ConnectionPool("hello",
        5, 10, 10000, 180000, strConnection, strDbUsername, strDbPassword);


  

   
/**
 * getter for the DatabaseStatement
 * @return PreparedStatement
 */
    public static PreparedStatement getDatabaseStatement() {
        return DatabaseStatement;
    }

   

   
/**
 * to load the properties from a property file
 * @param filepath for the property file as string
 */
public void loadProperties(String filepath) //to load the properties from a file
{

}

/**
 * Constructor of the class DatabaseOperation
 */
public DatabaseOperation () // Constructor of the class
{
    makeConnection(); // To establish the database connection
}

/**
 * To establish the database connection
 * @return true on successful connection and false otherwise
 */
public boolean makeConnection() // To establish the database connection, No return value and parameters
{ boolean connected=false;
    try
    {
        Class.forName(DbDriver); // Setting the driver

        DatabaseConnection = pool.getConnection(); // Establishing the connection with the database

        DatabaseStatement =  DatabaseConnection.prepareStatement(DbDriver); // Assigning the statement to the connection
        if(DatabaseStatement==null)
        {
            return connected;
        }
        System.out.println("Connection Success....");
        connected=true;
    }
    catch (Exception e) // In case of any Exception
    {
        System.out.println(e); // Print the Exception
    }finally{
        return connected;
    }
}
/**
 * A function which inserts the datavalues into the databases, No return value, Query as Parameter
 * @param Insert Query as string
 * @return true on successful insertion and false otherwise
 */
public boolean insertIntoDatabase(String strInsertQuery) // 
{
    boolean IsResult = false; // To test whether the record is inserted

    try
    {
        IsResult = DatabaseStatement.execute(strInsertQuery); // Execute the insert Query

        //DatabaseConnection.commit(); // Commit
    }
    catch (Exception e)
    {
        System.out.println(e); // Incase of any error print the error
    }finally{
        return IsResult;
    }
}
/**
 * A function that performs update query 
 * @param strUpdateQuery - update query as string
 * @return - number of rows affected as integer
 */
public int updataDatabase(String strUpdateQuery) // A function which updates the changes into the database, No return type, Query as parameter
{int row=0;
    try
    {
         row = DatabaseStatement.executeUpdate(strUpdateQuery); // Returns No of rows affected
    }
    catch (Exception e)
    {
        System.out.println(e); // In case of errors, print the error
    }finally{
        return row;
    }
}

/**
 *  A function which retrieves Records from the database, Returns Recordset, Query as parameter
 * @param strSelectQuery - sql query string to perform the select 
 * @return sql query result set
 * 
 */
public ResultSet selectFromDatabase(String strSelectQuery) // A function which retrieves Records from the database, Returns Recordset, Query as parameter
{
    try
    {
        RsStudent = DatabaseStatement.executeQuery(strSelectQuery); // Execute the select query
        return RsStudent; // Returns the resultset
    }
    catch (Exception e)
    {
        System.out.println(e); // Print the exception
        return null; // Return NULL
    }
}
/**
 * A function Which is used to execute delete Queries, No Return type, Query as Argument
 * @param strDeleteQuery - sql query as string
 * @return  - true on successful execution of the query and false otherwise
 */
public boolean deleteFromDB(String strDeleteQuery) // A function Which is used to execute delete Queries, No Return type, Query as Argument
{
    boolean IsReturn = false; // To check Whether the Query is appropriately Executed
    try
    {
        IsReturn = DatabaseStatement.execute(strDeleteQuery); // Executing the Query
    }
    catch (Exception e)
    {
        System.out.println(e); // Incase of Error, Print the error
    }finally{
        return IsReturn;
    }
}

/**
 * 
 * a function to disconnect from the MySql server
 * 
 */
public void disConnect() {
    try {
        DatabaseConnection.close();
    } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    // TODO Auto-generated method stub

}
    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
       while(true){
            DatabaseOperation db = new DatabaseOperation();
            System.out.println(DatabaseOperation.pool.getSize());
   }
            
}



    
    }

