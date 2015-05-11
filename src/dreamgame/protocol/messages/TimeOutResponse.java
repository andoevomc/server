/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Admin
 */
public class TimeOutResponse extends AbstractResponseMessage {

    public String errMgs;
    public String timeout_player_name;
    public long player_friend_id;

    public IResponseMessage createNew() {
        return new TimeOutResponse();
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        errMgs = aErrorMsg;
    }

    public void setSuccess(int aCode, long player_friend_id, String timeout_player_name) {
        mCode = aCode;
        this.timeout_player_name = timeout_player_name;
        this.player_friend_id = player_friend_id;
    }
}
