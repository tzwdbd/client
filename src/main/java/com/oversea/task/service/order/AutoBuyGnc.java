package com.oversea.task.service.order;

import java.math.BigDecimal;
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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
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

/**
 * @author xiong chen
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task.service.order
 * @Description:
 * @date 2016年11月2日
 */

public class AutoBuyGnc extends AutoBuy{
	
	private final Logger logger = Logger.getLogger(getClass());
	public AutoBuyGnc() {
		super(false);
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
	}
	String uName;
	String pwd;
	
	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		uName=userName;
		pwd = passWord;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		logger.debug("开始登陆");
		try {
			driver.get("https://www.gnc.com");
			logger.debug("跳转到登录页面");
			WebElement li = driver.findElement(By.xpath("//a[contains(text(),'Log In')]"));
			TimeUnit.SECONDS.sleep(1);
			li.click();
			
			logger.debug("输入用户名和密码");
			WebElement emailId = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='emailId']")));
			emailId.sendKeys(userName);
			TimeUnit.SECONDS.sleep(2);
			
			WebElement passwd = driver.findElement(By.xpath("//input[@id='passwd']"));
			passwd.sendKeys(passWord);
			TimeUnit.SECONDS.sleep(2);
			logger.debug("点击登录");
			WebElement LogIn = driver.findElement(By.cssSelector("a.button-inverted:nth-child(16)"));
			LogIn.click();
			TimeUnit.SECONDS.sleep(2);
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='shoppingBag']")));
			logger.debug("登陆完成");
		} catch (Exception e) {
			logger.debug("登录异常",e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		try {
			driver.navigate().to("http://www.gnc.com/cart/index.jsp?an_action=viewCart");
			logger.debug("清空购物车");
			while (true) {
				try {
					driver.findElement(By.xpath("//span[contains(text(),'There are no products in your Shopping Cart.')]"));
					logger.debug("购物车清空了");
					break;
				} catch (Exception e) {
				}
				logger.debug("点击remove");
				List<WebElement> removes = driver.findElements(By.xpath("//a[@class='details' and contains(text(),'Remove')]"));
				removes.get(0).click();
				TimeUnit.SECONDS.sleep(10);
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
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		List<String> skuList = Utils.getSku(sku);
		try {
			logger.debug("选择sku");
			driver.navigate().to(productUrl);
			TimeUnit.SECONDS.sleep(3);
			WebElement productSize = driver.findElement(By.xpath("//p[@class='product-size']/span"));
			String skuTh = productSize.getText();
			if(skuTh.equalsIgnoreCase(skuList.get(1))){
				logger.debug("sku选择完成");
			}else{
				logger.debug("没找到sku");
				return AutoBuyStatus.AUTO_SKU_NOT_FIND;
			}
		} catch (Exception e) {
			logger.debug("打开商品页面出错", e);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		close();
		if(!productNum.equals("1")){
			try {
				logger.debug("开始选择数量");
				WebElement qty_0 = driver.findElement(By.xpath("//input[@id='qty_0']"));
				qty_0.clear();
				qty_0.sendKeys(productNum);
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {
				logger.debug("选择数量失败");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		try {
			logger.debug("寻找商品单价");
			WebElement priceEl = driver.findElement(By.xpath("//p[@class='price-sale now']"));
			Matcher m = Pattern.compile("[0-9.]+").matcher(priceEl.getText());
			if(m.find()){
				String price = m.group();
				logger.debug("单价："+price);
				String productEntityId = param.get("productEntityId");
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
				priceMap.put(productEntityId, price);
			}else{
				logger.debug("没有找到单价[1]");
				
			}
		} catch (Exception e) {
			try {
				WebElement priceEl = driver.findElement(By.xpath("//p[@class='price-regular now']"));
				Matcher m = Pattern.compile("[0-9.]+").matcher(priceEl.getText());
				if(m.find()){
					String price = m.group();
					logger.debug("单价："+price);
					String productEntityId = param.get("productEntityId");
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, price);
					priceMap.put(productEntityId, price);
				}else{
					logger.debug("没有找到单价[2]");
				}
			} catch (Exception e2) {
				logger.debug("没有找到单价[3]");
			}
		}
		
		try {
			logger.debug("加购物车");
			WebElement addToCart = driver.findElement(By.xpath("//a[@class='add-to-cart']"));
			addToCart.click();
			TimeUnit.SECONDS.sleep(5);
			WebElement goToCart = driver.findElement(By.cssSelector("span.mc-continue-shopping"));
			goToCart.click();
			TimeUnit.SECONDS.sleep(5);
			logger.debug("加入购物车成功");
		} catch (Exception e) {
			logger.debug("加入购物车失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}
	void close(){
		try {
			WebElement fsrCloseBtn = driver.findElement(By.xpath("//a[@class='fsrCloseBtn']"));
			fsrCloseBtn.click();
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
		}
	}
	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		
		String cNo = param.get("cardNo");
		String owner = param.get("owner");
		String expiryDate = param.get("expiryDate");
		
		String countTemp = (String) param.get("count");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String myPrice = param.get("my_price");
		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		if (Utils.isEmpty(myPrice)) {
			logger.error("预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		String code = param.get("suffixNo");
		logger.debug("信用卡卡号是 = " + code);
		if (Utils.isEmpty(code)) {
			logger.debug("没有找到可用的信用卡安全码");
		}
		logger.debug("开始结算");
		try {
			driver.navigate().to("http://www.gnc.com/cart/index.jsp?an_action=viewCart");
			TimeUnit.SECONDS.sleep(2);
			WebElement checkOut = driver.findElement(By.cssSelector("div.checkoutOptions:nth-child(2) > form:nth-child(2) > button:nth-child(1)"));
			checkOut.click();
			logger.debug("跳转到结算页面");
			
			logger.debug("再次登录");
			WebElement emailLogin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='emailLogin']")));
			emailLogin.sendKeys(uName);
			TimeUnit.SECONDS.sleep(2);

			WebElement passwordLogin = driver.findElement(By.xpath("//input[@id='passwordLogin']"));
			passwordLogin.sendKeys(pwd);
			TimeUnit.SECONDS.sleep(2);
			
			WebElement login = driver.findElement(By.cssSelector("button.sprite:nth-child(4)"));
			login.click();
			logger.debug("登陆完成");
		} catch (Exception e) {
			logger.debug("再次登录失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		try {
			logger.debug("选择地址");
			WebElement edit;
			try {
				edit = driver.findElement(By.cssSelector("h4.subtitle > a:nth-child(1)"));
			} catch (Exception e) {
				edit = driver.findElement(By.cssSelector("div.address:nth-child(3) > h3:nth-child(1) > a:nth-child(1)"));
			}
			edit.click();
			TimeUnit.SECONDS.sleep(10);
			
			List<WebElement>  list = driver.findElements(By.xpath("//li[@class='address']"));
			Iterator<WebElement> iterator = list.iterator();
			while(iterator.hasNext()){
				WebElement ad = iterator.next();
				if(ad.getText().contains("Xi Dou Men")){
					iterator.remove();
				}
			}
			logger.debug("目前有"+list.size()+"个地址可选");
			int index = Integer.parseInt(countTemp)%list.size();
			logger.debug("选择第"+index+"个地址");
			
			WebElement target = list.get(index);
			WebElement sele = target.findElement(By.xpath(".//li[@class='use-as use-as-shipping sprite ']"));
			sele.click();
			logger.debug("选择地址完成");
			TimeUnit.SECONDS.sleep(2);
			
			WebElement confirm = driver.findElement(By.xpath("//div[@class='continue sprite']"));
			confirm.click();
			logger.debug("确认地址");
			TimeUnit.SECONDS.sleep(5);
			
			try {
				WebElement cardsel =  driver.findElement(By.xpath("//select[@id='ccType']"));
				Select select = new Select(cardsel);
				select.selectByVisibleText("Visa");
				TimeUnit.SECONDS.sleep(2);
				logger.debug("没绑卡的账号，开始绑卡");
				if(Utils.isEmpty(cNo) || Utils.isEmpty(owner) || Utils.isEmpty(expiryDate)){
					logger.debug("cardNo或者owner,suffixNo为空");
					return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
				}
				
				try {
					WebElement cadNo = driver.findElement(By.xpath("//input[@id='cc-number']"));
					cadNo.clear();
					cadNo.sendKeys(cNo.trim());
					TimeUnit.SECONDS.sleep(2);
					
					WebElement month = driver.findElement(By.xpath("//select[@id='cardExpDateMo']"));
					Select select2 = new Select(month);
					select2.selectByIndex(Integer.parseInt(expiryDate.split(" ")[1].trim())-1);
					TimeUnit.SECONDS.sleep(2);
					
					WebElement year = driver.findElement(By.xpath("//select[@id='cardExpDateYr']"));
					Select select3 = new Select(year);
					select3.selectByVisibleText(expiryDate.split(" ")[0].trim());
					TimeUnit.SECONDS.sleep(2);
					
					WebElement ccPin = driver.findElement(By.xpath("//input[@id='ccPin']"));
					ccPin.sendKeys(code);
					TimeUnit.SECONDS.sleep(2);
				} catch (Exception e) {
					logger.debug("绑卡异常",e);
					return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
				}
			} catch (Exception e) {
				logger.debug("输入安全码");
				WebElement suffix = driver.findElement(By.xpath("//input[@id='paymentMethod.ccvNumber']"));
				suffix.sendKeys(code);
			}
		} catch (Exception e) {
			logger.debug("选择地址失败",e);
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}
		
		try {
			logger.debug("开始查询总价");
			WebElement priceEl = driver.findElement(By.xpath("//tr[@class='total']"));
			Matcher m =  Pattern.compile("[0-9.]+").matcher(priceEl.getText());
			if(m.find()){
				String price = m.group();
				logger.debug("找到总价："+price);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, price);
				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(price);
				BigDecimal v = x.subtract(y);
				double d = Math.abs(v.doubleValue());
				if (d > 20.00D) {
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}
				logger.debug("开始确认");
				
				//使用优惠码0 失效,1互斥 ,9没修改过,10有效
				String promotionStr = param.get("promotion");
				Set<String> promotionList = getPromotionList(promotionStr);
				
				if(isPay){
					for(int i = 0;i< 3; i++){
						if(promotionList != null && promotionList.size() > 0){
							boolean isEffective = false;
							HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
							for(String promotionCode : promotionList){
								if(StringUtil.isNotEmpty(promotionCode)){
									promotionCode = promotionCode.trim();
									try {
										TimeUnit.SECONDS.sleep(2);
										WebElement promorionEl = driver.findElement(By.xpath("//input[@id='promoCode']"));
										promorionEl.clear();
										promorionEl.sendKeys(promotionCode);
										TimeUnit.SECONDS.sleep(1);
										WebElement applyPromoCode = driver.findElement(By.xpath("//input[@id='applyPromoCode']"));
										applyPromoCode.click();
										TimeUnit.SECONDS.sleep(3);
										try {
											driver.findElement(By.xpath("//div[contains(text(),'We didnt recognize that promotion code. Try entering']"));
											statusMap.put(code, 0);
										} catch (Exception e) {
											logger.debug("promotionCode:"+promotionCode,e);
											//优惠码有效
											logger.debug("优惠码有效");
											/*try {
												driver.findElement(By.xpath(""));
												statusMap.put(code, 10);
												isEffective = true;
											} catch (Exception e2) {
												logger.debug("promotionCode:"+e2);
											}*/
										}
									} catch (Exception e) {
										logger.debug("输入优惠吗错误");
									}
								}
							}
							setPromotionCodelistStatus(statusMap);
							if("true".equals(param.get("isStock")) && !isEffective){
								logger.debug("--->优惠码失效,中断采购");
								return AutoBuyStatus.AUTO_PAY_FAIL;
							}
						}
						
						WebElement submit = driver.findElement(By.xpath("//div[@id='submit-order-bottom']"));
						submit.click();
						
						try {
							TimeUnit.SECONDS.sleep(5);
							WebElement orderNo = driver.findElement(By.xpath("//p[@id='your-order-number']"));
							Matcher m1 = Pattern.compile("[0-9]+").matcher(orderNo.getText());
							if(m1.find()){
								String oNo = m1.group();
								logger.debug("找到订单号："+oNo);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, oNo);
							}else{
								logger.debug("获取订单号失败");
								return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
							}
							break;
						} catch (Exception e) {
							logger.debug("需要返回重新付款");
						}
						
						//中信银行  中国银行   建设银行  招商银行
						try {
							TimeUnit.SECONDS.sleep(25);
							logger.debug("取消返回");
							WebElement cancel = driver.findElement(By.xpath("//a[contains(text(),'取消')]"));
							cancel.click();
						} catch (Exception e) {
						}
						
						
						try {
							TimeUnit.SECONDS.sleep(5);
							WebElement sec = driver.findElement(By.xpath("//input[@id='ccPin']"));
							sec.sendKeys(code);
							TimeUnit.SECONDS.sleep(2);
						} catch (Exception e) {
							// TODO: handle exception
						}
						
						try {
							logger.debug("中信银行");
							WebElement sub = driver.findElement(By.cssSelector("div.sprite:nth-child(7)"));
							TimeUnit.SECONDS.sleep(30);
							sub.click();
							logger.debug("付款成功");
						} catch (Exception e) {
							
						}
						
						try {
							WebElement orderNo = driver.findElement(By.xpath("//p[@id='your-order-number']"));
							Matcher m1 = Pattern.compile("[0-9]+").matcher(orderNo.getText());
							if(m1.find()){
								String oNo = m1.group();
								logger.debug("找到订单号："+oNo);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, oNo);
								savePng();
							}
							break;
						} catch (Exception e) {
							logger.debug("需要返回重新付款");
						}
					}
					logger.debug("取消了3次还是不能下单");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("结算异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait ;
		try {
			driver.close();
			driver = new FirefoxDriver();
			driver.manage().deleteAllCookies();
			driver.manage().window().setSize(new Dimension(414, 736));
			driver.manage().window().setPosition(new Point(0, 0));
			wait = new WebDriverWait(driver, WAIT_TIME);
			driver.get("https://m.gnc.com");
			TimeUnit.SECONDS.sleep(3);
			logger.debug("开始登陆");
			WebElement menue = driver.findElement(By.xpath("//div[@class='sprites-menu-new']"));
			menue.click();
			TimeUnit.SECONDS.sleep(5);
			
			logger.debug("跳转到登录页面");
			WebElement logIn = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//a[@href='/coreg/index.jsp?step=register']/span")));
			logIn.click();
			TimeUnit.SECONDS.sleep(5);
			logger.debug("跳转到登录页面完成");
			
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		try {
			WebElement emailId = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//input[@id='emailId']")));
			emailId.sendKeys(uName);
			logger.debug("输入账号："+uName);
			TimeUnit.SECONDS.sleep(1);
			
			WebElement passwd = driver.findElement(By.xpath("//input[@id = 'passwd']"));
			passwd.sendKeys(pwd);
			logger.debug("输入密码："+pwd);
			TimeUnit.SECONDS.sleep(1);
			
		} catch (Exception e) {
			logger.error("输入账号密码遇到错误",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			WebElement emailId = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Sign In')]")));
			emailId.click();
			logger.error("点击登录");
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.error("登录失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//div[@class='sprites-cart']")));
			logger.debug("登陆成功");
		} catch (Exception e) {
			logger.debug("登录失败",e);
		}
		

		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			driver.navigate().to("https://m.gnc.com/checkout/index.jsp?process=orderTracking");
			logger.debug("跳转到订单详情页成功");
			
		} catch (Exception e) {
			logger.debug("跳转到订单详情页失败",e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			List<WebElement> orders =driver.findElements(By
					.xpath("//div[@class=' mw_was_table mw-main-table mw-recent-orders-container']/div[@class=' mw_was_tr']"));
			logger.debug("找到"+orders.size()+"笔订单");
			boolean flag = false;
			for (int i = 0; i < orders.size(); i++) {
				WebElement order = orders.get(i);
				List<WebElement> details = order.findElements(By.xpath(".//div"));
				String orderNo = details.get(1).getText();
				logger.debug("订单号："+orderNo);
				
				if(orderNo.equals(mallOrderNo)){
					flag=false;
					String status = details.get(4).getText();
					if(status.contains("Shipped on")){
						Matcher m = Pattern.compile("[0-9]{5,100}").matcher(status);
						if(m.find()){
							String expressNo = m.group();
							logger.debug("找到物流单号："+expressNo);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							flag = true;
						}else{
							logger.debug("找不到物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						
						String expressCompanyDetail = details.get(3).getText();
						String expressCompany = "";
						if(expressCompanyDetail.contains("Global Mail")){
							expressCompany = "USPS";
						}else if(expressCompanyDetail.contains("FedEx")){
							expressCompany = "fedex";
						}else{
							logger.debug("无法识别的物流公司");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
						logger.debug("物流公司:"+expressCompany);
						return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
					}else if(status.contains("located in stock") || status.contains("Processing")){
						logger.error("该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}else if(status.contains("Cancelled")){
						logger.error("订单被取消");
						return AutoBuyStatus. AUTO_SCRIBE_ORDER_CANCELED;
					}
					else{
						logger.debug("无法识别的状态："+status);
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}
				}
			}
			if(flag == false){
				logger.debug("找不到这个订单");
				return AutoBuyStatus.AUTO_SCRIBE_FAIL;
			}
		} catch (Exception e) {
			logger.debug("爬物流失败");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
	}
	
	@Override
	public boolean gotoMainPage() {
		return true;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return null;
	}
	public static void main(String[] args) {
		//47.88.9.177
		AutoBuyGnc gnc = new AutoBuyGnc();
		gnc.login("huax1@outlook.com", "tfb001001");
		gnc.cleanCart();
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "http://www.rebatesme.com/zh/click/?key=c84b611b7b48a1ca97228bdd37eafa7c&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=b991440710c44f1937f259c6cf9dfd02&targetUrl=http%3A%2F%2Fwww.gnc.com%2FGNC-Herbal-Plus-Grape-Seed-Extract-300-mg%2Fproduct.jsp%3FproductId%3D19023476%26affiliateCustomId%3D452ra5%26affiliateId%3D88878%26c3ch%3DAffliate%26c3nid%3D88878%26clickId%3D1728603984%26eesource%3DPJ_AD%3AZ%3AGNC");
		param.put("sku", "[[\"size\",\"100 Capsules\"]]");
		param.put("num", "3");
		param.put("my_price", "97.17");
		param.put("count", "4");
		param.put("suffixNo", "319");
		gnc.selectProduct(param);
		gnc.pay(param);
	}
}
