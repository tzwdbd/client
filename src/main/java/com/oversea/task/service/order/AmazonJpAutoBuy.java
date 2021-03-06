package com.oversea.task.service.order;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

public class AmazonJpAutoBuy extends AutoBuy
{
	private Logger logger = Logger.getLogger(getClass());
	private String totalPrice0 = "0";//除去运费的总价
	private String giftBalance = "0";//礼品卡余额
	private static final String end = "配達完了";

	public static final String capPath = "http://47.88.7.218:8080/ocr";
	
	public AmazonJpAutoBuy(){
		super(true);
	}
	
	public AmazonJpAutoBuy(boolean isWap){
		super(isWap);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord)
	{
		if(isWap){
			logger.debug("--->调整浏览器尺寸和位置");
			driver.manage().window().maximize();
			driver.get("http://www.amazon.co.jp/");
			
			WebDriverWait wait = new WebDriverWait(driver, 15);
			try
			{
				By bySignIn = By.xpath("//a[contains(text(),'サインイン')]");
				By bySignIn1 = By.id("nav-logobar-greeting");
				WebElement signIn = null;
				try {
					signIn = driver.findElement(bySignIn);
				} catch (Exception e) {
					signIn = driver.findElement(bySignIn1);
				}
				
				logger.debug("--->跳转到登录页面");
//				String url = signIn.getAttribute("href");
//				driver.navigate().to(url);
				signIn.click();
			}
			catch (Exception e)
			{
				logger.error("--->没有找到登陆按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
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
	
			try
			{
				WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email_login")));
				logger.debug("--->输入账号");
				username.sendKeys(userName);
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
					Utils.sleep(800);
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
				Utils.sleep(1200);
				List<WebElement> passwords = driver.findElements(By.id("ap_password"));
				logger.debug("--->输入密码");
				for(WebElement password:passwords){
					if(password.isDisplayed()){
						password.sendKeys(passWord);
						break;
					}
				}
				Utils.sleep(500);
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
			driver.get("https://www.amazon.co.jp");
			Utils.sleep(3000);
			driver.navigate().to("https://www.amazon.co.jp");
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

	AutoBuyStatus clickCart()
	{
		driver.executeScript("window.scrollTo(0,0);");
		By cartBy = By.id("nav-button-cart");
		WebElement cartBtn = null;
		try
		{
			TimeUnit.SECONDS.sleep(5);
			cartBtn = driver.findElement(cartBy);
			cartBtn.click();
			logger.debug("--->[1]去购物车");
		}
		catch (Exception e)
		{
			try
			{
				cartBy = By.id("navbar-icon-cart");
				cartBtn = driver.findElement(cartBy);
				driver.executeScript("arguments[0].style.display='block';", cartBtn);
				TimeUnit.SECONDS.sleep(5);
				cartBtn.click();
				logger.debug("--->[2]去购物车");
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
		if (!clickCart().equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS))
		{
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

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
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错");
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}
			}
			else
			{
				logger.info("--->购物车为空!");
			}

			try
			{
				logger.debug("--->继续购物,再次跳转");
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				By by = By.xpath("//a[@class='a-button-text' and  contains(text(),'ショッピングを続ける')]");
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				WebElement go = driver.findElement(by);
				go.click();

				TimeUnit.SECONDS.sleep(5);
			}
			catch (Exception e)
			{
				logger.debug("--->再次跳转失败");
			}
		}
		catch (Exception e)
		{
			logger.error("--->选择需要删除的商品出错");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	private WebElement waitForMainPanel()
	{
		// 等待最大的选择面板可见
		By byPanel = By.xpath("//div[@id='ppd']");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
		Utils.sleep(2000);
		return panel;
	}

	private void valueClick(String valueStr) throws Exception
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
				String valStr = String.format("\"label\":\"%s\"", valueStr);
				String format = String.format("//span[@class='a-declarative' and contains(@data-mobile-twister, '%s')]", valStr);
				By value = By.xpath(format);
				WebElement valueElement = wait1.until(ExpectedConditions.visibilityOfElementLocated(value));
				Utils.sleep(2000);
				driver.executeScript("var tar=arguments[0];tar.click();", valueElement);
				//valueElement.findElement(By.cssSelector("a")).click();
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

				if (!isSuccess)
				{
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
		if (!isSuccess)
		{
			throw new Exception();
		}
	}

	boolean newStyle = false;

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param)
	{
		String sign = (String) param.get("sign");
		
		if(!StringUtil.isBlank(sign) && "0".equals(sign)){
			return selectBrushProduct(param);
		}else{
			logger.debug("--->跳转到商品页面");
			String url = param.get("url");
			String orginalUrl = param.get("orginalUrl");
			
			logger.debug("--->url:" + url);
	
			for (int i = 0; i < 3; i++)
			{
				try
				{
					driver.navigate().to(url);
					// 防止返利地址跳不过去,刷新一下
					// driver.navigate().refresh();
					TimeUnit.SECONDS.sleep(3);
	
					try
					{
						WebElement _18 = driver.findElement(By.id("black-curtain-yes"));
						logger.debug("--->我是成人");
						_18.click();
					}
					catch (Exception e)
					{
	
					}
	
					waitForMainPanel();
					break;
				}
				catch (Exception e)
				{
					if (i == 2)
					{
						logger.debug("--->打开商品页面失败 :" + url);
						return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
					}
				}
			}
			//点击off
			try {
				List<WebElement> touchs = driver.findElements(By.cssSelector("a.a-touch-link-noborder"));
				for(WebElement touch:touchs){
					if(touch.isDisplayed() && touch.getText().contains("10%OFF")){
						touch.click();
						Utils.sleep(1500);
						driver.findElement(By.cssSelector(".apl_button")).click();
						Utils.sleep(1000);
						driver.navigate().to(orginalUrl);
						break;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			String productNum = param.get("num");
			Object sku = param.get("sku");
			if (sku != null)
			{
				List<String> skuList = Utils.getSku((String) sku);
				for (int i = 0; i < skuList.size(); i++)
				{
	
					if (i % 2 == 1)
					{
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrValue = skuList.get(i).replace("～", "~");
	
						logger.error("--->" + attrName + ":" + attrValue);
	
						// 判断是否是连续选择的sku
						boolean noStop = false;
						if (i > 1)
						{
							WebDriverWait wait0 = new WebDriverWait(driver, 5);
							try
							{
								wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='a-popover-content-1']")));
								noStop = true;
							}
							catch (Exception e)
							{
							}
							
							if(!noStop){
								try
								{
									wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-popover-header-secondary']")));
									noStop = true;
								}
								catch (TimeoutException e)
								{
								}
							}
						}
	
						if (noStop)
						{
							logger.debug("--->找到连续选择sku的模式");
							try
							{
								valueClick(attrValue);
							}
							catch (Exception e)
							{
								logger.debug("--->找不到对应属性,url= " + url + ",sku = " + (attrName + ":" + attrValue));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
						else
						{
							// 等待最大的选择面板可见	
							WebElement panel = waitForMainPanel();
							logger.debug("--->[1]try onlyYou");
							boolean onlyYou = false;
							List<WebElement> list = null;
							try
							{
								list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
								if (list != null && list.size() > 0)
								{
									for (WebElement e : list)
									{
										if (e != null)
										{
											String v = e.getText();
											if (!Utils.isEmpty(v) && v.indexOf(attrValue) != -1)
											{
												onlyYou = true;
												break;
											}
										}
									}
								}
							}
							catch (Exception e)
							{
							}
							
							//继续寻找onlyYou
							List<WebElement> clicks = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
							for(WebElement w : clicks){
								try{
									w.findElement(By.cssSelector("span.a-declarative"));
								}catch(Exception e){
									String text = w.getText();
								    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
								    	onlyYou = true;
										break;
									}
								}
							}
							
							
	
							if (onlyYou)
							{
								logger.debug("--->[1]使用默认:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
								continue;
							}
	
							logger.debug("--->[1]try selectYou");
							WebElement btnElem = null;
							Utils.sleep(1500);
							try
							{
								switch (attrName)
								{
									case "size":
									case "サイズ":
										btnElem = driver.findElement(By.id("size_name-button-announce"));
										break;
									case "color":
									case "色":
										btnElem = driver.findElement(By.id("color_name-button-announce"));
										break;
									case "unitcount":
									case "セット数":
										btnElem = driver.findElement(By.id("unit_count-button-announce"));
										break;
									case "patternname":
									case " パターン(種類)":
										btnElem = driver.findElement(By.id("pattern_name-button-announce"));
										break;
									case "flavorname":
									case "Flavor":
										btnElem = driver.findElement(By.id("flavor_name-button-announce"));
										break;
									case "style":
									case "スタイル":
										btnElem = driver.findElement(By.id("style_name-button-announce"));
										break;
									case "bandcolor":
										btnElem = driver.findElement(By.id("band_color-button-announce"));
										break;
									case "itemdimensions/length":
										btnElem = driver.findElement(By.id("item_display_length-button"));
										break;
										
									default:
									{
										boolean isSuccess = false;
										try
										{
											List<WebElement> alist = driver.findElements(By.xpath("//span[@class='a-button-text']"));
											if (alist != null && alist.size() > 0)
											{
												loop:
												for (WebElement w : alist)
												{
													try
													{
														List<WebElement> blist = w.findElements(By.xpath(".//div[@class='a-row']/div"));
														if(blist != null && blist.size() > 0){
															for(WebElement ww : blist){
																if (ww != null)
																{
																	if(ww.getText() != null && ww.getText().contains(attrName)){
																		isSuccess = true;
																		btnElem = w;
																		break loop;
																	}
																}
															}
														}
														
													}
													catch (Exception e)
													{
														logger.error(e);
													}
												}
	
											}
										}
										catch (Exception e)
										{
											logger.error(e);
										}
										if (!isSuccess)
										{
											logger.error("-->无法选择：" + attrName);
											return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
										}
									}
								}
							}
							catch (Exception mx)
							{
								try
								{
									logger.warn("--->[2]try selectYou");
									btnElem = panel.findElement(By.xpath("//span[@class='a-declarative' and contains(@data-mobile-twister,'" + "\"label\":\"" + attrValue + "\"" + "')]/label"));
									newStyle = true;
								}
								catch (Exception ex)
								{
									logger.error("-->选择" + attrName + "按钮出错");
								}
							}
	
							if (newStyle)
							{
								try
								{
									btnElem.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
									logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
									return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
								}
								catch (Exception e)
								{
	
								}
								btnElem.click();
								logger.debug("--->新姿势选择:[" + attrName + " = " + attrValue + "]");
								newStyle = false;
							}
							else
							{
								Utils.sleep(1500);
								driver.executeScript("var tar=arguments[0];tar.click();", btnElem);
								//btnElem.click();
	
								try
								{
									valueClick(attrValue);
								}
								catch (Exception e)
								{
									logger.debug("找不到valueElement url= " + url + ",sku = " + attrName + ":" + attrValue);
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						}
						Utils.sleep(1500);
					}
				}
			}
	
			// 等待最大的选择面板可见
			waitForMainPanel();
	
			By addonBy = By.xpath(".//i[contains(text(),'あわせ買い対象商品')]");
			String addon = param.get("addon");
			try
			{
				if(StringUtil.isBlank(addon)){
					WebElement priceTable = driver.findElement(By.xpath("//table[@id='price']"));
					priceTable.findElement(addonBy);
					logger.debug("--->[1]遇到add on商品,不支持购买");
					return AutoBuyStatus.AUTO_SKU_ADD_ON;
				}
			}
			catch (Exception e)
			{
				try
				{
					if(StringUtil.isBlank(addon)){
						WebElement addonPanel = driver.findElement(By.xpath("//div[@id='addOnItem_feature_div']"));
						addonPanel.findElement(addonBy);
						logger.debug("--->[2]遇到add on商品,不支持购买");
						return AutoBuyStatus.AUTO_SKU_ADD_ON;
					}
				}
				catch (Exception ex)
				{
				}
			}
	
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
	
			try
			{
				WebElement oneTimeBuy = driver.findElement(By.id("oneTimeBuyBox"));
				logger.debug("--->[1]这个商品是订阅商品,选择one time purchase 模式");
				WebElement radio = oneTimeBuy.findElement(By.cssSelector("a.a-declarative"));
				TimeUnit.SECONDS.sleep(1);
				radio.click();
				TimeUnit.SECONDS.sleep(1);
				radio.click();
			}
			catch (Exception e)
			{
				try
				{
					WebElement selectPanel = driver.findElement(By.xpath("//div[@id='ap-options']"));
					WebElement oneTimePurchase = selectPanel.findElement(By.xpath(".//span[@class='modeTitle a-text-bold' and contains(text(),'One-time Purchase')]"));
					logger.debug("--->[2]这个商品是订阅商品,选择one time purchase 模式");
					TimeUnit.SECONDS.sleep(1);
					oneTimePurchase.click();
				}
				catch (Exception ee)
				{
				}
			}
	
			// 获取单价
			try
			{
				WebElement singlePrice = driver.findElement(By.id("priceblock_ourprice"));
				String priceStr = singlePrice.getText();
				if (!Utils.isEmpty(priceStr))
				{
					priceStr = priceStr.replace("￥", "").replace(",", "");
					logger.error("--->[1]单价:" + priceStr);
					priceMap.put(param.get("productEntityId"), priceStr);
				}
				else
				{
					logger.error("--->单价获取失败");
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
						priceStr = priceStr.replace("￥", "").replace(",", "");
						logger.error("--->[2]单价:" + priceStr);
						priceMap.put(param.get("productEntityId"), priceStr);
					}
					else
					{
						logger.error("--->单价获取失败");
					}
				}
				catch (Exception ep)
				{
					try
					{
						WebElement pricePanel = driver.findElement(By.id("product-price"));
						String priceStr = pricePanel.getText();
						if (!Utils.isEmpty(priceStr))
						{
							priceStr = priceStr.replace("￥", "").replace(",", "");
							logger.error("--->[3]单价:" + priceStr);
							priceMap.put(param.get("productEntityId"), priceStr);
						}
						else
						{
							logger.error("--->单价获取失败");
						}
					}
					catch (Exception ex)
					{
						try {
							WebElement singlePrice = driver.findElement(By.id("priceblock_dealprice"));
							String priceStr = singlePrice.getText();
							if (!Utils.isEmpty(priceStr))
							{
								priceStr = priceStr.replace("￥", "").replace(",", "");
								logger.error("--->[4]单价:" + priceStr);
								priceMap.put(param.get("productEntityId"), priceStr);
							}else
							{
								logger.error("--->单价获取失败");
							}
						} catch (Exception e2) {
							try {
								WebElement singlePrice = driver.findElement(By.cssSelector("price-large"));
								String priceStr = singlePrice.getText();
								if (!Utils.isEmpty(priceStr))
								{
									priceStr = priceStr.replace("￥", "").replace(",", "");
									logger.error("--->[5]单价:" + priceStr);
									priceMap.put(param.get("productEntityId"), priceStr);
								}else
								{
									logger.error("--->单价获取失败");
								}
							} catch (Exception e3) {
								logger.error("--->获取单价失败");
								
							}
						}
					}
				}
			}
	
			try
			{
				WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
				wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
				WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
				String stockNum = availability.getText();
				logger.info("--->库存状态:" + stockNum);
				if (StringUtil.isNotEmpty(stockNum) && stockNum.contains("通常")){
					if(!(stockNum.contains("3日以内に発送します") || stockNum.contains("5日以内に発送します") 
							|| stockNum.contains("3~4日以内に発送")
							|| stockNum.contains("2~3日以内に発送")
							|| stockNum.contains("1~2日以内に発送")
							|| stockNum.contains("3～4日以内に発送")
							|| stockNum.contains("2～3日以内に発送")
							|| stockNum.contains("1～2日以内に発送"))){
						logger.warn("--->该商品已经下架:" + url);
						return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
					}
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
						if (!(text.indexOf("Amazon.co.jp が販売、") != -1 || text.indexOf("Amazon.co.jp が発送します") != -1))
						{
							logger.debug("第三方商品不支持购买 +url = " + url);
							
							//处理第三方其它商家商品
	//						try {
	//							boolean mark = true;
	//							WebElement newProduct = driver.findElement(By.cssSelector(".a-box-inner .a-text-bold"));
	//							if(newProduct.getText().contains("新品")){
	//								newProduct.click();
	//								logger.debug("新品点击");
	//								WebDriverWait wait = new WebDriverWait(driver, 30);
	//								wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-box-inner .a-container.olpNoPadding")));
	//								List<WebElement> otherProducts = driver.findElements(By.cssSelector(".a-box-inner .a-container.olpNoPadding"));
	//								for(WebElement w:otherProducts){
	//									WebElement dynamicRow = w.findElement(By.cssSelector(".olpDynamicRow"));
	//									if (dynamicRow.getText().contains("Amazon.co.jp")){
	//										mark = false;
	//										w.click();
	//										logger.debug("到达新品页面");
	//										break;
	//									}
	//								}
	//								if(mark){
	//									logger.debug("mark=true");
	//									return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
	//								}
	//							}else{
	//								logger.debug("不包含新品");
	//								return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
	//							}
	//						} catch (Exception e) {
	//							logger.debug("第三方页面异常",e);
	//							return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
	//						}
							boolean b = clickOther();
							if(!b){
								return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
							}
						}
					}
				}
			}
			catch (Exception e)
			{
	
			}
			
			//如果有优惠,点击优惠
			try{
				WebElement coupon = driver.findElement(By.xpath("//span[@id='clickableVpcButton']"));
				if(coupon != null){
					coupon.click();
					TimeUnit.SECONDS.sleep(2);
				}
			}catch(Exception e){
				logger.debug("没有优惠,carry on");
			}
			try{
				WebElement coupon = driver.findElement(By.xpath("//div[@id='oneTimeBuyVpcButton']/div/label/input"));
				if(coupon != null && !coupon.isSelected()){
					coupon.click();
					TimeUnit.SECONDS.sleep(2);
				}
			}catch(Exception e){
				logger.debug("--->领红包出错");
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
	
			try
			{
				TimeUnit.SECONDS.sleep(2);
				driver.findElement(By.id("no_thanks_button")).click();
				logger.debug("--->啊,NO THANKS");
			}
			catch (Exception e){}
			
			try{
	//			driver.findElement(By.xpath("//a[contains(text(),'時間内に確定されない場合は、無効となります。')]")).click();
				TimeUnit.SECONDS.sleep(3);
				driver.findElement(By.xpath("//a[contains(text(),'時間内に確定されない場合は、無効となります。')]")).sendKeys(Keys.ENTER);
				Utils.sleep(1000);
			}catch(Exception e){
				logger.error("time15",e);
			}
			
			try {
				TimeUnit.SECONDS.sleep(3);
				WebElement ad = driver.findElement(By.cssSelector("button[title='カートに入れる']"));
				ad.click();
				Utils.sleep(1000);
				WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'カートに入りました')]"));
				if(tt!=null){
					driver.navigate().to("https://www.amazon.co.jp/gp/aw/c/ref=navm_hdr_cart");
				}
			} catch (Exception e) {
				logger.error("没有カートに入れる",e);
			}
			
	//		try {
	//			WebElement gotocart = driver.findElement(By.id("aislesCartNav"));
	//			gotocart.click();
	//		} catch (Exception e) {
	//			logger.error("2222",e);
	//		}
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
					if(olpName.getText().contains("Amazon.co.jp")){
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
				WebDriverWait wait1 = new WebDriverWait(driver, 45);
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
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
			if(StringUtil.isBlank(addon)){
				try{
					WebElement w = driver.findElement(By.cssSelector(".a-color-alternate-background"));
					if(w != null && StringUtil.isNotEmpty(w.getText()) && w.getText().contains("あわせ買い対象商品")){
						logger.debug("这个商品是Add-on item,凑单商品,不支持购买" );
						return AutoBuyStatus.AUTO_SKU_ADD_ON;
					}
				}catch(Exception e){}
			}
	
			logger.debug("--->选择sku成功");
			return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
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
				if(olpName.getText().contains("Amazon.co.jp")){
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
	
	public AutoBuyStatus selectBrushProduct(Map<String, String> param)
	{	
		String productUrl = (String) param.get("url");
		try {
			driver.navigate().to(productUrl);
		} catch (Exception e) {
		}
		driver.get("https://www.amazon.co.jp");
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
		
		String productNum = param.get("num");
		Object sku = param.get("sku");
		if (sku != null)
		{
			List<String> skuList = Utils.getSku((String) sku);
			for (int i = 0; i < skuList.size(); i++)
			{

				if (i % 2 == 1)
				{
					String attrName = skuList.get(i - 1).toLowerCase();
					String attrValue = skuList.get(i).replace("～", "~");

					logger.error("--->" + attrName + ":" + attrValue);

					// 判断是否是连续选择的sku
					boolean noStop = false;
					if (i > 1)
					{
						WebDriverWait wait0 = new WebDriverWait(driver, 5);
						try
						{
							wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='a-popover-content-1']")));
							noStop = true;
						}
						catch (Exception e)
						{
						}
						
						if(!noStop){
							try
							{
								wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='a-popover-header-secondary']")));
								noStop = true;
							}
							catch (TimeoutException e)
							{
							}
						}
					}

					if (noStop)
					{
						logger.debug("--->找到连续选择sku的模式");
						try
						{
							valueClick(attrValue);
						}
						catch (Exception e)
						{
							logger.debug("--->找不到对应属性,url= ,sku = " + (attrName + ":" + attrValue));
							return AutoBuyStatus.AUTO_SKU_NOT_FIND;
						}
					}
					else
					{
						// 等待最大的选择面板可见	
						WebElement panel = waitForMainPanel();
						logger.debug("--->[1]try onlyYou");
						boolean onlyYou = false;
						List<WebElement> list = null;
						try
						{
							list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
							if (list != null && list.size() > 0)
							{
								for (WebElement e : list)
								{
									if (e != null)
									{
										String v = e.getText();
										if (!Utils.isEmpty(v) && v.indexOf(attrValue) != -1)
										{
											onlyYou = true;
											break;
										}
									}
								}
							}
						}
						catch (Exception e)
						{
						}
						
						//继续寻找onlyYou
						List<WebElement> clicks = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
						for(WebElement w : clicks){
							try{
								w.findElement(By.cssSelector("span.a-declarative"));
							}catch(Exception e){
								String text = w.getText();
							    if(StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))){
							    	onlyYou = true;
									break;
								}
							}
						}
						
						

						if (onlyYou)
						{
							logger.debug("--->[1]使用默认:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
							continue;
						}

						logger.debug("--->[1]try selectYou");
						WebElement btnElem = null;
						Utils.sleep(1500);
						try
						{
							switch (attrName)
							{
								case "size":
								case "サイズ":
									btnElem = driver.findElement(By.id("size_name-button-announce"));
									break;
								case "color":
								case "色":
									btnElem = driver.findElement(By.id("color_name-button-announce"));
									break;
								case "unitcount":
								case "セット数":
									btnElem = driver.findElement(By.id("unit_count-button-announce"));
									break;
								case "patternname":
								case " パターン(種類)":
									btnElem = driver.findElement(By.id("pattern_name-button-announce"));
									break;
								case "flavorname":
								case "Flavor":
									btnElem = driver.findElement(By.id("flavor_name-button-announce"));
									break;
								case "style":
								case "スタイル":
									btnElem = driver.findElement(By.id("style_name-button-announce"));
									break;
								case "bandcolor":
									btnElem = driver.findElement(By.id("band_color-button-announce"));
									break;

								default:
								{
									boolean isSuccess = false;
									try
									{
										List<WebElement> alist = driver.findElements(By.xpath("//span[@class='a-button-text']"));
										if (alist != null && alist.size() > 0)
										{
											loop:
											for (WebElement w : alist)
											{
												try
												{
													List<WebElement> blist = w.findElements(By.xpath(".//div[@class='a-row']/div"));
													if(blist != null && blist.size() > 0){
														for(WebElement ww : blist){
															if (ww != null)
															{
																if(ww.getText() != null && ww.getText().contains(attrName)){
																	isSuccess = true;
																	btnElem = w;
																	break loop;
																}
															}
														}
													}
													
												}
												catch (Exception e)
												{
													logger.error(e);
												}
											}

										}
									}
									catch (Exception e)
									{
										logger.error(e);
									}
									if (!isSuccess)
									{
										logger.error("-->无法选择：" + attrName);
										return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
									}
								}
							}
						}
						catch (Exception mx)
						{
							try
							{
								logger.warn("--->[2]try selectYou");
								btnElem = panel.findElement(By.xpath("//span[@class='a-declarative' and contains(@data-mobile-twister,'" + "\"label\":\"" + attrValue + "\"" + "')]/label"));
								newStyle = true;
							}
							catch (Exception ex)
							{
								logger.error("-->选择" + attrName + "按钮出错");
							}
						}

						if (newStyle)
						{
							try
							{
								btnElem.findElement(By.cssSelector("div.twister-mobile-tiles-swatch-unavailable"));
								logger.debug("--->新姿势选择的目标按钮不可点击,商品已经下架");
								return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
							}
							catch (Exception e)
							{

							}
							btnElem.click();
							logger.debug("--->新姿势选择:[" + attrName + " = " + attrValue + "]");
							newStyle = false;
						}
						else
						{
							Utils.sleep(1500);
							driver.executeScript("var tar=arguments[0];tar.click();", btnElem);
							//btnElem.click();

							try
							{
								valueClick(attrValue);
							}
							catch (Exception e)
							{
								logger.debug("找不到valueElement url= ,sku = " + attrName + ":" + attrValue);
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
					}
					Utils.sleep(1500);
				}
			}
		}

		// 等待最大的选择面板可见
		waitForMainPanel();

		By addonBy = By.xpath(".//i[contains(text(),'あわせ買い対象商品')]");
		try
		{
			WebElement priceTable = driver.findElement(By.xpath("//table[@id='price']"));
			priceTable.findElement(addonBy);
			logger.debug("--->[1]遇到add on商品,不支持购买");
			return AutoBuyStatus.AUTO_SKU_ADD_ON;
		}
		catch (Exception e)
		{
			try
			{
				WebElement addonPanel = driver.findElement(By.xpath("//div[@id='addOnItem_feature_div']"));
				addonPanel.findElement(addonBy);
				logger.debug("--->[2]遇到add on商品,不支持购买");
				return AutoBuyStatus.AUTO_SKU_ADD_ON;
			}
			catch (Exception ex)
			{
			}
		}

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

		try
		{
			WebElement oneTimeBuy = driver.findElement(By.id("oneTimeBuyBox"));
			logger.debug("--->[1]这个商品是订阅商品,选择one time purchase 模式");
			WebElement radio = oneTimeBuy.findElement(By.cssSelector("a.a-declarative"));
			TimeUnit.SECONDS.sleep(1);
			radio.click();
			TimeUnit.SECONDS.sleep(1);
			radio.click();
		}
		catch (Exception e)
		{
			try
			{
				WebElement selectPanel = driver.findElement(By.xpath("//div[@id='ap-options']"));
				WebElement oneTimePurchase = selectPanel.findElement(By.xpath(".//span[@class='modeTitle a-text-bold' and contains(text(),'One-time Purchase')]"));
				logger.debug("--->[2]这个商品是订阅商品,选择one time purchase 模式");
				TimeUnit.SECONDS.sleep(1);
				oneTimePurchase.click();
			}
			catch (Exception ee)
			{
			}
		}

		// 获取单价
		try
		{
			WebElement singlePrice = driver.findElement(By.id("priceblock_ourprice"));
			String priceStr = singlePrice.getText();
			if (!Utils.isEmpty(priceStr))
			{
				priceStr = priceStr.replace("￥", "").replace(",", "");
				logger.error("--->[1]单价:" + priceStr);
				priceMap.put(param.get("productEntityId"), priceStr);
			}
			else
			{
				logger.error("--->单价获取失败");
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
					priceStr = priceStr.replace("￥", "").replace(",", "");
					logger.error("--->[2]单价:" + priceStr);
					priceMap.put(param.get("productEntityId"), priceStr);
				}
				else
				{
					logger.error("--->单价获取失败");
				}
			}
			catch (Exception ep)
			{
				try
				{
					WebElement pricePanel = driver.findElement(By.id("product-price"));
					String priceStr = pricePanel.getText();
					if (!Utils.isEmpty(priceStr))
					{
						priceStr = priceStr.replace("￥", "").replace(",", "");
						logger.error("--->[3]单价:" + priceStr);
						priceMap.put(param.get("productEntityId"), priceStr);
					}
					else
					{
						logger.error("--->单价获取失败");
					}
				}
				catch (Exception ex)
				{
					try {
						WebElement singlePrice = driver.findElement(By.id("priceblock_dealprice"));
						String priceStr = singlePrice.getText();
						if (!Utils.isEmpty(priceStr))
						{
							priceStr = priceStr.replace("￥", "").replace(",", "");
							logger.error("--->[4]单价:" + priceStr);
							priceMap.put(param.get("productEntityId"), priceStr);
						}else
						{
							logger.error("--->单价获取失败");
						}
					} catch (Exception e2) {
						try {
							WebElement singlePrice = driver.findElement(By.cssSelector("price-large"));
							String priceStr = singlePrice.getText();
							if (!Utils.isEmpty(priceStr))
							{
								priceStr = priceStr.replace("￥", "").replace(",", "");
								logger.error("--->[5]单价:" + priceStr);
								priceMap.put(param.get("productEntityId"), priceStr);
							}else
							{
								logger.error("--->单价获取失败");
							}
						} catch (Exception e3) {
							logger.error("--->获取单价失败");
							
						}
					}
				}
			}
		}

		try
		{
			WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
			WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
			String stockNum = availability.getText();
			logger.info("--->库存状态:" + stockNum);
			if (StringUtil.isNotEmpty(stockNum) && stockNum.contains("通常")){
				if(!(stockNum.contains("3日以内に発送します") || stockNum.contains("5日以内に発送します") 
						|| stockNum.contains("3~4日以内に発送")
						|| stockNum.contains("2~3日以内に発送")
						|| stockNum.contains("1~2日以内に発送")
						|| stockNum.contains("3～4日以内に発送")
						|| stockNum.contains("2～3日以内に発送")
						|| stockNum.contains("1～2日以内に発送"))){
					logger.warn("--->该商品已经下架:");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
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
//					if(text.indexOf(shopName) == -1) {
//						return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
//					}
					if (!(text.indexOf("Amazon.co.jp が販売、") != -1 || text.indexOf("Amazon.co.jp が発送します") != -1))
					{
						logger.debug("第三方商品不支持购买 +url = ");
						
						//处理第三方其它商家商品
//						try {
//							boolean mark = true;
//							WebElement newProduct = driver.findElement(By.cssSelector(".a-box-inner .a-text-bold"));
//							if(newProduct.getText().contains("新品")){
//								newProduct.click();
//								logger.debug("新品点击");
//								WebDriverWait wait = new WebDriverWait(driver, 30);
//								wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-box-inner .a-container.olpNoPadding")));
//								List<WebElement> otherProducts = driver.findElements(By.cssSelector(".a-box-inner .a-container.olpNoPadding"));
//								for(WebElement w:otherProducts){
//									WebElement dynamicRow = w.findElement(By.cssSelector(".olpDynamicRow"));
//									if (dynamicRow.getText().contains("Amazon.co.jp")){
//										mark = false;
//										w.click();
//										logger.debug("到达新品页面");
//										break;
//									}
//								}
//								if(mark){
//									logger.debug("mark=true");
//									return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
//								}
//							}else{
//								logger.debug("不包含新品");
//								return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
//							}
//						} catch (Exception e) {
//							logger.debug("第三方页面异常",e);
//							return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
//						}
						return AutoBuyStatus.AUTO_SKU_THIRD_PRODUCT;
					}
				}
			}
		}
		catch (Exception e)
		{

		}
		
		//如果有优惠,点击优惠
		try{
			WebElement coupon = driver.findElement(By.xpath("//span[@id='clickableVpcButton']"));
			if(coupon != null){
				coupon.click();
				TimeUnit.SECONDS.sleep(2);
			}
		}catch(Exception e){
			logger.debug("没有优惠,carry on");
		}
		try{
			WebElement coupon = driver.findElement(By.xpath("//div[@id='oneTimeBuyVpcButton']/div/label/input"));
			if(coupon != null && !coupon.isSelected()){
				coupon.click();
				TimeUnit.SECONDS.sleep(2);
			}
		}catch(Exception e){
			logger.debug("--->领红包出错");
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

		try
		{
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.id("no_thanks_button")).click();
			logger.debug("--->啊,NO THANKS");
		}
		catch (Exception e){}
		
		try{
//			driver.findElement(By.xpath("//a[contains(text(),'時間内に確定されない場合は、無効となります。')]")).click();
			TimeUnit.SECONDS.sleep(3);
			driver.findElement(By.xpath("//a[contains(text(),'時間内に確定されない場合は、無効となります。')]")).sendKeys(Keys.ENTER);
			Utils.sleep(1000);
		}catch(Exception e){
			logger.error("time15",e);
		}
		
		try {
			TimeUnit.SECONDS.sleep(3);
			WebElement ad = driver.findElement(By.cssSelector("button[title='カートに入れる']"));
			ad.click();
			Utils.sleep(1000);
			WebElement tt = driver.findElement(By.xpath("//span[contains(text(),'カートに入りました')]"));
			if(tt!=null){
				driver.navigate().to("https://www.amazon.co.jp/gp/aw/c/ref=navm_hdr_cart");
			}
		} catch (Exception e) {
			logger.error("safss",e);
		}
		
//		try {
//			WebElement gotocart = driver.findElement(By.id("aislesCartNav"));
//			gotocart.click();
//		} catch (Exception e) {
//			logger.error("2222",e);
//		}
		
		
		//等待购物车加载完成
		try{
			WebDriverWait wait1 = new WebDriverWait(driver, 45);
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
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
		

		logger.debug("--->选择sku成功");
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
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
					driver.navigate().to("https://www.amazon.co.jp"+href);
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
			}else{
				name = name+"D  "+userTradeAddress.getName();
			}
		
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enterAddressFullName")));
			List<WebElement> fullNameList = driver.findElements(By.id("enterAddressFullName"));
			for(WebElement w:fullNameList){
				if(w.isDisplayed()){
					w.sendKeys(name);
					Utils.sleep(1500);
					logger.debug("--->输入fullName="+name);
				}
			}
			
			WebElement addressPostalCode = driver.findElement(By.id("enterAddressPostalCode1"));
			addressPostalCode.sendKeys(userTradeAddress.getZip().split("-")[0]);
			Utils.sleep(1500);
			logger.debug("--->输入addressPostalCode="+userTradeAddress.getZip().split("-")[0]);
			WebElement addressPostalCode2 = driver.findElement(By.id("enterAddressPostalCode2"));
			addressPostalCode2.sendKeys(userTradeAddress.getZip().split("-")[1]);
			Utils.sleep(1500);
			logger.debug("--->输入addressPostalCode2="+userTradeAddress.getZip().split("-")[1]);
			
			WebElement addressStateOrRegion = driver.findElement(By.cssSelector("select#enterAddressStateOrRegion"));
			Select select = new Select(addressStateOrRegion);
			select.selectByVisibleText(userTradeAddress.getState());
			Utils.sleep(1500);
			logger.debug("--->输入addressStateOrRegion="+userTradeAddress.getState());
			if(userTradeAddress.getAddress().contains(",")){
				List<WebElement> addressLine1List = driver.findElements(By.id("enterAddressAddressLine1"));
				for(WebElement w:addressLine1List){
					if(w.isDisplayed()){
						w.clear();
						w.sendKeys(userTradeAddress.getCity()+userTradeAddress.getAddress().split(",")[0]);
						Utils.sleep(1500);
						logger.debug("--->输入addressLine1="+userTradeAddress.getCity()+userTradeAddress.getAddress().split(",")[0]);
					}
				}
				List<WebElement> addressLine2List = driver.findElements(By.id("enterAddressAddressLine2"));
				for(WebElement w:addressLine2List){
					if(w.isDisplayed()){
						w.clear();
						w.sendKeys(userTradeAddress.getAddress().split(",")[1]);
						Utils.sleep(1500);
						logger.debug("--->输入addressLine2="+userTradeAddress.getAddress().split(",")[1]);
					}
				}
			}else{
				List<WebElement> addressLine1List = driver.findElements(By.id("enterAddressAddressLine1"));
				for(WebElement w:addressLine1List){
					if(w.isDisplayed()){
						w.clear();
						w.sendKeys(userTradeAddress.getCity()+userTradeAddress.getAddress());
						Utils.sleep(1500);
						logger.debug("--->输入addressLine1="+userTradeAddress.getCity()+userTradeAddress.getAddress());
					}
				}
			}
			
			
			WebElement addressPhoneNumber = driver.findElement(By.id("enterAddressPhoneNumber"));
			addressPhoneNumber.sendKeys(userTradeAddress.getMobile());
			Utils.sleep(1500);
			logger.debug("--->输入addressPhoneNumber="+userTradeAddress.getMobile());
			
			List<WebElement> shipAddressList = driver.findElements(By.cssSelector(".a-button-input.submit-button-with-name"));
			for(WebElement w:shipAddressList){
				if(w.isDisplayed()){
					w.click();
				}
			}	
			
		}catch (Exception e)
		{
			logger.error("--->添加地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
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

	AutoBuyStatus selectTargetAddr(String count,String username,UserTradeAddress userTradeAddress)
	{
		try
		{
			logger.debug("开始选择地址");
			TimeUnit.SECONDS.sleep(3);
			List<WebElement> addrs = driver.findElements(By.cssSelector("div.address-book-entry"));
			// todo 根据tarAddr选择地址
			logger.debug("开始选择地址 addrs");
			if (addrs != null && addrs.size() > 0)
			{
				logger.debug("开始选择地址 addrs is not empty");
				logger.debug("--->目前共有[" + addrs.size() + "]个可用地址");
				List<WebElement> availableAddr = new ArrayList<WebElement>();
				for(WebElement a:addrs){
					if(a.getText().toUpperCase().contains(userTradeAddress.getName())){
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
					logger.info("--->选择第[" + (tarAddr + 1) + "]个地址");
					cur.click();
					TimeUnit.SECONDS.sleep(2);
					try {
						cur.findElement(By.xpath(".//a[ contains(text(), 'この住所を使う')]")).click();
					} catch (Exception e) {
						try {
							cur.findElement(By.xpath(".//a[ contains(text(), 'Ship to this address')]")).click();
						} catch (Exception e2) {
							try {
								cur.findElement(By.xpath(".//a[ contains(text(), 'Deliver to this address')]")).click();
							} catch (Exception e3) {
								cur.findElement(By.xpath(".//a[ contains(text(), 'この住所に届ける')]")).click();
							}
							
						}
						
					}
					
					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				logger.debug("选地址进入新的修改方案");
				addrs = driver.findElements(By.xpath("//i[@data-a-input-name='ignore-addressID']"));
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
					return addAddr(userTradeAddress, username, addrs.size());
				}
				if (availableAddr != null && availableAddr.size() > 0)
				{
					int index = Integer.valueOf(count);
					int tarAddr = index % 4;

					if (tarAddr < availableAddr.size())
					{
						availableAddr.get(tarAddr).click();
						TimeUnit.SECONDS.sleep(2);
						driver.findElement(By.xpath(".//a[contains(text(), 'この住所を使う')]")).click();

						logger.debug("选地址进入新的修改方案选择完毕");
					}

					TimeUnit.SECONDS.sleep(5);
				}
				else
				{
					logger.debug("选地址进入新的修改方案:" + "addrs是空的");
				}
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
			logger.error("--->选择地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
	}

	AutoBuyStatus selectDeliveryOptions()
	{
		try
		{
			Utils.sleep(3000);
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shippingOptionFormId")));
			List<WebElement> opts = form.findElements(By.cssSelector(".a-form-control-group .selectable-box"));
			if (opts != null && opts.size() == 0)
			{
				logger.error("--->重新选择快递项列表");
				opts = form.findElements(By.cssSelector(".ship-speed .fake-label"));
			}

			if (opts != null && opts.size() > 0)
			{
				this.logger.debug("--->目前共有[" + opts.size() + "]种物流可选,使用默认");

				By by = By.xpath("//input[@class='a-button-text' and  contains(@value, '次に進む')]");
				WebElement next = null;
				try
				{
					next = driver.findElement(by);
					this.logger.debug("--->[1]next delivery");
				}
				catch (Exception e)
				{
					by = By.xpath("//input[@class='a-button-input' and  contains(@value, '次に進む')]");
					next = driver.findElement(by);
					this.logger.debug("--->[2]next delivery");
				}
				next.click();
			}
			else
			{
				logger.debug("--->[1]选择物流失败");
				return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
			}
			
//			2中2の発送
			TimeUnit.SECONDS.sleep(2);
			try{
				WebDriverWait wait0 = new WebDriverWait(driver, 6);
				wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), '2中2の発送')]")));
				logger.debug("--->找到选两次的物流");
				
				By by = By.xpath("//input[@class='a-button-text' and  contains(@value, '次に進む')]");
				WebElement next = null;
				try
				{
					next = driver.findElement(by);
					this.logger.debug("--->[1]next delivery");
				}
				catch (Exception e)
				{
					by = By.xpath("//input[@class='a-button-input' and  contains(@value, '次に進む')]");
					next = driver.findElement(by);
					this.logger.debug("--->[2]next delivery");
				}
				next.click();
//				WebElement updateTime = driver.findElement(By.xpath("//span[@class='a-button-text' and  contains(text(), '配達時間を変更')]"));
//				updateTime.click();
			}catch(Exception e){
				logger.debug("--->没有找到选两次的物流",e);
			}
		}
		catch (Exception e)
		{
			logger.debug("--->[2]选择物流失败",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS;

	}

	String totalInGiftCard = "";

	AutoBuyStatus selectGiftCard(String needPay, Boolean isPrime,Set<String> promotionList,Map<String, String> param)
	{
		String payType = param.get("payType");
		logger.debug("--->支付方式为"+payType);
		try
		{
			Utils.sleep(1000);
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("select-payments-view")));
			} catch (Exception e) {
				By by = By.xpath("//input[@class='a-button-text' and  contains(@value, '次に進む')]");
				WebElement next = null;
				try
				{
					next = driver.findElement(by);
					this.logger.debug("--->[3]next delivery");
				}
				catch (Exception e1)
				{
					by = By.xpath("//input[@class='a-button-input' and  contains(@value, '次に進む')]");
					next = driver.findElement(by);
					this.logger.debug("--->[4]next delivery");
				}
				next.click();
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("select-payments-view")));
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
					try{
						WebElement radio = driver.findElement(By.id("pm_300"));
						if(!radio.isSelected()){
							logger.debug("--->礼品卡没有选中,点击选中");
							
							try{
								driver.findElement(By.id("existing-gift-card-promo")).click();
								Utils.sleep(3000);
							}catch(Exception e){
								logger.debug("--->点击选中礼品卡出错",e);
								return AutoBuyStatus.AUTO_PAY_FAIL;
							}
						}
					}catch(Exception e){
						logger.debug("--->查找礼品卡选中按钮出错",e);
					}
				}
			} catch (Exception e) {
				logger.debug("--->查找礼品卡选中按钮出错",e);
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
						try{
							WebElement radio = driver.findElement(By.id("pm_300"));
							if(!radio.isSelected()){
								logger.debug("--->礼品卡没有选中,点击选中");
								
								try{
									driver.findElement(By.id("existing-gift-card-promo")).click();
									Utils.sleep(3000);
								}catch(Exception e){
									logger.debug("--->点击选中礼品卡出错",e);
									return AutoBuyStatus.AUTO_PAY_FAIL;
								}
							}
						}catch(Exception e){
							logger.debug("--->查找礼品卡选中按钮出错",e);
						}
					}
				} catch (Exception e) {
					logger.debug("--->查找礼品卡选中按钮出错",e);
				}
			}
			
			//获取礼品卡余额
			try{
				WebElement w = driver.findElement(By.xpath("//label[@for='pm_300']/span/span"));
				if(StringUtil.isNotEmpty(w.getText())){
					int index = w.getText().indexOf("分");
					if(index != -1){
						String a = w.getText().substring(0,index);
						giftBalance = a.replace("￥", "").replace(",", "").replace("-", "").trim();
						logger.error("giftBalance = "+giftBalance);
						data.put(AutoBuyConst.KEY_AUTO_BUY_GIFTCARD_LEFT, giftBalance);
						float value = sub(totalPrice0, giftBalance);
						value += 350;
						if(value < 0.0000001){
							value = 0;
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, String.valueOf(value));
					}
				}
			}catch(Exception e){
				logger.debug("--->获取礼品卡余额处处",e);
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
		catch (Exception e)
		{
			logger.error("--->选择礼品卡出错",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_FAIL;
		}
		
		try {
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pet-checkout-button")));
			driver.findElement(By.cssSelector(".pet-checkout-button")).click();
		} catch (Exception e) {
			logger.error("--->没出现注文を続ける");
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount)
	{
		String tarAddr = param.get("count");
		String myPrice = param.get("my_price");
		String primeStr = param.get("isPrime");
		String username = param.get("userName");
		String payType = param.get("payType");
		String size = param.get("size");
		try {
			doScreenShot();
		} catch (Exception e) {
		}
		
		if (Utils.isEmpty(myPrice))
		{
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		try {
			List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
			logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
			logger.debug("--->size有 [" + size + "]件商品");
			if(!size.equals(String.valueOf(goodsInCart.size()))){
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->购物车数量验证失败",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}

		Boolean isPrime = false;
		try
		{
			isPrime = Boolean.valueOf(primeStr);
		}
		catch (Exception e)
		{
			isPrime = false;
		}
		
		//优惠码
		String promotionStr = param.get("promotion");
		Set<String> promotionList = getPromotionList(promotionStr);
		
		
		logger.error("--->myPrice = " + myPrice);
		logger.debug("默认地址:" + tarAddr);

		if (!clickCart().equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS))
		{
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		//获取除去运费的总价
		try{
			WebElement w = driver.findElement(By.xpath("//div[@id='sc-buy-box']/form/span/div/span/span/span"));
			if(StringUtil.isNotEmpty(w.getText())){
				totalPrice0 = w.getText().replace("￥", "").replace(",", "").replace("-", "").trim();
				logger.error("totalPrice0 = "+totalPrice0);
			}
		}catch(Exception e){
			logger.error("--->查找除去运费的总价失败",e);
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
		
		//获取商品总价(不包含运费)
//		try{
//			WebElement priceEle = driver.findElement(By.xpath("//span[@class='a-color-price a-text-bold']/span"));
//			String text = priceEle.getText();
//			String priceD = text.replace("￥", "").replaceAll(",", "").replace("-", "").trim();
//			if(StringUtil.isNotEmpty(priceD)){
//				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceD);
//			}
//		}catch(Exception e){
//			logger.error("--->获取商品总价(不包含运费)出错",e);
//		}

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		By by = By.xpath("//button[@class='a-button-text' and contains(text(), 'レジに進む')]");
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

			try
			{
				WebElement pwd = driver.findElement(By.id("ap_password"));
				logger.debug("--->再次登陆");
				pwd.sendKeys(param.get("password"));

				WebElement btn = driver.findElement(By.id("signInSubmit"));
				btn.click();

			}
			catch (Exception e)
			{
			}

			// 以下付款方式只针对 GIFTCARD
			AutoBuyStatus status = selectTargetAddr(tarAddr,username,userTradeAddress);
			if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS))
			{
				status = selectDeliveryOptions();
				if (status.equals(AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS))
				{
					status = selectGiftCard(myPrice, isPrime,promotionList,param);
					if (!status.equals(AutoBuyStatus.AUTO_PAY_SELECT_GIFTCARD_SUCCESS))
					{
						return status;
					}
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

			Map<String,String> cache = getTotal();
			String total = null;
			if("credit".equals(payType)){
				total = cache.get("ご請求額");
			}else{
				total = cache.get("amazonギフト券");
			}
			
			String promotionFee = cache.get("割引");
			String mallFee = cache.get("配送料_手数料");
			
			try
			{
				logger.error("--->总价:" + total);
//				if (!Utils.isEmpty(total) && Double.valueOf(total.trim()).doubleValue() > 0)
//				{
//					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, total);
//					BigDecimal x = new BigDecimal(total.trim());
//					BigDecimal y = new BigDecimal(myPrice);
//					BigDecimal v = x.subtract(y);
//					double left = v.doubleValue();
//					logger.error("--->礼品卡余额:" + left);
//					data.put(AutoBuyConst.KEY_AUTO_BUY_GIFTCARD_LEFT, String.valueOf(left));
//				}
//				else
//				{
//					logger.error("--->[1]获取不到总价");
//					return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
//				}
				logger.error("--->总价:" + total);
				if (!Utils.isEmpty(total) && Double.valueOf(total.trim()).doubleValue() > 0)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, total);
				}
				else
				{
					logger.error("--->获取不到总价");
					return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				logger.error("--->[2]获取不到总价");
				return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
			}
			
			try
			{
				logger.error("--->优惠:" + promotionFee);
				if (!Utils.isEmpty(promotionFee) && Double.valueOf(promotionFee.trim()).doubleValue() >= 0)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_PROMOTION_FEE, promotionFee);
				}
				else
				{
					logger.error("--->获取不到优惠");
				}
			}
			catch (Exception e)
			{
				logger.error("--->获取不到优惠");
			}
			
			try
			{
				logger.error("--->运费:" + mallFee);
				if (!Utils.isEmpty(mallFee) && Double.valueOf(mallFee.trim()).doubleValue() >= 0)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE, mallFee);
				}
				else
				{
					logger.error("--->获取不到运费");
				}
			}
			catch (Exception e)
			{
				logger.error("--->获取不到运费");
			}

			try
			{
				if(!StringUtil.isBlank(getTotalPrice())){
					AutoBuyStatus priceStatus = comparePrice(total, getTotalPrice());
					logger.debug("--->完成付款,totalpay开始比价[" + getTotalPrice() + "," + total + "]");
					if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
					
				}else{
					logger.debug("--->完成付款,开始比价[" + myPrice + "," + total + "]");
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(total.trim());
					BigDecimal v = y.subtract(x);
					
					if (v.doubleValue() > 50.00D)
					{
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
				
				doScreenShot();

				Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
				if (isPay)
				{
					WebElement orderSubmit = null;
					By submitBy = By.xpath("//input[contains(@value, '注文を確定する')]");

					try
					{
						orderSubmit = wait.until(ExpectedConditions.visibilityOfElementLocated(submitBy));
						logger.info("--->找到了[Place your order]");
					}
					catch (Exception e)
					{
						logger.error("--->未找到[Place your order]");
						return AutoBuyStatus.AUTO_PAY_FAIL;
					}

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
				logger.error("--->付款出错啦!", e);
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			String orderNo = "";
			try {
				orderNo = getAmazonOrderNo();
			} catch (Exception e) {
				logger.error("--->获取amazonjp单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
			
			if (!Utils.isEmpty(orderNo))
			{
				logger.error("--->获取amazonjp单号成功:\t" + orderNo);
				savePng();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
			}
			else
			{
				logger.error("--->获取amazonjp单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		}
		else
		{
			logger.info("--->购物车为空!");
		}
		try {
			driver.navigate().to("https://www.amazon.co.jp/gp/gc/create/ref=gc_ya_topup_mbrowser?ie=UTF8&ref_=gc_ya_topup_mbrowser");
		} catch (Exception e) {
			logger.info("--->跳转礼品卡页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-color-success")));
			WebElement gitBalance = driver.findElement(By.cssSelector(".a-color-success"));
			String balanceText = gitBalance.getText().trim();
			logger.info("--->礼品卡余额:"+balanceText);
			balanceText = balanceText.replace("￥", "").replace(",", "").replace("-", "").trim();
			data.put(AutoBuyConst.KEY_AUTO_BALANCE_WB, balanceText);
		}catch (Exception e) {
			logger.info("--->礼品卡余额获取失败!");
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	private String getAmazonOrderNo()
	{
		String orderNo = null;
		List<WebElement> divs = driver.findElements(By.xpath("//div[@class='a-box-inner']"));
		boolean flag = false;
		if (divs != null && divs.size() > 0)
		{
			for (WebElement div : divs)
			{
				String txt = div.getText();
				if (txt.indexOf("注文詳細を見る") > -1)
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
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'注文内容を表示')]")));
				WebElement x = driver.findElement(By.xpath("//h3[contains(text(),'注文内容を表示')]/following-sibling::div[1]"));
				WebElement y = x.findElement(By.xpath(".//div[@class='a-row'][2]"));
				List<WebElement> ps = y.findElements(By.tagName("p"));
				if (ps.size() == 2)
				{
					orderNo = ps.get(1).getText();
				}
				//doScreenShot();
			}
			catch (Exception e)
			{
				logger.debug("整个html:"+driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text"));
			}
		}
		return orderNo;
	}

	Map<String, String> getTotal()
	{
		logger.debug("开始获取总价");
		Map<String, String> cache = new HashMap<String, String>();
		try
		{
			boolean isSuccess = false;
			try{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@name='placeYourOrder1']")));
				isSuccess = true;
			}catch(Exception e){
				logger.debug("等待价格页面加载完成出错",e);
				try{
					driver.findElement(By.xpath("//input[@title='注文を確定する']"));
					isSuccess = true;
				}catch(Exception ee){
					logger.debug("等待价格页面加载完成出错",ee);
				}
			}
			
			if(!isSuccess){
				throw new Exception("等待页面加载出错");
			}
			
			
			//判断是否有18禁的提示
			try{
				WebElement w = driver.findElement(By.xpath("//input[@id='riskDrugNoticeCheckBox']"));
				if(w != null && !w.isSelected()){
					logger.error("--->找到18禁的提示");
					driver.findElement(By.xpath("//label[@for='riskDrugNoticeCheckBox']")).click();;
					Utils.sleep(3000);
				}
			}catch(Exception e){
				logger.error("--->没有18禁的提示carry on");
			}
			
			By totalBy = By.cssSelector("div#subtotals-marketplace-table table tr");
			List<WebElement> trs = driver.findElements(totalBy);
			logger.debug("开始获取总价 trs");
			if (trs != null && trs.size() > 0)
			{
				logger.debug("开始获取总价 trs is not empty");
				for (WebElement tr : trs)
				{
					List<WebElement> tds = tr.findElements(By.tagName("td"));
					String key = tds.get(0).getText();
					String value = tds.get(1).getText();
					key = key.replace("・", "_").replaceAll("\\s+", "_").replace("：", "").replace("-", "").toLowerCase();
					value = value.replace("￥", "").replace(",", "").replace("-", "").trim();
					;
					cache.put(key, value);
				}
				logger.debug("--->付款详情:" + cache);
			}
			else
			{
				logger.debug("获取总价进入新的方案");
				trs = driver.findElements(By.xpath("//table[@id='subtotals-marketplace-table']/tbody/tr"));
				if (trs != null && trs.size() > 0)
				{
					for (WebElement tr : trs)
					{
						List<WebElement> tds = tr.findElements(By.tagName("td"));
						String key = tds.get(0).getText();
						String value = tds.get(1).getText();
						key = key.replace("・", "_").replaceAll("\\s+", "_").replace("：", "").replace("-", "").toLowerCase();
						value = value.replace("￥", "").replace(",", "").replace("-", "").trim();
						;
						cache.put(key, value);
					}
					logger.debug("--->付款详情:" + cache);
					logger.debug("获取总价进入新的方案 获取总价成功");
				}
				else
				{
					logger.debug("获取总价进入新的方案:trs是空的");
				}
			}
		}
		catch (Exception e)
		{
			logger.info("获取总价遇到异常:", e);
		}
		return cache;
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
			WebElement feedBack = order.findElement(By.id("商品レビューを書く_1"));
			feedBack.click();
			Utils.sleep(3000);
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_REVIEW_CAN_NOT_FIND_BUTTON;
		}
		
		try {
			WebElement reviewContainer = driver.findElement(By.cssSelector("div#reviewSection0"));

			WebElement urlEle = reviewContainer.findElement(By.cssSelector("a.a-size-base.a-link-normal.title[target=_blank]"));
			logger.error("review product_url:" + urlEle.getAttribute("href"));
			if (!urlEle.getAttribute("href").contains(productEntityCode)) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
			}
			
			WebElement starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"星1つを与える商品を選択します。\"]"));
			if (starELe.getAttribute("class").contains("yellowStar")) {
				return AutoBuyStatus.AUTO_CHECK_REVIEW_SUCCESS;
			} else {
				return AutoBuyStatus.AUTO_CHECK_REVIEW_FAIL;
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
			WebElement feedBack = order.findElement(By.id("商品レビューを書く_1"));
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
			
			logger.error("review打星");
			WebElement starELe = null;
			if (getBooleanByRate(0.2)) {
				starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"星4つを与える商品を選択します。\"]"));
			} else {
				starELe = reviewContainer.findElement(By.cssSelector("div[aria-label=\"星5つを与える商品を選択します。\"]"));
			}
			if (!starELe.getAttribute("class").contains("yellowStar")) {
				starELe.click();
				Utils.sleep(1500);
			}
			
			logger.error("review输入");
			WebElement reviewTextArea = reviewContainer.findElement(By.cssSelector("div.a-input-text-wrapper.bigTextArea.textField.reviewText.expandingTextSectionHeight[title=\"ここにレビューを記入してください\"] > textarea"));
			reviewTextArea.sendKeys(reviewContent);
			Utils.sleep(1500);

			logger.error("review headline输入");
			WebElement headline = reviewContainer.findElement(By.cssSelector("input.reviewTitle"));
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
	
	public AutoBuyStatus chooseOrder(BrushOrderDetail detail, String productEntityCode) {
		try {
			String mallOrderNo = detail.getMallOrderNo();
			if (Utils.isEmpty(mallOrderNo)) {
				return AutoBuyStatus.AUTO_CHOOSE_ORDER_MALL_ORDER_EMPTY;
			}

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			WebElement signin = null;
			try {
				signin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#nav-link-accountList")));
			} catch (Exception e) {
				signin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#nav-link-yourAccount")));
			}
			signin.click();
			
			logger.debug("查找订单，点击我的订单");
			WebElement ordersEle = driver.findElement(By.cssSelector("div[data-card-identifier=YourOrders]"));
			ordersEle.click();
			Utils.sleep(1500);

			logger.debug("查找订单，输入商城订单号：" + mallOrderNo);
			WebElement searchInput = driver.findElement(By.id("searchOrdersInput"));
			searchInput.sendKeys(mallOrderNo);
			Utils.sleep(1500);
			
			logger.debug("查找订单，点击查找按钮");
			WebElement searchButton = driver.findElement(By.xpath("//span[@id='a-autoid-0-announce' and contains(text(), '注文を検索')]"));
			searchButton.click();
			Utils.sleep(5000);
			logger.debug("查找订单完成");
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_SUCCESS;
		} catch (Exception e) {
			logger.error("feedBack or review查找订单失败", e);
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_FAIL;
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
		
		List<WebElement> orders = driver.findElementsByCssSelector("div.a-box-group.a-spacing-base.order");
		if (orders.size() != 1) {
			return AutoBuyStatus.AUTO_CHOOSE_ORDER_ORDER_NOT_FIND;
		}

		try {
			WebElement order = orders.get(0);
			WebElement feedBack = order.findElement(By.id("出品者を評価_1"));
			feedBack.click();
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
				if (fulfillmentAnswer.getText().contains("はい")) {
					fulfillmentAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);

			logger.error("feedBack itemAsDescribedAnswer");
			List<WebElement> itemAsDescribedAnswers = mainFeedBackContainer.findElements(By.cssSelector("div.a-radio[data-a-input-name=itemAsDescribedAnswer] > label > span"));
			for (WebElement itemAsDescribedAnswer : itemAsDescribedAnswers) {
				if (itemAsDescribedAnswer.getText().contains("はい")) {
					itemAsDescribedAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);

			logger.error("feedBack customerServiceAnswer");
			List<WebElement> customerServiceAnswers = mainFeedBackContainer.findElements(By.cssSelector("div.a-radio[data-a-input-name=customerServiceAnswer] > label > span"));
			for (WebElement customerServiceAnswer : customerServiceAnswers) {
				if (customerServiceAnswer.getText().contains("連絡していません")) {
					customerServiceAnswer.click();
					break;
				}
			}
			Utils.sleep(1500);
			
			logger.error("feedBack填写Comment:" + feedBackContent);
			WebElement feedbackText = mainFeedBackContainer.findElement(By.id("feedbackText"));
			feedbackText.sendKeys(feedBackContent);
			Utils.sleep(1500);
			
			WebElement submit = driver.findElement(By.xpath("//span[@id='a-autoid-0-announce' and contains(text(), '評価を送信する')]"));
			submit.click();
			Utils.sleep(5000);
			
			return AutoBuyStatus.AUTO_FEED_BACK_SUCCESS;
		} catch (Exception e) {
			logger.error("feedBack异常", e);
			return AutoBuyStatus.AUTO_FEED_BACK_FAIL;
		}
	}

	@Override
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
		logger.error("开始爬去物流,单号:" + mallOrderNo);
		if (Utils.isEmpty(mallOrderNo))
		{
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try
		{
			//寻找Your Account
			try
			{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				WebElement orderAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your Account");
				orderAccount.click();
			}
			catch (Exception e)
			{
				logger.error("找不到主页上面的Your Account");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			//寻找Your orders
			try
			{
				WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'注文履歴')]")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your orders");
				order.click();
			}
			catch (Exception e)
			{
				logger.error("找不到Your orders");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebElement searchBtn = null;
			boolean findById = true;
			try
			{
				logger.info("--->[1]find search btn");
				Utils.sleep(3000);
				searchBtn = driver.findElement(By.id("showSearchBar"));
				Utils.sleep(1500);
				searchBtn.click();
			}
			catch (Exception e)
			{
				try
				{
					logger.info("--->[2]find search btn");
					Utils.sleep(3000);
					searchBtn = driver.findElement(By.xpath("//a[contains(text(),'すべての注文を検索')]"));
					Utils.sleep(1500);
					searchBtn.click();
					findById = false;
				}
				catch (Exception ex)
				{
					logger.error("--->查找搜索按钮错误");
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}

			try
			{
				//寻找订单输入框
				Utils.sleep(3000);
				WebElement searchInput = driver.findElement(By.name("search"));
				Utils.sleep(1500);
				searchInput.sendKeys(mallOrderNo);
				Utils.sleep(2000);
				searchInput.sendKeys(Keys.ENTER);
				Utils.sleep(1500);
				logger.debug("--->查找到 搜索框并且点击搜索");
			}
			catch (Exception e)
			{
				logger.error("查找订单错误");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			// 被砍单
			try
			{
				Utils.sleep(5000);
				WebElement result = driver.findElement(By.id("no-result"));
				if (!result.isDisplayed() && result.getText().contains(mallOrderNo))
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				if (!findById)
				{
					logger.error("找不到这个订单 :" + mallOrderNo);
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
				}
			}
			
			try
			{
				WebElement result = driver.findElement(By.xpath("//span[@class='a-size-small a-color-secondary' and contains(text(),'キャンセル')]"));
				if (result != null && result.isDisplayed())
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				logger.error(mallOrderNo + " 这个订单没有被砍，carry on");
			}
			

			if (findById)
			{
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.name("search")));
			}
			
			TimeUnit.SECONDS.sleep(2);
			
			//对比asinCode
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section.a-padding-small.js-item")));
				logger.error("a-padding-small加载完成");
				List<WebElement> ll = driver.findElements(By.cssSelector(".a-section.a-padding-small.js-item"));
				if(ll != null && ll.size() > 0){
					boolean isFind = false;
					for(WebElement w : ll){
						WebElement ww = w.findElement(By.cssSelector("div.item-view-left-col-inner a.a-link-normal"));
						logger.error("a-link-normal加载完成");
						if(ww != null){
							String productLink = ww.getAttribute("href");
							logger.error("productLink = "+productLink);
							if(productLink != null && productLink.contains(productEntityCode)){
								try {
									WebElement seeShip = w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'配送状況を確認')]"));
									if(seeShip != null){
										isFind = true;
										seeShip.click();
										break;
									}
								} catch (Exception e) {
									try{
										w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'注文内容を表示')]")).click();
										logger.error("点击注文内容を表示");
									}catch(Exception e1){
										logger.error("注文内容を表示 出错1");
										break;
									}
									try{
										wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
										logger.error("到达配送状況を確認");
										List<WebElement> groups = driver.findElements(By.cssSelector(".a-section .a-box-group"));
										for(WebElement group:groups){
											WebElement aLink = group.findElement(By.cssSelector("a.a-link-normal"));
											String productLinks = aLink.getAttribute("href");
											logger.error("aLink = "+productLinks);
											if(productLinks != null && productLinks.contains(productEntityCode)){
												WebElement seeShip = group.findElement(By.cssSelector("a.a-touch-link.a-box"));
												if(seeShip != null){
													isFind = true;
													seeShip.click();
												}
											}
										}
									}catch(Exception e1){
										logger.error("配送状況を確認 出错1");
										break;
									}
								}
							}
						}
						if(isFind){
							break;
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
			
			//寻找物流公司和单号页面
			try
			{
				logger.debug("--->获取物流信息");
				WebElement base = driver.findElement(By.xpath("//div[@class='a-box-group a-spacing-large']/div[@class='a-box'][2]/div[@class='a-box-inner a-padding-medium']"));
				List<WebElement> ps = base.findElements(By.tagName("p"));
				if (ps != null && ps.size() == 2)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, ps.get(0).getText().trim());
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ps.get(1).getText().trim());

					logger.error("expressCompany = " + ps.get(0).getText());
					logger.error("expressNo = " + ps.get(1).getText());

					logger.debug("--->成功获取物流信息");

				}
				else
				{
					logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号码时出现异常:" + e.getMessage());
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			//获取国外物流节点
			String expressNo = data.get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO);
			logger.error("订单号" + detail.getOrderNo()+" 物流单号"+expressNo);
			if(!StringUtils.isBlank(detail.getOrderNo()) && !StringUtils.isBlank(expressNo)){
				List<ExpressNode> nodeList = getExpressNode(detail.getGmtCreate(),detail.getOrderNo(),expressNo);
				if(nodeList.size() > 0 && getTask() != null){
					logger.error("addParam expressNodeList"+expressNo);
					getTask().addParam("expressNodeList", nodeList);
				}
				try {
					driver.navigate().to("https://www.amazon.co.jp/gp/css/summary/print.html/ref=oh_aui_pi_o01_?ie=UTF8&orderID="+detail.getMallOrderNo());
					String text = (String) driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text");
					text = text.replace("_________________________様", "Fedroad Japan株式会社様");
					logger.error("--->text:"+text);
					download(text, "C://auto//screenshot//"+detail.getMallOrderNo()+".html");
					TimeUnit.SECONDS.sleep(5);
					driver.get("file:///C:/auto/screenshot/"+detail.getMallOrderNo()+".html");
					try {
						byte[] b = doScreenShot();
						if(b!=null){
							getTask().addParam("fedroadtext", b);
						}else{
							logger.error("--->fedroadtext为空");
						}
					} catch (Exception e) {
						logger.error("--->截图失败");
					}
				} catch (Exception e) {
					logger.error("爬 fedroad");
				}
				
				//gp/css/summary/print.html/ref=oh_aui_pi_o01_?ie=UTF8&orderID=250-9937288-7747802
			}
			
			return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			

//			//在查询结果订单里寻找View order details
//			try
//			{
//				WebElement seeOrder = driver.findElement(By.xpath("//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'注文内容を表示')]"));
//				Utils.sleep(1500);
//				logger.debug("--->找到订单,察看订单详情");
//				seeOrder.click();
//			}
//			catch (Exception e)
//			{
//				logger.error(mallOrderNo + "在查询结果里寻找View order details出异常");
//				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//			}
//			//寻找Track shipment
//			WebElement seeOrderDetail = null;
//			try
//			{
//				seeOrderDetail = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'配送状況を確認')]")));
//				driver.executeScript("(function(){window.scrollBy(100,100);})();");
//				Utils.sleep(3000);
////				seeOrderDetail.click();
////				logger.debug("--->找到订单,点击物流详情");
//			}
//			catch (Exception e)
//			{
//				logger.error(mallOrderNo + "找不到Track shipment,该订单还没发货,没产生物流单号");
////				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//			}
//			
//			//同个订单有不同的包裹
//			List<WebElement> list = driver.findElements(By.xpath("//div[@class='a-box-group']"));
//			if(list != null && list.size() > 1){
//				logger.error("订单:"+mallOrderNo + "有"+list.size()+"个不同包裹");
//				for(WebElement w : list){
//					boolean isMatch = false;
//					try{
//						
//						
//						List<WebElement> priceList = w.findElements(By.xpath(".//span[@class='a-size-base a-color-base']"));
//						if(priceList != null && priceList.size() > 0){
//							for(WebElement price : priceList){
//								String priceStr = price.getText();
//								if(StringUtil.isNotEmpty(priceStr) && priceStr.startsWith("￥")){
//									priceStr = priceStr.replaceAll(",", "");
//									priceStr = priceStr.substring(1);
//								}
//								String singlePrice = detail.getSinglePrice();
//								if(StringUtil.isNotEmpty(singlePrice)){
//									singlePrice = singlePrice.trim();
//								}
//								if(singlePrice != null && singlePrice.equals(priceStr)){
//									isMatch = true;
//									logger.error("找到相同价格的子订单 = "+singlePrice);
//									break;
//								}
//							}
//						}
//						
//						
////						WebElement price = w.findElement(By.xpath(".//span[@class='a-size-base a-color-base']"));
////						String priceStr = price.getText();
////						if(priceStr != null && priceStr.startsWith("￥")){
////							priceStr = priceStr.replaceAll(",", "");
////							priceStr = priceStr.substring(1);
////						}
////						String singlePrice = detail.getSinglePrice();
////						if(StringUtil.isNotEmpty(singlePrice) && singlePrice.contains(priceStr)){
////							isMatch = true;
////							logger.error("找到相同价格的子订单");
////						}
//					}catch(Exception e){
//						logger.error("查找子订单价格出现异常",e);
//					}
//					if(isMatch){
//						try{
//							WebElement subDetail = w.findElement(By.xpath(".//a/div[contains(text(),'配送状況を確認')]"));
//							if(subDetail != null){
//								seeOrderDetail = subDetail;
//								break;
//							}
//						}catch(Exception e){
//							logger.error(mallOrderNo + "找不到子订单Track shipment,该订单还没发货,没产生物流单号",e);
//							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//						}
//					}
//				}
//			}
//			
//			
//			//点击物流详情
//			try
//			{
//				seeOrderDetail.click();
//				logger.debug("--->找到订单,点击物流详情");
//			}
//			catch (Exception e)
//			{
//				logger.error(mallOrderNo + "找不到Track shipment");
//				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//			}
//
//			//在寻找物流公司和单号页面等待View order details可见
//			try
//			{
//				logger.debug("--->获取物流信息");
//				WebElement base = driver.findElement(By.xpath("//div[@class='a-box-group a-spacing-large']/div[@class='a-box'][2]/div[@class='a-box-inner a-padding-medium']"));
//				List<WebElement> ps = base.findElements(By.tagName("p"));
//				if (ps != null && ps.size() == 2)
//				{
//					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, ps.get(0).getText().trim());
//					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ps.get(1).getText().trim());
//
//					logger.error("expressCompany = " + ps.get(0).getText());
//					logger.error("expressNo = " + ps.get(1).getText());
//
//					logger.debug("--->成功获取物流信息");
//
//					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
//				}
//				else
//				{
//					logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
//					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
//				}
//			}
//			catch (Exception e)
//			{
//				logger.error("在寻找物流公司和单号码时出现异常:" + e.getMessage());
//				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
//			}
		}
		catch (Exception e)
		{
			logger.error("异常:" + e.getMessage());
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
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
		logger.error("开始爬去物流,单号:" + mallOrderNo);
		if (Utils.isEmpty(mallOrderNo))
		{
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try
		{
			//寻找Your Account
			try
			{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				WebElement orderAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your Account");
				orderAccount.click();
			}
			catch (Exception e)
			{
				logger.error("找不到主页上面的Your Account");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			//寻找Your orders
			try
			{
				WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'注文履歴')]")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your orders");
				order.click();
			}
			catch (Exception e)
			{
				logger.error("找不到Your orders");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebElement searchBtn = null;
			boolean findById = true;
			try
			{
				logger.info("--->[1]find search btn");
				Utils.sleep(3000);
				searchBtn = driver.findElement(By.id("showSearchBar"));
				Utils.sleep(1500);
				searchBtn.click();
			}
			catch (Exception e)
			{
				try
				{
					logger.info("--->[2]find search btn");
					Utils.sleep(3000);
					searchBtn = driver.findElement(By.xpath("//a[contains(text(),'すべての注文を検索')]"));
					Utils.sleep(1500);
					searchBtn.click();
					findById = false;
				}
				catch (Exception ex)
				{
					logger.error("--->查找搜索按钮错误");
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}

			try
			{
				//寻找订单输入框
				Utils.sleep(3000);
				WebElement searchInput = driver.findElement(By.name("search"));
				Utils.sleep(1500);
				searchInput.sendKeys(mallOrderNo);
				Utils.sleep(2000);
				searchInput.sendKeys(Keys.ENTER);
				Utils.sleep(1500);
				logger.debug("--->查找到 搜索框并且点击搜索");
			}
			catch (Exception e)
			{
				logger.error("查找订单错误");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			// 被砍单
			try
			{
				Utils.sleep(5000);
				WebElement result = driver.findElement(By.id("no-result"));
				if (!result.isDisplayed() && result.getText().contains(mallOrderNo))
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				if (!findById)
				{
					logger.error("找不到这个订单 :" + mallOrderNo);
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
				}
			}
			
			try
			{
				WebElement result = driver.findElement(By.xpath("//span[@class='a-size-small a-color-secondary' and contains(text(),'キャンセル')]"));
				if (result != null && result.isDisplayed())
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				logger.error(mallOrderNo + " 这个订单没有被砍，carry on");
			}
			

			if (findById)
			{
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.name("search")));
			}
			
			TimeUnit.SECONDS.sleep(2);
			
			//对比asinCode
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section.a-padding-small.js-item")));
				logger.error("a-padding-small加载完成");
				List<WebElement> ll = driver.findElements(By.cssSelector(".a-section.a-padding-small.js-item"));
				if(ll != null && ll.size() > 0){
					boolean isFind = false;
					for(WebElement w : ll){
						WebElement ww = w.findElement(By.cssSelector("div.item-view-left-col-inner a.a-link-normal"));
						logger.error("a-link-normal加载完成");
						if(ww != null){
							String productLink = ww.getAttribute("href");
							logger.error("productLink = "+productLink);
							if(productLink != null && productLink.contains(productEntityCode)){
								try {
									WebElement seeShip = w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'配送状況を確認')]"));
									if(seeShip != null){
										isFind = true;
										seeShip.click();
										break;
									}
								} catch (Exception e) {
									try{
										w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'注文内容を表示')]")).click();
										logger.error("点击注文内容を表示");
									}catch(Exception e1){
										logger.error("注文内容を表示 出错1");
										break;
									}
									try{
										wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
										logger.error("到达配送状況を確認");
										List<WebElement> groups = driver.findElements(By.cssSelector(".a-section .a-box-group"));
										for(WebElement group:groups){
											WebElement aLink = group.findElement(By.cssSelector("a.a-link-normal"));
											String productLinks = aLink.getAttribute("href");
											logger.error("aLink = "+productLinks);
											if(productLinks != null && productLinks.contains(productEntityCode)){
												WebElement seeShip = group.findElement(By.cssSelector("a.a-touch-link.a-box"));
												if(seeShip != null){
													isFind = true;
													seeShip.click();
												}
											}
										}
									}catch(Exception e1){
										logger.error("配送状況を確認 出错1");
										break;
									}
								}
							}
						}
						if(isFind){
							break;
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
			
			//寻找物流公司和单号页面
			try
			{
				logger.debug("--->获取物流信息");
				WebElement base = driver.findElement(By.xpath("//div[@class='a-box-group a-spacing-large']/div[@class='a-box'][2]/div[@class='a-box-inner a-padding-medium']"));
				List<WebElement> ps = base.findElements(By.tagName("p"));
				if (ps != null && ps.size() == 2)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, ps.get(0).getText().trim());
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ps.get(1).getText().trim());

					logger.error("expressCompany = " + ps.get(0).getText());
					logger.error("expressNo = " + ps.get(1).getText());

					logger.debug("--->成功获取物流信息");

				}
				else
				{
					logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号码时出现异常:" + e.getMessage());
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			//获取国外物流节点
			String expressNo = data.get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO);
			logger.error("订单号" + detail.getOrderNo()+" 物流单号"+expressNo);
			if(!StringUtils.isBlank(detail.getOrderNo()) && !StringUtils.isBlank(expressNo)){
				List<ExpressNode> nodeList = getExpressNode(detail.getGmtCreate(),detail.getOrderNo(),expressNo);
				if(nodeList.size() > 0 && getTask() != null){
					logger.error("addParam expressNodeList"+expressNo);
					getTask().addParam("expressNodeList", nodeList);
				}
				try {
					driver.navigate().to("https://www.amazon.co.jp/gp/css/summary/print.html/ref=oh_aui_pi_o01_?ie=UTF8&orderID="+detail.getMallOrderNo());
					String text = (String) driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text");
					text = text.replace("_________________________様", "Fedroad Japan株式会社様");
					logger.error("--->text:"+text);
					download(text, "C://auto//screenshot//"+detail.getMallOrderNo()+".html");
					TimeUnit.SECONDS.sleep(5);
					driver.get("file:///C:/auto/screenshot/"+detail.getMallOrderNo()+".html");
					try {
						byte[] b = doScreenShot();
						if(b!=null){
							getTask().addParam("fedroadtext", b);
						}else{
							logger.error("--->fedroadtext为空");
						}
					} catch (Exception e) {
						logger.error("--->截图失败");
					}
				} catch (Exception e) {
					logger.error("爬 fedroad");
				}
				
				//gp/css/summary/print.html/ref=oh_aui_pi_o01_?ie=UTF8&orderID=250-9937288-7747802
			}
			
			return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			

		}
		catch (Exception e)
		{
			logger.error("异常:" + e.getMessage());
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
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
		logger.error("开始爬去物流,单号:" + mallOrderNo);
		if (Utils.isEmpty(mallOrderNo))
		{
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try
		{
			//寻找Your Account
			try
			{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				WebElement orderAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your Account");
				orderAccount.click();
			}
			catch (Exception e)
			{
				logger.error("找不到主页上面的Your Account");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			//寻找Your orders
			try
			{
				WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='a-size-base' and contains(text(),'注文履歴')]")));
				Utils.sleep(1500);
				logger.debug("--->查找到Your orders");
				order.click();
			}
			catch (Exception e)
			{
				logger.error("找不到Your orders");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			WebElement searchBtn = null;
			boolean findById = true;
			try
			{
				logger.info("--->[1]find search btn");
				Utils.sleep(3000);
				searchBtn = driver.findElement(By.id("showSearchBar"));
				Utils.sleep(1500);
				searchBtn.click();
			}
			catch (Exception e)
			{
				try
				{
					logger.info("--->[2]find search btn");
					Utils.sleep(3000);
					searchBtn = driver.findElement(By.xpath("//a[contains(text(),'すべての注文を検索')]"));
					Utils.sleep(1500);
					searchBtn.click();
					findById = false;
				}
				catch (Exception ex)
				{
					logger.error("--->查找搜索按钮错误");
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}

			try
			{
				//寻找订单输入框
				Utils.sleep(3000);
				WebElement searchInput = driver.findElement(By.name("search"));
				Utils.sleep(1500);
				searchInput.sendKeys(mallOrderNo);
				Utils.sleep(2000);
				searchInput.sendKeys(Keys.ENTER);
				Utils.sleep(1500);
				logger.debug("--->查找到 搜索框并且点击搜索");
			}
			catch (Exception e)
			{
				logger.error("查找订单错误");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}

			// 被砍单
			try
			{
				Utils.sleep(5000);
				WebElement result = driver.findElement(By.id("no-result"));
				if (!result.isDisplayed() && result.getText().contains(mallOrderNo))
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				if (!findById)
				{
					logger.error("找不到这个订单 :" + mallOrderNo);
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
				}
			}
			
			try
			{
				WebElement result = driver.findElement(By.xpath("//span[@class='a-size-small a-color-secondary' and contains(text(),'キャンセル')]"));
				if (result != null && result.isDisplayed())
				{
					logger.error(mallOrderNo + " 这个订单被砍单了,需重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			catch (Exception e)
			{
				logger.error(mallOrderNo + " 这个订单没有被砍，carry on");
			}
			

			if (findById)
			{
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.name("search")));
			}
			
			TimeUnit.SECONDS.sleep(2);
			
			//对比asinCode
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section.a-padding-small.js-item")));
				logger.error("a-padding-small加载完成");
				List<WebElement> ll = driver.findElements(By.cssSelector(".a-section.a-padding-small.js-item"));
				if(ll != null && ll.size() > 0){
					boolean isFind = false;
					for(WebElement w : ll){
						WebElement ww = w.findElement(By.cssSelector("div.item-view-left-col-inner a.a-link-normal"));
						logger.error("a-link-normal加载完成");
						if(ww != null){
							String productLink = ww.getAttribute("href");
							logger.error("productLink = "+productLink);
							if(productLink != null && productLink.contains(productEntityCode)){
								try {
									WebElement seeShip = w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'配送状況を確認')]"));
									if(seeShip != null){
										isFind = true;
										seeShip.click();
										break;
									}
								} catch (Exception e) {
									try{
										w.findElement(By.xpath(".//a[@class='a-touch-link a-box']/div[@class='a-box-inner' and contains(text(),'注文内容を表示')]")).click();
										logger.error("点击注文内容を表示");
									}catch(Exception e1){
										logger.error("注文内容を表示 出错1");
										break;
									}
									try{
										wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-section .a-box-group")));
										logger.error("到达配送状況を確認");
										List<WebElement> groups = driver.findElements(By.cssSelector(".a-section .a-box-group"));
										for(WebElement group:groups){
											WebElement aLink = group.findElement(By.cssSelector("a.a-link-normal"));
											String productLinks = aLink.getAttribute("href");
											logger.error("aLink = "+productLinks);
											if(productLinks != null && productLinks.contains(productEntityCode)){
												WebElement seeShip = group.findElement(By.cssSelector("a.a-touch-link.a-box"));
												if(seeShip != null){
													isFind = true;
													seeShip.click();
												}
											}
										}
									}catch(Exception e1){
										logger.error("配送状況を確認 出错1");
										break;
									}
								}
							}
						}
						if(isFind){
							break;
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
			
			//寻找物流公司和单号页面
			try
			{
				logger.debug("--->获取物流信息");
				WebElement base = driver.findElement(By.xpath("//div[@class='a-box-group a-spacing-large']/div[@class='a-box'][2]/div[@class='a-box-inner a-padding-medium']"));
				List<WebElement> ps = base.findElements(By.tagName("p"));
				if (ps != null && ps.size() == 2)
				{
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, ps.get(0).getText().trim());
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ps.get(1).getText().trim());

					logger.error("expressCompany = " + ps.get(0).getText());
					logger.error("expressNo = " + ps.get(1).getText());

					logger.debug("--->成功获取物流信息");

				}
				else
				{
					logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
			catch (Exception e)
			{
				logger.error("在寻找物流公司和单号码时出现异常:" + e.getMessage());
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			String expressNo = data.get(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO);
			if(!StringUtils.isBlank(detail.getSaleOrderCode()) && !StringUtils.isBlank(expressNo)){
				try {
					driver.navigate().to("https://www.amazon.co.jp/gp/css/summary/print.html/ref=oh_aui_pi_o01_?ie=UTF8&orderID="+detail.getMallOrderNo());
					String text = (String) driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text");
					text = text.replace("_________________________様", "Fedroad Japan株式会社様");
					logger.error("--->text:"+text);
					download(text, "C://auto//screenshot//"+detail.getMallOrderNo()+".html");
					TimeUnit.SECONDS.sleep(5);
					driver.get("file:///C:/auto/screenshot/"+detail.getMallOrderNo()+".html");
					try {
						byte[] b = doScreenShot();
						if(b!=null){
							getTask().addParam("fedroadtext", b);
						}else{
							logger.error("--->fedroadtext为空");
						}
					} catch (Exception e) {
						logger.error("--->截图失败");
					}
				} catch (Exception e) {
					logger.error("爬 fedroad");
				}
			}
			return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			
		}
		catch (Exception e)
		{
			logger.error("异常:" + e.getMessage());
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
	}

	private List<ExpressNode> getExpressNode(Date date,String orderNo, String expressNo) throws ParseException {
		List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
		WebDriverWait wait = new WebDriverWait(driver, 10);
		try {
			WebElement expressElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.a-touch-link.a-box.a-touch-link-noborder.order-summary-tracking-details-link")));
			expressElement.click();
		} catch (Exception e) {
			logger.error("爬取国外物流异常1:",e);
		}
		logger.error("等待国外物流节点加载==>");
		try {
			WebElement expressDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-box-group.a-spacing-mini")));
			
			//List<WebElement> expressList = expressDiv.findElements(By.cssSelector(".a-box-inner.a-padding-medium"));
			List<WebElement> timeList = expressDiv.findElements(By.cssSelector(".a-column.a-span3.ship-track-grid-fixed-column"));
			//.a-column.a-span3.ship-track-grid-fixed-column  shijian
			//.a-box-inner.a-padding-medium span.a-text-bold latest-update-string a-text-bold
			//.a-box-inner.a-padding-medium 
			//.a-column.a-span9.ship-track-grid-responsive-column.a-span-last 内容
			List<WebElement> yearMonth = expressDiv.findElements(By.cssSelector(".a-text-bold"));
			
			if(yearMonth!=null){
				if(yearMonth.size()==1){
					WebElement ym = yearMonth.get(0);
					logger.error("等待国外物流节点时间年月"+ym.getText());
					logger.error("等待国外物流节点时间时分"+timeList.get(0).getText());
					String name = "発送済み";
					int status = 3;
					if(ym.getAttribute("class").contains("latest-update-string")){
						name = "配達完了";
						status = 14;
					}
					//获取时间
					ExpressNode expressNode1 = new ExpressNode();
					expressNode1.setOrderNo(orderNo);
					expressNode1.setExpressNo(expressNo);
					expressNode1.setName(name);
					expressNode1.setOccurTime(getJPdate(date,ym.getText().split("日")[0],timeList.get(0).getText()));
					expressNode1.setStatus(status);
					nodeList.add(expressNode1);
					
				}else{
					
					for(WebElement yms:yearMonth){
						logger.error("等待国外物流节点时间年月"+yms.getText());
						
						String name = "発送済み";
						int status = 3;
						WebElement hm = timeList.get(0);
						if(yms.getAttribute("class").contains("latest-update-string")){
							name = "配達完了";
							status = 14;
						}else{
							if(timeList.get(timeList.size()-1).getText().contains("--")){
								hm = timeList.get(timeList.size()-2);
							}else{
								hm = timeList.get(timeList.size()-1);
							}
						}
						logger.error("等待国外物流节点时间时分"+hm.getText());
						ExpressNode expressNode1 = new ExpressNode();
						expressNode1.setOrderNo(orderNo);
						expressNode1.setExpressNo(expressNo);
						expressNode1.setName(name);
						expressNode1.setOccurTime(getJPdate(date,yms.getText().split("日")[0],hm.getText()));
						expressNode1.setStatus(status);
						nodeList.add(expressNode1);
					}
					
				}
			} 
		} catch (Exception e) {
			logger.error("爬取国外物流异常2:",e);
		}
		return nodeList;
	}
	
	private Date getJPdate(Date orderDate,String expressDate,String hm) throws ParseException{
		int year = getfullDate(orderDate);
		if(orderDate.getMonth()>getJPmonth(expressDate) && orderDate.getMonth()==12){
			year = year+1;
		}
		String fullDate = year+"年"+expressDate+" "+hm;
		return getDate(fullDate);
	}
	
	private Date getDate(String fullDate) throws ParseException{
		SimpleDateFormat sl = new SimpleDateFormat("yyyy年MM月dd hh:mm");
		return sl.parse(fullDate);
	}
	
	private int getfullDate(Date orderDate){
		 Calendar a=Calendar.getInstance();
    	 a.setTime(orderDate);
    	 return a.get(Calendar.YEAR);
	}
	
	private int getJPmonth(String expressDate) throws ParseException{
		SimpleDateFormat s1 = new SimpleDateFormat("MM月dd");
    	Date date = s1.parse(expressDate);
    	return date.getMonth();
	}

	@Override
	public boolean gotoMainPage()
	{
		try
		{
			Utils.sleep(2000);
			driver.get("http://www.amazon.co.jp/");
			Utils.sleep(3000);
//			WebDriverWait wait = new WebDriverWait(driver, 10);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-ftr-auth")));
			return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance)
	{
		logger.info("--->开始充值礼品卡:" + cardNo);
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			WebElement orderAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
			orderAccount.click();
			logger.debug("--->查找到Your Account");
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Your Account");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[@class='a-box-title' and contains(text(),'支払い＆住所')]")));
			WebElement giftCard = driver.findElement(By.xpath("//li/span/a/div/span[@class='a-size-base' and contains(text(),'Amazonギフト券を登録')]"));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Manage gift card balance]");
			giftCard.click();
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Redeem a gift card");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		String curBalance = "", nowBalance = "";
		WebElement balanceElem = null;

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
			balanceElem = driver.findElement(By.xpath("//p[@class='a-spacing-medium a-spacing-top-small']/span[2]"));
			curBalance = balanceElem.getText().replace("￥", "").replace(",", "");
			logger.info("--->卡内余额:" + curBalance);
		}
		catch (Exception e)
		{
			logger.error("--->察看充值之前余额失败");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.id("claim-code")).sendKeys(cardNo);
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.id("gc-submit-announce")).click();
		}
		catch (Exception e)
		{
			logger.error("--->充值失败");
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
		}

		try
		{
			TimeUnit.SECONDS.sleep(5);
			driver.executeScript("(function(){window.scrollTo(0,0);})();");
			WebElement error = driver.findElement(By.xpath("//div[@class='a-alert-content']/p"));
			logger.error("--->message:" + error.getText());
			logger.error("--->充值卡已经使用:" + cardNo);
			return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_ALREADY_USE;
		}
		catch (Exception e)
		{
		}

		try
		{
			logger.error("--->查看充值结果:");
			TimeUnit.SECONDS.sleep(3);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
			balanceElem = driver.findElement(By.xpath("//p[@class='a-spacing-medium a-spacing-top-small']/span[2]"));
			nowBalance = balanceElem.getText().replace("￥", "").replace(",", "");
			logger.info("--->充值后卡内余额:" + nowBalance);

			BigDecimal y = new BigDecimal(nowBalance);
			BigDecimal x = new BigDecimal(curBalance);
			BigDecimal tar = new BigDecimal(balance);
			BigDecimal sub = y.subtract(x);

			if (sub.intValue() > 0)
			{
				if (tar.subtract(sub).intValue() == 0)
				{
					logger.error("--->充值成功");
				}
				else
				{
					logger.error("--->充值成功,但提供的数额不对");
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
			logger.error("--->查看充值结果失败");
		}

		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_SUCCESS;
	}
	
	@Override
	public String redeemGiftCard(List<GiftCard> list) {
		logger.info("--->开始充值礼品卡");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			WebElement orderAccount = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-button-avatar")));
			orderAccount.click();
			logger.debug("--->查找到Your Account");
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Your Account");
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[@class='a-box-title' and contains(text(),'支払い＆住所')]")));
			WebElement giftCard = driver.findElement(By.xpath("//li/span/a/div/span[@class='a-size-base' and contains(text(),'Amazonギフト券を登録')]"));
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->找到[Manage gift card balance]");
			giftCard.click();
		}
		catch (Exception e)
		{
			logger.error("找不到主页上面的Redeem a gift card");
		}

		String curBalance = "", nowBalance = "";
		WebElement balanceElem = null;

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-spacing-large #gc-current-balance")));
			balanceElem = driver.findElement(By.cssSelector(".a-spacing-large #gc-current-balance"));
			logger.info("--->卡内余额1:" + balanceElem.getText());
			curBalance = balanceElem.getText().replace("￥", "").replace(",", "");
			logger.info("--->卡内余额:" + curBalance);
			
		} catch (Exception e) {
			logger.error("--->察看充值之前余额失败",e);
			if(StringUtil.isBlank(curBalance)){
				logger.info("--->卡内余额为空");
				for (int i = 0; i < list.size(); i++) {
					GiftCard card = list.get(i);
					card.setIsSuspect("yes");
				}
				return null;
			}
		}
			
			
		for (int i = 0; i < list.size(); i++) {
			GiftCard card = list.get(i);
			String cardNo = card.getSecurityCode();
			String balance = card.getBalance();
				
			try {
				driver.findElement(By.id("gc-redemption-input")).clear();
				TimeUnit.SECONDS.sleep(2);
				driver.findElement(By.id("gc-redemption-input")).sendKeys(cardNo);
				TimeUnit.SECONDS.sleep(2);
				driver.findElement(By.id("gc-redemption-apply-button")).click();
			} catch (Exception e2) {
				logger.error("--->重试充值失败"+card.getSecurityCode());
				card.setIsSuspect("yes");
				continue;
			}
				
				

			try
			{
				TimeUnit.SECONDS.sleep(5);
				driver.executeScript("(function(){window.scrollTo(0,0);})();");
				WebElement error = driver.findElement(By.cssSelector("#gc-redemption-info-message .a-alert-content"));
				logger.error("--->message:" + error.getText());
				logger.error("--->充值卡已经使用:" + cardNo);
				card.setIsSuspect("yes");
				continue;
			}
			catch (Exception e){}

			try
			{
				logger.error("--->查看充值结果:");
				TimeUnit.SECONDS.sleep(3);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#gc-new-balance")));
				balanceElem = driver.findElement(By.cssSelector("#gc-new-balance"));
				nowBalance = balanceElem.getText().replace("￥", "").replace(",", "");
				logger.info("--->充值后卡内余额:" + nowBalance);

				BigDecimal y = new BigDecimal(nowBalance);
				BigDecimal x = new BigDecimal(curBalance);
				BigDecimal tar = new BigDecimal(balance);
				BigDecimal sub = y.subtract(x);

				card.setRealBalance(card.getBalance());
				if (sub.intValue() > 0)
				{
					if (tar.subtract(sub).intValue() == 0)
					{
						logger.error("--->充值成功");
						card.setIsSuspect("no");
						
					}
					else
					{
						logger.error("--->充值成功,但提供的数额不对"+String.valueOf(sub));
						card.setRealBalance(String.valueOf(sub));
						card.setIsSuspect("no");
					}
					TimeUnit.SECONDS.sleep(3);
					curBalance = nowBalance;
					try {
						driver.findElement(By.id("gc-mobile-redeem-another-card-touch-link")).click();
						TimeUnit.SECONDS.sleep(2);
					} catch (Exception e) {
						logger.error("--->点击继续充值失败");
					}
					continue;
				}
				else
				{
					card.setIsSuspect("yes");
					continue;
				}
			}
			catch (Exception e)
			{
				card.setIsSuspect("yes");
				continue;
			}

		
		}
		return nowBalance;
	}
	
	public String checkGiftCard(){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.co.jp/gp/gc/create/ref=gc_ya_topup_mbrowser?ie=UTF8&ref_=gc_ya_topup_mbrowser");
		} catch (Exception e) {
			logger.info("--->跳转礼品卡页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".a-color-success")));
			WebElement gitBalance = driver.findElement(By.cssSelector(".a-color-success"));
			String balanceText = gitBalance.getText().trim();
			logger.info("--->礼品卡余额:"+balanceText);
			balanceText = balanceText.replace("￥", "").replace(",", "").replace("-", "").trim();
			return balanceText;
		}catch (Exception e) {
			logger.info("--->礼品卡余额获取失败!");
		}
		return null;
	}
	
	public String checkCard(){
		WebDriverWait wait = new WebDriverWait(driver, 35);
		try {
			driver.navigate().to("https://www.amazon.co.jp/cpe/managepaymentmethods/ref=ya_mb_mpo");
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
			driver.navigate().to("https://www.amazon.co.jp/gp/aw/primecentral/ref=aw_ya_hp_prime_aui");
		} catch (Exception e) {
			logger.info("--->跳转account页面出错!");
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#primeCentralResponsiveGreetingContentFromMS3")));
			WebElement content = driver.findElement(By.cssSelector("#primeCentralResponsiveGreetingContentFromMS3"));
			if(content.getText().contains("お客様はもうプライム会員ではありません")){
				//不是会员
			}else{
				List<WebElement> rows = content.findElements(By.cssSelector(".a-row"));
				for(WebElement w:rows){
					if(w.getText().contains("会員タイプ")){
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
//			driver.navigate().to("https://www.amazon.co.jp/a/addresses/ref=mobile_ya_address_book");
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
//							if(del.isDisplayed() && del.getText().contains("はい")){
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
			driver.navigate().to("https://www.amazon.co.jp/a/addresses/ref=mobile_ya_address_book");
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
							if(del.isDisplayed() && del.getText().contains("はい")){
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
	
	public static void download(String urlString, String filename) throws Exception {
		OutputStream os = null;
		InputStream is = null;
		try{
		    // 输入流
			is = new ByteArrayInputStream(urlString.getBytes());
		    // 1K的数据缓冲
		    byte[] bs = new byte[1024];
		    // 读取到的数据长度
		    int len;
		    // 输出的文件流
		    os = new FileOutputStream(filename);
		    // 开始读取
		    while ((len = is.read(bs)) != -1) {
		      os.write(bs, 0, len);
		    }
		}
		catch (Throwable e){
		}finally {  
			try {  
		        if (os != null) {  
		        	os.close();  
		        } 
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    }  
		    try {  
		        if (is != null) {  
		        	is.close();  
		        } 
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    }  
		}
	}
	
	public static boolean getBooleanByRate(double rate) {
		java.util.Random r = new java.util.Random();
		int i = r.nextInt(999) + 1;
		return i <= 1000 * rate;
	}

	public static void main(String[] args) throws ParseException
	{
		AmazonJpAutoBuy autoBuy = new AmazonJpAutoBuy(true);
		//AmazonJpAutoBuy autoBuy = new AmazonJpAutoBuy();
		//AutoBuyStatus status = autoBuy.login("hotsuer@outlook.com", "haihu2015");
		/*status = Utils.switchStatus(status);
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setOrderNo("1111");
		detail.setProductEntityId(110l);
		detail.setMallOrderNo("249-6672894-1007831");
		detail.setGmtCreate(new Date());
		System.out.println("login success");
		autoBuy.scribeExpress(detail);*/
//		String stockNum = "通常3～4日以内に発送します";
//		if (StringUtil.isNotEmpty(stockNum) && stockNum.contains("通常")){
//			if(!(stockNum.contains("3日以内に発送します") || stockNum.contains("5日以内に発送します") 
//					|| stockNum.contains("3~4日以内に発送")
//					|| stockNum.contains("2~3日以内に発送")
//					|| stockNum.contains("1~2日以内に発送")
//					|| stockNum.contains("3～4日以内に発送")
//					|| stockNum.contains("2～3日以内に発送")
//					|| stockNum.contains("1～2日以内に発送"))){
//				System.out.println("xixi");
//			}else{
//				System.out.println("haha");
//			}
//		}
//		AmazonJpAutoBuy autoBuy = new AmazonJpAutoBuy(false);
//		autoBuy.login("worktfb@163.com", "haihu2015");
//		Map<Long, String> asinMap = new HashMap<>();
//		asinMap.put(1111L, "B00OAY62HM");
//		autoBuy.setAsinMap(asinMap);
//		BrushOrderDetail detail = new BrushOrderDetail();
//		detail.setReviewContent("今まで旅行に行く時、自撮り棒を連れてちょっと面倒だと思いますがこれを購入しました。山頂で友達との集合写真撮影する際に便利です。");
//		detail.setFeedbackContent("今まで旅行に行く時、自撮り棒を連れてちょっと面倒だと思いますがこれを購入しました。山頂で友達との集合写真撮影する際に便利です。");
//		detail.setReviewTitle("効果が良い");
//		detail.setProductEntityId(1111L);
//		detail.setMallOrderNo("250-4011291-5617404");
//		//System.out.println(autoBuy.review(detail));
//		System.out.println(autoBuy.checkReview(detail, null));
		
		
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("114-9894719-8964233");
//		detail.setProductEntityId(4999961L);
		//detail.setProductSku("[[\"Color\",\"Luggage/Black\"]]");
		Map<String, String> param = new HashMap<>();
		param.put("url", "http://www.amazon.co.jp/dp/B0732J856C");
		//param.put("sku", "[[\"サイズ\",\"90粒/約1ヶ月分.\"]]");
		param.put("sku", "[[\"色\",\"薄ピンク\"]]");
		//param.put("sku", "[[\"サイズ\",\"480ml\"],[\"スタイル名\",\"単品\"],[\"カラー\",\"ボルドー\"]]");
		param.put("num", "1");
		param.put("productEntityId", "4780644");
		param.put("sign", "0");
		param.put("productName","iPhone SE ケース");
		param.put("title","iPhone 5 カバー iPhone se ケース BENTOBEN iphone 5s ケース スリム 超薄 Diamonds ダイヤモンド 　電波影響無しソフトTPU 衝撃吸収 バンパー 落下防止 アイフォン 7 カバー ピンク");
		param.put("position","30");
		param.put("keywordUrl","http://www.amazon.co.jp/dp/B0732J856C");
		System.out.println(autoBuy.selectProduct(param));
//		Map<String, String> param1 = new HashMap<>();
//		param1.put("url", "http://haitao.bibiwo.com/j?t=http://www.amazon.co.jp/dp/B01EL660V6?tag=adiemar100052-22");
//		//param.put("sku", "[[\"color\",\"Red\"],[\"Special Size\",\"Little Boys\"],[\"size\",\"4\"]]");
////		//param.put("sku", "[[\"color\",\"Red\"]]");
//		param1.put("sku", "[[\"サイズ\",\"1袋\"]]");
//		param1.put("num", "1");
//		param1.put("productEntityId", "4780644");
//		param1.put("num", "1");
//		//param.put("sign", "0");
//		System.out.println(autoBuy.selectProduct(param1));
	}
}
