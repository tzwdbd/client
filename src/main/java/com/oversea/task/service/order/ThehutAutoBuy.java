package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.oversea.task.domain.Address;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class ThehutAutoBuy extends AutoBuy {

	private final Logger logger = Logger.getLogger(getClass());

	public ThehutAutoBuy() {
		super(false);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();

		driver.get("https://www.thehut.com/home.dept");
		// 点击登录
		try {
			By bySignIn = By.xpath("//a[@title='Login to your Account' and contains(text(), 'My Account')]");
			WebElement signIn = driver.findElement(bySignIn);
			logger.debug("--->跳转到登录页面");
			signIn.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		// 等到[输入框]出现
		WebDriverWait wait = new WebDriverWait(driver, 15);
		try {
			// 输入账号
			WebElement account = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='username']")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);

			// 输入密码
			WebElement password = driver.findElement(By.xpath("//input[@id='password']"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);

			// 提交
			WebElement submit = driver.findElement(By.xpath("//button[@id='login-submit']"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submit.click();
		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		// 等待登录完成
		try {
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//a[@title='Log out' and contains(text(), 'Log Out')]")));
			logger.debug("--->登录完成");
		} catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}
	
	AutoBuyStatus clickCart() {
		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.xpath("//a[@title='My Basket']"));
			logger.error("--->开始跳转到购物车");
			viewCart.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		AutoBuyStatus status = clickCart();
		if (!status.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) {
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.xpath("//a[@title='My Basket']"));
			logger.error("--->开始跳转到购物车");
			viewCart.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		// 清理购物车
		try {
			// 等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, 15);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Your Shopping Basket')]")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			// 清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.xpath("//a[@class='auto-basketaction-trash productdelete' and contains(text(),'Remove an item')]"));
			while (list != null && list.size() > 0) {
				logger.error("--->购物车list size=" + list.size());
				list.get(0).click();
				Utils.sleep(3000);
				list = driver.findElements(By.xpath("//a[@class='auto-basketaction-trash productdelete' and contains(text(),'Remove an item')]"));
			}
			logger.error("--->购物车页面清理完成");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		try {
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			By by = By.xpath("//td[@class='noItems' and contains(text(), 'There are currently no items in your basket.')]");
			WebElement cartBag = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			if (cartBag == null) {
				logger.debug("--->购物车数量清空异常");
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}
		} catch (Exception e) {
			logger.error("--->购物车数量清空异常");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try {
			driver.navigate().to(productUrl);
		} catch (Exception e) {
			logger.error("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@data-track='product-title']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		} catch (Exception e) {
			logger.error("--->等待商品页面加载异常");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		try {
			WebElement sessionButton = driver.findElement(By.xpath("//a[@class='js-sessionSettingsButton sessionSettings_button sessionSettings_button-active']"));
			sessionButton.click();
			Utils.sleep(1500);
			
			WebElement shippingCountrySelect = driver.findElement(By.xpath("//select[@class='sessionSettings_shippingCountrySelect']"));
			Select countrySelect = new Select(shippingCountrySelect);
			countrySelect.selectByVisibleText("China");
			Utils.sleep(1500);
			
			WebElement countrySiteSelect = driver.findElement(By.xpath("//select[@class='sessionSettings_countrySiteSelect']"));
			Select siteSelect = new Select(countrySiteSelect);
			siteSelect.selectByVisibleText("United Kingdom");
			Utils.sleep(1500);

			WebElement currencySelect = driver.findElement(By.xpath("//select[@class='sessionSettings_currencySelect']"));
			Select currency = new Select(currencySelect);
			currency.selectByVisibleText("£ GBP");
			Utils.sleep(1500);
			
			WebElement saveEle = driver.findElement(By.xpath("//button[@class='sessionSettings_saveButton js-sessionSettingsSave' and contains(text(), 'Save')]"));
			saveEle.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->设置session失败", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		// 售罄
		try {
			driver.findElement(By.xpath("//span[@class='cat-button soldout' and contains(text(), 'Out of Stock')]"));
			logger.debug("--->这款商品已经售罄");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		} catch (Exception e) {
		}

		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		// 开始选择sku
		logger.debug("--->开始选择sku");

		try {
			if (sku != null) {
				int findCount = 0;
				List<String> skuList = Utils.getSku((String) sku);
				for (int i = 0; i < skuList.size(); i++) {
					if (i % 2 == 1) {
						String keyStr = Utils.firstCharUpper(skuList.get(i - 1));
						String valueStr = skuList.get(i);
						WebElement legend = driver.findElement(By.xpath("//fieldset[@class='js-fieldSet-variations']/legend[contains(text(), '" + keyStr + "')]"));
						WebElement selectEle = legend.findElement(By.xpath("./following-sibling::label[1]/following-sibling::select[1]"));
						Select select = new Select(selectEle);
						Utils.sleep(1500);
						select.selectByVisibleText(valueStr);
						findCount++;
					}
				}
				if (findCount < skuList.size() / 2) {
					logger.error("--->缺少匹配的sku findCount = " + findCount + " && skuList.size()/2 = " + skuList.size() / 2);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}
			WebElement quantityELe = driver.findElement(By.xpath("//input[@id='qty-picker' and @name='quantity']"));
			quantityELe.clear();
			quantityELe.sendKeys(productNum);
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->选择sku碰到异常", e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->选择sku完成");
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.xpath("//span[@class='price' and @data-e2e='productCurrentPrice']"));
			String text = priceElement.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(text) && text.startsWith("£") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->找到商品单价 = " + text.substring(1));
				priceMap.put(productEntityId, text.substring(1));
			}
		} catch (Exception e) {
			logger.error("--->查询商品单价异常", e);
		}
		
		// 加购物车
		logger.debug("--->开始加购物车");
		try {
			if (sku != null) {
				WebElement cart = driver.findElement(By.xpath("//button[@data-e2e='addToBasketButton' and contains(text(), 'Buy Now')]"));
				cart.click();
			} else {
				WebElement cart = driver.findElement(By.xpath("//a[@data-e2e='addToBasketButton' and contains(text(), 'Buy Now')]"));
				cart.click();
			}
			Utils.sleep(1500);
			WebElement viewBasket = driver.findElement(By.xpath("//a[@id='basketAlertClose' and contains(text(), 'Continue Shopping')]/following-sibling::a[1]"));
			viewBasket.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->加购物车按钮找不到", e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}

		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Your Shopping Basket')]")));
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->加载Proceed to Checkout出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->购物车页面加载完成");
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}
	
	@Override
	public AutoBuyStatus pay(Map<String, String> param, UserTradeAddress address, OrderPayAccount payAccount) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		// 优惠码
		// Set<String> promotionList = getPromotionList(param.get("promotion"));
		
		// 设置价格
		logger.debug("--->myPrice = " + myPrice);

		AutoBuyStatus status = clickCart();
		if (!status.equals(AutoBuyStatus.AUTO_CLICK_CART_SUCCESS)) {
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@id='gotocheckout2' and contains(text(),'Checkout Securely Now')]")));
			Utils.sleep(1500);
			logger.debug("--->购物车页面加载完成");
			checkout.click();
		} catch (Exception e) {
			logger.error("--->加载Checkout Securely Now出现异常");
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

		// 选择地址
		status = chooseAddress(address);
		if (!AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS.equals(status)) {
			return status;
		}
		
		status = choosePayment(param);
		if (!AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_SUCCESS.equals(status)) {
			return status;
		}
		
		String totalPrice = "0";
		try {
			WebElement w = driver.findElement(By.id("summary-order-total-price"));
			if (StringUtil.isNotEmpty(w.getText())) {
				totalPrice = w.getText().replace("£", "").replace("-", "").replace(",", "").trim();
				logger.error("totalPrice = " + totalPrice);
			}
		} catch (Exception e) {
			logger.error("--->查找除去运费的总价失败", e);
		}

		// driver.executeScript("(function(){document.body.scrollTop = document.documentElement.scrollTop = 0;})();");
		logger.debug("--->开始比价[" + myPrice + "," + totalPrice + "]");
		BigDecimal x = new BigDecimal(myPrice);
		BigDecimal y = new BigDecimal(totalPrice);
		data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, totalPrice);
		BigDecimal v = y.subtract(x);
		if (v.doubleValue() > 15.00D) {
			logger.error("--->总价差距超过约定,不能下单");
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		doScreenShot();
		
		// 提交订单
		WebElement submit = driver.findElement(By.id("submit-my-order"));
		submit.click();
		Utils.sleep(5000);
		
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
	
	AutoBuyStatus chooseAddress(Address address) {
		try {
			logger.debug("--->开始填写地址");
			try {
				WebElement newAddress = driver.findElement(By.id("add-new-address"));
				if (newAddress != null) {
					newAddress.click();
					Utils.sleep(1500);
				}
			} catch (Exception e) {}
			
			// 姓名
			WebElement fullName = driver.findElement(By.id("delivery-addressee"));// 姓名
			fullName.clear();
			fullName.sendKeys(address.getName());
			Utils.sleep(1500);
			
			WebElement country = driver.findElement(By.id("delivery-country"));// 国家
			Select select = new Select(country);
			select.selectByVisibleText("China");
			Utils.sleep(1500);
			
			WebElement zip = driver.findElement(By.id("delivery-post-zip-code"));// 邮政编码
			zip.sendKeys(address.getZip());
			Utils.sleep(1500);
			
			WebElement address0 = driver.findElement(By.id("delivery-name-number"));
			address0.sendKeys(address.getAddress());
			Utils.sleep(1500);
			
			WebElement address1 = driver.findElement(By.id("delivery-street-name"));
			address1.sendKeys(address.getDistrict());
			Utils.sleep(1500);
			
			WebElement city = driver.findElement(By.id("delivery-town-city"));// 市
			city.sendKeys(address.getCity());
			Utils.sleep(1500);
			
			WebElement county = driver.findElement(By.id("delivery-state-province"));// 省
			county.sendKeys(address.getState());
			Utils.sleep(1500);
			
			WebElement contactNumber = driver.findElement(By.id("order-contact-number"));// 联系电话
			contactNumber.sendKeys(address.getMobile());
			Utils.sleep(1500);
			
			logger.debug("--->填写地址完成");
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
		} catch (Exception e) {
			logger.error("--->填写地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
	}
	
	AutoBuyStatus choosePayment(Map<String, String> param) {
		try {
			logger.debug("--->开始选择支付方式");
			String cardNo = param.get("cardNo");
			String nameOncard = param.get("owner");
			String expiry = param.get("expiryDate");
			String cv2 = param.get("suffixNo");
			
			try {
				WebElement payWithCard = driver.findElement(By.id("pay-with-card"));
				payWithCard.click();
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.debug("--->信用卡已存在");
				WebElement payWithRecenyCard = driver.findElement(By.id("pay-with-recent-card"));
				payWithRecenyCard.click();
				Utils.sleep(1500);

				WebElement cardRecentCv2ELe = driver.findElement(By.id("saved-card-recent-cv2"));
				cardRecentCv2ELe.sendKeys(cv2);
				Utils.sleep(1500);
				logger.debug("--->填写信用卡地址完成");
				
				return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_SUCCESS;
			}
			
			logger.debug("--->开始填写信用卡信息");
			WebElement cardNoELe = driver.findElement(By.id("credit-card-num"));
			cardNoELe.sendKeys(cardNo);
			Utils.sleep(1500);
			
			WebElement nameOncardEle = driver.findElement(By.id("credit-card-name-on-card"));
			nameOncardEle.clear();
			nameOncardEle.sendKeys(nameOncard);
			Utils.sleep(1500);
			
			String[] ss = expiry.split(" ");
			WebElement s = driver.findElement(By.id("expiration-month"));
			Select select = new Select(s);
			select.selectByVisibleText(ss[1]);
			Utils.sleep(2000);
			
			WebElement s0 = driver.findElement(By.id("expiration-year"));
			Select select0 = new Select(s0);
			select0.selectByVisibleText(ss[0].substring(2));
			Utils.sleep(2000);
			
			WebElement cardCv2ELe = driver.findElement(By.id("credit-card-cv2"));
			cardCv2ELe.sendKeys(cv2);
			Utils.sleep(1500);

			logger.debug("--->开始填写信用卡完成");
			logger.debug("--->开始填写信用卡地址");
			
			WebElement cardholderAddress = driver.findElement(By.id("use-different-address-as-cardholder-address"));
			cardholderAddress.click();
			Utils.sleep(1500);
			
			WebElement country = driver.findElement(By.id("billing-country"));// 国家
			Select select1 = new Select(country);
			select1.selectByVisibleText("China");
			Utils.sleep(1500);
			
			WebElement zip = driver.findElement(By.id("billing-post-zip-code"));// 邮政编码
			zip.sendKeys("310000");
			Utils.sleep(1500);
			
			WebElement address0 = driver.findElement(By.id("billing-name-number"));
			address0.sendKeys("2nd floor,1st building,Fu Di 2.0");
			Utils.sleep(1500);
			
			WebElement address1 = driver.findElement(By.id("billing-street-name"));
			address1.sendKeys("Xi Dou Men Street 9 , Xi Hu QU");
			Utils.sleep(1500);
			
			WebElement city = driver.findElement(By.id("billing-town-city"));// 市
			city.sendKeys("Hang Zhou");
			Utils.sleep(1500);
			
			WebElement county = driver.findElement(By.id("billing-state-province"));// 省
			county.sendKeys("Zhe Jiang");
			Utils.sleep(1500);
			logger.debug("--->填写信用卡地址完成");
			
			return AutoBuyStatus.AUTO_PAY_SELECT_VISA_CARD_SUCCESS;
		} catch (Exception e) {
			logger.error("--->选择支付方式失败", e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.debug("--->开始跳转到my account页面");
			driver.navigate().to("https://www.thehut.com/accountHome.account");
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
				driver.navigate().to("https://www.thehut.com/addressBook.account");
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
			driver.navigate().to("https://www.thehut.com/accountOrderHistory.account");
			Utils.sleep(6500);
		}
		catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询所有可见的订单
		try{
			boolean isFind = false;
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
																						 
					try{
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
												if(StringUtil.isNotEmpty(text) && !text.contains("TRAKPAK Tracking Number")){
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
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		ThehutAutoBuy autoBuy = new ThehutAutoBuy();
		Utils.sleep(2000);
		AutoBuyStatus status = autoBuy.login("huhy1099@foxmail.com", "hhy2240955810");
		// AutoBuyStatus status = autoBuy.login("wjglab3@126.com", "tfb001001"); //TODO
		

		Map<String, String> param = new HashMap<>();
		param.put("count", null);
		param.put("my_price", "50");
		param.put("userName", null);
		param.put("cardNo", "6225757555082164");
		param.put("nameOncard", "huhanyu");
		param.put("expiryDate", "1121");
		param.put("cv2", "123");
		
		UserTradeAddress address = new UserTradeAddress();
		address.setState("浙江");
		address.setCity("杭州市");
		address.setDistrict("西湖区");
		address.setAddress("西斗门路9号福地创业园9号4幢2楼海狐");
		address.setIdCard("330726199511251119");
		address.setMobile("17098041474");
		address.setZip("200001");
		address.setName("张明亮");
		address.setExpireDate("20170216");
		address.setIdCardFront("http://img.haihu.com/0117011371f4c87eb119406b91764f04d08919be.png");
		address.setIdCardBack("http://img.haihu.com/01170113a7a8e79c5973474c9e3656a52ad1a9f0.png");
		
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)) {
			status = autoBuy.cleanCart();
			if (AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)) {
//				param.put("url", "https://www.thehut.com/sports-clothing/under-armour-women-s-play-shorts-up-2.0-shorts-pomegranate/11399575.html");
//				param.put("sku", "[[\"Size\",\"S\"],[\"Colour\",\"Pomegranate\"]]");

				param.put("url", "https://www.thehut.com/kitchen-accessories/brita-marella-water-filter-jug-with-3-cartridges-white-2.4l/11090293.html");
				// param.put("sku", "[[\"Size\",\"S\"],[\"Colour\",\"Pomegranate\"]]");
				param.put("num", "1");
				autoBuy.selectProduct(param);
			}

			System.out.println(autoBuy.pay(param, address, null));
			
//			RobotOrderDetail detail = new RobotOrderDetail();
//			detail.setMallOrderNo("98027671");
//			detail.setProductSku("[[\"shade\",\"Bliss\"]]");
//			autoBuy.scribeExpress(detail);
		}
	}

}
