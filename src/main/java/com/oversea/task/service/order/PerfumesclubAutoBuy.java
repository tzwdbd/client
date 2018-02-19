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
import com.oversea.task.utils.ExpressUtils;
import com.oversea.task.utils.Utils;

public class PerfumesclubAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
    private Timer timer;
	public static void main(String[] args){
		PerfumesclubAutoBuy auto = new PerfumesclubAutoBuy();
		AutoBuyStatus status = auto.login("tzwdbd@126.com", "12345678");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
		status = auto.cleanCart();
			//if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				
				Map<String, String> param = new HashMap<String, String>();
		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
				param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=32b1UFcvBzd37BvFq5Da1umGUSN2Im_b3r4qkMWqXdkGE12Nr_aTIWRVArdd62YWwmHJOAJd8_c&new=http%3A%2F%2Fwww.perfumesclub.cn%2F1011570.html&tag=");
				param.put("orginalUrl", "http://www.perfumesclub.cn/1011570.html");
				param.put("num", "2");
				param.put("productEntityId", "4241869");
				auto.selectProduct(param);
				Map<String, String> param1 = new HashMap<String, String>();
		//		param.put("url", "http://cn.royyoungchemist.com.au/1131511.html/");
				param1.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=3c9cogD7q56i41tRtQjQFQ_bf8BUnkBBTPIsUaGA7y2M5fItPVbYwwi2ACX8acErSy1tLNmQ_c&new=http%3A%2F%2Fwww.perfumesclub.cn%2F1004639.html&tag=");
				param1.put("orginalUrl", "http://www.perfumesclub.cn/1004639.html/");
				param1.put("num", "3");
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
	
	public PerfumesclubAutoBuy(){
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
		driver.get("http://www.perfumesclub.cn/customer/account/login/");
		//登录
		try{
			driver.findElement(By.id("login-text-email")).sendKeys(userName);
			Utils.sleep(1000);
			driver.findElement(By.id("login-text-password")).sendKeys(passWord);
			Utils.sleep(1000);
			driver.findElement(By.id("login-btn-submit")).click();
			logger.debug("--->跳转到登录页面");
		}
		catch (Exception e){
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}		

		//等待登录完成
		try
		{
			WebDriverWait wait = new WebDriverWait(driver, 5);
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login")));
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		catch (Exception e)
		{
			logger.debug("--->登录完成");
			return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
		}
	}

	@Override
	public AutoBuyStatus cleanCart() {
		//跳转到购物车
		try{
			Utils.sleep(1000);
			driver.get("http://www.perfumesclub.cn/checkout/cart/");
			logger.error("--->开始跳转到购物车");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		WebDriverWait wait = new WebDriverWait(driver, 30);
		logger.debug("--->清空购物车");
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".operation-delete")));
			logger.error("--->开始清理购物车");
			WebElement deleteall = driver.findElement(By.cssSelector(".operation-delete"));
			deleteall.click();
			Utils.sleep(1000);
			driver.findElement(By.id("easyDialogYesBtn")).click();
			logger.error("--->购物车页面清理完成");
		} catch (Exception e) {
			logger.error("--->购物车页面清理完成");
		}
		try {
			logger.error("--->等待购物车加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".settlement-operation")));
			logger.error("--->开始清理购物车");
			WebElement deleteall = driver.findElement(By.cssSelector(".settlement-operation"));
			deleteall.click();
			Utils.sleep(1000);
			driver.findElement(By.id("easyDialogYesBtn")).click();
			logger.error("--->购物车页面清理完成");
		} catch (Exception e2) {
			logger.debug("--->购物车页面清理完成！");
		}
			
		try {
			driver.navigate().to("http://www.perfumesclub.cn/customer/address/");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#addressCard li")));
			List<WebElement> deleteList = driver.findElements(By.cssSelector("#addressCard li"));
			while (true) {
				int size = deleteList.size();
				if(deleteList!=null && size>0){
					WebElement dw = deleteList.get(0);
					List<WebElement> operations =  dw.findElements(By.cssSelector("a.operation-type"));
					for(WebElement w:operations){
						if(w.getText().contains("删除")){
							w.click();
							break;
						}
					}
					Utils.sleep(500);
					driver.findElement(By.id("easyDialogYesBtn")).click();
					Utils.sleep(500);
					if(size>1){
						deleteList = driver.findElements(By.cssSelector("#addressCard li"));
					}else{
						break;
					}
				}else{
					break;
				}
			}
		} catch (Exception e) {
		}
		
		
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		// TODO Auto-generated method stub
		Object sku = param.get("sku");
		if(sku!=null && !"".equals(sku)){
			logger.debug("--->sku选择失败");
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		String orginalUrl = (String) param.get("orginalUrl");
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
		
		String productNum = (String) param.get("num");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.cssSelector(".current-price"));
			WebElement integersW = priceElement.findElement(By.cssSelector(".integers"));
			WebElement decimalsW = priceElement.findElement(By.cssSelector(".decimals"));
			String text = integersW.getText()+decimalsW.getText();
			String productEntityId = param.get("productEntityId");
			logger.debug("--->寻找单价text = "+text);
			logger.debug("--->寻找单价productEntityId = "+productEntityId);
			if(!Utils.isEmpty(text)  && StringUtil.isNotEmpty(productEntityId)){
				logger.debug("--->找到商品单价 = "+text);
				priceMap.put(productEntityId, text);
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				WebElement select = driver.findElement(By.id("detail-text-quantity"));
				Select s = new Select(select);
				s.selectByVisibleText(productNum);
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
			WebElement cart = driver.findElement(By.id("detail-btn-addcart"));
			driver.executeScript("var tar=arguments[0];tar.click();", cart);
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			WebElement panel = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("easyDialogYesBtn")));
			driver.executeScript("var tar=arguments[0];tar.click();", panel);
			
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='cart']")));
//			
//			List<WebElement> trs = driver.findElements(By.cssSelector(".tr-selected"));
//			for(WebElement w:trs){
//				WebElement aw = w.findElement(By.cssSelector(".product-name a"));
//				if(aw.getAttribute("href").contains(orginalUrl)){
//					WebElement inputNum = w.findElement(By.cssSelector(".cart-text-quantity"));
//					Utils.sleep(2000);
//					inputNum.clear();
//					Utils.sleep(2000);
//					inputNum.sendKeys(productNum);
//					Utils.sleep(2000);
//					break;
//				}
//			}
			
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
		String size = param.get("size");
		WebDriverWait wait = new WebDriverWait(driver, 15);
		try {
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("checkoutBtn")));
		} catch (Exception e) {
			try {
				List<WebElement> goodsInCart = driver.findElements(By.cssSelector(".operation-delete"));
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				logger.debug("--->size有 [" + size + "]件商品");
				if(!size.equals(String.valueOf(goodsInCart.size()))){
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			} catch (Exception e1) {
				logger.debug("--->购物车验证数量出错",e1);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		
	
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("checkoutBtn")));
			goPay.click();
			Utils.sleep(3000);
		} catch (Exception e) {
			logger.debug("--->加载Pharmacyonline结账出现异常");
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.id("checkoutBtn")));
			goPay.click();
		}
		try {
			 driver.findElement(By.id("login-text-email"));
		} catch (Exception e) {
			logger.debug("--->重试结账1");
			try {
				driver.findElement(By.id("checkoutBtn")).click();
				Utils.sleep(3000);
			} catch (Exception e2) {
				
			}
			
		}
		try {
			 driver.findElement(By.id("login-text-email"));
		} catch (Exception e) {
			logger.debug("--->重试结账2");
			try {
				WebElement settle = driver.findElement(By.id("checkoutBtn"));
				driver.executeScript("var tar=arguments[0];tar.click();", settle);
			} catch (Exception e2) {
				
			}
			
		}
		
		String userName = param.get("userName");
		String passWord = param.get("password");
		try {
			// 因为返利地址的原因需要重新登录
			TimeUnit.SECONDS.sleep(5);
			// 输入账号
			WebElement username = driver.findElement(By.id("login-text-email"));
			logger.debug("--->输入账号");
			Utils.sleep(1500);
			username.sendKeys(userName);
			Utils.sleep(1500);
			
			// 输入密码
			WebElement passward = driver.findElement(By.id("login-text-password"));
			logger.debug("--->输入密码");
			Utils.sleep(1500);
			passward.sendKeys(passWord);
			Utils.sleep(1500);
			
			// 提交
			WebElement submitBtn = driver.findElement(By.id("login-btn-submit"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submitBtn.click();
			Utils.sleep(1500);
			//跳转到购物车
			try{
				Utils.sleep(1000);
				driver.get("http://www.perfumesclub.cn/checkout/cart/");
				logger.error("--->开始跳转到购物车");
			}catch(Exception e){
				logger.error("--->跳转到购物车失败",e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
			// 等待购物车页面加载完成
			logger.debug("--->等待购物车页面加载");
			try {
				WebElement goPay = null;
				try {
					 wait.until(ExpectedConditions.visibilityOfElementLocated(
								By.id("checkoutBtn")));
					goPay = driver.findElement(By.id("checkoutBtn"));
				} catch (Exception e) {
					try {
						goPay = driver.findElement(By.id("checkoutBtn"));
						logger.debug("1111");
					} catch (Exception e2) {
						try {
							goPay = driver.findElement(By.id("checkoutBtn"));
							logger.debug("333");
						} catch (Exception e3) {
							logger.debug("222");
						}
						
					}
					
				}
				//结账
				HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
				boolean isEffective = false;
				Set<String> promotionList = getPromotionList(param.get("promotion"));
				if (promotionList != null && promotionList.size() > 0) {
					for (String code : promotionList) {
						logger.debug("couponCode："+code);
						WebElement element = driver.findElement(By.id("usedCouponCode"));
						element.clear();
						element.sendKeys(code);
						TimeUnit.SECONDS.sleep(2);
						
						WebElement use = driver.findElement(By.cssSelector("#useCouponCode"));
						use.click();
						TimeUnit.SECONDS.sleep(2);
						
						try {
							driver.findElement(By.cssSelector("#useCodeSuccessTips"));
							isEffective = true;
							statusMap.put(code, 10);
							logger.debug("优惠码有效："+code);
						} catch (Exception e1) {
							logger.debug("优惠码无效："+code);
							try {
								driver.findElement(By.id("useCodeFailTips"));
								statusMap.put(code, 0);
							} catch (Exception e2) {
								logger.debug("异常："+e2);
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
				logger.debug("--->加载Pharmacyonline结账出现异常",e);
				//return AutoBuyStatus.AUTO_PAY_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->不需要重新登录");
		}
		
		logger.debug("--->等待支付页面加载");
		try {
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkout-section")));
			logger.debug("--->支付页面加载完成");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		String usedEmail = (String) param.get("userName");
		boolean mark = false;
		//找到添加新地址
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".address-btn-create")));
			WebElement w = driver.findElement(By.cssSelector(".address-btn-create"));
			w.click();
			Utils.sleep(1000);
		}catch(Exception e){
			logger.debug("--->添加出错");
			mark = true;
		}
		String name = userTradeAddress.getName();
		String address = userTradeAddress.getAddress();
		String zipcode = userTradeAddress.getZip();
		String mobile = userTradeAddress.getMobile();
		//输入收货地址
		try{
			
			WebElement firstname = driver.findElement(By.id("firstname"));
			logger.debug("--->输入收货人姓名"+name);
			Utils.sleep(1500);
			firstname.sendKeys(name);
			
			logger.debug("--->选择收货地址");
			WebElement countrySelect = driver.findElement(By.id("country"));
			Select select = new Select(countrySelect);
			List<WebElement> countrys = select.getOptions();
			if (countrys != null && countrys.size() > 1) {
				for(WebElement country : countrys){
					String countryVal =  country.getAttribute("value").trim();
					if(countryVal.equals("中国大陆")){
						select.selectByVisibleText(countryVal);
						break;
					}
				}
			}
			TimeUnit.SECONDS.sleep(2);
			
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
			WebElement state = null;
			try {
				state = driver.findElement(By.id("region_id"));
			} catch (Exception e) {
				state = driver.findElement(By.id("region"));
			}
				
			Select selectState = new Select(state);
			selectState.selectByVisibleText(stateStr);
			logger.debug("--->输入省");
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
				} else if("大理市".equals(cityStr)){
					cityStr = "大理州";
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
			WebElement district = null;
			try {
				district = driver.findElement(By.xpath("//select[@id='s_county']"));	
			} catch (Exception e) {
				district = driver.findElement(By.id("county"));	
			}
			
			
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
			logger.debug("--->输入区");
			Utils.sleep(2000);
			WebElement street = null;
			try {
				street = driver.findElement(By.id("street_1"));
			} catch (Exception e) {
				street = driver.findElement(By.cssSelector(".input-textarea"));
			}
			
			street.clear();
			Utils.sleep(1000);
			logger.debug("--->输入街道地址");
			Utils.sleep(1500);
			street.sendKeys(userTradeAddress.getDistrict()+address);
			
			WebElement postcode = driver.findElement(By.id("postcode"));
			postcode.clear();
			Utils.sleep(1000);
			logger.debug("--->输入邮编");
			Utils.sleep(1500);
			postcode.sendKeys(zipcode);
			
			WebElement telephone = driver.findElement(By.id("telephone"));
			telephone.clear();
			Utils.sleep(1000);
			logger.debug("--->输入电话");
			Utils.sleep(1500);
			telephone.sendKeys(mobile);
			
			WebElement email = driver.findElement(By.id("email"));
			email.clear();
			Utils.sleep(1000);
			logger.debug("--->输入常用邮箱");
			Utils.sleep(1500);
			email.sendKeys(usedEmail);
			
			Utils.sleep(1500);
			WebElement saveAddrBtn = null;
			try {
				saveAddrBtn = driver.findElement(By.id("AjaxSaveAddress"));
			} catch (Exception e) {
				saveAddrBtn = driver.findElement(By.cssSelector(".btn-save"));
			}
			saveAddrBtn.click();
			Utils.sleep(3000);
			logger.debug("--->点击保存地址");
			
		}catch(Exception e){
			logger.debug("--->设置收货地址出错",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		driver.executeScript("(function(){window.scrollBy(0,-100);})();");
		
		//选中支付宝
		try{
			WebElement alipayPayment = driver.findElement(By.id("payment-alipay_payment"));
			//driver.findElement(By.xpath("//input[@id='p_method_alipay_payment']")).click();
			driver.executeScript("var tar=arguments[0];tar.click();", alipayPayment);
			Utils.sleep(3000);
		}catch(Exception e){
			logger.debug("--->选中支付宝出错",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 20);
		driver.executeScript("(function(){window.scrollBy(0,200);})();");
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totalPriceElement = driver.findElement(By.id("grandTotal"));
			String text = totalPriceElement.getText();
			String priceStr = text.substring(0,text.length()-1);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
			logger.debug("--->找到商品结算总价 = "+priceStr);
			if(!StringUtil.isBlank(getTotalPrice())){
				AutoBuyStatus priceStatus = comparePrice(priceStr, getTotalPrice());
				if(AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT.equals(priceStatus)){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}else{
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 15.00D){
					logger.error("--->总价差距超过约定,不能下单");
					throw new Exception();
				}
				if (v.doubleValue() < -15.00D){
					logger.error("--->漏下单");
					throw new Exception();
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常",e);
			return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
		}
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		driver.executeScript("(function(){window.scrollBy(0,300);})();");
		//提交订单
		//logger.debug("--->开始点击提交订单 orderPayAccount.getPayPassword() = "+orderPayAccount.getPayPassword());
		try{
			WebElement placeOrder = driver.findElement(By.id("priceConfirm"));
			placeOrder.click();;
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#J_tip_qr a.switch-tip-btn")));
			WebElement gotologin = driver.findElement(By.cssSelector("div#J_tip_qr a.switch-tip-btn"));
			gotologin.click();
			logger.error("支付宝登陆按钮点击");
			//支付宝账号
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='J_tLoginId']")));
			} catch (Exception e) {
				gotologin = driver.findElement(By.cssSelector("div#J_tip_qr a.switch-tip-btn"));
				gotologin.click();
				logger.error("支付宝登陆按钮再次点击");
			}
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
			wait = new WebDriverWait(driver, WAIT_TIME);
			By byby = By.cssSelector(".order-number");
			WebElement orderElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byby));
			logger.debug("--->找到商品订单号 = "+orderElement.getText());
			String mallOrderNo = ExpressUtils.regularExperssNo(orderElement.getText());
			logger.debug("--->找到商品订单号1 = "+mallOrderNo);
			data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, mallOrderNo);
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
			driver.navigate().to("http://www.perfumesclub.cn/sales/order/history/");
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
							WebElement w = panel.findElement(By.cssSelector(".order-number"));
							if(w.getText().contains(mallOrderNo.trim())){
								isFind = true;
								
								//判断订单是否取消
								try{
									String s = panel.findElement(By.cssSelector(".non-payment")).getText();
									if(StringUtil.isNotEmpty(s) && s.contains("已取消")){
										logger.error("--->商城订单:"+mallOrderNo+"已取消");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
									}else if(StringUtil.isNotEmpty(s) && s.contains("已付款")){
										logger.error("--->商城订单:"+mallOrderNo+"还没有发货");
										return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
									}
								}catch(Exception e){
									
								}
								
								panel.findElement(By.cssSelector(".order_view a")).click();;
								break;
							}
						}catch(Exception e){}
					}
				}
				if(isFind){
					isFindMall = true;
//					物流单号:SRYA05838
					try{
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".track_number")));
						List<WebElement> shipments = driver.findElements(By.cssSelector(".track_number"));
						for(WebElement w:shipments){
							if(w.getText().startsWith("E")){
								String expressNo = w.getText();
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EMS");
								logger.error("--->找到物流单号 = "+w.getText());
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}
						}
						WebElement shipment = shipments.get(shipments.size()-1);
						logger.error("--->找到物流单号 = "+shipment.getText());
						String expressNo = shipment.getText();
										
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "postnl");
						if(expressNo.startsWith("E")){
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "EMS");
						}
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}catch(Exception e){
						logger.error("--->商城订单:"+mallOrderNo+"还没有发货",e);
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY; 
					}
				}
			}catch(Exception e){
				logger.error("--->查询订单出错", e);
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
			try{
				int page = i+2;
				String url= String.format("http://www.perfumesclub.cn/sales/order/history/?p=%d", page);
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
			driver.get("http://www.perfumesclub.cn/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav-wrapper")));
			Utils.sleep(1000);
		}
		catch (Exception e){
			logger.error("--->跳转perfumesclub主页面碰到异常");
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
