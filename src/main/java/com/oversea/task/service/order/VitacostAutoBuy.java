package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年12月8日 上午11:03:48 
*/
public class VitacostAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	public VitacostAutoBuy() {
		super(false);
	}

	public VitacostAutoBuy(boolean isWap) {
		super(isWap);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		driver.get("http://www.vitacost.com");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#yourAccount a")));
			Utils.sleep(1500);
			logger.debug("--->跳转到登录页面");
			driver.executeScript("var tar=arguments[0];tar.click();", signIn);
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		WebElement loginArea = null;
		try {
			loginArea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("IamMasterFrameYesIam_ctl02_objLogin_tblLogin")));
			Utils.sleep(5000);
			
			// 输入账号
			WebElement username = loginArea.findElement(By.id("IamMasterFrameYesIam_ctl02_objLogin_txtExistingCustomerEmail"));
			logger.debug("--->输入账号");
			driver.executeScript("arguments[0].focus();", username);
			username.sendKeys(userName);
			Utils.sleep(1500);
			
			// 输入密码
			WebElement passward = loginArea.findElement(By.id("IamMasterFrameYesIam_ctl02_objLogin_txtExistingCustomerPassword"));
			logger.debug("--->输入密码");
			passward.sendKeys(passWord);
			Utils.sleep(5000);

			// 提交
			WebElement submitBtn = loginArea.findElement(By.id("IamMasterFrameYesIam_ctl02_objLogin_buttonLogin"));
			logger.debug("--->开始提交");
			submitBtn.click();
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("yourAccount")));
			logger.info("--->登录成功");
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@id='MyCartHover']")));
			checkout.click();
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->加载购物车页面出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		try {
			//等待购物车页面加载完成
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='shoppingCartTable']")));
			logger.error("--->购物车页面加载完成");
			Utils.sleep(2000);
			
			logger.error("--->清空购物车商品");
			List<WebElement> goodsInCart = driver.findElements(By.cssSelector("li.delete-cart-item a.delete"));
			if (goodsInCart != null) {
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");

				for (int i = 0; i < goodsInCart.size(); i++) {
					try {
						if(i > 0){
							WebElement removeBtn = driver.findElement(By.cssSelector("li.delete-cart-item a.delete"));
							driver.executeScript("var tar=arguments[0];tar.click();", removeBtn);
						} else {
							goodsInCart.get(i).click();
						}
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						TimeUnit.SECONDS.sleep(5);
					} catch (Exception e) {
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
					TimeUnit.SECONDS.sleep(5);
				}
			} else {
				logger.debug("--->购物车为空！");
			}

		} catch (Exception e) {
			logger.error("--->选择需要删除的商品出错", e);
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		try {
			logger.error("--->确认购物车是否清空");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("IamMasterFrameYesIam_ctl02_divNoItems")));
		} catch (Exception e) {
			logger.debug("--->购物车不为空！");
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
			logger.debug("--->打开商品页面失败 = " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		// 等待商品页面加载
		logger.debug("--->开始等待商品页面加载");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("productDetailPage")));
			logger.debug("--->商品页面加载完成");
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//判断商品是否下架
		try {
			TimeUnit.SECONDS.sleep(2);
			WebElement outOfStockMsg = driver.findElement(By.className("RSTR_TopInStock_Product section"));
			if(outOfStockMsg!=null && !StringUtil.isEmpty(outOfStockMsg.getText()) && !(outOfStockMsg.getText().equals("in stock"))){
				logger.debug("--->这款商品已经下架");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("--->找到该款商品");
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			String priceStr = null;
			List<WebElement> prices = driver.findElements(By.cssSelector("ul#pdpProductPricing  li#pdpSubPrice p"));
	        if (prices != null) {
	            for (WebElement el : prices) {
	                String txt = el.getText().toLowerCase().trim();
	                if (txt.contains("our")) {
	                    String salePrice = txt;
	                    if (salePrice != null) {
	                    	priceStr = salePrice.substring(salePrice.indexOf("$")+1, salePrice.length());
	                    }
	                } else if (txt.contains("sale")) {
	                    String salePrice = txt;
	                    if (salePrice != null) {
	                    	priceStr = salePrice.substring(salePrice.indexOf("$")+1, salePrice.length());
	                    }
	                }
	            }
	        }

			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->找到商品单价 = " + priceStr);
				priceMap.put(productEntityId, priceStr);
			}
		} catch (Exception e) {
			logger.error("--->单价获取失败");
		}
		
		String productNum = (String) param.get("num");
		// 选择商品数量
		if (StringUtil.isNotEmpty(productNum) && !productNum.equals("1"))
		{
			try
			{
				logger.debug("--->选择数量:" + productNum);
				WebElement numInput = driver.findElement(By.xpath("//input[@class='pdp-quantity' and @value='1']"));
				numInput.clear();
				TimeUnit.SECONDS.sleep(2);
				numInput.sendKeys(productNum);
				TimeUnit.SECONDS.sleep(5);
			}
			catch (Exception e)
			{
				logger.debug("--->选择数量出错");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}
		
		//加购物车
		logger.debug("--->开始加购物车");
		try{
			WebElement cart = driver.findElement(By.id("addToCartButton"));
			Utils.sleep(1500);
			cart.click();
			logger.debug("--->加购物车成功");
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->点击继续购物");
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='atc-popup-wrapper']//a[contains(text(),'<< Continue Shopping')]")));
			checkout.click();
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->点击继续购物出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->点击继续购物完成");
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		// 设置价格
		logger.error("--->myPrice = " + myPrice);

		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		
		try {
			logger.debug("--->开始跳转到购物车页面");
			driver.navigate().to("http://www.vitacost.com/Checkout/ShoppingCart.aspx?sce=view");
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->跳转到购物车页面出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("IamMasterFrameYesIam_ctl02_btnCheckOut2")));
			Utils.sleep(1500);
			goPay.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->等待购物车页面加载出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		logger.debug("--->等待结算页面加载");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cnrCttW")));
		} catch (Exception e) {
			logger.debug("--->等待结算页面加载出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}
		// 选收货地址
		logger.debug("--->选择收货地址");
		try {
			TimeUnit.SECONDS.sleep(2);
			try {
				WebElement otherAddressBtn = driver.findElement(By.xpath("//a[@id='spcFrmShpAddrSelectShowAddressBook']"));
				otherAddressBtn.click();
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {}
			
			List<WebElement> addressEle = driver.findElements(By.cssSelector("div#spcShpAddressBookList div.abAddrOut"));
			Iterator<WebElement>  list= addressEle.iterator();
			while(list.hasNext()){
				WebElement element = list.next();
				if(element.getText().contains("310000")){
					logger.debug("This is billing address!");
					list.remove();
				}
			}
			if (addressEle != null && addressEle.size() > 0) {
				logger.debug("--->目前共有[" + addressEle.size() + "]个可用地址");
				
				int index = 0;
				try {
					index = Integer.valueOf(count);
					int tarAddr = index % 2;

					WebElement cur = addressEle.get(tarAddr);
					Utils.sleep(1500);
					cur.click();
					logger.debug("--->选择第" + (tarAddr + 1) + "个地址成功");
					Utils.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择地址出错 = ", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		
		// 选择物流 
		logger.debug("--->选择物流");
		try {
			List<WebElement> opts = driver.findElements(By.cssSelector("div#spcShpMtdFrm1 div.spcBlind_ShippingMethod_C_Loader input"));
			this.logger.debug("--->目前共有[" + opts.size() + "]种物流可选");
			List<WebElement> _opts = driver.findElements(By.cssSelector("div#spcShpMtdFrm1 div.spcBlind_ShippingMethod_C_Loader label"));
			for (WebElement opt : _opts) {
				if (opt != null) {
					System.out.println("======>"+opt.getText());
					if(opt.getText().contains("Standard Shipping")){
						opt.click();
						logger.debug("--->选择Standard Shipping物流成功");
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->选择物流失败");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}
		
		// 	选择支付方式
		try {
			logger.debug("--->选择支付方式");
			Utils.sleep(5000);
			WebElement spcFrmPmntInfOpen = driver.findElement(By.id("spcFrmPmntInfOpen"));
			spcFrmPmntInfOpen.click();
		} catch (Exception e) {
			logger.debug("--->点击支付方式失败");
		}
		
		// 填写信用卡安全码 卡号后3位数字
		try {
			Utils.sleep(5000);
			WebElement securityCode = driver.findElement(By.id("spcFrmPmntSavedCCSecCodeDisp"));
			logger.debug("--->找到信用卡输入框,开始输入");
			String code = (String) param.get("suffixNo");
			logger.debug("--->信用卡卡号是 = " + code);
			if (Utils.isEmpty(code)) {
				logger.debug("--->没有找到可用的信用卡安全码");
			}
			securityCode.sendKeys(code);
			Utils.sleep(1500);
			logger.debug("--->输入信用卡安全码结束");
		} catch (Exception e) {
			logger.debug("--->没找到信用卡安全码输入框");
		}
		
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement = driver
					.findElement(By.className("spcSFld"));
			String text = totalPriceElement.getText();
			if (!Utils.isEmpty(text) && text.indexOf("$") != -1) {
				String priceStr = text.replace("$", "").replace(",", "").trim();
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
				logger.debug("--->找到商品结算总价 = " + priceStr);
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
					if (v.doubleValue() > 100.00D) {
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
		}
		
		// placeOrder 点击付款
		logger.debug("--->开始点击付款 place my Order");
		try {
			Utils.sleep(1500);
			if (isPay) {
				logger.debug("------------------");
				logger.debug("--->开始付款啦！！！");
				driver.executeScript("document.getElementById('spcFrmPlcOrdr').click()");
				logger.debug("------------------");
			}
			logger.debug("--->点击付款完成 placeOrder finish");
		} catch (Exception e) {
			logger.debug("--->付款失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("orderConfirmationPage")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.id("IamMasterFrameYesIam_ctl02_lblOrderId"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取vitacost单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
			} else {
				logger.error("--->获取vitacost单号出错!");
				return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
			}
		} catch (Exception e) {
			logger.debug("--->查找商品订单号出现异常");
			return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}
	
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		try {
			Utils.sleep(5000);
			String orderUrl ="https://www.vitacost.com/MyAccount/OrderList.aspx";
			driver.navigate().to(orderUrl);
			
			Utils.sleep(5000);

			List<WebElement>  boxs = driver.findElements(By.xpath("//div[@class='section']"));
			if(boxs != null && boxs.size() > 0){
				for(WebElement box : boxs){
					boolean flag = box.getText().contains(mallOrderNo.trim());
					logger.debug("--->orderNo text:" + box.getText().replaceAll("[^\\d]", "").trim() + "对比结果:" + flag);
					if (flag) {
						if(box.getText().contains("Shipped on")){
							WebElement trackEle = box.findElement(By.cssSelector("div.oCol1of3 dl.columns a"));
							
							String expressNo = trackEle.getText().trim();
							String expressCompany = null;
							String url = trackEle.getAttribute("href");
							if(url.contains("ups")){
								expressCompany = "UPS";
							} else if(url.contains("usps")){
								expressCompany = "USPS";
							} else if(url.contains("ontrac")) {
								expressCompany = "OnTrac";
							} else if(url.contains("fedex")){
								expressCompany = "FedEx";
							} else {
								logger.debug("没有找到物流公司名称");
							}
							
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
							logger.error("expressCompany = " + expressCompany);
							logger.error("expressNo = " + expressNo);
							return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
						}else if(box.getText().contains("Not Yet Shipped")) {
							logger.error(mallOrderNo + "该订单还没发货,没产生物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}else if(box.getText().contains("Cancelled")){
							logger.error("该订单被砍单了");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
						} else {
							logger.error("该订单还没发货,没产生物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}
					}
				}
			} 
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单", e);
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		try {
			Utils.sleep(2000);
			driver.get("http://www.vitacost.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("C_TopNav_Logout")));
			return true;
		} catch (Exception e) {
			logger.error("--->跳转vitacost主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		VitacostAutoBuy auto = new VitacostAutoBuy();
		auto.login("hkyunl@163.com", "tfb001001");
		//auto.cleanCart();
	
		RobotOrderDetail detail = new RobotOrderDetail();
		/*Map<String, String> param = new LinkedHashMap<>();
		param.put("url", "http://www.vitacost.com/childlife-liquid-vitamin-c-natural-orange");
		param.put("num", "2");
		param.put("productEntityId", "4026058");
		param.put("isPay", "false");
		param.put("count", "2");
		param.put("my_price", "4.49");
		param.put("suffixNo", "123");
		auto.selectProduct(param);
		auto.pay(param);
		
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("309194345");
		auto.scribeExpress(detail);
		auto.logout();*/
		detail.setMallOrderNo("606668146");
		auto.scribeExpress(detail );
	}

}
 