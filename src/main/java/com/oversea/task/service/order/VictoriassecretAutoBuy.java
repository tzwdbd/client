package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: liuxf 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2017年3月4日 下午5:04:14 
*/
public class VictoriassecretAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	//private Timer timer;
	
	public VictoriassecretAutoBuy() {
		super(false);
		//color1=color
		//extend4=length
		//dot3 =size
		//band3 = Band Size
		//cup4 = Cup Size
		//size3 = size radiogroup
		//num=.plus-icon.base
		//.fab-alert售罄This item is no longer available.
//		timer = new Timer();
//		timer.schedule(new TimerTask()
//		{
//			@Override
//			public void run()
//			{
//				driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
//				driver.executeScript("(function(){var els = document.getElementsByClassName('acsCloseButton');if(els && els[0]){els[0].click();}})();");
//				driver.executeScript("(function(){var els = document.getElementsByClassName('close');if(els && els[0]){els[0].click();}})();");
//				
//			}
//		}, 3000, 3000);
		
	}
	

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();

		driver.get("http://www.victoriassecret.com/");
		WebDriverWait wait = new WebDriverWait(driver, 40);
		try
		{
			logger.error("--->打开登陆页面");
			driver.navigate().to("http://www.victoriassecret.com/account/signin");
		}
		catch (Exception e)
		{
			logger.error("--->打开登陆页面", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			logger.error("--->第一次关弹窗");
			driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
			driver.executeScript("(function(){var els = document.getElementsByClassName('acsCloseButton');if(els && els[0]){els[0].click();}})();");
			driver.executeScript("(function(){var els = document.getElementsByClassName('close');if(els && els[0]){els[0].click();}})();");
		} catch (Exception e) {
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			logger.error("--->第二次关弹窗");
			driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
			driver.executeScript("(function(){var els = document.getElementsByClassName('acsCloseButton');if(els && els[0]){els[0].click();}})();");
			driver.executeScript("(function(){var els = document.getElementsByClassName('close');if(els && els[0]){els[0].click();}})();");
		} catch (Exception e) {
		}
		try {
			TimeUnit.SECONDS.sleep(5);
			logger.error("--->第三次关弹窗");
			driver.findElement(By.cssSelector("#modal-content .close")).click();
		} catch (Exception e) {
		}
		
		
		
		try
		{
			WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("useremail")));
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
			WebElement password = driver.findElement(By.id("userpassword"));
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
			logger.debug("--->开始登陆");
			WebElement btn = driver.findElement(By.cssSelector("fieldset .fab-btn--primary"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountLanding")));
		}
		catch (Exception e)
		{
			logger.debug("--->登陆重试");
			driver.executeScript("(function(){var els = document.getElementsByClassName('fab-btn--primary');if(els && els[0]){els[0].click();}})();");
			try
			{
				logger.debug("--->确认是否登陆成功");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountLanding")));
			}
			catch (Exception e2)
			{
				logger.debug("--->登陆失败");
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			logger.error("--->跳转到购物车");
			WebElement cart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bag")));
			cart.click();
			logger.error("--->跳转到购物车成功");
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}

		logger.debug("--->清空购物车");
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bagData")));
			
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.cssSelector("button.remove-item"));
			while (true) {
				int size = list.size();
				logger.error("--->开始清理"+list.size());
				if(list!=null && size>0){
					list.get(0).click();
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.cssSelector("button.remove-item"));
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
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".empty-bag")));
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
		logger.debug("productUrl = " + productUrl);
		//判断是extrabux的返利链接
		String extrabuxMark = "&extrabux=1";
		if(StringUtil.isNotEmpty(productUrl) && productUrl.endsWith(extrabuxMark)){
			logger.debug("--->找到extrabux的返利原链接" + productUrl);
			int index = productUrl.indexOf(extrabuxMark);
			if(index != -1){
				productUrl = productUrl.substring(0,index);
			}
			String rebateUrl = productUrl;
			productUrl = param.get("orginalUrl");
			
			String isFirst = param.get("isFirst");
			if("true".equals(isFirst)){//只需打开一次返利链接
				try{
					driver.navigate().to(rebateUrl);
					TimeUnit.SECONDS.sleep(5);
				}catch(Exception e){
					logger.debug("--->返利链接失败" + rebateUrl);
				}
			}
			
			logger.debug("--->找到extrabux的返利链接" + rebateUrl);
			logger.debug("--->找到extrabux对应的商品返利" + productUrl);
		}
		try
		{
			driver.navigate().to(productUrl);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		logger.debug("--->页面title:"+driver.getTitle());
		//判断商品是否下架
		if("Page Not Available".equals(driver.getTitle())){
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}
		
		
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content")));
		}catch(Exception e){
			logger.error("--->商品页面加载出现异常",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		driver.executeScript("(function(){window.scrollBy(1,350);})();");
		
		try {
			driver.findElement(By.cssSelector(".fab-alert"));
			logger.debug("--->商品不可用");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		} catch (Exception e) {
			logger.debug("--->商品可用");
		}
		
		try {	
			
			logger.debug("--->商品页面加载完成");
			
			String sku = param.get("sku");
			
			// 开始选择sku
			logger.debug("--->开始选择sku");
			Map<String, String> skuMap = new HashMap<String, String>();
			int findCount = 0;
			if (StringUtil.isNotEmpty(sku)) {
				List<String> skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String attrName = skuList.get(i - 1).toLowerCase();
						String attrValue = skuList.get(i);
						skuMap.put(attrName, attrValue);
					}
				}
				List<String> keyList = new ArrayList<String>();
				List<WebElement> skuChooseElementsOther = driver.findElements(By.cssSelector(".primary h2.fab-body span"));
				if(skuChooseElementsOther==null || skuChooseElementsOther.size()==0){
					logger.debug("--->样式换了");
					List<WebElement> skuChooseElement = driver.findElements(By.cssSelector(".primary section div[role='radiogroup']"));
					for(WebElement w:skuChooseElement){
						List<WebElement> skuChoose = w.findElements(By.cssSelector("a"));
						for(WebElement c:skuChoose){
							WebElement skuEle = c.findElement(By.cssSelector(".fab-a11y-hide"));
							if(skuEle.getText().equalsIgnoreCase("color")){
								List<WebElement> colorList = c.findElements(By.cssSelector("img"));
								for(WebElement color:colorList){
									if(!StringUtil.isBlank(color.getAttribute("alt")) && color.getAttribute("alt").equals(skuMap.get("color"))){
										logger.debug("--->选择Color:" + skuMap.get("color"));
										if(!"true".equals(c.getAttribute("data-is-selected"))){
											color.click();
										}
										TimeUnit.SECONDS.sleep(2);
										break;
									}
								}
							}else{
								String s = c.getText();
								String aName = s.split("\n")[0].replace(" ", "").toLowerCase();
								String aValue = s.split("\n")[1];
								for(String key:skuMap.keySet()){
									if(key.equals(aName) && skuMap.get(key).equalsIgnoreCase(aValue)){
										logger.debug("--->选择:" + aName+":"+aValue);
										c.click();
										TimeUnit.SECONDS.sleep(2);
										break;
									}
								}
							}
						}
					}
					
					
					List<WebElement> skuChooseElements = driver.findElements(By.cssSelector(".primary section div[data-selector-title] span"));
					for(WebElement w:skuChooseElements){
						
						String s = w.getText();
						for (int i = 0; i < skuList.size(); i++) {
							if (i % 2 == 1) {
								String attrValue = skuList.get(i);
								if(attrValue.equalsIgnoreCase(s)){
									logger.debug("--->"+attrValue+"加1");
									findCount++;
									break;
								}
							}
						}
					}
					logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2+" && skuChooseElements.size="+skuChooseElements.size());
					if(findCount < skuList.size()/2 || skuChooseElements.size()<skuList.size()/2){
						logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
					
				}else{
					for(WebElement w:skuChooseElementsOther){
						for(String key:skuMap.keySet()){
							if(skuMap.get(key).equalsIgnoreCase(w.getText())){
								logger.debug("--->sku"+key+":"+w.getText());
								keyList.add(key);
								break;
							}
						}
					}
					
					List<WebElement> skuElements = driver.findElements(By.cssSelector(".primary h2.fab-body+div"));
					for(WebElement w:skuElements){
						List<WebElement> skuChoose = w.findElements(By.cssSelector("a"));
						if(!StringUtil.isBlank(w.getAttribute("data-name")) && w.getAttribute("data-name").equalsIgnoreCase("color")){
							for(WebElement c:skuChoose){
								List<WebElement> colorList = c.findElements(By.cssSelector("img"));
								for(WebElement color:colorList){
									if(!StringUtil.isBlank(color.getAttribute("alt")) && color.getAttribute("alt").equals(skuMap.get("color")) && !keyList.contains("color")){
										logger.debug("--->选择Color:" + skuMap.get("color"));
										color.click();
										TimeUnit.SECONDS.sleep(2);
										break;
									}
								}
							}
						}else{
							for(WebElement c:skuChoose){
								String s = c.getText();
								String aName = s.split("\n")[0].replace(" ", "").toLowerCase();
								String aValue = s.split("\n")[1];
								for(String key:skuMap.keySet()){
									if(key.equals(aName) && skuMap.get(key).equalsIgnoreCase(aValue)){
										logger.debug("--->选择:" + aName+":"+aValue);
										c.click();
										TimeUnit.SECONDS.sleep(2);
										break;
									}
								}
							}
						}
					}
					List<WebElement> skuChooseElements = driver.findElements(By.cssSelector(".primary h2.fab-body span"));
					for(WebElement w:skuChooseElements){
						for(String key:skuMap.keySet()){
							if(skuMap.get(key).equalsIgnoreCase(w.getText())){
								logger.debug("--->sku key = "+key+"&text="+w.getText());
								findCount++;
								TimeUnit.SECONDS.sleep(2);
								break;
							}
						}
					}
					logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2+" && skuChooseElements.size="+skuChooseElements.size());
					if(findCount < skuList.size()/2 || skuChooseElements.size()<skuList.size()/2){
						logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
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
			WebElement priceFilter = driver.findElement(By.cssSelector(".atp-message-price"));
			String text = priceFilter.getText().replace("each", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.substring(text.indexOf("$") + 1);
				logger.debug("--->[1-1]找到商品单价  = " + priceStr);
				priceMap.put(productEntityId, priceStr);
			}
		} catch (Exception e) {
			logger.debug("--->商品单价查找出错",e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			try {
				logger.debug("--->选择数量:" + productNum);
				List<WebElement> addButton = driver.findElements(By.cssSelector("section[data-name='quantity'] button"));
				for(WebElement w:addButton){
					if(!w.getAttribute("data-change-amount").equals("-1")){
						for(int i=1;i<Integer.parseInt(productNum);i++){
							w.click();
							TimeUnit.SECONDS.sleep(2);
						}
					}
				}
			} catch (Exception e) {
				logger.error("--->选择数量失败",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		//验证数量
		WebElement numText = driver.findElement(By.cssSelector("section[data-name='quantity'] div span"));
		logger.error("--->数量为:"+numText.getText());
		if(!numText.getText().equals(productNum)){
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		// 加购物车
		logger.debug("--->开始加购物车");
		driver.executeScript("(function(){window.scrollBy(1,200);})();");
		try{
			TimeUnit.SECONDS.sleep(3);
			//WebElement addCard =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".primary button.add-to-bag")));
			//addCard.click();
			driver.executeScript("(function(){var els = document.getElementsByClassName('add-to-bag');if(els && els[0]){els[0].click();}})();");
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		logger.debug("--->确认是否加购物车成功");
		try{
			
			WebElement continueShop = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.fab-btn--secondary.primary.close")));
			continueShop.click();
			logger.debug("--->点击继续购物");
		}catch(Exception e){
			logger.error("--->加购物车出现异常",e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
			
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
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

		WebDriverWait wait = new WebDriverWait(driver, 40);
		
		try
		{
			logger.error("--->跳转到购物车");
			WebElement cart = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bag")));
			cart.click();
			logger.error("--->跳转到购物车成功");
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try
		{
			logger.error("--->购物车页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bagData")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
		}catch (Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try
		{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bag-actions a.fab-btn--primary")));
			checkout.click();
			logger.error("--->点击checkout");
		}catch (Exception e){
			logger.error("--->点击checkout失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try
		{
			WebElement billingAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billingAddressContinue")));
			driver.executeScript("(function(){window.scrollBy(1,400);})();");
			Utils.sleep(2000);
			billingAddress.click();
			logger.error("--->点击billingAddress");
		}catch (Exception e){
			logger.error("--->点击billingAddress失败");
			//return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		/*String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}*/
		
		/*String userName = (String) param.get("userName");
		String passWord = (String) param.get("password");
		try {
			WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("useremail")));
			logger.debug("--->账号:"+username.getAttribute("value"));
			String userAccount = username.getAttribute("value");
			if(!userName.equals(userAccount)){
				logger.error("--->账号不对应");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			
			WebElement password = wait.until(ExpectedConditions.elementToBeClickable(By.id("userpassword")));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(5);
			
			logger.debug("--->开始登陆");
			WebElement btn = driver.findElement(By.cssSelector(".fab-btn--primary"));
			TimeUnit.SECONDS.sleep(5);
			btn.click();
			
		} catch (Exception e) {
			logger.debug("--->付款时帐号登录失败");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}*/
		
		
		
		
		
		
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fab-btn--checkout-submit")));
			driver.executeScript("(function(){window.scrollBy(1,400);})();");
			Utils.sleep(2000);
			WebElement gift = driver.findElement(By.id("giftOption"));
			
			if(!gift.isSelected()){
				gift.click();
				logger.error("--->选中礼品模式");
			}
			Utils.sleep(2000);
			WebElement placeOrderElement = driver.findElement(By.cssSelector(".fab-btn--checkout-submit"));
			driver.executeScript("var tar=arguments[0];tar.click();", placeOrderElement);
			logger.error("--->点击deliveryElement");
		}catch (Exception e){
			logger.error("--->点击deliveryElement失败");
			try
			{
				WebElement placeOrderElement = driver.findElement(By.cssSelector(".fab-btn--checkout-submit"));
				placeOrderElement.click();
				logger.error("--->点击deliveryElement1");
			}catch (Exception e1){
				logger.error("--->点击deliveryElement失败1");
				//return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}
		
		//使用优惠码0 失效,1互斥 ,9没修改过,10有效
		boolean isEffective = false;
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			logger.debug("--->等待优惠码框加载");
			logger.debug("promotionList.toString()" + promotionList.toString());
			if(!StringUtil.isBlank(type) && type.equals("1")){
				try
				{
					WebElement codeButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("offersForm")));
					WebElement codeB = codeButton.findElement(By.cssSelector("header button"));
					codeB.click();
					logger.error("--->点击优惠码");
				}catch (Exception e){
					logger.error("--->点击优惠码失败");
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
			}else{
				WebElement aButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.fab-link-item")));
				aButton.click();
			}
			
			int j=0;
			for (String code : promotionList) {
				try {
					logger.debug("code:"+code);
					TimeUnit.SECONDS.sleep(2);
					WebElement promoCode = null;
					try {
						if(j==0){
							promoCode = driver.findElement(By.cssSelector("#offerCode0"));
						}
						if(j==1){
							promoCode = driver.findElement(By.cssSelector("#offerCode1"));
						}
						if(j==2){
							promoCode = driver.findElement(By.cssSelector("#offerCode2"));
						}
					} catch (Exception e) {
						if(j==0){
							promoCode = driver.findElement(By.cssSelector("#offersForm #drawer-offerCode0"));
						}
						if(j==1){
							promoCode = driver.findElement(By.cssSelector("#offersForm #drawer-offerCode1"));
						}
						if(j==2){
							promoCode = driver.findElement(By.cssSelector("#offersForm #drawer-offerCode2"));
						}
					}
					
					logger.debug("--->找到优惠码输入框,开始输入");
					promoCode.clear();
					promoCode.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
					if(!StringUtil.isBlank(type) && type.equals("1")){
						WebElement apply = driver.findElement(By.cssSelector("#offersForm #applyOffersBtn"));
						driver.executeScript("var tar=arguments[0];tar.click();", apply);//apply.click();
					}else{
						WebElement apply = driver.findElement(By.id("applyOffersBtn"));
						driver.executeScript("var tar=arguments[0];tar.click();", apply);//apply.click();
					}
					TimeUnit.SECONDS.sleep(2);
					try {
						driver.findElement(By.cssSelector(".fab-alert.error"));
						logger.debug("--->优惠码不可用！");
						statusMap.put(code, 0);
					} catch (Exception e) {
						logger.debug("优惠码可用");
						isEffective = true;
						statusMap.put(code, 10);
						//promoCode.clear();
					}
					j++;
				} catch (Exception e) {
					logger.debug("输入优惠码异常",e);
				}
			}
			
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			try
			{
				WebElement codeButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("offersForm")));
				WebElement codeB = codeButton.findElement(By.cssSelector("header button"));
				codeB.click();
				logger.error("--->点击优惠码窗口");
			}catch (Exception e){
				logger.error("--->关闭优惠码窗口");
			}
			
//			try {
//				WebElement closeButton = driver.findElement(By.cssSelector(".drawer-close"));
//				closeButton.click();
//			} catch (Exception e) {
//				logger.debug("--->关闭优惠码窗口");
//			}
		}
		String shipPriceStr = "0";
		// 查询总价
		try {
			logger.debug("--->开始查询运费");
			Utils.sleep(5000);
			WebElement shipPriceElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-summary-review")));
			String text = shipPriceElement.getText().split("\n")[3];
			text = text.replace(",", "").trim();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				shipPriceStr = text.substring(text.indexOf("$") + 1);
				//data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, shipPriceStr);
				logger.debug("--->找到商品运费 = " + shipPriceStr);
			}
		} catch (Exception e) {
			logger.debug("--->查询商品运费出错");
		}
		BigDecimal total = new BigDecimal(0.00);
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fab-h2.orderTotal")));
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
		if(!StringUtil.isBlank(type) && type.equals("1")){
			try
			{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("giftCardForm")));
				Utils.sleep(2000);
				WebElement giftCard = driver.findElement(By.id("giftCardForm"));
				giftCard.click();
				logger.error("--->点击giftCard");
				TimeUnit.SECONDS.sleep(1);
				BigDecimal cardTotal = new BigDecimal(0.00);
				WebDriverWait wait0 = new WebDriverWait(driver, 20);
				boolean marks=false;
				for(GiftCard card:giftCardList){
					WebElement giftCardNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("giftCardNumber")));
					logger.debug("--->礼品卡号"+card.getSecurityCode());
					giftCardNumber.clear();
					giftCardNumber.sendKeys(card.getSecurityCode());
					TimeUnit.SECONDS.sleep(2);
					
					WebElement giftCardPin = driver.findElement((By.id("giftCardPin")));
					logger.debug("--->礼品卡密码"+card.getPassWord());
					giftCardPin.clear();
					giftCardPin.sendKeys(card.getPassWord());
					TimeUnit.SECONDS.sleep(2);
					
					WebElement checkBalance = driver.findElement((By.id("checkBalance")));
					driver.executeScript("var tar=arguments[0];tar.click();", checkBalance);
					//checkBalance.click();
					logger.debug("--->checkBalance click");
					TimeUnit.SECONDS.sleep(2);
					card.setIsUsed("yes");
					//比较真实余额是否正确
					WebElement balanceElement = null;
					try {
						balanceElement = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".balance")));
					} catch (Exception e) {
						card.setIsSuspect("yes");
						continue;
					}
					
					logger.debug("--->礼品卡余额:"+balanceElement.getText());
					
					
					String text = balanceElement.getText().replace("USD.", "").trim();
					if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
						String priceStr = text.substring(text.indexOf("$") + 1);
						logger.debug("--->礼品卡总额 = " + priceStr);
						BigDecimal y = new BigDecimal(priceStr);
						BigDecimal x = new BigDecimal(card.getRealBalance());
						if(x.doubleValue()!=y.doubleValue()){
							logger.debug("--->x = " + x+"--->y = "+y);
							//card.setIsSuspect("yes");
							//continue;
						}
						WebElement apply = driver.findElement(By.cssSelector("#giftCard button[name='Apply Gift Card']"));
						driver.executeScript("var tar=arguments[0];tar.click();", apply);//apply.click();
						logger.debug("--->apply click");
						TimeUnit.SECONDS.sleep(2);
						driver.executeScript("(function(){window.scrollBy(1,50);})();");
						TimeUnit.SECONDS.sleep(2);
						cardTotal = cardTotal.add(y);
						BigDecimal v = total.subtract(cardTotal);
						if (v.doubleValue() > 0.00D) {
							card.setRealBalance("0");
							logger.error("--->礼品卡不够继续，继续添加");
							
						}else{
							BigDecimal z = cardTotal.subtract(total);
							card.setRealBalance(String.valueOf(z));
							logger.error("--->礼品卡够了");
							marks = true;
							break;
						}
					}
					
				}
				if(marks){
					WebElement paymentCommit = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("paymentCommit")));
					driver.executeScript("var tar=arguments[0];tar.click();", paymentCommit);//paymentCommit.click();
					TimeUnit.SECONDS.sleep(2);
				}else{
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
				
				
			}catch (Exception e){
				logger.error("--->点击giftCard失败",e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}
		driver.executeScript("(function(){window.scrollBy(1,400);})();");
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		// placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		try {
			WebElement placeOrderButton = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".placeOrder")));
			Utils.sleep(1500);
			if (isPay) {
				logger.debug("------------------");
				logger.debug("--->啊啊啊啊，我要付款啦！！！");
				driver.executeScript("var tar=arguments[0];tar.click();", placeOrderButton);
				//placeOrderButton.click();
				logger.debug("------------------");
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		} catch (Exception e) {
			logger.debug("--->付款失败");
			try {
				logger.debug("--->重试1次");
				WebElement placeOrderButtonTryAgain = driver.findElement(By.cssSelector(".placeOrder"));
				placeOrderButtonTryAgain.click();
				Utils.sleep(5000);
				logger.debug("--->点击付款完成 placeOrder finish");
			} catch (Exception e2) {
				logger.debug("--->重试付款失败");
			}
		}
		
		// 查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("thankYouDetail")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.cssSelector(".thankYouDetail h2"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取Victoriassecret单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
				getTask().addParam("giftCardList", giftCardList);
			} else {
				logger.error("--->获取Victoriassecret单号出错!");
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
			String orderUrl ="https://www.victoriassecret.com/account/orderstatus";
			driver.navigate().to(orderUrl);
			logger.error("爬取物流开始");
			Utils.sleep(5000);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("content")));
			WebElement wb = null;
			try {
				
				wb = driver.findElement(By.cssSelector(".order-status"));
					
			} catch (Exception e) {
				logger.error("物流只能查看邮箱");
				return AutoBuyStatus.AUTO_SCRIBE_CALL_CUSTOMER_SERVICE;
			}
			List<WebElement> orders = wb.findElements(By.cssSelector("tbody tr"));
			int find = 0;
			for(WebElement o:orders){
				String str = o.getText().toLowerCase();
				if(str.contains(mallOrderNo)){
					find = 1;
					if(str.contains("cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if(str.contains("in process")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(str.contains("order placed")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(str.contains("preparing to ship")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(str.contains("payment pending")){
						logger.error("[1]该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(str.contains("shipped")){
						// 商城订单号一样 包裹号不一样
						WebElement orderNoElement = o.findElement(By.cssSelector("td a"));
						driver.navigate().to(orderNoElement.getAttribute("href"));
						//orderNoElement.click();
						Utils.sleep(2000);
						WebElement trackElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".track")));
						driver.navigate().to(trackElement.getAttribute("href"));
						//trackElement.click();
						Utils.sleep(2000);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
						List<WebElement> boxs = driver.findElements(By.cssSelector(".orderinfo.table .row"));
						String expressCompany = "";
						for(WebElement box : boxs){
							String s = box.getText().toLowerCase();
							logger.error("物流公司  " + s);
							if(s.contains("carrier")){
								if(s.contains("us")){
									expressCompany = "USPS";
									logger.error("expressCompany = " + expressCompany);
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
								}
								
							}
							if(s.contains("carrier tracking number")){
								String expressNo = s.replace("carrier tracking number ", "");
								logger.error("expressNo = " + expressNo);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								if(StringUtils.isBlank(expressCompany)){
									data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "USPS");
								}
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}
						}
					}else if(str.contains("delivered")){
						logger.debug("物流已完成"+str);
						return AutoBuyStatus.AUTO_SCRIBE_CALL_CUSTOMER_SERVICE;
					}else{
						logger.debug("未识别的物流状态"+str);
					}
					break;
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
			driver.get("http://www.victoriassecret.com/");
//			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper")));
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转victoriassecret主页面碰到异常");
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
		VictoriassecretAutoBuy auto = new VictoriassecretAutoBuy();
		AutoBuyStatus status = auto.login("tzwdbd@126.com", "Tfb001001");
		System.out.println(status);
		/*AutoBuyStatus status1 = auto.cleanCart();
		System.out.println(status1);*/
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "https://www.victoriassecret.com/clearance/bras/mesh-plunge-bralette-the-bralette-collection?ProductID=337567&CatalogueType=OLS");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param.put("orginalUrl", "https://www.victoriassecret.com/clearance/bras/mesh-plunge-bralette-the-bralette-collection?ProductID=337567&CatalogueType=OLS");
		param.put("sku", "[[\"color\",\"Sterling Pewter Two Tone Print\"],[\"size\",\"S\"]]");
		param.put("num", "3");
		param.put("productEntityId", "7512528");
		param.put("isPay", "false");
		param.put("isFirst", "ture");
		param.put("count", "1");
		param.put("suffixNo", "123");
		param.put("userName", "okpljhb@163.com");
		param.put("password", "Tfb001001");
		AutoBuyStatus status2 = auto.selectProduct(param);
		System.out.println(status2);
		/*Map<String, String> param1 = new HashMap<String, String>();
		param1.put("url", "http://www.dpbolvw.net/click-1915435-10596045?cm_mmc=CJ-_-1720273-_-1915435-_-Victorias%20Secret%20Logo&afsrc=1&sid=1662305_474_1489131184_58c256b0eeee6_SUB&extrabux=1");
//		param.put("sku", "[[\"color\",\"Black\"],[\"size\",\"10\"],[\"width\",\"M\"]]");
		//param.put("sku", "[[\"color\",\"Red\"]]");
		param1.put("orginalUrl", "https://www.victoriassecret.com/bralettes/bralettes-and-panties-special-offer/long-line-plunge-bralette-the-bralette-collection?ProductID=329280&CatalogueType=OLS");
		param1.put("sku", "[[\"color\",\"Coconut White\"],[\"size\",\"S\"]]");
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
		System.out.println(status3);*/
		
		/*AutoBuyStatus status6 = auto.pay(param);
		System.out.println(status6);*/
		
		
		/*RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("662842271");
		detail.setProductSku("[[\"color\",\"001 Pink\"]]");
		auto.scribeExpress(detail);*/
		//auto.logout();
	}
	
}
 