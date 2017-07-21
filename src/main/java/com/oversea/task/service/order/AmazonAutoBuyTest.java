package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.Gson;
import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.NetUtil;
import com.oversea.task.utils.Utils;

public class AmazonAutoBuyTest extends AutoBuy
{
	public static final String capPath = "http://47.88.7.218:8080/ocr";

	private final Logger logger = Logger.getLogger(getClass());

	public AutoBuyStatus login(String userName, String passWord)
	{
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();

		driver.get("http://www.amazon.com/");

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		// 等到[登陆]出现

		try
		{
//			By bySignIn = By.xpath("//a[contains(text(),'Sign In')]");
			By bySignIn = By.xpath("//a[@id='nav-logobar-greeting']");
			WebElement signIn = driver.findElement(bySignIn);
			logger.debug("--->跳转到登录页面");
			signIn.click();
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		// 等到[输入框]出现
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email")));
			WebElement username = driver.findElement(By.id("ap_email"));
			logger.debug("--->输入账号");
			username.sendKeys(userName);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement password = driver.findElement(By.id("ap_password"));
			logger.debug("--->输入密码");
			password.sendKeys(passWord);
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
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='nav-greeting-name' and contains(text(),'Hello')]")));
		}
		catch (Exception e)
		{
			logger.error("--->登陆失败,开始判断和处理账号异常");
			// todo 处理异常
			try
			{
				boolean newStyle = false;
				WebElement codeDiv = null;
				WebElement capImage = null;
				try
				{
					codeDiv = driver.findElement(By.id("ap_captcha_img"));
				}
				catch (Exception ex)
				{
					capImage = driver.findElement(By.id("auth-captcha-image"));
					newStyle = true;
					logger.error("--->验证码新样式");
				}

				if (!newStyle) capImage = codeDiv.findElement(By.tagName("img"));
				logger.error("--->开始破解验证码");
				String src = capImage.getAttribute("src");
				logger.error("--->src:" + src);
				if (!Utils.isEmpty(src))
				{
					try
					{
						WebElement password = driver.findElement(By.id("ap_password"));
						logger.debug("--->输入密码");
						password.sendKeys(passWord);
					}
					catch (Exception ex)
					{
						logger.error("--->没有找到密码框", ex);
						return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
					}

					HashMap<String, String> param = new HashMap<>();
					param.put("imgSrc", src);
					param.put("from", NetUtil.getExternalIp());

					String cap = Utils.httpPost(capPath, param);
					try
					{
						WebElement capGuess = null;
						if (!newStyle) capGuess = driver.findElement(By.id("ap_captcha_guess"));
						else
						{
							capGuess = driver.findElement(By.id("auth-captcha-guess"));
						}
						logger.debug("--->输入验证码");
						capGuess.sendKeys(cap);
					}
					catch (Exception ex)
					{
						logger.error("--->没有找到验证码框", ex);
						return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
					}

					try
					{
						WebElement btn = driver.findElement(By.id("signInSubmit"));
						logger.debug("--->开始登陆");
						btn.click();

						try
						{
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-ftr-auth")));
							driver.findElement(By.id("nav-ftr-auth"));
							return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
						}
						catch (Exception eo)
						{
							logger.error("--->破解验证码失败");
							return AutoBuyStatus.AUTO_LOGIN_EXP_MEET_VALIDATE_CODE;
						}
					}
					catch (Exception em)
					{
						logger.error("--->没有找到登陆确定按钮", em);
						return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
					}
				}
			}
			catch (Exception ex)
			{
			}

			try
			{
				WebElement el = driver.findElement(By.id("answer_dcq_question_subjective_1"));
				logger.error("--->需要输入zipcode");
				el.sendKeys("");
				return AutoBuyStatus.AUTO_LOGIN_EXP_MEET_ZIPCODE;
			}
			catch (Exception ex)
			{

			}
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		logger.debug("--->登陆成功,开始跳转");
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
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
						TimeUnit.SECONDS.sleep(3);
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
			catch (Exception e)
			{
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

	private static void valueClick(FirefoxDriver driver, String valueStr) throws Exception
	{
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
				driver.executeScript("var tar=arguments[0];var top=tar.offsetTop;window.scrollTo(0,top);", valueElement);
				Utils.sleep(6000);
				valueElement.click();
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
		By byPanel = By.xpath("//div[@id='ppd']");
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
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("productUrl = " + productUrl);
		logger.debug("--->选择商品");
		/*
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
		*/
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
				try
				{
					driver.findElement(By.xpath("//title[contains(text(),'Page Not Found')]"));
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

		//		try
		//		{
		//			driver.navigate().to(productUrl);
		//			driver.executeScript("(function(){window.scrollBy(100,250);})();");
		//			
		//			// 防止返利地址跳不过去,刷新一下
		//			driver.navigate().refresh();
		//			TimeUnit.SECONDS.sleep(5);
		//		}
		//		catch (Exception e)
		//		{
		//			logger.debug("--->打开商品页面失败 = " + productUrl);
		//			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		//		}
		//
		//		try
		//		{
		//			driver.findElement(By.xpath("//b[@class='h1' and contains(text(),'Looking for')]"));
		//			logger.warn("--->该商品已经下架:" + productUrl);
		//			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		//		}
		//		catch (Exception e)
		//		{
		//
		//		}
		//
		//		waitForMainPanel(driver);

//		try
//		{
//			WebElement stock = driver.findElement(By.id("availability"));
//			stock.findElement(By.xpath(".//span[contains(text(),'Currently unavailable')]"));
//			logger.warn("--->该商品已经下架:" + productUrl);
//			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
//		}
//		catch (Exception e)
//		{
//		}

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
						logger.error(skuList.get(i - 1) + ":" + skuList.get(i));

						// 判断是否是连续选择的sku
						boolean isFindNext = false;
						if (i > 1)
						{
							WebDriverWait wait0 = new WebDriverWait(driver, 10);
							try
							{
								wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='a-popover-content-1']")));
								isFindNext = true;
							}
							catch (TimeoutException e)
							{
							}
							
							if(!isFindNext){
								try
								{
									wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-popover-header-secondary']")));
									isFindNext = true;
								}
								catch (TimeoutException e)
								{
								}
							}
						}
						if (isFindNext)
						{
							logger.debug("找到连续选择sku的模式");
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
						else
						{
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
											if (v.contains(keyStr))
											{
												hasOneSize = true;
												break;
											}
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
							
							// 需要选择sku
							String format0 = String.format("//div[@class='a-column a-span9' and contains(text(), '%s')]", keyStr);
							By key = By.xpath(format0);
							WebElement keyElement = null;
							try
							{
								logger.warn("--->[1]try selectYou");
								keyElement = driver.findElement(key);
								if(keyElement != null){
									hasOneSize = false;
								}
							}
							catch (NoSuchElementException e)
							{
								try{
									String format1 = String.format("//div[@class='a-column a-span9' and contains(text(), '%s')]", skuList.get(i - 1));
									keyElement = driver.findElement(By.xpath(format1));
									if(keyElement != null){
										hasOneSize = false;
									}
								}catch(Exception ee){
									logger.warn("--->",ee);
								}
							}
							
							if (hasOneSize)
							{
								logger.debug("find one key");
								continue;
							}
							
							if (keyElement == null)
							{// 重新寻找keyElement
								if (!Utils.isEmpty(keyStr))
								{
									int half = keyStr.length() / 2;
									if (half > 0)
									{
										format0 = String.format("//div[@class='a-column a-span9' and contains(text(), '%s')]", keyStr.substring(0, half));
										key = By.xpath(format0);
										try
										{
											logger.warn("--->[2]try selectYou");
											keyElement = driver.findElement(key);
										}
										catch (NoSuchElementException e)
										{
											try
											{
												logger.warn("--->[3]try selectYou");
												keyElement = driver.findElement(By.xpath("//span[@class='a-declarative' and contains(@data-mobile-twister,'" + "\"label\":\"" + skuList.get(i) + "\"" + "')]/label"));
												newStyle = true;
											}
											catch (Exception ex)
											{
												logger.warn("--->实在找不到keyElement");
											}
										}
									}
								}
							}

							if (keyElement == null && !Utils.isEmpty(keyStr))
							{
								// 再次寻找keyElement
								List<WebElement> listTemp = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
								if (listTemp != null && listTemp.size() > 0)
								{
									for (WebElement e : listTemp)
									{
										if (e != null && !Utils.isEmpty(e.getText()))
										{
											String text = e.getText();
											int index = text.indexOf(":");
											if (index != -1)
											{
												String keyTemp = text.substring(0, index).trim();
												if (!Utils.isEmpty(keyTemp))
												{
													if (keyStr.indexOf(keyTemp) != -1)
													{
														keyElement = e;
														break;
													}
													if (keyTemp.indexOf(keyStr) != -1)
													{
														keyElement = e;
														break;
													}
												}
											}
										}
									}
								}
							}
							//再次寻找 分词匹配
							if (keyElement == null && !Utils.isEmpty(keyStr))
							{
								List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
								keyElement = findClosedWelement(keyStr, keyList);
							}

							if (keyElement == null)
							{
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
						}
						Utils.sleep(1500);
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
				WebElement oneTimeBuyBox = driver.findElement(By.xpath("//div[@id='oneTimeBuyBox']"));// 换种样式查找
				if (oneTimeBuyBox != null)
				{
					logger.debug("这个商品是订阅商品00,选择oneTimePurchase");
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
							logger.error("singleprice = " + singlePriceStr.substring(1));
//							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePriceStr.substring(1));
							if(StringUtil.isNotEmpty(productEntityId)){
								priceMap.put(productEntityId, singlePriceStr.substring(1));
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
//			try
//			{
//				if (Integer.valueOf(productNum) > 1)
//				{
//					WebElement num = driver.findElement(By.xpath("//span[@class='a-button a-button-dropdown a-button-small']"));
//					String numStr = num.getText();
//					if (!Utils.isEmpty(numStr))
//					{
//						logger.error("num = " + numStr + "pruductNum = " + productNum);
//						String[] strs = numStr.split(":");
//						if (strs.length > 1)
//						{
//							if (!productNum.equals(strs[1]))
//							{
//								try
//								{
//									Utils.sleep(2000);
//									num.click();
//									WebDriverWait wait1 = new WebDriverWait(driver, WAIT_TIME);
//									WebElement numSelectPanel = wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@class='a-nostyle a-list-link']")));
//									Utils.sleep(2000);
//									String str0 = String.format("\"stringVal\":\"%s\"", productNum);
//									String str1 = String.format(".//a[contains(@data-value,'%s')]", str0);
//									WebElement item = numSelectPanel.findElement(By.xpath(str1));
//									item.click();
//
//									// 等待最大的选择面板可见
//									waitForMainPanel(driver);
//								}
//								catch (Exception e)
//								{
//									logger.error("选择数量失败 pruductNum = " + productNum);
//									return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
//								}
//							}
//						}
//					}
//
//				}
//			}
//			catch (Exception e)
//			{
//				logger.error("选择数量失败 pruductNum = " + productNum);
//				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
//			}
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
							return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
						}
					}

				}
			}
			catch (NoSuchElementException e)
			{

			}

			// 加购物车按钮
			By byCart = By.xpath("//input[@id='add-to-cart-button' and contains(@name,'submit.add-to-cart')]");
			WebElement cart = null;
			try
			{
				cart = driver.findElement(byCart);
				cart.click();
			}
			catch (Exception e)
			{
				try{
					cart = driver.findElement(By.xpath("//button[contains(text(),'Add to Cart')]"));
					cart.click();
					logger.debug("找到新的加购物车按钮");
				}catch(Exception ee){
					logger.debug("寻找购物车按钮异常");
					return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
				}
			}
			
			try
			{
				driver.findElement(By.id("no_thanks_button")).click();
			}
			catch (Exception e)
			{
			}
			
			
			//等待购物车加载完成
			try{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
			}catch(Exception e){
				logger.error("等待购物车加载完成出错,e");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
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

	AutoBuyStatus selectTargetAddr(String count)
	{
		try
		{
			TimeUnit.SECONDS.sleep(3);
			List<WebElement> addrs = driver.findElements(By.cssSelector("div.address-book-entry"));
			// todo 根据tarAddr选择地址
			if (addrs != null && addrs.size() > 0)
			{
				logger.debug("--->目前共有[" + addrs.size() + "]个可用地址");
				int index = 0;
				try
				{
					index = Integer.valueOf(count);
					int tarAddr = index % 4;

					WebElement cur = addrs.get(tarAddr);
					
					cur.click();
					TimeUnit.SECONDS.sleep(2);
					cur.findElement(By.xpath(".//a[ contains(text(), 'Ship to this address')]")).click();
					
					
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
														if(StringUtil.isNotEmpty(singlePriceStr)){
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
						}
						logger.error("--->statusMap = "+statusMap.size());
						return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
					}catch(Exception e){
						logger.error("--->没有找到不支持的转运地址");
					}
					
					//保存礼品模式
					/*try{
						WebDriverWait wait = new WebDriverWait(driver, 15);
						WebElement gift = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-section save-gift-button-box']")));
						gift.click();
					}catch(Exception e){
						logger.error("--->等待保存礼品模式出错", e);
					}*/
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

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

					if (!Utils.isEmpty(text) && text.indexOf("FREE Two-Day Shipping") != -1)
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

	AutoBuyStatus selectGiftCard(String prePrice)
	{
		try
		{
			logger.error("--->开始选择礼品卡");
			Utils.sleep(2000);
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='select-payments-view']/div[@class='a-container']/div[@id='imb-wrapper']")));
//			WebElement wrapper = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.a-container form.checkout-page-form")));
			TimeUnit.SECONDS.sleep(2);
//			List<WebElement> giftBtns = driver.findElements(By.xpath("//label[@class='existing-gift-card-balance']"));
			List<WebElement> giftBtns = driver.findElements(By.cssSelector("form.checkout-page-form div.a-spacing-base div.a-box div.a-box-inner label.existing-gift-card-balance"));
			if(giftBtns == null || giftBtns.size() <= 0){
				giftBtns = driver.findElements(By.xpath("//label[@class='existing-gift-card-balance a-control-row a-touch-checkbox a-text-normal']"));
			}
			if(giftBtns == null || giftBtns.size() <= 0){
				giftBtns = driver.findElements(By.xpath("//label[@class='existing-gift-card-balance a-control-row a-touch-radio a-text-normal']"));
			}
			
			
			if (giftBtns != null && giftBtns.size() > 0)
			{
				for (WebElement thePanel : giftBtns)
				{
					if (thePanel.isEnabled() && thePanel.isDisplayed())
					{
						WebElement theBtn = thePanel.findElement(By.className("offscreen"));
						if (theBtn.isSelected())
						{
							logger.debug("--->找到礼品卡,已经默认选中");
						}
						else
						{
							logger.debug("--->找到礼品卡,已经手动选中");
							driver.executeScript("arguments[0].click()", theBtn);
						}

						String text = thePanel.getText().replace("$", "").toLowerCase();
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
//									logger.error("--->找不到Continue按钮");
//									return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
									boolean isFind = true;
									try{
										continueBtn = driver.findElement(By.xpath("//input[@id='continueButton']"));
										Utils.sleep(2000);
										continueBtn.click();
									}catch(Exception e0){
										isFind = false;
										logger.debug("--->continueButton 没找到111");
									}
									if(!isFind){
										logger.error("--->找不到Continue按钮111");
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
						break;
					}
					else
					{
						continue;
					}
				}
			}
			else
			{
				try{
					WebElement radio = driver.findElement(By.xpath("//label[@paymentmethodtype='creditCard']/i[@data-a-input-name='paymentMethod']"));
					if(radio != null && radio.isSelected()){
						return AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF;
					}
				}catch(Exception e){
					logger.error("--->查找信用卡出现异常");
				}
				
				try{
					List<WebElement> list = driver.findElements(By.xpath("//label[@for='pm_gc_checkbox']/span/span"));
					for(WebElement w : list){
						if(w != null && w.isDisplayed()){
							logger.error("--->checkGiftCard pm_gc_checkbox");
							return checkGiftCard(w.getText());
						}
					}
				}catch(Exception e){
					logger.error("--->查找giftCard出现异常 pm_gc_checkbox",e);
				}
				
				try{
					List<WebElement> list = driver.findElements(By.xpath("//label[@for='pm_gc_radio']/span/span"));
					for(WebElement w : list){
						if(w != null && w.isDisplayed()){
							logger.error("--->checkGiftCard pm_gc_radio");
							return checkGiftCard(w.getText());
						}
					}
				}catch(Exception e){
					logger.error("--->查找giftCard出现异常 pm_gc_radio",e);
				}
				
				
				logger.error("--->没有找到礼品卡");
				return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
			}
		}
		catch (Exception e)
		{
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

	public AutoBuyStatus pay(Map<String, String> param)
	{
		String tarAddr = param.get("count");
		String myPrice = param.get("my_price");

		if (Utils.isEmpty(myPrice))
		{
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}

		// 设置价格
		logger.error("--->myPrice = " + myPrice);

		logger.debug("默认地址:" + tarAddr);

		AutoBuyStatus status0 = clickCart();
		if (!status0.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) { return AutoBuyStatus.AUTO_CLICK_CART_FAIL; }
		
		//选中礼品模式
		/*try{
			WebElement gift = driver.findElement(By.xpath("//input[@id='sc-buy-box-gift-checkbox']"));
			if(!gift.isSelected()){
				gift.click();
				logger.error("--->选中礼品模式");
			}
			TimeUnit.SECONDS.sleep(2);
		}catch(Exception ee){
			logger.error("--->寻找礼品模式失败,"+ee);
		}*/

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		By by = By.xpath("//button[@class='a-button-text' and contains(text(), 'Proceed to checkout')]");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
		if (goodsInCart != null && goodsInCart.size() > 0)
		{
			logger.debug("--->去付款");
			driver.findElement(by).click();

			WebElement orderSubmit = null;
			By submitBy = By.xpath("//input[@class='a-button-text place-your-order-button' and contains(@value, 'Place your order')]");
			try
			{
				driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
				orderSubmit = driver.findElement(submitBy);
				logger.error("--->找到:Place your order");
			}
			catch (Exception e)
			{
				logger.error("--->未找到:Place your order");
			}

			// 以下付款方式只针对 GIFTCARD
			if (orderSubmit != null)
			{
				// 直接跳到了 Place your order
				logger.info("--->第一种方案:");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			else
			{
				// 直接跳到了 Select a shipping address
				logger.info("--->第二种方案:");
				AutoBuyStatus status = selectTargetAddr(tarAddr);
				if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS))
				{
					/*status = selectDeliveryOptions();
					if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS))
					{
						status = selectGiftCard(myPrice);
						if (!status.equals(AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS)) { return status; }
					}
					else
					{
						return status;
					}*/
				}
				else
				{
					return status;
				}

				/*try
				{
					TimeUnit.SECONDS.sleep(5);
					By next = By.xpath("//button[@class='pda_button pda_spacing_top_medium' and contains(text(),'Continue placing')]");
					WebElement nextBtn = driver.findElement(next);
					nextBtn.click();
				}
				catch (Exception e)
				{
				}

				try
				{
					wait.until(ExpectedConditions.visibilityOfElementLocated(submitBy));
					orderSubmit = driver.findElement(submitBy);
					logger.error("--->找到:Place your order");
				}
				catch (Exception e)
				{
					try{
						orderSubmit = driver.findElement(By.xpath("//input[@title='Place your order']"));
					}catch(Exception e0){
						orderSubmit = null;
					}
					if(orderSubmit == null){
						logger.error("--->未找到:Place your order");
					}else{
						logger.error("--->重新找到:Place your order");
					}
				}*/
			}

			/*String total = getTotal();
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
				logger.debug("--->完成付款,开始比价[" + myPrice + "," + total + "]");
				total = total.replace("$", "");
				
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(total);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 20.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}

				Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
				if (isPay)
				{
					logger.error("=================");
					logger.error("|---> 点击付款  <---|");
					logger.error("=================");
					orderSubmit.click();
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

			String orderNo = getAmazonOrderNo();
			if (!Utils.isEmpty(orderNo))
			{
				logger.error("--->获取amazon单号成功:\t" + orderNo);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
			}
			else
			{
				logger.error("--->获取amazon单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}*/
		}
		else
		{
			logger.info("--->购物车为空!");
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;

	}

	String getTotal()
	{
		Map<String, String> cache = new HashMap<String, String>();
		String total = null;
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
				value = value.replace("$", "").replace("-", "").trim();
				cache.put(Utils.underlineToCamel(key), value);
			}
			logger.debug("--->付款详情:" + cache);

			total = cache.get("giftCard");
			if (Utils.isEmpty(total))
			{
				logger.error("--->获取不到总价");
			}
		}
		catch (Exception e)
		{
			logger.error("--->获取不到总价");
		}
		return total;
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
				if (txt.indexOf("Review or edit your order") > -1)
				{
					logger.debug("--->找到了查看按钮");
					flag = true;
					div.click();
					break;
				}
			}
		}

		if (flag)
		{
			try
			{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
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
				logger.error("--->" + e.getMessage());
			}
		}
		return orderNo;
	}

	public boolean gotoMainPage()
	{
		try
		{
			Utils.sleep(2000);
			driver.get("http://www.amazon.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='nav-greeting-name' and contains(text(),'Hello')]")));
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
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
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);

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
			WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='showSearchBar']")));
			Utils.sleep(1500);
			search.click();

			// 寻找订单输入框
			Utils.sleep(3000);
			WebElement orderInput = driver.findElement(By.xpath("//input[@id='searchOrdersInput']"));
			Utils.sleep(1500);
			orderInput.sendKeys(mallOrderNo);
			Utils.sleep(2000);
			orderInput.sendKeys(Keys.ENTER);
			Utils.sleep(1500);

		}
		catch (Exception e)
		{
			logger.error("查找订单错误");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}

		// 等待Filter orders不可见
		try
		{
			boolean isVisbile = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@id='searchOrdersInput']")));
			logger.error("isVisbile = " + isVisbile);
			Utils.sleep(2000);
		}
		catch (Exception e)
		{
			logger.error("等待Filter orders不可见 出异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		Utils.sleep(2000);
		
		//对比asinCode
		try{
			List<WebElement> ll = driver.findElements(By.xpath("//div[@class='a-section a-padding-small js-item']"));
			if(ll != null && ll.size() > 0){
				boolean isFind = false;
				for(WebElement w : ll){
					WebElement ww = w.findElement(By.xpath(".//div[@class='item-view-left-col-inner']/a[@class='a-link-normal']"));
					if(ww != null){
						String productLink = ww.getAttribute("href");
						if(productLink != null && productLink.contains(productEntityCode)){
							WebElement seeShip = w.findElement(By.xpath(".//div[@class='a-box-inner' and contains(text(),'Track package')]"));
							if(seeShip != null){
								isFind = true;
								seeShip.click();
								break;
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ship-track-small-vertical-widget']")));
		}catch(Exception e){
			logger.debug("--->等待物流页面加载完成出错",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		
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
						String expressNo = str.substring(b + bStr.length()).trim();
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
						logger.error("expressCompany = " + expressCompany);
						logger.error("expressNo = " + expressNo);
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}
				}
			}
			logger.error("该订单还没发货,没产生物流单号");
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
		}
		catch (Exception e)
		{
			logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		/////////////////////////

		// 订单没找到
//		try
//		{
//			WebElement noResult = driver.findElement(By.xpath("//span[@class='a-text-bold' and contains(text(),'No results found. Please try another search.')]"));
//			if (noResult != null)
//			{
//				logger.error("找不到这个订单 = " + mallOrderNo);
//				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
//			}
//		}
//		catch (NoSuchElementException e)
//		{
//		}
//
//		try
//		{
//			// 被砍单
//			WebElement canceled = driver.findElement(By.xpath("//span[@class='a-size-small a-color-secondary' and contains(text(),'Cancelled')]"));
//			if (canceled != null)
//			{
//				logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
//				return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
//			}
//		}
//		catch (NoSuchElementException e)
//		{
//		}
//
//		// 在查询结果订单里寻找View order details
//		try
//		{
//			WebElement orderDetail0 = driver.findElement(By.xpath("//div[@class='a-box-inner' and contains(text(),'View order details')]"));
//			Utils.sleep(1500);
//			orderDetail0.click();
//		}
//		catch (Exception e)
//		{
//			logger.error("在查询结果里寻找View order details出异常");
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
//
//		// 寻找Track shipment
//		WebElement orderDetail = null;
//		try
//		{
//			orderDetail = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-box-inner' and contains(text(),'Track shipment')]")));
//			Utils.sleep(1500);
//		}
//		catch (Exception e)
//		{
////			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//			logger.error(mallOrderNo + "找不到Track shipment,该订单还没发货,没产生物流单号");
//			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//		}
//		
//		
//		//同个订单有不同的包裹
//		List<WebElement> list = driver.findElements(By.xpath("//div[@class='a-box-group']"));
//		if(list != null && list.size() > 1){
//			logger.error("订单:"+mallOrderNo + "有"+list.size()+"个不同包裹");
//			for(WebElement w : list){
//				boolean isMatch = false;
//				try{
//					List<WebElement> priceList = w.findElements(By.xpath(".//span[@class='a-size-base a-color-base']"));
//					if(priceList != null && priceList.size() > 0){
//						for(WebElement price : priceList){
//							String priceStr = price.getText();
//							if(priceStr != null && priceStr.startsWith("$")){
//								priceStr = priceStr.replaceAll(",", "");
//								priceStr = priceStr.substring(1);
//							}
//							//12.29 ($0.77 / Fl Oz)
//							String singlePrice = detail.getSinglePrice();
//							if(StringUtil.isNotEmpty(singlePrice)){
//								singlePrice = singlePrice.trim();
//								int index = singlePrice.indexOf("(");
//								if(index != -1){
//									singlePrice = singlePrice.substring(0,index).trim();
//								}
//							}
//								
//							if(StringUtil.isNotEmpty(singlePrice) && singlePrice.equals(priceStr)){
//								isMatch = true;
//								logger.error("找到相同价格的子订单 = "+singlePrice);
//								break;
//							}
//						}
//					}
//					
//					
//					
//					
//				}catch(Exception e){
//					logger.error("查找子订单价格出现异常",e);
//				}
//				if(isMatch){
//					try{
//						WebElement subDetail = w.findElement(By.xpath(".//a/div[contains(text(),'Track shipment')]"));
//						if(subDetail != null){
//							orderDetail = subDetail;
//							break;
//						}
//					}catch(Exception e){
//						logger.error(mallOrderNo + "找不到子订单Track shipment,该订单还没发货,没产生物流单号",e);
//						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//					}
//				}
//			}
//		}
//		
//		//点击物流详情
//		try
//		{
//			orderDetail.click();
//			logger.debug("--->找到订单,点击物流详情");
//		}
//		catch (Exception e)
//		{
//			logger.error(mallOrderNo + "找不到Track shipment");
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
//		
//
//		// 在寻找物流公司和单号页面等待View order details可见
//		try
//		{
//			// wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-text-bold' and contains(text(),'Carrier')]")));
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[contains(text(),'Order Details')]")));
//			Utils.sleep(2500);
//			List<WebElement> elements = driver.findElements(By.xpath("//div[@class='a-box-inner a-padding-medium']"));
//			if (elements != null && elements.size() > 0)
//			{
//				for (WebElement e : elements)
//				{
//					String aStr = "Carrier";
//					String bStr = "Tracking #";
//					String str = e.getText();
//					int a = str.indexOf(aStr);
//					int b = str.indexOf(bStr);
//					if (a != -1 && b != -1)
//					{
//						String expressCompany = str.substring(a + aStr.length(), b).trim();
//						String expressNo = str.substring(b + bStr.length()).trim();
//						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
//						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
//						logger.error("expressCompany = " + expressCompany);
//						logger.error("expressNo = " + expressNo);
//						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
//					}
//				}
//			}
//			logger.error("该订单还没发货,没产生物流单号");
//			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//		}
//		catch (Exception e)
//		{
//			logger.error("在寻找物流公司和单号页面等待Order Details可见出现异常");
//			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//		}
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance)
	{
		logger.info("--->开始充值礼品卡:" + cardNo);
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
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
				WebElement avatar = driver.findElement(By.xpath("//a[@id='nav-button-avatar']"));
				Utils.sleep(1500);
				avatar.click();
			}catch(Exception ee){
				logger.error("找不到主页上面的Avatar");
				return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
			}
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[@class='a-box-title' and contains(text(),'Account settings')]")));
			WebElement giftCard = driver.findElement(By.xpath("//li/span/a/div/span[@class='a-size-base' and contains(text(),'gift card')]"));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Manage gift card balance]");
			giftCard.click();
		}
		catch (Exception e)
		{
			logger.error("--->找不到[Manage gift card balance]");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("asv-your-account-balance")));
			WebElement redeem = panel.findElement(By.xpath("//div/a/div/h3[@class='a-text-bold' and contains(text(),'Redeem')]"));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Redeem a Gift Card]");
			redeem.click();
		}
		catch (Exception e)
		{
			logger.error("--->找不到[Redeem a Gift Card]");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gc-current-balance")));
			driver.findElement(By.id("gc-redemption-input")).sendKeys(cardNo);
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.id("gc-redemption-apply-announce")).click();
		}
		catch (Exception e)
		{
			logger.error("--->充值失败");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			TimeUnit.SECONDS.sleep(5);
			driver.findElement(By.xpath("//h4[@class='a-alert-heading' and contains(text(),'Already redeemed')]"));
			logger.error("--->充值卡已经使用:" + cardNo);
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_ALREADY_USE;
		}
		catch (Exception e)
		{

		}

		try
		{
			// 确保有成功的提示框
			logger.error("--->对比充值结果");
			TimeUnit.SECONDS.sleep(5);
			WebElement alertSuccess = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("alertRedemptionSuccess")));
			WebElement successInfo = alertSuccess.findElement(By.xpath("//h4[@class='a-alert-heading']"));
			String text = successInfo.getText().toLowerCase();
			logger.info("--->message:" + text);
			Pattern p = Pattern.compile("(\\d+\\.\\d+)\\s+was\\s+applied\\s+to\\s+your\\s+account");
			Matcher m = p.matcher(text.replace("$", ""));
			String redeemMoney = "0";
			if (m.find())
			{
				redeemMoney = m.group(1);

				BigDecimal tar = new BigDecimal(balance);
				BigDecimal sub = new BigDecimal(redeemMoney.trim());
				if (tar.subtract(sub).intValue() == 0)
				{
					logger.info("--->充值成功");
				}
				else
				{
					logger.error("--->充值成功,但数额不匹配");
					return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAKE_FAIL;
				}
			}
			else
			{
				return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
			}
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			logger.error("--->察看详细信息");
			WebElement ul = driver.findElement(By.id("gc-redeem-balance"));

			WebElement prev = ul.findElement(By.id("gc-previous-balance"));
			WebElement redeem = ul.findElement(By.id("gc-amount-redeemed"));
			WebElement now = ul.findElement(By.id("gc-new-balance"));

			logger.info("--->{'prev':'" + prev.getText() + "','redeem':'" + redeem.getText() + "','now':'" + now.getText() + "'}");
		}
		catch (Exception e)
		{
			logger.error("--->察看详细信息失败,不影响充值结果");
		}

		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_SUCCESS;
	}

	public static void main(String[] args)
	{
		AutoBuy autoBuy = new AmazonAutoBuyTest();
		Map<String, String> map = new HashMap<>();
		map.put("url", "http://www.amazon.com/dp/B00H390IV2");
		map.put("sku", "[[\"Size\",\"12-18 Months\"],[\"*\",\"Baby Aspen Let the Fun Begin Blue Shark Robe, Blue, 12-18 Months\"]]");
		map.put("num","1") ;
		autoBuy.selectProduct(map);
	}
}
