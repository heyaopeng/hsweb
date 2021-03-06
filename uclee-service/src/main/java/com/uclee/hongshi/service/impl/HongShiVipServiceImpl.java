package com.uclee.hongshi.service.impl;

import com.uclee.fundation.data.mybatis.mapping.HongShiVipMapper;
import com.uclee.fundation.data.mybatis.model.HongShiRecharge;
import com.uclee.fundation.data.mybatis.model.HongShiRechargeRecord;
import com.uclee.fundation.data.mybatis.model.HongShiVip;
import com.uclee.hongshi.service.HongShiVipServiceI;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class HongShiVipServiceImpl implements HongShiVipServiceI{
	
	@Autowired
	private HongShiVipMapper hongShiVipMapper;

	@Override
	public Integer addHongShiVipInfo(HongShiVip params) {
		return hongShiVipMapper.addVipInfo(params);
	}

	@Override
	public List<HongShiRechargeRecord> getRechargeRecord(Integer iVipID) {
		return hongShiVipMapper.getVipRechargeLog(iVipID);
	}

	@Override
	public List<HongShiVip> getVipInfo(String cWeiXinCode) {
		return hongShiVipMapper.getVipInfo(cWeiXinCode);
	}

	@Override
	public Integer hongShiRecharge(HongShiRecharge params) {
		hongShiVipMapper.hongShiRecharge(params);
		return 1;
	}
}
