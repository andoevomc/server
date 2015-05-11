package dreamgame.gameserver.framework.db.primrose;

import dreamgame.gameserver.framework.db.DBException;
import dreamgame.gameserver.framework.db.DatabaseModel;
import dreamgame.gameserver.framework.db.IConnectionManager;
import dreamgame.gameserver.framework.db.IConnectionManagerFactory;

public class PrimroseConnectionManagerFactory
  implements IConnectionManagerFactory
{
	
  @SuppressWarnings("unused")
private final String mMasterConfig = "conf/primrose-master.config";
  @SuppressWarnings("unused")
private final String mSlaveConfig = "conf/primrose-slave.config";

  public IConnectionManager createConnectionManager()
    throws DBException
  {
    return new PrimroseConnectionManager();
  }

  public DatabaseModel createDatabaseMode()
    throws DBException
  {
    DatabaseModel dbModel = new DatabaseModel();

    IConnectionManager masterMgr = createConnectionManager();
    masterMgr.init("conf/primrose-master.config");
    dbModel.addMaster(masterMgr);

    IConnectionManager slaveMgr = createConnectionManager();
    slaveMgr.init("conf/primrose-slave.config");
    dbModel.addSlave(slaveMgr);

    return dbModel;
  }
}