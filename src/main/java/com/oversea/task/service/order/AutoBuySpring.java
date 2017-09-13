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
//		AutoBuyStatus status = auto.login("tzwdbd@126.com", "Aa123456");
//		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.cleanCart();
//			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				param.put("url", "https://www.shopspring.com/products/53973851?refineFlags=on_sale");
//				param.put("url", "http://www.6pm.com/ugg-sea-glisten-anchor-red-suede");
//				param.put("url", "http://www.6pm.com/gabriella-rocha-alena-evening-purse-with-tassel-black");
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				param.put("sku", "[[\"color\",\"Ripped Medium Wash\"],[\"Fit\",\"Long\"],[\"Waist\",\"28\"]]");
				param.put("productEntityId", "1112");
				param.put("num", "2");
				auto.selectProduct(param);
				//if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "39.99");
//					param0.put("count", "1");
//					param0.put("isPay", String.valueOf(false));
//					param0.put("cardNo", "4662 4833 6029 1396");
//					status = auto.pay(param0);
//				//}
//			}
//		}
		//auto.logout();
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
		driver.manage().window().maximize();
		
		

		//点击登录
		try
		{
			driver.get("https://www.shopspring.com/signin");
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
			List<WebElement> list = driver.findElements(By.cssSelector("button[class^='remove']"));
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					list.get(0).click();
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.cssSelector("button[class^='remove']"));
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
			WebElement cartBag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-xfe-testid='active-cart-count']")));
			logger.debug("--->购物车内容:"+cartBag.getText());
			if(!StringUtil.isBlank(cartBag.getText())){
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}
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
								if(element.isDisplayed() && element.getText().startsWith(skuList.get(i-1))){
									WebElement updown = element.findElement(By.cssSelector("span"));
									updown.click();
									Utils.sleep(500);
									List<WebElement> labels = element.findElements(By.cssSelector(".options_13pflw div[data-xfe-testid='variant_selector_dropdown_option']"));
									for(WebElement label:labels){
										if(label.isDisplayed()){
											String test = label.getText().replace("Unavailable with current selections", "").trim();
											if(test.toLowerCase().contains(skuList.get(i).toLowerCase())){
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".priceWrapper_1jq5abk h4")));
			WebElement priceElement = driver.findElement(By.cssSelector(".priceWrapper_1jq5abk h4"));
			String text = priceElement.getText();
			String productEntityId = param.get("productEntityId");
			if(!Utils.isEmpty(text) && text.startsWith("$") && StringUtil.isNotEmpty(productEntityId)){
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
			cart.click();
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("a.button_16hyqux")));
			
			List<WebElement> attrs = driver.findElements(By.cssSelector(".text_296oy strong"));
			int attnum = 0;
			for(WebElement w:attrs){
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						if(w.getText().trim().equals(skuList.get(i))){
							logger.debug("--->属性"+w.getText().trim()+"对的");
							attnum++;
						}
					}
					
					
				}
			}
			if(attnum < skuList.size()/2){
				logger.debug("--->缺少匹配的sku findCount = "+attnum+" && skuList.size()/2 = "+skuList.size()/2);
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
			WebElement viewcart = driver.findElement(By.cssSelector("a.button_16hyqux"));
			viewcart.click();
			logger.debug("--->去购物车页面");
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
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
				WebElement updown = driver.findElement(By.cssSelector("div[data-xfe-testid='cart-active-products'] .priceAndQuantity_12blkl .selectedQuantity_e296pg"));
				updown.click();
				List<WebElement> upates = driver.findElements(By.cssSelector("div[data-xfe-testid='cart-active-products'] .priceAndQuantity_12blkl .label_1vrcax8 strong"));
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
		WebElement productNumElement = driver.findElement(By.cssSelector(".selectedQuantityText_n5t54s"));
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
	
		//设置价格
		logger.error("--->myPrice = "+myPrice);
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
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
					By.cssSelector(".text_17167xw")));
			Utils.sleep(1500);
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		//优惠码
		if(promotionList != null && promotionList.size() > 0){
			WebElement addlink = driver.findElement(By.cssSelector(".text_19ar56z-o_O-addPromoText_er4n8x"));
			addlink.click();
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						WebElement codeInput = driver.findElement(By.cssSelector(".promoCodesInput_odvh1a"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.cssSelector(".applyButton_tyi7sb")).click();
						Utils.sleep(5500);
						
						try{
							driver.findElement(By.cssSelector(".errorMsg_vromzt"));//礼品卡
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
		
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector(".text_17167xw")));
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
					By.cssSelector("button[data-xfe-testid='place-order-button']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		try {
			WebElement change = driver.findElement(By.cssSelector(".userInfoBox_1x3q7yf a"));
			change.click();
			Utils.sleep(1500);
			List<WebElement> address =  driver.findElements(By.cssSelector(".li_yud3bt"));
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
			WebElement totalPriceElement = driver.findElement(By.cssSelector(".lineItem_y5xc2p-o_O-total_a3qe0k .value_1olrz58"));
			String text = totalPriceElement.getText();
			String priceStr = text.substring(0,text.length()-1);
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
			WebElement placeOrderElement = driver.findElement(By.cssSelector("button[data-xfe-testid='place-order-button']"));
			Utils.sleep(1500);
			placeOrderElement.click();
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//查询商城订单号
		try{
			logger.debug("--->开始查找商品订单号");
			By byby = By.xpath("//span[@class='thank-you-order-id a-text-bold']");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderElement.getText());
			savePng();
			return AutoBuyStatus.AUTO_PAY_SUCCESS;
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		
		Long productEntityId = detail.getProductEntityId();
		logger.debug("productEntityId:"+productEntityId);
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		
		Map<String, String> orderMap = new HashMap<String, String>();
		List<String> orderSkuSth =  Utils.getSku(detail.getProductSku());
		for (int i = 0; i < orderSkuSth.size(); i++) {
			orderMap.put(orderSkuSth.get(i), orderSkuSth.get(++i));
		}
		orderMap.put("price",  detail.getSinglePrice());
		
		try {
			driver.get("https://secure-www.6pm.com/orders/"+mallOrderNo);
			//商品list
			List<WebElement> list = driver.findElements(By.xpath("//div[@id='orderDetail']/table/tbody/tr"));
			logger.debug(mallOrderNo+"有"+list.size()+"个商品");
			
			if(list.size()==1){
				WebElement product = list.get(0);
				String status = product.findElement(By.xpath(".//td[@class='shipping']/h5")).getText().trim();
				if(status.toLowerCase().contains("cancelled")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}else if(status.equalsIgnoreCase("customer action required")){
					return AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH;
				}else if(status.equalsIgnoreCase("shipped")){
					String trackNo = product.findElement(By.xpath(".//p[@class='tracking']")).getText().replace("Track This Shipment", "").replaceAll("[^0-9A-Za-z]", "");
					
					String expressCompany = "";
					if(trackNo.startsWith("1Z")){
						expressCompany = "UPS";
					}else if(trackNo.startsWith("TBA")){
						expressCompany = "AMZL US";
					}else if(trackNo.length()==22){
						expressCompany = "USPS";
					}
					logger.debug("--->获取物流单号成功："+expressCompany+" "+trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
					
					List<ExpressNode> nodeList = getNodeList(trackNo,product.findElement(By.xpath(".//p[@class='tracking']/a")).getAttribute("href"),mallOrderNo,driver.manage().getCookies());
					logger.debug("nodeList:"+nodeList.size());
					if(nodeList.size() > 0 && getTask() != null){
						logger.error("addParam expressNodeList");
						logger.debug(nodeList.toString());
						getTask().addParam("expressNodeList", nodeList);
					}
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}else if(status.equalsIgnoreCase("PROCESSING")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}else{
					logger.debug("未知的订单状态:"+status);
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}
			
			outer:
			for (int i = 0; i < list.size(); i++) {
				logger.debug("第"+(i+1)+"个商品");
				WebElement product = list.get(i);
				//ASIN COLOR SIZE NULL
				List<WebElement> liList =  product.findElements(By.xpath(".//ul[@class='details']/li"));
				//网页中的sku信息
				Map<String, String> trueSku = new HashMap<String, String>();
				for (WebElement webElement : liList) {
					String skuSth = webElement.getText();
					if(StringUtil.isNotEmpty(skuSth)){
						trueSku.put(skuSth.split(":")[0].trim().toLowerCase(), skuSth.split(":")[1].trim().toLowerCase());
					}
				}
				WebElement priceEle = product.findElement(By.xpath(".//td[@class='amt']"));
				trueSku.put("price", priceEle.getText().replaceAll("[^0-9.]", ""));
				
				logger.debug("orderMap："+orderMap.toString());
				logger.debug("trueSku:"+trueSku.toString());
				for (Entry<String, String> colie : orderMap.entrySet()) {
					String key = colie.getKey();
					String value = colie.getValue();
					if("width".equals(key)){
						continue;
					}
					if("one size".equalsIgnoreCase(value)){
						continue;
					}
					if(!StringUtil.isBlank(trueSku.get(key.toLowerCase())) && !trueSku.get(key.toLowerCase()).equalsIgnoreCase(value)){
						continue outer;
					}
				}
				
				String status = product.findElement(By.xpath(".//td[@class='shipping']/h5")).getText().trim();
				if(status.equalsIgnoreCase("cancelled")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}else if(status.equalsIgnoreCase("customer action required")){
					return AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH;
				}else if(status.equalsIgnoreCase("shipped")){
					String trackNo = product.findElement(By.xpath(".//p[@class='tracking']")).getText().replace("Track This Shipment", "").replaceAll("[^0-9A-Za-z]", "");
					
					String expressCompany = "";
					if(trackNo.startsWith("1Z")){
						expressCompany = "UPS";
					}else if(trackNo.startsWith("TBA")){
						expressCompany = "AMZL US";
					}else if(trackNo.length()==22){
						expressCompany = "USPS";
					}
					logger.debug("--->获取物流单号成功："+expressCompany+" "+trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
					if(!expressCompany.equals("UPS")){
						List<ExpressNode> nodeList = getNodeList(trackNo,product.findElement(By.xpath(".//p[@class='tracking']/a")).getAttribute("href"),detail.getOrderNo(),driver.manage().getCookies());
						logger.debug("nodeList:"+nodeList.size());
						if(nodeList.size() > 0 && getTask() != null){
							logger.error("addParam expressNodeList");
							logger.debug(nodeList.toString());
							getTask().addParam("expressNodeList", nodeList);
						}
					}
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}else if(status.equalsIgnoreCase("PROCESSING")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}else if(status.equalsIgnoreCase("SUBMITTED")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}else{
					logger.debug("未知的订单状态:"+status);
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("爬取物流单号失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
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
			driver.get("http://www.6pm.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'Logout')]")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Account']")));
		}
		catch (Exception e){
			logger.error("--->跳转6pm主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}
}
