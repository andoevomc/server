/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author binh_lethanh
 */
public class StartedResponse extends AbstractResponseMessage {

    public long mUid;
    public boolean isFinalFight;
    public long starterID;
    
    public void setStarterID(long starterID) {
		this.starterID = starterID;
	}
    
    public void setIsFinalFight(boolean aIs){
    	isFinalFight = aIs;
    }
    public void setSuccess(int aCode) {
        mCode = aCode;
    }

    public void setSuccess(int aCode, long aUid) {
        mCode = aCode;
        mUid = aUid;
    }

    public IResponseMessage createNew() {
        return new StartedResponse();
    }
}
