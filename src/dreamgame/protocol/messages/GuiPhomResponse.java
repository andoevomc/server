package dreamgame.protocol.messages;

import java.util.ArrayList;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GuiPhomResponse extends AbstractResponseMessage {
	
    public String phom="";
    public boolean haBaiFlag=false;
    public boolean u=false;
    
	public String message;
	public long dUID;
	public long sUID;
	public int phomID;
	//public ArrayList<Integer> cards;
        public String cards;
    public void setSuccess(int aCode, long duid, long suid, int phom)
    {
        mCode = aCode;
        //cards = cas;
        dUID = duid;
        sUID = suid;
        phomID = phom;
    }
    public void setFailure(int aCode, String msg){
    	mCode = aCode;
    	message = msg;
    }
    public IResponseMessage createNew()
    {
        return new GuiPhomResponse();
    }
}
