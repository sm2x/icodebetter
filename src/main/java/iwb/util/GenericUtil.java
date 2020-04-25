/*
 * Created on 21.Mar.2005
 *
 */
package iwb.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tomcat.jni.Address;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.net.InternetDomainName;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.W5Base;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5Param;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5TableParam;
import iwb.domain.db.W5WsMethodParam;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.engine.GlobalScriptEngine;
import iwb.exception.IWBException;

// import iwb.dao.RdbmsDao;

public class GenericUtil {

	public static final String dtCh = "/";
	public static final String dateFormat = "dd" + dtCh + "MM" + dtCh + "yyyy";
	public static final String[] dateFormatMulti = new String[] {"dd" + dtCh + "MM" + dtCh + "yyyy", "MM" + dtCh + "dd" + dtCh + "yyyy", "yyyy"  + dtCh + "MM" + dtCh + "dd" };
	private static final String strIndex = "0123456789+-" + dtCh;


	public static String orderStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static int orderLen = orderStr.length();

	private static long nextThreadId = 1000;

	public static synchronized long getNextThreadId() {
		return nextThreadId++;
	}

	private static long nextTmpIdId = System.currentTimeMillis() % 1000000;

	public static synchronized long getNextTmpId() {
		return nextTmpIdId++;
	}

	public static String getTreeOrderStrFromInt(int num, int prc) { // number,
																	// precision
		if (num < 0)
			return null;
		String result = "";
		while (num > 0) {
			result = orderStr.charAt(num % orderLen) + result;
			num = num / orderLen;
		}
		while (result.length() < prc)
			result = orderStr.charAt(0) + result;
		return result;
	}

	public static int getTreeOrderIntFromStr(String str) { // number, precision
		if (str == null || str.length() == 0)
			return 0;
		int result = 0, i = str.length() - 1;
		while (i >= 0) {
			result = orderLen * result + orderStr.indexOf(str.charAt(i));
			i--;
		}
		return result;
	}

	public static int uInt(JSONObject jo, String x) {
		try {
			return jo.getInt(x);
		} catch (Exception e) {
			return 0;
		}
	}

	public static String uStr(JSONObject jo, String x) {
		try {
			return jo.getString(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer uInteger(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		if (x.endsWith(".0"))
			x = x.substring(0, x.length() - 2);
		try {
			return Integer.valueOf(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static Long uLong(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return Long.valueOf(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static Double uDouble(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return Double.valueOf(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static double udouble(String x) {
		if (x == null || x.trim().length() == 0)
			return 0;
		try {
			return Double.parseDouble(x);
		} catch (Exception e) {
			return 0;
		}
	}

	public static BigDecimal uBigDecimal2(Object x) {
		if (x == null || x.toString().trim().length() == 0)
			return null;
		if (x instanceof String)
			return uBigDecimal((String) x);
		if (x instanceof Double)
			return new BigDecimal((Double) x);
		if (x instanceof Integer)
			return new BigDecimal((Integer) x);
		if (x instanceof Short)
			return new BigDecimal((Short) x);
		return uBigDecimal(x.toString());
	}

	public static BigDecimal uBigDecimal(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new BigDecimal(x);
		} catch (Exception e) {
			if (x.indexOf(',') == -1)
				return null;
			x = x.trim();
			while (x.indexOf('.') != -1)
				x = x.substring(0, x.indexOf('.')) + x.substring(x.indexOf('.') + 1);
			x = x.replace(',', '.');
			try {
				return new BigDecimal(x);
			} catch (Exception z) {
				return null;
			}
		}
	}

	public static Short uShort(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return Short.valueOf(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static Short uCheckBox(String x) {
		if (x == null || (x.compareTo("on") != 0 && x.compareTo("true") != 0 && x.compareTo("1") != 0))
			return (short) 0;
		else
			return (short) 1;
	}

	public static Date uDateEski(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new SimpleDateFormat(dateFormat).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(x);
		} catch (Exception e) {
		}
		return null;
	}

	public static Date uDate(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new SimpleDateFormat(dateFormat.concat(" HH:mm:ss")).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat(dateFormat.concat(" HH:mm")).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat(dateFormat).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(x);
		} catch (Exception e) {
		}
		return null;
	}


	public static Date uDate(String x, int dateFormat) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat].concat(" HH:mm:ss")).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat].concat(" HH:mm")).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat]).parse(x);
		} catch (Exception e) {
		}
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(x);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Date uDateTm(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new SimpleDateFormat(dateFormat.concat(" HH:mm")).parse(x);
		} catch (Exception e) {
			return null;
		}
	}
	

	public static Date uDateTm(String x, int dateFormat) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat].concat(" HH:mm")).parse(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static Calendar uCalendar(String x) {
		Calendar cal = Calendar.getInstance();
		Date d = uDate(x);
		if (d != null) {
			cal.setTime(uDate(x));
			return cal;
		} else
			return null;
	}

	public static String lPad(String s, int n, char c) {
		String r = (s == null) ? "" : "" + s;
		if (n <= r.length())
			return s.substring(r.length() - n, r.length());
		else
			for (int i = r.length(); i < n; i++)
				r = c + "" + r;
		return r;
	}

	public static String uDateStr(String e) {
		if (e == null)
			return "";
		String ev = e.trim();
		int evl = ev.length();
		if (evl == 0)
			return "";
		if (evl > 10) {
			ev = ev.substring(0, 10);
			evl = 10;
		}
		for (int i = 0; i < evl; i++)
			if (strIndex.indexOf(ev.substring(i, i + 1)) == -1) {
				return "HATA-1";
			}

		Calendar cal = Calendar.getInstance();
		if (evl > 2) {
			String tmp = ev.substring(0, 2);
			if (tmp.compareTo("++") == 0 && uInteger(ev.substring(2, evl)) != null) {
				cal.add(Calendar.MONTH, new Integer(ev.substring(2, evl)));
				ev = "0";
			}
			if (tmp.compareTo("--") == 0 && uInteger(ev.substring(2, evl)) != null) {
				cal.add(Calendar.MONTH, new Integer("-" + ev.substring(2, evl)));
				ev = "0";
			}
		}

		if (ev.compareTo("0") != 0) {
			String tmp = ev.substring(0, 1);
			if (tmp.compareTo("+") == 0 && uInteger(ev.substring(1, evl)) != null) {
				cal.add(Calendar.DATE, new Integer(ev.substring(1, evl)));
				ev = "0";
			}
			if (tmp.compareTo("-") == 0 && uInteger(ev.substring(1, evl)) != null) {
				cal.add(Calendar.DATE, new Integer("-" + ev.substring(1, evl)));
				ev = "0";
			}
		}

		String curDay = new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (cal.get(Calendar.DAY_OF_MONTH) < 10)
			curDay = "0" + curDay;
		String curMonth = new Integer(cal.get(Calendar.MONTH) + 1).toString();
		if (cal.get(Calendar.MONTH) < 9)
			curMonth = "0" + curMonth;
		String curYear = new Integer(cal.get(Calendar.YEAR)).toString();

		if (ev.compareTo("0") == 0) {
			return curDay + dtCh + curMonth + dtCh + curYear;
		}
		if (evl > 2 && ev.indexOf(dtCh) == -1) {
			ev = ev.substring(0, 2) + dtCh + ev.substring(2, evl);
			evl++;
			if (evl > 5 && ev.substring(5, evl).indexOf(dtCh) == -1) {
				ev = ev.substring(0, 5) + dtCh + ev.substring(5, evl);
			}
			e = ev;
			evl = ev.length();
		}

		if (ev.indexOf(dtCh) == -1) {
			ev += dtCh;
			evl++;
		}
		if (ev.indexOf(dtCh) == 1) {
			ev = '0' + ev;
			evl++;
		}
		if (evl == 10) {
			return (uDate(ev) == null) ? "HATA-2" : ev;
		}

		if (evl < 4) {
			ev = ev.substring(0, 2);
			e = ev + dtCh + curMonth + dtCh + curYear;
			return (uDate(e) == null) ? "HATA-3" : e;
		} else if (evl < 7) {
			evl = ev.substring(3, evl).indexOf(dtCh);
			if (evl == -1)
				evl = 2;
			if (evl + 3 > ev.length())
				evl = ev.length() - 3;
			e = ev.substring(0, 2) + dtCh + lPad(ev.substring(3, evl + 3), 2, '0') + dtCh + curYear;
			return (uDate(e) == null) ? "HATA-4" : e;
		} else {
			evl = ev.substring(3, evl).indexOf(dtCh);
			if (evl == -1)
				evl = 2;
			String x = ev.substring(4 + evl, ev.length());
			if (x.length() < 4)
				x = '2' + lPad(x, 3, '0');
			e = ev.substring(0, 2) + dtCh + lPad(ev.substring(3, evl + 3), 2, '0') + dtCh + x;
			return (uDate(e) == null) ? "HATA-5" : e;
		}
	}

	public static Timestamp uTimestamp(XMLGregorianCalendar x) {
		try {
			Timestamp t = new Timestamp(x.toGregorianCalendar().getTimeInMillis());
			return t;
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer uIntegerNullIfZero(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			Integer i = Integer.valueOf(x);
			if (i != null && i.intValue() == 0)
				return null;
			else
				return i;
		} catch (Exception e) {
			return null;
		}
	}

	public static Short uShortNullIfZero(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			Short s = Short.valueOf(x);
			if (s != null && s.shortValue() == 0)
				return null;
			else
				return s;
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer uIntegerZeroIfNull(String x) {
		if (x == null || x.trim().length() == 0)
			return new Integer(0);
		try {
			Integer i = Integer.valueOf(x);
			if (i == null)
				return new Integer(0);
			else
				return i;
		} catch (Exception e) {
			return new Integer(0);
		}
	}

	public static Short uShortZeroIfNull(String x) {
		if (x == null || x.trim().length() == 0)
			return new Short((short) 0);
		try {
			Short s = Short.valueOf(x);
			if (s == null)
				return new Short((short) 0);
			else
				return s;
		} catch (Exception e) {
			return new Short((short) 0);
		}
	}

	public static int uInt(String x) {
		if (x == null || x.trim().length() == 0)
			return 0;
		if (x.endsWith(".0"))
			x = x.substring(0, x.length() - 2);
		try {
			return Integer.parseInt(x);
		} catch (Exception e) {
			return 0;
		}
	}

	public static int uInt(Object x) {
		if (x == null)
			return 0;
		if (x instanceof Integer)
			return (Integer) x;
		if (x instanceof BigDecimal)
			return ((BigDecimal) x).intValue();
		if (x instanceof BigInteger)
			return ((BigInteger) x).intValue();
		try {
			String s = x.toString();
			if (s.endsWith(".0"))
				s = s.substring(0, s.length() - 2);
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	public static long uLong(Object x) {
		if (x == null)
			return 0;
		if (x instanceof Long)
			return (Long) x;
		try {
			String s = x.toString();
			if (s.endsWith(".0"))
				s = s.substring(0, s.length() - 2);
			return Long.parseLong(s);
		} catch (Exception e) {
			return 0;
		}
	}

	public static int uInt(HttpServletRequest request, String x) {
		return uInt(request.getParameter(x));
	}

	public static Float uFloat(String x) {
		if (x == null || x.trim().length() == 0)
			return null;
		try {
			return Float.valueOf(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uStrNvl(String x, String y) {
		if (x == null || x.length() == 0)
			return y;
		return x;
	}

	public static String uStrNvl(HttpServletRequest request, String x, String y) {
		x = request.getParameter(x);
		if (x == null || x.length() == 0)
			return y;
		return x;
	}

	public static int uIntNvl(HttpServletRequest request, String x, int y) {
		x = request.getParameter(x);
		if (x == null || x.length() == 0)
			return y;
		return uInt(x);
	}

	public static String uFormatDate(Date x) {
		try {
			return new SimpleDateFormat(dateFormat).format(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uFormatDate(Date x, String dateFormat) {
		try {
			return new SimpleDateFormat(dateFormat).format(x);
		} catch (Exception e) {
			return null;
		}
	}


	public static String uFormatDate(Date x, int dateFormat) {
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat]).format(x);
		} catch (Exception e) {
			return null;
		}
	}

	
	public static String uFormatDate(java.sql.Date x) {
		try {
			return new SimpleDateFormat(dateFormat).format(x).concat(" 00:00:00");
		} catch (Exception e) {
			return null;
		}
	}

	
	public static String uFormatDate(java.sql.Date x, int dateFormat) {
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat]).format(x).concat(" 00:00:00");
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String uFormatDateSade(java.sql.Date x) {
		try {
			return new SimpleDateFormat(dateFormat).format(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uFormatDateTimeSade(java.sql.Timestamp x) {
		try {
			return new SimpleDateFormat(dateFormat).format(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uFormatDateTime(Date x) {
		try {
			return new SimpleDateFormat(dateFormat.concat(" HH:mm:ss")).format(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uFormatDateTime(java.sql.Timestamp x) {
		try {
			return new SimpleDateFormat(dateFormat.concat(" HH:mm:ss")).format(x);
		} catch (Exception e) {
			return null;
		}
	}


	public static String uFormatDateTime(java.sql.Timestamp x, int dateFormat) {
		try {
			return new SimpleDateFormat(dateFormatMulti[dateFormat].concat(" HH:mm:ss")).format(x);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String uFormatDateOnlyTime(Date x) {
		try {
			return new SimpleDateFormat("HH:mm").format(x);
		} catch (Exception e) {
			return null;
		}
	}

	public static String uFormatDateTodayIfNull(Date x) {
		if (x == null)
			x = new Date();
		try {
			return new SimpleDateFormat(dateFormat).format(x);
		} catch (Exception e) {
			return "";
		}
	}

	public static Integer uInteger(HttpServletRequest request, String x) {
		return uInteger(request.getParameter(x));
	}

	public static Integer uInteger(Map<String, String> request, String x) {
		return uInteger(request.get(x));
	}

	public static Short uShort(HttpServletRequest request, String x) {
		return uShort(request.getParameter(x));
	}

	public static Object getObjectByTip(String value, int tip) {
		try {
			if (value != null)
				switch (tip) {
				case 1:
					return value;
				case 2:
					return GenericUtil.uDate(value);
				case 3:
					return GenericUtil.uDouble(value);
				case 4:
					return GenericUtil.uInteger(value);
				case 5:
					return GenericUtil.uCheckBox(value);
				case 6:
					return GenericUtil.uLong(value);
				case 8:
					return new JSONObject(value);
				case 9:
					return value.length() > 0 && value.charAt(0) == '{' ? new JSONObject(value) : value;
				case 10:
					return new JSONArray(value);
				}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static Object getObjectByControl(String value, int tip) {
		try {
			switch (tip) {
			case 1:
			case 11:
			case 12: // string
				return value;
			case 2: // date
				return GenericUtil.uDate(value);
			case 3: // double
				return GenericUtil.uDouble(value);
			case 4:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 13:
			case 14: // integer
				return GenericUtil.uInteger(value);
			case 5: // boolean
				return GenericUtil.uCheckBox(value);
			default: // aksi halde
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}


	public static String stringToHtml(Object x) {
		if (x == null)
			return "";
		String s = x.toString();
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			// be carefull with this one (non-breaking whitee space)
			// case ' ': sb.append("&nbsp;");break;
			case '\'':
				sb.append("\\'");
				break;
			// case '\\':sb.append("\\\\");break;
			case '\n':
				sb.append("<br>");
				break;
			case '\r':
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static String stringToHtml2(Object x) {
		if (x == null)
			return "";
		String s = x.toString();
		StringBuilder sb = new StringBuilder();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			// be carefull with this one (non-breaking whitee space)
			// case ' ': sb.append("&nbsp;");break;
			// case '\'':sb.append("\\'");break;
			// case '\\':sb.append("\\\\");break;
			case '\n':
				sb.append("");
				break;
			case '\r':
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	public static void replaceCharWidthString(StringBuilder source, StringBuilder destination, char c, String s) {
		int n = source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			if (x == c)
				destination.append(s);
			else
				destination.append(x);
		}
	}

	public static void replaceCharWidthString(String source, StringBuilder destination, char c, String s) {
		int n = source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			if (x == c)
				destination.append(s);
			else
				destination.append(x);
		}
	}

	// ' ile
	public static String stringToJS(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '/':
				if (j < n - 1 && source.charAt(j + 1) == '/')
					sb.append("\\/");
				else
					sb.append("/");
				break;
			// case '/':if(j<n-1 &&
			// source.charAt(j+1)=='/')sb.append("/'+'");break;
			// case '/':sb.append("\\/");break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\'':
				sb.append("\\'");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	public static String onlyHTMLToJS(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\n':
				sb.append("");
				break;
			case '\t':
				sb.append(" &nbsp; &nbsp; ");
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	// " ile
	public static String stringToJS2(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '/':
				if (j < n - 1 && source.charAt(j + 1) == '/')
					sb.append("\\/");
				else
					sb.append("/");
				break;
			// case '/':if(j<n-1 &&
			// source.charAt(j+1)=='/')sb.append("/'+'");break;
			// case '/':sb.append("\\/");break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\r':
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	// ' ile
	public static String stringToJS(StringBuilder source) {
		StringBuilder sb = new StringBuilder();
		int n = source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '/':
				if (j < n - 1 && source.charAt(j + 1) == '/')
					sb.append("\\/");
				else
					sb.append("/");
				break;
			// case '/':if(j<n-1 &&
			// source.charAt(j+1)=='/')sb.append("/'+'");else
			// sb.append("/");break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\'':
				sb.append("\\'");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	// " ile
	public static String stringToJS2SB(StringBuilder source) {
		StringBuilder sb = new StringBuilder();
		int n = source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '/':
				if (j < n - 1 && source.charAt(j + 1) == '/')
					sb.append("\\/");
				else
					sb.append("/");
				break;
			// case '/':if(j<n-1 &&
			// source.charAt(j+1)=='/')sb.append("/'+'");else
			// sb.append("/");break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	// ' ile
	public static String fromMapToJsonString(Map s) {
		if (s == null || s.isEmpty())
			return "{}";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		html.append("{");
		for (Object q : s.keySet()) {
			if (b)
				html.append(", ");
			else
				b = true;
			Object o = s.get(q);
			if (o != null && (o instanceof Integer || o instanceof Short || o instanceof Long || o instanceof Double || o instanceof BigDecimal))
				html.append("\"").append(q).append("\":").append(o != null ? o : "");
			else
				html.append("\"").append(q).append("\":\"").append(o != null ? stringToJS2(o.toString()) : "")
						.append("\"");
		}
		html.append("}");
		return html.toString();
	}

	// " ile
	public static String fromMapToJsonString2(Map s) {
		if (s == null || s.isEmpty())
			return "{}";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		html.append("{");
		for (Object q : s.keySet()) {
			if (b)
				html.append(",");//\n
			else
				b = true;
			Object o = s.get(q);
			if (o != null
					&& (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean))
				html.append("\"").append(q).append("\":").append(o);
			else
				html.append("\"").append(q).append("\":\"").append(o != null ? stringToJS2(o.toString()) : "")
						.append("\"");
		}
		html.append("}");
		return html.toString();
	}

	// " ile
	public static String fromMapToInfluxFields(Map s) {
		if (s == null || s.isEmpty())
			return "";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		for (Object q : s.keySet()) {
			if (b)
				html.append(",");
			else
				b = true;
			Object o = s.get(q);
			if (o != null
					&& (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean))
				html.append(q).append("=").append(o);
			else
				html.append(q).append("=\"").append(o != null ? stringToJS2(o.toString()) : "").append("\"");
		}
		return html.toString();
	}

	@SuppressWarnings("unchecked")
	public static String fromListToJsonString2Recursive(List<Object> s) {
		if (s == null || s.isEmpty())
			return "[]";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		html.append("[");
		for (Object o : s) {
			if (b)
				html.append(",");//\n
			else
				b = true;
			if (o == null)
				html.append("\"\"");
			else if (o instanceof Map)
				html.append(fromMapToJsonString2Recursive((Map<String, Object>) o));
			else if (o instanceof List) {
				html.append(fromListToJsonString2Recursive((List<Object>) o));
			} else if (o instanceof Object[]) {
				List lx = new ArrayList(((Object[]) o).length);
				for (int qi = 0; qi < ((Object[]) o).length; qi++)
					lx.add(((Object[]) o)[qi]);
				html.append(fromListToJsonString2Recursive(lx));
			} else if (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean)
				html.append(o);
/*			else if (o instanceof NativeObject)
				html.append(RhinoUtil.fromNativeObjectToJsonString2Recursive((NativeObject) o));
			else if (o instanceof NativeArray) {
				html.append(RhinoUtil.fromNativeArrayToJsonString2Recursive((NativeArray) o));
			}*/ else
				html.append("\"").append(stringToJS2(o.toString())).append("\"");
		}
		html.append("]");
		return html.toString();
	}

	// " ile
	@SuppressWarnings("unchecked")
	public static String fromMapToJsonString2Recursive(Map<String, Object> s) {
		if (s == null || s.isEmpty())
			return "{}";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		html.append("{");
		for (Object q : s.keySet()) {
			if (b)
				html.append(",");//\n
			else
				b = true;
			Object o = s.get(q);
			if (o == null)
				html.append("\"").append(q).append("\":\"\"");
			else if (o instanceof JSONObject)
				html.append("\"").append(q).append("\":").append(((JSONObject) o).toString());
			else if (o instanceof JSONArray)
				html.append("\"").append(q).append("\":").append(((JSONArray) o).toString());
			else if (o instanceof Map)
				html.append("\"").append(q).append("\":")
						.append(fromMapToJsonString2Recursive((Map<String, Object>) o));
			else if (o instanceof List) {
				html.append("\"").append(q).append("\":").append(fromListToJsonString2Recursive((List<Object>) o));
			} else if (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean
					|| o instanceof Short || o instanceof Long || o instanceof Float)
				html.append("\"").append(q).append("\":").append(o);
			/*else if (o instanceof NativeObject)
				html.append("\"").append(q).append("\":")
						.append(RhinoUtil.fromNativeObjectToJsonString2Recursive((NativeObject) o));
			else if (o instanceof NativeArray) {
				html.append("\"").append(q).append("\":")
						.append(RhinoUtil.fromNativeArrayToJsonString2Recursive((NativeArray) o));
			}*/ else
				html.append("\"").append(q).append("\":\"").append(stringToJS2(o.toString())).append("\"");
		}
		html.append("}");
		return html.toString();
	}

	@SuppressWarnings("unchecked")
	public static String fromMapToJsonString2Recursive(Map<String, Object> s, List<W5WsMethodParam> params, int parentId) {
		if (s == null || s.isEmpty())
			return "{}";
		if(GenericUtil.isEmpty(params))return fromMapToJsonString2Recursive(s);
		StringBuilder html = new StringBuilder();
		boolean b = false;
		html.append("{");
		for (W5WsMethodParam p:params)if(p.getOutFlag()==0 && p.getParentId()==parentId && s.containsKey(p.getDsc()) && p.getCredentialsFlag()>=3) {
			String q = p.getDsc();			
			if (b)
				html.append(",");//\n
			else
				b = true;
			Object o = s.get(q);
			
			if (o == null)
				html.append("\"").append(q).append("\":\"\"");
			else if (o instanceof JSONObject) {
				if(p.getParamTip()==8 || p.getParamTip()==9)
					html.append("\"").append(q).append("\":").append(((JSONObject) o).toString());
				else 
					html.append("\"").append(q).append("\":null");
			} else if (o instanceof JSONArray) {
				if(p.getParamTip()==10)
					html.append("\"").append(q).append("\":").append(((JSONArray) o).toString());
				else 
					html.append("\"").append(q).append("\":null");
			} else if (o instanceof Map) {
				if(p.getParamTip()==8 || p.getParamTip()==9)
					html.append("\"").append(q).append("\":")
						.append(fromMapToJsonString2Recursive((Map<String, Object>) o, params, p.getWsMethodParamId()));
				else 
					html.append("\"").append(q).append("\":null");
			} else if (o instanceof List) {
				if(p.getParamTip()==10)
					html.append("\"").append(q).append("\":").append(fromListToJsonString2Recursive((List<Object>) o));
				else 
					html.append("\"").append(q).append("\":null");
			} else {
				html.append("\"").append(q).append("\":");
				switch(p.getParamTip()) {
				case	4://integer
					html.append(GenericUtil.uInt(o));break;
				case	3://float
					if(o instanceof Float)html.append(o);
					else if(o instanceof Double)html.append(o);
					else if(o instanceof BigDecimal)html.append(o);
					else html.append(GenericUtil.uFloat(o.toString()));
					break;
				case	5://boolean
					if(o instanceof Boolean)html.append(o);
					else html.append(GenericUtil.uInt(o)!=0 || o.toString().toLowerCase().equals("true"));
					break;
				default:
					html.append("\"").append(stringToJS2(o.toString())).append("\"");
					
				}
			}
			/*else if (o instanceof NativeObject)
				html.append("\"").append(q).append("\":")
						.append(RhinoUtil.fromNativeObjectToJsonString2Recursive((NativeObject) o));
			else if (o instanceof NativeArray) {
				html.append("\"").append(q).append("\":")
						.append(RhinoUtil.fromNativeArrayToJsonString2Recursive((NativeArray) o));
			}*/
		}
		html.append("}");
		return html.toString();
	}
	public static String uCepTel(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			if (x >= '0' && x <= '9')
				sb.append(x);
		}
		return sb.toString();
	}

	public static String uAdres2String(Address[] a) {
		StringBuilder str = new StringBuilder();
		if (a != null) {
			boolean b = false;
			for (Address i : a) {
				if (b)
					str.append(", ");
				else
					b = true;
				String s = i.toString();
				if (s.charAt(0) == '=')
					s = s.substring(s.indexOf('<') + 1, s.length() - 1);
				str.append(s);
			}
		}
		return str.toString();
	}

	public static boolean uBoolean(String x) {
		if (x == null || x.trim().length() == 0)
			return false;
		try {
			return Boolean.parseBoolean(x.trim());
		} catch (Exception e) {
			return false;
		}
	}

	public static String uUrl2Str(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			char y = j < n - 1 ? source.charAt(j + 1) : '.';
			switch (x) {
			case 0xC5:
				if (y == 0x9F)
					sb.append('ş');
				j++;
				break;
			case 0xC3:
				if (y == 0xA7)
					sb.append('ç');
				if (y == 0xBC)
					sb.append('ü');
				if (y == 0xB6)
					sb.append('ö');
				j++;
				break;
			case 0xC4:
				if (y == 0xB1)
					sb.append('ı');
				if (y == 0x9F)
					sb.append('ğ');
				j++;
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	public static String uStr2English(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case 'ç':
				sb.append('c');
				break;
			case 'Ç':
				sb.append('C');
				break;
			case 'ş':
				sb.append('s');
				break;
			case 'Ş':
				sb.append('S');
				break;
			case 'ü':
				sb.append('u');
				break;
			case 'Ü':
				sb.append('Ü');
				break;
			case 'ö':
				sb.append('o');
				break;
			case 'Ö':
				sb.append('O');
				break;
			case 'ı':
				sb.append('i');
				break;
			case 'İ':
				sb.append('I');
				break;
			case 'ğ':
				sb.append('g');
				break;
			case 'Ğ':
				sb.append('G');
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}
	/*
	 * public static String uStr2Alpha(String source) { StringBuilder sb = new
	 * StringBuilder(); int n = source==null ? 0 : source.length(); for (int j =
	 * 0; j < n; j++){ char x = source.charAt(j); if((x>='a' && x<='z') ||
	 * (x>='A' && x<='Z') || (x>='0' && x<='0') || x=='_') sb.append(x); }
	 * return sb.toString(); }
	 * 
	 * public static String uStr2Alpha2(String source) { StringBuilder sb = new
	 * StringBuilder(); int n = source==null ? 0 : source.length(); for (int j =
	 * 0; j < n; j++){ char x = source.charAt(j); if((x>='a' && x<='z') ||
	 * (x>='A' && x<='Z') || (x>='0' && x<='0') || x=='_') sb.append(x); else
	 * if(x==' ') sb.append('_'); } return sb.toString(); }
	 */

	public static <T> T getFirstItem(List<T> find) {
		if (find == null || find.size() == 0)
			return null;
		return find.get(0);
	}

	public static int uIntNvl(Map<String, String> requestParams, String x, int y) {
		Object o = requestParams.get(x);
		if (o == null || o.toString().length() == 0)
			return y;
		return uInt(o);
	}

	public static int uInt(Map<String, String> requestParams, String x) {
		return uInt(requestParams.get(x));
	}

	public static int uInt(Map<String, String> requestParams, String x, int defaultValue) {
		String s = requestParams.get(x);
		return GenericUtil.isEmpty(s) ? defaultValue : uInt(s);
	}

	@SuppressWarnings("unchecked")
	public static <T> T stringToType(String value, Class<T> type, T defaultValue) {
		try {
			if ((value == null) || ("".equals(value)))
				return defaultValue;
			else if (type.isAssignableFrom(String.class))
				return (T) value;
			else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class))
				return (T) new Integer(value);
			else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class))
				return (T) new Double(value);
			else if (type.isAssignableFrom(BigDecimal.class))
				return (T) new BigDecimal(value);
			else if (type.isAssignableFrom(Long.class))
				return (T) new Long(value);
			else
				throw new RuntimeException("Bilinmeyen veri tipi " + type + " (property=" + value + ")");
		} catch (RuntimeException cce) {
			return defaultValue;
		}
	}

	public static boolean isComplex(Class<?> type) {
		return !((type.isAssignableFrom(String.class)) || (type.isAssignableFrom(Integer.class))
				|| (type.isAssignableFrom(int.class)) || (type.isAssignableFrom(Double.class))
				|| (type.isAssignableFrom(double.class)) || (type.isAssignableFrom(Float.class))
				|| (type.isAssignableFrom(float.class)) || (type.isAssignableFrom(Boolean.class))
				|| (type.isAssignableFrom(boolean.class)) || (type.isAssignableFrom(java.util.Date.class)));
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> T stringToType(String value, Class<T> type) {
		if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class))
			return (T) (Boolean) ("true".equalsIgnoreCase(value) || "1".equals(value) || "on".equals(value));
		if (type.isAssignableFrom(String.class))
			return (T) value;
		if ((value == null) || ("".equals(value)))
			return null;
		if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class))
			return (T) new Integer(value);
		if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class))
			return (T) ((Object) value.charAt(0));
		if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class))
			return (T) new Double(value);
		if (type.isAssignableFrom(BigDecimal.class))
			return (T) new BigDecimal(value);
		if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class))
			return (T) new Long(value);
		if (type.isAssignableFrom(Date.class))
			try {
				return (T) new SimpleDateFormat("dd/MM/yyyy").parse(value);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		if (Enum.class.isAssignableFrom(type)) {
			java.lang.Enum<?>[] e = (java.lang.Enum<?>[]) type.getEnumConstants();
			Object res = null;
			for (int i = 0; i < e.length; i++)
				if (e[i].toString().equals(value))
					res = e[i];
			return (T) res;
		}
		if (type.isAssignableFrom(Integer[].class)) {
			String[] s = value.trim().split(",");
			Integer[] ia = new Integer[s.length];
			for (int i = 0; i < ia.length; i++)
				ia[i] = uInt(s[i]);
			return (T) ia;
		}
		throw new RuntimeException("Bilinmeyen veri tipi " + type + " (property=" + value + ")");
	}

	public static <T> T objectToType(Object o, Class<T> type) {
		if (o == null)
			return null;
		return stringToType(o.toString(), type);
	}

	public static String objectToCurrency(Object o) {
		if (o == null)
			return "";
		if (o instanceof Double || o instanceof Float)
			return new DecimalFormat("###,###,###,##0.00").format(o);
		else if (o instanceof BigDecimal)
			return new DecimalFormat("###,###,###,##0.00").format(((BigDecimal) o).doubleValue());
		return o.toString();
	}

	public static String obj2Str_AlignLengthChar(Object o, int a, int l, char c) { // a:0center,1left,2right
		StringBuilder b = new StringBuilder(l);
		for (int i = 0; i < l; i++)
			b.append(c);
		if (o == null)
			return b.toString();
		String s = o.toString();
		if (s.length() > l)
			s = s.substring(0, l);
		switch (a) {
		case 0:
			int j = (l - s.length()) / 2;
			b.replace(j, s.length() + j, s);
			break;
		case 1:
			b.replace(0, s.length(), s);
			break;
		case 2:
			b.replace(l - s.length(), l, s);
			break;
		}
		return b.toString();
	}

	public static boolean hasPartInside(String all, String sub) {
		if (all == null || all.length() == 0)
			return true;
		for (String s : all.split(",")) {
			if (sub.equals(s))
				return true;
		}
		return false;
	}

	public static boolean hasPartInside2(String all, Object sub) {
		if (all == null || all.length() == 0)
			return false;
		String z = sub == null ? null : sub.toString();
		if (z == null || z.length() == 0)
			return false;
		all = "," + all + ",";
		z = "," + z + ",";
		return all.contains(z);
	}


	public static boolean hasPartInside2b(String all, Object sub) {
		if (all == null || all.length() == 0)
			return false;
		String z = sub == null ? null : sub.toString();
		if (z == null || z.length() == 0)
			return false;
		String[] sub2 = z.split(",");
		all = "," + all + ",";
		for(int qi=0;qi<sub2.length;qi++)
			if(all.contains("," + sub2[qi] + ","))return true;
		return false;
	}
	public static String toCsv(List<String[]> list) {
		String res = "";
		for (String[] arr : list) {
			for (String s : arr)
				res += s != null ? s + ";" : ";";
			res = res.substring(0, res.length() - 1) + "\n";
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getParameterMap(HttpServletRequest request) throws IOException {
		Map<String, String[]> m = request.getParameterMap();
		Map<String, String> res = new HashMap<String, String>();
		for (Map.Entry<String, String[]> e : m.entrySet()) {
			String resx = "";
			for (String s : (String[]) e.getValue())
				resx += "," + s;
			res.put(e.getKey(), resx.substring(1));
		}
		res.put("_ServerURL_", request.getServerName());
		res.put("_ClientURL_", request.getRemoteHost());
		String transactionId = (String)request.getAttribute("_trid_");
		if(transactionId!=null)res.put("_trid_", transactionId);

		if (GenericUtil.safeEquals(request.getContentType(), "application/json")) {
			try {
				JSONObject jo = HttpUtil.getJson(request);
				if (jo != null) {
					Map<String, Object> res2 = GenericUtil.fromJSONObjectToMap(jo);
					if(!GenericUtil.isEmpty(res2)) for(String k:res2.keySet()){
						Object v = res2.get(k);
						res.put(k, v!=null ? v.toString():null);
					}
					
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return res;
	}

	static long next_id = System.currentTimeMillis();

	public static String getNextId(String prefix) {
		return prefix + "_" + (next_id++);
	}

	public static Object prepareParam(W5Param param, Map<String, Object> scd, Map<String, String> requestParams,
			short sourceTip, Map<String, String> extraParams, short notNullFlag, String dsc, String defaultValue,
			Map<String, String> errorMap, PostgreSQL dao) {
		String pvalue = null;
		boolean hasError = false;
		if (sourceTip < 0)
			sourceTip = param.getSourceTip();
		if (notNullFlag == 0)
			notNullFlag = param.getNotNullFlag();
		if (sourceTip == param.getSourceTip() && (defaultValue == null || defaultValue.length() == 0))
			defaultValue = param.getDefaultValue();

		switch (sourceTip) {
		case 0: // non-interaktif
			pvalue = defaultValue;
			break;
		case 8: // global Nextval
			return getGlobalNextval(defaultValue, scd != null ? (String) scd.get("projectId") : null,
					scd != null ? (Integer) scd.get("userId") : 0,
					scd != null ? (Integer) scd.get("customizationId") : 0);
		case 9: // UUID
			pvalue = UUID.randomUUID().toString();
			break;
		case 1: // request : post edilmisse request'ten aksi halde grid'den al
			if (extraParams != null) {
				pvalue = extraParams.get(param.getDsc());
			}
			if (pvalue == null) {
				String xdsc = dsc != null ? dsc : param.getDsc();
				if (param.getParamTip() == 10 && xdsc.contains("-")) { // ardarda
																		// birkactane,
																		// sadece
																		// custom
																		// operator
																		// ile
																		// kullanilabilir
					String[] xdscs = xdsc.split("-");
					Object[] pvalues = new Object[xdscs.length];
					for (int q7 = 0; q7 < xdscs.length; q7++) {
						pvalues[q7] = GenericUtil.getObjectByTip(requestParams.get(xdscs[q7].trim()),
								param.getParamTip());
					}
					return pvalues;
				}
				Object oo = requestParams.get(xdsc);
				if (oo == null && param.getParamTip() == 5)
					oo = requestParams.get(xdsc + "[]");
				pvalue = oo == null ? null : oo.toString();
			}
			break;
		case 2: // session
			if (param.getDefaultValue() != null || defaultValue != null) {
				String dv = defaultValue != null ? defaultValue : param.getDefaultValue();
				if (dv.contains("-")) { // ardarda birkactane, sadece custom
										// operator ile kullanilabilir
					String[] xdscs = dv.split("-");
					Object[] pvalues = new Object[xdscs.length];
					for (int q7 = 0; q7 < xdscs.length; q7++) {
						pvalues[q7] = scd.get(xdscs[q7].trim());
					}
					return pvalues;
				}
				Object o = scd.get(dv);
				pvalue = o == null ? null : o.toString();
			} else
				pvalue = null;
			break;
		case 3: // app_setting
			pvalue = FrameworkCache.getAppSettingStringValue(scd, param.getDefaultValue());
			break;
		case 4: // expression, ornegin seq_ali.nextval
			if (dao != null) {
				Object[] oz = DBUtil.filterExt4SQL(defaultValue, scd, requestParams, null);
				// List<Map> lm =
				// dao.executeSQLQuery2Map(oz[0].toString(),(List)oz[1]);
				List lr = dao.executeSQLQuery2(oz[0].toString(), oz.length > 1 ? (List) oz[1] : null);
				return GenericUtil.isEmpty(lr) ? null : lr.get(0);
			} else
				return defaultValue;
		case 5: // Custom JS Rhino

			Object o = GlobalScriptEngine.executePrepareParam(defaultValue, scd, requestParams, dao);
			if(o==null)pvalue = null;
			else if(o instanceof String) pvalue= o.toString();
			else return o;
			
		}

		if (pvalue == null || pvalue.trim().length() == 0)
			pvalue = defaultValue;

		Object psonuc = null;
		switch(param.getParamTip()) {
		case 2: if(scd!=null && pvalue!=null) {
			psonuc = GenericUtil.uDate(pvalue, uInt(scd.get("date_format")));
			break;
		}
		default:
			psonuc = GenericUtil.getObjectByTip(pvalue, param.getParamTip());
		}
		if (notNullFlag != 0 && psonuc == null) { // not null
			hasError = true;
			errorMap.put(param.getDsc(), LocaleMsgCache.get2(scd, "validation_error_not_null")); 
		} else if ((param.getParamTip() == 5 || param.getParamTip() == 2) && (param instanceof W5TableField)) {
			W5TableField tf = (W5TableField) param;
			if (tf.getDefaultControlTip() == param.getParamTip() && tf.getDefaultLookupTableId() > 0) {
				if (param.getParamTip() == 5) {
					psonuc = GenericUtil.uInt(psonuc) != 0;
				} else {
					psonuc = GenericUtil.uDateTm(pvalue, scd!=null ? uInt(scd.get("date_format")): 0);
				}
			}
		}

		if (!hasError && psonuc != null && (psonuc instanceof Integer || psonuc instanceof Double)
				&& (param.getMinValue() != null || param.getMaxValue() != null)) {
			BigDecimal bd = new BigDecimal(
					psonuc instanceof Integer ? ((Integer) psonuc).intValue() : ((Double) psonuc).doubleValue());
			if (param.getMinValue() != null && param.getMinValue().compareTo(bd) == 1) {
				hasError = true;
				errorMap.put(param.getDsc(),
						LocaleMsgCache.get2(scd, "validation_error_value_min") + " (" + param.getMinValue() + ")"); 
			} else if (param.getMaxValue() != null && param.getMaxValue().compareTo(bd) == -1) {
				hasError = true;
				errorMap.put(param.getDsc(),
						LocaleMsgCache.get2(scd, "validation_error_value_max") + " (" + param.getMaxValue() + ")"); 
			}
		}

		if (!hasError && psonuc != null && (psonuc instanceof String)
				&& (param.getMinLength() != null || param.getMaxLength() != null)) {
			String s = (String) psonuc;
			if (param.getMinLength() != null && param.getMinLength() > s.length()) {
				hasError = true;
				errorMap.put(param.getDsc(),
						LocaleMsgCache.get2(scd, "validation_error_length_min") + " (" + param.getMinLength() + ")"); // "Uzunluk
																														// Sorunu"
			} else if (param.getMaxLength() != null && param.getMaxLength() > 0 && param.getMaxLength() < s.length()) {
				hasError = true;
				errorMap.put(param.getDsc(),
						LocaleMsgCache.get2(scd, "validation_error_length_max") + " (" + param.getMaxLength() + ")"); // "Uzunluk
																														// Sorunu"
			}
		}

		return psonuc;
	}

	public static Object prepareParam(W5Param param, Map<String, Object> scd, Map<String, String> requestParams,
			short sourceTip, Map<String, String> extraParams, short notNullFlag, String dsc, String defaultValue,
			Map<String, String> errorMap) {
		return prepareParam(param, scd, requestParams, sourceTip, extraParams, notNullFlag, dsc, defaultValue, errorMap,
				null);
	}

	public static boolean accessControl(Map<String, Object> scd, short accessTip, String accessRoles,
			String accessUsers) {
		boolean b = (accessTip == 0)
				|| (hasPartInside2(accessRoles, scd.get("roleId")) || hasPartInside2(accessUsers, scd.get("userId")));
		return b;
	}

	public static int accessControlFormCell(Map<String, Object> scd, short accessTip, String accessRoles,
			String accessUsers) { // 0:izin var, 1:izin yok, 2: izin var,
									// readonly

		if (accessTip == 0)
			return 0;
		boolean b = (hasPartInside2(accessRoles, scd.get("roleId")) || hasPartInside2(accessUsers, scd.get("userId")));
		return b ? 0 : accessTip;
	}

	public static String replaceSql(String sql, List<Object> params) {
		if (params == null || sql == null)
			return sql;
		StringBuilder b = new StringBuilder();
		int paramIndex = 0;
		for (int i = 0; i < sql.length(); i++)
			if (sql.charAt(i) == '?') {
				Object o = params!=null && params.size()>paramIndex? params.get(paramIndex++):null;
				if (o == null)
					b.append("null");
				else if (o instanceof String)
					b.append("'").append(o).append("'");
				else if (o instanceof java.sql.Timestamp)
					b.append("'").append(o).append("'");
				else if (o instanceof String)
					b.append("'").append(o).append("'");
				else if (o instanceof java.sql.Timestamp)
					b.append("to_date('").append(uFormatDateTime((java.sql.Timestamp) o)).append("','")
							.append(dateFormat.concat(" HH:mm:ss")).append("')");
				else if (o instanceof java.sql.Date)
					b.append("to_date('").append(uFormatDate((java.sql.Date) o)).append("','").append(dateFormat)
							.append("')");
				else if (o instanceof java.util.Date)
					b.append("to_date('").append(uFormatDate((java.util.Date) o)).append("','").append(dateFormat)
							.append("')");
				else
					b.append(o);
				if (paramIndex >= params.size()) {
					return b.append(sql.substring(i + 1)).toString();
				}
			} else
				b.append(sql.charAt(i));

		return b.toString();
	}

	public static String replaceInfluxQL(String sql, List<Object> params) {
		if (params == null || sql == null)
			return sql;
		StringBuilder b = new StringBuilder();
		int paramIndex = 0;
		for (int i = 0; i < sql.length(); i++)
			if (sql.charAt(i) == '?') {
				Object o = params.get(paramIndex++);
				if (o == null)
					b.append("null");
				else
					b.append(o);
				if (paramIndex >= params.size()) {
					return b.append(sql.substring(i + 1)).toString();
				}
			} else
				b.append(sql.charAt(i));

		return b.toString();
	}

	public static StringBuilder filterExt(String code, Map<String, Object> scd, Map<String, String> requestParams,
			Map<String, Object> obj) {
		StringBuilder tmp = new StringBuilder();
		if (code == null || code.length() == 0)
			return tmp;
		tmp.append(code);
		for (int bas = tmp.indexOf("${"); bas > -1; bas = tmp.indexOf("${", bas + 2)) {
			int bit = tmp.indexOf("}", bas + 2);
			String subStr = tmp.substring(bas + 2, bit);
			String replaced = null;
			if (subStr.startsWith("scd.")) {
				replaced = scd != null && scd.get(subStr.substring(4)) != null ? scd.get(subStr.substring(4)).toString()
						: "null"; // getMsgHTML de olabilirdi
			} else if (subStr.startsWith("req.")) {
				replaced = requestParams != null && requestParams.get(subStr.substring(4)) != null
						? requestParams.get(subStr.substring(4)) : "null"; // getMsgHTML
																			// de
																			// olabilirdi
			} else if (subStr.startsWith("obj.")) {
				replaced = obj != null && obj.get(subStr.substring(4)) != null ? obj.get(subStr.substring(4)).toString()
						: "null"; // getMsgHTML de olabilirdi
			} else if (subStr.startsWith("app.")) {
				replaced = FrameworkCache.getAppSettingStringValue(scd.get("customizationId"), subStr.substring(4));
			} else {
				replaced = LocaleMsgCache.get2(scd,
						subStr); // getMsgHTML de olabilirdi
			}
			if (replaced != null)
				tmp.replace(bas, bit + 1, replaced); // getMsgHTML de olabilirdi
		}
		return tmp;
	}

	public static StringBuilder filterURI(String code, Map<String, Object> scd, Map<String, String> requestParams,
			Map<String, Object> obj) {
		StringBuilder tmp = new StringBuilder();
		if (code == null || code.length() == 0)
			return tmp;
		tmp.append(code);
		for (int bas = tmp.indexOf("${"); bas > -1; bas = tmp.indexOf("${", bas + 2)) try{
			int bit = tmp.indexOf("}", bas + 2);
			String subStr = tmp.substring(bas + 2, bit);
			String replaced = null;
			if (subStr.startsWith("scd.")) {
				replaced = scd != null && scd.get(subStr.substring(4)) != null ? scd.get(subStr.substring(4)).toString()
						: "null"; // getMsgHTML de olabilirdi
			} else if (subStr.startsWith("req.")) {
				replaced = requestParams != null && requestParams.get(subStr.substring(4)) != null
						? requestParams.get(subStr.substring(4)) : "null"; // getMsgHTML
																			// de
																			// olabilirdi
			} else if (subStr.startsWith("obj.")) {
				replaced = obj != null && obj.get(subStr.substring(4)) != null ? obj.get(subStr.substring(4)).toString()
						: "null"; // getMsgHTML de olabilirdi
			} else if (subStr.startsWith("app.")) {
				replaced = FrameworkCache.getAppSettingStringValue(scd.get("customizationId"), subStr.substring(4));
			} else {
				replaced = replaced = requestParams != null && requestParams.get(subStr) != null
						? requestParams.get(subStr) : "null"; // getMsgHTML de olabilirdi
			}
			if (replaced != null)
				tmp.replace(bas, bit + 1, URLEncoder.encode(replaced, "UTF-8")); // getMsgHTML de olabilirdi
		} catch (Exception ee) {
			throw new IWBException("framework", "filterURI", 0,
					"FilterURI Exception: " + ee.getMessage(), null, ee);
		}
		return tmp;
	}
	
	public static StringBuilder filterExtWithPrefix(String code, String prefix) {
		StringBuilder tmp = new StringBuilder();
		if (code == null || code.length() == 0)
			return tmp;
		tmp.append(code);
		if (prefix == null)
			return tmp;
		for (int bas = tmp.indexOf("${req."); bas > 0; bas = tmp.indexOf("${req.", bas + 2)) {
			int bit = tmp.indexOf("}", bas + 2);
			tmp.replace(bit, bit, prefix); // getMsgHTML de olabilirdi
		}
		return tmp;
	}


	public static W5FormCellHelper getFormCellResultByQueryRecord(Object[] d) {
		W5FormCell cell = new W5FormCell();
		cell.setDsc((String) d[0]);
		cell.setLocaleMsgKey((String) d[1]);
		cell.setControlTip((short) GenericUtil.uInt(d[2]));
		cell.setControlWidth((short) GenericUtil.uInt(d[3]));
		cell.setLookupQueryId(GenericUtil.uInt(d[4]));
		if (cell.getControlTip() != 5)
			cell.setNotNullFlag((short) GenericUtil.uInt(d[5]));
		cell.setFormModuleId(GenericUtil.uInt(d[7]));
		W5FormCellHelper result = new W5FormCellHelper(cell);
		result.setValue((String) d[6]);
		if (d.length > 8)
			cell.setExtraDefinition((String) d[8]);
		if (d.length > 9)
			cell.setLookupIncludedParams((String) d[9]);
		if (d.length > 10)
			cell.setLookupIncludedValues((String) d[10]);
		if (d.length > 11)
			cell.setTabOrder((short) GenericUtil.uInt(d[11])); // istenirse
																// alindiktan
																// sonra
																// override
																// edilebilir
		if (d.length > 12)
			cell.setFormCellId(GenericUtil.uInt(d[12]));
		if (d.length > 13)
			cell.setParentFormCellId(GenericUtil.uInt(d[13]));
		cell.setActiveFlag((short) 1);
		return result;
	}

	public static String strUTF2En(String txt) {
		txt = txt.replace("ı", "i");
		txt = txt.replace("İ", "I");
		txt = txt.replace("ö", "o");
		txt = txt.replace("Ö", "O");
		txt = txt.replace("ğ", "g");
		txt = txt.replace("Ğ", "G");
		txt = txt.replace("ü", "u");
		txt = txt.replace("Ü", "U");
		txt = txt.replace("ş", "s");
		txt = txt.replace("Ş", "S");
		txt = txt.replace("ç", "c");
		txt = txt.replace("Ç", "C");
		return txt;
	}

	public static boolean writeFile(File file, String data) {
		boolean b = true;
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(data);
			out.close();
			fstream.close();
		} catch (Exception e) { // Catch exception if any
			e.printStackTrace();
			b = false;
		}
		return b;
	}

	public static XMLGregorianCalendar long2Gregorian(long ldate) {
		DatatypeFactory dataTypeFactory = null;
		GregorianCalendar gc = new GregorianCalendar();
		try {
			dataTypeFactory = DatatypeFactory.newInstance();
			gc.setTimeInMillis(ldate);
		} catch (Exception e) {
			return null;
		}
		return dataTypeFactory.newXMLGregorianCalendar(gc);
	}

	public static byte[] file2Byte(File file) { // File to Byte[]
		InputStream is;
		try {
			is = new FileInputStream(file);
			long length = file.length();
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}
			byte[] bytes = new byte[(int) length];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			if (offset < bytes.length)
				throw new IOException("Could not completely read file " + file.getName());
			is.close();
			return bytes;

		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			return null;
		}
	}

	public static int getIndexNo(String value, List list) {
		int indexNo = -1;
		for (int index = 0; index < list.size(); index++) {
			W5LookUpDetay ld = (W5LookUpDetay) list.get(index);
			if ((ld.getVal()).equals(value))
				indexNo = index;
		}
		return indexNo;
	}

	public static boolean moveFile(String fileName, String directoryName) {
		boolean success = true;
		try { // copy then delete,
			File afile = new File(fileName);
			File directory = new File(directoryName);
			if (!directory.exists())
				directory.mkdir();
			InputStream in = new FileInputStream(afile);
			OutputStream out = new FileOutputStream(directory + "\\" + afile.getName());
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			// success=afile.renameTo(new File(directoryName+"\\" +
			// afile.getName()));
			success = afile.delete();
			if (!success)
				System.out.println("File is failed to move!,fileName: " + fileName + ",newFile : " + directoryName
						+ "\\" + afile.getName());
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	public static void setProxySettings(int customizationId) {
		try {
			if (FrameworkCache.getAppSettingIntValue(customizationId, "proxy_set_flag") == 1) {
				System.setProperty("http.proxyHost",
						FrameworkCache.getAppSettingStringValue(customizationId, "proxy_host"));
				System.setProperty("http.proxyUser",
						FrameworkCache.getAppSettingStringValue(customizationId, "proxy_user"));
				System.setProperty("http.proxyPassword",
						FrameworkCache.getAppSettingStringValue(customizationId, "proxy_password"));
				System.setProperty("http.proxyPort",
						FrameworkCache.getAppSettingStringValue(customizationId, "proxy_port"));
				System.setProperty("http.proxySet", "true");
				System.setProperty("http.proxyDomain",
						FrameworkCache.getAppSettingStringValue(customizationId, "proxy_domain"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String duplicateChar(String s, short count) {
		String res = "";
		for (int i = 0; i < count; i++) {
			res += s;
		}
		return res;
	}

	public static String completeChar(String deger, String karakter, short count, boolean alignRight) {
		String res = deger;
		for (int i = 0; i < (count - deger.length()); i++) {
			if (alignRight)
				res = karakter + res;
			else
				res += karakter;
		}
		return res;
	}

	public static String report2csv(List<W5ReportCellHelper> reportList) {
		StringBuilder sb = new StringBuilder();
		String sp = ";";
		boolean ilk = true;

		for (W5ReportCellHelper sonuc : reportList) {
			if ((sonuc.getRowTip() == 2) || (sonuc.getRowTip() == 3)) { // kolon
																		// baï¿½lï¿½ï¿½ï¿½
																		// yada
																		// deï¿½erler
																		// ise
				if (sonuc.getColumnId() == 1) {
					if (ilk == false)
						sb.append(new Character((char) 13).toString() + new Character((char) 10).toString()); // satï¿½r
																												// sonu
					else
						ilk = false;
				}
				String deger = "";
				if (sonuc.getDeger() != null)
					deger = sonuc.getDeger();
				sb.append(deger);
				sb.append(sonuc.getColspan() > 0 ? duplicateChar(sp, sonuc.getColspan()) : sp);
			}
		}
		return sb.toString();
	}

	public static String report2text(List<W5ReportCellHelper> reportList) {
		StringBuilder sb = new StringBuilder();
		ArrayList<Short> columnWidth = new ArrayList<Short>();
		ArrayList<String> columnChar = new ArrayList<String>();
		boolean ilk = true;

		for (W5ReportCellHelper sonuc : reportList) {
			if (sonuc.getColumnId() == 1) {
				if (ilk == false)
					sb.append(new Character((char) 13).toString() + new Character((char) 10).toString()); // satï¿½r
																											// sonu
				else
					ilk = false;
			}
			String deger = "";
			if (sonuc.getDeger() != null)
				deger = sonuc.getDeger();

			if ((sonuc.getRowTip() == 0)) { // baï¿½lï¿½k
				sb.append(deger);
			} else if ((sonuc.getRowTip() == 1)) { // params

			} else if (sonuc.getRowTip() == 2) { // kolon baï¿½lï¿½ï¿½ï¿½
				columnWidth.add(sonuc.getCellTip());
				String sp = " ";
				if ((sonuc.getTag() != null) && (sonuc.getTag() != ""))
					sp = sonuc.getTag();
				columnChar.add(sp);
				if (sonuc.getRowId() == 0)
					ilk = true;
				else
					sb.append(completeChar(deger, sp, sonuc.getCellTip(), false));
			} else if (sonuc.getRowTip() == 3) { // deï¿½erler
				sb.append(completeChar(deger, columnChar.get(sonuc.getColumnId() - 1),
						columnWidth.get(sonuc.getColumnId() - 1), false));
			}
		}
		return sb.toString();
	}

	public static String fromMapToHtmlString(Map s) {
		if (s == null || s.isEmpty())
			return "";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		for (Object q : s.keySet()) {
			if (b)
				html.append("<br>");
			else
				b = true;
			Object o = s.get(q);
			html.append("<b>").append(q).append("</b>:").append(o != null ? stringToJS(o.toString()) : "");
		}
		return html.toString();
	}
	

	public static String fromMapToHtmlString2(Map scd, Map s) {
		if (s == null || s.isEmpty())
			return "";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		for (Object q : s.keySet()) {
			html.append("<li>");
			Object o = s.get(q);
			html.append("<b>").append(LocaleMsgCache.get2(scd, q.toString())).append("</b>:").append(o != null ? stringToJS(o.toString()) : "");
		}
		return html.toString();
	}

	public static boolean isEmpty(Object o) {
		if (o == null)
			return true;
		if (o instanceof Map)
			return isEmpty((Map) o);
		if (o instanceof List)
			return isEmpty((List) o);
		if (o instanceof String)
			return isEmpty((String) o);
		if (o instanceof Set)
			return isEmpty((Set) o);
		return false;
	}

	public static boolean isEmpty(Map m) {
		return m == null || m.isEmpty();
	}

	public static boolean isEmpty(List l) {
		return l == null || l.isEmpty();
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isEmpty(Set s) {
		return s == null || s.isEmpty();
	}

	public static boolean accessControlTable(Map<String, Object> scd, W5Table t) {
		if (t == null || scd == null)
			return false;
		switch (t.getAccessViewTip()) {
		case 0:
			if (!FrameworkCache.roleAccessControl(scd, 0)) {
				return false;
			}
			break;
		case 1:
			if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
					t.getAccessViewRoles(), t.getAccessViewUsers())) {
				return false;
			}
		}
		return true;
	}



	public static class ResultMessage {

		boolean success;
		String result;

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}
	}

	public static boolean safeEquals(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.equals(o2);
	}

	public static boolean safeEquals2(Object o1, Object o2) {
		if (o1 == null && o2 == null)
			return true;
		// if(uStrNvl((String)o1,"").equals(uStrNvl((String)o2,"")))return true;
		if ((o1 == null || o1.toString().equals("")) && (o2 == null || o2.toString().equals("")))
			return true;
		if (o1 == null || o2 == null)
			return false;
		return o1.toString().equals(o2.toString());
	}

	public static boolean safeEquals(List<W5Base> l1, List<W5Base> l2) {
		if (l1 != null && l2 != null) {
			if (l1.size() == l2.size())
				return false;
			for (int i = 0; i < l1.size(); i++) {
				W5Base c1 = l1.get(i);
				W5Base c2 = l2.get(i);
				if (!c1.safeEquals(c2))
					return false;
			}
		} else if (l1 != null ^ l2 != null)
			return false;
		return true;
	}

	// BaseURL

	public static String getBaseURL(HttpServletRequest request) {
		String baseUrl = null;
		if ((request.getServerPort() == 80) || (request.getServerPort() == 443))
			baseUrl = request.getScheme() + "://" + request.getServerName() + request.getContextPath();
		else
			baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
		return baseUrl;
	}

	// Get parametresinden gelen farklï¿½ character kodlarï¿½nï¿½ utf-8 e
	// ï¿½evirme
	public static String encodeGetParamsToUTF8(String param) {
		String result = param;
		try {
			if (param != null)
				result = new String(param.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	// Subdomain bulma
	public static String getSubdomainName(HttpServletRequest request) {
		/*
		 * String scheme = request.getScheme(); // http String serverName =
		 * request.getServerName(); // hostname.com int serverPort =
		 * request.getServerPort(); // 80 String contextPath =
		 * request.getContextPath(); // /mywebapp String servletPath =
		 * request.getServletPath(); // /servlet/MyServlet String pathInfo =
		 * request.getPathInfo(); // /a/b;c=123 String queryString =
		 * request.getQueryString(); // d=789
		 */
		String subdomain = "";
		try {
			String rawUrl = request.getServerName();
			if (InternetDomainName.isValid(rawUrl)) {
				if (InternetDomainName.from(rawUrl).hasPublicSuffix()) {
					String topDomain = InternetDomainName.from(rawUrl).topPrivateDomain().toString();
					if (topDomain.compareTo(rawUrl) != 0) {
						if (rawUrl.replace("." + topDomain, "").compareTo("www") != 0) {
							subdomain = rawUrl.replace("." + topDomain, "");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return subdomain;
	}

	public static int uTime2Millis(String alarmTm) {
		if (GenericUtil.isEmpty(alarmTm))
			return 0;
		if (!alarmTm.contains(":"))
			return 0;
		String[] a = alarmTm.split(":");
		return GenericUtil.uInt(a[0]) * 60 * 60 * 1000 + GenericUtil.uInt(a[1]) * 60 * 1000;
	}

	public static Date uUnixTime2Date(long unixTime) {
		Date date = new Date();
		date.setTime((long) unixTime * 1000);
		return date;
	}

	public static int getSafeSize(Object o) {
		if (o == null)
			return 0;
		if (o instanceof Map)
			return ((Map) o).size();
		if (o instanceof List)
			return ((List) o).size();
		if (o instanceof String)
			return ((String) o).length();
		if (o instanceof Set)
			return ((Set) o).size();
		return 0;
	}

	/*
	 * public static String encodeStr(String str) throws
	 * UnsupportedEncodingException{ String encodedStr =
	 * Base64.encodeBase64String(str.getBytes("UTF8")); return encodedStr; }
	 */
	public static String decodeStr(String str) throws UnsupportedEncodingException {
		byte[] decodedByte = Base64.getDecoder().decode(str.getBytes());
		String decodedStr = new String(decodedByte, "UTF8");
		return decodedStr;
	}

	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				System.out.println("Directory copied from " + src + "  to " + dest);
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			System.out.println("File copied from " + src + " to " + dest);
		}
	}

	public static String smsCodeGenerator(int type, int length) {
		String code = "";
		String[] arr;
		switch (type) {
		case 1: // Nï¿½merik
			arr = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
			break;
		case 2: // Alfa Nï¿½merik
			arr = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G",
					"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "W", "V", "X", "Y", "Z" };
			break;
		default:
			arr = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
			break;
		}

		Random r;
		try {
			r = SecureRandom.getInstance("SHA1PRNG");
			for (int i = 0; i < length; i++) {
				int rand_number = r.nextInt(arr.length - 1);
				code += arr[rand_number];
			}
		} catch (NoSuchAlgorithmException nsae) {
			code = "123456";
		}

		return code;
	}

	public static String uStrMax(String s, int maxLength) {
		if (s == null)
			return "";
		return s.length() > maxLength ? (s.substring(0, maxLength - 3) + "...") : s;
	}

	public static String addUniqueValToStr(String source, String add, String delimeter) {
		if (isEmpty(source))
			return add;
		if (isEmpty(add))
			return source;
		if (hasPartInside(source, add))
			return source;
		else
			return source + delimeter + add;
	}

	public static String uCoalesce(String x, String defaultValue) {
		if (x.isEmpty())
			return defaultValue;
		else
			return x;
	}

	public static int getGlobalNextval(String seq, String projectUuid, int userId, int customizationId) {
		String vcsUrl = FrameworkCache.getAppSettingStringValue(0, "vcs_url");
		// String vcsClientKey = FrameworkCache.getAppSettingStringValue(0,
		// "vcs_client_key");
		if (GenericUtil.isEmpty(vcsUrl))
			throw new IWBException("framework", "vcs_url OR vcs_client_key not defined for versioning", 0,
					"vcs_url not defined for versioning", null, null);
		String s = HttpUtil.send(vcsUrl + "/ajaxGlobalNextVal",
				"id=" + seq + "&key=" + customizationId + "." + userId + "." + projectUuid);
		JSONObject json;
		if (s != null)
			try {
				json = new JSONObject(s);
				boolean b = json.getBoolean("success") && json.has("val");
				if (b)
					return json.getInt("val");
				if (json.has("errorType") && json.has("error"))
					try {
						throw new IWBException(json.getString("errorType"), "Global Nextval: remote Error", 0, null,
								json.getString("error"), null);
					} catch (JSONException e) {
						throw new IWBException("framework", "Global Nextval", 0, "JSONException", e.getMessage(), null);
					}
				else
					throw new IWBException("framework", "Global Nextval: remote Error", 0, s, "Unknown Error", null);
			} catch (JSONException e) {
				throw new IWBException("framework", "Global Nextval", 0, "JSONException", e.getMessage(), e.getCause());
			}
		else
			throw new IWBException("framework", "Global Nextval", 0, "JSONException", "No Response from Server", null);
	}

	public static JSONObject fromMapToJSONObject(Map o) throws JSONException {
		JSONObject jo = new JSONObject();
		if (isEmpty(o))
			return jo;
		for (Object k : o.keySet())
			jo.put(k.toString(), o.get(k));
		return jo;
	}

	public static Map fromJSONObjectToMap(JSONObject o) throws JSONException {
		if (o == null)
			return null;
		Map no = new HashMap();
		for (Iterator<String> it = o.keys(); it.hasNext();) {
			String key = it.next();
			Object val = o.get(key);
			if (val == null) {
				no.put(key, null);
			} else if (val instanceof JSONObject) {
				no.put(key, fromJSONObjectToMap((JSONObject) val));
			} else if (val instanceof JSONArray) {
				no.put(key, fromJSONArrayToList((JSONArray) val));
			} else if(val instanceof Integer || val instanceof Float || val instanceof Double) {
				no.put(key, val);
			} else
				no.put(key, val.toString());
		}
		return no;
	}


	public static List fromJSONArrayToList(JSONArray o) throws JSONException {
		// return null;
		if (o == null)
			return null;
		List no = new ArrayList(o.length());
		for (int qi = 0; qi < o.length(); qi++) {
			Object val = o.get(qi);
			if (val == null) {
				no.add(val);
			} else if (val instanceof JSONObject) {
				no.add(fromJSONObjectToMap((JSONObject) val));
			} else if (val instanceof JSONArray) {
				no.add(fromJSONArrayToList((JSONArray) val));
			} else
				no.add(val.toString());
		}
		return no;
	}

	public static boolean hasCustomization(List<W5TableParam> paramList) {
		if (paramList.size() < 2)
			return false;
		for (int qi = 1; qi < paramList.size(); qi++)
			if (paramList.get(qi).getExpressionDsc().equals("customization_id")) {
				return true;
			}
		return false;
	}

	public static String fileAttachSubFolderName() {
		Date today = Calendar.getInstance().getTime();
		String dt = new SimpleDateFormat("MMyyyy").format(today);
		return dt;
	}

	public static String fileAttachPath(int customizationId, String subFolder) {
		String path = FrameworkCache.getAppSettingStringValue(customizationId + "", "file_local_path") + File.separator
				+ customizationId + File.separator + "attachment";
		if (!isEmpty(subFolder))
			path = path + File.separator + subFolder;
		return path;
	}


	/*
	 * public static String uStr2English(String source) { StringBuilder sb = new
	 * StringBuilder(); int n = source==null ? 0 : source.length(); for (int j =
	 * 0; j < n; j++){ char x = source.charAt(j); switch(x){ case
	 * 'Ã§':sb.append('c');break; case 'Ã‡':sb.append('C');break; case
	 * 'ÅŸ':sb.append('s');break; case 'Å�':sb.append('S');break; case
	 * 'Ã¼':sb.append('u');break; case 'Ãœ':sb.append('Ãœ');break; case
	 * 'Ã¶':sb.append('o');break; case 'Ã–':sb.append('O');break; case
	 * 'Ä±':sb.append('i');break; case 'Ä°':sb.append('I');break; case
	 * 'ÄŸ':sb.append('g');break; case 'Ä�':sb.append('G');break;
	 * default:sb.append(x); } } return sb.toString(); }
	 */

	public static String uStr2Alpha(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			if ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x >= '0' && x <= '9') || x == '_')
				sb.append(x);
		}
		return sb.toString();
	}

	public static String uStr2Alpha2(String source, String prefixIfError) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			if ((x >= 'a' && x <= 'z') || (x >= 'A' && x <= 'Z') || (x >= '0' && x <= '9') || x == '_')
				sb.append(x);
			else if (x == ' ')
				sb.append('_');
		}
		char x = source.charAt(0);
		if (!isEmpty(prefixIfError) && !(x >= 'a' && x <= 'z') && !(x >= 'A' && x <= 'Z'))
			sb.insert(0, prefixIfError);
		return sb.toString();
	}

	public static Object strToSoap(String source) {
		StringBuilder sb = new StringBuilder();
		int n = source == null ? 0 : source.length();
		for (int j = 0; j < n; j++) {
			char x = source.charAt(j);
			switch (x) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&gt;");
				break;
			case '>':
				sb.append("&lt;");
				break;
			default:
				sb.append(x);
			}
		}
		return sb.toString();
	}

	public static String fromMapToURI(Map s) {
		if (s == null || s.isEmpty())
			return "";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		for (Object q : s.keySet()) try{
			if (b)
				html.append("&");
			else
				b = true;
			Object o = s.get(q);
			if (o != null) {
				if(o instanceof List) {
					String ss =o.toString(); 
					o = ss.substring(1,ss.length()-1);
				}
				html.append(q).append("=").append(URLEncoder.encode(o.toString(),"UTF-8"));
			}
		} catch(Exception ee) {}
		return html.toString();
	}

	public static Object getSafeObject(JSONObject prj, String key) {
		if (prj == null || !prj.has(key))
			return null;
		try {
			return prj.get(key);
		} catch (JSONException e) {
			return null;
		}
	}

	public static String getRenderer(Object renderer) {
		return new String[] { "0", "ext3_4", "webix3_3", "open1_4", "webix4_2", "react16", "vue2" }[uInt(renderer)];
	}

	public static String fromMapToYamlString2Recursive(Map s, int level) {// TODO
																			// Auto-generated
																			// method
																			// stub
		if (s == null || s.isEmpty())
			return "";
		StringBuilder html = new StringBuilder();
		boolean b = false;
		for (Object q : s.keySet()) {
			if (b)
				html.append("\n");
			else
				b = true;
			for (int ti = 0; ti < level; ti++)
				html.append("  ");
			Object o = s.get(q);
			if (o == null)
				html.append(q).append(":");
			else /*
					 * if (o instanceof JSONObject)
					 * html.append("\"").append(q).append(": "
					 * ).append(((JSONObject) o).toString()); else if (o
					 * instanceof JSONArray) html.append("\"").append(q).append(
					 * ": ").append(((JSONArray) o).toString()); else
					 */if (o instanceof Map)
				html.append(q).append(":\n").append(fromMapToYamlString2Recursive((Map<String, Object>) o, level + 1));
			else if (o instanceof List) {
				html.append(q).append(":\n").append(fromListToYamlString2Recursive((List<Object>) o, level + 1));
			} else if (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean
					|| o instanceof Short || o instanceof Long || o instanceof Float)
				html.append(q).append(": ").append(o);
			else /*
					 * if (o instanceof NativeObject) html.append(q) .append(
					 * ": ") .append(fromNativeObjectToJsonString2Recursive((
					 * NativeObject) o)); else if (o instanceof NativeArray) {
					 * html.append(q) .append(": ")
					 * .append(fromNativeArrayToJsonString2Recursive((
					 * NativeArray) o)); } else
					 */
				html.append(q).append(": ").append((o.toString()));// stringToJS2
		}
		return html.toString();

	}

	private static Object fromListToYamlString2Recursive(List<Object> s, int level) {
		if (isEmpty(s))
			return "";
		StringBuilder str = new StringBuilder();
		boolean b = false;
		for (Object o : s) {

			StringBuilder html = new StringBuilder();

			if (o == null)
				html.append("");
			else if (o instanceof Map)
				html.append(fromMapToYamlString2Recursive((Map<String, Object>) o, level));
			else if (o instanceof List) {
				html.append(fromListToYamlString2Recursive((List<Object>) o, level + 1));
			} else if (o instanceof Object[]) {
				List lx = new ArrayList(((Object[]) o).length);
				for (int qi = 0; qi < ((Object[]) o).length; qi++)
					lx.add(((Object[]) o)[qi]);
				html.append(fromListToYamlString2Recursive(lx, level + 1));
			} else {
				for (int ti = 0; ti < level; ti++)
					html.append("  ");
				if (o instanceof Integer || o instanceof Double || o instanceof BigDecimal || o instanceof Boolean)
					html.append(o);
				else
					html.append(stringToJS2(o.toString()));
			}

			if (b)
				str.append("\n");
			else
				b = true;

			String newStr = "";
			for (int ti = 0; ti < level - 1; ti++)
				newStr += "  ";
			newStr += "- ";
			html = html.replace(0, 2 * level, newStr);

			str.append(html);
		}
		return str.toString();
	}
	private static MessageDigest messageDigest;
	
	public static String getMd5Hash(String s) {
		if(messageDigest==null) try{
			messageDigest = MessageDigest.getInstance("MD5");
		} catch(Exception e) {}
		try{
		byte[] decodedByte =messageDigest.digest(s.getBytes("UTF-8"));
		StringBuilder stringBuffer = new StringBuilder();
        for (byte bytes : decodedByte) {
            stringBuffer.append(String.format("%02x", bytes & 0xff));
        }
		return stringBuffer.toString();
		} catch(Exception e) {}
		return "error";
	}

	public static String getTransactionId() {
		return UUID.randomUUID().toString();
	}
	
	public static boolean accessControl4SessionField(Map<String, Object> scd, String relatedSessionField) {
		if(isEmpty(relatedSessionField))return true;
		boolean not = false;
		if(relatedSessionField.charAt(0)=='!') {
			not = true;
			relatedSessionField = relatedSessionField.substring(1);
		}
		int val = relatedSessionField.startsWith("app.") ? FrameworkCache.getAppSettingIntValue(scd, relatedSessionField.substring(4)) : uInt(scd.get(relatedSessionField));
		return not ? val==0 : val!=0;
		
	}

	private static Pattern alphaPattern = Pattern.compile("^[a-zA-Z]*$");
	private static Pattern alphanumPattern = Pattern.compile("^[a-zA-Z0-9_]*$");
	private static Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	private static Pattern urlPattern = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	private static Pattern ibanPattern = Pattern.compile("\b[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?!(?:[ ]?[0-9]){3})(?:[ ]?[0-9]{1,2})?\b"); 
	
	public static boolean validateVtype(String str, String vType) {
		if(isEmpty(vType))return true;
		if(vType.equals("alpha"))return alphaPattern.matcher(str).find();
		if(vType.equals("alphanum"))return alphanumPattern.matcher(str).find();
		if(vType.equals("email"))return emailPattern.matcher(str).find();
		if(vType.equals("url"))return urlPattern.matcher(str).matches();
		if(vType.equals("iban"))return ibanPattern.matcher(str).matches();
		return true;
	}
}
