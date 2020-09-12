/*
 * Created on 21.Mar.2005
 *
 */
package iwb.cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;






public class FrameworkSetting {
	public static  String projectId = null;
	public static int transpile=0; //0:none, 1:nashorn, 2:nodejs
	public static Map<String, String> argMap = new HashMap();
	public static  boolean monaco = true;
	public	static	int	rdbmsTip= 0; //0:PostgreSQL, 1: SQL Server, 2: Oracle
	public	static	boolean	tsdbFlag= true; //Time SeriesDB
	public	static	int	systemStatus = 2; //0:working, 1:backup, 2:suspended
	public static final Map<String, Integer> projectSystemStatus = new HashMap<String, Integer>();//
	public final static Locale appLocale = new Locale("en");
	public final static String instanceUuid=UUID.randomUUID().toString();
	
	public final static String devUuid="067e6162-3b6f-4ae2-a221-2470b63dff00";
	public final static String rbacUuid="4147a129-06ad-4983-9b1c-8e88826454ac";
	public final static String fileUuid="59b0b784-6dbb-48bb-8e61-0948dee1817f";
	public final static String commentUuid="4094b109-bcc8-470a-b794-93a4b6b932b5";
	public final static String workflowUuid="10c3db7e-1045-4359-854b-a939fc9ae872";
	public final static String emailUuid="04fc4100-d464-41b8-bb78-e3672e761e3d";
	
	
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
//	public static String mailSchema="promis_mail";
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
	
		
	public static boolean showOnlineStatus = true;
	
	public static boolean redisCache = false;
	public static String redisHost = "localhost";//"35.226.30.186"; //
	
	public static boolean reactLabelRequired = true;
	public static boolean metadata = false;
	final public static String defaultPasswordMask = "**********";
	public static boolean logVcs = true;
	final public static int customFileTableId = 6973;
	final public static int customCommentTableId = 6975;
	final public static int customWorkflowTableId = 6976;
	final public static int customEmailTableId = 6977;

}
