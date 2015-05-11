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
public class CancelRequest extends AbstractRequestMessage
{

    public long mMatchId;
    public long uid;
    public boolean isLogout;
    public boolean isOutOfGame;
    public IRequestMessage createNew()
    {
        return new CancelRequest();
    }
}
