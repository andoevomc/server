package dreamgame.gameserver.framework.db;

import java.util.Enumeration;
import java.util.Hashtable;

public class DatabaseManager
{
  public static final int DB_FLAG_UNUSE = 1;
  public static final int DB_FLAG_NONTRANSACTION = 2;
  public static final int DB_FLAG_TRANSACTION = 3;
  @SuppressWarnings("unchecked")
  private static final Hashtable<String, DatabaseModel> mModels = new Hashtable();

  public static void addModel(String aModelName, DatabaseModel aModel)
    throws DBException
  {
    if (mModels.containsKey(aModelName))
    {
      throw new DBException("Database Model with name = " + aModelName + " is already exist.");
    }

    aModel.setModelName(aModelName);
    mModels.put(aModelName, aModel);
  }

  public static IConnection openConnection(String aModelName, int aDbFlag)
    throws DBException
  {
    DatabaseModel model = (DatabaseModel)mModels.get(aModelName);
    
    if (aDbFlag == 3)
    {
      return model.openMasterConnection(); }
    if (aDbFlag == 2)
    {
      return model.openSlaveConnection();
    }

    return null;
  }

  public static void closeConnection(String aModelName, IConnection aConn, boolean aIsCommit)
  {
    if (aConn == null)
    {
      return;
    }

    DatabaseModel model = (DatabaseModel)mModels.get(aModelName);
    try
    {
      if (aConn.isTransaction())
      {
        model.closeMasterConnection(aConn, aIsCommit);
      }
      else
        model.closeSlaveConnection(aConn);
    }
    catch (DBException dbe)
    {
    }
  }

  public static void destroy(String aModelName)
  {
    DatabaseModel model = (DatabaseModel)mModels.get(aModelName);
    model.destroy();
  }
  @SuppressWarnings("unchecked")
  public static void destroy()
  {
    Enumeration dbModels = mModels.elements();
    while (dbModels.hasMoreElements())
    {
      DatabaseModel model = (DatabaseModel)dbModels.nextElement();
      model.destroy();
    }
  }
}