package test.performant;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unchecked")
public class SocketServices {

    /**
     * constants
     */
    private static final int MAX_WRITE_BUFFER = 10 * 1024;
    private static final int MAX_READ_BUFFER = 10 * 1024;
    /**
     * fields
     */
    
	private final Vector mReqMsgs = new Vector();
    private final Vector mListeners = new Vector();
    // Socket's information
    private SocketConnection mConnection;
    
    private DataOutputStream mRequestStream;
    private DataInputStream mResponseStream;
    // The current host.
    private String mHost;
    // The current port.
    private int mPort;
    // The game controll
    private Thread mReaderThread;
    private Thread mWriterThread;
    // Socket's state
    private boolean mConnected = false;
    private boolean mustClose = false;
    // protocol information
    private static final String JSON_FORMAT = "json";
    private String mSessionId = "";

    /** Creates a new instance of SocketServices */
    public SocketServices(String aHost, int aPort) {
        mHost = aHost;
        mPort = aPort;
    }

    public synchronized void connect() {
        if (!isConnected()) {
            // create socket connection to server
            if (!createSocket()) {
                return;
            }
            // start writer thread to process requests out
            startWriterThread();
            // start reader thread to read responses in
            startReaderThread();
            // debug
            System.out.println("Service connected!");
        }
    }

    @SuppressWarnings("static-access")
	private boolean createSocket() {
        try {
            onConnect(NetworkStates.AEE_NET_CONNECTING);
            String url = "socket://" + mHost + ":" + mPort;
            mConnection = (SocketConnection) Connector.open(url);
            mConnection.setSocketOption(mConnection.SNDBUF, MAX_WRITE_BUFFER);
            mConnection.setSocketOption(mConnection.RCVBUF, MAX_READ_BUFFER);
            mConnection.setSocketOption(SocketConnection.LINGER, 5);
        } catch (Throwable t) {
            mConnected = false;
            mConnection = null;
            onConnect(NetworkStates.AEE_NET_CONNECTING_ERROR);
            return false;
        }

        try {
            if (mRequestStream != null) {
                mRequestStream.close();
                mRequestStream = mConnection.openDataOutputStream();
            } else {
                mRequestStream = mConnection.openDataOutputStream();
            }

            if (mResponseStream != null) {
                mResponseStream.close();
                mResponseStream = mConnection.openDataInputStream();
            } else {
                mResponseStream = mConnection.openDataInputStream();
            }
        } catch (Throwable t) {
            mConnected = false;
            mConnection = null;
            onConnect(NetworkStates.AEE_NET_CONNECTING_ERROR);
            return false;
        }

        mConnected = true;
        mustClose = false;
        mSessionId = "";
        //Context.getInstance().mUid = -1;
        mReqMsgs.removeAllElements();
        onConnect(NetworkStates.AEE_NET_CONNECTED);
        return true;
    }

    private void startWriterThread() {
        // writer
        WriterRequest writer = new WriterRequest();
        // start writer thread to process requests
        mWriterThread = new Thread(writer);
        mWriterThread.start();
        // debug
        System.out.println("Writer started!");
    }

    private void startReaderThread() {
        // reader
        ReaderResponse reader = new ReaderResponse();
        // start reader thread to process responses
        mReaderThread = new Thread(reader);
        mReaderThread.start();
        // debug
        System.out.println("Reader started!");
    }

    public void close() {
        mustClose = true;
    }

    private void closeThread() {
        if (!isConnected()) {
            return;
        }

        // mark closed
        mConnected = false;
        // close connection and running thread
        if (mConnection != null) {
            try {
                // kill writer thread
                mWriterThread.join(); // join to kill
                mWriterThread = null;
                // kill reader thread
                mReaderThread.join();
                mReaderThread = null;
                // close connection
                mConnection.close();
            } catch (Throwable t) {
                onConnect(NetworkStates.AEE_NET_ERROR);
            }
            mConnection = null;
        }
        // close resource streaming
        // out stream
        if (mRequestStream != null) {
            try {
                mRequestStream.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            mRequestStream = null;
        }
        // in stream
        if (mResponseStream != null) {
            try {
                mResponseStream.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            mResponseStream = null;
        }
        // notify closed
        onConnect(NetworkStates.AEE_NET_CLOSED);
    }

    public boolean isConnected() {
        return mConnected;
    }

    protected void onConnect(int aEvent) {
        switch (aEvent) {
            case NetworkStates.AEE_NET_CONNECTING:
                // No error.
                notifyEvent(NetworkEvents.EVT_CONNECTING);
                break;
            case NetworkStates.AEE_NET_CONNECTING_ERROR:
                notifyEvent(NetworkEvents.EVT_CONNECTING_ERROR);
                break;
            case NetworkStates.AEE_NET_CONNECTED:
                // No error.
                notifyEvent(NetworkEvents.EVT_CONNECTED);
                break;
            case NetworkStates.AEE_NET_CLOSED:
                // closed
                notifyEvent(NetworkEvents.EVT_CLOSED);
                break;
            case NetworkStates.AEE_NET_ERROR:
            default:
                // There was an error connecting.
                close();
                notifyEvent(NetworkEvents.EVT_ERROR);
                break;
        }
    }

    // hungcn
    public synchronized void addNetworkListener(INetworkListener aListener) {
        synchronized (mListeners) {
            mListeners.addElement(aListener);
        }
    }

    public synchronized void removeNetworkListener(INetworkListener aListener) {
        synchronized (mListeners) {
            mListeners.removeElement(aListener);
        }
    }

    private void notifyEvent(int aEvent) {
        synchronized (mListeners) {
            int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                INetworkListener listenter = (INetworkListener) mListeners.elementAt(i);
                listenter.onEvent(aEvent);
            }
        }
    }

    private synchronized void notifyResponse(final Vector aResponses) {
        synchronized (mListeners) {
            synchronized (aResponses) {
                int size = mListeners.size();
                for (int i = 0; i < size; i++) {
                    INetworkListener listenter = (INetworkListener) mListeners.elementAt(i);
                    listenter.onResponse(aResponses);
                }
                // remove all after notified
                aResponses.removeAllElements();
            }
        }
    }

    public void removeAllMessage() {
        synchronized (mReqMsgs) {
            mReqMsgs.removeAllElements();
        }
    }

    public void addRequestMessage(JSONObject aMsg) {
        if (aMsg == null) {
            return;
        }
        synchronized (mReqMsgs) {
            mReqMsgs.addElement(aMsg);
        }
    }

    class BlockData {

        final int INIT_SIZE = -1;
        int mBlockSize;
        DataOutput mBufferData;

        public BlockData() {
            mBlockSize = INIT_SIZE;
            mBufferData = new DataOutput();
        }

        public void setBlockSize(int aBlockSize) {
            mBlockSize = aBlockSize;
        }

        public int getBlockSize() {
            return mBlockSize;
        }

        public boolean isStarting() {
            return (mBlockSize < 0);
        }

        public void put(byte[] aPartialData) throws Throwable {
            mBufferData.write(aPartialData);
        }

        public void put(byte[] aPartialData, int aOff, int aFrag) throws Throwable {
            mBufferData.write(aPartialData, aOff, aFrag);
        }

        public void put(int aPartialData) throws Throwable {
            mBufferData.writeInt(aPartialData);
        }

        public void reset() {
            mBlockSize = INIT_SIZE;
            mBufferData.reset();
        }

        public boolean canDecode() throws Throwable {
            return (mBlockSize == mBufferData.size());
        }

        public byte[] getInternalData() throws Throwable {
            return mBufferData.getBytes();
        }

        public int size() throws Throwable {
            return mBufferData.size();
        }
    }

    /**
     * <code>WriterThread</code> is an thread to write requests from client to server
     */
    class WriterRequest implements Runnable {

        public void run() {
            while (isConnected()) {
                if (mustClose) {
                    closeThread();
                    mustClose = false;
                    return;
                }

                // wait to process the next request
                try {
                    Thread.sleep(100L);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                try {
                    // process requests
                    //processRequestsString();
                    processRequests();
                } catch (Throwable t) {
                    t.printStackTrace();
                    onConnect(NetworkStates.AEE_NET_ERROR);
                }
            }
        }

        void processRequestsString() throws Throwable {
            String bufferRequest = "<policy-file-request/>";
            byte[] outData = bufferRequest.getBytes();
            DataOutput bufferOut = new DataOutput();
            bufferOut.writeInt(outData.length);
            bufferOut.write(outData);
            // package data
            byte[] pkgData = bufferOut.getBytes();
            // send to server
            mRequestStream.write(pkgData);
            mRequestStream.flush();
        }

        void processRequests() throws Throwable {
            Vector tempMsgs = new Vector();
            synchronized (mReqMsgs) {
                Enumeration enumMsgs = mReqMsgs.elements();
                while (enumMsgs.hasMoreElements()) {
                    JSONObject reqMsg = (JSONObject) enumMsgs.nextElement();
                    tempMsgs.addElement(reqMsg);
                }
                mReqMsgs.removeAllElements();
            }
            if (!tempMsgs.isEmpty()) {
                // buffer out
                DataOutput bufferRequest = new DataOutput();
                // format
                bufferRequest.writeUTF(JSON_FORMAT);
                // requests package
                JSONArray pkgMsgs = new JSONArray();
                Enumeration enumMsgs = tempMsgs.elements();
                while (enumMsgs.hasMoreElements()) {
                    // get each request message
                    JSONObject reqMsg = (JSONObject) enumMsgs.nextElement();
                    // then put into package
                    pkgMsgs.put(reqMsg);
                }
                // request data
                JSONObject encodedData = new JSONObject();
                // System.out.println("mSessionId======================================: " + mSessionId);
                encodedData.put("sessionid", mSessionId);
                encodedData.put("requests", pkgMsgs);
                // serialize data
                String serializedData = encodedData.toString();
                // put into buffer

                bufferRequest.writeUTF(serializedData);
                // write to output stream
                byte[] outData = bufferRequest.getBytes();
                if (outData != null) {
                    // packaging request data
                    DataOutput bufferOut = new DataOutput();
                    bufferOut.writeInt(outData.length);
                    System.out.println("Header:" + outData.length);
                    System.out.println("Header:" + outData.toString());
                    bufferOut.write(outData);
                    System.out.println("Content:" + outData.toString());
                    // package data
                    byte[] pkgData = bufferOut.getBytes();
                    // send to server
                    String log = new String(pkgData);
                    System.out.println("Total:" + log);
                    mRequestStream.write(pkgData);
                    mRequestStream.flush();
                    System.out.println("[REQUEST] " + serializedData);
                }
            }
        }
    }

    /**
     * <code>ReaderResponse</code> is an thread to read responses from server
     */
    class ReaderResponse implements Runnable {

        public void run() {
            BlockData pkgBlock = new BlockData();

            while (isConnected()) {
                if (mustClose) {
                    closeThread();
                    mustClose = false;
                    return;
                }

                //delay a bit for network transmition
                try {
                    Thread.sleep(100L);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                try {
                    int pkgSize = -1;
                    if (pkgBlock.isStarting()) {
                        try {
                            //#ifdef AVAILABLE_WORKS
                            if (mResponseStream.available() >= 4) {
                                pkgSize = mResponseStream.readInt();
                            } else {
                                continue;
                            }
                            //#else
//#                             pkgSize = mResponseStream.readInt();
                            //#endif
                            pkgBlock.setBlockSize(pkgSize);
                        } catch (Throwable t) {
                            continue;
                        }
                    }
                    // get the current pkg size
                    pkgSize = pkgBlock.getBlockSize();

                    // decode this package
                    if (pkgSize > 0) {
                        int blkAvail = pkgBlock.size();
                        int needRead = pkgSize - blkAvail;
                        byte[] partialData = new byte[needRead];
                        int realRead = mResponseStream.read(partialData);
                        pkgBlock.put(partialData, 0, realRead);

                        if (pkgBlock.canDecode()) {
                            byte[] pkgData = pkgBlock.getInternalData();
                            pkgBlock.reset();
                            processResponses(pkgData);
                        } else {
                            continue;
                        }
                    } else {
                        pkgBlock.reset();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    onConnect(NetworkStates.AEE_NET_ERROR);
                }
            }
        }

        private void processResponses(byte[] aResponseData) throws Throwable {
            // buffer to parse response data
            DataInput bufferResponse = new DataInput(aResponseData);
            // format
            bufferResponse.readUTF();
            // package
            String serializedObj = bufferResponse.readUTF();
            System.out.println("[RESPONSE] " + serializedObj);
            // convert to json object
            JSONObject resPkg = new JSONObject(serializedObj);
            // session id
            mSessionId = resPkg.getString("sessionid");
            // response package
            Vector decodedMessages = new Vector();
            // response messages array
            JSONArray arrMsgs = resPkg.getJSONArray("responses");
            int size = arrMsgs.length();
            for (int i = 0; i < size; i++) {
                // encoded response message
                JSONObject resMsg = arrMsgs.getJSONObject(i);
                // put into the current package
                decodedMessages.addElement(resMsg);
            }
            // notify to listeners
            notifyResponse(decodedMessages);
        }
    }
    // end hungcn
}
