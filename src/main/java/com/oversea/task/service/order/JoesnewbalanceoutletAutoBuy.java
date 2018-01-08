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
public class JoesnewbalanceoutletAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	public JoesnewbalanceoutletAutoBuy() {
		super(false);
	}
	

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("https://www.joesnewbalanceoutlet.com/");
		WebDriverWait wait = new WebDriverWait(driver, 40);
		
		try {
			TimeUnit.SECONDS.sleep(1);
			driver.navigate().to("https://www.joesnewbalanceoutlet.com/account/login");
		} catch (Exception e) {
			logger.error("--->点击去登录页面异常",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.id("UserName")));
			List<WebElement> usernames = driver.findElements(By.id("UserName"));
			for(WebElement w:usernames){
				if(w.isDisplayed()){
					w.sendKeys(userName);
					logger.debug("--->输入账号");
					break;
				}
			}
			TimeUnit.SECONDS.sleep(1);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			List<WebElement> passwords = driver.findElements(By.id("Password"));
			for(WebElement w:passwords){
				if(w.isDisplayed()){
					w.sendKeys(passWord);
					logger.debug("--->输入密码");
					break;
				}
			}
			TimeUnit.SECONDS.sleep(2);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			logger.debug("--->开始登陆");
			WebElement btn = driver.findElement(By.cssSelector("input[name='LoginBtn']"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hideOnMobileInline .fa-caret-right")));
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
			driver.navigate().to("https://www.joesnewbalanceoutlet.com/cart");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		logger.debug("--->清空购物车");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("CartMain")));
			
			Utils.sleep(800);
			logger.error("--->购物车页面加载完成");
			//清理
			List<WebElement> list = driver.findElements(By.cssSelector(".removeItem"));
			logger.error("--->开始清理购物车"+list.size());
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					list.get(0).click();
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("CartMain")));
					if(size>1){
						list = driver.findElements(By.cssSelector(".removeItem"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".emptyCart")));
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
//		try {
//			TimeUnit.SECONDS.sleep(3);
//			driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
//			driver.executeScript("(function(){var els = document.getElementsByClassName('acsCloseButton');if(els && els[0]){els[0].click();}})();");
//		} catch (Exception e) {
//		}
		
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ProductDetails")));
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		driver.executeScript("(function(){window.scrollBy(1,250);})();");
		List<String> skuList = null;
		try {	
			
			logger.debug("--->商品页面加载完成");
			
			String sku = param.get("sku");
			
			// 开始选择sku
			logger.debug("--->开始选择sku");
			Map<String, String> skuMap = new HashMap<String, String>();
			int findCount = 0;
			if (StringUtil.isNotEmpty(sku)) {
				skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrValue = skuList.get(i);
						skuMap.put(attrName, attrValue);
					}
				}
				//比较颜色
				WebElement colorWeb = driver.findElement(By.id("DisplayColor"));
				if(!skuMap.get("color").toLowerCase().contains(colorWeb.getText().toLowerCase())){
					logger.debug("--->颜色不对"+colorWeb.getText());
					//return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
				List<WebElement> skuChooseElement = driver.findElements(By.cssSelector(".addToCartControl"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement w:skuChooseElement){
							if(w.getAttribute("data-name").equals(skuList.get(i-1).toLowerCase()) && !skuList.get(i-1).toLowerCase().equals("color")){
								logger.debug("--->开始选择"+skuList.get(i-1));
								try {
									WebElement skuChooses = w.findElement(By.cssSelector("span a[data-value="+"'"+skuList.get(i)+"'"+"]"));
									
									skuChooses.click();
									logger.debug("--->选择"+skuList.get(i));
								} catch (Exception e) {
									WebElement skuChooses = w.findElement(By.cssSelector("span[data-value="+"'"+skuList.get(i)+"'"+"]"));
									skuChooses.click();
									logger.debug("--->1选择"+skuList.get(i));
								}
								break;
							}
						}
					}
				}
				try {
					List<WebElement> skuChooseElements = driver.findElements(By.cssSelector(".selected"));
					for(WebElement w:skuChooseElements){
						String s = w.getAttribute("data-value").trim();
						logger.debug("--->select:"+s);
						for (int i = 0; i < skuList.size(); i++) {
							if (i % 2 == 1) {
								String attrValue = skuList.get(i).trim();
								logger.debug("--->attrValue:"+attrValue);
								if(attrValue.equalsIgnoreCase(s)){
									logger.debug("--->"+attrValue+"加1");
									findCount++;
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					logger.debug("--->单sku");
				}	
				
				logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2);
				if(findCount < skuList.size()/2-1 ){
					logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
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
			WebElement priceFilter = driver.findElement(By.cssSelector("#Price .productPrice"));
			String text = priceFilter.getText().replace("Sale", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1);
				logger.debug("--->[1-1]找到商品单价  = " + priceStr);
				priceMap.put(productEntityId, priceStr);
			}
		} catch (Exception e) {
			try {
				logger.debug("--->[2]开始寻找商品单价");
				String productEntityId = param.get("productEntityId");
				WebElement priceFilter = driver.findElement(By.cssSelector(".singlePrice"));
				String text = priceFilter.getText().replace("Sale", "").trim();
				if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
					String priceStr = text.substring(text.indexOf("$") + 1);
					logger.debug("--->[2-2]找到商品单价  = " + priceStr);
					priceMap.put(productEntityId, priceStr);
				}
			} catch (Exception e1) {
				logger.debug("--->商品单价查找出错1",e1);
				return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
			}
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			try {
				logger.debug("--->选择数量:" + productNum);
				WebElement addButton = driver.findElement(By.cssSelector("#Quantity"));
				Select select = new Select(addButton);
				select.selectByValue(productNum);
			} catch (Exception e) {
				logger.error("--->选择数量失败",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		// 加购物车
		logger.debug("--->开始加购物车");
		try{
			TimeUnit.SECONDS.sleep(3);
			WebElement addCard =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#AddToCartButton")));
			driver.executeScript("var tar=arguments[0];tar.click();", addCard);
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#CartMain")));
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
//		//验证数量
		WebElement numText = driver.findElement(By.cssSelector("#Quantity option[selected='selected']"));
		logger.error("--->数量为:"+numText.getText()+"skuum:"+productNum);
		if(!numText.getText().equals(productNum)){
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		logger.debug("--->确认是否加购物车成功");
			
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount,List<GiftCard> giftCardList) {
		
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		String cardNo = "";
		String year = "";
		String month = "";
		try {
			logger.error("--->myPrice = " + myPrice);
			cardNo = param.get("cardNo");
			String[] cardGroup = cardNo.split(" ");
			cardNo = "";
			for(String s:cardGroup){
				cardNo += s.trim();
			}
			logger.error("--->cardNo = " + cardNo);
			String expiryDate = param.get("expiryDate");
			year = expiryDate.split(" ")[0];
			month = expiryDate.split(" ")[1];
			logger.error("--->year = " + year +" month = "+month);
		} catch (Exception e) {
			logger.error("--->cardNo year",e);
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		WebDriverWait wait = new WebDriverWait(driver, 40);
		
		try
		{
			logger.error("--->购物车页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("CartMain")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
		}catch (Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		String size = param.get("size");
		try {
			List<WebElement> goodsInCart = driver.findElements(By.cssSelector(".removeItem"));
			logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
			logger.debug("--->size有 [" + size + "]件商品");
			if(!size.equals(String.valueOf(goodsInCart.size()))){
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->购物车验证数量出错",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkoutButton")));
			List<WebElement> checkouts = driver.findElements(By.cssSelector(".checkoutButton"));
			for(WebElement w:checkouts){
				if(w.isDisplayed()){
					w.click();
					logger.error("--->点击checkout");
					break;
				}
			}
		}catch (Exception e){
			logger.error("--->点击checkout失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		//选地址
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".billingAddressChooser span")));
			WebElement change = driver.findElement(By.cssSelector(".billingAddressChooser span"));
			change.click();
			Utils.sleep(1000);
			logger.debug("--->点击address edit按钮,等待address 打开");
			String expressAddress = param.get("expressAddress");
			if(!StringUtil.isBlank(expressAddress)){
				logger.debug("--->传过来的转运公司标识为:"+expressAddress);
			}
			List<WebElement> addressList = driver.findElements(By.cssSelector(".addressLoop"));
			logger.debug("--->总共有"+addressList.size()+"个地址");
			if(addressList.size()>0){
				List<WebElement> addrs = new ArrayList<WebElement>();
				String countTemp = (String) param.get("count");
				int count = 0;
				if (!Utils.isEmpty(countTemp)) {
					count = Integer.parseInt(countTemp);
				}
				logger.debug("--->默认地址:"+count);
				for(WebElement w:addressList){
					if(StringUtil.isBlank(expressAddress) || w.getText().contains(expressAddress)){
						addrs.add(w);
					}
				}
				int tarAddr = count % addrs.size();
				WebElement radio = addrs.get(tarAddr).findElement(By.cssSelector(".selectAddress"));
				radio.click();
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#ContinueToPayment")));
					WebElement saveAddress = driver.findElement(By.id("ContinueToPayment"));
					driver.executeScript("var tar=arguments[0];tar.click();", saveAddress);
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#UseEnteredSingleMatch")));
					WebElement useEnteredSingleMatch = driver.findElement(By.id("UseEnteredSingleMatch"));
					driver.executeScript("var tar=arguments[0];tar.click();", useEnteredSingleMatch);
				} catch (Exception e) {
					logger.error("--->地址已经选了");
				}
				
				
			}
		} catch (Exception e) {
			logger.error("--->选择地址失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		//选物流
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".shippingMethod")));
			WebElement shipping = null;
			try {
				shipping = driver.findElement(By.cssSelector(".shippingMethod input[value='UPSSPPS']"));
			} catch (Exception e) {
				shipping = driver.findElement(By.cssSelector(".shippingMethod input[value='Ground']"));
			}
			
			
			shipping.click();
			logger.error("--->选择free 物流");
		}catch (Exception e){
			logger.error("--->点击选择free 物流失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
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
						List<WebElement> codeInputs = driver.findElements(By.id("CustomerDiscountCode"));
						for(WebElement w:codeInputs){
							if(w.isDisplayed()){
								w.clear();
								Utils.sleep(1500);
								w.sendKeys(code);
								Utils.sleep(1500);
								break;
							}
						}
						driver.findElement(By.id("PromoCodeSubmit")).click();
						Utils.sleep(5500);
						
						try{
							WebElement closeWeb = driver.findElement(By.cssSelector(".fancybox-close"));//礼品卡
							closeWeb.click();
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
		
		// 输入安全码
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Payment_CardNumber")));
			WebElement cardNoWeb = driver.findElement(By.id("Payment_CardNumber"));
			cardNoWeb.sendKeys(cardNo);
			Utils.sleep(1500);
			WebElement expirationMonth = driver.findElement(By.id("Payment_ExpirationMonth"));
			Select select = new Select(expirationMonth);
			select.selectByValue(month);
			Utils.sleep(1500);
			WebElement expirationYear = driver.findElement(By.id("Payment_ExpirationYear"));
			Select selectYear = new Select(expirationYear);
			selectYear.selectByValue(year);
			Utils.sleep(1500);
			WebElement securityCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Payment_SecurityCode")));
			logger.debug("--->找到信用卡输入框,开始输入");
			String code = (String) param.get("suffixNo");
			logger.debug("--->信用卡卡号是 = " + code);
			if (Utils.isEmpty(code)) {
				logger.debug("--->没有找到可用的信用卡安全码");
			}
			securityCode.sendKeys(code);
			Utils.sleep(1500);
			logger.debug("--->输入信用卡安全码结束");
			WebElement reviewWeb = driver.findElement(By.id("ContinueToReview"));
			reviewWeb.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->没找到信用卡安全码输入框");
		}
		
		String shipPriceStr = "0";
		// 查询总价
		BigDecimal total = new BigDecimal(0.00);
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("OrderTotal")));
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
			WebElement placeOrderButton = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("SubmitOrderButton")));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".confirmationOrder")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.cssSelector(".confirmationOrder"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取Joesnewbalanceoutlet单号成功:\t" + orderNumber);
				orderNumber = ExpressUtils.regularMallOrderNo(orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
				getTask().addParam("giftCardList", giftCardList);
			} else {
				logger.error("--->获取macy单号出错!");
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
			String orderUrl ="https://www.macys.com/service/order-status?cm_sp=navigation-_-top_nav-_-my_order_history";
			driver.navigate().to(orderUrl);
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orderHistoryDetail")));
			List<WebElement> orders = driver.findElements(By.cssSelector(".orderHistoryDetail"));
			int find = 0;
			for(WebElement o:orders){
				WebElement w = o.findElement(By.cssSelector(".devider"));
				String str = w.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					WebElement orderStatus = o.findElement(By.cssSelector(".orderStatus h2"));
					find = 1;
					logger.error("text:"+orderStatus.getText());
					if(orderStatus.getText().contains("cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(orderStatus.getText().contains("processing")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("order placed")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("preparing to ship")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("payment pending")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("shipment")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector(".trackShipmentBtn"));
						orderNoElement.click();
						//orderNoElement.click();
						Utils.sleep(2000);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".trackID")));
						WebElement box = o.findElement(By.cssSelector(".trackID"));
						String expressCompany = "";
						String expressNo = ExpressUtils.regularExperssNo(box.getText());
						String[] expressGroup = expressNo.split(",");
						for(String s:expressGroup){
							if(!s.startsWith("t")){
								expressNo = s;
								break;
							}
						}
						WebElement boxs = o.findElement(By.cssSelector(".trackEventItem"));
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
						if(boxs.getText().contains("USPS")){
							expressCompany = "USPS";
							logger.error("expressCompany = " + expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							
						}else if(boxs.getText().contains("UPS")){
							expressCompany = "UPS";
							logger.error("expressCompany = " + expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
						}else{
							WebElement trackCar = o.findElement(By.cssSelector(".trackCarrier"));
							if(trackCar.getText().contains("USPS")){
								expressCompany = "USPS";
								logger.error("expressCompany = " + expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								
							}else if(trackCar.getText().contains("UPS")){
								expressCompany = "UPS";
								logger.error("expressCompany = " + expressCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							}
						}
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

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("https://www.joesnewbalanceoutlet.com/");
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转joesnewbalanceoutlet主页面碰到异常");
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
		JoesnewbalanceoutletAutoBuy auto = new JoesnewbalanceoutletAutoBuy();
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
		param1.put("url", "https://www.joesnewbalanceoutlet.com/product/ml1980-ss/ml1980ag");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param1.put("orginalUrl", "https://www.joesnewbalanceoutlet.com/product/ml1980-ss/ml1980ag");
		param1.put("sku", "[[\"color\",\"Grey with Grey\"],[\"size\",\"8\"],[\"width\",\"D\"]]");
		param1.put("num", "2");
		param1.put("productEntityId", "7825898");
		param1.put("isPay", "false");
		param.put("isFirst", "ture");
		param.put("count", "1");
		param.put("suffixNo", "123");
		param.put("my_price", "50");
		param.put("userName", "sbctfb@163.com");
		param.put("password", "Tfb001001");
		AutoBuyStatus status3 = auto.selectProduct(param1);
		Map<String, String> param2 = new HashMap<String, String>();
		param2.put("url", "https://www.joesnewbalanceoutlet.com/product/wl696-smp/wl696sb");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param2.put("orginalUrl", "https://www.joesnewbalanceoutlet.com/product/wl696-smp/wl696sb");
		param2.put("sku", "[[\"color\",\"Black with Blue\"],[\"size\",\"8.5\"],[\"width\",\"B\"]]");
		param2.put("num", "3");
		param2.put("productEntityId", "7825898");
		param2.put("isPay", "false");
		AutoBuyStatus status4 = auto.selectProduct(param2);
		System.out.println(status3);
		
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
			driver.navigate().to("https://www.joesnewbalanceoutlet.com/customer/myaccount");
		} catch (Exception e) {
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#OrderHistory")));
			WebElement orderListWeb = driver.findElement(By.cssSelector("#OrderHistory"));
			orderListWeb.click();
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		try {
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#OrderHistoryTable")));
			List<WebElement> orders = driver.findElements(By.cssSelector("#OrderHistoryTable .orderRow"));
			int find = 0;
			for(WebElement o:orders){
				String str = o.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					WebElement orderStatus = o.findElement(By.cssSelector(".orderStatus"));
					find = 1;
					logger.error("text:"+orderStatus.getText());
					if(orderStatus.getText().contains("cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(orderStatus.getText().contains("Pending")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("New")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Complete")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector(".orderNumber"));
						orderNoElement.click();
						Utils.sleep(5000);
						WebElement box = o.findElement(By.cssSelector("#LineItems"));
						List<WebElement> orderItemList = box.findElements(By.cssSelector(".orderItem"));
						for(WebElement orderI:orderItemList){
							WebElement line = orderI.findElement(By.cssSelector(".lineItem"));
							WebElement aWeb = line.findElement(By.cssSelector(".productDesc"));
							logger.error("url="+aWeb.getText().toLowerCase());
							if(detail.getProductUrl().toLowerCase().endsWith(aWeb.getText().toLowerCase())){
								WebElement track = orderI.findElement(By.cssSelector("#Tracking a"));
								String url = track.getAttribute("href");
								logger.error("href="+url);
								String expressCompany = "";
								if(url.contains("ups")){
									expressCompany = "UPS";
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
									String expressNo = ExpressUtils.regularExperssNo(url);
									String[] expressGroup = expressNo.split(",");
									for(String s:expressGroup){
										if(s.startsWith("1Z")){
											expressNo = s;
											break;
										}
									}
									logger.error("expressNo="+expressNo);
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
									return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								}
							}
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
 