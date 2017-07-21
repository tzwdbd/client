package com.oversea.task.service.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.utils.Utils;

public class NissenAutoBuy extends AutoBuy
{
	protected final Logger logger = Logger.getLogger(getClass());

	public NissenAutoBuy()
	{
		try
		{
			logger.debug("--->关闭WAP版本");
			TimeUnit.SECONDS.sleep(2);
			driver.close();
			logger.debug("--->重新打开PC版");
			driver = new FirefoxDriver();
			driver.manage().deleteAllCookies();
		}
		catch (Exception e)
		{
			logger.error("--->初始化失败", e);
			return;
		}
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord)
	{
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		driver.get("http://www.nissen.co.jp/");

		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			driver.switchTo().frame(driver.findElement(By.className("nlq_loginFrame")));
			WebElement head = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("nlq_myp_login")));
			By bySignIn = By.xpath("//a[contains(text(),'ログイン')]");
			WebElement signIn = head.findElement(bySignIn);
			logger.debug("--->跳转到登录页面");
			signIn.click();
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		WebElement main = null;

		try
		{
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#body #main .login .round")));
			WebElement username = main.findElement(By.name("Id"));
			username.sendKeys(userName);
			logger.debug("--->输入账号");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到输入框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement password = main.findElement(By.name("PASSWD"));
			password.sendKeys(passWord);
			logger.debug("--->输入密码");
		}
		catch (Exception e)
		{
			logger.error("--->没有找到密码框", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			WebElement btn = main.findElement(By.cssSelector("p.button .imgover"));
			logger.debug("--->开始登陆");
			btn.click();
		}
		catch (Exception e)
		{
			logger.error("--->没有找到登陆确定按钮", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}

		try
		{
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#body #main .login .round")));
			WebElement username = main.findElement(By.name("LNM"));
			logger.debug("--->输入firstName");
			username.sendKeys("ニッセン");
			TimeUnit.SECONDS.sleep(2);
			WebElement btn = main.findElement(By.cssSelector("p.button .imgover"));
			btn.click();
		}
		catch (Exception e)
		{
			logger.error("--->输入firstName出错", e);
		}

		try
		{
			driver.switchTo().frame(driver.findElement(By.className("nlq_loginFrame")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("nlq_myp_name")));
			logger.error("--->登陆成功");
		}
		catch (Exception e)
		{
			logger.error("--->登陆失败");
			return AutoBuyStatus.AUTO_LOGIN_EXP_UNKNOWN;
		}

		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart()
	{
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			// 坑爹呀,来回跳
			driver.switchTo().parentFrame().switchTo().frame("miniCart");
			TimeUnit.SECONDS.sleep(5);
			WebElement cart = driver.findElement(By.id("nlq_cart_btn"));
			cart.click();
			logger.debug("--->跳转到购物车");
		}
		catch (InterruptedException e)
		{
			logger.debug("--->跳转到购物车失败");
			return AutoBuyStatus.AUTO_CLICK_CART_FAIL;
		}

		try
		{
			TimeUnit.SECONDS.sleep(5);
			driver.switchTo().frame(driver.findElement(By.name("top")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#body #main .order_table")));
			List<WebElement> goodsInCart = driver.findElements(By.cssSelector("p.cancel a"));
			if (goodsInCart != null && goodsInCart.size() > 0)
			{
				logger.debug("--->购物车有 [" + goodsInCart.size() + "]件商品");
				for (int i = 0; i < goodsInCart.size(); i++)
				{
					try
					{
						TimeUnit.SECONDS.sleep(5);
						if (i > 0)
						{
							driver.switchTo().frame(driver.findElement(By.name("top")));
							wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#body #main .order_table")));
						}

						WebElement goods = driver.findElement(By.cssSelector("p.cancel a"));
						TimeUnit.SECONDS.sleep(5);
						goods.click();
						logger.debug("--->删除购物车第[" + (i + 1) + "]件商品");

						TimeUnit.SECONDS.sleep(5);
						Alert alert = driver.switchTo().alert();
						alert.accept();
					}
					catch (Exception e)
					{
						logger.error("--->删除购物车第[" + (i + 1) + "]件商品出错", e);
						return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
					}
				}
			}
			else
			{
				logger.debug("--->购物车有[0]件商品");
			}
		}
		catch (Exception e)
		{
			logger.debug("--->清空购物车失败");
			return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
		}

		return AutoBuyStatus.AUTO_CLICK_CART_SUCCESS;
	}

	WebElement whenMainPanelReady()
	{
		WebElement main = null;
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try
		{
			logger.debug("--->等待mainPanel加载完成");
			main = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
		}
		catch (Exception e)
		{
			logger.debug("--->等待主页面加载完成出错");
		}
		return main;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param)
	{
		logger.debug("--->跳转到商品页面");
		String productUrl = (String) param.get("url");
		logger.debug("--->跳转到商品页,url: " + productUrl);
		try
		{
			driver.navigate().to(productUrl);
			driver.executeScript("(function(){window.scrollBy(0,250);})();");
		}
		catch (Exception e)
		{
			logger.debug("--->打开商品页面失败 : " + productUrl);
			return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
		}

		WebElement main = whenMainPanelReady();

		String sku = param.get("sku");
		String num = param.get("num");
		if (sku != null)
		{
			Map<String, String> skuMap = new HashMap<String, String>();
			List<String> skuList = Utils.getSku(sku);
			for (int i = 0; i < skuList.size(); i += 2)
			{
				skuMap.put(skuList.get(i), skuList.get(i + 1));
			}

			try
			{
				WebElement modalBtn = main.findElement(By.id("open_modal"));
				TimeUnit.SECONDS.sleep(2);
				modalBtn.click();
				logger.debug("--->打开弹出框");
				TimeUnit.SECONDS.sleep(5);
				main = driver.findElement(By.cssSelector("#cartin_modal #cartin_modal_contents"));
				logger.debug("--->弹出框出现");
			}
			catch (Exception e)
			{
				return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
			}

			String curtain = skuMap.get("curtain");
			String length = skuMap.get("length");
			String color = skuMap.get("color");
			String size = skuMap.get("size");

			logger.debug("--->sku :" + skuMap);

			if (curtain != null)
			{
				try
				{
					boolean select = false;
					List<WebElement> curtains = main.findElements(By.cssSelector("#cartin_curtainlist li"));
					for (WebElement el : curtains)
					{
						String val = el.getAttribute("val").trim();
						String text = el.getText().trim();
						if (val.equals(curtain))
						{
							el.click();
							logger.debug("--->选择curtain:" + curtain + " =" + text);
							select = true;
							break;
						}
					}

					if (!select)
					{
						logger.error("--->没有找到对应的SKU,curtain=" + curtain);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}

					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception e)
				{
					logger.error("--->选择curtain出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}

			if (length != null)
			{
				try
				{
					boolean select = false;
					List<WebElement> lengths = main.findElements(By.cssSelector("#cartin_lengthlist li"));
					for (WebElement el : lengths)
					{
						String val = el.getAttribute("val").trim();
						String text = el.getText().trim();
						if (val.equals(text))
						{
							el.click();
							logger.debug("--->选择length:" + length);
							select = true;
							break;
						}
					}
					
					if (!select)
					{
						logger.error("--->没有找到对应的SKU,length=" + length);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
					
					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception e)
				{
					logger.error("--->选择length出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}

			if (color != null)
			{
				try
				{
					boolean select = false;
					List<WebElement> colors = main.findElements(By.cssSelector("#cartin_colorlist li"));
					for (WebElement el : colors)
					{
						String symbol = el.getAttribute("symbol").trim();
						String match = symbol + "=" + color;
						String text = el.getText().trim();
						if (match.equals(text))
						{
							el.click();
							logger.debug("--->选择color:" + color);
							select = true;
							break;
						}
					}
					
					if (!select)
					{
						logger.error("--->没有找到对应的SKU,color=" + color);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
					
					TimeUnit.SECONDS.sleep(5);
				}
				catch (InterruptedException e)
				{
					logger.error("--->选择color出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}
			}

			if (size != null)
			{
				try
				{
					boolean select = false;
					List<WebElement> sizes = main.findElements(By.cssSelector("#cartin_sizelist li"));
					for (WebElement el : sizes)
					{
						WebElement sizelabel = el.findElement(By.className("sizelabel"));
						String sizeStr = sizelabel.getText().trim();
						WebElement pricelabel = el.findElement(By.cssSelector(".listinfo .pricelabel"));
						String priceStr = pricelabel.getText().replace(",", "").replace("￥", "").replace("（＋税）", "");
						WebElement stocklabel = el.findElement(By.cssSelector(".listinfo .stocklabel"));
						String stockStr = stocklabel.getText().trim();

						if (curtain != null)
						{
							String val = el.getAttribute("val").trim();
							if (val.equals(size))
							{
								select = true;
								logger.debug("--->price:" + priceStr + " ,stock:" + stockStr + " ,size:" + val);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr);
								if (stockStr.contains("売り切れ") || stockStr.contains("入荷待ち"))
								{
									logger.debug("--->商品已经缺货!");
									return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
								}
								else
								{
									logger.debug("--->选择size:" + size);
									el.click();
									break;
								}
							}
						}
						else
						{
							if (sizeStr.equals(size))
							{
								select = true;
								logger.debug("--->price:" + priceStr + " ,stock:" + stockStr);
								data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_SINGLE_PRICE, priceStr);
								if (stockStr.contains("売り切れ") || stockStr.contains("入荷待ち"))
								{
									logger.debug("--->商品已经缺货!");
									return AutoBuyStatus.AUTO_SKU_IS_OFFLINE;
								}
								else
								{
									logger.debug("--->选择size:" + size);
									el.click();
									break;
								}
							}
						}
					}
					
					if (!select)
					{
						logger.error("--->没有找到对应的SKU,size=" + size);
						return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
					}
					
					TimeUnit.SECONDS.sleep(5);
				}
				catch (Exception e)
				{

					logger.error("--->选择size出错");
					return AutoBuyStatus.AUTO_SKU_SELECT_EXCEPTION;
				}

				if (!num.equals("1"))
				{
					try
					{
						TimeUnit.SECONDS.sleep(5);
						Integer numInt = Integer.valueOf(num);
						WebElement addBtn = main.findElement(By.id("cartin_amount_add"));
						for (int i = 0; i < numInt - 1; i++)
						{
							addBtn.click();
							TimeUnit.SECONDS.sleep(5);
							logger.debug("--->数量加1");
						}
					}
					catch (Exception e)
					{
						logger.error("--->选择数量失败");
						return AutoBuyStatus.AUTO_SKU_SELECT_NUM_FAIL;
					}
				}

				try
				{
					TimeUnit.SECONDS.sleep(5);
					WebElement addCart = main.findElement(By.id("carinbtn"));
					addCart.click();
					logger.debug("--->添加到购物车");
				}
				catch (Exception e)
				{
					logger.debug("--->添加购物车失败");
					return AutoBuyStatus.AUTO_SKU_CART_NOT_FIND;
				}
			}
		}

		return AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS;
	}

	@Override
	public AutoBuyStatus pay(Map<String, String> param)
	{
		return null;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail){
		String mallOrderNo = detail.getMallOrderNo();
		return null;
	}

	@Override
	public boolean gotoMainPage()
	{
		try
		{
			Utils.sleep(2000);
			driver.navigate().to("http://www.nissen.co.jp/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			driver.switchTo().frame(driver.findElement(By.className("nlq_loginFrame")));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("nlq_myp_name")));
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance)
	{
		return null;
	}

	public static void main(String[] args)
	{
		Map<String, String> param = new HashMap<String, String>();
		NissenAutoBuy autoBuy = new NissenAutoBuy();
		autoBuy.login("huao15@outlook.com", "tfb001001");
		autoBuy.cleanCart();

		param.put("url", "http://www.nissen.co.jp/sho_item/regular/8850/8850_12404.asp?book=8850&cat=keysch&bu=2&thum=keysch_cate018_002_000_000&search=2&sf=5&keysrch=word#itemArea");
		param.put("sku", "[[\"curtain\",\"GRP_4\"],[\"color\",\"白\"],[\"size\",\"252\"]]");
		param.put("num", "2");

		autoBuy.selectProduct(param);
	}
}
