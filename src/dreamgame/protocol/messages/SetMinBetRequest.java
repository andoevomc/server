/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.cotuong.data.moveFromTo;
import java.util.ArrayList;

/**
 *
 * @author Thomc
 */
public class SetMinBetRequest extends AbstractRequestMessage {

    public long mMatchId;
    public String errMsg;
    public long moneyBet;

    public IRequestMessage createNew() {
        return new SetMinBetRequest();
    }
}
