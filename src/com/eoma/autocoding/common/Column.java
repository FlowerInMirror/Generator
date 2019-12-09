package com.eoma.autocoding.common;

public class Column {
    private boolean keyFlag;
    private boolean convert;
	private String name;
	private String dbName;
	private String label;
	private String type;
	private String dbType;
	private Integer length;
	private Boolean isStr = false;
	private Boolean end = true;
	private Boolean nullable;
	private Integer decimalDigits;
	
	
	public boolean isConvert() {
		return convert;
	}

	public void setConvert(boolean convert) {
		this.convert = convert;
	}

	public Boolean getStr() {
		return isStr;
	}

	public void setStr(Boolean str) {
		isStr = str;
	}

	public boolean isKeyFlag() {
		return keyFlag;
	}

	public void setKeyFlag(boolean keyFlag) {
		this.keyFlag = keyFlag;
	}

	public String getName() {
		return name;
	}
	
	public String getNameUpper() {
		return name.replaceFirst(name.substring(0, 1), name.substring(0, 1).toUpperCase());
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}

	public Boolean getEnd() {
		return end;
	}

	public void setEnd(Boolean end) {
		this.end = end;
	}

	public Boolean getNullable() {
		return nullable;
	}
	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}
	public Integer getDecimalDigits() {
		return decimalDigits;
	}
	public void setDecimalDigits(Integer decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
}
