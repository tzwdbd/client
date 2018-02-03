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
public class SwarovskiAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	public SwarovskiAutoBuy() {
		super(false);
	}
	

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("https://www.swarovski.com/Web_US/en/login");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try {
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='LoginForm_Login']")));
		} catch (Exception e) {
			logger.error("--->点击去登录页面异常",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		try{
			WebElement w = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".simplemodal-close")));
			w.click();
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
		}
		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='LoginForm_Login']")));
			WebElement username = driver.findElement(By.cssSelector("input[name='LoginForm_Login']"));
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
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='LoginForm_Password']")));
			WebElement password = driver.findElement(By.cssSelector("input[name='LoginForm_Password']"));
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
			logger.debug("--->开始登陆");
			WebElement btn = driver.findElement(By.cssSelector("input[value='Login']"));
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
			WebElement ww = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".account")));
			if(ww.getText().contains("LOGIN")){
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#linkMiniBasketViewBasket")));
			WebElement viewBasket = driver.findElement(By.cssSelector("#linkMiniBasketViewBasket"));
			viewBasket.click();
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		logger.debug("--->清空购物车");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shoppingbag")));
			
			Utils.sleep(800);
			logger.error("--->购物车页面加载完成");
			//清理
			List<WebElement> list = driver.findElements(By.cssSelector(".sub-actions"));
			logger.error("--->开始清理购物车"+list.size());
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					WebElement w = list.get(0);
					List<WebElement> as = w.findElements(By.cssSelector("a"));
					
					driver.executeScript("var tar=arguments[0];tar.click();", as.get(as.size()-1));
					TimeUnit.SECONDS.sleep(1);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shoppingbag")));
					if(size>1){
						list = driver.findElements(By.cssSelector(".sub-actions"));
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
			WebElement bag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
			if(!bag.getText().equals("Your Shopping Bag is empty.")){
				logger.debug("--->购物车不为空！"+bag.getText());
				List<WebElement> list = driver.findElements(By.cssSelector(".sub-actions"));
				if(list.size()>0){
					return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
				}
			}
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#productdetailpage")));
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		try{
			WebElement w = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".simplemodal-close")));
			w.click();
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
		}
		
		
		List<String> skuList = null;
		try {	
			TimeUnit.SECONDS.sleep(3);
			logger.debug("--->商品页面加载完成");
			
			String sku = param.get("sku");
			
			// 开始选择sku
			logger.debug("--->开始选择sku");
			if (StringUtil.isNotEmpty(sku)) {
				skuList = Utils.getSku(sku);
				WebElement skuChooseElement = driver.findElement(By.cssSelector("a.handle"));
				skuChooseElement.click();
				TimeUnit.SECONDS.sleep(1);
				WebElement ops = driver.findElement(By.cssSelector("#variation-mirror-pd"));
				List<WebElement> lis =ops.findElements(By.cssSelector("li a"));
				for(WebElement w:lis){
					if(w.getText().equals(skuList.get(1))){
						w.click();
						break;
					}
				}
				skuChooseElement= driver.findElement(By.cssSelector("a.handle"));
				if(!skuChooseElement.getText().equals(skuList.get(1))){
					logger.error("--->选择size失败");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}
		}catch (Exception e) {
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		
		logger.debug("--->选择sku完成");
		
		// 寻找商品单价
		try {
			logger.debug("--->[1]开始寻找商品单价");
			String productEntityId = param.get("productEntityId");
			WebElement priceFilter = driver.findElement(By.cssSelector(".shopping-box .price .new"));
			String text = priceFilter.getText().trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1).trim();
				logger.debug("--->[1-1]找到商品单价  = " + priceStr);
				priceMap.put(productEntityId, priceStr);
			}
		} catch (Exception e) {
			logger.error("--->获取单价失败");
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			
			try {
				TimeUnit.SECONDS.sleep(1);
				WebElement sb= driver.findElement(By.cssSelector(".btn-plus"));
				for(int i=0;i<Integer.parseInt(productNum)-1;i++){
					sb.click();
					TimeUnit.SECONDS.sleep(1);
				}
			} catch (Exception e) {
				logger.error("--->选择数量失败1",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
			
			
		}
		// 加购物车
		logger.debug("--->开始加购物车");
		try{
			TimeUnit.SECONDS.sleep(3);
			WebElement addCard =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#warenkorb_button")));
			driver.executeScript("var tar=arguments[0];tar.click();", addCard);
			WebElement viewbucker = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#btnMiniBasketViewBasket")));
			List<WebElement> quants = driver.findElements(By.cssSelector(".col-quant"));
			if(!quants.get(quants.size()-1).getText().equals(productNum)){
				logger.error("--->选择数量失败");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
			viewbucker.click();
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
		
		WebDriverWait wait = new WebDriverWait(driver, 40);
		String suffix = (String) param.get("suffixNo");
		try
		{
			logger.error("--->购物车页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shoppingbag")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
		}catch (Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		
		String size = param.get("size");
		try {
			List<WebElement> goodsInCart = driver.findElements(By.cssSelector(".sub-actions"));
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
			driver.executeScript("(function(){window.scrollBy(0,250);})();");
			Utils.sleep(2000);
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						WebElement codeInput = driver.findElement(By.id("vouchercode"));
						codeInput.clear();
						Utils.sleep(800);
						codeInput.sendKeys(code);
						Utils.sleep(900);
						
						driver.findElement(By.cssSelector("#submitcode")).click();
						Utils.sleep(5500);
						
						try{
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#voucher_form_error")));
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
			WebElement checkout = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#securecheckout")));
			driver.executeScript("var tar=arguments[0];tar.click();", checkout);
			
		}catch (Exception e){
			logger.error("--->点击checkout失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try {
			Utils.sleep(5800);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#anker-terms_conditions")));
			try {
				WebElement condition = driver.findElement(By.cssSelector("#anker-terms_conditions #terms_conditions"));
				driver.executeScript("var tar=arguments[0];tar.click();", condition);
			} catch (Exception e) {
				logger.error("--->点击terms_conditions失败");
				WebElement condition = driver.findElement(By.cssSelector("#anker-terms_conditions div div"));
				driver.executeScript("var tar=arguments[0];tar.click();", condition);
			}
			
			Utils.sleep(1800);
			WebElement billing = driver.findElement(By.cssSelector("#btnContinueToPayment"));
			driver.executeScript("var tar=arguments[0];tar.click();", billing);
		} catch (Exception e) {
			logger.error("--->点击billing失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		String shipPriceStr = "0";
		// 查询总价
		BigDecimal total = new BigDecimal(0.00);
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".totalamount .value")));
			String text = totalPriceElement.getText().replace(",", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1).trim();
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
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#PaymentFrameContent")));
			driver.switchTo().frame("PaymentFrameContent");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.handle")));
			WebElement skuChooseElement = driver.findElement(By.cssSelector("a.handle"));
			driver.executeScript("var tar=arguments[0];tar.click();", skuChooseElement);
			TimeUnit.SECONDS.sleep(1);
			WebElement ops = driver.findElement(By.cssSelector("#credit_card-mirror-pd"));
			List<WebElement> lis =ops.findElements(By.cssSelector("li a"));
			for(WebElement w:lis){
				if(w.getText().trim().equals("VISA")){
					driver.executeScript("var tar=arguments[0];tar.click();", w);
					break;
				}
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#cardholder")));
			WebElement cardname = driver.findElement(By.cssSelector("#cardholder"));
			cardname.sendKeys(param.get("owner"));
			TimeUnit.SECONDS.sleep(1);
			driver.executeScript("(function(){window.scrollBy(1,250);})();");
			TimeUnit.SECONDS.sleep(1);
			WebElement frameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#globalcollect iframe")));
			driver.switchTo().frame(frameElement);
			WebElement cardno = driver.findElement(By.cssSelector("input[name='CREDITCARDNUMBER']"));
			cardno.sendKeys(param.get("cardNo"));
			TimeUnit.SECONDS.sleep(1);
			String expiry = param.get("expiryDate");
			String[] ss = expiry.split(" ");
			Utils.sleep(2000);
			WebElement mm = driver.findElement(By.cssSelector("select[name='EXPIRYDATE_MM']"));
			Select smm = new Select(mm);
			smm.selectByVisibleText(ss[1]);
			TimeUnit.SECONDS.sleep(1);
			WebElement yy = driver.findElement(By.cssSelector("select[name='EXPIRYDATE_YY']"));
			Select syy = new Select(yy);
			syy.selectByVisibleText(ss[0].substring(2));
			TimeUnit.SECONDS.sleep(1);
			WebElement cv = driver.findElement(By.cssSelector("input[name='CVV']"));
			cv.sendKeys(suffix);
			TimeUnit.SECONDS.sleep(1);
			
		} catch (Exception e) {
			logger.error("--->点击VISA失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		
		// placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		try {
			WebElement placeOrderButton = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#btnSubmit")));
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
			WebElement order = null;
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orderdetails li")));
				order = driver.findElement(By.cssSelector(".orderdetails li"));
			} catch (Exception e) {
				order = driver.findElement(By.cssSelector(".sub-leftcol p"));
			}
			
			logger.debug("--->订单页面加载完成");
			
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
			logger.debug("--->查找商品订单号出现异常",e);
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
			driver.get("http://estore-us.pandora.net/");
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
		SwarovskiAutoBuy auto = new SwarovskiAutoBuy();
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
		param1.put("url", "http://www.rebatesme.com/zh/click/?key=1532287760d2426336ffd915d0a0b65b&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=bac6e3c9cc3a8e538564075a95b2ae05&targetUrl=https%3A%2F%2Fwww.swarovski.com%2FWeb_US%2Fen%2F5391767%2Fproduct%2FLove_Ring%2C_White%2C_Rhodium_plating.html");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param1.put("orginalUrl", "https://www.swarovski.com/Web_US/en/5391767/product/Love_Ring,_White,_Rhodium_plating.html");
		param1.put("sku", "[[\"size\",\"4 3/4-5 / XS\"]]");
		param1.put("num", "4");
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
			String orderUrl ="https://www.swarovski.com/Web_US/en/orderhistory";
			driver.navigate().to(orderUrl);
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#orderoverview")));
			List<WebElement> orders = driver.findElements(By.cssSelector("#orderoverview tr"));
			int find = 0;
			for(WebElement o:orders){
				WebElement w = null;
				try {
					w = o.findElement(By.cssSelector("a"));
				} catch (Exception e) {
					continue;
				}
				
				String str = w.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					WebElement orderStatus = o.findElement(By.cssSelector("td strong"));
					find = 1;
					logger.error("text:"+orderStatus.getText());
					if(orderStatus.getText().contains("Canceled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(orderStatus.getText().contains("Not shipped yet")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Sent")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector("td p"));
						logger.error("orderNoElement: "+orderNoElement.getText());
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
						String expressNo = ExpressUtils.regularExperssNo(orderNoElement.getText());
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
 