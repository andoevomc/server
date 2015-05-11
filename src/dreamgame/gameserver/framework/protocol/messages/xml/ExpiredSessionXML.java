// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 20-Aug-10 8:49:14 AM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ExpiredSessionXML.java

package dreamgame.gameserver.framework.protocol.messages.xml;

import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.messages.ExpiredSessionResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExpiredSessionXML
    implements IMessageProtocol
{

    public ExpiredSessionXML()
    {
    }

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj)
        throws ServerException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage)
        throws ServerException
    {
        try
        {
            ExpiredSessionResponse login = (ExpiredSessionResponse)aResponseMessage;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element requestEle = document.createElement("responses");
            requestEle.setAttribute("mid", Integer.toString(aResponseMessage.getID()));
            requestEle.setAttribute("error_msg", login.mErrorMsg);
            return requestEle;
        }
        catch(Throwable t)
        {
            mLog.error((new StringBuilder()).append("[ENCODER] ").append(aResponseMessage.getID()).toString(), t);
        }
        return null;
    }

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ExpiredSessionXML.class);
}