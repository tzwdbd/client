package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
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
import com.oversea.task.utils.ExpressUtils;
import com.oversea.task.utils.Utils;

/**
 * @author xiong chen
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.service.order
 * @Description:
 * @date 2017年3月8日
 */

public class RalphlaurenAutoBuy extends AutoBuy{
	
	protected final Logger logger = Logger.getLogger(getClass());
	
	public RalphlaurenAutoBuy() {
		super(false);
	}
	
	String userName ="";
	String pwd ="";
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		this.userName =userName;
		pwd=passWord;
		WebDriverWait wait = new WebDriverWait(driver, 45);
		try {
			driver.navigate().to("https://www.ralphlauren.com/checkout/index.jsp?process=login&ab=global_ma_signin");
			closeAdv();
		} catch (Exception e) {
			logger.debug("打开首页失败",e);
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		
		logger.debug("开始登陆");
		driver.executeScript("(function(){window.scrollBy(0,350);})();");
		try {
			TimeUnit.SECONDS.sleep(1);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#dwfrm_login")));
			logger.debug("开始登陆2");
			List<WebElement> accounts = driver.findElements(By.cssSelector("#dwfrm_login input[placeholder='Email Address']"));

			for(WebElement account:accounts){
				if(account.isDisplayed()){
					account.sendKeys(userName);
				}
			}
			TimeUnit.SECONDS.sleep(1);
			List<WebElement> passwords = driver.findElements(By.cssSelector("#dwfrm_login input[placeholder='Password']"));
			for(WebElement password:passwords){
				if(password.isDisplayed()){
					password.sendKeys(passWord);
				}
			}
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.cssSelector(".valid")).submit();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Shopping Bag']")));
			logger.debug("登录成功");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		} catch (Exception e) {
			logger.debug("登录失败");
			try {
				TimeUnit.SECONDS.sleep(1);
				List<WebElement> accounts = driver.findElements(By.cssSelector("#dwfrm_login input[placeholder='Email Address']"));
				for(WebElement account:accounts){
					if(account.isDisplayed()){
						account.sendKeys(userName);
					}
				}
				TimeUnit.SECONDS.sleep(1);
				List<WebElement> passwords = driver.findElements(By.cssSelector("#dwfrm_login input[placeholder='Password']"));
				for(WebElement password:passwords){
					if(password.isDisplayed()){
						password.sendKeys(passWord);
					}
				}
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.cssSelector("button.valid")).click();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".myaccount")));
				logger.debug("登录成功");
				return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
			} catch (Exception e1) {
				logger.debug("登录失败1",e1);
				return AutoBuyStatus.AUTO_LOGIN_SETUP_FAIL;
			}
		}
	}

	@Override
	public AutoBuyStatus cleanCart() {
		try {
			driver.navigate().to("http://www.ralphlauren.com/cart/index.jsp?ab=global_bag");
			logger.debug("开始清空购物车");
			List<WebElement> list = driver.findElements(By.cssSelector(".remove-item"));
			while(null!=list && list.size()!=0){
				list.get(0).click();
				list.remove(0);
				Thread.sleep(1000);
				list = driver.findElements(By.cssSelector(".remove-item"));
			}
		} catch (Exception e) {
			logger.debug("清空购物车失败：",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try {
			logger.error("--->确认购物车是否清空");
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-empty")));
		} catch (Exception e) {
			logger.debug("--->购物车不为空！");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("跳转到商品页面");
		String productUrl = (String) param.get("url");
		String sku = param.get("sku");
		List<String>  skuValueList = Utils.getSku(sku);
		String productNum = (String) param.get("num");
		String color="";
		String size="";
		//String code = "";
		WebDriverWait wait = new WebDriverWait(driver, 45);
		logger.debug("选择商品 productUrl = " + productUrl);
		try {
			driver.navigate().to(productUrl);
			//closeAdv();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pdpMain")));
			//code = driver.findElementByClassName("style-num").getText().toLowerCase();
		} catch (Exception e) {
			logger.error("打开商品页面失败："+e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		int skuNum = 0;
		try {
			for (int i = 0; i < skuValueList.size(); i++) {
				String skuName = skuValueList.get(i);
				String skuValue = skuValueList.get(++i);
				logger.error("skuName = "+skuName +" && skuValue = "+skuValue);
				List<WebElement> skus =  driver.findElements(By.cssSelector("li.attribute"));
				logger.error("skus size= "+skus.size() );
				for(WebElement w:skus){
					WebElement value = w.findElement(By.cssSelector(".value"));
					logger.error("value text= "+value.getText() );
					if(value.getText().toLowerCase().contains(skuName.toLowerCase())){
						WebElement attribute = w.findElement(By.cssSelector(".attribute-list"));
						try {
							wait.until(ExpectedConditions.elementToBeClickable(attribute));
						} catch (Exception e) {
							w.click();
						}
						TimeUnit.SECONDS.sleep(1);
						List<WebElement> lis = w.findElements(By.cssSelector(".selectable"));
						logger.error("lis size= "+lis.size() );
						for(WebElement l:lis){
							if(l.isDisplayed()){
								WebElement aw = l.findElement(By.cssSelector("a"));
								if("color".equalsIgnoreCase(skuName)){
									WebElement imgs = aw.findElement(By.cssSelector("img"));
									if(imgs.getAttribute("alt").equalsIgnoreCase(skuValue)){
										imgs.click();
										TimeUnit.SECONDS.sleep(1);
										skuNum++;
										break;
									}
								}else{
									logger.error("aw text= "+aw.getText() );
									if(aw.getText().equalsIgnoreCase(skuValue)){
										aw.click();
										TimeUnit.SECONDS.sleep(1);
										skuNum++;
										break;
									}
								}
								
							}
						}
						break;
					}
				}
			}
			int t = skuValueList.size()/2;
			if(skuNum < t){
				logger.error("findCount = "+skuNum +" && skuList.size() = "+t);
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
		} catch (Exception e) {
			logger.error("选择sku异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
			String price = driver.findElement(By.cssSelector(".price-sales")).getText().trim().substring(1);
			logger.debug("单价："+price);
			
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
			String productEntityId = param.get("productEntityId");
			if(StringUtil.isEmpty(productEntityId)){
				logger.error("--->productEntityId  is Null");
			}else{
				priceMap.put(param.get("productEntityId"), price);
			}
		} catch (Exception e) {
			logger.debug(e);
		}
		

		if(!"1".equalsIgnoreCase(productNum)){
			WebElement s = driver.findElement(By.id("Quantity"));
			Select select = new Select(s);
			select.selectByVisibleText(productNum);
			logger.debug("选择了:"+productNum);
		}
		
		try {
			logger.debug("加入购物车");
			TimeUnit.SECONDS.sleep(2);
			driver.executeScript("document.getElementById('add-to-cart').click();");
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.cssSelector(".mini-cart-link-checkout")).click();
		} catch (Exception e) {
			logger.debug(e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		//去购物车检查了到底有没有加入购物城成功 数量对不对
		try {
			TimeUnit.SECONDS.sleep(2);
			WebElement webElement = driver.findElement(By.cssSelector(".quantityinput"));
			Select select = new Select(webElement);
			List<WebElement> op = select.getOptions();
			for(WebElement o:op){
				if(o.isSelected()){
					logger.debug("select:"+o.getText());
					if(!o.getText().equals(productNum)){
						logger.debug("--->选择商品数量碰到异常");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
			}
		} catch (Exception e) {
			logger.debug(e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		logger.debug(mallOrderNo);
		try {
			driver.navigate().to("https://www.ralphlauren.com/orders");
			WebDriverWait wait1 = new WebDriverWait(driver, 30);
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-history-header")));
			List<WebElement> ww = driver.findElements(By.cssSelector(".order-history-header"));
			for(WebElement w:ww){
				WebElement order = w.findElement(By.cssSelector(".order-number"));
				WebElement orderStatus = w.findElement(By.cssSelector(".order-status"));
				if(order.getText().contains(mallOrderNo)){
					if(orderStatus.getText().contains("Awaiting")){
						logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(orderStatus.getText().contains("Cancelled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}
					w.findElement(By.cssSelector("button")).click();
					wait1.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".trackingnumber")));
					WebElement trackingnumber = driver.findElement(By.cssSelector(".trackingnumber"));
					String expressNo = ExpressUtils.regularExperssNo(trackingnumber.getText());
					String[] expressGroup = expressNo.split(",");
					for(String s:expressGroup){
						if(!s.startsWith("T")){
							expressNo = s;
							break;
						}
					}
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
					logger.error("expressCompany =UPS " );
					logger.error("expressNo = " + expressNo);
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}
			}
			
		} catch (Exception e) {
			logger.debug("爬取物流异常",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		return true;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		
		String suffixNo = param.get("suffixNo").replaceAll("[^0-9]", "").trim();
		String myPrice = param.get("my_price");
		logger.debug("myPrice="+myPrice);
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		boolean isEffective = false;
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();		
		logger.debug("优惠码");
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0) {
			for (String code : promotionList) {
				try {
					WebElement input = driver.findElement(By.id("dwfrm_billing_couponCode"));
					input.clear();
					Thread.sleep(100);
					input.sendKeys(code);
					Thread.sleep(100);
					WebElement  apply= driver.findElement(By.id("add-coupon"));
					apply.click();
					try {
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".discount")));
						logger.debug("优惠码可用");
						statusMap.put(code, 10);
						isEffective = true;
					} catch (Exception e) {
						logger.debug("优惠码不可用");
						statusMap.put(code, 0);
					}
				} catch (Exception e) {
					logger.debug("输入优惠码异常");
					statusMap.put(code, 0);
				}
			}
			setPromotionCodelistStatus(statusMap);
			System.out.println(statusMap.toString());
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
	
		try {
			logger.debug("开始付款");
			
			WebElement gotopay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[name='dwfrm_cart_checkoutCart']")));
			gotopay.click();
//			try {
//				logger.debug("再次登录");
//				TimeUnit.SECONDS.sleep(1);
//				driver.findElement(By.xpath("//form[@id='login']/input[@id='email']")).sendKeys(userName);
//				TimeUnit.SECONDS.sleep(1);
//				driver.findElement(By.xpath("//form[@id='login']/input[@id='password']")).sendKeys(pwd);
//				TimeUnit.SECONDS.sleep(1);
//				driver.findElement(By.xpath("//form[@id='login']/button[@class='submit css-button primary']")).click();;
//				TimeUnit.SECONDS.sleep(1);
//				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("right-side")));
//			} catch (Exception e) {
//				logger.debug(e);
//			}
			WebElement shipping = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[name='dwfrm_singleshipping_shippingAddress_save']")));
			shipping.click();
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".creditCardListPayment")));
				WebElement card = driver.findElement(By.cssSelector(".creditCardListPayment"));
				card.click();
			} catch (Exception e) {
				logger.error("--->没有绑定信用卡",e);
				return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_FAIL;
			}
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-value")));
			String price = driver.findElement(By.cssSelector(".order-value")).getText().substring(1);
			logger.debug("price:"+price);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, price);
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
				if (v.doubleValue() > 9.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
			
			if(isPay){
				logger.debug("开始付款");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".button-fancy-large"))).click();
				logger.error("付款成功");
				TimeUnit.SECONDS.sleep(10);
			}
			
			logger.debug("开始获取订单号");
			try {
				String mallOrderNo = driver.findElement(By.cssSelector(".order-number .value")).getText();
				logger.debug("商城订单号："+mallOrderNo);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, mallOrderNo);
				savePng();
			} catch (Exception e) {
				logger.debug("查找商品订单号出现异常");
				logger.debug("整个html:"+driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text"));
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
			
		} catch (Exception e) {
			logger.debug(e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	void closeAdv(){
		try {
			driver.findElement(By.xpath("//a[@class='monetate_lightbox_close_0']")).click();
		} catch (Exception e) {
		}
	}
	public static void main(String[] args) {
		RalphlaurenAutoBuy autoBuy = new RalphlaurenAutoBuy();
		autoBuy.login("nmbwoc@163.com", "tfb001001");
//		RobotOrderDetail detail = new RobotOrderDetail();
//		autoBuy.cleanCart();
////		Map<String, String> param = new HashMap<String, String>();
////		param.put("url", "http://www.ralphlauren.com/product/index.jsp?productId=119352126");
////		param.put("sku", "[[\"size\",\"10 D\"],[\"color\",\"Newport Navy\"]]");
////		param.put("num", "1");
////		autoBuy.selectProduct(param );
//		//autoBuy.pay(param);
//		detail.setMallOrderNo("5230585745");
//		detail.setProductUrl("http://www.ralphlauren.com/product/index.jsp?productId=127749496");
//		detail.setProductSku("[[\"color\",\"Polo Black\"],[\"size\",\"L\"]]");
//		autoBuy.scribeExpress(detail );
	}
}
