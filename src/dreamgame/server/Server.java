/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.server;

import dreamgame.databaseDriven.DatabaseDriver;

import dreamgame.gameserver.framework.common.ServerException;
//import com.migame.gameserver.framework.db.IConnection;
import dreamgame.gameserver.framework.workflow.SimpleWorkflow;
import java.sql.DriverManager;

/**
 *
 * @author binh_lethanh
 */
public class Server {

    public static void main(String[] args) throws ServerException {
        try {
            String userName = "root";
            String password = "";
           // String url = "jdbc:mysql://localhost:3307/com.migame";
            String url = "jdbc:mysql://localhost/db_gameonline";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            DatabaseDriver.conn = DriverManager.getConnection(url, userName, password);
            System.out.println("Database connection established");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot connect to database server");
            return;
        }
        SimpleWorkflow worker = new SimpleWorkflow();
        worker.start();
    }
}
