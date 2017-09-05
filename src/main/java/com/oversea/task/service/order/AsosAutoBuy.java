package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class AsosAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	static int quality = 10;
	
	public AsosAutoBuy() {
		super(false);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://us.asos.com/");

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement current = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".selected-currency")));
			if(!current.getText().contains("USD")){
				WebElement usd = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".currency-locale-link")));
				usd.click();
				WebElement sel = driver.findElement(By.id("currencyList"));
				Select s = new Select(sel);
				s.selectByVisibleText("$ USD");
			}
		} catch (Exception e) {
			logger.error("--->修改英镑出错", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		try {
			WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Sign In')]")));
			logger.debug("--->跳转到登录页面");
			signIn.click();
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			// 输入账号
			wait.until(ExpectedConditions.elementToBeClickable(By.id("EmailAddress")));
			WebElement username = driver.findElement(By.id("EmailAddress"));
			logger.debug("--->输入账号");

			username.sendKeys(userName);

			// 输入密码
			wait.until(ExpectedConditions.elementToBeClickable(By.id("Password")));
			WebElement passward = driver.findElement(By.id("Password"));
			logger.debug("--->输入密码");

			Utils.sleep(1500);
			passward.sendKeys(passWord);

			// 提交
			WebElement submitBtn = driver.findElement(By.id("signin"));
			logger.debug("--->开始提交");

			Utils.sleep(1500);
			submitBtn.click();

		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		// 等待登录完成
		try {
			logger.debug("--->等待登录完成");

			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'sign out')]")));

			logger.debug("--->登录完成");
		} catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.xpath("//span[contains(text(),'Cart')]"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			viewCart.click();
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		logger.error("--->清空购物车商品");
		// 清理购物车
		try {
			TimeUnit.SECONDS.sleep(5);
			List<WebElement> goodsInCart = driver.findElements(By.xpath("//button[@class='bag-item-remove']"));
			if (goodsInCart != null) {
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");

				for (int i = 0; i < goodsInCart.size(); i++) {
					try {
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						goodsInCart.get(i).click();
						TimeUnit.SECONDS.sleep(3);
					} catch (Exception e) {
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}
			} else {
				logger.debug("--->购物车为空！");
			}

			// 跳转继续购物
			try {
				logger.debug("--->继续购物,再次跳转");

				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				By by = By.xpath("//a[contains(text(),'Continue Shopping')]");
				wait.until(ExpectedConditions.elementToBeClickable(by));
				WebElement go = driver.findElement(by);
				go.click();
			} catch (Exception e) {
				logger.error("--->继续购物跳转失败", e);
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}

		} catch (Exception e) {
			logger.error("--->选择需要删除的商品出错", e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
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
			try {
				WebElement outofstack = driver.findElement(By.cssSelector("div.out-of-stock > h3:nth-child(1)"));
				if(outofstack!=null&&outofstack.getText().contains("Out of stock")){
					logger.debug("--->商品售罄");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			} catch (Exception e) {
			}
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		} catch (Exception e) {
			logger.debug("--->商品页面加载失败");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//判断商品是否下架
		try{
			driver.findElement(By.xpath("//div[@class='outofstock-msg']"));
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}
		
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		// 开始选择sku
		logger.debug("--->开始选择sku");
		List<String>  skuValueList = new ArrayList<String>();
		String tempColor = null;
		String tempSize = null;
		try {
			WebElement main = null;
			if (sku != null) {
				main = driver.findElement(By.id("aside-content"));
				List<String> skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrVal = skuList.get(i);
						switch (attrName) {
						case "color":
							try {
								logger.error("选择color:" + attrVal);
								skuValueList.add(attrVal);
								WebElement colorSpan = main.findElement(By.xpath("//span[@class = 'product-colour']"));
								if(colorSpan!=null ){
									if(colorSpan.getText().equalsIgnoreCase(attrVal)){
										logger.error("选择color" + attrVal+"成功");
									}else{
										logger.error("[4]找不到指定sku:" + attrVal);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
								}else{
									WebElement colorSelect = main
											.findElement(By.id("ctl00_ContentMainPage_ctlSeparateProduct_drpdwnColour"));
									Select select = new Select(colorSelect);
									List<WebElement> colors = select.getOptions();
									if (colors != null && colors.size() > 1) {
										int find = 0;
										for (WebElement color : colors) {
											String real = color.getText();
											String ct =  color.getAttribute("value").trim().toLowerCase();
											ct = ct.replaceAll("[\\s&]", "").replaceAll("[\\s/]", "");
											if (attrVal.toLowerCase().equals(ct)) {
												find = 1;
												select.selectByVisibleText(real);
												tempColor = attrVal;
												break;
											}
										}

										if (find == 0) {
											logger.error("[3]找不到指定sku:" + attrVal);
											return AutoBuyStatus.AUTO_SKU_NOT_FIND;
										}
									} else {
										logger.error("[2]找不到指定sku:" + attrVal);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
								}
							} catch (Exception e) {
								logger.error("[1]找不到指定sku:" + attrVal);
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
							TimeUnit.SECONDS.sleep(5);
							break;
						case "size":
							if(!StringUtil.isEmpty(attrVal) && !attrVal.equalsIgnoreCase("no size") && !attrVal.equalsIgnoreCase("one size")){
								if(!attrVal.contains("-")){
									skuValueList.add(attrVal);
								}else{
									skuValueList.add(attrVal.split("-")[0].trim());
								};
								try {
									logger.error("选择size:" + attrVal);
									int find = 0;
									WebElement sizeSelect = main
											.findElement(By.xpath(".//select[@data-id='sizeSelect']"));
									Select select = new Select(sizeSelect);
									List<WebElement> siezes = select.getOptions();
									
									for (WebElement size : siezes) {
										String real = size.getText();
										String txt = size.getText().toLowerCase();
										String value = txt;
										if(!value.equals("-1")){
											if(txt.indexOf("-") != -1){
												String newSize = txt;
												if ((newSize.equals(attrVal.toLowerCase())
														&& !txt.contains("not available"))) {
													find = 1;
													select.selectByVisibleText(real);
													tempSize = attrVal;
													break;
												}
											} else {
												if (txt.equalsIgnoreCase(attrVal) 
														&& !txt.contains("not available")) {
													find = 1;
													select.selectByVisibleText(real);
													tempSize = attrVal;
													break;
												}else if(txt.contains("not available")){
													logger.debug("售罄");
													return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
												}
											}
										}
									}

									TimeUnit.SECONDS.sleep(5);
									if (find == 0) {
										logger.error("[2]找不到指定sku:" + attrVal);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
								} catch (Exception e) {
									logger.error("[1]找不到指定sku:" + attrVal);
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}else if(!StringUtil.isEmpty(attrVal)){
								skuValueList.add(attrVal);
							}
							break;
						default:
							logger.error("选择" + attrName + "出错:" + attrVal);
							return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
						}

					}
				}
			}

		} catch (Exception e) {
			logger.debug("--->选择sku碰到异常", e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->选择sku完成");

		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElment = driver.findElement(By.xpath("//span[@class='current-price product-price-discounted']"));
			String priceStr = priceElment.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->找到商品单价 = " + priceStr.substring(1));
//				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.substring(1));
				priceMap.put(productEntityId, priceStr.substring(1));
			}
		} catch (Exception e) {
			try {
				WebElement pricePanel = driver.findElement(By.xpath("//span[@class='current-price']"));
				String priceStr = pricePanel.getText();
				String productEntityId = param.get("productEntityId");
				if (!Utils.isEmpty(priceStr) && StringUtil.isNotEmpty(productEntityId)) {
					logger.error("--->单价:" + priceStr.replace("$", ""));
//					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.replace("$", ""));
					priceMap.put(productEntityId, priceStr.replace("$", ""));
				} else{
					WebElement mypricePanel = driver.findElement(By.id("ctl00_ContentMainPage_ctlSeparateProduct_lblProductPrice"));
					String myPriceStr = mypricePanel.getText();
					if (!Utils.isEmpty(myPriceStr) && StringUtil.isNotEmpty(productEntityId)) {
						logger.error("--->单价:" + myPriceStr.replace("$", ""));
//						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, myPriceStr.replace("$", ""));
						priceMap.put(productEntityId, myPriceStr.replace("$", ""));
					} else
					{
						logger.error("--->单价获取失败");
					}
				}
			} catch (Exception e2) {
				logger.error("--->获取单价失败");
			}
		}
		
		// 加购物车
		logger.debug("--->开始加购物车");
		try {
			WebElement addCart = driver
					.findElement(By.xpath("//span[contains(text(),'Add to cart')]"));
			Utils.sleep(1500);
			addCart.click();
			Utils.sleep(5000);
			logger.debug("--->加入购物车成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("--->加入购物车失败");
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}

		// 跳转到购物车
		WebElement cartBtn = null;
		try {
			cartBtn = driver.findElement(By.xpath("//span[contains(text(),'Cart')]"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			cartBtn.click();
			logger.debug("--->[1]去购物车");
		} catch (Exception e) {
			try {
				cartBtn = driver.findElement(
						By.xpath("//a[@class='button grey large view-bag' and contains(text(),'View Cart')]"));
				TimeUnit.SECONDS.sleep(5);
				cartBtn.click();
				logger.debug("--->[2]去购物车");
			} catch (Exception ex) {
				logger.error("--->跳转到购物车失败");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}

		// 选择商品数量
		if (!Utils.isEmpty(productNum)) {
			try {
				TimeUnit.SECONDS.sleep(15);
				logger.debug("--->商品数量 = " + productNum);
				List<WebElement> cartList = driver.findElements(By.xpath("//ul[@class ='bag-items']/li"));
				logger.debug("--->购物车现有商品数 = " + cartList.size());
				for (int i = 0; i < cartList.size(); i++) {
					boolean flag = true;
					WebElement product = cartList.get(i);
					for (int j = 0; j < skuValueList.size(); j++) {
						if(!product.getText().toLowerCase().contains(skuValueList.get(j).toLowerCase())){
							flag = false;
						}
					}
					if(flag == true){
						logger.debug("--->找到商品，开始选择数量 ");
						TimeUnit.SECONDS.sleep(5);
						logger.debug("--->找到eidt数量按钮 ");
						List<WebElement> editList = product.findElements(By.xpath(".//span[@class='select2-selection__arrow']"));
						WebElement edit = editList.get(editList.size()-1);
						edit.click();
						
						logger.debug("--->点击eidt数量按钮 ");
						TimeUnit.SECONDS.sleep(5);
						
						logger.debug("--->开始选择商品数量 ");
						List<WebElement>  liList = driver.findElements(By.xpath("//span[@class = 'select2-results']/ul/li"));
						liList.get(Integer.parseInt(productNum)-1).click();
						
						logger.debug("--->更新商品数量");
						TimeUnit.SECONDS.sleep(5);
						try {
							WebElement update = driver.findElement(By.xpath("//button[@class = 'bag-item-edit-update']"));
							update.click();
						} catch (Exception e) {
						}
						TimeUnit.SECONDS.sleep(10);
						try {
							WebElement sorr = driver.findElement(By.xpath("//p[contains(text(),'Sorry')"));
							if(sorr!=null){
								logger.debug("--->库存不足 ");
								return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
							}
						} catch (Exception e) {
						}
						break;
					}else{
						logger.error("没有找到商品,所以不能选择数量");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
					
				}
			} catch (Exception e) {
				logger.debug("--->选择商品数量碰到异常", e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		//查询商品数量是否可买
		if(!Utils.isEmpty(productNum) && Integer.parseInt(productNum) > quality){
			logger.debug("--->选择商品数量太多，不支付一次性购买这么多件商品");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		else{
			logger.debug("--->商品数量没有问题,carry on");
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		// 设置价格
//		int num = 1;
//		try {
//			num = Integer.parseInt(param.get("num"));
//		} catch (Exception e) {
//			num = 1;
//		}
//		myPrice = String.valueOf(Float.parseFloat(myPrice) * num);
		logger.error("--->myPrice = " + myPrice);

		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}

		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = driver.findElement(
					By.cssSelector("a.bag-total-button--checkout--total"));
			Utils.sleep(1500);
			goPay.click();
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("--->加载CHECKOUT出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		logger.debug("--->等待支付页面加载");
		// 等待支付页面加载完成
		try {
			TimeUnit.SECONDS.sleep(5);
			logger.debug("--->支付页面加载完成");
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		try {
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			boolean isEffective = false;
			Set<String> promotionList = getPromotionList(param.get("promotion"));
			if (promotionList != null && promotionList.size() > 0) {
				WebElement click = driver.findElement(By.cssSelector("a.expandable-heading-container"));
				click.click();
				TimeUnit.SECONDS.sleep(3);
				logger.debug("点开输入优惠吗");
				for (String code : promotionList) {
					logger.debug("code:" + code);
					WebElement proIn = driver.findElement(By.xpath("//input[@id='discountCode']"));
					proIn.clear();
					proIn.sendKeys(code);
					TimeUnit.SECONDS.sleep(3);
					
					WebElement apply = driver.findElement(By.cssSelector("button.btn:nth-child(3)"));
					apply.click();
					TimeUnit.SECONDS.sleep(2);
					
					try {
						WebElement error = driver.findElement(By.cssSelector("span.validation-error:nth-child(4)"));
						if(error.getText().contains("doesn't exist")||error.getText().contains("spend a bit more")){
							logger.debug("优惠码无效"+code);
							statusMap.put(code, 0);
						}else{
							logger.debug("无法识别的提示信息:"+error.getText());
						}
					} catch (Exception e) {
						try {
							driver.findElement(By.xpath("//input[@id='discountCode']"));
						} catch (Exception e2) {
							logger.debug("优惠码生效:"+code);
							isEffective = true;
							statusMap.put(code, 10);
							break;
						}
					}
				}
				setPromotionCodelistStatus(statusMap);
				if("true".equals(param.get("isStock")) && !isEffective){
					logger.debug("--->优惠码失效,中断采购");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
			
		} catch (Exception e) {
			logger.debug("输入优惠吗异常【1】", e);
		}
		
		try {
			WebElement cancle = driver.findElement(By.xpath("//a[contains(text(),'Cancel Add/Edit Address')]"));
			cancle.click();
		} catch (Exception e) {
		}
		
		logger.debug("--->选择收货地址");
		// 选收货地址
		try {
			logger.debug("--->点击change按钮");
			WebElement chageAddress = driver.findElement(By.xpath("//button[contains(text(),'Change')]"));
			chageAddress.click();
			TimeUnit.SECONDS.sleep(1);
			
			List<WebElement> addressEle = driver.findElements(By.xpath("//ul[@class = 'option-list']/li"));
			if (addressEle != null && addressEle.size() > 0) {
				logger.debug("--->目前共有[" + addressEle.size() + "]个可用地址");

				int index = 0;
				try {
					index = Integer.valueOf(count);
					int tarAddr = index % 4;

					WebElement cur = addressEle.get(tarAddr);
					Utils.sleep(1500);
					//WebElement radio = cur.findElement(By.cssSelector("td.radio span.radio input"));
					cur.click();
					logger.debug("--->选择第" + (tarAddr + 1) + "个地址成功");
					Utils.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择地址出错 = ", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}

		// 选择物流
		logger.debug("--->选择物流");
		try {
			List<WebElement> opts = driver.findElements(By.xpath("//li[@class = 'delivery-option']"));
			this.logger.debug("--->目前共有[" + opts.size() + "]种物流可选");
			for (WebElement opt : opts) {
				if (opt != null) {
					if(opt.getText().contains("2-Day Shipping")){
						opt.click();
					}
					/*WebElement ele = opt.findElement(By.xpath("//td[@class='deliverytypename']"));
					String text = ele.getText().toLowerCase().trim();
					if (!Utils.isEmpty(text) && text.equals("standard shipping*")) {
						this.logger.warn("--->找到[standard shipping]物流");
						WebElement optRadio = opt.findElement(By.xpath("//input[@name='radShippingMethod']"));
						optRadio.click();
						break;
					}

					if (!Utils.isEmpty(text) && text.equals("4-day shipping")) {
						this.logger.warn("--->找到[4-Day Shipping]物流");
						WebElement optRadio = opt.findElement(By.xpath("//input[@name='radShippingMethod']"));
						optRadio.click();
						break;
					}*/
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择物流失败");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}

		// 填写信用卡安全码 卡号后3位数字
		try {
			Utils.sleep(5000);
			WebElement securityCode = driver.findElement(By.id("securityCode"));
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

		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement = driver
					.findElement(By.xpath("//div[@data-bind='currency: total']"));
			String text = totalPriceElement.getText();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->[1]找到商品结算总价 = " + priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (Math.abs(v.doubleValue()) > 5.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
				
			}
		} catch (Exception e) {
			try {
				logger.debug("--->[2]开始查询总价");
				Utils.sleep(5000);
				WebElement totalPriceElement = driver
						.findElement(By.id("_ctl0_ContentBody_ctlReceiptSummary_rptReceiptItems__ctl2_lblGrandTotal"));
				String text = totalPriceElement.getText();
				if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
					String priceStr = text.substring(text.indexOf("$") + 1);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
					logger.debug("--->[2]找到商品结算总价 = " + priceStr);
//					BigDecimal x = new BigDecimal(myPrice);
//					BigDecimal y = new BigDecimal(priceStr);
//					BigDecimal v = x.subtract(y);
//					double m = Math.abs(v.doubleValue());
//					if (m > 5.00D) {
//						logger.error("--->总价差距超过约定,不能下单");
//						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
//					}
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(priceStr);
					BigDecimal v = y.subtract(x);
					if (v.doubleValue() > 5.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			} catch (Exception e2) {
				logger.debug("--->查询结算总价出现异常");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}

		// placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try {
			WebElement placeOrderElement = driver.findElement(By.xpath("//button[@class = 'btn cta place-order']"));
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
		// 这个网站有个bug，付完款后给的单号是错的，需要到订单历史中找正确的订单号
		try {
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			logger.debug("--->订单页面加载完成");
			Utils.sleep(5000);
			
			driver.navigate().to("https://my.asos.com/my-account/orders");
			TimeUnit.SECONDS.sleep(3);

			List<WebElement> lastOrdes = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("#main ul li")));
			String orderNo = lastOrdes.get(0).findElement(By.tagName("dd")).getText();
			logger.error("获取asos单号成功:\t" + orderNo);
			savePng();
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
		} catch (Exception e) {
			logger.debug("--->查找商品订单号出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}

		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail)
	{
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		// 寻找my account
		try {
			logger.debug("--->开始跳转到my account页面");
			TimeUnit.SECONDS.sleep(5);
			WebElement myAccount = driver.findElement(By.xpath("//a[contains(text(),'My Account')]"));
			Utils.sleep(1500);
			myAccount.click();
			TimeUnit.SECONDS.sleep(10);
		} catch (Exception e) {
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		try {
			driver.get("https://my.asos.com/my-account/orders");
		} catch (Exception e) {
		}
		

		try {
			Utils.sleep(3000);
			logger.debug("--->开始查找对应订单");
			List<WebElement> orderList = driver.findElements(By.cssSelector("#main ul li"));

			for (WebElement tr : orderList) {
				List<WebElement> tds = tr.findElements(By.tagName("dd"));

				// 第一个td是订单编号
				WebElement ordernum = tds.get(0);

				boolean flag = ordernum.getText().trim().contains(mallOrderNo.trim());
				logger.debug("--->orderNo text:" + ordernum.getText() + "对比结果:" + flag);

				if (flag) {
					// 第一个td是订单编号状态
					WebElement trd = null;
					try {
						trd= tr.findElement(By.cssSelector(" .Tappable-inactive"));
						if(trd.getText().contains("Help")){
							logger.error("--->查询订单异常,找不到该订单");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
						}
					} catch (Exception e) {
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}
					
					trd.click();
						TimeUnit.SECONDS.sleep(2);//等待页面载入

						logger.debug("--->跨域调用");
						
					    try {  
					        String currentHandle = driver.getWindowHandle();  
					        Set<String> handles = driver.getWindowHandles();  
					        for (String s : handles) {  
					            if (s.equals(currentHandle))  
					                continue;  
					            else {  
					                driver.switchTo().window(s);  
					                if (driver.getTitle().contains("wnDirect") || driver.getTitle().contains("Your ASOS order will be with you soon")
					                		|| driver.getTitle().contains("ups")) {  
					                	logger.debug("--->Switch to window successfully!");  
					                    break;  
					                } else  
				                    continue;  
					            }  
					        }  
					    } catch (NoSuchWindowException e) {  
					    	logger.error("Window cound not found!");  
					    }  
						logger.debug("--->得到窗口的句柄");
						boolean notFind = false;
						// 在寻找物流公司和单号页面等待
						// wnDirect USA
						try {
							TimeUnit.SECONDS.sleep(15);
							logger.debug("--->[1]开始查找物流");

							List<WebElement> tarTds = driver.findElements(By.cssSelector("#tracking-results td.order"));
							
							if(tarTds!=null && tarTds.size() > 0){
								for (WebElement td : tarTds) {
									String txt = td.getText();
									if (txt != null && txt.contains("Order:")) {
										logger.debug("--->找到物流对应的table"); 

										String[] arr = td.getText().split("\n");

										String expressCompanyEle = arr[2].trim();
										String expressNoEle = arr[3].trim();

										String expressCompany = expressCompanyEle.replace("Carrier:", "").trim();
										String expressNo = expressNoEle.replace("Carrier Reference:", "").trim();

										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
										logger.error("expressCompany = " + expressCompany);
										logger.error("expressNo = " + expressNo);

										return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
									}
								}
							} else {
								WebElement ordersoon = driver.findElement(By.xpath("//div[@class='cnt-wrapper']"));
								String orderReady = ordersoon.getText().trim().toLowerCase();
								if(orderReady.contains("just a quick update...")){
									logger.error("[2]该订单还没发货,没产生物流单号");
									return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
								}
							}
						} catch (Exception e) {
							notFind = true;
						}
						
						if(notFind){
							notFind = false;
							
							try {
								TimeUnit.SECONDS.sleep(10);
								logger.debug("--->[2]开始查找物流");
								
								String expressCompanyStr = driver.getTitle().trim();
								String expressCompany = "";
								if(expressCompanyStr.contains("UPS")){
									expressCompany = "UPS";
								}
								WebElement expressNoEle = driver.findElement(By.xpath("//input[@class='modTxtMedium']"));
								String expressNo = expressNoEle.getAttribute("value");
								
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);

								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} catch (Exception e2) {
								notFind = true;
							}
						}
						
						// UPS tracking detail页面
						if(notFind){
							notFind = false;
							
							try {
								TimeUnit.SECONDS.sleep(10);
								logger.debug("--->[3]开始查找物流");
								
								//WebElement expressNoEle = driver.findElement(By.id("trackingnumber"));
								WebElement expressNoEle = driver.findElement(By.id("trkNum"));
								String expressNo = expressNoEle.getText().trim();
								
								String expressCompanyStr = driver.getTitle().trim();
								String expressCompany = "";
								if(expressCompanyStr.contains("UPS")){
									expressCompany = "UPS";
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} catch (Exception e3) {
								notFind = true;
							}
						}
						
						if(notFind){
							notFind = false;
							
							try {
								TimeUnit.SECONDS.sleep(10);
								logger.debug("--->[4]开始查找物流");
								
								WebElement trackNumEle = driver.findElement(By.xpath("//a[@target='track']"));
								String expressNo = trackNumEle.getText().trim();
								
								String expressCompanyStr = driver.getTitle().trim();
								String expressCompany = "";
								if(expressCompanyStr.contains("UPS")){
									expressCompany = "UPS";
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} catch (Exception e4) {
								notFind = true;
							}
						}
						
						if(notFind){
							notFind = false;
							
							try {
								TimeUnit.SECONDS.sleep(10);
								logger.debug("--->[5]开始查找物流");
								
								WebElement trackNumEle = driver.findElement(By.cssSelector(".waybill strong"));
								String expressNo = trackNumEle.getText().trim();
								expressNo=Pattern.compile("[^0-9]").matcher(expressNo).replaceAll("");
								String expressCompanyStr = driver.getCurrentUrl().trim();
								String expressCompany = "";
								if(expressCompanyStr.contains("UPS")){
									expressCompany = "UPS";
								}
								if(expressCompanyStr.contains("dhl")){
									expressCompany = "DHL";
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} catch (Exception e4) {
								notFind = true;
							}
						}
						
						Pattern pa = Pattern.compile("tracking number is\\s([0-9A-Z]+)\\s*,");
						
						// 改版过的UPS i-parcel Tracking
						if(notFind){
							notFind = false;
							
							try
							{
								TimeUnit.SECONDS.sleep(10);
								logger.debug("--->[6]开始查找物流");
								WebElement expressNoEles = driver.findElement(By.xpath("//span[contains(text(), 'you can link to it')]"));
								String expressNoStr = expressNoEles.getText().trim();
								String expressNo = null;
								Matcher matchs = pa.matcher(expressNoStr);
								if(matchs.find()){
									expressNo = matchs.group(1);
								}
								String expressCompanyStr = driver.getTitle().trim();
								String expressCompany = "";
								if(expressCompanyStr.contains("UPS")){
									expressCompany = "UPS";
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}
							catch (Exception e)
							{
								logger.error("[3]该订单还没发货,没产生物流单号");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
							}
					}  else {
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}
				}
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
			driver.get("http://us.asos.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'sign out')]")));
			return true;
		} catch (Exception e) {
			logger.error("--->跳转asos主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return null;
	}
	public static void main(String[] args) throws Exception {
		AsosAutoBuy autoBuy = new AsosAutoBuy();
		autoBuy.login("caaoxo@163.com", "tfb001001");
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("281574597");
		autoBuy.scribeExpress(detail);
	}
}
