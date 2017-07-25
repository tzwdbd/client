package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.remote.server.handler.FindElements;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.ReCaptchasHelper;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

/**
 * @author xiong chen
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.service.order
 * @Description:
 * @date 
 */

public class IherbAutoBuy extends AutoBuy
{
	protected final Logger logger = Logger.getLogger(getClass());
	
	public IherbAutoBuy() {
		super(true,false);
	}
	@Override
	public AutoBuyStatus login(String userName, String passWord)
	{
		logger.debug("--->调整浏览器尺寸和位置");
//		driver.manage().window().setSize(new Dimension(414, 1000));
		Cookie ck=new Cookie("iher-pref1","ctd=www&sccode=US&scurcode=USD&lan=en-US&lchg=1&ifv=1&chkdc=6");
		driver.manage().addCookie(ck);
		driver.manage().window().setPosition(new Point(0, 0));
		driver.manage().window().maximize();
		driver.get("http://www.iherb.com/");
		WebDriverWait wait = new WebDriverWait(driver, 40);
		try {
			driver.findElement(By.cssSelector(".icon-hamburgermenufat")).click();
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->保存编码点击1");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".selected-country-wrapper[data-id='language-menu']")));
			driver.findElement(By.cssSelector(".selected-country-wrapper[data-id='language-menu']")).click();
			logger.debug("--->保存编码点击2");
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropdown-icon")));
			driver.findElement(By.cssSelector(".dropdown-icon")).click();
			logger.debug("--->保存编码点击3");
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.cssSelector(".search-list .item")).click();
			logger.debug("--->保存编码点击4");
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.cssSelector(".save-selection")).click();
			logger.debug("--->保存编码点击5");
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			logger.debug("--->保存编码出错",e);
		}
		
		//.icon-hamburgermenufat .icon-globeoutline .dropdown-icon .search-list .item .save-selection
		try
		{
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("iherb-my-account")));
			signIn.click();
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By
					.xpath("//input[@id = 'UserName' and @placeholder = 'Email Address']")));
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			AutoBuyStatus code = getCode();
			 if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(code)){
				 try
					{
						WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By
								.xpath("//input[@id = 'UserName' and @placeholder = 'Email Address']")));
						username.sendKeys(userName);
						logger.debug("--->输入账号");
						TimeUnit.SECONDS.sleep(5);
					}
				 catch (Exception e1)
				 	{
					 return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
					}
			 }else{
				 return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			 }
		}

		try
		{
			WebElement password = driver.findElement(By
					.xpath("//input[@id = 'Password' and @placeholder = 'Password']"));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement btn = driver.findElement(By.name("save"));
			TimeUnit.SECONDS.sleep(5);
			btn.click();
			logger.debug("--->开始登陆");
			
			try {
				driver.findElement(By.xpath("//span[contains(text(),'something')"));
				TimeUnit.SECONDS.sleep(30);
			} catch (Exception e) {
			}
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("responsive-container")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section.my-account-section")));
			logger.info("--->登录成功");
		}
		catch (Exception e)
		{
			
			for(int i=0;i<3;i++){
				boolean mark = true;
				try {
					WebElement capImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recaptcha_challenge_image")));
					logger.error("--->开始破解验证码");
					String src = capImage.getAttribute("src");
					logger.error("--->src:" + src);
					String cap = ReCaptchasHelper.getCaptchas(src, "iherb");
					if(!StringUtils.isBlank(cap)){
						try
						{
							WebElement	capGuess = driver.findElement(By.id("recaptcha_response_field"));
							logger.debug("--->输入验证码"+cap);
							capGuess.sendKeys(cap);
							TimeUnit.SECONDS.sleep(5);
							WebElement btn = driver.findElement(By.id("dCF_input_complete"));
							logger.debug("--->验证验证码");
							btn.click();
						}
						catch (Exception ex)
						{
							logger.error("--->没有找到验证码框", ex);
							return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
						}
						
						try
						{
							WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("iherb-my-account")));
							signIn.click();
							logger.debug("--->重试--->跳转到登录页面");
						}
						catch (Exception e1)
						{
							logger.error("--->重试--->没有找到登陆按钮", e1);
							if(i==2){
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
							}else{
								mark = false;
							}
						}
						if(mark){
							try
							{
								WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By
										.xpath("//input[@id = 'UserName' and @placeholder = 'Email Address']")));
								username.sendKeys(userName);
								logger.debug("--->重试--->输入账号");
								TimeUnit.SECONDS.sleep(5);
							}
							catch (Exception e1)
							{
								logger.error("--->重试--->没有找到输入框", e1);
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
							}
	
							try
							{
								WebElement password = driver.findElement(By
										.xpath("//input[@id = 'Password' and @placeholder = 'Password']"));
								password.sendKeys(passWord);
								logger.debug("--->重试--->输入密码");
								TimeUnit.SECONDS.sleep(5);
							}
							catch (Exception e1)
							{
								logger.error("--->重试--->没有找到密码框", e1);
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
							}
	
							try
							{
								WebElement btn = driver.findElement(By.name("save"));
								TimeUnit.SECONDS.sleep(5);
								btn.click();
								logger.debug("--->重试--->开始登陆");
								
								try {
									driver.findElement(By.xpath("//span[contains(text(),'something')"));
									TimeUnit.SECONDS.sleep(30);
								} catch (Exception e1) {
								}
							}
							catch (Exception e1)
							{
								logger.error("--->重试--->没有找到登陆确定按钮", e);
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
								
							}
							break;
						}
					}
				} catch (Exception e2) {
					logger.error("重试222--->登录失败", e);
					return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
				}
			}
			
			try
			{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section.my-account-section")));
				logger.info("--->重试--->登录成功");
			}
			catch (Exception e1)
			{
				logger.error("重试--->登录失败", e);
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}
	
	public AutoBuyStatus getCode(){
		WebDriverWait wait = new WebDriverWait(driver, 40);
		try {
			WebElement capImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recaptcha_challenge_image")));
			logger.error("--->开始破解验证码");
			String src = capImage.getAttribute("src");
			logger.error("--->src:" + src);
			String cap = ReCaptchasHelper.getCaptchas(src, "iherb");
			if(!StringUtils.isBlank(cap)){
				try
				{
					WebElement	capGuess = driver.findElement(By.id("recaptcha_response_field"));
					logger.debug("--->输入验证码"+cap);
					capGuess.sendKeys(cap);
					TimeUnit.SECONDS.sleep(5);
					WebElement btn = driver.findElement(By.id("dCF_input_complete"));
					logger.debug("--->验证验证码");
					btn.click();
					return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
				}
				catch (Exception ex)
				{
					logger.error("--->没有找到验证码框", ex);
					return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
				}
				
			}
		} catch (Exception e2) {
			logger.error("重试333--->登录失败", e2);
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
	}

	@Override
	public AutoBuyStatus cleanCart()
	{
		driver.navigate().to("https://checkout.iherb.com/editcart");
		Utils.sleep(3000);
		
		logger.error("--->开始清理购物车");
		List<WebElement> list = driver.findElements(By.xpath("//button[@class='btn btn-remove-item']"));
		while (true) {
			int size = list.size();
			if(list!=null && size>0){
				list.get(0).click();
				Utils.sleep(2000);
				if(size>1){
					list = driver.findElements(By.xpath("//button[@class='btn btn-remove-item']"));
				}else{
					break;
				}
			}else{
				break;
			}
		}
		Utils.sleep(2000);
		logger.error("--->购物车页面清理完成");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			logger.error("--->确认购物车是否清理完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='empty-state']")));
		} catch (Exception e) {
			logger.debug("--->购物车数量清空异常");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	WebElement whenMainPanelReady()
	{
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("product")));
		return panel;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param)
	{
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		logger.debug("--->跳转到商品页面");
		String url = param.get("url");
		logger.debug("--->url:" + url);
		try
		{
			driver.navigate().to(url);
			TimeUnit.SECONDS.sleep(10);
		}
		catch (Exception e)
		{
			logger.error("--->打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		String title = "";
		
		try
		{
			title = driver.findElement(By.xpath("//div[@class='col-xs-24']/strong")).getText();
			
			WebElement main = whenMainPanelReady();
			List<WebElement> info = main.findElements(By.cssSelector("section.product-info div.row"));
			if (info != null && info.size() == 1)
			{	
				WebElement addToCart =  main.findElement(By.xpath("//section[@class = 'product-submit product-section']/form[@name='addCart']/input[@id='addToCart']"));;
				
				if (addToCart == null) {
					logger.warn("--->url:" + url + " \t已经下架或者缺货");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
				
				WebElement priceElement =  main.findElement(By.className("iherb-price"));
				String price  = priceElement.getText().replace("$", "");
				
				logger.warn("--->单价 :" + price);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
				
				String productEntityId = param.get("productEntityId");
				if(StringUtil.isEmpty(productEntityId)){
					logger.error("--->productEntityId  is Null");
				}else{
					priceMap.put(param.get("productEntityId"), price);
				}
				
				
				String number = param.get("num");
				if(!"1".equals(number)){
					try {
						WebElement ddlQty = driver.findElement(By.xpath("//select[@id = 'ddlQty']"));
						Select select = new Select(ddlQty);
						select.selectByValue(number);
						Utils.sleep(2000);
					} catch (Exception e) {
						logger.warn("--->选择数量异常 ");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
				
				try
				{
					TimeUnit.SECONDS.sleep(1);
					WebElement addCart = driver.findElement(By.xpath("//section[@class ='product-submit product-section']/form[@name ='addCart']"));
					TimeUnit.SECONDS.sleep(3);
					//addCart.click();
					addCart.submit();
					TimeUnit.SECONDS.sleep(5);
					logger.debug("--->添加到购物车");
					
					WebElement products = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
							"//section[@class='products']")));
					List<WebElement> productList = products.findElements(By.xpath(".//article"));
					boolean hasFind = false;
					for (int i = 0; i < productList.size(); i++) {
						WebElement product = productList.get(i);
						if(product.getText().toLowerCase().contains(title.toLowerCase())){
							logger.debug("找到商品，是第"+(i+1)+"个");
							hasFind = true;
							WebElement num = product.findElement(By.xpath(".//select[@class='quantity']/option[@selected='']"));
							logger.debug("这个商品的数量："+num.getAttribute("value"));
							if(!num.getAttribute("value").equals(number)){
								logger.debug("调整数量");
								WebElement nums = product.findElement(By.xpath(".//select[@class='quantity']"));
								Select select = new Select(nums);
								select.selectByValue(number);
								TimeUnit.SECONDS.sleep(2);
								break;
							}
							break;
						}
					}
					if(hasFind==false){
						logger.debug("这个商品加入购物车失败");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
					
				}
				catch (Exception e)
				{	
					logger.debug("--->添加到购物车失败");
					return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
				}
			}
			else
			{
				logger.error("--->SKU信息获取失败");
				return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
			}
		}
		catch (Exception e)
		{
			logger.error("--->选择SKU异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}

		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount )
	{	
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String myPrice = param.get("my_price");
		Boolean isPay = Boolean.valueOf(param.get("isPay"));
		String cNo = param.get("cardNo");
		String owner = param.get("owner");
		String suffixNo = param.get("suffixNo");
		String expiryDate = param.get("expiryDate");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		try
		{
			logger.error("--->跳转到购物车");
			WebElement cart = driver.findElement(By.xpath("//i[@class ='icon-cart']"));
			Utils.sleep(1000);
			cart.click();
			Utils.sleep(3500);
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
//		validateVisaCard(param);
		
		
		
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			
			try {
				logger.debug("进入优惠吗页面");
				TimeUnit.SECONDS.sleep(2);
				WebElement enterCodes = driver.findElement(By.cssSelector("div.enter-promo-codes"));
				enterCodes.click();
			} catch (Exception e) {
				logger.debug("进入优惠吗页面异常");
			}
			
			
			for (String code : promotionList) {
				logger.debug("code:" + code);
				try {
					logger.debug("输入优惠吗");
					TimeUnit.SECONDS.sleep(2);
					WebElement codeEl = driver.findElement(By.xpath("//input[@id='nCopounCode']"));
					codeEl.clear();
					codeEl.sendKeys(code);

					logger.debug("点击确认");
					TimeUnit.SECONDS.sleep(2);
					WebElement apply = driver.findElement(By
							.xpath("//a[@id='applyPromoCode']"));
					apply.click();;
					TimeUnit.SECONDS.sleep(2);

					try {
						logger.debug("查询优惠吗书否有效");
						WebElement cancle = driver.findElement(By.xpath("//div[@class='pop-cancel']"));
						cancle.click();
						statusMap.put(code, 0);
						logger.debug("优惠码无效【1】:" + code);
					} catch (Exception e) {
						try {
							WebElement codeEls = driver.findElement(By.xpath("//div[@class='col-xs-18 col']"));
							logger.debug("codeEls.getText()"+codeEls.getText());
							if (codeEls.getText().contains(code)) {
								isEffective = true;
								logger.debug("优惠码有效【2】");
								statusMap.put(code, 10);
								break;
							}
						} catch (Exception e2) {
							logger.debug("异常【3】", e2);
						}
					}
				} catch (Exception e) {
					logger.debug("输入优惠吗异常【4】", e);
				}
			}
			
			try {
				WebElement ctn = driver.findElement(By.cssSelector("#promoCodes > section:nth-child(4) > div:nth-child(1)"));
				ctn.click();
			} catch (Exception e) {
				logger.debug("确认失败",e);
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		try
		{
			Utils.sleep(2000);
			WebElement checkout = driver.findElement(By.xpath("//a[@class = 'ihb-btn ihb-btn-orange proceed-to-checkout']"));
			checkout.sendKeys(Keys.RETURN);
			Utils.sleep(2000);
			logger.info("--->跳转到结算页面");
			ensureCard(param);
		}
		catch (Exception e)
		{
			logger.error("--->跳转到结算页面异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		
		//判断是否直接跳转到选地址页面
		Utils.sleep(5000);
		try {
			List<WebElement> ships = driver.findElements(By.cssSelector(".shipping-methods-list"));
			if(ships!=null && ships.size()==4){
				driver.navigate().to("https://checkout.iherb.com/EditCart");
				try
				{
					Utils.sleep(2000);
					WebElement checkout = driver.findElement(By.xpath("//a[@class = 'ihb-btn ihb-btn-orange proceed-to-checkout']"));
					checkout.sendKeys(Keys.RETURN);
					Utils.sleep(2000);
					logger.info("--->跳转到结算页面");
					ensureCard(param);
				}
				catch (Exception e)
				{
					logger.error("--->跳转到结算页面异常",e);
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		} catch (Exception e) {
			logger.error("--->okok");
		}
		
		
		boolean isGotoAddress = false;
		try{
			driver.findElement(By.id("chkOutAddrSelct"));
			isGotoAddress = true;
		}catch(Exception e){}
		
		if(!isGotoAddress){
			try
			{
				logger.error("--->第一步");
				WebElement payPanel = whenPayPanelReady();
				try {
					WebDriverWait wait0 = new WebDriverWait(driver, 30);
					wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-select-btn")));
					List<WebElement> cards = driver.findElements(By.cssSelector(".card-select-btn"));
					for(WebElement w:cards){
						w.click();
						logger.error("--->card-select-btn click");
						break;
					}
					Utils.sleep(2000);
					wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".address-continue")));
					List<WebElement> addresss = driver.findElements(By.cssSelector(".address-continue"));
					for(WebElement w:addresss){
						if(w.isDisplayed()){
							w.click();
							logger.error("--->address-continue click");
							break;
						}
					}
				} catch (Exception e) {
					
				}
				Utils.sleep(5000);
				WebElement edit = payPanel.findElement(By.id("shipAddrChng"));
				edit.click();
				Utils.sleep(2000);
				logger.info("--->开始选择地址");
			}
			catch (Exception e)
			{
				logger.error("--->找不到 edit地址按钮",e);
				try {
					WebElement edit = driver.findElement(By.id("shipAddrChng"));
					edit.click();
					logger.error("--->shipAddrChng click");
					Utils.sleep(2000);
				} catch (Exception e2) {
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chkOutAddrSelct")));
			
			WebElement addr = driver.findElement(By.id("switchAddr"));
			addr.click();
			WebElement addrcount = driver.findElement(By.id("ddlCountries"));
			Select select = new Select(addrcount);
			select.selectByVisibleText("China");
			Utils.sleep(1500);
			
			WebElement fullname = driver.findElement(By.id("lblFullName"));
			fullname.sendKeys(userTradeAddress.getName());
			Utils.sleep(1500);
			
			WebElement lblAddressLine1 = driver.findElement(By.id("lblAddressLine1"));
			lblAddressLine1.sendKeys(userTradeAddress.getDeliveryAddress());
			Utils.sleep(1500);
			
			WebElement city = driver.findElement(By.id("City"));
			city.sendKeys(userTradeAddress.getCity());
			Utils.sleep(1500);
			
			WebElement regionName = driver.findElement(By.id("RegionName"));
			regionName.sendKeys(userTradeAddress.getState());
			Utils.sleep(1500);
			
			WebElement postalCode = driver.findElement(By.id("PostalCode"));
			postalCode.sendKeys(userTradeAddress.getZip());
			Utils.sleep(1500);
			
			WebElement telNumber = driver.findElement(By.id("TelNumber"));
			telNumber.sendKeys(userTradeAddress.getMobile());
			Utils.sleep(1500);
			
			WebElement continueSwitch = driver.findElement(By.id("continueSwitchAddr"));
			continueSwitch.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		try {
			WebElement shipcontinue = driver.findElement(By.id("ship-continue"));
			shipcontinue.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->ship-continue id");
			try {
				WebElement shipcontinue = driver.findElement(By.cssSelector(".ship-continue"));
				shipcontinue.click();
				Utils.sleep(1500);
			} catch (Exception e2) {
				logger.debug("--->ship-continue class");
			}
			
		}
		try {
			WebElement radioI = driver.findElement(By.cssSelector(".box-flex[for='radioID']"));
			radioI.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->radioID",e);
		}
		try {
			WebElement identification = driver.findElement(By.id("identificationCode"));
			identification.sendKeys(userTradeAddress.getIdCard());
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->identification",e);
		}
		
		try {
			driver.executeScript("(function(){window.scrollBy(0,350);})();");
			Utils.sleep(1500);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btnSSN")));
			WebElement btnSSN = driver.findElement(By.id("btnSSN"));
			driver.executeScript("var tar=arguments[0];tar.click();", btnSSN);
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->btnSSN",e);
		}
		try {
			WebElement cardSelect = driver.findElement(By.cssSelector(".card-select-btn"));
			cardSelect.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->cardSelect",e);
		}
		
		try {
			List<WebElement> addresscontinues = driver.findElements(By.cssSelector(".address-continue"));
			for(WebElement w:addresscontinues){
				if(w.isDisplayed() && w.isEnabled()){
					w.click();
				}
			}
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->addresscontinue",e);
		}
		
		try {
			WebElement input = driver.findElement(By.cssSelector("input[name='cardNumber']"));
			
			logger.debug("--->找到信用卡输入框,开始输入");
			Utils.sleep(1500);
			String cardNo = param.get("cardNo");
			logger.debug("--->信用卡卡号是 = "+cardNo);
			
			if(Utils.isEmpty(cardNo)){
				logger.debug("--->没有找到可用的信用卡卡号");
			}
			input.sendKeys(cardNo);
			Utils.sleep(1500);
		} 
		catch (Exception e) {
			logger.debug("--->没找到需要输入卡号的input",e);
		}
		
		try {
			WebElement verif = driver.findElement(By.cssSelector("#ccVerify div button.continue"));
			verif.click();
			logger.debug("--->visa卡验证完成");
		} catch (Exception e) {
			logger.debug("--->没找到提交按钮");
		}
		
		
		
		
		
//		try
//		{
//			// 选择送货地址
//			String count = param.get("count");
//			WebElement selectAddr = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("chkOutAddrSelct")));
//			List<WebElement> addrs = selectAddr.findElements(By.cssSelector("div.saved-address"));
//			if (addrs != null && addrs.size() > 0)
//			{
//				logger.debug("--->目前共有[" + addrs.size() + "]个可用地址");
//				int index = 0;
//				try
//				{
//					index = Integer.valueOf(count);
//					int tarAddr = index % 4;
//
//					WebElement cur = addrs.get(tarAddr);
//					if(tarAddr == 2){
//						driver.executeScript("(function(){window.scrollBy(0,250);})();");
//					}
//					logger.info("--->选择第[" + (tarAddr + 1) + "]个地址");
//					cur.sendKeys(Keys.LEFT);
//					cur.sendKeys(Keys.ENTER);
//					Utils.sleep(1000);
//					
//					List<WebElement> curs = cur.findElements(By.xpath("//form[@class ='saved-info-radioBtn']"));
//					curs.get(tarAddr).click();
//					Utils.sleep(1000);
//					try {
//						logger.error("--->点击continue按钮");
//						WebElement ctn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addressSelectBtn")));
//						ctn.click();
//					} 
//					catch (Exception e) {
//						logger.error("--->没有找到continue");
//						return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
//					}
//				}
//				catch (Exception e)
//				{
//					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
//				}
//			}
//			else
//			{
//				return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
//			}
//			
//		}
//		catch (Exception e)
//		{
//			logger.error("--->选择地址,visa卡,物流出错",e);
//			return AutoBuyStatus.AUTO_PAY_FAIL;
//		}
		validateVisaCard(param);
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			Utils.sleep(1500);
			WebElement totalPriceElement = driver.findElement(By.xpath("//bdi[@dir='ltr']"));
			Utils.sleep(1500);
			String text = totalPriceElement.getText();
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = "+priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 10.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
		}
		
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			WebElement placeOrderElement = driver.findElement(By.id("btnPlaceOrder"));
			Utils.sleep(3000);
			
			if(isPay){
				logger.debug("--->要付款了,慎重慎重");
				placeOrderElement.click();
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		
		//获取订单号
		try {
			Utils.sleep(1500);
			WebElement web = driver.findElement(By.xpath("//span[contains(text(),'Order Number')]"));
			WebElement orderNumberElement =  web.findElement(By.xpath("parent::div"));
			String orderNumber = orderNumberElement.getText().replace("Order Number:", "").replace("\n", "");
			if(StringUtil.isNotEmpty("orderNumber")){
				logger.debug("--->获取iherb单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
			}else{
				logger.error("--->获取iherb单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} 
		catch (Exception e) {
			logger.error("找不到订单号");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	WebElement whenPayPanelReady()
	{
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkoutContainer")));
		return panel;
	}

	void ensureCard(Map<String, String> param){
		logger.error("--->ensureCard enter");
		try {
			WebDriverWait wait0 = new WebDriverWait(driver, 30);
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-select-btn")));
			List<WebElement> cards = driver.findElements(By.cssSelector(".card-select-btn"));
			for(WebElement w:cards){
				w.click();
				logger.error("--->card-select-btn click");
				break;
			}
			Utils.sleep(2000);
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".address-continue")));
			List<WebElement> addresss = driver.findElements(By.cssSelector(".address-continue"));
			for(WebElement w:addresss){
				if(w.isDisplayed()){
					w.click();
					logger.error("--->address-continue click");
					break;
				}
			}
		} catch (Exception e) {
			
		}
		
		Utils.sleep(4500);
		try {
			WebElement input = driver.findElement(By.cssSelector("input[name='cardNumber']"));
			
			logger.debug("--->ensureCard 找到输入框");
			Utils.sleep(1500);
			String cardNo = param.get("cardNo");
			logger.debug("--->ensureCard 信用卡卡号是 = "+cardNo);
			
			if(Utils.isEmpty(cardNo)){
				logger.debug("--->ensureCard 没有找到可用的信用卡卡号");
			}
			input.sendKeys(cardNo);
			Utils.sleep(1500);
			driver.findElement(By.cssSelector("button[name='save']")).click();
			Utils.sleep(4500);
		} 
		catch (Exception e) {
			logger.debug("--->没找到需要输入卡号的input",e);
		}
	}
	
	void validateVisaCard(Map<String, String> param)
	{
		//判断是否直接跳转到选信用卡页面
		boolean isGotoCreditcard = false;
		Utils.sleep(4500);
		try{
			driver.findElement(By.cssSelector("div.paymentType"));
			isGotoCreditcard = true;
		}catch(Exception e){}
		
		//可能需要验证VISA卡
		try
		{
			if(!isGotoCreditcard){
				WebElement curPay = driver.findElement(By.className("gray"));
				curPay.click();
				Utils.sleep(4500);
			}
			logger.debug("--->此处可能要验证VISA卡");
			WebElement click = driver.findElement(By.cssSelector("div.paymentType:nth-child(3)"));
			click.click();
			Utils.sleep(3000);
			
			driver.findElement(By.cssSelector("div#creditCardBtn")).click();;	
			Utils.sleep(5500);
			
			try {
				WebElement input = driver.findElement(By.cssSelector("input[name='cardNumber']"));
				
				logger.debug("--->找到信用卡输入框,开始输入");
				Utils.sleep(1500);
				String cardNo = param.get("cardNo");
				logger.debug("--->信用卡卡号是 = "+cardNo);
				
				if(Utils.isEmpty(cardNo)){
					logger.debug("--->没有找到可用的信用卡卡号");
				}
				input.sendKeys(cardNo);
				Utils.sleep(1500);
			} 
			catch (Exception e) {
				logger.debug("--->没找到需要输入卡号的input",e);
			}
			
			try {
				WebElement verif = driver.findElement(By.xpath("//form[@id='ccVerify']//button[@name='save']"));
				verif.click();
				logger.debug("--->visa卡验证完成");
			} catch (Exception e) {
				logger.debug("--->没找到提交按钮");
			}
		}
		catch (Exception e)
		{
			logger.error("此处不需要验证visa卡",e);
		}
	}
	
	public void testAddress(Map<String, String> param){
		driver.get("file:///F:/placeorder.htm");
		WebElement placeOrderElement = driver.findElement(By.id("btnPlaceOrder"));
		placeOrderElement.click();
		
	}
		
	
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; }
		driver.get("http://www.iherb.com/");
		
		WebDriverWait wait = new WebDriverWait(driver, 15);
		
	/*	try
		{
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("iherb-my-account")));
			//WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sign-in")));
			signIn.click();
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}*/
		/*try {
			Utils.sleep(1500);
			WebElement hamburger = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//i[@data-id='hamburger']")));
			logger.debug("--->myAccoun");
			hamburger.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("-->找不到myAccoun");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			Utils.sleep(1500);
			WebElement myAccountBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("My Account")));
			logger.debug("--->myAccoun");
			myAccountBtn.click();
		} 
		catch (Exception e) {
			logger.error("找不到myAccoun按钮");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			Utils.sleep(1500);
			WebElement orderHistory = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Order History")));
			logger.debug("--->Order History");
			orderHistory.click();
		} catch (Exception e) {
			logger.error("找不到orderHistory按钮");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}*/
		
		//找到所有订单,筛选需要的订单，判断订单状态，若已发货，跳转到订单详情页
		try {
			driver.navigate().to("https://secure.iherb.com/myaccount/orderhistory");
			Utils.sleep(1500);
			logger.debug("--->获取所有订单");
			List<WebElement> orderList =  driver.findElements(By.xpath("//section[@class='order-history white-bg-all multi-page']/article/div"));
			if(orderList == null || orderList.size() == 0 ){
				logger.debug("--->订单列表为空");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
			//遍历订单，找到需要的订单
			for (WebElement order : orderList) {
				try {
					String content = order.getText().replace("\n", "}");
					
					logger.debug("--->查找订单号为"+mallOrderNo+"的订单");
					String orderNo = getJSValueByTag(content, "Order Number", ":", "}");
					
					//根据订单号查找到需要的订单
					if(StringUtil.isNotEmpty(orderNo)&&orderNo.equals(mallOrderNo)){
						logger.debug("--->找到了订单号为"+mallOrderNo+"的订单");
						try {
							//取出订单状态
							logger.debug("--->查找单号为"+mallOrderNo+"的订单状态");
							//如果订单状态为Shipping out,点击a标签，跳转到订单详情页面
							String trackingStatue = getJSValueByTag(content, "Status", ":", "}");
							if(StringUtil.isEmpty(trackingStatue)){
								logger.error(mallOrderNo + "该订单状态为空");
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}
							if(trackingStatue.equals("Shipped Out")){
								logger.debug("--->订单已发货");
								String trackingNo =  getJSValueByTag(content, "Tracking", ":", "[\\s\\S]+");
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackingNo);
								logger.debug("--->获取物流单号成功："+trackingNo);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "顺丰");
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								
							}else if(trackingStatue.equals("Processing")){
								logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
							}else if(trackingStatue.equals("Cancelled")){
								logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							}else{
								logger.error(mallOrderNo + " 这个订单状态不明:"+trackingStatue);
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}
						} 
						catch (Exception e) {
							logger.error("找不到订单号为"+mallOrderNo+"的订单状态");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
					}
				} 
				catch (Exception e) {
					logger.error("找不到orderNumber");
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}
		} 
		catch (Exception e) {
			logger.error("找不到订单列表");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return null;
	}

	@Override
	public boolean gotoMainPage()
	{
		try {
			Utils.sleep(2000);
			driver.get("http://www.iherb.com/");
			return true;
		} 
		catch (Exception e) {
			logger.error("gotoMainPage ERROR");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance)
	{
		return null;
	}

	public static void main(String[] args)
	{
		//47.88.9.209
		IherbAutoBuy iherb = new IherbAutoBuy();
		iherb.login("1113973076@qq.com", "qq258369");
		iherb.cleanCart();

		Map<String, String> param = new LinkedHashMap<>();
		param.put("url", "http://www.iherb.com/Gummi-King-Calcium-Plus-Vitamin-D-for-Kids-60-Gummies/34010");
		param.put("url", "http://www.iherb.com/Now-Foods-Maca-Raw-750-mg-90-Veg-Caps/18046");
		param.put("num", "20");
		param.put("count", "4");
		param.put("isPay", "false");
		param.put("my_price", "318");
		param.put("cardNo", "5273380300218539");
		iherb.selectProduct(param);
		iherb.pay(param);
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("43472437");
		//iherb.scribeExpress(detail);
		
		/*Map<String, String> param = new LinkedHashMap<>();
		param.put("cardNo", "5273380300218539");
		param.put("count", "1");
		IherbAutoBuy iherb = new IherbAutoBuy();
		iherb.testAddress(param);;*/
		
	}
		
	public static String getJSValueByTag(String jsContant, String jsName,String cut ,String end ) {
	     
	     	StringBuffer returnVal = new StringBuffer();
	        jsContant = jsContant.replace("'", "").replace( "\"", "");
	       
	        String patternStr = jsName + "\\s*"+cut+"\\s*([\\s\\S]*?)" +end;
	        Pattern p = Pattern. compile(patternStr, Pattern.MULTILINE | Pattern.DOTALL );
	        Matcher m = p.matcher( jsContant);
	        while (m.find()) {
	            returnVal.append( m.group().replaceAll( jsName + "\\s*"+cut+"\\s*" , "" ).replaceAll("\r" , "" ).replaceAll("\n" , "").replace( end, ""));
	        }
	        return returnVal.toString();
	    }

}
