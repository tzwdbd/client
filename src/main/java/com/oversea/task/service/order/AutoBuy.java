package com.oversea.task.service.order;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.util.StringUtil;
import com.oversea.task.utils.Utils;

abstract public class AutoBuy
{
	private Logger logger = Logger.getLogger(getClass());

	protected FirefoxDriver driver = null;

	public static final int WAIT_TIME = 90;
	public static final String path = "C:\\Users\\Administrator\\AppData\\Local\\Temp";

	// 不要static
	protected Map<String, String> data = new HashMap<String, String>();
	protected Map<String, String> priceMap = new HashMap<String, String>();
	protected Map<String, Integer> statusMap = new HashMap<String, Integer>();
	protected boolean isInitSuccess = false;
	private Map<Long,String> asinMap = null;
	private List<RobotOrderDetail> orderDetailList;
	protected boolean isWap ;
	private Task task;
	protected TaskResult taskResult;
	private String orderNo;
	private BrushOrderDetail brushOrderDetail;
	
	public void setTaskResult(TaskResult taskResult){
		this.taskResult = taskResult;
	}

	public AutoBuy(){
		init(true,true);
	}
	
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public AutoBuy(boolean isWap){
		init(isWap,true);
	}
	
	public AutoBuy(boolean isWap,boolean isNeedDeleteCooike){
		init(isWap,isNeedDeleteCooike);
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	private void init(boolean isWap,boolean isNeedDeleteCooike){
		logger.debug("--->启动驱动并做清理工作"+isNeedDeleteCooike);
		this.isWap = isWap;
		try{
			if(isWap){
				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("general.useragent.override", "Mozilla/5.0 (iPhone; CPU iPhone OS 8_1_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Mobile/12B466 MicroMessenger/6.2.4 NetType/WIFI Language/zh_CN");     
				driver = new FirefoxDriver(profile);
			}else{
				driver = new FirefoxDriver();
			}
			if(isNeedDeleteCooike){
				driver.manage().deleteAllCookies();
			}
			driver.manage().window().maximize();
			isInitSuccess = true;
		}catch(Throwable t){
			if(driver != null){
				try{
					driver.quit();
				}catch(Throwable tt){}
			}
		}
	}
	
	public static float add(float v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).floatValue();
    }
	
	public static float sub(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).floatValue();
    }
	
	public Map<Long, String> getAsinMap() {
		return asinMap;
	}

	public void setAsinMap(Map<Long, String> asinMap) {
		this.asinMap = asinMap;
	}

	public boolean isInitSuccess() {
		return isInitSuccess;
	}


	public void cleanTemp()
	{
		long s = System.currentTimeMillis();
		logger.debug("-->start:" + s);
		Utils.deleteFiles(new File(path));
		long e = System.currentTimeMillis();
		logger.debug("-->end:" + e);
		logger.debug("use :" + (e - s));
	}

	public List<RobotOrderDetail> getOrderDetailList() {
		return orderDetailList;
	}

	public void setOrderDetailList(List<RobotOrderDetail> orderDetailList) {
		this.orderDetailList = orderDetailList;
	}

	/**
	 * 登录
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public abstract AutoBuyStatus login(String userName, String passWord);

	/**
	 * 清空购物车
	 */
	public abstract AutoBuyStatus cleanCart();

	/**
	 * 选择商品
	 * @param param
	 * @return
	 */
	public abstract AutoBuyStatus selectProduct(Map<String, String> param);
	
	/**
	 * 付款
	 * @param param
	 * @return
	 */
	public AutoBuyStatus pay(Map<String, String> param){
		return AutoBuyStatus.AUTO_PAY_FAIL;
	}
	
	
	/**
	 * 
	 * @param param
	 * @param address
	 * @return
	 */
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount){
		return pay(param);
	}
	/**
	 * 
	 * @param param
	 * @param address
	 * @return
	 */
	public AutoBuyStatus pay(Map<String, String> param,UserTradeAddress address,OrderPayAccount payAccount,List<GiftCard> giftCardList){
		return pay(param,address,payAccount);
	}
	
	public byte[] doScreenShot(){
		byte[] screenshot = null;
		if (driver != null){
			FileOutputStream fos = null;
			try{
				File file = new File("screenshot");
				if (!file.exists()){
					file.mkdir();
				}
				screenshot = driver.getScreenshotAs(OutputType.BYTES);
				String filePathName = "";
				if(StringUtil.isNotEmpty(orderNo)){
					filePathName = "screenshot/" + orderNo+"-"+System.currentTimeMillis()/1000 + ".png";
				}else{
					filePathName = "screenshot/" + System.currentTimeMillis()/1000 + ".png";
				}
				logger.debug("--->开始截图,路径:"+filePathName);
				fos = new FileOutputStream(filePathName);
				fos.write(screenshot);
			}
			catch (Throwable e){
				logger.debug("--->截图出现异常",e);
			}finally {  
			    try {  
			        if (fos != null) {  
			        	fos.close();  
			        } 
			    } catch (Exception e) {  
			        e.printStackTrace();  
			    }  
			}
		}
		Utils.sleep(200);
		return screenshot;
	}

	/**
	 * 
	 * @return
	 */
	public boolean logout(boolean isScreenShot){
		if(isScreenShot){
			doScreenShot();
		}
		killFirefox();
		return true;
	}
	
	private void killFirefox(){
		//删除临时文件
		try {  
	    	 cleanTemp();
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }
		
		//浏览器退出
		quitDriver(2500);
		
		//杀掉浏览器进程
		try {
			logger.debug("--->开始结束firefox进程");
			Runtime.getRuntime().exec("taskkill -f -im firefox.exe");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("结束firefox进程异常",e);
		}
		
		Utils.sleep(200);
		logger.info("--->浏览器退出!");
	}

	/**
	 * 带回所需参数
	 * @return
	 */
	public Map<String, String> getData()
	{
		return data;
	}
	
	/**
	 * 
	 */
	public Map<String, String> getPriceMap(){
		return priceMap;
	}
	

	public Map<String, Integer> getStatusMap() {
		return statusMap;
	}

	public void setStatusMap(Map<String, Integer> statusMap) {
		this.statusMap = statusMap;
	}

	/**
	 * 爬取物流公司和单号
	 * @param mallOrderNo
	 * @return
	 */
	public AutoBuyStatus scribeExpress(BrushOrderDetail detail){
		return scribeExpress(detail);
	}
	
	public abstract AutoBuyStatus scribeExpress(RobotOrderDetail detail);

	/**
	 * 
	 * @return
	 */
	public abstract boolean gotoMainPage();

	/**
	 * 充值giftCard
	 * @param cardNo
	 * @param balance
	 * @return
	 */
	public abstract AutoBuyStatus redeemGiftCard(String cardNo, String balance);
	
	public String redeemGiftCard(List<GiftCard> list){return "";}
	
	protected static Set<String> getPromotionList(String promotionStr){
		if(StringUtil.isNotEmpty(promotionStr)){
			Set<String> promotionList = new HashSet<String>();
			String[] as = promotionStr.split(";");
			if(as != null && as.length > 0){
				for(String a : as){
					if(StringUtil.isNotEmpty(a)){
						String[] aas = a.split(",");
						if(aas != null && aas.length > 0){
							for(String aa : aas){
								if(StringUtil.isNotEmpty(aa)){
									promotionList.add(aa.trim());
								}
							}
						}
					}
				}
			}
			return promotionList;
		}
		return null;
	}
	
	public void savePng(){
		try {
			byte[] b = doScreenShot();
			if(b!=null){
				getTask().addParam("screentShot", b);
			}else{
				logger.error("--->screentShot为空");
			}
		} catch (Exception e) {
			logger.error("--->截图失败");
		}
	}
	
	public void quitDriver(long joinTime){
		Runnable quitRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {  
			        logger.debug("--->完成本次浏览器操作");
					logger.debug("===================");
					if (driver != null){
						driver.quit();
			    	}
			    } catch (Throwable e) {  
			    	logger.error(e);
			    } 
			}
		};
		Thread t = new Thread(quitRunnable);
		t.start();
		try{
			t.join(joinTime);
		}catch(Exception e){
			logger.error("join exception = ",e);
		}
		try{
			t.interrupt();
		}catch(Exception e){
			logger.error("interrupt exception = ",e);
		}
		Utils.sleep(2500);
	}
	
	/**
	 * 
	 * @param statusMap 使用优惠码0 失效,1互斥 ,9没修改过,10有效
	 */
	protected void setPromotionCodelistStatus(HashMap<String, Integer> statusMap){
		if(orderDetailList != null && orderDetailList.size() > 0){
			for(RobotOrderDetail robotOrderDetail : orderDetailList){
				String pro = robotOrderDetail.getPromotionCodeList();
				if(StringUtil.isNotEmpty(pro)){
					String[] pros = pro.split(",");
					if(pros != null && pros.length > 0){
						String promotionCodeListStatus = "";
						for(String p : pros){
							if(p != null && StringUtil.isNotEmpty(p.trim())){
								Integer b = statusMap.get(p.trim());
								if(b != null){
									promotionCodeListStatus += (String.valueOf(b)+",");
								}else{
									promotionCodeListStatus += "9,";
								}
							}
						}
						if(StringUtil.isNotEmpty(promotionCodeListStatus) && promotionCodeListStatus.endsWith(",")){
							promotionCodeListStatus = promotionCodeListStatus.substring(0, promotionCodeListStatus.length()-1);
						}
						if(StringUtil.isNotEmpty(promotionCodeListStatus)){
							robotOrderDetail.setPromotionCodeListStatus(promotionCodeListStatus);
						}
					}
				}
			}
		}
	}

	public BrushOrderDetail getBrushOrderDetail() {
		return brushOrderDetail;
	}

	public void setBrushOrderDetail(BrushOrderDetail brushOrderDetail) {
		this.brushOrderDetail = brushOrderDetail;
	}
}
