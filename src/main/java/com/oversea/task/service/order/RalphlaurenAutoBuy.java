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
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
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
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			driver.navigate().to("https://www.ralphlauren.com/checkout/index.jsp?process=login&ab=global_ma_signin");
			closeAdv();
		} catch (Exception e) {
			logger.debug("打开首页失败",e);
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		
		logger.debug("开始登陆");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("li#accountsel a")));
			TimeUnit.SECONDS.sleep(1);
			driver.findElementByCssSelector("li#accountsel a").click();
			WebElement account =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailId")));
			TimeUnit.SECONDS.sleep(1);
			account.sendKeys(userName);
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.id("passwd")).sendKeys(passWord);
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.xpath("//form[@name='returningCustomer']")).submit();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Shopping Bag']")));
			logger.debug("登录成功");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		} catch (Exception e) {
			logger.debug("登录失败");
			try {
				TimeUnit.SECONDS.sleep(1);
				driver.findElementByCssSelector("li#accountsel a").click();
				WebElement account =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emailId")));
				TimeUnit.SECONDS.sleep(1);
				account.sendKeys(userName);
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.id("passwd")).sendKeys(passWord);
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.xpath("//form[@name='returningCustomer']")).submit();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@title='Shopping Bag']")));
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
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='remove']"));
			while(null!=list && list.size()!=0){
				list.get(0).click();
				list.remove(0);
				Thread.sleep(1000);
				list = driver.findElements(By.xpath("//a[@class='remove']"));
			}
		} catch (Exception e) {
			logger.debug("清空购物车失败：",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try {
			logger.error("--->确认购物车是否清空");
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("emptyCartCMS")));
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
		String code = "";
		logger.debug("选择商品 productUrl = " + productUrl);
		try {
			driver.navigate().to(productUrl);
			closeAdv();
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("style-num")));
			code = driver.findElementByClassName("style-num").getText().toLowerCase();
		} catch (Exception e) {
			logger.error("打开商品页面失败："+e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		for (int i = 0; i < skuValueList.size(); i++) {
			String skuName = skuValueList.get(i);
			String skuValue = skuValueList.get(++i);
			if("color".equalsIgnoreCase(skuName)){
				logger.debug("开始选择颜色："+skuValue);
				try {
					color = skuValue.toLowerCase();
					driver.findElement(By.xpath("//li[@title='"+skuValue+"']")).click();
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					logger.debug("选择颜色失败");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
				logger.debug("选择颜色完成");
			}else if("size".equalsIgnoreCase(skuName)){
				logger.debug("开始选择大小："+skuValue);
				try {
					size = skuValue.toLowerCase();
					driver.findElement(By.xpath("//li[@title='"+skuValue+"']")).click();
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					logger.debug("选择size失败",e);
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
				logger.debug("选择大小完成");
			}
		}
		try {
			TimeUnit.SECONDS.sleep(2);
			String container = driver.findElement(By.id("waitlist-container")).getText();
			if(StringUtil.isNotEmpty(container) && container.toLowerCase().contains("sorry")){
				logger.debug("售罄啦");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
			String price = driver.findElement(By.xpath("//span[@itemprop='price']")).getText().replaceAll("[^0-9.]", "");
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
		logger.debug("还有的");

		if(!"1".equalsIgnoreCase(productNum)){
			try {
				for (int j = 0; j < Integer.parseInt(productNum) -1; j++) {
					logger.debug("调整数量");
					driver.executeScript("document.querySelector('div.quantity-control.add').click();");
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				logger.debug("增加数量失败",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		try {
			logger.debug("加入购物车");
			TimeUnit.SECONDS.sleep(2);
			driver.executeScript("document.getElementById('addToCart').click();");
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			logger.debug(e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		//去购物车检查了到底有没有加入购物城成功 数量对不对
		boolean find = false;
		try {
			driver.navigate().to("http://www.ralphlauren.com/cart/index.jsp?ab=global_bag");
			TimeUnit.SECONDS.sleep(2);
			List<WebElement> list = driver.findElements(By.xpath("//table[@class='cart']/tbody/tr"));
			for (WebElement webElement : list) {
				String text = webElement.getText().toLowerCase();
				if(text.contains(size) && text.contains(color) && text.contains(code)){
					logger.debug("检查数量："+size+" "+code+" "+color);
					WebElement numIn = webElement.findElement(By.cssSelector(".quantity"));
					if(numIn.getAttribute("value").equalsIgnoreCase(productNum)){
						logger.debug("数量ok");
					}else{
						logger.debug("数量异常");
					}
					find =true;
					break;
				}
			}
		} catch (Exception e) {
			logger.debug(e);
			return AutoBuyStatus.AUTO_ADD_CART_FAIL;
		}
		if(find == false){
			logger.debug("并没有加入购物车");
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
			driver.navigate().to("https://www.ralphlauren.com/checkout/index.jsp?process=poloOrderTrackingDetail&orderId="+mallOrderNo);
			WebDriverWait wait1 = new WebDriverWait(driver, 30);
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.upperTxt.notBold")));
			String text = driver.findElement(By.cssSelector("div.upperTxt.notBold")).getText();
			if(text.toLowerCase().contains("will")){
				logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}else if(text.toLowerCase().contains("shipped")){
				logger.debug("已经发货了");
				Matcher upsMatcher = Pattern.compile("1Z.+").matcher(text);
				Matcher fedExMatcher = Pattern.compile("[0-9]{10,100}").matcher(text.toLowerCase());
				if(upsMatcher.find()){
					String trackNo = upsMatcher.group();
					logger.debug(trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "ups");
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}else if(fedExMatcher.find()){
					String trackNo =  fedExMatcher.group();
					logger.debug(trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "FedEx");
					return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
				}
			}else if(text.toLowerCase().contains("cancel")){
				logger.debug("被砍单了");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
			}else{
				logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
		} catch (Exception e) {
			logger.debug("爬取物流异常");
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
					WebElement input = driver.findElement(By.id("promoCode"));
					input.clear();
					Thread.sleep(100);
					input.sendKeys(code);
					Thread.sleep(100);
					WebElement  apply= driver.findElement(By.id("promoApply"));
					apply.click();
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("promoApply")));
					try {
						driver.findElement(By.cssSelector("p.error.promoError"));
						logger.debug("优惠码不可用");
						statusMap.put(code, 0);
					} catch (Exception e) {
						logger.debug("优惠码可用");
						statusMap.put(code, 10);
						isEffective = true;
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
			driver.executeScript("document.getElementById('proceedToCheckoutBtn').click();");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addressBook")));
			try {
				logger.debug("再次登录");
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.xpath("//form[@id='login']/input[@id='email']")).sendKeys(userName);
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.xpath("//form[@id='login']/input[@id='password']")).sendKeys(pwd);
				TimeUnit.SECONDS.sleep(1);
				driver.findElement(By.xpath("//form[@id='login']/button[@class='submit css-button primary']")).click();;
				TimeUnit.SECONDS.sleep(1);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("right-side")));
			} catch (Exception e) {
				logger.debug(e);
			}
			List<WebElement> trList = driver.findElements(By.xpath("//table[@id='addressBook']/tbody/tr"));
			trList.remove(0);
			Iterator<WebElement> iterator = trList.iterator();
			while(iterator.hasNext()){
				WebElement now = iterator.next();
				if(now.getText().toLowerCase().contains("310000")){
					logger.debug("找到账单地址了");
					WebElement zd = now.findElement(By.xpath(".//td[@class='radio border']/input"));
					zd.click();
					iterator.remove();
				}else if(now.getText().toLowerCase().contains("add a new address") || now.getText().toLowerCase().contains("ship to more than o") ){
					iterator.remove();
				}
			}
			
			int s = trList.size();
			logger.debug("有："+s+"个地址");
			int target = count % s;
			logger.debug("选第"+target+"个地址");
			TimeUnit.SECONDS.sleep(2);
			trList.get(target).findElement(By.xpath(".//td[@class='radio']/input")).click();
			TimeUnit.SECONDS.sleep(1);
			
			logger.debug("继续结算");
			driver.findElement(By.xpath("//button[@class='continue css-button primary']")).click();
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("td.td-gift")));
			
			TimeUnit.SECONDS.sleep(2);
			logger.debug("选择物流");
			driver.executeScript("document.getElementById('shippingBucketsByAddress0.shippingBuckets0.currentShippingOptionId2').click();");
			
			TimeUnit.SECONDS.sleep(2);
			logger.debug("物流选择页继续");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='proceed']/button"))).click();
			
			logger.debug("开始输入安全码");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@class='text pin cIDNum']"))).sendKeys(suffixNo);
			
			logger.debug("安全码页结算");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='proceed']/button"))).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tr.total")));
			String price = driver.findElement(By.cssSelector("tr.total")).getText().replaceAll("[^0-9.]", "");
			logger.debug("price:"+price);
			BigDecimal x = new BigDecimal(myPrice);
			BigDecimal y = new BigDecimal(price);
			BigDecimal v = y.subtract(x);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, price);
			if (v.doubleValue() > 9.00D){
				logger.error("--->总价差距超过约定,不能下单");
				return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
			}
			//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='procee']/a[@class='sendOrder css-button primary']"))).click();;
			
			if(isPay){
				logger.debug("开始付款");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='sendOrder css-button primary']"))).click();
				logger.error("付款成功");
				TimeUnit.SECONDS.sleep(10);
			}
			
			logger.debug("开始获取订单号");
			try {
				String str = driver.findElement(By.cssSelector("h3 span")).getText();
				Matcher m = Pattern.compile("[0-9]{8,15}").matcher(str);
				if(m.find()){
					String mallOrderNo = m.group();
					logger.debug("商城订单号："+mallOrderNo);
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, mallOrderNo);
					savePng();
				}else{
					logger.debug("--->查找商品订单号出现异常");
					return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
				}
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
		autoBuy.login("snhtgb@163.com", "tfb001001");
		//RobotOrderDetail detail = new RobotOrderDetail();
		autoBuy.cleanCart();
//		Map<String, String> param = new HashMap<String, String>();
//		param.put("url", "http://www.ralphlauren.com/product/index.jsp?productId=119352126");
//		param.put("sku", "[[\"size\",\"10 D\"],[\"color\",\"Newport Navy\"]]");
//		param.put("num", "1");
//		autoBuy.selectProduct(param );
		//autoBuy.pay(param);
		//detail.setMallOrderNo("5231709466");
		//autoBuy.scribeExpress(detail );
	}
}
