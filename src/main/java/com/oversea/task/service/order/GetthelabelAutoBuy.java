package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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
import com.oversea.task.utils.Utils;

public class GetthelabelAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
    private Timer timer;
	public static void main(String[] args){
		GetthelabelAutoBuy auto = new GetthelabelAutoBuy();
		AutoBuyStatus status = auto.login("tzwdbd@126.com", "123456");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
		status = auto.cleanCart();
			//if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				
				Map<String, String> param = new HashMap<String, String>();
		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
				param.put("url", "http://www.rebatesme.com/zh/click/?key=3b27c8fda4c3ff307a5ec0766a50d0a2&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=0995f957e0c90e5563b6b7cd85a70e03&targetUrl=http%3A%2F%2Fcn.getthelabel.com%2F1010315.html%2F");
				param.put("num", "1");
				param.put("sku", "[[\"color\",\"白色\"],[\"size\",\"XS\"]]");
				param.put("productEntityId", "4241869");
				auto.selectProduct(param);
				Map<String, String> param1 = new HashMap<String, String>();
		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
				param1.put("url", "http://www.rebatesme.com/zh/click/?key=3b27c8fda4c3ff307a5ec0766a50d0a2&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=0995f957e0c90e5563b6b7cd85a70e03&targetUrl=http%3A%2F%2Fcn.getthelabel.com%2F1023646.html");
				param1.put("num", "2");
				param1.put("sku", "[[\"color\",\"Blue\"],[\"size\",\"L\"]]");
				param1.put("productEntityId", "42418619");
				auto.selectProduct(param1);
//				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "39.99");
//					param0.put("count", "1");
////					param0.put("isPay", String.valueOf(true));
//					param0.put("cardNo", "4662 4833 6029 1396");
//					UserTradeAddress userTradeAddress = new UserTradeAddress();
//					userTradeAddress.setName("刘波");
//					userTradeAddress.setAddress("西斗门路9号");
//					userTradeAddress.setState("浙江省");
//					userTradeAddress.setCity("杭州市");
//					userTradeAddress.setDistrict("西湖区");
//					userTradeAddress.setZip("310000");
//					userTradeAddress.setIdCard("330881198506111918");
//					userTradeAddress.setMobile("18668084980");
//					OrderPayAccount orderPayAccount = new OrderPayAccount();
//					orderPayAccount.setAccount("15268125960");
//					orderPayAccount.setPayPassword("199027");
//					status = auto.pay(param0,userTradeAddress,orderPayAccount);
//				}
			//}
		}
//		auto.logout();
	}
	
	public GetthelabelAutoBuy(){
		super(false);
		
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				logger.debug("15分钟start");
				if (driver != null){
					logger.debug("15分钟run");
					driver.quit();
				}
				logger.debug("15分钟end");
			}
		}, 1000*60*15);
		
	}
	

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://cn.getthelabel.com/customer/account/login/");
		//登录
		try{
			driver.findElement(By.id("LoginEmail")).sendKeys(userName);
			Utils.sleep(1000);
			driver.findElement(By.id("LoginPwd")).sendKeys(passWord);
			Utils.sleep(1000);
			driver.findElement(By.id("PageLogin")).click();
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e){
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}		

		//等待登录完成
		try
		{
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".logout")));
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
		//跳转到购物车
		try{
			Utils.sleep(1000);
			driver.get("http://cn.getthelabel.com/checkout/cart/");
			logger.error("--->开始跳转到购物车");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 30);
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".delete-checked")));
			logger.error("--->开始清理购物车");
			WebElement deleteall = driver.findElement(By.cssSelector(".delete-checked"));
			deleteall.click();
			Utils.sleep(1000);
			driver.findElement(By.id("easyDialogYesBtn")).click();
			logger.error("--->购物车页面清理完成");
		} catch (Exception e) {
			logger.error("--->购物车页面清理完成");
		}
			
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
		// TODO Auto-generated method stub
		
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
		//pd-selection
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		//开始选择sku
		logger.debug("--->开始选择sku");
		if(sku!=null){
			try{
				List<String> skuList = Utils.getSku((String) sku);
	//			List<WebElement> elements = driver.findElements(By.xpath("//form[@id='prForm']/ul[@class='wingInfo onSale noFreeShip']/li"));
				List<WebElement> elements = driver.findElements(By.cssSelector(".pd-selection ul li a"));
				int findCount = 0;
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								if(!StringUtil.isBlank(element.getText()) && element.getText().trim().equalsIgnoreCase(skuList.get(i).trim())){
									element.click();
									logger.debug("--->"+element.getText()+"点击");
									Utils.sleep(1000);
								}
							}catch(Exception e){
								continue;
							}
						}
					}
				}
				List<WebElement> skuChooseElements = driver.findElements(By.cssSelector(".pd-selection .selection-selected em"));
				for(WebElement w:skuChooseElements){
					String s = w.getText();
					for (int i = 0; i < skuList.size(); i++) {
						if (i % 2 == 1) {
							String attrValue = skuList.get(i);
							if(attrValue.equalsIgnoreCase(s)){
								logger.debug("--->"+attrValue+"加1");
								findCount++;
								break;
							}
						}
					}
				}
				logger.debug("--->sku findCount = "+findCount+" && skuList.size/2 = "+skuList.size()/2+" && skuChooseElements.size="+skuChooseElements.size());
				if(findCount < skuList.size()/2 || skuChooseElements.size()<skuList.size()/2){
					logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
			}catch(Exception e){
				logger.debug("--->选择sku碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
			}
		}
		logger.debug("--->选择sku完成");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.cssSelector(".current-price .price-number"));
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
				WebElement inputNum = driver.findElement(By.xpath("//input[@id='qty']"));
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
		try{
			WebElement cart = driver.findElement(By.xpath("//button[@id='cart_btn']"));
			Utils.sleep(1500);
			cart.click();
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//div[@id='easyDialogWrapper']")));
			Utils.sleep(1000);
			panel.findElement(By.xpath("//div[@class='tip-success-content']"));
			Utils.sleep(1000);
			panel.findElement(By.xpath(".//button[@id='easyDialogYesBtn']")).click();
			Utils.sleep(1500);
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='cart']")));
		}catch(Exception e){
			logger.debug("--->加载Proceed to Checkout出现异常,",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}
	
	public AutoBuyStatus pay(Map<String, String> param) {
		return AutoBuyStatus.AUTO_PAY_FAIL;
	}
	
	@Override
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress userTradeAddress,OrderPayAccount orderPayAccount) {
		// TODO Auto-generated method stub
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		String cardNo = param.get("cardNo");
		logger.error("cardNo = "+cardNo);
		
		//优惠码

	
		//设置价格
		logger.error("--->myPrice = "+myPrice);
		
		
		WebDriverWait wait = new WebDriverWait(driver, 15);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("AccountButton")));
			goPay.click();
			Utils.sleep(3000);
		} catch (Exception e) {
			logger.debug("--->加载Pharmacyonline结账出现异常");
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("AccountButton")));
			goPay.click();
		}
		
		String userName = param.get("userName");
		String passWord = param.get("password");
		try {
			// 因为返利地址的原因需要重新登录
			TimeUnit.SECONDS.sleep(5);
			// 输入账号
			WebElement username = driver.findElement(By.id("LoginEmail"));
			logger.debug("--->输入账号");
			Utils.sleep(1500);
			username.sendKeys(userName);
			Utils.sleep(1500);
			
			// 输入密码
			WebElement passward = driver.findElement(By.id("LoginPwd"));
			logger.debug("--->输入密码");
			Utils.sleep(1500);
			passward.sendKeys(passWord);
			Utils.sleep(1500);
			
			// 提交
			WebElement submitBtn = driver.findElement(By.xpath("//button[@id='PageLogin']"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submitBtn.click();
			Utils.sleep(1500);
			
			// 等待购物车页面加载完成
			logger.debug("--->等待购物车页面加载");
			try {
				driver.executeScript("(function(){window.scrollBy(300,500);})();");
				TimeUnit.SECONDS.sleep(5);
				WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
						By.id("AccountButton")));
				//结账
				HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
				boolean isEffective = false;
				Set<String> promotionList = getPromotionList(param.get("promotion"));
				if (promotionList != null && promotionList.size() > 0) {
					for (String code : promotionList) {
						logger.debug("couponCode："+code);
						WebElement element = driver.findElement(By.id("coupon_code"));
						element.clear();
						element.sendKeys(code);
						TimeUnit.SECONDS.sleep(2);
						
						WebElement use = driver.findElement(By.cssSelector(".use-code-btn"));
						use.click();
						TimeUnit.SECONDS.sleep(2);
						
						try {
							driver.findElement(By.xpath("//div[@class='easyDialog_text']"));
							logger.debug("优惠码无效："+code);
							driver.findElement(By.xpath("//button[@id='easyDialogYesBtn']")).click();;
							statusMap.put(code, 0);
						} catch (Exception e) {
							try {
								driver.findElement(By.xpath("//div[@class='coupon-done']"));
								logger.debug("优惠码有效："+code);
								isEffective = true;
								statusMap.put(code, 10);
							} catch (Exception e2) {
								logger.debug("异常："+e);
							}
						}
					}
					setPromotionCodelistStatus(statusMap);
					System.out.println(statusMap.toString());
					if("true".equals(param.get("isStock")) && !isEffective){
						logger.debug("--->优惠码失效,中断采购");
						return AutoBuyStatus.AUTO_PAY_FAIL;
					}
				}
				
				Utils.sleep(1500);
				goPay.click();
				Utils.sleep(5000);
			} catch (Exception e) {
				logger.debug("--->加载Pharmacyonline结账出现异常");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->不需要重新登录");
		}
		
		logger.debug("--->等待支付页面加载");
		try {
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".col-main")));
			logger.debug("--->支付页面加载完成");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		String usedEmail = (String) param.get("userName");
		try {
			List<WebElement> deleteList = driver.findElements(By.cssSelector(".address .delete"));
			while (true) {
				int size = deleteList.size();
				if(deleteList!=null && size>0){
					if(deleteList.get(0).isDisplayed()){
						deleteList.get(0).click();
						Utils.sleep(1000);
						driver.findElement(By.id("easyDialogYesBtn")).click();
						Utils.sleep(1000);
						if(size>1){
							deleteList = driver.findElements(By.cssSelector(".address .delete"));
						}else{
							break;
						}
					}
				}else{
					break;
				}
			}
		} catch (Exception e) {
			logger.debug("--->删除完了");
		}
		//找到添加新地址
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.address-book-add")));
			WebElement w = driver.findElement(By.cssSelector("a.address-book-add"));
			w.click();
			Utils.sleep(1000);
		}catch(Exception e){
			logger.debug("--->添加",e);
		}
		
		//输入收货地址
		try{
			//国家
			WebElement country = driver.findElement(By.xpath("//select[@id='country']"));
			Select select = new Select(country);
			select.selectByVisibleText("中国大陆");
			Utils.sleep(2000);
			
			//省份
			String stateStr = userTradeAddress.getState().trim();
			if("广西壮族自治区".equals(stateStr)){
				stateStr = "广西";
			}else if("西藏自治区".equals(stateStr)){
				stateStr = "西藏";
			}else if("宁夏回族自治区".equals(stateStr)){
				stateStr = "宁夏";
			}else if("新疆维吾尔自治区".equals(stateStr)){
				stateStr = "新疆";
			}else if("内蒙古自治区".equals(stateStr)){
				stateStr = "内蒙古";
			}
			WebElement state = driver.findElement(By.xpath("//select[@id='region_id']"));	
			Select selectState = new Select(state);
			selectState.selectByVisibleText(stateStr);
			Utils.sleep(2000);
			
			//市
			WebElement city = driver.findElement(By.xpath("//select[@id='city']"));	
			Select selectCity = new Select(city);
			String cityStr = userTradeAddress.getCity().trim();
			try {
				if("襄阳市".equals(cityStr)){
					cityStr = "襄樊市";
				} else if("上海市".equals(stateStr)){
					cityStr = "上海市";
				} else if("北京市".equals(stateStr)){
					cityStr = "北京市";
				} else if("重庆市".equals(stateStr)){
					cityStr = "重庆市";
				} else if("天津市".equals(stateStr)){
					cityStr = "天津市";
				} else if("陵水黎族自治县".equals(cityStr)){
					cityStr = "陵水县";
				}
				selectCity.selectByVisibleText(cityStr);
			} catch (Exception e) {
				try {
					if(!cityStr.endsWith("市")){
						cityStr = cityStr + "市";
					}
					selectCity.selectByVisibleText(cityStr);
				} catch (Exception e2) {
					logger.debug("--->输入市出错", e2);
				}
			}
			logger.debug("--->输入市");
			Utils.sleep(2000);
			
			//区
			String districtStr = userTradeAddress.getDistrict().trim();
			WebElement district = driver.findElement(By.xpath("//select[@id='s_county']"));	
			Select selectdistrict = new Select(district);
			try{
				selectdistrict.selectByVisibleText(districtStr);
			}catch(Exception e){				
				
				if(districtStr.endsWith("区")){//区改市
					districtStr = districtStr.subSequence(0, districtStr.length()-1)+"市";
				} else if(districtStr.equals("经济开发区")){
					districtStr = "经济技术开发区";
				}
				try{
					selectdistrict.selectByVisibleText(districtStr);
				}catch(Exception ee){
					selectdistrict.selectByIndex(1);
				}
			}
			Utils.sleep(2000);
			/*//地址
			driver.findElement(By.xpath("//input[@id='street_1']")).sendKeys(userTradeAddress.getDistrict()+userTradeAddress.getAddress());
			Utils.sleep(1500);
			//邮编
			driver.findElement(By.xpath("//input[@id='zip']")).sendKeys(userTradeAddress.getZip());
			Utils.sleep(1500);
			//电话
			driver.findElement(By.xpath("//input[@id='telephone']")).sendKeys(userTradeAddress.getMobile());
			Utils.sleep(1500);
			//常用邮箱
			driver.findElement(By.xpath("//input[@id='email']")).sendKeys(email);
			Utils.sleep(1500);*/
			
			//地址
			WebElement street = driver.findElement(By.id("street_1"));
			street.clear();
			Utils.sleep(1000);
			logger.debug("--->输入街道地址");
			Utils.sleep(1500);
			street.sendKeys(userTradeAddress.getDistrict()+userTradeAddress.getAddress());
			
			//邮编
			WebElement postcode = driver.findElement(By.id("postcode"));
			postcode.clear();
			Utils.sleep(1000);
			logger.debug("--->输入邮编");
			Utils.sleep(1500);
			postcode.sendKeys(userTradeAddress.getZip());
			
			//姓名
			WebElement w = driver.findElement(By.xpath("//input[@id='firstname']"));
			w.clear();
			Utils.sleep(1500);
			w.sendKeys(userTradeAddress.getName());
			Utils.sleep(1500);
			
			//电话
			WebElement telephone = driver.findElement(By.id("telephone"));
			telephone.clear();
			Utils.sleep(1000);
			logger.debug("--->输入电话");
			Utils.sleep(1500);
			telephone.sendKeys(userTradeAddress.getMobile());
			
			//常用邮箱
			WebElement emailEle = driver.findElement(By.id("email"));
			emailEle.clear();
			Utils.sleep(1000);
			logger.debug("--->输入常用邮箱");
			Utils.sleep(1500);
			emailEle.sendKeys(usedEmail);
			
			//保存地址
			driver.findElement(By.xpath("//input[@id='AjaxSaveAddress']")).click();
			Utils.sleep(3000);
			
		}catch(Exception e){
			logger.debug("--->设置收货地址出错",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		driver.executeScript("(function(){window.scrollBy(0,100);})();");
		
		//选中支付宝
		try{
			WebElement alipayPayment = driver.findElement(By.id("p_method_alipay_payment"));
			//driver.findElement(By.xpath("//input[@id='p_method_alipay_payment']")).click();
			driver.executeScript("var tar=arguments[0];tar.click();", alipayPayment);
		}catch(Exception e){
			logger.debug("--->选中支付宝出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 20);
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.cssSelector(".should-pay strong"));
			String text = totalPriceElement.getText();
			String priceStr = text.substring(1);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
			logger.debug("--->找到商品结算总价 = "+priceStr);
			BigDecimal x = new BigDecimal(myPrice);
			BigDecimal y = new BigDecimal(priceStr);
			BigDecimal v = y.subtract(x);
			if (v.doubleValue() > 6.00D){
				logger.error("--->总价差距超过约定,不能下单");
				throw new Exception();
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		// 查询优惠
		try {
			logger.debug("--->开始查询优惠");
			WebElement totalElement = driver
					.findElement(By.cssSelector(".computed-price"));
			
			WebElement expressElement = totalElement.findElement(By.cssSelector("#H-ship"));
			BigDecimal totalExpress = new BigDecimal(0);
			if (!Utils.isEmpty(expressElement.getText()) && expressElement.getText().indexOf("£") != -1) {
				String express = expressElement.getText().replace("£", "");
				logger.debug("--->[1]找到运费 = " + express);
				BigDecimal x = new BigDecimal(express);
				totalExpress = totalExpress.add(x);
			}
			data.put(AutoBuyConst.KEY_AUTO_BUY_MALL_EXPRESS_FEE,String.valueOf(totalExpress));
		} catch (Exception e) {
			logger.debug("--->查询总运费出现异常=", e);
		}
		driver.executeScript("(function(){window.scrollBy(0,300);})();");
		//提交订单
		logger.debug("--->开始点击提交订单 orderPayAccount.getPayPassword() = "+orderPayAccount.getPayPassword());
		try{
			WebElement placeOrder = driver.findElement(By.id("onestepcheckout-place-order"));
			placeOrder.click();;
			WebElement gotologin = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#J_tip_qr a.switch-tip-btn")));
			gotologin.click();
			logger.error("支付宝登陆按钮点击");
			//支付宝账号
			WebElement names = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='J_tLoginId']")));
			names.sendKeys(orderPayAccount.getAccount());
			Utils.sleep(1500);
			//密码
			driver.findElement(By.xpath("//input[@id='payPasswd_rsainput']")).sendKeys(orderPayAccount.getPayPassword());
			Utils.sleep(1500);
			//下一步
			driver.findElement(By.xpath("//a[@id='J_newBtn']")).click();
			Utils.sleep(1500);
			
			//输入支付密码
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='sixDigitPassword']")));
			String payPwd = orderPayAccount.getPayPassword();
			String str = "(function(){var els = document.getElementById('payPassword_rsainput');if(els){els.value='%s';}})();";
			String ss = String.format(str, payPwd);
			logger.debug("--->ss = "+ss);
			driver.executeScript(ss);
			Utils.sleep(3000);
			
			//输入银行卡号
			try{
				WebElement bank = wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='bankCardNo']")));
				bank.sendKeys(cardNo);
			}catch(Exception e){
				logger.debug("--->没找到输入银行卡的输入框",e);
			}
			
			Utils.sleep(1000);
			driver.findElement(By.xpath("//input[@id='J_authSubmit']")).click();
			Utils.sleep(1000);
			
		}catch(Exception e){
			logger.debug("--->点击付款出现异常",e);
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		
		//查询商城订单号
		try{
			logger.debug("--->开始查找商品订单号");
			wait = new WebDriverWait(driver, 2*WAIT_TIME);
			By byby = By.xpath("//p[@class='order-id']");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderElement.getText().substring(4));
			savePng();
			return AutoBuyStatus.AUTO_PAY_SUCCESS;
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		String mallOrderNo = detail.getMallOrderNo();
		// TODO Auto-generated method stub
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.debug("--->开始跳转到订单页面");
			driver.navigate().to("http://cn.getthelabel.com/sales/order/history/");
		}
		catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待order页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-item")));
			Utils.sleep(1500);
			logger.debug("--->order页面加载完成");
		}
		catch (Exception e){
			logger.error("--->加载order页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//查询所有可见的订单
		boolean isFindMall = false; 
		for(int i = 0;i<3;i++){
			try{
				boolean isFind = false; 
				List<WebElement> list = driver.findElements(By.cssSelector(".order-item"));
				if(list != null && list.size() > 0){
					for(WebElement panel : list){
						try{
							WebElement w = panel.findElement(By.xpath(".//span[@class='order-number']"));
							if(w.getText().contains(mallOrderNo.trim())){
								isFind = true;
								
								//判断订单是否取消
								try{
									String s = panel.findElement(By.xpath(".//span[@class='order-status right']")).getText();
									if(StringUtil.isNotEmpty(s) && s.contains("已取消")){
										logger.error("--->商城订单:"+mallOrderNo+"已取消");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
									}else if(StringUtil.isNotEmpty(s) && s.contains("已付款")){
										logger.error("--->商城订单:"+mallOrderNo+"还没有发货");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
									}
								}catch(Exception e){
									
								}
								
								panel.findElement(By.xpath(".//td[@class='item-order-status']/a")).click();;
								break;
							}
						}catch(Exception e){}
					}
				}
				if(isFind){
					isFindMall = true;
//					物流单号:SRYA05838
					try{
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='info-contain']")));
						WebElement status = driver.findElement(By.cssSelector(".status"));
						String text = status.getText();
						if(StringUtil.isNotEmpty(text) && text.equals("已发货")){
							WebElement shipment = driver.findElement(By.cssSelector(".hb-shipment span"));
							logger.error("--->找到物流单号 = "+shipment.getText().substring(4));
							String expressNo = shipment.getText().substring(4);
							driver.navigate().to("http://www.trackmytrakpak.com/?MyTrakPakNumber="+expressNo);
							List<WebElement> tempList = driver.findElements(By.xpath("//table/tbody/tr"));
							if(tempList != null && tempList.size() > 0){
								for(WebElement ww : tempList){
									String texts = ww.getText();
									if(StringUtil.isNotEmpty(texts) && texts.contains("Tracking Number:")){
										WebElement ee = ww.findElement(By.xpath(".//td"));
										if(ee != null && StringUtil.isNotEmpty(ee.getText())){
											logger.error("--->找到单号 = "+ee.getText());
											
											data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, ee.getText());
											if(StringUtil.isNotEmpty(texts) && !texts.contains("TRAKPAK Tracking Number")){
												data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EMS");
												return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
											}
										}else{
											logger.error("--->找到Tracking Number但是还没发货");
											return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
										}
									}
								}
							}
						}
					}catch(Exception e){
						logger.error("--->商城订单:"+mallOrderNo+"还没有发货",e);
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
					}
					break;
				}
			}catch(Exception e){
				logger.error("--->查询订单出错", e);
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			try{
				int page = i+2;
				String url= String.format("http://cn.getthelabel.com/sales/order/history/?p=%d", page);
				driver.navigate().to(url);
				Utils.sleep(5500);
			}catch(Exception e){
				logger.error("--->跳转page出错:",e);
			}
		}
		if(!isFindMall){
			logger.error("--->没有找到商城订单:"+mallOrderNo);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}

		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		// TODO Auto-generated method stub
		try{
			Utils.sleep(2000);
			driver.get("http://cn.getthelabel.com");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".nav")));
			Utils.sleep(1000);
		}
		catch (Exception e){
			logger.error("--->跳转getthelabel主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}
	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		timer.cancel();
		return true;
	}
}
