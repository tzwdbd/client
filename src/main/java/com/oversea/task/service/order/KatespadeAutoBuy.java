package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.ExpressNode;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.service.express.ExpressSpiderFactory;
import com.oversea.task.service.express.UPSExpressSpider;
import com.oversea.task.service.express.entity.ResultItem;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class KatespadeAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
    private String homeUrl = "https://www.katespade.com/" ;
    
    private boolean isLoginSuccessWWW = true ;
    
    //private Timer timer;
    
	public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	public static void mainText(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/123/KateSpade.htm");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		List<WebElement> orders = driver.findElements(By.xpath("//table[@class='item-list order-details-info-top']"));
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains("06855477")){
				try{
					WebElement orderStatusEle = order.findElement(By.xpath(".//tbody/tr/td[@class='details order-status']"));
					String orderStatus = orderStatusEle.getText() ;
					if(StringUtil.isBlank(orderStatus)){
						System.out.println("找不到订单号："+"06855477");
					}else if("being processed".equals(orderStatus.toLowerCase())){
						System.out.println("商城还未发货，订单号："+"06855477");
					}else if("canceled".equals(orderStatus.toLowerCase())){
						System.out.println("商城砍单，商城订单号："+"06855477");
					}else if("shipped".equals(orderStatus.toLowerCase())){
						WebElement td = order.findElement(By.xpath(".//tbody/tr/td[@class='trackingnumber details']"));
						String trackNo = td.getText() ;
						System.out.println("已发货，开始查找物流单号");
						if(StringUtil.isBlank(trackNo)){
							System.out.println("找不到订单号");
						}else{
							System.out.println("找到物流单号："+trackNo);
							//查找国外的物流节点
							UPSExpressSpider upsExpressSpider = ExpressSpiderFactory.getUPSExpressSpider() ;
							ArrayList<ResultItem> list = upsExpressSpider.spiderByExpressNo(trackNo) ;
							if(list!=null&&list.size()>0){
								List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
								for(ResultItem resultItem:list){
									String context = resultItem.getContext() ;
									ExpressNode expressNode = new ExpressNode();
									expressNode.setExpressNo(trackNo);
									expressNode.setName(context);
									expressNode.setOccurTime(resultItem.getOccurTime());
									if(context.toLowerCase().contains("delivered")){
										expressNode.setStatus(14);//已签收
									}else{
										expressNode.setStatus(3);
									}
									nodeList.add(expressNode);
								}
							}
						}
					}
				}catch(Exception e){
					System.out.println("该订单还没发货,没产生物流单号");
				}
			}
		}
	}
	public static void main(String[] args){
		/*FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/666/Kate%20Sale.htm");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//点击更改
		try{
			WebElement changeBillingAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='ChangeBillingAddress']")));
			Utils.sleep(1000);
			changeBillingAddress.click();
		}catch(Exception e){
			System.out.println("--->没有找到更改账单地址按钮");
		}
		try{
			//更改账单地址ChangeBillingAddress
			Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dwfrm_billing_addressList"))));
			List<WebElement> selectList = select.getOptions() ;
			if(selectList!=null&&selectList.size()>0){
				for(WebElement oneSelect:selectList){
					String value = oneSelect.getAttribute("value") ;
					if(!StringUtil.isBlank(value)&&value.toUpperCase().equals("BILLING")){
						select.selectByValue(value);
						break ;
					}
				}
			}
		}catch(Exception e){
			System.out.println("--->更改账单地址出现异常:"+e);
		}
		//滚动6次
		for(int i=0;i<6;i++){
			driver.executeScript("(function(){window.scrollBy(0,150);})();");
			Utils.sleep(1000);
		}
		
		//寻找信用卡付款
		try{
			WebElement payment = driver.findElement(By.xpath("//input[@id='is-CREDIT_CARD']"));
			System.out.println("--->找到信用卡付款");
			Utils.sleep(1500);
			payment.click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='goToPayPanel']")));
			Utils.sleep(1500);
		}catch(Exception e){
			System.out.println("--->没有找到信用卡付款");
		}*/
		
		
		KatespadeAutoBuy auto = new KatespadeAutoBuy();
		auto.setHomeUrl("http://www.katespade.com");
		AutoBuyStatus status = auto.login("rablfg2@126.com", "tfb001001");
//		//AutoBuyStatus status = auto.login("oxoxbnm@163.com", "tfb001001");
//		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
//			status = auto.cleanCart();
//			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				//param.put("url", "http://surprise.katespade.com/WKRU3255-2.html?pid=WKRU3255-2");//下架商品
				//param.put("url", "https://www.katespade.com/products/cedar-street-nika/PWRU4193-1.html?pid=PWRU4193-1");//售罄商品
				//param.put("url", "https://www.katespade.com/products/mock-neck-knit-flounce-dress/NJMU6948.html?cgid=ks-clothing-dresses-view-all&dwvar_NJMU6948_color=098#start=3&cgid=ks-clothing-dresses-view-all");//正常商品
				param.put("url", "https://www.katespade.com/products/daniels-drive-wendi/PXRU7741-1.html");//正常商品
				param.put("orginalUrl", "https://www.katespade.com/products/spice-things-up-snake-wrap-bracelet/WBRUD898.html?pid=WBRUD898");//正常商品
				//param.put("url", "http://www.rebatesme.com/zh/click/?key=633e2036f0228f010b8ea3961e8db42f&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=c99cba6462c9c3650990fa4224bd24e5&targetUrl=http%3A%2F%2Fsurprise.katespade.com%2Fon%2Fdemandware.store%2FSites-KateSale-Site%2Fen_US%2FProduct-Show%3Fpid%3DWLRU2737");//售罄商品
				//param.put("url", "http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=O0RU1099");//正常商品
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				//param.put("sku", "[[\"color\",\"black\"],[\"size\",\"7\"]]");
				param.put("sku", "[[\"color\",\"prickly pear\"]]");
				param.put("num", "1");
				auto.selectProduct(param);
				//Map<String, String> params = new HashMap<String, String>();
				//param.put("url", "http://surprise.katespade.com/WKRU3255-2.html?pid=WKRU3255-2");//下架商品
				//param.put("url", "https://www.katespade.com/products/cedar-street-nika/PWRU4193-1.html?pid=PWRU4193-1");//售罄商品
				//param.put("url", "https://www.katespade.com/products/mock-neck-knit-flounce-dress/NJMU6948.html?cgid=ks-clothing-dresses-view-all&dwvar_NJMU6948_color=098#start=3&cgid=ks-clothing-dresses-view-all");//正常商品
				//params.put("url", "http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=WKRU4170");//正常商品
				//param.put("url", "http://www.rebatesme.com/zh/click/?key=633e2036f0228f010b8ea3961e8db42f&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=c99cba6462c9c3650990fa4224bd24e5&targetUrl=http%3A%2F%2Fsurprise.katespade.com%2Fon%2Fdemandware.store%2FSites-KateSale-Site%2Fen_US%2FProduct-Show%3Fpid%3DWLRU2737");//售罄商品
				//param.put("url", "http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=O0RU1099");//正常商品
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				//param.put("sku", "[[\"color\",\"black\"],[\"size\",\"7\"]]");
//				params.put("sku", "[[\"color\",\"black/sweetheart pink\"]]");
//				params.put("num", "1");
				//auto.selectProduct(params);
//				System.out.println(status);
//				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
//					Map<String, String> param0 = new HashMap<String, String>();
//					param0.put("my_price", "213");
//					param0.put("count", "1");
//					param0.put("isPay", String.valueOf(false));
//					param0.put("suffixNo", "123");
//					param0.put("promotion", "BUBBLY");
//					//param0.put("cardNo", "4662 4833 6029 1396");
//					status = auto.pay(param0);
//				}
//			}
		//}
		//auto.logout();
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("08556754");
//		detail.setProductSku("[[\"color\",\"001 Pink\"]]");
//		auto.scribeExpress(detail);
	}
	
	public static void main0(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/123456/Kate%20Sale.htm");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//更改账单地址ChangeBillingAddress
		WebElement changeBillingAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='ChangeBillingAddress']")));
		Utils.sleep(1000);
		changeBillingAddress.click();
		Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dwfrm_billing_addressList"))));
		List<WebElement> selectList = select.getOptions() ;
		if(selectList!=null&&selectList.size()>0){
			for(WebElement oneSelect:selectList){
				String value = oneSelect.getAttribute("value") ;
				if(!StringUtil.isBlank(value)&&value.toUpperCase().equals("BILLING")){
					select.selectByValue(value);
					break ;
				}
			}
		}
	}
	
	public static void mainPromotion(String[] args) {
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/KateSpade.html");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		Set<String> promotionList = new HashSet<String>() ;
		promotionList.add("123") ;
		promotionList.add("BUBBLY123") ;
		promotionList.add("BUBBLY") ;
		boolean previousSuccess = true ;
		//使用优惠码0 失效,1互斥 ,9没修改过,10有效
		if(promotionList != null && promotionList.size() > 0){
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				if(StringUtil.isNotEmpty(code)){
					code = code.trim();
					//上一个优惠码成功需要重新点击
					if(previousSuccess){
						try{
							WebElement clickCoupon = driver.findElement(By.xpath("//div[@class='cart-coupon-code ']"));
							clickCoupon.click();
							Utils.sleep(2000);
						}catch(Exception e){
							WebElement clickCoupon = driver.findElement(By.xpath("//div[@class='cart-coupon-code']"));
							clickCoupon.click();
							Utils.sleep(2000);
						}
					}
					//输入优惠码	
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@id='dwfrm_billing_couponCode']"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//button[@name='dwfrm_billing_applyCoupon']")).click();
						Utils.sleep(5500);
						//如果有发现错误
						try{
							WebElement codeMessage = driver.findElement(By.xpath("//span[@class='coupon-code-error']"));
							String codeText = codeMessage.getText() ;
							if(!StringUtil.isBlank(codeText)&&codeText.contains("is already in your cart")){
								//已经用过优惠码了
								statusMap.put(code, 10);
								previousSuccess = true ;
								isEffective = true;
							}else{
								statusMap.put(code, 0);
								previousSuccess = false ;
							}
						}catch(Exception ee){
							//成功处理
							driver.findElement(By.xpath("//span[@class='price-promo-message']"));
							statusMap.put(code, 10);
							previousSuccess = true ;
							isEffective = true;
						}
					}catch(Exception e){
						System.out.println("输入优惠码的时候错误"+e);
					}
				}
			}
		}
	}
	
	public static void main11(String[] args){
		/*KatespadeAutoBuy auto = new KatespadeAutoBuy();
		AutoBuyStatus status = auto.login("415407279@qq.com", "ws153,.");*/
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("https://www.katespade.com/products/surie-boots/888445712654.html");
		
		WebElement e = driver.findElement(By.xpath("//div[@id='pdpQtySelectSelectBoxItArrowContainer']"));
		e.click();
		
		Utils.sleep(1000);
		WebElement selectOptions = driver.findElement(By.xpath("//ul[@id='pdpQtySelectSelectBoxItOptions']/li/a[@class='selectboxit-option-anchor' and contains(text(),'"+2+"')]"));
		selectOptions.click();
	}
	
	public static void mainPay(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/3/KateSpade.htm");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		System.out.println("--->开始等待信用卡确认");
		/*WebElement e1 = driver.findElement(By.xpath("//div[@id='primary']")) ;
		System.out.println(e1.getText());
		WebElement e2 = driver.findElement(By.xpath("//div[@id='primary']/div[@class='order-confirmation']")) ;
		System.out.println(e2.getText());*/
		WebElement e3 = driver.findElement(By.xpath("//div[@id='primary']/div[@class='order-confirmation']/div[@class='order-details']")) ;
		System.out.println(e3.getText());
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='primary']/div[@class='order-confirmation']/div[@class='order-details']")));
		System.out.println("--->confirm已经不可见");
		Utils.sleep(1000);
		System.out.println("--->开始查找商品订单号");
		List<WebElement> trs = driver.findElements(By.xpath("//div[@class='order-details-info']/table/tbody/tr")) ;
		if(trs!=null&&trs.size()>0){
			for(WebElement tr:trs){
				try{
					WebElement td = tr.findElement(By.xpath("./td[@class='label']"));
					String text = td.getText() ;
					if(!StringUtil.isBlank(text)&&text.toUpperCase().contains("ORDER NUMBER")){
						WebElement td0 = tr.findElement(By.xpath(".//td[@class='details']"));
						System.out.println("--->找到商品订单号 = "+td0.getText());
					}
				}catch(Exception e){
					//System.out.println(e) ;
				}
			}
		}else{
			System.out.println("--->页面不是成功下单页面");
		}
	}

	public KatespadeAutoBuy() {
		super(false) ;
		
		try {
			driver.manage().deleteAllCookies();
			
//			timer = new Timer();
//			timer.schedule(new TimerTask(){
//				@Override
//				public void run(){
//					try{
//						driver.executeScript("(function(){var els = document.getElementsById('sr_header_close');if(els && els[0]){els[0].click();}})();");
//					}catch(Exception e){}
//				}
//			}, 3000, 3000);
		} catch (Exception e) {
			logger.error("--->初始化失败", e);
			return;
		}
	}
	
	@Override
	public boolean logout(boolean isScreenShot){
		super.logout(isScreenShot);
		//timer.cancel();
		return true;
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		List<RobotOrderDetail> list = super.getOrderDetailList() ;
		if(list!=null&&list.size()>0){
			RobotOrderDetail detail = list.get(0) ;
			String productUrl = detail.getProductUrl() ;
			logger.error("productUrl:"+productUrl);
			if(!StringUtil.isBlank(productUrl)){
				if(productUrl.startsWith("http://surprise")){
					this.homeUrl= "http://surprise.katespade.com/" ;
					isLoginSuccessWWW = false ;
				}
				if(productUrl.startsWith("https://surprise")){
					this.homeUrl= "https://surprise.katespade.com/" ;
					isLoginSuccessWWW = false ;
				}
			}
		}else{
			logger.error("list is null");
		}
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get(this.homeUrl);
		
		//如果有弹窗，关闭弹窗
		try{
			Utils.sleep(3000);
			//By byAlert= By.xpath("//a[@id='bx-close-inside-314953']");
			By byAlert= By.xpath("//a[@class='bx-close-link']");
			WebElement alert = driver.findElement(byAlert);
			logger.debug("--->关闭弹窗");
			alert.click();
		}catch(Exception e){
			
		}
		//如果需要验证邮编
		try{
			WebElement email = driver.findElement(By.xpath("//input[@id='dwfrm_loginbarrier_emailAddress']"));
			logger.debug("--->开始输入邮箱");
			Utils.sleep(1500);
			email.sendKeys(userName);
			WebElement zip = driver.findElement(By.xpath("//input[@id='dwfrm_loginbarrier_postalCode']"));
			logger.debug("--->开始输入邮编");
			Utils.sleep(1500);
			zip.sendKeys("97218-2862");
			//提交
			WebElement submit = driver.findElement(By.xpath("//button[@id='barrierSubmitBtn']"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submit.sendKeys(Keys.RETURN);
			submit.click();
		}catch (Exception e){
			
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//点击登录
		try{
//			By bySignIn = By.xpath("//a[contains(text(),'Log In or Register')]");
			By bySignIn = By.xpath("//a[@id='loginHopupLink']");
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(bySignIn));
			Utils.sleep(1500);
			logger.debug("--->跳转到登录页面");
			signIn.click();
		}catch (Exception e){
			//1、活动结束去www页面
			driver.get("http://www.katespade.com/") ;
			//2、如果有弹窗，关闭弹窗
			try{
				Utils.sleep(3000);
				//By byAlert= By.xpath("//a[@id='bx-close-inside-314953']");
				By byAlert= By.xpath("//a[@class='bx-close-link']");
				WebElement alert = driver.findElement(byAlert);
				logger.debug("--->关闭弹窗");
				alert.click();
			}catch(Exception ee){
				
			}
			//3、点击登录
			try{
				By bySignIn = By.xpath("//a[@id='loginHopupLink']");
				WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(bySignIn));
				Utils.sleep(1500);
				logger.debug("--->跳转到登录页面");
				signIn.click();
			}catch (Exception e1){
				logger.error("--->没有找到登陆按钮", e1);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
			isLoginSuccessWWW = true ;
			
		}
		
		// 等到[输入框]出现
		try{
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@class='input-text email left required']")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.xpath("//input[@id='dwfrm_login_password']"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.xpath("//button[@class='apply-button']"));
			logger.debug("--->开始提交");
			Utils.sleep(1500);
			submit.click();
		}catch (Exception e){
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		//等待登录完成
		try{
			logger.debug("--->等待登录完成");
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'Logout')]")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@class='account-link']")));
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
			driver.get("https://www.katespade.com/shopping-bag");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("main")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			
			//查看是否有弹窗
			try{
				driver.findElement(By.cssSelector("a.sr_UI_close")).click();
				logger.debug("--->关闭弹窗");
				Utils.sleep(1500);
			}catch(Exception ee){}
			try{
				driver.findElement(By.cssSelector("div#sr_header_close")).click();
				logger.debug("--->关闭弹窗");
				Utils.sleep(1500);
			}catch(Exception ee){}
			
			this.goodsRemove();
			Utils.sleep(2000);
			logger.error("--->购物车页面清理完成");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		try {
			logger.error("--->确认购物车是否清空");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-empty")));
		} catch (Exception e) {
			logger.debug("--->购物车不为空！");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	private void goodsRemove(){
		List<WebElement> list = driver.findElements(By.xpath("//button[@value='Remove']"));
		if(list != null && list.size() > 0){
			WebElement remove = list.get(0) ;
			Utils.sleep(3000);
			if(remove != null){
				remove.click();
			}
			goodsRemove() ;
		}
	}
	
	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		String orginalUrl = (String) param.get("orginalUrl");
		
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try
		{
			driver.navigate().to(productUrl);
		}
		catch (Exception e){
			logger.debug("--->打开商品页面失败1 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		if(orginalUrl.contains("surprise")){
			try
			{
				driver.navigate().to(orginalUrl);
			}
			catch (Exception e){
				logger.debug("--->打开商品页面失败2 = " + orginalUrl);
				return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
			}
		}
		
		//商品页面404,商品也是下架
		try{
			driver.findElement(By.xpath("//title[contains(text(),'Invalid URL')]"));
			logger.debug("--->这款商品的页面404错误,找不到该款商品");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){}
		
		//等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='main']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		//如果有弹窗，关闭弹窗
		try{
			Utils.sleep(3000);
			//By byAlert= By.xpath("//a[@id='bx-close-inside-314953']");
			By byAlert= By.xpath("//a[@class='bx-close-link']");
			WebElement alert = driver.findElement(byAlert);
			logger.debug("--->关闭弹窗");
			alert.click();
		}catch(Exception e){
			
		}
		
		try {
			List<WebElement> mainList = driver.findElements(By.cssSelector("#main"));
			logger.debug("--->mainList size="+mainList.size());
			if(mainList.size()>1){
				driver.executeScript("arguments[0].remove();",mainList.get(0));
			}
		} catch (Exception e) {
			logger.debug("--->没有main");
		}
		
		/*
		//判断商品是否下架
		try{
			driver.findElement(By.xpath("//div[@class='searchNone']"));
			logger.debug("--->这款商品已经下架");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}*/
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		//开始选择sku
		int findCount = 0;
		logger.debug("--->开始选择sku");
		driver.executeScript("(function(){window.scrollBy(0,150);})();");
		try{
			if (sku != null){
				List<String> skuList = Utils.getSku((String) sku);
//				WebElement iframe = null;
//				try{
//					iframe = driver.findElement(By.xpath("//iframe[@class='scroll-frame-iframe']"));
//				}catch(Exception e){
//					iframe = null;
//					logger.debug("--->查找iframe错误",e);
//				}
//				 elements = null;
//				if(iframe != null){
//					elements = driver.switchTo().frame(iframe).findElements(By.xpath("//div[@class='product-variations']/ul/li"));
//				}else{
				 List<WebElement>	elements = driver.findElements(By.cssSelector(".product-variations ul li.attribute"));
				//}
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								String text = element.getText();
								if(!Utils.isEmpty(text)){
									text = text.toLowerCase();
									logger.error("text:"+text);
									String key = skuList.get(i-1).toLowerCase();
									if(text.startsWith(key)){
										try{
											WebElement ele = element.findElement(By.xpath(".//a[@title='"+skuList.get(i)+"']"));
											ele.click();
											logger.error("选择了:"+key);
											findCount ++;
											elements = driver.findElements(By.cssSelector(".product-variations ul li.attribute"));
											Utils.sleep(1000);
											break;
										}catch(Exception e){
											logger.debug("--->找不到指定的sku no success= "+(skuList.get(i-1)+":"+skuList.get(i)));
											return AutoBuyStatus.AUTO_SKU_NOT_FIND;
										}
									}
								}
							}catch(Throwable e){
								logger.error("选择sku出错",e);
							}
						}
					}
				}
				int t = skuList.size()/2;
				if(findCount < t){
					logger.error("findCount = "+findCount +" && skuList.size() = "+skuList.size());
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
				}
				elements = driver.findElements(By.cssSelector(".product-variations ul li.attribute"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								String text = element.getText();
								if(!Utils.isEmpty(text)){
									text = text.toLowerCase();
									logger.error("text1:"+text);
									logger.error("skuvalue:"+skuList.get(i));
									String key = skuList.get(i-1).toLowerCase();
									if(text.startsWith(key)){
										if(!text.contains(skuList.get(i))){
											return AutoBuyStatus.AUTO_SKU_NOT_FIND;
										}
									}
								}
							}catch(Throwable e){
								logger.error("选择sku出错",e);
							}
						}
					}
				}
				
			}
		}catch(Exception e){
			logger.debug("--->选择sku碰到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		logger.debug("--->选择sku完成");
		
		//寻找商品单价
		try{
			logger.debug("--->开始寻找商品单价");
			WebElement priceElement = driver.findElement(By.xpath("//div[@id='product-content']/div/span[@class='price-sales']"));
			String text = priceElement.getText();
			String productEntityId = param.get("productEntityId");
			if(!Utils.isEmpty(text) && text.startsWith("$") && StringUtil.isNotEmpty(productEntityId)){
				logger.debug("--->找到商品单价 = "+text.substring(1));
//				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, text.substring(1));
				priceMap.put(productEntityId, text.substring(1));
			}
		}catch(Exception e){
			logger.debug("--->查询商品单价异常",e);
		}
		
		
		//判断商品是否下架
//		try{
//			Utils.sleep(3000);
//			WebElement ppov = driver.findElement(By.xpath("//div[@id='oosPopover']"));
//			if(ppov != null && ppov.isDisplayed()){
//				logger.debug("--->商品这款sku已经售完");
//				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE; 
//			}
//		}catch(Exception e){
//			logger.debug("--->商品没有售罄,carry on");
//		}
		//滚动2次
		for(int i=0;i<2;i++){
			driver.executeScript("(function(){window.scrollBy(0,150);})();");
			Utils.sleep(1000);
		}
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				Utils.sleep(2000);
 				WebElement selectContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='pdpQtySelectSelectBoxIt']")));
//				WebElement selectContainer = driver.findElement(By.xpath("//div[@id='pdpQtySelectSelectBoxIt']"));
				selectContainer.click();
/*				WebElement selectContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pdpQtySelectSelectBoxIt")));
				driver.executeScript("var tar=arguments[0];tar.click();", selectContainer);*/
//				driver.executeScript("(function(){var els = document.getElementById('pdpQtySelectSelectBoxIt');if(els && els[0]){els[0].click();}})();");
				//selectContainer.sendKeys(Keys.ENTER);
				Utils.sleep(1000);
				WebElement selectOptions = driver.findElement(By.xpath("//ul[@id='pdpQtySelectSelectBoxItOptions']/li/a[@class='selectboxit-option-anchor' and contains(text(),'"+productNum+"')]"));
				selectOptions.click();
				logger.debug("--->选择商品数量完成");
			}catch(Exception e){
				logger.debug("--->选择商品数量碰到异常",e);
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		/*
		//查询商品数量是否可买
		try{
			driver.findElement(By.xpath("//div[@id='jsError']/h3"));
			logger.debug("--->选择商品数量太多，不支付一次性购买这么多件商品");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}catch(Exception e){
			logger.debug("--->商品数量没有问题,carry on");
		}
		*/
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			WebElement cart = driver.findElement(By.id("add-to-cart"));
			String text = cart.getText();
			logger.error("text = "+text);
			if(StringUtil.isNotEmpty(text)){
				text = text.toLowerCase();
				if(text.contains("out of stock")){
					logger.debug("--->商品这款sku已经售完");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
				if(text.contains("add to bag")){
					driver.executeScript("var tar=arguments[0];tar.click();", cart);
					//cart.click();
					logger.debug("--->加购物车成功");
				}
			}else{
				logger.debug("--->加购物车按钮找不到");
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		try{
			Utils.sleep(1000);
			WebElement soldout = driver.findElement(By.xpath("//div[@class='availability-msg purchase-limit-exceeded']"));
			String text = soldout.getText();
			if(StringUtil.isNotEmpty(text)){
				text = text.toLowerCase();
				if(text.contains("sold out")){
					logger.debug("--->这款商品sku已经售完");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			}
		}catch(Exception e){
			logger.debug("--->商品没有售罄");
		}
		//滚动页面
		driver.executeScript("(function(){window.scrollBy(0,250);})();");
		//去购物车页面
		logger.debug("--->去购物车页面");
		boolean mark = true;
		try{
			List<WebElement> list = driver.findElements(By.cssSelector("a.mini-cart-link-cart"));
			if(list != null && list.size() > 0){
				for(WebElement w : list){
					logger.debug("--->test = "+w.getText()+"--->uptest:"+w.getText().toUpperCase());
					if(StringUtil.isNotEmpty(w.getText()) && w.getText().toUpperCase().contains("VIEW BAG")){
						logger.debug("--->checkout = "+w.getText());
						driver.executeScript("var tar=arguments[0];tar.click();", w);
						//w.click();
						mark = false;
						Utils.sleep(1000);
						break;
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->加载bag-label出现异常");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		if(mark){
			logger.debug("--->没点击view bag");
			driver.executeScript("(function(){window.scrollBy(0,-50);})();");
			try {
				logger.debug("--->加购重试");
				WebElement cart = driver.findElement(By.id("add-to-cart"));
				cart.click();
				List<WebElement> list = driver.findElements(By.cssSelector("a.mini-cart-link-cart"));
				if(list != null && list.size() > 0){
					for(WebElement w : list){
						if(StringUtil.isNotEmpty(w.getText()) && w.getText().contains("VIEW BAG")){
							logger.debug("--->checkout重试 = "+w.getText());
							w.click();
							mark = false;
							Utils.sleep(1000);
							break;
						}
					}
				}
			} catch (Exception e) {
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
			if(mark){
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
			
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		logger.error("--->myPrice = "+myPrice);
		
		Utils.sleep(5500);
		try{
			driver.findElement(By.cssSelector("a.sr_UI_close")).click();
			logger.debug("--->关闭弹窗");
			Utils.sleep(1500);
		}catch(Exception ee){}
		try{
			driver.findElement(By.cssSelector("div#sr_header_close")).click();
			logger.debug("--->关闭弹窗");
			Utils.sleep(1500);
		}catch(Exception ee){}
		
		String countTemp = (String)param.get("count");
		int count = 0;
		if(!Utils.isEmpty(countTemp)){
			count = Integer.parseInt(countTemp);
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//去选收货地址页面
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='main']")));
			logger.debug("--->购物车页面加载完成");
			driver.executeScript("(function(){window.scrollBy(300,500);})();");
			Utils.sleep(3000);
			
			//优惠码
			String promotionStr = param.get("promotion");
			Set<String> promotionList = getPromotionList(promotionStr);
			//使用优惠码0 失效,1互斥 ,9没修改过,10有效
			WebDriverWait wait0 = new WebDriverWait(driver, 30);
			if(promotionList != null && promotionList.size() > 0){
				boolean isEffective = false;
				HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
				for(String code : promotionList){
					if(StringUtil.isNotEmpty(code)){
						driver.executeScript("(function(){window.scrollBy(0,-150);})();");
						code = code.trim();
						wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-coupon-code")));
						WebElement clickCoupon = driver.findElement(By.cssSelector(".cart-coupon-code"));
						clickCoupon.click();
						Utils.sleep(2000);
						//输入优惠码	
						try{
							wait0.until(ExpectedConditions.visibilityOfElementLocated(By.id("dwfrm_cart_couponCode")));
							WebElement codeInput = driver.findElement(By.id("dwfrm_cart_couponCode"));
							codeInput.clear();
							Utils.sleep(2500);
							codeInput.sendKeys(code);
							Utils.sleep(1500);
							driver.findElement(By.id("add-coupon")).click();
							Utils.sleep(5500);
							//如果有发现错误
							try{
								wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
								driver.findElement(By.cssSelector(".error"));
								statusMap.put(code, 0);
								logger.error("优惠码失效:"+code);
							}catch(Exception ee){
								//成功处理
								driver.findElement(By.cssSelector(".discount"));
								statusMap.put(code, 10);
								isEffective = true;
							}
						}catch(Exception e){
							logger.error("输入优惠码的时候错误",e);
						}
					}
				}
				setPromotionCodelistStatus(statusMap);
				
				if("true".equals(param.get("isStock")) && !isEffective){
					logger.debug("--->囤货订单优惠码失效,中断采购");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
			Utils.sleep(3000);
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[name='dwfrm_cart_checkoutCart']")));
			WebElement checkCart = driver.findElement(By.cssSelector("button[name='dwfrm_cart_checkoutCart']"));
			driver.executeScript("var tar=arguments[0];tar.click();", checkCart);
		}catch(Exception e){
			logger.debug("--->购物车页面加载异常",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		Utils.sleep(5000);
		
		//选收货地址
		By by = By.xpath("//select[@id='dwfrm_singleshipping_addressList']/option");
		try{
			List<WebElement> list = driver.findElements(by);
			if(list != null && list.size() > 0){
				int tarAddr = count % 4;
				WebElement ele = list.get(tarAddr+1);
				Utils.sleep(1500);
				ele.click();
			}else{
				logger.debug("--->该账号没有设置收货地址1");
				driver.findElement(By.cssSelector("button[name='dwfrm_cart_checkoutCart']")).click();
				list = driver.findElements(by);
				if(list != null && list.size() > 0){
					int tarAddr = count % 4;
					WebElement ele = list.get(tarAddr+1);
					Utils.sleep(1500);
					ele.click();
				}else{
					logger.debug("--->该账号没有设置收货地址2");
					driver.executeScript("var tar=arguments[0];tar.click();", driver.findElement(By.cssSelector("button[name='dwfrm_cart_checkoutCart']")));
					list = driver.findElements(by);
					if(list != null && list.size() > 0){
						int tarAddr = count % 4;
						WebElement ele = list.get(tarAddr+1);
						Utils.sleep(1500);
						ele.click();
					}else{
						logger.debug("--->该账号没有设置收货地址3");
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->选择地址出错 = ",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		//去选择快递方式
		logger.debug("--->去选择快递方式页面");
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@id='single_shipping_form']")));
			Utils.sleep(1500);
			driver.executeScript("var tar=arguments[0];tar.click();", checkout);
			//checkout.click();
		}catch(Exception e){
			logger.debug("--->加载continue出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		try{
			//选择KEEP ORIGINAL ADDRESS
			Utils.sleep(4000);
			List<WebElement> keeps = driver.findElements(By.xpath("//div[@id='radioText']/input[@value='0']"));
			for(WebElement keep:keeps){
				Utils.sleep(1000);
				try{
					keep.click();
				}catch(Exception e){
				}
			}
			List<WebElement> continueButtons = driver.findElements(By.xpath("//button[@id='verifyBtn']"));
			for(WebElement continueButton:continueButtons){
				Utils.sleep(1000);
				try{
					continueButton.click();
				}catch(Exception e){
				}
			}
		}catch(Exception e){
			logger.debug("--->加载continue出现异常");
		}
		Utils.sleep(4500);
		
		//关闭弹框
		try{
			driver.findElement(By.cssSelector("a.sr_UI_close")).click();
			logger.debug("--->关闭弹窗");
			Utils.sleep(1500);
		}catch(Exception ee){}
		try{
			driver.findElement(By.cssSelector("div#sr_header_close")).click();
			logger.debug("--->关闭弹窗");
			Utils.sleep(1500);
		}catch(Exception ee){}
		
		driver.executeScript("(function(){window.scrollBy(0,200);})();");
		Utils.sleep(3500);
		//去更改账单地址
		try{
			logger.debug("--->去更改账单地址");
//			WebElement shippingMethodButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@name='dwfrm_multishipping_shippingOptions_save']")));
//			WebElement shippingMethodButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[name='dwfrm_multishipping_shippingOptions_save']")));
//			Utils.sleep(1000);
//			shippingMethodButton.click();
			WebElement shippingOptions = driver.findElement(By.cssSelector("button[name='dwfrm_multishipping_shippingOptions_save']"));
			driver.executeScript("var tar=arguments[0];tar.click();", shippingOptions);
			Utils.sleep(2000);
			logger.debug("--->已经点击过continue");
		}catch(Exception e){
			logger.debug("--->更改账单地址出现异常:",e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//点击更改
//		try{
//			WebElement changeBillingAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a#ChangeBillingAddress")));
//			Utils.sleep(1000);
//			changeBillingAddress.click();
//		}catch(Exception e){
//			logger.debug("--->没有找到更改账单地址按钮",e);
//			return AutoBuyStatus.AUTO_PAY_FAIL;
//		}
//		try{
//			//更改账单地址ChangeBillingAddress
//			Select select = new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dwfrm_billing_addressList"))));
//			List<WebElement> selectList = select.getOptions() ;
//			if(selectList!=null&&selectList.size()>0){
//				for(WebElement oneSelect:selectList){
//					String value = oneSelect.getAttribute("value") ;
//					if(!StringUtil.isBlank(value)&&value.toUpperCase().equals("BILLING")){
//						select.selectByValue(value);
//						break ;
//					}
//				}
//			}
//		}catch(Exception e){
//			logger.debug("--->更改账单地址出现异常:",e);
//			return AutoBuyStatus.AUTO_PAY_FAIL;
//		}
		//滚动5次
		for(int i=0;i<5;i++){
			driver.executeScript("(function(){window.scrollBy(0,150);})();");
			Utils.sleep(1000);
		}
		
		//寻找信用卡付款
		try{
			WebElement payment = driver.findElement(By.xpath("//input[@id='is-CREDIT_CARD']"));
			logger.debug("--->找到信用卡付款");
			Utils.sleep(1500);
			driver.executeScript("var tar=arguments[0];tar.click();", payment);
			//payment.click();
			/*wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='goToPayPanel']")));
			Utils.sleep(1500);*/
		}catch(Exception e){
			logger.debug("--->没有找到信用卡付款");
		}

		try{
			driver.executeScript("(function(){var els = document.getElementById('is-CREDIT_CARD');if(els){els.checked=true;}})();");
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->",e);
		}
		//信用卡安全码
		try {
			Utils.sleep(5000);
			WebElement securityCode = driver.findElement(By.xpath("//input[@id='dwfrm_billing_paymentMethods_creditCard_cvn']"));
			logger.debug("--->找到信用卡安全码输入框,开始输入");
			String cardCode = (String) param.get("suffixNo");
			logger.debug("--->信用卡安全码是 = " + cardCode);
			securityCode.sendKeys(cardCode);
			Utils.sleep(1500);
			logger.debug("--->输入信用卡安全码结束");
		} catch (Exception e) {
			logger.debug("--->没找到信用卡安全码输入框");
		}
		
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			List<WebElement> trs = driver.findElements(By.xpath("//div[@class='checkout-order-totals']/table/tbody/tr[@class='order-total']")) ;
			for(WebElement tr:trs){
				try{
					WebElement td = tr.findElement(By.xpath("./td"));
					String text1 = td.getText() ;
					if(!StringUtil.isBlank(text1)&&text1.toUpperCase().contains("ORDER TOTAL")){
						WebElement td0 = tr.findElement(By.xpath(".//td[@class='price']"));
						String text = td0.getText() ;
						logger.debug("--->找到商品总价 = "+text);
						if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
							String priceStr = text.substring(text.indexOf("$")+1);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
							logger.debug("--->找到商品结算总价 = "+priceStr);
							BigDecimal x = new BigDecimal(myPrice);
							BigDecimal y = new BigDecimal(priceStr);
							BigDecimal v = y.subtract(x);
							if (v.doubleValue() > 5.00D){
								logger.error("--->总价差距超过约定,不能下单");
								return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
							}
						}
					}
				}catch(Exception e){
					//System.out.println(e) ;
				}
			}
			/*WebElement totalPriceElement = driver.findElement(By.xpath("//span[@id='subtotals-marketplace-spp-bottom']"));
			String text = totalPriceElement.getText();
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = "+priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 20.00D){
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
			}*/
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
		}
		//确认
		try{
			//输入信用卡点确定
			driver.executeScript("(function(){window.scrollBy(0,300);})();");
			Utils.sleep(3500);
//			WebElement confirm = driver.findElement(By.xpath("//div[@id='submitOrderButton']/button"));
			WebElement confirm = driver.findElement(By.cssSelector("#submitOrderButton button"));
			Utils.sleep(1500);
			confirm.click();
		}catch(Exception e){
			logger.debug("--->没找到信用卡输入框"+e);
		}
		
		//查询商城订单号
		try{
			logger.debug("--->开始等待信用卡确认");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("primary")));
			logger.debug("--->confirm已经不可见");
			Utils.sleep(1000);
			logger.debug("--->开始查找商品订单号");
			List<WebElement> trs = driver.findElements(By.xpath("//div[@class='order-details-info']/table/tbody/tr")) ;
			if(trs!=null&&trs.size()>0){
				for(WebElement tr:trs){
					try{
						WebElement td = tr.findElement(By.xpath("./td[@class='label']"));
						String text = td.getText() ;
						if(!StringUtil.isBlank(text)&&text.toUpperCase().contains("ORDER NUMBER")){
							WebElement td0 = tr.findElement(By.xpath(".//td[@class='details']"));
							logger.debug("--->找到商品订单号 = "+td0.getText());
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, td0.getText());
							savePng();
							return AutoBuyStatus.AUTO_PAY_SUCCESS;
						}
					}catch(Exception e){
						//System.out.println(e) ;
					}
				}
			}else{
				logger.debug("--->页面不是成功下单页面");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常",e);
			logger.debug("整1个html:"+driver.executeScript("var text = document.getElementsByTagName('html')[0].innerHTML;return text"));
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_FAIL;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.debug("--->开始跳转到my account页面");
			if(isLoginSuccessWWW){
				driver.navigate().to("https://www.katespade.com/on/demandware.store/Sites-Shop-Site/en_US/Order-History");
			}else{
				driver.navigate().to("https://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Order-History");
			}
			
		}catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待my account页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='primary']")));
			Utils.sleep(1500);
			//滚动2次
			for(int i=0;i<2;i++){
				driver.executeScript("(function(){window.scrollBy(0,150);})();");
				Utils.sleep(1000);
			}
			logger.debug("--->my account页面加载完成");
		}catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
		}
		//查找物流
		AutoBuyStatus status = AutoBuyStatus.AUTO_SCRIBE_FAIL; 
		List<WebElement> orders = driver.findElements(By.xpath("//table[@class='item-list order-details-info-top']"));
		for (WebElement order:orders) {
			WebElement sectionHeader = order.findElement(By.cssSelector(".section-header"));
			String orderText = sectionHeader.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains(mallOrderNo)){
				try{
					WebElement orderStatusEle = order.findElement(By.xpath(".//tbody/tr/td[@class='details order-status']"));
					String orderStatus = orderStatusEle.getText() ;
					if(StringUtil.isBlank(orderStatus)){
						logger.debug("找不到订单号："+mallOrderNo);
						status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}else if("being processed".equals(orderStatus.toLowerCase())){
						logger.debug("商城还未发货，订单号："+mallOrderNo);
						status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY ;
					}else if("canceled".equals(orderStatus.toLowerCase())){
						logger.debug("商城砍单，商城订单号："+mallOrderNo);
						status = AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED ;
					}else if("shipped".equals(orderStatus.toLowerCase())){
						WebElement td = null;
						try {
							td = order.findElement(By.xpath(".//tbody/tr/td[@class='trackingnumber details']"));
						} catch (Exception e) {
							order.findElement(By.cssSelector(".details-link .linkbutton")).click();
							wait0.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".trackingnumber")));
							td = driver.findElement(By.cssSelector(".trackingnumber"));
						}
						
						String trackNo = td.getText() ;
						logger.debug("已发货，开始查找物流单号");
						if(StringUtil.isBlank(trackNo)){
							logger.debug("找不到订单号");
							status = AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}else{
							logger.debug("找到物流单号："+trackNo);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "UPS");
							return  AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							//查找国外的物流节点
//							UPSExpressSpider upsExpressSpider = ExpressSpiderFactory.getUPSExpressSpider() ;
//							ArrayList<ResultItem> list = upsExpressSpider.spiderByExpressNo(trackNo) ;
//							if(list!=null&&list.size()>0){
//								List<ExpressNode> nodeList = new ArrayList<ExpressNode>();
//								for(ResultItem resultItem:list){
//									String context = resultItem.getContext() ;
//									ExpressNode expressNode = new ExpressNode();
//									expressNode.setOrderNo(detail.getOrderNo());
//									expressNode.setExpressNo(trackNo);
//									expressNode.setName(context);
//									expressNode.setOccurTime(resultItem.getOccurTime());
//									if(context.contains("Delivered")){
//										expressNode.setStatus(14);//已签收
//									}else{
//										expressNode.setStatus(3);
//									}
//									nodeList.add(expressNode);
//								}
//								getTask().addParam("expressNodeList", nodeList);
//							}
						}
					}
				}catch(Exception e){
					logger.debug("该订单还没发货,没产生物流单号");
					status = AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
		}
		return status;
	}

	@Override
	public boolean gotoMainPage() {
		try{
			Utils.sleep(2000);
			driver.get("https://www.katespade.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(),'Logout')]")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='loginHopupLink']")));
		}
		catch (Exception e){
			logger.error("--->跳转katespade主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}
}
