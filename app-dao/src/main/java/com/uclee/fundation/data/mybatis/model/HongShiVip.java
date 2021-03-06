package com.uclee.fundation.data.mybatis.model;

import java.math.BigDecimal;

public class HongShiVip {
	
	private Integer id;
	
	private String cWeiXinCode;
	
	private String cMobileNumber;
	
	private String cName;

	private String code;
	
	private String cBirthday;
	
	private Integer bIsLunar;
	
	private String cVipCode;
	
	private BigDecimal balance;
	
	private String vipImage;
	
	private double bonusPoints;
	
	private String vipJbarcode;
	
	public String getVipJbarcode() {
		return vipJbarcode;
	}

	public void setVipJbarcode(String vipJbarcode) {
		this.vipJbarcode = vipJbarcode;
	}

	public double getBonusPoints() {
		return bonusPoints;
	}

	public void setBonusPoints(double bonusPoints) {
		this.bonusPoints = bonusPoints;
	}

	public String getVipImage() {
		return vipImage;
	}

	public void setVipImage(String vipImage) {
		this.vipImage = vipImage;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public HongShiVip setBalance(BigDecimal balance) {
		this.balance = balance;
		return this;
	}

	public Integer getId() {
		return id;
	}

	public HongShiVip setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getcWeiXinCode() {
		return cWeiXinCode;
	}

	public HongShiVip setcWeiXinCode(String cWeiXinCode) {
		this.cWeiXinCode = cWeiXinCode;
		return this;
	}

	public String getcMobileNumber() {
		return cMobileNumber;
	}

	public HongShiVip setcMobileNumber(String cMobileNumber) {
		this.cMobileNumber = cMobileNumber;
		return this;
	}

	public String getcName() {
		return cName;
	}

	public HongShiVip setcName(String cName) {
		this.cName = cName;
		return this;
	}

	public String getcBirthday() {
		return cBirthday;
	}

	public HongShiVip setcBirthday(String cBirthday) {
		this.cBirthday = cBirthday;
		return this;
	}


	public Integer getbIsLunar() {
		return bIsLunar;
	}

	public HongShiVip setbIsLunar(Integer bIsLunar) {
		this.bIsLunar = bIsLunar;
		return this;
	}

	public String getcVipCode() {
		return cVipCode;
	}

	public HongShiVip setcVipCode(String cVipCode) {
		this.cVipCode = cVipCode;
		return this;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}