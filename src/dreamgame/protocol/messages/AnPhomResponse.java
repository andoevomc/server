package dreamgame.protocol.messages;


import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class AnPhomResponse extends AbstractResponseMessage {
	
	public String message;
	public long money;
	public long uid;
	public long p_uid;
        
        public int cardValue=0;
        public long swap1,swap2;
        
        public boolean haBaiFlag=false;
        public long prePlayer=0;
        
        public boolean u=false;
        public String phom="";
	
    public void setSuccess(int aCode,long money,long uid,long p_uid)
    {
        mCode = aCode;
        this.uid = uid;
        this.money = money;
        this.p_uid = p_uid;
    }
    public void setFailure(int aCode, String msg){
    	mCode = aCode;
    	message = msg;
    }
    public IResponseMessage createNew()
    {
        return new AnPhomResponse();
    }
}
