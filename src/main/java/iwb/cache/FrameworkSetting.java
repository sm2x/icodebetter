/*
 * Created on 21.Mar.2005
 *
 */
package iwb.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;






public class FrameworkSetting {
	public static  String projectId = null;
	public static Map<String, String> argMap = new HashMap();
	public static  boolean monaco = false;
	public	static	int	rdbmsTip= 0; //0:PostgreSQL, 1: SQL Server, 2: Oracle
	public	static	boolean	tsdbFlag= true; //Time SeriesDB
	public	static	int	systemStatus = 2; //0:working, 1:backup, 2:suspended
	public static final Map<String, Integer> projectSystemStatus = new HashMap<String, Integer>();//
	public final static Locale appLocale = new Locale("en");
	public final static String instanceUuid=UUID.randomUUID().toString();
	public final static String devUuid="067e6162-3b6f-4ae2-a221-2470b63dff00";
	public static boolean mq=true;
	public static boolean cloud=false;
	public static boolean debug=true;
	public static boolean chat=true;
	public static boolean sms=true;
	public static boolean mail=true;
	public static int functionTimeout=5000;
	public static boolean externalDb=true;
	
//	public static boolean profilePicture=true;
//	public static boolean allowMultiLogin=true;
	public static boolean vcs=true;
	public static boolean vcsServer=false;
	public static boolean vcsServerClient=false;
	public static int rhinoInstructionCount = 10000;
//	public static boolean commentSummary=true;
	public static boolean alarm=true;
	public static boolean workflow=true;
//	public static boolean cacheObject = false;
	public static boolean hibernateCloseAfterWork=false;
	public static Set<Integer> dealerTableIds=new HashSet<Integer>();
	public static Set<Integer> dealerDetailTableIds=new HashSet<Integer>();
	public static int logMemoryAction=0;
	final public static String[] operatorMap = new String[]{"=","!=","<",">","<=",">="," like "," not like "," in ", " not in "," (custom) "," like "," not like "," like "};
	final public static String[] typeMap = new String[]{"-Yok-","varchar","date","double","int","int"};
	final public static String[] labelMap = new String[]{"information","warning","error"};
	final public static String[] sortMap = new String[]{"","","","float","int","bool","auto"};
	final public static String[] alignMap = new String[]{"center","left","right"};
	final public static String[] filterMap = new String[]{"", "string", "date", "numeric", "numeric", "boolean"};
	public static int mailIntervalCount=100;	
	public static boolean feed = false;
	public static int feedMaxDepth=1000;
	public static int feedLoadAtStartupDepth = 30; //0 if not wanted 
	public static int optimizerCount=0;
	public static boolean advancedSelectShowEmptyText=true;
	public static boolean simpleSelectShowEmptyText=true;
	public static String mailSchema="promis_mail";
	public static String crudLogSchema="promis_log";
	public static String crudLogTablePrefix="";
	public static String revisionLogTablePrefix="rev_";
	final public static String[] postQueryGridImgMap = new String[]{"checked.png","record_security.png","paperclip-16.png","comments-16.png","keyword.png","approval-16.png","mail.gif","picture.png","revision.png",".ivcs-icon"};
	final public static String[] postQueryGridImgMap4Webix = new String[]{"check","key","cloud-download","comment-o","key","puzzle-piece","mail-forward","file-picture-o","history","git"};
	public static final String bulkOperatorPostfix = "_opr_Zz_qw_";
	public static final boolean logGeoPos = true;

//	final public static Map<String, OnlineUserBean> lastUserAction= new HashMap<String, OnlineUserBean>();

//	public static int preloadWEngine = 0;
	public static int cacheStatus = 0;
//	public static String formBodyColor = null;
	public static String formCellColor = null;
	public static int cacheTimeoutRecord = 3600;
	
	public static boolean recursiveSecurity=true;
	public	static	int	asyncTimeout = 110; // in seconds
	public	static	int	onlineUsersAwayMinute = 3;
	public	static	int	onlineUsersLimitMinute = 10;
	public static int cacheAge = 60*60*24;
	public static int tableChildrenMaxRecordNumber = 50;
	public static boolean mailPassEncrypt=true;
	
//BMP icin specific	
	/*public static boolean vehicleTracking=true;
	public static int vehicleRefreshInterval=2;
	public static int vehicleIntervalCount=100;*/
//	public static boolean checkLicenseFlag = false;	

	public static String mailSeperator="<br/>---------<br/>";

	public static boolean mobilePush = true;	
	public static boolean mobilePushProduction = false;
	public static boolean mobilePushSound = true;
	public static boolean chatShowAllUsers = true;
	public static long onlineUsersLimitMobileMinute = 1000*60*(60+1);
	public static boolean validateLookups = true;
	public static boolean liveSyncRecord = true;
	public static boolean liveSyncRecord4WS = true;
	public static long asyncToleranceTimeout = 1000*(3*60+10);
	public static int liveSyncMaxMessage4Tpl = 10;
	public static boolean lookupEditFormFlag = true;
	public static boolean replaceSqlSelectX = true;
	public static boolean projectAccessControl = true;
	public static boolean log2tsdb = false;

	public static int logType = 0;

	public static String log2tsdbUrl = "http://localhost:8086";
	public static String log2tsdbDbName = "icb_log";
	public static String log2tsdbDbName4Crud = "icb_crud_log";
	public static String log2tsdbDbName4Vcs = "icb_vcs_log";
	public static boolean logQueryAction = true;
	public static int logQueryActionMinTime = 1;
	public static boolean logScriptAction = true;

	
	public static String log2mqUrl = "localhost";
	public static String log2mqQueue = "icodebetter-tsdb-exchange";


	public static boolean localTimer = true;
	
	/**
     * This is your auth0 domain (tenant you have created when registering with auth0 - account name)
     */
    //@Value(value = "${com.auth0.domain}")
	public static  String domain = "iwb.auth0.com";

    /**
     * This is the client id of your auth0 application (see Settings page on auth0 dashboard)
     */
    //@Value(value = "${com.auth0.clientId}")
	public static  String clientId = "eucJR6BM7WzoG336tT5MYCew6YpxJMyv";

    /**
     * This is the client secret of your auth0 application (see Settings page on auth0 dashboard)
     */
    //@Value(value = "${com.auth0.clientSecret}")
	public static  String clientSecret = "U5qMXxnQ9jr2Hm7AGg7COwikaWGcOhl28fLpVfUEqe6_bVjJ1A2FnWCbmjNN8BzR";
	public static boolean showOnlineStatus = true;
	
	public static boolean redisCache = false;
	public static String redisHost = "localhost";//"35.226.30.186"; //
	
	public static boolean reactLabelRequired = true;

}
