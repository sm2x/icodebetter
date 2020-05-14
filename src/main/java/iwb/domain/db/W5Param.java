package iwb.domain.db;

import java.math.BigDecimal;



public interface W5Param  {


	public String getDsc();

	public short getParamType();
	
	public short getNotNullFlag();
	
	public short getSourceType();
	

	public String getDefaultValue();
	
	public BigDecimal getMinValue();

	public BigDecimal getMaxValue();

	public Short getMinLength();
	
	public Integer getMaxLength();
	
	public short getTabOrder();
	
	public int getParentId();
	
}
