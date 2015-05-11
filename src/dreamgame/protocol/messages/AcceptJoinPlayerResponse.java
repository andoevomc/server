/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class AcceptJoinPlayerResponse extends AbstractResponseMessage {

    public String message, username;
    public long acceptPlayerID,cash;
    public int available = 0; //cờ tướng: chấp
    public int totalTime = 0;
    public int avatar, level;

    public void setSuccess(int aCode, long playerID) {
        mCode = aCode;
        acceptPlayerID = playerID;
    }

    public void setFailure(int aCode, String msg) {
        mCode = aCode;
        message = msg;
    }

    public IResponseMessage createNew() {
        return new AcceptJoinPlayerResponse();
    }
}
