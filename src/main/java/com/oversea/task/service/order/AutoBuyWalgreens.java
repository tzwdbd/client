package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class AutoBuyWalgreens extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
   // private Timer timer;
	
	public AutoBuyWalgreens(){
		super(false);
		
//		timer = new Timer();
//		timer.schedule(new TimerTask()
//		{
//			@Override
//			public void run()
//			{
//				driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
//			}
//		}, 3000, 3000);
	}
	
	public static void main(String[] args){
		AutoBuyWalgreens auto = new AutoBuyWalgreens();
		AutoBuyStatus status = auto.login("tzwdbd@126.com", "aa123456");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.scribeExpress("114-4751822-5849814");
			status = auto.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
					Map<String, String> param = new HashMap<String, String>();
					param.put("url", "https://www.walgreens.com/store/c/schiff-move-free-joint-health-glucosamine-chondroitin-plus-msm--vitamin-d3-tablets/ID=prod3855572-product?AID=10652189&PID=6110390&SID=4s8qrx&CID=3791870&ext=6110390");
					param.put("num", "2");
					param.put("productEntityId", "efe");
					param.put("sku", null);
					status = auto.selectProduct(param);
					Map<String, String> params = new HashMap<String, String>();
					params.put("url", "https://www.walgreens.com/store/c/schiff-move-free-advanced-triple-strength-glucosamine-chondroitin-coated-tablets/ID=prod1993553-product?AID=10652189&PID=6110390&SID=4s8x03&CID=3791870&ext=6110390");
					params.put("num", "1");
					params.put("productEntityId", "efse");
					params.put("sku", null);
					status = auto.selectProduct(params);
					System.out.println("status1 = "+ status.getValue());
//					if (AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//						Map<String, String> params = new HashMap<String, String>();
//						params.put("url", "https://www.walgreens.com/store/c/vitafusion-women's-daily-multivitamin-gummy/ID=prod6108350-product");
//						params.put("num", "3");
//						param.put("productEntityId", "sefe");
//						status = auto.selectProduct(params);
//						System.out.println("status2 = "+ status.getValue());
//						//if (AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//							Map<String, String> paramss = new HashMap<String, String>();
//							paramss.put("my_price", "38.5");
//							paramss.put("count", "1");
//							paramss.put("isPay", String.valueOf(false));
//							paramss.put("cardNo", "4662 4833 6029 1396");
//							status = auto.pay(paramss);
//						//}
//					}
			}
			System.out.println("status = "+ status.getValue());
		}
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("https://www.walgreens.com");
		Utils.sleep(1000);
		driver.navigate().to("https://www.walgreens.com/login.jsp");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName-phoneNo")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			WebElement continueElement = driver.findElement(By.id("continueBtn"));
			continueElement.click();
			Utils.sleep(1500);
			//输入密码
			WebElement password = driver.findElement(By.id("password"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.id("passwordtabSignIn"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("wag-body-main-container")));
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
		//跳转到购物车
		try{
			WebElement cart = driver.findElement(By.id("cartFocus"));
			//WebElement viewCart = cart.findElement(By.xpath("//span[@class='icon-Cart wag-icon-shoppingcart-logo']"));
			logger.error("--->开始跳转到购物车");
			Utils.sleep(1500);
			cart.click();
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			Utils.sleep(5000);
			List<WebElement> remoteItem = driver.findElements(By.linkText("Remove"));
			while (true) {
				int size = remoteItem.size();
				if(remoteItem!=null && size>0){
					remoteItem.get(0).click();
					Utils.sleep(1000);
					if(size>1){
						remoteItem = driver.findElements(By.linkText("Remove"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		Utils.sleep(1000);
		logger.error("--->确认购物车是否清空");
		try {
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("continue-no-item-link-text")));
		} catch (Exception e) {
			logger.error("--->清空购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		Object sku = param.get("sku");
		if(sku!=null && !"".equals(sku)){
			logger.debug("--->sku选择失败");
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		
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
		WebElement joinCart = null;
		try{
			joinCart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("receiveing-addToCartbtn")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		
		
		String productNum = (String) param.get("num");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement integerElement = driver.findElement(By.cssSelector(".wag-price-line .wag-price-info span"));
			String integerText = integerElement.getText();
			logger.debug("--->开始寻找商品单价"+integerElement.getText());
			List<WebElement> floatElements = driver.findElements(By.cssSelector(".wag-price-line .wag-price-info sup"));
			String floatText = floatElements.get(1).getText();
			String productEntityId = param.get("productEntityId");
			logger.debug("--->找到商品单价 = "+integerText+"."+floatText);
			priceMap.put(productEntityId, integerText+"."+floatText);
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				WebElement inputNum = driver.findElement(By.id("wag-qt-y"));
				Utils.sleep(2000);
				inputNum.clear();
				Utils.sleep(2000);
				inputNum.sendKeys(productNum);
				Utils.sleep(2000);
				logger.debug("--->选择商品数量完成");
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		//加入购物车
		logger.debug("--->加入购物车");
		try {
			joinCart.click();
		} catch (Exception e) {
			logger.debug("--->加入碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		Utils.sleep(2000);
		logger.debug("--->去购物车");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addToCart-cart-checkout")));
			WebElement gotoCart = driver.findElement(By.id("addToCart-cart-checkout"));
			Utils.sleep(1000);
			gotoCart.click();
		} catch (Exception e) {
			logger.debug("--->去购物车碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		Utils.sleep(1000);
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		//优惠码
		Set<String> promotionList = getPromotionList(param.get("promotion"));
	
		//设置价格
		logger.error("--->myPrice = "+myPrice);
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String)param.get("count");
		int count = 0;
		if(!Utils.isEmpty(countTemp)){
			count = Integer.parseInt(countTemp);
		}
		
		//跳转到购物车页面
		/*logger.debug("--->跳转到购物车页面");
		try{
			WebElement cart = driver.findElement(By.id("cartFocus"));
			//WebElement viewCart = cart.findElement(By.xpath("//span[@class='icon-Cart wag-icon-shoppingcart-logo']"));
			logger.error("--->开始跳转到购物车");
			Utils.sleep(1500);
			cart.click();
			Utils.sleep(1500);
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->加载购物车出现异常");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}*/
		
		//优惠码
		if(promotionList != null && promotionList.size() > 0){
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						
						List<WebElement> codeInputs = driver.findElements(By.id("enter_code"));
						for(WebElement codeInput:codeInputs){
							if(codeInput.isDisplayed()){
								codeInput.clear();
								Utils.sleep(2500);
								codeInput.sendKeys(code);
								Utils.sleep(1500);
								break;
							}
						}
						
						List<WebElement> applyCodes = driver.findElements(By.id("apply_code"));
						for(WebElement applyCode:applyCodes){
							if(applyCode.isDisplayed()){
								applyCode.click();
								break;
							}
						}
						Utils.sleep(5500);
						
						try{
							List<WebElement> invalidCodes =driver.findElements(By.cssSelector("strong.ng-binding .sr-only"));
							for(WebElement invalidCode:invalidCodes){
								if(invalidCode.isDisplayed()){
									if(invalidCode.getText().equals("Success:")){
										statusMap.put(code, 10);
										isEffective = true;
									}else if(invalidCode.getText().equals("Error:")){
										statusMap.put(code, 0);
									}
									break;
								}
							}
							
						}catch(Exception e){//ng-binding sr-only
							//Your promotion code has been applied successfully!
							logger.error("promotionCode:"+code,e);
							try{
								statusMap.put(code, 0);
							}catch(Exception ee){
								logger.error("promotionCode:"+code,ee);
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
		
		logger.debug("--->等待支付页面加载");
		Utils.sleep(2000);
		//跳转支付页面
		try{
			List<WebElement> checkout = driver.findElements(By.id("proceedtocheckout"));
			for(WebElement wl:checkout){
				if(wl.isDisplayed()){
					wl.click();
					break;
				}
			}
			logger.debug("--->跳转支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->跳转支付页面加载异常");
			
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		//选收货地址
		
		if(count % 2==1){
			try{
				logger.debug("--->开始寻找address change按钮");
				Utils.sleep(1500);
				List<WebElement> gotoShipAddress = driver.findElements(By.id("wag-cko-id-sa-btn-edad-b"));
				for(WebElement address:gotoShipAddress){
					if(address.isDisplayed() && "Edit shipping address ›".equals(address.getText())){
						address.click();
						break;
					}
				}
				Utils.sleep(1500);
				logger.debug("--->address打开完成");
				WebElement addressElement = driver.findElement(By.xpath("//section[@id='ship-address']/section[@class='row wag-cko-add-book-block']"));
				List<WebElement> addressList = addressElement.findElements(By.cssSelector(".col-lg-8.col-md-8.col-sm-12.col-xs-12.mb20"));
				if(addressList != null && addressList.size() > 0){
	//						int tarAddr = count % list.size();
					int tarAddr = count % 2;
					WebElement ele = addressList.get(tarAddr);
					Utils.sleep(1500);
					WebElement buttonSelect = ele.findElement(By.xpath(".//button[@id='wag-cko-id-sa-sel-d']"));
					if(buttonSelect!=null){
						logger.debug("--->地址另一个地址");
						buttonSelect.click();
					}
					logger.debug("--->点击所选地址,等待address关闭");
					
				}else{
					logger.debug("--->该账号没有设置收货地址");
					return AutoBuyStatus.AUTO_PAY_ADDR_INDEX_OVER_MAX;
				}
			}catch(Exception e){
				logger.debug("--->选择地址出错 = ",e);
				return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
			}
		}
		Utils.sleep(5000);
		
		//勾选
		try {
			
			WebElement giftElement =  driver.findElement(By.xpath("//input[@id='sendgifts']"));
			giftElement.click();
		} catch (Exception e) {
			logger.debug("--->勾选出错");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.xpath("//p[@id='wag-cko-id-os-esttoat-txt2']"));
			String text = totalPriceElement.getText().replace(",", "").trim();
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1);
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
					if (v.doubleValue() > 20.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
		}
		
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			List<WebElement> placeOrderElements = driver.findElements(By.xpath("//button[@id='wag-cko-osb-btn-subord']"));
			WebElement placeOrderElement = null;
			for(WebElement po:placeOrderElements){
				if(po.isDisplayed()){
					placeOrderElement = po;
					break;
				}
			}
			Utils.sleep(1500);
			if(isPay){
				logger.debug("--->啊啊啊啊啊，我要付款了");
				placeOrderElement.click();
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){}
		//还未完成
		//查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("strong .ng-binding")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.cssSelector("strong .ng-binding"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取walgreens单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
			} else {
				logger.error("--->获取walgreens单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->查找商品订单号出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		try {
			driver.navigate().to("https://www.walgreens.com/youraccount/orderstatus/orderstatus.jsp");
		} catch (Exception e) {
			logger.error("--->查询url订单异常");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("wag-cac-purchase-history-container")));
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		try {
			List<WebElement> mallOrderNoList = driver.findElementsByCssSelector(".wag-cac-orderelipsis");
			for(WebElement w:mallOrderNoList){
				if(w.getText().contains(mallOrderNo)){
					w.findElement(By.cssSelector("a")).click();
					logger.error("--->选中:"+mallOrderNo);
					break;
				}
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("wag-body-main-container")));
			WebElement status = driver.findElement(By.xpath("//p[@class='wag-head-text4 ng-scope']"));
			String str = status.getText().toLowerCase();
			logger.error("--->查询订单status:"+str);
			if(str.contains("shipped")){
				List<WebElement> trackList = driver.findElementsByCssSelector(".wag-cac-bg-tracknum-mob");
				for(WebElement w:trackList){
					String expressCompany = null;
					if(w.getText().toLowerCase().contains("ups")){
						expressCompany = "UPS";
					} else if(w.getText().toLowerCase().contains("usps")){
						expressCompany = "USPS";
					} else if(w.getText().toLowerCase().contains("ontrac")) {
						expressCompany = "OnTrac";
					} else if(w.getText().toLowerCase().contains("fedex")){
						expressCompany = "FedEx";
					} else {
						logger.debug("没有找到物流公司名称");
					}
					logger.error("expressCompany and expressNo = " + w.findElement(By.cssSelector("a")).getText());
					logger.error("expressNo = " + w.findElement(By.cssSelector("a")).getText().split(" ")[1]);
					logger.error("expressCompany = " + expressCompany);
					
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY,expressCompany );
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, w.findElement(By.cssSelector("a")).getText().split(" ")[1]);
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}
			}else if(str.contains("cancelled")){
				logger.error("该订单被砍单了");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
			}else if(str.contains("in progress")){
				logger.error("[1]该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}else{
				logger.debug("未识别的物流状态"+str);
			}
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		try{
			Utils.sleep(2000);
			driver.get("http://www.walgreens.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("youaccountdropdown")));
		}
		catch (Exception e){
			logger.error("--->跳转walgreens主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}
	
	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		//timer.cancel();
		return true;
	}
}
