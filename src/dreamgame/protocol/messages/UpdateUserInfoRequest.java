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
public class UpdateUserInfoRequest extends AbstractRequestMessage
{

    public String newPassword;
    public String oldPassword;
    public String email;
    public String number;
    public IRequestMessage createNew()
    {
        return new UpdateUserInfoRequest();
    }
}
