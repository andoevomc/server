package dreamgame.gameserver.framework.protocol.json;

import dreamgame.bacay.data.BacayTable;
import dreamgame.config.DebugConfig;
import dreamgame.data.SimpleTable;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
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
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import phom.data.PhomTable;

public class PackageProtocolJSON extends AbstractPackageProtocol
{
  private final Logger mLog;

  public PackageProtocolJSON()
  {
    this.mLog = LoggerContext.getLoggerFactory().getLogger(PackageProtocolJSON.class);
  }

    public void printPhomData(ISession aSession, String s) {
//	int previousMethodCallLevel;
//	if (DebugConfig.FOR_DEBUG) {
//	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	    DebugConfig.CALL_LEVEL ++;
//	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PackageProtocolJSON - printPhomData");
//	}
	
        try {
            //System.out.println("u : "+aSession.getUserName());
            
            if (DatabaseDriver.log_code==false) return;
            
            s=s.replace("com.migame.protocol.messages.", "");
            s=s.replace("Request", "");
            s=s.replace("Response", "");
                        
            Vector<Room> joinedRoom = aSession.getJoinedRooms();
            String rStr="";            

            if (joinedRoom.size() > 0 ) {

                //if(joinedRoom.size()>1)
                {
                    for (Room r: joinedRoom)
                    {
                        rStr=rStr+"_"+r.getRoomId();
                    }
                    rStr=" {in match : "+rStr+"}";
                }

                for (Room r : joinedRoom) {
                    try {
                        //Room r = joinedRoom.firstElement();
                        SimpleTable p = (SimpleTable) r.getAttactmentData();
                        if (p.out_code == null) {
                            p.initLogFile();
                        }

                        if (s.contains("Receiv")) {
                            p.logCode();
                        }

                        String s1 = " [ti_" + System.currentTimeMillis() + "]";
                        if (s.contains("Receiv")) {
                            s1 = rStr + s1;
                        }
//                if (s.contains("Receiv") && p.ownerSession!=null)
                        //                  s1=" [o:"+(System.currentTimeMillis()-p.ownerSession.getLastMessage())+"ms]["+p.isPlaying+"]";

                        p.logCode(s + s1);
                        //p.out_code.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }else
                if (DatabaseDriver.log_user)
                    aSession.logCode(s);

        } catch (Exception e) {
        }
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
    }

  public IRequestPackage decode(ISession aSession, IByteBuffer aEncodedObj) throws ServerException {
//    int previousMethodCallLevel;
//    if (DebugConfig.FOR_DEBUG) {
//	previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	DebugConfig.CALL_LEVEL ++;
//	DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PackageProtocolJSON - decode");
//    }
    
    String reqData;
    try {
      reqData = aEncodedObj.getString();
      JSONObject jsonPkg = new JSONObject(reqData);

      IRequestPackage pkgRequest = new SimpleRequestPackage();

//      PackageHeader header = pkgRequest.getRequestHeader();
//      String sessionId = jsonPkg.getString("sessionid");
//      header.setSessionID(sessionId);

      aSession.receiveMessage();
      JSONArray requests = jsonPkg.getJSONArray("requests");
      int size = requests.length();
            
      for (int i = 0; i < size; ++i)
      {
        JSONObject jsonMsg = (JSONObject)requests.get(i);

        int msgId = jsonMsg.getInt("mid");

        if (DatabaseDriver.log_all_code)
            this.mLog.debug("[Received]"+ aSession.userInfo() + msgId+" : "+jsonMsg);
        else
            System.out.println("[Received]"+ aSession.userInfo() + msgId+" : "+jsonMsg);
        
        IMessageProtocol msgProtocol = getMessageProtocol(msgId);

        MessageFactory msgFactor = aSession.getMessageFactory();
        IRequestMessage requestMsg = msgFactor.getRequestMessage(msgId);

        if (aSession.getUserName()==null && jsonMsg.has("username") && jsonMsg.getString("username").length()>0)
        {
            aSession.setUserName(jsonMsg.getString("username"));
        }
        
        printPhomData(aSession,"[Received]"+aSession.userInfo()+"[ msg : " + msgId+" : "+requestMsg+" ] : "+jsonMsg);

        boolean decodedResult = msgProtocol.decode(jsonMsg, requestMsg);

        if (decodedResult)
        {
          pkgRequest.addMessage(requestMsg);
        }
      }
      
//      if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
      return pkgRequest;
    }
    catch (Exception  e) {
        mLog.error("Decode error : "+e);
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	throw new ServerException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public IByteBuffer encode(ISession aSession, IResponsePackage aResPkg) throws ServerException
  {
//      int previousMethodCallLevel;
//      if (DebugConfig.FOR_DEBUG) {
//	  previousMethodCallLevel = DebugConfig.CALL_LEVEL;
//	  DebugConfig.CALL_LEVEL ++;
//	  DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PackageProtocolJSON - encode");
//      }
      
    JSONObject jsonPkg;
    try
    {
      jsonPkg = new JSONObject();

      String sessionId = aSession.getID();
      jsonPkg.put("sessionid", sessionId);

      Vector pkgData = aResPkg.optAllMessages();

      JSONArray responses = new JSONArray();
      int size = pkgData.size();
      for (int i = 0; i < size; ++i)
      {
        IResponseMessage resMsg = (IResponseMessage)pkgData.get(i);

        IMessageProtocol msgProtocol = getMessageProtocol(resMsg.getID());

        JSONObject jsonMsg = (JSONObject)msgProtocol.encode(resMsg);
        jsonMsg.put("servertime", System.currentTimeMillis());
        
        if (DatabaseDriver.log_all_code)
            this.mLog.debug("[Send] [id "+aSession.getUserName()+"]["+aSession.getUID()+"]"+ jsonMsg.getInt("mid")+" : "+jsonMsg);
        else
            System.out.println("[Send] [id "+aSession.getUserName()+"]["+aSession.getUID()+"]"+ jsonMsg.getInt("mid")+" : "+jsonMsg);
        
        
        if (aSession.getUserName()==null && jsonMsg.has("username")&&jsonMsg.getString("username").length()>0)
        {
            aSession.setUserName(jsonMsg.getString("username"));
        }
        
        printPhomData(aSession,"[Send]["+aSession.getUserName()+"]["+aSession.getUID()+"-"+aSession.getCurrentZone()+"] [ msg : " + jsonMsg.getInt("mid")+" : "+resMsg+" ] : "+jsonMsg);

        if (jsonMsg != null)
        {
          responses.put(jsonMsg);
        }
      }

      jsonPkg.put("responses", responses);

      String resData = jsonPkg.toString();

      String pkgFormat = aSession.getPackageFormat();

      int dataSize = resData.getBytes("utf-8").length + 2 + pkgFormat.getBytes("utf-8").length + 2;

      IByteBuffer encodingBuffer = ByteBufferFactory.allocate(dataSize + 4);

      encodingBuffer.putInt(dataSize);

      encodingBuffer.putString(pkgFormat);

      encodingBuffer.putString(resData);

      encodingBuffer.flip();
      
//      if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
      return encodingBuffer;
    }
    catch (Throwable t) {
        mLog.error("Encode error : "+t);
//	if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
	throw new ServerException(t);
    }
  }
}