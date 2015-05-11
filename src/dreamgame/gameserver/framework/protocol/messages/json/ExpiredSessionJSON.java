package dreamgame.gameserver.framework.protocol.messages.json;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.messages.ExpiredSessionResponse;
import org.json.JSONObject;
import org.slf4j.Logger;

public class ExpiredSessionJSON
  implements IMessageProtocol
{
  private final Logger mLog;

  public ExpiredSessionJSON()
  {
    this.mLog = LoggerContext.getLoggerFactory().getLogger(ExpiredSessionJSON.class);
  }

  public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Object encode(IResponseMessage aResponseMessage) throws ServerException
  {
    JSONObject encodingObj;
    try {
      encodingObj = new JSONObject();

      encodingObj.put("mid", aResponseMessage.getID());
      ExpiredSessionResponse login = (ExpiredSessionResponse)aResponseMessage;
      encodingObj.put("error_msg", login.mErrorMsg);

      return encodingObj;
    }
    catch (Throwable t) {
      this.mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
      return null;
    }
  }
}