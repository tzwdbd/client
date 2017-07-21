package com.oversea.task.service.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.StringUtils;
import com.oversea.task.utils.Utils;

public class DrugstoreAutoBuy extends AutoBuy
{
	private final Logger logger = Logger.getLogger(getClass());

	String host = "www.beauty.com";

	private Timer timer;

	public DrugstoreAutoBuy()
	{
		logger.debug("--->关闭WAP版本");
		try
		{
			TimeUnit.SECONDS.sleep(2);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		driver.close();
		logger.debug("--->重新打开PC版");
		driver = new FirefoxDriver();
		driver.manage().deleteAllCookies();
		
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				driver.executeScript("(function(){var els = document.getElementsByClassName('fsrDeclineButton');if(els && els[0]){els[0].click();}})();");
			}
		}, 3000, 3000);
		
		
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord)
	{
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();

		driver.get("http://www.drugstore.com/");

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			WebElement signIn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("signin")));
			logger.debug("--->跳转到登录页面");
			
//			signIn.click();
			signIn.sendKeys(Keys.ENTER);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			wait.until(ExpectedConditions.elementToBeClickable(By.id("txtEmail")));
			WebElement username = driver.findElement(By.id("txtEmail"));
			logger.debug("--->输入账号");
			username.sendKeys(userName);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement password = driver.findElement(By.id("txtPassword"));
			logger.debug("--->输入密码");
			password.sendKeys(passWord);
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement btn = driver.findElement(By.id("btnContinue"));
			logger.debug("--->开始登陆");
			TimeUnit.SECONDS.sleep(5);
			btn.click();
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("signoff")));
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	WebElement whenMainPanelReady()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			logger.debug("--->等待DrugStore主页面加载完成");
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("TblSiteRedesignBody")));
		}
		catch (Exception e)
		{
			logger.debug("--->等待主页面加载完成出错");
		}
		return main;
	}

	WebElement whenBeautyMainPanelReady()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			logger.debug("--->等待Beauty主页面加载完成");
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("twoColumnContainer")));
		}
		catch (Exception ex)
		{
			logger.debug("--->等待主页面加载完成出错");
		}
		return main;
	}

	@Override
	public AutoBuyStatus cleanCart()
	{
		try
		{
			WebElement cart = driver.findElement(By.id("shoppingBag"));
			cart.click();
		}
		catch (Exception e)
		{
			logger.error("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		logger.debug("--->清空购物车");
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			List<WebElement> goodsInCart = driver.findElements(By.className("bag-remove"));
			if (goodsInCart != null)
			{
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				for (int i = 0; i < goodsInCart.size(); i++)
				{
					driver.executeScript("(function(){window.scrollBy(100,300);})();");
					try
					{
						// 顺序不要换
						WebElement goods = driver.findElement(By.className("bag-remove"));
						TimeUnit.SECONDS.sleep(8);
						goods.click();
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");
						driver.executeScript("window.stop()");
					}
					catch (Exception e)
					{
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}

			}
		}
		catch (Exception e)
		{
			logger.error("--->清空购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		// 确保删除了
		try
		{
			driver.findElement(By.id("emptybag"));
			logger.error("--->购物车确实清空了");
			TimeUnit.SECONDS.sleep(8);
		}
		catch (Exception e)
		{
			logger.error("--->清空购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}
		
		WebElement con = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bag-continue")));
		con.click();

		return AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param)
	{
		logger.debug("--->跳转到商品页面");
		String url = param.get("url");
		logger.debug("--->选择商品 > url:" + url);
		try
		{
			driver.get(url);
		}
		catch (Exception e)
		{
			logger.debug("--->打开商品页面失败 :" + url);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		String num = param.get("num");
		String sku = param.get("sku");

		WebElement main = null;

		// 处理Beauty的
		if (url.contains(host))
		{
			main = whenBeautyMainPanelReady();
			driver.executeScript("(function(){window.scrollBy(100,300);})();");

			// 获取单价
			try
			{
				WebElement pricePanel = main.findElement(By.cssSelector("div.prodprice>p>span[itemprop=price]"));
				String priceStr = pricePanel.getText();
				if (StringUtil.isNotEmpty(priceStr))
				{
					logger.debug("--->单价:" + priceStr.trim().replace("$", ""));
					priceMap.put(param.get("productEntityId"), priceStr.trim().replace("$", ""));
				}
				else
				{
					logger.error("--->获取单价失败");
				}
			}
			catch (Exception e)
			{
				logger.error("--->获取单价失败");
			}

			try
			{
				logger.debug("--->选择数量:" + num);
				WebElement numInput = main.findElement(By.xpath("//input[@id='txtQuantity' and @value='1' and @maxlength='2']"));
				numInput.clear();
				TimeUnit.SECONDS.sleep(2);
				numInput.sendKeys(num);
				TimeUnit.SECONDS.sleep(5);
			}
			catch (Exception e)
			{
				logger.debug("--->选择数量出错");
				return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
			}

			try
			{
				logger.debug("--->添加到购物车");
				WebElement add2Cart = main.findElement(By.xpath("//input[@id='altAddToBag' and @type='image']"));
				TimeUnit.SECONDS.sleep(8);
//				add2Cart.click();
				add2Cart.sendKeys(Keys.ENTER);
			}
			catch (Exception e)
			{
				logger.debug("--->添加到购物车失败");
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
		}
		else
		{
			// 处理DR的
			main = whenMainPanelReady();
			driver.executeScript("(function(){window.scrollBy(100,300);})();");
			// 获取单价
			try
			{
				WebElement pricePanel = main.findElement(By.xpath("//span[@class='price' and @itemprop='price']"));
				String priceStr = pricePanel.getText();
				String unitStr = null;
				if (StringUtil.isNotEmpty(priceStr))
				{
					WebElement unit = null;
					try
					{
						unit = pricePanel.findElement(By.className("unitPrice"));
						unitStr = unit.getText();
					}
					catch (Exception e)
					{
					}

					if (StringUtil.isNotEmpty(unitStr))
					{
						priceStr = priceStr.replace(unitStr, "");
					}

//					data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr.trim().replace("$", ""));
					String productEntityId = param.get("productEntityId");
					logger.error("productEntityId = " + productEntityId);
					if(StringUtil.isNotEmpty(productEntityId)){
						priceMap.put(productEntityId, priceStr.trim().replace("$", ""));
					}
					logger.debug("--->单价:" + priceStr.trim().replace("$", ""));
				}
				else
				{
					logger.error("--->获取单价失败");
				}
			}
			catch (Exception e)
			{
				logger.error("--->获取单价失败");
			}

			if (StringUtil.isNotEmpty(num) && !num.equals("1"))
			{
				try
				{
					// 保险起见,多走一步
					logger.debug("--->选择数量:" + num);
					WebElement numPanel = driver.findElement(By.id("divQuantity"));
					WebElement numInput = numPanel.findElement(By.xpath("//input[@value='1' and @maxlength='2']"));
					numInput.clear();
					TimeUnit.SECONDS.sleep(2);
					numInput.sendKeys(num);
					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception e)
				{
					logger.debug("--->选择数量出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
				}
			}

			try
			{
				logger.debug("--->添加到购物车");
				WebElement addBtn = driver.findElement(By.id("divAddBtn"));
				TimeUnit.SECONDS.sleep(8);
				addBtn.click();
//				addBtn.sendKeys(Keys.ENTER);
			}
			catch (Exception e)
			{
				logger.debug("--->添加到购物车失败");
				return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
			}
		}

		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	AutoBuyStatus selectAddr(String count, WebElement main)
	{
		// 选地址
		try
		{
			TimeUnit.SECONDS.sleep(10);
			List<WebElement> addrs = main.findElements(By.cssSelector("#ShipAddr > table input[type='radio']"));
			if (addrs != null && addrs.size() > 0)
			{
				logger.debug("--->目前共有[" + addrs.size() + "]个可用地址");
				int index = 0;
				try
				{
					index = Integer.valueOf(count);
					int tarAddr = index % addrs.size();

					WebElement radio = addrs.get(tarAddr);
					if (!radio.isSelected())
					{
						logger.info("--->选择第[" + (tarAddr + 1) + "]个地址");
						radio.click();
					}
					else
					{
						logger.info("--->默认使用第[" + (tarAddr + 1) + "]个");
					}
				}
				catch (Exception ex)
				{
					logger.error("--->选择地址失败", ex);
					return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
				}
			}
		}
		catch (InterruptedException e)
		{
			logger.error("--->选择地址失败", e);
			return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_ADDR_SUCCESS;
	}

	AutoBuyStatus selectShipping(WebElement main)
	{
		try
		{
			boolean defaultIsSelected = true;
			TimeUnit.SECONDS.sleep(10);
			List<WebElement> shippings = main.findElements(By.cssSelector("#ShipOptions > table input[type='radio']"));
			if (shippings != null && shippings.size() > 0)
			{
				this.logger.debug("--->目前共有[" + shippings.size() + "]种物流可选");
				for (WebElement el : shippings)
				{
					String title = el.getAttribute("title");
					if (title.contains("Standard Shipping") && !el.isSelected())
					{
						el.click();
						TimeUnit.SECONDS.sleep(5);
						logger.error("--->手动选择　Standard Shipping");
						defaultIsSelected = false;
						break;
					}
				}

				if (defaultIsSelected)
				{
					logger.error("--->默认已经选中　Standard Shipping");
				}
			}
			else
			{
				return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
			}
		}
		catch (Exception ex)
		{
			return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_FAIL;
		}
		return AutoBuyStatus.AUTO_PAY_SELECT_DElLIVERY_OPTIONS_SUCCESS;
	}

	WebElement whenCheckoutPanelOk()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("PlaceOrderCntr")));
		}
		catch (Exception e)
		{
			logger.debug("--->等待checkout主页面加载完成出错");
		}
		return main;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param)
	{
		String count = param.get("count");
		String myPrice = param.get("my_price");
		if (Utils.isEmpty(myPrice))
		{
			logger.error("--->预算总价没有传值过来,无法比价");
			return AutoBuyStatus.AUTO_ORDER_PARAM_IS_NULL;
		}

		logger.error("--->myPrice = " + myPrice);
		logger.debug("--->默认地址:" + count);

		String url = param.get("url");
		try
		{
			WebElement checkout = null;
			if (url.contains(host))
			{
				checkout = driver.findElement(By.cssSelector("li.checkout>a"));
			}
			else
			{
				checkout = driver.findElement(By.id("checkoutBtn"));
			}
			checkout.click();
			logger.error("--->开始checkout");
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.error("--->checkout失败");
			return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
		}

		WebElement main = null;
		//处理Beauty
		if (url.contains(host))
		{
			main = whenCheckoutPanelOk();
			selectAddr(count, main);
			main = whenCheckoutPanelOk();
			selectShipping(main);
			main = whenCheckoutPanelOk();
		}
		else
		{

			main = whenMainPanelReady();
			selectAddr(count, main);
			main = whenMainPanelReady();
			selectShipping(main);
			main = whenMainPanelReady();
		}

		try
		{
			String total = "";
			try
			{
				TimeUnit.SECONDS.sleep(10);
				WebElement taotalPanel = main.findElement(By.id("GrandTotal"));
				total = taotalPanel.getText().trim().replace("$", "");
			}
			catch (Exception e)
			{
			}

			if (StringUtil.isNotEmpty(total))
			{
				data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_TOTAL_PRICE, total);
				logger.debug("--->完成付款,开始比价[" + myPrice + "," + total + "]");

				BigDecimal x = new BigDecimal(myPrice);
				BigDecimal y = new BigDecimal(total.trim());
				BigDecimal v = x.subtract(y);
				double m = Math.abs(v.doubleValue());
				if (m > 20.00D)
				{
					logger.error("--->总价差距超过约定,不能下单");
					return AutoBuyStatus.AUTO_PAY_TOTAL_GAP_OVER_APPOINT;
				}

				Boolean isPay = Boolean.valueOf((String) param.get("isPay"));
				if (isPay)
				{
					try
					{
						WebElement placeYourOrder = main.findElement(By.id("tbl_place_your_order"));
						WebElement orderSubmit = placeYourOrder.findElement(By.xpath("//input[contains(@alt,'Place Your Order')]"));

						logger.error("===>!!!我要付款了!!!<===");
						TimeUnit.SECONDS.sleep(3);
						orderSubmit.click();

						logger.error("=================");
						logger.error("|---> 点击付款  <---|");
						logger.error("=================");

						try
						{
							if (url.contains(host))
							{
								logger.debug("--->等待Beauty订单页主面板加载完成");
								TimeUnit.SECONDS.sleep(8);
								main = driver.findElement(By.id("pagebody"));
							}
							else
							{
								main = whenMainPanelReady();
							}

							WebElement orderNo = main.findElement(By.cssSelector("#OrderDetails a.orderlink"));
							String orderNoStr = orderNo.getText().trim().replace("#", "");
							logger.error("--->mall_order_no:" + orderNoStr);
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_ORDER_NO, orderNoStr);
							savePng();
						}
						catch (Exception e)
						{
							logger.error("--->获取单号出错!");
							return AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL;
						}

					}
					catch (Exception e)
					{
						logger.error("--->付款出错!", e);
						return AutoBuyStatus.AUTO_PAY_FAIL;
					}
				}
				else
				{
					return AutoBuyStatus.AUTO_PAY_SERVER_SIDE_DISALLOW;
				}
			}
			else
			{
				logger.error("--->[1]获取不到总价");
				return AutoBuyStatus.AUTO_PAY_GET_TOTAL_PRICE_FAIL;
			}
		}
		catch (Exception e)
		{
			return AutoBuyStatus.AUTO_PAY_FAIL;
		}

		return AutoBuyStatus.AUTO_PAY_SUCCESS;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		String mallOrderNo = detail.getMallOrderNo();
		logger.debug("--->爬取物流开始:" + mallOrderNo);
		try
		{
			WebElement yourAccount = driver.findElement(By.id("yourAccount"));
			logger.debug("--->找到 your account");
			yourAccount.click();
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.debug("--->找不到 your account");
		}

		whenMainPanelReady();

		try
		{
			WebElement yourOrder = driver.findElement(By.cssSelector("#Orders>h4>a"));
			logger.debug("--->找到 your order");
			yourOrder.click();
			TimeUnit.SECONDS.sleep(5);
		}
		catch (Exception e)
		{
			logger.debug("--->找不到 your order");
		}

		whenMainPanelReady();

		try
		{
			logger.debug("--->开始查找对应订单");
			WebElement ordersTable = driver.findElement(By.id("TblOrderHistory"));
			logger.debug("--->找到对应的table");
			List<WebElement> orderList = ordersTable.findElements(By.tagName("tr"));
			//　遍历每一个tr
			for (int x = 1; x < orderList.size(); x++)
			{
				WebElement tr = orderList.get(x);
				List<WebElement> tds = tr.findElements(By.tagName("td"));

				// 第一个td是单号
				WebElement orderNo = tds.get(0);
				boolean flag = orderNo.getText().trim().contains(mallOrderNo.trim());
				logger.debug("--->orderNo text:" + orderNo.getText() + "对比结果:" + flag);
				if (flag)
				{
					// 第二个td是状态
					WebElement status = tds.get(1);
					String str = status.getText();
					if (str.startsWith("Complete"))
					{
						TimeUnit.SECONDS.sleep(5);
						orderNo.findElement(By.tagName("a")).click();
						logger.debug("--->查看订单详情");
						WebElement main = whenMainPanelReady();
						try
						{
							Pattern p = Pattern.compile("(\\d+\\/\\d+\\/\\d+)\\s*\\-\\s*([a-zA-z0-9 ]+)\\s*\\-\\s*([0-9A-Z]+)");
							List<WebElement> shippings = main.findElements(By.xpath("//div[@class='shipping']/p"));
							// 保险起见,遍历一下,其实可以直接取最后一个
							for (WebElement shipping : shippings)
							{
								String shippingStr = shipping.getText();
								if (shippingStr.contains("Tracking"))
								{
									logger.error("--->物流信息:" + shippingStr);
									shippingStr = shippingStr.replaceAll("\r", " ").replace("\n", " ");
									Matcher m = p.matcher(shippingStr);
									if (m.find())
									{
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, m.group(2).trim());
										data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, m.group(3).trim());
										logger.error("expressCompany = " + m.group(2).trim());
										logger.error("expressNo = " + m.group(3).trim());
										return AutoBuyStatus.AUTO_SCRIBE_SUCCESS;
									}
									else
									{
										logger.debug("--->获取物流单号失败");
										return AutoBuyStatus.AUTO_SCRIBE_FAIL;
									}
								}
							}
						}
						catch (Exception e)
						{
							logger.debug("--->获取物流单号出现异常");
							return AutoBuyStatus.AUTO_SCRIBE_FAIL;
						}
					}
					else if (str.startsWith("Canceled"))
					{
						logger.error("--->被砍单");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_CANCELED;
					}
					else if (str.startsWith("Processing"))
					{
						logger.error("--->还未发货");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_READY;
					}
					else if (str.startsWith("Problem"))
					{
						// 这里是一个折中的处理
						logger.error("--->订单出现问题,需要联系客服");
						return AutoBuyStatus.AUTO_SCRIBE_ORDER_NOT_FIND;
					}
					break;
				}

			}
		}
		catch (Exception e)
		{
			logger.error("--->出错啦:", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}

		return AutoBuyStatus.AUTO_SCRIBE_FAIL;
	}

	@Override
	public boolean gotoMainPage()
	{
		try
		{
			Utils.sleep(2000);
			driver.navigate().to("https://www.drugstore.com");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("signoff")));
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean logout(boolean isScreenShot)
	{
		super.logout(isScreenShot);
		timer.cancel();
		return true;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance)
	{
		return null;
	}

	public static void main(String[] args)
	{
		DrugstoreAutoBuy autoBuy = new DrugstoreAutoBuy();
		AutoBuyStatus status = autoBuy.login("cuiyl9527@sina.com", "tfb001001");
		if (status.equals(AutoBuyStatus.AUTO_LOGIN_SUCCESS))
		{
		}
		System.out.println(status);
	}
}
