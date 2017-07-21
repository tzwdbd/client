package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
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
 * @date 2016年10月25日
 */
//未发货状态待添加
public class DisneystoreAutoBuy extends AutoBuy{
	
	protected final Logger logger = Logger.getLogger(getClass());
	WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
	private  List<String> accountInf = new ArrayList<String>();
	
	public DisneystoreAutoBuy() {
		logger.debug("调整浏览器尺寸和位置");
		driver.manage().window().setSize(new Dimension(414, 736));
		driver.manage().window().setPosition(new Point(0, 0));
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		
		driver.get("https://www.disneystore.com/");
		//跳转到登录页面
		try {
			logger.debug("跳转到登录页面");
			WebElement menue =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='iconMenu']")));
			TimeUnit.SECONDS.sleep(1);
			menue.click();
			TimeUnit.SECONDS.sleep(1);
			
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='signin']")));
			signIn.click();
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			logger.debug("跳转到登录页面失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		//输入用户名和密码，点击登录
		try {
			logger.debug("输入用户名和密码，点击登录");
			WebElement loginIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='loginButton']")));
			WebElement userNameElement = driver.findElement(By.xpath("//input[@id='logonId']"));
			userNameElement.sendKeys(userName);
			TimeUnit.SECONDS.sleep(1);
			WebElement passWordElement = driver.findElement(By.xpath("//input[@id='password']"));
			passWordElement.sendKeys(passWord);
			TimeUnit.SECONDS.sleep(1);
			loginIn.click();
			TimeUnit.SECONDS.sleep(10);
		} catch (Exception e) {
			logger.debug("输入用户名和密码，点击登录遇到异常",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		closePage();
		//登陆成功
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='iconMenu']")));
			logger.debug("登陆成功");
		} catch (Exception e) {
			logger.debug("登录失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		accountInf.add(userName);
		accountInf.add(passWord);
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		try {
			logger.debug("跳转到购物车页面");
			WebElement iconBag = driver.findElement(By.xpath("//a[@id='iconBag']"));
			iconBag.click();
			TimeUnit.SECONDS.sleep(1);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='iconBag']")));
			logger.debug("跳转到购物车成功");
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			logger.debug("跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			logger.debug("清空购物车");
			int i =0;
			while (true) {
				try {
					WebElement emptyBag = driver.findElement(By.xpath("//div[@class='emptyBag']"));
					if(emptyBag.getText().contains("Your bag is currently")){
						logger.debug("购物车空了");
						break;
					}
				} catch (Exception e) {
				}
				logger.debug("删除第"+(++i)+"件商品");
				List<WebElement> list = driver.findElements(By.xpath("//a[@class='cartLink confirmDelete']"));
				list.get(0).click();
				TimeUnit.SECONDS.sleep(1);
				
				WebElement yes = driver.findElement(By.xpath("//a[@class='button primary itemRemove']"));
				yes.click();
				TimeUnit.SECONDS.sleep(5);
				
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='iconBag']")));
			}
		} catch (Exception e) {
			logger.debug("清空购物车出现异常",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("跳转到商品页面");
		String url = param.get("url");
		logger.debug("url:" + url);
		try
		{
			driver.navigate().to(url);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='addToBag']")));
			logger.debug("商品加载完成");
		} catch (Exception e) {
			logger.debug("商品加载失败");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		closePage();
		String sku = param.get("sku");
		if(StringUtil.isNotEmpty(sku)){
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i++) {
				String attrName = skuList.get(i);
				String attrValue = skuList.get(++i);
				if(attrName.equalsIgnoreCase("size")){
					try {
						WebElement sizeElement = driver.findElement(By.xpath("//select[@id='Size_']"));
						Select select = new Select(sizeElement);
						select.selectByValue(attrValue);
						TimeUnit.SECONDS.sleep(1);
						logger.debug("选择size完成："+attrValue);
					} catch (Exception e) {
						logger.debug("选择尺码异常",e);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
				}
			}
		}
		//查询单价
		try {
			WebElement priceElement;
			try {
				priceElement = driver.findElement(By.xpath("//p[@class='productPrice productSalePrice']"));
			} catch (Exception e) {
				priceElement = driver.findElement(By.xpath("//p[@class='productPrice productPrice']"));
			}
			String priceStr = priceElement.getText();
			Matcher m = Pattern.compile("[0-9.]+").matcher(priceStr);
			if(m.find()){
				String price = m.group();
				logger.debug("找到单价:"+price);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
				String productEntityId = param.get("productEntityId");
				if(StringUtil.isEmpty(productEntityId)){
					logger.error("--->productEntityId  is Null");
				}else{
					priceMap.put(param.get("productEntityId"), price);
				}
			}else{
				logger.debug("获取单价失败");
			}
		} catch (Exception e) {
			logger.debug("获取单价失败",e);
		}
		
		String number = param.get("num");
		if(!"1".equalsIgnoreCase(number)){
			try {
				WebElement qty = driver.findElement(By.xpath("//input[@id='qty']"));
				qty.clear();
				qty.sendKeys(number);
				TimeUnit.SECONDS.sleep(1);
				logger.debug("设置数量成功");
			} catch (Exception e) {
				//另外一种数量选择方式
				try {
					WebElement qty = driver.findElement(By.xpath("//select[@id='qty']"));
					Select select = new Select(qty);
					select.selectByValue(number);
					TimeUnit.SECONDS.sleep(1);
					logger.debug("设置数量成功");
				} catch (Exception e2) {
					logger.debug("设置数量失败",e2);
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			}
		}
		
		try {
			WebElement addToCart = driver.findElement(By.xpath("//a[@id='addToBag']"));
			if(addToCart.getText().contains("Sold Out")){
				logger.debug("售罄");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
			addToCart.click();
			logger.debug("点击加入购物车");
			TimeUnit.SECONDS.sleep(10);
			
			/*WebElement cart = driver.findElement(By.xpath("//span[@class='numCartItems primary pushNotification']"));
			if(StringUtil.isEmpty(cart.getText())){
				
			}*/
			closePage();
			
			try {
				logger.debug("skip");
				WebElement skip = driver.findElement(By.cssSelector("a.fullwidth:nth-child(2)"));
				skip.click();
				TimeUnit.SECONDS.sleep(3);
			} catch (Exception e) {
				logger.debug("skip",e);
			}
		} catch (Exception e) {
			logger.debug("加入购物车失败",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}
	@Override
	public AutoBuyStatus pay(Map<String, String> param){
		
		String myPrice = param.get("my_price");
		Boolean isPay = Boolean.valueOf(param.get("isPay"));
		String suffixNo = param.get("suffixNo");
		String isStockpile = param.get("isStockpile");
		String promotionCodeList = param.get("promotion");
		logger.debug("promotionCodeList:"+promotionCodeList);
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		try {
			WebElement cart = driver.findElement(By.cssSelector("#iconBag"));
			cart.click();
			logger.debug("跳转到购物车");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if (promotionList != null && promotionList.size() > 0){
			for (String code : promotionList) {
				logger.debug("code:" + code);
				try {
					WebElement promo = driver.findElement(By.xpath("//input[@id='promo_code']"));
					promo.clear();
					promo.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
					
					WebElement apply = driver.findElement(By.xpath("//a[@id='enter_promo_code']"));
					apply.click();
					TimeUnit.SECONDS.sleep(2);
					
					WebElement contents = driver.findElement(By.xpath("//p[@class='contents alert']"));
					if(contents.getText().contains("is invalid")){
						logger.debug("优惠码无效:"+code);
						statusMap.put(code, 0);
					}else if(contents.getText().contains("has already been used")){
						logger.debug("这个优惠码已经有了啊"+code);
						isEffective = true ;
						statusMap.put(code, 10);
					}else{
						logger.debug(code+"优惠码提示信息无法识别:"+contents.getText());
					}
				} catch (Exception e) {
					logger.debug("优惠码有效:"+code);
					isEffective = true ;
					statusMap.put(code, 10);
				}
				
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		try {
			logger.debug("结算");
			WebElement checkOut = driver.findElement(By.cssSelector("#checkout_button"));
			checkOut.click();
			
			TimeUnit.SECONDS.sleep(3);
			
			WebElement address = driver.findElement(By.cssSelector("#address-list > ul:nth-child(1)"));
			List<WebElement> addrs = address.findElements(By.xpath(".//li"));
			
			logger.debug("--->目前共有[" + addrs.size() + "]个可用地址");
			int index = 0;
			try
			{
				index = Integer.valueOf(count);
				int tarAddr = index % addrs.size();
				WebElement cur = addrs.get(tarAddr);
				cur.click();
				logger.debug("选择第"+tarAddr+"个地址");
				TimeUnit.SECONDS.sleep(2);
				
				logger.debug("确认物流");
				WebElement ctn = driver.findElement(By.cssSelector("button.button:nth-child(1)"));
				ctn.click();
				TimeUnit.SECONDS.sleep(3);
				
			}catch (Exception e){
				logger.debug("选择物流失败",e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}		
		} catch (Exception e) {
			logger.debug("选择物流失败[1]",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			WebElement orderTotal  = driver.findElement(By.xpath("//p[@id='orderTotal']"));
			String ot = orderTotal.getText();
			Matcher m =  Pattern.compile("[0-9.]+").matcher(ot);
			if(m.find()){
				ot = m.group();
				logger.debug("找到总价:"+ot);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, ot);
			}else{
				logger.debug("查询结算总价出现问题");
				return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
			}
			BigDecimal x = new BigDecimal(myPrice);
			BigDecimal y = new BigDecimal(ot);
			BigDecimal v = y.subtract(x);
			if (v.doubleValue() > 5.00D){
				logger.error("--->总价差距超过约定,不能下单");
				return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
		}
		
		
		try {
			WebElement sec = driver.findElement(By.xpath("//div[@class='cvvHolder']/input"));
			sec.sendKeys(suffixNo);
			TimeUnit.SECONDS.sleep(3);
			logger.debug("输入安全码完成");
			
			WebElement ctn = driver.findElement(By.cssSelector("button.button:nth-child(1)"));
			ctn.click();
			TimeUnit.SECONDS.sleep(3);
			logger.debug("点击进行下一步");
			
			
			WebElement ctn1 =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.button:nth-child(1)")));
			if(isPay){
				logger.debug("开始付款");
				ctn1.click();
				logger.debug("付款完成");
			}
		} catch (Exception e) {
			logger.debug("输入安全码出现异常",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			WebElement rcptOrderDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[@id='rcptOrderDetails']")));
			String orderNo = rcptOrderDetails.getText();
			Matcher m  =  Pattern.compile("[0-9]{5,10}").matcher(orderNo);
			if (m.find()) {
				orderNo = m.group();
				logger.debug("找到商城订单号:"+orderNo);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
				savePng();
			}else{
				logger.debug("获取订单号失败");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("获取订单号失败");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String userName = accountInf.get(0);
		
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			logger.debug("mallOrderNo是空的，什么鬼");
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		
		logger.debug("登陆完成，然后关了浏览器重新登录");
		driver.close();
		driver = new FirefoxDriver();
		driver.manage().deleteAllCookies();
		driver.manage().window().maximize();
		driver.get("https://www.disneystore.com/disneystore/account/orderHistory");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		/*try {
			logger.debug("开始登陆");
			TimeUnit.SECONDS.sleep(3); 
			WebElement singIn = driver.findElement(By.xpath("//input[@id='logonIdOrderHistory']"));
			singIn.sendKeys(userName);
			TimeUnit.SECONDS.sleep(3); 
			
			WebElement pwd = driver.findElement(By.xpath("//input[@id='logonPasswordOrderHistory']"));
			pwd.sendKeys(passWord);
			TimeUnit.SECONDS.sleep(1);
			
			WebElement signClick = driver.findElement(By.xpath("//a[@id='signInOrderHistory']"));
			signClick.click();
			TimeUnit.SECONDS.sleep(20);
		} catch (Exception e) {
			logger.debug("登录电脑版失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}*/
		
		try {
			logger.debug("开始查询订单");
			logger.debug("输入订单号");
			WebElement orderNumber =wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='orderNumber']")));
			TimeUnit.SECONDS.sleep(1);
			orderNumber.sendKeys(mallOrderNo);
			TimeUnit.SECONDS.sleep(1);
			logger.debug("输入email");
			WebElement email = driver.findElement(By.xpath("//input[@id='email1']"));
			email.sendKeys(userName);
			TimeUnit.SECONDS.sleep(1);
			logger.debug("点击查找");
			WebElement trackOrder = driver.findElement(By.xpath("//button[@id='trackOrder']"));
			trackOrder.click();
		} catch (Exception e) {
			logger.debug("查询订单出现异常",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			driver.findElement(By.xpath("//div[@class='errorMessageText']"));
		    logger.debug("找不到订单");
		} catch (Exception e) {
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
			logger.debug("跳转到订单页面");
			WebElement element =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='order-list']/table/tbody/tr/td/div/a")));
			element.click();
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			logger.debug("查找订单发生异常",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@class='orderItem']")));
			List<WebElement> list = driver.findElements(By.xpath("//td[@class='itemStatus']"));
			TreeSet<String> trackNo = new TreeSet<String>();
			for (int i = 0; i < list.size(); i++) {
				trackNo.add(list.get(i).getText().replace("#","").trim());
			}
			if(trackNo.size()>1){
				logger.debug("出现了两个物流单号，无法识别");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			logger.debug("物流单号："+trackNo.first());
			if(StringUtil.isEmpty(trackNo.first())){
				logger.debug("未发货");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}else{
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo.first());
			}
		} catch (Exception e) {
			logger.debug("查找物流单号失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	public void closePage(){
		try {
			WebElement element = driver.findElement(By.xpath("//a[@class='fsrCloseBtn']"));
			element.click();
			logger.debug("出现一个广告页，关掉它");
		} catch (Exception e) {
		}
	}
	public static void main(String[] args) {
		DisneystoreAutoBuy autoBuy = new DisneystoreAutoBuy();
		autoBuy.login("menegar@163.com", "tfb001001");
		Map<String, String> param = new LinkedHashMap<>();
		param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=17c6LYWX9xKwVxc2adstgGXo2EqUc_agu826U6QE6Wa_aJauhssDoLyRVCuPXg_akGrsM_bT7A_c_c&new=https%3A%2F%2Fwww.disneystore.com%2Fbodysuits-clothes-angel-bodysuit-costume-for-baby-personalizable%2Fmp%2F1401428%2F1000388%2F&tag=");
		param.put("num", "2");
		param.put("count", "4");
		param.put("isPay", "false");
		param.put("my_price", "13.96");
		param.put("cardNo", "5273380300218539");
		//正常
		param.put("sku", "[[\"size\",\"12-18M\"]]");
		param.put("productEntityId", "12");
		//autoBuy.selectProduct(param);
		//只有一个价格
		param.put("url", "https://m.disneystore.com/charms-jewelry-accessories-mickey-mouse-mickey-swirls-charm-by-pandora/mp/1365764/1010210/?LSID=6110390|10676026|lh_ccqfn");
		param.put("sku", "");
		//autoBuy.selectProduct(param);
		//售罄
		param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=56ccX8rTGWTgSIQMdTE_abfvp_bu90f1xGiMjzejyPZGi9K2NNPS_blkPtAd_bW2cH6mK6_burg_c_c&new=https%3A%2F%2Fwww.disneystore.com%2Fshoes-socks-accessories-angel-costume-shoes-for-baby%2Fmp%2F1414284%2F1000222%2F&tag=");
		param.put("sku", "[[\"size\",\"0-6M\"]]");
		//autoBuy.selectProduct(param);
		//autoBuy.cleanCart();
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("1386652677");
		autoBuy.scribeExpress(detail);
	}
}
