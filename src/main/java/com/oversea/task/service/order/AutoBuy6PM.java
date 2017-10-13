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

public class AutoBuy6PM extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		
		AutoBuy6PM auto = new AutoBuy6PM();
//		AutoBuyStatus status = auto.login("liuyuandong2@hotmail.com", "leixun123456");
//		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.cleanCart();
//			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				param.put("url", "https://p.gouwuke.com/c?w=858413&c=18133&i=43784&pf=y&e=&t=http://www.6pm.com/p/tommy-hilfiger-corinne-ii-dome-backpack-dory-blue/product/8852166/color/639597");
//				param.put("url", "http://www.6pm.com/ugg-sea-glisten-anchor-red-suede");
//				param.put("url", "http://www.6pm.com/gabriella-rocha-alena-evening-purse-with-tassel-black");
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				param.put("sku", "[[\"color\",\"Dory Blue\"],[\"size\",\"One Size\"]]");
				param.put("num", "1");
				auto.selectProduct(param);
				//if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "39.99");
//					param0.put("count", "1");
//					param0.put("isPay", String.valueOf(false));
//					param0.put("cardNo", "4662 4833 6029 1396");
//					status = auto.pay(param0);
				//}
			//}
		//}
		//auto.logout();
	}
	
	public static void main0(String[] args){
		AutoBuy6PM auto = new AutoBuy6PM();
		AutoBuyStatus status = auto.login("luyao.huang@yahoo.com", "leixun123456");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.scribeExpress("114-4751822-5849814");
			System.out.println("status = "+ status.getValue());
		}
	}
	
	public AutoBuy6PM(){
		super(false);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("http://www.6pm.com/");

		//点击登录
		try
		{
//			By bySignIn = By.xpath("//a[contains(text(),'Log In or Register')]");
			By bySignIn = By.xpath("//a[@title='Account']");
			WebElement signIn = driver.findElement(bySignIn);
			Utils.sleep(1500);
			logger.debug("--->跳转到登录页面");
			signIn.click();
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
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ap_email']")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.xpath("//input[@id='ap_password']"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.xpath("//input[@id='signInSubmit-input']"));
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
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'Logout')]")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='accountHeader']")));
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
//			WebElement viewCart = driver.findElement(By.xpath("//a[@id='viewCart']"));
			WebElement viewCart = driver.findElement(By.xpath("//a[@title='Shopping Bag']"));
			logger.error("--->开始跳转到购物车");
			Utils.sleep(1500);
			viewCart.click();
		}catch(Exception e){
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
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='remove' and contains(text(),'Remove')]"));
			if(list != null && list.size() > 0){
				logger.error("--->购物车list size="+list.size());
				for(WebElement remove : list){
					Utils.sleep(2000);
					if(remove != null && remove.isDisplayed()){
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
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			logger.error("--->确认购物车是否清理完成");
			WebElement cartBag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".z-hd-beret")));
			logger.debug("--->购物车内容:"+cartBag.getText());
			if(!cartBag.getText().equals("Your shopping bag is empty") && !cartBag.getText().equals("You have 0 items in your bag!")){
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mdl-layout__content")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("root")));
				logger.debug("--->商品页面加载完成");
				Utils.sleep(2000);
			}catch(Exception e1){
				logger.debug("--->等待商品页面加载");
				return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
			}
		}
		
		//商品页面404,商品也是下架
		try{
			driver.findElement(By.xpath("//div[@id='content' and @class='error-page error-404']"));
			logger.debug("--->这款商品的页面404错误,找不到该款商品");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){}
		
		//判断商品是否下架
		try{
			driver.findElement(By.xpath("//div[@class='searchNone']"));
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		//开始选择sku
		logger.debug("--->开始选择sku");
		List<String> skuList = null;
		try{
			skuList = Utils.getSku((String) sku);
//			List<WebElement> elements = driver.findElements(By.xpath("//form[@id='prForm']/ul[@class='wingInfo onSale noFreeShip']/li"));
			List<WebElement> elements = driver.findElements(By.cssSelector("form .VrH5P"));
			int findCount = 0;
			for (int i = 0; i < skuList.size(); i++){
				if (i % 2 == 1){
					for(WebElement element : elements){
						try{
							if(element.isDisplayed()){
								WebElement label = element.findElement(By.xpath(".//label"));
								if(label.getText().toLowerCase().contains(skuList.get(i-1).toLowerCase())){
									findCount++;
									WebElement ele = element.findElement(By.cssSelector("select"));
									try{
										Select select = new Select(ele);
										select.selectByVisibleText(skuList.get(i).trim());
										Utils.sleep(2000);
										logger.debug("--->选择了"+select.getFirstSelectedOption().getText());
										
										if(!select.getFirstSelectedOption().getText().equalsIgnoreCase(skuList.get(i).trim())){
											return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
										}
									}catch(Exception e){
										logger.debug("--->找不到指定的sku no success= "+(skuList.get(i-1)+":"+skuList.get(i)),e);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
									
								}
							}
						}catch(Exception e){
							continue;
						}
					}
				}
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
			List<WebElement> priceElements = driver.findElements(By.cssSelector("._3r_Ou"));
			for(WebElement priceElement:priceElements){
				if(priceElement.isDisplayed()){
					String text = priceElement.getText();
					String productEntityId = param.get("productEntityId");
					if(!Utils.isEmpty(text) && text.startsWith("$") && StringUtil.isNotEmpty(productEntityId)){
						logger.debug("--->找到商品单价 = "+text.substring(1));
		//				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, text.substring(1));
						priceMap.put(productEntityId, text.substring(1));
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		
		//判断商品是否下架
		try{
			Utils.sleep(3000);
			WebElement ppov = driver.findElement(By.xpath("//div[@id='oosPopover']"));
			if(ppov != null && ppov.isDisplayed()){
				logger.debug("--->商品这款sku已经售完");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE; 
			}
		}catch(Exception e){
			logger.debug("--->商品没有售罄,carry on");
		}
		
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			List<WebElement> carts = driver.findElements(By.cssSelector("._1HQVd"));
			for(WebElement cart:carts){
				if(cart.isDisplayed()){
					Utils.sleep(1500);
					String text = cart.getText();
					logger.error("text = "+text);
					if(StringUtil.isNotEmpty(text)){
						text = text.toLowerCase();
						if(text.contains("out of stock")){
							logger.debug("--->商品这款sku已经售完");
							return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
						}
						if(text.contains("add to shopping bag")){
							cart.click();
							logger.debug("--->加购物车成功");
						}
					}else{
						logger.debug("--->加购物车按钮找不到");
						return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@class='z-btn-secondary back' and contains(text(),'Back to Shopping')]")));
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
				logger.debug("--->商品数量 = "+productNum);
				WebElement inputNum = driver.findElement(By.xpath("//input[@type='text' and @name='changeQuantity']"));
				Utils.sleep(2000);
				inputNum.clear();
				Utils.sleep(2000);
				inputNum.sendKeys(productNum);
				Utils.sleep(2000);
//				WebElement upate = driver.findElement(By.xpath("//button[@class='z-btn-primary' and contains(text(),'Update')]"));
				WebElement upate = driver.findElement(By.xpath("//a[@class='changeQuantity' and contains(text(),'Update')]"));
				Utils.sleep(2000);
				upate.click();
				Utils.sleep(4000);
				//等待加载进度条不可见
//				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//span[@id='loader']")));
//				Utils.sleep(2000);
				logger.debug("--->选择商品数量完成");
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		int attnum = 0;
		try {
			WebElement detail = driver.findElement(By.cssSelector(".details"));
			List<WebElement> details = detail.findElements(By.cssSelector("li"));
			for(WebElement w:details){
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						if(w.getText().trim().split(":")[1].trim().toUpperCase().equals(skuList.get(i).toUpperCase())){
							logger.debug("--->属性"+w.getText().trim()+"对的");
							attnum++;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择商品数量碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}
		
		if(attnum < skuList.size()/2){
			logger.debug("--->缺少匹配的sku findCount = "+attnum+" && skuList.size()/2 = "+skuList.size()/2);
			return AutoBuyStatus.AUTO_SKU_NOT_FIND;
		}
		
		//查询商品数量是否可买
		try{
			driver.findElement(By.xpath("//div[@id='jsError']/h3"));
			logger.debug("--->选择商品数量太多，不支付一次性购买这么多件商品");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}catch(Exception e){
			logger.debug("--->商品数量没有问题,carry on");
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
		Set<String> promotionList = getPromotionList(param.get("promotion"));
	
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
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//button[@class='z-btn-cart' and contains(text(),'Proceed to Checkout')]")));
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
					By.xpath("//div[@id='pay']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//选收货地址
		By by = By.xpath("//a[@class='a-button-text checkout-continue-button' and contains(text(),'Ship to this address')]");
		try{
			logger.debug("--->开始寻找address change按钮");
			WebElement gotoShipAddress = driver.findElement(By.xpath("//a[@id='goToShipAddressPanel']"));
			Utils.sleep(1500);
			gotoShipAddress.click();
			Utils.sleep(1500);
			logger.debug("--->点击address change按钮,等待address 打开");
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));	
			logger.debug("--->address打开完成");
			List<WebElement> list = driver.findElements(by);
			if(list != null && list.size() > 0){
//				int tarAddr = count % list.size();
				int tarAddr = count % 4;
				WebElement ele = list.get(tarAddr);
				Utils.sleep(1500);
				ele.click();
				logger.debug("--->点击所选地址,等待address关闭");
				try{
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='goToShipAddressPanel']")));
					logger.debug("--->选择第"+tarAddr+"个地址成功");
					Utils.sleep(2000);
				}catch(Exception e){
					logger.debug("--->所选的的第"+tarAddr+"个地址有问题");
					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
				}
			}else{
				logger.debug("--->该账号没有设置收货地址");
				return AutoBuyStatus.AUTO_PAY_ADDR_INDEX_OVER_MAX;
			}
		}catch(Exception e){
			logger.debug("--->选择地址出错 = ",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		//寻找输入框
		try{
			WebElement input = driver.findElement(By.xpath("//input[@id='addCreditCardNumber']"));
			Utils.sleep(1500);
			logger.debug("--->找到信用卡输入框,开始输入");
			String cardNo = param.get("cardNo");
			logger.debug("--->信用卡卡号是 = "+cardNo);
			if(Utils.isEmpty(cardNo)){
				logger.debug("--->没有找到可用的信用卡卡号");
				return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
			}
			input.sendKeys(cardNo);
			Utils.sleep(1500);
			logger.debug("--->输入信用卡结束");
			//输入信用卡点确定
			WebElement confirm = driver.findElement(By.xpath("//button[@id='confirm-card-announce' and contains(text(),'Confirm Card')]"));
			Utils.sleep(1500);
			confirm.click();
			logger.debug("--->开始等待信用卡确认");
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//button[@id='confirm-card-announce']")));
			logger.debug("--->confirm已经不可见");
			Utils.sleep(3000);
		}catch(Exception e){
			logger.debug("--->没找到信用卡输入框");
		}
		
		//寻找use this payment method
		try{
			WebElement payment = driver.findElement(By.xpath("//input[@id='savePayAndContinueTop']"));
			logger.debug("--->找到use this payment method");
			Utils.sleep(1500);
			payment.click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='goToPayPanel']")));
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->没有找到use this payment method");
		}
		
		//优惠码
		if(promotionList != null && promotionList.size() > 0){
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@id='gc-promo-input']"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//button[@id='redeem-gc-announce']")).click();
						Utils.sleep(5500);
						
						try{
							driver.findElement(By.xpath("//p[@id='gc-success']"));//礼品卡有效
							statusMap.put(code, 10);
							isEffective = true;
						}catch(Exception e){
							logger.error("promotionCode:"+code,e);
							try{
								driver.findElement(By.xpath("//div[@id='gc-error']/div/div/div"));
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
		
		//选择配送方式
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.xpath("//span[@id='subtotals-marketplace-spp-bottom']"));
			String text = totalPriceElement.getText();
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
					if (v.doubleValue() > 5.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
		}
		// 查询优惠
		try {
			logger.debug("--->开始查询优惠");
			List<WebElement> expressElements = driver.findElements(By.cssSelector(".order-summary-unidenfitied-style"));
			BigDecimal totalExpress = new BigDecimal(0); 
			for(WebElement w:expressElements){
				if (!Utils.isEmpty(w.getText()) && w.getText().contains("Shipping") && w.getText().indexOf("$") != -1) {
					String express = w.findElement(By.cssSelector(".a-text-right")).getText().replace("$", "");
					logger.debug("--->[1]找到运费 = " + express);
					BigDecimal x = new BigDecimal(express);
					totalExpress = totalExpress.add(x);
				}
				logger.debug("--->[1]找到总运费 = " + totalExpress);
				data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE,String.valueOf(totalExpress));
			}
		}catch (Exception e) {
			logger.debug("--->查询总运费出现异常=", e);
		}
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			WebElement placeOrderElement = driver.findElement(By.xpath("//button[@id='placeOrder']"));
			Utils.sleep(1500);
			if(isPay){
				logger.debug("--->啊啊啊啊啊，我要付款了");
				placeOrderElement.click();
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){}
		
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
				}else if(status.equalsIgnoreCase("SUBMITTED")){
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
				logger.debug("status："+status);
				if(status.equalsIgnoreCase("cancelled")){
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
				}else if(status.equalsIgnoreCase("customer action required")){
					return AutoBuyStatus.AUTO_SCRIBE_LOGIN_FAIL_NEED_AUTH;
				}else if(status.equalsIgnoreCase("shipped")){
					String trackNo = product.findElement(By.xpath(".//p[@class='tracking']")).getText().replace("Track This Shipment", "").replaceAll("[^0-9A-Za-z]", "");
					logger.debug("trackNo："+trackNo);
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
