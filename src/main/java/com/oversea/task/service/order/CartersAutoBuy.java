package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
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
 * @date 2016年10月18日
 */

public class CartersAutoBuy extends AutoBuy{
	
	private final Logger logger = Logger.getLogger(getClass());
	
	public CartersAutoBuy() {
		super(true);
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().setSize(new Dimension(414, 1200));
		driver.manage().window().setPosition(new Point(0, 0));
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("http://www.carters.com/");
		
		//点击登录
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			By bySignIn = By.cssSelector(".user-login");
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(bySignIn));
			signIn.click();
			logger.debug("--->跳转到登录页面");
		} 
		catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		// 等到[输入框]出现
		try {
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".email-input")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.cssSelector(".password-input"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.id("login_btn"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".user-account")));
			logger.debug("--->登录完成");
		} 
		catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}
	
	public void closeBtn(){
		try {
			WebElement closeBtn = driver.findElement(By.xpath("//img[@alt='close-btn']"));
			logger.debug("出现了一个页面，关掉它");
			closeBtn.click();
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
		}
	}
	
	@Override
	public AutoBuyStatus cleanCart() {
		//跳转到购物车
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement cart = driver.findElement(By.xpath("//div[@class='global-minicart']"));
			cart.click();
			logger.debug("跳转到购物车");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='cartAnchor']")));
			logger.debug("购物车加载完成");
		} catch (Exception e) {
			logger.debug("跳转到购物车出现异常", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		//清空购物车
		int flag =0;
		try {
			while (true) {
				try {
					WebElement cartEmpty = driver.findElement(By.xpath("//div[@class='cart-empty']"));
					if(cartEmpty.getText().contains("Your cart is a little empty")){
						logger.debug("购物已经车清空了");
						break;
					}
				} catch (Exception e) {
				}
				List<WebElement> remove = driver.findElements(By.xpath("//button[@class='button-text remove-from-cart']"));
				remove.get(0).click();
				logger.debug("删除第"+(++flag)+"件商品");
				TimeUnit.SECONDS.sleep(5);
			}
		} catch (Exception e) {
			logger.debug("清空购物车异常",e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("选择商品 productUrl = " + productUrl);
		String productNum = (String) param.get("num");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			driver.navigate().to(productUrl);
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.debug("打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		WebElement main = null;
		try {
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='product-col-2  product-detail']")));
		} catch (Exception e) {
			logger.debug("加载商品页面出错");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		closeBtn();
		//判断商品是否有库存
		try {
			driver.findElement(By.xpath("//p[@contains(text(),'Out Of Stock')]"));
			logger.debug("库存不足");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		} catch (Exception e) {
			logger.debug("未售罄");
		}
		//开始选择sku
		String sku = param.get("sku");
		List<String> skuList = Utils.getSku(sku);
		for (int i = 0; i < skuList.size(); i++) {
			String skuName = skuList.get(i);
			String skuValue = skuList.get(++i);
			if(skuName.equalsIgnoreCase("color")){
				logger.debug("开始选择color");
				try {
					boolean flag = false;
					List<WebElement> colorList = main.findElements(By.xpath(".//ul[@class='swatches color']/li/a"));
					if(colorList.size()==1 && colorList.get(0).getText().contains(skuValue)){
						flag = true;
						logger.debug("选择color完成");
					}else{
						for (int j = 0; j < colorList.size(); j++) {
							WebElement color = colorList.get(j);
							if(color.getText().contains(skuValue)){
								flag = true;
								color.click();
								color.click();
								logger.debug("color选择完成");
								TimeUnit.SECONDS.sleep(3);
								break;
							}
						}
					}
					if(flag == false){
						logger.debug("没有找到颜色");
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
				} catch (Exception e) {

				}
			}else if(skuName.equalsIgnoreCase("size")){
				logger.debug("开始选择size");
				try {
					boolean flag = false;
					List<WebElement> list = main.findElements(By.xpath(".//ul[@class='swatches size']/li/a"));
					for (int j = 0; j < list.size(); j++) {
						WebElement size = list.get(j);
						if(size.getText().equalsIgnoreCase(skuValue)){
							size.click();
							flag = true;
							logger.debug("选择size完成");
							TimeUnit.SECONDS.sleep(7);
							break;
						}
					}
					if(flag == false){
						logger.debug("选择size错误");
						return AutoBuyStatus.AUTO_SKU_NOT_FIND;
					}
				} catch (Exception e) {
					logger.debug("选择size异常",e);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}else{
				logger.debug("出现了新的sku，网站可能改版了");
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
			
		}
		logger.debug("选择sku完成");
		
		// 寻找商品单价
		try {
			WebElement price = main.findElement(By.xpath(".//span[@class='price-sales ']"));
			Matcher m =Pattern.compile("[0-9.]+").matcher(price.getText());
			if(m.find()){
				String pricestr = m.group();
				logger.debug("找到商品单价:"+pricestr);
				String productEntityId = param.get("productEntityId");
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, pricestr);
				priceMap.put(productEntityId, pricestr);
			}else{
				logger.debug("获取单价失败");
			}
		} catch (Exception e) {
			logger.debug("获取单价失败",e);
		}
		// 选择商品数量
		try {
			WebElement numSelect = main.findElement(By.xpath(".//select[@id='Quantity']"));
			Select select = new Select(numSelect);
			select.selectByVisibleText(productNum);
			logger.debug("选择数量成功");
		} catch (Exception e) {
			try {
				TimeUnit.SECONDS.sleep(2);
				WebElement add = main.findElement(By.xpath(".//button[@id='qty-add']"));
				int s = Integer.parseInt(productNum)-1;
				for (int i = 0; i < s; i++) {
					add.click();
					TimeUnit.SECONDS.sleep(2);
				}
				logger.debug("选择数量完成");
			} catch (Exception e2) {
				logger.debug("选择数量异常");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		//判断sku是否有库存
		try {
			WebElement skuStock = driver.findElement(By.xpath("//div[@class='availability-msg']"));
			if(skuStock.getText().contains("In stock")){
				logger.debug("该sku有库存");
			}else{
				logger.debug("该sku库存不足");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("判断sku库存出现异常",e);
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}
		//加购物车
		try {
			TimeUnit.SECONDS.sleep(10);
			WebElement productContent = driver.findElement(By.xpath("//div[@id='product-content']"));
			WebElement btnList =productContent.findElement(By.xpath(".//button[@class='add-to-cart primary-btn ']"));
			btnList.sendKeys(Keys.ENTER);;
			TimeUnit.SECONDS.sleep(3);
			logger.debug("加入购物车");
			
			WebElement continueShopping = driver.findElement(By.xpath("//a[contains(text(),'Continue Shopping')]"));
			continueShopping.click();
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("加入购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		logger.error("myPrice = " + myPrice);
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		
		String isStock = param.get("isStock");
		logger.debug("是囤货订单："+isStock);
		
		//跳转到购物车
		try {
			WebElement cart = driver.findElement(By.xpath("//div[@class='global-minicart']"));
			cart.click();
			logger.debug("跳转到购物车");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='cartAnchor']")));
			logger.debug("购物车加载完成");
		} catch (Exception e) {
			logger.debug("跳转到购物车出现异常", e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
		boolean isEffective = false;
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		
		if (promotionList != null && promotionList.size() > 0) {
			try {
				List<WebElement> tr = driver.findElements(By.xpath("//tr[@class='rowcoupons']"));
				for (int i = 0; i < tr.size(); i++) {
					if(tr.get(i).getText().contains("Applied")){
						logger.debug("已经有优惠码生效");
						isEffective = true;
					}
				}
			} catch (Exception e) {
			}
			
			for (String code : promotionList) {
				try {
					logger.debug("code:" + code);
					WebElement promoCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter code']")));
					TimeUnit.SECONDS.sleep(1);
					promoCode.sendKeys(code);
					TimeUnit.SECONDS.sleep(1);
					
					WebElement apply = driver.findElement(By.xpath("//button[@id='add-coupon']"));
					apply.click();
					TimeUnit.SECONDS.sleep(5);
				} catch (Exception e) {
					logger.debug("输入优惠吗异常",e);
				}
				
				try {
					WebElement error = driver.findElement(By.xpath("//div[@class='error']"));
					if(error.getText().contains("Invalid Promo Code")||
							error.getText().contains("cannot be combined ")){
						logger.debug("优惠码无效或者冲突");
						statusMap.put(code, 0);
					}else{
						List<WebElement> tr = driver.findElements(By.xpath("//tr[@class='rowcoupons']"));
						for (int i = 0; i < tr.size(); i++) {
							logger.debug("tr.get(i).getText():"+tr.get(i).getText());
							if(tr.get(i).getText().contains(code)){
								logger.debug("优惠码有效");
								isEffective = true;
								statusMap.put(code, 10);
							}
						}
					}
				} catch (Exception e) {
					List<WebElement> tr = driver.findElements(By.xpath("//tr[@class='rowcoupons']"));
					for (int i = 0; i < tr.size(); i++) {
						logger.debug("tr.get(i).getText():"+tr.get(i).getText());
						if(tr.get(i).getText().contains(code)){
							logger.debug("优惠码有效");
							isEffective = true;
							statusMap.put(code, 10);
						}
					}
				}
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		/*if(StringUtil.isNotEmpty(promotionCodeList)){
			logger.debug("promotionCodeList:"+promotionCodeList);
			List<String> promotionList = Arrays.asList(promotionCodeList.split(";"));
			boolean promoSuccess = false;
			try {
				for (int i = 0; i < promotionList.size(); i++) {
					logger.debug("第"+i+"次输入优惠码");
					WebElement promoCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Enter code']")));
					TimeUnit.SECONDS.sleep(1);
					promoCode.sendKeys(promotionList.get(i));
					TimeUnit.SECONDS.sleep(1);
					
					WebElement apply = driver.findElement(By.xpath("//button[@id='add-coupon']"));
					apply.click();
					TimeUnit.SECONDS.sleep(5);
					try {
						driver.findElement(By.xpath("//div[@class='error']"));
					} catch (Exception e) {
						promoSuccess = true;
						break;
					}
				}
				if(promoSuccess==false && isStock.equals("true") ){
					logger.debug("是囤货订单而且没有可用的优惠码");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			} catch (Exception e) {
				logger.debug("输入优惠码异常",e);
				if(promoSuccess==false && isStock.equals("true") ){
					logger.debug("囤货订单输入优惠码异常",e);
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		}
		*/
		//跳转到结算页面
		try {
			List<WebElement> checkOut = driver.findElements(By.xpath("//button[@class='primary-btn ']"));
			checkOut.get(1).click();
			logger.debug("跳转到结算页面");
			TimeUnit.SECONDS.sleep(3);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='billingForm clearfix']")));
			logger.debug("跳转到结算页面完成");
		} catch (Exception e) {
			logger.debug("跳转大结算页面异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//选择收货地址
		try {
			
			logger.debug("选择信用卡");
			driver.executeScript("document.querySelectorAll('label.custom-radio.fa')[2].click();");
			TimeUnit.SECONDS.sleep(2);
			driver.findElementById("dwfrm_billing_paymentMethods_creditCard_cvn").sendKeys((String) param.get("suffixNo"));
			
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.xpath("//button[@class='primary-btn mobilevisible continue-checkout-mobile']")).click();
			
			/*logger.debug("选择物流");
			WebElement shipMethod = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='inputShipToCustomer']")));
			shipMethod.click();
			TimeUnit.SECONDS.sleep(1);
			logger.debug("选择直接送达收货地址");*/
			
			WebElement eidtShip = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='edit-shipping right']")));
			eidtShip.click();
			TimeUnit.SECONDS.sleep(1);
			logger.debug("点击edit地址");
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='reviewLabel']")));
			
			int sum = driver.findElementsByXPath("//div[@class='address-lessthan-five address-list-row']").size();
			logger.debug("目前有"+sum+"个地址可选");
			int index = 0;
			try {
				index = count%sum;
			} catch (Exception e) {

			}
			logger.debug("选择第"+(index+1)+"个地址");
			driver.executeScript("document.querySelectorAll('label.custom-radio.fa')["+index+"].click();");
			
			
			TimeUnit.SECONDS.sleep(2);
			logger.debug("选择当做gift寄出");
			driver.executeScript("document.querySelectorAll('label.custom-checkbox.fa')[2].click();");
			
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.xpath("//button[@class='primary-btn mobilevisible continue-checkout-mobile']")).click();
			logger.debug("选择收货地址完成");
		} catch (Exception e) {
			logger.debug("选择收货地址异常",e);
		}
		//选择付款方式
		try {
			TimeUnit.SECONDS.sleep(2);
			driver.findElementById("dwfrm_billing_paymentMethods_creditCard_cvn").sendKeys((String) param.get("suffixNo"));
			
			TimeUnit.SECONDS.sleep(2);
			driver.findElement(By.xpath("//button[@class='primary-btn mobilevisible continue-checkout-mobile']")).click();
		} catch (Exception e) {
			logger.debug("选择付款方式出现问题");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		
		logger.debug("开始付款");
		try {
			WebElement orderTotal = driver.findElement(By.xpath("//tr[@class='order-total']"));
			String priceTotalStr = orderTotal.getText();
			String taotalPrice = "";
			Matcher m = Pattern.compile("[0-9.]+").matcher(priceTotalStr);
			if(m.find()){
				taotalPrice = m.group();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, taotalPrice);
				logger.debug("找到商品结算总价 = " + taotalPrice);
				if(!StringUtil.isBlank(getTotalPrice())){
					AutoBuyStatus priceStatus = comparePrice(taotalPrice, getTotalPrice());
					if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}else{
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(taotalPrice);
					BigDecimal v = x.subtract(y);
					double s = v.doubleValue();
					//6块钱运费
					if (s > 11.00D) {
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}else{
				logger.debug("查询结算总价出现异常");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			
			WebElement submit = driver.findElement(By.xpath("//button[@id='general_submit_button_top']"));
			if(isPay){
				logger.debug("点击付款");
				submit.click();
				logger.debug("付款完成");
			}
			
		} catch (Exception e) {
			logger.debug("付款异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		try {
			WebElement orderNo =  wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='orderNumb']")));
			String oN = orderNo.getText();
			logger.debug("找到单号:"+oN);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, oN);
			savePng();
		} catch (Exception e) {
			logger.error("--->获取carters单号出错!");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		//查询订单号
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, 45);
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			driver.navigate().to("https://www.carters.com/on/demandware.store/Sites-Carters-Site/default/OrderHistory-GetOrderList?viewAll=true");
			driver.navigate().to("https://www.carters.com/on/demandware.store/Sites-Carters-Site/default/OrderHistory-GetOrderDetail?OrderId="+mallOrderNo);
			try{
				logger.debug("--->开始等待物流页面加载");
				WebElement orderNo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".trackBox .trackingID a")));
				logger.debug("--->已发货");
				String url = orderNo.getAttribute("href");
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, orderNo.getText().trim());
				logger.debug("--->物流号:"+orderNo.getText().trim());
				if(url.contains(".ups.")){
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
					logger.debug("--->物流公司:UPS");
				}else{
					logger.debug("--->未知的物流公司");
				}
				return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;	
			}
			catch (Exception e){
				logger.error("--->未发货");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
			}
		} catch (Exception e) {
			logger.debug("查找物流单号失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("http://www.carters.com/");
			Utils.sleep(5000);
			return true;
		} catch (Exception e) {
			logger.error("--->跳转Carters主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	public static void main(String[] args) {
//		CartersAutoBuy autoBuy = new CartersAutoBuy();
//		autoBuy.login("qx50yfn@163.com", "tfb001001");
//		
//		//out of stock
//		//http://www.carters.com/carters-kid-boy-tops/V_263G683.html
//		
//		Map<String, String> param = new LinkedHashMap<>();
//		param.put("url", "http://www.carters.com/carters-toddler-girl-pajamas/V_351G215.html");
//		param.put("num", "2");
//		param.put("count", "5");
//		param.put("isPay", "false");
//		param.put("cardNo", "5273380300218539");
//		param.put("sku", "[[\"color\",\"White\"],[\"size\",\"4T\"]]");
//		param.put("productEntityId", "1111111111111111111111111111111111111111");
//		//autoBuy.selectProduct(param);
//		param.put("url", "http://www.carters.com/carters-kid-girl-bottoms/V_278G335.html");
//		param.put("sku", "[[\"color\",\"Pink\"],[\"size\",\"6X\"]]");
//		//autoBuy.selectProduct(param);
//		param.put("my_price", "200");
//		//autoBuy.cleanCart();
//		param.put("suffixNo", "945");
//		//autoBuy.pay(param);
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("CAR21213508");
//		autoBuy.scribeExpress(detail);
		String orderNo = "";
		String filePathName = "";
		if(StringUtil.isNotEmpty(orderNo)){
			filePathName = "screenshot/" + orderNo+"-"+System.currentTimeMillis()/1000 + ".png";
		}else{
			filePathName = "screenshot/" + System.currentTimeMillis()/1000 + ".png";
		}
		System.out.println(filePathName);
	}
}
