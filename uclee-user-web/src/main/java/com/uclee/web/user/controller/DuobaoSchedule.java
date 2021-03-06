package com.uclee.web.user.controller;

import com.alibaba.fastjson.JSON;
import com.uclee.datasource.service.DataSourceInfoServiceI;
import com.uclee.date.util.DateUtils;
import com.uclee.dynamicDatasource.DataSourceFacade;
import com.uclee.fundation.data.mybatis.mapping.VarMapper;
import com.uclee.fundation.data.mybatis.model.DataSourceInfo;
import com.uclee.fundation.data.mybatis.model.Var;
import com.uclee.user.service.DuobaoServiceI;
import com.uclee.user.service.UserServiceI;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Configurable
@EnableScheduling
public class DuobaoSchedule {
	
	private static final Logger logger = Logger.getLogger(DuobaoSchedule.class);
	
	@Autowired
	private UserServiceI userService;
	@Autowired
	private DuobaoServiceI duobaoService;
	@Autowired
	private VarMapper varMapper;
	@Autowired
	private DataSourceFacade dataSource;
	@Autowired
	private DataSourceInfoServiceI dataSourceInfoService;

	/*@Scheduled(cron="0 0 0 * * *")
	private void updateWXInfo(){
		userService.updateWXInfo();
	}*/
	
	@Scheduled(fixedRate = 1000 * 10)
	private void refreshWXToken(){
		dataSource.switchDataSource("master");
		List<DataSourceInfo> t = dataSourceInfoService.getAllDataSourceInfo();
		logger.info("t: " + JSON.toJSONString(t));
		for(DataSourceInfo info:t) {
			if(!info.getMerchantCode().equals("master")) {
				dataSource.switchDataSource(info.getMerchantCode());
				Var var = varMapper.selectByPrimaryKey(new Integer(1));
				logger.info(JSON.toJSONString(var));
				if (DateUtils.addSecond(var.getStorageTime(), 7200).before(new Date())) {
					logger.info("更新微信Token");
					duobaoService.getGolbalAccessToken();
				}
			}
		}
	}
}
