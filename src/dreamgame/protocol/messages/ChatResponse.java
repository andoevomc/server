package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class ChatResponse extends AbstractResponseMessage {

    public String mErrorMsg;
   // public boolean  mUid;
    public String mMessage;
      public String mUsername;
        public long roomid;
   public int type;
    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode) {
        mCode = aCode;
   //     mUid = aUid;
    }

    public void setMessage(String aMessage) {
        mMessage = aMessage;
    }
    public void setUsername(String aUsername) {
        mUsername = aUsername;
    }

     public void setRoomID(long aroomid) {
        roomid = aroomid;
    }
     public void setType(int aType) {
        type = aType;
    }



    public IResponseMessage createNew() {
        return new ChatResponse();
    }

}
