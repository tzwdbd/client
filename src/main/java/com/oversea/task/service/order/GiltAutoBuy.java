package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class GiltAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args){
		GiltAutoBuy auto = new GiltAutoBuy();
		AutoBuyStatus status = auto.login("yoonaar@tom.com", "tfb001001");
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("86483019");
		auto.scribeExpress(detail );
		/*if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = auto.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				//param.put("url", "http://www.gilt.com/brand/saint-laurent-paris/product/1118170178-saint-laurent-paris-classic-hunting-star-backpack?origin=search");//下架商品
				//param.put("url", "http://www.backcountry.com/the-north-face-tomales-bay-jacket-womens?CMP_SKU=TNF02A6&MER=0406&skid=TNF02A6-SEDSGGRE-M&CMP_ID=PLA_GOc001&mv_pc=r101&utm_source=Google&utm_medium=PLA&mr:trackingCode=52452D2A-92BB-E511-80F3-005056944E17&mr:referralID=NA&mr:device=c&mr:adType=plaonline&mr:ad=92885917477&mr:keyword=&mr:match=&mr:tid=pla-94469057797&mr:ploc=2840&mr:iloc=&mr:store=&mr:filter=94469057797&gclid=CKqutOXMrs0CFZcRvQodQGkJ_A&gclsrc=aw.ds");//售罄商品
				//param.put("url", "https://www.katespade.com/products/mock-neck-knit-flounce-dress/NJMU6948.html?cgid=ks-clothing-dresses-view-all&dwvar_NJMU6948_color=098#start=3&cgid=ks-clothing-dresses-view-all");//正常商品
				//param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=4a75QTDvq2zGqWfIshiwPZQYMs83UOrMxTByxez6gJSlicdVnm0c4w4tc6vvmfbJWLaAPw_c_c&new=http%3A%2F%2Fwww.backcountry.com%2Farcteryx-gothic-glove%3Fskid%3DARC00DE-ROS-S%26ti%3DUExQIFJ1bGUgQmFzZWQ6QXJjJ3Rlcnl4IFNhbGU6NDozMzo%3D&tag=");//正常商品
				//param.put("url", "https://www.gilt.com/sale/women/cn16-designer-steals/product/1153657769-prada-galleria-double-zip-micro-saffiano-leather-tote?origin=sale");//正常商品
				param.put("url", "https://www.gilt.com/sale/men/drivers-1293/product/1147289633-millburn-co-bit-driver?origin=sale");//正常商品
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				param.put("sku", "[[\"color\",\"chocolate\"],[\"size\",\"8\"]]");
				//param.put("sku", "[[\"color\",\"pink\"]]");
				param.put("productEntityId", "112233");
				param.put("num", "2");
				status = auto.selectProduct(param);
				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
					Map<String, String> param0 = new HashMap<String, String>();
					param0.put("my_price", "1608");
					param0.put("count", "1");
					param0.put("isPay", String.valueOf(false));
					param0.put("suffixNo", "123");
					param0.put("cardNo", "4662 4833 6029 1396");
					param0.put("promotion", "hjasubyy;COUNTDOWN;COUNTDOWPP");
					status = auto.pay(param0);
				}
			}
		}
		auto.logout();*/
	}
	
	public static void main11(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/2/Receipt%20_%20Backcountry.com.htm");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//查询商城订单号
		try{
			WebElement orderNum = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//strong[@id='order-num']")));
			String text = orderNum.getText() ;
			if(!StringUtil.isBlank(text)){
				System.out.println("orderNum:"+text) ;
			}
		}catch(Exception e){
			//System.out.println(e) ;
		}
	}
	
	public static void mainShip(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/3/My%20Account%20_%20Backcountry.com.htm");
		List<WebElement> orders = driver.findElements(By.xpath("//div[@class='order js-order']"));
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains("b2528796681")){
				try{
					WebElement a = order.findElement(By.xpath(".//a[@class='track-link btn order-history__link--secondary']"));
					String href = a.getAttribute("href") ;
					String trackNo = href.split("=")[href.split("=").length-1] ;
					String trackCompany = href.split("\\.")[1] ;
					System.out.println("已发货，开始查找物流单号");
					if(StringUtil.isBlank(trackNo)){
						System.out.println("找不到订单号");
					}else{
						System.out.println("找到物流单号："+trackNo);
						System.out.println("找到物流公司："+trackCompany);
					}
				}catch(Exception e){
					System.out.println("该订单还没发货,没产生物流单号");
				}
			}
		}
	}
	
	public static void mainPromotion(String[] args) {
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("https://www.gilt.com/web-checkout/buynow/1181377126/6025921/1?store_id=2&add_to_cart_origin=sale&checkout_session_guid=dad77bdf-6e44-4836-bc8c-0852b40c2c9c#form-review-view");
		Set<String> promotionList = new HashSet<String>() ;
		promotionList.add("123") ;
		promotionList.add("SHIP99") ;
		promotionList.add("SHIP99675") ;
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
						WebElement clickCoupon = driver.findElement(By.xpath("//p[@class='promo-code-trigger']"));
						clickCoupon.click();
						Utils.sleep(2000);
					}
					//输入优惠码	
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@class='promo-code']"));
						codeInput.clear();
						Utils.sleep(1500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//button[@data-gilt-test='input-submit-apply-promo-code']")).click();
						Utils.sleep(5500);
						//如果有发现错误
						try{
							WebElement error = driver.findElement(By.xpath("//span[@class='error-message']"));
							if(error.isDisplayed()){
								statusMap.put(code, 0);
								previousSuccess = false ;
							}else{
								//成功处理
								statusMap.put(code, 10);
								previousSuccess = true ;
								isEffective = true;
							}
						}catch(Exception ee){
							//成功处理
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

	public GiltAutoBuy() {
		super(false) ;
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("https://www.gilt.com/login");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@id='main']")));
		try{
			//输入账号
			List<WebElement> accountList = driver.findElements(By.xpath("//input[@id='email']"));
			logger.debug("--->开始输入账号");
			if(accountList!=null&&accountList.size()>0){
				for(WebElement account:accountList){
					if(account.isDisplayed()){
						account.clear();
						Utils.sleep(1500);
						account.sendKeys(userName);
					}
				}
			}
			
			//输入密码
			List<WebElement> passwordList = driver.findElements(By.xpath("//input[@id='password']"));
			logger.debug("--->开始输入密码");
			if(passwordList!=null&&passwordList.size()>0){
				for(WebElement password:passwordList){
					if(password.isDisplayed()){
						password.clear();
						Utils.sleep(1500);
						password.sendKeys(passWord);
					}
				}
			}
			
			//提交
			List<WebElement> submitList = driver.findElements(By.xpath("//input[@class='button login']"));
			logger.debug("--->开始提交");
			if(submitList!=null&&submitList.size()>0){
				for(WebElement submit:submitList){
					if(submit.isDisplayed()){
						Utils.sleep(1500);
						submit.click();
					}
				}
			}
		}catch (Exception e){
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		//等待登录完成
		try{
			logger.debug("--->等待登录完成");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='nav-wrap']")));
			logger.debug("--->登录完成");
		}catch (Exception e){
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		//跳转到购物车
		try{
			WebElement viewCart = driver.findElement(By.xpath("//span[@class='nav-cart-count cart-count nav-cart-item-count']"));
			logger.error("--->开始跳转到购物车");
			viewCart.click();
			Utils.sleep(1500);
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		//driver.navigate().to("http://www.backcountry.com/Store/cart/cart.jsp");
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@class='dom-dialog-content']")));
			Utils.sleep(2000);
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			this.goodsRemove();
			Utils.sleep(2000);
			logger.error("--->购物车页面清理完成");
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	private void goodsRemove(){
		List<WebElement> list = driver.findElements(By.xpath("//button[@class='view-cart-dialog-item-remove' and contains(text(),'Remove')]"));
		if(list != null && list.size() > 0){
			WebElement remove = list.get(0) ;
			Utils.sleep(1000);
			if(remove != null){
				remove.click();
			}
			Utils.sleep(2000);
			goodsRemove() ;
		}
	}
	
	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->选择商品 productUrl = " + productUrl);
		try
		{
			driver.navigate().to(productUrl);
		}catch (Exception e){
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		Utils.sleep(2000);
		//找不到buy now,商品下架
		try{
			driver.findElement(By.xpath("//button[@id='buy-now']"));
			logger.debug("--->找到该款商品");
		}catch(Exception e){
			logger.debug("--->这款商品找不到商品详情");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}
		/*
		//商品页面404,商品也是下架
		try{
			driver.findElement(By.xpath("//p[@class='text-shadow sorry-label' and contains(text(),'Sorry we couldn't find that page')]"));
			logger.debug("--->这款商品的页面404错误,找不到该款商品");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){}
		*/
		//等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@class='product-details']")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		}catch(Exception e){
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL; 
		}
		
		String productNum = (String) param.get("num");
		Object sku = param.get("sku");
		//开始选择sku
		int findCount = 0;
		logger.debug("--->开始选择sku");
		try{
			if (sku != null){
				List<String> skuList = Utils.getSku((String) sku);
				List<WebElement> elements = driver.findElements(By.xpath("//section[@id='sku-selection']/div"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								WebElement label = element.findElement(By.xpath(".//dl[@class='sku-attribute-label']"));
								if(label.getText().toLowerCase().contains(skuList.get(i-1).toLowerCase())){
									findCount++;
									try{
										WebElement li = element.findElement(By.xpath("./ul/li[@data-value-name='"+skuList.get(i)+"']"));
										li.click();
										Utils.sleep(2000);
									}catch(Exception e){
										logger.debug("--->找不到指定的sku no success= "+(skuList.get(i-1)+":"+skuList.get(i)),e);
										return AutoBuyStatus.AUTO_SKU_NOT_FIND;
									}
								}
							}catch(Exception e){
								continue;
							}
						}
					}
				}
				if(findCount < skuList.size()/2){
					logger.debug("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
					return AutoBuyStatus.AUTO_SKU_NOT_FIND;
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
			WebElement priceElement = null ;
			try{
				priceElement = driver.findElement(By.xpath("//span[@class='product-price-sale']"));
			}catch(Exception e){
				priceElement = driver.findElement(By.xpath("//span[@class='product-price-sale']"));
			}
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
		
		//选择商品数量
		if(!Utils.isEmpty(productNum) && !productNum.equals("1")){
			try{
				logger.debug("--->商品数量 = "+productNum);
				Utils.sleep(2000);
				WebElement qty = driver.findElement(By.xpath("//div[@class='qty-label']"));
				qty.click();
				Utils.sleep(1000);
				List<WebElement> selectOptions = driver.findElements(By.xpath("//ul[@class='qty-items']/li"));
				if(selectOptions!=null&&selectOptions.size()>0){
					for(WebElement select:selectOptions){
						if(select!=null&&productNum.equals(select.getText())){
							select.click();
							break ;
						}
					}
				}
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
			WebElement cart = driver.findElement(By.xpath("//button[@id='add-to-cart']"));
			Utils.sleep(1500);
			String text = cart.getText();
			logger.error("text = "+text);
			if(StringUtil.isNotEmpty(text)){
				text = text.toLowerCase();
				if(text.contains("out of stock")){
					logger.debug("--->商品这款sku已经售完");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
				if(text.contains("add to cart")){
					cart.click();
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
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)){
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		//设置价格
//		int num = 1;
//		try{
//			num = Integer.parseInt(param.get("num"));
//		}catch(Exception e){
//			num = 1;
//		}
//		myPrice = String.valueOf(Float.parseFloat(myPrice) * num);
		logger.error("--->myPrice = "+myPrice);
		
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String)param.get("count");
		int count = 0;
		if(!Utils.isEmpty(countTemp)){
			count = Integer.parseInt(countTemp);
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//article[@class='dom-dialog-content']")));
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->购物车页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//去选收货地址页面
		try{
			Utils.sleep(1500);
			WebElement checkout = driver.findElement(By.xpath("//div[@class='view-cart-dialog-commands']")) ;//Checkout Now
			checkout.click();
			logger.debug("--->去选收货地址页面");
		}catch(Exception e){
			logger.debug("--->去选收货地址页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//等待收货地址加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='checkout-forms']")));
			logger.debug("--->收货地址页面加载完成");
		}catch(Exception e){
			logger.debug("--->收货地址页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		/*
		//选择信用卡
		logger.debug("--->选择信用卡");
		try{
			Select creditcardSelect = new Select(driver.findElement(By.xpath("//select[@id='credit-card-dropdown']"))) ;
			creditcardSelect.selectByIndex(1);
		}catch(Exception e){
			logger.debug("--->选择信用卡异常:\n"+e);
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		*/
		//选收货地址
		try{
			WebElement change = driver.findElement(By.xpath("//a[@class='change-shipping-address highlight']"));
			change.click();
			
			By by = By.xpath("//ul[@class='shipping-addresses-list group']/li");
			List<WebElement> list = driver.findElements(by);
			if(list != null && list.size() > 0){
				int tarAddr = count % 4;
				WebElement ele = null ;
				//如果地址不够选择最后一个
				if(list.size()<tarAddr+2){
					ele = list.get(0);
				}else{
					ele = list.get(tarAddr);
				}
				ele.click();
			}else{
				logger.debug("--->该账号没有设置收货地址");
				return AutoBuyStatus.AUTO_PAY_ADDR_INDEX_OVER_MAX;
			}
		}catch(Exception e){
			logger.debug("--->选择地址出错 = ",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		//输入信用卡卡号
		Utils.sleep(2000);
		try{
			WebElement cardNumber = driver.findElement(By.xpath("//input[@data-name='Card Number']"));
			String cardNo = param.get("cardNo").replace(" ","") ;
			cardNumber.sendKeys(cardNo);
		}catch(Exception e){
			
		}
		/*
		try{
			WebElement shippingMethodButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@name='dwfrm_multishipping_shippingOptions_save']")));
			Utils.sleep(1000);
			shippingMethodButton.click();
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
		}catch(Exception e){
			logger.debug("--->更改账单地址出现异常:"+e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		*/
		//选择快递方式
		/*
		logger.debug("--->选择快递方式");
		try{
			List<WebElement> shippingOptions = driver.findElements(By.xpath("//div[@id='shipping-content']/ul/li"));
			logger.debug("--->目前共有[" + shippingOptions.size() + "]种物流可选");
			boolean flag = false;
			for(WebElement shippingOption:shippingOptions){
				String text = shippingOption.getText().toUpperCase() ;
				if(!StringUtil.isBlank(text)&&text.contains("FREE")&&text.contains("TWO BUSINESS DAYS")){
					logger.debug("--->找到[Two Business Days]免费物流") ;
					WebElement radio = shippingOption.findElement(By.xpath(".//input[@type='radio']"));
					radio.click();
					flag = true ;
				}
			}
			if(!flag){
				logger.debug("--->没有找到[Two Business Days]免费物流,使用5-10天免费物流") ;
				for(WebElement shippingOption:shippingOptions){
					String text = shippingOption.getText().toUpperCase() ;
					if(!StringUtil.isBlank(text)&&text.contains("FREE")){
						logger.debug("--->找到免费物流") ;
						WebElement radio = shippingOption.findElement(By.xpath(".//input[@type='radio']"));
						radio.click();
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->选择快递方式出现异常");
		}
		*/
		//作为礼物，不需要账单明细
		try{
			WebElement gift = driver.findElement(By.xpath("//input[@class='gift-message-checkbox']")) ;
			gift.click();
		}catch(Exception e){
			logger.debug("--->作为礼物，不需要账单明细出现异常");
		}
		
		//优惠码
		String promotionStr = param.get("promotion");
		Set<String> promotionList = getPromotionList(promotionStr);
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
						WebElement clickCoupon = driver.findElement(By.xpath("//p[@class='promo-code-trigger']"));
						clickCoupon.click();
						Utils.sleep(2000);
					}
					//输入优惠码	
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@class='promo-code']"));
						codeInput.clear();
						Utils.sleep(1500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//button[@data-gilt-test='input-submit-apply-promo-code']")).click();
						Utils.sleep(3500);
						//如果有发现错误
						try{
							WebElement error = driver.findElement(By.xpath("//span[@class='error-message']"));
							if(error.isDisplayed()){
								statusMap.put(code, 0);
								previousSuccess = false ;
								logger.error("优惠码失效:"+code);
							}else{
								//成功处理
								statusMap.put(code, 10);
								previousSuccess = true ;
								isEffective = true;
							}
						}catch(Exception ee){
							//成功处理
							statusMap.put(code, 10);
							previousSuccess = true ;
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
		
		
		//查询总价
		try{
			logger.debug("--->开始查询总价");
			WebElement totlePrice = driver.findElement(By.xpath("//div[@class='clearfix charge-total']/span[@class='sum']")) ;
			String text = totlePrice.getText() ;
			logger.debug("--->找到商品总价 = "+text);
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1).replace(",", "");
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
					if (v.doubleValue() > 5.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		if(!isPay){
			return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
		}
		//确认
		try{
			WebElement submit = driver.findElement(By.xpath("//div[@class='alt-submit medium wide super-wide']/button[@class='button-large-primary submit top-submit']")) ;
			if(isPay){
				logger.debug("--->确认订单:"+submit.getText());
				submit.click();
			}
		}catch(Exception e){
			logger.debug("--->确认订单异常:"+e);
		}
		
		//查询商城订单号
		logger.debug("--->开始查找商品订单号");
		try{
			WebElement orderNum = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//strong[@id='order-num']")));
			String text = orderNum.getText() ;
			if(!StringUtil.isBlank(text)){
				logger.debug("--->找到商品订单号 = "+text);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, text);
				savePng();
				return AutoBuyStatus.AUTO_PAY_SUCCESS;
			}
		}catch(Exception e){
			logger.debug("--->查找商品订单号出现异常"+e);
			return AutoBuyStatus.AUTO_PAY_FAIL;
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
			driver.navigate().to("https://www.gilt.com/web-account/orders");
		}catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//等待my account页面加载完成
		
		List<WebElement> orders = driver.findElements(By.xpath("//article[@class='order']"));
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains(mallOrderNo)){
				try{
					WebElement a = order.findElement(By.xpath(".//a[@class='small-secondary']"));
					String href = a.getAttribute("href") ;
					logger.debug("已发货，开始查找物流单号,网址:"+href);
					
					String trackNo = "" ;
					Matcher m = Pattern.compile("tracknumbers=.+").matcher(href);
					if(m.find()){
						trackNo = m.group().replace("tracknumbers=", "");
					}
					String trackCompany = href.split("\\.")[1] ;
					if(StringUtil.isBlank(trackNo)){
						logger.debug("找不到订单号");
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}else{
						logger.debug("找到物流单号："+trackNo+";物流公司："+trackCompany);
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, trackCompany);
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}
				}catch(Exception e){
					logger.debug("该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
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
