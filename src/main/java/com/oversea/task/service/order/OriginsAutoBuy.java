package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年10月19日 下午5:20:46 
*/
public class OriginsAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	
	public OriginsAutoBuy() {
		super(false);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		driver.get("http://www.origins.com");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		
		try {
			WebElement cboxClose = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cboxClose")));
			cboxClose.click();
		} catch (Exception e) {
			logger.error("--->没有蒙层不需要关闭");
		}
		
		try {
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='gnav_lyl_signin']")));
			Utils.sleep(1500);
			logger.debug("--->跳转到登录页面");
			driver.executeScript("var tar=arguments[0];tar.click();", signIn);
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		WebElement loginArea = null;
		try {
			loginArea = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.page-header__nav div.page-header__nav__supplemental div.signin-block__forms")));
			Utils.sleep(5000);
			
			// 输入账号
			WebElement username = loginArea.findElement(By.name("EMAIL_ADDRESS"));
			logger.debug("--->输入账号");
			driver.executeScript("arguments[0].focus();", username);
			username.sendKeys(userName);
			Utils.sleep(1500);
			
			// 输入密码
			List<WebElement> passward = loginArea.findElements(By.name("PASSWORD"));
			logger.debug("--->输入密码");
			if(passward.size()>0){
				for(WebElement el:passward){
					if(el.isDisplayed()){
						el.sendKeys(passWord);
					}
				}
			}
			Utils.sleep(5000);

			// 提交
			List<WebElement> submitBtn = loginArea.findElements(By.cssSelector("input.signin-block__submit"));
			logger.debug("--->开始提交");
			if(passward.size()>0){
				for(WebElement el:submitBtn){
					if(el.isDisplayed()){
						el.click();
					}
				}
			}
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		
		// 等待登录完成
		try {
			logger.debug("--->等待登录完成"); 
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.content-header__inner")));
			logger.debug("--->登录完成");
		} catch (Exception e) {
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		
		driver.navigate().to("https://www.origins.com/checkout/viewcart.tmpl");
		//清理购物车
		try{
			//等待购物车页面加载完成
			logger.error("--->开始等待购物车页面加载");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("header.checkout__header")));
			logger.error("--->购物车页面加载完成");
			//清理
			logger.error("--->开始清理购物车");
			List<WebElement> list = driver.findElements(By.cssSelector("a.link.remove_link"));
			while (true) {
				int size = list.size();
				if(list!=null && size>0){
					driver.executeScript("(function(){var els = document.getElementsByClassName('remove_link');if(els && els[0]){els[0].click();}})();");
					Utils.sleep(2000);
					if(size>1){
						list = driver.findElements(By.cssSelector("a.link.remove_link"));
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
			WebElement cartNum = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("error_cart.empty")));
			logger.debug("--->购物车内容:"+cartNum.getText());
		} catch (Exception e) {
			logger.debug("--->购物车数量清空异常");
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("content")));
			logger.debug("--->商品页面加载完成");
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//判断商品是否下架
		try {
			TimeUnit.SECONDS.sleep(2);
			WebElement outOfStockMsg = driver.findElement(By.className("gnav_lyl_pts js-gnav_lyl_pts"));
			if(outOfStockMsg!=null && !StringUtil.isEmpty(outOfStockMsg.getText())){
				logger.debug("--->这款商品已经下架");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("--->找到该款商品");
		}
		
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		if (sku != null)
		{
			// 开始选择sku
			logger.debug("--->开始选择sku");
			
			Map<String, String> skuMap = new HashMap<String, String>();
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i += 2)
			{
				skuMap.put(skuList.get(i), skuList.get(i + 1));
			}
			
			String size = skuMap.get("size").trim();
			logger.debug("--->sku :" + skuMap);
			//选择size
			if (StringUtil.isNotEmpty(size)){
				WebElement w = null;
				try{
					w = driver.findElement(By.cssSelector("div.product-sku-select>div.product-sku-select__placeholder"));
				}catch(Exception e){
					logger.error(e);
					try{
						w = driver.findElement(By.cssSelector("a.selectBox.product-sku-select__selectbox>span.selectBox-label"));
					}catch(Exception ee){
						logger.error(ee);
					}
				}
				if(w == null){
					logger.error("找不到product-sku-select__selectbox");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
				try {
					String text = w.getText();
					int index = text.indexOf(":");
					String v = text.substring(index+1).trim();
					v = v.toLowerCase();
					if(!size.equals(v)){
						w.click();
						Utils.sleep(1500);
						boolean isSelect = false;
						List<WebElement> list = driver.findElements(By.cssSelector("ul.selectBox-dropdown-menu.selectBox-options.product-sku-select__selectbox-selectBox-dropdown-menu>li"));
						for(WebElement ww : list){
							String str = ww.getText().toLowerCase();
							if(str.contains(size)){
								isSelect = true;
								logger.error("选择sku:"+size);
								ww.click();
								break;
							}
						}
						if(!isSelect){
							logger.error("没有选择正确的sku,return");
							return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
						}
					}else{
						logger.error("默认size正确,不需要选择");
					}
					
					TimeUnit.SECONDS.sleep(2);
				} catch (Exception e) {
					logger.error(e);
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElment = driver.findElement(By.cssSelector("span.product-sku-price__value"));
			String priceStr = priceElment.getText();
			String productEntityId = param.get("productEntityId");
			if (!Utils.isEmpty(priceStr) && priceStr.startsWith("$") && StringUtil.isNotEmpty(productEntityId)) {
				logger.debug("--->找到商品单价 = " + priceStr.substring(1));
				priceMap.put(productEntityId, priceStr.substring(1));
			}
		} catch (Exception e) {
			logger.error("--->单价获取失败");
		}
		
		// 选择商品数量
		if (StringUtil.isNotEmpty(productNum) && !productNum.equals("1"))
		{
			try
			{
				logger.debug("--->选择数量:" + productNum);
				WebElement productQty = driver.findElement(By.cssSelector("a.product-qty-select__selectbox"));
				productQty.click();
				TimeUnit.SECONDS.sleep(3);
				List<WebElement> qtySelect = driver.findElements(By.cssSelector("ul.selectBox-dropdown-menu li a"));
				for(WebElement qtyEle : qtySelect){
					String val = qtyEle.getAttribute("rel");
					if(productNum.equals(val)){
						logger.debug("--->选择数量成功：" + val);
						qtyEle.click();
						break;
					}
				}
				TimeUnit.SECONDS.sleep(2);
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
			/*WebElement cart = driver.findElement(By.cssSelector("a.product-add-to-bag"));
			Utils.sleep(1500);
			cart.click();*/
			
			driver.executeScript("document.querySelectorAll('a.product-add-to-bag.button.button--medium.js-add-to-cart.js-product-cta')[0].click();");
			TimeUnit.SECONDS.sleep(5);
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到",e);
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->查看购物车");
		try{
			WebElement checkout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='cart-block__content']//a[contains(text(),'View Bag')]")));
			checkout.click();
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->查看购物车出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->查看购物车完成");
		
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
		/*String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}*/
		
		/*int index = Integer.valueOf(count);
		int tarAddr = index % 2;
		try {
			if(tarAddr == 1){
				//设置收货地址
				logger.debug("--->开始跳转到收货地址页面");
				driver.navigate().to("https://www.origins.com/account/address_book/index.tmpl");
				Utils.sleep(1500);
				
				WebElement ship = wait.until(ExpectedConditions.visibilityOfElementLocated(
						By.className("page-wrapper")));
				WebElement defaultAddr = ship.findElement(By.cssSelector("a.js-default-address"));
				defaultAddr.click();
				logger.debug("--->选择第" + (tarAddr + 1) + "个地址成功");
				Utils.sleep(1500);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}*/
		
		try {
			logger.debug("--->开始跳转到购物车页面");
			driver.navigate().to("https://www.origins.com/checkout/viewcart.tmpl");
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->跳转到购物车页面出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@class='checkout-buttons__item button continue-checkout' and contains(text(),'Checkout')]")));
			Utils.sleep(1500);
			goPay.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->等待购物车页面加载出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		logger.debug("--->等待继续页面加载");
		try {
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.cssSelector("div.continue-shopping-button")));
			Utils.sleep(1500);
			goPay.click();
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.debug("--->等待继续页面加载出现异常");
			//return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 选择收货地址
		logger.debug("--->选择收货地址");
		try {
			boolean defaultIsSelected = true;
			if (defaultIsSelected)
			{
				Utils.sleep(1500);
				logger.error("--->默认已经选中收货地址");
			}
		} catch (Exception e) {
			logger.debug("--->选择收货地址失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 选择账单地址 billing-address
		logger.debug("--->选择账单地址");
		try {
			boolean defaultIsSelected = true;
			if (defaultIsSelected)
			{
				Utils.sleep(1500);
				logger.error("--->默认已经选中账单地址");
			}
		} catch (Exception e) {
			logger.debug("--->选择账单地址失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		/*// 同意购买协议
		logger.debug("--->选择同意购买协议");
		try {
			Utils.sleep(1500);
			WebElement agree = driver.findElement(By.className("sms_promo_label"));
			agree.click();
			Utils.sleep(1500);
		} catch (Exception e) {
			logger.debug("--->选择同意购买协议失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}*/
		
		// 查询总价
		try {
			logger.debug("--->开始查询总价");
			Utils.sleep(5000);
			WebElement totalPriceElement = driver
					.findElement(By.cssSelector("div.order-summary-panel__total--value"));
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
					//运费 $6
					if (v.doubleValue() > 11.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			}
		} catch (Exception e) {
			logger.debug("--->查询结算总价出现异常");
		}
		
		// 点击付款
		logger.debug("--->开始点击付款");
		try {
			driver.executeScript("(function(){window.scrollBy(600,800);})();");
			WebElement placeOrderElement = driver.findElement(By.xpath("//input[@class='button button form-submit']"));
			Utils.sleep(1500);
			if (isPay) {
				logger.debug("------------------");
				logger.debug("--->啊啊啊啊，我要付款啦！！！");
				placeOrderElement.sendKeys(Keys.ENTER);;
				Utils.sleep(1500);
				logger.debug("------------------");
			}
			logger.debug("--->点击付款完成");
		} catch (Exception e) {
			logger.debug("--->付款失败");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 查询商城订单号
		try {
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("checkout__content")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.cssSelector("p.confirmation-panel__confirm-text--signedin a.link"));
			String orderNumber = order.getText().trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取origins单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
				savePng();
			} else {
				logger.error("--->获取origins单号出错!");
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
		
		//寻找Orders
		try{
			logger.debug("--->开始跳转到订单页面");
			driver.navigate().to("https://www.origins.com/account/order_history/index.tmpl");
		}
		catch (Exception e){
			logger.error("--->跳转到Orders页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section.account-page__content")));
			List<WebElement> orderList = driver.findElements(By.cssSelector("div.orders-list__table div.order-row"));
			if(orderList != null && orderList.size() > 0){
				for (WebElement orders : orderList) {
					WebElement orderNo = orders.findElement(By.cssSelector("a.order-detail-item-link"));
					boolean flag = orderNo.getText().trim().contains(mallOrderNo.trim());
					logger.debug("--->orderNo text:" + orderNo.getText().trim() + "对比结果:" + flag);
					if (flag) {
						// 订单编号状态
						WebElement status = orders.findElement(By.cssSelector("div.order-status"));
						
						String str = status.getText().trim();
						if (str.equals("Shipped")) {
							Utils.sleep(2500);
							logger.debug("--->查找物流单号");
							
							WebElement tarTd = orders.findElement(By.cssSelector("li.tracking-link-item a"));
							if(tarTd != null){
								String expressCompany = "";
								String url = tarTd.getAttribute("href");
								String expressNo = tarTd.getText().trim();
								
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
							}
						} else if(str.equals("Cancelled")){
							logger.error("[1]该订单被砍单了");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
						}else{
							logger.error("[1]该订单还没发货,没产生物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("--->查询订单异常,找不到该订单");
			return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
		}
		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage() {
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		OriginsAutoBuy auto = new OriginsAutoBuy();
		//auto.login("zafu231@sina.com", "tfb001001");
		auto.login("1007978129@qq.com", "YY19940715");
		auto.cleanCart();
	
		Map<String, String> param = new HashMap<String, String>();
		param.put("url", "http://www.rebatesme.com/zh/click/?key=33d82e63e9b822a98d242a7ae8856275&sitecode=haihu&showpage=0&partneruname=wenzhe@taofen8.com&checkcode=2bad597c5cf7df4f9cb5373ba5a2d124&targetUrl=http%3A%2F%2Fwww.origins.com%2Fproduct%2F15349%2F23003%2Fskincare%2Ftreat%2Ftreatment-lotions%2FPlantscription%2FAnti-aging-Treatment-Lotion");
		param.put("sku", "[[\"size\",\"5.0 fl. oz. / 150 ml \"]]");
		param.put("num", "1");
		param.put("productEntityId", "4026058");
		param.put("isPay", "false");
		param.put("count", "2");
		param.put("my_price", "31.00");
		auto.selectProduct(param);
		auto.pay(param);
		
//		RobotOrderDetail detail = new RobotOrderDetail();
//		detail.setMallOrderNo("2624933035");
//		auto.scribeExpress(detail);
		//auto.logout();*/
	}
}
 