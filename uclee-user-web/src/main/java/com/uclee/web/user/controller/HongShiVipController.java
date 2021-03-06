package com.uclee.web.user.controller;

import com.alibaba.fastjson.JSON;
import com.uclee.fundation.config.links.GlobalSessionConstant;
import com.uclee.fundation.data.mybatis.model.HongShiRechargeRecord;
import com.uclee.fundation.data.mybatis.model.HongShiVip;
import com.uclee.fundation.data.mybatis.model.OauthLogin;
import com.uclee.fundation.data.mybatis.model.UserProfile;
import com.uclee.hongshi.service.HongShiVipServiceI;
import com.uclee.sms.util.VerifyCode;
import com.uclee.user.service.UserServiceI;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Administrator
 * 洪石会员相关接口
 *
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("/uclee-user-web/")
public class HongShiVipController {
	@Autowired
	private UserServiceI userService;
	
	@Autowired
	private HongShiVipServiceI hongShiVipService;
	
	private static final Logger logger = Logger.getLogger(HongShiVipController.class);
	
	
	
	/** 
	* @Title: getVipInfo 
	* @Description: 调用存储过程得到会员信息
	* @param @param type
	* @param @param session
	* @param @return    设定文件 
	* @return HongShiVip    返回类型 
	* @throws 
	*/
	@RequestMapping("getVipInfo")
	public @ResponseBody HongShiVip  getVipInfo(Integer type,HttpSession session ){
		Integer userId = (Integer)session.getAttribute(GlobalSessionConstant.USER_ID);
		UserProfile userProfile = userService.getBasicUserProfile(userId);
		logger.info("user_id:"+userId);
		if(userId!=null){
			OauthLogin tt = userService.getOauthLoginInfoByUserId(userId);
			if(tt!=null){
				List<HongShiVip> ret= hongShiVipService.getVipInfo(tt.getOauthId());//openid 去拿信息
				if(ret!=null&&ret.size()>0){
					if(userProfile!=null){
						if(userProfile.getVipImage()!=null&&userProfile.getVipImage().length()>2){
							ret.get(0).setVipImage(userProfile.getVipImage());
						}else{
							ret.get(0).setVipImage(userService.getVipImage(tt.getOauthId(),userId));
						}
						/*if(userProfile.getVipJbarcode()!=null&&userProfile.getVipJbarcode().length()>2){
							ret.setVipJbarcode(userProfile.getVipJbarcode());
						}else{
							ret.setVipJbarcode(userService.getVipJbarcode(tt.getOauthId(),userId));
						}*/
					}
					return ret.get(0);
				}
			}
		}
		
		return new HongShiVip();
	}
	
	/** 
	* @Title: addVipInfo 
	* @Description: 绑定会员卡处理
	* @param @param vip post的会员信息数据
	* @param @param session
	* @param @return    设定文件 
	* @return Map<String,Object>    返回类型 
	* @throws 
	*/
	@RequestMapping("addVipInfo")
	public @ResponseBody Map<String,Object>  addVipInfo(@RequestBody HongShiVip vip,HttpSession session ){
		Map<String,Object> ret=new HashMap<String,Object>();
		ret.put("result", "fail");
		Integer userId = (Integer)session.getAttribute(GlobalSessionConstant.USER_ID);
		logger.info("user_id:"+userId);
		if(vip==null){
			ret.put("reason", "没传数据");
			return ret;
		}
		if(vip.getcMobileNumber()==null||vip.getcMobileNumber().isEmpty()){
			if(vip.getCode()==null||vip.getCode().isEmpty()){
				if(!VerifyCode.checkVerifyCode(session,vip.getcMobileNumber(),vip.getCode())){
					ret.put("reason", "验证码错误");
					return ret;
				}
			}else {
				ret.put("reason", "无输入验证码");
				return ret;
			}

		}
		if(userId!=null){
			OauthLogin tt = userService.getOauthLoginInfoByUserId(userId);
			if(tt!=null){
				vip.setcWeiXinCode(tt.getOauthId());
				Integer res=hongShiVipService.addHongShiVipInfo(vip);
				logger.info("addVipInfo res:"+res);
				if(res!=null&&res!=0){
					if(res==-1){
						ret.put("reason", "该手机号已绑定其他会员");
					}else if(res==-2){
						ret.put("reason", "该手机号没绑定线下会员卡");
					}else if(res==-3){
						ret.put("reason", "该会员卡已停用");
					}else if(res==-201){
						ret.put("reason", "该手机号存在多张卡");
					}else{
						ret.put("reason", "网络繁忙，请稍后重试");
					}
					return ret;
				}
				ret.put("result", "success");
			}
		}
		logger.info("rec:"+JSON.toJSONString(vip));
		
		return ret;
	}
	
	
	/** 
	* @Title: rechargeRecord 
	* @Description: 会员消费明细，对应getviplog存储过程 
	* @param @param session
	* @param @return    设定文件 
	* @return List<HongShiRechargeRecord>    返回类型 
	* @throws 
	*/
	@RequestMapping("rechargeRecord")
	public @ResponseBody List<HongShiRechargeRecord>  rechargeRecord(HttpSession session ){
		List<HongShiRechargeRecord> ret=new ArrayList<HongShiRechargeRecord>();
		
		Integer userId = (Integer)session.getAttribute(GlobalSessionConstant.USER_ID);
		logger.info("user_id:"+userId);
		if(userId!=null){
			OauthLogin tt = userService.getOauthLoginInfoByUserId(userId);
			if(tt!=null){
				List<HongShiVip> vip= hongShiVipService.getVipInfo(tt.getOauthId());//openid 去拿信息
				if(vip!=null&&vip.size()>0){
					ret= hongShiVipService.getRechargeRecord(vip.get(0).getId());
					for(HongShiRechargeRecord record:ret){
						if(record.getSource().equals("订单")){
							record.setLogType(1);
						}else if(record.getSource().equals("充值")){
							record.setLogType(2);
						}else if(record.getSource().equals("签到送积分")){
							record.setLogType(3);
						}else{
							record.setLogType(4);
						}
					}
				}
			}
		}
		return ret;
	}
}
