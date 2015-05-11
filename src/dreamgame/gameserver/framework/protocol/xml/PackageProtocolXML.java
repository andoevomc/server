package dreamgame.gameserver.framework.protocol.xml;

import dreamgame.gameserver.framework.bytebuffer.ByteBufferFactory;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractPackageProtocol;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestPackage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.protocol.PackageHeader;
import dreamgame.gameserver.framework.protocol.SimpleRequestPackage;
import dreamgame.gameserver.framework.session.ISession;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class PackageProtocolXML extends AbstractPackageProtocol
{
  private final Logger mLog;

  public PackageProtocolXML()
  {
    this.mLog = LoggerContext.getLoggerFactory().getLogger(PackageProtocolXML.class);
  }

  public IRequestPackage decode(ISession aSession, IByteBuffer aEncodedObj) throws ServerException
  {
    IRequestPackage pkgRequest;
    try {
      pkgRequest = new SimpleRequestPackage();

      PackageHeader header = pkgRequest.getRequestHeader();

      String reqData = aEncodedObj.getString();

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(reqData));
      Document doc = db.parse(is);

      Element rootEle = doc.getDocumentElement();

      Node sessionidNode = rootEle.getElementsByTagName("sessionid").item(0);

      String sessionId = sessionidNode.getTextContent();

      header.setSessionID(sessionId);

      NodeList requestList = rootEle.getElementsByTagName("requests");
      int size = requestList.getLength();

      for (int i = 0; i < size; ++i)
      {
        Node requestNode = requestList.item(i);
        NamedNodeMap midNode = requestNode.getAttributes();
        Node node = midNode.getNamedItem("mid");

        String msgIdStr = node.getNodeValue();

        int msgId = Integer.parseInt(msgIdStr);
        this.mLog.debug("Message " + msgId);

        IMessageProtocol msgProtocol = getMessageProtocol(msgId);

        MessageFactory msgFactory = aSession.getMessageFactory();
        IRequestMessage requestMsg = msgFactory.getRequestMessage(msgId);

        boolean decodedResult = msgProtocol.decode((Element)requestNode, requestMsg);

        if (decodedResult)
        {
          pkgRequest.addMessage(requestMsg);
        }
      }

      return pkgRequest;
    }
    catch (Throwable t) {
      throw new ServerException(t);
    }
  }
  @SuppressWarnings("unchecked")
  public IByteBuffer encode(ISession aSession, IResponsePackage aResPkg)
    throws ServerException
  {
    String sessionId;
    try
    {
      sessionId = aSession.getID();

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      Element rootElement = document.createElement("data");
      document.appendChild(rootElement);
      Element sessionEle = document.createElement("sessionid");
      sessionEle.appendChild(document.createTextNode(sessionId));

      rootElement.appendChild(sessionEle);

      Vector pkgData = aResPkg.optAllMessages();
      int size = pkgData.size();
      for (int i = 0; i < size; ++i)
      {
        IResponseMessage resMsg = (IResponseMessage)pkgData.get(i);

        IMessageProtocol msgProtocol = getMessageProtocol(resMsg.getID());

        Element resEle = (Element)msgProtocol.encode(resMsg);
        if (resEle != null)
        {
          Element responseElem = document.createElement("responses");

          NamedNodeMap attrList = resEle.getAttributes();
          int numOfAttr = attrList.getLength();
          for (int j = 0; j < numOfAttr; ++j)
          {
            String attName = attrList.item(j).getNodeName();
            String attValue = attrList.item(j).getNodeValue();
            responseElem.setAttribute(attName, attValue);
          }
          rootElement.appendChild(responseElem);
        }
      }

      DOMSource domSource = new DOMSource(document);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty("method", "html");
      transformer.setOutputProperty("encoding", "utf-8");
      StringWriter sw = new StringWriter();
      StreamResult sr = new StreamResult(sw);
      transformer.transform(domSource, sr);
      String resData = sw.toString();

      String pkgFormat = aSession.getPackageFormat();

      int dataSize = resData.getBytes("utf-8").length + 2 + pkgFormat.getBytes("utf-8").length + 2;

      IByteBuffer encodingBuffer = ByteBufferFactory.allocate(dataSize + 4);

      encodingBuffer.putInt(dataSize);

      encodingBuffer.putString(pkgFormat);

      encodingBuffer.putString(resData);

      encodingBuffer.flip();
      return encodingBuffer;
    }
    catch (Throwable t) {
      throw new ServerException(t);
    }
  }
}