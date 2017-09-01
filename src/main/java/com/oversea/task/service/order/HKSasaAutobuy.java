package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class HKSasaAutobuy extends AutoBuy{
	private final Logger logger = Logger.getLogger(getClass());
	static int quality = 5;
	
	public HKSasaAutobuy() {
		try {
			logger.debug("--->关闭WAP版本");
			TimeUnit.SECONDS.sleep(2);
			driver.close();
			logger.debug("--->重新打开PC版");
			driver = new FirefoxDriver();
			driver.manage().deleteAllCookies();
		} catch (Exception e) {
			logger.error("--->初始化失败", e);
			return;
		}
	}
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("http://web1.sasa.com//SasaWeb/tch/preference/changePreference.jspa?location=0&currency=1");
		
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		try {
			By bySignIn = By.xpath("//a[contains(text(),'登入')]");
			WebElement signIn = driver.findElement(bySignIn);
			Utils.sleep(1500);
			logger.debug("--->跳转到登录页面");
			signIn.click();
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			// 输入账号
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='login']")));
			WebElement username = driver.findElement(By.xpath("//input[@name='login']"));
			logger.debug("--->输入账号");
			Utils.sleep(1500);
			username.sendKeys(userName);

			// 输入密码
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='password']")));
			WebElement passward = driver.findElement(By.xpath("//input[@name='password']"));
			logger.debug("--->输入密码");
			Utils.sleep(1500);
			passward.sendKeys(passWord);

			// 提交
			WebElement submitBtn = driver.findElement(By.className("login_btn"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submitBtn.click();

		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		// 等待登录完成
		try {
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'登出')]")));
			logger.debug("--->登录完成");
		} catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			try{
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'退出')]")));
				logger.debug("--->登录完成");
			}catch(Exception e1){
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}
	
	@Override
	public AutoBuyStatus cleanCart() {
		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.id("basketBtnTotalItem"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			viewCart.click();
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}		
		
		List<WebElement> goodUrls = driver.findElements(By.xpath("//td[@width='235']/a[@class='price_zone_2']"));
		List<WebElement> goodsInCart = driver.findElements(By.xpath("//img[@width='16']"));
		for(int i=0;i<goodUrls.size();i++){
			try {
				logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
				goodsInCart.get(i).click();
				logger.debug("--->删除购物车第[" + (i + 1) + "]件商品成功");
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}
		}
		
		// 跳转继续购物
		try {
			logger.debug("--->继续购物,再次跳转");

			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			By by = By.xpath("//img[@src='/shop/tch/images/v3/btn_Contin_shop.jpg']");
			wait.until(ExpectedConditions.elementToBeClickable(by));
			WebElement go = driver.findElement(by);
			go.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->继续购物跳转失败", e);
		}
		
		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.id("basketBtnTotalItem"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			viewCart.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}
	
	public AutoBuyStatus cleanCart(Map<String, ArrayList> param) {
		ArrayList<String> urlList = param.get("urlList");

		List<WebElement> goodurl = driver.findElements(By.xpath("//td[@width='235']/a[@class='price_zone_2']"));
		logger.error("--->清空购物车商品");
		int urllistSize = urlList.size();
		int goodurlSize = goodurl.size();
		for(int m=0;m<(goodurlSize-urllistSize);m++){
			List<WebElement> goodUrls = driver.findElements(By.xpath("//td[@width='235']/a[@class='price_zone_2']"));
			List<WebElement> goodsInCart = driver.findElements(By.xpath("//img[@width='16']"));
			for(int i=0;i<goodUrls.size();i++){
				WebElement goodUrl = goodUrls.get(i);
				String Url = goodUrl.getAttribute("href").replace("https", "http");
				if(!urlList.contains(Url)){
					try {
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						//driver.executeScript("arguments[0].click()", goodsInCart.get(i)); 
						goodsInCart.get(i).click();
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品成功");
						Utils.sleep(1500);
					} catch (Exception e) {
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
					continue;
				}
			}
			
			// 跳转继续购物
			try {
				logger.debug("--->继续购物,再次跳转");

				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				By by = By.xpath("//img[@src='/shop/tch/images/v3/btn_Contin_shop.jpg']");
				wait.until(ExpectedConditions.elementToBeClickable(by));
				WebElement go = driver.findElement(by);
				go.click();
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->继续购物跳转失败", e);
			}
			
			// 跳转到购物车
			try {
				WebElement viewCart = driver.findElement(By.id("basketBtnTotalItem"));
				logger.error("--->开始跳转到购物车");
				TimeUnit.SECONDS.sleep(5);
				viewCart.click();
				Utils.sleep(1500);
			} catch (Exception e) {
				logger.error("--->跳转到购物车失败");
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		Utils.sleep(1500);
		
		try {
			driver.navigate().to(productUrl);
		} catch (Exception e) {
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='detail-size']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		try{
			driver.findElement(By.xpath("//div[@class='outofstock-msg']"));
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}
		
		// 获取单价
		try{
			WebElement pricePanel = driver.findElement(By.xpath("//div[@class='right']//div[contains(text(),'現   價')]"));
			String priceStr = pricePanel.getText().replace("HK$", "").replace("現 價：", "");
			if (StringUtil.isNotEmpty(priceStr)){
				logger.debug("--->单价:" + priceStr.trim());
				priceMap.put(param.get("productEntityId"), priceStr.trim());
			}
			else{
				logger.error("--->获取单价失败");
			}
		}catch (Exception e){
			logger.error("--->获取单价失败");
		}
		
		String productNum = (String) param.get("num");
		
		//获取数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			try {
				logger.debug("--->商品数量 = " + productNum);
				
				WebElement addButton = driver.findElement(By.id("increase_num"));
				
				for(int i=1;i<Integer.valueOf(productNum);i++){
					addButton.click();
				}
				Utils.sleep(1000);
				logger.debug("--->选择商品数量完成");
			}catch (Exception e){
				logger.debug("--->选择商品数量异常");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		// 加购物车
		logger.debug("--->开始加购物车");
		try {
			WebElement addCart = driver.findElement(By.xpath("//img[@src='http://web1.sasa.com/shop/cn/images/detail-info-cart.gif']"));
			Utils.sleep(1500);
			addCart.click();
			logger.debug("--->加入购物车成功");
		} catch (Exception e) {
			logger.debug("--->加入购物车失败");
		}

		// 跳转到购物车
		try {
			WebElement viewCart = driver.findElement(By.id("basketBtnTotalItem"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			viewCart.click();
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
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
		logger.error("--->myPrice = " + myPrice);

		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));

		logger.debug("--->等待进行结账程序");
		//进行结账程序
		try {
			TimeUnit.SECONDS.sleep(5);
			By by = By.xpath("//img[@src='/shop/tch/images/v3/btn_checkout.jpg']");
			wait.until(ExpectedConditions.elementToBeClickable(by));
			WebElement go = driver.findElement(by);
			go.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->加载Checkout出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		logger.debug("--->等待支付页面加载");
		
		logger.debug("--->选择默认的收货地址");
		
		logger.debug("--->选择付款方式");
		try {
			WebElement editPaymentMethod = driver.findElement(By.xpath("//a[@id='editPaymentMethod']/img"));
			editPaymentMethod.click();
			TimeUnit.SECONDS.sleep(30);
			
			// 切换到iframe上去
			driver.switchTo().frame("TB_iframeContent");
			WebElement div = driver.findElement(By.xpath("//img[@id='payPal']"));
			div.click();
			Utils.sleep(1500);
			
			WebElement submit = driver.findElement(By.xpath("//a[@id='submitBtn']/span/b"));
			submit.click();
			driver.switchTo().defaultContent();
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.debug("--->选择付款方式失败");
		}
		
		WebElement termsOfUse = driver.findElement(By.xpath("//input[@id='termsOfUse']"));
		termsOfUse.click();
		
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement = driver.findElement(By.xpath("//td[@class='txt_12px_b_666666'][2]"));
			String text = totalPriceElement.getText();
			logger.debug(text);
			if (!Utils.isEmpty(text)) {
				String priceStr = text.replace("HK$", "").trim();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = " + priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = x.subtract(y);
				double m = Math.abs(v.doubleValue());
				if (m > 20.00D) {
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
		}
		
		logger.debug("--->进行结账程序");
		try {
			WebElement confirmOrder = driver.findElement(By.xpath("//a[@id='confirmOrder']/img"));
			confirmOrder.click();
			Utils.sleep(1500);
			WebElement paybutton_paypal = driver.findElement(By.xpath("//img[@class='paybutton_paypal']"));
			paybutton_paypal.click();
			Utils.sleep(1500);
		} catch (Exception e1) {
			logger.debug("--->支付页面跳转失败");
		}
		
		WebElement zhCNtext = driver.findElement(By.xpath("//tr[@valign='middle'][4]/td[@class='zh_CN_text' and @width='80']"));
		String orderNumber = zhCNtext.getText();
		logger.debug(orderNumber);
		
		String paypalaccount = param.get("paypalaccount");
		String paypalpassword = param.get("paypalpassword");
		logger.debug("--->登陆paypal");
		try {
			
			Utils.sleep(1500);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='email']")));
			WebElement email = driver.findElement(By.xpath("//input[@id='email']"));
			email.sendKeys(paypalaccount);
			
			Utils.sleep(1500);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='password']")));
			WebElement passwordEle = driver.findElement(By.xpath("//input[@id='password']"));
			passwordEle.sendKeys(paypalpassword);
			
			Utils.sleep(1500);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='btnLogin']")));
			WebElement btnLogin = driver.findElement(By.xpath("//button[@id='btnLogin']"));
			btnLogin.click();
			
		} catch (Exception e) {
			logger.debug("--->登陆paypal失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		
		//寻找历史订单
		try{
			logger.debug("--->开始跳转到个人中心页面");
			driver.navigate().to("https://web1.sasa.com/SasaWeb/sch/shopping/prepareOrderHistory.jspa");
		}
		catch (Exception e){
			logger.error("--->跳转到个人中心页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}			
		
		WebDriverWait wait = new WebDriverWait(driver, 30);
		//等待历史订单页面加载完成
		try{
			logger.debug("--->开始等待历史订单页面加载完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'订单历史')]"))); 
			Utils.sleep(1500);
			logger.debug("--->开始查找已付款的商品");
			WebElement payed = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'已付款')]"))); 
			payed.click();
			Utils.sleep(1500);
			// 根据商城订单号来查找订单
			try{
				List<WebElement> orderList = driver.findElements(By.xpath("//div[@class='content']//table/tbody/tr")); 
				for(int i=1 ; i<orderList.size() ; ++i){ // 跳过第一行
					WebElement tr = orderList.get(i);
					WebElement order = tr.findElement(By.xpath("./td[1]"));
					String orderNo = order.getText();
					if(orderNo.contains(mallOrderNo)){
						String orderStatus = tr.findElement(By.xpath("./td[4]/a")).getText();  // 查找订单的状态
						logger.debug("--->订单状态:" + orderStatus); 
						if("已付运".equals(orderStatus)){
							logger.debug("--->跳转订单详情页面");
							WebElement  lookOrder = order.findElement(By.xpath("../td[5]/p/a"));
							lookOrder.click();
							Utils.sleep(1500);
							logger.debug("--->等待订单详情页面加载");
							WebElement orderDetail = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='orderDetailTab']"))); 
							List<WebElement> trList = orderDetail.findElements(By.cssSelector("div .tabContainer>div[id='tab3']>table>tbody>tr"));
							String company = null , companyNo = null;
							for(WebElement trDetail : trList){
								String content = trDetail.getText();
								if(content.contains("速递公司")){
									logger.debug("--->获取快递公司:");
									company = trDetail.findElement(By.xpath(".//a")).getText();
									logger.debug("--->" + company);
								}else if(content.contains("追踪编号")){
									logger.debug("--->获取物流单号:");
									companyNo = trDetail.findElement(By.xpath(".//a")).getText();
									logger.debug("--->" + companyNo);
								}else{
									continue;
								}
							}
							if(StringUtil.isNotEmpty(company) && StringUtil.isNotEmpty(companyNo)){
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, company);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, companyNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}else{
								logger.error("--->获取商城订单:"+mallOrderNo+"失败");
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}
						}else{
							logger.debug("--->商城订单:"+mallOrderNo+"还没有发货");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}
					}else{
						continue;
					}
				
				} 
				
				//  被砍单的订单在其它中
				WebElement other = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'其它')]"))); 
				other.click();
				Utils.sleep(1500);
				
				// 根据商城订单号来查找订单
				try{
					orderList = driver.findElements(By.xpath("//div[@class='content']//table/tbody/tr")); 
					for(int i=1 ; i<orderList.size() ; ++i){ // 跳过第一行
						WebElement tr = orderList.get(i);
						WebElement order = tr.findElement(By.xpath("./td[1]"));
						String orderNo = order.getText();
						if(orderNo.contains(mallOrderNo)){
							logger.debug("--->商城订单:"+mallOrderNo+"被砍单");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
						} 
					}
				}catch(Exception e1){
					logger.error("--->根据商城订单号没有找到订单",e1);
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}	
				
				//  被砍单的订单在其它中
				WebElement waitPay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'等待付款')]"))); 
				waitPay.click();
				Utils.sleep(1500);
				
				// 根据商城订单号来查找订单
				try{
					orderList = driver.findElements(By.xpath("//div[@class='content']//table/tbody/tr")); 
					for(int i=1 ; i<orderList.size() ; ++i){ // 跳过第一行
						WebElement tr = orderList.get(i);
						WebElement order = tr.findElement(By.xpath("./td[1]"));
						String orderNo = order.getText();
						if(orderNo.contains(mallOrderNo)){
							String orderStatus = tr.findElement(By.xpath("./td[4]/a")).getText();  // 查找订单的状态
							logger.debug("--->订单状态:" + orderStatus); 
							if("缺货".equals(orderStatus)){
								logger.debug("--->商城订单:"+mallOrderNo+"缺货被砍单");
								return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
							} 
						}
					}
					logger.debug("--->根据商城订单号没有找到订单");
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}catch(Exception e1){
					logger.error("--->根据商城订单号没有找到订单",e1);
					return AutoBuyStatus.AUTO_SCRIBE_FAIL;
				}					
			}catch(Exception e){
				logger.error("--->根据商城订单号没有找到订单",e);
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		} catch (Exception e){
			logger.error("--->加载历史订单页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		
		return null;
	}
	public static void main(String args[]){
		HKSasaAutobuy autobuy = new HKSasaAutobuy();
		AutoBuyStatus status = autobuy.login("lhh3wd@163.com", "tfb001001");
		if(status.getValue() == AutoBuyStatus.AUTO_LOGIN_SUCCESS.getValue()){
			Map<String, String> param2 = new HashMap<String, String>();
			param2.put("url", "http://web1.sasa.com/SasaWeb/tch/product/viewProductDetail.jspa?itemno=104034907010");
			param2.put("num", "1");
			autobuy.selectProduct(param2);
			
			Map<String, ArrayList> param = new HashMap<String, ArrayList>();
			ArrayList<String> urlList = new ArrayList<String>();
			urlList.add("http://web1.sasa.com/SasaWeb/tch/product/viewProductDetail.jspa?itemno=107284002001");
			param.put("urlList", urlList);
			autobuy.cleanCart();
			
			RobotOrderDetail detail = new RobotOrderDetail();
			detail.setMallOrderNo("A171425670");
			autobuy.scribeExpress(detail);
			return;
		}
		
		
/*		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "http://web1.sasa.com/SasaWeb/tch/product/viewProductDetail.jspa?itemno=101526704001");
		param.put("num", "2");*/
		
		Map<String, String> param2 = new HashMap<String, String>();
		param2.put("url", "http://web1.sasa.com/SasaWeb/tch/product/viewProductDetail.jspa?itemno=107284002001");
		param2.put("num", "1");
		autobuy.selectProduct(param2);
		
		Map<String, String> param1 = new LinkedHashMap<>();
		//param1.put("url", "http://www.ninewest.com/Jackpot-Pointy-Toe-Pumps/18495878,default,pd.html?recommendationsource=home");
		param1.put("isPay", "false");
		param1.put("my_price", "100");
		param1.put("paypalaccount", "qdnqnl@163.com");
		param1.put("paypalpassword", "tfb001001");
		autobuy.pay(param1);
	}
	
	@Override
	public boolean productOrderCheck(TaskResult taskResult){
		WebDriverWait wait = new WebDriverWait(driver, 30);
		
		try{
			TimeUnit.SECONDS.sleep(5);
			
			By by = By.cssSelector("a img[src='/shop/sch/images/v3/btn_checkout.jpg']");
			wait.until(ExpectedConditions.visibilityOfElementLocated(by));
			WebElement go = driver.findElement(by);
			go.click();
			Utils.sleep(1500);
			
			logger.debug("--->开始查找是否存在不支持付运商品");
			List<String> errorExternalId = new ArrayList<String>();
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form[name='frmRemoveOrderDetails']")));
				logger.debug("--->存在不支持付运商品");
				List<WebElement> tableA = driver.findElements(By.cssSelector("form[name='frmRemoveOrderDetails'] table tr table a.price_zone_2"));
				for(WebElement w:tableA){
					String url = w.getAttribute("href");
					logger.debug("--->href:"+url);
					String resultUrl = getSkuIdByUrl(url);
					logger.debug("--->resultUrl:"+resultUrl);
					errorExternalId.add(resultUrl);
				}
				logger.debug("--->存在不支持付运商品件数为"+errorExternalId.size());
				taskResult.addParam("errorExternalId", errorExternalId);
				return true;
			} catch (Exception e) {
				logger.debug("--->不存在不支持付运商品");
				taskResult.addParam("errorExternalId", errorExternalId);
				return true;
			}
		}catch(Exception e){
			logger.error("--->选择禁运商品出现异常", e);
			return false;
		}
	}
	
	private String getSkuIdByUrl(String url){
		int start = url.indexOf("itemno");
		return url.substring(start + 7, url.length());
	}
	
}
