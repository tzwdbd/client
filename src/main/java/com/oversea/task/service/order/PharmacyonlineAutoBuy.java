package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年9月28日 上午10:45:35 
*/
public class PharmacyonlineAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	private Timer timer;
	public PharmacyonlineAutoBuy() {
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
		driver.get("http://cn.pharmacyonline.com.au/");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement activeBox = driver.findElement(By.xpath("//div[@class='active_gg_box']"));
			WebElement need_close = activeBox.findElement(By.xpath("//p[@class='active_gg_close']"));
			need_close.click();
		} catch (Exception e) {
			logger.error("--->没有蒙层不需要关闭");
		}
		
		try {
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='HeaderWord login']")));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".HeaderWord.logout")));
			logger.debug("--->登录完成");
		}
		catch (Exception e)
		{
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		// 跳转到购物车
		try {
			Utils.sleep(1000);
			driver.findElement(By.xpath("//a[@class='HeaderCart']")).click();
			logger.error("--->开始跳转到购物车");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 45);
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
//			logger.debug("--->购物车不为空！",e);
//			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
//		}
		
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("col-main")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElment = driver.findElement(By.cssSelector(".DetailPrice .PriceNow"));
			String priceStr = priceElment.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && priceStr.startsWith("AU$") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->[1]找到商品单价 = " + priceStr.replace("AU$", ""));
				priceMap.put(productEntityId, priceStr.replace("AU$", ""));
			}
		} catch (Exception e) {
			try {
				WebElement priceElment = driver.findElement(By.cssSelector(".price-information .end-price"));
				String priceStr = priceElment.getText();
				String productEntityId = param.get("productEntityId");
				if (!Utils.isEmpty(priceStr) && priceStr.startsWith("AU$") && StringUtil.isNotEmpty(productEntityId)) {
					logger.debug("--->[2]找到商品单价 = " + priceStr.replace("AU$", ""));
					priceMap.put(productEntityId, priceStr.replace("AU$", ""));
				}
			} catch (Exception e2) {
				logger.error("--->单价获取失败");
			}
			
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (StringUtil.isNotEmpty(productNum) && !productNum.equals("1"))
		{
			try
			{
				logger.debug("--->选择数量:" + productNum);
				WebElement numInput = driver.findElement(By.cssSelector(".quantity-display"));
				numInput.clear();
				TimeUnit.SECONDS.sleep(2);
				numInput.sendKeys(productNum);
				TimeUnit.SECONDS.sleep(5);
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
			WebElement cart = driver.findElement(By.cssSelector(".btn-buy"));
			Utils.sleep(1500);
			cart.click();
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		logger.debug("--->等待购物车页面加载");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("easyDialogYesBtn")));
			WebElement easyBtn = driver.findElement(By.id("easyDialogYesBtn"));
			easyBtn.click();
			Utils.sleep(1500);
			logger.debug("--->跳转到购物车页面成功");
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param, UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount) {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		// 设置价格
		logger.error("--->myPrice = " + myPrice);

		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("a[sel-id='cart-btn-settle']")));
			WebElement goPay = driver.findElement(By.cssSelector("a[sel-id='cart-btn-settle']"));
			driver.executeScript("var tar=arguments[0];tar.click();", goPay);
			Utils.sleep(3000);
		} catch (Exception e) {
			logger.debug("--->加载Pharmacyonline结账出现异常");
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
		
		//进行身份证验证
		/*String id_card = userTradeAddress.getIdCard();
		  boolean isSuccess = false;
		int loop = 0;
		logger.debug("--->进行身份证验证");
        while (!isSuccess && loop <= 2) {
            try {
            	Utils.sleep(1500);
				WebElement idcard = driver.findElement(By.id("receiver-id"));
				idcard.clear();
				Utils.sleep(1500);
				idcard.sendKeys(id_card);
				Utils.sleep(1500);
				WebElement idSubBtn = driver.findElement(By.id("idSubBtn"));
				idSubBtn.click();
				Utils.sleep(5000);
				WebElement passValidate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='pass-validate']")));
				if(passValidate!=null){
					String tip = passValidate.getText();
					if(tip!=null && tip.contains("（您已通过实名验证）")){
						logger.debug("--->身份证号验证成功，您已通过实名验证");
					}
				} else {
					logger.debug("--->身份证号验证出错");
					return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_FAIL;
				}
				Utils.sleep(1500);
				isSuccess = true;
            } catch (Exception e) {
            	isSuccess = false;
                loop++;
                logger.debug("--->身份证验证出现异常= ", e);
                return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_FAIL;
            }
        }
		
		if(!isSuccess){
			logger.debug("--->身份证校验出错");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}*/
		
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
		
		//选择支付方式
		logger.debug("--->选择支付方式");
		try {
			Utils.sleep(1500);
			WebElement alipay = null;
			try {
				alipay = driver.findElement(By.id("p_method_alipay_payment"));
			} catch (Exception e) {
				alipay = driver.findElement(By.id("payment-alipay_payment"));
			}
			
			alipay.click();
			Utils.sleep(1500);
			logger.debug("--->选择支付宝支付");
		} catch (Exception e) {
			logger.debug("--->选择支付方式出现异常= ", e);
		}
		
			//结账
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			boolean isEffective = false;
			Set<String> promotionList = getPromotionList(param.get("promotion"));
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
		
		// 查询优惠
		try {
			logger.debug("--->开始查询优惠");
			WebElement totalElement = driver
					.findElement(By.cssSelector(".computed-price"));
			
			List<WebElement> expressElements = totalElement.findElements(By.cssSelector("#H-ship"));
			BigDecimal totalExpress = new BigDecimal(0); 
			for(WebElement w:expressElements){
				if (!Utils.isEmpty(w.getText()) && w.getText().indexOf("AU$") != -1) {
					String express = w.getText().replace("AU$", "");
					logger.debug("--->[1]找到运费 = " + express);
					BigDecimal x = new BigDecimal(express);
					totalExpress = totalExpress.add(x);
				}
				logger.debug("--->[1]找到总运费 = " + totalExpress);
				data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE,String.valueOf(totalExpress));
			}
			WebElement promotionElement = totalElement.findElement(By.cssSelector("#p-ship"));
			if (!Utils.isEmpty(promotionElement.getText()) && promotionElement.getText().indexOf("AU$") != -1) {
				String promotion = promotionElement.getText().replace("-AU$", "");
				logger.debug("--->[1]找到商品优惠 = " + promotion);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PROMOTION_FEE,promotion);
			}
		} catch (Exception e) {
			logger.debug("--->查询总运费出现异常=", e);
		}
		
		String cardNo = param.get("cardNo");
		logger.error("cardNo = "+cardNo);
		
		//提交订单
		logger.debug("--->开始点击提交订单 orderPayAccount.getPayPassword() = "+orderPayAccount.getPayPassword());
		try{
			WebElement placeOrder = null;
			try {
				placeOrder = driver.findElement(By.id("onestepcheckout-place-order"));
			} catch (Exception e) {
				placeOrder = driver.findElement(By.id("priceConfirm"));
			}
			
			driver.executeScript("var tar=arguments[0];tar.click();", placeOrder);
			//driver.findElement(By.xpath("//button[@id='onestepcheckout-place-order']")).click();
			WebElement gotologin = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#J_tip_qr a.switch-tip-btn")));
			gotologin.click();
			logger.error("支付宝登陆按钮点击");
			
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
			By byby = By.xpath("//p[@class='order-id']");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderElement.getText().substring(4));
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
			driver.navigate().to("http://cn.pharmacyonline.com.au/sales/order/history");
		}
		catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待order页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='my-account']")));
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
				List<WebElement> orderList = driver.findElements(By.cssSelector("div.order-item table.order-table"));
				if(orderList != null && orderList.size() > 0){
					for (WebElement orders : orderList) {
						WebElement orderNo = orders.findElement(By.cssSelector("td.order-summary span.order-number"));
						boolean flag = orderNo.getText().trim().replaceAll("[^\\d]", "").contains(mallOrderNo.trim());
						logger.debug("--->orderNo text:" + orderNo.getText().trim().replaceAll("[^\\d]", "") + "对比结果:" + flag);
						if (flag) {
							// 订单编号状态
							WebElement status = orders.findElement(By.cssSelector("span.order-status"));
							
							String str = status.getText().trim();
							if(StringUtil.isNotEmpty(str) && str.contains("已取消")){
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							}
							if (str.equals("已发货")) {
								Utils.sleep(2500);
								logger.debug("--->查找物流单号");
								
								List<WebElement> tarTds = orders.findElements(By.cssSelector("div.hb-shipment span"));
								if(tarTds!=null && tarTds.size() > 0){
									String expressNoEle = tarTds.get(0).getText();
									String expressCompanyEle = tarTds.get(1).getText();
									
									String expressCompany = expressNoEle.trim().replace("物流厂商:", "");
									String expressNo = expressCompanyEle.trim().replace("运单号:", "");
									
									if(StringUtil.isNotEmpty(expressCompany) && expressCompany.equals("EWE-AU")){
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EWE全球");
									} else {
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
									}
									
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
									logger.error("expressCompany = " + expressCompany);
									logger.error("expressNo = " + expressNo);

									return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								}
							} else{
								try {
									logger.debug("去EWE官网查一哈");
									driver.navigate().to("https://ewe.com.au/track?cno="+mallOrderNo+"614816222553901#track-results");
									Matcher m = Pattern.compile("SPOA[0-9]{5,7}").matcher(driver.getPageSource());
									if(m.find()){
										String trackNo =  m.group();
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EWE全球");
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO,trackNo);
										logger.debug("trackNo:"+trackNo);
										return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
									}
								} catch (Exception e) {

								}
								logger.error("[1]该订单还没发货,没产生物流单号");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
							}
							break loop;
						}
					}
				}
				
				driver.findElement(By.xpath("//a[@class='next_jump']")).click();
				Utils.sleep(4000);
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单");
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("http://cn.pharmacyonline.com.au/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'退出')]")));
			return true;
		} catch (Exception e) {
			logger.error("--->跳转pharmacyonline主页面碰到异常");
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
		PharmacyonlineAutoBuy auto = new PharmacyonlineAutoBuy();
		AutoBuyStatus status = auto.login("thrmas@163.com", "tfb001001");
//		System.out.println(status);
		//auto.cleanCart();
//	
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=e5d3SjZ7JpLUsz8IO8lPw0WSuw7FeaFDPHiYHxSmoimgsq3RenHAdTN5CstYfU2Q&new=http%3A%2F%2Fcn.pharmacyonline.com.au%2F1106863.html&tag=");
		param.put("num", "2");
		param.put("productEntityId", "4288120");
		param.put("isPay", "false");
//		param.put("count", "2");
//		param.put("suffixNo", "123");
		param.put("my_price", "16.45");
		auto.selectProduct(param);
//		Map<String, String> param1 = new HashMap<String, String>();
//		param1.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=8169bYNrDG8mqBgBHgc7oVMixsRf4wVEmHBot7JgKSCILAR4OUd41GzivIzzFVYo&new=http%3A%2F%2Fcn.pharmacyonline.com.au%2F1114069.html%2F&tag=");
//		param1.put("num", "1");
//		param1.put("productEntityId", "42881200");
//		param1.put("isPay", "false");
		param.put("count", "2");
		param.put("suffixNo", "123");
		param.put("my_price", "19.25");
//		auto.selectProduct(param1);
		param.put("userName", "hanhya@outlook.com");
		param.put("password", "tfb001001");
		
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
		payaccount.setAccount("fitboy96430@163.com");
		payaccount.setPayPassword("0010012");
		//auto.pay(param, address, payaccount);
//		
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("614792670649618");
//		auto.scribeExpress(detail);
//		auto.logout();
	}

}
 