/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author Dinhpv
 */
public class LotteryRequest extends AbstractRequestMessage {

    public int date=0;
    public int type=0;
    public String value;

    public IRequestMessage createNew() {
        return new LotteryRequest();
    }
}
