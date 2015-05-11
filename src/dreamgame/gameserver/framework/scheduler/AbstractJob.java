package dreamgame.gameserver.framework.scheduler;

//import com.migame.gameserver.framework.db.DBException;
//import com.migame.gameserver.framework.db.DatabaseManager;
//import com.migame.gameserver.framework.db.IConnection;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
//import com.migame.gameserver.framework.protocol.IRequestMessage;
//import com.migame.gameserver.framework.protocol.IResponsePackage;
//import com.migame.gameserver.framework.session.ISession;
import org.quartz.Job;
//import org.quartz.JobDataMap;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;

public abstract class AbstractJob extends AbstractBusiness
  implements Job
{
  /*static final String JOB_DB_MODEL_NAME = "job.db.model.name";
  private String mDBModelName;

  void setDBModelName(String aModelName)
  {
    this.mDBModelName = aModelName;
  }

  protected IConnection openMasterConnection()
    throws DBException
  {
    return DatabaseManager.openConnection(this.mDBModelName, 3);
  }

  protected IConnection openSlaveConnection()
    throws DBException
  {
    return DatabaseManager.openConnection(this.mDBModelName, 2);
  }

  protected void closeSlaveConnection(IConnection aConn)
  {
    DatabaseManager.closeConnection(this.mDBModelName, aConn, false);
  }

  protected void closeMasterConnection(IConnection aConn, boolean aIsCommit)
  {
    DatabaseManager.closeConnection(this.mDBModelName, aConn, aIsCommit);
  }

  @Deprecated
  public final int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public final void execute(JobExecutionContext aJec)
    throws JobExecutionException
  {
    JobDataMap jobData = aJec.getMergedJobDataMap();

    String dbModelName = jobData.getString("job.db.model.name");

    setDBModelName(dbModelName);

    doJob();
  }

  protected abstract void doJob();*/
}