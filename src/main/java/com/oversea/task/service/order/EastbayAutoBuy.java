package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.utils.Utils;

public class EastbayAutoBuy extends AutoBuy {
	protected final Logger logger = Logger.getLogger(getClass());

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.get("http://eastbay.com/");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.id("header_account_link")));
			driver.executeScript("document.getElementById('header_account_link').click();");
			logger.debug("--->跳转到登录页面");
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			 wait.until(ExpectedConditions
					.visibilityOfElementLocated(By
							.id("my_account_login_toggle")));
			driver.executeScript("document.getElementById('my_account_login_toggle').click();");
			logger.debug("--->打开登录框");
		} catch (Exception e) {
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			WebElement username = wait.until(ExpectedConditions
					.elementToBeClickable(By.id("login_email")));
			username.sendKeys(userName);
			logger.debug("--->输入账号");
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			WebElement password = driver.findElement(By.id("login_password"));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
			TimeUnit.SECONDS.sleep(1);
		} catch (Exception e) {
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			WebElement btn = driver.findElement(By.id("login_submit"));
			TimeUnit.SECONDS.sleep(1);
			btn.click();
			logger.debug("--->开始登陆");
		} catch (Exception e) {
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.id("nav_logout_link")));
		} catch (Exception e) {
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		try {
			driver.navigate().to("http://m.eastbay.com/?uri=cart");
		} catch (Exception e) {
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		logger.debug("--->清空购物车");
		try {
			
			try {
				WebElement continueButton = driver.findElement(By.xpath("//a[@title='Continue Button']"));
				continueButton.click();
			} catch (Exception e) {
			}
			TimeUnit.SECONDS.sleep(5);
			List<WebElement> goodsInCart = driver
					.findElements(By
							.xpath("//a[@class='button' and @data-btnname='cart_remove']"));
			if (goodsInCart != null) {
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				for (int i = 0; i < goodsInCart.size(); i++) {
					driver.executeScript("(function(){window.scrollBy(100,300);})();");
					try {
						WebElement goods = driver
								.findElement(By
										.xpath("//a[@class='button' and @data-btnname='cart_remove']"));
						goods.click();
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						TimeUnit.SECONDS.sleep(8);
					} catch (Exception e) {
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}

				goodsInCart = driver
						.findElements(By
								.xpath("//a[@class='button' and @data-btnname='cart_remove']"));
				if (goodsInCart != null && goodsInCart.size() == 0) {
					logger.error("--->购物车确实清空了");
				} else {
					return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
				}
			}
		} catch (Exception e) {
			logger.error("--->清空购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	WebElement waitForMainPanel() {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		WebElement panel = wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.id("pdp_container")));
		return panel;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		logger.debug("--->跳转到商品页面");
		String url = param.get("url");
		logger.debug("--->url:" + url);
		try {
			driver.navigate().to(url);
			TimeUnit.SECONDS.sleep(10);
			close();

			String sku = param.get("sku");
			if (sku != null) {
				Map<String, String> map = new LinkedHashMap<>();
				List<String> skuList = Utils.getSku(sku);
				for (int i = 0; i < skuList.size(); i += 2) {
					map.put(skuList.get(i), skuList.get(i + 1));
				}

				//driver.executeScript("document.getElementById('full_wrapper').scrollBy(100,500)");
				String style = map.get("style");
				
				if (style != null) {
					
					driver.executeScript("document.getElementById('show_all').click();");
					TimeUnit.SECONDS.sleep(2);
					
					List<WebElement> alternateColors = driver.findElements(By.xpath("//div[@id='alternate_images']/div/ul/li/a"));
					
					//List<WebElement> alternateColors = driver.findElements(By.className("container"));
					Iterator<WebElement> iterator = alternateColors.iterator();
					int i = 0;
					int tar = 0;
					while(iterator.hasNext()){
						i++;
						WebElement color = iterator.next();
						//WebElement color = cl.findElement(By.xpath(".//a"));
						String cst = color.getAttribute("title").replace("Style Color ", "");
						if(style.contains("#")){
							style = style.substring(0,style.lastIndexOf("#")-1).replace("# ", "");
						}
						if(!cst.contains(style)){
							iterator.remove();
						}else{
							tar = i-1;
						}
					}
					
					if(alternateColors.size()==1){
						logger.debug("找到正确sku");
						TimeUnit.SECONDS.sleep(2);
						driver.executeScript("document.querySelectorAll('div#alternate_images div.slide_content ul li a')["+tar+"].click()");
						TimeUnit.SECONDS.sleep(2);
					}else{
						logger.debug("选择颜色失败");
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
					
					/*
					logger.debug("--->选择style:" + style);
					String[] styleArr = style.split("#");
					String skuId = styleArr[styleArr.length - 1].trim();
					try {
						WebElement color = colorPanel
								.findElement(By
										.xpath("//div[@id='alternate_colors']/div[@class='container']/a[@data-sku='"
												+ skuId + "']"));
						TimeUnit.SECONDS.sleep(1);
						driver.executeScript(
								"var tar=arguments[0];tar.click();if(jQuery){var left=$(tar).offset().left;$('#alternate_colors_wrapper').scrollLeft(left);}",
								color);
						TimeUnit.SECONDS.sleep(1);
						logger.error("--->核对style信息:");
						try {
							WebElement pi = driver.findElement(By
									.id("product_style_info"));
							String txt = pi.getText().replace("\r\n", " + ")
									.replace("\n", " + ").replace("\r", " + ");
							logger.error("所选则的sku信息:" + txt);
							for (int i = 0; i < styleArr.length; i++) {
								String str = styleArr[i].replace("Width -", "")
										.trim();
								if (txt.contains(str)) {
									logger.error("[" + str + "] 匹配成功");
								} else {
									logger.error("[" + str + "] 匹配失败");
									return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
								}
							}
						} catch (Exception e) {
							return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
						}
					} catch (Exception e) {
						logger.debug("--->选择style遇到异常,找不到对应的颜色:" + skuId);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
				*/}

				String size = map.get("size");
				if (size != null) {
					try {
						size = size.trim();
						logger.debug("--->选择size:" + size);
						WebElement sizePanel = driver
								.findElement(By.id("size"));
						WebElement tarOpt = sizePanel.findElement(By
								.xpath("./option[@value='" + size + "']"));
						String text = tarOpt.getText();
						if (text.contains("⊗")) {
							logger.debug("--->所选择的size暂时缺货");
							return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
						} else {
							logger.debug("                         size:"+size);
							Select select = new Select(sizePanel);
							select.selectByValue(size);
							TimeUnit.SECONDS.sleep(1);
							logger.error("--->核对size信息:");
							WebElement mask = driver.findElement(By
									.id("pdp_size_select_mask"));
							if (mask.getText().contains(size)) {
								logger.error("--->选择size:[" + size + "] 匹配成功");
							} else {
								logger.error("--->选择size:[" + size + "] 匹配失败");
								return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
							}
						}
					} catch (Exception e) {
						logger.debug("--->选择size遇到异常,找不到对应的size:" + size,e);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
				}
			}

			String number = param.get("num");
			if (!"1".equals(number)) {
				try {
					logger.debug("--->选择数量:" + number);
					WebElement quantity = driver.findElement(By.id("quantity"));
					// 确保有对应数量的option,避免限购的时候出错
					quantity.findElement(By.xpath("./option[@value="
							+ number.trim() + "]"));
					Select select = new Select(quantity);
					select.selectByValue(number);

					TimeUnit.SECONDS.sleep(1);
					logger.error("--->核对number信息:");
					WebElement mask = driver.findElement(By
							.id("pdp_quantity_select_mask"));
					if (mask.getText().contains(number)) {
						logger.error("--->选择number:[" + number + "] 匹配成功");
					} else {
						logger.error("--->选择number:[" + number + "] 匹配失败");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				} catch (Exception e) {
					logger.debug("--->选择number遇到异常:" + number);
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}

			
			String selectedSku = driver.findElement(By.xpath("//div[@id='product_color']/span[@class='attType_color']")).getText();
			logger.debug("selectedSku:"+selectedSku);
			
			try {
				TimeUnit.SECONDS.sleep(1);
				WebElement addToCart = driver.findElement(By
						.id("pdp_addtocart_button"));
				TimeUnit.SECONDS.sleep(1);
				addToCart.click();
				logger.debug("--->添加到购物车");
				TimeUnit.SECONDS.sleep(1);
				try {
					WebElement error = driver.findElement(By
							.id("pdp_add_to_cart_errors"));
					if (error != null) {
						String errorTxt = error.getText();
						if (errorTxt.contains("not available")) {
							logger.debug("--->所选择的size暂时缺货");
							return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
						}
					}
				} catch (Exception e) {

				}
				TimeUnit.SECONDS.sleep(1);

				try {
					WebElement price = driver
							.findElement(By
									.cssSelector(".minicart_product_details  .minicart_sale_price"));
					String priceStr = price.getText();
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE,
							priceStr.replace("$", ""));
					logger.error("--->获取单价:" + priceStr);
				} catch (Exception e) {
					logger.debug("--->获取单价失败");
				}
				
				
				//pdp_addtocart_button
				//driver.executeScript("document.getElementById('pdp_addtocart_button').click();");
				TimeUnit.SECONDS.sleep(5);
				driver.findElement(By.id("pdp_addtocart_button")).click();
				TimeUnit.SECONDS.sleep(2);
				
				//driver.executeScript("document.getElementById('minicart_fullcart_button').click();");
				TimeUnit.SECONDS.sleep(2);
				try {
					driver.findElement(By.id("launch_halt_overlay"));
					logger.debug("不能加购物车："+driver.findElement(By.id("launch_halt_overlay")).getText());
				} catch (Exception e) {
				}
				
				
				driver.get("http://m.eastbay.com/?uri=cart");
				List<WebElement> products =  driver.findElements(By.xpath("//form[@id='shoppingCartForm']/div/ul/li"));
				Iterator<WebElement> iterator = products.iterator();
				while (iterator.hasNext()) {
					WebElement product = iterator.next();
					if(!product.getText().toLowerCase().contains(selectedSku.toLowerCase()))
						iterator.remove();
				}
				if(products.size()!=1){
					logger.debug("在购物车中没有找到这个商品，无法调整数量");
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}else{
					logger.debug("开始调整数量");
					close();
					WebElement numEle = products.get(0).findElement(By.xpath(".//input[@name='quantity']"));
					if(!numEle.getAttribute("value").equals(number)){
						int tar = Integer.parseInt(number) - Integer.parseInt(numEle.getAttribute("value"));
						for (int i = 0; i < Math.abs(tar); i++) {
							products =  driver.findElements(By.xpath("//form[@id='shoppingCartForm']/div/ul/li"));
							Iterator<WebElement> iterator1 = products.iterator();
							while (iterator1.hasNext()) {
								WebElement product = iterator1.next();
								if(!product.getText().toLowerCase().contains(selectedSku.toLowerCase()))
									iterator1.remove();
							}
							WebElement editNum = null ;
							if(tar<0){
								editNum =  products.get(0).findElement(By.xpath(".//span[@class='subtract_quantity']"));
								logger.debug("需要减："+tar);
							}else if(tar>0){
								editNum = products.get(0).findElement(By.xpath(".//span[@class='add_quantity']"));
								logger.debug("需要加："+tar);
							}
							tar = Math.abs(tar);
							editNum.click();
							TimeUnit.SECONDS.sleep(10);
						}
					}
					products =  driver.findElements(By.xpath("//form[@id='shoppingCartForm']/div/ul/li"));
					Iterator<WebElement> iterator1 = products.iterator();
					while (iterator1.hasNext()) {
						WebElement product = iterator1.next();
						if(!product.getText().toLowerCase().contains(selectedSku.toLowerCase()))
							iterator1.remove();
					}
					numEle = products.get(0).findElement(By.xpath(".//input[@name='quantity']"));
					if(products.size()!=1){
						logger.debug("在购物车中没有找到这个商品，无法调整数量");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
					logger.debug(numEle.getAttribute("value"));
					if(!numEle.getAttribute("value").equals(number)){
						logger.debug("数量还是不对!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}
				selectedSku = "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq";
			} catch (Exception e) {
				logger.debug("--->添加到购物车失败",e);
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
			close();
		} catch (Exception e) {
			logger.debug("--->选择SKU遇到异常",e);
			return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
		}
		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param,
			UserTradeAddress address, OrderPayAccount payAccount) {
		close();
		Boolean isPay = Boolean.valueOf(param.get("isPay"));
		String suffixNo = param.get("suffixNo");
		
		try {
			String tarAddr = param.get("count");
			String myPrice = param.get("my_price");

			if (Utils.isEmpty(myPrice)) {
				logger.error("--->预算总价没有传值过来,无法比价");
				return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
			}

			// 设置价格
			int num = 1;
			try {
				num = Integer.parseInt(param.get("num"));
			} catch (Exception e) {
				num = 1;
			}
			myPrice = String.valueOf(Float.parseFloat(myPrice) * num);
			logger.error("--->myPrice = " + myPrice);


			try {
				//driver.navigate().to("http://m.eastbay.com/?uri=cart");
				
				driver.findElement(By.id("cart_checkout_button")).click();
				
				try {
					TimeUnit.SECONDS.sleep(8);
					//billPaneContinue
					logger.debug("email");
					driver.findElement(By.id("billEmailAddress")).clear();
					TimeUnit.SECONDS.sleep(2);
					driver.findElement(By.id("billEmailAddress")).sendKeys("giftcard@qichuang.com");;
					TimeUnit.SECONDS.sleep(2);
					//driver.executeScript("document.getElementById('billEmailAddress').value='giftcard@qichuang.com'");
					TimeUnit.SECONDS.sleep(20);
					//driver.findElement(By.id("billPaneContinue")).click();
					driver.executeScript("document.getElementById('billPaneContinue').click();");
				} catch (Exception e) {
				}
				
				WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
				WebElement billAddrViewPaneData = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billAddrViewPaneData")));
				if(!billAddrViewPaneData.getText().contains("310000")){
					logger.debug("账单地址不对！");
					return AutoBuyStatus.AUTO_PAY_FAIL;
				}
				
				logger.debug("开始选择收货地址");
				TimeUnit.SECONDS.sleep(1);
				//shipMethodPaneEdit
				try {
					driver.executeScript("document.getElementById('shipPaneEdit').click();");
				} catch (Exception e) {
				}
				TimeUnit.SECONDS.sleep(1);
				
				WebElement shipOtherAddress = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("shipOtherAddress")));
				Select select = new Select(shipOtherAddress);
				List<WebElement> options =  select.getOptions();
				Iterator<WebElement> iterator = options.iterator();
				while (iterator.hasNext()) {
					WebElement option = iterator.next();
					String text = option.getText().toLowerCase();
					if(text.contains("address")||text.contains("xi dou men")||text.contains("fu di")){
						logger.debug("删除错误选项："+text);
						iterator.remove();
					}
				}
				logger.debug("可供选择的地址数量："+options.size());
				int tar = Integer.parseInt(tarAddr)%options.size();
				String text = options.get(tar).getText();
				logger.debug("选择第"+tar+"个地址:"+text);
				select.selectByVisibleText(text);
				
				driver.executeScript("document.getElementById('shipPaneContinue').click();");
				TimeUnit.SECONDS.sleep(2);
				try {
					driver.executeScript("document.getElementById('address_verification_use_original_button').click()");;
				} catch (Exception e) {
				}
				
				logger.debug("选择第一个物流方式");
				TimeUnit.SECONDS.sleep(2);
				driver.executeScript("document.getElementById('shipMethod0').click();");
				
				TimeUnit.SECONDS.sleep(2);
				driver.executeScript("document.getElementById('shipMethodPaneContinue').click();");
				
				TimeUnit.SECONDS.sleep(2);
				
				//使用优惠码0 失效,1互斥 ,9没修改过,10有效
				Set<String> promotionList = getPromotionList(param.get("promotion"));
				if(promotionList != null && promotionList.size() > 0){
					logger.debug("promotionList.toString()"+promotionList.toString());
					logger.debug("开始输入优惠码");
					boolean isEffective = false;
					HashMap<String, Integer> statusMap = new HashMap<String, Integer>();
					for(String code : promotionList){
						try {
							WebElement codeEl = driver.findElement(By.id("CPCOrSourceCode"));
							TimeUnit.SECONDS.sleep(3);
							codeEl.clear();
							TimeUnit.SECONDS.sleep(3);
							codeEl.sendKeys(code);
							TimeUnit.SECONDS.sleep(3);
							driver.executeScript("document.getElementById('applyPromo').click();");
							WebElement oc = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("CPCOrSourceCode_error")));
							String info = oc.getText().toLowerCase();
							if(info.contains("applied")){
								logger.debug("优惠码有效："+code);
								statusMap.put(code, 10);
								isEffective = true;
								break;
							}else{
								logger.debug("优惠码无效："+code);
								statusMap.put(code, 0);
							}
						} catch (Exception e) {
							logger.debug("输优惠码异常",e);
						}
					}
					setPromotionCodelistStatus(statusMap);
					if("true".equals(param.get("isStock")) && !isEffective){
						logger.debug("--->优惠码失效,中断采购");
						return AutoBuyStatus.AUTO_PAY_FAIL;
					}
				}
				TimeUnit.SECONDS.sleep(2);
				driver.executeScript("document.getElementById('promoCodePaneContinue').click();");

				logger.debug("输入安全码："+suffixNo);
				TimeUnit.SECONDS.sleep(2);
				logger.debug("suffixNo:"+suffixNo);
				driver.findElement(By.id("CardCCV")).sendKeys(suffixNo);
				
				TimeUnit.SECONDS.sleep(10);
				driver.findElement(By.id("payMethodPaneContinue")).click();
				TimeUnit.SECONDS.sleep(3);
				
				logger.debug("查询总价");
				String total =  driver.findElement(By.id("summaryPaneGrandTotal")).getText();
				System.out.println("total:"+total);
				if(!Utils.isEmpty(total)){
					String priceStr = total.replaceAll("[^0-9.]", "");
					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, priceStr);
					logger.debug("--->找到商品结算总价 = "+priceStr);
					BigDecimal x = new BigDecimal(myPrice);
					BigDecimal y = new BigDecimal(priceStr);
					BigDecimal v = y.subtract(x);
					if (v.doubleValue() > 20.00D){
						logger.error("--->总价差距超过约定,不能下单");
						return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
					}
				}
			} catch (Exception e) {
				logger.error("--->付款失败",e);
				return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
			}
		} catch (Exception e) {
			logger.error("--->付款遇到异常!");
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}
		
		logger.debug("--->开始点击付款 placeOrder:isPay"+isPay);
		try {
			if (isPay) {
				driver.executeScript("document.getElementById('orderSubmit').click()");
				logger.debug("付款成功");
			}
		} catch (Exception e) {

		}
		logger.debug("开始获取商城订单号");
		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	void close(){
		try {
			driver.findElement(By.xpath("//button[@class='overlay_header_close_button']")).click();
		} catch (Exception e) {
		}
	}
	
	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) {
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY;
		}
		driver.get("https://m.eastbay.com/?uri=account/accountInformation");
		try {
			WebElement element = driver.findElement(By
					.id("orderHistoryContent"));
			List<WebElement> orderHistory = element.findElements(By
					.xpath(".//ul/li//div"));
			Boolean isFind = false;
			for (WebElement webElement : orderHistory) {
				if (webElement.getText().contains(mallOrderNo)) {
					isFind = true;
					if (webElement.getText().toLowerCase().contains("process")) {
						logger.debug("商城未发货，未产生物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					} else if (webElement.getText().toLowerCase()
							.contains("shipped")) {
						String url = webElement.findElement(By.xpath(".//a"))
								.getAttribute("href");
						logger.debug("url:" + url);
						driver.navigate().to(url);
						String orderMethod = driver.findElementById(
								"order_detail_order_method").getText();
						if (orderMethod.contains("USPS")) {
							Matcher m = Pattern.compile("[0-9]{20,100}")
									.matcher(orderMethod);
							if (m.find()) {
								String trackNO = m.group();
								data.put(
										AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY,
										"USPS");
								data.put(
										AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO,
										trackNO);
								logger.debug("USPS:找到物流单号：" + trackNO);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} else {
								logger.debug("找不到物流单号：" + orderMethod);
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}
						} else if (orderMethod.contains("UPS")) {
							Matcher m = Pattern.compile("1Z[0-9]{15,100}")
									.matcher(orderMethod);
							if (m.find()) {
								String trackNO = m.group();
								data.put(
										AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY,
										"UPS");
								data.put(
										AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO,
										trackNO);
								logger.debug("UPS:找到物流单号：" + trackNO);
								return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
							} else {
								logger.debug("找不到物流单号：" + orderMethod);
								return AutoBuyStatus.AUTO_SCRIBE_FAIL;
							}
						} else {
							logger.debug("无法识别的物流订：" + orderMethod);
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
					} else {
						logger.debug("物流状态不明：" + webElement.getText());
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}
				}
			}
			if (isFind == false) {
				logger.debug("未找到订单");
				return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
			}
		} catch (Exception e) {
			logger.debug("爬物流异常：" + e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
	}

	@Override
	public boolean gotoMainPage() {
		try {
			TimeUnit.SECONDS.sleep(1);
			driver.get("http://eastbay.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By
					.id("nav_logout_link")));
			return true;
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		return null;
	}

	public static void main(String[] args) {
		Map<String, String> param = new HashMap<String, String>();
		
		param.put("count","2");
		param.put("my_price","122");
		param.put("url", "http://www.eastbay.com/product/model:256028/sku:691616/asics-tiger-gel-lyte-v-womens/grey/light-green/");
		param.put("sku", "[[\"size\",\"05.5\"],[\"style\",\"Whisper Pink/Whisper Pink # Width - B - Medium # 692121\"]]");
		
		String text = "$1@23.9";
		//String priceStr = text.replaceAll("[^0-9.]", "");
		//System.out.println(priceStr);
		EastbayAutoBuy autoBuy = new EastbayAutoBuy();
		autoBuy.selectProduct(param);
	/*	EastbayAutoBuy autoBuy = new EastbayAutoBuy();
		AutoBuyStatus status = autoBuy.login("cuiyl@qichuang.com", "tfb001001");*/
		/*if (status.equals(AutoBuyStatus.AUTO_LOGIN_SUCCESS)) {
			status = autoBuy.cleanCart();
			if (status.equals(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS)) {
				Map<String, String> param = new LinkedHashMap<>();
				param.put(
						"url",
						"https://www.linkhaitao.com/index.php?mod=lhdeal&track=09eaA6idPNIHnyNw0aLEezyHChqcV_a2bNnd7yJHkeXgzzc1FX7ZVqXuB1yDDr4VE&new=http%3A%2F%2Fwww.eastbay.com%2Fproduct%2Fmodel%3A252217%2F&tag=");
				param.put(
						"sku",
						"[[\"size\",\"08.5\"],[\"style\",\"Black/White # Width - D - Medium # Premium - Composition Pack # 39731001\"]]");
				param.put("num", "2");
				status = autoBuy.selectProduct(param);
				if (status.equals(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS)) {
					param.put("count", "2");
					param.put("num", "2");
					param.put("my_price", "49.99");
					autoBuy.pay(param);
				}
			}
		}*/
		/*autoBuy.pay(param);
		System.out.println(status);*/
	}
}
