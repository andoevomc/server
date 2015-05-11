package dreamgame.gameserver.framework.servers.nettysocket;

import java.util.Vector;

import dreamgame.data.MessagesID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.CancelRequest;

import dreamgame.gameserver.framework.bytebuffer.ByteBufferFactory;
import dreamgame.gameserver.framework.bytebuffer.ByteBufferImpl;
import dreamgame.gameserver.framework.bytebuffer.IByteBuffer;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.workflow.IWorkflow;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;

public class NettySocketHandler extends SimpleChannelUpstreamHandler {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(NettySocketHandler.class);
    private IWorkflow mWorkflow;

    public NettySocketHandler(IWorkflow aWorkflow) {
        this.mWorkflow = aWorkflow;
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        ISession session;
        try {
            //mLog.debug("[Netty Socket] messageReceived : "+ctx.getName()+" : "+e+" : ");
            session = (ISession) ctx.getAttachment();
            //mLog.debug("[Netty Socket] messageReceived: "+session.getUserName()+" : "+session.getIP());
            
            byte[] resData = null;
            ChannelBuffer requestBuffer = (ChannelBuffer) e.getMessage();
            if (requestBuffer.readable()) {
                byte[] data = requestBuffer.array();
                String requestString = new String(data);
                //System.out.println(requestString);
                mLog.debug("[Netty Socket] messageReceived: "+requestString);
                
                if (requestString.equalsIgnoreCase("<policy-file-request/>" + '\0')) {
                    session.write("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\"/></cross-domain-policy>" + '\0');
                    return;
                }
                
                
           // big request -> cache it, small data -> process     
                boolean isDataReady = false;
                IByteBuffer dataBuffer = null;
                IByteBuffer dataBuffer1 = ByteBufferFactory.wrap(data);
                
                // request package <= 10 bytes -> chunk request
                if (dataBuffer1.capacity() < 11) {
                    session.addIncompleteDataChunk(dataBuffer1);
                    if (session.getIncompleteDataLength() == session.getCompleteDataLength()) {
                        dataBuffer = ByteBufferFactory.wrap(session.getCompleteData());
                        isDataReady = true; 
                    }
                }
                // process normal request
                else {
                    int length = dataBuffer1.getInt();
                    int remain = dataBuffer1.remaining();
                    // request contains json string -> new request
                    dataBuffer1.skip(2);                // skip length of string
                    byte[] jsonData = new byte[4];      // read string content
                    dataBuffer1.get(jsonData);
                    String sentS = new String(jsonData, "UTF-8");
                    
                    dataBuffer1.position(0);    // rewind buffer position to 0
                    // new request
                    if (sentS.equalsIgnoreCase("json")) {
                        // small request -> process
                        if (length == remain) {
                            dataBuffer = ByteBufferFactory.wrap(data);
                            isDataReady = true;
                        }
                        // big request -> cache it
                        else {
                            session.resetIncompleteData();
                            session.setCompleteDataLength(length + 4);
                            session.addIncompleteDataChunk(dataBuffer1);
                        }
                    }
                    // chunk request
                    else {
                        // append the cache
                        session.addIncompleteDataChunk(dataBuffer1);
                        if (session.getIncompleteDataLength() == session.getCompleteDataLength()) {
                            dataBuffer = ByteBufferFactory.wrap(session.getCompleteData());
                            isDataReady = true; 
                        }
//                        // not completed yet -> store the cache and wait for next request
//                        else if (session.getIncompleteDataLength() < session.getCompleteDataLength()) {
//                            session.setIncompleteData(destination);
//                        }
                    }
                }
                
                // data received completed -> process it
                if (isDataReady) {
                    IByteBuffer resultBuffer = this.mWorkflow.process(session, dataBuffer);
                    
                    if (resultBuffer != null) {
                       resData = resultBuffer.array();
                    }
                }                
            }

            if ((!(session.isClosed())) && (resData != null)) {
                boolean isDirect = session.isDirect();

                ChannelBuffer responseBuffer = ChannelBuffers.copiedBuffer(resData);
                ChannelFuture future = e.getChannel().write(responseBuffer);
                if (!(isDirect)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        } catch (ServerException se) {
            this.mLog.error("[Netty Socket] Request Process Error", se);
        }
        catch (Exception ex) {
            this.mLog.error("[Netty Socket] Request Process Error", ex);
        }
    }
    //BINHLT - Cancel Message while disconnected.
    private void cancelTable(ISession session){
    	IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();

        MessageFactory msgFactory = session.getMessageFactory();
        IBusiness business = null;
        long uid = session.getUID();
        Vector<Room> joinedRoom = session.getJoinedRooms();
        long matchID; // Find match
        if (joinedRoom.size() > 0) {
            Room room = joinedRoom.lastElement();
            matchID = room.getRoomId();
        } else {
            matchID = -1;
        }
        // Case
        business = msgFactory.getBusiness(MessagesID.MATCH_CANCEL);
        CancelRequest rqMatchCancel =
                (CancelRequest) msgFactory.getRequestMessage(MessagesID.MATCH_CANCEL);
        rqMatchCancel.uid = uid;
        rqMatchCancel.mMatchId = matchID;
        rqMatchCancel.isLogout = true;

        try {
            business.handleMessage(session, rqMatchCancel, responsePkg);
        } catch (ServerException se) {
            this.mLog.error("[Netty Socket] Exception Catch Error!", se.getCause());
        }
    }
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        //Begin
        //ISession session = (ISession) ctx.getAttachment();
        e.getChannel().close();
        //this.mLog.error("[Netty Socket] Unexpected Exception: ", e.getCause());
        // End


    }

    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        Channel currentChannel;
        try {
            currentChannel = e.getChannel();
            ISession session = this.mWorkflow.sessionCreated(currentChannel);
            session.setIP(currentChannel.getRemoteAddress().toString());
            
            ctx.setAttachment(session);

            super.channelConnected(ctx, e);
            this.mLog.debug("[Netty Socket] Channel Connected: " + currentChannel.getRemoteAddress() + ", " + currentChannel.getId());
        } catch (ServerException se) {
            this.mLog.error("[Netty Socket] Channel Connected Exception", se);
        }
    }

    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        ISession session = (ISession) ctx.getAttachment();
        if (session != null) {
            this.mLog.debug("Channel Disconnected: " + session.getID());
            cancelTable(session);
            session.sessionClosed();
        }
        super.channelDisconnected(ctx, e);
    }

    public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelBound(ctx, e);
    }

    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelClosed(ctx, e);
    }

    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelOpen(ctx, e);
        
        //tuanha added
        System.out.println("Add Channel");
        System.out.println("Add : "+NettySocketServer.allChannels.add(e.getChannel()));
        System.out.println("allChannels.size() : "+NettySocketServer.allChannels.size());
    }

    public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelUnbound(ctx, e);
    }
}
