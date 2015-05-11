package dreamgame.gameserver.framework.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class ExpiredSessionResponse extends AbstractResponseMessage
{
  public String mErrorMsg;

  public IResponseMessage createNew()
  {
    return new ExpiredSessionResponse();
  }
}