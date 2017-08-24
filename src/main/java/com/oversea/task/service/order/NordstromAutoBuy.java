package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年8月4日 下午5:04:14 
*/
public class NordstromAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	//private Timer timer;
	public NordstromAutoBuy() {
		super(false,false);
		//每三分钟监控黑色的弹出框
		
//		timer = new Timer();
//		timer.schedule(new TimerTask()
//		{
//			@Override
//			public void run()
//			{
//				driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
//				driver.executeScript("(function(){var els = document.getElementsByClassName('acsCloseButton');if(els && els[0]){els[0].click();}})();");
//				
//			}
//		}, 3000, 3000);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();

		driver.get("http://shop.nordstrom.com/");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		Utils.sleep(3000);
//		try
//		{
//			
//			WebElement loginBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign Out")));
//			logger.error("--->退出重新登陆");
//			loginBtn.click();
//		}
//		catch (Exception e)
//		{
//			logger.error("--->没有找到退出重新登陆");
//		}
		try
		{
			driver.get("https://secure.nordstrom.com/SignIn.aspx?cm_sp=SI_SP_A-_-SI_SP_B-_-SI_SP_C&origin=tab&ReturnURL=http%3A%2F%2Fshop.nordstrom.com%2F");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登录框", e);
			
		}
		try
		{
			WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_mainContentPlaceHolder_signIn_email")));
			username.clear();
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(3);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement password = driver.findElement(By.id("ctl00_mainContentPlaceHolder_signIn_password"));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(3);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			WebElement btn = driver.findElement(By.id("ctl00_mainContentPlaceHolder_signIn_enterButton"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("main")));
		}
		catch (Exception e)
		{
			logger.error("--->没找到main");
			try {
				WebDriverWait wait1 = new WebDriverWait(driver, 5);
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.id("mcp")));
			} catch (Exception e2) {
				logger.error("--->没找到mcp");
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			} 
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try
		{
			driver.get("https://secure.nordstrom.com/ShoppingBag.aspx");
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}

		logger.debug("--->清空购物车");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_shoppingBagHeader_ShoppingBagHeaderInfo")));
			
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.xpath("//a[contains(text(),'Remove')]"));
			while (true) {
				int size = list.size();
				if(list!=null && size>0){
					list.get(0).click();
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.xpath("//a[contains(text(),'Remove')]"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
			Utils.sleep(2000);
			logger.error("--->购物车页面清理完成");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		try {
			logger.error("--->确认购物车是否清空");
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_shoppingBagHeader_bagEmptyNeedInspiration")));
		} catch (Exception e) {
			logger.debug("--->购物车不为空！");
			try {
				logger.error("--->确认购物车是否清空");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_shoppingBagList_orderItemRepeater_ctl00_orderItemTableRow")));
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			} catch (Exception e1) {
				logger.debug("--->购物车为空！");
			}
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}
	
	
	WebElement whenMainPanelReady()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{	
			logger.debug("--->等待mainPanel加载完成");
			TimeUnit.SECONDS.sleep(5);
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("product-selection")));
		}
		catch (Exception e)
		{
			logger.debug("--->等待主页面加载完成出错",e);
		}
		return main;
	}
	

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		
		logger.debug("--->跳转到商品页面");
		String url = param.get("url");
		logger.debug("--->url:" + url);
		try
		{
			driver.navigate().to(url);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//商品页面找不到,商品也是下架
		try {
			WebElement unavailableText = driver.findElement(By.cssSelector("div.error-page-header"));
			if(unavailableText!=null && !StringUtil.isEmpty(unavailableText.getText())){
				logger.debug("--->这款商品的页面cannot be found错误,找不到该款商品");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {}
		
		//判断商品是否下架
		try {
			WebElement unavailableText = driver.findElement(By.cssSelector("div.unavailable-alert"));
			if(unavailableText!=null && !StringUtil.isEmpty(unavailableText.getText())){
				logger.debug("--->这款商品已经下架");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("--->找到该款商品");
		}
		driver.executeScript("(function(){window.scrollBy(0,100);})();");
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		try {
			WebElement main = whenMainPanelReady();
			
			logger.debug("--->商品页面加载完成");
			
			// 关闭黑色弹出框
			try {
				TimeUnit.SECONDS.sleep(3);
				WebElement closeBox = driver.findElement(By.className("fsrDeclineButton"));
				closeBox.click();
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {}
			
			String sku = param.get("sku");
			
			// 开始选择sku
			logger.debug("--->开始选择sku");
			Map<String, String> skuMap = new HashMap<String, String>();
			if (StringUtil.isNotEmpty(sku)) {
				
				List<String> skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrValue = skuList.get(i);
						skuMap.put(attrName, attrValue);
						
						if(attrName.equalsIgnoreCase("color")  && StringUtil.isNotEmpty(attrValue)  && !attrValue.equalsIgnoreCase("no color")  && !attrValue.equalsIgnoreCase("none")){
							try {
								WebElement colorSelect = main
										.findElement(By.xpath("//select[@class='color-select']"));
								Select select = new Select(colorSelect);
								List<WebElement> colors = select.getOptions();
								if (colors != null && colors.size() > 1) {
									int find = 0;
									for (WebElement color : colors) {
										String real = color.getText();
										String ct =  color.getAttribute("value").trim().toLowerCase();
										if (attrValue.toLowerCase().equals(ct)) {
											find = 1;
											select.selectByVisibleText(real);
											logger.debug("--->选择Color:" + attrValue);
											TimeUnit.SECONDS.sleep(2);
											driver.executeScript("(function(){window.scrollTo(0,200);})();");
											break;
										}
									}
									
									if (find == 0) {
										logger.error("[3]找不到指定sku:" + attrValue);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
								}
							} catch (Exception e) {
								logger.error("[5]样式换了" + attrValue);
								try {
									WebElement descElement = null;
									try {
										descElement = driver.findElement(By.cssSelector(".product-information"));
										String desc = descElement.getText();
										if(desc.length()>100){
											logger.error("--->desc:"+desc);
											driver.executeScript("arguments[0].remove();",descElement.findElement(By.cssSelector(".product-details-and-care")));
										}
									} catch (Exception e2) {
										logger.error("--->删除描述异常");
										try {
											driver.executeScript("arguments[0].remove();",descElement.findElement(By.cssSelector(".product-selling-statement")));
										} catch (Exception e3) {
											logger.error("--->删除描述异常");
										}
									}
									
									List<WebElement> colorChips = driver.findElements(By.cssSelector("div.np-circular-swatches span img.np-image"));
									if(colorChips.size()==1){
										colorChips.get(0).click();
										TimeUnit.SECONDS.sleep(2);
									}else{
										int find = 0;
										for(WebElement w:colorChips){
											String ct =  w.getAttribute("alt").trim().toLowerCase().split(" swatch image")[0];
											if (ct.equals(attrValue.toLowerCase())) {
												driver.executeScript("arguments[0].scrollIntoView();",w);
												TimeUnit.SECONDS.sleep(1);
												driver.executeScript("(function(){window.scrollTo(0,200);})();");
												TimeUnit.SECONDS.sleep(1);
												w.click();
												find++;
												logger.debug("--->选择Color:" + attrValue);
												TimeUnit.SECONDS.sleep(2);
											}
										}
										if (find == 0) {
											logger.error("[3]找不到指定sku:" + attrValue);
											boolean mark = false;
											try {
												List<WebElement> singelSection = driver.findElements(By.cssSelector(".single-selection .color-option-text"));
												for(WebElement w:singelSection){
													String sizeValue = w.getText().toLowerCase();
													if(sizeValue.equalsIgnoreCase(attrValue)){
														mark = true;
														logger.error("[3]选对了颜色:" + attrValue);
													}
												}
												if(!mark){
													return AutoBuyStatus.AUTO_SKU_NOT_FIND;
												}
											} catch (Exception e1) {
												logger.error("[3]color:",e1);
												return AutoBuyStatus.AUTO_SKU_NOT_FIND;
											}
										}
										if(find>1){
											logger.error("[4]超过1个颜色sku:" + attrValue);
											return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
										}
									}
								} catch (Exception e2) {
									logger.debug("--->选择sku碰到异常", e);
									return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
								}
							}
						}
						
						if(attrName.equalsIgnoreCase("size")  && StringUtil.isNotEmpty(attrValue)){
							try {
								List<WebElement> sizes = driver.findElements(By.cssSelector("div.size-options a.np-product-swatch"));
								Iterator<WebElement> iterator = sizes.iterator();
								int find = 0;
								while(iterator.hasNext()){
									WebElement element = iterator.next();
									String className = element.getAttribute("class");
									String text = element.getText().trim();
									String[] strList = text.split("\n");
									if(strList[0].equalsIgnoreCase(attrValue) && !className.contains("unavailable")){
										find = 1;
										element.click();
										logger.debug("--->选择Size:" + attrValue);
										TimeUnit.SECONDS.sleep(2);
										break;
									} 
								}
								if(sizes.size()==1){
									find = 1;
									sizes.get(0).click();
									TimeUnit.SECONDS.sleep(2);
								}
								
								if (find == 0) {
									//logger.error("该商品sku没货:" + attrValue);
									
									logger.error("size新样式"+attrValue);
									try {
										try {
											driver.findElement(By.cssSelector(".size-filter .drop-down-open-icon")).click();
										} catch (Exception e2) {
											logger.debug("--->size:不是下拉框不能点击" + attrValue);
										}
										List<WebElement> colorChips = driver.findElements(By.cssSelector(".size-filter div.drop-down-options div.drop-down-option"));
										if(colorChips.size()==1){
											colorChips.get(0).click();
											TimeUnit.SECONDS.sleep(2);
										}else{
											int finds = 0;
											for(WebElement w:colorChips){
												String ct =  w.getText().trim().toLowerCase();
												if (ct.equals(attrValue.toLowerCase())) {
													driver.executeScript("arguments[0].scrollIntoView();",w);
													TimeUnit.SECONDS.sleep(1);
													driver.executeScript("(function(){window.scrollTo(0,200);})();");
													TimeUnit.SECONDS.sleep(1);
													w.click();
													finds++;
													logger.debug("--->选择size:" + attrValue);
													TimeUnit.SECONDS.sleep(2);
													break;
												}
											}
											if (finds == 0) {
												logger.error("[3]找不到指定sizesku:" + attrValue);
												boolean mark = false;
												try {
													List<WebElement> singelSection = driver.findElements(By.cssSelector(".single-selection"));
													for(WebElement w:singelSection){
														String sizeValue = w.getText().toLowerCase();
														if(sizeValue.contains("size")){
															if(sizeValue.split(":")[1].trim().equalsIgnoreCase(attrValue)){
																mark = true;
																logger.error("[3]选对了size:" + attrValue);
															}
														}
													}
													if(!mark){
														return AutoBuyStatus.AUTO_SKU_NOT_FIND;
													}
												} catch (Exception e) {
													logger.error("[3]size:",e);
													return AutoBuyStatus.AUTO_SKU_NOT_FIND;
												}
												
												
												//return AutoBuyStatus.AUTO_SKU_NOT_FIND;
											}
											if(finds>1){
												logger.error("[4]超过1个size sku:" + attrValue);
												return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
											}
										}
									} catch (Exception e2) {
										logger.debug("--->选择sku碰到异常");
										return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
									}
								}
							} catch (Exception e) {
								logger.debug("--->选择sku碰到异常",e);
								return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
							}
						}
						
						if(attrName.equalsIgnoreCase("width") && StringUtil.isNotEmpty(attrValue)){
							try {
								List<WebElement> sizes = driver.findElements(By.cssSelector("div.width-options a.np-product-swatch"));
								Iterator<WebElement> iterator = sizes.iterator();
								int find = 0;
								while(iterator.hasNext()){
									WebElement element = iterator.next();
									String className = element.getAttribute("class");
									String text = element.getText().trim();
									String[] strList = text.split("\n");
									if(strList[0].equalsIgnoreCase(attrValue) && !className.contains("unavailable")){
										find = 1;
										element.click();
										logger.debug("--->选择width:" + attrValue);
										TimeUnit.SECONDS.sleep(2);
										break;
									} 
								}
								if(sizes.size()==1){
									find = 1;
									sizes.get(0).click();
									TimeUnit.SECONDS.sleep(2);
								}
								
								if (find == 0) {
									//logger.error("该商品sku没货:" + attrValue);
									logger.error("width新样式"+attrValue);
									try {
										try {
											driver.findElement(By.cssSelector(".width-filter .drop-down-open-icon")).click();
										} catch (Exception e) {
											logger.debug("--->width不是下拉框不能点击:" + attrValue);
										}
										List<WebElement> colorChips = driver.findElements(By.cssSelector(".width-filter div.drop-down-options div.drop-down-option"));
										if(colorChips.size()==1){
											colorChips.get(0).click();
											TimeUnit.SECONDS.sleep(2);
										}else{
											int finds = 0;
											for(WebElement w:colorChips){
												String ct =  w.getText().trim().toLowerCase().split("\\(")[0];
												logger.debug("--->width:"+w.getText()+"--->width:"+ct);
												if (ct.trim().equalsIgnoreCase(attrValue)) {
													w.click();
													finds++;
													logger.debug("--->选择width:" + attrValue);
													TimeUnit.SECONDS.sleep(2);
													break;
												}
											}
											if (finds == 0) {
												logger.error("[3]找不到指定widthsku:" + attrValue);
												//return AutoBuyStatus.AUTO_SKU_NOT_FIND;
											}
											if(finds>1){
												logger.error("[4]超过1个width sku:" + attrValue);
												return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
											}
										}
									} catch (Exception e2) {
										logger.debug("--->选择sku碰到异常",e2);
										return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
									}
								}
							} catch (Exception e) {
								logger.debug("--->选择sku碰到异常",e);
								return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
							}
						}
					}
				}
			}
			
			logger.debug("--->选择sku完成");
			
			
			// 寻找商品单价
			try {
				logger.debug("--->[1]开始寻找商品单价");
				String productEntityId = param.get("productEntityId");
				List<WebElement> priceElment = driver.findElements(By.xpath("//div[@class='price-display']"));
				if(priceElment != null && priceElment.size() > 1){
					String size = skuMap.get("size");
					for(WebElement priceEle : priceElment){
						WebElement priceFilter = priceEle.findElement(By.className("price-radio-button"));
						if(priceFilter.getText().trim().equalsIgnoreCase(size)){
							WebElement regularPrice = priceEle.findElement(By.cssSelector("div.regular-price"));
							String priceStr = regularPrice.getText().replace(",", "").trim();
							if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
								if(priceStr.indexOf("–") > -1){
									WebElement _priceElment = driver.findElement(By.xpath("//div[@class='sku-price-display']"));
									String _priceStr = _priceElment.getText();
									if (!Utils.isEmpty(_priceStr)){
										priceMap.put(productEntityId, _priceStr.substring(1));
									}
									logger.debug("--->[1-2]找到商品单价  = " + _priceStr.substring(1));
								} else {
									logger.debug("--->[1-1]找到商品单价  = " + priceStr.substring(1));
									priceMap.put(productEntityId, priceStr.substring(1));
								}
							}
							break;
						}
					}
				} else {
					WebElement onePriceElment = driver.findElement(By.xpath("//div[@class='price-display-item regular-price']"));
					String priceStr = onePriceElment.getText().replace(",", "").trim();
					if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
						if(priceStr.indexOf("–") > -1){
							WebElement _priceElment = driver.findElement(By.xpath("//div[@class='sku-price-display']"));
							String _priceStr = _priceElment.getText();
							if (!Utils.isEmpty(_priceStr)){
								priceMap.put(productEntityId, _priceStr.substring(1));
							}
							logger.debug("--->[1-3]找到商品单价  = " + _priceStr.substring(1));
							
						} else {
							logger.debug("--->[1-4]找到商品单价  = " + priceStr.substring(1));
							priceMap.put(productEntityId, priceStr.substring(1));
						}
					}
				}
			} catch (Exception e) {
				logger.debug("--->[2]开始寻找商品单价");
				try {
					WebElement priceElment = driver.findElement(By.cssSelector("div.price-current"));
					String priceStr = priceElment.getText().replace("Now: ", "").replace(",", "").trim();
					String productEntityId = param.get("productEntityId");
					if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
						if(priceStr.indexOf(" – ") > -1){
							WebElement _priceElment = driver.findElement(By.xpath("//div[@class='sku-price-display']"));
							String _priceStr = _priceElment.getText();
							if (!Utils.isEmpty(_priceStr)){
								priceMap.put(productEntityId, _priceStr.substring(1));
							}
							logger.debug("--->[2-1]找到商品单价  = " + _priceStr.substring(1));
							
						} else {
							logger.debug("--->[2-2]找到商品单价  = " + priceStr.substring(1));
							priceMap.put(productEntityId, priceStr.substring(1));
						}
					}
				} catch (Exception e2) {
					try {
						WebElement priceElment = driver.findElement(By.xpath("//div[@class='price-limited-time']"));
						String priceStr = priceElment.getText().replace("Now: ", "").replace(",", "").trim();
						String productEntityId = param.get("productEntityId");
						if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
							if(priceStr.indexOf(" – ") > -1){
								WebElement _priceElment = driver.findElement(By.xpath("//div[@class='sku-price-display']"));
								String _priceStr = _priceElment.getText();
								if (!Utils.isEmpty(_priceStr)){
									priceMap.put(productEntityId, _priceStr.substring(1));
								}
								logger.debug("--->[3-1]找到商品单价  = " + _priceStr.substring(1));
								
							} else {
								logger.debug("--->[3-2]找到商品单价  = " + priceStr.substring(1));
								priceMap.put(productEntityId, priceStr.substring(1));
							}
						}
					} catch (Exception e3) {
						logger.debug("--->[4]开始寻找商品单价");
						try {
							WebElement priceElment = driver.findElement(By.cssSelector("div.current-price"));
							String priceStr = priceElment.getText().replace("Now: ", "").replace(",", "").trim();
							String productEntityId = param.get("productEntityId");
							if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
								if(priceStr.indexOf(" – ") > -1){
									WebElement _priceElment = driver.findElement(By.xpath("//div[@class='sku-price-display']"));
									String _priceStr = _priceElment.getText();
									if (!Utils.isEmpty(_priceStr)){
										priceMap.put(productEntityId, _priceStr.substring(1));
									}
									logger.debug("--->[4-1]找到商品单价  = " + _priceStr.substring(1));
									
								} else {
									logger.debug("--->[4-2]找到商品单价  = " + priceStr.substring(1));
									priceMap.put(productEntityId, priceStr.substring(1));
								}
							}
						} catch (Exception e4) {
							logger.error("--->获取单价失败");
						}
					}
				}
			}
			
			Utils.sleep(6000);
			
			String productNum = (String) param.get("num");
			// 选择商品数量
			if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
				try {
					logger.debug("--->选择数量:" + productNum);
					WebElement numInput = driver.findElement(By.cssSelector("input.quantity-input"));
					numInput.clear();
					TimeUnit.SECONDS.sleep(1);
					numInput.sendKeys(productNum);
					TimeUnit.SECONDS.sleep(3);
				} catch (Exception e) {
					logger.error("--->选择数量失败",e);
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			}
			
			// 加购物车
			logger.debug("--->开始加购物车");
			try{
				//driver.executeScript("(function(){var els = document.getElementsByClassName('add-to-bag-button');if(els && els[0]){els[0].click();}})();");
				WebElement addCard = driver.findElement(By.cssSelector("button.add-to-bag-button"));
				addCard.click();
			}catch(Exception e){
				logger.error("--->加购物车出现异常",e);
				return AutoBuyStatus.AUTO_ADD_CART_FAIL;
			}
			logger.debug("--->确认是否加购物车成功");
			try{
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				WebElement textDes = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.text-description")));
				if(!textDes.getText().equals("Added to your bag.")){
					logger.error("--->加购物车出现异常啦");
					return AutoBuyStatus.AUTO_ADD_CART_FAIL;
				}
			}catch(Exception e){
				logger.error("--->加购物车出现异常重试");
				logger.debug("--->开始加购物车重试");
				try{
					//driver.executeScript("(function(){var els = document.getElementsByClassName('add-to-bag-button');if(els && els[0]){els[0].click();}})();");
					WebElement addCard = driver.findElement(By.cssSelector("button.add-to-bag-button"));
					addCard.click();
				}catch(Exception e1){
					logger.error("--->加购物车出现异常重试",e);
					return AutoBuyStatus.AUTO_ADD_CART_FAIL;
				}
				logger.debug("--->确认是否加购物车成功重试");
				try{
					WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
					WebElement textDes = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.text-description")));
					if(!textDes.getText().equals("Added to your bag.")){
						logger.error("--->加购物车出现异常啦重试");
						return AutoBuyStatus.AUTO_ADD_CART_FAIL;
					}
				}catch(Exception e1){
					logger.error("--->加购物车出现异常重试",e);
					return AutoBuyStatus.AUTO_ADD_CART_FAIL;
				}
			}
			
			
//			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			try {
//				WebElement addCart = wait.until(ExpectedConditions.visibilityOfElementLocated((By.xpath("//button[@class='nui-button-v6 add-to-bag-button primary' and contains(text(),'Add to Bag')]"))));
//				Utils.sleep(1500);
//				Integer numInt = Integer.valueOf(productNum);
//				if(numInt == 1){
//					driver.executeScript("var tar=arguments[0];tar.click();", addCart);
//					TimeUnit.SECONDS.sleep(2);
//				} else {
//					for (int i = 0; i < numInt; i++)
//					{
//						TimeUnit.SECONDS.sleep(5);
//						addCart.sendKeys(Keys.ENTER);
//						if(numInt > 1){
//							logger.debug("--->数量加1");
//							TimeUnit.SECONDS.sleep(3);
//						}
//					}
//				}
//				
//				logger.debug("--->加入购物车成功");
//				TimeUnit.SECONDS.sleep(3);
//			} catch (Exception e) {
//				logger.debug("--->加入购物车失败");
//			}
		} catch (Exception e) {
			logger.debug("--->等待商品加载完成出错",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 跳转到购物车
		/*try
		{
			logger.error("--->跳转到购物车");
			driver.navigate().to("https://secure.nordstrom.com/ShoppingBag.aspx?origin=tab");
			logger.error("--->跳转到购物车成功");
			TimeUnit.SECONDS.sleep(3);
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}*/
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	
	WebElement whenCheckoutPanelOk()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, 40);
		try
		{
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content")));
			logger.debug("--->checkout主页面加载完成");
		}
		catch (Exception e)
		{
			logger.debug("--->等待checkout主页面加载完成出错");
		}
		return main;
	}
	
	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount,List<GiftCard> giftCardList) {
		
		String myPrice = param.get("my_price");
		String type = param.get("type");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		logger.error("--->myPrice = " + myPrice);

		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		// 此处必须由js点击关闭按钮 否则窗口关不掉
		/*logger.debug("--->关闭tip框");
		try {
			TimeUnit.SECONDS.sleep(5);
			driver.executeScript("(function(){var closeBox = document.getElementsByClassName('shopping-bag-notification');"
					+ "var box = closeBox[0]; var els = box.getElementsByTagName('a');"
					+ "if(els){ for(var i = 0; i<els.length; i++){var ele =els[i]; if(ele.getAttribute('href')=='#close'){ele.click();}}}})();");
			
			// 隐藏keep shopping弹框
			driver.executeScript("(function(){var els = document.getElementsByClassName('shopping-bag-notification');if(els && els[0]){els[0].style.display = 'none'}})();");
			logger.debug("--->关闭tip框成功");
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		};*/
		
		// 等待购物车页面加载完成
		try
		{
			logger.error("--->跳转到购物车");
			driver.navigate().to("https://m.secure.nordstrom.com/ShoppingBag.aspx");
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContent")));
			logger.error("--->跳转到购物车成功");
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			driver.executeScript("(function(){window.scrollBy(300,500);})();");
			TimeUnit.SECONDS.sleep(5);
			WebElement checkOut = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_shoppingBagTotals_proceedToCheckoutButton2")));
			TimeUnit.SECONDS.sleep(5);
			checkOut.click();
			//checkOut.sendKeys(Keys.ENTER);
			logger.error("--->点击checkout按钮");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("--->找不到 checkout 按钮");
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		
		/*// 点击checkout按钮
		try {
			WebElement checkOut = driver
					.findElement(By.xpath("//button[contains(text(),'Checkout')]"));
			checkOut.click();
			logger.error("--->点击checkout按钮成功");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("--->找不到 checkout 按钮");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}*/
		
		// 不要小样
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement skip = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_SampleButtonActions1_noThanksButton")));
			skip.click();
			logger.debug("--->不要赠送的小样, 点击Skip按钮成功");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("--->赠送小样页面不存在，无需选择小样");
		}
		
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		
		logger.debug("--->等待checkout页面载入");
		whenCheckoutPanelOk();
		
		String userName = (String) param.get("userName");
		String passWord = (String) param.get("password");
		try {
			WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("emailAddress")));
			username.clear();
			TimeUnit.SECONDS.sleep(2);
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(5);
			
			WebElement password = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(5);
			
			WebElement btn = driver.findElement(By.xpath("//input[@class='continue button primary' and @value='Sign In']"));
			TimeUnit.SECONDS.sleep(5);
			btn.click();
			logger.debug("--->开始登陆");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("--->付款时帐号登录失败");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 选择收货地址
		try {
			// 选择地址点击edit按钮
			logger.debug("--->点击address edit按钮,等待address 打开");
			String expressAddress = param.get("expressAddress");
			logger.debug("--->传过来的转运公司标识为:"+expressAddress);
			WebElement adrEditBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.note a.edit")));
			adrEditBtn.click();
			logger.debug("--->address打开完成");
			Utils.sleep(1500);
			
			WebElement selectAddr = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shipping-address")));
			WebElement addrSelect = selectAddr.findElement(By.cssSelector("select.select-input"));
			Select select = new Select(addrSelect);
			List<WebElement> addrs = select.getOptions();
			Iterator<WebElement>  list= addrs.iterator();
			while(list.hasNext()){
				WebElement element = list.next();
				if(element.getText().contains("310000")){
					logger.debug("This is billing address!");
					list.remove();
				}
			}
			int index = 0;
			logger.debug("--->总共有"+addrs.size()+"个地址");
			if (addrs != null && addrs.size() > 0)
			{
				index = Integer.valueOf(count);
				int tarAddr = index % 2;
				for (WebElement addr : addrs) {
					String val =  addr.getAttribute("value").trim();
					String text = addr.getText();
					logger.debug("--->test:"+text);
					if(!val.isEmpty() && !text.contains("+ Add New Address") && text.toUpperCase().contains(expressAddress.toUpperCase())){
						int realVal = Integer.valueOf(val)%2;
						
						if(!val.isEmpty() && tarAddr == realVal){
							select.selectByVisibleText(text);
							logger.debug("--->选择第" + (tarAddr + 1) + "个地址成功");
							break;
						}
					} 
				}
			}
			
			//点击 Save & Continue 按钮
			Utils.sleep(1500);
			WebElement saveBtn = selectAddr.findElement(By.cssSelector("div.actions input.continue"));
			saveBtn.click();
			logger.debug("--->选择地址点击 Save & Continue 按钮成功");
			Utils.sleep(5000);
			// 点击使用当前地址（不选择推荐地址）
			try {
				WebElement useAddrsBtn = driver
						.findElement(By.xpath("//a[contains(text(),'Use This Address')]"));
//				WebElement useAddrsBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'Use This Address')]")));
				Utils.sleep(2000);
				useAddrsBtn.click();
				logger.debug("--->点击使用当前地址成功");
				Utils.sleep(5000);
			} catch (Exception e) {
				logger.debug("--->选择新地址");
			}
			
		} catch (Exception e) {
			logger.debug("--->选择地址出错", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		// 选择物流 默认为free
		whenCheckoutPanelOk();
		logger.debug("--->选择物流");
		try {
			boolean defaultIsSelected = true;
			if (defaultIsSelected)
			{
				Utils.sleep(5000);
				logger.debug("--->默认已经选中 Standard - FREE");
			} else {
				WebElement shipMethod = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shipping-method")));
				logger.debug("--->点击Shipping Method edit按钮,等待Shipping Method 打开");
				WebElement shipEditBtn = shipMethod.findElement(By.cssSelector("div.note a.edit"));
				shipEditBtn.click();
				logger.debug("--->Shipping Method打开完成");
				Utils.sleep(5000);
				
				WebElement newShip = driver.findElement(By.id("shipping-method"));
				WebElement formTable = newShip.findElement(By.tagName("form"));
				List<WebElement> opts = formTable.findElements(By.xpath("//input[@name='ship-method']")); 
				if(opts!=null && opts.size() > 0){
					for(WebElement methods : opts){
						
						String val = methods.getAttribute("value").trim();
						
						if (!Utils.isEmpty(val) && val.equals("Standard")) {
							this.logger.warn("--->找到[standard shipping]物流");
							methods.click();
							Utils.sleep(5000);
							break;
						}

						if (!Utils.isEmpty(val) && val.equals("Two Day")) {
							this.logger.warn("--->找到[Two Day]物流");
							methods.click();
							Utils.sleep(5000);
							break;
						}
						
						if (!Utils.isEmpty(val) && val.equals("Next Day")) {
							this.logger.warn("--->找到[Next Day]物流");
							methods.click();
							Utils.sleep(5000);
							break;
						}
						
						if (!Utils.isEmpty(val) && val.equals("Saturday")) {
							this.logger.warn("--->找到[Saturday]物流");
							methods.click();
							Utils.sleep(5000);
							break;
						}
					}
				}
				
				
			}
		} catch (Exception e) {
			logger.debug("--->选择物流失败");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}
		try {
			WebElement newShip = driver.findElement(By.id("shipping-method"));
			//点击 Save & Continue 按钮
			Utils.sleep(1500);
			WebElement saveBtn = newShip.findElement(By.xpath("//a[contains(text(),'Save & Continue')]"));
			saveBtn.click();
			logger.debug("--->选择物流点击 Save & Continue 按钮成功");
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.debug("--->选择物流失败");
		}
		logger.debug("--->等待优惠码框加载");
		
		//使用优惠码0 失效,1互斥 ,9没修改过,10有效
		boolean isEffective = false;
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			logger.debug("promotionList.toString()" + promotionList.toString());
			try {
				WebElement promoCodeEle = driver
						.findElement(By
								.xpath("//a[contains(text(),'Apply a Promotion Code')]"));
				promoCodeEle.click();
			} catch (Exception e) {
				logger.debug("打开优惠码输入框异常");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			for (String code : promotionList) {
				try {
					logger.debug("code:"+code);
					TimeUnit.SECONDS.sleep(2);
					WebElement promoCode = driver.findElement(By.cssSelector(".manual-promo > input:nth-child(2)"));
					logger.debug("--->找到优惠码输入框,开始输入");
					promoCode.clear();
					promoCode.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
					WebElement apply = driver.findElement(By.cssSelector(".promo-information > div:nth-child(1) > div:nth-child(2) > input:nth-child(1)"));
					apply.click();
					TimeUnit.SECONDS.sleep(2);
					try {
						driver.findElement(By.xpath("//span[@class='invalid']"));
						logger.debug("--->优惠码不可用！");
						statusMap.put(code, 0);
					} catch (Exception e) {
						logger.debug("优惠码可用");
						isEffective = true;
						statusMap.put(code, 10);
						break;
					}
				} catch (Exception e) {
					logger.debug("输入优惠码异常",e);
				}
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		/*// 输入优惠码
		logger.debug("--->等待优惠码框加载");
		// 输入优惠码
		try {
			TimeUnit.SECONDS.sleep(2);
			WebElement promoCodeEle = driver.findElement(
					By.xpath("//a[contains(text(),'Apply a Promotion Code')]"));
			promoCodeEle.click();
			TimeUnit.SECONDS.sleep(2);
			WebElement promoCode = driver.findElement(By.xpath("//input[@class='ng-pristine ng-valid']"));
			logger.debug("--->找到优惠码输入框,开始输入");
			String promotionCode = (String) param.get("promoCode");
			logger.debug("--->优惠码是 = " + promotionCode);
			if (Utils.isEmpty(promotionCode)) {
				logger.debug("--->没有找到可用的优惠码");
			}
			promoCode.sendKeys(promotionCode);
			Utils.sleep(1500);
			WebElement updateBtn = driver.findElement(
					By.xpath("//input[@class='apply button']"));
			Utils.sleep(1500);
			updateBtn.click();
			logger.debug("--->输入优惠码结束");
			
			try {
				WebElement errorEle = driver.findElement(By.xpath("//span[@class='invalid']"));
				if(errorEle!=null){
					logger.debug("--->优惠码不可用！");
				}
			} catch (Exception e) {}
			
			Utils.sleep(2500);
		} catch (Exception e) {
			logger.debug("--->输入优惠码异常");
		}*/
		// 选择礼品卡
		if(!StringUtil.isBlank(type) && type.equals("1")){
			try {
				try {
					WebElement checkOut = driver
							.findElement(By.xpath("//a[contains(text(),'Apply a Gift Card')]"));
					checkOut.click();
					logger.error("--->点击Gift按钮成功");
				} catch (Exception e) {
					WebElement another = driver.findElement(By.cssSelector(".apply-another-giftCard"));
					another.click();
					logger.error("--->继续添加点击按钮");
				}
				
				TimeUnit.SECONDS.sleep(5);
				BigDecimal cardTotal = new BigDecimal(0.00);
				WebDriverWait wait0 = new WebDriverWait(driver, 20);
				logger.error("--->giftCardList size="+giftCardList.size());
				for(GiftCard card:giftCardList){
					WebElement number = null;
					try {
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".gift-card-number .ng-pristine")));
						number = driver.findElement(By.cssSelector(".gift-card-number .ng-pristine"));
					} catch (Exception e) {
						number = driver.findElement(By.cssSelector(".gift-card-number .ng-valid"));
					}
					
					number.clear();
					number.sendKeys(card.getSecurityCode());
					TimeUnit.SECONDS.sleep(1);
					WebElement access = null;
					try {
						access = driver.findElement(By.cssSelector(".gift-card-access .ng-pristine"));
					} catch (Exception e) {
						access = driver.findElement(By.cssSelector(".gift-card-access .ng-valid"));
					}
					
					access.clear();
					access.sendKeys(card.getPassWord());
					TimeUnit.SECONDS.sleep(1);
					WebElement apply = driver.findElement(By.cssSelector(".gift-card-apply"));
					apply.click();
					TimeUnit.SECONDS.sleep(1);
					card.setIsUsed("yes");
					
					try {
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".gift-card-number .misc-error")));
						card.setIsSuspect("yes");
						continue;
					} catch (Exception e) {
						List<WebElement> amounts = driver.findElements(By.cssSelector(".applied-gift-cards .amount"));
						
						String price = amounts.get(amounts.size()-1).getText().substring(1).trim();
						BigDecimal x = new BigDecimal(price);
						cardTotal = cardTotal.add(x);
						try {
							driver.findElement(By.cssSelector(".billing-address"));
						} catch (Exception e2) {
							logger.error("--->礼品卡够了");
							break;
						}
						card.setRealBalance("0");
						logger.error("--->礼品卡不够继续，继续添加");
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".apply-another-giftCard")));
						WebElement another = driver.findElement(By.cssSelector(".apply-another-giftCard"));
						another.click();
						logger.error("--->继续添加点击");
					}
				}
				
				
				
			} catch (Exception e) {
				logger.debug("--->找不到 checkout 按钮");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}else{
			// 选择信用卡
			logger.debug("--->选择信用卡");
			try {
				boolean defaultIsSelected = true;
				if (defaultIsSelected)
				{
					Utils.sleep(5000);
					logger.debug("--->默认已经选中信用卡");
				} else {
					WebElement creditCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.payment-information")));
				
					WebElement creditCardSelect = creditCard.findElement(By.cssSelector("select.select-input"));
					Select select = new Select(creditCardSelect);
					List<WebElement> creditCards = select.getOptions();
					
					if (creditCards != null && creditCards.size() > 0)
					{
						for (WebElement credit : creditCards) {
							String val =  credit.getAttribute("value").trim();
							String text = credit.getText();
							if(!val.isEmpty() && !text.contains("+ Add New Card")){
								select.selectByVisibleText(text);
								logger.debug("--->选择信用卡成功");
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				logger.debug("--->没有找到可用的信用卡卡号");
				return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
			}
			
			// 输入安全码
			try {
				Utils.sleep(5000);
				WebElement securityCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credit-ccv")));
				logger.debug("--->找到信用卡输入框,开始输入");
				String code = (String) param.get("suffixNo");
				logger.debug("--->信用卡卡号是 = " + code);
				if (Utils.isEmpty(code)) {
					logger.debug("--->没有找到可用的信用卡安全码");
				}
				securityCode.sendKeys(code);
				Utils.sleep(1500);
				logger.debug("--->输入信用卡安全码结束");
			} catch (Exception e) {
				logger.debug("--->没找到信用卡安全码输入框");
			}
			
			// 选择账单地址 billing-address
			logger.debug("--->选择账单地址");
			try {
				boolean defaultIsSelected = true;
				if (defaultIsSelected)
				{
					Utils.sleep(5000);
					logger.error("--->默认已经选中账单地址");
				}
			} catch (Exception e) {
				logger.debug("--->选择账单地址失败");
			}
		}
		
		//点击 Save & Continue 按钮
		logger.debug("--->输入安全码之后点击 Save & Continue按钮");
		try {
			Utils.sleep(5000);
			WebElement saveBtn = driver.findElement(By.cssSelector("div.payment-contact input.continue"));
			saveBtn.click();
			logger.error("--->点击 Save & Continue按钮成功");
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->点击 Save & Continue按钮失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		whenCheckoutPanelOk();
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement reviewOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("review-order")));
			WebElement totalPriceElement = reviewOrder.findElement(By.cssSelector(".price"));
			String text = totalPriceElement.getText().replace(",", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = " + priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 5.00D) {
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
				if(v.doubleValue()<-5.00D){
					logger.error("--->漏单,不能下单");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE,"0");
		data.put(AutoBuyConst.KEY_AUTO_BUY_PROMOTION_FEE,"0");
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		// placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try {
			WebElement placeOrderElement = driver.findElement(By.xpath("//a[contains(text(),'Place Order')]"));
			Utils.sleep(1500);
			if (isPay) {
				logger.debug("------------------");
				logger.debug("--->啊啊啊啊，我要付款啦！！！");
				placeOrderElement.click();
				logger.debug("------------------");
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		} catch (Exception e) {
			logger.debug("--->付款失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 查询商城订单号
		try {
			WebDriverWait wait0 = new WebDriverWait(driver, WAIT_TIME);
			logger.debug("--->等待订单页面加载");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.className("order-number-value")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.className("order-number-value"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取nordstrom单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
			} else {
				logger.error("--->获取nordstrom单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->查找商品订单号出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		String mallOrderNo = detail.getMallOrderNo();
		Map<String, String> skuMap = new HashMap<String, String>();
		String color = "";
		String size = "";
		if(detail.getProductSku() != null){
			List<String> skuList = Utils.getSku(detail.getProductSku());
			for (int i = 0; i < skuList.size(); i += 2)
			{
				skuMap.put(skuList.get(i), skuList.get(i + 1));
			}
			
			color = skuMap.get("color");
			if(color == null){
				color = "no color";
			}
			size = skuMap.get("size");
			if(size == null){
				size = "one size";
			}
		}
		
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			String orderUrl ="https://secure.nordstrom.com/myaccount/OrderStatus.aspx?origin=orderhistory&ordernum="+ mallOrderNo;
			driver.navigate().to(orderUrl);
			
			Utils.sleep(5000);
			WebElement statusEle =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_mainContentPlaceHolder_orderStatusDisplay_OrderHeaderStatus")));
			String str = statusEle.getText().toLowerCase();
			if(str.contains("cancelled")){
				logger.error("该订单被砍单了");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
			}else if(str.contains("in process")){
				logger.error("[1]该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}else if(str.contains("complete")){
				// 商城订单号一样 包裹号不一样
				List<WebElement>  boxs = driver.findElements(By.cssSelector("div.ostatIRBox"));
				
				if(boxs != null && boxs.size() > 1){
					for(WebElement box : boxs){
						String rowText = box.findElement(By.className("ostatItemRowTd3")).getText().toLowerCase();
						if(rowText!=null){
							String[] txt = rowText.split("\n");
							if((color!=null && txt[4].contains(color.toLowerCase()))
									&& (size!=null && txt[3].contains(size.toLowerCase()))){
								logger.debug("--->sku :" + skuMap);
								
								WebElement statusElment = box.findElement(By.cssSelector("td.ostatItemRowTd6 span span"));
								String realStatus = statusElment.getText().trim();
								if(realStatus.contains("shipped")){
									WebElement expressNoEle = box.findElement(By.cssSelector("td.ostatItemRowTd6 a"));
									String expressNo = expressNoEle.getText().trim();
									String expressCompany = null;
									String url = expressNoEle.getAttribute("href");
									if(url.contains("ups")){
										expressCompany = "UPS";
									} else if(url.contains("usps")){
										expressCompany = "USPS";
									} else if(url.contains("ontrac")) {
										expressCompany = "OnTrac";
									} else if(url.contains("fedex")){
										expressCompany = "FedEx";
									} else {
										logger.debug("没有找到物流公司名称");
									}
									
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
									logger.error("expressCompany = " + expressCompany);
									logger.error("expressNo = " + expressNo);
									return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								} else if(realStatus.contains("Cancelled")){
									logger.error("该订单被砍单了");
									return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
								} else {
									logger.error("该订单还没发货,没产生物流单号");
									return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
								}
							}
						}
					}
				} else {
					WebElement expressNoEle = driver.findElement(By.id("ctl00_mainContentPlaceHolder_orderStatusDisplay_orderDetailsRepeater_ctl01_trackingURLHyperLink"));
					String expressNo = expressNoEle.getText().trim();
					String expressCompany = null;
					String url = expressNoEle.getAttribute("href");
					if(url.contains("ups")){
						expressCompany = "UPS";
					} else if(url.contains("usps")){
						expressCompany = "USPS";
					} else if(url.contains("ontrac")) {
						expressCompany = "OnTrac";
					} else if(url.contains("fedex")){
						expressCompany = "FedEx";
					} else {
						logger.debug("没有找到物流公司名称");
					}
					
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
					logger.error("expressCompany = " + expressCompany);
					logger.error("expressNo = " + expressNo);

					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}
			}else{
				logger.debug("未识别的物流状态"+str);
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("http://shop.nordstrom.com/");
//			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper")));
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转nordstrom主页面碰到异常");
		}
		return false;
	}

	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		//timer.cancel();
		return true;
	}
	
	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static void main(String[] args)
	{
		NordstromAutoBuy auto = new NordstromAutoBuy();
		System.out.println(auto.login("ladflew@tom.com", "tfb001001"));
		System.out.println(auto.cleanCart());
	
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "http://shop.nordstrom.com//s/adidas-stan-smith-leather-sneaker-big-kid/4652826");
		param.put("sku", "[[\"color\",\"Core White/ Green\"],[\"size\",\"7 M\"]]");
//		//param.put("sku", "[[\"color\",\"Red\"]]");
//		//param.put("sku", "[[\"color\",\"714 Caresse\"]]");
		param.put("num", "1");
		param.put("productEntityId", "4780644");
//		param.put("isPay", "false");
//		param.put("count", "1");
//		param.put("suffixNo", "123");
//		param.put("my_price", "16.00");
//		param.put("userName", "basiace@outlook.com");
//		param.put("password", "tfb001001");
		auto.selectProduct(param);
		//auto.pay(param);
		
		
		//RobotOrderDetail detail = new RobotOrderDetail();
		//detail.setMallOrderNo("833728294");
		//detail.setProductSku("[[\"color\",\"001 Pink\"]]");
		//auto.scribeExpress(detail);
	}
	
}
 