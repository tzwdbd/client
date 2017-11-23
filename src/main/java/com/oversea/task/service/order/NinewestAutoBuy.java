package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class NinewestAutoBuy extends AutoBuy {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	public NinewestAutoBuy() {
		super(false);
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		driver.manage().window().maximize();
		logger.debug("--->调整浏览器尺寸和位置");
		
		driver.get("https://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/Login-Show");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			// 输入账号
			WebElement username = driver.findElement(By.id("signinEmail"));
			logger.debug("--->输入账号");
			Utils.sleep(500);
			username.sendKeys(userName);

			// 输入密码
			WebElement passward = driver.findElement(By.id("signinPwd"));
			logger.debug("--->输入密码");
			Utils.sleep(500);
			passward.sendKeys(passWord);

			// 提交
			WebElement submitBtn = driver.findElement(By.id("signinBtn1"));
			logger.debug("--->开始提交");
			Utils.sleep(500);
			submitBtn.click();

		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		// 等待登录完成
		try {
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='urAccount']")));
			logger.debug("--->登录完成");
			TimeUnit.SECONDS.sleep(2);
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
			driver.navigate().to("https://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/FluidStorefront-RenderCart");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		logger.error("--->清空购物车商品");
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("contentHolder")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='cart-item-row__actions-link remove-product']"));
			while (true) {
				int size = list.size();
				if(list!=null && size>0){
					list.get(0).click();
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.xpath("//a[@class='cart-item-row__actions-link remove-product']"));
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
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			logger.debug("--->确认购物车是否清空");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".grid-cell.default-100.fluid-empty-cart")));
		} catch (Exception e) {
			logger.debug("--->购物车清空异常");
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
			TimeUnit.SECONDS.sleep(10);
		} catch (Exception e) {
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		closeGuangGao();
		// 等待商品页面加载
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		
		// 开始选择sku
		logger.debug("--->开始选择sku");
		if (StringUtil.isNotEmpty(sku)) {
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i++) {
				String attrName = skuList.get(i);
				String attrValue = skuList.get(++i);
				if(attrName.equalsIgnoreCase("color")){
					try {
						List<WebElement> colorChips = driver.findElements(By.xpath("//div[@id='colorChips']/span/img"));
						Iterator<WebElement> iterator = colorChips.iterator();
						while(iterator.hasNext()){
							WebElement element = iterator.next();
							if(!element.getAttribute("alt").equalsIgnoreCase(attrValue)){
								iterator.remove();
							}
						}
						if(colorChips.size()==1){
							colorChips.get(0).click();
							TimeUnit.SECONDS.sleep(2);
						}else{
							logger.debug("--->选择color碰到异常");
							return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
						}
					} catch (Exception e) {
						logger.debug("--->选择sku碰到异常", e);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
				}else if(attrName.equalsIgnoreCase("size")){
					try {
						List<WebElement> sizes = driver.findElements(By.xpath("//div[@id='sizes']/span"));
						Iterator<WebElement> iterator = sizes.iterator();
						while(iterator.hasNext()){
							WebElement element = iterator.next();
							if(!element.getText().equalsIgnoreCase(attrValue)){
								iterator.remove();
							}
						}
						if(sizes.size()==1){
							if(sizes.get(0).getAttribute("class").contains("notAvailable")){
								logger.debug("该size没有库存了");
								return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
							}
							sizes.get(0).click();
							TimeUnit.SECONDS.sleep(2);
						}else{
							logger.debug("--->选择size碰到异常");
							return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
						}
					} catch (Exception e) {
						logger.debug("--->选择sku碰到异常",e);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
				}else if(attrName.equalsIgnoreCase("width")){
					
				}else{
					logger.debug("未识别的sku样式");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}
		}
		logger.debug("--->选择sku完成");
		
		// 获取单价 判断库存
		try{
			WebElement pricePanel = driver.findElement(By.id("PDPSellingPrice"));
			String singlePrice = pricePanel.getText().replaceAll("[^0-9.]", "");
			logger.debug("单价："+singlePrice);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, singlePrice);
			String productEntityId = param.get("productEntityId");
			if(StringUtil.isEmpty(productEntityId)){
				logger.error("--->productEntityId  is Null");
			}else{
				priceMap.put(param.get("productEntityId"), singlePrice);
			}
		}catch (Exception e){
			logger.error("--->获取单价 判断库存 异常",e);
		}
		
		// 加购物车
		logger.debug("--->开始加购物车");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement addCart = driver.findElement(By.id("addToCartButton"));
			addCart.click();
			TimeUnit.SECONDS.sleep(2);
			logger.debug("--->加入购物车成功");
		} catch (Exception e) {
			logger.debug("--->加入购物车失败",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		
		//设置商品数量
		if(!"1".equalsIgnoreCase(productNum)){
			try {
				WebElement sele = driver.findElement(By.id("quantityValue"));
				Select select = new Select(sele);
				
				Set<String> set = new HashSet<String>();
				List<WebElement> list = select.getOptions();
				for (WebElement webElement : list) {
					set.add(webElement.getText());
				}
				if(!set.contains(productNum)){
					logger.debug("不能选择这么多："+productNum);
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
				
				select.selectByValue(productNum);
				logger.debug("设置数量成功");
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {
				logger.debug("设置数量失败",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		logger.debug("--->确认是否加购物车成功");
		try{
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			WebElement shopping = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("minibagContinue")));
			logger.debug("--->继续购物");
			shopping.click();
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		
		String cNo = param.get("cardNo").replaceAll("[^0-9]", "");
		String suffixNo = param.get("suffixNo").replaceAll("[^0-9]", "").trim();
		String expiryDate = param.get("expiryDate");
		
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		logger.error("--->myPrice = " + myPrice);

		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		logger.debug("--->等待购物车页面加载");
		try {
			driver.navigate().to("https://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/FluidStorefront-RenderCart");
		} catch (Exception e) {
			logger.debug("--->加载Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		logger.debug("--->等待支付页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			for (String code : promotionList) {
				try {
					WebElement promo = driver.findElement(By.xpath("//input[@id='promoCode']"));
					promo.click();
					promo.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
					
					WebElement apply = driver.findElement(By.xpath("//a[@class='submit-button fluid-checkout-button fluid-checkout-button--secondary fluid-checkout-button--half']"));
					apply.click();
					TimeUnit.SECONDS.sleep(1);
					try {
						WebElement error = driver.findElement(By.xpath("//p[@class='form-error__text js-form-error-text']"));
						if(error.getText().contains("is not valid.")){
							logger.debug("优惠码不可用");
							statusMap.put(code, 10);
						}else{
							logger.debug("未识别的提示信息："+error.getText());
							statusMap.put(code, 10);
						}
					} catch (Exception e) {
						try {
							logger.debug("优惠码有效");
							statusMap.put(code, 0);
							isEffective = true;
						} catch (Exception e2) {
							logger.debug("异常【3】", e2);
						}
					}
				} catch (Exception e) {
					logger.debug("输入优惠码异常");
				}
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		try {
			driver.executeAsyncScript("document.querySelectorAll('.fluid-checkout-button__text')[0].click();");
		} catch (Exception e) {
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			logger.debug("开始选择地址");
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#shipping-addressForm")));
//			List<WebElement> savedAddresses =  driver.findElements(By.cssSelector("div#shipping-addressForm div.saved-addresses__item-actions input"));
//			closeAdv();
//			int sumAddress = savedAddresses.size()-1;
//			logger.debug("共有"+sumAddress+"个地址可选");
//			int target = count % sumAddress;
//			savedAddresses.get(target).click();
			logger.debug("选择地址成功");
			TimeUnit.SECONDS.sleep(1);
			//closeAdv();
			WebElement submit = driver.findElement(By.cssSelector("input[value='Continue to Billing']"));
			//WebElement submit = driver.findElement(By.xpath("//input[@class='submit-button fluid-checkout-button fluid-checkout-button--primary continue-button']"));
			//submit.click();
			driver.executeScript("var tar=arguments[0];tar.click();", submit);
			TimeUnit.SECONDS.sleep(5);
//			try {
//				TimeUnit.SECONDS.sleep(2);
//				driver.findElement(By.xpath("//input[@class='submit-button secondary-button continue-button']")).click();
//			} catch (Exception e) {}
		} catch (Exception e) {
			logger.debug("选择地址出现异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL; 
		}
		
		try {
			TimeUnit.SECONDS.sleep(5);
			try {
				logger.debug("开始输入信用卡信息");
				WebElement cardN = driver.findElement(By.xpath("//input[@id='cardNumberFieldGuest']"));
				cardN.sendKeys(cNo);
				TimeUnit.SECONDS.sleep(1);
				
				WebElement cardCodeField = driver.findElement(By.xpath("//input[@id='cardCodeFieldGuest']"));
				cardCodeField.sendKeys(suffixNo.replaceAll("[^0-9]", "").trim());
				TimeUnit.SECONDS.sleep(1);

				WebElement Month = driver.findElement(By.xpath("//select[@name = 'monthGuest']"));
				Select mSelect = new Select(Month);
				String[] time = expiryDate.split(" ");
				mSelect.selectByIndex(Integer.parseInt(time[1]));
				
				WebElement year = driver.findElement(By.xpath("//select[@name = 'yearGuest']"));
				Select ySelect = new Select(year);
				ySelect.selectByValue(time[0]);
				
//				try {
//					TimeUnit.SECONDS.sleep(5);
//					WebElement uses = driver.findElement(By.xpath("//div[@class='use-shipping-address checkbox']/input"));
//					uses.click();
//				} catch (Exception e) {
//				}
//				
//				closeAdv();
//				try {
//					logger.debug("信用卡信息输入完成");
//					driver.findElement(By.xpath("//input[@class='fluid-checkout-button--primary submit-button primary-button continue-button']")).click();;
//					TimeUnit.SECONDS.sleep(5);
//				} catch (Exception e) {
//				}
			} catch (Exception e) {
			}
			try {
				WebElement element = driver.findElement(By.xpath("//input[@class='fluid-checkout-button--primary submit-button primary-button continue-button']"));
				element.click();
				List<WebElement> elements = driver.findElements(By.cssSelector("input[value='Continue to Order Review']"));
				for(WebElement w:elements){
					if(w.isDisplayed()){
						driver.executeScript("var tar=arguments[0];tar.click();", w);
						break;
					}
				}
				
				logger.debug("确认信用卡信息");
			} catch (Exception e) {
			}
		} catch (Exception e) {
			logger.debug("输入信用卡信息出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 查询总价
		try {
			WebElement total = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-total-value")));
			String price = total.getText();
			Matcher m = Pattern.compile("[0-9.]+").matcher(price);
			if(m.find()){
				price = m.group();
				logger.debug("找到总价"+price);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, price);
			}else{
				logger.debug("找不到总价");
				return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
			}
			if(!StringUtil.isBlank(getTotalPrice())){
				AutoBuyStatus priceStatus = comparePrice(price, getTotalPrice());
				if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}else{
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(price);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 5.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
			TimeUnit.SECONDS.sleep(3);
			WebElement placeOrder = driver.findElement(By.xpath("//a[@class='fluid-checkout-button fluid-checkout-button--primary submit-button primary-button place-order-button']"));
			logger.debug("点击placeOrder");
			if(isPay){
				placeOrder.click();
			}
		} catch (Exception e) {
			logger.debug("付款出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		// 查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			WebElement order = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='grid-cell default-70 tablet-60 mobile-100 order-confirmation-info']")));
			Matcher m = Pattern.compile("E[0-9]{5,7}").matcher(order.getText());
			if(m.find()){
				String orderNo = m.group();
				logger.debug("找到订单号："+orderNo);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
				savePng();
			}else{
				logger.debug("--->查找商品订单号出现异常");
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
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		logger.debug(mallOrderNo);
		try {
			driver.navigate().to("https://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/Customer-Orders");
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		try {
			List<WebElement> list =  driver.findElements(By.cssSelector(".orderHolder"));
			Iterator<WebElement> iterator = list.iterator();
			while (iterator.hasNext()) {
				WebElement element = iterator.next();
				logger.debug(element.getText());
				if(element.getText().contains(mallOrderNo)){
					driver.executeScript("showOrderDetails(true, '"+mallOrderNo+"');");
					WebElement items = element.findElement(By.cssSelector(".orderContent .productLineItemDetails .orderStatusTracking .statusBlack"));
					String sth = items.getText();
					if(sth.toLowerCase().contains("shipped")){
						logger.debug("发货了");
						WebElement trackElement = element.findElement(By.cssSelector(".orderContent .productLineItemDetails .orderStatusTracking a.orderContentValue"));
						logger.debug("找到物流单号:"+trackElement.getText());
						if(trackElement.getText().startsWith("61")){
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, "92"+trackElement.getText());
						}else{
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackElement.getText());
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "FedEx");
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}else if(sth.toLowerCase().contains("cancel")){
						logger.debug("被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(sth.toLowerCase().contains("processing")){
						logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(sth.toLowerCase().contains("available")){
						logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else{
						logger.debug("订单状态不明："+sth);
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("爬物流失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		return true;
	}
	public void closeGuangGao(){
		try {
			Utils.sleep(1000);
			WebElement close = driver.findElement(By.id("t005_1-close"));
			close.click();
			logger.debug("closeGuangGao");
		} catch (Exception e) {
		}
	}
	
	public void closeAdv(){
		try {
			Utils.sleep(100);
			WebElement close = driver.findElement(By.xpath("//div[@class='qubit-AdvancedModal-close']"));
			close.click();
			logger.debug("closeAdv");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return null;
	}
	public static void main(String[] args){
		//47.88.35.138
		NinewestAutoBuy autoBuy = new NinewestAutoBuy();
		autoBuy.login("hy11hy@outlook.com", "tfb001001");
		System.out.println("======================");
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("E0840222");
		autoBuy.scribeExpress(detail);
		/*autoBuy.cleanCart();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("url", "http://www.rebatesme.com/zh/click/?key=edb217332c4dbd6539a5367b09144dd9&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=4077dc10681088b33f2cf7955f64b547&targetUrl=http%3A%2F%2Fwww.ninewest.com%2FAccount-Kiltie-Loafers%2F23227185%2Cdefault%2Cpd.html%3FvariantColor%3DJJ2TUC0%26cgid%3D10726902%26prefn1%3DvariantSize%26prefv1%3D6%252e5%26srule%3DPriceLowHigh");
		map.put("sku", "[[\"color\",\"BLACK LEATHER\"],[\"size\",\"9.5\"],[\"width\",\"M\"]]");
		map.put("num", "3");
		map.put("productEntityId", "111112");
		autoBuy.selectProduct(map);
		map.put("url", "http://www.rebatesme.com/zh/click/?key=edb217332c4dbd6539a5367b09144dd9&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=4077dc10681088b33f2cf7955f64b547&targetUrl=http%3A%2F%2Fwww.ninewest.com%2FMystik-Platform-Sneakers%2F22533569%2Cdefault%2Cpd.html%3FvariantColor%3DJJ169F2%26cgid%3D10726902%26prefn1%3DvariantSize%26prefv1%3D6%252e5%26srule%3DPriceLowHigh");
		map.put("sku", "[[\"color\",\"BLACK SYNTHETIC\"],[\"size\",\"6\"],[\"width\",\"M\"]]");
		autoBuy.selectProduct(map);*/
	}
}
