package dreamgame.gameserver.framework.memcache;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;

class MemcacheClientImpl extends AbstractMemcacheClient
{
  private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(MemcacheClientImpl.class);
  private MemcachedClient mClient;
  private String mNameSpace;
  @SuppressWarnings("unused")
private final int MAX_KEY_LEN = 250;

  public MemcacheClientImpl(String aAddrList, String aNameSpace)
    throws Throwable
  {
    this.mClient = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(aAddrList));
    this.mNameSpace = aNameSpace;
  }

  public void set(String aKey, int aExpiration, Object aSerializableObj)
  {
    if (aSerializableObj != null)
    {
      try
      {
        String fullKey = this.mNameSpace + "_" + aKey;

        fullKey.replace("/", "SsS");
        if (fullKey.length() <= 250)
        {
          this.mClient.set(fullKey, aExpiration, aSerializableObj);
        }
      }
      catch (Throwable t) {
        this.mLog.error("[MEMCACHE]", t);
      }
    }
  }
  @SuppressWarnings("unchecked")
  public Object get(String aKey)
  {
    Object valObj = null;
    String fullKey = this.mNameSpace + "_" + aKey;

    fullKey.replace("/", "SsS");
    if (fullKey.length() <= 250)
    {
      Future f = this.mClient.asyncGet(fullKey);
      try
      {
        valObj = f.get(5L, TimeUnit.SECONDS);
      }
      catch (Throwable t)
      {
        this.mLog.warn("[MEMCACHE] get key = " + fullKey + " timeout within 5 seconds.");

        f.cancel(true);
      }
    }

    return valObj;
  }

  public void delete(String aKey)
  {
    String fullKey;
    try {
      fullKey = this.mNameSpace + "_" + aKey;

      fullKey.replace("/", "SsS");
      this.mClient.delete(fullKey);
    }
    catch (Throwable t) {
      this.mLog.error("[MEMCACHE]", t);
    }
  }

  public void close()
  {
    try
    {
      this.mClient.shutdown();
    }
    catch (Throwable t) {
      this.mLog.error("[MEMCACHE]", t);
    }
  }
}