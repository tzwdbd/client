package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.oversea.task.utils.DateUtils;
import com.oversea.task.utils.Utils;

/**
 * @author xiong chen
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.service.order
 * @Description:
 * @date 2016年8月5日
 */

public class ZapposAutoBuy extends AutoBuy{
	
	private final Logger logger = Logger.getLogger(getClass());
	
	public ZapposAutoBuy(){
		super(false);
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("http://www.zappos.com/");
		
		//点击登录
		try {
			Utils.sleep(1500);
			By bySignIn = By.id("accountHeaderSignIn");
			WebElement signIn = driver.findElement(bySignIn);
			signIn.click();
			logger.debug("--->跳转到登录页面");
		} 
		catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		// 等到[输入框]出现
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ap_email")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.id("ap_password"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.id("signInSubmit-input"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submit.click();
		} 
		catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		//等待登录完成
		try {
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountHeaderLoggedIn")));
			logger.debug("--->登录完成");
		} 
		catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		//跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.xpath("//a[@id='viewCart']"));
			logger.error("--->开始跳转到购物车");
			Utils.sleep(1500);
			viewCart.click();
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}	
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='content' and @class='cart']")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='remove gae-click*Cart*Cart-Update*Remove-Item']"));
			if(list != null && list.size() > 0){
				for(WebElement remove : list){
					Utils.sleep(2000);
					if(remove != null){
						remove.click();
					}
				}
			}
			Utils.sleep(2000);
			logger.error("--->购物车页面清理完成");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try
		{	
			Utils.sleep(2000);
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@id='addToCart']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		
		//判断商品是否下架
		try{
			driver.findElement(By.xpath("//p[contains(text(),'currently unavailable')]"));
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		
		//开始选择sku
		logger.debug("--->开始选择sku");
		try{
			if (sku != null){
				List<String> skuList = Utils.getSku((String) sku);
				List<WebElement> elements = driver.findElements(By.xpath("//form[@id='prForm']/div[@id='purchaseOptions']/div[@class='dimension']"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							String text = element.getAttribute("id");
							if(!Utils.isEmpty(text)){
								text = text.toLowerCase();
								String key = skuList.get(i-1).toLowerCase();
								if(text.indexOf(key) != -1){
									WebElement ele = null;
									try{
										ele = element.findElement(By.xpath(".//select[@class='btn secondary']"));
									}catch(Exception e){
										ele = null;
									}
									if(ele != null){
										Select select = new Select(ele);
										try{
											select.selectByVisibleText(skuList.get(i));
										}catch(Exception e){
											logger.debug("--->找不到指定的sku = "+(skuList.get(i-1)+":"+skuList.get(i)));
											return AutoBuyStatus.AUTO_SKU_NOT_FIND;
										}
										Utils.sleep(2000);
										try{
											driver.findElement(By.xpath("//div[@id='oosPopover']/h1[contains(text(),'Out of Stock')]"));
											logger.debug("--->sku = "+(skuList.get(i-1)+":"+skuList.get(i))+"售罄");
											return AutoBuyStatus.AUTO_SKU_IS_OFFLINE; 
										}catch(Exception e){
											logger.debug("--->sku = "+(skuList.get(i-1)+":"+skuList.get(i))+"有库存");
										}	
									}
								}
								Utils.sleep(2000);
							}
						}
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->选择sku完成");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.xpath("//div[@id='priceSlot']/span[@class='price nowPrice']/span"));
			String text = priceElement.getText();
			if(!Utils.isEmpty(text) && text.startsWith("$")){
				logger.debug("--->找到商品单价 = "+text.substring(1));
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, text.substring(1));
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			WebElement cart = driver.findElement(By.xpath("//button[@id='addToCart']"));
			Utils.sleep(1500);
			cart.click();
			Utils.sleep(2000);
			try{
				driver.findElement(By.xpath("//div[@id='oosPopover']/h1[contains(text(),'Out of Stock')]"));
				logger.debug("--->商品 productUrl = "+productUrl+"已经售罄");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE; 
			}catch(Exception e){
				logger.debug("--->商品 productUrl = "+productUrl+"有库存");
			}
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@class='btn secondary back gae-click*Cart*Continue-Shopping*Bottom' and contains(text(),'Continue Shopping')]")));
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常"); 
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->购物车页面加载完成");
		
		//判断商品是否It is no longer available.
		try{
			driver.findElement(By.xpath("//div[@id='jsError']/h3[@class='errorMsg']"));
			logger.debug("--->找到商品已经不可用的标记");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->商品可用,继续");
		}
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				List<String> skuList = Utils.getSku((String) sku);
				
				List<WebElement> allProduct = driver.findElements(By.xpath("//tbody/tr"));
				
				for (WebElement productWebElement : allProduct) {
					String test = productWebElement.getText();
					for(int i = 1;i<skuList.size();i+=2){
						if(!test.contains(skuList.get(i))){
							break;
						}
					}
					
					logger.debug("--->商品数量 = "+productNum);
					WebElement single = productWebElement.findElement(By.xpath("//td[@class = 'each']"));
					String singlePrice = single.getText().replace("$", "");
					if(singlePrice.contains("ONLY")){
						Matcher m = Pattern.compile("\\$.*ONLY ", Pattern.MULTILINE | Pattern.DOTALL).matcher(single.getText());
						while (m.find()) {
							singlePrice = m.group().replace(" ", "").replace("$", "").replace("ONLY", "");
						}
					}
					
					WebElement inputNum = productWebElement.findElement(By.xpath("//input[@type='text' and @name='changeQuantity']"));
					Utils.sleep(2000);
					inputNum.clear();
					Utils.sleep(2000);
					inputNum.sendKeys(productNum);
					Utils.sleep(2000);
					WebElement upate = productWebElement.findElement(By.xpath("//button[@class='btn secondary gae-click*Cart*Cart-Update*Update-Qty' and contains(text(),'Update')]"));
					Utils.sleep(2000);
					upate.click();
					Utils.sleep(2000);	
					String totalPrice = "";
					Matcher m = Pattern.compile("ites[\\s\\S]*\\$.*", Pattern.MULTILINE | Pattern.DOTALL).matcher(productWebElement.getText());
					while (m.find()) {
						totalPrice = m.group().replace(" ", "").replace("$", "").replace("ites", "");
					}
					
					if(StringUtil.isNotEmpty(singlePrice) && StringUtil.isNotEmpty(totalPrice) 
							&& Double.parseDouble(totalPrice)/Double.parseDouble(singlePrice)==Double.parseDouble(productNum)){
							logger.debug("--->选择商品数量完成");
						break;
					}else{
						logger.debug("--->商品库存不足");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
				
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		/*//查询商品数量是否可买
		try{
			driver.findElement(By.xpath("//div[@id='jsError']/h3[contains(text(),'We're very sorry')]"));
			logger.debug("--->选择商品数量太多，不支付一次性购买这么多件商品");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}catch(Exception e){
			logger.debug("--->商品数量没有问题,carry on");
		}*/
		
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
		
		myPrice = String.valueOf(Float.parseFloat(myPrice));
		logger.error("--->myPrice = "+myPrice);
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@class='btn action addToCart gae-click*Cart*Proceed-to-Checkout*Bottom']")));
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
					By.xpath("//button[@id='submitOrderButton']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//物流是不是免费的
		try {
			driver.findElement(By.xpath("td//[contains(text(),'Free Shipping']"));
			logger.debug("--->物流不是免费的了");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		} 
		catch (Exception e) {
			logger.debug("--->物流免费,carry on");
		}
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.xpath("//tr[@id='subtotal']/td[@class='amt']"));
			String text = totalPriceElement.getText();
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = "+priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = x.subtract(y);
				double m = Math.abs(v.doubleValue());
				if (m > 20.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
		}
		
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			WebElement placeOrderElement = driver.findElement(By.xpath("//button[@id='submitOrderButton']"));
			Utils.sleep(1500);
			if(isPay){
				logger.debug("--->啊啊啊啊啊，我要付款了");
				placeOrderElement.click();
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		}
		catch(Exception e)
		{
			logger.error("--->付款出错啦!", e);
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
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.error("--->跳转到订单页面");
			driver.navigate().to("https://secure-www.zappos.com/account");
		}
		catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待my account页面加载完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orderHistory")));
			logger.debug("--->my account页面加载完成");
		}
		catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//寻找商城输入框
		try{
			logger.debug("--->开始等待寻找商城输入框");
		    WebElement orderInput = null;
		    try{
		    	orderInput = driver.findElement(By.id("orderId"));
		    }catch(Exception e){
		    	logger.debug("--->第一次寻找orderInput 失败");
		    }
		    if(orderInput == null){
		    	orderInput = driver.findElement(By.cssSelector("input[name='orderId']"));
		    }
		    		
			Utils.sleep(1500);
			orderInput.sendKeys(mallOrderNo);
			Utils.sleep(1500);
			logger.debug("--->商城订单号输入完成");
			
			//寻找Find Your Order按钮
			WebElement find = null;
			try{
				find = driver.findElement(By.cssSelector("#findOrderNumber button"));
			}catch(Exception e){
				logger.debug("--->第一次寻找Find Your Order 失败");
			}
			if(find == null){
				find = driver.findElement(By.cssSelector(".z-btn[value='Find This Order']"));
			}
			Utils.sleep(1500);
			find.click();
		}
		catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询是否被砍单或者没有生成物流单号
		try {
			WebDriverWait wait1 = new WebDriverWait(driver, 30);
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#orderHeader .orderStatus")));
			WebElement orderStatus = driver.findElement(By.cssSelector("#orderHeader .orderStatus"));
			logger.error("--->[1]样式1");
			if(orderStatus.getText().contains("cancelled") || orderStatus.getText().contains("Never Ordered") ){
				logger.error("--->订单被砍,请重新下单");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
			}
			if(orderStatus.getText().contains("We've Sent It")){
				logger.error("--->开始查找Track This Shipment");
				WebElement track = driver.findElement(By.cssSelector(".shippingInfo a"));
				String url = track.getAttribute("href");
				
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, track.findElement(By.cssSelector("i")).getText());
				logger.debug("--->物流号:"+track.findElement(By.cssSelector("i")).getText());
				if(url.contains(".ups.")){
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
					logger.debug("--->物流公司:UPS");
				}else{
					logger.debug("--->未知的物流公司");
				}
				return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;	
				
			}else {
				logger.error("该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
		
		} catch (Exception e) {
			logger.error("--->[2]样式2");
			try {
				WebElement orderStatus = driver.findElement(By.cssSelector("#orderDetail h5"));
				logger.error("--->[2]订单状态"+orderStatus.getText());
				if(orderStatus.getText().toLowerCase().contains("cancelled") || orderStatus.getText().contains("Never Ordered") ){
					logger.error("--->订单被砍,请重新下单");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}
				if(orderStatus.getText().toLowerCase().contains("shipped")){
					logger.error("--->开始查找Track This Shipment");
					WebElement track = driver.findElement(By.cssSelector("#orderDetail .tracking a"));
					track.click();
					Utils.sleep(1500);
					
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ccInfo")));
					List<WebElement> list = driver.findElements(By.cssSelector(".ccInfo p"));
					
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, list.get(1).getText().trim());
					logger.debug("--->物流号:"+list.get(1).getText().trim());
					logger.debug("--->物流公司:"+list.get(0).getText().trim());
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, list.get(0).getText().trim());
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;	
					
				}else {
					logger.error("该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			} catch (Exception e2) {
				logger.error("--->查询订单异常,找不到该订单");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			
		}	
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		try{
			Utils.sleep(2000);
			driver.get("http://www.zappos.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountHeaderLoggedIn")));
			return true;
		}
		catch (Exception e){
			logger.error("--->跳转zappos主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		ZapposAutoBuy autoBuy = new ZapposAutoBuy();
		AutoBuyStatus status = autoBuy.login("tangt12@outlook.com", "tfb001001");
		System.out.println(status);
//		if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = autoBuy.cleanCart();
//			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
//				Map<String, String> param = new HashMap<String, String>();
//				param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=46d6vnGHX0ZAfkP7T8Uym7SQOsdjVDSkl8r1mgW7vk_asTWyc_b_arSulxIB4Cz_aL4_c&new=http%3A%2F%2Fwww.zappos.com%2Fpuma-suede-classic-black-white%3Fef_id%3DVwxoggAAAeatEDRr%3A20160612095234%3As&tag=");
//				param.put("sku", "[[\"color\",\"Black/White\"],[\"size\",\"Men's 4, Women's 5.5\"],[\"width\",\"Medium\"]]");
//				param.put("num", "1");
//				param.put("my_price", "59.00");
//				param.put("isPay","false");
//				param.put("count", "2");
//				status = autoBuy.selectProduct(param);
//				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					autoBuy.pay(param);
//				}
//			}
//		}
		
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("111-1973212-4716230");
		System.out.println(autoBuy.scribeExpress(detail));
		
	}
	
	public static void main1(String[] args) {
		List<String> skuList = Utils.getSku("[[\"color\",\"White Canvas\"],[\"size\",\"6.5\"],[\"width\",\"B - Medium\"]]");
		System.out.println(skuList);
		
	}
	
	//判断日期距今是否超出两天
	public boolean isRemind(String orderDate){
		
		String[] date =  orderDate.split(" ");
		
		String month = "";
		
		switch (date[0]) {
			case "Jan" : month = "01" ; break ;
			case "Feb" : month = "02" ; break ;
			case "Mar" : month = "03" ; break ;
			case "Apr" : month = "04" ; break ;
			case "May" : month = "05" ; break ;
			case "Jun" : month = "06" ; break ;
			case "Jul" : month = "07" ; break ;
			case "Aug" : month = "08" ; break ;
			case "Sep" : month = "09" ; break ;
			case "Oct" : month = "10" ; break ;
			case "Nov" : month = "11" ; break ;
			case "Dec" : month = "12" ; break ;
		}
		
		String time = null;
		if(date[5].equals("PM")){
			time = (Integer.parseInt(date[4].split(":")[0])+12)+":"+date[4].split(":")[1] ;
		}else if(date[5].equals("AM")){
			time =date[4].split(":")[0]+":"+date[4].split(":")[1] ;
		}
		
		String day = null;
		if(Integer.parseInt(date[1].replace(",", ""))<10){
			day = "0"+date[1].replace(",", "");
		}else{
			day =date[1].replace(",", "");
		}
		
		String sj = date[2]+"-"+month+"-"+day+" "+time+":00";
		
//		System.out.println("已下单天数："+DateUtil.getDaysFromNow(DateUtil.ymdhmsString2DateTime(sj)));
		
		return DateUtils.getDaysFromNow(DateUtils.ymdhmsString2DateTime(sj))>2f;
	}
}
