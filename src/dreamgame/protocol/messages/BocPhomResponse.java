package dreamgame.protocol.messages;

import phom.data.Poker;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class BocPhomResponse extends AbstractResponseMessage {

    public String message;
    public int card=-1;
      
    public String phom="";
    public boolean haBaiFlag=false;
    public boolean u=false;

    public void setSuccess(int aCode, int ca) {
        mCode = aCode;
        card = ca;
    }   

    public void setSuccess(int aCode) {
        mCode = aCode;
    }

    public void setFailure(int aCode, String msg) {
        mCode = aCode;
        message = msg;
    }

    public IResponseMessage createNew() {
        return new BocPhomResponse();
    }
}
