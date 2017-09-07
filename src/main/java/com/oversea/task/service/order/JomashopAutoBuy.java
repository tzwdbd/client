package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
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
import com.oversea.task.utils.ReCaptchasHelper;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

/** 
* @author: yangyan 
  @Package: com.oversea.task.service.order
  @Description:
* @time   2016年8月17日 下午2:56:26 
*/
public class JomashopAutoBuy extends AutoBuy {
	private final Logger logger = Logger.getLogger(getClass());
	private Timer timer;
	
	public JomashopAutoBuy() {
		super(false,false);
		try {
			//每三分钟监控黑色的蒙层
			
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					driver.executeScript("(function(){var els = document.getElementsByClassName('bx-close-link');if(els && els[0]){els[0].click();}})();");
				}
			}, 3000, 3000);
		} catch (Exception e) {
			logger.error("--->初始化失败", e);
			return;
		}
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://www.jomashop.com/");

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement account = wait.until(ExpectedConditions.elementToBeClickable(By.id("top-myaccount-handle")));
			account.click();
			logger.debug("--->跳转到登录页面");
		} catch (Exception e) {
			logger.error("--->跳转到登录页面异常", e);
			sendCode();
			try {
				WebElement account = wait.until(ExpectedConditions.elementToBeClickable(By.id("top-myaccount-handle")));
				account.click();
				logger.debug("--->跳转到登录页面");
			} catch (Exception e1) {
				logger.error("--->跳转到登录页面异常", e);
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		
		driver.executeScript("(function(){window.scrollBy(0,200);})();");
		try {
			Utils.sleep(1500);
			// 输入账号
			wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
			WebElement username = driver.findElement(By.id("email"));
			logger.debug("--->输入账号"+username.getText());
			Utils.sleep(1500);
			username.sendKeys(userName);

			// 输入密码
			WebElement passward = driver.findElement(By.id("register-password"));
			logger.debug("--->输入密码");

			Utils.sleep(1500);
			passward.sendKeys(passWord);

			// 提交
			WebElement submitBtn = driver.findElement(By.id("send2"));
			logger.debug("--->开始提交");

			Utils.sleep(1500);
			submitBtn.click();

		} catch (Exception e) {
			logger.error("--->输入账号或者密码错误");
			sendCode();
			try {
				Utils.sleep(1500);
				// 输入账号
				wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
				WebElement username = driver.findElement(By.id("email"));
				logger.debug("--->输入账号"+username.getText());
				Utils.sleep(1500);
				username.sendKeys(userName);

				// 输入密码
				WebElement passward = driver.findElement(By.id("register-password"));
				logger.debug("--->输入密码");

				Utils.sleep(1500);
				passward.sendKeys(passWord);

				// 提交
				WebElement submitBtn = driver.findElement(By.id("send2"));
				logger.debug("--->开始提交");

				Utils.sleep(1500);
				submitBtn.click();

			} catch (Exception e2) {
				logger.error("--->输入账号或者密码错误");
				return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
			}
		}
		
		// 等待登录完成
		try {
			logger.debug("--->等待登录完成"); 
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-account")));

			logger.debug("--->登录完成");
		} catch (Exception e) {
			driver.executeScript("(function(){window.scrollBy(0,400);})();");
			for(int i=0;i<3;i++){
				boolean mark = true;
				try {
					WebElement capImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recaptcha_challenge_image")));
					logger.error("--->开始破解验证码");
					String src = capImage.getAttribute("src");
					logger.error("--->src:" + src);
					String cap = ReCaptchasHelper.getCaptchas(src, "jomashop");
					if(!StringUtils.isBlank(cap)){
						try
						{
							WebElement	capGuess = driver.findElement(By.id("recaptcha_response_field"));
							logger.debug("--->输入验证码");
							capGuess.sendKeys(cap);
							TimeUnit.SECONDS.sleep(5);
							WebElement btn = driver.findElement(By.id("dCF_input_complete"));
							logger.debug("--->验证验证码");
							btn.click();
						}
						catch (Exception ex)
						{
							logger.error("--->没有找到验证码框", ex);
							return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
						}
						
						try
						{
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
						}
						catch (Exception e1)
						{
							logger.error("--->重试--->没有找到登陆按钮", e1);
							if(i==2){
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
							}else{
								mark = false;
							}
						}
						if(mark){
							try {
								Utils.sleep(1500);
								// 输入账号
								wait.until(ExpectedConditions.elementToBeClickable(By.id("email")));
								WebElement username = driver.findElement(By.id("email"));
								logger.debug("--->输入账号"+username.getText());
								Utils.sleep(1500);
								username.sendKeys(userName);

								// 输入密码
								WebElement passward = driver.findElement(By.id("register-password"));
								logger.debug("--->输入密码");

								Utils.sleep(1500);
								passward.sendKeys(passWord);

								// 提交
								WebElement submitBtn = driver.findElement(By.id("send2"));
								logger.debug("--->开始提交");

								Utils.sleep(1500);
								submitBtn.click();

							} catch (Exception e2) {
								logger.error("--->输入账号或者密码错误", e2);
								return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
							}
							break;
						}
					}
				} catch (Exception e2) {
					logger.error("重试222--->登录失败", e);
					return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
				}
			}
			
			// 等待登录完成
			try {
				logger.debug("--->等待登录完成"); 
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-account")));

				logger.debug("--->登录完成");
			} catch (Exception e3) {
				logger.error("重试222--->登录失败", e3);
				return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
			}
			
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}
	
	public void sendCode(){
		WebDriverWait wait = new WebDriverWait(driver, 30);
			try {
				WebElement capImage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recaptcha_challenge_image")));
				logger.error("--->开始破解验证码");
				String src = capImage.getAttribute("src");
				logger.error("--->src:" + src);
				String cap = ReCaptchasHelper.getCaptchas(src, "jomashop");
				if(!StringUtils.isBlank(cap)){
					try
					{
						WebElement	capGuess = driver.findElement(By.id("recaptcha_response_field"));
						logger.debug("--->输入验证码");
						capGuess.sendKeys(cap);
						TimeUnit.SECONDS.sleep(5);
						WebElement btn = driver.findElement(By.id("dCF_input_complete"));
						logger.debug("--->验证验证码");
						btn.click();
					}
					catch (Exception ex)
					{
						logger.error("--->没有找到验证码框", ex);
					}
				}
			} catch (Exception e2) {
				logger.error("重试222--->登录失败", e2);
			}
	}

	@Override
	public AutoBuyStatus cleanCart() {
		
		boolean hasNoItems = false;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		// 跳转到购物车
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement cart = driver.findElement(By.id("richCartTrigger"));
			logger.error("--->开始跳转到购物车");
			TimeUnit.SECONDS.sleep(5);
			cart.click();
			TimeUnit.SECONDS.sleep(5);
			WebElement cartELe = driver.findElement(By.id("cartDropDwn"));
			
			try {
				WebElement noItems = cartELe.findElement(By.xpath("//div[@class='noItems']"));
				if(noItems!=null && !StringUtil.isEmpty(noItems.getText())){
					logger.debug("--->购物车为空！");
					hasNoItems = true;
				}
			} catch (Exception e) {
			}
			
			if(!hasNoItems){
				WebElement viewCart = cartELe.findElement(By.className("view-cart"));
				TimeUnit.SECONDS.sleep(5);
				viewCart.click();
			}
			TimeUnit.SECONDS.sleep(5);
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		// 清理购物车
		if(!hasNoItems){
			try {
				//等待购物车页面加载完成
				logger.error("--->开始等待购物车页面加载");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='checkout-wrapper-content']")));
				logger.error("--->购物车页面加载完成");
				Utils.sleep(2000);
				
				logger.error("--->清空购物车商品");
				List<WebElement> goodsInCart = driver.findElements(By.xpath("//a[@class='remove btn_temp_1']"));
				if (goodsInCart != null) {
					logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");

					for (int i = 0; i < goodsInCart.size(); i++) {
						try {
							if(i > 0){
								WebElement removeBtn = driver.findElement(By.xpath("//a[@class='remove btn_temp_1']"));
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
					}
				} else {
					logger.debug("--->购物车为空！");
				}

			} catch (Exception e) {
				logger.error("--->选择需要删除的商品出错", e);
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}
		}
		try {
			
			WebElement cartNum = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".bag-qty")));
			logger.debug("--->购物车数量="+cartNum.getText());
			if(!cartNum.getText().equals("0")){
				return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
			}
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("atg_store_content")));
			logger.debug("--->商品页面加载完成");
			Utils.sleep(2000);
		} catch (Exception e) {
			logger.debug("--->等待商品页面加载");
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}
		
		//判断商品是否下架
		try {
			WebElement outOfStockMsg = driver.findElement(By.xpath("//div[@class='outOfStockMsg']"));
			if(outOfStockMsg!=null && !StringUtil.isEmpty(outOfStockMsg.getText())){
				logger.debug("--->这款商品已经下架");
				return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
			}
		} catch (Exception e) {
			logger.debug("--->找到该款商品");
		}
		
		
		String productNum = (String) param.get("num");
		String sku = param.get("sku");
		if (StringUtil.isNotEmpty(sku))
		{
			// 开始选择sku
			logger.debug("--->开始选择sku");
			
			Map<String, String> skuMap = new HashMap<String, String>();
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i += 2)
			{
				skuMap.put(skuList.get(i), skuList.get(i + 1));
			}
			
			String size = skuMap.get("size");
			logger.debug("--->sku :" + skuMap);
			//选择size
			if (size != null)
			{
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sizeValues")));
					List<WebElement> sizes= driver.findElements(By.cssSelector("div#sizeValues div.sizeVal"));
					if(sizes!=null && sizes.size() > 0){
						for (WebElement el : sizes)
						{
							String sizeVal = el.getText().trim().replaceAll("[\r|\n]", "");
							if(sizeVal.equals(size)){
								el.click();
								driver.executeScript("arguments[0].click()", el);
								logger.debug("--->选择size:" + size);
								break;
							}
						}
						logger.debug("--->选择sku完成");
						TimeUnit.SECONDS.sleep(2);
					}
				} catch (Exception e) {
					logger.error("--->选择size出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}
		}
		
		//选择size之后再验证下,判断商品是否下架
		try {
			WebElement outOfStockMsg = driver.findElement(By.xpath("//div[@id='size_picker']"));
			if(outOfStockMsg!=null && !StringUtil.isEmpty(outOfStockMsg.getText())){
				if(outOfStockMsg.getText().trim().contains("OUT OF STOCK")){
					logger.debug("--->这款商品已经下架");
					return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
				}
			}
		} catch (Exception e) {
			logger.debug("--->找到该款商品");
		}
		
		// 寻找商品单价
		try {
			logger.debug("--->开始寻找商品单价");
			WebElement priceElment = driver.findElement(By.cssSelector("table#pricing td.highlight"));
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
		if (!StringUtils.isBlank(productNum) && !productNum.equals("1"))
		{
			try
			{
				logger.debug("--->选择数量:" + productNum);
				WebElement numInput = driver.findElement(By.xpath("//input[@class='qty atg_store_numericInput' and @value='1' and @maxlength='1']"));
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
			WebElement cart = driver.findElement(By.xpath("//input[@id='add_To_Cart_Button']"));
			Utils.sleep(1500);
			cart.click();
			logger.debug("--->加购物车成功");
		}catch(Exception e){
			logger.debug("--->加购物车按钮找不到");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		
		//等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='shoppingCart']")));
			Utils.sleep(1500);
		}catch(Exception e){
			logger.debug("--->加载购物车页面出现异常");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}
		logger.debug("--->购物车页面加载完成");
		
		/*// 选择商品数量
		if (!Utils.isEmpty(productNum) && !productNum.equals("1")) {
			try {
				logger.debug("--->商品数量 = " + productNum);
				
				WebElement inputNum = driver.findElement(By.cssSelector("div.quantity input.product_quantity"));
				Utils.sleep(2000);
				inputNum.clear();
				Utils.sleep(2000);
				inputNum.sendKeys(productNum);
				Utils.sleep(2000);
				WebElement upate = driver.findElement(By.cssSelector("div.cart_item_content a.update"));
				Utils.sleep(2000);
				upate.click();
			}  catch (Exception e) {
				logger.error("--->选择数量失败");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}
		}*/
		
		/*//查询商品数量是否可买
		try{
			driver.findElement(By.xpath("//li[@class='errorMessage']"));
			logger.debug("--->选择商品数量太多，不支付一次性购买这么多件商品");
			return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
		}catch(Exception e){
			logger.debug("--->商品数量没有问题,carry on");
		}*/
		
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param) {
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice)) {
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}
		
		
		// 设置价格
		logger.error("--->myPrice = " + myPrice);

		Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
		String countTemp = (String) param.get("count");
		int count = 0;
		if (!Utils.isEmpty(countTemp)) {
			count = Integer.parseInt(countTemp);
		}

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		//使用优惠码0 失效,1互斥 ,9没修改过,10有效
		Set<String> promotionList = getPromotionList(param.get("promotion"));
		if(promotionList != null && promotionList.size() > 0){
			logger.debug("promotionList.toString()"+promotionList.toString());
			//判断是否已经有优惠吗输进去
			logger.debug("开始输入优惠码");
			try {
				TimeUnit.SECONDS.sleep(2);		
				WebElement del =  driver.findElement(By.xpath("//a[@id='atg_store_promotionRemove']"));
				del.click();
				logger.debug("有优惠码生效，删除掉，输入新的优惠码");
			} catch (Exception e) {}
			boolean isEffective = false;
			HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
			for(String code : promotionList){
				code = code.trim();
				logger.debug("code:"+code);
				try {
					logger.debug("--->等待优惠码框加载");
					TimeUnit.SECONDS.sleep(2);									              
					WebElement promotionCodeInput = driver.findElement(By.xpath("//input[@id='promotionCodeInput']"));
					WebElement applyBtn = driver.findElement(
							By.xpath("//a[@class='add_promotion btn_temp_2' and contains(text(),'应用')]"));
					logger.debug("--->找到优惠码输入框,开始输入");
					promotionCodeInput.sendKeys(code);
					logger.debug("--->输入优惠码结束");
					driver.executeScript("arguments[0].click()", applyBtn);
					logger.debug("--->点击应用");
					try {
						TimeUnit.SECONDS.sleep(3);
						driver.findElement(By.xpath("//a[@id='atg_store_promotionRemove']"));
						logger.debug("优惠码生效:"+code);
						isEffective = true;
						statusMap.put(code, 10);
						break;
					} catch (Exception e) {
						statusMap.put(code, 0);
						logger.debug("优惠码无效:"+code);
					}
				} catch (Exception e) {
					logger.debug("输入优惠码异常",e);
				}
			}
			setPromotionCodelistStatus(statusMap);
			if("true".equals(param.get("isStock")) && !isEffective){
				logger.debug("--->优惠码失效,中断采购");
				return AutoBuyStatus.AUTO_PAY_FAIL;
			}
			/*logger.debug("--->等待优惠码框加载");
			try {
				WebElement promotionCodeInput = driver.findElement(By.xpath("//input[@id='promotionCodeInput']"));
				WebElement applyBtn = driver.findElement(
						By.xpath("//a[@class='add_promotion btn_temp_2' and contains(text(),'应用')]"));

				logger.debug("--->找到优惠码输入框,开始输入");
				if(promotionCodeInput!=null && applyBtn!=null){
					for(String code : promotionList){
						if(StringUtil.isNotEmpty(code)){
							logger.debug("--->优惠码是 = " + code);
							if (Utils.isEmpty(code)) {
								logger.debug("--->没有找到可用的优惠码");
							}
							promotionCodeInput.sendKeys(code);
							logger.debug("--->输入优惠码结束");
//							applyBtn.click();
							driver.executeScript("arguments[0].click()", applyBtn);
							logger.debug("--->点击应用");
							Utils.sleep(1500);
							try {
								WebElement errorEle = driver.findElement(By.xpath("//div[@id='promoCodeText']"));
								if(errorEle!=null){
									logger.debug("--->优惠码不可用！");
								}
							} catch (Exception e) {}
						}
					}
				}
			} catch (Exception e) {
				try {
					WebElement promoMsgEle = driver.findElement(By.cssSelector("div.shopping_promotion_CodeApplied label"));
					String promoMsg = promoMsgEle.getText();
					if(promoMsg.contains("已经应用") || promoMsg.contains("已生效")){
						logger.debug("--->优惠码已存在，已生效！");
					}
				} catch (Exception e2) {
					logger.debug("--->输入优惠码异常");
				}
			}*/
		}
		
		
		// 等待购物车页面加载完成
		logger.debug("--->等待购物车页面加载");
		try {
			TimeUnit.SECONDS.sleep(5);
			WebElement goPay = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//a[@class='checkout_btn btn_temp_2' and contains(text(),'Ashford   结账')]")));
			Utils.sleep(1500);
			goPay.click();
			Utils.sleep(5000);
		} catch (Exception e) {
			logger.debug("--->加载Ashford   结账出现异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		logger.debug("--->等待支付页面加载");
		
		driver.executeScript("(function(){window.scrollBy(100,300);})();");
		try {
			TimeUnit.SECONDS.sleep(2);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='checkout-wrapper-content']")));
			logger.debug("--->支付页面加载完成");
			TimeUnit.SECONDS.sleep(3);
		} catch (Exception e) {
			logger.debug("--->支付页面加载异常");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		// 选收货地址
		logger.debug("--->选择收货地址");
		try {
			TimeUnit.SECONDS.sleep(2);
			try {
				WebElement otherAddressBtn = driver.findElement(By.xpath("//a[@id='moreAddressBtn' and contains(text(),'其他地址')]"));
				otherAddressBtn.click();
				TimeUnit.SECONDS.sleep(2);
			} catch (Exception e) {}
			
			List<WebElement> addressEle = driver.findElements(By.cssSelector("div.checkout-shippingAddress-card"));
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
					WebElement radio = cur.findElement(By.cssSelector("div.addressCard-radio input[id]"));
					radio.click();
					logger.debug("--->选择第" + (tarAddr + 1) + "个地址成功");
					Utils.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
				}
				// 保存收货地址
				WebElement saveAddressBtn = driver.findElement(By.id("saveAddressBtn"));
				Utils.sleep(1500);
				saveAddressBtn.sendKeys(Keys.ENTER);
				//saveAddressBtn.click();
				logger.debug("--->保存地址成功");
				Utils.sleep(5000);
			}
		} catch (Exception e) {
			logger.debug("--->选择地址出错 = ", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}

		// 选择物流
		logger.debug("--->选择物流");
		try {
			boolean defaultIsSelected = true;
			if (defaultIsSelected)
			{
				Utils.sleep(5000);
				logger.debug("--->默认已经选中FREE");
			} 
		} catch (Exception e) {
			logger.debug("--->选择物流失败");
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}	
		
		// 选择信用卡
		logger.debug("--->选择信用卡");
		try {
			driver.executeScript("(function(){window.scrollBy(200,300);})();");
			TimeUnit.SECONDS.sleep(5);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkout-payment")));
			//WebElement checkout = driver.findElement(By.id("checkout-payment"));
			//WebElement creditCardTabNav = checkout.findElement(By.cssSelector("li#creditCardTabNav a"));
			WebElement select = driver.findElement(By.xpath("//li[@id='creditCardTabNav']/a"));
			select.sendKeys(Keys.ENTER);
			//driver.executeScript("arguments[0].click()", creditCardTabNav);
			TimeUnit.SECONDS.sleep(3);
			logger.debug("--->切换到信用卡页面");
		} catch (Exception e) {
			logger.debug("--->没有切换信用卡");
			return AutoBuyStatus.AUTO_PAY_CAN_NOT_FIND_CARDNO;
		}
		
		// 输入安全码
		try {
			Utils.sleep(5000);
			WebElement securityCode = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("securityCodeInput")));
			logger.debug("--->找到信用卡安全码输入框,开始输入");
			String code = (String) param.get("suffixNo");
			logger.debug("--->信用卡安全码是 = " + code);
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
					.findElement(By.cssSelector("li span.shipping-total"));
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
					if (v.doubleValue() > 5.00D) {
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
			WebElement placeOrderElement = driver.findElement(By.xpath("//button[@class='btn_temp_2 pull-right']"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ship_cart")));
			logger.debug("--->订单页面加载完成");
			WebElement order = driver.findElement(By.className("cart_title"));
			String orderNumber = order.getText().replaceAll("[^\\d]", "").trim();

			if (!Utils.isEmpty(orderNumber)) {
				logger.error("--->获取ashford单号成功:\t" + orderNumber);
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNumber);
			} else {
				logger.error("--->获取ashford单号出错!");
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
		String tag = mallOrderNo.substring(0,1);
		if(tag.equalsIgnoreCase("M")){
			mallOrderNo = mallOrderNo.substring(1);
		}
		try{
			driver.navigate().to(detail.getProductUrl());
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e){
			logger.debug("--->商品页面打开失败");
		}
		logger.debug("--->页面title:"+driver.getTitle());
		String productName = driver.getTitle();
		try {
			driver.navigate().to("https://www.jomashop.com/sales/order/history/");
		} catch (Exception e) {
			logger.error("--->查询url订单异常");
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		Utils.sleep(1500);
		// 寻找我的订单
		try{
			logger.debug("--->等待订单页面加载");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".my-account")));
		}
		catch (Exception e){
			logger.error("--->订单页面加载出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		try {
			logger.debug("--->开始查找对应订单");
			TimeUnit.SECONDS.sleep(5);
			WebElement orderTable = driver.findElement(By.id("my-orders-table"));
			logger.debug("--->找到对应的table");
			List<WebElement> orderList = orderTable.findElements(By.cssSelector("tbody tr"));
			for (WebElement orders : orderList) {
				boolean flag = orders.getText().trim().contains(mallOrderNo.trim());
				logger.debug("--->orderNo text:" + orders.getText().trim() + "对比结果:" + flag);
				
				if (flag) {
					Utils.sleep(1500);
					// 订单编号状态
					WebElement status = orders.findElement(By.cssSelector("td em"));
					
					String str = status.getText().trim().toLowerCase();
					if (str.equals("shipped") || str.equals("partially shipped")) {
						Utils.sleep(2500);
						logger.debug("--->点击查看物流详情");
						
						WebElement viewBtn = orders.findElement(By.cssSelector("td.a-center a"));
						viewBtn.click();
						TimeUnit.SECONDS.sleep(2);//等待页面载入
						
						//等到物流单号页面加载
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".order-info")));
						logger.debug("--->开始查找物流 ");
						WebElement shipment = driver.findElement(By.xpath("//a[contains(text(),'View Shipments')]"));
						shipment.click();
						TimeUnit.SECONDS.sleep(2);
						List<WebElement> productList = driver.findElements(By.cssSelector(".accordian-body"));
						boolean mark = true;
						for(WebElement w:productList){
							WebElement productNameElement = w.findElement(By.cssSelector("table .product-name"));
							if(productName.contains(productNameElement.getText())){
								mark = false;
								w.findElement(By.cssSelector("table a")).click();
								Utils.sleep(10000);
								String parentWindowId = driver.getWindowHandle();
								Set<String> allWindowsId = driver.getWindowHandles();
								for (String windowId : allWindowsId) {
						            if (!windowId.equals(parentWindowId)) {
						                driver.switchTo().window(windowId);
						                break;
						            }
						        }
								List<WebElement> trs = driver.findElements(By.cssSelector("table tbody tr"));
								int i = 0;
								for(WebElement w1:trs){
									String expressKey= w1.findElement(By.cssSelector(".label")).getText();
									String expressValue= w1.findElement(By.cssSelector(".value")).getText();
									if(expressKey.toLowerCase().contains("tracking number")){
										String expressNo = expressValue.trim();
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, expressNo);
										logger.error("expressNo = " + expressNo);
										i++;
			
									}else if(expressKey.toLowerCase().contains("carrier")){
										if(expressValue.toLowerCase().contains("united states postal service")){
											data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "USPS");
											logger.error("expressCompany = USPS");
											i++;
										}else if(expressValue.toLowerCase().contains("shipping")){
											String expressCompany = "UPS";
											data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, expressCompany);
											logger.error("expressCompany = " + expressCompany);
											i++;
										}else{
											logger.error("未知物流"+expressValue);
										}
									}
									
								}
								driver.switchTo().window(parentWindowId);
								if(i==2){
									return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
								}
								break;
							}
						}
						if(mark){
							logger.error("该订单还没发货,没产生物流单号");
							return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
						}
						//driver.findElement(By.cssSelector("a.track-order")).click();
						
					} else if(str.equals("canceled")){
						logger.error("该订单被砍单了");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					} else {
						logger.error("该订单还没发货,没产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
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
		try {
			Utils.sleep(2000);
			driver.get("http://www.jomashop.com/");
			return true;
		} catch (Exception e) {
			logger.error("--->跳转jomashop主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		timer.cancel();
		return true;
	}

	public static void main(String[] args) {
		JomashopAutoBuy auto = new JomashopAutoBuy();
		AutoBuyStatus status = auto.login("tdbcyw@163.com", "tfb001001");
		System.out.println(status);
		/*auto.cleanCart();
	
		Map<String, String> param = new LinkedHashMap<>();
//		param.put("url", "http://zh.ashford.com/us/watches/all+brands/calvin+klein/fly/K9922120.pid?nid=cpg&skid=K9923120");
//		param.put("sku", "[[\"size\",\"7\"]]");
		param.put("url", "http://zh.ashford.com/us/watches/fossil/ES3077.pid");
		param.put("num", "1");
		param.put("productEntityId", "4026058");
		param.put("isPay", "false");
		param.put("count", "2");
		param.put("suffixNo", "123");
		param.put("my_price", "66.15");
		auto.selectProduct(param);
		auto.pay(param);*/
		
		
		RobotOrderDetail details = new RobotOrderDetail();
		details.setMallOrderNo("15B3282");
		details.setProductUrl("http://www.jomashop.com/swarovski-5102552.html");
		System.out.println(auto.scribeExpress(details));
		//auto.logout();
	}
}
 