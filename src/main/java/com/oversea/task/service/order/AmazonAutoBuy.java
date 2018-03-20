package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.domain.ExpressNode;
import com.oversea.task.domain.ExternalOrderDetail;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class AmazonAutoBuy extends AutoBuy
{
	public static final String capPath = "http://47.88.7.218:8080/ocr";

	private final Logger logger = Logger.getLogger(getClass());
	private String totalPrice0 = "0";//除去运费的总价
	private String giftBalance = "0";//礼品卡余额
	
	public AmazonAutoBuy(){
		super(true);
	}
	
	public AmazonAutoBuy(boolean isWap){
		super(isWap);
	}

	public AutoBuyStatus login(String userName, String passWord)
	{
		if(isWap){
			logger.debug("--->调整浏览器尺寸和位置");
			driver.manage().window().maximize();
			
			driver.get("https://www.amazon.com");

			WebDriverWait wait = new WebDriverWait(driver, 15);
			// 等到[登陆]出现
			try
			{
				By bySignIn = By.xpath("//a[@id='nav-logobar-greeting']");
				WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(bySignIn));
				logger.debug("--->跳转到登录页面");
//				String url = signIn.getAttribute("href");
//				driver.navigate().to(url);
				signIn.click();
				
				
			}
			catch (Exception e)
			{
				try {
					WebElement signIn = driver.findElement(By.id("gw-sign-in-button"));
					signIn.click();
				} catch (Exception e2) {
					logger.error("--->没有找到登陆按钮", e);
					return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
				}
				
			}
			
			try {
				String currentUrl = driver.getCurrentUrl();
				if(currentUrl.contains("pre-prod.")){
					currentUrl = driver.getCurrentUrl().replace("pre-prod.", "");
					driver.navigate().to(currentUrl);
				}
			} catch (Exception e) {
				logger.error("--->没有找到登陆按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			// 等到[输入框]出现
			try
			{
				Utils.sleep(1000);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email_login")));
				WebElement username = driver.findElement(By.id("ap_email_login"));
				logger.debug("--->输入账号");
				username.sendKeys(userName);
				Utils.sleep(800);
			}
			catch (Exception e)
			{
				logger.error("--->没有找到输入框", e);
				try
				{
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email")));
					WebElement username = driver.findElement(By.id("ap_email"));
					logger.debug("--->输入账号");
					username.sendKeys(userName);
					Utils.sleep(1200);
				}
				catch (Exception e1)
				{
					logger.error("--->没有找到输入框", e1);
					return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
				}
			}
			
			try {
				Utils.sleep(900);
				List<WebElement> continueButton = driver.findElementsByCssSelector("input#continue");
				for(WebElement w:continueButton){
					if(w.isDisplayed()){
						w.click();
						Utils.sleep(1000);
						break;
					}
				}
			} catch (Exception e1) {
				logger.error("--->continue");
			}

			try
			{
				Utils.sleep(800);
				List<WebElement> passwords = driver.findElements(By.id("ap_password"));
				logger.debug("--->输入密码");
				for(WebElement password:passwords){
					if(password.isDisplayed()){
						password.sendKeys(passWord);
						break;
					}
				}
				Utils.sleep(1000);
			}
			catch (Exception e)
			{
				
				logger.error("--->没有找到密码框", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			try
			{
				WebElement btn = driver.findElement(By.id("signInSubmit"));
				logger.debug("--->开始登陆");
				btn.click();
				Utils.sleep(800);
			}
			catch (Exception e)
			{
				logger.error("--->没有找到登陆确定按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			try
			{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
			}
			catch (Exception e)
			{
				logger.error("--->登陆失败,开始判断和处理账号异常");
				try
				{
					WebElement username = driver.findElement(By.id("ap_email_login"));
					logger.debug("--->输入账号1");
					username.clear();
					username.sendKeys(userName);
					Utils.sleep(800);
					List<WebElement> passwords = driver.findElements(By.id("ap_password"));
					for(WebElement password:passwords){
						if(password.isDisplayed()){
							password.clear();
							logger.debug("--->输入密码1");
							password.sendKeys(passWord);
							break;
						}
					}
					Utils.sleep(1000);
					WebElement btn = driver.findElement(By.id("signInSubmit"));
					logger.debug("--->开始登陆1");
					btn.click();
					Utils.sleep(800);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
				}
				catch (Exception e1)
				{
					logger.error("--->登陆失败,开始判断和处理账号异常1");
					// todo 处理异常
					return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
				}
			}
			logger.debug("--->登陆成功,开始跳转");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		}else{
			logger.debug("--->调整浏览器尺寸和位置");
			driver.manage().window().maximize();
			Utils.sleep(1500);
			driver.get("https://www.amazon.com");
			Utils.sleep(3000);
			driver.navigate().to("https://www.amazon.com");
			Utils.sleep(3000);
			
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			
			try{
				WebElement signin = null;
				try {
					signin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#nav-link-accountList")));
				} catch (Exception e) {
					signin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#nav-link-yourAccount")));
				}
				signin.click();
				
				WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ap_email']")));
				email.sendKeys(userName);
				Utils.sleep(1500);
				
				List<WebElement> continueButton = driver.findElementsByCssSelector("input#continue");
				if (continueButton.size() > 0) {
					continueButton.get(0).click();
					Utils.sleep(1500);
				}
				
				driver.findElement(By.xpath("//input[@id='ap_password']")).sendKeys(passWord);
				Utils.sleep(1500);
				driver.findElement(By.xpath("//input[@id='signInSubmit']")).click();
			}catch(Exception e){
				logger.error("--->登陆失败",e);
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
			
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#nav-search")));
			}catch(Exception e){
				logger.error("--->登陆超时",e);
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
			
			logger.debug("--->登陆成功,开始跳转");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		}
	}

	public WebDriver getDriver()
	{
		return this.driver;
	}

	AutoBuyStatus clickCart()
	{
		By cartBy = By.id("nav-button-cart");
		WebElement cartBtn = null;
		try
		{
			TimeUnit.SECONDS.sleep(2);
			cartBtn = driver.findElement(cartBy);
			logger.debug("--->去购物车");
			cartBtn.click();
		}
		catch (Exception e)
		{
			try
			{
				cartBy = By.id("navbar-icon-cart");
				cartBtn = driver.findElement(cartBy);
				logger.debug("--->去购物车");
				cartBtn.click();
			}
			catch (Exception ex)
			{
				logger.error("--->跳转到购物车失败");
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
	}

	public AutoBuyStatus cleanCart()
	{
		AutoBuyStatus status = clickCart();
		if (!status.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) { return AutoBuyStatus.AUTO_CLICK_CART_FAIL; }

		logger.debug("--->清空购物车");
		try
		{
			// 商品不能多,否则会分页
			List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
			if (goodsInCart != null)
			{
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				for (int i = 0; i < goodsInCart.size(); i++)
				{
					try
					{
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						// 停止5秒
						goodsInCart.get(i).click();
						TimeUnit.SECONDS.sleep(4);
					}
					catch (Exception e)
					{
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}
			}
			else
			{
				logger.info("购物车为空!");
			}

			try
			{
				logger.debug("--->继续购物,再次跳转");
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				By by = By.xpath("//a[@class='a-button-text' and  contains(text(),'Continue shopping')]");
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				WebElement go = driver.findElement(by);
				go.click();

				TimeUnit.SECONDS.sleep(5);
			}
			catch (Exception e){
				logger.debug("--->再次跳转失败", e);
			}
		}
		catch (Exception e)
		{
			logger.error("--->选择需要删除的商品出错", e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	private  void valueClick(FirefoxDriver driver, String valueStr) throws Exception
	{
		//判断是否有sku选择新样式
		TimeUnit.SECONDS.sleep(3);
		try{
			WebElement panel = driver.findElement(By.xpath("//div[@id='twister_bottom_sheet']"));
			logger.error("找到选sku新样式-底部弹出样式");
			List<WebElement> list = panel.findElements(By.xpath(".//ul/li/span/span/span/span/label"));
			if(list != null && list.size() > 0 && StringUtil.isNotEmpty(valueStr)){
				for(WebElement w : list){
					if(w != null && valueStr.equalsIgnoreCase(w.getText())){
						//driver.executeScript("var tar=arguments[0];tar.click();", w);
						w.click();
						break;
					}
				}
			}
			TimeUnit.SECONDS.sleep(4);
			try{
				WebElement w = driver.findElement(By.cssSelector(".a-sheet-close"));
				w.click();
				Utils.sleep(1000);
				((JavascriptExecutor)driver).executeScript("arguments[0].click();", w);
				Utils.sleep(1000);
			}catch(Exception e){
				logger.error("关闭底部弹出出错",e);
			}
			return;
		}catch(Exception e){
			logger.error("没有找到选sku新样式-底部弹出样式",e);
		}
		
		
		WebDriverWait wait1 = new WebDriverWait(driver, 30);
		boolean isSuccess = false;
		for (int i = 0; i < 4; i++)
		{
			try
			{
				isSuccess = true;
				String temp = String.format("\"label\":\"%s\"", valueStr);
				String format1 = String.format("//span[@class='a-declarative' and contains(@data-mobile-twister, '%s')]", temp);
				By value = By.xpath(format1);
				//				String format1 = String.format("//span[@class='a-declarative' and contains(text(), '%s')]", valueStr);
				//				By value = By.xpath(format1);
				WebElement valueElement = wait1.until(ExpectedConditions.visibilityOfElementLocated(value));// ExpectedConditions.elementToBeClickable(value)
				Utils.sleep(2000);
				driver.executeScript("var tar=arguments[0];tar.click();", valueElement);
				//driver.executeScript("var tar=arguments[0];var top=tar.offsetTop;window.scrollTo(0,top);", valueElement);
				//Utils.sleep(6000);
				//valueElement.click();
			}
			catch (Exception e)
			{
				isSuccess = false;
				
				List<WebElement> elements = driver.findElements(By.xpath("//h4"));
				if(elements != null && elements.size() > 0){
					for(WebElement w : elements){
						if(w != null && StringUtil.isNotEmpty(w.getText()) && valueStr.equalsIgnoreCase(w.getText().trim())){
							w.click();
							isSuccess = true;
							Utils.sleep(2000);
							break;
						}
					}
				}
				
				if(!isSuccess){
					String js = "var q=document.documentElement.scrollTop=10000";
					driver.executeScript(js);
					Utils.sleep(3000);
				}
			}
			if (isSuccess)
			{
				break;
			}
		}
		if (!isSuccess) { throw new Exception(); }
	}

	private static WebElement waitForMainPanel(WebDriver driver)
	{
		// 等待最大的选择面板可见
		By byPanel = By.id("ppd");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
		Utils.sleep(2000);
		return panel;
	}

	private static WebElement waitForNumPanel(WebDriver driver)
	{
		// 等待最大的选择面板可见
		By byPanel = By.xpath("//span[@class='a-button a-button-dropdown a-button-small']");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		WebElement panel = null;
		try
		{
			panel = wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
		}
		catch (Exception e)
		{
		}
		Utils.sleep(2000);
		return panel;
	}

	boolean newStyle = false;

	public AutoBuyStatus selectProduct(Map<String, String> param)
	{
		
		String sign = (String) param.get("sign");
		
		if(!StringUtil.isBlank(sign) && "0".equals(sign)){
			return selectBrushProduct(param);
		}else if (!StringUtil.isBlank(sign) && "1".equals(sign)){
			return externalProduct(param);
		}else{
			logger.debug("--->跳转到商品页面");
			String productUrl = (String) param.get("url");
			logger.debug("productUrl = " + productUrl);
			logger.debug("--->选择商品");
			
			//判断是extrabux的返利链接
			String extrabuxMark = "&extrabux=1";
			if(StringUtil.isNotEmpty(productUrl) && productUrl.endsWith(extrabuxMark)){
				logger.debug("--->找到extrabux的返利原链接" + productUrl);
				int index = productUrl.indexOf(extrabuxMark);
				if(index != -1){
					productUrl = productUrl.substring(0,index);
				}
				String rebateUrl = productUrl;
				productUrl = param.get("orginalUrl");
				
				String isFirst = param.get("isFirst");
				if("true".equals(isFirst)){//只需打开一次返利链接
					try{
						driver.navigate().to(rebateUrl);
						TimeUnit.SECONDS.sleep(5);
					}catch(Exception e){
						logger.debug("--->返利链接失败" + rebateUrl);
					}
				}
				
				logger.debug("--->找到extrabux的返利链接" + rebateUrl);
				logger.debug("--->找到extrabux对应的商品返利" + productUrl);
			}
	
			for (int i = 0; i < 3; i++)
			{
				try
				{
					driver.navigate().to(productUrl);
					driver.executeScript("(function(){window.scrollBy(0,250);})();");
	
					TimeUnit.SECONDS.sleep(5);
	
					try
					{
						driver.findElement(By.xpath("//b[@class='h1' and contains(text(),'Looking for')]"));
						logger.warn("--->该商品已经下架:" + productUrl);
						return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
					}
					catch (Exception e)
					{
	
					}
	
					waitForMainPanel(driver);
	
					break;
				}
				catch (Exception e)
				{
					if (i == 2)
					{
						logger.debug("--->打开商品页面失败 = " + productUrl);
						return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
					}
				}
			}
	
			String productNum = (String) param.get("num");
			Object sku = param.get("sku");
			try
			{
				if (sku != null)
				{
					List<String> skuList = Utils.getSku((String) sku);
					for (int i = 0; i < skuList.size(); i++)
					{
						if (i % 2 == 1)
						{
							boolean mark = false;
							logger.error(skuList.get(i - 1) + ":" + skuList.get(i));
							By byPanel = By.xpath("//div[@id='ppd']");
							WebDriverWait wait = new WebDriverWait(driver, 30);
							try {
								wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
							} catch (Exception e) {
								mark = true;
							}
							
							// 判断是否是连续选择的sku
							if (mark){
								for(int k = 0;k<3;k++){
									try{
										Utils.sleep(4500);
										List<WebElement> lists = driver.findElements(By.cssSelector("ul>li.a-align-center h4"));
										for(WebElement w:lists){
											for (int j = 0; j < skuList.size(); j++){
												if (j % 2 == 1){
													if(w.getText().equals(skuList.get(j))){
														logger.error("连续选择:"+skuList.get(j));
														w.click();
														break;
													}
												}
											}
												
										}
											
										Utils.sleep(2500);
									}catch(Exception e){
										break;
									}
								}
							}
							
							
							
							String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
							// 等待最大的选择面板可见
							WebElement panel = waitForMainPanel(driver);
	
							// 不需要选择的sku,比如one size
							boolean hasOneSize = false;
							List<WebElement> list = null;
							try
							{
								list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
							}
							catch (NoSuchElementException e)
							{
							}
							if (list != null && list.size() > 0)
							{
								for (WebElement e : list)
								{
									if (e != null)
									{
										String v = e.getText();
										if (!Utils.isEmpty(v))
										{
											if (v.indexOf(skuList.get(i)) != -1)
											{
												hasOneSize = true;
												break;
											}
											v = v.replaceAll(" ", "");
//											if (v.contains(keyStr))
//											{
//												hasOneSize = true;
//												break;
//											}
//											String[] ss = keyStr.split("(?<!^)(?=[A-Z])");
//											String[] vv = v.split("(?<!^)(?=[A-Z])");
//											if (vv != null && vv.length > 0 && !Utils.isEmpty(vv[0]))
//											{
//												for (String sss : ss)
//												{
//													if (vv[0].contains(sss))
//													{
//														hasOneSize = true;
//														break;
//													}
//												}
//												if (hasOneSize)
//												{
//													break;
//												}
//											}
										}
	
									}
								}
							}
							
							if (hasOneSize){
								logger.debug("find one key first~~");
								continue;
							}
							
							WebElement keyElement = null;
							
							List<WebElement> list0 = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
							if(list0 != null && list0.size() > 0){
								//判断onesize
								for(WebElement w : list0){
									try{
										w.findElement(By.cssSelector("span.a-declarative"));
									}catch(Exception e){
										String text = w.getText();
									    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
											hasOneSize = true;
											break;
										}
									}
								}
								if(!hasOneSize){
									//精确匹配
									for(WebElement w : list0){
										WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
										if(ww != null && StringUtil.isNotEmpty(ww.getText())){
											String[] ss = ww.getText().split("\n");
											String text = ss[0].replace(":", "");
											if(keyStr.equals(text) || keyStr.equalsIgnoreCase(text)){
												keyElement = w;
												break;
											}
										}
									}
									//模糊匹配
									if(keyElement == null){
										for(WebElement w : list0){
											WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
											if(ww != null && StringUtil.isNotEmpty(ww.getText())){
												String[] ss = ww.getText().split("\n");
												String text = ss[0].replace(":", "");
												if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
													keyElement = w;
													break;
												}
											}
										}
									}
								}
							}
							
				
							
							if (hasOneSize){
								logger.debug("find one key second");
								continue;
							}
							
							//再次寻找 分词匹配
							if (keyElement == null && !Utils.isEmpty(keyStr)){
								List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
								keyElement = findClosedWelement(keyStr, keyList);
							}
							
							if(keyElement == null){
								List<WebElement> l = driver.findElements(By.cssSelector(".a-button-text.a-text-left"));
								if(l != null && l.size() > 0){
									for(WebElement w : l){
										if(w != null){
											String text = w.getText();
											if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
												keyElement = w;
												break;
											}
										}
										
									}
								}
							}
	
							if (keyElement == null){
								logger.debug("找不到keyElement url= " + productUrl + "&& sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
							Utils.sleep(2000);
	
							if (newStyle)
							{
								try
								{
									keyElement.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
									logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
									return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
								}
								catch (Exception e)
								{
	
								}
								keyElement.click();
								logger.debug("--->新姿势选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
								newStyle = false;
							}
							else
							{
								logger.debug("--->自动选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
								keyElement.click();
								try
								{
									valueClick(driver, skuList.get(i));
								}
								catch (Exception e)
								{
									logger.debug("找不到valueElement url= " + productUrl + "&& sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
							
							Utils.sleep(2500);
						}
					}
					int findCount = 0;
					try {
						WebDriverWait wait = new WebDriverWait(driver, 30);
						try {
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dimension-label")));
						} catch (Exception e) {
							logger.debug("都是一个sku ");
						}
						
						List<WebElement> dimensions = driver.findElements(By.cssSelector(".dimension-label"));
						for(WebElement w:dimensions){
							
							String s = w.getText().trim().replaceAll(" ", "");
							for (int i = 0; i < skuList.size(); i++) {
								if (i % 2 == 1) {
									String attrValue = skuList.get(i).replaceAll(" ", "");
									if(attrValue.equalsIgnoreCase(s)){
										logger.debug("--->"+attrValue+"加1");
										findCount++;
										break;
									}
								}
							}
						}
						logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2+" && dimensions.size="+dimensions.size());
						if(findCount < dimensions.size() ){
							logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
							driver.navigate().refresh();
							selectsku(sku);
							
							int findCount1 = 0;
							try {
								wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dimension-label")));
								List<WebElement> dimensions1 = driver.findElements(By.cssSelector(".dimension-label"));
								for(WebElement w:dimensions1){
									
									String s = w.getText().trim().replaceAll(" ", "");
									for (int i = 0; i < skuList.size(); i++) {
										if (i % 2 == 1) {
											String attrValue = skuList.get(i).replaceAll(" ", "");
											if(attrValue.equalsIgnoreCase(s)){
												logger.debug("--->"+attrValue+"加1");
												findCount1++;
												break;
											}
										}
									}
								}
								logger.debug("--->sku findCount = "+findCount1+" && skuList.size/2 = "+skuList.size()/2+" && dimensions.size="+dimensions1.size());
								if(findCount1 < dimensions1.size() ){
									logger.debug("--->缺少匹配的sku findCount = "+findCount1+" && skuList.size()/2 = "+skuList.size()/2);
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							} catch (Exception e) {
								logger.debug("售罄 ");
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
					} catch (Exception e) {
						logger.debug("售罄 ");
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
				}
	
				// 等待最大的选择面板可见
				waitForMainPanel(driver);
				
				
				
				//Currently unavailable 判断这个情况
				try
				{
					WebElement stock = driver.findElement(By.id("availability"));
					stock.findElement(By.xpath(".//span[contains(text(),'Currently unavailable')]"));
					logger.warn("--->该商品已经下架:" + productUrl);
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
				catch (Exception e)
				{
				}
	
				// 是否add-on item凑单商品,不支持购买
				boolean isAddon = false;
				WebElement price = null;
				WebElement addOn = null;
				By ByAddOn = null;
				try{
					ByAddOn = By.xpath(".//i[contains(text(),'Add-on Item')]");
					price = driver.findElement(By.xpath("//table[@id='price']"));
					try
					{
						addOn = price.findElement(ByAddOn);
					}
					catch (NoSuchElementException e)
					{
					}
					if (addOn != null)
					{
						isAddon = true;
					}
				}catch(Exception e){
					logger.warn("--->判断addon出现异常",e);
				}
				
				
				
				if (!isAddon)
				{// 换种样式查找
					WebElement addOnItem = null;
					try
					{
						addOnItem = driver.findElement(By.xpath("//div[@id='addOnItem_feature_div']"));
					}
					catch (NoSuchElementException e)
					{
					}
					if (addOnItem != null)
					{
						try
						{
							addOn = addOnItem.findElement(ByAddOn);
						}
						catch (NoSuchElementException e)
						{
						}
					}
					if (addOn != null)
					{
						isAddon = true;
					}
				}
				if (isAddon)
				{
					logger.debug("这个商品是Add-on item,凑单商品,不支持购买 url = " + productUrl);
					return AutoBuyStatus.AUTO_SKU_ADD_ON;
				}
				// 订阅商品
				try
				{
					WebElement oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));// 换种样式查找
					if (oneTimeBuyBox != null)
					{
						logger.debug("这个商品是订阅商品00,选择oneTimePurchase");
						oneTimeBuyBox.click();
						Utils.sleep(2000);
						oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));
						oneTimeBuyBox.click();
						Utils.sleep(2000);
						waitForNumPanel(driver);
					}
				}
				catch (NoSuchElementException e)
				{
					try
					{
						WebElement selectPanel = driver.findElement(By.xpath("//div[@id='ap-options']"));
						WebElement oneTimePurchase = selectPanel.findElement(By.xpath(".//span[@class='modeTitle a-text-bold' and contains(text(),'One-time Purchase')]"));
						if (oneTimePurchase != null)
						{
							logger.debug("这个商品是订阅商品,选择oneTimePurchase");
							oneTimePurchase.click();
							Utils.sleep(2000);
							waitForNumPanel(driver);
						}
					}
					catch (NoSuchElementException ee)
					{
					}
				}
	
				// 如果有红包,获取红包
				try
				{
					WebElement coupon = driver.findElementById("oneTimeBuyVpcButton");
					if (coupon.isDisplayed() && coupon.isEnabled())
					{
						logger.debug("--->[1]这个商品有红包优惠,点击领取了");
						TimeUnit.SECONDS.sleep(1);
						coupon.click();
					}
					else
					{
						logger.debug("--->[1]这个商品红包已经领过了");
					}
				}
				catch (Exception e)
				{
	
				}
				try{
					WebElement coupon = driver.findElement(By.xpath("//div[@id='oneTimeBuyVpcButton']/div/label/input"));
					if(coupon != null && !coupon.isSelected()){
						coupon.click();
						TimeUnit.SECONDS.sleep(1);
					}
				}catch(Exception e){
					logger.debug("--->领红包出错");
				}
				
				
	
				// 获取单价
				try
				{
					WebElement singlePrice = driver.findElement(By.xpath("//span[@id='priceblock_ourprice']"));
					if (singlePrice != null)
					{
						String singlePriceStr = singlePrice.getText();
						if (!Utils.isEmpty(singlePriceStr))
						{
							String productEntityId = param.get("productEntityId");
							logger.error("productEntityId = " + productEntityId);
							int index = singlePriceStr.indexOf("(");
							if(index != -1){
								singlePriceStr = singlePriceStr.substring(0,index).trim();
							}
							// $24.99 21.91 ($0.18 / Count)
							if (singlePriceStr.startsWith("$") && singlePriceStr.length() > 1)
							{
								singlePriceStr = singlePriceStr.substring(1).replace(" ", ".");
								if(singlePriceStr.startsWith(".")){
									singlePriceStr = singlePriceStr.replaceFirst(".", "");								
								}
								logger.debug("singlePriceStr:"+singlePriceStr);
	//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr.substring(1));
								if(StringUtil.isNotEmpty(productEntityId)){
									priceMap.put(productEntityId, singlePriceStr);
								}
							}
							else
							{
	//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr);
								if(StringUtil.isNotEmpty(productEntityId)){
									priceMap.put(productEntityId, singlePriceStr);
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					try
					{
						WebElement pricePanel = driver.findElement(By.cssSelector("table#price td#priceblock_dealprice > span"));
						String priceStr = pricePanel.getText();
						if (!Utils.isEmpty(priceStr))
						{
							logger.error("--->单价:" + priceStr.replace("$", ""));
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
						}
					}
					catch (Exception ep)
					{
						try
						{
							WebElement pricePanel = driver.findElement(By.xpath("//span[@id='product-price']"));
							String priceStr = pricePanel.getText();
							if (!Utils.isEmpty(priceStr))
							{
								logger.error("--->单价:" + priceStr.replace("$", ""));
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
							}
							else
							{
								logger.error("--->单价获取失败");
							}
						}
						catch (Exception ex)
						{
							logger.error("--->获取单价失败");
						}
					}
				}
	
				try
				{
					WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
					wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
					WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
					String stockNum = availability.getText().toLowerCase();
					logger.info("--->库存状态:" + stockNum);
					if (stockNum.contains("out of stock") || stockNum.contains("in stock on"))
					{
						logger.warn("--->该商品已经下架:" + productUrl);
						return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
					}
				}
				catch (Exception e)
				{
				}
	
				// 选择数量
				try
				{
					if (!productNum.equals("1"))
					{
						Select select = new Select(driver.findElement(By.id("mobileQuantityDropDown")));
						Utils.sleep(2000);
						driver.executeScript("window.scrollBy(0,150);");
						select.selectByValue(productNum);
						Utils.sleep(2000);
						WebElement numBtn = driver.findElement(By.xpath("//span[@class='a-button a-button-dropdown a-button-small']"));
						String txt = numBtn.getText();
						logger.info("--->选择数量结果:" + txt);
						if (!txt.contains(productNum))
						{
							return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
						}
					}
				}
				catch (Exception e)
				{
					logger.error("选择数量失败 pruductNum = " + productNum);
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
	
				// 第三方商品,不支持购买
				try
				{
					WebElement third = driver.findElement(By.xpath("//div[@id='merchant-info']"));
					if (third != null)
					{
						String text = third.getText();
						if (!Utils.isEmpty(text))
						{
							if (!(text.indexOf("sold by Amazon") != -1 || text.indexOf("Fulfilled by Amazon") != -1))
							{
								logger.debug("第三方商品不支持购买 +productUrl = " + productUrl);
								//看下有没有其它不是第三方
								boolean b = clickOther();
								if(!b){
									return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
								}
							}
						}
	
					}
				}
				catch (NoSuchElementException e)
				{
	
				}
	
				// 加购物车按钮
				try{
					driver.findElement(By.id("buybox.addToCart")).click();
				}catch(Exception e){
					logger.debug("寻找购物车按钮异常11112121");
				}
				Utils.sleep(1000);
				try{
					driver.findElement(By.cssSelector("input#add-to-cart-button")).click();
				}catch(Exception e){
					logger.debug("寻找购物车按钮异常");
				}
				
				Utils.sleep(3000);
				
				
				try
				{
					driver.findElement(By.id("no_thanks_button")).click();
				}
				catch (Exception e)
				{
				}
				
				
				
				
				
				Utils.sleep(3000);
				
				try
				{
					driver.findElement(By.xpath("//a[@id='edit-cart-button-announce']")).click();;
				}
				catch (Exception e)
				{
				}
				Utils.sleep(1000);
				
//				try
//				{
//					driver.findElement(By.id("a-autoid-0-announce")).click();;
//				}
//				catch (Exception e)
//				{
//					logger.debug("没有 a-autoid-0-announce");
//				}
				Utils.sleep(1000);
				
				
				
				try {
					TimeUnit.SECONDS.sleep(3);
					WebElement ad = driver.findElement(By.cssSelector("button[title='Add to Shopping Cart']"));
					ad.click();
					Utils.sleep(1000);
					WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'Added to cart')]"));
					if(tt!=null){
						driver.navigate().to("https://www.amazon.com/gp/aw/c/ref=navm_hdr_cart");
					}
				} catch (Exception e) {
					logger.error("safss");
				}
				
				try{
					Utils.sleep(3000);
					List<WebElement> checkOuts = driver.findElements(By.cssSelector("a.a-link-normal"));
					for(WebElement w:checkOuts){
						if(w.isDisplayed() && w.getText().equals("check out")){
							w.click();
							break;
						}
					}
					//driver.findElement(By.xpath("//a[contains(text(),'check out')]")).click();;
					Utils.sleep(1000);
				}catch(Exception e){
					logger.debug("没有 check out");
				}
				
				try {
					logger.debug("new check out");
					WebElement tt = driver.findElement(By.cssSelector("span.a-color-base a.a-link-normal"));
					String url = tt.getAttribute("herf");
					driver.navigate().to(url);
				} catch (Exception e) {
					logger.error("safss55",e);
				}
				
				try {
					WebElement gotocart = driver.findElement(By.id("aislesCartNav"));
					gotocart.click();
				} catch (Exception e) {
					logger.error("2222",e);
				}
				
				try {
					TimeUnit.SECONDS.sleep(1);
					WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'Continue')]"));
					tt.click();
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					logger.error("Continue 点击出错");
				}
				
				try {
					TimeUnit.SECONDS.sleep(3);
					WebElement ad = driver.findElement(By.cssSelector("#seeBuyingChoices span"));
					ad.click();
					logger.error("seeBuyingChoices 点击");
					WebDriverWait wait = new WebDriverWait(driver, 30);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".olpMobileOffer")));
					List<WebElement> skuList = driver.findElements(By.cssSelector(".olpMobileOffer"));
					for(WebElement w:skuList){
						WebElement olpName = w.findElement(By.cssSelector(".olpSellerName"));
						if(olpName.getText().contains("Amazon.com")){
							List<WebElement> addCard = w.findElements(By.cssSelector("input[name='submit.addToCart']"));
							for(WebElement card:addCard){
								if(card.isDisplayed()){
									card.click();
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					logger.error("没有seeBuyingChoices");
				}
				
				//等待购物车加载完成
				try{
					WebDriverWait wait = new WebDriverWait(driver, 45);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
					try{
						WebElement numText = driver.findElement(By.cssSelector(".a-dropdown-prompt"));
						logger.error("购物车数量为"+numText.getText().trim());
						if(!productNum.equals(numText.getText().trim())){
							logger.error("选择数量失败 pruductNum = " + productNum);
							return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
						}
					}catch(Exception e){
						logger.error("等待购物车数量出错");
					}
				}catch(Exception e){
					logger.error("等待购物车加载完成出错,e");
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
				
				//判断是否有alert
				try{
					WebElement w = driver.findElement(By.cssSelector("div.a-alert-content"));
					if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("Important messages for items in your Cart")){
						logger.error("购物车页面弹出警告标记");
						return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
					}
				}catch(Exception e){}
				try{
					WebElement w = driver.findElement(By.cssSelector(".a-color-alternate-background"));
					if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("Add-on")){
						logger.debug("这个商品是Add-on item,凑单商品,不支持购买 url = " + productUrl);
						return AutoBuyStatus.AUTO_SKU_ADD_ON;
					}
				}catch(Exception e){}
				
				logger.debug("选择sku成功");
				return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
			}
			catch (Exception e)
			{
				logger.error("选择sku出现异常:", e);
				return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
			}
		}
	}
	
	private boolean clickOther() {
		boolean b = false;
		try {
			WebElement olp = driver.findElement(By.cssSelector("#olp a"));
			//driver.executeScript("var tar=arguments[0];tar.click();", olp);
			olp.click();
			logger.error("新品点击");
			Utils.sleep(500);
			WebDriverWait wait = new WebDriverWait(driver, 30);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".olpMobileOffer")));
			List<WebElement> skuList = driver.findElements(By.cssSelector(".olpMobileOffer"));
			for(WebElement w:skuList){
				WebElement olpName = w.findElement(By.cssSelector(".olpSellerName"));
				if(olpName.getText().contains("Amazon.com")){
					List<WebElement> addCard = w.findElements(By.cssSelector("input[name='submit.addToCart']"));
					for(WebElement card:addCard){
						if(card.isDisplayed()){
							card.click();
							b=true;
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("选择第三方异常", e);
		}
		
		return b;
	}
	
	public void selectsku(Object sku){
		if (sku != null){
			driver.executeScript("(function(){window.scrollBy(0,250);})();");
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
			}
			
			List<String> skuList = Utils.getSku((String) sku);
			for (int i = 0; i < skuList.size(); i++)
			{
				if (i % 2 == 1)
				{
					boolean mark = false;
					logger.error(skuList.get(i - 1) + ":" + skuList.get(i));
					By byPanel = By.xpath("//div[@id='ppd']");
					WebDriverWait wait = new WebDriverWait(driver, 30);
					try {
						wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
					} catch (Exception e) {
						mark = true;
					}
					
					// 判断是否是连续选择的sku
					if (mark){
						for(int k = 0;k<3;k++){
							try{
								Utils.sleep(4500);
								List<WebElement> lists = driver.findElements(By.cssSelector("ul>li.a-align-center h4"));
								for(WebElement w:lists){
									for (int j = 0; j < skuList.size(); j++){
										if (j % 2 == 1){
											if(w.getText().equals(skuList.get(j))){
												logger.error("连续选择:"+skuList.get(j));
												w.click();
												break;
											}
										}
									}
										
								}
									
								Utils.sleep(2500);
							}catch(Exception e){
								break;
							}
						}
					}
					
					
					
					String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
					// 等待最大的选择面板可见
					WebElement panel = waitForMainPanel(driver);
	
					// 不需要选择的sku,比如one size
					boolean hasOneSize = false;
					List<WebElement> list = null;
					try
					{
						list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
					}
					catch (NoSuchElementException e)
					{
					}
					if (list != null && list.size() > 0)
					{
						for (WebElement e : list)
						{
							if (e != null)
							{
								String v = e.getText();
								if (!Utils.isEmpty(v))
								{
									if (v.indexOf(skuList.get(i)) != -1)
									{
										hasOneSize = true;
										break;
									}
									v = v.replaceAll(" ", "");
	//								if (v.contains(keyStr))
	//								{
	//									hasOneSize = true;
	//									break;
	//								}
	//								String[] ss = keyStr.split("(?<!^)(?=[A-Z])");
	//								String[] vv = v.split("(?<!^)(?=[A-Z])");
	//								if (vv != null && vv.length > 0 && !Utils.isEmpty(vv[0]))
	//								{
	//									for (String sss : ss)
	//									{
	//										if (vv[0].contains(sss))
	//										{
	//											hasOneSize = true;
	//											break;
	//										}
	//									}
	//									if (hasOneSize)
	//									{
	//										break;
	//									}
	//								}
								}
	
							}
						}
					}
					
					if (hasOneSize){
						logger.debug("find one key first~~");
						continue;
					}
					
					WebElement keyElement = null;
					
					List<WebElement> list0 = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
					if(list0 != null && list0.size() > 0){
						//判断onesize
						for(WebElement w : list0){
							try{
								w.findElement(By.cssSelector("span.a-declarative"));
							}catch(Exception e){
								String text = w.getText();
							    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
									hasOneSize = true;
									break;
								}
							}
						}
						if(!hasOneSize){
							//精确匹配
							for(WebElement w : list0){
								WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
								if(ww != null && StringUtil.isNotEmpty(ww.getText())){
									String[] ss = ww.getText().split("\n");
									String text = ss[0].replace(":", "");
									if(keyStr.equals(text) || keyStr.equalsIgnoreCase(text)){
										keyElement = w;
										break;
									}
								}
							}
							//模糊匹配
							if(keyElement == null){
								for(WebElement w : list0){
									WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
									if(ww != null && StringUtil.isNotEmpty(ww.getText())){
										String[] ss = ww.getText().split("\n");
										String text = ss[0].replace(":", "");
										if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
											keyElement = w;
											break;
										}
									}
								}
							}
						}
					}
					
		
					
					if (hasOneSize){
						logger.debug("find one key second");
						continue;
					}
					
					//再次寻找 分词匹配
					if (keyElement == null && !Utils.isEmpty(keyStr)){
						List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
						keyElement = findClosedWelement(keyStr, keyList);
					}
					
					if(keyElement == null){
						List<WebElement> l = driver.findElements(By.xpath("//button[@class='a-button-text a-text-left']"));
						if(l != null && l.size() > 0){
							for(WebElement w : l){
								if(w != null){
									String text = w.getText();
									if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
										keyElement = w;
										break;
									}
								}
								
							}
						}
					}
	
					if (keyElement == null){
						logger.debug("找不到keyElement url= && sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
					}
					Utils.sleep(2000);
	
					if (newStyle)
					{
						try
						{
							keyElement.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
							logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
						}
						catch (Exception e)
						{
	
						}
						keyElement.click();
						logger.debug("--->新姿势选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
						newStyle = false;
					}
					else
					{
						logger.debug("--->自动选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
						keyElement.click();
						try
						{
							valueClick(driver, skuList.get(i));
						}
						catch (Exception e)
						{
							logger.debug("找不到valueElement url= && sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
						}
					}
					
					Utils.sleep(2500);
				}
			}
			
		}
	}
	
	public AutoBuyStatus externalProduct(Map<String, String> param)
	{
		
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("productUrl = " + productUrl);
		logger.debug("--->选择商品");
		String addon = (String) param.get("addon");
		//判断是extrabux的返利链接
		String extrabuxMark = "&extrabux=1";
		if(StringUtil.isNotEmpty(productUrl) && productUrl.endsWith(extrabuxMark)){
			logger.debug("--->找到extrabux的返利原链接" + productUrl);
			int index = productUrl.indexOf(extrabuxMark);
			if(index != -1){
				productUrl = productUrl.substring(0,index);
			}
			String rebateUrl = productUrl;
			productUrl = param.get("orginalUrl");
			
			String isFirst = param.get("isFirst");
			if("true".equals(isFirst)){//只需打开一次返利链接
				try{
					driver.navigate().to(rebateUrl);
					TimeUnit.SECONDS.sleep(5);
				}catch(Exception e){
					logger.debug("--->返利链接失败" + rebateUrl);
				}
			}
			
			logger.debug("--->找到extrabux的返利链接" + rebateUrl);
			logger.debug("--->找到extrabux对应的商品返利" + productUrl);
		}

		for (int i = 0; i < 3; i++)
		{
			try
			{
				driver.navigate().to(productUrl);
				driver.executeScript("(function(){window.scrollBy(0,250);})();");

				TimeUnit.SECONDS.sleep(5);

				try
				{
					driver.findElement(By.xpath("//b[@class='h1' and contains(text(),'Looking for')]"));
					logger.warn("--->该商品已经下架:" + productUrl);
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
				catch (Exception e)
				{

				}

				waitForMainPanel(driver);

				break;
			}
			catch (Exception e)
			{
				if (i == 2)
				{
					logger.debug("--->打开商品页面失败 = " + productUrl);
					return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
				}
			}
		}

		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		try
		{
			if (sku != null)
			{
				List<String> skuList = Utils.getSku((String) sku);
				for (int i = 0; i < skuList.size(); i++)
				{
					if (i % 2 == 1)
					{
						boolean mark = false;
						logger.error(skuList.get(i - 1) + ":" + skuList.get(i));
						By byPanel = By.xpath("//div[@id='ppd']");
						WebDriverWait wait = new WebDriverWait(driver, 30);
						try {
							wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
						} catch (Exception e) {
							mark = true;
						}
						
						// 判断是否是连续选择的sku
						if (mark){
							for(int k = 0;k<3;k++){
								try{
									Utils.sleep(4500);
									List<WebElement> lists = driver.findElements(By.cssSelector("ul>li.a-align-center h4"));
									for(WebElement w:lists){
										for (int j = 0; j < skuList.size(); j++){
											if (j % 2 == 1){
												if(w.getText().equals(skuList.get(j))){
													logger.error("连续选择:"+skuList.get(j));
													w.click();
													break;
												}
											}
										}
											
									}
										
									Utils.sleep(2500);
								}catch(Exception e){
									break;
								}
							}
						}
						
						
						
						String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
						// 等待最大的选择面板可见
						WebElement panel = waitForMainPanel(driver);

						// 不需要选择的sku,比如one size
						boolean hasOneSize = false;
						List<WebElement> list = null;
						try
						{
							list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
						}
						catch (NoSuchElementException e)
						{
						}
						if (list != null && list.size() > 0)
						{
							for (WebElement e : list)
							{
								if (e != null)
								{
									String v = e.getText();
									if (!Utils.isEmpty(v))
									{
										if (v.indexOf(skuList.get(i)) != -1)
										{
											hasOneSize = true;
											break;
										}
										v = v.replaceAll(" ", "");
//										if (v.contains(keyStr))
//										{
//											hasOneSize = true;
//											break;
//										}
//										String[] ss = keyStr.split("(?<!^)(?=[A-Z])");
//										String[] vv = v.split("(?<!^)(?=[A-Z])");
//										if (vv != null && vv.length > 0 && !Utils.isEmpty(vv[0]))
//										{
//											for (String sss : ss)
//											{
//												if (vv[0].contains(sss))
//												{
//													hasOneSize = true;
//													break;
//												}
//											}
//											if (hasOneSize)
//											{
//												break;
//											}
//										}
									}

								}
							}
						}
						
						if (hasOneSize){
							logger.debug("find one key first~~");
							continue;
						}
						
						WebElement keyElement = null;
						
						List<WebElement> list0 = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
						if(list0 != null && list0.size() > 0){
							//判断onesize
							for(WebElement w : list0){
								try{
									w.findElement(By.cssSelector("span.a-declarative"));
								}catch(Exception e){
									String text = w.getText();
								    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
										hasOneSize = true;
										break;
									}
								}
							}
							if(!hasOneSize){
								//精确匹配
								for(WebElement w : list0){
									WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
									if(ww != null && StringUtil.isNotEmpty(ww.getText())){
										String[] ss = ww.getText().split("\n");
										String text = ss[0].replace(":", "");
										if(keyStr.equals(text) || keyStr.equalsIgnoreCase(text)){
											keyElement = w;
											break;
										}
									}
								}
								//模糊匹配
								if(keyElement == null){
									for(WebElement w : list0){
										WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
										if(ww != null && StringUtil.isNotEmpty(ww.getText())){
											String[] ss = ww.getText().split("\n");
											String text = ss[0].replace(":", "");
											if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
												keyElement = w;
												break;
											}
										}
									}
								}
							}
						}
						
			
						
						if (hasOneSize){
							logger.debug("find one key second");
							continue;
						}
						
						//再次寻找 分词匹配
						if (keyElement == null && !Utils.isEmpty(keyStr)){
							List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
							keyElement = findClosedWelement(keyStr, keyList);
						}
						
						if(keyElement == null){
							List<WebElement> l = driver.findElements(By.cssSelector(".a-button-text.a-text-left"));
							if(l != null && l.size() > 0){
								for(WebElement w : l){
									if(w != null){
										String text = w.getText();
										if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
											keyElement = w;
											break;
										}
									}
									
								}
							}
						}

						Utils.sleep(2000);
						try {
							if (newStyle)
							{
								try
								{
									keyElement.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
									logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
									return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
								}
								catch (Exception e)
								{

								}
								keyElement.click();
								logger.debug("--->新姿势选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
								newStyle = false;
							}
							else
							{
								logger.debug("--->自动选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
								keyElement.click();
								try
								{
									valueClick(driver, skuList.get(i));
								}
								catch (Exception e)
								{
									logger.debug("找不到valueElement url= " + productUrl + "&& sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						} catch (Exception e) {
							logger.debug("找不到valueElement");
						}
						
						
						Utils.sleep(2500);
					}
				}
				
				int findCount = 0;
				try {
					WebDriverWait wait = new WebDriverWait(driver, 30);
					try {
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dimension-label")));
					} catch (Exception e) {
						logger.debug("都是一个sku ");
					}
					
					List<WebElement> dimensions = driver.findElements(By.cssSelector(".dimension-label"));
					for(WebElement w:dimensions){
						
						String s = w.getText().trim().replaceAll(" ", "");
						s = s.replace("\"", "&quot;");
						for (int i = 0; i < skuList.size(); i++) {
							if (i % 2 == 1) {
								String attrValue = skuList.get(i).replaceAll(" ", "");
								if(attrValue.equalsIgnoreCase(s)){
									logger.debug("--->"+attrValue+"加1");
									findCount++;
									break;
								}
							}
						}
					}
					logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2+" && dimensions.size="+dimensions.size());
					if(findCount < dimensions.size() ){
						logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
				} catch (Exception e) {
					logger.debug("售罄 ");
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}

			// 等待最大的选择面板可见
			waitForMainPanel(driver);
			
			//Currently unavailable 判断这个情况
			try
			{
				WebElement stock = driver.findElement(By.id("availability"));
				stock.findElement(By.xpath(".//span[contains(text(),'Currently unavailable')]"));
				logger.warn("--->该商品已经下架:" + productUrl);
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
			catch (Exception e)
			{
			}

			// 是否add-on item凑单商品,不支持购买
			boolean isAddon = false;
			WebElement price = null;
			WebElement addOn = null;
			By ByAddOn = null;
			try{
				ByAddOn = By.xpath(".//i[contains(text(),'Add-on Item')]");
				price = driver.findElement(By.xpath("//table[@id='price']"));
				try
				{
					addOn = price.findElement(ByAddOn);
				}
				catch (NoSuchElementException e)
				{
				}
				if (addOn != null)
				{
					isAddon = true;
				}
			}catch(Exception e){
				logger.warn("--->判断addon出现异常",e);
			}
			
			
			
			if (!isAddon)
			{// 换种样式查找
				WebElement addOnItem = null;
				try
				{
					addOnItem = driver.findElement(By.xpath("//div[@id='addOnItem_feature_div']"));
				}
				catch (NoSuchElementException e)
				{
				}
				if (addOnItem != null)
				{
					try
					{
						addOn = addOnItem.findElement(ByAddOn);
					}
					catch (NoSuchElementException e)
					{
					}
				}
				if (addOn != null)
				{
					isAddon = true;
				}
			}
			if (isAddon && StringUtil.isBlank(addon))
			{
				logger.debug("这个商品是Add-on item,凑单商品,不支持购买 url = " + productUrl);
				return AutoBuyStatus.AUTO_SKU_ADD_ON;
			}
			// 订阅商品
			try
			{
				WebElement oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));// 换种样式查找
				if (oneTimeBuyBox != null)
				{
					logger.debug("这个商品是订阅商品00,选择oneTimePurchase");
					oneTimeBuyBox.click();
					Utils.sleep(2000);
					oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));
					oneTimeBuyBox.click();
					Utils.sleep(2000);
					waitForNumPanel(driver);
				}
			}
			catch (NoSuchElementException e)
			{
				try
				{
					WebElement selectPanel = driver.findElement(By.xpath("//div[@id='ap-options']"));
					WebElement oneTimePurchase = selectPanel.findElement(By.xpath(".//span[@class='modeTitle a-text-bold' and contains(text(),'One-time Purchase')]"));
					if (oneTimePurchase != null)
					{
						logger.debug("这个商品是订阅商品,选择oneTimePurchase");
						oneTimePurchase.click();
						Utils.sleep(2000);
						waitForNumPanel(driver);
					}
				}
				catch (NoSuchElementException ee)
				{
				}
			}

			// 如果有红包,获取红包
			try
			{
				WebElement coupon = driver.findElementById("oneTimeBuyVpcButton");
				if (coupon.isDisplayed() && coupon.isEnabled())
				{
					logger.debug("--->[1]这个商品有红包优惠,点击领取了");
					TimeUnit.SECONDS.sleep(1);
					coupon.click();
				}
				else
				{
					logger.debug("--->[1]这个商品红包已经领过了");
				}
			}
			catch (Exception e)
			{

			}
			try{
				WebElement coupon = driver.findElement(By.xpath("//div[@id='oneTimeBuyVpcButton']/div/label/input"));
				if(coupon != null && !coupon.isSelected()){
					coupon.click();
					TimeUnit.SECONDS.sleep(1);
				}
			}catch(Exception e){
				logger.debug("--->领红包出错");
			}

			// 获取单价
			try
			{
				WebElement singlePrice = driver.findElement(By.xpath("//span[@id='priceblock_ourprice']"));
				if (singlePrice != null)
				{
					String singlePriceStr = singlePrice.getText();
					if (!Utils.isEmpty(singlePriceStr))
					{
						String productEntityId = param.get("productEntityId");
						logger.error("productEntityId = " + productEntityId);
						int index = singlePriceStr.indexOf("(");
						if(index != -1){
							singlePriceStr = singlePriceStr.substring(0,index).trim();
						}
						// $24.99 21.91 ($0.18 / Count)
						if (singlePriceStr.startsWith("$") && singlePriceStr.length() > 1)
						{
							singlePriceStr = singlePriceStr.substring(1).replace(" ", ".");
							if(singlePriceStr.startsWith(".")){
								singlePriceStr = singlePriceStr.replaceFirst(".", "");								
							}
							logger.debug("singlePriceStr:"+singlePriceStr);
//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr.substring(1));
							if(StringUtil.isNotEmpty(productEntityId)){
								priceMap.put(productEntityId, singlePriceStr);
							}
						}
						else
						{
//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr);
							if(StringUtil.isNotEmpty(productEntityId)){
								priceMap.put(productEntityId, singlePriceStr);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				try
				{
					WebElement pricePanel = driver.findElement(By.cssSelector("table#price td#priceblock_dealprice > span"));
					String priceStr = pricePanel.getText();
					if (!Utils.isEmpty(priceStr))
					{
						logger.error("--->单价:" + priceStr.replace("$", ""));
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
					}
				}
				catch (Exception ep)
				{
					try
					{
						WebElement pricePanel = driver.findElement(By.xpath("//span[@id='product-price']"));
						String priceStr = pricePanel.getText();
						if (!Utils.isEmpty(priceStr))
						{
							logger.error("--->单价:" + priceStr.replace("$", ""));
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
						}
						else
						{
							logger.error("--->单价获取失败");
						}
					}
					catch (Exception ex)
					{
						logger.error("--->获取单价失败");
					}
				}
			}

			try
			{
				WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
				wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
				WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
				String stockNum = availability.getText().toLowerCase();
				logger.info("--->库存状态:" + stockNum);
				if (stockNum.contains("out of stock") || stockNum.contains("in stock on"))
				{
					logger.warn("--->该商品已经下架:" + productUrl);
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			}
			catch (Exception e)
			{
			}

			// 选择数量
			try
			{
				if (!productNum.equals("1"))
				{
					Select select = new Select(driver.findElement(By.id("mobileQuantityDropDown")));
					Utils.sleep(2000);
					driver.executeScript("window.scrollBy(0,150);");
					select.selectByValue(productNum);
					Utils.sleep(2000);
					WebElement numBtn = driver.findElement(By.xpath("//span[@class='a-button a-button-dropdown a-button-small']"));
					String txt = numBtn.getText();
					logger.info("--->选择数量结果:" + txt);
					if (!txt.contains(productNum))
					{
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("选择数量失败 pruductNum = " + productNum);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}

			// 第三方商品,不支持购买
			try
			{
				WebElement third = driver.findElement(By.xpath("//div[@id='merchant-info']"));
				if (third != null)
				{
					String text = third.getText();
					if (!Utils.isEmpty(text))
					{
						if (!(text.indexOf("sold by Amazon") != -1 || text.indexOf("Fulfilled by Amazon") != -1))
						{
							logger.debug("第三方商品不支持购买 +productUrl = " + productUrl);
							boolean b = clickOther();
							if(!b){
								return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
							}
						}
					}

				}
			}
			catch (NoSuchElementException e)
			{

			}

			// 加购物车按钮
			try{
				driver.findElement(By.id("buybox.addToCart")).click();
			}catch(Exception e){
				logger.debug("寻找购物车按钮异常11112121");
			}
			Utils.sleep(1000);
			try{
				driver.findElement(By.cssSelector("input#add-to-cart-button")).click();
			}catch(Exception e){
				logger.debug("寻找购物车按钮异常");
			}
			
			Utils.sleep(3000);
			
			
			try
			{
				driver.findElement(By.id("no_thanks_button")).click();
			}
			catch (Exception e)
			{
			}
			
			
			
			
			
			Utils.sleep(3000);
			
			try
			{
				driver.findElement(By.xpath("//a[@id='edit-cart-button-announce']")).click();;
			}
			catch (Exception e)
			{
			}
			Utils.sleep(1000);
			
//			try
//			{
//				driver.findElement(By.id("a-autoid-0-announce")).click();;
//			}
//			catch (Exception e)
//			{
//				logger.debug("没有 a-autoid-0-announce");
//			}
			Utils.sleep(1000);
			
			
			
			try {
				TimeUnit.SECONDS.sleep(3);
				WebElement ad = driver.findElement(By.cssSelector("button[title='Add to Shopping Cart']"));
				ad.click();
				Utils.sleep(1000);
				WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'Added to cart')]"));
				if(tt!=null){
					driver.navigate().to("https://www.amazon.com/gp/aw/c/ref=navm_hdr_cart");
				}
			} catch (Exception e) {
				logger.error("safss");
			}
			
			try{
				Utils.sleep(3000);
				List<WebElement> checkOuts = driver.findElements(By.cssSelector("a.a-link-normal"));
				for(WebElement w:checkOuts){
					if(w.isDisplayed() && w.getText().equals("check out")){
						w.click();
						break;
					}
				}
				//driver.findElement(By.xpath("//a[contains(text(),'check out')]")).click();;
				Utils.sleep(1000);
			}catch(Exception e){
				logger.debug("没有 check out");
			}
			
			try {
				logger.debug("new check out");
				WebElement tt = driver.findElement(By.cssSelector("span.a-color-base a.a-link-normal"));
				String url = tt.getAttribute("herf");
				driver.navigate().to(url);
			} catch (Exception e) {
				logger.error("safss55",e);
			}
			
			try {
				WebElement gotocart = driver.findElement(By.id("aislesCartNav"));
				gotocart.click();
			} catch (Exception e) {
				logger.error("2222",e);
			}
			try {
				TimeUnit.SECONDS.sleep(1);
				WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'Continue')]"));
				tt.click();
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				logger.error("Continue 点击出错");
			}
			
			try {
				TimeUnit.SECONDS.sleep(3);
				WebElement ad = driver.findElement(By.cssSelector("#seeBuyingChoices span"));
				ad.click();
				logger.error("seeBuyingChoices 点击");
				WebDriverWait wait = new WebDriverWait(driver, 30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".olpMobileOffer")));
				List<WebElement> skuList = driver.findElements(By.cssSelector(".olpMobileOffer"));
				for(WebElement w:skuList){
					WebElement olpName = w.findElement(By.cssSelector(".olpSellerName"));
					if(olpName.getText().contains("Amazon.com")){
						List<WebElement> addCard = w.findElements(By.cssSelector("input[name='submit.addToCart']"));
						for(WebElement card:addCard){
							if(card.isDisplayed()){
								card.click();
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("没有seeBuyingChoices");
			}
			
			//等待购物车加载完成
			try{
				WebDriverWait wait = new WebDriverWait(driver, 45);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
				try{
					WebElement numText = driver.findElement(By.cssSelector(".a-dropdown-prompt"));
					logger.error("购物车数量为"+numText.getText().trim());
					if(!productNum.equals(numText.getText().trim())){
						logger.error("选择数量失败 pruductNum = " + productNum);
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}catch(Exception e){
					logger.error("等待购物车数量出错");
				}
			}catch(Exception e){
				logger.error("等待购物车加载完成出错,e");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
			
			//判断是否有alert
			try{
				WebElement w = driver.findElement(By.cssSelector("div.a-alert-content"));
				if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("Important messages for items in your Cart")){
					logger.error("购物车页面弹出警告标记");
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
			}catch(Exception e){}
			if(StringUtil.isBlank(addon)){
				try{
					WebElement w = driver.findElement(By.cssSelector(".a-color-alternate-background"));
					if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("Add-on")){
						logger.debug("这个商品是Add-on item,凑单商品,不支持购买 url = " + productUrl);
						return AutoBuyStatus.AUTO_SKU_ADD_ON;
					}
				}catch(Exception e){}
			}
			logger.debug("选择sku成功");
			return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
		}
		catch (Exception e)
		{
			logger.error("选择sku出现异常:", e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
	}

	private static WebElement findClosedWelement(String keyStr, List<WebElement> keyList)
	{
		if (!Utils.isEmpty(keyStr))
		{
			String[] ss = keyStr.split("(?<!^)(?=[A-Z])");
			if (ss != null && ss.length > 0)
			{
				if (keyList != null && keyList.size() > 0)
				{
					for (WebElement e : keyList)
					{
						if (e != null)
						{
							String text = e.getText();
							if (StringUtil.isNotEmpty(text))
							{
								String[] tempKeys = text.split("\n");
								if (tempKeys != null && tempKeys.length > 0)
								{
									if (StringUtil.isNotEmpty(tempKeys[0]))
									{
										String[] keys = tempKeys[0].split("(?<!^)(?=[A-Z])");
										if (keys != null && keys.length > 0)
										{
											for (String m : keys)
											{
												m = m.trim();
												for (String n : ss)
												{
													if (m.contains(n)) { return e; }
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	AutoBuyStatus addAddr(UserTradeAddress userTradeAddress,String userName,int size){
		
		try {
			int i = size%4;
			String name = userName.split("@")[0];
			if(i==0){
				name = name+"A  "+userTradeAddress.getName();
			}else if(i==1){
				name = name+"B  "+userTradeAddress.getName();
			}else if(i==2){
				name = name+"C  "+userTradeAddress.getName();
			}else if(i==3){
				name = name+"D  "+userTradeAddress.getName();
			}else if(i==4){
				name = name+"E  "+userTradeAddress.getName();
			}else{
				name = name+"F  "+userTradeAddress.getName();
			}
		
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enterAddressFullName")));
			WebElement fullName = driver.findElement(By.id("enterAddressFullName"));
			fullName.sendKeys(name);
			Utils.sleep(1500);
			logger.debug("--->输入fullName="+name);
			
			WebElement addressLine1 = driver.findElement(By.id("enterAddressAddressLine1"));
			addressLine1.sendKeys(userTradeAddress.getAddress());
			Utils.sleep(1500);
			logger.debug("--->输入addressLine1="+userTradeAddress.getAddress());
			
			WebElement addressCity = driver.findElement(By.id("enterAddressCity"));
			addressCity.sendKeys(userTradeAddress.getCity());
			Utils.sleep(1500);
			logger.debug("--->输入addressCity="+userTradeAddress.getCity());
			
			WebElement addressStateOrRegion = driver.findElement(By.id("enterAddressStateOrRegion"));
			addressStateOrRegion.sendKeys(userTradeAddress.getState());
			Utils.sleep(1500);
			logger.debug("--->输入addressStateOrRegion="+userTradeAddress.getState());
			
			WebElement addressPostalCode = driver.findElement(By.id("enterAddressPostalCode"));
			addressPostalCode.sendKeys(userTradeAddress.getZip());
			Utils.sleep(1500);
			logger.debug("--->输入addressPostalCode="+userTradeAddress.getZip());
			
			WebElement addressPhoneNumber = driver.findElement(By.id("enterAddressPhoneNumber"));
			addressPhoneNumber.sendKeys(userTradeAddress.getMobile());
			Utils.sleep(1500);
			logger.debug("--->输入addressPhoneNumber="+userTradeAddress.getMobile());
			
			WebElement shipAddress = driver.findElement(By.cssSelector(".a-button-input.submit-button-with-name"));
			shipAddress.click();
			
		}catch (Exception e)
		{
			logger.error("--->添加地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		//判断是否不支持的转运地址
		try{
			statusMap.clear();
			logger.error("--->priceMap = "+new Gson().toJson(priceMap));
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-alert-content']/div")));
			List<WebElement> list = driver.findElements(By.xpath("//div[@class='a-box a-spacing-base stma-line-item-details']"));
			if(list != null && list.size() > 0){
				for(WebElement w : list){
					try{
						WebElement ww = w.findElement(By.xpath(".//div[@class='a-alert-content']/div"));
						if(ww != null){
							String text = ww.getText();
							if(StringUtil.isNotEmpty(text) && text.contains("can't be shipped to your selected address")){
								WebElement we = w.findElement(By.xpath(".//span[@class='a-color-price']"));
								if(we != null){
									String singlePriceStr = we.getText();
									logger.error("--->singlePriceStr = "+singlePriceStr);
									if (StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.startsWith("$") && singlePriceStr.length() > 1){
										singlePriceStr = singlePriceStr.substring(1);
										for (Map.Entry<String, String> entry : priceMap.entrySet()) {
											if(StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.equals(entry.getValue())){
												statusMap.put(entry.getKey(), AutoBuyStatus.AUTO_PAY_NOT_SUPPORT_ADDRESS.getValue());
											}
										} 
									}
								}
							}
						}
					}catch(Exception e){
						logger.error("--->没有找到can't be shipped to your selected address");
					}
				}
				logger.error("--->statusMap = "+statusMap.size());
				return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
			}
		}catch(Exception e){
			logger.error("--->没有找到不支持的转运地址");
		}
		
		//保存礼品模式
		try{
			WebDriverWait wait = new WebDriverWait(driver, 15);
			WebElement gift = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-section save-gift-button-box']")));
			gift.click();
		}catch(Exception e){
			logger.error("--->等待保存礼品模式出错", e);
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
	}
	AutoBuyStatus selectBrushTargetAddr(String count){
		try
		{
			logger.debug("--->刷单选地址");
			TimeUnit.SECONDS.sleep(5);
			//prime member ship include prime video
			try{
				driver.findElement(By.cssSelector("button.a-button-text.pet-checkout-button")).click();
				Utils.sleep(4500);
			}catch(Exception e){}
			
			
			
			List<WebElement> addrs = driver.findElements(By.cssSelector("div.address-book-entry"));
			
			// todo 根据tarAddr选择地址
			if (addrs != null && addrs.size() > 0)
			{
				logger.debug("--->目前共有[" + addrs.size() + "]个地址");
				try
				{

					WebElement cur = addrs.get(0);
					
					cur.click();
					TimeUnit.SECONDS.sleep(2);
					try {
						cur.findElement(By.xpath(".//a[ contains(text(), 'Ship to this address')]")).click();
					} catch (Exception e) {
						cur.findElement(By.xpath(".//a[ contains(text(), 'Deliver to this address')]")).click();
					}
					
					
					
					//判断是否不支持的转运地址
					try{
						statusMap.clear();
						logger.error("--->priceMap = "+new Gson().toJson(priceMap));
						WebDriverWait wait = new WebDriverWait(driver, 10);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-alert-content']/div")));
						List<WebElement> list = driver.findElements(By.xpath("//div[@class='a-box a-spacing-base stma-line-item-details']"));
						if(list != null && list.size() > 0){
							for(WebElement w : list){
								try{
									WebElement ww = w.findElement(By.xpath(".//div[@class='a-alert-content']/div"));
									if(ww != null){
										String text = ww.getText();
										if(StringUtil.isNotEmpty(text) && text.contains("can't be shipped to your selected address")){
											WebElement we = w.findElement(By.xpath(".//span[@class='a-color-price']"));
											if(we != null){
												String singlePriceStr = we.getText();
												logger.error("--->singlePriceStr = "+singlePriceStr);
												if (StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.startsWith("$") && singlePriceStr.length() > 1){
													singlePriceStr = singlePriceStr.substring(1);
													for (Map.Entry<String, String> entry : priceMap.entrySet()) {
														if(StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.equals(entry.getValue())){
															statusMap.put(entry.getKey(), AutoBuyStatus.AUTO_PAY_NOT_SUPPORT_ADDRESS.getValue());
														}
													} 
												}
											}
										}
									}
								}catch(Exception e){
									logger.error("--->没有找到can't be shipped to your selected address");
								}
							}
							logger.error("--->statusMap = "+statusMap.size());
							return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
						}
					}catch(Exception e){
						logger.error("--->没有找到不支持的转运地址");
					}
					
					//保存礼品模式
					try{
						WebDriverWait wait = new WebDriverWait(driver, 15);
						WebElement gift = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-section save-gift-button-box']")));
						gift.click();
					}catch(Exception e){
						logger.error("--->等待保存礼品模式出错", e);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}else{
				logger.error("--->选择地址失败1");
				return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
			}
		}
		catch (Exception e)
		{
			logger.error("--->选择地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
	}

	AutoBuyStatus selectTargetAddr(String count,String username,UserTradeAddress userTradeAddress)
	{
		try
		{
			logger.debug("--->选地址");
			TimeUnit.SECONDS.sleep(5);
			//prime member ship include prime video
			try{
				driver.findElement(By.cssSelector("button.a-button-text.pet-checkout-button")).click();
				Utils.sleep(4500);
			}catch(Exception e){}
			
			
			
			List<WebElement> addrs = driver.findElements(By.cssSelector("div.address-book-entry"));
			
			// todo 根据tarAddr选择地址
			if (addrs != null && addrs.size() > 0)
			{
				logger.debug("--->目前共有[" + addrs.size() + "]个地址");
				List<WebElement> availableAddr = new ArrayList<WebElement>();
				for(WebElement a:addrs){
					if(a.getText().toUpperCase().contains(userTradeAddress.getName().toUpperCase())){
						availableAddr.add(a);
					}
				}
				logger.debug("--->下单的地址有[" + availableAddr.size() + "]个可用");
				if(availableAddr.size()<4){
					//添加地址
					WebElement addAddress = driver.findElement(By.cssSelector("a[data-pipeline-link-from-page='address']"));
					addAddress.click();
					return addAddr(userTradeAddress, username, availableAddr.size());
				}
				int index = 0;
				try
				{
					index = Integer.valueOf(count);
					int tarAddr = index % 4;

					WebElement cur = availableAddr.get(tarAddr);
					
					cur.click();
					TimeUnit.SECONDS.sleep(2);
					try {
						cur.findElement(By.xpath(".//a[ contains(text(), 'Ship to this address')]")).click();
					} catch (Exception e) {
						cur.findElement(By.xpath(".//a[ contains(text(), 'Deliver to this address')]")).click();
					}
					
					
					
					//判断是否不支持的转运地址
					try{
						statusMap.clear();
						logger.error("--->priceMap = "+new Gson().toJson(priceMap));
						WebDriverWait wait = new WebDriverWait(driver, 10);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-alert-content']/div")));
						List<WebElement> list = driver.findElements(By.xpath("//div[@class='a-box a-spacing-base stma-line-item-details']"));
						if(list != null && list.size() > 0){
							for(WebElement w : list){
								try{
									WebElement ww = w.findElement(By.xpath(".//div[@class='a-alert-content']/div"));
									if(ww != null){
										String text = ww.getText();
										if(StringUtil.isNotEmpty(text) && text.contains("can't be shipped to your selected address")){
											WebElement we = w.findElement(By.xpath(".//span[@class='a-color-price']"));
											if(we != null){
												String singlePriceStr = we.getText();
												logger.error("--->singlePriceStr = "+singlePriceStr);
												if (StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.startsWith("$") && singlePriceStr.length() > 1){
													singlePriceStr = singlePriceStr.substring(1);
													for (Map.Entry<String, String> entry : priceMap.entrySet()) {
														if(StringUtil.isNotEmpty(singlePriceStr) && singlePriceStr.equals(entry.getValue())){
															statusMap.put(entry.getKey(), AutoBuyStatus.AUTO_PAY_NOT_SUPPORT_ADDRESS.getValue());
														}
													} 
												}
											}
										}
									}
								}catch(Exception e){
									logger.error("--->没有找到can't be shipped to your selected address");
								}
							}
							logger.error("--->statusMap = "+statusMap.size());
							return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
						}
					}catch(Exception e){
						logger.error("--->没有找到不支持的转运地址");
					}
					
					//保存礼品模式
					try{
						WebDriverWait wait = new WebDriverWait(driver, 15);
						WebElement gift = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-section save-gift-button-box']")));
						gift.click();
					}catch(Exception e){
						logger.error("--->等待保存礼品模式出错", e);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}else{
				logger.error("--->选择地址失败1");
				return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
			}
		}
		catch (Exception e)
		{
			logger.error("--->选择地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
	}
	
	

	AutoBuyStatus selectDeliveryOptions()
	{
		try
		{
			try{
				logger.error("选择物流");
				driver.findElement(By.cssSelector("a[data-pipeline-link-to-page='shipoptionselect']")).click();
				Utils.sleep(5000);
			}catch(Exception e){
				logger.error("isgotopay选配送异常",e);
			}
			try{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@id='shippingOptionFormId']")));
			}catch(Exception e){}
			
			
			TimeUnit.SECONDS.sleep(2);
			
			boolean hasFindTwich = false;
			List<WebElement> opts = driver.findElements(By.xpath("//div[@class='description strong']"));
			if(opts == null || opts.size() == 0){
				hasFindTwich = true;
				opts = driver.findElements(By.xpath("//div[@data-a-input-name='order_0_ShippingSpeed']"));
			}
			this.logger.debug("--->目前共有[" + opts.size() + "]种物流可选");
			boolean flag = false;
			for (WebElement e : opts)
			{
				if (e != null)
				{
					String text = "";
					if(!hasFindTwich){
						text = e.getText();
					}else{
						try{
							WebElement w = e.findElement(By.xpath(".//span[@class='a-text-bold']"));
							if(w != null){
								text = w.getText();
							}
						}catch(Exception ee){}
					}
//					FREE Two-Day Shipping with a free trial of
					if (!Utils.isEmpty(text) && text.indexOf("FREE Two-Day Shipping") != -1 && !text.contains("when you join") && !text.contains("a free trial of"))
					{
						this.logger.warn("--->找到[FREE Two-Day Shipping]免费物流");
						flag = true;
						e.click();
						break;
					}

					if (!Utils.isEmpty(text) && text.indexOf("FREE Standard Shipping") != -1)
					{
						this.logger.warn("--->找到[FREE Standard Shipping]标准物流");
						flag = true;
						e.click();
						break;
					}

					if (!Utils.isEmpty(text) && text.indexOf("FREE Shipping") != -1)
					{
						this.logger.warn("--->找到免费物流");
						flag = true;
						e.click();
						break;
					}

					if (!Utils.isEmpty(text) && text.indexOf("Standard Shipping") != -1)
					{
						this.logger.warn("--->找到标准物流");
						flag = true;
						e.click();
						break;
					}
				}
			}

			if (!flag)
			{
				this.logger.warn("--->找不到免费物流,使用默认");
			}
			TimeUnit.SECONDS.sleep(2);
			try{
				driver.findElement(By.xpath("//input[@class='a-button-text' and  contains(@value, 'Continue')]")).click();
			}catch(Exception e){
				try{
					driver.findElement(By.xpath("//input[@class='a-button-input' and  contains(@value, 'Continue')]")).click();
				}catch(Exception ee){
					try{
						driver.findElement(By.xpath("//input[contains(@value, 'Continue')]")).click();
					}catch(Exception eee){
						try{
							driver.findElement(By.xpath("//form[@id='shippingOptionFormId']/span/span[@class='a-button-inner']")).click();
						}catch(Exception e0){
							try{
								driver.findElement(By.xpath("//form[@id='shippingOptionFormId']/div/span[@class='a-button-inner']")).click();
							}catch(Exception e1){
								
							}
						}
					}
				}
				
			}
			
			//Shipment 2 of 2 
			TimeUnit.SECONDS.sleep(4);
			try{
				WebDriverWait wait = new WebDriverWait(driver, 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@id='shippingOptionFormId']")));
				try{
					driver.findElement(By.xpath("//input[@class='a-button-text' and  contains(@value, 'Continue')]")).click();
				}catch(Exception e){
					try{
						driver.findElement(By.xpath("//input[@class='a-button-input' and  contains(@value, 'Continue')]")).click();
					}catch(Exception ee){
						try{
							driver.findElement(By.xpath("//input[contains(@value, 'Continue')]")).click();
						}catch(Exception eee){
							try{
								driver.findElement(By.xpath("//form[@id='shippingOptionFormId']/span/span[@class='a-button-inner']")).click();
							}catch(Exception e0){
								try{
									driver.findElement(By.xpath("//form[@id='shippingOptionFormId']/div/span[@class='a-button-inner']")).click();
								}catch(Exception e1){
									
								}
							}
						}
					}
				}
			}catch(Exception e){}
			

		}
		catch (Exception e)
		{
			logger.debug("--->选择物流失败");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS;
	}

	Pattern canNext = Pattern.compile("(\\d+\\.\\d+)\\s+of\\s+your\\s+(\\d+\\.\\d+)");
	Pattern canNotNext = Pattern.compile("use\\s+your\\s+(\\d+\\.\\d+)\\s+gift\\s+card\\s+balance");
	

	AutoBuyStatus selectGiftCard(String prePrice,Set<String> promotionList,Map<String, String> param)
	{
		String payType = param.get("payType");
		logger.debug("--->支付方式为"+payType);
		try{
			Utils.sleep(5000);
			
			//prime member ship include prime video
			try{
				driver.findElement(By.cssSelector("button.a-button-text.pet-checkout-button")).click();
			}catch(Exception e){}
			Utils.sleep(1000);
			
			
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='select-payments-view']")));
			try {
				List<WebElement> radios = driver.findElements(By.cssSelector(".a-icon-radio"));
				if(!StringUtil.isBlank(payType) && payType.equals("credit")){
					int i=1;
					try {
						List<WebElement> inputradios = driver.findElements(By.cssSelector("input[type='radio']"));
						i=0;
						for(WebElement w:inputradios){
							if(w.getAttribute("value").startsWith("creditCard")){
								break;
							}
							i++;
						}
					} catch (Exception e) {
						logger.debug("--->input radio"+i);
					}
					radios.get(i).click();
					logger.debug("--->credit 点击"+i);
					Utils.sleep(1000);
					try {
						WebElement card = driver.findElement(By.id("addCreditCardNumber"));
						card.clear();
						Utils.sleep(1000);
						card.sendKeys(param.get("cardNo"));
						logger.debug("--->cardno="+param.get("cardNo"));
						List<WebElement> sumbits = driver.findElements(By.cssSelector(".a-button-input"));
						for(WebElement w:sumbits){
							if(w.isDisplayed()){
								w.click();
								logger.debug("--->sumbit 点击");
								break;
							}
						}
						
					} catch (Exception e) {
						logger.debug("--->credit 点击异常");
					}
					
				}else{
					radios.get(0).click();
					logger.debug("--->礼品卡 点击");
				}
			} catch (Exception e) {
				logger.debug("--->查找礼品卡选中按钮出错",e);
			}
			
			try{
				WebElement w = driver.findElement(By.xpath("//label[@for='pm_gc_checkbox']/span/span"));
				String text = w.getText();
				if(StringUtil.isNotEmpty(text)){
					int begin = w.getText().indexOf("$");
					if(begin != -1){
						int end = w.getText().indexOf(" ",begin);
						String price = w.getText().substring(begin+1, end);
						giftBalance = price.replace("-", "").replace(",", "").trim();
						logger.error("giftBalance = "+giftBalance);
						data.put(AutoBuyConst.KEY_AUTO_BUY_GIFTCARD_LEFT, giftBalance);
						float value = sub(totalPrice0, giftBalance);
						value += 5;//运费
						if(value < 0.0000001){
							value = 0;
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, String.valueOf(value));
					}
					
				}
				
			}catch(Exception e){
				
			}
			
			WebElement continueBtn = driver.findElement((By.xpath("//input[@id='continueButton']")));
			if(continueBtn != null){
				String isEnable = continueBtn.getAttribute("disabled");
				if("true".equalsIgnoreCase(isEnable)){
					logger.debug("--->continue按钮是灰色的");
					return AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF;
				}else{
					WebElement visaBox = null;
					try{
						visaBox = driver.findElement(By.xpath("//label[@paymentmethodtype='creditCard']"));
					}catch(Exception e){
						try{
							visaBox = driver.findElement(By.xpath("//div[@data-paymentmethodtype='creditCard']"));
						}catch(Exception ee){}
					}
					
					if(visaBox != null){
						try{
							WebElement radio = visaBox.findElement(By.xpath("./label/input"));
							if(radio != null && radio.isSelected()){
								logger.debug("--->信用卡选项已经选中,需要充值");
								if(!StringUtil.isBlank(payType) && !payType.equals("credit")){
								  return AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF;
								}
							}
						}catch(Exception e){
							logger.debug("--->查找信用卡是否选中出错",e);
							//return AutoBuyStatus.AUTO_PAY_FAIL;
						}
					}
					
					
					//使用优惠码0 失效,1互斥 ,9没修改过,10有效
					if(promotionList != null && promotionList.size() > 0){
						boolean isEffective = false;
						HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
						for(String code : promotionList){
							if(StringUtil.isNotEmpty(code)){
								code = code.trim();
								try{
									WebElement codeInput = driver.findElement(By.xpath("//input[@id='gcpromoinput']"));
									codeInput.clear();
									Utils.sleep(2500);
									codeInput.sendKeys(code);
									Utils.sleep(1500);
									driver.findElement(By.xpath("//button[@id='button-add-gcpromo']")).click();
									Utils.sleep(5500);
									
									try{
										driver.findElement(By.xpath("//div[@class='a-box a-alert-inline a-alert-inline-success']"));
										statusMap.put(code, 10);
										isEffective = true;
									}catch(Exception e){
										logger.error("promotionCode:"+code,e);
										try{
											driver.findElement(By.xpath("//div[@class='a-box a-alert-inline a-alert-inline-error']"));
											statusMap.put(code, 0);
										}catch(Exception ee){
											logger.error("promotionCode:"+code,ee);
										}
									}
								}catch(Exception e){
									logger.error("输入优惠码的时候错误",e);
								}
							}
						}
						setPromotionCodelistStatus(statusMap);
						
						if("true".equals(param.get("isStock")) && !isEffective){
							logger.debug("--->囤货订单优惠码失效,中断采购");
							return AutoBuyStatus.AUTO_PAY_FAIL;
						}
						
						try {
							List<WebElement> radios = driver.findElements(By.cssSelector(".a-icon-radio"));
							if(!StringUtil.isBlank(payType) && payType.equals("credit")){
								int i=1;
								try {
									List<WebElement> inputradios = driver.findElements(By.cssSelector("input[type='radio']"));
									i=0;
									for(WebElement w:inputradios){
										if(w.getAttribute("value").startsWith("creditCard")){
											break;
										}
										i++;
									}
								} catch (Exception e) {
									logger.debug("--->input radio"+i);
								}
								radios.get(i).click();
								logger.debug("--->credit2 点击"+i);
								Utils.sleep(1000);
								try {
									WebElement card = driver.findElement(By.id("addCreditCardNumber"));
									card.clear();
									Utils.sleep(1000);
									card.sendKeys(param.get("cardNo"));
									logger.debug("--->cardno="+param.get("cardNo"));
									List<WebElement> sumbits = driver.findElements(By.cssSelector(".a-button-input"));
									for(WebElement w:sumbits){
										if(w.isDisplayed()){
											w.click();
											logger.debug("--->credit 点击");
											break;
										}
									}
									
								} catch (Exception e) {
									logger.debug("--->credit 点击异常");
								}
							}else{
								radios.get(0).click();
								logger.debug("--->礼品卡2点击");
							}
						} catch (Exception e) {
							logger.debug("--->查找礼品卡选中按钮出错1");
						}
					}
					
					//再次寻找
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("continueButton")));
					continueBtn = driver.findElement(By.id("continueButton"));
					continueBtn.click();
				}
			}else{
				logger.debug("--->找不到continue按钮");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		catch (Exception e){
			logger.error("--->选择礼品卡出错",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS;
		
	}
	
	private AutoBuyStatus checkGiftCard(String str){
		String text = str.replace("$", "").toLowerCase();
		logger.info("--->付款内容:" + text);
		Matcher m = canNext.matcher(text);
		String use = "", all = "";

		if (m.find())
		{
			use = m.group(1);
			all = m.group(2);

			BigDecimal x = new BigDecimal(use);
			BigDecimal y = new BigDecimal(all);
			BigDecimal v = y.subtract(x);
			double left = v.doubleValue();
			if (left > 0)
			{
				logger.debug("--->本次购物之后礼品卡还剩下: $" + left);
				data.put(AutoBuyConst.KEY_AUTO_BUY_GIFTCARD_LEFT, String.valueOf(left));

				WebElement continueBtn = null;
				try
				{
					Utils.sleep(2000);
					continueBtn = driver.findElement(By.xpath("//input[contains(@value, 'Continue')]"));
					Utils.sleep(2000);
					continueBtn.click();
				}
				catch (Exception e)
				{
					boolean isFind = true;
					try{
						continueBtn = driver.findElement(By.xpath("//input[@id='continueButton']"));
						Utils.sleep(2000);
						continueBtn.click();
					}catch(Exception e0){
						isFind = false;
						logger.debug("--->continueButton 没找到000");
					}
					if(!isFind){
						logger.error("--->找不到Continue按钮");
						return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
					}
				}
			}
			else
			{
				// 目前这一步多余
				logger.error("--->[2]礼品卡里的金额不足本次付款");
				return AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF;
			}
		}
		else
		{
			m = canNotNext.matcher(text);
			if (m.find())
			{
				logger.debug("--->礼品卡还剩下: $" + m.group(1));
				data.put(AutoBuyConst.KEY_AUTO_BUY_GIFTCARD_LEFT, String.valueOf(m.group(1)));
				logger.error("--->[1]礼品卡里的金额不足本次付款");
				return AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF;
			}
			else
			{
				logger.error("--->获取礼品卡两种情况都出错");
				return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
			}
		}
		logger.error("checkGiftCard success");
		return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS;
	}

	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount)
	{
		String tarAddr = param.get("count");
		String myPrice = param.get("my_price");
		String username = param.get("userName");
		String payType = param.get("payType");
		String review = param.get("review");
		String size = param.get("size");
		logger.error("--->review:"+review);
		if (Utils.isEmpty(myPrice))
		{
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		try {
			doScreenShot();
		} catch (Exception e) {
		}
		
		try {
			List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
			logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
			logger.debug("--->size有 [" + size + "]件商品");
			if(!size.equals(String.valueOf(goodsInCart.size()))){
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->购物车验证数量出错",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}

		// 设置价格
		logger.error("--->myPrice = " + myPrice);

		logger.debug("默认地址:" + tarAddr);

		AutoBuyStatus status0 = clickCart();
		if (!status0.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) { return AutoBuyStatus.AUTO_CLICK_CART_FAIL; }
		
		//等待购物车页面加载完成
		WebDriverWait wait = new WebDriverWait(driver, 15);
		By by = By.xpath("//button[@class='a-button-text' and contains(text(), 'Proceed to checkout')]");
		WebElement checkout = null;
		try{
			checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		}
		catch (Exception e){
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//获取除去运费的总价
		try{
			WebElement w = driver.findElement(By.xpath("//div[@id='sc-buy-box']/form/span/div/span/span/span"));
			if(StringUtil.isNotEmpty(w.getText())){
				totalPrice0 = w.getText().replace("$", "").replace("-", "").replace(",", "").trim();
				logger.error("totalPrice0 = "+totalPrice0);
			}
		}catch(Exception e){
			try{
				WebElement w = driver.findElement(By.xpath("//div[@id='sc-buy-box']/form/div/span/span/span"));
				if(StringUtil.isNotEmpty(w.getText())){
					totalPrice0 = w.getText().replace("$", "").replace("-", "").replace(",", "").trim();
					logger.error("totalPrice0 = "+totalPrice0);
				}
			}catch(Exception ee){
				logger.error("--->查找除去运费的总价失败",e);
			}
		}

		
		//选中礼品模式
		try{
			WebElement gift = driver.findElement(By.xpath("//input[@id='sc-buy-box-gift-checkbox']"));
			if(!gift.isSelected()){
				gift.click();
				logger.error("--->选中礼品模式");
			}
			TimeUnit.SECONDS.sleep(2);
		}catch(Exception ee){
			logger.error("--->寻找礼品模式失败,"+ee);
		}
		
		//去付款
		logger.debug("--->去付款");
		checkout.click();
		
		//点击跳出来的
		Utils.sleep(5000);
		try{
			driver.findElement(By.cssSelector("button.pda_button.pda_spacing_top_medium")).click();
			Utils.sleep(4000);
		}catch(Exception e){}
		
		//判断是否直接跳转到结算页面
		boolean isGotoPay = false;
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input.a-button-input.place-your-order-button")));
			isGotoPay = true;
		}catch(Exception e){
			logger.error("没有等到结算按钮");
		}
		if(isGotoPay){
			logger.error("直接跳到结算页面");
		}else{
			logger.error("没有直接跳到结算页面");
		}
			
		//开始选地址
		if(!isGotoPay){
			AutoBuyStatus status = null;
			if(!StringUtil.isBlank(review)){
				status = selectBrushTargetAddr(tarAddr);
			}else{
				status = selectTargetAddr(tarAddr,username,userTradeAddress);
			}
			if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS))
			{
				status = selectDeliveryOptions();
				if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS))
				{
					try {
						driver.findElement(By.cssSelector("#payment-info a.a-first")).click();
					} catch (Exception e) {
					}
			
					//优惠码
					String promotionStr = param.get("promotion");
					Set<String> promotionList = getPromotionList(promotionStr);
					status = selectGiftCard(myPrice,promotionList,param);
					if (!status.equals(AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS)) { return status; }
				}
				else
				{
					return status;
				}
			}
			else
			{
				return status;
			}
			
			//等待价格页面摘要加载完成
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='order-summary-box']")));
			}catch(Exception e){
				try{
					try
					{
						driver.findElement(By.xpath("//button[contains(text(),'Continue placing your order')]")).click();;
					}
					catch (Exception ee){
					}
					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception ee){
					logger.error("等待价格页面摘要加载出错",e);
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		}
		
		//如果是isGotoPay重新选择配送方式
		if(isGotoPay){
			try {
				driver.findElement(By.id("change-shipping-address")).click();
			} catch (Exception e) {
				logger.error("isgotopay选地址异常",e);
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			AutoBuyStatus status = null;
			if(!StringUtil.isBlank(review)){
				status = selectBrushTargetAddr(tarAddr);
			}else{
				status = selectTargetAddr(tarAddr,username,userTradeAddress);
			}
			if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS)){
				try{
					logger.error("选择物流");
					driver.findElement(By.cssSelector("a[data-pipeline-link-to-page='shipoptionselect']")).click();
					selectDeliveryOptions();
					Utils.sleep(5000);
				}catch(Exception e){
					logger.error("isgotopay选配送异常",e);
				}
				if(!StringUtil.isBlank(payType) && payType.equals("credit")){
					try {
						logger.error("选择支付方式");
						try {
						driver.findElement(By.cssSelector("#payment-info a.a-first")).click();
						} catch (Exception e) {
						}
						//优惠码
						String promotionStr = param.get("promotion");
						Set<String> promotionList = getPromotionList(promotionStr);
						selectGiftCard(myPrice,promotionList,param);
						Utils.sleep(5000);
					} catch (Exception e) {
						logger.error("支付方式选择异常",e);
					}
				}
			}else{
				return status;
			}
		}
		if(!StringUtil.isBlank(payType) && payType.equals("credit")){
			logger.error("点击usd");
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("marketplaceRadio")));
				Utils.sleep(5000);
				WebElement w =driver.findElement(By.id("marketplaceRadio"));
				driver.executeScript("var tar=arguments[0];tar.click();", w);
				Utils.sleep(5000);
			} catch (Exception e) {
				logger.error("点击usd出错");
			}
		}
		Map<String,String> cacheMap = getTotal();
		String total = null;
		if("credit".equals(payType)){
			total = cacheMap.get("orderTotal");
			total = total.replace("USD", "").trim();
		}else{
			total = cacheMap.get("giftCard");
		}
		String mallFee = cacheMap.get("shippingHandling");
		String promotionFee = cacheMap.get("promotionApplied");
		
		try
		{
			logger.error("--->总价:" + total);
			if (!Utils.isEmpty(total) && Double.valueOf(total).doubleValue() > 0)
			{
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, total.replace("$", ""));
			}
			else
			{
				logger.error("--->获取不到总价");
				return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
			}
		}
		catch (Exception e)
		{
			logger.error("--->获取不到总价");
			return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
		}
		
		try
		{
			logger.error("--->运费:" + mallFee);
			if (!Utils.isEmpty(mallFee) && Double.valueOf(mallFee).doubleValue() >= 0)
			{
				data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE, mallFee.replace("$", ""));
			}
			else
			{
				logger.error("--->运费:");
			}
		}
		catch (Exception e)
		{
			logger.error("--->获取不到运费:");
		}
		try
		{
			logger.error("--->优惠:" + promotionFee);
			if (!Utils.isEmpty(promotionFee) && Double.valueOf(promotionFee).doubleValue() >= 0)
			{
				data.put(AutoBuyConst.KEY_AUTO_BUY_PROMOTION_FEE, promotionFee.replace("$", ""));
			}
			else
			{
				logger.error("--->获取不到优惠:");
			}
		}
		catch (Exception e)
		{
			logger.error("--->获取不到优惠:");
		}

		try
		{
			logger.debug("--->完成付款,开始比价[" + myPrice + "," + total + "]");
			total = total.replace("$", "");
			
			if(!StringUtil.isBlank(getTotalPrice())){
				AutoBuyStatus priceStatus = comparePrice(total, getTotalPrice());
				if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}else{
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(total);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 0D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
			
			doScreenShot();

			Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
			if (isPay)
			{
				logger.error("=================");
				logger.error("|---> 点击付款  <---|");
				logger.error("=================");
				
				Utils.sleep(1500);
				try{
					driver.findElement(By.cssSelector("input[name='placeYourOrder1']")).click();
				}catch(Exception e){
					try{
						driver.findElement(By.cssSelector("input.a-button-input.place-your-order-button")).click();
					}catch(Exception ee){
						logger.error("--->点击付款出错", ee);
						return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
					}
				}
			}
			else
			{
				return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
			}
		}
		catch (Exception e)
		{
			logger.error("--->付款出错!", e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		String orderNo  = "";
		try {
			orderNo = getAmazonOrderNo();
		} catch (Exception e) {
			logger.error("--->获取amazon单号出错!");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		
		if (!Utils.isEmpty(orderNo))
		{
			logger.error("--->获取amazon单号成功:\t" + orderNo);
			savePng();
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
			
		}
		else
		{
			logger.error("--->获取amazon单号出错!");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		
		try {
			driver.navigate().to("https://www.amazon.com/balance?ref_=ya_mb_asv_b_m");
		} catch (Exception e) {
			logger.info("--->跳转礼品卡页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-color-success")));
			WebElement gitBalance = driver.findElement(By.cssSelector(".a-color-success"));
			String balanceText = gitBalance.getText().trim();
			logger.info("--->礼品卡余额:"+balanceText);
			balanceText = balanceText.replace("$", "").replace("-", "").replace(",", "").trim();
			data.put(AutoBuyConst.KEY_AUTO_BALANCE_WB, balanceText);
		}catch (Exception e) {
			logger.info("--->礼品卡余额获取失败!");
		}

//		List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
//		if (goodsInCart != null && goodsInCart.size() > 0)
//		{
//			
//			
//		}
//		else
//		{
//			logger.info("--->购物车为空!");
//		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;

	}

	Map<String, String> getTotal()
	{
		Map<String, String> cache = new HashMap<String, String>();
		try
		{
			By totalBy = By.cssSelector("div#subtotals-marketplace-table table tr");
			List<WebElement> trs = driver.findElements(totalBy);
			if(trs == null || trs.size() <= 0){
				trs = driver.findElements(By.cssSelector("table#subtotals-marketplace-table tbody tr"));
			}
			for (WebElement tr : trs)
			{
				List<WebElement> tds = tr.findElements(By.tagName("td"));
				String key = tds.get(0).getText();
				String value = tds.get(1).getText();
				key = key.replace("&", "").replaceAll("\\s+", "_").replace(":", "").replace("-", "").toLowerCase();
				value = value.replace("$", "").replace("-", "").replace(",", "").trim();
				cache.put(Utils.underlineToCamel(key), value);
			}
			logger.debug("--->付款详情:" + cache);

		}
		catch (Exception e)
		{
			logger.error("--->获取不到总价");
		}
		return cache;
	}

	String getAmazonOrderNo()
	{
		String orderNo = null;
		List<WebElement> divs = driver.findElements(By.xpath("//div[@class='a-box-inner']"));
		boolean flag = false;
		if (divs != null && divs.size() > 0)
		{
			for (WebElement div : divs)
			{
				String txt = div.getText();
				logger.debug("--->txt:"+txt);
				if (txt.indexOf("Review or edit your order") > -1 || txt.indexOf("Review or edit order") > -1)
				{
					logger.debug("--->找到了查看按钮");
					flag = true;
					div.click();
					break;
				}
			}
		}else{
			logger.debug("--->查找Review or edit order");
			//doScreenShot();
			try {
				WebElement w =driver.findElement(By.xpath("//span[contains(text(),'Review or edit order')]"));
				logger.debug("--->找到了Review or edit order");
				w.click();
				flag = true;
			} catch (Exception e) {
				logger.debug("--->找不到了Review or edit order");
			}
			
		}

		if (flag)
		{
			try
			{
				doScreenShot();
				WebDriverWait wait = new WebDriverWait(driver, 30);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'View order details')]")));
				WebElement x = driver.findElement(By.xpath("//h3[contains(text(),'View order details')]/following-sibling::div[1]"));
				WebElement y = x.findElement(By.xpath(".//div[@class='a-row'][2]"));
				List<WebElement> ps = y.findElements(By.tagName("p"));
				if (ps.size() == 2)
				{
					orderNo = ps.get(1).getText();
				}
			}
			catch (Exception e)
			{
				try {
					
					WebElement hrefUrl = driver.findElement(By.cssSelector("#ordersContainer a"));
					String url = hrefUrl.getAttribute("href");
					logger.debug("获取订单号href:"+url);
					String group[] = url.split("&");
			    	for(String s:group){
			    		if(s.contains("orderId")){
			    			s = s.replace("orderId=", "");
			    			return s;
			    		}
			    	}
				} catch (Exception e2) {
					logger.debug("查找订单号失败",e2);
				}
				
				logger.debug("整1个html:"+driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text"));
			}
		}else{
			logger.debug("整个html:"+driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text"));
		}
		return orderNo;
	}
	
	private static Date getDate(String dateStr,String timeStr) throws Exception{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.SECOND, 0);
		int currMonth = calendar.get(Calendar.MONTH);
		//Nov 28
		//3:06 AM
		String[] dates = dateStr.split(" ");
		if(dates.length > 0){
			int day = Integer.parseInt(dates[dates.length-1]);
			calendar.set(Calendar.DAY_OF_MONTH, day);
		}
		String[] times = timeStr.split(" ");
		if(times.length == 2){
			String[] strs = times[0].split(":");
			int minite = Integer.parseInt(strs[1]);
			int hour = Integer.parseInt(strs[0]);
			calendar.set(Calendar.MINUTE, minite);
			if("pm".equalsIgnoreCase((times[1])) && hour < 12){//下午
				hour += 12;
			}
			calendar.set(Calendar.HOUR_OF_DAY, hour);
		}
		if(StringUtil.isNotEmpty(dates[0])){
			if("Jan".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 0);
			}else if("Feb".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 1);
			}else if("Mar".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 2);
			}else if("Apr".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 3);
			}else if("May".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 4);
			}else if("Jun".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 5);
			}else if("Jul".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 6);
			}else if("Aug".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 7);
			}else if("Sep".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 8);
			}else if("Oct".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 9);
			}else if("Nov".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 10);
			}else if("Dec".equalsIgnoreCase(dates[0])){
				calendar.set(Calendar.MONTH, 11);
			}
		}
		if(calendar.get(Calendar.MONTH) > currMonth){
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)-1);
		}
		
		return calendar.getTime();
	}

	public boolean gotoMainPage()
	{
		if(isWap){
			try
			{
				Utils.sleep(2000);
				driver.get("http://www.amazon.com/");
				WebDriverWait wait = new WebDriverWait(driver, 10);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='nav-greeting-name' and contains(text(),'Hello')]")));
				return true;
			}
			catch (Exception e)
			{
			}
			return false;
		}else{
			try
			{
				Utils.sleep(2000);
				driver.get("http://www.amazon.com/");
				WebDriverWait wait = new WebDriverWait(driver, 20);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#nav-search")));
				return true;
			}
			catch (Exception e)
			{
			}
			return false;
		}
	}
	
	public AutoBuyStatus scribeExpress1(RobotOrderDetail detail){
		String productEntityCode = "";
		if(getAsinMap() != null){
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = "+productEntityCode);
		if (Utils.isEmpty(productEntityCode)){
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; }
		
		driver.navigate().to("https://www.amazon.com/gp/your-account/order-history?ie=UTF8&ref=ya_orders_css");
		WebDriverWait wait = new WebDriverWait(driver, 25);
		try{
			WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input#searchOrdersInput")));
			input.sendKeys(mallOrderNo);
			Utils.sleep(1500);
			driver.findElement(By.xpath("//span[@id='a-autoid-0']")).click();
		}catch(Exception e){
			logger.error("等待mall_order_no input error",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		Utils.sleep(4000);
		
		//对比asinCode
		try{
//			List<WebElement> ll = driver.findElements(By.xpath("//div[@class='a-box-group a-spacing-base order']/div[@class='a-box shipment']"));
			List<WebElement> ll = driver.findElements(By.cssSelector("div.a-box.shipment"));
			if(ll != null && ll.size() > 0){
				boolean isFind = false;
				loop:
				for(WebElement w : ll){
					List<WebElement> wwList = w.findElements(By.xpath(".//div[@class='item-view-left-col-inner']/a[@class='a-link-normal']"));
					if(wwList != null && wwList.size() > 0){
						for(WebElement ww : wwList){
							if(ww != null){
								String productLink = ww.getAttribute("href");
								if(productLink != null && productLink.contains(productEntityCode)){
									// 被砍单
									try{
										WebElement canceled = w.findElement(By.xpath(".//span[@class='a-size-small a-color-secondary' and contains(text(),'Cancelled')]"));
										if (canceled != null){
											logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
											return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
										}
									}
									catch (NoSuchElementException e){}
									WebElement seeShip = w.findElement(By.xpath(".//a[contains(text(),'Track package')]"));
									if(seeShip != null){
										isFind = true;
										seeShip.click();
										break loop;
									}
								}
							}
						}
					}
				}
				if(!isFind){
					logger.debug("--->isFind = false 没找到订单:"+mallOrderNo);
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
				}
			}else{
				logger.debug("--->没找到订单:"+mallOrderNo);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("对比asinCode出现异常,商城还没发货",e);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
		}
		
		//等待物流页面加载完成
		try{																		
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-large-horizontal-widget']")));
		}catch(Exception e){
			logger.debug("--->等待物流页面加载完成出错",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查找物流节点show more
		try{
			driver.findElement(By.xpath("//a[@class='a-expander-header a-declarative a-expander-extend-header']")).click();
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->点击show more出错",e);
		}
				
		//查找物流
		String expressNo = detail.getExpressNo();
		AutoBuyStatus status = null;
		if(detail.getStatus() != 100){
			try{
				Utils.sleep(1000);
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-row a-spacing-top-mini a-size-small a-color-tertiary ship-track-grid-subtext']"));
				if(elements == null || elements.size() == 0){
					elements = driver.findElements(By.xpath("//div[@class='a-row a-spacing-top-small a-size-small a-color-tertiary']"));
				}
				if (elements != null && elements.size() > 0){
					for (WebElement e : elements){
						String aStr = "Carrier: ";
						String bStr = ", Tracking #: ";
						String str = e.getText();
						int a = str.indexOf(aStr);
						int b = str.indexOf(bStr);
						if (a != -1 && b != -1){
							String expressCompany = str.substring(a + aStr.length(), b).trim();
							expressNo = str.substring(b + bStr.length()).trim();
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							logger.error("expressCompany = " + expressCompany);
							logger.error("expressNo = " + expressNo);
							status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							break;
						}
					}
				}
				logger.error("该订单还没发货,没产生物流单号");
				if(status != AutoBuyStatus.AUTO_SCRIBE_SUCCESS){
					status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
				status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}else{
			status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
		}
		
		//查找国外的物流节点
		try{
			List<WebElement> panelList = driver.findElements(By.xpath("//div[@class='a-box-group ship-track-latest-event-wrapper']"));
			List<WebElement> panelList0 = driver.findElements(By.xpath("//div[@class='a-box-group ship-track-other-events-wrapper']"));
			if(panelList != null && panelList.size() > 0){
				if(panelList0 != null && panelList0.size() > 0){
					panelList.addAll(panelList0);
				}
				String dateStr = "";
				List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
				for(WebElement panel : panelList){
					List<WebElement> list = panel.findElements(By.xpath(".//div"));
					if(list != null && list.size() > 0){
						for(WebElement w : list){
							try{
								String cssClass = w.getAttribute("class");
								if(StringUtil.isNotEmpty(cssClass) ){
									if(cssClass.contains("a-color-alternate-background")){//日期条
										WebElement wDate = w.findElement(By.xpath(".//div/span[@class='a-text-bold']"));
										String str = wDate.getText();
										if(StringUtil.isNotEmpty(str)){
											String split = ", ";
											int index = str.indexOf(split);
											if(index != -1){
												dateStr = str.substring(index+split.length()).trim();
												logger.error("dateStr = "+dateStr);
											}
										}
									}else{
										WebElement wTime = w.findElement(By.xpath(".//div/div/div[@class='a-column a-span3 ship-track-grid-fixed-column']"));
										String timeStr = wTime.getText();
										WebElement node = w.findElement(By.xpath(".//div[@class='a-column a-span9 ship-track-grid-responsive-column a-span-last']"));
										String nodeStr = node.getText();
										Date nodeDate = getDate(dateStr, timeStr);
										
										if(nodeDate != null && StringUtil.isNotEmpty(nodeStr) && StringUtil.isNotEmpty(expressNo)){
											ExpressNode expressNode = new ExpressNode();
											expressNode.setOrderNo(detail.getOrderNo());
											expressNode.setExpressNo(expressNo);
											expressNode.setName(nodeStr);
											expressNode.setOccurTime(nodeDate);
											if(nodeStr.contains("signed by") || nodeStr.contains("was delivered")){
												expressNode.setStatus(14);//已签收
											}else{
												expressNode.setStatus(3);
											}
											nodeList.add(expressNode);
										}
									}
								}
							}catch(Exception e){
								logger.error("寻找节点出错",e);
							}
						}
					}
				}
				
				if(nodeList.size() > 0 && getTask() != null){
					logger.error("addParam expressNodeList");
					getTask().addParam("expressNodeList", nodeList);
				}
			}
		}catch(Exception e){
			logger.error("查找国外的物流节点出错",e);
		}
		
		return status;
	}
	
	public AutoBuyStatus chooseOrder(BrushOrderDetail detail, String productEntityCode) {
		try {
			String mallOrderNo = detail.getMallOrderNo();
			if (Utils.isEmpty(mallOrderNo)) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_MALL_ORDER_EMPTY;
			}
			
			logger.debug("查找订单，点击我的订单");
			WebElement ordersEle = driver.findElement(By.id("nav-orders"));
			ordersEle.click();
			Utils.sleep(1500);

			logger.debug("查找订单，输入商城订单号：" + mallOrderNo);
			WebElement searchInput = driver.findElement(By.id("searchOrdersInput"));
			searchInput.sendKeys(mallOrderNo);
			Utils.sleep(1500);
			
			logger.debug("查找订单，点击查找按钮");
			WebElement searchButton = driver.findElement(By.xpath("//span[@id='a-autoid-0-announce' and contains(text(), 'Search Orders')]"));
			searchButton.click();
			Utils.sleep(5000);
			logger.debug("查找订单完成");
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_SUCCESS;
		} catch (Exception e) {
			logger.error("feedBack or review查找订单失败", e);
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_FAIL;
		}
	}
	
	public AutoBuyStatus checkReview(BrushOrderDetail detail, Map<String, String> param) {
		String productEntityCode = "";
		if (getAsinMap() != null) {
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = " + productEntityCode);
		if (Utils.isEmpty(productEntityCode)) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}
		
		AutoBuyStatus status = chooseOrder(detail, productEntityCode);
		if (!AutoBuyStatus.AUTO_CHOOSE_ORDER_SUCCESS.equals(status)) {
			return status;
		}
		
		List<WebElement> orders = driver.findElementsByCssSelector("div.a-box-group.a-spacing-base.order");
		if (orders.size() != 1) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}

		try {
			WebElement order = orders.get(0);
			WebElement feedBack = order.findElement(By.id("Write-a-product-review_1"));
			feedBack.click();
			Utils.sleep(3000);
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_REVIEW_CAN_NOT_FIND_BUTTON;
		}
		
		try {
			List<WebElement> reviewProducts = driver.findElements(By.cssSelector("div#reviewProductsViewport > div"));
			WebElement reviewContainer = null;
			for (WebElement reviewProduct : reviewProducts) {
				WebElement urlEle = reviewProduct.findElement(By.cssSelector("a.a-size-base.a-link-normal.title[target=_blank]"));
				logger.error("review product_url:" + urlEle.getAttribute("href"));
				if (urlEle.getAttribute("href").contains(productEntityCode)) {
					logger.error("review 找到review product");
					reviewContainer = reviewProduct;
					break;
				}
			}
			
			if (reviewContainer == null) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
			}
			
			WebElement starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"select to rate item three star.\"]"));
			if (starELe.getAttribute("class").contains("yellowStar")) {
				return AutoBuyStatus.AUTO_CHECK_REVIEW_SUCCESS;
			} else {
				starELe.click();
				Utils.sleep(1500);
				try {
					WebElement alertHeading = reviewContainer.findElement(By.xpath("//h4[@class='a-alert-heading' and contains(text(),'Sorry, we are unable to accept your review of this product')]"));
					if (alertHeading != null) {
						return AutoBuyStatus.AUTO_CHECK_REVIEW_FAIL;
					} else {
						return AutoBuyStatus.AUTO_CHECK_REVIEW_NEVER_REVIEW;
					}
				} catch (Exception e) {
					logger.error("检查review找不到失败提示", e);
					return AutoBuyStatus.AUTO_CHECK_REVIEW_EXCEPTION;
				}
				
			}
		} catch (Exception e) {
			logger.error("检查review异常", e);
			return AutoBuyStatus.AUTO_CHECK_REVIEW_EXCEPTION;
		}
	}
	
	public AutoBuyStatus review(BrushOrderDetail detail) {
		String productEntityCode = "";
		String reviewContent = detail.getReviewContent();
		String headlineContent = detail.getReviewTitle();
		if (getAsinMap() != null) {
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = " + productEntityCode);
		if (Utils.isEmpty(productEntityCode)) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}
		
		if (StringUtil.isEmpty(reviewContent)) {
			return AutoBuyStatus.AUTO_REVIEW_FAIL;
		}
		
		AutoBuyStatus status = chooseOrder(detail, productEntityCode);
		if (!AutoBuyStatus.AUTO_CHOOSE_ORDER_SUCCESS.equals(status)) {
			return status;
		}
		
		List<WebElement> orders = driver.findElementsByCssSelector("div.a-box-group.a-spacing-base.order");
		if (orders.size() != 1) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}

		try {
			WebElement order = orders.get(0);
			WebElement feedBack = order.findElement(By.id("Write-a-product-review_1"));
			feedBack.click();
			Utils.sleep(3000);
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_REVIEW_CAN_NOT_FIND_BUTTON;
		}
		
		try {
			List<WebElement> reviewProducts = driver.findElements(By.cssSelector("div#reviewProductsViewport > div"));
			WebElement reviewContainer = null;
			for (WebElement reviewProduct : reviewProducts) {
				List<WebElement> urlEles = reviewProduct.findElements(By.cssSelector("a.a-size-base.a-link-normal.title[target=_blank]"));
				if (urlEles == null || urlEles.size() == 0) {
					continue;
				}
				logger.error("review product_url:" + urlEles.get(0).getAttribute("href"));
				if (urlEles.get(0).getAttribute("href").contains(productEntityCode)) {
					logger.error("review 找到review product");
					reviewContainer = reviewProduct;
					break;
				}
			}
			
			if (reviewContainer == null) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
			}
			
			List<WebElement> fitEles = reviewContainer.findElements(By.cssSelector("input.a-button-input[value=TRUE_TO_FIT]"));
			if (fitEles.size() > 0) {
				fitEles.get(0).click();
				Utils.sleep(3000);
			}
			
			logger.error("review打星");
			WebElement starELe = null;
			if (getBooleanByRate(0.2)) {
				starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"select to rate item four star.\"]"));
			} else {
				starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"select to rate item five star.\"]"));
			}
			if (!starELe.getAttribute("class").contains("yellowStar")) {
				starELe.click();
				Utils.sleep(1500);
			}
			
			logger.error("review输入");
			WebElement reviewTextArea = reviewContainer.findElement(By.cssSelector("div.a-input-text-wrapper.bigTextArea.textField.reviewText.expandingTextSectionHeight[title=\"Write your review here\"] > textarea"));
			reviewTextArea.sendKeys(reviewContent);
			Utils.sleep(1500);

			logger.error("review headline输入");
			WebElement headline = reviewContainer.findElement(By.cssSelector("input[aria-label=\"Headline for your review\"]"));
			headline.sendKeys(headlineContent);
			Utils.sleep(2500);

			logger.error("review 提交");
			WebElement submitELe = reviewContainer.findElement(By.cssSelector("span.submitText"));
			WebElement submitButtom = submitELe.findElement(By.xpath("./parent::span/preceding-sibling::input[1]"));
			submitButtom.click();
			Utils.sleep(1500);
			return AutoBuyStatus.AUTO_REVIEW_SUCCESS;
		} catch (Exception e) {
			logger.error("review异常", e);
			return AutoBuyStatus.AUTO_REVIEW_FAIL;
		}
	}
	
	public AutoBuyStatus feedBack(BrushOrderDetail detail) {
		String productEntityCode = "";
		String feedBackContent = detail.getFeedbackContent();
		if (getAsinMap() != null) {
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = " + productEntityCode);
		if (Utils.isEmpty(productEntityCode)) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}
		
		if (StringUtil.isEmpty(feedBackContent)) {
			return AutoBuyStatus.AUTO_FEED_BACK_FAIL;
		}
		
		AutoBuyStatus status = chooseOrder(detail, productEntityCode);
		if (!AutoBuyStatus.AUTO_CHOOSE_ORDER_SUCCESS.equals(status)) {
			return status;
		}
		Utils.sleep(3000);
		
		List<WebElement> orders = driver.findElementsByCssSelector("div.a-box-group.a-spacing-base.order");
		if (orders.size() != 1) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}

		try {
			WebElement order = orders.get(0);
			List<WebElement> feedBacks = order.findElements(By.id("Leave-seller-feedback_1"));
			if (feedBacks.size() == 0) {
				return AutoBuyStatus.AUTO_FEED_BACK_SUCCESS;
			} else {
				feedBacks.get(0).click();
			}
			Utils.sleep(3000);
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_FEED_BACK_CAN_NOT_FIND_BUTTON;
		}
		
		try {
			WebElement mainFeedBackContainer = null;
			List<WebElement> fbContainers = driver.findElements(By.cssSelector("div.fb_container"));
			for (WebElement fbContainer : fbContainers) {
				WebElement urlEle = fbContainer.findElement(By.cssSelector("span > h4 > a[target=_blank]"));
				logger.error("feedBack product_url:" + urlEle.getAttribute("href"));
				if (urlEle.getAttribute("href").contains(productEntityCode)) {
					mainFeedBackContainer = fbContainer;
					
				}
			}
			if (mainFeedBackContainer == null) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
			}
			
			logger.error("feedBack打星");

			if (getBooleanByRate(0.2)) {
				mainFeedBackContainer.findElement(By.cssSelector("div.a-section.a-spacing-none.starSprite.bigStar.clickable.rating4")).click();
			} else {
				mainFeedBackContainer.findElement(By.cssSelector("div.a-section.a-spacing-none.starSprite.bigStar.clickable.rating5")).click();
			}

			logger.error("feedBack fulfillmentAnswer");
			List<WebElement> fulfillmentAnswers = mainFeedBackContainer.findElements(By.cssSelector("div.a-radio[data-a-input-name=fulfillmentAnswer] > label > span"));
			for (WebElement fulfillmentAnswer : fulfillmentAnswers) {
				if (fulfillmentAnswer.getText().contains("Yes")) {
					fulfillmentAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);

			logger.error("feedBack itemAsDescribedAnswer");
			List<WebElement> itemAsDescribedAnswers = mainFeedBackContainer.findElements(By.cssSelector("div.a-radio[data-a-input-name=itemAsDescribedAnswer] > label > span"));
			for (WebElement itemAsDescribedAnswer : itemAsDescribedAnswers) {
				if (itemAsDescribedAnswer.getText().contains("Yes")) {
					itemAsDescribedAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);

			logger.error("feedBack customerServiceAnswer");
			List<WebElement> customerServiceAnswers = mainFeedBackContainer.findElements(By.cssSelector("div.a-radio[data-a-input-name=customerServiceAnswer] > label > span"));
			for (WebElement customerServiceAnswer : customerServiceAnswers) {
				if (customerServiceAnswer.getText().contains("Did not contact")) {
					customerServiceAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);
			
			logger.error("feedBack填写Comment:" + feedBackContent);
			WebElement feedbackText = mainFeedBackContainer.findElement(By.id("feedbackText"));
			feedbackText.sendKeys(feedBackContent);
			Utils.sleep(1500);
			
			WebElement submit = driver.findElementById("a-autoid-0-announce");
			if (!submit.getText().contains("Submit Feedback")) {
				logger.error("a-autoid-0-announce 不是feedback");
				driver.findElement(By.xpath("//span[@id='a-autoid-1-announce' and contains(text(), 'Submit Feedback')]")).click();
			} else {
				submit.click();
			}
			Utils.sleep(5000);
			
			return AutoBuyStatus.AUTO_FEED_BACK_SUCCESS;
		} catch (Exception e) {
			logger.error("feedBack异常", e);
			return AutoBuyStatus.AUTO_FEED_BACK_FAIL;
		}
	}

	public AutoBuyStatus scribeExpress(RobotOrderDetail detail)
	{
		String productEntityCode = "";
		if(getAsinMap() != null){
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = "+productEntityCode);
		if (Utils.isEmpty(productEntityCode)){
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; }
		// 寻找Your Account
		try
		{
			WebElement panelAccount = driver.findElement(By.xpath("//ul[@id='nav-ftr-links']"));
			WebElement account = panelAccount.findElement(By.xpath(".//span[@class='nav-ftr-text' and contains(text(),'Your Account')]"));
			Utils.sleep(1500);
			account.click();
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Your Account");
			try{
				WebElement avatar = driver.findElement(By.xpath("//a[@id='nav-button-avatar']"));
				Utils.sleep(1500);
				avatar.click();
			}catch(Exception ee){
				logger.error("找不到主页上面的Avatar");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}
		WebDriverWait wait = new WebDriverWait(driver, 45);

		// 寻找Your orders
		try
		{
			WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'Your orders')]")));
			Utils.sleep(1500);
			order.click();
		}
		catch (Exception e)
		{
			logger.error("找不到Your orders");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}

		try
		{
			// 寻找Search all orders showSearchBar
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='showSearchBar']")));
			
			Utils.sleep(1500);
//			WebElement search = 
//			search.click();
//
//			// 寻找订单输入框
//			Utils.sleep(3000);
//			WebElement orderInput = driver.findElement(By.xpath("//input[@id='searchOrdersInput']"));
//			Utils.sleep(1500);
//			orderInput.sendKeys(mallOrderNo);
//			Utils.sleep(2000);
//			orderInput.sendKeys(Keys.ENTER);
			
//			List<WebElement> orderLists = driver.findElements(By.cssSelector("#ordersContainer .a-section a"));
//			for(WebElement w:orderLists){
//				if(w.getAttribute("href").contains(mallOrderNo)){
//					w.click();
//					break;
//				}
//			}
			//Utils.sleep(1500);
		}
		catch (Exception e)
		{
			logger.error("查找订单错误");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		

		// 等待Filter orders不可见
//		try
//		{
//			boolean isVisbile = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@id='searchOrdersInput']")));
//			logger.error("isVisbile = " + isVisbile);
//			Utils.sleep(2000);
//		}
//		catch (Exception e)
//		{
//			logger.error("等待Filter orders不可见 出异常", e);
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
		driver.get("https://www.amazon.com/gp/your-account/ship-track/ref=oh_aui_i_sh_post_o0?ie=UTF8&itemId=jmlipsjtprpwun&orderId="+mallOrderNo);
		Utils.sleep(5000);
		//对比asinCode
		boolean mark = true;
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
			List<WebElement> ll = driver.findElements(By.cssSelector(".a-section .a-box-group"));
			if(ll != null && ll.size() > 0){
				for(WebElement w : ll){
					try {
						List<WebElement> ww = w.findElements(By.cssSelector(".a-link-normal"));
						for(WebElement pw:ww){
							String productLink = pw.getAttribute("href");
							if(productLink != null && productLink.contains(productEntityCode)){
								mark = false;
								logger.error("ll productEntityCode="+productEntityCode);
								// 被砍单
								try{
									WebElement canceled = w.findElement(By.xpath("/span[@class='a-size-small a-color-secondary' and contains(text(),'Cancelled')]"));
									if (canceled != null){
										logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
									}
								}
								catch (NoSuchElementException e){}
								try{
									List<WebElement> tracks = w.findElements(By.cssSelector("a.a-touch-link"));
									for(WebElement tra:tracks){
										if(tra.getText().contains("Track")){
											tra.click();
											break;
										}
									}
								}catch(Exception e1){
									logger.error("View tracking details 出错1");
								}
								
//								try {
//									WebElement seeShip = w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track package')]"));
//									if(seeShip != null){
//										seeShip.click();
//										break;
//									}
//								} catch (Exception e) {
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'View order details')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错");
//									}
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track shipment')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错1");
//									}
//								}
								break;
							}
							
						}
					} catch (Exception e) {
					}
				}
			}else{
				logger.debug("--->没找到订单:"+mallOrderNo);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("对比asinCode出现异常,商城还没发货",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		if(mark){
			logger.debug("--->没找到订单:"+mallOrderNo);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		//等待物流页面加载完成
//		try{
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
//		}catch(Exception e){
//			logger.debug("--->等待物流页面加载完成出错",e);
//			try{
//				List<WebElement> tracks = driver.findElements(By.cssSelector(".a-section .a-box-group a.a-touch-link"));
//				for(WebElement tra:tracks){
//					if(tra.getText().contains("Track")){
//						tra.click();
//						break;
//					}
//				}
//			}catch(Exception e1){
//				logger.error("View tracking details 出错1");
//			}
//		}
		AutoBuyStatus status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
		//等待物流页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
		}catch(Exception e){
			logger.debug("--->等待物流页面加载完成出错");
			try {
				WebElement trackNum = driver.findElement(By.cssSelector("#carrierRelatedInfo-container .carrierRelatedInfo-trackingId-text"));
				WebElement trackingCompany = driver.findElement(By.cssSelector("#carrierRelatedInfo-container h1"));
				String expressNo = trackNum.getText().replace("Tracking ID", "").trim();
				
				String expressCompany = trackingCompany.getText().replace("Shipped with", "").trim();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
				logger.error("expressCompany = " + expressCompany);
				logger.error("expressNo = " + expressNo);
				status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			} catch (Exception e2) {
				logger.debug("对比asinCode出现异常,商城还没发货",e2);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
			
		}
		
		//查找物流
		String expressNo = detail.getExpressNo();
		
		if(detail.getStatus() != 100 && !status.equals(AutoBuyStatus.AUTO_SCRIBE_SUCCESS)){
			try
			{
				Utils.sleep(1000);
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-box-inner a-padding-medium']"));
				if (elements != null && elements.size() > 0)
				{
					for (WebElement e : elements)
					{
						String aStr = "Carrier";
						String bStr = "Tracking #";
						String str = e.getText();
						int a = str.indexOf(aStr);
						int b = str.indexOf(bStr);
						if (a != -1 && b != -1)
						{
							String expressCompany = str.substring(a + aStr.length(), b).trim();
							expressNo = str.substring(b + bStr.length()).trim();
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							logger.error("expressCompany = " + expressCompany);
							logger.error("expressNo = " + expressNo);
							status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}
					}
				}
				logger.error("该订单还没发货,没产生物流单号");
				if(status != AutoBuyStatus.AUTO_SCRIBE_SUCCESS){
					status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
				status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}else{
			status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
		}
		
		
		try{
			driver.findElement(By.xpath("//div[contains(text(),'View tracking details')]")).click();
		}catch(Exception e){
			logger.error("View tracking details 出错",e);
		}
		Utils.sleep(5000);
		
		
		
		//查找国外的物流节点
		try{
			List<WebElement> panelList = driver.findElements(By.xpath("//div[@class='a-box-group a-spacing-mini']"));
			if(panelList != null && panelList.size() > 0){
				String dateStr = "";
				List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
				for(WebElement panel : panelList){
					List<WebElement> list = panel.findElements(By.cssSelector("div.a-box"));
					if(list != null && list.size() > 0){
						for(WebElement w : list){
							try{
								String cssClass = w.getAttribute("class");
								if(StringUtil.isNotEmpty(cssClass) ){
									if(cssClass.contains("a-color-alternate-background")){//日期条
										WebElement wDate = w.findElement(By.cssSelector("span.a-text-bold"));
										String str = wDate.getText();
										if(StringUtil.isNotEmpty(str)){
											String split = ", ";
											int index = str.indexOf(split);
											if(index != -1){
												dateStr = str.substring(index+split.length()).trim();
												logger.error("dateStr = "+dateStr);
											}
										}
									}else{
										WebElement wTime = w.findElement(By.cssSelector("div>div>div.a-column.a-span3.ship-track-grid-fixed-column"));
										String timeStr = wTime.getText();
										WebElement node = w.findElement(By.xpath(".//div[@class='a-column a-span9 ship-track-grid-responsive-column a-span-last']"));
										String nodeStr = node.getText();
										Date nodeDate = getDate(dateStr, timeStr);
										
										if(nodeDate != null && StringUtil.isNotEmpty(nodeStr) && StringUtil.isNotEmpty(expressNo)){
											nodeStr = nodeStr.trim();
											ExpressNode expressNode = new ExpressNode();
											expressNode.setOrderNo(detail.getOrderNo());
											expressNode.setExpressNo(expressNo);
											expressNode.setName(nodeStr);
											expressNode.setOccurTime(nodeDate);
											if(nodeStr.contains("signed by") || nodeStr.contains("was delivered") || nodeStr.contains("Out for delivery") || nodeStr.contains("handed off directly")){
												expressNode.setStatus(14);//已签收
											}else{
												expressNode.setStatus(3);
											}
											nodeList.add(expressNode);
										}
									}
								}
							}catch(Exception e){
//								logger.error("寻找节点出错",e);
							}
						}
					}
				}
				
				if(nodeList.size() > 0 && getTask() != null){
					logger.error("addParam expressNodeList");
					getTask().addParam("expressNodeList", nodeList);
				}
			}else{
				try {
					logger.error("查找国外的新的物流节点");
					WebElement seeDetailsLink = driver.findElement(By.cssSelector(".milestone-primaryMessage"));
					String nodeStr = seeDetailsLink.getText();
					List<ExpressNode> nodeList = new ArrayList<ExpressNode>();	
					nodeStr = nodeStr.trim();
					ExpressNode expressNode = new ExpressNode();
					expressNode.setOrderNo(detail.getOrderNo());
					expressNode.setExpressNo(expressNo);
					expressNode.setName(nodeStr);
					if(nodeStr.contains("Delivered")){
						expressNode.setStatus(14);//已签收
					}
					nodeList.add(expressNode);
					if(nodeList.size() > 0 && getTask() != null){
						logger.error("addParam expressNodeList");
						getTask().addParam("expressNodeList", nodeList);
					}
				} catch (Exception e) {
					logger.error("查找国外的新的物流节点出错",e);
				}
				
			}
		}catch(Exception e){
			logger.error("查找国外的物流节点出错",e);
		}
		return status;
	}
	
	public AutoBuyStatus scribeExpress(ExternalOrderDetail detail)
	{
		String productEntityCode = "";
		if(getAsinMap() != null){
			productEntityCode = getAsinMap().get(detail.getId());
		}
		logger.error("productEntityCode = "+productEntityCode);
		if (Utils.isEmpty(productEntityCode)){
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; }
		// 寻找Your Account
		try
		{
			WebElement panelAccount = driver.findElement(By.xpath("//ul[@id='nav-ftr-links']"));
			WebElement account = panelAccount.findElement(By.xpath(".//span[@class='nav-ftr-text' and contains(text(),'Your Account')]"));
			Utils.sleep(1500);
			account.click();
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Your Account");
			try{
				WebElement avatar = driver.findElement(By.xpath("//a[@id='nav-button-avatar']"));
				Utils.sleep(1500);
				avatar.click();
			}catch(Exception ee){
				logger.error("找不到主页上面的Avatar");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}
		WebDriverWait wait = new WebDriverWait(driver, 45);

		// 寻找Your orders
		try
		{
			WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'Your orders')]")));
			Utils.sleep(1500);
			order.click();
		}
		catch (Exception e)
		{
			logger.error("找不到Your orders");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}

		try
		{
			// 寻找Search all orders showSearchBar
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='showSearchBar']")));
			
			Utils.sleep(1500);
//			WebElement search = 
//			search.click();
//
//			// 寻找订单输入框
//			Utils.sleep(3000);
//			WebElement orderInput = driver.findElement(By.xpath("//input[@id='searchOrdersInput']"));
//			Utils.sleep(1500);
//			orderInput.sendKeys(mallOrderNo);
//			Utils.sleep(2000);
//			orderInput.sendKeys(Keys.ENTER);
			
//			List<WebElement> orderLists = driver.findElements(By.cssSelector("#ordersContainer .a-section a"));
//			for(WebElement w:orderLists){
//				if(w.getAttribute("href").contains(mallOrderNo)){
//					w.click();
//					break;
//				}
//			}
			//Utils.sleep(1500);
		}
		catch (Exception e)
		{
			logger.error("查找订单错误");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		

		// 等待Filter orders不可见
//		try
//		{
//			boolean isVisbile = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@id='searchOrdersInput']")));
//			logger.error("isVisbile = " + isVisbile);
//			Utils.sleep(2000);
//		}
//		catch (Exception e)
//		{
//			logger.error("等待Filter orders不可见 出异常", e);
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
		driver.get("https://www.amazon.com/gp/your-account/ship-track/ref=oh_aui_i_sh_post_o0?ie=UTF8&itemId=jmlipsjtprpwun&orderId="+mallOrderNo);
		Utils.sleep(5000);
		//对比asinCode
		boolean mark = true;
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
			List<WebElement> ll = driver.findElements(By.cssSelector(".a-section .a-box-group"));
			if(ll != null && ll.size() > 0){
				for(WebElement w : ll){
					try {
						List<WebElement> ww = w.findElements(By.cssSelector(".a-link-normal"));
						for(WebElement pw:ww){
							String productLink = pw.getAttribute("href");
							if(productLink != null && productLink.contains(productEntityCode)){
								mark = false;
								logger.error("ll productEntityCode="+productEntityCode);
								// 被砍单
								try{
									WebElement canceled = w.findElement(By.xpath("/span[@class='a-size-small a-color-secondary' and contains(text(),'Cancelled')]"));
									if (canceled != null){
										logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
									}
								}
								catch (NoSuchElementException e){}
								try{
									List<WebElement> tracks = w.findElements(By.cssSelector("a.a-touch-link"));
									for(WebElement tra:tracks){
										if(tra.getText().contains("Track")){
											tra.click();
											break;
										}
									}
								}catch(Exception e1){
									logger.error("View tracking details 出错1");
								}
								
//								try {
//									WebElement seeShip = w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track package')]"));
//									if(seeShip != null){
//										seeShip.click();
//										break;
//									}
//								} catch (Exception e) {
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'View order details')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错");
//									}
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track shipment')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错1");
//									}
//								}
								break;
							}
							
						}
					} catch (Exception e) {
					}
				}
			}else{
				logger.debug("--->没找到订单:"+mallOrderNo);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("对比asinCode出现异常,商城还没发货",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//等待物流页面加载完成
//		try{
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
//		}catch(Exception e){
//			logger.debug("--->等待物流页面加载完成出错",e);
//			try{
//				List<WebElement> tracks = driver.findElements(By.cssSelector(".a-section .a-box-group a.a-touch-link"));
//				for(WebElement tra:tracks){
//					if(tra.getText().contains("Track")){
//						tra.click();
//						break;
//					}
//				}
//			}catch(Exception e1){
//				logger.error("View tracking details 出错1");
//			}
//		}
		
		if(mark){
			logger.debug("--->没找到订单:"+mallOrderNo);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		//等待物流页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
		}catch(Exception e){
			logger.debug("--->等待物流页面加载完成出错");
			try {
				WebElement trackNum = driver.findElement(By.cssSelector("#carrierRelatedInfo-container .carrierRelatedInfo-trackingId-text"));
				WebElement trackingCompany = driver.findElement(By.cssSelector("#carrierRelatedInfo-container h1"));
				String expressNo = trackNum.getText().replace("Tracking ID", "").trim();
				
				String expressCompany = trackingCompany.getText().replace("Shipped with", "").trim();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
				logger.error("expressCompany = " + expressCompany);
				logger.error("expressNo = " + expressNo);
				return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			} catch (Exception e2) {
				logger.debug("对比asinCode出现异常,商城还没发货",e2);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
		}
		
		//查找物流
		String expressNo = detail.getExpressNo();
		AutoBuyStatus status = null;
		if(detail.getStatus() != 100){
			try
			{
				Utils.sleep(1000);
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-box-inner a-padding-medium']"));
				if (elements != null && elements.size() > 0)
				{
					for (WebElement e : elements)
					{
						String aStr = "Carrier";
						String bStr = "Tracking #";
						String str = e.getText();
						int a = str.indexOf(aStr);
						int b = str.indexOf(bStr);
						if (a != -1 && b != -1)
						{
							String expressCompany = str.substring(a + aStr.length(), b).trim();
							expressNo = str.substring(b + bStr.length()).trim();
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							logger.error("expressCompany = " + expressCompany);
							logger.error("expressNo = " + expressNo);
							status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}
					}
				}
				logger.error("该订单还没发货,没产生物流单号");
				if(status != AutoBuyStatus.AUTO_SCRIBE_SUCCESS){
					status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
				status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}else{
			status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
		}
		
		return status;
	}
	
	public AutoBuyStatus scribeExpress(BrushOrderDetail detail)
	{
		String productEntityCode = "";
		if(getAsinMap() != null){
			productEntityCode = getAsinMap().get(detail.getProductEntityId());
		}
		logger.error("productEntityCode = "+productEntityCode);
		if (Utils.isEmpty(productEntityCode)){
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		if(detail.getExpressStatus()!=null && (detail.getExpressStatus()==5 || detail.getExpressStatus()==6)){
			if(detail.getExpressStatus()==5){
				return feedBack(detail);
			}else{
				return review(detail);
			}
		}
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; }
		// 寻找Your Account
		try
		{
			WebElement panelAccount = driver.findElement(By.xpath("//ul[@id='nav-ftr-links']"));
			WebElement account = panelAccount.findElement(By.xpath(".//span[@class='nav-ftr-text' and contains(text(),'Your Account')]"));
			Utils.sleep(1500);
			account.click();
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Your Account");
			try{
				WebElement avatar = driver.findElement(By.xpath("//a[@id='nav-button-avatar']"));
				Utils.sleep(1500);
				avatar.click();
			}catch(Exception ee){
				logger.error("找不到主页上面的Avatar");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}
		WebDriverWait wait = new WebDriverWait(driver, 45);

		// 寻找Your orders
		try
		{
			WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'Your orders')]")));
			Utils.sleep(1500);
			order.click();
		}
		catch (Exception e)
		{
			logger.error("找不到Your orders");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}

		try
		{
			// 寻找Search all orders showSearchBar
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='showSearchBar']")));
			
			Utils.sleep(1500);
//			WebElement search = 
//			search.click();
//
//			// 寻找订单输入框
//			Utils.sleep(3000);
//			WebElement orderInput = driver.findElement(By.xpath("//input[@id='searchOrdersInput']"));
//			Utils.sleep(1500);
//			orderInput.sendKeys(mallOrderNo);
//			Utils.sleep(2000);
//			orderInput.sendKeys(Keys.ENTER);
			
//			List<WebElement> orderLists = driver.findElements(By.cssSelector("#ordersContainer .a-section a"));
//			for(WebElement w:orderLists){
//				if(w.getAttribute("href").contains(mallOrderNo)){
//					w.click();
//					break;
//				}
//			}
			//Utils.sleep(1500);
		}
		catch (Exception e)
		{
			logger.error("查找订单错误");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		

		// 等待Filter orders不可见
//		try
//		{
//			boolean isVisbile = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@id='searchOrdersInput']")));
//			logger.error("isVisbile = " + isVisbile);
//			Utils.sleep(2000);
//		}
//		catch (Exception e)
//		{
//			logger.error("等待Filter orders不可见 出异常", e);
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
		driver.get("https://www.amazon.com/gp/your-account/ship-track/ref=oh_aui_i_sh_post_o0?ie=UTF8&itemId=jmlipsjtprpwun&orderId="+mallOrderNo);
		Utils.sleep(5000);
		//对比asinCode
		boolean mark = true;
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
			List<WebElement> ll = driver.findElements(By.cssSelector(".a-section .a-box-group"));
			if(ll != null && ll.size() > 0){
				for(WebElement w : ll){
					try {
						List<WebElement> ww = w.findElements(By.cssSelector(".a-link-normal"));
						for(WebElement pw:ww){
							String productLink = pw.getAttribute("href");
							if(productLink != null && productLink.contains(productEntityCode)){
								mark = false;
								logger.error("ll productEntityCode="+productEntityCode);
								// 被砍单
								try{
									WebElement canceled = w.findElement(By.xpath("/span[@class='a-size-small a-color-secondary' and contains(text(),'Cancelled')]"));
									if (canceled != null){
										logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
									}
								}
								catch (NoSuchElementException e){}
								try{
									List<WebElement> tracks = w.findElements(By.cssSelector("a.a-touch-link"));
									for(WebElement tra:tracks){
										if(tra.getText().contains("Track")){
											tra.click();
											break;
										}
									}
								}catch(Exception e1){
									logger.error("View tracking details 出错1");
								}
								
//								try {
//									WebElement seeShip = w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track package')]"));
//									if(seeShip != null){
//										seeShip.click();
//										break;
//									}
//								} catch (Exception e) {
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'View order details')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错");
//									}
//									try{
//										w.findElement(By.xpath("/div[@class='a-box-inner' and contains(text(),'Track shipment')]")).click();
//									}catch(Exception e1){
//										logger.error("View tracking details 出错1");
//									}
//								}
								break;
							}
							
						}
					} catch (Exception e) {
					}
				}
			}else{
				logger.debug("--->没找到订单:"+mallOrderNo);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("对比asinCode出现异常,商城还没发货",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		if(mark){
			logger.debug("--->没找到订单:"+mallOrderNo);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		AutoBuyStatus status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
		//等待物流页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
		}catch(Exception e){
			logger.debug("--->等待物流页面加载完成出错",e);
			try {
				WebElement trackNum = driver.findElement(By.cssSelector("#carrierRelatedInfo-container .carrierRelatedInfo-trackingId-text"));
				WebElement trackingCompany = driver.findElement(By.cssSelector("#carrierRelatedInfo-container h1"));
				String expressNo = trackNum.getText().replace("Tracking ID", "").trim();
				
				String expressCompany = trackingCompany.getText().replace("Shipped with", "").trim();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
				logger.error("expressCompany = " + expressCompany);
				logger.error("expressNo = " + expressNo);
				status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			} catch (Exception e2) {
				logger.debug("对比asinCode出现异常,商城还没发货",e2);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
		}
		
		//查找物流
		String expressNo = detail.getExpressNo();
		
		if(detail.getStatus() != 100 && !status.equals(AutoBuyStatus.AUTO_SCRIBE_SUCCESS)){
			try
			{
				Utils.sleep(1000);
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-box-inner a-padding-medium']"));
				if (elements != null && elements.size() > 0)
				{
					for (WebElement e : elements)
					{
						String aStr = "Carrier";
						String bStr = "Tracking #";
						String str = e.getText();
						int a = str.indexOf(aStr);
						int b = str.indexOf(bStr);
						if (a != -1 && b != -1)
						{
							String expressCompany = str.substring(a + aStr.length(), b).trim();
							expressNo = str.substring(b + bStr.length()).trim();
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							logger.error("expressCompany = " + expressCompany);
							logger.error("expressNo = " + expressNo);
							status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}
					}
				}
				logger.error("该订单还没发货,没产生物流单号");
				if(status != AutoBuyStatus.AUTO_SCRIBE_SUCCESS){
					status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
				status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		}else{
			status = AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
		}
		
		
		try{
			driver.findElement(By.xpath("//div[contains(text(),'View tracking details')]")).click();
		}catch(Exception e){
			logger.error("View tracking details 出错",e);
		}
		Utils.sleep(5000);
		
		
		
		//查找国外的物流节点
		try{
			List<WebElement> panelList = driver.findElements(By.xpath("//div[@class='a-box-group a-spacing-mini']"));
			if(panelList != null && panelList.size() > 0){
				String dateStr = "";
				List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
				for(WebElement panel : panelList){
					List<WebElement> list = panel.findElements(By.cssSelector("div.a-box"));
					if(list != null && list.size() > 0){
						for(WebElement w : list){
							try{
								String cssClass = w.getAttribute("class");
								if(StringUtil.isNotEmpty(cssClass) ){
									if(cssClass.contains("a-color-alternate-background")){//日期条
										WebElement wDate = w.findElement(By.cssSelector("span.a-text-bold"));
										String str = wDate.getText();
										if(StringUtil.isNotEmpty(str)){
											String split = ", ";
											int index = str.indexOf(split);
											if(index != -1){
												dateStr = str.substring(index+split.length()).trim();
												logger.error("dateStr = "+dateStr);
											}
										}
									}else{
										WebElement wTime = w.findElement(By.cssSelector("div>div>div.a-column.a-span3.ship-track-grid-fixed-column"));
										String timeStr = wTime.getText();
										WebElement node = w.findElement(By.xpath(".//div[@class='a-column a-span9 ship-track-grid-responsive-column a-span-last']"));
										String nodeStr = node.getText();
										Date nodeDate = getDate(dateStr, timeStr);
										
										if(nodeDate != null && StringUtil.isNotEmpty(nodeStr) && StringUtil.isNotEmpty(expressNo)){
											nodeStr = nodeStr.trim();
											ExpressNode expressNode = new ExpressNode();
											expressNode.setOrderNo(detail.getOrderNo());
											expressNode.setExpressNo(expressNo);
											expressNode.setName(nodeStr);
											expressNode.setOccurTime(nodeDate);
											if(nodeStr.contains("signed by") || nodeStr.contains("was delivered") || nodeStr.contains("Out for delivery") || nodeStr.contains("handed off directly")){
												expressNode.setStatus(14);//已签收
											}else{
												expressNode.setStatus(3);
											}
											nodeList.add(expressNode);
										}
									}
								}
							}catch(Exception e){
//								logger.error("寻找节点出错",e);
							}
						}
					}
				}
				
				if(nodeList.size() > 0 && getTask() != null){
					logger.error("addParam expressNodeList");
					getTask().addParam("expressNodeList", nodeList);
				}
			}else{
				try {
					logger.error("查找国外的新的物流节点");
					WebElement seeDetailsLink = driver.findElement(By.cssSelector(".milestone-primaryMessage"));
					String nodeStr = seeDetailsLink.getText();
					List<ExpressNode> nodeList = new ArrayList<ExpressNode>();	
					nodeStr = nodeStr.trim();
					ExpressNode expressNode = new ExpressNode();
					expressNode.setOrderNo(detail.getOrderNo());
					expressNode.setExpressNo(expressNo);
					expressNode.setName(nodeStr);
					if(nodeStr.contains("Delivered")){
						expressNode.setStatus(14);//已签收
					}
					nodeList.add(expressNode);
					if(nodeList.size() > 0 && getTask() != null){
						logger.error("addParam expressNodeList");
						getTask().addParam("expressNodeList", nodeList);
					}
				} catch (Exception e) {
					logger.error("查找国外的新的物流节点出错",e);
				}
				
			}
		}catch(Exception e){
			logger.error("查找国外的物流节点出错",e);
		}
		return status;
	}

	@Override
	public String redeemGiftCard(List<GiftCard> list)
	{
		logger.info("--->开始充值礼品卡");
		String accountBalance = "";
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try
		{	
			WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-ftr")));
			WebElement yourAccount = panel.findElement(By.xpath("//li[@class='nav-li']/a[@class='nav-a']/span[@class='nav-ftr-text' and contains(text(),'Your Account')]"));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Your Account]");
			yourAccount.click();
		}
		catch (Exception e)
		{
			logger.error("--->找不到[Your Account]");
			try{
				driver.get("https://www.amazon.com/gp/aw/ya/ref=navm_hdr_profile");
//				WebElement avatar = driver.findElement(By.xpath("//a[@id='nav-button-avatar']"));
//				Utils.sleep(1500);
//				avatar.click();
			}catch(Exception ee){
				logger.error("找不到主页上面的Avatar");
			}
		}
		
		try
		{
			WebElement manageBalance = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'Manage gift card balance')]")));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Manage gift card balance]");
			manageBalance.click();
		}
		catch (Exception e)
		{
			try {
				driver.get("https://www.amazon.com/balance?ref_=ya_mb_asv_b_m");
			} catch (Exception e2) {
			}
			
			
			logger.error("--->找不到[Manage gift card balance]");
		}
		
		try
		{
			WebElement reddem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Redeem a Gift Card')]")));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Redeem a Gift Card]");
			reddem.click();
		}
		catch (Exception e)
		{
			logger.error("--->找不到[Redeem a Gift Card]");
			
			try
			{
				WebElement giftCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@name='redeemGiftCard']")));
				TimeUnit.SECONDS.sleep(2);
				logger.debug("--->找到[redeemGiftCard]");
				giftCard.click();
			}
			catch (Exception ee)
			{
				logger.error("--->找不到[redeemGiftCard]");
			}
		}
		
		for (int i = 0; i < list.size(); i++) {
			GiftCard card = list.get(i);
			String cardNo = card.getSecurityCode();
			logger.debug("开始充值第"+(i+1)+"张礼品卡  卡号:"+cardNo+"  金额："+card.getBalance());

			try
			{
				WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='gc-redemption-input']")));
				TimeUnit.SECONDS.sleep(2);
				input.clear();
				TimeUnit.SECONDS.sleep(2);
				input.sendKeys(cardNo);
				TimeUnit.SECONDS.sleep(2);
				driver.findElement(By.id("gc-redemption-apply-announce")).click();
			}
			catch (Exception e)
			{
				logger.error("--->充值失败",e);
				card.setIsSuspect("yes");
				continue;
			}
			
			
			//是否已经充值过
			try{
				TimeUnit.SECONDS.sleep(5);
				driver.findElement(By.xpath("//h4[@class='a-alert-heading' and contains(text(),'Already redeemed')]"));
				logger.error("--->充值卡已经使用:" + cardNo);
				card.setIsSuspect("yes");
				continue;
			}
			catch (Exception e){}
			
			
			boolean isRechargeSuccess = false;
			try
			{
				TimeUnit.SECONDS.sleep(5);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertRedemptionSuccess")));
				isRechargeSuccess = true;
			}catch(Exception e){}
			
			
			if(isRechargeSuccess){
				logger.error("--->充值卡:"+cardNo + "充值成功");
				card.setIsSuspect("no");
				
				//获取充值金额
				try{
					WebElement recharge = driver.findElement(By.cssSelector("p#gc-amount-redeemed"));
					if(recharge != null && StringUtil.isNotEmpty(recharge.getText())){
						String amount = recharge.getText().trim();
						amount = amount.replace("$", "");
						card.setRealBalance(amount);
					}
				}catch(Exception e){
					logger.debug("获取充值金额",e);
				}
				
				//获取账号余额
				try{
					WebElement recharge = driver.findElement(By.cssSelector("p#gc-new-balance"));
					if(recharge != null && StringUtil.isNotEmpty(recharge.getText())){
						String amount = recharge.getText().trim();
						amount = amount.replace("$", "");
						accountBalance = amount;
					}
				}catch(Exception e){
					logger.debug("获取账号余额",e);
				}
			
				//继续充值
				try {
					WebElement another = driver.findElement(By.cssSelector("a#gc-mobile-redeem-another-card-touch-link"));
					another.click();
					Utils.sleep(2000);
				} catch (Exception e) {
					logger.debug("继续充值失败",e);
				}
				
			}else{
				logger.error("--->充值卡:"+cardNo + "充值失败");
				
				//是否是出现验证码
				boolean isCode = false;
				try{
					driver.findElement(By.cssSelector("img#gc-captcha-image"));
					logger.error("礼品卡充值出现验证码");
					isCode = true;
					break;
				}catch(Exception e){}
				
				if(!isCode){
					card.setIsSuspect("yes");
				}
			}
		}
		return accountBalance;
	}
	
	public String checkGiftCard(){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.com/balance?ref_=ya_mb_asv_b_m");
		} catch (Exception e) {
			logger.info("--->跳转礼品卡页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-color-success")));
			WebElement gitBalance = driver.findElement(By.cssSelector(".a-color-success"));
			String balanceText = gitBalance.getText().trim();
			logger.info("--->礼品卡余额:"+balanceText);
			balanceText = balanceText.replace("$", "").replace("-", "").replace(",", "").trim();
			return balanceText;
		}catch (Exception e) {
			logger.info("--->礼品卡余额获取失败!");
		}
		return null;
	}
	
	public String checkCard(){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.com/cpe/managepaymentmethods/ref=ya_mb_mpo");
		} catch (Exception e) {
			logger.info("--->跳转account页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pmts-account-tail")));
			List<WebElement> cards = driver.findElements(By.cssSelector(".pmts-account-tail"));
			for(WebElement w:cards){
				String text = w.getText().trim();
				if(text.contains("Visa")){
					logger.info("--->Visa:"+text);
					return text.substring(text.length()-4,text.length());
				}
			}
		}catch (Exception e) {
			logger.info("--->account获取失败!");
		}
		return null;
		
	}
	
	public String checkPrime(OrderAccount account){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.com/gp/aw/primecentral/ref=aw_ya_hp_prime_aui");
		} catch (Exception e) {
			logger.info("--->跳转account页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#primeCentralResponsiveGreetingContentFromMS3")));
			WebElement content = driver.findElement(By.cssSelector("#primeCentralResponsiveGreetingContentFromMS3"));
			if(content.getText().contains("You are no longer an Amazon Prime member")){
				//不是会员
				account.setCardNo("You are no longer an Amazon Prime member");
			}else{
				List<WebElement> rows = content.findElements(By.cssSelector(".a-row"));
				for(WebElement w:rows){
					if(w.getText().contains("Membership")){
						account.setCardNo(w.getText().trim());
						break;
					}
				}
				//续费时间
				WebElement time = driver.findElement(By.id("renewalMessageForPrimeCustomer"));
				String timeText = time.getText();
				account.setSuffixNo(timeText.trim());
			}
		}catch (Exception e) {
			logger.info("--->account获取失败!");
		}
//		try {
//			driver.navigate().to("https://www.amazon.com/a/addresses/ref=mobile_ya_address_book");
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".id-addr-ux-search-item")));
//			List<WebElement> addressLists = driver.findElements(By.cssSelector(".id-addr-ux-search-item"));
//			Map<String,Object> map = new HashMap<String, Object>();
//			Iterator<WebElement> it = addressLists.iterator();
//	        while (it.hasNext()) {
//	        	WebElement w = (WebElement) it.next();
//	        	WebElement address = w.findElement(By.id("address-ui-widgets-FullName"));
//				String key = address.getText().replace(" ", "");
//				logger.info("--->key:"+key);
//				if(!StringUtil.isBlank(key)){
//					if(map.containsKey(key)){
//						logger.info("--->重复:"+key);
//						w.findElement(By.cssSelector("span[id*='address-delete']")).click();
//						Utils.sleep(1000);
//						List<WebElement> deletes = driver.findElements(By.cssSelector("span[id*='deleteAddressModal']"));
//						for(WebElement del:deletes){
//							if(del.isDisplayed() && del.getText().contains("Yes")){
//								del.click();
//								break;
//							}
//						}
//					}else{
//						map.put(key, "1");
//					}
//				}
//	        }
//		} catch (Exception e) {
//			logger.info("--->address获取失败!",e);
//		}
		
//		try {
//			driver.navigate().to("https://www.amazon.com/a/addresses/ref=mobile_ya_address_book");
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".id-addr-ux-search-item")));
//			List<WebElement> addressLists = driver.findElements(By.cssSelector(".id-addr-ux-search-item"));
//			Map<String,Integer> numberMap = new HashMap<String, Integer>();
//			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
//			Map<String,Object> maps = new HashMap<String, Object>();
//			for(WebElement ad:addressLists){
//				WebElement address = ad.findElement(By.id("address-ui-widgets-FullName"));
//				String[] keys = address.getText().split(" ");
//				if(keys.length==2){
//					String key = keys[1];
//					logger.info("--->address:"+address.getText());
//					Map<String,Object> map = new HashMap<String, Object>();
//					map.put(keys[1], keys[0]);
//					list.add(map);
//					if(!StringUtil.isBlank(key)){
//						if(numberMap.containsKey(key)){
//							numberMap.put(key, numberMap.get(key)+1);
//						}else{
//							numberMap.put(key, 1);
//						}
//					}
//				}
//			}
//			for(String i:numberMap.keySet()){
//				if(numberMap.get(i)<4){
//					logger.info("--->4i:"+i);
//					for(Map<String,Object> map :list){
//						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
//							logger.info("--->Di:"+map.get(i));
//							maps.put(i, i);
//						}
//					}
//					
//				}
//				if(numberMap.get(i)<3){
//					logger.info("--->3i:"+i);
//					for(Map<String,Object> map :list){
//						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
//							logger.info("--->Di:"+map.get(i));
//							maps.put(i, i);
//						}
//						if(map.containsKey(i) && map.get(i).toString().endsWith("C")){
//							logger.info("--->Ci:"+map.get(i));
//							maps.put(i, i);
//						}
//					}
//					
//				}
//				if(numberMap.get(i)<2){
//					logger.info("--->2i:"+i);
//					for(Map<String,Object> map :list){
//						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
//							logger.info("--->Di:"+map.get(i));
//							maps.put(i, i);
//						}
//						if(map.containsKey(i) && map.get(i).toString().endsWith("C")){
//							logger.info("--->Ci:"+map.get(i));
//							maps.put(i, i);
//						}
//						if(map.containsKey(i) && map.get(i).toString().endsWith("B")){
//							logger.info("--->Bi:"+map.get(i));
//							maps.put(i, i);
//						}
//					}
//					
//				}
//			}
//			Iterator<WebElement> it = addressLists.iterator();
//	        while (it.hasNext()) {
//	        	WebElement w = (WebElement) it.next();
//	        	WebElement address = w.findElement(By.id("address-ui-widgets-FullName"));
//				String[] keys = address.getText().split(" ");
//				if(keys.length==2){
//					String key = keys[1];
//					logger.info("--->key:"+key);
//					if(!StringUtil.isBlank(key) && maps.containsKey(key)){
//						logger.info("--->删除:"+key);
//						w.findElement(By.cssSelector("span[id*='address-delete']")).click();
//						Utils.sleep(1000);
//						List<WebElement> deletes = driver.findElements(By.cssSelector("span[id*='deleteAddressModal']"));
//						for(WebElement del:deletes){
//							if(del.isDisplayed() && del.getText().contains("Yes")){
//								del.click();
//								break;
//							}
//						}
//					}
//				}
//				
//	        }
//		} catch (Exception e) {
//			logger.info("--->address获取失败!",e);
//			deleteAddress();
//		}
		
		
		return null;
		
	}
	
	private void deleteAddress(){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.com/a/addresses/ref=mobile_ya_address_book");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".id-addr-ux-search-item")));
			List<WebElement> addressLists = driver.findElements(By.cssSelector(".id-addr-ux-search-item"));
			Map<String,Integer> numberMap = new HashMap<String, Integer>();
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			Map<String,Object> maps = new HashMap<String, Object>();
			for(WebElement ad:addressLists){
				WebElement address = ad.findElement(By.id("address-ui-widgets-FullName"));
				String[] keys = address.getText().split(" ");
				if(keys.length==2){
					String key = keys[1];
					logger.info("--->address:"+address.getText());
					Map<String,Object> map = new HashMap<String, Object>();
					map.put(keys[1], keys[0]);
					list.add(map);
					if(!StringUtil.isBlank(key)){
						if(numberMap.containsKey(key)){
							numberMap.put(key, numberMap.get(key)+1);
						}else{
							numberMap.put(key, 1);
						}
					}
				}
			}
			for(String i:numberMap.keySet()){
				if(numberMap.get(i)<4){
					logger.info("--->4i:"+i);
					for(Map<String,Object> map :list){
						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
							logger.info("--->Di:"+map.get(i));
							maps.put(i, i);
						}
					}
					
				}
				if(numberMap.get(i)<3){
					logger.info("--->3i:"+i);
					for(Map<String,Object> map :list){
						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
							logger.info("--->Di:"+map.get(i));
							maps.put(i, i);
						}
						if(map.containsKey(i) && map.get(i).toString().endsWith("C")){
							logger.info("--->Ci:"+map.get(i));
							maps.put(i, i);
						}
					}
					
				}
				if(numberMap.get(i)<2){
					logger.info("--->2i:"+i);
					for(Map<String,Object> map :list){
						if(map.containsKey(i) && map.get(i).toString().endsWith("D")){
							logger.info("--->Di:"+map.get(i));
							maps.put(i, i);
						}
						if(map.containsKey(i) && map.get(i).toString().endsWith("C")){
							logger.info("--->Ci:"+map.get(i));
							maps.put(i, i);
						}
						if(map.containsKey(i) && map.get(i).toString().endsWith("B")){
							logger.info("--->Bi:"+map.get(i));
							maps.put(i, i);
						}
					}
					
				}
			}
			Iterator<WebElement> it = addressLists.iterator();
	        while (it.hasNext()) {
	        	WebElement w = (WebElement) it.next();
	        	WebElement address = w.findElement(By.id("address-ui-widgets-FullName"));
				String[] keys = address.getText().split(" ");
				if(keys.length==2){
					String key = keys[1];
					logger.info("--->key:"+key);
					if(!StringUtil.isBlank(key) && maps.containsKey(key)){
						logger.info("--->删除:"+key);
						w.findElement(By.cssSelector("span[id*='address-delete']")).click();
						Utils.sleep(1000);
						List<WebElement> deletes = driver.findElements(By.cssSelector("span[id*='deleteAddressModal']"));
						for(WebElement del:deletes){
							if(del.isDisplayed() && del.getText().contains("Yes")){
								del.click();
								break;
							}
						}
					}
				}
				
	        }
		} catch (Exception e) {
			logger.info("--->address获取失败!",e);
			deleteAddress();
		}
	}

	public static void main1(String[] args) throws Exception {
		AmazonAutoBuy autoBuy = new AmazonAutoBuy(false);
		autoBuy.login("letvtct@163.com", "tfb001001");
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("114-9894719-8964233");
		detail.setProductEntityId(4999961L);
		detail.setProductSku("[[\"Color\",\"Luggage/Black\"]]");
		Map<String, String> param = new HashMap<>();
		param.put("feedBack", "今天的天气好晴朗！");
		param.put("review", "今天的天气好晴朗！");
		param.put("headline", "今天的天气好晴朗！");
		// autoBuy.review(detail, param);
		//autoBuy.feedBackAndReview(detail);
	}
	
	public static void main(String[] args) throws Exception {
		AmazonAutoBuy autoBuy = new AmazonAutoBuy(true);
		//autoBuy.login("xyz1hh@outlook.com", "tfb001001");
//		Map<Long, String> asinMap = new HashMap<>();
//		asinMap.put(1111L, "B074W66D5J");
//		autoBuy.setAsinMap(asinMap);
//		BrushOrderDetail detail = new BrushOrderDetail();
//		detail.setFeedbackContent("Just what I needed. ");
//		detail.setProductEntityId(1111L);
//		detail.setMallOrderNo("113-7878063-4592256");
//		System.out.println(autoBuy.feedBack(detail));
		//autoBuy.login("tukotu@163.com", "tfb001001");
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("114-9894719-8964233");
//		detail.setProductEntityId(4999961L);
		//detail.setProductSku("[[\"Color\",\"Luggage/Black\"]]");
		Map<String, String> param = new HashMap<>();
		param.put("url", "https://www.amazon.com/dp/B071JM7VTK?psc=1");
		//param.put("sku", "[[\"Color\",\"A\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param.put("sku", "[[\"Color\",\"Navy/Silver/Pink\"],[\"Size\",\"12 B(M) US\"]]");
		param.put("num", "1");
		param.put("productEntityId", "4780644");
		//param.put("sign", "0");
		//param.put("productName","ApudArmis Knee Support Strap for Pain Relief Knee Brace for Patella Tendonitis, Jumpers & Runner, Hiking Basketball - One Size Adjustable, Black");
		//param.put("title","knee brace");
		//param.put("position","30");
		//param.put("keywordUrl", "http://www.amazon.com/dp/B073PQ7H55");
		autoBuy.selectProduct(param);
		// autoBuy.review(detail, param);
		//autoBuy.feedBackAndReview(detail, param);
	}
	
	public static void main0(String[] args) throws Exception{
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///Users/boliu/Documents/server/haitao/haitao-task/haitao-task-client/new/a/b/Track%20Package.htm");
		String expressNo = "1Z9F7Y840308303979";
		//查找国外的物流节点
		try{
			List<WebElement> panelList = driver.findElements(By.xpath("//div[@class='a-box-group a-spacing-mini']"));
			if(panelList != null && panelList.size() > 0){
				String dateStr = "";
				List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
				for(WebElement panel : panelList){
					List<WebElement> list = panel.findElements(By.xpath(".//div"));
					if(list != null && list.size() > 0){
						for(WebElement w : list){
							try{
								String cssClass = w.getAttribute("class");
								if(StringUtil.isNotEmpty(cssClass) ){
									if(cssClass.contains("a-color-alternate-background")){//日期条
										WebElement wDate = w.findElement(By.cssSelector("span.a-text-bold"));
										String str = wDate.getText();
										if(StringUtil.isNotEmpty(str)){
											String split = ", ";
											int index = str.indexOf(split);
											if(index != -1){
												dateStr = str.substring(index+split.length()).trim();
//												System.out.println("dateStr = "+dateStr);
											}
										}
									}else{
										WebElement wTime = w.findElement(By.xpath(".//div/div/div[@class='a-column a-span3 ship-track-grid-fixed-column']"));
										String timeStr = wTime.getText();
										WebElement node = w.findElement(By.xpath(".//div[@class='a-column a-span9 ship-track-grid-responsive-column a-span-last']"));
										String nodeStr = node.getText();
										Date nodeDate = getDate(dateStr, timeStr);
										
										if(nodeDate != null && StringUtil.isNotEmpty(nodeStr) && StringUtil.isNotEmpty(expressNo)){
											nodeStr = nodeStr.trim();
											ExpressNode expressNode = new ExpressNode();
													expressNode.setOrderNo("1702270926450943847134557");
													expressNode.setExpressNo(expressNo);
											expressNode.setName(nodeStr);
											expressNode.setOccurTime(nodeDate);
											if(nodeStr.contains("signed by") || nodeStr.contains("was delivered")){
												expressNode.setStatus(14);//已签收
											}else{
												expressNode.setStatus(3);
											}
											nodeList.add(expressNode);
										}
									}
								}
							}catch(Exception e){
//								e.printStackTrace();
//										logger.error("寻找节点出错",e);
							}
						}
					}
				}
				
//						if(nodeList.size() > 0 && getTask() != null){
//							logger.error("addParam expressNodeList");
//						}
			}
		}catch(Exception e){
//			e.printStackTrace();
		}
		
//		System.out.println(DateUtil.ymdhmsFormat(getDate("Latest update: Tuesday, Feb 28 ", "12:21 PM")));
		
//		AutoBuyStatus status = null;
//		if(status != AutoBuyStatus.AUTO_SCRIBE_SUCCESS){
//			System.out.println("yes");
//		}else{
//			System.out.println("no");
//		}
		
//		AmazonAutoBuy autoBuy = new AmazonAutoBuy(false);
//		
//		try{
//			AutoBuyStatus status = autoBuy.login("kopnux@gmail.com", "boraliu611");
//			status = Utils.switchStatus(status);
//			if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_SUCCESS.equals(status)){
//				RobotOrderDetail detail = new RobotOrderDetail();
//				detail.setProductEntityId(110l);
//				detail.setMallOrderNo("110-110-110");
//				System.out.println("login success");
//				status = autoBuy.scribeExpress(detail);
//				detail.setStatus(status.getValue());
//				if (AutoBuyStatus.AUTO_SCRIBE_SUCCESS.equals(status)){
//					detail.setExpressCompany(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY));
//					detail.setExpressNo(autoBuy.getData().get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO));
//				}
//				else{
//					if (AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY.equals(status) || AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED.equals(status) || AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND.equals(status))
//					{//这些错误不需要重试
//					}
//				}
//			}
//			else
//			{
//				System.out.println("login fail");
//				if (AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH.equals(status)){//这些错误不需要重试
////					break;
//				}
//			}
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
//		finally{
//			autoBuy.logout();
//		}
		
//		FirefoxDriver driver = new FirefoxDriver();	
//		driver.get("file:///Users/boliu/Documents/server/haitao/haitao-task/haitao-task-client/new/Track%20Package.htm");
//		
//		//查找物流节点show more
//		try{
//			driver.findElement(By.xpath("//a[@class='a-expander-header a-declarative a-expander-extend-header']")).click();
//			Utils.sleep(1500);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		
//		
//		try{
//			Utils.sleep(1000);
//			List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-row a-spacing-top-mini a-size-small a-color-tertiary ship-track-grid-subtext']"));
//			if(elements == null || elements.size() == 0){
//				elements = driver.findElements(By.xpath("//div[@class='a-row a-spacing-top-small a-size-small a-color-tertiary']"));
//			}
//			if (elements != null && elements.size() > 0){
//				for (WebElement e : elements){
//					String aStr = "Carrier: ";
//					String bStr = ", Tracking #: ";
//					String str = e.getText();
//					int a = str.indexOf(aStr);
//					int b = str.indexOf(bStr);
//					if (a != -1 && b != -1){
//						String expressCompany = str.substring(a + aStr.length(), b).trim();
//						String expressNo = str.substring(b + bStr.length()).trim();
//						System.out.println("expressCompany = " + expressCompany);
//						System.out.println("expressNo = " + expressNo);
//					}
//				}
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
		
//		String aStr = "Carrier: ";
//		String bStr = ", Tracking #: ";
//		String str = "Carrier: AMZL US, Tracking #: TBA141478335000";
//		int a = str.indexOf(aStr);
//		int b = str.indexOf(bStr);
//		if (a != -1 && b != -1){
//			String expressCompany = str.substring(a + aStr.length(), b).trim();
//			String expressNo = str.substring(b + bStr.length()).trim();
//			System.out.println("expressCompany = "+expressCompany);
//			System.out.println("expressNo = "+expressNo);
//		}
		
//		String dateStr = "Latest update: Monday, Nov 28";
//		String dateStr = "Saturday, Nov 26 ";
//		String split = ", ";
//		int index = dateStr.indexOf(split);
//		if(index != -1){
//			dateStr = dateStr.substring(index+split.length()).trim();
//			System.out.println("dateStr = "+dateStr);
//		}
		
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(new Date());
//		
//		System.out.println("dateStr = "+calendar.get(Calendar.DAY_OF_MONTH));
//		System.out.println("dateStr = "+calendar.get(Calendar.MONTH));
		
//		for(int i=0;i<1000;i++){
//			FirefoxDriver driver = new FirefoxDriver();
//			System.out.println("--->跳转到商品页面");
//			String productUrl = "http://www.amazon.com/dp/B019SLXOJU";
//			System.out.println("--->选择商品");
//			driver.navigate().to(productUrl);
//			//保存验证码
//			WebElement sorry = null ;
//			try{
//				sorry = driver.findElement(By.xpath("//p[@class='a-last' and contains(text(),'Sorry')]"));
//			}catch(Exception e){
//				driver.quit();
//			}
//			
//			if(sorry!=null){
//				WebElement image = driver.findElement(By.xpath("//div[@class='a-row a-text-center']/img"));
//				String imageUrl = image.getAttribute("src") ;
//				URL url = new URL(imageUrl);
//			    // 打开连接
//			    URLConnection con = url.openConnection();
//			    // 输入流
//			    InputStream is = con.getInputStream();
//			    // 1K的数据缓冲
//			    byte[] bs = new byte[1024];
//			    // 读取到的数据长度
//			    int len;
//			    // 输出的文件流
//			    Date date = new Date() ;
//			    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss") ;
//			    String picName = sdf.format(date) ;
//			    OutputStream os = new FileOutputStream("C:\\Users\\leixun\\Desktop\\验证码\\"+picName+".jpg");
//			    // 开始读取
//			    while ((len = is.read(bs)) != -1) {
//			      os.write(bs, 0, len);
//			    }
//			    // 完毕，关闭所有链接
//			    os.close();
//			    is.close();
//			}
//			driver.quit();
//		}
		
	}
	
	public AutoBuyStatus clickProduct(Map<String, String> param){
		WebDriverWait wait = new WebDriverWait(driver, 45);
		for(int i=0;i<3;i++){
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sp_phone_detail")));
				driver.executeScript("(function(){window.scrollBy(0,450);})();");
				Utils.sleep(1000);
				WebElement detail = driver.findElement(By.cssSelector("#sp_phone_detail .a-carousel li a"));
				String href = detail.getAttribute("href");
				if(href.startsWith("http")){
					driver.navigate().to(href);
				}else{
					driver.navigate().to("https://www.amazon.com"+href);
				}
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->加载商品详情页异常");
				try {
					driver.findElement(By.cssSelector("#sims-session .a-link-normal")).click();
				} catch (Exception e2) {
				}
				
				//return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		param.put("signs", "0");
		return selectBrushProduct(param);
	}
	
	
	public AutoBuyStatus selectBrushProduct(Map<String, String> param)
	{	
		String productUrl = (String) param.get("url");
		try {
			driver.navigate().to(productUrl);
		} catch (Exception e) {
		}
		driver.get("https://www.amazon.com");
		Utils.sleep(3000);
		String key = param.get("productName");
		String title = param.get("title");
		int position = Integer.parseInt(param.get("position"));
		String signs = param.get("signs");
		
		String keywordUrl = param.get("keywordUrl");
		//String shopName = param.get("shopName");
		position = 30;
		
		WebDriverWait wait = new WebDriverWait(driver, 45);
		// 等到[登陆]出现

		try
		{
			WebElement productWord = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-search-keywords")));
			productWord.sendKeys(key);
			TimeUnit.SECONDS.sleep(1);
			logger.debug("--->查找"+key);
			productWord.sendKeys(Keys.ENTER);
			Utils.sleep(1500);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到查找", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		boolean mark = false;
		try {
			for(int i=1;i<position+5;i++){
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("results")));
				List<WebElement> results = driver.findElements(By.cssSelector("#resultItems li"));
				for(WebElement w:results){
					WebElement titleWeb = null;
					try {
						titleWeb = w.findElement(By.cssSelector(".sx-title span"));
					} catch (Exception e) {
						continue;
					}
					String smallTitle = titleWeb.getText();
					if(smallTitle.contains("...")){
						smallTitle = smallTitle.replace("...", "");
					}
					logger.error("--->title1="+smallTitle);
					logger.error("--->title2="+title);
					if(!StringUtil.isBlank(smallTitle) && title.startsWith(smallTitle)){
						logger.error("--->title"+titleWeb.getText());
						WebElement ww = w.findElement(By.cssSelector("a"));
						driver.executeScript("var tar=arguments[0];tar.click();", ww);
						mark = true;
						break;
					}
				}
				if(mark){
					break;
				}
				driver.executeScript("(function(){window.scrollBy(0,400);})();");
				Utils.sleep(1500);
				driver.findElement(By.cssSelector("li.a-last a")).click();;
				Utils.sleep(1500);
				
			}
		} catch (Exception e) {
			logger.error("--->点击下一页异常", e);
			try {
				driver.navigate().to(keywordUrl);
			} catch (Exception e1) {
			}
		}
		if(StringUtil.isBlank(signs)){
			logger.error("--->查找clickProduct");
			return clickProduct(param);
		}
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		try
		{
			if (sku != null)
			{
				List<String> skuList = Utils.getSku((String) sku);
				for (int i = 0; i < skuList.size(); i++)
				{
					if (i % 2 == 1)
					{
						boolean mark1 = false;
						logger.error(skuList.get(i - 1) + ":" + skuList.get(i));
						By byPanel = By.xpath("//div[@id='ppd']");
						WebDriverWait wait2 = new WebDriverWait(driver, 30);
						try {
							wait2.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
						} catch (Exception e) {
							mark1 = true;
						}
						
						// 判断是否是连续选择的sku
						if (mark1){
							for(int k = 0;k<3;k++){
								try{
									Utils.sleep(4500);
									List<WebElement> lists = driver.findElements(By.cssSelector("ul>li.a-align-center h4"));
									for(WebElement w:lists){
										for (int j = 0; j < skuList.size(); j++){
											if (j % 2 == 1){
												if(w.getText().equals(skuList.get(j))){
													logger.error("连续选择:"+skuList.get(j));
													w.click();
													break;
												}
											}
										}
											
									}
										
									Utils.sleep(2500);
								}catch(Exception e){
									break;
								}
							}
						}
						
						String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
						// 等待最大的选择面板可见
						WebElement panel = waitForMainPanel(driver);

						// 不需要选择的sku,比如one size
						boolean hasOneSize = false;
						List<WebElement> list = null;
						try
						{
							list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
						}
						catch (NoSuchElementException e)
						{
						}
						if (list != null && list.size() > 0)
						{
							for (WebElement e : list)
							{
								if (e != null)
								{
									String v = e.getText();
									if (!Utils.isEmpty(v))
									{
										if (v.indexOf(skuList.get(i)) != -1)
										{
											hasOneSize = true;
											break;
										}
										v = v.replaceAll(" ", "");
//										if (v.contains(keyStr))
//										{
//											hasOneSize = true;
//											break;
//										}
										String[] ss = keyStr.split("(?<!^)(?=[A-Z])");
										String[] vv = v.split("(?<!^)(?=[A-Z])");
										if (vv != null && vv.length > 0 && !Utils.isEmpty(vv[0]))
										{
											for (String sss : ss)
											{
												if (vv[0].contains(sss))
												{
													hasOneSize = true;
													break;
												}
											}
											if (hasOneSize)
											{
												break;
											}
										}
									}

								}
							}
						}
						
						if (hasOneSize){
							logger.debug("find one key first~~");
							continue;
						}
						
						WebElement keyElement = null;
						
						List<WebElement> list0 = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
						if(list0 != null && list0.size() > 0){
							//判断onesize
							for(WebElement w : list0){
								try{
									w.findElement(By.cssSelector("span.a-declarative"));
								}catch(Exception e){
									String text = w.getText();
								    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
										hasOneSize = true;
										break;
									}
								}
							}
							if(!hasOneSize){
								//精确匹配
								for(WebElement w : list0){
									WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
									if(ww != null && StringUtil.isNotEmpty(ww.getText())){
										String[] ss = ww.getText().split("\n");
										String text = ss[0].replace(":", "");
										if(keyStr.equals(text) || keyStr.equalsIgnoreCase(text)){
											keyElement = w;
											break;
										}
									}
								}
								//模糊匹配
								if(keyElement == null){
									for(WebElement w : list0){
										WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
										if(ww != null && StringUtil.isNotEmpty(ww.getText())){
											String[] ss = ww.getText().split("\n");
											String text = ss[0].replace(":", "");
											if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
												keyElement = w;
												break;
											}
										}
									}
								}
							}
						}
						
			
						
						if (hasOneSize){
							logger.debug("find one key second");
							continue;
						}
						
						//再次寻找 分词匹配
						if (keyElement == null && !Utils.isEmpty(keyStr)){
							List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
							keyElement = findClosedWelement(keyStr, keyList);
						}
						
						if(keyElement == null){
							List<WebElement> l = driver.findElements(By.cssSelector(".a-button-text.a-text-left"));
							if(l != null && l.size() > 0){
								for(WebElement w : l){
									if(w != null){
										String text = w.getText();
										if(StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())){
											keyElement = w;
											break;
										}
									}
									
								}
							}
						}

						if (keyElement == null){
							logger.debug("找不到keyElement  sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
							return AutoBuyStatus.AUTO_SKU_NOT_FIND;
						}
						Utils.sleep(2000);

						if (newStyle)
						{
							try
							{
								keyElement.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
								logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
								return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
							}
							catch (Exception e)
							{

							}
							keyElement.click();
							logger.debug("--->新姿势选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
							newStyle = false;
						}
						else
						{
							logger.debug("--->自动选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
							keyElement.click();
							try
							{
								valueClick(driver, skuList.get(i));
							}
							catch (Exception e)
							{
								logger.debug("找不到valueElement  sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
						
						Utils.sleep(2500);
					}
				}
			}

			// 等待最大的选择面板可见
			waitForMainPanel(driver);
			
			//Currently unavailable 判断这个情况
			try
			{
				WebElement stock = driver.findElement(By.id("availability"));
				stock.findElement(By.xpath(".//span[contains(text(),'Currently unavailable')]"));
				logger.warn("--->该商品已经下架:");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
			catch (Exception e)
			{
			}

			// 是否add-on item凑单商品,不支持购买
			boolean isAddon = false;
			WebElement price = null;
			WebElement addOn = null;
			By ByAddOn = null;
			try{
				ByAddOn = By.xpath(".//i[contains(text(),'Add-on Item')]");
				price = driver.findElement(By.xpath("//table[@id='price']"));
				try
				{
					addOn = price.findElement(ByAddOn);
				}
				catch (NoSuchElementException e)
				{
				}
				if (addOn != null)
				{
					isAddon = true;
				}
			}catch(Exception e){
				logger.warn("--->判断addon出现异常",e);
			}
			
			
			
			if (!isAddon)
			{// 换种样式查找
				WebElement addOnItem = null;
				try
				{
					addOnItem = driver.findElement(By.xpath("//div[@id='addOnItem_feature_div']"));
				}
				catch (NoSuchElementException e)
				{
				}
				if (addOnItem != null)
				{
					try
					{
						addOn = addOnItem.findElement(ByAddOn);
					}
					catch (NoSuchElementException e)
					{
					}
				}
				if (addOn != null)
				{
					isAddon = true;
				}
			}
			if (isAddon)
			{
				logger.debug("这个商品是Add-on item,凑单商品,不支持购买 ");
				return AutoBuyStatus.AUTO_SKU_ADD_ON;
			}
			// 订阅商品
			try
			{
				WebElement oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));// 换种样式查找
				if (oneTimeBuyBox != null)
				{
					logger.debug("这个商品是订阅商品00,选择oneTimePurchase");
					oneTimeBuyBox.click();
					Utils.sleep(2000);
					oneTimeBuyBox = driver.findElement(By.id("oneTimeBuyBox"));
					oneTimeBuyBox.click();
					Utils.sleep(2000);
					waitForNumPanel(driver);
				}
			}
			catch (NoSuchElementException e)
			{
				try
				{
					WebElement selectPanel = driver.findElement(By.xpath("//div[@id='ap-options']"));
					WebElement oneTimePurchase = selectPanel.findElement(By.xpath(".//span[@class='modeTitle a-text-bold' and contains(text(),'One-time Purchase')]"));
					if (oneTimePurchase != null)
					{
						logger.debug("这个商品是订阅商品,选择oneTimePurchase");
						oneTimePurchase.click();
						Utils.sleep(2000);
						waitForNumPanel(driver);
					}
				}
				catch (NoSuchElementException ee)
				{
				}
			}

			// 如果有红包,获取红包
			try
			{
				WebElement coupon = driver.findElementById("oneTimeBuyVpcButton");
				if (coupon.isDisplayed() && coupon.isEnabled())
				{
					logger.debug("--->[1]这个商品有红包优惠,点击领取了");
					TimeUnit.SECONDS.sleep(1);
					coupon.click();
				}
				else
				{
					logger.debug("--->[1]这个商品红包已经领过了");
				}
			}
			catch (Exception e)
			{

			}
			try{
				WebElement coupon = driver.findElement(By.xpath("//div[@id='oneTimeBuyVpcButton']/div/label/input"));
				if(coupon != null && !coupon.isSelected()){
					coupon.click();
					TimeUnit.SECONDS.sleep(1);
				}
			}catch(Exception e){
				logger.debug("--->领红包出错");
			}

			// 获取单价
			try
			{
				WebElement singlePrice = driver.findElement(By.xpath("//span[@id='priceblock_ourprice']"));
				if (singlePrice != null)
				{
					String singlePriceStr = singlePrice.getText();
					if (!Utils.isEmpty(singlePriceStr))
					{
						String productEntityId = param.get("productEntityId");
						logger.error("productEntityId = " + productEntityId);
						int index = singlePriceStr.indexOf("(");
						if(index != -1){
							singlePriceStr = singlePriceStr.substring(0,index).trim();
						}
						// $24.99 21.91 ($0.18 / Count)
						if (singlePriceStr.startsWith("$") && singlePriceStr.length() > 1)
						{
							singlePriceStr = singlePriceStr.substring(1).replace(" ", ".");
							if(singlePriceStr.startsWith(".")){
								singlePriceStr = singlePriceStr.replaceFirst(".", "");								
							}
							logger.debug("singlePriceStr:"+singlePriceStr);
//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr.substring(1));
							if(StringUtil.isNotEmpty(productEntityId)){
								priceMap.put(productEntityId, singlePriceStr);
							}
						}
						else
						{
//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr);
							if(StringUtil.isNotEmpty(productEntityId)){
								priceMap.put(productEntityId, singlePriceStr);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				try
				{
					WebElement pricePanel = driver.findElement(By.cssSelector("table#price td#priceblock_dealprice > span"));
					String priceStr = pricePanel.getText();
					if (!Utils.isEmpty(priceStr))
					{
						logger.error("--->单价:" + priceStr.replace("$", ""));
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
					}
				}
				catch (Exception ep)
				{
					try
					{
						WebElement pricePanel = driver.findElement(By.xpath("//span[@id='product-price']"));
						String priceStr = pricePanel.getText();
						if (!Utils.isEmpty(priceStr))
						{
							logger.error("--->单价:" + priceStr.replace("$", ""));
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
						}
						else
						{
							logger.error("--->单价获取失败");
						}
					}
					catch (Exception ex)
					{
						logger.error("--->获取单价失败");
					}
				}
			}

			try
			{
				WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
				wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
				WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
				String stockNum = availability.getText().toLowerCase();
				logger.info("--->库存状态:" + stockNum);
				if (stockNum.contains("out of stock") || stockNum.contains("in stock on"))
				{
					logger.warn("--->该商品已经下架:");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			}
			catch (Exception e)
			{
			}

			// 选择数量
			try
			{
				if (!productNum.equals("1"))
				{
					Select select = new Select(driver.findElement(By.id("mobileQuantityDropDown")));
					Utils.sleep(2000);
					driver.executeScript("window.scrollBy(0,150);");
					select.selectByValue(productNum);
					Utils.sleep(2000);
					WebElement numBtn = driver.findElement(By.xpath("//span[@class='a-button a-button-dropdown a-button-small']"));
					String txt = numBtn.getText();
					logger.info("--->选择数量结果:" + txt);
					if (!txt.contains(productNum))
					{
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("选择数量失败 pruductNum = " + productNum);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
			
			// 第三方商品,不支持购买
			try
			{
				WebElement third = driver.findElement(By.xpath("//div[@id='merchant-info']"));
				if (third != null)
				{
					String text = third.getText();
					if (!Utils.isEmpty(text))
					{
//						if(text.indexOf(shopName) == -1) {
//							return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
//						}
						if (!(text.indexOf("sold by Amazon") != -1 || text.indexOf("Fulfilled by Amazon") != -1))
						{
							logger.debug("第三方商品支持购买 " );
							getBrushOrderDetail().setIsDirect("yes");
						}
					}

				}
			}
			catch (NoSuchElementException e)
			{

			}
			String productEntityCode = "";
			if(getAsinMap() != null){
				logger.error("getAsinMap pid="+param.get("productEntityId"));
				for (Map.Entry<Long, String> entry : getAsinMap().entrySet()) {  
					  
					logger.error("Key = " + entry.getKey() + ", Value = " + entry.getValue());  
				  
				}  
				productEntityCode = getAsinMap().get(Long.parseLong(param.get("productEntityId")));
			}else{
				logger.error("getAsinMap is null");
			}
			logger.error("productEntityCode = "+productEntityCode);
			if (Utils.isEmpty(productEntityCode)){
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
			//比较url
			if(!driver.getCurrentUrl().contains(productEntityCode)){
				logger.error("urrentUrl() = "+driver.getCurrentUrl());
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}

			// 加购物车按钮
			try{
				driver.findElement(By.id("buybox.addToCart")).click();
			}catch(Exception e){
				logger.debug("寻找购物车按钮异常11112121");
			}
			Utils.sleep(1000);
			try{
				driver.findElement(By.cssSelector("input#add-to-cart-button")).click();
			}catch(Exception e){
				logger.debug("寻找购物车按钮异常");
			}
			
			Utils.sleep(3000);
			
			
			try
			{
				driver.findElement(By.id("no_thanks_button")).click();
			}
			catch (Exception e)
			{
			}
			
			
			
			
			
			Utils.sleep(3000);
			
			try
			{
				driver.findElement(By.xpath("//a[@id='edit-cart-button-announce']")).click();;
			}
			catch (Exception e)
			{
			}
			Utils.sleep(1000);
			
			try
			{
				driver.findElement(By.xpath("//a[@id='a-autoid-0-announce']")).click();;
			}
			catch (Exception e)
			{
			}
			Utils.sleep(1000);
			
			try{
				driver.findElement(By.xpath("//a[contains(text(),'check out')]")).click();;
				Utils.sleep(1000);
			}catch(Exception e){}
			
			try {
				logger.debug("new check out");
				WebElement tt = driver.findElement(By.cssSelector("span.a-color-base a.a-link-normal"));
				String url = tt.getAttribute("herf");
				driver.navigate().to(url);
			} catch (Exception e) {
				logger.error("safss55",e);
			}
			
			try {
				TimeUnit.SECONDS.sleep(3);
				WebElement ad = driver.findElement(By.cssSelector("button[title='Add to Shopping Cart']"));
				ad.click();
				Utils.sleep(1000);
				WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'Added to cart')]"));
				if(tt!=null){
					driver.navigate().to("https://www.amazon.com/gp/aw/c/ref=navm_hdr_cart");
				}
			} catch (Exception e) {
				logger.error("safss",e);
			}
			
			try {
				WebElement gotocart = driver.findElement(By.id("aislesCartNav"));
				gotocart.click();
			} catch (Exception e) {
				logger.error("2222",e);
			}
			
			
			//等待购物车加载完成
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
				try{
					WebElement numText = driver.findElement(By.cssSelector(".a-dropdown-prompt"));
					logger.error("购物车数量为"+numText.getText().trim());
					if(!productNum.equals(numText.getText().trim())){
						logger.error("选择数量失败 pruductNum = " + productNum);
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}catch(Exception e){
					logger.error("等待购物车数量出错");
				}
			}catch(Exception e){
				logger.error("等待购物车加载完成出错,e");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
			
			//判断是否有alert
			try{
				WebElement w = driver.findElement(By.cssSelector("div.a-alert-content"));
				if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("Important messages for items in your Cart")){
					logger.error("购物车页面弹出警告标记");
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
			}catch(Exception e){}
			
			logger.debug("选择sku成功");
			return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
		}
		catch (Exception e)
		{
			logger.error("选择sku出现异常:", e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static boolean getBooleanByRate(double rate) {
		java.util.Random r = new java.util.Random();
		int i = r.nextInt(999) + 1;
		return i <= 1000 * rate;
	}

}
