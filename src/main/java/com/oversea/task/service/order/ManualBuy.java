package com.oversea.task.service.order;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.AutoOrderCleanCart;
import com.oversea.task.domain.AutoOrderExpressDetail;
import com.oversea.task.domain.AutoOrderLogin;
import com.oversea.task.domain.AutoOrderPay;
import com.oversea.task.domain.AutoOrderScribeExpress;
import com.oversea.task.domain.AutoOrderSelectProduct;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.enums.MoneyUnits;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.utils.ExpressUtils;
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
		WebDriverWait wait = new WebDriverWait(driver, 45);
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
						if(!StringUtil.isBlank(autoOrderCleanCart.getConfirmCode())){
							driver.findElement(By.cssSelector(autoOrderCleanCart.getConfirmCode())).click();
							Utils.sleep(2000);
						}
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
			if(!StringUtil.isBlank(autoOrderCleanCart.getCleanEndCode())){
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderCleanCart.getCleanEndCode())));
			}
		} catch (Exception e) {
			logger.debug(autoOrderCleanCart.getSiteName()+"--->购物车不为空！");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		try {
			driver.navigate().to("https://www.bonpont.com/customer/address/");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-item .item-operation")));
			List<WebElement> deleteList = driver.findElements(By.cssSelector(".card-item .item-operation"));
			while (true) {
				int size = deleteList.size();
				if(deleteList!=null && size>0){
					deleteList.get(0).click();
					WebElement ww = driver.findElement(By.xpath("//a[@class='operation-type' and contains(text(),'删除')]"));
					driver.executeScript("var tar=arguments[0];tar.click();", ww);
					Utils.sleep(500);
					driver.findElement(By.id("easyDialogYesBtn")).click();
					Utils.sleep(500);
					if(size>1){
						deleteList = driver.findElements(By.cssSelector(".card-item .item-operation"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
		} catch (Exception e) {
		}
		
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	};

	/**
	 * 选择商品
	 * @param param
	 * @return
	 */
	public AutoBuyStatus selectProduct(RobotOrderDetail detail, AutoOrderSelectProduct autoOrderSelectProduct){
		logger.debug("--->跳转到商品页面");
		String productUrl = detail.getProductRebateUrl();
		if(StringUtil.isBlank(productUrl)){
			productUrl = detail.getProductUrl();
		}
		logger.debug("productUrl = " + productUrl);
		try{
			driver.navigate().to(productUrl);
			TimeUnit.SECONDS.sleep(5);
		}catch (Exception e){
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, 45);
		//商品详情页加载完成标识
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderSelectProduct.getProductLoadedCode())));
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		try {	
			logger.debug("--->商品页面加载完成");
			// 开始选择sku
			if (!StringUtil.isBlank(detail.getProductSku())) {
				logger.debug("--->开始选择sku");
				logger.debug("--->选择sku完成");
			}
		}catch (Exception e) {
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			//商品单价元素
			WebElement priceFilter = driver.findElement(By.cssSelector(autoOrderSelectProduct.getSinglePriceCode()));
			String units = MoneyUnits.getMoneyUnitsByCode(detail.getUnits()).getValue();
			logger.debug("--->units:"+units);
			String text = priceFilter.getText().trim();
			if (!Utils.isEmpty(text) && text.indexOf(units)!=-1) {
				String priceStr = text.substring(text.indexOf(units) + 1);
				logger.debug("--->找到商品单价  = " + priceStr);
				detail.setSinglePrice(priceStr);
			}else{
				String priceStr = text.substring(units.length());
				logger.debug("--->找到商品单价1  = " + priceStr);
				detail.setSinglePrice(priceStr);
			}
		} catch (Exception e) {
			logger.debug("--->商品单价查找出错",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		String productNum = String.valueOf(detail.getNum());
		// 选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				WebElement inputNum = driver.findElement(By.cssSelector(autoOrderSelectProduct.getNumCode()));
				inputNum.clear();
				Utils.sleep(1000);
				inputNum.sendKeys(productNum);
				Utils.sleep(1000);
				logger.debug("--->选择商品数量完成");
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		// 加购物车
		logger.debug("--->开始加购物车");
		try{
			TimeUnit.SECONDS.sleep(3);
			WebElement addCard =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderSelectProduct.getAddCartCode())));
			addCard.click();
			TimeUnit.SECONDS.sleep(2);
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		logger.debug("--->确认是否加购物车成功");
		try{
			WebElement continueShop = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderSelectProduct.getAddedCode())));
			continueShop.click();
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->跳转到购物");
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderSelectProduct.getCartLoadedCode())));
		} catch (Exception e) {
			logger.error("--->加购物车出现异常",e);
			//return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		//验证数量
		try {
			if(!StringUtil.isBlank(autoOrderSelectProduct.getNumedCode())){
				WebElement numText = driver.findElement(By.cssSelector(autoOrderSelectProduct.getNumedCode()));
				logger.error("--->数量为:"+numText.getAttribute("value"));
				if(!numText.getAttribute("value").equals(productNum)){
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择商品数量碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		
			
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	};
	
	/**
	 * 
	 * @param param
	 * @param address
	 * @return
	 */
	public AutoBuyStatus pay(List<RobotOrderDetail> details,OrderAccount account,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount, AutoOrderLogin autoOrderLogin,AutoOrderPay autoOrderPay){
		
		if(StringUtil.isBlank(details.get(0).getTotalPay())){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		String cardNo = account.getCardNo();
		logger.error("cardNo = "+cardNo);
		
		try {
			doScreenShot(details.get(0).getOrderNo());
		} catch (Exception e) {
		}
		//优惠码
	
		WebDriverWait wait = new WebDriverWait(driver, 30);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderPay.getCartLoadedCode())));
			logger.debug("--->购物车页面加载完成");
		} catch (Exception e) {
			logger.error("--->加购物车出现异常",e);
			//return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector(autoOrderPay.getSubmitCode())));
			WebElement goPay = driver.findElement((By.cssSelector(autoOrderPay.getSubmitCode())));
			driver.executeScript("var tar=arguments[0];tar.click();", goPay);
			Utils.sleep(3000);
		} catch (Exception e) {
			logger.debug("--->加载结账出现异常");
			String size = String.valueOf(details.size());
			try {
				List<WebElement> goodsInCart =  driver.findElements(By.cssSelector(".operation-delete"));
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				logger.debug("--->size有 [" + size + "]件商品");
				if(!size.equals(String.valueOf(goodsInCart.size()))){
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			} catch (Exception e1) {
				logger.debug("--->购物车验证数量出错",e1);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
			try {
				WebElement goPay = driver.findElement(By.cssSelector(autoOrderPay.getSubmitCode()));
				goPay.click();
			} catch (Exception e2) {
				logger.debug("--->点击结算出现异常");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			
		}
//		try{
//			WebElement checkout = driver.findElement(By.cssSelector(autoOrderPay.getSubmitCode()));
//			checkout.click();
//			logger.debug("--->点击结算");
//			Utils.sleep(2000);
//		}catch(Exception e){
//			logger.debug("--->点击结算出现异常");
//			return AutoBuyStatus.AUTO_PAY_FAIL;
//		}
		logger.debug("--->等待输入地址页面加载");
		
		//返利链接需要重新登录一次
		boolean isUseFanli = true;
		try{
			loginUser(account.getPayAccount(), account.getLoginPwd(), autoOrderLogin);
		}catch(Exception e){
			isUseFanli = false;
			logger.debug("--->重新登录异常",e);
		}
		logger.debug("--->等待支付页面加载");
		try {
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addressSection")));
			logger.debug("--->支付页面加载完成");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		String usedEmail = account.getPayAccount();
		String name = userTradeAddress.getName();
		String address = userTradeAddress.getAddress();
		String zipcode = userTradeAddress.getZip();
		String mobile = userTradeAddress.getMobile();
		logger.debug("--->删除收货地址");
		try {
			List<WebElement> deleteList = driver.findElements(By.cssSelector("#addressList .address-item"));
			logger.debug("--->收货地址有"+deleteList.size());
			while (true) {
				int size = deleteList.size();
				if(deleteList!=null && size>0){
					deleteList.get(0).click();
					WebElement ww = driver.findElement(By.xpath("//a[@class='operation-type' and contains(text(),'删除')]"));
					driver.executeScript("var tar=arguments[0];tar.click();", ww);
					Utils.sleep(500);
					driver.findElement(By.id("easyDialogYesBtn")).click();
					Utils.sleep(500);
					if(size>1){
						deleteList = driver.findElements(By.cssSelector("#addressList .address-item"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
		} catch (Exception e2) {
			logger.debug("--->删除收货地址出错",e2);
		}
		logger.debug("--->选择收货地址");
		
		
		// 选收货地址
		try {
			WebElement addAddr = null;
			try {
				addAddr = driver.findElement(By.cssSelector("a[sel-id='settle-link-new-address']"));
			} catch (Exception e) {
				
			}
			
			try {
				addAddr.click();
			} catch (Exception e) {
			}
			
			TimeUnit.SECONDS.sleep(2);
			
			WebElement firstname = driver.findElement(By.id("firstname"));
			logger.debug("--->输入收货人姓名"+name);
			Utils.sleep(1500);
			firstname.sendKeys(name);
			
			logger.debug("--->选择收货地址");
			WebElement countrySelect = driver.findElement(By.id("country"));
			Select select = new Select(countrySelect);
			List<WebElement> countrys = select.getOptions();
			if (countrys != null && countrys.size() > 1) {
				for(WebElement country : countrys){
					String countryVal =  country.getAttribute("value").trim();
					if(countryVal.equals("中国大陆")){
						select.selectByVisibleText(countryVal);
						break;
					}
				}
			}
			TimeUnit.SECONDS.sleep(2);
			
			//省份
			String stateStr = userTradeAddress.getState().trim();
			if("广西壮族自治区".equals(stateStr)){
				stateStr = "广西";
			}else if("西藏自治区".equals(stateStr)){
				stateStr = "西藏";
			}else if("宁夏回族自治区".equals(stateStr)){
				stateStr = "宁夏";
			}else if("新疆维吾尔自治区".equals(stateStr)){
				stateStr = "新疆";
			}else if("内蒙古自治区".equals(stateStr)){
				stateStr = "内蒙古";
			}
			WebElement state = null;
			try {
				state = driver.findElement(By.id("region_id"));
			} catch (Exception e) {
				state = driver.findElement(By.id("region"));
			}
				
			Select selectState = new Select(state);
			selectState.selectByVisibleText(stateStr);
			logger.debug("--->输入省");
			Utils.sleep(2000);
			
			//市
			WebElement city = driver.findElement(By.xpath("//select[@id='city']"));	
			Select selectCity = new Select(city);
			String cityStr = userTradeAddress.getCity().trim();
			try {
				if("襄阳市".equals(cityStr)){
					cityStr = "襄樊市";
				} else if("上海市".equals(stateStr)){
					cityStr = "上海市";
				} else if("北京市".equals(stateStr)){
					cityStr = "北京市";
				} else if("重庆市".equals(stateStr)){
					cityStr = "重庆市";
				} else if("天津市".equals(stateStr)){
					cityStr = "天津市";
				} else if("大理市".equals(cityStr)){
					cityStr = "大理州";
				} else if("陵水黎族自治县".equals(cityStr)){
					cityStr = "陵水县";
				}
				selectCity.selectByVisibleText(cityStr);
			} catch (Exception e) {
				try {
					if(!cityStr.endsWith("市")){
						cityStr = cityStr + "市";
					}
					selectCity.selectByVisibleText(cityStr);
				} catch (Exception e2) {
					logger.debug("--->输入市出错", e2);
				}
			}
			logger.debug("--->输入市");
			Utils.sleep(2000);
			
			//区
			String districtStr = userTradeAddress.getDistrict().trim();
			WebElement district = null;
			try {
				district = driver.findElement(By.xpath("//select[@id='s_county']"));	
			} catch (Exception e) {
				district = driver.findElement(By.id("county"));	
			}
			
			
			Select selectdistrict = new Select(district);
			try{
				selectdistrict.selectByVisibleText(districtStr);
			}catch(Exception e){
				if(districtStr.endsWith("区")){//区改市
					districtStr = districtStr.subSequence(0, districtStr.length()-1)+"市";
				} else if(districtStr.equals("经济开发区")){
					districtStr = "经济技术开发区";
				}
				try{
					selectdistrict.selectByVisibleText(districtStr);
				}catch(Exception ee){
					selectdistrict.selectByIndex(1);
				}
			}
			logger.debug("--->输入区");
			Utils.sleep(2000);
			WebElement street = null;
			try {
				street = driver.findElement(By.id("street_1"));
			} catch (Exception e) {
				street = driver.findElement(By.cssSelector(".input-textarea"));
			}
			
			street.clear();
			Utils.sleep(1000);
			logger.debug("--->输入街道地址");
			Utils.sleep(1500);
			street.sendKeys(userTradeAddress.getDistrict()+address);
			
			WebElement postcode = driver.findElement(By.id("postcode"));
			postcode.clear();
			Utils.sleep(1000);
			logger.debug("--->输入邮编");
			Utils.sleep(1500);
			postcode.sendKeys(zipcode);
			
			WebElement telephone = driver.findElement(By.id("telephone"));
			telephone.clear();
			Utils.sleep(1000);
			logger.debug("--->输入电话");
			Utils.sleep(1500);
			telephone.sendKeys(mobile);
			
			WebElement email = driver.findElement(By.id("email"));
			email.clear();
			Utils.sleep(1000);
			logger.debug("--->输入常用邮箱");
			Utils.sleep(1500);
			email.sendKeys(usedEmail);
			
			Utils.sleep(1500);
			WebElement saveAddrBtn = null;
			try {
				saveAddrBtn = driver.findElement(By.id("AjaxSaveAddress"));
			} catch (Exception e) {
				saveAddrBtn = driver.findElement(By.cssSelector(".btn-save"));
			}
			saveAddrBtn.click();
			Utils.sleep(3000);
			logger.debug("--->点击保存地址");
			
		} catch (Exception e) {
			logger.debug("--->选择地址出现异常 = ",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		//输入身份证号码
		boolean isSuccess = false;
		WebDriverWait wait0 = new WebDriverWait(driver, 8);
		for(int i=0;i<3;i++){
			try{
				WebElement input = null;
				try {
					input = driver.findElement(By.xpath("//input[@id='receiver-id']"));
				} catch (Exception e2) {
					input = driver.findElement(By.id("identityNumber"));
				}
				
				input.clear();
				Utils.sleep(1500);
				input.sendKeys(userTradeAddress.getIdCard());
				Utils.sleep(2000);
				try {
					driver.findElement(By.xpath("//span[@id='idSubBtn']")).click();
				} catch (Exception e) {
					driver.findElement(By.cssSelector(".btn-identity")).click();
				}
				
				Utils.sleep(1000);
				try {
					wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='pass-validate']")));
				} catch (Exception e) {
					wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("identityResult")));
				}
				
				isSuccess = true;
				break;
			}catch(Exception e){
				logger.debug("--->身份证检验出错",e);
			}
		}
		if(!isSuccess){
			logger.debug("--->身份证校验出错");
			return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_FAIL;
		}
		
		//选中支付宝
		try{
			WebElement alipayPayment = driver.findElement(By.cssSelector(autoOrderPay.getAlipayCode()));
			driver.executeScript("var tar=arguments[0];tar.click();", alipayPayment);
		}catch(Exception e){
			logger.debug("--->选中支付宝出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//结账
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(details.get(0).getPromotionCodeList());
		if (promotionList != null && promotionList.size() > 0) {
			try {
				WebElement promotions = driver.findElement(By.cssSelector(".derate-handler"));
				promotions.click();
				TimeUnit.SECONDS.sleep(2);
				WebElement codepro = driver.findElement(By.cssSelector(".derate-label-code"));
				codepro.click();
				TimeUnit.SECONDS.sleep(2);
				for (String code : promotionList) {
					logger.debug("couponCode："+code);
					WebElement element = driver.findElement(By.id("derateInputCode"));
					element.clear();
					element.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
					
					WebElement use = driver.findElement(By.id("derateUse"));
					use.click();
					TimeUnit.SECONDS.sleep(2);
					
					try {
						WebElement tip = driver.findElement(By.id("derateInputTips"));
						if(tip.getText().contains("错误")){
							logger.debug("优惠码无效："+code);
							statusMap.put(code, 0);
						}
					} catch (Exception e) {
						try {
							//driver.findElement(By.xpath("//div[@class='coupon-done']"));
							logger.debug("优惠码有效："+code);
							isEffective = true;
							statusMap.put(code, 10);
						} catch (Exception e2) {
							logger.debug("异常："+e);
						}
					}
				}
				System.out.println(statusMap.toString());
				if("1".equals(details.get(0).getIsStockpile()) && !isEffective){
					logger.debug("--->优惠码失效,中断采购");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			} catch (Exception e) {
			}
		}
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.cssSelector(autoOrderPay.getTotalPriceCode()));
			String priceStr = totalPriceElement.getText();
			for(RobotOrderDetail detail:details){
				detail.setTotalPrice(priceStr);
			}
			logger.debug("--->找到商品结算总价 = "+priceStr);
			if(!StringUtil.isBlank(details.get(0).getTotalPay())){
				AutoBuyStatus priceStatus = comparePrice(priceStr, details.get(0).getTotalPay());
				if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		//提交订单
		logger.debug("--->开始点击提交订单 orderPayAccount.getPayPassword() = "+orderPayAccount.getPayPassword());
		try{
			WebElement placeOrder = driver.findElement(By.cssSelector(autoOrderPay.getOrderPlaceCode()));
			placeOrder.click();;
			WebElement gotologin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#J_tip_qr a.switch-tip-btn")));
			gotologin.click();
			logger.error("支付宝登陆按钮点击");
			//支付宝账号
			WebElement name1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='J_tLoginId']")));
			name1.sendKeys(orderPayAccount.getAccount());
			Utils.sleep(1500);
			//密码
			driver.findElement(By.xpath("//input[@id='payPasswd_rsainput']")).sendKeys(orderPayAccount.getPayPassword());
			Utils.sleep(1500);
			//下一步
			driver.findElement(By.xpath("//a[@id='J_newBtn']")).click();
			Utils.sleep(1500);
			
			//输入支付密码
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='sixDigitPassword']")));
			String payPwd = orderPayAccount.getPayPassword();
			String str = "(function(){var els = document.getElementById('payPassword_rsainput');if(els){els.value='%s';}})();";
			String ss = String.format(str, payPwd);
			logger.debug("--->ss = "+ss);
			driver.executeScript(ss);
			Utils.sleep(3000);
			
			//输入银行卡号
			try{
				WebElement bank = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='bankCardNo']")));
				bank.sendKeys(cardNo);
			}catch(Exception e){
				logger.debug("--->没找到输入银行卡的输入框",e);
			}
			
			Utils.sleep(1000);
			driver.findElement(By.xpath("//input[@id='J_authSubmit']")).click();
			Utils.sleep(1000);
			
		}catch(Exception e){
			logger.debug("--->点击付款出现异常",e);
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		
		//查询商城订单号
		try{
			logger.debug("--->开始查找商品订单号");
			wait = new WebDriverWait(driver, 2*WAIT_TIME);
			By byby = By.cssSelector(autoOrderPay.getMallOrderNoCode());
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			String mallOrderNo = ExpressUtils.regularExperssNo(orderElement.getText());
			logger.debug("--->找到商品订单号1 = "+mallOrderNo);
			for(RobotOrderDetail detail:details){
				detail.setMallOrderNo(mallOrderNo);
			}
			savePng(details.get(0).getOrderNo());
			return AutoBuyStatus.AUTO_PAY_SUCCESS;
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
	}

	/**
	 * 爬取物流公司和单号
	 * @param mallOrderNo
	 * @return
	 */
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail,AutoOrderScribeExpress autoOrderScribeExpress){
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			logger.debug(autoOrderScribeExpress.getSiteName()+"--->mallOrderNo没有");
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		try{
			logger.debug(autoOrderScribeExpress.getSiteName()+"--->开始跳转到订单页面");
			driver.navigate().to(autoOrderScribeExpress.getOrderListUrl());
		}
		catch (Exception e){
			logger.error(autoOrderScribeExpress.getSiteName()+"--->跳转到订单页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug(autoOrderScribeExpress.getSiteName()+"--->开始等待order页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderScribeExpress.getOrderListLoadedCode())));
			Utils.sleep(1500);
			logger.debug(autoOrderScribeExpress.getSiteName()+"--->order页面加载完成");
		}
		catch (Exception e){
			logger.error(autoOrderScribeExpress.getSiteName()+"--->加载order页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询所有可见的订单
		boolean isFind = false; 
		for(int i = 0;i<7;i++){
			List<WebElement> list = driver.findElements(By.cssSelector(autoOrderScribeExpress.getOrderListCode()));
			for(WebElement panel : list){
				WebElement w = panel.findElement(By.cssSelector(autoOrderScribeExpress.getOrderNumberCode()));
				if(w.getText().contains(mallOrderNo.trim())){
					logger.error(autoOrderScribeExpress.getSiteName()+"--->找到商城单号"+mallOrderNo);
					isFind = true;
					//判断订单是否取消
					String s = panel.findElement(By.cssSelector(autoOrderScribeExpress.getOrderStatusCode())).getText();
					logger.error(autoOrderScribeExpress.getSiteName()+"--->OrderStatusCode="+s);
					if(!StringUtil.isBlank(s) && s.contains(autoOrderScribeExpress.getCancelledStr())){
						logger.error(autoOrderScribeExpress.getSiteName()+"--->商城订单:"+mallOrderNo+"已取消");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(!StringUtil.isBlank(s) && s.contains(autoOrderScribeExpress.getNotShipingStr())){
						logger.error(autoOrderScribeExpress.getSiteName()+"--->商城订单:"+mallOrderNo+"还没有发货");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
					}else if(!StringUtil.isBlank(s) && s.contains(autoOrderScribeExpress.getShipingStr())){
						WebElement orderDetailJump = panel.findElement(By.cssSelector(autoOrderScribeExpress.getOrderDetailJump()));
						orderDetailJump.click();
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}else{
						logger.error(autoOrderScribeExpress.getSiteName()+"--->商城订单:"+mallOrderNo+"未知状态");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
					}
				}
			}
			if(!isFind){
				if(!StringUtil.isBlank(autoOrderScribeExpress.getNextPageUrl())){
					try{
						int page = i+2;
						String url= String.format(autoOrderScribeExpress.getNextPageUrl()+"%d", page);
						driver.navigate().to(url);
						Utils.sleep(5500);
					}catch(Exception e){
						logger.error(autoOrderScribeExpress.getSiteName()+"--->跳转page出错:",e);
					}
				}else{
					break;
				}
			}
		}
		if(!isFind){
			logger.error(autoOrderScribeExpress.getSiteName()+"--->商城订单:"+mallOrderNo+"找不到");
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	};
	
	public AutoBuyStatus expressDetail(RobotOrderDetail detail,AutoOrderExpressDetail autoOrderExpressDetail){
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(autoOrderExpressDetail.getOrderDetailLoadedCode())));
		WebElement shipment = driver.findElement(By.cssSelector(autoOrderExpressDetail.getExpressNoCode()));
		logger.error(autoOrderExpressDetail.getSiteName()+"--->找到物流单号 = "+shipment.getText());
		String expressNo = ExpressUtils.regularExperssNo(shipment.getText());
		logger.error(autoOrderExpressDetail.getSiteName()+"--->找到匹配的物流单号 = "+expressNo);
		String company = "";
		try {
			if(!StringUtil.isBlank(autoOrderExpressDetail.getExpressCompanyCode())){
				WebElement shipmentCompany = driver.findElement(By.cssSelector(autoOrderExpressDetail.getExpressCompanyCode()));
				logger.error(autoOrderExpressDetail.getSiteName()+"--->找到物流公司 = "+shipmentCompany.getText());
				company = shipmentCompany.getText();
			}
		} catch (Exception e) {
			logger.error(autoOrderExpressDetail.getSiteName()+"--->找到物流公司异常");
		}
		
		//trakpak 处理
		if(autoOrderExpressDetail.getOrderType()==0){
			driver.navigate().to("http://www.trackmytrakpak.com/?MyTrakPakNumber="+expressNo);
			List<WebElement> tempList = driver.findElements(By.xpath("//table/tbody/tr"));
			if(tempList != null && tempList.size() > 0){
				for(WebElement ww : tempList){
					String texts = ww.getText();
					if(!StringUtil.isBlank(texts) && texts.contains("Tracking Number:")){
						WebElement ee = ww.findElement(By.xpath(".//td"));
						if(ee != null && !StringUtil.isBlank(ee.getText())){
							logger.error(autoOrderExpressDetail.getSiteName()+"--->找到单号 = "+ee.getText());
							expressNo = ee.getText().replaceAll("[^a-z^A-Z^0-9]", "");
							if(!StringUtil.isBlank(texts) && texts.contains("Local Tracking Number")){
								company="EMS";
								logger.error(autoOrderExpressDetail.getSiteName()+"--->company = "+company);
							}
						}else{
							logger.error(autoOrderExpressDetail.getSiteName()+"--->找到Tracking Number但是还没发货");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}
					}
				}
			}
		}
		detail.setExpressNo(expressNo);
		detail.setExpressCompany(company);
		return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
	}


	public boolean productOrderCheck(TaskResult taskResult){
		return false;
	}
	
	public void killFirefox(){
		
		//浏览器退出
		quitDriver(2500);
		
		//杀掉浏览器进程
		try {
			logger.debug("--->开始结束firefox进程");
			Runtime.getRuntime().exec("taskkill -f -im firefox.exe");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("结束firefox进程异常",e);
		}
		
		Utils.sleep(200);
		logger.info("--->浏览器退出!");
	}
	
	public void quitDriver(long joinTime){
		Runnable quitRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {  
			        logger.debug("--->完成本次浏览器操作");
					logger.debug("===================");
					if (driver != null){
						driver.quit();
			    	}
			    } catch (Throwable e) {  
			    	logger.error(e);
			    } 
			}
		};
		Thread t = new Thread(quitRunnable);
		t.start();
		try{
			t.join(joinTime);
		}catch(Exception e){
			logger.error("join exception = ",e);
		}
		try{
			t.interrupt();
		}catch(Exception e){
			logger.error("interrupt exception = ",e);
		}
		Utils.sleep(2500);
	}
	
	public static void main(String[] args){
		ManualBuy auto = new ManualBuy(false);
		AutoOrderLogin autoOrderLogin = new AutoOrderLogin();
		autoOrderLogin.setLoginUrl("https://www.bonpont.com/customer/account/login");
		autoOrderLogin.setConfirmCode(".top-login");
		autoOrderLogin.setMallName("bonpont");
		autoOrderLogin.setPasswordCode("#password");
		autoOrderLogin.setUsernameCode("#username");
		autoOrderLogin.setSubmitCode("#loginBtn");
		AutoBuyStatus status = auto.login("yunzh17@163.com", "tfb001001",autoOrderLogin);
//		AutoOrderCleanCart autoOrderCleanCart = new AutoOrderCleanCart();
//		autoOrderCleanCart.setCartLoadedCode(".cart-wrap");
//		autoOrderCleanCart.setCartUrl("https://www.bonpont.com/cart/index/index");
//		autoOrderCleanCart.setCleanEndCode("#emptyHandler");
//		autoOrderCleanCart.setCleanType(0);
//		autoOrderCleanCart.setConfirmCode("#easyDialogYesBtn");
//		autoOrderCleanCart.setRemoveCode(".delete");
//		autoOrderCleanCart.setSiteName("bonpont");
//		status = auto.cleanCart(autoOrderCleanCart);
		System.out.println(status);
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			RobotOrderDetail detail = new RobotOrderDetail();
			detail.setProductRebateUrl("https://p.gouwuke.com/c?w=858413&c=19247&i=43784&pf=y&e=&t=https://www.bonpont.com/1003539.html");
			detail.setUnits("CNY");
			detail.setNum(2);
			//detail.setPromotionCodeList("231231");
			status = auto.selectProduct(detail, null);
			RobotOrderDetail detail1 = new RobotOrderDetail();
			detail1.setProductRebateUrl("https://p.gouwuke.com/c?w=858413&c=19247&i=43784&pf=y&e=&t=https://www.bonpont.com/1003346.html");
			detail1.setUnits("CNY");
			detail1.setNum(3);
			//detail.setPromotionCodeList("231231");
			status = auto.selectProduct(detail1, null);
//			System.out.println(status);
//			Map<String, String> param0 = new HashMap<String, String>();
//			param0.put("my_price", "319.99");
//			param0.put("count", "1");
////			param0.put("isPay", String.valueOf(true));
//			param0.put("cardNo", "4662 4833 6029 1396");
//			UserTradeAddress userTradeAddress = new UserTradeAddress();
//			userTradeAddress.setName("刘波");
//			userTradeAddress.setAddress("西斗门路9号");
//			userTradeAddress.setState("浙江省");
//			userTradeAddress.setCity("杭州市");
//			userTradeAddress.setDistrict("西湖区");
//			userTradeAddress.setZip("310000");
//			userTradeAddress.setIdCard("330881198506111918");
//			userTradeAddress.setMobile("18668084980");
//			OrderPayAccount orderPayAccount = new OrderPayAccount();
//			orderPayAccount.setAccount("15268125960");
//			orderPayAccount.setPayPassword("199027");
//			status = auto.pay(detail,param0, userTradeAddress, orderPayAccount,"yunzh17@163.com", "tfb001001",autoOrderLogin);
//			System.out.println(status);
//			AutoOrderCleanCart autoOrderCleanCart = new AutoOrderCleanCart();
//			autoOrderCleanCart.setCartLoadedCode(".delete-checked");
//			autoOrderCleanCart.setCartUrl("http://cn.getthelabel.com/checkout/cart/");
//			autoOrderCleanCart.setCleanEndCode(".cart-empty");
//			autoOrderCleanCart.setCleanType(1);
//			autoOrderCleanCart.setConfirmCode("#easyDialogYesBtn");
//			autoOrderCleanCart.setRemoveCode(".delete-checked");
//			autoOrderCleanCart.setSiteName("getthelabel");
//			status = auto.cleanCart(autoOrderCleanCart);
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
	
	protected static Set<String> getPromotionList(String promotionStr){
		if(!StringUtil.isBlank(promotionStr)){
			Set<String> promotionList = new HashSet<String>();
			String[] as = promotionStr.split(";");
			if(as != null && as.length > 0){
				for(String a : as){
					if(!StringUtil.isBlank(a)){
						String[] aas = a.split(",");
						if(aas != null && aas.length > 0){
							for(String aa : aas){
								if(!StringUtil.isBlank(aa)){
									promotionList.add(aa.trim());
								}
							}
						}
					}
				}
			}
			return promotionList;
		}
		return null;
	}
	public AutoBuyStatus comparePrice(String mallPrice,String totalPay){
		BigDecimal x = new BigDecimal(totalPay);
		BigDecimal y = new BigDecimal(mallPrice);
		BigDecimal v = y.subtract(x);
		if (v.doubleValue() > 0.00D){
			logger.error("--->总价差距超过约定,不能下单"+totalPay);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}else{
			return AutoBuyStatus.AUTO_PAY_PARPARE;
		}
	}
	
	public void savePng(String orderNo){
		try {
			byte[] b = doScreenShot(orderNo);
			if(b!=null){
				getTask().addParam("screentShot", b);
			}else{
				logger.error("--->screentShot为空");
			}
		} catch (Exception e) {
			logger.error("--->截图失败");
		}
	}
	
	public byte[] doScreenShot(String orderNo){
		byte[] screenshot = null;
		if (driver != null){
			FileOutputStream fos = null;
			try{
				File file = new File("screenshot");
				if (!file.exists()){
					file.mkdir();
				}
				screenshot = driver.getScreenshotAs(OutputType.BYTES);
				String filePathName = "";
				if(!StringUtil.isBlank(orderNo)){
					filePathName = "screenshot/" + orderNo+"-"+System.currentTimeMillis()/1000 + ".png";
				}else{
					filePathName = "screenshot/" + System.currentTimeMillis()/1000 + ".png";
				}
				logger.debug("--->开始截图,路径:"+filePathName);
				fos = new FileOutputStream(filePathName);
				fos.write(screenshot);
			}
			catch (Throwable e){
				logger.debug("--->截图出现异常",e);
			}finally {  
			    try {  
			        if (fos != null) {  
			        	fos.close();  
			        } 
			    } catch (Exception e) {  
			        e.printStackTrace();  
			    }  
			}
		}
		Utils.sleep(200);
		return screenshot;
	}
	
}