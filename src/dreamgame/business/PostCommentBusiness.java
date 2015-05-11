/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.PostCommentRequest;
import dreamgame.protocol.messages.PostCommentResponse;
import dreamgame.protocol.messages.PostNewRequest;
import dreamgame.protocol.messages.PostNewResponse;
import dreamgame.protocol.messages.SuggestRequest;
import dreamgame.protocol.messages.SuggestResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

/**
 *
 * @author Dinhpv
 */
public class PostCommentBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(SuggestBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PostCommentBusiness - handleMessage");
	}
        mLog.debug("[ COMMENT ]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        PostCommentResponse resSuggest = (PostCommentResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            PostCommentRequest rqSuggest = (PostCommentRequest) aReqMsg;
            String name = rqSuggest.name;
            String note = rqSuggest.note;
            int postID = rqSuggest.postID;

            mLog.debug("[ COMMENT ]: of " + name);
            DatabaseDriver.insertComment(postID, name, note);
            String namepost = "";
            try {
                namepost = DatabaseDriver.getUserNameByPost(postID);
            } catch (Exception ee) {
            }
            if (!aSession.getUserName().equalsIgnoreCase(namepost)) {
                DatabaseDriver.updateNewComment(postID, 1);
            }
            resSuggest.setSuccess(ResponseCode.SUCCESS);
        } catch (Throwable t) {
            resSuggest.setFailure(ResponseCode.FAILURE, "Xử lý bị lỗi!!!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resSuggest != null) {
                aResPkg.addMessage(resSuggest);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
