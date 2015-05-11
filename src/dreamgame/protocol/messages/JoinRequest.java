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
public class JoinRequest extends AbstractRequestMessage {
    public long mMatchId;
    public long uid;
    public String password;
    public int zone_id=-1;
    public boolean quickplay=false;
    
    public IRequestMessage createNew()
    {
        return new JoinRequest();
    }
}
