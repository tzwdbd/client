package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.domain.AutoOrderCleanCart;
import com.oversea.task.domain.AutoOrderLogin;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.utils.Utils;

public class ManualBuy{
	private Logger logger = Logger.getLogger(getClass());

	protected FirefoxDriver driver = null;

	public static final int WAIT_TIME = 90;
	public static final String path = "C:\\Users\\Administrator\\AppData\\Local\\Temp";

	private List<RobotOrderDetail> orderDetailList;
	private Task task;
	protected TaskResult taskResult;
	
	public void setTaskResult(TaskResult taskResult){
		this.taskResult = taskResult;
	}

	public ManualBuy(){
		init(true,true);
	}
	
	public ManualBuy(boolean isWap){
		init(isWap,true);
	}
	
	public ManualBuy(boolean isWap,boolean isNeedDeleteCooike){
		init(isWap,isNeedDeleteCooike);
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	private void init(boolean isWap,boolean isNeedDeleteCooike){
		logger.debug("--->启动驱动并做清理工作"+isNeedDeleteCooike);
		try{
			if(isWap){
				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("general.useragent.override", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_1_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12B466 MicroMessenger/6.2.4 NetType/WIFI Language/zh_CN");     
				driver = new FirefoxDriver(profile);
			}else{
				driver = new FirefoxDriver();
			}
			if(isNeedDeleteCooike){
				driver.manage().deleteAllCookies();
			}
			driver.manage().window().maximize();
		}catch(Throwable t){
			if(driver != null){
				try{
					driver.quit();
				}catch(Throwable tt){}
			}
		}
	}
	
	public static float add(float v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).floatValue();
    }
	
	public static float sub(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).floatValue();
    }
	
	public List<RobotOrderDetail> getOrderDetailList() {
		return orderDetailList;
	}

	public void setOrderDetailList(List<RobotOrderDetail> orderDetailList) {
		this.orderDetailList = orderDetailList;
	}

	/**
	 * 登录
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public AutoBuyStatus login(String userName, String passWord,AutoOrderLogin autoOrderLogin){
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get(autoOrderLogin.getLoginUrl());
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			loginUser(userName, passWord, autoOrderLogin);
		} catch (Exception e) {
			logger.error(autoOrderLogin.getMallName()+"--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			logger.debug(autoOrderLogin.getMallName()+"--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderLogin.getConfirmCode())));
			logger.debug(autoOrderLogin.getMallName()+"--->登录完成");
		}
		catch (Exception e)
		{
			logger.error(autoOrderLogin.getMallName()+"--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	};
	
	public void loginUser(String userName, String passWord,AutoOrderLogin autoOrderLogin){
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		// 输入账号
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(autoOrderLogin.getUsernameCode())));
		WebElement username = driver.findElement(By.cssSelector(autoOrderLogin.getUsernameCode()));
		logger.debug(autoOrderLogin.getMallName()+"--->输入账号");
		Utils.sleep(1500);
		username.sendKeys(userName);

		// 输入密码
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(autoOrderLogin.getPasswordCode())));
		WebElement passward = driver.findElement(By.cssSelector(autoOrderLogin.getPasswordCode()));
		logger.debug(autoOrderLogin.getMallName()+"--->输入密码");

		Utils.sleep(1500);
		passward.sendKeys(passWord);

		// 提交
		Utils.sleep(1500);
		WebElement submitBtn = driver.findElement(By.cssSelector(autoOrderLogin.getSubmitCode()));
		logger.debug(autoOrderLogin.getMallName()+"--->开始提交");
		submitBtn.click();
	}

	/**
	 * 清空购物车
	 */
	public AutoBuyStatus cleanCart(AutoOrderCleanCart autoOrderCleanCart){
		//跳转到购物车
		try{
			Utils.sleep(1000);
			driver.get(autoOrderCleanCart.getCartUrl());
			logger.error(autoOrderCleanCart.getSiteName()+"--->开始跳转到购物车");
		}catch(Exception e){
			logger.error(autoOrderCleanCart.getSiteName()+"--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try {
			logger.error(autoOrderCleanCart.getSiteName()+"--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderCleanCart.getCartLoadedCode())));
			logger.error(autoOrderCleanCart.getSiteName()+"--->开始清理购物车");
			//一次性清除
			if(autoOrderCleanCart.getCleanType()==1){
				WebElement deleteall = driver.findElement(By.cssSelector(autoOrderCleanCart.getRemoveCode()));
				deleteall.click();
				Utils.sleep(1000);
				if(!StringUtil.isBlank(autoOrderCleanCart.getConfirmCode())){
					driver.findElement(By.cssSelector(autoOrderCleanCart.getConfirmCode())).click();
				}
			}else{
				//循坏清除
				List<WebElement> list = driver.findElements(By.cssSelector(autoOrderCleanCart.getRemoveCode()));
				while (true) {
					int size = list.size();
					logger.error(autoOrderCleanCart.getSiteName()+"--->开始清理"+list.size());
					if(list!=null && size>0){
						list.get(0).click();
						Utils.sleep(2000);
						if(size>1){
							list = driver.findElements(By.cssSelector(autoOrderCleanCart.getRemoveCode()));
						}else{
							break;
						}
					}else{
						break;
					}
				}
				Utils.sleep(2000);
			}
			logger.error(autoOrderCleanCart.getSiteName()+"--->购物车页面清理完成");
		} catch (Exception e) {
			logger.error(autoOrderCleanCart.getSiteName()+"--->购物车页面清理完成");
		}
			
		try {
			logger.error(autoOrderCleanCart.getSiteName()+"--->确认购物车是否清空");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderCleanCart.getCleanEndCode())));
		} catch (Exception e) {
			logger.debug(autoOrderCleanCart.getSiteName()+"--->购物车不为空！");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	};

	/**
	 * 选择商品
	 * @param param
	 * @return
	 */
	public AutoBuyStatus selectProduct(Map<String, String> param){
		return null;
	};
	
	/**
	 * 
	 * @param param
	 * @param address
	 * @return
	 */
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount,List<GiftCard> giftCardList){
		return null;
	}

	/**
	 * 爬取物流公司和单号
	 * @param mallOrderNo
	 * @return
	 */
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		return null;
	};


	public boolean productOrderCheck(TaskResult taskResult){
		return false;
	}
	
	public static void main(String[] args){
		ManualBuy auto = new ManualBuy(false);
		AutoOrderLogin autoOrderLogin = new AutoOrderLogin();
		autoOrderLogin.setLoginUrl("http://cn.getthelabel.com/customer/account/login/");
		autoOrderLogin.setConfirmCode(".main-container");
		autoOrderLogin.setMallName("getthelabel");
		autoOrderLogin.setPasswordCode("#LoginPwd");
		autoOrderLogin.setUsernameCode("#LoginEmail");
		autoOrderLogin.setSubmitCode("#PageLogin");
		AutoBuyStatus status = auto.login("tzwdbd@126.com", "123456",autoOrderLogin);
		System.out.println(status);
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			AutoOrderCleanCart autoOrderCleanCart = new AutoOrderCleanCart();
			autoOrderCleanCart.setCartLoadedCode(".delete-checked");
			autoOrderCleanCart.setCartUrl("http://cn.getthelabel.com/checkout/cart/");
			autoOrderCleanCart.setCleanEndCode(".cart-empty");
			autoOrderCleanCart.setCleanType(1);
			autoOrderCleanCart.setConfirmCode("#easyDialogYesBtn");
			autoOrderCleanCart.setRemoveCode(".delete-checked");
			autoOrderCleanCart.setSiteName("getthelabel");
			status = auto.cleanCart(autoOrderCleanCart);
			//if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				
//				Map<String, String> param = new HashMap<String, String>();
//		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
//				param.put("url", "http://www.rebatesme.com/zh/click/?key=3b27c8fda4c3ff307a5ec0766a50d0a2&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=0995f957e0c90e5563b6b7cd85a70e03&targetUrl=http%3A%2F%2Fcn.getthelabel.com%2F1010315.html%2F");
//				param.put("num", "1");
//				param.put("sku", "[[\"color\",\"白色\"],[\"size\",\"XS\"]]");
//				param.put("productEntityId", "4241869");
//				auto.selectProduct(param);
//				Map<String, String> param1 = new HashMap<String, String>();
//		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
//				param1.put("url", "http://www.rebatesme.com/zh/click/?key=3b27c8fda4c3ff307a5ec0766a50d0a2&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=0995f957e0c90e5563b6b7cd85a70e03&targetUrl=http%3A%2F%2Fcn.getthelabel.com%2F1023646.html");
//				param1.put("num", "2");
//				param1.put("sku", "[[\"color\",\"Blue\"],[\"size\",\"L\"]]");
//				param1.put("productEntityId", "42418619");
//				auto.selectProduct(param1);
//				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "39.99");
//					param0.put("count", "1");
////					param0.put("isPay", String.valueOf(true));
//					param0.put("cardNo", "4662 4833 6029 1396");
//					UserTradeAddress userTradeAddress = new UserTradeAddress();
//					userTradeAddress.setName("刘波");
//					userTradeAddress.setAddress("西斗门路9号");
//					userTradeAddress.setState("浙江省");
//					userTradeAddress.setCity("杭州市");
//					userTradeAddress.setDistrict("西湖区");
//					userTradeAddress.setZip("310000");
//					userTradeAddress.setIdCard("330881198506111918");
//					userTradeAddress.setMobile("18668084980");
//					OrderPayAccount orderPayAccount = new OrderPayAccount();
//					orderPayAccount.setAccount("15268125960");
//					orderPayAccount.setPayPassword("199027");
//					status = auto.pay(param0,userTradeAddress,orderPayAccount);
//				}
			//}
		}
	}
}