package com.oversea.task.service.order;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.oversea.task.AutoBuyConst;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.ExpressUtils;
import com.oversea.task.utils.Utils;

public class EscentualAutoBuy extends AutoBuy {
	
	private final Logger logger = Logger.getLogger(getClass());
	
	public static void main(String[] args) {
		EscentualAutoBuy autoBuy = new EscentualAutoBuy();
		AutoBuyStatus status = autoBuy.login("taccws@163.com", "tfb001001");
		System.out.println(status);
		RobotOrderDetail detail = new RobotOrderDetail();
		detail.setMallOrderNo("701409658");
		autoBuy.scribeExpress(detail);
	}

	@Override
	public AutoBuyStatus login(String userName, String passWord) {
		logger.debug("--->调整浏览器尺寸和位置");
		driver.manage().window().maximize();
		
		driver.get("https://www.escentual.com/customer/account/login");
		
		WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
		try {
			WebElement close = driver.findElement(By.cssSelector(".close"));
			if (close != null) {
				close.click();
			}
		} catch (Exception e) {
		}
		try{
			//输入账号
			WebElement account = driver.findElement(By.id("email"));
			logger.debug("--->开始输入账号");
			Utils.sleep(1500);
			account.sendKeys(userName);
			
			//输入密码
			WebElement password = driver.findElement(By.id("pass"));
			logger.debug("--->开始输入密码");
			Utils.sleep(1500);
			password.sendKeys(passWord);
			
			driver.executeScript("(function(){window.scrollBy(0,200);})();");
			Utils.sleep(1500);
			//提交
			WebElement submit = driver.findElement(By.id("send2"));
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
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ec-ca__welcome")));
			logger.debug("--->登录完成");
		}catch (Exception e){
			logger.error("--->登录碰到异常", e);
			return AutoBuyStatus.AUTO_CLIENT_NETWORK_TIMEOUT;
		}
		return AutoBuyStatus.AUTO_LOGIN_SUCCESS;
	}

	@Override
	public AutoBuyStatus cleanCart() {
		// TODO Auto-generated method stub
		return AutoBuyStatus.AUTO_CLEAN_CART_FAIL;
	}

	@Override
	public AutoBuyStatus selectProduct(Map<String, String> param) {
		// TODO Auto-generated method stub
		return AutoBuyStatus.AUTO_SKU_OPEN_FAIL;
	}

	@Override
	public AutoBuyStatus scribeExpress(RobotOrderDetail detail) {
		String mallOrderNo = detail.getMallOrderNo();
		if (Utils.isEmpty(mallOrderNo)) { 
			return AutoBuyStatus.AUTO_SCRIBE_MALL_ORDER_EMPTY; 
		}
		//寻找my account
		try{
			logger.debug("--->开始跳转到my account页面");
			driver.navigate().to("https://www.escentual.com/sales/order/history/");
		}catch (Exception e){
			logger.error("--->跳转到my account页面出现异常", e);
			return AutoBuyStatus.AUTO_SCRIBE_FAIL;
		}
		
		//等待my account页面加载完成
		List<WebElement> orders = driver.findElements(By.xpath("//div[@class='ec-card ec-ca-order-history__order']"));
		for (WebElement order:orders) {
			String orderText = order.getText() ;
			if(!StringUtil.isBlank(orderText)&&orderText.contains(mallOrderNo)){
				try{
					WebElement a = order.findElement(By.xpath(".//a[@class='ec-card__action ec-card__action--secondary ec-button ec-button--text']"));
					String url = a.getAttribute("href");
					driver.navigate().to(url);
					logger.debug("打开订单页面");
					String trackNo = "" ;
					a = driver.findElement(By.cssSelector("a[title='Track your parcel']"));
					if (a != null) {
						driver.executeScript("var tar=arguments[0];tar.click();",a);
						Utils.sleep(2000);
						a.click();
						logger.debug("打开物流弹窗");
						Utils.sleep(6000);
						
						String parentWindowId = driver.getWindowHandle();
						Set<String> allWindowsId = driver.getWindowHandles();
						for (String windowId : allWindowsId) {
				            if (!windowId.equals(parentWindowId)) {
				                driver.switchTo().window(windowId);
				                break;
				            }
				        }
						Utils.sleep(5000);
						WebElement body = driver.findElementByTagName("body");
						logger.debug(body.getText());
						trackNo = ExpressUtils.extractExperssNo(body.getText());
						
						WebElement aelement = driver.findElement(By.cssSelector("tbody a"));
						String companyUrl = aelement.getAttribute("href");
						if(companyUrl.contains("postnl")){
							logger.debug("找到物流公司：postnl");
							data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_COMPANY, "postnl");
						}
						driver.switchTo().window(parentWindowId);
					}
					
					if(StringUtil.isBlank(trackNo)){
						logger.debug("找不到物流单号");
						return AutoBuyStatus.AUTO_SCRIBE_FAIL;
					}else{
						logger.debug("找到物流单号："+trackNo);
						data.put(AutoBuyConst.KEY_AUTO_BUY_PRO_EXPRESS_NO, trackNo);
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
			driver.get("https://www.escentual.com/");
			WebDriverWait wait = new WebDriverWait(driver, WAIT_TIME);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ec-ca__welcome")));
			return true;
		}
		catch (Exception e){
			logger.error("--->跳转escentual主页面碰到异常");
		}
		return false;
	}

	@Override
	public AutoBuyStatus redeemGiftCard(String cardNo, String balance) {
		// TODO Auto-generated method stub
		return AutoBuyStatus.AUTO_REDEEM_GIFT_CARD_FAIL;
	}

}
