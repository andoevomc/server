/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.data.PostEntity;
import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.Vector;

/**
 *
 * @author Dinhpv
 */
public class PostDetailRequest  extends AbstractRequestMessage
{
    public int postID;
    public IRequestMessage createNew()
    {
        return new PostDetailRequest();
    }
}
