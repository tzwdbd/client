package com.oversea.task.service.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

public class SkinstoreAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		SkinstoreAutoBuy auto = new SkinstoreAutoBuy();
		auto.getSkuIdByUrl("https://www.skinstore.com/filorga-foam-cleanser-5oz/11394748.html");
		AutoBuyStatus status = auto.login("grtery@tom.com", "tfb001001");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = auto.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				List<String> urlList =  new ArrayList<>();
				urlList.add("https://www.skinstore.com/filorga-foam-cleanser-5oz/11394748.html");
				urlList.add("https://www.skinstore.com/filorga-time-zero-serum-1oz/11394758.html");
				urlList.add("https://www.skinstore.com/filorga-anti-aging-micellar-cleansing-solution-14oz/11394747.html");
				urlList.add("https://www.skinstore.com/filorga-time-filler-cream-2oz/11394759.html");
				urlList.add("https://www.skinstore.com/t3-anti-gravity-barrel-3.0-inch-brush/11356716.html");
				for(int i=0 ; i<5 ; ++i){
					param.put("url", urlList.get(i));
					param.put("num", "1");
					status = auto.selectProduct(param);
				}
				
				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
					TaskResult result = null;
					auto.productOrderCheck(result);
					
					Map<String, String> param0 = new HashMap<String, String>();
					param0.put("my_price", "85");
					param0.put("count", "1");
					param0.put("isPay", String.valueOf(false));
					param0.put("cardNo", "4662 4833 6029 1396");
					param0.put("suffixNo", "423");
					
//					UserTradeAddress userTradeAddress = new UserTradeAddress();
//					userTradeAddress.setName("刘波");
//					userTradeAddress.setZip("310000");
//					userTradeAddress.setAddress("西斗门路9号");
//					userTradeAddress.setDistrict("西湖区");
//					userTradeAddress.setCity("杭州市");
//					userTradeAddress.setState("浙江省");
//					userTradeAddress.setMobile("18668084980");
//					status = auto.pay(param0, userTradeAddress, null);
				}
			}
		}
//		auto.logout();
	}
	
	public SkinstoreAutoBuy(){
		super(false);
		
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("https://www.skinstore.com/login.jsp");
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
		try{
			driver.executeScript("(function(){var els = document.getElementsByClassName('internationalOverlay_stayButton');if(els && els[0]){els[0].click();}})();");
			Utils.sleep(2500);
		}catch(Exception e){}
		
		//跳转到购物车
		try{
			driver.findElement(By.xpath("//a[@title='My Bag']")).click();
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
		if(sku!=null && !"".equals(sku)){
			logger.debug("--->sku选择失败");
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
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
			if(!Utils.isEmpty(text) && text.startsWith("$") && StringUtil.isNotEmpty(productEntityId)){
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
				WebElement cart = driver.findElement(By.xpath("//a[contains(text(),'Add to bag')]"));
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
					By.xpath("//a[contains(text(),'View Bag')]")));
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
		return null;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		return null;
	}

	@Override
	public boolean gotoMainPage() {
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}

	@Override
	public boolean productOrderCheck(TaskResult taskResult) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='gotocheckout1']")));
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.error("--->加载Checkout Securely Now出现异常");
			return false;
		}
		
		//点击checkout
		try{
			logger.debug("--->点击Checkout Securely Now");
			driver.findElement(By.xpath("//a[@id='gotocheckout1']")).click();
		}catch(Exception e){
			logger.error("--->点击Checkout Securely Now出现异常" , e);
			return false;
		}
		
		//等待checkout页面加载完成
		try{
			logger.debug("--->等待支付页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@id='checkout-form']")));
			Utils.sleep(1500);
			logger.debug("--->支付页面加载完成");
		}catch(Exception e){
			logger.error("--->支付页面加载异常");
			return false;
		}		
		
		try{
			WebElement errorElement = driver.findElement(By.xpath("//span[@id='shippable-message-text']"));
			List<String> errorExternalId = new ArrayList<String>();
			if(errorElement == null){
				logger.error("--->无不允许付运商品");
				taskResult.addParam("errorProductEntityId", errorExternalId);
				return true;
			}else{
				logger.debug("--->跳转到禁运商品详情页面");
				driver.findElement(By.xpath("//a[@class='nonshippable-item-message-link']")).click();
				logger.debug("--->等待商品详情页面加载");
				WebElement tableElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.basket-table")));
				logger.debug("--->商品详情页面加载成功");
				
				List<WebElement> trElementList = tableElement.findElements(By.xpath("./tbody/tr"));
				for(WebElement trElement : trElementList){
					WebElement pElement = trElement.findElement(By.xpath("./td/div/div/p[@class='product-name']"));
					if(pElement.getText().contains("Free Gift") || pElement.getText().contains("FREE GIFT")){
						logger.debug("--->赠品跳过");
						continue;
					}
					String productUrl = pElement.findElement(By.xpath("./a")).getAttribute("href");
					logger.debug("--->禁运商品url: " + productUrl);
					errorExternalId.add(this.getSkuIdByUrl(productUrl));
				}
				taskResult.addParam("errorExternalId", errorExternalId);
				return true;
			}
		}catch(Exception e){
			logger.error("--->查找禁运商品失败" , e);
			return false;
		}
	}
	
	private String getSkuIdByUrl(String url){
		int start = url.lastIndexOf("/");
		int end = url.lastIndexOf(".html");
		return url.substring(start+1, end);
	}
}
