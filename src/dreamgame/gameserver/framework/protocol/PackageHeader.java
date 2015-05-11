package dreamgame.gameserver.framework.protocol;

public class PackageHeader
{
  private String mSessionId;

  public String getSessionID()
  {
    return this.mSessionId;
  }

  public void setSessionID(String aSessionId)
  {
    this.mSessionId = aSessionId;
  }
}