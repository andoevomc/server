/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author binh_lethanh
 */
public class StartRequest extends AbstractRequestMessage {
    
    public long mMatchId;

    public IRequestMessage createNew()
    {
        return new StartRequest();
    }
}
