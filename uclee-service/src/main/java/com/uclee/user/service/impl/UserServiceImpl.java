package com.uclee.user.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.h2.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import com.alibaba.fastjson.JSON;
import com.backend.service.ProductManageServiceI;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.uclee.QRCode.util.BarcodeUtil;
import com.uclee.QRCode.util.MyQRCode;
import com.uclee.date.util.DateUtils;
import com.uclee.dynamicDatasource.DataSourceFacade;
import com.uclee.file.util.FileUtil;
import com.uclee.fundation.config.links.DuobaoRoleConstant;
import com.uclee.fundation.config.links.GlobalSessionConstant;
import com.uclee.fundation.config.links.WebConfig;
import com.uclee.fundation.config.links.WechatMerchantInfo;
import com.uclee.fundation.data.mybatis.mapping.BalanceLogMapper;
import com.uclee.fundation.data.mybatis.mapping.BalanceMapper;
import com.uclee.fundation.data.mybatis.mapping.CartMapper;
import com.uclee.fundation.data.mybatis.mapping.CityMapper;
import com.uclee.fundation.data.mybatis.mapping.ConfigMapper;
import com.uclee.fundation.data.mybatis.mapping.DeliverAddrMapper;
import com.uclee.fundation.data.mybatis.mapping.FinancialAccountMapper;
import com.uclee.fundation.data.mybatis.mapping.HongShiMapper;
import com.uclee.fundation.data.mybatis.mapping.LoginHistoryMapper;
import com.uclee.fundation.data.mybatis.mapping.NapaStoreMapper;
import com.uclee.fundation.data.mybatis.mapping.OauthLoginMapper;
import com.uclee.fundation.data.mybatis.mapping.OrderItemMapper;
import com.uclee.fundation.data.mybatis.mapping.OrderMapper;
import com.uclee.fundation.data.mybatis.mapping.PaymentMapper;
import com.uclee.fundation.data.mybatis.mapping.PaymentOrderMapper;
import com.uclee.fundation.data.mybatis.mapping.PermissionMapper;
import com.uclee.fundation.data.mybatis.mapping.ProductGroupLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.ProductGroupMapper;
import com.uclee.fundation.data.mybatis.mapping.ProductImageLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.ProductMapper;
import com.uclee.fundation.data.mybatis.mapping.ProductStoreLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.ProvinceMapper;
import com.uclee.fundation.data.mybatis.mapping.RegionMapper;
import com.uclee.fundation.data.mybatis.mapping.RoleMapper;
import com.uclee.fundation.data.mybatis.mapping.RolePermissionLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.SignRecordMapper;
import com.uclee.fundation.data.mybatis.mapping.SpecificationMapper;
import com.uclee.fundation.data.mybatis.mapping.SpecificationValueMapper;
import com.uclee.fundation.data.mybatis.mapping.SpecificationValueStoreLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.UserInvitedLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.UserMapper;
import com.uclee.fundation.data.mybatis.mapping.UserProfileMapper;
import com.uclee.fundation.data.mybatis.mapping.UserRoleLinkMapper;
import com.uclee.fundation.data.mybatis.mapping.VarMapper;
import com.uclee.fundation.data.mybatis.mapping.*;
import com.uclee.fundation.data.mybatis.model.*;
import com.uclee.fundation.data.web.dto.BossCenterItem;
import com.uclee.fundation.data.web.dto.CartDto;
import com.uclee.fundation.data.web.dto.OrderPost;
import com.uclee.fundation.data.web.dto.ProductDto;
import com.uclee.fundation.data.web.dto.StockPost;
import com.uclee.fundation.dfs.fastdfs.FDFSFileUpload;
import com.uclee.hongshi.service.HongShiVipServiceI;
import com.uclee.number.util.NumberUtil;
import com.uclee.payment.exception.PaymentHandlerException;
import com.uclee.payment.strategy.alipay.util.AlipayNotify;
import com.uclee.payment.strategy.alipay.util.AlipaySubmit;
import com.uclee.payment.strategy.wcPaymetnTools.*;
import com.uclee.payment.strategy.wcPaymetnTools.config.MerchantInfo;
import com.uclee.user.model.PaymentStrategyResult;
import com.uclee.user.model.UserForm;
import com.uclee.user.service.DuobaoServiceI;
import com.uclee.user.service.UserServiceI;
import com.uclee.user.util.EndecryptUtils;
import com.uclee.user.util.RandomNum;
import com.uclee.user.util.SHA256;
import com.uclee.user.util.UserUtil;
import com.uclee.userAgent.util.UserAgentUtils;
import com.uclee.weixin.util.EmojiFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.h2.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

public class UserServiceImpl implements UserServiceI {

	private static final Logger logger = Logger.getLogger(UserServiceImpl.class);
	@Autowired
	private SpecificationValueMapper specificationValueMapper;
	@Autowired
	private FreightMapper freightMapper;
	@Autowired
	private LotteryDrawConfigMapper lotteryDrawConfigMapper;
	@Autowired
	private FDFSFileUpload fDFSFileUpload;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserProfileMapper userProfileMapper;
	@Autowired
	private LoginHistoryMapper loginHistoryMapper;
	@Autowired
	private UserRoleLinkMapper userRoleLinkMapper;
	@Autowired
	private WinningRecordMapper winningRecordMapper;
	@Autowired
	private RoleMapper roleMapper;
	@Autowired
	private PermissionMapper permissionMapper;
	@Autowired
	private RolePermissionLinkMapper rolePermissionLinkMapper;
	@Autowired
	private OauthLoginMapper oauthLoginMapper;
	@Autowired
	private ProductMapper productMapper;
	@Autowired
	private ProductImageLinkMapper productImageLinkMapper;
	@Autowired
	private CityMapper cityMapper;
	@Autowired
	private RegionMapper regionMapper;
	@Autowired
	private DuobaoServiceI duobaoService;
	@Autowired
	private ProductManageServiceI productManageService;
	@Autowired
	private VarMapper varMapper;
	@Autowired
	private FinancialAccountMapper financialAccountMapper;
	@Autowired
	private UserInvitedLinkMapper userInvitedLinkMapper;
	@Autowired
	private NapaStoreMapper napaStoreMapper;
	@Autowired
	private BalanceMapper balanceMapper;
	@Autowired
	private BalanceLogMapper balanceLogMapper;
	
	@Autowired
	private HongShiMapper hongShiMapper;
	
	@Autowired
	private HongShiVipServiceI hongShiVipService;
	@Autowired
	private PaymentMapper paymentMapper;
	
	@Autowired
	private ProductStoreLinkMapper productStoreLinkMapper;
	@Autowired
	private ProvinceMapper provinceMapper;
	@Autowired
	private ProductSaleMapper productSaleMapper;
	@Autowired
	private PaymentOrderMapper paymentOrderMapper;
	@Autowired
	private DeliverAddrMapper deliverAddrMapper;
	@Autowired
	private OrderMapper orderMapper;
	@Autowired
	private OrderItemMapper orderItemMapper;
	@Autowired
	private ProductGroupMapper productGroupMapper;
	@Autowired
	private ProductGroupLinkMapper productGroupLinkMapper;
	@Autowired
	private SpecificationValueStoreLinkMapper specificationValueStoreLinkMapper;
	
	@Autowired
	private CartMapper cartMapper;
	@Autowired
	private ConfigMapper configMapper;
	@Autowired
	private SignRecordMapper signRecordMapper;
	@Autowired
	private RechargeConfigMapper rechargeConfigMapper;
	@Autowired
	private BannerMapper bannerMapper;
	
	@Autowired
	private LotteryRecordMapper lotteryRecordMapper;
	@Autowired
	private ShakeRecordMapper shakeRecordMapper;
	@Autowired
	private SpecificationMapper specificationMapper;
	@Autowired
	private DataSourceFacade datasource;
	private String alipay_notify_url = "http://hs.uclee.com/uclee-user-web/alipayNotifyHandler";
	private String alipay_return_url = "http://cooka.vicp.cc/fastpaysuccess/";

	private String wc_general_order = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	// 微信JSSDK的AccessToken请求URL地址
	final static String weixin_jssdk_acceToken_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx68abe3fb2a71dcc7&secret=00030b62032af67f83e535223616a0d6";
	// 微信JSSDK的ticket请求URL地址
	final static String weixin_jssdk_ticket_url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	public User getUserById(Integer userId) {
		return userMapper.selectByPrimaryKey(userId);
	}

	/** 
	* @Title: hasMatchUserPwd 
	* @Description: 微信登陆，暂时没用
	* @param @param user
	* @param @param inputPassword
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean hasMatchUserPwd(User user, String inputPassword) {
		logger.info("user: " + JSON.toJSONString(user));
		logger.info("inputPassword: " + JSON.toJSONString(inputPassword));
		boolean validatePwd = false;
		if (null == user || Strings.isNullOrEmpty(inputPassword) || user.getRegistTime() == null
				|| Strings.isNullOrEmpty(user.getPassword())) {
			logger.info("验证用户密码需要信息不完整\n" + JSON.toJSONString(user));
			return false;
		}
		String salt = UserUtil.getSalt(user.getRegistTime());
		validatePwd = user.getPassword().equals(SHA256.encrypt(inputPassword, salt));
		if(!validatePwd){
			logger.info("密码错误real");
			logger.info("user pass in database:"+user.getPassword());
			logger.info("user pass input:"+SHA256.encrypt(inputPassword, salt));
		}
		return validatePwd;
	}

	/** 
	* @Title: updatePassword 
	* @Description: 更新用户密码 
	* @param @param user
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean updatePassword(User user) {
		if (Strings.isNullOrEmpty(user.getPassword()) || user.getUserId() == null) {
			logger.info("密码为空或者缺少用户id");
			return false;
		}

		UserProfile userProfile = new UserProfile(user.getUserId());
		userProfile = userProfileMapper.selectByUserProfile(userProfile);
		if (userProfile == null) {
			logger.info("错误，该用户没有userProfile 记录");
			return false;
		}

		String salt = UserUtil.getSalt(userProfile.getRegistTime());
		user.setPassword(SHA256.encrypt(user.getPassword(), salt));
		userMapper.updateByPrimaryKeySelective(user);
		return true;
	}

	/** 
	* @Title: regUser 
	* @Description: 用户注册
	* @param @param userForm
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public int regUser(UserForm userForm) {
		if (userForm == null || StringUtils.isNullOrEmpty(userForm.getAccount())
				|| StringUtils.isNullOrEmpty(userForm.getPassword()) || userForm.getRoleId() == null) {
			logger.info("注册用户数据不完整" + JSON.toJSON(userForm));
			return -1;
		}

		if (isExistAccount(userForm.getAccount())) {
			logger.info("已经存在相同的账号\n" + userForm.getAccount());
			return -1;
		}

		UserProfile userProfile = new UserProfile();
		Date regTime = new Date();
		userProfile.setRegistTime(regTime);

		String salt = UserUtil.getSalt(regTime);
		User user = new User();
		// System.err.println(""userForm.getPassword());
		user.setPassword(SHA256.encrypt(userForm.getPassword(), salt));
		userMapper.insertSelective(user);

		// 插入用户信息
		userProfile.setUserId(user.getUserId());
		String account = userForm.getAccount();
		if (UserUtil.isPhone(account))
			userProfile.setPhone(account);
		else
			userProfile.setEmail(account);
		userProfileMapper.insertSelective(userProfile);

		// 为用户添加角色
		UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
		userRoleLinkKey.setRoleId(userForm.getRoleId());
		userRoleLinkKey.setUserId(user.getUserId());
		userRoleLinkMapper.insert(userRoleLinkKey);

		// 默认开通金融账号
		FinancialAccount financialAccount = new FinancialAccount();
		financialAccount.setUserId(user.getUserId());
		return user.getUserId();
	}

	/** 
	* @Title: regUserWithoutPassword 
	* @Description: 非密码注册用户，微商城没用
	* @param @param userForm
	* @param @return    设定文件  
	* @throws 
	*/
	public int regUserWithoutPassword(UserForm userForm) {
		if (userForm == null || StringUtils.isNullOrEmpty(userForm.getAccount()) || userForm.getRoleId() == null) {
			logger.info("注册用户数据不完整" + JSON.toJSON(userForm));
			return -1;
		}

		if (isExistAccount(userForm.getAccount())) {
			logger.info("已经存在相同的账号\n" + userForm.getAccount());
			return -1;
		}

		UserProfile userProfile = new UserProfile();
		Date regTime = new Date();
		userProfile.setRegistTime(regTime);

		String salt = UserUtil.getSalt(regTime);
		User user = new User();

		String account = userForm.getAccount();
		String pswd = new Sha256Hash(account + salt).toBase64().substring(0, 20);
		user.setPassword(pswd);
		userMapper.insertSelective(user);

		// 插入用户信息
		userProfile.setUserId(user.getUserId());

		if (UserUtil.isPhone(account))
			userProfile.setPhone(account);
		else
			userProfile.setEmail(account);
		userProfileMapper.insertSelective(userProfile);

		// 为用户添加角色
		UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
		userRoleLinkKey.setRoleId(userForm.getRoleId());
		userRoleLinkKey.setUserId(user.getUserId());
		userRoleLinkMapper.insert(userRoleLinkKey);

		// 默认开通金融账号
		FinancialAccount financialAccount = new FinancialAccount();
		financialAccount.setUserId(user.getUserId());
		return user.getUserId();
	}

	/** 
	* @Title: socialRegister 
	* @Description: 第三方登陆注册处理，微商城没用
	* @param @param oauthLogin
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public int socialRegister(OauthLogin oauthLogin) {
		if (oauthLogin == null || oauthLogin.getRoleId() == null || Strings.isNullOrEmpty(oauthLogin.getOauthId())
				|| Strings.isNullOrEmpty(oauthLogin.getOauthType())
				|| Strings.isNullOrEmpty(oauthLogin.getOauthExpires())
				|| Strings.isNullOrEmpty(oauthLogin.getOauthUserName())
				|| Strings.isNullOrEmpty(oauthLogin.getOauthAccessToken())
				|| Strings.isNullOrEmpty(oauthLogin.getPhone())) {
			logger.info("社会化登录信息为登陆 缺失" + JSON.toJSONString(oauthLogin));
			return -1;
		}

		if (getOauthLoginInfoByOauthId(oauthLogin.getOauthId()) != null) {
			logger.info("+++ 存在相同的oauthId");
			return -1;
		}
		User user = new User();
		String pswd = new Sha256Hash(oauthLogin.getOauthAccessToken()).toBase64().substring(0, 20);
		user.setPassword(pswd);
		userMapper.insertSelective(user);

		Integer userId = user.getUserId();

		// 为用户添加角色
		UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
		userRoleLinkKey.setRoleId(oauthLogin.getRoleId());
		userRoleLinkKey.setUserId(userId);
		userRoleLinkMapper.insert(userRoleLinkKey);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserId(user.getUserId());
		userProfile.setName(oauthLogin.getOauthUserName());
		userProfile.setPhone(oauthLogin.getPhone());
		userProfileMapper.insertSelective(userProfile);

		oauthLogin.setUserId(userId);
		oauthLogin.setOauthName(oauthLogin.getOauthType());
		oauthLoginMapper.insertSelective(oauthLogin);

		// 默认开通金融账号
		FinancialAccount financialAccount = new FinancialAccount();
		financialAccount.setUserId(userId);

		return userId;
	}

	/** 
	* @Title: isSettingPassword 
	* @Description: 判断用户是否设置了密码 ，微商城没用到
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	public boolean isSettingPassword(Integer userId) {
		User user = userMapper.selectByPrimaryKey(userId);
		logger.info(user.getPassword().length());
		return user.getPassword().length() == 20 ? false : true;
	}

	public int deleByUserId(Integer userId) {
		UserProfile up = userProfileMapper.selectByUserId(userId);
		userProfileMapper.deleteByPrimaryKey(up.getProfileId());
		return userMapper.deleteByPrimaryKey(userId);
	}

	/** 
	* @Title: addChiledUser 
	* @Description: 添加子用户，微商城没用到 
	* @param @param userForm
	* @param @return    设定文件  
	* @throws 
	*/
	public boolean addChiledUser(UserForm userForm) {
		if (userForm.getParentId() == null || userForm.getRoleId() == null
				|| Strings.isNullOrEmpty(userForm.getPassword()) || Strings.isNullOrEmpty(userForm.getEmail())
				|| Strings.isNullOrEmpty(userForm.getPhone()) || Strings.isNullOrEmpty(userForm.getName())) {
			logger.info("添加子账号 数据不完整" + JSON.toJSONString(userForm));
			return false;
		}
		User user = new User();
		user.setParentId(userForm.getParentId());
		user.setPassword(userForm.getPassword());

		UserProfile userProfile = new UserProfile();
		Date regTime = new Date();
		userProfile.setRegistTime(regTime);

		String salt = UserUtil.getSalt(regTime);
		user.setPassword(SHA256.encrypt(userForm.getPassword(), salt));
		userMapper.insertSelective(user);

		userProfile.setUserId(user.getUserId());
		userProfile.setEmail(userForm.getEmail());
		userProfile.setPhone(userForm.getPhone());
		userProfile.setName(userForm.getName());
		if (!Strings.isNullOrEmpty(userForm.getGender())) {
			userProfile.setGender(userForm.getGender());
		}
		userProfile.setIsActive(userForm.isActive());
		userProfileMapper.insertSelective(userProfile);

		// 为子账户新建角色,新角色不归属于角色管理列表
		Role role = new Role();
		role.setRole(userForm.getParentId() + "_" + user.getUserId() + "_" + userForm.getName());
		role.setIsInList(false);
		role.setParentId(-1);
		roleMapper.insertSelective(role);

		int roleId = role.getRoleId();
		// 角色权限关联
		List<Permission> permissions = permissionMapper.selectRolePermissions(userForm.getRoleId());
		addRolePermissions(permissions, roleId);

		// 为用户添加角色
		UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
		userRoleLinkKey.setRoleId(roleId);
		userRoleLinkKey.setUserId(user.getUserId());
		userRoleLinkMapper.insert(userRoleLinkKey);

		return true;
	}

    /** 
    * @Title: addPhoneUser 
    * @Description: 添加手机登陆用户，微商城没用到
    * @param @param name
    * @param @param phone
    * @param @return    设定文件  
    * @throws 
    */
    @Override
    public boolean addPhoneUser(String name, String phone) {
		try {
			UserProfile search=new UserProfile();
			search.setPhone(phone);
			UserProfile check = userProfileMapper.selectByUserProfile(search);
			if(check==null) {
				User user = new User();
				String pswd = "no password";
				user.setPassword(pswd);
				int max = 11;
				int min = 1;
				Random random = new Random();
				int randomNumber = random.nextInt(max) % (max - min + 1) + min;
				user.setSerialNum(NumberUtil.generateSerialNum(randomNumber));
				user.setParentId(0);
				userMapper.insertSelective(user);

				Integer userId = user.getUserId();

				// 为用户添加角色
				UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
				userRoleLinkKey.setRoleId(DuobaoRoleConstant.business);
				userRoleLinkKey.setUserId(userId);
				userRoleLinkMapper.insert(userRoleLinkKey);

				UserProfile userProfile = new UserProfile();
				userProfile.setUserId(user.getUserId());
				userProfile.setPhone(phone);
				userProfile.setName(name);

				userProfileMapper.insertSelective(userProfile);
				return true;
			}else{
				logger.info("already had phone:"+phone);
			}
		}catch(Exception e){
			logger.error("add user error",e);
			return false;
		}

        return false;
    }

	/** 
	* @Title: phoneUserList 
	* @Description: 老板列表，微商城后台加盟商管理列表 
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<UserProfile> phoneUserList() {
		return userProfileMapper.selectUserByRoleType(DuobaoRoleConstant.business);
	}


	/** 
	* @Title: addRolePermissions 
	* @Description: 角色权限关联，微商城暂时没用到 
	* @param @param permissions
	* @param @param roleId    设定文件 
	* @return void    返回类型 
	* @throws 
	*/
	private void addRolePermissions(List<Permission> permissions, Integer roleId) {
		if (CollectionUtils.isEmpty(permissions)) {
			logger.info("权限列表为空");
			return;
		}
		List<Permission> list = new ArrayList<Permission>();
		for (Permission per : permissions) {
			if (per.getPermissionId() != null) {
				list.add(per);
			}
		}
		Map<String, Object> map = new HashMap<>();

		map.put("permissions", list);
		map.put("roleId", roleId);
		rolePermissionLinkMapper.addRolePermissionsByList(map);
	}

	/** 
	* @Title: getSubUserList 
	* @Description: 取得子用户列表，微商城没用到 
	* @param @param parentId
	* @param @return    设定文件  
	* @throws 
	*/
	public List<UserProfile> getSubUserList(Integer parentId) {
		if (parentId == null) {
			logger.info("传入 的用户id  为空");
			return null;
		}
		return userProfileMapper.selectUserListByParentId(parentId);
	}

	/** 
	* @Title: getOauthLoginInfoByOauthId 
	* @Description: 根据第三方登陆的id，比如微信的openId获得对应的第三方登陆用户信息 
	* @param @param oauthId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public OauthLogin getOauthLoginInfoByOauthId(String oauthId) {
		return oauthLoginMapper.getOauthLoginInfoByOauthId(oauthId);
	}

	/** 
	* @Title: updateLoginUserMsg 
	* @Description: 更新用户信息，微商城没用到
	* @param @param userProfile
	* @param @param IP
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public int updateLoginUserMsg(UserProfile userProfile, String IP) {
		LoginHistory lgh = new LoginHistory();
		lgh.setUserId(userProfile.getUserId());
		lgh.setLoginIp(IP);
		lgh.setLoginTime(new Date());
		return loginHistoryMapper.insert(lgh);
	}

	/** 
	* @Title: isExistAccount 
	* @Description: 判断当前用户是否存在，微商城没用到 
	* @param @param account
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean isExistAccount(String account) {
		if (Strings.isNullOrEmpty(account)) {
			logger.info("传入的account为空");
			return false;
		}
		UserProfile userProfile = new UserProfile();
		if (UserUtil.isPhone(account))
			userProfile.setPhone(account);
		else
			userProfile.setEmail(account);

		UserProfile result = userProfileMapper.selectByUserProfile(userProfile);
		if (null == result)
			return false;
		else
			return true;
	}


	/** 
	   * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址, 
	   * 
	   * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？ 
	   * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。 
	   * 
	   * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 
	   * 192.168.1.100 
	   * 
	   * 用户真实IP为： 192.168.1.110 
	   * 
	   * @param request 
	   * @return 
	   */
	@Override
	public String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for"); 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("WL-Proxy-Client-IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("HTTP_CLIENT_IP"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
	    } 
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	      ip = request.getRemoteAddr(); 
	    } 
	    logger.info("x-forwarded-for: " + ip);
	    return ip;
	}
	
	/** 
	* @Title: getIpAddr 
	* @Description: 获取ip对应的地理位置，微商城没用到
	* @param @param request
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String getIpAddr(HttpServletRequest request) {
		String ip = getIp(request);
		JSONObject json;
		try {
			json = readJsonFromUrl("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip="+ip);
		    return (String) json.get("province") + (String) json.get("city");
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
	 
	  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	     // System.out.println("同时 从这里也能看出 即便return了，仍然会执行finally的！");
	    }
	  }
	 
	/** 
	* @Title: getUserPermissions 
	* @Description: 取得用户所拥有的权限，微商城没用到
	* @param @param userId
	* @param @return    设定文件 
	* @return List<String>    返回类型 
	* @throws 
	*/
	public List<String> getUserPermissions(Integer userId) {
		List<Role> roleList = roleMapper.selectUserRoles(userId);
		List<String> permissions = new ArrayList<String>();
		for (Role role : roleList) {
			if (role.getIsActive()) {
				List<Permission> permsList = permissionMapper.selectRolePermissions(role.getRoleId());
				for (Permission perms : permsList) {
					if (perms.getIsActive()) {
						permissions.add(perms.getPermission());
					}
				}
			} else {
				logger.info(role.getRole() + "is  not active");
			}
		}
		return removeDuplicate(permissions);
	}
	/** 
	* @Title: getUserPermissions 
	* @Description: 取得用户所拥有的权限，微商城没用到
	* @param @param userId
	* @param @return    设定文件 
	* @return List<String>    返回类型 
	* @throws 
	*/
	public List<Permission> getUserPermission(Integer userId) {
		List<Role> roleList = roleMapper.selectUserRoles(userId);
		Set<Permission> permissions = new HashSet<Permission>();
		for (Role role : roleList) {
			List<Permission> permsList = permissionMapper.selectRolePermissions(role.getRoleId());
			for (Permission perms : permsList) {
				if (perms.getIsActive()) {
					permissions.add(perms);
				}
			}
		}
		List<Permission> userPermissions = new ArrayList<Permission>();
		userPermissions.addAll(permissions);
		logger.info("用户ID为" + userId + "拥有的所有角色：" + JSON.toJSONString(roleList));
		logger.info("用户ID为" + userId + "拥有的所有权限：" + JSON.toJSONString(userPermissions));
		return userPermissions;
	}

	/** 
	* @Title: removeDuplicate 
	* @Description: 删除list里面的重复项
	* @param @param list
	* @param @return    设定文件 
	* @return List<String>    返回类型 
	* @throws 
	*/
	private List<String> removeDuplicate(List<String> list) {
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);
		return list;
	}

	/** 
	* @Title: getUserByChild 
	* @Description: 根据子用户得到父用户，微商城没用
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public User getUserByChild(Integer userId) {
		User user = getUserById(userId);
		if (user.getParentId() != 0) {
			user = getUserByChild(user.getParentId());
		}
		return user;
	}

	/** 
	* @Title: updateUser 
	* @Description: 更新用户
	* @param @param user
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public int updateUser(User user) {
		return userMapper.updateByPrimaryKeySelective(user);
	}

	/** 
	* @Title: getBasicUserProfile 
	* @Description: 得到当前用户的基本信息
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public UserProfile getBasicUserProfile(Integer userId) {
		UserProfile search = new UserProfile();
		search.setUserId(userId);
		UserProfile userProfile = userProfileMapper.selectByUserProfile(search);
		return userProfile;
	}
	@Override
	public String getRandomNum(){
		return new RandomNum().sixRandomNum();
	}

	/** 
	* @Title: createNewAccount 
	* @Description: 第三方登陆成功，创建新用户 
	* @param @param oauthLogin
	* @param @param headimgurl
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	//duobao
	public boolean createNewAccount(OauthLogin oauthLogin,String headimgurl) {
		User user = new User();
		String pswd = new Sha256Hash(oauthLogin.getOauthAccessToken()).toBase64().substring(0, 20);
		user.setPassword(pswd);
		int max = 11;
		int min = 1;
		Random random = new Random();
		int randomNumber = random.nextInt(max) % (max - min + 1) + min;
		user.setSerialNum(NumberUtil.generateSerialNum(randomNumber));
		userMapper.insertSelective(user);

		Integer userId = user.getUserId();

		// 为用户添加角色
		UserRoleLinkKey userRoleLinkKey = new UserRoleLinkKey();
		userRoleLinkKey.setRoleId(DuobaoRoleConstant.user);
		userRoleLinkKey.setUserId(userId);
		userRoleLinkMapper.insert(userRoleLinkKey);

		UserProfile userProfile = new UserProfile();
		userProfile.setUserId(user.getUserId());
		userProfile.setNickName(oauthLogin.getOauthUserName());
		userProfile.setName(oauthLogin.getOauthUserName());
		userProfile.setImage(headimgurl);
		userProfileMapper.insertSelective(userProfile);

		oauthLogin.setUserId(userId);
		oauthLogin.setOauthName(oauthLogin.getOauthType());
		oauthLoginMapper.insertSelective(oauthLogin);

		return true;
	}

	/** 
	* @Title: getFinancialAccount 
	* @Description: 微商城没用到
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public FinancialAccount getFinancialAccount(Integer userId) {
		FinancialAccount account = financialAccountMapper.selectByUserId(userId);
		return account;
	}

	/** 
	* @Title: getInvitation 
	* @Description: 取得下级用户列表，微商城不用
	* @param @param userId
	* @param @param pageNum
	* @param @param pageSize
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public PageInfo<UserInvitedLink> getInvitation(Integer userId, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<UserInvitedLink> invitation = userInvitedLinkMapper.selectByUserId(userId);
		for(UserInvitedLink item : invitation){
			UserProfile profile = userProfileMapper.selectByUserId(item.getInvitedId());
			item.setTimeStr(DateUtils.format(item.getInviteTime(), DateUtils.FORMAT_SHORT));
			item.setUserProfile(profile);
		}
		PageInfo<UserInvitedLink> pageInfo = new PageInfo<UserInvitedLink>(invitation);
		return pageInfo;
	}
	
	/** 
	* @Title: getInvitation 
	* @Description: 取得下级用户列表，微商城不用
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<UserInvitedLink> getInvitation(Integer userId) {
		HashSet<Integer> ids = new HashSet<Integer>();
		List<UserInvitedLink> levelOne = userInvitedLinkMapper.selectByUserId(userId);
		for(UserInvitedLink item : levelOne){
			ids.add(item.getInvitedId());
		}
		List<UserInvitedLink> levelSecond =userInvitedLinkMapper.selectByUserIds(ids);
		levelOne.addAll(levelSecond);
		for(UserInvitedLink item : levelOne){
			UserProfile profile = userProfileMapper.selectByUserId(item.getInvitedId());
			item.setTimeStr(DateUtils.format(item.getInviteTime(), DateUtils.FORMAT_SHORT));
			item.setUserProfile(profile);
		}
		return levelOne;
	}
	/** 
	* @Title: getInvitation 
	* @Description: 取得下级用户列表，微商城不用
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public HashSet<Integer> getInvitationId(Integer userId){
		HashSet<Integer> ids = new HashSet<Integer>();
		ids.add(userId);
		for(int i =0;i<2;i++){
			HashSet<Integer> tmp = userInvitedLinkMapper.selectIdsByUserId(ids);
			ids.addAll(tmp);
		}
		ids.remove(userId);
		return ids;
	}

	@Override
	public List<Province> getAllProvince() {
		return provinceMapper.selectAll();
	}

	@Override
	public List<City> getCities(Integer provinceId) {
		return cityMapper.selectByProvinceId(provinceId);
	}

	@Override
	public List<Region> getRegions(Integer cityId) {
		return regionMapper.selectByCityId(cityId);
	}

	@Override
	public User getUserBySerialNum(String serialNum) {
		return userMapper.selectBySerialNum(serialNum);
	}

	/** 
	* @Title: getWCPayment 
	* @Description: 对接微信支付接口，发起支付处理 
	* @param @param openId 用户openId
	* @param @param paymentSerialNum 支付单号
	* @param @param money 支付金额
	* @param @param title 支付标题
	* @param @return
	* @param @throws PaymentHandlerException    设定文件  
	* @throws 
	*/
	@Override
	public PaymentStrategyResult getWCPayment(String openId, String paymentSerialNum,BigDecimal money, String title) throws PaymentHandlerException{
		PaymentStrategyResult wcPaymentResult = new PaymentStrategyResult();
		if(openId==null){
			wcPaymentResult.setResult(false);
			logger.info("openid为空");
			return wcPaymentResult;
		}
		logger.info("支付,openid : " + openId);
		try {
		
		String moneyPost = "";
		moneyPost = money.multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_UP).toString();
		Map<String,String> weixinConfig = getWeixinConfig();
		UniteOrderResult result = getWCPayResult(openId, paymentSerialNum, moneyPost,new String(title.getBytes("UTF-8"), "UTF-8"),new String(title.getBytes("UTF-8"), "UTF-8"));
		System.out.println("result:" + JSON.toJSONString(result));
		if(result.getReturn_code().equals("SUCCESS")){
			wcPaymentResult.setAppId(weixinConfig.get(WechatMerchantInfo.APPID_CONFIG));
			wcPaymentResult.setTimeStamp(Long.toString(System.currentTimeMillis()));
			wcPaymentResult.setNonceStr(result.getNonce_str());
			String preSign = "appId="+result.getAppid()+"&nonceStr="+result.getNonce_str()+"&package=prepay_id=" + result.getPrepay_id() + "&signType=" + "MD5" + "&timeStamp=" + Long.toString(System.currentTimeMillis()) + "&key=" + weixinConfig.get(WechatMerchantInfo.AppSecret_CONFIG);
			wcPaymentResult.setPaySign(MyTool.getMD5(preSign.getBytes()).toUpperCase());
			wcPaymentResult.setPrePackage("prepay_id="+result.getPrepay_id());
			wcPaymentResult.setSignType("MD5");
			wcPaymentResult.setPaymentSerialNum(paymentSerialNum);
			wcPaymentResult.setOrderSerialNum(paymentSerialNum);
			logger.info("wcresult: " + JSON.toJSONString(wcPaymentResult));
			wcPaymentResult.setResult(true);
			return wcPaymentResult;
		}
		return wcPaymentResult;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/** 
	* @Title: getWCPayment 
	* @Description: 对接微信支付接口，发起支付处理 
	* @param @param openId 用户openId
	* @param @param paymentSerialNum 支付单号
	* @param @param money 支付金额
	* @param @param title 支付标题
	* @param @return
	* @param @throws PaymentHandlerException    设定文件  
	* @throws 
	*/
	public UniteOrderResult getWCPayResult(String openId, String paymentSerialNum,String money,String body,String detail) {
		try {
		Map<String,String> weixinConfig = getWeixinConfig();
		UniteOrder order = new UniteOrder();
		order.setAttach(datasource.getDataSourceStr());
		order.setAppid(weixinConfig.get(WechatMerchantInfo.APPID_CONFIG));
		order.setMch_id(weixinConfig.get(WechatMerchantInfo.MERCHANT_CODE_CONFIG));
		order.setOpenid(openId.toString());
		order.setDevice_info(PaymentTools.getServerIP());
		order.setNonce_str(PayMD5.GetMD5nonce_str());
		
			order.setBody(body);
		order.setDetail(detail);
		order.setOut_trade_no(paymentSerialNum);
		order.setFee_type("CNY");
		
		
		logger.info("money1: " + money);
		order.setTotal_fee(money);
		order.setSpbill_create_ip(PaymentTools.getServerIP());
		order.setNotify_url(weixinConfig.get(WechatMerchantInfo.NOTIFY_URL_CONFIG));
		order.setTrade_type("JSAPI");  
		order.setProduct_id(paymentSerialNum);
		String reqXML = PayImpl.generateXML(order,weixinConfig.get(WechatMerchantInfo.AppSecret_CONFIG));
		reqXML = new String(reqXML.getBytes("UTF-8"), "UTF-8");
		System.out.println("reqXML:" + reqXML);
		
		String respXML = PayImpl.requestWechat(wc_general_order, reqXML);
		 //String respXML =HttpsPost.httpsPost(reqXML);			
		
		logger.info("respXML: " + JSON.toJSONString(respXML));
		UniteOrderResult result = (UniteOrderResult) PayImpl.turnObject(UniteOrderResult.class, respXML);
		logger.info("WXresult: " + JSON.toJSONString(result));
		return result;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
	* @Title: WCScan 
	* @Description: 获取微信jssdk所需要的公共参数
	* @param @param url
	* @param @param request
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, String> WCScan(String url, HttpServletRequest request) {
		Map<String,String> weixinConfig = getWeixinConfig();
		logger.info(url);
		Map<String, String> map = new TreeMap<String, String>();
		WechatPay wechatPay = new WechatPay();
		wechatPay.setAppId(weixinConfig.get(WechatMerchantInfo.APPID_CONFIG));
		String nonceStr = PayMD5.GetMD5nonce_str();
		wechatPay.setNonceStr(nonceStr);
		wechatPay.setSignType("MD5");
		String timestamp = PaymentTools.getTimeStamp();
		wechatPay.setTimeStamp(timestamp);
		String appId = weixinConfig.get(WechatMerchantInfo.APPID_CONFIG);
		String preSignParam = "";
		HttpSession session = request.getSession();
		String jsapi_ticket = (String) session.getAttribute(GlobalSessionConstant.Ticket);
		// String jsapi_ticket = null;
		if (jsapi_ticket == null) {
			Var var = varMapper.selectByPrimaryKey(1);
			jsapi_ticket = getJSSDKTicket(var.getValue());
			session.setAttribute(GlobalSessionConstant.Ticket, jsapi_ticket);
			logger.info("设置ticket");
		}
		map.put("jsapi_ticket", jsapi_ticket);
		map.put("noncestr", nonceStr);
		map.put("timestamp", timestamp);
		for (Entry<String, String> s : map.entrySet()) {
			preSignParam = preSignParam.concat(s.getKey() + "=" + s.getValue() + "&");
		}
		preSignParam = preSignParam.concat("url=" + url);
		logger.info("preSignParam: " + preSignParam);
		String signature = EndecryptUtils.SHA1(preSignParam);
		logger.info("signature: " + signature);
		map.put("appId", appId);
		map.put("signature", signature);
		return map;
	}
	/** 
	* @Title: getJSSDKAccessToken 
	* @Description: 取得微信AccessToken 
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String getJSSDKAccessToken() {
		String requestUrl = weixin_jssdk_acceToken_url;
		Map<String, String> response = HttpUtils.transStringToMap(HttpUtils.sendGet(requestUrl, null));
		for (Entry<String, String> s : response.entrySet()) {
			logger.info("key: " + s.getKey() + "---" + "value: " + s.getValue());
		}
		return response.get("access_token");
	}
	/** 
	* @Title: getJSSDKTicket 
	* @Description: 取得微信Ticket
	* @param @param access_token
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String getJSSDKTicket(String access_token) {
		String requestUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + access_token + "&type=jsapi";
		Map<String, String> response = HttpUtils.transStringToMap(HttpUtils.sendGet(requestUrl, null));

		for (Entry<String, String> s : response.entrySet()) {
			logger.info("key: " + s.getKey() + "---" + "value: " + s.getValue());
		}
		return response.get("ticket");
	}
	/** 
	* @Title: alipayNotifyHandle 
	* @Description: 支付宝支付结果回调处理
	* @param @param request
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String alipayNotifyHandle(HttpServletRequest request) {
		logger.info("支付宝异步通知开始");
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		logger.info(JSON.toJSONString(requestParams));
		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		// 商户订单号
		String out_trade_no = null;
		String trade_no = null;
		String trade_status = null;
		String total_fee = null;
		String seller_id = null;
		String body = null;
		try {
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
			// 支付宝交易号
			body = new String(request.getParameter("body").getBytes("ISO-8859-1"), "UTF-8");

			trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");

			// 交易状态
			trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
			logger.info("trade_status: " + trade_status);

			total_fee = new String(request.getParameter("total_fee").getBytes("ISO-8859-1"), "UTF-8");

			seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
		Map<String,String> config = getAlipayConfig();
		if (AlipayNotify.verify(params,config)) {// 验证成功
			//////////////////////////////////////////////////////////////////////////////////////////
			// 请在这里加上商户的业务逻辑程序代码

			// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

			if (trade_status.equals("TRADE_FINISHED")) {
				// 判断该笔订单是否在商户网站中已经做过处理
				// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
				// 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
				// 如果有做过处理，不执行商户的业务程序

				// 注意：
				// 退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
			} else if (trade_status.equals("TRADE_SUCCESS")) {
				// 判断该笔订单是否在商户网站中已经做过处理
				// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
				// 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
				// 如果有做过处理，不执行商户的业务程序
				datasource.switchDataSource(body);
				logger.info("支付宝回调datasource: " + body);
				if (alipayNotifySuccessHandle(out_trade_no, total_fee, seller_id,trade_no,body)) {
					return "success";
				}
				// 注意：
				// 付款完成后，支付宝系统发送该交易状态通知
			}

			// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——

			//////////////////////////////////////////////////////////////////////////////////////////
		} else {// 验证失败
			return "fail";
		}
		return "fail";
	}

	/**
	 * @param body  
	* @Title: alipayNotifySuccessHandle 
	* @Description: 支付宝支付成功处理
	* @param @param out_trade_no 微商城支付单号
	* @param @param total_fee 总金额
	* @param @param seller_id 商户id
	* @param @param trade_no 支付宝单号
	* @param @return    设定文件 
	* @return boolean    返回类型 
	* @throws 
	*/
	private boolean alipayNotifySuccessHandle(String out_trade_no, String total_fee, String seller_id,
			String trade_no, String body) {
		
		return alipayNotifyHandle(out_trade_no,trade_no);
	}
	

	/** 
	* @Title: isWC 
	* @Description: 判断当前请求是否来源于微信 
	* @param @param request
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean isWC(HttpServletRequest request) {
		String browser = UserAgentUtils.getBrowserInfo(JSON.toJSONString(request.getHeader("User-Agent")));
		if (browser.contains("MicroMessenger")) {
			int version = Integer.parseInt(browser.substring(browser.length() - 1, browser.length()));
			if (version >= 5) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/** 
	* @Title: updateWXInfo 
	* @Description: 更新微信用户信息
	* @param     设定文件  
	* @throws 
	*/
	@Override
	public void updateWXInfo() {
		List<OauthLogin> oauthLogins = oauthLoginMapper.selectForSchedule();
		for(OauthLogin oauthLogin : oauthLogins){
			Var var = varMapper.selectByPrimaryKey(new Integer(1));
			//获取微信用户的name 作为account
			String weiXinUserInfo = getWeiXinUserInfo(var.getValue(), oauthLogin.getOauthId());
			com.alibaba.fastjson.JSONObject weiXinUserInfoJsonData = JSON.parseObject(weiXinUserInfo);
			String errcode = weiXinUserInfoJsonData.getString("errcode");
			logger.info("erroCode: " + errcode);
			if(errcode!=null&&(errcode.equals("40001")||errcode.equals("42001"))){
				duobaoService.getGolbalAccessToken();
				var = varMapper.selectByPrimaryKey(new Integer(1));
				//获取微信用户的name 作为account
				weiXinUserInfo = getWeiXinUserInfo(var.getValue(), oauthLogin.getOauthId());
				weiXinUserInfoJsonData = JSON.parseObject(weiXinUserInfo);
			}
			String headimgurl;
			String weiXinNickName = "";
			String isSubcribe = weiXinUserInfoJsonData.getString("subscribe");
			if (isSubcribe!=null&&isSubcribe.equals("1")) {
				UserProfile profile = userProfileMapper.selectByUserId(oauthLogin.getUserId());
				weiXinNickName = EmojiFilter.filterEmoji(weiXinUserInfoJsonData.getString("nickname"));
				//headimgurl = headimgurl.replaceAll("\\", "");
				if (!Strings.isNullOrEmpty(weiXinNickName)) {
					profile.setNickName(weiXinNickName);
					profile.setName(weiXinNickName);
				}
				headimgurl = weiXinUserInfoJsonData.getString("headimgurl");
				profile.setImage(headimgurl);
				try {
					userProfileMapper.updateByPrimaryKeySelective(profile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				oauthLogin.setIsSubcribe(true);
				oauthLoginMapper.updateByPrimaryKeySelective(oauthLogin);
			}else{
				oauthLogin.setIsSubcribe(false);
				oauthLoginMapper.updateByPrimaryKeySelective(oauthLogin);
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/** 
	* @Title: getWeiXinUserInfo 
	* @Description: 通过全局的token 和 openId 获取用户的信息
	* @param @param token
	* @param @param openId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String getWeiXinUserInfo(String token,String openId){  
		String url =  "https://api.weixin.qq.com/cgi-bin/user/info?access_token="+token+"&openid="+openId; 
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        HttpGet httpget = new HttpGet(url);   
        CloseableHttpResponse response = null;  
        String content ="";  
        try {  
            response = httpclient.execute(httpget);  
            if(response.getStatusLine().getStatusCode()==200){  
                content = EntityUtils.toString(response.getEntity(),"utf-8");  
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return content;  
    }

	@Override
	public OauthLogin getOauthLoginInfoByUserId(Integer userId) {
		return oauthLoginMapper.selectByUserId(userId);
	}

	public static int getRandom(int min,int max){
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	@Override
	public void updateOauthLogin(OauthLogin oauthLogin) {
		// TODO Auto-generated method stub
		oauthLoginMapper.updateByPrimaryKeySelective(oauthLogin);
	}

	/** 
	* @Title: getIsSubScribe 
	* @Description: 判断当前用户是否关注公众号
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean getIsSubScribe(Integer userId) {
		OauthLogin oauthLogin = getOauthLoginInfoByUserId(userId);
		
		Var var = varMapper.selectByPrimaryKey(new Integer(1));
		//获取微信用户的name 作为account
		String weiXinUserInfo = getWeiXinUserInfo(var.getValue(), oauthLogin.getOauthId());
		com.alibaba.fastjson.JSONObject weiXinUserInfoJsonData = JSON.parseObject(weiXinUserInfo);
		String errcode = weiXinUserInfoJsonData.getString("errcode");
		logger.info("erroCode: " + errcode);
		if(errcode!=null){
			duobaoService.getGolbalAccessToken();
			var = varMapper.selectByPrimaryKey(new Integer(1));
			//获取微信用户的name 作为account
			weiXinUserInfo = getWeiXinUserInfo(var.getValue(), oauthLogin.getOauthId());
			weiXinUserInfoJsonData = JSON.parseObject(weiXinUserInfo);
		}
		String isSubcribe = weiXinUserInfoJsonData.getString("subscribe");
		if (isSubcribe!=null&&isSubcribe.equals("1")) {
			return true;
		} else {
			return false;
		}
	}
	/** 
	* @Title: updateProfile 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param userId
	* @param @param userProfile
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean updateProfile(Integer userId, UserProfile userProfile) {

		UserProfile profile = userProfileMapper.selectByUserId(userId);
		logger.info("profile:"+JSON.toJSONString(profile));

		if(profile!=null) {
			if (!StringUtils.isNullOrEmpty(userProfile.getNickName())) {
				profile.setNickName(userProfile.getNickName());
			}
			if (!StringUtils.isNullOrEmpty(userProfile.getName())) {
				profile.setName(userProfile.getName());
			}
			if (!StringUtils.isNullOrEmpty(userProfile.getPhone())) {
			    if(!userProfile.getPhone().equals(profile.getPhone())) {
					UserProfile search=new UserProfile();
					search.setPhone(userProfile.getPhone());
					UserProfile check = userProfileMapper.selectByUserProfile(search);
					if(check==null) {
						profile.setPhone(userProfile.getPhone());
					}else{
						return false;
					}
				}
			}
			if (userProfileMapper.updateByPrimaryKeySelective(profile) > 0) {
				return true;
			}
		}
		return false;
	}

	/** 
	* @Title: selectPaymentForRecharge 
	* @Description: 取得充值可用的支付方式 
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<Payment> selectPaymentForRecharge() {
		return paymentMapper.selectAllForRecharge();
	}

	/** 
	* @Title: getPaymentMethodById 
	* @Description: 根据支付方式id获取支付方式 
	* @param @param paymentId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Payment getPaymentMethodById(Integer paymentId) {
		return paymentMapper.selectByPrimaryKey(paymentId);
	}

	/** 
	* @Title: insertPaymentOrder 
	* @Description: 插入支付订单
	* @param @param paymentOrder
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public int insertPaymentOrder(PaymentOrder paymentOrder) {
		return paymentOrderMapper.insertSelective(paymentOrder);
	}

	/** 
	* @Title: WechatNotifyHandle 
	* @Description: 微信支付结果回调处理
	* @param @param out_trade_no 微商城支付单号
	* @param @param transaction_id 微信支付单号
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean WechatNotifyHandle(String out_trade_no, String transaction_id,String attach) {
		datasource.switchDataSource(attach);
		logger.info("微信回调datasource: " + attach);
		PaymentOrder paymentOrder = paymentOrderMapper.selectByPaymentSerialNum(out_trade_no);
		if(paymentOrder!=null&&!paymentOrder.getIsCompleted()){
			paymentOrder.setTransactionId(transaction_id);
			paymentOrder.setIsCompleted(true);
			paymentOrder.setCompleteTime(new Date());
			if(paymentOrderMapper.updateByPrimaryKeySelective(paymentOrder)>0){
				//TODO 调用存储过程
				OauthLogin oauthLogin = getOauthLoginInfoByUserId(paymentOrder.getUserId());
				if(oauthLogin!=null){
					if(paymentOrder.getTransactionType()==1){
						return paymentSuccessHandler(paymentOrder, oauthLogin);
					}else{
						return rechargeSuccessHandler(paymentOrder, oauthLogin);
					}
				}
			}
		}
		return false;
	}
	/** 
	 * @Title: alipayNotifyHandle 
	 * @Description: 支付宝支付结果回调处理
	 * @param @param out_trade_no 微商城支付单号
	 * @param @param transaction_id 微信支付单号
	 * @param @return    设定文件  
	 * @throws 
	 */
	@Override
	public boolean alipayNotifyHandle(String out_trade_no, String transaction_id) {
		PaymentOrder paymentOrder = paymentOrderMapper.selectByPaymentSerialNum(out_trade_no);
		if(paymentOrder!=null&&!paymentOrder.getIsCompleted()){
			paymentOrder.setTransactionId(transaction_id);
			paymentOrder.setIsCompleted(true);
			paymentOrder.setCompleteTime(new Date());
			if(paymentOrderMapper.updateByPrimaryKeySelective(paymentOrder)>0){
				//TODO 调用存储过程
				OauthLogin oauthLogin = getOauthLoginInfoByUserId(paymentOrder.getUserId());
				if(oauthLogin!=null){
					if(paymentOrder.getTransactionType()==1){
						return paymentSuccessHandler(paymentOrder, oauthLogin);
					}else{
						return rechargeSuccessHandler(paymentOrder, oauthLogin);
					}
				}
			}
		}
		return false;
	}
	
	
	/** 
	* @Title: paymentSuccessHandler 
	* @Description: 支付结果成功处理
	* @param @param paymentOrder 微商城支付订单
	* @param @param oauthLogin 第三方登陆用户信息
	* @param @return    设定文件 
	* @return boolean    返回类型 
	* @throws 
	*/
	private boolean paymentSuccessHandler(PaymentOrder paymentOrder, OauthLogin oauthLogin) {
		//更新订单状态
		List<Order> orders = this.selectOrderByPaymentSerialNum(paymentOrder.getUserId(), paymentOrder.getPaymentSerialNum());
		for(Order order:orders){
			order.setStatus((short)2);
			//调用存储过程插入洪石订单
			int hongShiResult=0;
			try {
				HongShiCreateOrder createOrderData = new HongShiCreateOrder();
				NapaStore napaStore = napaStoreMapper.selectByPrimaryKey(order.getStoreId());
				if(napaStore!=null){
					createOrderData.setDepartment(napaStore.getHsCode());
				}else{
					logger.info("加盟店不存在");
				}
				createOrderData.setCallNumber(order.getPhone());
				if(order.getIsSelfPick()){
					createOrderData.setDestination(order.getProvince()+order.getCity()+order.getRegion()+order.getAddrDetail()+"(收货人:" + order.getName()+")");
				}
				createOrderData.setOrderCode(null);
				createOrderData.setRemarks(order.getRemark());
				BigDecimal total = order.getTotalPrice(); 
				createOrderData.setWeiXinCode(oauthLogin.getOauthId());
				createOrderData.setWSC_TardNo(order.getOrderSerialNum());
				createOrderData.setPayment(new BigDecimal(0));
				if(order.getVoucherCode()!=null&&!order.getVoucherCode().equals("")){
					List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(order.getVoucherCode());
					if(coupon!=null&&coupon.size()>0){
						createOrderData.setVoucher(coupon.get(0).getPayQuota());
					}else{
						createOrderData.setVoucher(new BigDecimal(0));
					}
				}else{
					createOrderData.setVoucher(new BigDecimal(0));
				}
				createOrderData.setTotalAmount(total);
				CreateOrderResult createOrderResult = hongShiMapper.createOrder(createOrderData);
				//回收礼券
				try {
					if(createOrderResult!=null){
						List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(order.getVoucherCode());
						if(coupon!=null&&coupon.size()>0){
							hongShiMapper.recoverVoucher(coupon.get(0).getGoodsCode(),createOrderResult.getOrderID(),order.getVoucherCode(),"微商城使用单号："+order.getOrderSerialNum());
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//调用存储过程插入订单明细
				List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
				for(OrderItem item:items){
					HongShiCreateOrderItem createOrderItem = new HongShiCreateOrderItem();
					SpecificationValue value = specificationValueMapper.selectByPrimaryKey(item.getValueId());
					if(value!=null){
						createOrderItem.setGoodsCode(value.getHsGoodsCode());
					}
					createOrderItem.setGoodsCount(item.getAmount().intValue());
					createOrderItem.setpId(createOrderResult.getOrderID());
					createOrderItem.setPrice(item.getPrice());
					createOrderItem.setTotalAmount(item.getPrice());
					hongShiMapper.createOrderItem(createOrderItem);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			//处理分销
			try {
				Config firstDistConfig = configMapper.getByTag(WebConfig.firstDis);
				Config secondDistConfig = configMapper.getByTag(WebConfig.secondDis);
				User firstDist = userMapper.selectByInvitedId(order.getUserId());
				if(firstDist!=null){
					//一级分销
					Balance firstBalance = this.getBalance(firstDist.getUserId());
					if(firstBalance!=null&&firstDistConfig!=null){
						BigDecimal firstMoney = order.getTotalPrice().multiply(new BigDecimal((firstDistConfig.getValue())).divide(new BigDecimal(100)));
						firstBalance.setBalance(firstBalance.getBalance().add(firstMoney));
						if(balanceMapper.updateByPrimaryKeySelective(firstBalance)>0){
							//记录日志
							BalanceLog log = new BalanceLog();
							log.setUserId(firstDist.getUserId());
							log.setBalance(firstBalance.getBalance());
							log.setMoney(firstMoney);
							balanceLogMapper.insertSelective(log);
							//记录订单分销信息
							order.setFirstDistId(firstDist.getUserId());
							order.setFirstDistMoney(firstMoney);
						}
					}
					
					//二级分销
					User secondDist = userMapper.selectByInvitedId(firstDist.getUserId());
					Balance secondBalance = this.getBalance(secondDist.getUserId());
					if(secondDist!=null&&secondBalance!=null&&secondDistConfig!=null){
						BigDecimal secondMoney = order.getTotalPrice().multiply(new BigDecimal((secondDistConfig.getValue())).divide(new BigDecimal(100)));
						secondBalance.setBalance(secondBalance.getBalance().add(secondMoney));
						if(balanceMapper.updateByPrimaryKeySelective(secondBalance)>0){
							//记录日志
							BalanceLog log = new BalanceLog();
							log.setUserId(secondDist.getUserId());
							log.setBalance(secondBalance.getBalance());
							log.setMoney(secondMoney);
							balanceLogMapper.insertSelective(log);
							//记录订单分销信息
							order.setFirstDistId(secondDist.getUserId());
							order.setFirstDistMoney(secondMoney);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			order.setSyncStatus(hongShiResult);
			orderMapper.updateByPrimaryKeySelective(order);
			//更新支付单状态
			paymentOrder.setIsSync(true);
			this.updatePaymentOrder(paymentOrder);
			//发送购买成功短信
			String[] key = {"keyword1","keyword2","keyword3","keyword4"};
			Payment payment = paymentMapper.selectByPrimaryKey(paymentOrder.getPaymentId());
			String paymentMethod="微信支付";
			if(payment!=null){
				if(payment.getStrategyClassName().equals("MemberCardPaymentStrategy")){
					paymentMethod="会员卡余额支付";
				}else if(payment.getStrategyClassName().equals("AlipayPaymentStrategy")){
					paymentMethod="支付宝支付";
				}
			}
			String[] value = {paymentOrder.getPaymentSerialNum(),DateUtils.format(paymentOrder.getCompleteTime(), DateUtils.FORMAT_LONG).toString(),paymentOrder.getMoney()+"元".toString(),paymentMethod};
			sendWXMessage(oauthLogin.getOauthId(), "S3vfLhEEbVICFmwgpHedYUtlm7atyY3zl-GxJYY20ok", "hs.uclee.com/order-list", "尊敬的会员，您有一笔订单已经支付成功", key,value, "感谢您的惠顾");
		}
		return true;
	}

	/** 
	* @Title: getBalance 
	* @Description: 取得用户的微商城余额 
	* @param @param userId
	* @param @return    设定文件 
	* @return Balance    返回类型 
	* @throws 
	*/
	private Balance getBalance(Integer userId) {
		Balance balance = balanceMapper.selectByUserId(userId);
		if(balance==null){
			Balance tmp = new Balance();
			tmp.setBalance(new BigDecimal(0));
			tmp.setUserId(userId);
			if(balanceMapper.insertSelective(tmp)>0){
				return tmp;
			}else{
				return null;
			}
			
		}
		return balance;
	}

	/** 
	* @Title: rechargeSuccessHandler 
	* @Description: 充值成功处理 
	* @param @param paymentOrder
	* @param @param oauthLogin
	* @param @return    设定文件 
	* @return boolean    返回类型 
	* @throws 
	*/
	private boolean rechargeSuccessHandler(PaymentOrder paymentOrder, OauthLogin oauthLogin) {
		RechargeConfig rechargeConfig = rechargeConfigMapper.selectByMoney(paymentOrder.getMoney());
		
		BigDecimal realMoney = paymentOrder.getMoney();
		if(rechargeConfig.getType().equals(2)){
			List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByGoodsCode(rechargeConfig.getVoucherCode());
			if (coupon!=null&&coupon.size()>0) {
				try {
					hongShiMapper.saleVoucher(oauthLogin.getOauthId(), coupon.get(0).getVouchersCode(),rechargeConfig.getVoucherCode());
				} catch (Exception e) {
					
				}
			}
		}else{
			realMoney = realMoney.add(rechargeConfig.getRewards());
		}
		HongShiRecharge dd=new HongShiRecharge().setcWeiXinCode(oauthLogin.getOauthId())
				.setcWeiXinOrderCode(paymentOrder.getPaymentSerialNum())
				.setnAddMoney(realMoney);
		Integer res=hongShiVipService.hongShiRecharge(dd);
		//发送充值成功微信通知
		if(res==0){
			paymentOrder.setIsSync(true);
			paymentOrderMapper.updateByPrimaryKeySelective(paymentOrder);
			String[] key = {"keyword1","keyword2"};
			String[] value = {DateUtils.format(paymentOrder.getCompleteTime(), DateUtils.FORMAT_LONG).toString(),paymentOrder.getMoney()+"元".toString()};
			sendWXMessage(oauthLogin.getOauthId(), "TBY-Wrn9sQuOoM_BUNZO2aEjX56AG6RRNxHrEH8k_pI", "hs.uclee.com/recharge-list", "尊敬的会员，您本次充值成功到账", key,value, "如有疑问，请点击这里");
		}
		return true;
	}
	/** 
	* @Title: sendWXMessage 
	* @Description: 发送微信信息工具 
	* @param @param openId  用户的openID
	* @param @param templateId 微信短信模板id
	* @param @param url	短信内容点击跳转地址
	* @param @param firstData	参考微信短信模板教程
	* @param @param key 参考微信短信模板教程，这里的key和下面的value一一对应
	* @param @param value 参考微信短信模板教程
	* @param @param remarkData 参考微信短信模板教程
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String sendWXMessage(String openId,String templateId,String url, String firstData,String[] key,String[] value,String remarkData) {
		Map<String,Object> sendData = new LinkedHashMap<String,Object>();
		sendData.put("touser", openId);
		sendData.put("template_id", templateId);
		sendData.put("topcolor", "#FF0000");
		sendData.put("url", url);
		Map<String,Object> mainData = new TreeMap<String,Object>();
		
		Map<String,Object> mapFirst = new TreeMap<String,Object>();
		mapFirst.put("value", firstData);
		mainData.put("first", mapFirst);
		
		for(int i=0;i<key.length;i++){
			Map<String,Object> Keyword = new TreeMap<String,Object>();
			Keyword.put("value", value[i]);
			mainData.put(key[i], Keyword);
		}
		
		Map<String,Object> mapRemark = new TreeMap<String,Object>();
		mapRemark.put("value", remarkData);
		mapRemark.put("color", "#173177");
		mainData.put("remark", mapRemark);
		
		sendData.put("data", mainData);
		logger.info(JSON.toJSONString(sendData));
		try {
        Var var = varMapper.selectByPrimaryKey(new Integer(1));
        URL urlPost = new URL("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + var.getValue());// 创建连接  
        HttpURLConnection connection = (HttpURLConnection) urlPost  
                .openConnection();  
        connection.setDoOutput(true);  
        connection.setDoInput(true);  
        connection.setUseCaches(false);  
        connection.setInstanceFollowRedirects(true);  
        connection.setRequestMethod("POST"); // 设置请求方式  
        connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式  
        connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式  
        connection.connect();  
        OutputStreamWriter out = new OutputStreamWriter(  
                connection.getOutputStream(), "UTF-8"); // utf-8编码  
        out.append(JSON.toJSONString(sendData));  
        out.flush();  
        out.close();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String lines;
        StringBuffer sb = new StringBuffer("");
        while ((lines = reader.readLine()) != null) {
            lines = new String(lines.getBytes(), "utf-8");
            sb.append(lines);
        }
        System.err.println(sb);
        reader.close();
        // 断开连接
        connection.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/** 
	* @Title: getProductDtoById 
	* @Description: 获取产品详情
	* @param @param productId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public ProductDto getProductDtoById(Integer productId) {
		ProductDto productDto = productMapper.getProductById(productId);
		productDto.setDescription(FileUtil.UrlRequest(productDto.getDescription()));
		List<ProductImageLink> images = productImageLinkMapper.selectByProductId(productId);
		productDto.setImages(images);
		List<Specification> specifications = specificationMapper.getByProductId(productId);
		productDto.setSpecifications(specifications);
		if(specifications!=null&&specifications.size()>0){
			if(specifications.get(0).getValues()!=null&&specifications.get(0).getValues().size()==1){
				if(specifications.get(0).getValues().get(0)!=null){
					productDto.setCurrentSpecValudId(specifications.get(0).getValues().get(0).getValueId());
				}else{
					productDto.setCurrentSpecValudId(null);
				}
			}else{
				productDto.setCurrentSpecValudId(null);
			}
		}
		ProductSale sale = productSaleMapper.selectByProductId(productDto.getProductId());
		if(sale!=null){
			productDto.setSalesAmount(sale.getCount());
		}else{
			productDto.setSalesAmount(0);
		}
		return productDto;
	}

	@Override
	public Integer addToCart(Cart cart) {
		Cart cartTmp = cartMapper.selectExisted(cart);
		if(cartTmp!=null){
			cartTmp.setAmount(cartTmp.getAmount()+cart.getAmount());
			if(cartMapper.updateByPrimaryKeySelective(cartTmp)>0)
			return cartTmp.getCartId();
			return null;
		}else{
			if(cartMapper.insertSelective(cart)>0)
			return cart.getCartId();
			return null;
		}
	}

	@Override
	public Product getProductById(Integer productId) {
		return productMapper.selectByPrimaryKey(productId);
	}

	@Override
	public SpecificationValue getSpecificationValue(Integer productId, Integer valueId) {
		return specificationValueMapper.selectByProductIdAndValueId(productId,valueId);
	}

	/** 
	* @Title: getUserCart 
	* @Description: 获取用户的购物车列表 
	* @param @param userId
	* @param @param selectedStoreId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<CartDto> getUserCart(Integer userId,Integer selectedStoreId) {
		List<CartDto> ret = new ArrayList<CartDto>();
		List<CartDto> carts = cartMapper.selectUserCart(userId);
		for(CartDto cart : carts){
			SpecificationValueStoreLink link = specificationValueStoreLinkMapper.selectByValueAndStoreId(cart.getSpecificationValueId(), selectedStoreId);
			if(link==null){
				cart.setIsDisabled(true);
			}else{
				cart.setIsDisabled(false);
			}
			String specifcationStr = "";
			
			List<ProductDto> products  = productMapper.selectOneImage(cart.getProductId());
			if(products.size()>0){
				cart.setTitle(products.get(0).getTitle());
				cart.setImage(products.get(0).getImage());
			}
			SpecificationValue specificationValue = specificationValueMapper.selectByPrimaryKey(cart.getSpecificationValueId());
			if(specificationValue!=null){
				cart.setStock(specificationValue.getHsStock());
				specifcationStr = specifcationStr + "：" + specificationValue.getValue();
				Specification specification = specificationMapper.selectByPrimaryKey(specificationValue.getSpecificationId());
				cart.setMoney(specificationValue.getHsGoodsPrice());
				if(specification!=null){
					specifcationStr = specification.getSpecification() +  specifcationStr;
				}
				cart.setSpecification(specifcationStr);
				ret.add(cart);
			}else{
				cartMapper.deleteByPrimaryKey(cart.getCartId());
			}
		}
		return ret;
	}

	/** 
	* @Title: getDeliverAddrList 
	* @Description: 获取用户收货地址列表
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<DeliverAddr> getDeliverAddrList(Integer userId) {
		logger.info("userId:" + userId);
		return deliverAddrMapper.selectByUserId(userId);
	}
	
	/** 
	* @Title: editAddrHandler 
	* @Description: 编辑地址处理 
	* @param @param addr
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String editAddrHandler(DeliverAddr addr) {
		if(addr.getProvinceId()!=null&&addr.getCityId()!=null&&addr.getRegionId()!=null&&addr.getAddrDetail()!=null&&addr.getName()!=null&&addr.getPhone()!=null){
			Province province = provinceMapper.selectByPrimaryKey(addr.getProvinceId());
			City city = cityMapper.selectByPrimaryKey(addr.getCityId());
			Region region = regionMapper.selectByPrimaryKey(addr.getRegionId());
			if(province==null||city==null){
				return "dataError";
			}
			addr.setCity(city.getCity());
			addr.setProvince(province.getProvince());
			addr.setRegion(region.getRegion());
			if(addr.getDeliveraddrId()!=null){
				if(deliverAddrMapper.updateByPrimaryKeySelective(addr)>0){
					return "editsuccess";
				}else{
					return "failed";
				}
			}else{
				if(deliverAddrMapper.insertSelective(addr)>0){
					return "addsuccess";
				}else{
					return "failed";
				}
			}
		}else{
			return "dataError";
		}
	}
	/** 
	* @Title: delAddrHandler 
	* @Description: 删除地址处理
	* @param @param deliverAddr
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String delAddrHandler(DeliverAddr deliverAddr) {
		DeliverAddr addr = deliverAddrMapper.selectByUserAndAddrId(deliverAddr.getUserId(),deliverAddr.getDeliveraddrId());
		if(addr!=null&&addr.getIsDefault()){
			return "isDefault";
		}
		if(deliverAddr.getUserId()==null || deliverAddr.getDeliveraddrId()==null){
			return "dataEmpty";
		}
		if(deliverAddrMapper.deleteByUserIdAndAddrId(deliverAddr.getUserId(),deliverAddr.getDeliveraddrId())>0){
			return "success";
		}else{
			return "failed";
		}
	}

	/** 
	* @Title: setDefaultAddr 
	* @Description: 设置默认地址 
	* @param @param deliverAddr
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String setDefaultAddr(DeliverAddr deliverAddr) {
		DeliverAddr defaultAddr = deliverAddrMapper.selectDefaultByUserId(deliverAddr.getUserId());
		defaultAddr.setIsDefault(false);
		DeliverAddr newDefaultAddr = deliverAddrMapper.selectByUserAndAddrId(deliverAddr.getUserId(),deliverAddr.getDeliveraddrId());
		newDefaultAddr.setIsDefault(true);
		if(deliverAddrMapper.updateByPrimaryKeySelective(defaultAddr)>0&&deliverAddrMapper.updateByPrimaryKeySelective(newDefaultAddr)>0){
			return "success";
		}else{
			return "failed";
		}
	}

	/** 
	* @Title: getInvitationHandler 
	* @Description: 用户上下级关系绑定
	* @param @param userId
	* @param @param serialNum
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean getInvitationHandler(Integer userId, String serialNum) {
		UserInvitedLink tmp = userInvitedLinkMapper.selectByInvitedId(userId);
		logger.info(JSON.toJSONString(tmp));
		if(tmp==null){
			User user = userMapper.selectByPrimaryKey(userId);
			User invitor = userMapper.selectBySerialNum(serialNum);
			logger.info(JSON.toJSONString(user));
			if(user!=null&&!user.getSerialNum().equals(serialNum)){
				UserInvitedLink link = new UserInvitedLink();
				link.setUserId(invitor.getUserId());
				link.setInvitedId(userId);
				logger.info(JSON.toJSONString(link));
				if(userInvitedLinkMapper.insertSelective(link)>0){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public DeliverAddr getDefaultAddrByUserId(Integer userId) {
		return deliverAddrMapper.selectDefaultByUserId(userId);
	}
	
	@Override
	public Map<String, Object> stockCheck(StockPost stockPost, Integer userId) {
		Map<String,Object> map = new TreeMap<String,Object>();
		for(Integer cartId : stockPost.getCartIds()){
			Cart cart = cartMapper.selectByUserIdAndCartId(userId,Integer.valueOf(cartId));
			if (cart!=null) {
				SpecificationValue value = specificationValueMapper.selectByPrimaryKey(cart.getSpecificationValueId());
				Product product = productMapper.selectByPrimaryKey(cart.getProductId());
				if (product != null && value != null) {
					if (cart.getAmount() > value.getHsStock()) {
						map.put("result", false);
						map.put("reason", product.getTitle() + "库存不足,剩余库存为：" + value.getHsStock());
						return map;
					} 
				}else{
					map.put("result", false);
					map.put("reason", "产品"+product.getTitle() + "已失效，请重新加入购物车");
					return map;
				}
			}
		}
		map.put("result", true);
		return map;
	}

	/** 
	* @Title: orderHandler 
	* @Description: 提交订单处理 
	* @param @param orderPost post过来的订单信息
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> orderHandler(OrderPost orderPost, Integer userId) {
		Map<String,Object> map = new TreeMap<String,Object>();
		//记录最后一次购买时间
		UserProfile userProfile = userProfileMapper.selectByUserId(userId);
		if(userProfile!=null){
			userProfile.setLastBuy(new Date());
			logger.info(JSON.toJSONString(userProfile));
			userProfileMapper.updateByPrimaryKeySelective(userProfile);
		}
		//创建订单和支付单
		Order order = new Order();
		order.setOrderSerialNum(NumberUtil.generateSerialNum());
		if(orderPost.getIsSelfPick().equals("true")){
			order.setIsSelfPick(true);
		}else{
			order.setIsSelfPick(false);
		}
		order.setUserId(userId);
		if(orderPost.getStoreId()==null){
			map.put("result", false);
			map.put("reason", "请选择门店");
			return map;
		}
		order.setStoreId(orderPost.getStoreId());
		if(orderPost.getIsSelfPick()!=null&&orderPost.getIsSelfPick().equals("false")){
			//处理收货地址
			DeliverAddr addr = deliverAddrMapper.selectByPrimaryKey(orderPost.getAddrId());
			if(addr==null){
				map.put("result", false);
				map.put("reason", "请选择地址");
				return map;
			}
			order.setAddrDetail(addr.getAddrDetail());
			order.setCity(addr.getCity());
			order.setProvince(addr.getProvince());
			order.setRegion(addr.getRegion());
			order.setName(addr.getName());
			order.setPhone(addr.getPhone());
			order.setZipCode(addr.getZipcode());
			addr.setIsDefault(true);
			deliverAddrMapper.clearDeault(userId);
			deliverAddrMapper.updateByPrimaryKey(addr);
		}else{
			order.setPhone(orderPost.getPhone());
		}
		order.setRemark(orderPost.getRemark());
		order.setPickTimeStr(orderPost.getPickDateStr() + " " + orderPost.getPickTimeStr()+":00");
		logger.info(order.getPickTimeStr());
		order.setPickTime(DateUtils.parse(order.getPickTimeStr(), DateUtils.FORMAT_LONG));
		logger.info(order.getPickTime());
		BigDecimal totalMoney = new BigDecimal(0);
		//订单项处理
		List<OrderItem> orderItem = new ArrayList<OrderItem>();
		String[] cartIds = orderPost.getCartIds().split(",");
		for(String cartId : cartIds){
			Cart cart = cartMapper.selectByUserIdAndCartId(userId,Integer.valueOf(cartId));
			if(cart==null){
				map.put("result", false);
				map.put("reason", "非法数据，请返回购物车重新提交");
				return map;
			}
			Product product = productMapper.selectByPrimaryKey(cart.getProductId());
			SpecificationValue value = specificationValueMapper.selectByPrimaryKey(cart.getSpecificationValueId());
			if(value==null){
				map.put("result", false);
				map.put("reason", "非法数据，请返回购物车重新提交");
				return map;
			}
			//TODO 改成购物车失效
			if(product==null){
				map.put("result", false);
				map.put("reason", value.getValue() + "已被删除，请重新加入购物车");
				return map;
			}
			if(cart.getAmount()>value.getHsStock()){
				map.put("result", false);
				map.put("reason", product.getTitle() + "库存不足,剩余库存为：" + value.getHsStock());
				return map;
			}
			SpecificationValueStoreLink link = specificationValueStoreLinkMapper.selectByValueAndStoreId(value.getValueId(),orderPost.getStoreId());
			if(link==null){
				map.put("result", false);
				map.put("reason", "非法数据，请返回购物车重新提交");
				return map;
			}
			OrderItem item = new OrderItem();
			item.setAmount(cart.getAmount().shortValue());
			item.setValueId(cart.getSpecificationValueId());
			item.setValue("款式：" + value.getValue());
			item.setItemSerialNum(NumberUtil.generateSerialNum());
			item.setPrice(value.getHsGoodsPrice());
			item.setProductId(cart.getProductId());
			item.setStoreId(orderPost.getStoreId());
			ProductImageLink productImageLink = productImageLinkMapper.selectByProductIdLimit(cart.getProductId());
			if(productImageLink!=null){
				item.setImageUrl(productImageLink.getImageUrl());
			}
			totalMoney = totalMoney.add(new BigDecimal(cart.getAmount()).multiply(value.getHsGoodsPrice()));
			//记录销量
			try {
				ProductSale tmp = productSaleMapper.selectByProductId(cart.getProductId());
				if(tmp==null){
					ProductSale sale = new ProductSale();
					sale.setCount(cart.getAmount());
					sale.setProductId(cart.getProductId());
					productSaleMapper.insertSelective(sale);
				}else{
					tmp.setCount(tmp.getCount()+cart.getAmount());
					productSaleMapper.updateByPrimaryKeySelective(tmp);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			orderItem.add(item);
		}
		order.setTotalPrice(totalMoney);
		if(orderPost.getIsSelfPick()!=null&&orderPost.getIsSelfPick().equals("false")){
			order.setShippingCost(orderPost.getShippingFee());
			totalMoney = totalMoney.add(orderPost.getShippingFee());
		}
		//洪石礼券减免
		if(orderPost.getVoucherCode()!=null){
			try {
				List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(orderPost.getVoucherCode());
				if(coupon!=null&&coupon.size()>0){
					order.setVoucherCode(orderPost.getVoucherCode());
					totalMoney = totalMoney.subtract(coupon.get(0).getPayQuota());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				map.put("result", false);
				map.put("reason", "网络繁忙，请稍后重试");
				return map;
			}
		}
		//创建支付单
		PaymentOrder paymentOrder = new PaymentOrder();
		if(totalMoney.compareTo(new BigDecimal(0))>0){
			paymentOrder.setMoney(totalMoney);
		}else{
			paymentOrder.setMoney(new BigDecimal(0));
		}
		paymentOrder.setPaymentId(0);
		paymentOrder.setPaymentSerialNum(NumberUtil.generateSerialNum());
		paymentOrder.setUserId(userId);
		paymentOrder.setIsSync(false);
		paymentOrder.setIsCompleted(false);
		//插入支付单
		boolean insertResult = true;
		if(paymentOrderMapper.insertSelective(paymentOrder)>0){
			order.setPaymentOrderId(paymentOrder.getPaymentOrderId());
			//插入订单
			if(orderMapper.insertSelective(order)>0){
				
				//插入订单项
				for(OrderItem item : orderItem){
					item.setOrderId(order.getOrderId());
					if(orderItemMapper.insertSelective(item)>0){
						
					}else{
						insertResult=false;
					}
				}
			}else{
				insertResult=false;
			}
		}
		if(!insertResult){
			map.put("result", false);
			map.put("reason", "网络繁忙，请稍后重试");
			return map;
		}
		//删除购物车
		for(String cartId : cartIds){
			//减库存
			Cart cart = cartMapper.selectByPrimaryKey(Integer.valueOf(cartId));
			SpecificationValue value = specificationValueMapper.selectByPrimaryKey(cart.getSpecificationValueId());
			logger.info(JSON.toJSONString(cart));
			logger.info(JSON.toJSONString(value));
			if(cart!=null&&value!=null){
				value.setHsStock(value.getHsStock()-cart.getAmount());
				logger.info(JSON.toJSONString(value));
				specificationValueMapper.updateByPrimaryKeySelective(value);
			}
			cartMapper.deleteByPrimaryKey(Integer.valueOf(cartId));
		}
		map.put("result", true);
		map.put("paymentSerialNum", paymentOrder.getPaymentSerialNum());
		return map;
	}

	/** 
	* @Title: getUnpayOrderListByUserId 
	* @Description: 取得当前用户未支付订单列表
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<Order> getUnpayOrderListByUserId(Integer userId) {
		List<Order> orders = orderMapper.getUnpayOrderListByUserId(userId);
		for(Order order:orders){
			if(order.getVoucherCode()!=null&&!order.getVoucherCode().equals("")){
				List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(order.getVoucherCode());
				if(coupon!=null&&coupon.size()>0){
					order.setDiscount(coupon.get(0).getPayQuota());
				}else{
					order.setDiscount(new BigDecimal(0));
				}
			}else{
				order.setDiscount(new BigDecimal(0));
			}
			order.setCreateTimeStr(DateUtils.format(order.getCreateTime(), DateUtils.FORMAT_LONG));
			NapaStore store = napaStoreMapper.selectByPrimaryKey(order.getStoreId());
			if(store!=null){
				order.setStoreName(store.getStoreName());
			}
		}
		return orders;
	}

	/** 
	* @Title: getInvitationList 
	* @Description: 取得用户的分销下级列表
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<UserInvitedLink> getInvitationList(Integer userId) {
		List<UserInvitedLink> links = userInvitedLinkMapper.selectByUserId(userId);
		for(UserInvitedLink link : links){
			link.setTimeStr(DateUtils.format(link.getInviteTime(), DateUtils.FORMAT_SHORT));
			UserProfile userProfile = userProfileMapper.selectByUserId(link.getInvitedId());
			link.setUserProfile(userProfile);
		}
		return links;
	}

	/** 
	* @Title: getInvitationOrder 
	* @Description: 取得用户分销订单列表 
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<Order> getInvitationOrder(Integer userId) {
		List<Order> orders = orderMapper.getInvitationOrder(userId);
		for(Order order:orders){
			order.setCreateTimeStr(DateUtils.format(order.getCreateTime(), DateUtils.FORMAT_LONG));
			if(order.getFirstDistId()!=null&&order.getFirstDistId().equals(userId)){
				order.setDistLevel(1);
			}else if(order.getSecondDistId()!=null&&order.getSecondDistId().equals(userId)){
				order.setDistLevel(2);
			}
			UserProfile userProfile = userProfileMapper.selectByUserId(order.getUserId());
			order.setUserProfile(userProfile);
			order.setPayTimeStr(DateUtils.format(order.getPayTime(), DateUtils.FORMAT_LONG));
		}
		return orders;
	}

	/** 
	* @Title: getTermGroups 
	* @Description: 取得首页对应的产品系想你 
	* @param @param tags
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<ProductGroup> getTermGroups(String[] tags) {
		List<ProductGroup> groups = productGroupMapper.selectByTags(tags);
		for(ProductGroup group : groups){
			List<ProductDto> products = new ArrayList<ProductDto>();
			List<ProductGroupLink> links  = productGroupLinkMapper.selectByGroupId(group.getGroupId());
			for(ProductGroupLink link : links){
				ProductDto productDto = productMapper.getProductById(link.getProductId());
				if(productDto!=null){
					ProductImageLink productImageLink = productImageLinkMapper.selectByProductIdLimit(link.getProductId());
					if(productImageLink!=null){
						productDto.setImage(productImageLink.getImageUrl());
					}
					SpecificationValue value = specificationValueMapper.selectByProductIdLimit(link.getProductId());
					if(value!=null){
						productDto.setPrice(value.getHsGoodsPrice());
					}
					products.add(productDto);
				}
			}
			group.setProducts(products);	
		}
		return groups;
	}

	/** 
	* @Title: getAddrList 
	* @Description: 取得用户的地址列表
	* @param @param userId
	* @param @param page
	* @param @param pageSize
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public PageInfo<DeliverAddr> getAddrList(Integer userId, Integer page, Integer pageSize) {
		PageHelper.startPage(page, pageSize);
		List<DeliverAddr> addr = deliverAddrMapper.selectByUserId(userId);
		PageInfo<DeliverAddr> pageInfo = new PageInfo<DeliverAddr>(addr);
		return pageInfo;
	}

	/** 
	* @Title: selectCartByIds 
	* @Description: 根据cartId取得对应的产品信息，用户提交订单 
	* @param @param userId
	* @param @param cart
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<CartDto> selectCartByIds(Integer userId,List<CartDto> cart) {
		List<CartDto> result = new ArrayList<CartDto>();
		for(CartDto item:cart){
			CartDto tmp = cartMapper.selectByUserIdAndCartId(userId, item.getCartId());
			if(tmp!=null){
				String specifcationStr = "款式：";
				SpecificationValue specificationValue = specificationValueMapper.selectByPrimaryKey(tmp.getSpecificationValueId());
				if(specificationValue!=null){
					tmp.setStock(specificationValue.getHsStock());
					specifcationStr = specifcationStr + "" + specificationValue.getValue();
					tmp.setMoney(specificationValue.getHsGoodsPrice());
					tmp.setSpecification(specifcationStr);
				}
				List<ProductDto> products  = productMapper.selectOneImage(tmp.getProductId());
				if(products.size()>0){
					tmp.setTitle(products.get(0).getTitle());
					tmp.setImage(products.get(0).getImage());
				}
				result.add(tmp);
			}
		}
		return result;
	}

	@Override
	public DeliverAddr selectAddrById(Integer addrId) {
		return deliverAddrMapper.selectByPrimaryKey(addrId);
	}

	@Override
	public List<Payment> selectAllPayment() {
		return paymentMapper.selectAllForPayment();
	}

	@Override
	public List<Order> selectOrderByPaymentSerialNum(Integer userId,String paymentSerialNum) {
		List<Order> orders = orderMapper.selectByPaymentSerialNum(userId,paymentSerialNum);
		return orders;
	}

	@Override
	public PaymentOrder selectPaymentOrderBySerialNum(String paymentSerialNum) {
		return paymentOrderMapper.selectByPaymentSerialNum(paymentSerialNum);
	}

	/** 
	* @Title: getHongShiVip 
	* @Description: 根据用户id取得洪石对应的vip
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public HongShiVip getHongShiVip(Integer userId) {
		OauthLogin login = oauthLoginMapper.selectByUserId(userId);
		if(login!=null){
			List<HongShiVip> ret = hongShiVipService.getVipInfo(login.getOauthId());
			if(ret!=null&&ret.size()>0){
				return ret.get(0);
			}
		}
		return null;
	}

	@Override
	public int updatePaymentOrder(PaymentOrder paymentOrder) {
		return paymentOrderMapper.updateByPrimaryKeySelective(paymentOrder);
	}

	/** 
	* @Title: getHongShiOrder 
	* @Description: 根据用户id取得洪石订单列表 
	* @param @param userId 微商城用户id
	* @param @param isEnd 是否结单
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<HongShiOrder> getHongShiOrder(Integer userId,Boolean isEnd) {
		OauthLogin oauthLogin = oauthLoginMapper.selectByUserId(userId);
		if (oauthLogin!=null) {
			List<HongShiOrder> orders = hongShiMapper.getHongShiOrder(oauthLogin.getOauthId(),isEnd);
			for (HongShiOrder order : orders) {
				BigDecimal discount = new BigDecimal(0);
				BigDecimal total = new BigDecimal(0);
				List<HongShiOrderItem> orderItems = hongShiMapper.getHongShiOrderItems(order.getId());
				for (HongShiOrderItem item : orderItems) {
					HongShiGoods goods = hongShiMapper.getHongShiGoods(item.getCode());
					if (goods != null) {
						ProductImageLink link = productImageLinkMapper.selectByHongShiGoodsCodeLimit(goods.getCode());
						if (link != null) {
							goods.setImage(link.getImageUrl());
						}
					}
					item.setHongShiGoods(goods);
					total = total.add(new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getCount())));
				}
				order.setOrderItems(orderItems);
				Order tmp = orderMapper.selectBySerialNum(order.getOuterOrderCode());
				if(tmp!=null){
					if(tmp.getVoucherCode()!=null){
						try {
							List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(tmp.getVoucherCode());
							if(coupon!=null&&coupon.size()>0){
								discount = coupon.get(0).getPayQuota();
							}else{
							}
						} catch (Exception e) {
						}
					}
					order.setShippingCost(tmp.getShippingCost());
					order.setIsSelfPick(tmp.getIsSelfPick());
					NapaStore napaStore = napaStoreMapper.selectByPrimaryKey(tmp.getStoreId());
					if(napaStore!=null){
						order.setPickAddr(napaStore.getProvince()+napaStore.getCity()+napaStore.getRegion()+napaStore.getAddrDetail());
					}
				}
				if(order.getShippingCost()==null){
					order.setShippingCost(new BigDecimal(0));
				}
				order.setDiscount(discount);
				order.setTotalAmount(total.doubleValue());
				order.setAccounts(total.add(order.getShippingCost()).subtract(discount).doubleValue());
			}
			return orders;
		}
		return null;
	}

	/** 
	* @Title: selectCouponById 
	* @Description: 取得用户可用的优惠券列表
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<HongShiCoupon> selectCouponById(Integer userId) {
		OauthLogin login = oauthLoginMapper.selectByUserId(userId);
		List<HongShiCoupon> coupons = hongShiMapper.getHongShiCoupon(login.getOauthId());
		List<HongShiCoupon> couponsRet = new ArrayList<HongShiCoupon>();
		for(HongShiCoupon coupon:coupons){
			logger.info(JSON.toJSONString(orderMapper));
			logger.info(JSON.toJSONString(coupon));
			if (coupon!=null) {
				List<Order> order = orderMapper.selectByVoucherCode(coupon.getVouchersCode());
				if (order == null || order.size() == 0) {
					couponsRet.add(coupon);
				} 
			}
		}
		return couponsRet;
	}

	/** 
	* @Title: signInHandler 
	* @Description: 签到处理 
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String,Object> signInHandler(Integer userId) {
		Map<String,Object> map = new TreeMap<String,Object>();
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		SignRecord existed = signRecordMapper.selectToday(userId,today);
		if(existed!=null){
			map.put("existed", true);
			return map;
		}
		SignRecord record = new SignRecord();
		Config config = configMapper.getByTag(WebConfig.signInPoint);
		record.setUserId(userId);
		try {
			record.setPoint(Integer.parseInt(config.getValue()));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			map.put("result", false);
			return map;
		}
		if(signRecordMapper.insertSelective(record)>0){
			//同步积分到洪石系统
			OauthLogin oauthLogin = oauthLoginMapper.selectByUserId(userId);
			if(oauthLogin!=null){
				hongShiMapper.signInAddPoint(oauthLogin.getOauthId(),record.getPoint());
			}
			
			map.put("result", true);
			map.put("point", config.getValue());
		}else{
			map.put("result", false);
		}
		return map;
	}
	
	@Override
	public Integer getChoujiangResult(Integer userId) {
		int a[] = {3888,3083,3485,2679,2276,1870,4291,4694};
		java.util.Random random=new java.util.Random();// 定义随机类
		int result=random.nextInt(7);// 返回[0,10)集合中的整数，注意不包括10
		return a[result];
	}

	/** 
	* @Title: getVipImage 
	* @Description: 取得会员对应的二维码 
	* @param @param getcVipCode
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public String getVipImage(String getcVipCode, Integer userId) {
		UserProfile profile = userProfileMapper.selectByUserId(userId);
		File file = MyQRCode.generateQRCode(600,600,"http://weixin.qq.com/r/"+getcVipCode);
		String vipImage = fDFSFileUpload.getFileId(file);
		if(profile!=null){
			profile.setVipImage(vipImage);
			userProfileMapper.updateByPrimaryKeySelective(profile);
		}
		return vipImage;
	}
	@Override
	public String getVipJbarcode(String getcVipCode, Integer userId) {
		UserProfile profile = userProfileMapper.selectByUserId(userId);
		File file = BarcodeUtil.generateFile(getcVipCode, System.getProperty("java.io.tmpdir")+".png");
		String vipImage = fDFSFileUpload.getFileId(file);
		logger.info(vipImage);
		if(profile!=null){
			profile.setVipImage(vipImage);
			userProfileMapper.updateByPrimaryKeySelective(profile);
		}
		file.delete();
		return vipImage;
	}

	/** 
	* @Title: distCenter 
	* @Description: 分销中心数据
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> distCenter(Integer userId) {
		Map<String,Object> ret = new TreeMap<String,Object>();
		User user = userMapper.selectByPrimaryKey(userId);
		if(user!=null){
			ret.put("serialNum", user.getSerialNum());
		}
		Balance balance = this.getBalance(userId);
		if(balance!=null){
			ret.put("money", balance.getBalance());
		}
		return ret;
	}

	@Override
	public Balance selectBalanceByUserId(Integer userId) {
		return balanceMapper.selectByUserId(userId);
	}
	@Override
	public PaymentStrategyResult getAlipayForFastPay(PaymentOrder paymentOrder,String title,String return_url) {
		logger.info("进入支付宝支付");
		PaymentStrategyResult alipaymentResult = new PaymentStrategyResult();
		Map<String,String> config = getAlipayConfig();
		String notify_url = config.get("alipayNotifyUrl");
		alipaymentResult.setHtml(AlipaySubmit.buildRequest("get", "确认", paymentOrder.getPaymentSerialNum(), title, paymentOrder.getMoney().toString(), "hs.uclee.com", datasource.getDataSourceStr(),notify_url,return_url, config));
		alipaymentResult.setType("alipay");
		alipaymentResult.setResult(true);
		return alipaymentResult;
	}

	@Override
	public PaymentOrder getPaymentOrderBySerialNum(String paymentSerialNum) {
		return paymentOrderMapper.selectByPaymentSerialNum(paymentSerialNum);
	}

	/** 
	* @Title: getLotteryConfig 
	* @Description: 获取抽奖配置类 
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public List<LotteryDrawConfig> getLotteryConfig() {
		logger.info("in");
		List<LotteryDrawConfig> configs = lotteryDrawConfigMapper.selectAll();
		for(LotteryDrawConfig config:configs){
			if (config.getCount()>=1) {
				if (config.getVoucherCode() != null) {
					List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByGoodsCode(config.getVoucherCode());
					if (coupon != null && coupon.size() > 0) {
						config.setText(coupon.get(0).getPayQuota().setScale(2, BigDecimal.ROUND_FLOOR) + "现金优惠券");
					} else {
						config.setText("再接再励");
					}
				} else {
					config.setText(config.getMoney().toString() + "元会员卡余额");
				} 
			}else{
				config.setText("再接再励");
			}
		}
		return configs;
	}

	/** 
	* @Title: memberCardPaymentHandler 
	* @Description: 会员卡支付处理
	* @param @param paymentOrder
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public PaymentStrategyResult memberCardPaymentHandler(PaymentOrder paymentOrder) {
		PaymentStrategyResult result = new PaymentStrategyResult();
		try {
			OauthLogin oauthLogin = oauthLoginMapper.selectByUserId(paymentOrder.getUserId());
			//更新订单状态
			List<Order> orders = this.selectOrderByPaymentSerialNum(paymentOrder.getUserId(), paymentOrder.getPaymentSerialNum());
			for(Order order:orders){
				
				//调用存储过程插入洪石订单
				int hongShiResult=0;
				try {
					HongShiCreateOrder createOrderData = new HongShiCreateOrder();
					NapaStore napaStore = napaStoreMapper.selectByPrimaryKey(order.getStoreId());
					if(napaStore!=null){
						createOrderData.setDepartment(napaStore.getHsCode());
					}else{
						logger.info("加盟店不存在");
					}
					createOrderData.setPickUpTime(order.getPickTime());
					createOrderData.setCallNumber(order.getPhone());
					createOrderData.setDestination(order.getProvince()+order.getCity()+order.getRegion()+order.getAddrDetail()+"(收货人:" + order.getName()+")");
					createOrderData.setOrderCode(null);
					createOrderData.setRemarks(order.getRemark());
					createOrderData.setWeiXinCode(oauthLogin.getOauthId());
					createOrderData.setWSC_TardNo(order.getOrderSerialNum());
					BigDecimal total = order.getTotalPrice();
					createOrderData.setPayment(paymentOrder.getMoney());
					if(order.getVoucherCode()!=null&&!order.getVoucherCode().equals("")){
						List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(order.getVoucherCode());
						if(coupon!=null&&coupon.size()>0){
							createOrderData.setVoucher(coupon.get(0).getPayQuota());
						}else{
							createOrderData.setVoucher(new BigDecimal(0));
						}
					}else{
						createOrderData.setVoucher(new BigDecimal(0));
					}
					
					total = total.add(order.getShippingCost());
					createOrderData.setTotalAmount(total);
					CreateOrderResult createOrderResult = hongShiMapper.createOrder(createOrderData);
					//回收礼券
					try {
						if(createOrderResult!=null){
							List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByCode(order.getVoucherCode());
							if(coupon!=null&&coupon.size()>0){
								hongShiMapper.recoverVoucher(coupon.get(0).getGoodsCode(),createOrderResult.getOrderID(),order.getVoucherCode(),"微商城使用单号："+order.getOrderSerialNum());
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					order.setStatus((short)2);
					//调用存储过程插入订单明细
					List<OrderItem> items = orderItemMapper.selectByOrderId(order.getOrderId());
					for(OrderItem item:items){
						HongShiCreateOrderItem createOrderItem = new HongShiCreateOrderItem();
						SpecificationValue value = specificationValueMapper.selectByPrimaryKey(item.getValueId());
						if(value!=null){
							createOrderItem.setGoodsCode(value.getHsGoodsCode());
						}
						createOrderItem.setGoodsCount(item.getAmount().intValue());
						createOrderItem.setpId(createOrderResult.getOrderID());
						createOrderItem.setPrice(item.getPrice());
						createOrderItem.setTotalAmount(item.getPrice());
						hongShiMapper.createOrderItem(createOrderItem);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//处理分销
				try {
					Config firstDistConfig = configMapper.getByTag(WebConfig.firstDis);
					Config secondDistConfig = configMapper.getByTag(WebConfig.secondDis);
					User firstDist = userMapper.selectByInvitedId(order.getUserId());
					if(firstDist!=null){
						//一级分销
						Balance firstBalance = this.getBalance(firstDist.getUserId());
						if(firstBalance!=null&&firstDistConfig!=null){
							BigDecimal firstMoney;
							try {
								firstMoney = order.getTotalPrice().multiply(new BigDecimal((firstDistConfig.getValue())).divide(new BigDecimal(100)));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								firstMoney=new BigDecimal(0.01);
							}
							firstBalance.setBalance(firstBalance.getBalance().add(firstMoney));
							if(balanceMapper.updateByPrimaryKeySelective(firstBalance)>0){
								//记录日志
								BalanceLog log = new BalanceLog();
								log.setUserId(firstDist.getUserId());
								log.setBalance(firstBalance.getBalance());
								log.setMoney(firstMoney);
								balanceLogMapper.insertSelective(log);
								//记录订单分销信息
								order.setFirstDistId(firstDist.getUserId());
								order.setFirstDistMoney(firstMoney);
							}
						}
						
						//二级分销
						User secondDist = userMapper.selectByInvitedId(firstDist.getUserId());
						Balance secondBalance = this.getBalance(secondDist.getUserId());
						if(secondDist!=null&&secondBalance!=null&&secondDistConfig!=null){
							BigDecimal secondMoney;
							try {
								secondMoney = order.getTotalPrice().multiply(new BigDecimal((secondDistConfig.getValue())).divide(new BigDecimal(100)));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								secondMoney=new BigDecimal(0.01);
							}
							secondBalance.setBalance(secondBalance.getBalance().add(secondMoney));
							if(balanceMapper.updateByPrimaryKeySelective(secondBalance)>0){
								//记录日志
								BalanceLog log = new BalanceLog();
								log.setUserId(secondDist.getUserId());
								log.setBalance(secondBalance.getBalance());
								log.setMoney(secondMoney);
								balanceLogMapper.insertSelective(log);
								//记录订单分销信息
								order.setFirstDistId(secondDist.getUserId());
								order.setFirstDistMoney(secondMoney);
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				order.setSyncStatus(hongShiResult);
				orderMapper.updateByPrimaryKeySelective(order);
				//发送购买成功短信
				String[] key = {"keyword1","keyword2","keyword3","keyword4"};
				Payment payment = paymentMapper.selectByPrimaryKey(paymentOrder.getPaymentId());
				String paymentMethod="微信支付";
				if(payment!=null){
					if(payment.getStrategyClassName().equals("MemberCardPaymentStrategy")){
						paymentMethod="会员卡余额支付";
					}else if(payment.getStrategyClassName().equals("AlipayPaymentStrategy")){
						paymentMethod="支付宝支付";
					}
				}
				String[] value = {paymentOrder.getPaymentSerialNum(),DateUtils.format(new Date(), DateUtils.FORMAT_LONG).toString(),paymentOrder.getMoney()+"元".toString(),paymentMethod};
				sendWXMessage(oauthLogin.getOauthId(), "S3vfLhEEbVICFmwgpHedYUtlm7atyY3zl-GxJYY20ok", "hs.uclee.com/order-list", "尊敬的会员，您有一笔订单已经支付成功", key,value, "感谢您的惠顾");
				//改变支付单状态
				paymentOrder.setCompleteTime(new Date());
				paymentOrder.setIsCompleted(true);
				paymentOrder.setIsSync(true);
				this.updatePaymentOrder(paymentOrder);
			}
			result.setResult(true);
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.setResult(false);
			return result;
		}
	}

	@Override
	public List<Freight> getAllFreightConfig() {
		return freightMapper.selectAll();
	}
	
	/** 
	* @Title: tranferBalance 
	* @Description: 微商城余额转进会员卡 
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> tranferBalance(Integer userId) {
		Map<String,Object> map = new TreeMap<String,Object>();
		Balance balance = this.getBalance(userId);
		map.put("result", false);
		if(balance!=null){
			BigDecimal money = balance.getBalance();
			OauthLogin oauthLogin = oauthLoginMapper.selectByUserId(userId);
			if(oauthLogin!=null){
				//更新余额
				balance.setBalance(new BigDecimal(0));
				balanceMapper.updateByPrimaryKeySelective(balance);
				//充值到会员卡
				HongShiRecharge dd = new HongShiRecharge().setcWeiXinCode(oauthLogin.getOauthId())
						.setcWeiXinOrderCode("微商城分销佣金转入").setnAddMoney(money);
				Integer res = hongShiVipService.hongShiRecharge(dd);
				map.put("result", true);
			}
		}
		return map;
	}
	
	

	/** 
	* @Title: lotteryHandler 
	* @Description: 抽奖结果处理 
	* @param @param userId
	* @param @param configCode
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String,Object> lotteryHandler(Integer userId, String configCode) {
		Map<String,Object> map = new TreeMap<String,Object>();
		LotteryDrawConfig config = lotteryDrawConfigMapper.selectByConfigCode(configCode);
		OauthLogin oauthLogin = oauthLoginMapper.getOauthLoginInfoByUserId(userId);
		if (config!=null&&oauthLogin!=null) {
			//减去积分
			Config webConfig = getLotteryWebConfig();
			if(webConfig!=null){
				logger.info(JSON.toJSONString("进入抽奖扣除积分"));
				int point = 1;
				try {
					point = Integer.parseInt(webConfig.getValue());
					logger.info(JSON.toJSONString("进入抽奖扣除积分1"));
					hongShiMapper.lotteryPoint(oauthLogin.getOauthId(),-point);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					map.put("result", false);
					map.put("text", "网络错误，请稍后重试");
					return map;
				}
			}
			//记录抽奖记录
			LotteryRecord record = new LotteryRecord();
			record.setTime(new Date());
			record.setUserId(userId);
			lotteryRecordMapper.insertSelective(record);
			if (config.getCount()>0) {
				if (config.getVoucherCode() != null) {
					//抽奖优惠券处理
					List<HongShiCoupon> coupon = hongShiMapper.getHongShiCouponByGoodsCode(config.getVoucherCode());
					if (coupon != null && coupon.size() > 0) {
						try {
							hongShiMapper.saleVoucher(oauthLogin.getOauthId(), coupon.get(0).getVouchersCode(),
									config.getVoucherCode());
							map.put("result", true);
							map.put("text", "恭喜抽中" + coupon.get(0).getPayQuota().setScale(2, BigDecimal.ROUND_HALF_UP)
									+ "现金优惠券，奖品已放入账户中，请注意查看");
							return map;
						} catch (Exception e) {
							map.put("result", true);
							map.put("text", "再接再励");
							return map;
						}
					} else {
						map.put("result", true);
						map.put("text", "再接再励");
					}
				} else if (config.getMoney() != null) {
					//抽奖余额处理
					HongShiRecharge dd = new HongShiRecharge().setcWeiXinCode(oauthLogin.getOauthId())
							.setcWeiXinOrderCode("微商城积分抽奖赠送余额").setnAddMoney(config.getMoney());
					Integer res = hongShiVipService.hongShiRecharge(dd);
					map.put("text", "恭喜抽中" + config.getMoney() + "会员卡余额，奖品已充值到账户中，请注意查看");
					map.put("result", true);
					return map;
				} 
				config.setCount(config.getCount()-1);
				lotteryDrawConfigMapper.updateByPrimaryKeySelective(config);
			}else{
				map.put("result", true);
				map.put("text", "再接再励");
				return map;
			}
		}
		return map;
	}

	@Override
	public Config getLotteryWebConfig() {
		return configMapper.getByTag(WebConfig.drawPoint);
	}

	/** 
	* @Title: cardAddHandler 
	* @Description: 添加购物车处理 
	* @param @param userId
	* @param @param cartId
	* @param @param amount
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> cardAddHandler(Integer userId,Integer cartId, Integer amount) {
		Map<String,Object> map = new TreeMap<String,Object>();
		Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
		if(cart!=null){
			cart.setAmount(amount);
			cartMapper.updateByPrimaryKey(cart);
			map.put("result", true);
		}else{
			map.put("result", false);
		}
		return map;
	}
	/** 
	* @Title: cardDelHandler 
	* @Description: 删除购物车处理 
	* @param @param userId
	* @param @param cartId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> cardDelHandler(Integer userId,Integer cartId) {
		Map<String,Object> map = new TreeMap<String,Object>();
		Cart cart = cartMapper.selectByUserIdAndCartId(userId, cartId);
		if(cart!=null){
			cartMapper.deleteByPrimaryKey(cart.getCartId());
			map.put("result", true);
		}else{
			map.put("result", false);
		}
		return map;
	}
	/** 
	* @Title: getAddrInfo 
	* @Description: 取得地址相信信息 
	* @param @param userId
	* @param @param deliverAddrId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public DeliverAddr getAddrInfo(Integer userId, Integer deliverAddrId) {
		DeliverAddr addr = deliverAddrMapper.selectByUserAndAddrId(userId,deliverAddrId);
		logger.info(JSON.toJSONString(addr));
		Province province = provinceMapper.selectByProvince(addr.getProvince());
		City city = cityMapper.selectByCity(addr.getCity());
		Region region = regionMapper.selectByRegionAndCityId(addr.getRegion(),city.getCityId());
		if(province!=null&&city!=null&&region!=null){
			addr.setProvinceId(province.getProvinceId());
			addr.setCityId(city.getCityId());
			addr.setRegionId(region.getRegionId());
		}
		return addr;
	}

	@Override
	public List<City> getCitiesByStr(String province) {
		Province ret = provinceMapper.selectByProvince(province);
		if(ret!=null){
			return cityMapper.selectByProvinceId(ret.getProvinceId());
		}
		return null;
	}

	@Override
	public List<Region> getRegionsByStr(String city) {
		City ret = cityMapper.selectByCity(city);
		if(ret!=null){
			return regionMapper.selectByCityId(ret.getCityId());
		}
		return null;
	}

	@Override
	public String getStoreAddr(Integer storeId) {
		NapaStore store = napaStoreMapper.selectByPrimaryKey(storeId);
		if(store!=null){
			return store.getProvince()+store.getCity()+store.getRegion()+store.getAddrDetail();
		}
		return null;
	}

	@Override
	public NapaStore getNapaStore(Integer storeId) {
		return napaStoreMapper.selectByPrimaryKey(storeId);
	}

	@Override
	public List<Banner> selectAllBanner() {
		return bannerMapper.selectAll();
	}

	@Override
	public List<LotteryRecord> getUserLotteryRecord(Integer userId) {
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		return lotteryRecordMapper.selectTodayByUserId(userId,today);
	}

	@Override
	public int delOrder(String orderSerialNum) {
		Order order = orderMapper.selectBySerialNum(orderSerialNum);
		orderMapper.deleteByOrderSerialNum(orderSerialNum);
		if(order!=null){
			List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getOrderId());
			logger.info(JSON.toJSONString(orderItems));
			for(OrderItem orderItem : orderItems){
				SpecificationValue value = specificationValueMapper.selectByPrimaryKey(orderItem.getValueId());
				if(value!=null){
					value.setHsStock(value.getHsStock()+orderItem.getAmount());
					logger.info(JSON.toJSONString(value));
					specificationValueMapper.updateByPrimaryKeySelective(value);
				}
			}
		}
		return 1;
	}

	@Override
	public Boolean getNapaStoreByPhone(String phone) {
		List<NapaStore> napaStore = napaStoreMapper.selectByPhone(phone);
		if(napaStore!=null&&napaStore.size()>0){
			return true;
		}
		return false;
	}
	@Override
	public Map<String,String> getWeixinConfig() {
		Map<String,String> map = new HashMap<String,String>();
		List<Config> configs = configMapper.getWeixinConfig();
		for(Config config:configs){
			map.put(config.getTag(), config.getValue());
		}
		return map;
	}
	@Override
	public Map<String,String> getSMSConfig() {
		Map<String,String> map = new HashMap<String,String>();
		List<Config> configs = configMapper.getSMSConfig();
		for(Config config:configs){
			map.put(config.getTag(), config.getValue());
		}
		return map;
	}
	@Override
	public Map<String,String> getAlipayConfig() {
		Map<String,String> map = new HashMap<String,String>();
		List<Config> configs = configMapper.getAlipayConfig();
		for(Config config:configs){
			map.put(config.getTag(), config.getValue());
		}
		return map;
	}

	/** 
	* @Title: getShakeRecord 
	* @Description: 摇一摇记录
	* @param @return    设定文件  
	* @throws 
	*/
	
	@Override
	public Map<String, Object> getShakeRecord() {
		Map<String,Object> map = new TreeMap<String,Object>();
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		List<ShakeRecord> record = shakeRecordMapper.selectToday(today);
		map.put("shakeRecords", record);
		return map;
	}

	/** 
	* @Title: shakeHandler 
	* @Description: 摇一摇处理 
	* @param @param userId
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public boolean shakeHandler(Integer userId) {
		/*Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);
        Date today = todayStart.getTime();*/
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
        List<ShakeRecord> shakeRecord = shakeRecordMapper.selectTodayByUserId(userId, today);
        logger.info(JSON.toJSONString(shakeRecord));
        if(shakeRecord==null||shakeRecord.size()==0){
        	ShakeRecord tmp = new ShakeRecord();
        	tmp.setIsShow(false);
        	tmp.setTime(new Date());
        	tmp.setUserId(userId);
        	shakeRecordMapper.insertSelective(tmp);
        }
		return true;
	}

	@Override
	public List<Payment> selectMemberPayment() {
		return paymentMapper.selectMemberPayment();
	}

	/** 
	* @Title: getBossCenter 
	* @Description: 老板助手数据
	* @param @param phone
	* @param @param hsCode
	* @param @return    设定文件  
	* @throws 
	*/
	@Override
	public Map<String, Object> getBossCenter(String phone, String hsCode) {
		Map<String, Object> ret = new HashMap<String, Object>();
		Integer userId = null;
		ret.put("result", false);
		if(phone==null&&hsCode==null){
			ret.put("reason", "param_error");
			return ret;
		}
		List<NapaStore> napaStores = napaStoreMapper.selectByPhone(phone);
		if(napaStores!=null&&napaStores.size()>0){
			userId = napaStores.get(0).getUserId();
			ret.put("napaStores", napaStores);
		}else{
			ret.put("reason", "no_department");
			return ret;
		}
		List<BossCenterItem> items = hongShiMapper.selectBossCenter(hsCode,userId);
		for(BossCenterItem item:items){
			if (item.getValue()!=null) {
				if (item.getValueType().equals("monery")) {
					DecimalFormat df1 = new DecimalFormat("0.00");
					item.setValue(new BigDecimal(df1.format(item.getValue())));
				} else {
					DecimalFormat df1 = new DecimalFormat("0");
					item.setValue(new BigDecimal(df1.format(item.getValue())));
				} 
			}else{
				item.setValue(new BigDecimal(0));
			}
		}
		ret.put("items", items);
		ret.put("result", true);
		return ret;
	}

	@Override
	public Map<String, Object> getShakePageData() {
		Map<String,Object> map = new TreeMap<String,Object>();
		Config first = configMapper.getByTag(WebConfig.FIRST_PRIZE);
		Config second = configMapper.getByTag(WebConfig.SECOND_PRIZE);
		Config third = configMapper.getByTag(WebConfig.THIRD_PRIZE);
		map.put("level1Config", first);
		map.put("level2Config", second);
		map.put("level3Config", third);
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		List<ShakeRecord> record = shakeRecordMapper.selectToday(today);
		map.put("shakeRecords", record);
		if(record!=null){
			map.put("total", record.size());
		}else{
			map.put("total", 0);
		}
		List<WinningRecord> level1 = winningRecordMapper.selectLevelOne(today);
		List<WinningRecord> level2 = winningRecordMapper.selectLevelTwo(today);
		List<WinningRecord> level3 = winningRecordMapper.selectLevelThree(today);
		map.put("level1", level1);
		map.put("level2", level2);
		map.put("level3", level3);
		return map;
	}

	@Override
	public Map<String, Object> firstDrawHandler() {
		Map<String,Object> map = new TreeMap<String,Object>();
		Config first = configMapper.getByTag(WebConfig.FIRST_PRIZE);
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		List<WinningRecord> level1tmp = winningRecordMapper.selectLevelOne(today);
		Config configCount = configMapper.getByTag(WebConfig.THIRD_COUNT);
		if(configCount==null){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		Integer count = Integer.parseInt(configCount.getValue());
		Integer last = Integer.parseInt(first.getValue())-level1tmp.size();
		if(count>last){
			count=last;
		}
		if(!(count<=Integer.parseInt(first.getValue()))||!(level1tmp.size()<Integer.parseInt(first.getValue()))){
			map.put("result", false);
			map.put("reason", "limit");
			return map;
		}
		List<ShakeRecord> tmp = shakeRecordMapper.selectToday(today);
		if(tmp==null||tmp.size()==0){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		List<ShakeRecord> win = new ArrayList<ShakeRecord>();
		HashSet<Integer> index = new HashSet<Integer>();
		for(int i=0;i<count;i++){
			if(percentageRandom(0.3,0.7)==0){
				java.util.Random random=new java.util.Random();// 定义随机类
				int result=random.nextInt(tmp.size());// 返回[0,record.size())集合中的整数，注意不包括record.size()
				index.add(result);
			}
		}
		for(Integer i : index){
			ShakeRecord tmpShake = null;
			if (tmp.size()>=i+1) {
				tmpShake = tmp.get(i);
			}
			if (tmpShake!=null) {
				WinningRecord winRecord = new WinningRecord();
				winRecord.setUserId(tmpShake.getUserId());
				winRecord.setWinningLevel(1);
				winRecord.setTime(new Date());
				winningRecordMapper.insertSelective(winRecord);
				win.add(tmpShake);
			}
		}
		map.put("win", win);
		String text = "";
		for(ShakeRecord item : win){
			text = text+item.getNickName()+",";
		}
		if (text.length()>=1) {
			text = text.substring(0, text.length() - 1);
		}
		map.put("text", text);
		List<ShakeRecord> shakeRecord = shakeRecordMapper.selectToday(today);
		map.put("shakeRecord", shakeRecord);
		List<WinningRecord> level1 = winningRecordMapper.selectLevelOne(today);
		map.put("level1", level1);
		map.put("result", true);
		return map;
	}
	@Override
	public Map<String, Object> secondDrawHandler() {
		Map<String,Object> map = new TreeMap<String,Object>();
		Config second = configMapper.getByTag(WebConfig.SECOND_PRIZE);
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		List<WinningRecord> level2tmp = winningRecordMapper.selectLevelTwo(today);
		Config configCount = configMapper.getByTag(WebConfig.THIRD_COUNT);
		if(configCount==null){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		Integer count = Integer.parseInt(configCount.getValue());
		Integer last = Integer.parseInt(second.getValue())-level2tmp.size();
		if(count>last){
			count=last;
		}
		if(!(count<=Integer.parseInt(second.getValue()))||!(level2tmp.size()<Integer.parseInt(second.getValue()))){
			map.put("result", false);
			map.put("reason", "limit");
			return map;
		}
		List<ShakeRecord> tmp = shakeRecordMapper.selectToday(today);
		if(tmp==null||tmp.size()==0){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		List<ShakeRecord> win = new ArrayList<ShakeRecord>();
		HashSet<Integer> index = new HashSet<Integer>();
		for(int i=0;i<count;i++){
			if(percentageRandom(0.6,0.4)==0){
				java.util.Random random=new java.util.Random();// 定义随机类
				int result=random.nextInt(tmp.size());// 返回[0,record.size())集合中的整数，注意不包括record.size()
				index.add(result);
			}
		}
		for(Integer i : index){
			ShakeRecord tmpShake = null;
			if (tmp.size()>=i+1) {
				tmpShake = tmp.get(i);
			}
			if (tmpShake!=null) {
				WinningRecord winRecord = new WinningRecord();
				winRecord.setUserId(tmpShake.getUserId());
				winRecord.setWinningLevel(2);
				winRecord.setTime(new Date());
				winningRecordMapper.insertSelective(winRecord);
				win.add(tmpShake);
			}
		}
		map.put("win", win);
		String text = "";
		for(ShakeRecord item : win){
			text = text+item.getNickName()+",";
		}
		if (text.length()>=1) {
			text = text.substring(0, text.length() - 1);
		}
		map.put("text", text);
		List<ShakeRecord> shakeRecord = shakeRecordMapper.selectToday(today);
		map.put("shakeRecord", shakeRecord);
		List<WinningRecord> level2 = winningRecordMapper.selectLevelTwo(today);
		map.put("level2", level2);
		map.put("result", true);
		return map;
	}
	@Override
	public Map<String, Object> thirdDrawHandler() {
		Map<String,Object> map = new TreeMap<String,Object>();
		map.put("result", true);
		Config third = configMapper.getByTag(WebConfig.THIRD_PRIZE);
		Date today = DateUtils.parse(DateUtils.format(new Date(), DateUtils.FORMAT_SHORT), DateUtils.FORMAT_SHORT);
		List<WinningRecord> level3tmp = winningRecordMapper.selectLevelThree(today);
		Config configCount = configMapper.getByTag(WebConfig.THIRD_COUNT);
		if(configCount==null){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		Integer count = Integer.parseInt(configCount.getValue());
		Integer last = Integer.parseInt(third.getValue())-level3tmp.size();
		if(count>last){
			count=last;
		}
		if(!(count<=Integer.parseInt(third.getValue()))||!(level3tmp.size()<Integer.parseInt(third.getValue()))){
			map.put("result", false);
			map.put("reason", "limit");
			return map;
		}
		List<ShakeRecord> tmp = shakeRecordMapper.selectToday(today);
		if(tmp==null||tmp.size()==0){
			map.put("result", false);
			map.put("reason", "noWinner");
			return map;
		}
		List<ShakeRecord> win = new ArrayList<ShakeRecord>();
		HashSet<Integer> index = new HashSet<Integer>();
		for(int i=0;i<count;i++){
			if(percentageRandom(0.9,0.1)==0){
				java.util.Random random=new java.util.Random();// 定义随机类
				int result=random.nextInt(tmp.size());// 返回[0,record.size())集合中的整数，注意不包括record.size()
				index.add(result);
			}
		}
		for(Integer i : index){
			ShakeRecord tmpShake = null;
			if (tmp.size()>=i+1) {
				tmpShake = tmp.get(i);
			}
			if (tmpShake!=null) {
				WinningRecord winRecord = new WinningRecord();
				winRecord.setUserId(tmpShake.getUserId());
				winRecord.setWinningLevel(3);
				winRecord.setTime(new Date());
				winningRecordMapper.insertSelective(winRecord);
				win.add(tmpShake);
			}
		}
		map.put("win", win);
		String text = "";
		for(ShakeRecord item : win){
			text = text+item.getNickName()+",";
		}
		if (text.length()>=1) {
			text = text.substring(0, text.length() - 1);
		}
		map.put("text", text);
		List<ShakeRecord> shakeRecord = shakeRecordMapper.selectToday(today);
		map.put("shakeRecord", shakeRecord);
		List<WinningRecord> level3 = winningRecordMapper.selectLevelThree(today);
		map.put("level3", level3);
		return map;
	}
	/** 
	  * Math.random()产生一个double型的随机数，判断一下 
	  * 例如0出现的概率为%50，则介于0到0.50中间的返回0 
	     * @return int 
	     * 
	     */ 
	
	 public int percentageRandom(double rate0,double rate1)  
	 {  
	  double randomNumber;  
	  randomNumber = Math.random();  
	  if (randomNumber >= 0 && randomNumber <= rate0)  
	  {  
	   return 0;  
	  }  
	  else if (randomNumber >= rate0 && randomNumber <= rate0 + rate1)  
	  {  
	   return 1;  
	  }
	  return -1;  
	 }

	@Override
	public boolean resetDraw() {
		shakeRecordMapper.reset();
		winningRecordMapper.reset();
		return true;
	}

	@Override
	public String getAppId(String merchantCode) {
		if(merchantCode!=null&&merchantCode.isEmpty()){
			datasource.switchDataSource(merchantCode); 
		}
		Map<String,String> config = getWeixinConfig();
		return config.get(WebConfig.APPID);
	}
	 
}
