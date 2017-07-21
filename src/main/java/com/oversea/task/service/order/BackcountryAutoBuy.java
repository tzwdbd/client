package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

public class BackcountryAutoBuy extends AutoBuy {

    private final Logger logger = Logger.getLogger(getClass());
	
	public static void main0(String[] args){
		/*FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///D:/444/Kate%20Sale.htm");
		List<WebElement> trs = driver.findElements(By.xpath("//div[@class='checkout-order-totals']/table/tbody/tr[@class='order-total']")) ;
		for(WebElement tr:trs){
			try{
				WebElement td = tr.findElement(By.xpath("./td"));
				String text1 = td.getText() ;
				if(!StringUtil.isBlank(text1)&&text1.toUpperCase().contains("ORDER TOTAL")){
					WebElement td0 = tr.findElement(By.xpath(".//td[@class='price']"));
					String text = td0.getText() ;
					System.out.println("--->找到商品总价 = "+text);
					if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
						String priceStr = text.substring(text.indexOf("$")+1);
						System.out.println("--->找到商品结算总价 = "+priceStr);
						BigDecimal x = new BigDecimal(10);
						BigDecimal y = new BigDecimal(priceStr);
						BigDecimal v = y.subtract(x);
						if (v.doubleValue() > 20.00D){
							System.out.println("--->总价差距超过约定,不能下单");
							//return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
						}
					}
				}
			}catch(Exception e){
				//System.out.println(e) ;
			}
		}*/
		
		BackcountryAutoBuy auto = new BackcountryAutoBuy();
		AutoBuyStatus status = auto.login("415407279@qq.com", "ws1530952");
		if (AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
			status = auto.cleanCart();
			if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
				Map<String, String> param = new HashMap<String, String>();
				//param.put("url", "http://surprise.katespade.com/WKRU3255-2.html?pid=WKRU3255-2");//下架商品
				//param.put("url", "http://www.backcountry.com/the-north-face-tomales-bay-jacket-womens?CMP_SKU=TNF02A6&MER=0406&skid=TNF02A6-SEDSGGRE-M&CMP_ID=PLA_GOc001&mv_pc=r101&utm_source=Google&utm_medium=PLA&mr:trackingCode=52452D2A-92BB-E511-80F3-005056944E17&mr:referralID=NA&mr:device=c&mr:adType=plaonline&mr:ad=92885917477&mr:keyword=&mr:match=&mr:tid=pla-94469057797&mr:ploc=2840&mr:iloc=&mr:store=&mr:filter=94469057797&gclid=CKqutOXMrs0CFZcRvQodQGkJ_A&gclsrc=aw.ds");//售罄商品
				//param.put("url", "https://www.katespade.com/products/mock-neck-knit-flounce-dress/NJMU6948.html?cgid=ks-clothing-dresses-view-all&dwvar_NJMU6948_color=098#start=3&cgid=ks-clothing-dresses-view-all");//正常商品
				//param.put("url", "https://www.linkhaitao.com/index.php?mod=lhdeal&track=4a75QTDvq2zGqWfIshiwPZQYMs83UOrMxTByxez6gJSlicdVnm0c4w4tc6vvmfbJWLaAPw_c_c&new=http%3A%2F%2Fwww.backcountry.com%2Farcteryx-gothic-glove%3Fskid%3DARC00DE-ROS-S%26ti%3DUExQIFJ1bGUgQmFzZWQ6QXJjJ3Rlcnl4IFNhbGU6NDozMzo%3D&tag=");//正常商品
				param.put("url", "http://www.backcountry.com/columbia-drainmaker-iii-water-shoe-womens?CMP_SKU=COL00N6&MER=0406&skid=COL00N6-QUAMT-S7&CMP_ID=PLA_GOc001&mv_pc=r101&utm_source=Google&utm_medium=PLA&mr:trackingCode=FA9B69BC-8FCE-E511-80F3-005056944E17&mr:referralID=NA&mr:device=c&mr:adType=plaonline&mr:ad=92885920237&mr:keyword=&mr:match=&mr:tid=pla-98526848677&mr:ploc=2840&mr:iloc=&mr:store=&mr:filter=98526848677&gclid=COiy4q7GpM0CFQomvQod2NkN9Q&gclsrc=aw.ds");//正常商品
//				param.put("sku", "[[\"color\",\"Anchor Navy Suede\"],[\"size\",\"9\"],[\"width\",\"B - Medium\"]]");
				//param.put("sku", "[[\"color\",\"black\"],[\"size\",\"7\"]]");
				param.put("sku", "[[\"size\",\"8.5\"],[\"color\",\"Razzle/Zour\"]]");
				param.put("num", "2");
				status = auto.selectProduct(param);
				if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
					Map<String, String> param0 = new HashMap<String, String>();
					param0.put("my_price", "29");
					param0.put("count", "1");
					param0.put("isPay", String.valueOf(false));
					param0.put("suffixNo", "123");
					//param0.put("cardNo", "4662 4833 6029 1396");
					status = auto.pay(param0);
				}
			}
		}
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
	
	public static void main1122(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/44556677/My%20Account%20_%20Backcountry.com.htm");
		List<WebElement> orders = driver.findElements(By.xpath("//div[@class='order js-order']"));
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains("b2573745629")){
				try{
					WebElement orderStatusEle = order.findElement(By.xpath(".//h2[@class='js-unshipped-order-status order-status']"));
					String orderStatus  = orderStatusEle.getText() ;
					if(StringUtil.isBlank(orderStatus)){
						WebElement orderShipStatusEle = order.findElement(By.xpath(".//h2[@class='order-status order-history-shipment__status']"));
						String orderShipStatus  = orderShipStatusEle.getText() ;
						if(StringUtil.isBlank(orderShipStatus)){
							System.out.println("找不到订单号:"+"");
							
						}else if("shipped".equals(orderShipStatus.toLowerCase())){
							WebElement a = order.findElement(By.xpath(".//a[@class='track-link btn order-history__link--secondary']"));
							String href = a.getAttribute("href") ;
							System.out.println("已发货，开始查找物流单号,网址:"+href);
							String trackNo = href.split("=")[href.split("=").length-1] ;
							String trackCompany = href.split("\\.")[1] ;
							if(StringUtil.isBlank(trackNo)){
								System.out.println("找不到订单号");
								
							}else{
								System.out.println("找到物流单号："+trackNo+";物流公司："+trackCompany);
								
							}
						}
					}else if("canceled".equals(orderStatus.toLowerCase())){
						System.out.println("商城砍单,订单号:"+"");
						
					}else if("call customer service".equals(orderStatus.toLowerCase())){
						System.out.println("订单hold，需要联系客服,订单号:"+"");
						
					}
				}catch(Exception e){
					System.out.println("该订单还没发货,没产生物流单号");
				}
			}
		}
	}
	
	public static void main22(String[] args){
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("http://www.backcountry.com/haglfs-lizard-ii-softshell-jacket-womens?skid=HAG00BV-VOLPK-S&ti=OkhhZ2zDtmZzOjE6NDo=");
		//开始选择sku
		int findCount = 0;
		String sku = "[[\"size\",\"S\"],[\"color\",\"Volcanic Pink\"]]" ;
		System.out.println("--->开始选择sku");
		try{
			if (sku != null){
				List<String> skuList = Utils.getSku((String) sku);
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='product-variant-selector js-product-variant-selector']/div"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								WebElement label = element.findElement(By.xpath(".//p"));
								if(label.getText().toLowerCase().contains(skuList.get(i-1).toLowerCase())){
									findCount++;
									WebElement a = element.findElement(By.xpath("./a"));
									a.click();
									try{
										if(i>=skuList.size()-1){
											WebElement li = element.findElement(By.xpath("./div/ul/li/div/span[text()='"+skuList.get(i)+"']"));
											li.click();
										}else{
											WebElement li = element.findElement(By.xpath("./div/ul/li/span[text()='"+skuList.get(i)+"']"));
											System.out.println(li.getText());
											li.click();
										}
										Utils.sleep(2000);
									}catch(Exception e){
										System.out.println("--->找不到指定的sku no success= "+(skuList.get(i-1)+":"+skuList.get(i))+e);
									}
								}
							}catch(Exception e){
								continue;
							}
						}
					}
				}
				if(findCount < skuList.size()/2){
					System.out.println("--->缺少匹配的sku findCount = "+findCount+" && skuList.size()/2 = "+skuList.size()/2);
				}
			}
		}catch(Exception e){
			System.out.println("--->选择sku碰到异常"+e);
		}
		System.out.println("--->选择sku完成");
	}
	
	public static void main99(String[] args) {
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("https://www.backcountry.com/Store/checkout/checkout.jsp?_DARGS=/Store/cart/cart.body.jsp.checkout-form");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		Set<String> promotionList = new HashSet<String>() ;
		promotionList.add("123") ;
		promotionList.add("BC20PERCENT") ;
		promotionList.add("BC20PERF") ;
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
						WebElement clickCoupon = driver.findElement(By.xpath("//a[@id='coupon-redemption-link']"));
						clickCoupon.click();
						Utils.sleep(2000);
					}
					//输入优惠码	
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@id='promo-code']"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//a[@id='redeem-code']")).click();
						Utils.sleep(5500);
						//如果有发现错误
						try{
							WebElement error = driver.findElement(By.xpath("//form[@id='coupon-container-form']/ul[@class='messaging']"));
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

	public static void main(String[] args) {
		double myPrice = 100 ;
		int count = 1 ;
		FirefoxDriver driver = new FirefoxDriver();
		driver.get("file:///C:/Users/leixun/Desktop/backcountry/Checkout%20_%20Backcountry.com.html");
		//选择信用卡
		System.out.println("--->选择信用卡");
		try{
			Select creditcardSelect = new Select(driver.findElement(By.xpath("//select[@id='credit-card-dropdown']"))) ;
			creditcardSelect.selectByIndex(1);
		}catch(Exception e){
			System.out.println("--->选择信用卡异常:\n"+e);
		}
		
		//选收货地址
		By by = By.xpath("//select[@id='shipping-address-selector']/option");
		try{
			List<WebElement> list = driver.findElements(by);
			if(list != null && list.size() > 0){
				int tarAddr = count % 4;
				WebElement ele = null ;
				//如果地址不够选择最后一个
				if(list.size()<tarAddr+1){
					ele = list.get(0);
				}else{
					ele = list.get(tarAddr);
				}
				Utils.sleep(1500);
				ele.click();
			}else{
				System.out.println("--->该账号没有设置收货地址");
			}
		}catch(Exception e){
			System.out.println("--->选择地址出错 = "+e);
		}
		//选择快递方式
		System.out.println("--->选择快递方式");
		try{
			List<WebElement> shippingOptions = driver.findElements(By.xpath("//div[@id='shipping-content']/ul/li"));
			System.out.println("--->目前共有[" + shippingOptions.size() + "]种物流可选");
			boolean flag = false;
			for(WebElement shippingOption:shippingOptions){
				String text = shippingOption.getText().toUpperCase() ;
				if(!StringUtil.isBlank(text)&&text.contains("FREE")&&text.contains("TWO BUSINESS DAYS")){
					System.out.println("--->找到[Two Business Days]免费物流") ;
					WebElement radio = shippingOption.findElement(By.xpath(".//input[@type='radio']"));
					radio.click();
					flag = true ;
				}
			}
			if(!flag){
				System.out.println("--->没有找到[Two Business Days]免费物流,使用5-10天免费物流") ;
				for(WebElement shippingOption:shippingOptions){
					String text = shippingOption.getText().toUpperCase() ;
					if(!StringUtil.isBlank(text)&&text.contains("FREE")){
						System.out.println("--->找到免费物流") ;
						WebElement radio = shippingOption.findElement(By.xpath(".//input[@type='radio']"));
						radio.click();
					}
				}
			}
		}catch(Exception e){
			System.out.println("--->选择快递方式出现异常");
		}
		//作为礼物，不需要账单明细
		try{
			WebElement gift = driver.findElement(By.xpath("//input[@id='gift']")) ;
			gift.click();
		}catch(Exception e){
			System.out.println("--->作为礼物，不需要账单明细出现异常");
		}
		
		//查询总价
		try{
			System.out.println("--->开始查询总价");
			WebElement totlePrice = driver.findElement(By.xpath("//strong[@id='order-total']")) ;
			String text = totlePrice.getText() ;
			System.out.println("--->找到商品总价 = "+text);
			if(!Utils.isEmpty(text) && text.indexOf("$") != -1){
				String priceStr = text.substring(text.indexOf("$")+1);
				System.out.println("--->找到商品结算总价 = "+priceStr);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(priceStr);
				BigDecimal v = y.subtract(x);
				if (v.doubleValue() > 20.00D){
					System.out.println("--->总价差距超过约定,不能下单");
				}
			}
		}catch(Exception e){
			System.out.println("--->查询结算总价出现异常");
		}
	}
	
	public BackcountryAutoBuy() {
		super(false) ;
	}
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("https://www.backcountry.com/Store/account/login.jsp");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='page']")));
		
		// 等到[输入框]出现
		try{
			//输入账号
			WebElement account = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='login']")));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.xpath("//input[@id='password']"));
			logger.debug("--->开始输入密码");
			password.clear();
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			//提交
			WebElement submit = driver.findElement(By.xpath("//input[@id='btn-login']"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='account-page']")));
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
		/*try{
			WebElement viewCart = driver.findElement(By.xpath("//div[@class='header-cart__icon']"));
			logger.error("--->开始跳转到购物车");
			Utils.sleep(1500);
			viewCart.click();
		}catch(Exception e){
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}*/
		driver.navigate().to("http://www.backcountry.com/Store/cart/cart.jsp");
		
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@id='cart-wishlist-cont']")));
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
		List<WebElement> list = driver.findElements(By.xpath("//a[@class='remove-link js-remove-link qa-remove-link' and contains(text(),'Remove')]"));
		if(list != null && list.size() > 0){
			WebElement remove = list.get(0) ;
			Utils.sleep(1000);
			if(remove != null){
				remove.click();
			}
			Utils.sleep(3000);
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
		}
		catch (Exception e){
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		//找不到商品详情,商品下架
		try{
			driver.findElement(By.xpath("//section[@class='plp-main plp-container']"));
			logger.debug("--->这款商品找不到商品详情");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){
			logger.debug("--->找到该款商品");
		}
		
		//商品页面404,商品也是下架
		try{
			driver.findElement(By.xpath("//p[@class='text-shadow sorry-label' and contains(text(),'Sorry we couldn't find that page')]"));
			logger.debug("--->这款商品的页面404错误,找不到该款商品");
			return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
		}catch(Exception e){}
		
		//等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//section[@class='product-media']")));
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
				List<WebElement> elements = driver.findElements(By.xpath("//div[@class='product-variant-selector js-product-variant-selector']/div"));
				for (int i = 0; i < skuList.size(); i++){
					if (i % 2 == 1){
						for(WebElement element : elements){
							try{
								WebElement label = element.findElement(By.xpath(".//p"));
								if(label.getText().toLowerCase().contains(skuList.get(i-1).toLowerCase())){
									findCount++;
									WebElement a = element.findElement(By.xpath("./a"));
									a.click();
									try{
										if(i>=skuList.size()-1){
											WebElement li = element.findElement(By.xpath("./div/ul/li/div/span[text()='"+skuList.get(i)+"']"));
											li.click();
										}else{
											WebElement li = element.findElement(By.xpath("./div/ul/li/span[text()='"+skuList.get(i)+"']"));
											li.click();
										}
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
				priceElement = driver.findElement(By.xpath("//span[@class='product-pricing__retail js-product-pricing__retail']"));
			}catch(Exception e){
				priceElement = driver.findElement(By.xpath("//span[@class='product-pricing__sale js-product-pricing__sale']"));
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
				WebElement selectOptions = driver.findElement(By.xpath("//input[@class='product-buybox-quantity__input js-product-buybox-quantity']"));
				selectOptions.clear();
				selectOptions.sendKeys(productNum);
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
			WebElement cart = driver.findElement(By.xpath("//a[@class='product-buybox__btn product-buybox__btn--cart js-add-to-cart']"));
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
					try{
						Utils.sleep(3000);
						WebElement error = driver.findElement(By.xpath("//ul[@id='form-errors']"));
						String errorText = error.getText() ;
						if(!StringUtil.isBlank(errorText)&&errorText.contains("not available")){
							logger.debug("--->商品这款sku已经售完");
							return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
						}
					}catch(Exception e){
					}
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
		//等待购物车页面加载完成
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//nav[@id='cart-navigation']")));
			logger.debug("--->购物车页面加载完成");
		}catch(Exception e){
			logger.debug("--->购物车页面加载异常");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
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
		//去选收货地址页面
		try{
			WebElement checkout = driver.findElement(By.xpath("//a[@id='proceed-to-checkout-bottom']")) ;//Proceed With Order
			Utils.sleep(1500);
			checkout.click();
			logger.debug("--->去选收货地址页面");
		}catch(Exception e){
			logger.debug("--->去选收货地址页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//选择信用卡
		logger.debug("--->选择信用卡");
		try{
			Select creditcardSelect = new Select(driver.findElement(By.xpath("//select[@id='credit-card-dropdown']"))) ;
			creditcardSelect.selectByIndex(1);
		}catch(Exception e){
			logger.debug("--->选择信用卡异常:\n"+e);
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		
		//选收货地址
		By by = By.xpath("//select[@id='shipping-address-selector']/option");
		try{
			List<WebElement> list = driver.findElements(by);
			if(list != null && list.size() > 0){
				int tarAddr = count % 4;
				WebElement ele = null ;
				//如果地址不够选择最后一个
				if(list.size()<tarAddr+1){
					ele = list.get(0);
				}else{
					ele = list.get(tarAddr);
				}
				Utils.sleep(1500);
				ele.click();
			}else{
				logger.debug("--->该账号没有设置收货地址");
				return AutoBuyStatus.AUTO_PAY_ADDR_INDEX_OVER_MAX;
			}
		}catch(Exception e){
			logger.debug("--->选择地址出错 = ",e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
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
		//作为礼物，不需要账单明细
		try{
			WebElement gift = driver.findElement(By.xpath("//input[@id='gift']")) ;
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
						WebElement clickCoupon = driver.findElement(By.xpath("//a[@id='coupon-redemption-link']"));
						clickCoupon.click();
						Utils.sleep(2000);
					}
					//输入优惠码	
					try{
						WebElement codeInput = driver.findElement(By.xpath("//input[@id='promo-code']"));
						codeInput.clear();
						Utils.sleep(2500);
						codeInput.sendKeys(code);
						Utils.sleep(1500);
						driver.findElement(By.xpath("//a[@id='redeem-code']")).click();
						Utils.sleep(5500);
						//如果有发现错误
						try{
							WebElement error = driver.findElement(By.xpath("//form[@id='coupon-container-form']/ul[@class='messaging']"));
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
			WebElement totlePrice = driver.findElement(By.xpath("//strong[@id='order-total']")) ;
			String text = totlePrice.getText() ;
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
		}catch(Exception e){
			logger.debug("--->查询结算总价出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		//确认
		try{
			//return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
			Utils.sleep(1500);
			WebElement submit = driver.findElement(By.xpath("//div[@class='js-submit-order js-submit-order-btn qa-submit-order btn-icon submit-order']/span[@class='js-submit-order-msg qa-submit-order-msg submit-order-msg']")) ;
			logger.debug("--->确认订单:"+submit.getText());
			submit.click();
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
			driver.navigate().to("https://www.backcountry.com/Store/account/account.jsp");
		}catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		WebDriverWait wait0 = new WebDriverWait(driver, 30);
		//等待my account页面加载完成
		try{
			logger.debug("--->开始等待my account页面加载完成");
			wait0.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='account-page']")));
			Utils.sleep(1500);
			logger.debug("--->my account页面加载完成");
		}catch (Exception e){
			logger.error("--->加载my account页面出现异常", e);
		}
		
		List<WebElement> orders = driver.findElements(By.xpath("//div[@class='order js-order']"));
		
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains(mallOrderNo)){
				try{
					WebElement orderStatusEle = order.findElement(By.xpath(".//h2[@class='js-unshipped-order-status order-status']"));
					String orderStatus  = orderStatusEle.getText() ;
					if(StringUtil.isBlank(orderStatus)){
						WebElement orderShipStatusEle = order.findElement(By.xpath(".//h2[@class='order-status order-history-shipment__status']"));
						String orderShipStatus  = orderShipStatusEle.getText() ;
						if(StringUtil.isBlank(orderShipStatus)){
							logger.debug("找不到订单号:"+mallOrderNo);
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}else if("shipped".equals(orderShipStatus.toLowerCase())){
							WebElement a = order.findElement(By.xpath(".//a[@class='track-link btn order-history__link--secondary']"));
							String href = a.getAttribute("href") ;
							logger.debug("已发货，开始查找物流单号,网址:"+href);
							String trackNo = href.split("=")[href.split("=").length-1] ;
							String trackCompany = href.split("\\.")[1] ;
							if(StringUtil.isBlank(trackNo)){
								logger.debug("找不到订单号:"+mallOrderNo);
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}else{
								logger.debug("找到物流单号："+trackNo+";物流公司："+trackCompany);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, trackCompany);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							}
						}
					}else if("canceled".equals(orderStatus.toLowerCase())){
						logger.debug("商城砍单,订单号:"+mallOrderNo);
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}else if("call customer service".equals(orderStatus.toLowerCase())){
						logger.debug("订单hold，需要联系客服,订单号:"+mallOrderNo);
						return AutoBuyStatus.AUTO_SCRIBE_CALL_CUSTOMER_SERVICE;
					}
				}catch(Exception e){
					logger.debug("该订单还没发货,没产生物流单号");
					return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
				}
			}
		}
		return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
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
