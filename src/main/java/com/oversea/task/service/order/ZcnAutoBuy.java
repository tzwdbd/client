package com.oversea.task.service.order;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.Address;
import com.oversea.task.domain.ExpressNode;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class ZcnAutoBuy extends AutoBuy {
	
	private final Logger logger = Logger.getLogger(getClass());

	public ZcnAutoBuy(boolean isWap) {
		super(isWap);
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		if (isWap) {
			logger.debug("--->调整浏览器尺寸和位置");
			driver.manage().window().maximize();
			Utils.sleep(3000);
			driver.get("https://www.amazon.cn");
			Utils.sleep(3000);

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			try {
				By bySignIn = By.xpath("//a[@id='nav-logobar-greeting']");
				WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(bySignIn));
				TimeUnit.SECONDS.sleep(1);
				logger.debug("--->跳转到登录页面");
				driver.get(signIn.getAttribute("href"));
				Utils.sleep(3000);
			} catch (Exception e) {
				logger.error("--->没有找到登陆按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email")));
				WebElement username = driver.findElement(By.id("ap_email"));
				logger.debug("--->输入账号");
				username.sendKeys(userName);
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->没有找到输入框", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			try {
				WebElement password = driver.findElement(By.id("ap_password"));
				logger.debug("--->输入密码");
				password.sendKeys(passWord);
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->没有找到密码框", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			try {
				WebElement btn = driver.findElement(By.id("signInSubmit"));
				logger.debug("--->开始登陆");
				btn.click();
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->没有找到登陆确定按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@id='nav-greeting-name' and contains(text(),'您好')]")));
			} catch (Exception e) {
				logger.error("--->登陆失败,开始判断和处理账号异常");
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
			logger.debug("--->登陆成功,开始跳转");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
			
		} else {
			logger.debug("--->调整浏览器尺寸和位置");
			driver.manage().window().maximize();

			driver.get("https://www.amazon.cn/");
			WebDriverWait wait = new WebDriverWait(driver, 40);
			try
			{
				WebElement loginBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-link-yourAccount")));
				logger.debug("--->打开登录框");
				loginBtn.click();
			}
			catch (Exception e)
			{
				logger.error("--->没有找到登录框", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			
			try
			{
				WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("ap_email")));
				username.sendKeys(userName);
				logger.debug("--->输入账号");
				TimeUnit.SECONDS.sleep(5);
			}
			catch (Exception e)
			{
				logger.error("--->没有找到输入框", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}

			try
			{
				WebElement password = driver.findElement(By.id("ap_password"));
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
				WebElement btn = driver.findElement(By.id("signInSubmit"));
				TimeUnit.SECONDS.sleep(5);
				btn.click();
				logger.debug("--->开始登陆");
			}
			catch (Exception e)
			{
				logger.error("--->没有找到登陆确定按钮", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			
			try
			{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#nav-link-yourAccount[href='/gp/css/homepage.html/ref=nav_youraccount_btn']")));
			}
			catch (Exception e)
			{
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		}
	}
	
	AutoBuyStatus clickCart() {
		By cartBy = By.id("nav-button-cart");
		WebElement cartBtn = null;
		try {
			TimeUnit.SECONDS.sleep(2);
			cartBtn = driver.findElement(cartBy);
			logger.debug("--->去购物车");
			cartBtn.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			try {
				cartBy = By.id("navbar-icon-cart");
				cartBtn = driver.findElement(cartBy);
				logger.debug("--->去购物车");
				cartBtn.click();
				Utils.sleep(1500);
			} catch (Exception e2) {
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		AutoBuyStatus status = clickCart();
		if (!status.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) {
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		logger.debug("--->清空购物车");
		try {
			// 商品不能多,否则会分页
			List<WebElement> goodsInCart = driver.findElements(By.className("sc-action-delete"));
			if (goodsInCart != null) {
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				for (int i = 0; i < goodsInCart.size(); i++) {
					try {
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						// 停止5秒
						goodsInCart.get(i).click();
						TimeUnit.SECONDS.sleep(4);
					} catch (Exception e) {
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}
			} else {
				logger.info("购物车为空!");
			}

			try {
				logger.debug("--->继续购物,再次跳转");
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				By by = By.xpath("//a[@class='a-button-text' and  contains(text(),'继续购物')]");
				wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				WebElement go = driver.findElement(by);
				go.click();

				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				logger.debug("--->再次跳转失败", e);
			}
		} catch (Exception e) {
			logger.error("--->选择需要删除的商品出错", e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		AutoBuyStatus status = deleteIdcard();
		if(!AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("productUrl = " + productUrl);
		logger.debug("--->选择商品");
		
		for (int i = 0; i < 3; i++) {
			try {
				driver.navigate().to(productUrl);
				TimeUnit.SECONDS.sleep(3);
				waitForMainPanel();
				break;
			} catch (Exception e) {
				if (i == 2) {
					logger.debug("--->打开商品页面失败 :" + productUrl);
					return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
				}
			}
		}
		
		// 领取优惠
		List<WebElement> promotionList = driver.findElements(By.xpath("//div[contains(text(), '促销信息')]/following-sibling::div[1]"));
		if (promotionList != null && promotionList.size() > 0) {
			try {
				WebElement button = promotionList.get(0).findElement(By.cssSelector("div div a"));
				button.click();
				TimeUnit.SECONDS.sleep(3);
				List<WebElement> buttons = driver.findElements(By.xpath("//span[@class='a-size-base' and contains(text(),'领优惠码')]/parent::span/parent::span[@class='a-button-inner']"));
				while (buttons != null && buttons.size() > 0) {
					buttons.get(0).click();
					Utils.sleep(1500);
					buttons = driver.findElements(By.xpath("//span[@class='a-size-base' and contains(text(),'领优惠码')]/parent::span/parent::span[@class='a-button-inner']"));
				}
				WebElement returnButton = driver.findElement(By.xpath("//div[@id='promotion-upsell']/parent::div/parent::div/preceding-sibling::div[1]/div/div/a"));
				returnButton.click();
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.debug("--->领取优惠失败 :", e);
				return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
			}
		}

		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		
		// 开始选择sku
		logger.debug("--->开始选择sku");
		try {
			if (sku != null) {
				List<String> skuList = Utils.getSku((String) sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						logger.debug("选择sku" + skuList.get(i - 1) + ":" + skuList.get(i));
						String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
						
						List<WebElement> chooseELes = driver.findElements(By.xpath("//h2[@class='a-box-title' and contains(text(), '选择" + keyStr + "')]"));
						if (chooseELes != null && chooseELes.size() > 0) {
							try {
								valueClick(driver, skuList.get(i));
							} catch (Exception e) {
								logger.debug("找不到valueElement url= " + productUrl + "&& sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
							Utils.sleep(2500);
							continue;
						}
						WebElement panel = waitForMainPanel();

						// 不需要选择的sku, 比如one size
						boolean hasOneSize = false;
						List<WebElement> list = null;
						try {
							list = panel.findElements(By.xpath(".//div[@class='a-row a-spacing-small']"));
							if (list != null && list.size() > 0) {
								for (WebElement e : list) {
									if (e != null) {
										String v = e.getText();
										if (!Utils.isEmpty(v) && v.indexOf(skuList.get(i)) != -1) {
											hasOneSize = true;
											break;
										}
									}
								}
							}
						} catch (Exception e) {
						}
	
						List<WebElement> clicks = driver.findElements(By.cssSelector("div.twisterButton.nocopypaste"));
						for (WebElement w : clicks) {
							try {
								w.findElement(By.cssSelector("span.a-declarative"));
							} catch (Exception e) {
								String text = w.getText();
								if (StringUtil.isNotEmpty(text) && text.contains(skuList.get(i))) {
									hasOneSize = true;
									break;
								}
							}
						}
	
						if (hasOneSize) {
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
							if (!hasOneSize) {
								// 精确匹配
								for (WebElement w : list0) {
									WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
									if (ww != null && StringUtil.isNotEmpty(ww.getText())) {
										String[] ss = ww.getText().split("\n");
										String text = ss[0].replace(":", "").trim();
										if (keyStr.equals(text) || keyStr.trim().equalsIgnoreCase(text)) {
											keyElement = w;
											break;
										}
									}
								}
								// 模糊匹配
								if (keyElement == null) {
									for (WebElement w : list0) {
										WebElement ww = w.findElement(By.cssSelector("div.a-column.a-span9"));
										if (ww != null && StringUtil.isNotEmpty(ww.getText())) {
											String[] ss = ww.getText().split("\n");
											String text = ss[0].replace(":", "").trim();
											if (StringUtil.isNotEmpty(text)
													&& text.toLowerCase().contains(keyStr.toLowerCase().trim())) {
												keyElement = w;
												break;
											}
										}
									}
								}
							}
							
//							//再次寻找 分词匹配
//							if (keyElement == null && !Utils.isEmpty(keyStr)){
//								List<WebElement> keyList = driver.findElements(By.xpath("//div[@class='a-column a-span9']"));
//								keyElement = findClosedWelement(keyStr, keyList);
//							}
//							
//							if (keyElement == null) {
//								List<WebElement> l = driver.findElements(By.xpath("//button[@class='a-button-text a-text-left']"));
//								if (l != null && l.size() > 0) {
//									for (WebElement w : l) {
//										if (w != null) {
//											String text = w.getText();
//											if (StringUtil.isNotEmpty(text) && text.toLowerCase().contains(keyStr.toLowerCase())) {
//												keyElement = w;
//												break;
//											}
//										}
//									}
//								}
//							}

							if (keyElement == null) {
								logger.debug("找不到keyElement url= " + productUrl + "&& sku = " + (skuList.get(i - 1) + ":" + skuList.get(i)));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
							Utils.sleep(2000);
							
							logger.debug("--->自动选择:[" + skuList.get(i - 1) + " = " + skuList.get(i) + "]");
							keyElement.click();
							Utils.sleep(1500);
							try {
								valueClick(driver, skuList.get(i));
							} catch (Exception e) {
								logger.debug("找不到valueElement url= " + productUrl + "&& sku = "
										+ (skuList.get(i - 1) + ":" + skuList.get(i)));
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
							Utils.sleep(2500);
						}
					}
				}
			}
			// 等待最大的选择面板可见
			waitForMainPanel();
			
			// 获取单价
			try {
				WebElement singlePrice = driver.findElement(By.xpath("//span[@id='priceblock_ourprice']"));
				if (singlePrice != null) {
					String singlePriceStr = singlePrice.getText();
					if (!Utils.isEmpty(singlePriceStr)) {
						String productEntityId = param.get("productEntityId");
						logger.error("productEntityId = " + productEntityId);
						int index = singlePriceStr.indexOf("(");
						if (index != -1) {
							singlePriceStr = singlePriceStr.substring(0, index).trim();
						}
						// $24.99 21.91 ($0.18 / Count)
						if (singlePriceStr.startsWith("￥") && singlePriceStr.length() > 1) {
							singlePriceStr = singlePriceStr.substring(1).replace(" ", ".");
							if (singlePriceStr.startsWith(".")) {
								singlePriceStr = singlePriceStr.replaceFirst(".", "");
							}
							logger.debug("singlePriceStr:" + singlePriceStr);
							if (StringUtil.isNotEmpty(productEntityId)) {
								priceMap.put(productEntityId, singlePriceStr);
							}
						} else {
							if (StringUtil.isNotEmpty(productEntityId)) {
								priceMap.put(productEntityId, singlePriceStr);
							}
						}
					}
				}
			} catch (Exception e) {
				try {
					WebElement pricePanel = driver.findElement(By.cssSelector("table#price td#priceblock_dealprice > span"));
					String priceStr = pricePanel.getText();
					if (!Utils.isEmpty(priceStr)) {
						logger.error("--->单价:" + priceStr.replace("￥", ""));
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("￥", ""));
					}
				} catch (Exception ep) {
					try {
						WebElement pricePanel = driver.findElement(By.xpath("//span[@id='product-price']"));
						String priceStr = pricePanel.getText();
						if (!Utils.isEmpty(priceStr)) {
							logger.error("--->单价:" + priceStr.replace("￥", ""));
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("￥", ""));
						} else {
							logger.error("--->单价获取失败");
						}
					} catch (Exception ex) {
						logger.error("--->获取单价失败");
					}
				}
			}
			
			try {
				WebDriverWait wait0 = new WebDriverWait(driver, 15);
				wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("availability")));
				WebElement availability = driver.findElement(By.cssSelector("div#availability > span"));
				String stockNum = availability.getText().toLowerCase();
				logger.info("--->库存状态:" + stockNum);
				if (stockNum.contains("out of stock") || stockNum.contains("in stock on")) {// TODO
					logger.warn("--->该商品已经下架:" + productUrl);
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			} catch (Exception e) {
			}
			
			// 选择数量
			try {
				if (!productNum.equals("1")) {
					Select select = new Select(driver.findElement(By.id("mobileQuantityDropDown")));
					Utils.sleep(2000);
					driver.executeScript("window.scrollBy(0,150);");
					select.selectByValue(productNum);
					Utils.sleep(2000);
					WebElement numBtn = driver.findElement(By.xpath("//span[@class='a-button a-button-dropdown a-button-small']"));
					String txt = numBtn.getText();
					logger.info("--->选择数量结果:" + txt);
					if (!txt.contains(productNum)) {
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
			} catch (Exception e) {
				logger.error("选择数量失败 pruductNum = " + productNum);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
			
//			// 加购物车按钮
//			try {
//				driver.findElement(By.id("buybox.addToCart")).click();
//			} catch (Exception e) {
//				logger.debug("寻找购物车按钮异常11112121");
//			}
//			Utils.sleep(1000);
			try {
				driver.findElement(By.cssSelector("input#add-to-cart-button")).click();
			} catch (Exception e) {
				try {
					driver.findElement(By.xpath("//button[contains(text(),'加入购物车')]")).click();
				} catch (Exception e2) {
					logger.debug("寻找购物车按钮异常");
				}
			}
			Utils.sleep(3000);
			
			try {
				WebElement element = driver.findElement(By.xpath("//span[contains(text(),'这个商品在您的购物车中。')]"));
				if (element != null) {
					return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
				}
			} catch (Exception e) {
			}
			
			try {
				driver.findElement(By.id("no_thanks_button")).click();
			} catch (Exception e) {
			}
			Utils.sleep(3000);
			try {
				driver.findElement(By.xpath("//a[@id='edit-cart-button-announce']")).click();
			} catch (Exception e) {
			}
			Utils.sleep(1000);
			try {
				driver.findElement(By.xpath("//a[@id='a-autoid-0-announce']")).click();
			} catch (Exception e) {
			}
			Utils.sleep(1000);

			try {
				driver.findElement(By.xpath("//a[contains(text(),'check out')]")).click();
				Utils.sleep(1000);
			} catch (Exception e) {
			}

			// 等待购物车加载完成
			try {
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='sc-buy-box']")));
			} catch (Exception e) {
				logger.error("等待购物车加载完成出错,e");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}

			// 判断是否有alert
			try {
				WebElement w = driver.findElement(By.cssSelector("div.a-alert-content"));
				if (w != null && StringUtil.isNotEmpty(w.getText())
						&& w.getText().contains("Important messages for items in your Cart")) {
					logger.error("购物车页面弹出警告标记");
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
			} catch (Exception e) {
			}

			logger.debug("选择sku成功");
			return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
			
		} catch (Exception e) {
			logger.debug("--->选择sku碰到异常", e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
	}
	
	private void valueClick(FirefoxDriver driver, String valueStr) throws Exception {
		// 判断是否有sku选择新样式
		TimeUnit.SECONDS.sleep(3);
		try {
			WebElement panel = driver.findElement(By.xpath("//div[@id='twister_bottom_sheet']"));
			logger.error("找到选sku新样式-底部弹出样式");
			List<WebElement> list = panel.findElements(By.xpath(".//ul/li/span/span/span/span/label"));
			if (list != null && list.size() > 0 && StringUtil.isNotEmpty(valueStr)) {
				for (WebElement w : list) {
					if (w != null && valueStr.equalsIgnoreCase(w.getText())) {
						w.click();
						Utils.sleep(1500);
						break;
					}
				}
			}
			TimeUnit.SECONDS.sleep(4);
			try {
				WebElement w = driver.findElement(By.xpath("//span[@class='a-sheet-close']"));
				w.click();
				Utils.sleep(1000);
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", w);
				Utils.sleep(1000);
			} catch (Exception e) {
				logger.error("关闭底部弹出出错", e);
			}
			return;
		} catch (Exception e) {
			logger.error("没有找到选sku新样式-底部弹出样式", e);
		}

		WebDriverWait wait1 = new WebDriverWait(driver, 30);
		boolean isSuccess = false;
		for (int i = 0; i < 4; i++) {
			try {
				isSuccess = true;
				String temp = String.format("\"label\":\"%s\"", valueStr);
				String format1 = String.format("//span[@class='a-declarative' and contains(@data-mobile-twister, '%s')]", temp);
				By value = By.xpath(format1);
				WebElement valueElement = wait1.until(ExpectedConditions.visibilityOfElementLocated(value));// ExpectedConditions.elementToBeClickable(value)
				Utils.sleep(2000);
				driver.executeScript("var tar=arguments[0];var top=tar.offsetTop;window.scrollTo(0,top);", valueElement);
				Utils.sleep(6000);
//				WebElement stockEle = valueElement.findElement(By.cssSelector("div.twister-dimsum > span.a-color-secondary"));
//				if (stockEle.getText().contains("无法提供")) {
//					throw new Exception();
//				}
				valueElement.click();
			} catch (Exception e) {
				isSuccess = false;
				List<WebElement> elements = driver.findElements(By.xpath("//h4"));
				if (elements != null && elements.size() > 0) {
					for (WebElement w : elements) {
						if (w != null && StringUtil.isNotEmpty(w.getText())
								&& valueStr.equalsIgnoreCase(w.getText().trim())) {
//							WebElement stockEle = w.findElement(By.xpath("//following-sibling::div[@class='twister-dimsum']/span[@class='a-color-secondary']"));
//							if (stockEle.getText().contains("无法提供")) {
//								throw new Exception();
//							}
							w.click();
							isSuccess = true;
							Utils.sleep(2000);
							break;
						}
					}
				}
				if (!isSuccess) {
					String js = "var q=document.documentElement.scrollTop=10000";
					driver.executeScript(js);
					Utils.sleep(3000);
				}
			}
			if (isSuccess) {
				break;
			}
		}
		if (!isSuccess) {
			throw new Exception();
		}
	}
	
	private WebElement waitForMainPanel() {
		By byPanel = By.xpath("//div[@id='ppd']");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(byPanel));
		Utils.sleep(2000);
		return panel;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param, UserTradeAddress address, OrderPayAccount payAccount) {
		String myPrice = param.get("my_price");
		
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		// 设置价格
		logger.error("--->myPrice = " + myPrice);
		
		AutoBuyStatus status0 = clickCart();
		if (!status0.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) {
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		// 等待购物车页面加载完成
		WebDriverWait wait = new WebDriverWait(driver, 15);
		By by = By.xpath("//button[@class='a-button-text' and contains(text(), '进入结算中心')]");
		WebElement checkout = null;
		try {
			checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		//去付款
		logger.debug("--->去付款");
		checkout.click();
		
		AutoBuyStatus status = chooseAddress(address, payAccount);
		if(!AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS.equals(status)){
			return status;
		}
		
		try {
			List<WebElement> payment = driver.findElements(By.cssSelector("div.a-box.payment-box.payment-box-aui-template"));
			if (payment != null && payment.size() > 0) {
				payment.get(0).click();
				WebElement creditCardNumber = driver.findElement(By.id("addCreditCardNumber"));
				creditCardNumber.sendKeys(param.get("cardNo"));
				WebElement submit = driver.findElement(By.cssSelector("span#confirm-card span input"));
				submit.click();
				Utils.sleep(2000);
				WebElement continueButton = driver.findElement(By.id("continueButton"));
				continueButton.click();
				Utils.sleep(5000);
			}
		} catch (Exception e) {
			logger.error("--->选择支付方式失败", e);
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		
		try{
//			WebElement setOrderingPrefsCheckbox = driver.findElement(By.cssSelector("div[data-a-input-name=setOrderingPrefs]"));
//			setOrderingPrefsCheckbox.click();
			
			// 优惠码 spc-gcpromoinput
			boolean isEffective = false;
			Set<String> promotionList = getPromotionList(param.get("promotion"));
			if (promotionList != null && promotionList.size() > 0) {
				try {
					WebElement promotionInput = driver.findElement(By.id("spc-gcpromoinput"));
					WebElement applyText = driver.findElement(By.id("apply-text"));
					for (String promotion : promotionList) {
						logger.debug("输入优惠码" + promotion);
						Utils.sleep(1500);
						promotionInput.sendKeys(promotion);
						Utils.sleep(1500);
						applyText.click();
						Utils.sleep(3000);
						WebElement isPromoSuccess = driver.findElement(By.id("promo-success"));
						if (isPromoSuccess.isDisplayed() && isPromoSuccess.getText().contains("你已成功获得促销代码对应的优惠。")) {
							isEffective = true;
						}
					}
				} catch (Exception e) {
					logger.error("输入优惠码的时候错误", e);
				}
				if ("true".equals(param.get("isStock")) && !isEffective) {
					logger.debug("--->囤货订单优惠码失效,中断采购");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
			
			String totalPrice = "0";
			try {
				WebElement w = driver.findElement(By.xpath("//td[contains(text(), '订单总计：')]/following-sibling::td[1]"));
				if (StringUtil.isNotEmpty(w.getText())) {
					totalPrice = w.getText().replace("￥", "").replace("-", "").replace(",", "").trim();
					logger.error("totalPrice0 = " + totalPrice);
				}
			} catch (Exception e) {
				logger.error("--->查找除去运费的总价失败", e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
			
			logger.debug("--->开始比价[" + myPrice + "," + totalPrice + "]");
			BigDecimal x = new BigDecimal(myPrice);
			BigDecimal y = new BigDecimal(totalPrice);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, totalPrice);
			BigDecimal v = y.subtract(x);
			if (v.doubleValue() > 20.00D) {
				logger.error("--->总价差距超过约定,不能下单");
				return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
			}
			doScreenShot();
			
			try {
				WebElement checkbox = driver.findElement(By.cssSelector("label[for=rcx-checkout-spc-nonrefundable-confirm-checkbox]"));
				checkbox.click();
			} catch (Exception e) {
				logger.debug("--->无必选项");
			}
			
			by = By.xpath("//span[@class='a-button-text' and contains(text(), '提交订单')]");
			WebElement submitButton = driver.findElement(by);
			submitButton.click();
			Utils.sleep(5);
			try {
				List<WebElement> divs = driver.findElements(By.xpath("//div[@class='a-box-inner']"));
				if (divs != null && divs.size() > 0) {
					for (WebElement div : divs) {
						String txt = div.getText();
						if (txt.indexOf("查看或更改您的订单") > -1) {
							logger.debug("--->找到了查看按钮");
							div.click();
							break;
						}
					}
				}
				Utils.sleep(5000);
				WebElement orderEle = driver.findElement(By.xpath("//p[@class='a-size-base a-color-secondary' and contains(text(),'订单编号')]/parent::div/following-sibling::div[1]/p[@class='a-size-base']"));
				String orderNo = orderEle.getText();
				if (!Utils.isEmpty(orderNo)) {
					logger.error("--->获取zcn单号成功:\t" + orderNo);
					savePng();
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
					return AutoBuyStatus.AUTO_PAY_SUCCESS;
				} else {
					logger.error("--->获取zcn单号出错!");
					return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
				}
			} catch (Exception e) {
				logger.error("--->获取zcn单号出错!", e);
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->提交订单失败", e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
	}
	
	AutoBuyStatus deleteIdcard() {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		driver.navigate().to("https://www.amazon.cn/gp/aw/kyc?ie=UTF8&ref_=hp_ags_customer_clearance__kyc");
		try {
			List<WebElement> addresses = driver.findElements(By.cssSelector("span.a-list-item > a.a-touch-link.a-box.a-touch-link-noborder"));
			while (addresses != null && addresses.size() > 0) {
				addresses.get(0).click();
				List<WebElement> checkouts = driver.findElements(By.cssSelector("span.a-button.a-button-small > span.a-button-inner > span.a-button-text"));
				out: while (checkouts != null && checkouts.size() > 0) {
					for (WebElement checkout : checkouts) {
						if (checkout.getText().contains("删除")) {
							checkout.click();
							Utils.sleep(2000);
							By by = By.cssSelector("div#a-popover-1 div.a-popover-footer > span:nth-child(2)");
							WebElement subCheckout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
							subCheckout.click();
							Utils.sleep(5000);
							break out;
						}
					}
				}
				addresses = driver.findElements(By.cssSelector("span.a-list-item > a.a-touch-link.a-box.a-touch-link-noborder"));
			}
			return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
		} catch (Exception e) {
			logger.error("--->删除身份证失败", e);
			try {
				List<WebElement> addresses = driver.findElements(By.cssSelector("span.a-list-item > a.a-touch-link.a-box.a-touch-link-noborder"));
				while (addresses != null && addresses.size() > 0) {
					addresses.get(0).click();
					List<WebElement> checkouts = driver.findElements(By.cssSelector("span.a-button.a-button-small > span.a-button-inner > span.a-button-text"));
					out: while (checkouts != null && checkouts.size() > 0) {
						for (WebElement checkout : checkouts) {
							if (checkout.getText().contains("删除")) {
								checkout.click();
								Utils.sleep(2000);
								By by = By.cssSelector("div#a-popover-1 div.a-popover-footer > span:nth-child(2)");
								WebElement subCheckout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
								subCheckout.click();
								Utils.sleep(5000);
								break out;
							}
						}
					}
					addresses = driver.findElements(By.cssSelector("span.a-list-item > a.a-touch-link.a-box.a-touch-link-noborder"));
				}
				return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
			} catch (Exception e1) {
				logger.error("--->删除身份证失败", e1);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
	}
	
	AutoBuyStatus chooseAddress(Address address, OrderPayAccount payAccount) {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		// 格式化地址，使之于中亚下拉框匹配
		relapceAddress(address);
		
		AutoBuyStatus status = deleteAddress();
		if (!status.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) {
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			for (int i = 0; i < 3; i++) {
				WebElement fullName = driver.findElement(By.id("enterAddressFullName"));
				fullName.sendKeys(address.getName());
				TimeUnit.SECONDS.sleep(1);
				WebElement state = driver.findElement(By.id("chooseState"));
				Select stateSelect = new Select(state);
				stateSelect.selectByVisibleText(address.getState());
				TimeUnit.SECONDS.sleep(1);
				WebElement city = driver.findElement(By.id("chooseCity"));
				logger.error("disabled" + city.getAttribute("disabled"));
				if (!"true".equals(city.getAttribute("disabled"))) {
					Select citySelect = new Select(city);
					citySelect.selectByVisibleText(address.getCity());
					TimeUnit.SECONDS.sleep(1);
				}
				
				WebElement district = driver.findElement(By.id("chooseDistrict"));
				Select districtSelect = new Select(district);
				districtSelect.selectByVisibleText(address.getDistrict());
				TimeUnit.SECONDS.sleep(1);
				WebElement addressLine = driver.findElement(By.id("enterAddressAddressLine1"));
				addressLine.sendKeys(address.getAddress());
				TimeUnit.SECONDS.sleep(1);
				WebElement postalCode = driver.findElement(By.id("enterAddressPostalCode"));
				postalCode.sendKeys(address.getZip());
				TimeUnit.SECONDS.sleep(1);
				WebElement phoneNumber = driver.findElement(By.id("enterAddressPhoneNumber"));
				phoneNumber.sendKeys(address.getMobile());
				
				Utils.sleep(2000);
				By by = By.xpath("//input[@class='a-button-input submit-button-with-name' and @name='shipToThisAddress']");
				WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				checkout.click();
				Utils.sleep(3000);
				List<WebElement> alerts = driver.findElements(By.cssSelector("div.a-box-inner.a-alert-container h4.a-alert-heading"));
				if (alerts.size() == 0) {
					logger.error("填写地址break");
					break;
				} else {
					logger.error("填写地址失败，重试");
					driver.navigate().refresh();
					Utils.sleep(3000);
				}
			}
			
		} catch (Exception e) {
			logger.error("--->填写地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		try {
			By by = By.xpath("//span[@class='a-button-text' and contains(text(), '配送到此地址')]");
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			checkout.click();
		} catch (Exception e) {
			logger.error("--->填写地址失败", e);
			try {
				List<WebElement> checkouts = driver.findElements(By.cssSelector(".a-button-input"));
				for(WebElement w:checkouts){
					if(w.isDisplayed()){
						w.click();
						break;
					}
				}
			} catch (Exception e1) {
				logger.error("--->填写地址失败", e1);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		Utils.sleep(2000);
		
		try {
			By by = By.xpath("//input[@class='a-button-input' and @value='继续']");
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			checkout.click();
		} catch (Exception e) {
			logger.error("--->选择配送方式失败");
			try {
				List<WebElement> checkouts = driver.findElements(By.cssSelector(".a-button-input"));
				for(WebElement w:checkouts){
					if(w.isDisplayed()){
						w.click();
						break;
					}
				}
			} catch (Exception e1) {
				logger.error("--->填写地址失败", e1);
			}
		}
		Utils.sleep(2000);
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("kyc-number")));
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		try {
			WebElement kycNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("kyc-number")));
			kycNumber.sendKeys(address.getIdCard());
			logger.error("--->IdCard="+address.getIdCard());
			TimeUnit.SECONDS.sleep(1);
			Select year = new Select(driver.findElement(By.id("kyc-expiration-year-dropdown")));
			logger.error("--->ExpireDate="+address.getExpireDate());
			year.selectByVisibleText(address.getExpireDate().substring(0,4));
			TimeUnit.SECONDS.sleep(1);
			Select month = new Select(driver.findElement(By.id("kyc-expiration-month-dropdown")));
			String monthStr = address.getExpireDate().substring(4,6);
			if(monthStr.startsWith("0")){
				monthStr = monthStr.substring(1);
			}
			month.selectByVisibleText(monthStr);
			TimeUnit.SECONDS.sleep(1);
			Select day = new Select(driver.findElement(By.id("kyc-expiration-day-dropdown")));
			String dayStr = address.getExpireDate().substring(6,8);
			if(dayStr.startsWith("0")){
				dayStr = dayStr.substring(1);
			}
			day.selectByVisibleText(dayStr);
			TimeUnit.SECONDS.sleep(1);
			
			try {
				WebElement alertContent = driver.findElement(By.xpath("//div[@class='a-alert-content' and contains(text(),'请不要选择过期的日期')]"));
				if (alertContent != null && alertContent.isDisplayed()) {
					return AutoBuyStatus.AUTO_PAY_SELECT_EXPIRE_DATE_OVERDUE;
				}
			} catch (Exception e) {}
			
			driver.executeScript("(function(){window.scrollBy(0,200);})();");
			TimeUnit.SECONDS.sleep(1);
			WebElement photoFront = driver.findElement(By.cssSelector("input[name=kyc-front-photo]"));// TODO
			String front = ""+System.currentTimeMillis()/1000;
			logger.error("--->front="+address.getIdCardFront());
			download(address.getIdCardFront(), "C://auto//screenshot//"+front+".png");
			Utils.sleep(1000);
			photoFront.sendKeys("C:\\auto\\screenshot\\"+front+".png");
			Utils.sleep(25000);
			WebElement photoBack = driver.findElement(By.cssSelector("input[name=kyc-back-photo]"));
			String back = ""+System.currentTimeMillis()/1000;
			logger.error("--->back="+address.getIdCardBack());
			download(address.getIdCardBack(), "C://auto//screenshot//"+back+".png");
			Utils.sleep(1000);
			photoBack.sendKeys("C:\\auto\\screenshot\\"+back+".png");
			Utils.sleep(25000);
			WebDriverWait wait0 = new WebDriverWait(driver, 45);
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("kyc-continue-button-announce")));
			WebElement checkout = driver.findElement(By.id("kyc-continue-button-announce"));
			checkout.click();
			Utils.sleep(5000);
			logger.error("--->设置身份证完成");
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
		} catch (Exception e) {
			logger.error("--->选择地址失败",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_EXPIRE_DATE_OVERDUE;
		}
	}
	
	public static void download(String urlString, String filename) throws Exception {
		OutputStream os = null;
		InputStream is = null;
		try{
		    // 构造URL
		    URL url = new URL(urlString);
		    // 打开连接
		    URLConnection con = url.openConnection();
		    // 输入流
		    is = con.getInputStream();
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
	
	
	AutoBuyStatus deleteAddress() {
		WebDriverWait wait = new WebDriverWait(driver, 15);
		//driver.navigate().to("https://www.amazon.cn/gp/buy/addressselect/handlers/display.html?hasWorkingJavascript=1");
		try {
			List<WebElement> addresses = driver.findElements(By.cssSelector("div.displayAddressDiv ul.displayAddressUL"));
			while (addresses != null && addresses.size() > 0) {
				By by = By.xpath("//a[@class='a-button-text deletebutton' and contains(text(), '删除')]");
				WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
				checkout.click();
				try {
					Alert alert = driver.switchTo().alert();
			        if (alert != null) {
			        	alert.accept();
					}
				} catch (Exception e) {
				}
				Utils.sleep(2000);
				addresses = driver.findElements(By.cssSelector("div.displayAddressDiv ul.displayAddressUL"));
			}
		} catch (Exception e) {
			logger.error("--->删除地址失败", e);
			try {
				WebElement changeAddr = driver.findElement(By.id("change-shipping-address"));
				changeAddr.click();
				Utils.sleep(2000);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.displayAddressDiv ul.displayAddressUL")));
				List<WebElement> addresses = driver.findElements(By.cssSelector("div.displayAddressDiv ul.displayAddressUL"));
				while (addresses != null && addresses.size() > 0) {
					By by = By.xpath("//a[@class='a-button-text deletebutton' and contains(text(), '删除')]");
					WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
					checkout.click();
					try {
						Alert alert = driver.switchTo().alert();
				        if (alert != null) {
				        	alert.accept();
						}
					} catch (Exception e1) {
					}
					Utils.sleep(2000);
					addresses = driver.findElements(By.cssSelector("div.displayAddressDiv ul.displayAddressUL"));
				}
			} catch (Exception e2) {
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			
			
		}
		
		List<WebElement> continueShopping = driver.findElements(By.id("a-autoid-0-announce"));
		if (continueShopping != null && continueShopping.size() > 0) {
			logger.error("--->没有地址，自动跳转到购物车");
			continueShopping.get(0).click();
			Utils.sleep(2000);
			return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
		}
		
		By by = By.xpath("//h1[@class='a-size-large a-spacing-base' and contains(text(), '输入新的配送地址')]");
		try {
			WebElement newAddrress = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			if (newAddrress == null) {
				logger.error("null");
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			logger.debug("--->删除地址完成");
			return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
		} catch (Exception e) {
			logger.error("--->删除地址失败", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

	}
	
	public void relapceAddress(Address address) {
		address.setState(address.getState().replaceAll("省", ""));
		if ("上海市".equals(address.getState())) {
			address.setState("上海");
		} else if ("重庆市".equals(address.getState())) {
			address.setState("重庆");
		} else if ("北京市".equals(address.getState())) {
			address.setState("北京");
		} else if ("天津市".equals(address.getState())) {
			address.setState("天津");
		}
		if (address.getState().contains("内蒙古")) {
			address.setState("内蒙古");
		} else if (address.getState().contains("广西")) {
			address.setState("广西");
		} else if (address.getState().contains("新疆")) {
			address.setState("新疆");
		} else if (address.getState().contains("宁夏")) {
			address.setState("宁夏");
		} else if (address.getState().contains("西藏")) {
			address.setState("西藏");
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		String mallOrderNo = detail.getMallOrderNo();
		
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			String orderUrl ="https://www.amazon.cn/gp/your-account/order-details/ref=oh_aui_or_o02_?ie=UTF8&orderID="+ mallOrderNo;
			driver.navigate().to(orderUrl);
			try {
				driver.findElement(By.cssSelector(".a-button-stack a")).click();
			} catch (Exception e) {
				WebElement cancelled = driver.findElement(By.cssSelector(".a-alert-heading"));
				if(cancelled.getText().contains("取消")){
					logger.error("该订单已取消");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
			}
			Utils.sleep(10000);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".top")));
			
			//点击显示更多
			try {
				driver.findElement(By.cssSelector(".a-expander-header")).click();
			} catch (Exception e) {
			}
			List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
			List<WebElement> boxList = driver.findElements(By.cssSelector(".a-span-last .a-box"));
			String day = null;
			for(WebElement w:boxList){
				if(w.getAttribute("class").contains("a-color-alternate-background")){
					day = w.getText().trim();
					if(day.contains("最新状态")){
						day = day.substring(5).trim();
				    }
					day = day.split("日")[0];
				}else{
					if(day!=null){
						WebElement timeEle = w.findElement(By.cssSelector(".ship-track-grid-fixed-column"));
						WebElement nameEle = w.findElement(By.cssSelector(".ship-track-grid-responsive-column"));
						ExpressNode expressNode = new ExpressNode();
						expressNode.setOrderNo(detail.getOrderNo());
						expressNode.setExpressNo(mallOrderNo);
						logger.error("--->text="+nameEle.getText().replaceAll("\n", ""));
						expressNode.setName(nameEle.getText().replaceAll("\n", ""));
						expressNode.setOccurTime(getJPdate(detail.getGmtCreate(), day, timeEle.getText()));
						expressNode.setStatus(3);
						
						nodeList.add(expressNode);
					}
				}
			}
			if(nodeList.size()>0){
				WebElement statusEle =  driver.findElement(By.cssSelector(".top"));
				String str = statusEle.getText().toLowerCase();
				if(str.contains("配送成功")){
					nodeList.get(0).setStatus(14);//已签收
				}
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, mallOrderNo);
				logger.error("--->nodeList="+str);
				getTask().addParam("expressNodeList", nodeList);
				return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
			}else{
				logger.error("该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
	}
	
	private Date getJPdate(Date orderDate,String expressDate,String hm) throws ParseException{
		int year = getfullDate(orderDate);
		if(orderDate.getMonth()>getJPmonth(expressDate)){
			year = year+1;
		}
		hm = getHM(hm);
		String fullDate = year+"年"+expressDate+" "+hm;
		return getDate(fullDate);
	}
	
	private Date getDate(String fullDate) throws ParseException{
		SimpleDateFormat sl = new SimpleDateFormat("yyyy年MM月dd hh:mm");
		return sl.parse(fullDate);
	}
	
	private String getHM(String hm) throws ParseException{
		if(hm.contains("上午")){
			hm = hm.substring(2);
		}else{
			hm = hm.substring(2);
			int h = Integer.parseInt(hm.split(":")[0])+12;
			int m = Integer.parseInt(hm.split(":")[1]);
			hm = h+":"+m; 
		}
		return hm;
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
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("https://www.amazon.cn/");
//			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper")));
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转amazon.cn主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		ZcnAutoBuy autoBuy = new ZcnAutoBuy(true);
		AutoBuyStatus status = autoBuy.login("huhy1099@foxmail.com", "hhy2240955810");
		
		Map<String, String> param = new HashMap<>();
		param.put("count", null);
		param.put("my_price", "819.00");
		param.put("userName", null);
		param.put("cardNo", "6225757555082164");
		
		UserTradeAddress address = new UserTradeAddress();
		address.setState("浙江");
		address.setCity("杭州市");
		address.setDistrict("西湖区");
		address.setAddress("西斗门路9号福地创业园9号4幢2楼海狐");
		address.setIdCard("330726199511251119");
		address.setMobile("17098041474");
		address.setZip("200001");
		address.setName("张明亮");
		address.setExpireDate("20170216");
		address.setIdCardFront("http://img.haihu.com/0117011371f4c87eb119406b91764f04d08919be.png");
		address.setIdCardBack("http://img.haihu.com/01170113a7a8e79c5973474c9e3656a52ad1a9f0.png");
		OrderPayAccount payaccount = new OrderPayAccount();
		payaccount.setAccount("huhy1099@163.com");
		payaccount.setPayPassword("0010012");
		//8KWBW73K
		
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = autoBuy.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				param.put("url", "https://www.amazon.cn/gp/aw/d/B0721V3L4B");
				// https://www.amazon.cn/gp/aw/d/B0721V3L4B
				//param.put("sku", "[[\" 颜色\",\"黄水400ml\"]]");
				param.put("num", "1");
				param.put("promotion", "CMVSPAM8");
				System.out.println(autoBuy.selectProduct(param));
			}
		}
		
		System.out.println(autoBuy.pay(param, address, null));
	}

}
