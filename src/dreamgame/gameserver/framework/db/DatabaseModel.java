package dreamgame.gameserver.framework.db;

public class DatabaseModel
{
  private String mModelName;
  private IConnectionManager mMasterMgr;
  private IConnectionManager mSlaveMgr;

  void setModelName(String aModelName)
  {
    this.mModelName = aModelName;
  }

  public String getModelName()
  {
    return this.mModelName;
  }

  public void addMaster(IConnectionManager aConnMgr)
  {
    this.mMasterMgr = aConnMgr;
  }

  public void addSlave(IConnectionManager aConnMgr)
  {
    this.mSlaveMgr = aConnMgr;
  }

  public IConnection openMasterConnection() throws DBException
  {
    if (this.mMasterMgr != null)
    {
      return this.mMasterMgr.openTransactionConnection();
    }

    throw new DBException(this.mModelName + " hasn't been initialized yet. Please initial master database before using it.");
  }

  public IConnection openSlaveConnection()
    throws DBException
  {
    if (this.mSlaveMgr != null)
    {
      return this.mSlaveMgr.openConnection();
    }

    throw new DBException(this.mModelName + " hasn't been initialized yet. Please initial slave database before using it.");
  }

  public void closeMasterConnection(IConnection aConn, boolean aIsCommit)
  {
    if (this.mMasterMgr != null)
    {
      this.mMasterMgr.closeConnection(aConn, aIsCommit);
    }
  }

  public void closeSlaveConnection(IConnection aConn)
  {
    if (this.mSlaveMgr != null)
    {
      this.mSlaveMgr.closeConnection(aConn);
    }
  }

  public void destroy()
  {
    if (this.mMasterMgr != null)
    {
      this.mMasterMgr.destroy();
    }
    if (this.mSlaveMgr != null)
    {
      this.mSlaveMgr.destroy();
    }
  }
}