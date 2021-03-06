package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.ExpressNode;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.DateUtils;
import com.oversea.task.utils.ExpressUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年9月28日 上午10:45:35 
*/
public class BodyguardapothekeAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	private Timer timer;
	public BodyguardapothekeAutoBuy() {
		super(false);
		
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				logger.debug("15分钟start");
				if (driver != null){
					logger.debug("15分钟run");
					driver.quit();
				}
				logger.debug("15分钟end");
			}
		}, 1000*60*15);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://www.ba.de/");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login")));
			logger.debug("--->跳转到登录页面");
			signIn.click();
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try {
			// 输入账号
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[sel-id='login-text-username']")));
			WebElement username = driver.findElement(By.cssSelector("input[sel-id='login-text-username']"));
			logger.debug("--->输入账号");
			Utils.sleep(1500);
			username.sendKeys(userName);

			// 输入密码
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[sel-id='login-text-password']")));
			WebElement passward = driver.findElement(By.cssSelector("input[sel-id='login-text-password']"));
			logger.debug("--->输入密码");

			Utils.sleep(1500);
			passward.sendKeys(passWord);

			// 提交
			Utils.sleep(1500);
			WebElement submitBtn = driver.findElement(By.id("accout-login"));
			logger.debug("--->开始提交");
			submitBtn.click();

		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".welcome")));
			logger.debug("--->登录完成");
		}
		catch (Exception e)
		{
			try
			{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".header")));
				logger.debug("--->登录完成1");
			}
			catch (Exception e1)
			{
				logger.error("--->登录碰到异常1", e1);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		// 跳转到购物车
		try {
			driver.findElement(By.cssSelector(".cart-index a")).click();
			logger.error("--->开始跳转到购物车");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		logger.debug("--->清空购物车");
		WebDriverWait wait = new WebDriverWait(driver, 20);
		logger.debug("--->清空购物车");
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".operation-delete")));
			logger.error("--->开始清理购物车");
			WebElement deleteall = driver.findElement(By.cssSelector(".operation-delete"));
			deleteall.click();
			Utils.sleep(1000);
			driver.findElement(By.id("easyDialogYesBtn")).click();
			logger.error("--->购物车页面清理完成");
		} catch (Exception e) {
			logger.error("--->购物车页面清理完成");
		}
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".settlement-operation")));
			logger.error("--->开始清理购物车");
			WebElement deleteall = driver.findElement(By.cssSelector(".settlement-operation"));
			deleteall.click();
			Utils.sleep(1000);
			driver.findElement(By.id("easyDialogYesBtn")).click();
			logger.error("--->购物车页面清理完成");
		} catch (Exception e2) {
			logger.debug("--->购物车页面清理完成！");
		}
//		try {
//			logger.error("--->确认购物车是否清空");
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".car-not-items")));
//		} catch (Exception e) {
//			logger.debug("--->购物车不为空！");
//			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
//		}
		try {
			driver.navigate().to("https://www.ba.de/customer/address/");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#addressList a.ctrl-delete")));
			List<WebElement> deleteList = driver.findElements(By.cssSelector("#addressList a.ctrl-delete"));
			while (true) {
				int size = deleteList.size();
				if(deleteList!=null && size>0){
					deleteList.get(0).click();
					Utils.sleep(500);
					driver.findElement(By.id("easyDialogYesBtn")).click();
					Utils.sleep(500);
					if(size>1){
						deleteList = driver.findElements(By.cssSelector("#addressList a.ctrl-delete"));
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
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);

		try {
			driver.navigate().to(productUrl);
		} catch (Exception e) {
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pds")));
			logger.debug("--->商品页面加载完成");
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElment = driver.findElement(By.cssSelector("#end-price"));
			String priceStr = priceElment.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && priceStr.startsWith("€") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->[1]找到商品单价 = " + priceStr.replace("€", ""));
				priceMap.put(productEntityId, priceStr.replace("€", ""));
			}
		} catch (Exception e) {
			logger.debug("--->开始寻找商品单价1");
			WebElement priceElment = driver.findElement(By.cssSelector(".end-price"));
			String priceStr = priceElment.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && priceStr.startsWith("€") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->[2]找到商品单价 = " + priceStr.replace("€", ""));
				priceMap.put(productEntityId, priceStr.replace("€", ""));
			}
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (StringUtil.isNotEmpty(productNum) && !productNum.equals("1"))
		{
			try
			{
				logger.debug("--->选择数量:" + productNum);
				WebElement numInput = driver.findElement(By.cssSelector(".js-qty"));
				numInput.clear();
				TimeUnit.SECONDS.sleep(1);
				numInput.sendKeys(productNum);
				TimeUnit.SECONDS.sleep(1);
			}
			catch (Exception e)
			{
				logger.debug("--->选择数量出错");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			driver.executeScript("(function(){window.scrollBy(0,200);})();");
			TimeUnit.SECONDS.sleep(2);
			WebElement cart = driver.findElement(By.cssSelector(".btn-buy"));
			if("已售罄".equals(cart.getText())){
				logger.debug("--->加购物车按钮找不到"+cart.getText());
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
			driver.executeScript("var tar=arguments[0];tar.click();", cart);
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		logger.debug("--->等待购物车页面加载");
		//Utils.sleep(1500);
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("easyDialogWrapper")));
			//WebElement easyBtn = driver.findElement(By.id("easyDialogNoBtn"));
			//easyBtn.click();
			logger.debug("--->添加到购物车成功");
		} catch (Exception e) {
			logger.error("--->添加到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param, UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		// 设置价格
		logger.error("--->myPrice = " + myPrice);
		// 等待购物车页面加载完成
		try {
			driver.get("https://www.ba.de/cart/index");
			logger.error("--->开始跳转到购物车");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("a[sel-id='cart-btn-settle']")));
			WebElement goPay = driver.findElement(By.cssSelector("a[sel-id='cart-btn-settle']"));
			driver.executeScript("var tar=arguments[0];tar.click();", goPay);
			Utils.sleep(3000);
		} catch (Exception e) {
			logger.debug("--->加载Pharmacyonline结账出现异常");
			String size = param.get("size");
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
			WebElement goPay = driver.findElement(By.cssSelector("a[sel-id='cart-btn-settle']"));
			goPay.click();;
		}
		
		String userName = param.get("userName");
		String passWord = param.get("password");
		try {
			// 因为返利地址的原因需要重新登录
			TimeUnit.SECONDS.sleep(5);
			// 输入账号
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[sel-id='login-text-username']")));
			WebElement username = driver.findElement(By.cssSelector("input[sel-id='login-text-username']"));
			logger.debug("--->输入账号");
			Utils.sleep(1500);
			username.sendKeys(userName);

			// 输入密码
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[sel-id='login-text-password']")));
			WebElement passward = driver.findElement(By.cssSelector("input[sel-id='login-text-password']"));
			logger.debug("--->输入密码");

			Utils.sleep(1500);
			passward.sendKeys(passWord);

			// 提交
			Utils.sleep(1500);
			WebElement submitBtn = driver.findElement(By.id("accout-login"));
			logger.debug("--->开始提交");
			submitBtn.click();
			Utils.sleep(1500);
			
			// 等待购物车页面加载完成
			logger.debug("--->等待购物车页面加载");
			try {
				//driver.executeScript("(function(){window.scrollBy(300,500);})();");
				TimeUnit.SECONDS.sleep(5);
				WebElement goPay = null;
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(
							By.id("AccountButton")));
					goPay = driver.findElement(By.id("AccountButton"));
				} catch (Exception e) {
					goPay = driver.findElement(By.cssSelector(".btn-checkout"));
				}
				
				//结账
				HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
				boolean isEffective = false;
				Set<String> promotionList = getPromotionList(param.get("promotion"));
				if (promotionList != null && promotionList.size() > 0) {
					for (String code : promotionList) {
						logger.debug("couponCode："+code);
						WebElement element = driver.findElement(By.xpath("//input[@id='coupon_code']"));
						element.clear();
						element.sendKeys(code);
						TimeUnit.SECONDS.sleep(2);
						
						WebElement use = driver.findElement(By.xpath("//input[@class='OrangeButton use-code-btn']"));
						use.click();
						TimeUnit.SECONDS.sleep(2);
						
						try {
							driver.findElement(By.xpath("//div[@class='easyDialog_text']"));
							logger.debug("优惠码无效："+code);
							driver.findElement(By.xpath("//button[@id='easyDialogYesBtn']")).click();;
							statusMap.put(code, 0);
						} catch (Exception e) {
							try {
								driver.findElement(By.xpath("//div[@class='coupon-done']"));
								logger.debug("优惠码有效："+code);
								isEffective = true;
								statusMap.put(code, 10);
							} catch (Exception e2) {
								logger.debug("异常："+e);
							}
						}
					}
					setPromotionCodelistStatus(statusMap);
					System.out.println(statusMap.toString());
					if("true".equals(param.get("isStock")) && !isEffective){
						logger.debug("--->优惠码失效,中断采购");
						return AutoBuyStatus.AUTO_PAY_FAIL;
					}
				}
				
				Utils.sleep(1500);
				goPay.click();
				Utils.sleep(5000);
			} catch (Exception e) {
				logger.debug("--->加载Pharmacyonline结账出现异常");
				//return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->不需要重新登录");
		}
		
		logger.debug("--->等待支付页面加载");
		try {
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='col-main']")));
			logger.debug("--->支付页面加载完成");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		String usedEmail = (String) param.get("userName");
		String name = userTradeAddress.getName();
		String address = userTradeAddress.getAddress();
		String zipcode = userTradeAddress.getZip();
		String mobile = userTradeAddress.getMobile();
		
		
		//删除地址
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ctrl-delete")));
			List<WebElement> addresss = driver.findElements(By.cssSelector(".ctrl-delete"));
			for(WebElement w:addresss){
				if(w.isDisplayed()){
					w.click();
					TimeUnit.SECONDS.sleep(1);
					WebElement yesbtn = driver.findElement(By.id("easyDialogYesBtn"));
					yesbtn.click();
					TimeUnit.SECONDS.sleep(1);
					break;
				}
			}
		
		} catch (Exception e) {
			logger.debug("--->删除默认地址出错");
		}
		
		List<WebElement> deleteAddress = driver.findElements(By.cssSelector(".address-item"));
		for(WebElement wa:deleteAddress){
			if(wa.isDisplayed()){
				wa.click();
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ctrl-delete")));
					List<WebElement> addresss = driver.findElements(By.cssSelector(".ctrl-delete"));
					for(WebElement w:addresss){
						if(w.isDisplayed()){
							w.click();
							TimeUnit.SECONDS.sleep(1);
							WebElement yesbtn = driver.findElement(By.id("easyDialogYesBtn"));
							yesbtn.click();
							TimeUnit.SECONDS.sleep(1);
							break;
						}
					}
				
				} catch (Exception e) {
					logger.debug("--->删除默认地址出错");
				}
			}
			
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
		WebDriverWait wait0 = new WebDriverWait(driver, 8);
		//输入身份证号码
		
		//选择支付方式
		logger.debug("--->选择支付方式");
		try {
			Utils.sleep(1500);
			WebElement alipay = driver.findElement(By.id("payment-alipay_payment"));
			alipay.click();
			Utils.sleep(1500);
			logger.debug("--->选择支付宝支付");
		} catch (Exception e) {
			logger.debug("--->选择支付方式出现异常= ", e);
		}
		
		//结账
		//结账
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			try {
				try {
					wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".derate-label-code")));
					WebElement codepro = driver.findElement(By.cssSelector(".derate-label-code"));
					codepro.click();
				} catch (Exception e) {
					try {
						WebElement promotions = driver.findElement(By.cssSelector(".derate-handler"));
						promotions.click();
						TimeUnit.SECONDS.sleep(2);
						WebElement codepro = driver.findElement(By.cssSelector(".derate-label-code"));
						codepro.click();
					} catch (Exception e1) {
						logger.debug("--->优惠码异常1",e1);
					}
				}
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
						if(tip.getText().contains("错误") || tip.getText().contains("已过期")){
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
				setPromotionCodelistStatus(statusMap);
				System.out.println(statusMap.toString());
				if("true".equals(param.get("isStock")) && !isEffective){
					logger.debug("--->优惠码失效,中断采购");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			} catch (Exception e) {
			}
		}
		
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement = driver
					.findElement(By.id("div.should-pay p.fee strong"));
			String text = totalPriceElement.getText();
			if (!Utils.isEmpty(text) && text.indexOf("AU$") != -1) {
				String priceStr = text.replace("AU$", "");
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->[1]找到商品结算总价 = " + priceStr);
				if(!StringUtil.isBlank(getTotalPrice())){
					AutoBuyStatus priceStatus = comparePrice(priceStr, getTotalPrice());
					if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}else{
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(priceStr);
					BigDecimal v = y.subtract(x);
					if (v.doubleValue() > 20.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常=", e);
			WebElement totalPriceElement = driver
					.findElement(By.id("grandTotal"));
			String priceStr = totalPriceElement.getText();
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
			logger.debug("--->[1]找到商品结算总价 = " + priceStr);
			if(!StringUtil.isBlank(getTotalPrice())){
				AutoBuyStatus priceStatus = comparePrice(priceStr, getTotalPrice());
				if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}else{
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 20.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
		}
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		String cardNo = param.get("cardNo");
		logger.error("cardNo = "+cardNo);
		
		//提交订单
		//logger.debug("--->开始点击提交订单 orderPayAccount.getPayPassword() = "+orderPayAccount.getPayPassword());
		try{
			WebElement placeOrder = null;
			try {
				placeOrder = driver.findElement(By.id("onestepcheckout-place-order"));
			} catch (Exception e) {
				placeOrder = driver.findElement(By.id("priceConfirm"));
			}
			driver.executeScript("var tar=arguments[0];tar.click();", placeOrder);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#J_tip_qr a.switch-tip-btn")));
			WebElement gotologin = driver.findElement(By.cssSelector("div#J_tip_qr a.switch-tip-btn"));
			gotologin.click();
			logger.error("支付宝登陆按钮点击");
			//支付宝账号
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='J_tLoginId']")));
			} catch (Exception e) {
				gotologin = driver.findElement(By.cssSelector("div#J_tip_qr a.switch-tip-btn"));
				gotologin.click();
				logger.error("支付宝登陆按钮再次点击");
			}
			
			//支付宝账号
			WebElement alipayName = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='J_tLoginId']")));
			alipayName.sendKeys(orderPayAccount.getAccount());
			Utils.sleep(1500);
			//密码
			driver.findElement(By.xpath("//input[@id='payPasswd_rsainput']")).sendKeys(orderPayAccount.getPayPassword());
			Utils.sleep(1500);
			//下一步
			driver.findElement(By.xpath("//a[@id='J_newBtn']")).click();
			Utils.sleep(1500);
			
			//输入支付密码
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='sixDigitPassword']")));
			String payPwd = orderPayAccount.getPayPassword();
			String str = "(function(){var els = document.getElementById('payPassword_rsainput');if(els){els.value='%s';}})();";
			String ss = String.format(str, payPwd);
			logger.debug("--->ss = "+ss);
			driver.executeScript(ss);
			Utils.sleep(3000);
			
			//输入银行卡号
			try{
				WebElement bank = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='bankCardNo']")));
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
			wait = new WebDriverWait(driver, WAIT_TIME);
			By byby = By.cssSelector(".order-number");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			String mallOrderNo = ExpressUtils.regularExperssNo(orderElement.getText());
			logger.debug("--->找到商品订单号1 = "+mallOrderNo);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, mallOrderNo);
			savePng();
			return AutoBuyStatus.AUTO_PAY_SUCCESS;
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			try {
				WebElement balanceNotEnough = driver.findElement(By.id("errorTipsFlowName"));
				logger.debug("--->余额不足提示:"+balanceNotEnough.getText());
			} catch (Exception e2) {
			}
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		
		//寻找my account
		try{
			logger.debug("--->开始跳转到订单页面");
			driver.navigate().to("http://www.ba.de/sales/order/history/");
		}
		catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待order页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-account")));
			Utils.sleep(1500);
			logger.debug("--->order页面加载完成");
		}
		catch (Exception e){
			logger.error("--->加载order页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			logger.debug("--->找到对应的table");
			loop:
			for(int i = 0;i<6;i++){//最多翻6页
				List<WebElement> orderList = driver.findElements(By.cssSelector("table.order-table"));
				if(orderList != null && orderList.size() > 0){
					for (WebElement orders : orderList) {
						WebElement orderNo = null;
						boolean flag = false;
						try {
							orderNo = orders.findElement(By.cssSelector("td p.order-num"));
							flag = orderNo.getText().trim().contains(mallOrderNo.trim());
							logger.debug("--->orderNo text:" + orderNo.getText().trim() + "对比结果:" + flag);
						} catch (Exception e) {
						}
						if (flag) {
							// 订单编号状态
							WebElement status = orders.findElement(By.cssSelector("td .order-status"));
							
							String str = status.getText().trim();
							if(StringUtil.isNotEmpty(str) && str.contains("已取消")){
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							}
							if (str.contains("已发货")) {
								Utils.sleep(2500);
								logger.debug("--->查找物流单号");
								WebElement shipOrder = orders.findElement(By.cssSelector(".order-ctrl a"));
								String url = shipOrder.getAttribute("href");
								logger.debug("--->跳转url:"+url);
								driver.navigate().to(url);
								
								WebElement w = driver.findElement(By.cssSelector(".logistics-details"));
								String expressNo = ExpressUtils.regularExperssNo(w.getText());
								if(expressNo.length()>20){
									String[] expressGroup = expressNo.split(",");
									for(String s:expressGroup){
										if(s.startsWith("EA")){
											expressNo = s;
											break;
										}
									}
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EMS");
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = EMS" );
								logger.error("expressNo = " + expressNo);
								if(!StringUtil.isBlank(expressNo) && expressNo.startsWith("3SCF")){
									getNodeList(detail.getOrderNo(), expressNo);
								}
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								
								
							} else{
								logger.error("[1]该订单还没发货,没产生物流单号");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
							}
						}
					}
				}
				
				driver.findElement(By.xpath("//a[contains(text(),'退出')]")).click();
				Utils.sleep(4000);
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单");
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}
	
	public void getNodeList(String orderNo,String expressNo){
		try {
			List<WebElement> tarTrs = driver.findElements(By.cssSelector(".result-info tr"));
			if (tarTrs != null && tarTrs.size() > 0) {
				List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
				for (WebElement tarTr : tarTrs) {
					List<WebElement> tarTds = tarTr.findElements(By.cssSelector("td"));
					if (tarTds.size() == 3) {
						String date = tarTds.get(0).getText().trim();
						String context = tarTds.get(2).getText().trim();
						if (!StringUtil.isBlank(date) && !"-".equals(date)) {
							ExpressNode expressNode = new ExpressNode();
							expressNode.setExpressNo(expressNo);
							expressNode.setOrderNo(orderNo);
							expressNode.setName(context);
							try {
								expressNode.setOccurTime(DateUtils.ymdhmsString2DateTime(date));
							} catch (Exception e) {
								logger.debug("日期转换异常：" + date);
							}
							if (context.contains("delivered") || context.contains("签收")) {
								expressNode.setStatus(14);
							} else {
								expressNode.setStatus(3);
							}
							nodeList.add(expressNode);
						}
					}
				}
				logger.debug("nodeList:" + nodeList.size());
				if (nodeList.size() > 0 && getTask() != null) {
					logger.error("addParam expressNodeList");
					logger.debug(nodeList.toString());
					getTask().addParam("expressNodeList", nodeList);
				}
			}
		} catch (Exception e) {
			logger.error("expressNodeList 出错",e);
		}
		
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("http://www.ba.de");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".welcome")));
			return true;
		} catch (Exception e) {
			logger.error("--->跳转ba主页面碰到异常");
		}
		return false;
	}


	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		timer.cancel();
		return true;
	}
	
	public static void main(String[] args) {
		BodyguardapothekeAutoBuy auto = new BodyguardapothekeAutoBuy();
		//AutoBuyStatus status = auto.login("xnangnn2@126.com", "tfb001001");
		//System.out.println(status);
		//auto.cleanCart();
	
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=f3ceMTHcH1xI6Y44JxqjTh3yFw5V3iGTai2kWcaja07f4APnzhOViTqluoB_ayMq_afiw_c&new=http%3A%2F%2Fwww.ba.de%2F1019391.html&tag=");
		param.put("num", "2");
		param.put("productEntityId", "8039475");
		param.put("isPay", "false");
		param.put("my_price", "99.00");
		param.put("userName", "xnangnn2@126.com");
		param.put("password", "tfb001001");
		param.put("size", "1");
		auto.selectProduct(param);
//		param.put("userName", "tzwdbd@126.com");
//		param.put("password", "123456");
		param.put("promotion", "BA3HT");
//		
		UserTradeAddress address = new UserTradeAddress();
		address.setState("上海市");
		address.setCity("杨浦区");
		address.setDistrict("杨浦区");
		address.setAddress("营口北路268号富维江森技术中心");
		address.setIdCard("22010519880711061X");
		address.setMobile("13624494790");
		address.setZip("130000");
		address.setName("史鑫");
		OrderPayAccount payaccount = new OrderPayAccount();
		payaccount.setAccount("fitboy964130@163.com");
		payaccount.setPayPassword("00101012");
		auto.pay(param, address, payaccount);
		
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("BA1497161784196");
//		auto.scribeExpress(detail);
		//auto.logout();*/
	}

}
 