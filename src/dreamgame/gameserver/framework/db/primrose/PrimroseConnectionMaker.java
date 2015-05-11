package dreamgame.gameserver.framework.db.primrose;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.db.DBException;
import dreamgame.gameserver.framework.db.IConnection;
import dreamgame.gameserver.framework.db.IConnectionMaker;
import dreamgame.gameserver.framework.db.SimpleConnnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.slf4j.Logger;
import uk.org.primrose.vendor.standalone.PrimroseLoader;

public class PrimroseConnectionMaker
  implements IConnectionMaker
{
  private DataSource mDataSource = null;
  private String mPoolName;
  private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(PrimroseConnectionMaker.class);

  public void destroy()
  {
    try
    {
      PrimroseLoader.stopPool(this.mPoolName);
    }
    catch (Throwable t) {
      this.mLog.error("[PRIMROSE] STOP pool error.", t);
    }
  }
  @SuppressWarnings("unchecked")
  void init(String aSource)
    throws DBException
  {
    List loadedPoolNames;
    try
    {
      loadedPoolNames = PrimroseLoader.load(aSource, true);
      this.mPoolName = ((String)loadedPoolNames.get(0));
      Context ctx = new InitialContext();
      this.mDataSource = ((DataSource)ctx.lookup("java:comp/env/" + this.mPoolName));
    }
    catch (Exception ex) {
      throw new DBException(ex);
    }
  }

  public IConnection makeConnection()
    throws DBException
  {
    Connection con;
    try
    {
      con = this.mDataSource.getConnection();
      SimpleConnnection crConn = new SimpleConnnection(con);
      return crConn;
    }
    catch (SQLException ex) {
      throw new DBException(ex);
    }
  }
}