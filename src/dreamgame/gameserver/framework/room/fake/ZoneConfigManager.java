package dreamgame.gameserver.framework.room.fake;

//import com.migame.gameserver.framework.common.ILoggerFactory;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.gameserver.framework.session.ISessionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ZoneConfigManager
{
  private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ZoneConfigManager.class);
//  public HashMap<Integer, ZoneConfig_FakeRoom> zoneConfigs;
  public static ArrayList<ZoneConfig_FakeRoom> zoneConfigs;
  public static ArrayList<String> ownerNameList;
  public static ArrayList<Long> moneyList;
  public static ArrayList<Long> moneyListPoker;
  
  public static ConcurrentHashMap<Integer, ArrayList<Long>> fakeRoomMapByZone = new ConcurrentHashMap<Integer, ArrayList<Long>>();
  public static ConcurrentHashMap<Long, String> fakeRoomExistedOwnerNameMap = new ConcurrentHashMap<Long, String>();
  
  
//  public static 
  @SuppressWarnings("unchecked")
  public static void init() throws ServerException 
  {
//    this.zoneConfigs = new HashMap<Integer, ZoneConfig_FakeRoom>();
    zoneConfigs = new ArrayList<ZoneConfig_FakeRoom>();
    ownerNameList = new ArrayList<String>();
    
    moneyList = new ArrayList<Long>();
    moneyList.add(1000L);
    moneyList.add(3000L);
    moneyList.add(5000L);
    moneyList.add(10000L);
    moneyList.add(20000L);
    
    moneyListPoker = new ArrayList<Long>();
    moneyListPoker.add(100L);
    moneyListPoker.add(200L);
    moneyListPoker.add(300L);
    moneyListPoker.add(500L);
    moneyListPoker.add(1000L);
    
    initConfigs();
  }

  private static void initConfigs() throws ServerException {
      initZoneConfigs();
      initOwnerNameList();
      initOwnerNameListInDatabase();
  }
  
  private static void initZoneConfigs() throws ServerException {
    File fConfig;
    try
    {
      fConfig = new File("conf/_fake_rooms-config.xml");
      mLog.info("Init Zone Config Fake Room From file = conf/_fake_rooms-config.xml");

      if (!(fConfig.exists()))
      {
        throw new IOException("File " + fConfig.getName() + " is not exist.");
      }
      if (!(fConfig.canRead()))
      {
        throw new IOException("File " + fConfig.getName() + " must be readable.");
      }

      DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuildFac.newDocumentBuilder();
      Document aDoc = docBuilder.parse(fConfig);

      Element root = aDoc.getDocumentElement();

      NodeList zoneList = root.getElementsByTagName("config");
      int numZone = zoneList.getLength();
      int zoneIdx = 0;
      int zoneId, minNo, maxNo;
      String zoneName;
      while (true) { 
	if (zoneIdx >= numZone)
	    break;

        Element zone = (Element)zoneList.item(zoneIdx);
//        zoneId = Integer.parseInt(zone.getAttribute("zone_id"));
//        zoneName = zone.getAttribute("zone_name");
//        minNo = Integer.parseInt(zone.getAttribute("min_no"));
//        maxNo = Integer.parseInt(zone.getAttribute("max_no"));
	
        ZoneConfig_FakeRoom newZone = new ZoneConfig_FakeRoom();
	newZone.zoneId = Integer.parseInt(zone.getAttribute("zone_id"));
	newZone.zoneName = zone.getAttribute("zone_name");
	newZone.minNoOfFakeRooms = Integer.parseInt(zone.getAttribute("min_no"));
	newZone.maxNoOfFakeRooms = Integer.parseInt(zone.getAttribute("max_no"));
//        this.zoneConfigs.put(Integer.valueOf(zoneId), newZone);
	zoneConfigs.add(newZone);
        ++zoneIdx;
      }
    }
    catch (Throwable t)
    {
      throw new ServerException(t);
    }
  }
  
  private static void initOwnerNameList() throws ServerException {
    File fConfig;
    try
    {
      fConfig = new File("conf/_fake_user_name-list.xml");
      mLog.info("Init Fake owner name list From file = conf/_fake_user_name-list.xml");

      if (!(fConfig.exists()))
      {
        throw new IOException("File " + fConfig.getName() + " is not exist.");
      }
      if (!(fConfig.canRead()))
      {
        throw new IOException("File " + fConfig.getName() + " must be readable.");
      }

      DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuildFac.newDocumentBuilder();
      Document aDoc = docBuilder.parse(fConfig);

      Element root = aDoc.getDocumentElement();

      NodeList nameList = root.getElementsByTagName("user");
      int numNames = nameList.getLength();
      int idx = 0;
      while (true) { 
	if (idx >= numNames)
	    break;

        Element nameNode = (Element)nameList.item(idx);
	ownerNameList.add(nameNode.getAttribute("name"));
        ++idx;
      }
    }
    catch (Throwable t)
    {
      throw new ServerException(t);
    }
  }
  
  // init username list in db:
  private static void initOwnerNameListInDatabase() throws ServerException {
      ArrayList<String> lowercaseNameList = new ArrayList<String>(ownerNameList.size());
      for(String item : ownerNameList) 
	  lowercaseNameList.add(item.toLowerCase());
      try {
	ownerNameList = DatabaseDriver.initOwnerNameListInDatabase(lowercaseNameList);
      }
      catch (Throwable t) {
	  throw new ServerException(t);
      }
  }
  
    // random money for table
    public static long generateRandomMoneyForTable(int zoneID) {
	switch (zoneID) {
	    case ZoneID.POKER:
	    case ZoneID.XITO:
		return moneyListPoker.get(generateRandomNumber(0, moneyListPoker.size() - 1));
		
	    default:
		return moneyList.get(generateRandomNumber(0, moneyList.size() - 1));
	}
    }
  
    // get available owner name for assigning to fake table
    public static String generateAvailableOwnerName() {
	synchronized(fakeRoomExistedOwnerNameMap) {
	    String result = null;
	    int tempi, noOfNameInList = ownerNameList.size() - 1;
	    String tempS;
	    while (result == null) {
		tempi = generateRandomNumber(0, noOfNameInList);
		tempS = ownerNameList.get(tempi);
		if (fakeRoomExistedOwnerNameMap.containsValue(tempS)) {
		    continue;
		}
		result = tempS;
	    }
	    return result;
	}
    }
    
    // get list available owner name with specific size
    public static ArrayList<String> generateAvailableOwnerNameList(int noOfNames) {
	synchronized(fakeRoomExistedOwnerNameMap) {
	    int count = 0;
	    ArrayList<String> resultList = new ArrayList<String>();
	    String result = null, tempS;
	    int tempi, noOfNameInList = ownerNameList.size() - 1;
	    while (count < noOfNames) {
		result = null;
		while (result == null) {
		    tempi = generateRandomNumber(0, noOfNameInList);
		    tempS = ownerNameList.get(tempi);
		    if (fakeRoomExistedOwnerNameMap.containsValue(tempS)) {
			continue;
		    }
		    result = tempS;
		}
		++ count;
		resultList.add(result);
	    }
	    return resultList;
	}
    }
  
    // get list available owner name with specific size and given previous generated Name list
    public static ArrayList<String> generateAvailableOwnerNameList(int noOfNames, ArrayList<String> previousNameList) {
	synchronized(fakeRoomExistedOwnerNameMap) {
	    ArrayList<String> resultList = new ArrayList<String>();
	    String result = null, tempS;
	    int count = 0, tempi, noOfNameInList = ownerNameList.size() - 1;
	    boolean isKeep;
	    // xu ly dong name cu, zeo xuc xac xem co giu lai name do hay khong
	    if (previousNameList != null) {
		for (String name : previousNameList) {
		    tempi = generateRandomNumber(1, 4);
		    isKeep = (tempi % 2) == 0;
		    if (isKeep) {
			resultList.add(name);
			count++;
		    }
		    if (count == noOfNames) break;
		}
	    }
	    
	    // neu chua du thi lai tim tiep nao
	    while (count < noOfNames) {
		result = null;
		while (result == null) {
		    tempi = generateRandomNumber(0, noOfNameInList);
		    tempS = ownerNameList.get(tempi);
		    if (fakeRoomExistedOwnerNameMap.containsValue(tempS)) {
			continue;
		    }
		    result = tempS;
		}
		++ count;
		resultList.add(result);
	    }
	    return resultList;
	}
    }
    
    // generate random number
    public static int generateRandomNumber(int aStart, int aEnd) {
	if ( aStart > aEnd ) {
	    throw new IllegalArgumentException("Start cannot exceed End.");
	}
	return aStart + (int)(Math.random() * ((aEnd - aStart) + 1));
    }
  
    // calculate number of fake user / table in specific period of time
    public static int noOfFakeUserOrTablesBasedOnCurrentTime(int number) {
	int result = number;
	// 00h -> 09h: 1/3 so ban thoi
	SimpleDateFormat sdf = new SimpleDateFormat("H");
	int currentHour = Integer.parseInt(sdf.format(new Date()));
	if (currentHour < 9 && currentHour >= 0 ) {
	    result = result / 3;
	    if (result < 1)
		result = 1;
	}
	// 23 -> 00h hoac 09->10h: 2/3 so ban thoi
	else if (currentHour == 23 || currentHour == 9 ) {
	    result = result * 2 / 3;
	    if (result < 1)
		result = 1;
	}
	return result;
    }
  
}