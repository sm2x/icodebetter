package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="w5_excel_import_sheet_data",schema="iwb")
public class W5ExcelImportSheetData implements java.io.Serializable {
/*TABLE_ID: 669*/

	private static final long serialVersionUID = 76762342342341L;
	private int excelImportSheetDataId;
	private int excelImportSheetId;
	private int rowNo;
	private String a;
	private String b;
	private String c;
	private String d;
	private String e;
	private String f;
	private String g;
	private String h;
	private String i;
	private String j;
	private String k;
	private String l;
	private String m;
	private String n;
	private String o;
	private String p;
	private String q;
	private String r;
	private String s;
	private String t;
	private String u;
	private String v;	
	private String w;
	private String x;
	private String y;
	private String z;

	private String a2;
	private String b2;
	private String c2;
	private String d2;
	private String e2;
	private String f2;
	private String g2;
	private String h2;
	private String i2;
	private String j2;
	private String k2;
	private String l2;
	private String m2;
	private String n2;
	private String o2;
	private String p2;
	private String q2;
	private String r2;
	private String s2;
	private String t2;
	private String u2;
	private String v2;	
	private String w2;
	private String x2;
	private String y2;
	private String z2;
	
	
	@SequenceGenerator(name="sex_excel_import_sheet_data",sequenceName="iwb.seq_excel_import_sheet_data",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_excel_import_sheet_data")
	@Column(name="excel_import_sheet_data_id")
	public int getExcelImportSheetDataId() {
		return excelImportSheetDataId;
	}



	public void setExcelImportSheetDataId(int excelImportSheetDataId) {
		this.excelImportSheetDataId = excelImportSheetDataId;
	}



	@Column(name="excel_import_sheet_id")
	public int getExcelImportSheetId() {
		return excelImportSheetId;
	}



	public void setExcelImportSheetId(int excelImportSheetId) {
		this.excelImportSheetId = excelImportSheetId;
	}



	public void setCell(String column, String val){
		switch(column){
		case "A":a=val;break;
		case "B":b=val;break;
		case "C":c=val;break;
		case "D":d=val;break;
		case "E":e=val;break;
		case "F":f=val;break;
		case "G":g=val;break;
		case "H":h=val;break;
		case "I":i=val;break;
		case "J":j=val;break;
		case "K":k=val;break;
		case "L":l=val;break;
		case "M":m=val;break;
		case "N":n=val;break;
		case "O":o=val;break;
		case "P":p=val;break;
		case "Q":q=val;break;
		case "R":r=val;break;
		case "S":s=val;break;
		case "T":t=val;break;
		case "U":u=val;break;
		case "V":v=val;break;
		case "W":w=val;break;
		case "X":x=val;break;
		case "Y":y=val;break;
		case "Z":z=val;break;
		case "AA":a2=val;break;
		case "AB":b2=val;break;
		case "AC":c2=val;break;
		case "AD":d2=val;break;
		case "AE":e2=val;break;
		case "AF":f2=val;break;
		case "AG":g2=val;break;
		case "AH":h2=val;break;
		case "AI":i2=val;break;
		case "AJ":j2=val;break;
		case "AK":k2=val;break;
		case "AL":l2=val;break;
		case "AM":m2=val;break;
		case "AN":n2=val;break;
		case "AO":o2=val;break;
		case "AP":p2=val;break;
		case "AQ":q2=val;break;
		case "AR":r2=val;break;
		case "AS":s2=val;break;
		case "AT":t2=val;break;
		case "AU":u2=val;break;
		case "AV":v2=val;break;
		case "AW":w2=val;break;
		case "AX":x2=val;break;
		case "AY":y2=val;break;
		case "AZ":z2=val;break;
		}
	}

	
		
	@Column(name="row_no")
	public int getRowNo() {
		return rowNo;
	}

	public void setRowNo(int rowNo) {
		this.rowNo = rowNo;
	}

	
	@Column(name="a")
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	@Column(name="b")
	public String getB() {
		return b;
	}
	public void setB(String b) {
		this.b = b;
	}
	@Column(name="c")
	public String getC() {
		return c;
	}
	public void setC(String c) {
		this.c = c;
	}
	@Column(name="d")
	public String getD() {
		return d;
	}
	public void setD(String d) {
		this.d = d;
	}
	@Column(name="e")
	public String getE() {
		return e;
	}
	public void setE(String e) {
		this.e = e;
	}
	@Column(name="f")
	public String getF() {
		return f;
	}
	public void setF(String f) {
		this.f = f;
	}
	@Column(name="g")
	public String getG() {
		return g;
	}
	public void setG(String g) {
		this.g = g;
	}
	@Column(name="h")
	public String getH() {
		return h;
	}
	public void setH(String h) {
		this.h = h;
	}
	@Column(name="i")
	public String getI() {
		return i;
	}
	public void setI(String i) {
		this.i = i;
	}
	@Column(name="j")
	public String getJ() {
		return j;
	}
	public void setJ(String j) {
		this.j = j;
	}
	@Column(name="k")
	public String getK() {
		return k;
	}
	public void setK(String k) {
		this.k = k;
	}
	@Column(name="l")
	public String getL() {
		return l;
	}
	public void setL(String l) {
		this.l = l;
	}
	@Column(name="m")
	public String getM() {
		return m;
	}
	public void setM(String m) {
		this.m = m;
	}
	@Column(name="n")
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	@Column(name="o")
	public String getO() {
		return o;
	}
	public void setO(String o) {
		this.o = o;
	}	
	@Column(name="p")
	public String getP() {
		return p;
	}
	public void setP(String p) {
		this.p = p;
	}
	@Column(name="q")
	public String getQ() {
		return q;
	}
	public void setQ(String q) {
		this.q = q;
	}
	@Column(name="r")
	public String getR() {
		return r;
	}
	public void setR(String r) {
		this.r = r;
	}
	@Column(name="s")
	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	@Column(name="t")
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	@Column(name="u")
	public String getU() {
		return u;
	}
	public void setU(String u) {
		this.u = u;
	}
	@Column(name="v")
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	@Column(name="w")
	public String getW() {
		return w;
	}
	public void setW(String w) {
		this.w = w;
	}
	@Column(name="x")
	public String getX() {
		return x;
	}
	public void setX(String x) {
		this.x = x;
	}
	@Column(name="y")
	public String getY() {
		return y;
	}
	public void setY(String y) {
		this.y = y;
	}
	@Column(name="z")
	public String getZ() {
		return z;
	}
	public void setZ(String z) {
		this.z = z;
	}



	public String getA2() {
		return a2;
	}



	public void setA2(String a2) {
		this.a2 = a2;
	}



	public String getB2() {
		return b2;
	}



	public void setB2(String b2) {
		this.b2 = b2;
	}



	public String getC2() {
		return c2;
	}



	public void setC2(String c2) {
		this.c2 = c2;
	}



	public String getD2() {
		return d2;
	}



	public void setD2(String d2) {
		this.d2 = d2;
	}



	public String getE2() {
		return e2;
	}



	public void setE2(String e2) {
		this.e2 = e2;
	}



	public String getF2() {
		return f2;
	}



	public void setF2(String f2) {
		this.f2 = f2;
	}



	public String getG2() {
		return g2;
	}



	public void setG2(String g2) {
		this.g2 = g2;
	}



	public String getH2() {
		return h2;
	}



	public void setH2(String h2) {
		this.h2 = h2;
	}



	public String getI2() {
		return i2;
	}



	public void setI2(String i2) {
		this.i2 = i2;
	}



	public String getJ2() {
		return j2;
	}



	public void setJ2(String j2) {
		this.j2 = j2;
	}



	public String getK2() {
		return k2;
	}



	public void setK2(String k2) {
		this.k2 = k2;
	}



	public String getL2() {
		return l2;
	}



	public void setL2(String l2) {
		this.l2 = l2;
	}



	public String getM2() {
		return m2;
	}



	public void setM2(String m2) {
		this.m2 = m2;
	}



	public String getN2() {
		return n2;
	}



	public void setN2(String n2) {
		this.n2 = n2;
	}



	public String getO2() {
		return o2;
	}



	public void setO2(String o2) {
		this.o2 = o2;
	}



	public String getP2() {
		return p2;
	}



	public void setP2(String p2) {
		this.p2 = p2;
	}



	public String getQ2() {
		return q2;
	}



	public void setQ2(String q2) {
		this.q2 = q2;
	}



	public String getR2() {
		return r2;
	}



	public void setR2(String r2) {
		this.r2 = r2;
	}



	public String getS2() {
		return s2;
	}



	public void setS2(String s2) {
		this.s2 = s2;
	}



	public String getT2() {
		return t2;
	}



	public void setT2(String t2) {
		this.t2 = t2;
	}



	public String getU2() {
		return u2;
	}



	public void setU2(String u2) {
		this.u2 = u2;
	}



	public String getV2() {
		return v2;
	}



	public void setV2(String v2) {
		this.v2 = v2;
	}



	public String getW2() {
		return w2;
	}



	public void setW2(String w2) {
		this.w2 = w2;
	}



	public String getX2() {
		return x2;
	}



	public void setX2(String x2) {
		this.x2 = x2;
	}



	public String getY2() {
		return y2;
	}



	public void setY2(String y2) {
		this.y2 = y2;
	}



	public String getZ2() {
		return z2;
	}



	public void setZ2(String z2) {
		this.z2 = z2;
	}

	
	
	private String projectUuid;

	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}	
}
