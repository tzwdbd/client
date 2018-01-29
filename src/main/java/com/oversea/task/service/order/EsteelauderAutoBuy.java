package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.ExternalOrderDetail;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.ExpressUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: liuxf 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2017年3月4日 下午5:04:14 
*/
public class EsteelauderAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	public EsteelauderAutoBuy() {
		super(false);
	}
	

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("https://www.esteelauder.com/");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		driver.navigate().to("https://www.esteelauder.com/");
		try {
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("span[data-test-id='gnav_account_menu_login']")));
			WebElement w = driver.findElement(By.cssSelector("span[data-test-id='gnav_account_menu_login']"));
			driver.executeScript("var tar=arguments[0];tar.click();", w);
		} catch (Exception e) {
			logger.error("--->点击去登录页面异常",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test-id='gnav_login_form_email']")));
		}
		catch (Exception e)
		{
			WebElement w = driver.findElement(By.cssSelector("span[data-test-id='gnav_account_menu_login']"));
			driver.executeScript("var tar=arguments[0];tar.click();", w);
		}

		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test-id='gnav_login_form_email']")));
			WebElement username = driver.findElement(By.cssSelector("input[data-test-id='gnav_login_form_email']"));
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(1);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test-id='gnav_login_form_password']")));
			WebElement password = driver.findElement(By.cssSelector("input[data-test-id='gnav_login_form_password']"));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(1);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			WebElement username = driver.findElement(By.cssSelector("input[data-test-id='gnav_login_form_email']"));
			username.clear();
			TimeUnit.SECONDS.sleep(1);
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(1);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			logger.debug("--->开始登陆");
			WebElement btn = driver.findElement(By.cssSelector("input[data-test-id='gnav_form_login']"));
			btn.click();
			logger.debug("--->点击登陆");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			logger.debug("--->确认是否登陆成功");
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".user-greeting")));
			logger.debug("--->登陆成功");
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			TimeUnit.SECONDS.sleep(1);
			driver.navigate().to("https://www.esteelauder.com/checkout/viewcart.tmpl");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		logger.debug("--->清空购物车");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("viewcart-panel")));
			
			Utils.sleep(800);
			logger.error("--->购物车页面加载完成");
			//清理
			List<WebElement> list = driver.findElements(By.cssSelector("a[data-test-id='cart_product_remove']"));
			logger.error("--->开始清理购物车"+list.size());
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					driver.executeScript("var tar=arguments[0];tar.click();", list.get(0));
					TimeUnit.SECONDS.sleep(2);
					try {
						WebElement w = driver.findElement(By.cssSelector(".remove-buttons a"));
						driver.executeScript("var tar=arguments[0];tar.click();", w);
					} catch (Exception e) {
						logger.error("--->.remove-buttons 点击异常");
					}
					TimeUnit.SECONDS.sleep(1);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("viewcart-panel")));
					if(size>1){
						list = driver.findElements(By.cssSelector("a[data-test-id='cart_product_remove']"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
			Utils.sleep(1000);
			logger.error("--->购物车页面清理完成");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		try {
			logger.error("--->确认购物车是否清空");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".viewcart-empty")));
		} catch (Exception e) {
			logger.debug("--->购物车不为空！");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}
	
	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		
		logger.debug("--->选择商品 orginalUrl = " + productUrl);
		try{
			driver.navigate().to(productUrl);
		}
		catch (Exception e){
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content")));
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		driver.executeScript("(function(){window.scrollBy(1,250);})();");
		List<String> skuList = null;
		try {	
			TimeUnit.SECONDS.sleep(3);
			logger.debug("--->商品页面加载完成");
			
			String sku = param.get("sku");
			
			// 开始选择sku
			logger.debug("--->开始选择sku");
			Map<String, String> skuMap = new HashMap<String, String>();
			if (StringUtil.isNotEmpty(sku)) {
				skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrValue = skuList.get(i);
						skuMap.put(attrName, attrValue);
					}
				}
				try {
					List<WebElement> skuChooseElement = driver.findElements(By.cssSelector(".swatches--single a"));
					for (int i = 0; i < skuList.size(); i++){
						if (i % 2 == 1){
							if(skuList.get(i-1).toLowerCase().equals("color")){
								boolean mark = true;
								for(WebElement w:skuChooseElement){
									if(w.getAttribute("name").equalsIgnoreCase(skuList.get(i))){ 
										w.click();
										logger.debug("--->选择"+w.getAttribute("name"));
										mark = false;
										break;
									}
								}
								if(mark){
									logger.debug("--->找不到指定颜色"+skuList.get(i));
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						}
					}
				} catch (Exception e) {
				}
				String productEntityId = param.get("productEntityId");
				try {
					WebElement size = driver.findElement(By.cssSelector(".product-full__price-text"));
					String sizeName = size.getText().split("\\$")[0].trim();
					for (int i = 0; i < skuList.size(); i++){
						if (i % 2 == 1){
							if(skuList.get(i-1).toLowerCase().equals("size")){
								boolean mark = true;
								if(sizeName.equalsIgnoreCase(skuList.get(i))){
									mark = false;
									logger.debug("--->选择"+skuList.get(i));
								}
								if(mark){
									logger.debug("--->找不到指定size"+skuList.get(i));
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						}
					}
					logger.debug("--->[1-1]找到商品单价  = " + size.getText().split("\\$")[1].trim());
					priceMap.put(productEntityId, size.getText().split("\\$")[1].trim());
				} catch (Exception e) {
				}
				try {
					WebElement size = driver.findElement(By.cssSelector(".product-full__price-size-select .selectBox-label"));
					String sizeName = size.getText().split("\\$")[0].trim();
					for (int i = 0; i < skuList.size(); i++){
						if (i % 2 == 1){
							if(skuList.get(i-1).toLowerCase().equals("size")){
								boolean mark = true;
								if(sizeName.equalsIgnoreCase(skuList.get(i))){
									mark = false;
									logger.debug("--->选择"+skuList.get(i));
								}
								if(mark){
									logger.debug("--->找不到指定size"+skuList.get(i));
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						}
					}
					logger.debug("--->[1-1]找到商品单价  = " + size.getText().split("\\$")[1].trim());
					priceMap.put(productEntityId, size.getText().split("\\$")[1].trim());
				} catch (Exception e) {
				}
				
				
			}
		}catch (Exception e) {
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		
		logger.debug("--->选择sku完成");
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			logger.error("--->选择数量失败");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		// 加购物车
		logger.debug("--->开始加购物车");
		try{
			TimeUnit.SECONDS.sleep(3);
			WebElement addCard =  wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".js-add-to-cart")));
			driver.executeScript("var tar=arguments[0];tar.click();", addCard);
			
			WebElement check =  wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".cart-confirm__shade-button")));
			String content = "";
			List<WebElement> shades = driver.findElements(By.cssSelector(".cart-confirm__shade"));
			for(WebElement w:shades){
				content+=w.getText();
			}
			for (int i = 0; i < skuList.size(); i++) {
				if (i % 2 == 1) {
					String attrValue = skuList.get(i).trim();
					logger.debug("--->attrValue:"+attrValue);
					if(!content.contains(attrValue)){
						logger.debug("--->找不到指定sku"+skuList.get(i));
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
				}
			}
			check.click();
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".gwp-continue-checkout")));
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount,List<GiftCard> giftCardList) {
		
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		try {
			TimeUnit.SECONDS.sleep(1);
			driver.navigate().to("https://www.esteelauder.com/checkout/viewcart.tmpl");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		WebDriverWait wait = new WebDriverWait(driver, 40);
		
		try
		{
			logger.error("--->购物车页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("viewcart-panel")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
		}catch (Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		//处理礼品
		try {
			WebElement panel = driver.findElement(By.cssSelector("#promo-panel div"));
			panel.click();
			logger.debug("--->礼品点击");
			TimeUnit.SECONDS.sleep(1);
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".offer__kit__option--01")));
			} catch (Exception e) {
				panel = driver.findElement(By.cssSelector("#promo-panel div"));
				driver.executeScript("var tar=arguments[0];tar.click();", panel);
				logger.debug("--->礼品点击1");
				TimeUnit.SECONDS.sleep(1);
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".offer__kit__option--01")));
			TimeUnit.SECONDS.sleep(1);
			driver.executeScript("(function(){window.scrollBy(1,250);})();");
			TimeUnit.SECONDS.sleep(1);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".offer__kit__option--01")));
			List<WebElement> options = driver.findElements(By.cssSelector(".offer__kit__option--01"));
			for(WebElement w:options){
				if(w.getText().contains("SOLD OUT")){
					logger.debug("--->礼品售罄");
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}
			if(options.size()!=2){
				logger.debug("--->礼品售罄");
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
			List<WebElement> offers = driver.findElements(By.cssSelector(".offer__kit__option--01 input"));
			for(WebElement w:offers){
				driver.executeScript("var tar=arguments[0];tar.click();", w);
				driver.executeScript("(function(){window.scrollBy(1,250);})();");
				Utils.sleep(2000);
			}
			WebElement addcard = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".offer__kit__button__add")));
			driver.executeScript("var tar=arguments[0];tar.click();", addcard);
//			TimeUnit.SECONDS.sleep(3);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".offer__kit__success__overlay__button")));
//			List<WebElement> overlays = driver.findElements(By.cssSelector(".offer__kit__success__overlay__button"));
//			for(WebElement w:overlays){
//				if(w.isDisplayed()){
//					driver.executeScript("var tar=arguments[0];tar.click();", w);
//					break;
//				}
//			}
			try {
				TimeUnit.SECONDS.sleep(3);
				driver.navigate().to("https://www.esteelauder.com/checkout/viewcart.tmpl");
			} catch (Exception e) {
				logger.error("--->跳转到购物车失败",e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			try
			{
				logger.error("--->购物车页面加载");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("viewcart-panel")));
				Utils.sleep(2000);
				logger.error("--->购物车页面加载完成");
			}catch (Exception e){
				logger.error("--->跳转到购物车失败");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		} catch (Exception e) {
			logger.error("--->处理礼品异常",e);
		}
		String size = param.get("size");
		try {
			List<WebElement> goodsInCart = driver.findElements(By.cssSelector("a[data-test-id='cart_product_remove']"));
			logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
			logger.debug("--->size有 [" + size + "]件商品");
			if(!size.equals(String.valueOf(goodsInCart.size()))){
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->购物车验证数量出错",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		
		//输入优惠码
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		//优惠码
		if(promotionList != null && promotionList.size() > 0){
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						WebElement codeInput = driver.findElement(By.id("form--offer_code--field--OFFER_CODE"));
						codeInput.clear();
						Utils.sleep(800);
						codeInput.sendKeys(code);
						Utils.sleep(900);
						
						driver.findElement(By.cssSelector("input[data-test-id='form_offer_code_apply']")).click();
						Utils.sleep(5500);
						
						try{
							wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".offer-code__messages")));
							statusMap.put(code, 0);
						}catch(Exception e){
							try {
								statusMap.put(code, 10);
								isEffective = true;
							} catch (Exception e2) {
								logger.error("promotionCode:"+code,e);
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
		}
		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test-id='cart_primarycheckout']")));
			WebElement checkout = driver.findElement(By.cssSelector("a[data-test-id='cart_primarycheckout']"));
			checkout.click();
			logger.error("--->点击checkout");
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[data-test-id='form_checkout_samples_continue']")));
			WebElement samples = driver.findElement(By.cssSelector("input[data-test-id='form_checkout_samples_continue']"));
			samples.click();
			logger.error("--->点击samples");
			
		}catch (Exception e){
			logger.error("--->点击checkout失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try {
			Utils.sleep(800);
			WebElement billing = driver.findElement(By.cssSelector("input[data-test-id='form_checkout_billing_continue']"));
			driver.executeScript("var tar=arguments[0];tar.click();", billing);
		} catch (Exception e) {
			logger.error("--->点击billing失败");
		}
		
		String shipPriceStr = "0";
		// 查询总价
		BigDecimal total = new BigDecimal(0.00);
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".total.value")));
			String text = totalPriceElement.getText().replace(",", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = " + priceStr);
				if(!StringUtil.isBlank(getTotalPrice())){
					total = new BigDecimal(priceStr);
					AutoBuyStatus priceStatus = comparePrice(priceStr, getTotalPrice());
					if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}else{
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(priceStr);
					BigDecimal s = new BigDecimal(shipPriceStr);
					BigDecimal v = y.subtract(x).subtract(s);
					total = y;
					if (v.doubleValue() > 9.00D) {
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		// placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		try {
			WebElement placeOrderButton = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-test-id='form_checkout_confirm']")));
			Utils.sleep(1500);
			if (isPay) {
				logger.debug("------------------");
				logger.debug("--->啊啊啊啊，我要付款啦！！！");
				driver.executeScript("var tar=arguments[0];tar.click();", placeOrderButton);
				logger.debug("------------------");
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		} catch (Exception e) {
			logger.debug("--->付款失败");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		
		// 查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[data-test-id='order_id']")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.cssSelector("a[data-test-id='order_id']"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取Esteelauder单号成功:\t" + orderNumber);
				orderNumber = ExpressUtils.regularMallOrderNo(orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
			} else {
				logger.error("--->获取Esteelauder单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->查找商品订单号出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	@Override
	public AutoBuyStatus scribeExpress(ExternalOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String mallOrderNo = detail.getMallOrderNo();
		
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			String orderUrl ="https://www.esteelauder.com/account/order_history/index.tmpl";
			driver.navigate().to(orderUrl);
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-test-id='past_purchase_data']")));
			List<WebElement> orders = driver.findElements(By.cssSelector("div[data-test-id='past_purchase_data']"));
			int find = 0;
			for(WebElement o:orders){
				WebElement w = o.findElement(By.cssSelector("a"));
				String str = w.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					WebElement orderStatus = o.findElement(By.cssSelector(".order-status-data"));
					find = 1;
					logger.error("text:"+orderStatus.getText());
					if(orderStatus.getText().contains("Cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(orderStatus.getText().contains("Processing")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Warehouse")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Shipped")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector(".tracking-link-list a"));
						String href = orderNoElement.getAttribute("href");
						if(href.contains("ontrac")){
							logger.error("expressCompany =narvar ");
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "ONTRAC");
						}else{
							logger.debug("未识别的物流状态"+str);
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						String expressNo = ExpressUtils.regularExperssNo(href);
						String[] expressGroup = expressNo.split(",");
						for(String s:expressGroup){
							if(s.startsWith("D")){
								expressNo = s;
								break;
							}
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}else{
						logger.debug("未识别的物流状态"+str);
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}
				}
			}
			if(find==0){
				logger.error("物流只能查看邮箱");
				return AutoBuyStatus.AUTO_SCRIBE_CALL_CUSTOMER_SERVICE;
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("https://www.esteelauder.com/");
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转esteelauder主页面碰到异常");
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
		EsteelauderAutoBuy auto = new EsteelauderAutoBuy();
		//AutoBuyStatus status = auto.login("tzwdbd@126.com", "Tfb001001");
		//System.out.println(status);
		/*AutoBuyStatus status1 = auto.cleanCart();
		System.out.println(status1);*/
		Map<String, String> param = new HashMap<String, String>();
//		param.put("url", "https://www.victoriassecret.com/bras/shop-all-bras/add-2-cups-multi-way-push-up-bra-bombshell?ProductID=303412&CatalogueType=OLS");
////		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
//		//param.put("sku", "[[\"color\",\"Red\"]]");
//		param.put("orginalUrl", "https://www.victoriassecret.com/bras/shop-all-bras/add-2-cups-multi-way-push-up-bra-bombshell?ProductID=303412&CatalogueType=OLS");
//		param.put("sku", "[[\"color\",\"black\"],[\"bandSize\",\"34\"],[\"cupSize\",\"DD\"]]");
//		param.put("num", "3");
//		param.put("productEntityId", "7512528");
//		param.put("isPay", "false");
//		param.put("isFirst", "ture");
//		param.put("count", "1");
//		param.put("suffixNo", "123");
//		param.put("userName", "okpljhb@163.com");
//		param.put("password", "Tfb001001");
//		AutoBuyStatus status2 = auto.selectProduct(param);
//		System.out.println(status2);
		Map<String, String> param1 = new HashMap<String, String>();
		param1.put("url", "https://www.esteelauder.com/product/681/26959/product-catalog/skincare/advanced-night-repair/synchronized-recovery-complex-ii");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param1.put("orginalUrl", "https://www.esteelauder.com/product/681/26959/product-catalog/skincare/advanced-night-repair/synchronized-recovery-complex-ii");
		param1.put("sku", "[[\"size\",\"1.0 oz.\"]]");
		param1.put("num", "1");
		param1.put("productEntityId", "7825898");
		param1.put("isPay", "false");
		param.put("isFirst", "ture");
		param.put("count", "1");
		param.put("suffixNo", "123");
		param.put("my_price", "50");
		param.put("userName", "sbctfb@163.com");
		param.put("password", "Tfb001001");
		AutoBuyStatus status3 = auto.selectProduct(param1);
//		Map<String, String> param2 = new HashMap<String, String>();
//		param2.put("url", "https://www.joesnewbalanceoutlet.com/product/wl696-smp/wl696sb");
////		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
//		//param.put("sku", "[[\"color\",\"Red\"]]");
//		param2.put("orginalUrl", "https://www.joesnewbalanceoutlet.com/product/wl696-smp/wl696sb");
//		param2.put("sku", "[[\"color\",\"Black with Blue\"],[\"size\",\"8.5\"],[\"width\",\"B\"]]");
//		param2.put("num", "3");
//		param2.put("productEntityId", "7825898");
//		param2.put("isPay", "false");
//		AutoBuyStatus status4 = auto.selectProduct(param2);
//		System.out.println(status3);
		
		/*AutoBuyStatus status6 = auto.pay(param);
		System.out.println(status6);*/
		
		
		/*RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("662842271");
		detail.setProductSku("[[\"color\",\"001 Pink\"]]");
		auto.scribeExpress(detail);*/
		//auto.logout();
	}


	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String mallOrderNo = detail.getMallOrderNo();
		
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			String orderUrl ="https://www.esteelauder.com/account/order_history/index.tmpl";
			driver.navigate().to(orderUrl);
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-test-id='past_purchase_data']")));
			List<WebElement> orders = driver.findElements(By.cssSelector("div[data-test-id='past_purchase_data']"));
			int find = 0;
			for(WebElement o:orders){
				WebElement w = o.findElement(By.cssSelector("a"));
				String str = w.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					WebElement orderStatus = o.findElement(By.cssSelector(".order-status-data"));
					find = 1;
					logger.error("text:"+orderStatus.getText());
					if(orderStatus.getText().contains("Cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(orderStatus.getText().contains("Processing")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Warehouse")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Shipped")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector(".tracking-link-list a"));
						String href = orderNoElement.getAttribute("href");
						logger.error("href = "+href);
						if(href.contains("ontrac")){
							logger.error("expressCompany =narvar ");
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "ONTRAC");
							String expressNo = ExpressUtils.regularExperssNo(href);
							String[] expressGroup = expressNo.split(",");
							for(String s:expressGroup){
								if(s.startsWith("D")){
									expressNo = s;
									break;
								}
							}
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}else if(href.contains("ups")){
							logger.error("expressCompany =ups ");
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
							String expressNo = ExpressUtils.regularExperssNo(href);
							String[] expressGroup = expressNo.split(",");
							for(String s:expressGroup){
								if(s.startsWith("1Z")){
									expressNo = s;
									break;
								}
							}
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}else{
							logger.debug("未识别的物流状态"+str);
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						
					}else{
						logger.debug("未识别的物流状态"+str);
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}
				}
			}
			if(find==0){
				logger.error("物流只能查看邮箱");
				return AutoBuyStatus.AUTO_SCRIBE_CALL_CUSTOMER_SERVICE;
			}
			
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}
	
}
 