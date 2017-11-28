package com.oversea.task.service.order;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.ExpressNode;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class AutoBuySpring extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		
		AutoBuySpring auto = new AutoBuySpring();
		AutoBuyStatus status = auto.login("plmono@163.com", "Tfb001001");
//		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = auto.cleanCart();
//			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				param.put("url", "https://www.shopspring.com/products/53905890");
//				param.put("url", "http://www.6pm.com/ugg-sea-glisten-anchor-red-suede");
//				param.put("url", "http://www.6pm.com/gabriella-rocha-alena-evening-purse-with-tassel-black");
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				param.put("sku", "[[\"Color\",\"Marine\"],[\"Size\",\"S\"]]");
				param.put("productEntityId", "1112");
				param.put("num", "2");
				auto.selectProduct(param);
//				param.put("url", "https://www.shopspring.com/products/52914274?sortBy=price&sortOrder=DESC");
////				param.put("url", "http://www.6pm.com/ugg-sea-glisten-anchor-red-suede");
////				param.put("url", "http://www.6pm.com/gabriella-rocha-alena-evening-purse-with-tassel-black");
////				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
//				param.put("sku", "[[\"Color\",\"CHARCOAL\"],[\"Size\",\"XXL\"]]");
//				param.put("productEntityId", "1112");
//				param.put("num", "3");
//				auto.selectProduct(param);
				//if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "208.50");
//					param0.put("count", "1");
//					status = auto.pay(param0);
//				//}
//			}
//		}
		//auto.logout();
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("C4060048");
//		auto.scribeExpress(detail);
	}
	
	public static void main0(String[] args){
		AutoBuySpring auto = new AutoBuySpring();
		AutoBuyStatus status = auto.login("luyao.huang@yahoo.com", "leixun123456");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.scribeExpress("114-4751822-5849814");
			System.out.println("status = "+ status.getValue());
		}
	}
	
	public AutoBuySpring(){
		super(false);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().setSize(new Dimension(1280, 720));
		
		try
		{
			driver.get("https://www.shopspring.com");
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		//点击登录
		try
		{
			driver.navigate().to("https://www.shopspring.com/signin");
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		// 等到[输入框]出现
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']")));
			logger.debug("--->开始输入账号");
			Utils.sleep(900);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.cssSelector("input[name='password']"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1100);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.cssSelector("button[type='submit']"));
			logger.debug("--->开始提交");
			submit.click();
		}
		catch (Exception e)
		{
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		//等待登录完成
		try
		{
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart_fssplm")));
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
		// TODO Auto-generated method stub
		//跳转到购物车
		try{
			driver.get("https://www.shopspring.com/cart");
			logger.error("--->开始跳转到购物车");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, 45);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-xfe-testid='cart-active-products']")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			//循坏清除
			List<WebElement> list = driver.findElements(By.xpath("//span[contains(text(),'Remove')]"));
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					driver.executeScript("var tar=arguments[0];tar.click();", list.get(0));
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.xpath("//span[contains(text(),'Remove')]"));
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
			logger.error("--->确认购物车是否清理完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".noItemsButton_1dk02ws")));
			return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
		} catch (Exception e) {
			logger.debug("--->购物车数量清空异常");
			try{
				driver.get("https://www.shopspring.com/cart");
				logger.error("--->开始跳转到购物车");
			}catch(Exception e1){
				logger.error("--->跳转到购物车失败");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}
		try {
			logger.error("--->确认购物车是否清理完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".noItemsButton_1dk02ws")));
		} catch (Exception e) {
			logger.debug("--->购物车数量清空异常");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
		
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		// TODO Auto-generated method stub
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try
		{
			driver.navigate().to(productUrl);
		}
		catch (Exception e){
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-xfe-testid='product_attribute_selectors']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		//开始选择sku
		logger.debug("--->开始选择sku");
		List<String> skuList = null;
		try{
			skuList = Utils.getSku((String) sku);
			int findCount = 0;
			try {
				List<WebElement> singleelements = driver.findElements(By.cssSelector("div[data-xfe-testid='product_attribute_selectors'] div[class^='single']"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : singleelements){
							try{
								if(element.isDisplayed()){
									if(element.getText().toLowerCase().contains(skuList.get(i-1).toLowerCase()) && element.getText().toLowerCase().contains(skuList.get(i).toLowerCase())){
										findCount++;
										logger.debug("--->选择了"+skuList.get(i) );
										Utils.sleep(500);
									}
								}
							}catch(Exception e){
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
					}
				}
			} catch (Exception e) {
			}
			try {
				List<WebElement> selectelements = driver.findElements(By.cssSelector("div[data-xfe-testid='product_attribute_selectors'] div[data-xfe-testid='variant_selector_dropdown_container']"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : selectelements){
							try{
								if(element.isDisplayed() && element.getText().contains(skuList.get(i-1))){
									logger.debug("--->element.getText():"+element.getText() );
									WebElement updown = element.findElement(By.cssSelector("span"));
									updown.click();
									Utils.sleep(500);
									List<WebElement> labels = element.findElements(By.cssSelector(".options_13pflw div[data-xfe-testid='variant_selector_dropdown_option']"));
									for(WebElement label:labels){
										if(label.isDisplayed()){
											String test = label.getText().replace("Unavailable with current selections", "").trim();
											logger.debug("--->test:"+test );
											if(test.toLowerCase().equals(skuList.get(i).toLowerCase())){
												findCount++;
												driver.executeScript("var tar=arguments[0];tar.click();", label);
												Utils.sleep(3500);
												logger.debug("--->点击name" );
												driver.findElement(By.cssSelector("span[data-xfe-testid='pdp_product_name']")).click();
												logger.debug("--->选择了"+skuList.get(i) );
												break;
											}
										}
									}
								}
							}catch(Exception e){
								return AutoBuyStatus.AUTO_SKU_NOT_FIND;
							}
						}
					}
				}
			} catch (Exception e) {
				
			}
			
			
			if(findCount < skuList.size()/2){
				logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->选择sku完成");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class^='priceContainer'] h4")));
			WebElement priceElement = driver.findElement(By.cssSelector("div[class^='priceContainer'] h4"));
			String text = priceElement.getText();
			String productEntityId = param.get("productEntityId");
			if(!Utils.isEmpty(text) && text.contains("$") && StringUtil.isNotEmpty(productEntityId)){
				text = text.replace("Now", "").trim();
				logger.debug("--->找到商品单价 = "+text.substring(1));
//				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, text.substring(1));
				priceMap.put(productEntityId, text.substring(1));
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			WebElement cart = driver.findElement(By.cssSelector("button[data-xfe-testid='add-to-cart-button']"));
			driver.executeScript("var tar=arguments[0];tar.click();", cart);
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		WebDriverWait wait1 = new WebDriverWait(driver, 30);
		try {
			wait1.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector(".text_296oy strong")));
			logger.debug("刷新页面1");
			driver.navigate().refresh();
		} catch (Exception e) {
			logger.debug("刷新页面");
			driver.navigate().refresh();
		}
		int attnum = 0;
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("div[class^='productDetails']")));
			WebElement productDetails = driver.findElement(By.cssSelector("div[class^='productDetails']"));
			List<WebElement>text_296oys = productDetails.findElements(By.cssSelector("div[class^='dimensionWrapper']"));
			for(WebElement text_296oy:text_296oys){
				WebElement	w = text_296oy.findElement(By.cssSelector("strong"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						if(w.getText().trim().toUpperCase().equals(skuList.get(i).toUpperCase())){
							logger.debug("--->属性"+w.getText().trim()+"对的");
							attnum++;
						}
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		if(attnum < skuList.size()/2){
			logger.debug("--->缺少匹配的sku findCount = "+attnum+" && skuList.size()/2 = "+skuList.size()/2);
			return AutoBuyStatus.AUTO_SKU_NOT_FIND;
		}
		
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-xfe-testid='cart-active-products']")));
		} catch (Exception e) {
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				WebElement updown = driver.findElement(By.cssSelector("div[data-xfe-testid='cart-active-products'] div[class^=selectedQuantity]"));
				updown.click();
				List<WebElement> upates = driver.findElements(By.cssSelector("div[data-xfe-testid='cart-active-products'] strong[class^='labelQuantity']"));
				Utils.sleep(2000);
				for(WebElement upate:upates){
					logger.debug("--->upate.getText().trim() = "+upate.getText().trim());
					if(upate.getText().trim().equals(productNum)){
						upate.click();
						break;
					}
				}
				Utils.sleep(4000);
				logger.debug("--->选择商品数量完成");
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		WebElement productNumElement = driver.findElement(By.cssSelector("span[class^=selectedQuantity]"));
		logger.debug("--->选择商品数量后为"+productNumElement.getText().trim());
		if(!productNumElement.getText().trim().equals(productNum)){
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		// TODO Auto-generated method stub
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		//优惠码
		try {
			doScreenShot();
		} catch (Exception e) {
		}
		//设置价格
		logger.error("--->myPrice = "+myPrice);
		
		String countTemp = (String)param.get("count");
		int count = 0;
		if(!Utils.isEmpty(countTemp)){
			count = Integer.parseInt(countTemp);
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("a[data-xfe-testid='cart-summary-checkout-button']")));
			Utils.sleep(1500);
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		//优惠码
		if(promotionList != null && promotionList.size() > 0){
			driver.executeScript("(function(){window.scrollBy(0,200);})();");
			Utils.sleep(2500);
			boolean mark = true;
			try {
				driver.findElement(By.cssSelector("div[class^='promoTitle']"));
				logger.error("页面上已经有优惠码了");
				mark =false;
			} catch (Exception e) {
				logger.error("页面上没有优惠码了");
			}
			if(mark){
				try {
					WebElement addlink = driver.findElement(By.cssSelector("span[class^='text_19ar56z-o_O-addPromoText']"));
					driver.executeScript("var tar=arguments[0];tar.click();", addlink);
				} catch (Exception e) {
				}
				
				
				boolean isEffective = false;
				HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
				for(String code : promotionList){
					if(StringUtil.isNotEmpty(code)){
						code = code.trim();
						try{
							WebElement codeInput = driver.findElement(By.cssSelector("input[class^='promoCodeInput']"));
							codeInput.clear();
							Utils.sleep(2500);
							codeInput.sendKeys(code);
							Utils.sleep(1500);
							driver.findElement(By.cssSelector("button[class^='applyPromoButton']")).click();
							Utils.sleep(5500);
							
							try{
								driver.findElement(By.cssSelector("span[class*='errorMessage']"));//礼品卡
								statusMap.put(code, 0);
							}catch(Exception e){
								logger.error("promotionCode:"+code,e);
								statusMap.put(code, 10);
								isEffective = true;
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
		}
		
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("a[data-xfe-testid='cart-summary-checkout-button']")));
			Utils.sleep(1500);
			logger.debug("--->购物车页面加载完成");
			checkout.click();
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		logger.debug("--->等待支付页面加载");
		//等待支付页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("div[data-xfe-testid='place-order-button']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		try {
			WebElement change = driver.findElement(By.cssSelector(".userInfoBox_1915hxd a"));
			change.click();
			Utils.sleep(1500);
			List<WebElement> address =  driver.findElements(By.cssSelector("li[class^='li_yud3bt']"));
			address.get(count%address.size()).click();
			Utils.sleep(500);
			WebElement saveAddress = driver.findElement(By.cssSelector(".button_ghuiww"));
			saveAddress.click();
			Utils.sleep(500);
		} catch (Exception e) {
			logger.debug("--->修改地址异常",e);
		}
		
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.cssSelector("h5[class *= 'totalPrice']"));
			String text = totalPriceElement.getText();
			String priceStr = text.substring(1);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
			logger.debug("--->找到商品结算总价 = "+priceStr);
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
				if (v.doubleValue() > 5.00D){
					logger.error("--->总价差距超过约定,不能下单");
					throw new Exception();
				}
				if (v.doubleValue() < -5.00D){
					logger.error("--->漏下单");
					throw new Exception();
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			WebElement placeOrderElement = driver.findElement(By.cssSelector("div[data-xfe-testid='place-order-button'] span"));
			driver.executeScript("var tar=arguments[0];tar.click();", placeOrderElement);
			//placeOrderElement.click();;
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		WebDriverWait wait0 = new WebDriverWait(driver, 20);
		try {
			By byby = By.cssSelector(".text_17p9f19");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(byby));
		} catch (Exception e) {
			try {
				WebElement placeOrderElement = driver.findElement(By.cssSelector("div[data-xfe-testid='place-order-button'] span"));
				placeOrderElement.click();
				logger.debug("--->点击付款完成 placeOrder finish1");
			} catch (Exception e2) {
				logger.debug("--->点击付款完成 placeOrder finish11");
			}
			
		}
		try {
			By byby = By.cssSelector(".text_17p9f19");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(byby));
		} catch (Exception e) {
			try {
				WebElement placeOrderElement = driver.findElement(By.cssSelector("div[data-xfe-testid='place-order-button']"));
				placeOrderElement.click();;
				logger.debug("--->点击付款完成 placeOrder finish2");
			} catch (Exception e2) {
				logger.debug("--->点击付款完成 placeOrder finish22");
			}
			
		}
		try {
			By byby = By.cssSelector(".text_17p9f19");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(byby));
		} catch (Exception e) {
			try {
				WebElement placeOrderElement = driver.findElement(By.cssSelector("div[data-xfe-testid='place-order-button']"));
				driver.executeScript("var tar=arguments[0];tar.click();", placeOrderElement);
				logger.debug("--->点击付款完成 placeOrder finish3");
			} catch (Exception e2) {
				logger.debug("--->点击付款完成 placeOrder finish33");
			}
			
		}
	
		//查询商城订单号
		try{
			logger.debug("--->开始查找商品订单号");
			By byby = By.cssSelector("button[class^='goShoppingButton']");
			WebElement wi = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			wi.click();
			logger.debug("--->开始跳转到订单页面");
			driver.navigate().to("https://www.shopspring.com/me/orders");
			
			logger.debug("--->开始等待order页面加载完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orderCard_z5kzmw")));
			Utils.sleep(1500);
			logger.debug("--->order页面加载完成");
			List<WebElement> orderElements = driver.findElements(By.cssSelector(".orderCard_z5kzmw .orderHeader_1yzb2uh .text_10l4bzs-o_O-headerContent_pd4weg"));
			for(WebElement w:orderElements){
				if(w.getText().startsWith("C")){
					logger.debug("--->找到商品订单号 = "+w.getText());
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, w.getText());
					savePng();
					return AutoBuyStatus.AUTO_PAY_SUCCESS;
				}
			}
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			logger.debug("--->mallOrderNo没有");
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		try{
			logger.debug("--->开始跳转到订单页面");
			driver.navigate().to("https://www.shopspring.com/me/orders");
		}
		catch (Exception e){
			logger.error("--->跳转到订单页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待order页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orderCard_z5kzmw")));
			Utils.sleep(1500);
			logger.debug("--->order页面加载完成");
		}
		catch (Exception e){
			logger.error("--->加载order页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询所有可见的订单
		boolean isFind = false; 
		for(int i = 0;i<7;i++){
			List<WebElement> list = driver.findElements(By.cssSelector(".orderCard_z5kzmw"));
			for(WebElement panel : list){
				WebElement w = panel.findElement(By.cssSelector(".orderHeader_1yzb2uh"));
				if(w.getText().contains(mallOrderNo.trim())){
					logger.error("--->找到商城单号"+mallOrderNo);
					isFind = true;
					List<WebElement> orderBodys = panel.findElements(By.cssSelector(".orderBody_maj7wc"));
					for(WebElement worderBody:orderBodys){
						WebElement aWeb = worderBody.findElement(By.cssSelector("a"));
						String link = aWeb.getAttribute("href");
						logger.error("--->商品链接:"+link);
						if(detail.getProductUrl().contains(link)){
							//判断订单是否取消
							String s = worderBody.findElement(By.cssSelector("div[data-xfe-testid='order-shipping-progress'] .text_296oy-o_O-headerLabel_1lelydb")).getText();
							logger.error("--->OrderStatusCode="+s);
							if(!StringUtil.isBlank(s) && s.contains("Canceled")){
								logger.error("--->商城订单:"+mallOrderNo+"已取消");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							}else if(!StringUtil.isBlank(s) && s.contains("Placed")){
								logger.error("--->商城订单:"+mallOrderNo+"还没有发货");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
							}else if(!StringUtil.isBlank(s) && (s.contains("Shipped") || s.contains("Delivered"))){
								WebElement expressElement = worderBody.findElement(By.cssSelector(".orderCard_z5kzmw .shippingLink_1y5ph1v"));
								String expressNo = expressElement.getText();
								String expressCompany = "";
								if(expressNo.startsWith("1Z")){
									expressCompany = "UPS";
								}else if(expressNo.startsWith("92")){
									expressCompany = "FedEx-国际";
								}else if(expressNo.startsWith("40")){
									expressCompany = "FedEx";
								}
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								logger.error("expressCompany = " + expressCompany);
								logger.error("expressNo = " + expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}else{
								logger.error("--->商城订单:"+mallOrderNo+"未知状态");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
							}
						}
					}
				}
			}
			if(!isFind){
				driver.executeScript("(function(){window.scrollBy(0,1000);})();");
				Utils.sleep(5500);
				logger.error("--->第"+i+"次加载");
			}
		}
		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	private List<ExpressNode> getNodeList(String trackNo, String attribute,
			String orderNo, Set<Cookie> cookies) {
		List<ExpressNode> list = new ArrayList<ExpressNode>();
		Map<String, String> cookieMap = new HashMap<String, String>();
		for (Cookie cookie : cookies) {
			cookieMap.put(cookie.getName(), cookie.getValue());
		}
		try {
			Document document = Jsoup.connect(attribute).cookies(cookieMap).timeout(20000).get();
			Elements elements =  document.select("div.trackingDetails table tbody tr");
			for (Element element : elements) {
				ExpressNode expressNode = new ExpressNode();
				String date = element.select("td.date").get(0).text();
				String location = element.select("td.location").get(0).text();
				String description = element.select("td.description").get(0).text();
				expressNode.setExpressNo(trackNo);
				expressNode.setOrderNo(orderNo);
				expressNode.setName(location+"  "+description);
				try {
					expressNode.setOccurTime(dateFormat(date));
				} catch (Exception e) {
					logger.debug("日期转换异常："+date);
				}
				if(description.contains("Delivered")){
					expressNode.setStatus(14);
				}else{
					expressNode.setStatus(3);
				}
				list.add(expressNode);
			}
		} catch (IOException e) {
			logger.debug("爬取物流节点失败",e);
		}
		return list;
	}
	private Date dateFormat(String date) throws Exception {
		Date d = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MM dd,yyyy HH:mm:ss");
		date = date.replace("Jan", "1").replace("Feb", "2").replace("Mar", "3")
				.replace("Apr", "4").replace("May", "5").replace("Jun", "6")
				.replace("July", "7").replace("Aug", "8").replace("Sept", "9")
				.replace("Oct", "10").replace("Nov", "11").replace("Dec", "12");
		if (date.contains("AM")) {
			date = date.replace("AM", "");
			d = format.parse(date);
		} else if (date.contains("PM")) {
			date = date.replace("PM", "");
//			d = DateUtils.addHours(format.parse(date), 12);
			
			Date dd = format.parse(date);
	        Calendar c = Calendar.getInstance();
	        c.setTime(dd);
	        c.add(Calendar.HOUR_OF_DAY, 12);
	        d = c.getTime();
		}
		return d;
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		try{
			Utils.sleep(2000);
			driver.get("https://www.shopspring.com/");
		}
		catch (Exception e){
			logger.error("--->跳转shopspring主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}
}
