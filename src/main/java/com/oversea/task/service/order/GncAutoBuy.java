package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
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
 * @date 2016年10月12日
 */

public class GncAutoBuy extends AutoBuy{
	
	private final Logger logger = Logger.getLogger(getClass());
	String uname;
	String passwd;
	
	public GncAutoBuy() {
		super(false);
		driver.manage().window().setSize(new Dimension(414, 736));
		driver.manage().window().setPosition(new Point(0, 0));
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		driver.get("https://m.gnc.com");
		
		try {
			logger.debug("开始登陆");
			WebElement menue = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//div[@class='sprites-menu-new']")));
			menue.click();
			TimeUnit.SECONDS.sleep(5);
			
			logger.debug("跳转到登录页面");
			WebElement logIn = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//div[@class='sprites-signin']")));
			logIn.click();
			TimeUnit.SECONDS.sleep(5);
			logger.debug("跳转到登录页面完成");
			
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		uname = userName;
		passwd = passWord;
		try {
			WebElement emailId = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//input[@id='emailId']")));
			emailId.sendKeys(userName);
			logger.debug("输入账号："+userName);
			TimeUnit.SECONDS.sleep(1);
			
			WebElement passwd = driver.findElement(By.xpath("//input[@id = 'passwd']"));
			passwd.sendKeys(passWord);
			logger.debug("输入密码："+passWord);
			TimeUnit.SECONDS.sleep(1);
			
		} catch (Exception e) {
			logger.error("输入账号密码遇到错误",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try {
			WebElement emailId = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Sign In')]")));
			emailId.click();
			logger.error("点击登录");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.error("登录失败",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try {
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//div[@class='sprites-cart']")));
			logger.debug("登陆成功");
		} catch (Exception e) {
			logger.debug("登录失败",e);
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		logger.debug("跳转到购物车");
		try {
			WebElement cart = driver.findElement(By.xpath("//div[@class='sprites-cart']"));
			cart.click();
			logger.debug("跳转到购物车成功");
		} catch (Exception e) {
			logger.debug("跳转到购物车失败", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			logger.debug("等待加载购车");
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.cssSelector(".sprites-search-white")));
			logger.debug("购物车加载成功");
		} catch (Exception e) {
			logger.debug("加载购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		
		logger.error("开始加载购物车");
		try {
			List<WebElement> productList = driver.findElements(By.xpath("//a[@data-og-event='removeFromCart']"));
			logger.debug("购物车中有"+productList.size()+"件商品");
			
			boolean flag = true;
			int i = 0;
			
			while(flag) {
				try {
					logger.debug("开始删除第"+i+"轮删除商品");
					List<WebElement> productNow = driver.findElements(By.xpath("//a[@data-og-event='removeFromCart']"));
					if(productNow.size()==0){
						logger.debug("购物车确实清空了");
						TimeUnit.SECONDS.sleep(10);
						break;
					}
					productNow.get(0).click();
					TimeUnit.SECONDS.sleep(10);
					logger.debug("第"+(++i)+"轮商品删除成功");
					
					wait.until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("//div[@class='sprites-cart']")));
					
				} catch (Exception e) {
					logger.error("第" + i + "轮删除商品错误", e);
					return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("清空购物车失败",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		logger.debug("跳转到商品页面");
		String productUrl = (String) param.get("url");
		productUrl = productUrl.replace("www.gnc.com", "m.gnc.com");
		logger.debug("选择商品 productUrl = " + productUrl);
		
		try
		{
			driver.navigate().to(productUrl);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.debug("打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		validate();
		
		logger.debug("等待商品页面加载完成");
		try {
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.cssSelector(".add-to-cart")));
			logger.debug("商品页面加载完成");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		/*logger.debug("判断商品是否下架");
		try {
			WebElement inStock =  driver.findElement(By.cssSelector(".avail-message"));
			if(inStock.getText().equalsIgnoreCase("In Stock")){
				logger.debug("商品没有下架");
			}else{
				logger.debug("商品已经下架了");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}*/
		
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		
		
		driver.executeScript("(function(){window.scrollBy(0,550);})();");
		// 开始选择sku
		logger.debug("--->开始选择sku");
		
		try {
			WebElement skuEdit = driver.findElement(By.cssSelector(".product-dropdown-arrow"));
			List<String> skuList = Utils.getSku(sku);
			skuEdit.click();
			TimeUnit.SECONDS.sleep(3);
			
			if(sku.contains("flavor")){
				logger.debug("[1]开始选择sku");
				String attrVal = skuList.get(skuList.indexOf("flavor")+1);
				try {
					logger.debug("点击sku选择按钮");
					List<WebElement> skuEles = driver.findElements(By.xpath("//ul[@class='product-flavor-options']/li"));
					for (int i = 0; i < skuEles.size(); i++) {
						String skuTxt = skuEles.get(i).findElement(By.xpath(".//span")).getText();
						if(skuTxt.equalsIgnoreCase(attrVal)){
							skuEles.get(i).click();
							TimeUnit.SECONDS.sleep(3);
							logger.debug("sku选择完成");
							break;
						}
						if(i==skuEles.size()-1){
							logger.debug("没找到sku");
							return AutoBuyStatus.AUTO_SKU_NOT_FIND;
						}
					}
					
				} catch (Exception e) {
					logger.debug("sku选择错误",e);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}else{
				logger.debug("[2]开始选择sku");
				String attrVal = skuList.get(skuList.indexOf("size")+1);
				try {
					List<WebElement> skuEles = driver.findElements(By.xpath("//ul[@class='product-size-options']/li"));
					logger.debug("选择sku");
					for (int i = 0; i < skuEles.size(); i++) {
						WebElement li = skuEles.get(i);
						List<WebElement> span = li.findElements(By.xpath(".//span"));
						if(span.get(0).getText().contains(attrVal)){
							li.click();
							logger.debug("选择sku完成");
							TimeUnit.SECONDS.sleep(10);
							break;
						}
						if(i==skuEles.size()-1){
							logger.debug("没找到sku");
							return AutoBuyStatus.AUTO_SKU_NOT_FIND;
						}
					}
				} catch (Exception e) {
					logger.debug("没找到sku");
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}
		} catch (Exception e) {
			logger.debug("[3]开始选择sku");
			logger.debug("没有sku按钮，直接购买");
		}
		
		
		
		// 寻找商品单价
		try {
			WebElement priceEl = driver.findElement(By.xpath("//div[contains(text(),'Member Price')]"));
			Matcher m =  Pattern.compile("[0-9.]+").matcher(priceEl.getText());
			if (m.find()) {
				String price = m.group();
				logger.debug("找打商品单价"+price);
				String productEntityId = param.get("productEntityId");
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
				priceMap.put(productEntityId, price);
			}else{
				logger.error("--->获取单价失败");
			}
		} catch (Exception e) {
			logger.error("--->获取单价失败",e);
		}
		
		// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			try {
				logger.debug("修改数量");
				WebElement numEle = driver.findElement(By.xpath("//input[@id = 'qty_0']"));
				numEle.clear();
				numEle.sendKeys(productNum);
				TimeUnit.SECONDS.sleep(5);
				logger.debug("修改数量成功");
			} catch (Exception e) {
				logger.debug("修改数量错误",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		// 加购物车
		logger.debug("开始加购物车");
		try {
			WebElement addCart = driver.findElement(By.cssSelector(".add-to-cart"));
			addCart.click();
			TimeUnit.SECONDS.sleep(5);
			logger.debug("加入购物车成功");
		} catch (Exception e) {
			logger.debug("加入购物车失败", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		
		try {
			logger.debug("跳转到购物车");
			WebElement gotoCart = driver.findElement(By.xpath("//span[@class='mc-go-to-cart']/a"));
			gotoCart.click();
		} catch (Exception e) {
			logger.debug("跳转到购物车失败");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}
		
		logger.debug("等待跳转到购物车");
		try {
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//div[@class='sprites-cart']")));
			logger.debug("跳转到购物车成功");
		} catch (Exception e) {
			logger.debug("跳转到购物车失败", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		logger.debug("判断库存是否充足");
		try {
			WebElement error= driver.findElement(By.cssSelector(".error"));
			if(error.getText().contains("The quantity you selected")){
				logger.debug("库存不足");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("库存充足");
		}
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		String isStockpile = param.get("isStockpile");
		String cNo = param.get("cardNo");
		String code = param.get("suffixNo");
		logger.debug("信用卡卡号是 = " + code);
		if (Utils.isEmpty(code)) {
			logger.debug("没有找到可用的信用卡安全码");
		}
		String promotionCodeList = param.get("promotion");
		logger.debug("promotionCodeList:"+promotionCodeList);
		List<String> promotionList = Arrays.asList(promotionCodeList.split(";"));
		
		String expiryDate = param.get("expiryDate");
		logger.debug("expiryDate:"+expiryDate);
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		logger.debug("跳转到购物车");
		try {
			WebElement cart = driver.findElement(By.xpath("//div[@class='sprites-cart']"));
			cart.click();
			logger.debug("点击跳转到购物车按钮");
		} catch (Exception e) {
			logger.debug("跳转到购物车失败", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			WebElement gotoPay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@id='proceedToCheckoutForm']")));
			logger.debug("购物车页面加载完成"); 
			TimeUnit.SECONDS.sleep(5);
			logger.debug("开始跳转到结算页面");
			gotoPay.click();
		} catch (Exception e) {
			logger.debug("跳转到购物车失败", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try {
			TimeUnit.SECONDS.sleep(2);
			WebElement login = driver.findElement(By.xpath("//input[@id='emailLogin']"));
			login.sendKeys(uname);
			TimeUnit.SECONDS.sleep(2);
			WebElement pwd = driver.findElement(By.xpath("//input[@id='passwordLogin']"));
			pwd.sendKeys(passwd);
			TimeUnit.SECONDS.sleep(2);
			WebElement btn = driver.findElement(By.xpath("//button[@class='sign-in-button mw_btn1']"));
			btn.click();
			TimeUnit.SECONDS.sleep(2);
		} catch (Exception e) {
			logger.debug("登录失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		boolean isNewAccount = false;
		//判断是不是新账号
		try {
			logger.debug("开始判断是不是新账号");
			TimeUnit.SECONDS.sleep(5);
			driver.findElement(By.xpath("//input[@id='paymentMethod.ccvNumber']"));
		} catch (Exception e) {
			isNewAccount = true;
			logger.debug("是新的账号，需要添加信用卡");
		}
		
		if(isNewAccount){
			String[] ex = expiryDate.split(" ");
			
			try {
				logger.debug("开始输入信用卡信息");
				TimeUnit.SECONDS.sleep(3);
				WebElement ccNumber = driver.findElement(By.xpath("//input[@id='cc-number']"));
				ccNumber.sendKeys(cNo.trim());
				TimeUnit.SECONDS.sleep(3);
				
				logger.debug("选择月份");
				WebElement month = driver.findElement(By.xpath("//select[@id='cardExpDateMo']"));
				Select select = new Select(month);
				select.selectByIndex(Integer.parseInt(ex[1])-1);
				logger.debug("选择月份完成");
				TimeUnit.SECONDS.sleep(3);
				
				logger.debug("选择年份");
				WebElement year = driver.findElement(By.xpath("//select[@id='cardExpDateYr']"));
				Select select2 = new Select(year);
				select2.selectByValue(ex[0]);
				logger.debug("选择年份完成");
				TimeUnit.SECONDS.sleep(3);
				
				logger.debug("开始输入安全码");
				WebElement cw = driver.findElement(By.xpath("//input[@id='ccPin']"));
				cw.sendKeys(code);
				logger.debug("输入安全码完成");
				TimeUnit.SECONDS.sleep(3);
				
				logger.debug("开始确认");
				List<WebElement> list = driver.findElements(By.xpath("//div[@class='sprite continue mw_btn1']"));
				list.get(0).click();
				logger.debug("确认完成");
				TimeUnit.SECONDS.sleep(3);
			} catch (Exception e) {
				logger.debug("添加新卡异常");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}
		try {
			logger.debug("等待结算页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='submit-order-top']")));
			logger.debug("结算页面加载完成");
		} catch (Exception e) {
			logger.debug("结算页面加载失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		
		//选择地址
		logger.debug("--->选择地址");
		try {
			WebElement editAdd = driver.findElement(By.xpath("//h4[@class='subtitle mw_shipping_addr']/a"));
			editAdd.click();
			TimeUnit.SECONDS.sleep(5);
			WebElement billingAddressEle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='billingAddress.address.address1']")));
			String billingAddress = billingAddressEle.getAttribute("value");
			logger.debug("billingAddressEle："+billingAddress);
			
			List<WebElement> addList = driver.findElements(By.xpath("//li[@class='address']"));
			Iterator<WebElement> it = addList.iterator();
			while(it.hasNext()){
				WebElement address = it.next();
				if(address.getText().contains(billingAddress)){
					it.remove();
				}
			}
			
			logger.debug("共有"+addList.size()+"个地址可选");
			
			int index = Integer.parseInt(countTemp)%addList.size();
			logger.debug("选择第"+index+"个地址");
			WebElement aimAddress = addList.get(index);
			logger.debug(aimAddress.getText());
			WebElement useAddress = aimAddress.findElement(By.xpath(".//li[@class='use-as use-as-shipping sprite ']"));
			useAddress.click();
			logger.debug("选择地址成功");
			
			TimeUnit.SECONDS.sleep(5);
			//这里网页上有个坑
			WebElement shipaddresscity = driver.findElement(By.xpath("//select[@id='shippingAddress.address.state']"));
			shipaddresscity.click();
			TimeUnit.SECONDS.sleep(3);
			shipaddresscity.click();
			//跳出了坑
			
			WebElement ctn = driver.findElement(By.xpath("//div[@class='continue-wrap']"));
			logger.debug(ctn.getText());
			ctn.click();
			
			try {
				logger.debug("等待地址页面加载");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='submit-order-top']")));
				logger.debug("地址确认完成");
			} catch (Exception e) {
				logger.debug("地址确认完成失败",e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
			
		} catch (Exception e) {
			logger.debug("物流选择失败",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		if(promotionList.size()>0 && StringUtil.isNotEmpty(promotionList.get(0))){
			try {
				logger.debug("开始输入优惠码");
				WebElement codes = driver.findElement(By.xpath("//div[@class='gold-card-promobox mw-promo-margins']"));
				codes.click();
				TimeUnit.SECONDS.sleep(5);
				
				WebElement codeinput = driver.findElement(By.xpath("//input[@id='promoCode']"));
				codeinput.sendKeys(promotionList.get(0));
				logger.debug("优惠码输入完成");
			
				WebElement submit = driver.findElement(By.xpath("//button[@id='applyPromoCode']"));
				submit.click();
				logger.debug("确认优惠码完成");
				
			} catch (Exception e) {
				logger.debug("优惠码输入错误",e);
			}
			
			try {
				driver.findElement(By.xpath("//div[@class='error mw-padding mw-error-msg']"));
				logger.debug("优惠码不能用");
				if(isStockpile.equals("true")){
					logger.debug("囤货订单，优惠码不能用，停止下单");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			} catch (Exception e) {
				logger.debug("优惠码ok");
			}
		}
		
		try {
			logger.debug("等待结算页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='submit-order-top']")));
			logger.debug("结算页面加载完成");
		} catch (Exception e) {
			logger.debug("结算页面加载失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			WebElement cardNo = driver.findElement(By.xpath("//input[@id='paymentMethod.ccvNumber']"));
			logger.debug("找到信用卡输入框,开始输入");
			cardNo.sendKeys(code);
			TimeUnit.SECONDS.sleep(2);
			logger.debug("输入信用卡安全码结束");
			
		} catch (Exception e) {
			logger.debug("输入信用卡安全码错误",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		//查询总价
		try {
			WebElement priceEl = driver.findElement(By.xpath("//tr[@class='sub-total']"));
			Matcher matcher= Pattern.compile("[0-9.]+").matcher(priceEl.getText());
			if(matcher.find()){
				String price = matcher.group();
				logger.debug("找到商品总价"+price);
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
					BigDecimal v = x.subtract(y);
					double m = Math.abs(v.doubleValue());
					if (m > 20.00D) {
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}else{
				logger.debug("查询总价错误");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		} catch (Exception e) {
			logger.debug("查询总价错误");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		try {
			logger.debug("等待结算页面加载");
			WebElement submit = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='submit-order-top']")));
			logger.debug("开始付款");
			submit.click();
		} catch (Exception e) {
			logger.debug("结算页面加载失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		int status = 0;
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement cw =  driver.findElement(By.xpath("input[@id='ccPin']"));
			cw.sendKeys(code);
			status = 3;
		} catch (Exception e) {

		}
		
		try {
			logger.debug("点击continue");
			TimeUnit.SECONDS.sleep(5);
			WebElement ctn = driver.findElement(By.xpath("//button[@class='continue img mw_btn2']"));
			TimeUnit.SECONDS.sleep(2);
			ctn.click();
			status = 2;
		} catch (Exception e) {
			logger.debug("点击ctn异常");
		}
		
		try {
			logger.debug("判断是否需要返回重新付款");
			TimeUnit.SECONDS.sleep(10);
			driver.findElement(By.xpath("//a[contains(text(),'取消')]"));
			status =1;
		} catch (Exception e) {
		}
		
		logger.debug("status="+status);
		if(status == 3){
			try {
				WebElement cw = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("input[@id='ccPin']")));
				cw.sendKeys(code);
				List<WebElement> list = driver.findElements(By.xpath("//div[@class='sprite continue mw_btn1']"));
				list.get(0).click();
				logger.debug("点击继续");
				TimeUnit.SECONDS.sleep(5);
				WebElement submit = driver.findElement(By.xpath("//div[@id='submit-order-top']"));
				logger.debug("开始付款");
				if(isPay){
					submit.click();
					logger.debug("付款成功");
				}
			} catch (Exception e) {
				logger.debug("付款异常：【3】",e);
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		if(status==0){
			for (int i = 0; i < 3; i++) {
				try {
					WebElement cw = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ccPin']")));
					cw.sendKeys(code);
					logger.debug("输入了安全码");
					List<WebElement> list = driver.findElements(By.xpath("//div[@class='sprite continue mw_btn1']"));
					list.get(0).click();
					logger.debug("点击继续");
					TimeUnit.SECONDS.sleep(5);
					WebElement submit = driver.findElement(By.xpath("//div[@id='submit-order-top']"));
					logger.debug("开始付款");
					if(isPay){
						submit.click();
						logger.debug("付款成功");
						try {
							TimeUnit.SECONDS.sleep(5);
							driver.findElement(By.xpath("//p[@id='your-order-number']"));
							break;
						} catch (Exception e) {
							logger.debug("返回去了，再次重新付款");
						}
					}
				} catch (Exception e) {
					logger.debug("付款异常：【0】",e);
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		}
		
		if(status==1){
			try {
				TimeUnit.SECONDS.sleep(10);
				WebElement cancle = driver.findElement(By.xpath("//a[contains(text(),'取消')]"));
				cancle.click();
				logger.debug("点击取消验证码");
				TimeUnit.SECONDS.sleep(5);
				WebElement cw = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ccPin']")));
				cw.sendKeys(code);
				logger.debug("输入了安全码");
				List<WebElement> list = driver.findElements(By.xpath("//div[@class='sprite continue mw_btn1']"));
				list.get(0).click();
				logger.debug("点击继续");
				TimeUnit.SECONDS.sleep(5);
				WebElement submit = driver.findElement(By.xpath("//div[@id='submit-order-top']"));
				logger.debug("开始付款");
				if(isPay){
					submit.click();
					logger.debug("付款成功");
				}
			} catch (Exception e) {
				logger.debug("付款异常：【1】",e);
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		
		try {
			WebElement order = driver.findElement(By.xpath("//p[@id='your-order-number']"));
			Matcher m = Pattern.compile("[0-9]+").matcher(order.getText());
			if(m.find()){
				String orderNo = m.group();
				logger.debug("找到订单号"+orderNo);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNo);
				savePng();
			}else{
				logger.debug("没有找到订单号");
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
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			driver.navigate().to("https://m.gnc.com/checkout/index.jsp?process=orderTracking");
			//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'Recent Orders')]")));
			logger.debug("跳转到订单详情页成功");
			
		} catch (Exception e) {
			logger.debug("跳转到订单详情页失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			List<WebElement> orders =driver.findElements(By
					.xpath("//div[@class=' mw_was_table mw-main-table mw-recent-orders-container']/div[@class=' mw_was_tr']"));
			logger.debug("找到"+orders.size()+"笔订单");
			boolean flag = false;
			for (int i = 0; i < orders.size(); i++) {
				WebElement order = orders.get(i);
				List<WebElement> details = order.findElements(By.xpath(".//div"));
				String orderNo = details.get(1).getText();
				logger.debug("订单号："+orderNo);
				
				if(orderNo.equals(mallOrderNo)){
					flag=false;
					String status = details.get(4).getText();
					if(status.contains("Shipped on")){
						Matcher m = Pattern.compile("[0-9]{5,100}").matcher(status);
						if(m.find()){
							String expressNo = m.group();
							logger.debug("找到物流单号："+expressNo);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							flag = true;
						}else{
							logger.debug("找不到物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						
						String expressCompanyDetail = details.get(3).getText();
						String expressCompany = "";
						//TODO
						if(expressCompanyDetail.contains("Global Mail")){
							expressCompany = "USPS";
						}else if(expressCompanyDetail.contains("FedEx")){
							expressCompany = "fedex";
						}else{
							logger.debug("无法识别的物流公司");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
						logger.debug("物流公司:"+expressCompany);
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}else if(status.contains("located in stock")){
						logger.error("该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else{
						logger.debug("无法识别的状态："+status);
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}
					
				}
			}
			if(flag == false){
				logger.debug("找不到这个订单");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		} catch (Exception e) {
			logger.debug("爬物流失败");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
	}

	public void validate(){
		try {
			WebElement valiElement = driver.findElement(By.xpath("//a[@class = 'fsrDeclineButton']"));
			valiElement.click();
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
		}
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
	public static void main(String[] args) {
		//47.88.76.37
		GncAutoBuy autoBuy =  new GncAutoBuy();
		//autoBuy.login("xiaoy2@outlook.com", "tfb001001");
		//autoBuy.cleanCart();
		Map<String, String> param = new LinkedHashMap<>();
		param.put("url", "http://www.gnc.com/GNC-Pro-Performance-100-Whey-Protein-Chocolate-Supreme/product.jsp?productId=41784826");
		param.put("url", "http://www.gnc.com/GNC-Herbal-Plus-Grape-Seed-Extract-100-mg/product.jsp?productId=112169976&affiliateCustomId=3580304&affiliateId=103250&c3ch=Affliate&c3nid=103250&clickId=1745125653&eesource=PJ_AD%3AZ%3AGNC&flavourID=19023656");
		//有sku的链接  http://m.gnc.com/GNC-Pro-Performance-100-Whey-Protein-Cookies-Cream/product.jsp?productId=101176366&flavourID=13262311
		//http://m.gnc.com/GNC-Herbal-Plus-Grape-Seed-Extract-100-mg/product.jsp?productId=112169976&flavourID=19023656
		param.put("num", "2");
		param.put("count", "5");
		param.put("isPay", "true");
		param.put("cardNo", "5273380300218539");
		param.put("sku", "[[\"flavor\",\"Banana Cream\"],[\"size\",\"1 lb\"]]");
		param.put("sku", "[[\"size\",\"200 Capsules\"]]");
		autoBuy.selectProduct(param);
		param.put("my_price", "104");
		param.put("suffixNo", "747");
		//autoBuy.pay(param);
		
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("4805783155");
		autoBuy.scribeExpress(detail);
		//47.88.9.209
	}
	public static void main1(String[] args) {
		String sku = "[[\"flavor\",\"Banana Cream\"],[\"size\",\"1 lb\"]]";
		List<String> skuList = Utils.getSku(sku);
		if(sku.contains("flavor")){
			String attrVal = skuList.get(skuList.indexOf("flavor")+1);
			System.out.println(attrVal);
		}
	}
}
