package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

public class LookfantasticAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		LookfantasticAutoBuy auto = new LookfantasticAutoBuy();
		AutoBuyStatus status = auto.login("kopnux@163.com", "12345687");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = auto.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				param.put("url", "https://www.lookfantastic.com/chantecaille-just-skin-anti-smog-tinted-moisturiser-spf-15-50g/11234137.html");
				param.put("num", "1");
				param.put("sku", "[[\"shade\",\"Bliss\"]]");
				param.put("productEntityId", "7492376");
				status = auto.selectProduct(param);
				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
					Map<String, String> param0 = new HashMap<String, String>();
					param0.put("my_price", "85");
					param0.put("count", "1");
					param0.put("isPay", String.valueOf(false));
					param0.put("cardNo", "4662 4833 6029 1396");
					param0.put("suffixNo", "423");
					
					UserTradeAddress userTradeAddress = new UserTradeAddress();
					userTradeAddress.setName("刘波");
					userTradeAddress.setZip("310000");
					userTradeAddress.setAddress("西斗门路9号");
					userTradeAddress.setDistrict("西湖区");
					userTradeAddress.setCity("杭州市");
					userTradeAddress.setState("浙江省");
					userTradeAddress.setMobile("18668084980");
					status = auto.pay(param0, userTradeAddress, null);
				}
			}
		}
//		auto.logout();
	}
	
	public LookfantasticAutoBuy(){
		super(false);
		
	}
	
	public static void main0(String[] args){
		LookfantasticAutoBuy auto = new LookfantasticAutoBuy();
		AutoBuyStatus status = auto.login("fhagbgb2@126.com", "tfb001001");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			RobotOrderDetail detail = new RobotOrderDetail();
			detail.setMallOrderNo("90129023");
			detail.setProductSku("[[\"shade\",\"Bliss\"]]");
			auto.scribeExpress(detail);
			System.out.println("status = "+ status.getValue());
		}
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://www.lookfantastic.com/login.jsp");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//登录
		try{
			driver.findElement(By.id("username")).sendKeys(userName);
			logger.error("--->输入用户名");
			Utils.sleep(1500);
			driver.findElement(By.id("password")).sendKeys(passWord);
			logger.error("--->输入密码");
			Utils.sleep(1500);
			try{
				driver.executeScript("(function(){var els = document.getElementsByClassName('internationalOverlay_stayButton');if(els && els[0]){els[0].click();}})();");
				Utils.sleep(2500);
			}catch(Exception e){}
			
			WebElement login = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button#login-submit")));
			logger.error("--->点击登陆按钮");
			login.click();
		}
		catch (Exception e){
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}		

		//等待登录完成
		try
		{
			
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='nav-row']")));
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
		try{
			driver.executeScript("(function(){var els = document.getElementsByClassName('internationalOverlay_stayButton');if(els && els[0]){els[0].click();}})();");
			Utils.sleep(2500);
		}catch(Exception e){}
		
		//跳转到购物车
		try{
			driver.findElement(By.xpath("//a[@title='My Basket']")).click();
			logger.error("--->开始跳转到购物车");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@class='basket-table']")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='auto-basketaction-trash productdelete']"));
			while (true) {
				int size = list.size();
				if(list!=null && size>0){
					list.get(0).click();
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.xpath("//a[@class='auto-basketaction-trash productdelete']"));
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
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement cartNum = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".basket-item-qty")));
			logger.debug("--->购物车数量="+cartNum.getText());
			if(!cartNum.getText().equals("(0)")){
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
		try{
			driver.executeScript("(function(){var els = document.getElementsByClassName('internationalOverlay_stayButton');if(els && els[0]){els[0].click();}})();");
			Utils.sleep(2500);
		}catch(Exception e){}
		
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try{
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='product-area']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		
		//商品页面404,商品也是下架
//		try{
//			driver.findElement(By.xpath("//div[@id='content' and @class='error-page error-404']"));
//			logger.debug("--->这款商品的页面404错误,找不到该款商品");
//			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
//		}catch(Exception e){}
		
		//判断商品是否下架
		try{
			WebElement stockMsg = driver.findElement(By.xpath("//span[@class='product-stock-message']"));
			String text = stockMsg.getText();
			if("Sold out".equalsIgnoreCase(text)){
				logger.debug("--->这款商品已经下架");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		}catch(Exception e){
			logger.debug("--->找到该商品的stockMsg出错",e);
		}
		
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		//开始选择sku
		logger.debug("--->开始选择sku");
		if (!StringUtils.isBlank(sku)) {
			Map<String, String> skuMap = new HashMap<String, String>();
			
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i++) {
				if (i % 2 == 1) {
					String attrName = skuList.get(i - 1).toLowerCase();
					String attrValue = skuList.get(i);
					skuMap.put(attrName, attrValue);
					
					if(attrName.equalsIgnoreCase("shade")  && StringUtil.isNotEmpty(attrValue)){
						try {
							WebElement colorSelect = driver.findElement(By.id("opts-4"));
							Select select = new Select(colorSelect);
							List<WebElement> colors = select.getOptions();
							if (colors != null && colors.size() > 1) {
								int find = 0;
								for (WebElement color : colors) {
									String real = color.getText();
									String ct =  color.getText().trim().toLowerCase();
									if (attrValue.toLowerCase().equals(ct)) {
										find = 1;
										select.selectByVisibleText(real);
										logger.debug("--->选择shade:" + attrValue);
										TimeUnit.SECONDS.sleep(2);
										break;
									}
								}
								
								if (find == 0) {
									logger.error("[3]找不到指定sku:" + attrValue);
									return AutoBuyStatus.AUTO_SKU_NOT_FIND;
								}
							}
						} catch (Exception e) {
							return AutoBuyStatus.AUTO_SKU_NOT_FIND;
						}
					}
				
				}
			}
		}
		logger.debug("--->选择sku完成");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.xpath("//span[@class='price']"));
			String text = priceElement.getText();
			String productEntityId = param.get("productEntityId");
			logger.debug("--->寻找单价text = "+text);
			logger.debug("--->寻找单价productEntityId = "+productEntityId);
			if(!Utils.isEmpty(text) && text.startsWith("£") && StringUtil.isNotEmpty(productEntityId)){
				logger.debug("--->找到商品单价 = "+text.substring(1));
				priceMap.put(productEntityId, text.substring(1));
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				WebElement inputNum = driver.findElement(By.xpath("//input[@id='qty-picker']"));
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
		
		//加购物车
		logger.debug("--->开始加购物车");
		if (!StringUtils.isBlank(sku)) {
			try{
				driver.executeScript("(function(){var els = document.getElementsByClassName('cat-button');if(els && els[0]){els[0].click();}})();");
			}catch(Exception e){
				logger.debug("--->加购物车按钮找不到");
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
		}else{
			try{
				WebElement cart = driver.findElement(By.xpath("//a[contains(text(),'Add to basket')]"));
				Utils.sleep(1500);
				cart.click();
			}catch(Exception e){
				logger.debug("--->加购物车按钮找不到");
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			WebElement continueShopping = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[contains(text(),'View Basket')]")));
			Utils.sleep(1500);
			continueShopping.click();
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->购物车页面加载完成");
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}
	
	public AutoBuyStatus pay(Map<String, String> param) {
		return AutoBuyStatus.AUTO_PAY_FAIL;
	}
	
	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount) {
		// TODO Auto-generated method stub
		try{
			driver.executeScript("document.getElementById(\"popup\").style.visibility=\"hidden\";");
			Utils.sleep(1500);
		}catch(Exception e){}
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		String securityCode = param.get("suffixNo");
		if (Utils.isEmpty(securityCode)){
			logger.error("--->找不到信用卡安全码");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
	
		//设置价格
		logger.error("--->myPrice = "+myPrice);
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@id='gotocheckout1']")));
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//使用优惠码0 失效,1互斥 ,9没修改过,10有效
		String promotionStr = param.get("promotion");
		Set<String> promotionList = getPromotionList(promotionStr);
		if(promotionList != null && promotionList.size() > 0){
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@id='discountcode']"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//button[@id='add-discount-code']")).click();
						Utils.sleep(5500);
						
						try{
							driver.findElement(By.xpath("//div[@class='alert alert-danger']"));
							statusMap.put(code, 0);
						}catch(Exception e){
							logger.error("promotionCode:"+code,e);
							try{
								driver.findElement(By.xpath("//div[@class='alert discount-alert']"));
								statusMap.put(code, 10);
								isEffective = true;
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
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		
		//点击checkout
		try{
			driver.findElement(By.xpath("//a[@id='gotocheckout1']")).click();
		}catch(Exception e){
			logger.debug("--->点击Proceed to Checkout出现异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		logger.debug("--->等待支付页面加载");
		
		//等待checkout页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//form[@id='checkout-form']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//找到添加新地址
		try{
			WebElement w = driver.findElement(By.xpath("//label[@id='lbl-add-new-address']"));
			w.click();
			Utils.sleep(1000);
		}catch(Exception e){
			logger.debug("--->添加",e);
		}
		
		//输入收货地址
		try{
			WebElement w = driver.findElement(By.xpath("//input[@id='delivery-addressee']"));
			w.clear();
			Utils.sleep(1500);
			w.sendKeys(userTradeAddress.getName());
			Utils.sleep(1500);
			WebElement country = driver.findElement(By.xpath("//select[@id='delivery-country']"));
			Select select = new Select(country);
			select.selectByVisibleText("China");
			Utils.sleep(3000);
			driver.findElement(By.xpath("//input[@id='delivery-post-zip-code']")).sendKeys(userTradeAddress.getZip());
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='delivery-name-number']")).sendKeys("x");
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='delivery-street-name']")).sendKeys(userTradeAddress.getAddress());
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='delivery-address-line-2']")).sendKeys(userTradeAddress.getDistrict());
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='delivery-town-city']")).sendKeys(userTradeAddress.getCity());
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='delivery-state-province']")).sendKeys(userTradeAddress.getState());
			Utils.sleep(1500);
			driver.findElement(By.xpath("//input[@id='order-contact-number']")).sendKeys("+86"+userTradeAddress.getMobile());
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->设置收货地址出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		//选择Tracked Delivery
		try{
			logger.debug("--->开始select delivery");
			Utils.sleep(2000);                      
			driver.findElement(By.cssSelector("input#delivery-set-1-group-1-option-2")).click();
			Utils.sleep(2000);
			logger.debug("--->结束select delivery");
		}catch(Exception e){
			logger.debug("--->选择Tracked Delivery出错",e);
		}
		
		//再次确认选择Tracked Delivery
		try{
			logger.debug("--->开始select delivery000");
			Utils.sleep(2000);                  
			driver.findElement(By.cssSelector("label[for='delivery-set-1-group-1-option-2']")).click();
			Utils.sleep(2000);
			logger.debug("--->结束select delivery000");
		}catch(Exception e){
			logger.debug("--->选择Tracked Delivery000出错",e);
		}
		
		//选择信用卡
		try{
			driver.findElement(By.xpath("//input[@id='pay-with-recent-card']")).click();
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->点击信用卡选项出错",e);
		}
		try{
			driver.findElement(By.xpath("//input[@id='credit-card-num']")).sendKeys(param.get("cardNo"));
			Utils.sleep(2000);
			
			WebElement n = driver.findElement(By.xpath("//input[@id='credit-card-name-on-card']"));
			n.clear();
			Utils.sleep(2000);
			n.sendKeys(param.get("owner"));
			
			String expiry = param.get("expiryDate");
			String[] ss = expiry.split(" ");
			WebElement s = driver.findElement(By.xpath("//select[@id='expiration-month']"));
			Select select = new Select(s);
			select.selectByVisibleText(ss[1]);
			Utils.sleep(2000);
			
			WebElement s0 = driver.findElement(By.xpath("//select[@id='expiration-year']"));
			Select select0 = new Select(s0);
			select0.selectByVisibleText(ss[0].substring(2));
			Utils.sleep(2000);
			
		}catch(Exception e){
			logger.debug("--->点击信用卡选项出错",e);
		}
		
		Utils.sleep(2000);
		try{
			driver.findElement(By.xpath("//input[@id='saved-card-recent-cv2']")).sendKeys(securityCode);
		}catch(Exception e){
			logger.debug("--->第一次输入信用卡信息出错",e);
			try{
				driver.findElement(By.xpath("//input[@id='credit-card-cv2']")).sendKeys(securityCode);
			}catch(Exception ee){
				logger.debug("--->第二次输入信用卡信息出错",e);
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		}
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.xpath("//span[@id='summary-order-total-price']"));
			String text = totalPriceElement.getText();
			String priceStr = text.substring(text.indexOf("£")+1);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
			logger.debug("--->找到商品结算总价 = "+priceStr);
			BigDecimal x = new BigDecimal(myPrice);
			BigDecimal y = new BigDecimal(priceStr);
			BigDecimal v = y.subtract(x);
			if (v.doubleValue() > 20.00D){
				logger.error("--->总价差距超过约定,不能下单");
				throw new Exception();
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		//placeOrder 点击付款
		logger.debug("--->开始点击付款 placeOrder");
		try{
			WebElement placeOrderElement = driver.findElement(By.xpath("//button[@id='submit-my-order']"));
			Utils.sleep(1500);
			if(isPay){
				logger.debug("--->啊啊啊啊啊，我要付款了");
				placeOrderElement.click();
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		}catch(Exception e){
			logger.debug("--->点击付款出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		
		//查询商城订单号
		try{
			logger.debug("--->开始查找商品订单号");
			By byby = By.xpath("//strong[@id='order-complete-id']");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderElement.getText());
			savePng();
			return AutoBuyStatus.AUTO_PAY_SUCCESS;
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		try{
			driver.executeScript("document.getElementById(\"popup\").style.visibility=\"hidden\";");
			Utils.sleep(1500);
		}catch(Exception e){}
		String mallOrderNo = detail.getMallOrderNo();
		// TODO Auto-generated method stub
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.debug("--->开始跳转到my account页面");
			driver.navigate().to("https://www.lookfantastic.com/lfint/accountHome.account");
		}
		catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待my account页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='myAccountSection_yourOrders']")));
			Utils.sleep(1500);
			//清理下有多的地址
			try{
				driver.navigate().to("https://www.lookfantastic.com/lfint/addressBook.account");
				Utils.sleep(6500);
				List<WebElement> list = driver.findElements(By.xpath("//a[@class='addressBookCard_deleteAddress_button']"));
				if(list != null && list.size() > 5){
					for(int i = 0;i<list.size()-5;i++){
						WebElement w = list.get(i);
						w.click();
						Utils.sleep(4000);
					}
				}
			}catch(Exception e){
				logger.error("--->清理地址出错", e);
			}
			//跳转订单页
			logger.debug("--->开始跳转订单页面");
			driver.navigate().to("https://www.lookfantastic.com/lfint/accountOrderHistory.account");
			Utils.sleep(6500);
		}
		catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询所有可见的订单
		try{
			boolean isFind = false;
//			List<WebElement> list = driver.findElements(By.xpath("//a[@class='orderCard_cardAsLink']"));
			List<WebElement> list = driver.findElements(By.xpath("//div[@class='orderCard']"));
			if(list != null && list.size() > 0){
				loop:
				for(WebElement panel : list){
					List<WebElement> list0 = panel.findElements(By.xpath(".//a/p[@class='orderCard_text']"));
					List<WebElement> list1 = panel.findElements(By.xpath(".//p[@class='orderCard_detailsValue']"));
					if(list0 == null){
						list0 = new ArrayList<WebElement>();
					}
					if(list1 != null && list1.size() > 0){
						list0.addAll(list1);
					}
					if(list0 != null && list0.size() > 0){
						for(WebElement w:list0){
							String text = w.getText();
							if(StringUtil.isNotEmpty(text) && text.contains(mallOrderNo)){
								isFind = true;
								panel.click();
								break loop;
							}
						}
					}
				}
			
				if(!isFind){
					for(WebElement panel : list){
						try{
							WebElement w = panel.findElement(By.xpath(".//p[@class='orderCard_orderNumber']"));
							String text = w.getText();
							if(StringUtil.isNotEmpty(text) && text.contains(mallOrderNo)){
								isFind = true;
								panel.click();
								break;
							}
						}catch(Exception ee){}
					}
				}
					
			}
			if(isFind){
				WebElement panel = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='orderProductCard']")));
				try{
					try{
						WebElement status = driver.findElement(By.xpath("//p[@class='orderProductCard_productStatus']"));
						String text = status.getText();
						if(StringUtil.isNotEmpty(text)){
							text = text.toLowerCase();
							if(text.contains("cancelled")){
								logger.error("--->商城订单:"+mallOrderNo+"被砍单");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							}
						}
					}catch(Exception e){
						logger.error("--->商城订单:"+mallOrderNo+"没有被砍carryon");
					}
																						 
//					http://www.dpd.co.uk/tracking/quicktrack.do?search.consignmentNumber=15502210105647Q&search.searchType=16
					try{
//						WebElement w = panel.findElement(By.xpath(".//a[@class='orderProductCard_textLink']"));
//						WebElement w = panel.findElement(By.xpath(".//a[@class='orderProductCard_trackButton']"));
						WebElement w = panel.findElement(By.cssSelector("a.orderProductCard_trackButton"));
						if(w != null){
							String link = w.getAttribute("href");
							if(StringUtil.isNotEmpty(link) && link.contains("dpd.co.uk")){
								String mark = "consignmentNumber=";
								int begin = link.indexOf(mark);
								if(begin != -1){
									int end = link.indexOf("&",begin);
									if(end != -1){
										String a = link.substring(begin+mark.length(),end);
										Pattern p = Pattern.compile("([A-Za-z])$");
										Matcher m = p.matcher(a);
								        if (m.find()) {
								        	a = a.substring(0, a.length()-1);
								        }
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "dpd");
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, a);
										logger.error("--->找到物流单号 = "+a);
										return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
									}
								}
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;  
							}else{
								w.click();
							}
						}
					}catch(Exception e){
						logger.error("--->商城订单:"+mallOrderNo+"还没有发货",e);
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
					}
					
					Utils.sleep(3000);
					String currentWindow = driver.getWindowHandle();//获取当前窗口句柄  
					Set<String> handles = driver.getWindowHandles();//获取所有窗口句柄  
					Iterator<String> it = handles.iterator();  
					while (it.hasNext()) {  
						if (currentWindow == it.next()) {  
							continue;  
						}  
						WebDriver window = driver.switchTo().window(it.next());//切换到新窗口 
						if(window != null){
							try{
								Utils.sleep(5000);
								List<WebElement> tempList = driver.findElements(By.xpath("//table/tbody/tr"));
								boolean mark = false;
								if(tempList != null && tempList.size() > 0){
									for(WebElement ww : tempList){
										String text = ww.getText();
										if(StringUtil.isNotEmpty(text) && text.contains("Tracking Number:")){
											WebElement ee = ww.findElement(By.xpath(".//td"));
											if(ee != null && StringUtil.isNotEmpty(ee.getText())){
												mark = true;
												logger.error("--->找到物流单号 = "+ee.getText());
												
												data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "TRAKPAK");
												data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ee.getText());
												if(StringUtil.isNotEmpty(text) && !text.contains("Local Tracking Number")){
													data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EMS");
													return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
												}
											}else{
												logger.error("--->找到Tracking Number但是还没发货");
												return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
											}
										}
									}
									if(mark){
										return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
									}
								}
							}finally{
								window.close();
								driver.switchTo().window(currentWindow);//回到原来页面  
							}
						}
					}  
					
				}catch(Exception e){
					logger.error("--->获取商城订单:"+mallOrderNo+"出现异常",e);
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}
			}else{
				logger.error("--->没有找到商城订单:"+mallOrderNo);
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		}catch(Exception e){
			logger.error("--->查询订单出错", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		try{
			Utils.sleep(2000);
			driver.get("https://www.lookfantastic.com");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='nav-row']")));
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
