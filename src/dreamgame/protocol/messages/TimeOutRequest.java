/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author Admin
 */
public class TimeOutRequest extends AbstractRequestMessage {

    public long mMatchId;
    public String errMsg;
    public long player_friend_id;

    public IRequestMessage createNew() {
        return new TimeOutRequest();
    }
}
