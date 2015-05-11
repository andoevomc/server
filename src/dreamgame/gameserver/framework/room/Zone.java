package dreamgame.gameserver.framework.room;

import dreamgame.data.SimpleTable;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.session.ISession;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("unchecked")
public class Zone {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(Zone.class);

    public static final long UNSPECIFIED_ROOM_ID = -1L;
    public static final int JOIN_UNLIMITED = -1;
    private int mRoomCapacity = -1;
    private int mPlayerSize = -1;

    public final static int MAX_ROOM=20;
    private final ConcurrentHashMap<Long, Room> mRooms[]=new ConcurrentHashMap[MAX_ROOM];
    
    private final Vector<Long> mRoomIds[] = new Vector[MAX_ROOM];

    private ZoneManager.IdRoomGenerator mIdGenerator;
    private int mZoneId;
    private String mZoneName;
    private int mJoinLimited;

    public Zone() {

        for (int i=0;i<MAX_ROOM;i++)
        {
            this.mRooms[i] = new ConcurrentHashMap();
            this.mRoomIds[i] = new Vector();
        }
        
    }

    void setIdRoomGenerator(ZoneManager.IdRoomGenerator aIdGenerator) {
        this.mIdGenerator = aIdGenerator;
    }

    public long getNumRoom()
    {
        
        long num=0;
        synchronized (this.mRooms) {
            for (int i = 0; i < MAX_ROOM; i++)             
            {                                
                int numRooms = this.mRooms[i].size();
                num=num+numRooms;
            }}
        return num;            
    }
   
    public void setRoomCapacity(int aRoomCapacity) {
        this.mRoomCapacity = aRoomCapacity;
    }

    public void setPlayerSize(int aPlayerSize) {
        this.mPlayerSize = aPlayerSize;
    }

    public void setZoneId(int aZoneId) {
        this.mZoneId = aZoneId;
    }

    public int getZoneId() {
        return this.mZoneId;
    }

    void setZoneName(String aZoneName) {
        this.mZoneName = aZoneName;
    }

    public String getZoneName() {
        return this.mZoneName;
    }

    void setJoinLimited(int aJoinLimited) {
        this.mJoinLimited = aJoinLimited;
    }

    public int getJoinLimited() {
        return this.mJoinLimited;
    }

    public Room findRoom(long aRoomId) {
        synchronized (this.mRooms) {
            for (int i=0;i<MAX_ROOM;i++)
            {
                if (mRooms[i].containsKey(Long.valueOf(aRoomId)))
                    return ((Room) this.mRooms[i].get(Long.valueOf(aRoomId)));
                
            }
            return null;
            //return ((Room) this.mRooms.get(Long.valueOf(aRoomId)));
        }
    }

    public Room createRoom(String des, long id,int channel) {
        synchronized (this.mRooms) {
            Room newRoom = new Room(this.mRoomCapacity, this.mPlayerSize, this);
            try {
                long roomId = DatabaseDriver.logMatch(
                        id, System.currentTimeMillis(), 0, "" + des);
                System.out.println("roomId : "+roomId);
                
                if (roomId==0)
                {
                    System.out.println("Error! roomId is zero!");
                    roomId = DatabaseDriver.newlogMatch(
                        id, System.currentTimeMillis(), 0, "" + des);
                    System.out.println("new roomId : "+roomId);
                }
                
                newRoom.setRoomId(roomId);
                
                
                addRoom(roomId, newRoom,channel);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            return newRoom;
        }
    }

    void addRoom(long aRoomId, Room aRoom,int channel) {
        synchronized (this.mRooms) {
            deleteRoom(aRoom);
            this.mRooms[channel].put(Long.valueOf(aRoomId), aRoom);
            
            this.mRoomIds[channel].add(Long.valueOf(aRoomId));
        }
    }

    public void deleteRoom(Room aRoom) {
        if ((aRoom != null) && (!(aRoom.isPermanent()))) {
            synchronized (this.mRooms) {
                if (aRoom.getAttactmentData()!=null)
                    aRoom.getAttactmentData().destroy();

                long roomId = aRoom.getRoomId();

                for (int i=0;i<MAX_ROOM;i++)
                if (mRooms[i].containsKey(Long.valueOf(roomId)))    
                {
                    this.mRooms[i].remove(Long.valueOf(roomId));
                    this.mRoomIds[i].remove(new Long(roomId));
                    break;
                }
                
            }
        }
    }

    public void deleteFakeRoom(long roomId) {
	synchronized (this.mRooms) {
	    this.mRooms[1].remove(Long.valueOf(roomId));
	    this.mRoomIds[1].remove(new Long(roomId));
	}
    }
    
    public void addFakeRoom(Room aRoom) {
	synchronized (this.mRooms) {
	    this.mRooms[1].put(Long.valueOf(aRoom.getRoomId()), aRoom);
            this.mRoomIds[1].add(Long.valueOf(aRoom.getRoomId()));
	}
    }
    
    public void changeFakeRoomPlayingStatus(long roomId, boolean isPlaying) {
	synchronized (this.mRooms) {
	    Room r = this.mRooms[1].get(Long.valueOf(roomId));
	    r.setPlaying(isPlaying);
	}
    }
    
    public Room createFakeRoom(int zoneId, long roomId, String roomName, String ownerName, int numberOfPlayers, long money, boolean isPlaying, int position) {
	Room newRoom = new Room(this.mRoomCapacity, this.mPlayerSize, this);
	newRoom.isFakeRoom = true;
	newRoom.setZoneID(zoneId);
	newRoom.setRoomId(roomId);
	newRoom.setName(roomName);
	newRoom.setPlayerSize(numberOfPlayers);
	newRoom.setOwnerName(ownerName);
	newRoom.roomPosition = position;
	
	TienLenPlayer owner = new TienLenPlayer();
	owner.username = ownerName;
	owner.avatarID = 1;
	owner.level = 12;
	
	SimpleTable newTable = new SimpleTable();
	newTable.startTime = System.currentTimeMillis();
	newTable.owner = owner;
	newTable.firstCashBet = money;
	newTable.matchID = roomId;
	newTable.maximumPlayer = numberOfPlayers;
	newTable.isPlaying = isPlaying;
	newTable.name = roomName;
	
	newRoom.setAttachmentData(newTable);
//	return new Room(this.mRoomCapacity, this.mPlayerSize, this);
	return newRoom;
    }
    
    public int getTotalRoom() {
        int sum=0;
        for (int i=0;i<MAX_ROOM;i++)
            sum=sum+mRooms[i].size();
        return sum;
    }

    public int getTotalRoom(int channel) {
        return mRooms[channel].size();
    }

    /*public int getNumPlaylingRoom() {
        int count = 0;

        Enumeration eRooms = null;
        synchronized (this.mRooms) {
            eRooms = this.mRooms.elements();
        }

        while ((eRooms != null)
                && (eRooms.hasMoreElements())) {
            Room room = (Room) eRooms.nextElement();
            long roomStatus = room.getStatus();
            if (roomStatus == 1L) {
                ++count;
            }

        }

        return count;
    }*/

    /*public Vector<RoomEntity> dumpPlayingRooms(int aOffset, int aLength, int zoneID) {
        Vector roomEntities = new Vector();

        

        synchronized (this.mRooms) {
            int loopIdx = (aOffset >= 0) ? aOffset : 0;

            int numRooms = this.mRooms.size();

            int results = 0;
            while (true) {
                if ((loopIdx >= numRooms) || (results >= aLength)) {
                    break;
                }

                long roomId = ((Long) this.mRoomIds.get(loopIdx)).longValue();

                Room room = findRoom(roomId);

                if ((room.getStatus() == 1L) && (room.getZoneID() == zoneID)) {
                    roomEntities.add(room.dumpRoom());

                    ++results;
                }

                ++loopIdx;
            }
        }

        return roomEntities;
    }*/

    /*public int getNumWaitingRoom() {
        int count = 0;

        Enumeration eRooms = null;
        synchronized (this.mRooms) {
            eRooms = this.mRooms.elements();
        }

        while ((eRooms != null)
                && (eRooms.hasMoreElements())) {
            Room room = (Room) eRooms.nextElement();
            long roomStatus = room.getStatus();
            if (roomStatus == 2L) {
                ++count;
            }

        }

        return count;
    }*/

    
    public Vector<RoomEntity> dumpWaiting() {
        return dumpWaiting(0);
    }        
    
    // get list of waiting rooms
    public Vector<RoomEntity> dumpWaiting(int channel) {
        
        Vector roomEntities = new Vector();
        int aOffset = 0;
        int aLength = 10000;
        
        synchronized (this.mRooms) {
            for (int i = 0; i < MAX_ROOM; i++) 
            if (channel==0 || i==channel)
            {
                
                int loopIdx = (aOffset >= 0) ? aOffset : 0;

                int numRooms = this.mRooms[i].size();

                int results = 0;
                while (true) {
                    if ((loopIdx >= numRooms) || (results >= aLength)) {
                        break;
                    }

                    long roomId = ((Long) this.mRoomIds[i].get(loopIdx)).longValue();

                    Room room = findRoom(roomId);

                    if (!room.getAttactmentData().isPlaying && room.playingSize() < room.mCapacity && room.getNumOnline() > 0) {
                        roomEntities.add(room.dumpRoom());
                        ++results;
                    }

                    ++loopIdx;
                }
            }
        }

        return roomEntities;
    }

    public RoomEntity findRoomByPosition(int channel, int position) {
        synchronized (this.mRooms) {
//	    Iterator roomIter = this.mRooms[channel].entrySet().iterator();
	    Enumeration<Room> roomEnum = this.mRooms[channel].elements();
	    while (roomEnum.hasMoreElements()) {
		Room r = roomEnum.nextElement();
		if (r.roomPosition == position) {
		    return r.dumpRoom();
		}
	    }
        }
	return null;
    }

    public int findAvailablePositionForRoom(int channel) {
	synchronized (this.mRooms) {
            for (int i = 1; i <= DatabaseDriver.maxRoom; i++) {
		Enumeration<Room> roomEnum = this.mRooms[channel].elements();
		boolean isExisted = false;
		while (roomEnum.hasMoreElements()) {
		    if (roomEnum.nextElement().roomPosition == i) {
			isExisted = true;
			break;
		    }
		}
		if ( ! isExisted) {
		    return i;
		}
	    }
	    return 0;
	}
    }
    
    public ArrayList<Integer> findAvailablePositionForFakeRoom() {
	synchronized (this.mRooms) {
	    Enumeration<Room> roomEnum = this.mRooms[1].elements();
	    ArrayList<Integer> result = new ArrayList<Integer>();
            for (int i = 1; i <= DatabaseDriver.maxRoom; i++) {
		boolean isExisted = false;
		while (roomEnum.hasMoreElements()) {
		    if (roomEnum.nextElement().roomPosition == i) {
			isExisted = true;
			break;
		    }
		}
		if ( ! isExisted) {
		    result.add(new Integer(i));
		    continue;
		}
	    }
	    return result;
	}
    }
    
    
    public void create10room(ISession aSession, int zoneID)
    {
        if (mRooms[1].size()==0)
        {
            //System.out.println("Came here");
            for (int i=0;i<50;i++)
            {
                Room newRoom = createRoom("test"+i, 1,1);
                newRoom.setZoneID(zoneID);
                newRoom.setName("test_"+i);
                newRoom.setPlayerSize(4);
                newRoom.setOwnerName("hatuan");
                newRoom.join(aSession);


                TienLenTable newTable = new TienLenTable(new TienLenPlayer(), 5000, newRoom.getRoomId(), 4);
                newTable.setOwnerSession(aSession);
                newRoom.setAttachmentData(newTable);
            }
        }
    }


    public Vector<RoomEntity> dumpWaitingRooms(int aOffset, int aLength,
            int aLevel, int minLevel, int zoneID,int channelId) {
        
        Vector roomEntities = new Vector();

        System.out.println("channelId : " +channelId);
        
        int fail=0;


        synchronized (this.mRooms) 
        {
            //int loopIdx = (aOffset >= 0) ? aOffset : 0;
            for (int i = 0; i < MAX_ROOM; i++)
            if (channelId==0 || channelId==i)
            {
                int loopIdx = 0;
                int numRooms = this.mRooms[i].size();

                //System.out.println("Dump waiting : " + numRooms);
                int results = 0;
                while (true) {
                    if ((loopIdx >= this.mRooms[i].size()) || (results >= aLength)) {
                        break;
                    }

                    long roomId = ((Long) this.mRoomIds[i].get(loopIdx)).longValue();

                    Room room = findRoom(roomId);

                    //System.out.println("Room : "+room.getName()+" ; "+room.isPlaying()+" : "+room.getLevel() +" ; "+aLevel+" ; "+minLevel+" ;; numOnline : "+room.getNumOnline());

                    if (room.channel == 0) {
                        room.channel = 1;
                    }

                    if (room.getNumOnline() <= 0 && (! room.isFakeRoom)) {
                        try {

                            mLog.error("Room : " + room.getName() + " is Empty. ; roomID : " + room.getRoomId());
                            room.allNewLeft();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } 
		    else if (
//			((room.getNumOnline() > 0) || (room.isFakeRoom))
//			&& (room.getLevel() <= aLevel) 
//			    && (room.getNumOnline() > 0)
                            (room.getLevel() >= minLevel) 
			    && (room.getZoneID() == zoneID)
                            && (channelId == 0 || channelId == room.channel)) {
                        if (fail >= aOffset) {
                            roomEntities.add(room.dumpRoom());
                            ++results;
                        }
                        fail++;
                    }

                    ++loopIdx;
                }
            }
        }

        return roomEntities;
    }
    //Binhlt
    

    /*public boolean deleteAllRoomByOwner(String owner) {
        synchronized (this.mRooms) {
            Enumeration<Long> keys = this.mRooms.keys();
            while (keys.hasMoreElements()) {
                long k = keys.nextElement();
                Room room = this.mRooms.get(Long.valueOf(k));
                if (owner.toLowerCase().equalsIgnoreCase(room.getOwnerName().toLowerCase())) {
                    room.allLeft();
                }
            }
        }
        return true;
    }*/

    public Room findRoomByOwner(String owner) {
        synchronized (this.mRooms) {
            for (int i = 0; i < MAX_ROOM; i++) {
                Enumeration<Long> keys = this.mRooms[i].keys();
                while (keys.hasMoreElements()) {
                    long k = keys.nextElement();
                    Room room = this.mRooms[i].get(Long.valueOf(k));
                    if (owner.toLowerCase().indexOf(room.getOwnerName().toLowerCase()) >= 0) {
                        return room;
                    }
                }
            }
        }
        return null;
    }
    public Room findRoomPlayer(String name) {
        synchronized (this.mRooms) {
            for (int i = 0; i < MAX_ROOM; i++) {
                Enumeration<Long> keys = this.mRooms[i].keys();
                while (keys.hasMoreElements()) {
                    long k = keys.nextElement();
                    Room room = this.mRooms[i].get(Long.valueOf(k));
                    if (name.toLowerCase().indexOf(room.getPlayer(name).toLowerCase()) >= 0) {
                        return room;
                    }
                }
            }
        }
        return null;
    }
}
