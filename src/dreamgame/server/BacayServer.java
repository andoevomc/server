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
public class BacayServer {

    public static void main(String[] args) throws ServerException {        
        SimpleWorkflow worker = new SimpleWorkflow();

        worker.start();
    }
}
