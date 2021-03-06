package com.oversea.task.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.common.RemoteService;
import com.oversea.task.common.TaskService;
import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.domain.ExternalOrderDetail;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.enums.ClientTaskResult;
import com.oversea.task.enums.SiteName;
import com.oversea.task.handle.BrushOrderHandler;
import com.oversea.task.handle.BrushShipHandler;
import com.oversea.task.handle.CheckGiftCardAmazonHandler;
import com.oversea.task.handle.CheckGiftCardAmazonJPHandler;
import com.oversea.task.handle.ExternalOrderHandler;
import com.oversea.task.handle.ExternalShipHandler;
import com.oversea.task.handle.GiftCardCheckHandler;
import com.oversea.task.handle.ManualOrderHandler;
import com.oversea.task.handle.ManualShipHandler;
import com.oversea.task.handle.OrderHandler;
import com.oversea.task.handle.ProductOrderCheckHandler;
import com.oversea.task.handle.RechargeAmazonHandler;
import com.oversea.task.handle.RechargeAmazonJPHandler;
import com.oversea.task.handle.RechargeHandler;
import com.oversea.task.handle.ShipHandler;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.util.StringUtil;

@RemoteService
public class TaskServiceImpl implements TaskService {
	private Log logger = LogFactory.getLog(getClass());
	@Resource
	private OrderHandler orderHandler;
	
	@Resource
	private BrushShipHandler brushShipHandler;
	@Resource
	private ShipHandler shipHandler;
	
	@Resource
	private RechargeAmazonHandler rechargeAmazonHandler;
	
	@Resource
	private RechargeAmazonJPHandler rechargeAmazonJPHandler;
	
	@Resource
	private BrushOrderHandler brushOrderHandler;
	
	@Resource
	private CheckGiftCardAmazonHandler checkGiftCardAmazonHandler;

	@Resource
	private CheckGiftCardAmazonJPHandler checkGiftCardAmazonJPHandler;
	
	@Resource
	private ProductOrderCheckHandler productOrderCheckHandler;
	
	@Resource
	private ManualShipHandler manualShipHandler;
	@Resource
	private ManualOrderHandler manualOrderHandler;
	@Resource
	private ExternalOrderHandler externalOrderHandler;
	@Resource
	private ExternalShipHandler externalShipHandler;

	@Override
	public TaskResult orderService(final Task task) {
		// TODO Auto-generated method stub
		logger.error("orderService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			String mallName = (String) task.getParam("mallName");
			if(StringUtil.isEmpty(mallName)){
				logger.error("mallName is null");
				return taskResult;
			}
			logger.error("sitename = " + mallName);
			Object obj = task.getParam("robotOrderDetails");
			if(obj == null){
				logger.error("robotOrderDetails is null");
				return taskResult;
			}
			taskResult.setValue(obj);
			//开始下单
			orderHandler.handle(task,taskResult,obj);
			try {
				Object obj1 = task.getParam("screentShot");
				if(obj1 != null){
					taskResult.addParam("screentShot", obj1);
				}else{
					logger.error(mallName+"调用screentShot为空");
				}
			} catch (Exception e) {
				logger.error("调用screentShot出现异常",e);
			}
			try {
				Object card = task.getParam("giftCardList");
				if(card != null){
					taskResult.addParam("giftCardList", card);
				}else{
					logger.error(mallName+"调用giftCardList为空");
				}
			} catch (Exception e) {
				logger.error("调用giftCardList出现异常",e);
			}
			
		}catch(Throwable e){
			logger.error("调用orderService出现异常",e);
		}finally{
			logger.error("下单任务结束");
		}
		return taskResult;
	}

	@Override
	public TaskResult shipService(final Task task) {
		// TODO Auto-generated method stub
		logger.error("shipService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			List<RobotOrderDetail> orderDetails = (List<RobotOrderDetail>) task.getParam("robotOrderDetails");
			taskResult.setValue(orderDetails);
			if (orderDetails == null || orderDetails.size() <= 0){
				logger.error("orderDetail is null");
				return taskResult;
			}
			
			//爬物流
			shipHandler.handle(task,taskResult,orderDetails);
			Object obj = task.getParam("expressNodeList");
			if(obj != null){
				taskResult.addParam("expressNodeList", obj);
			}
			Object obj1 = task.getParam("fedroadtext");
			if(obj1 != null){
				taskResult.addParam("fedroadtext", obj1);
			}
			
		}catch(Throwable e){
			logger.error("调用shipService出现异常",e);
		}
		return taskResult;
	}

	@Override
	public TaskResult giftService(final Task task) {
		// TODO Auto-generated method stub
		logger.error("giftService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			List<GiftCard> giftCard = (List<GiftCard>) task.getParam("giftCard");
	        if (giftCard == null) {
	            logger.error("giftCard is null");
	        }
	        taskResult.setValue(giftCard);
	        
	        //充值
	        String siteName = "";
	        if(giftCard != null && giftCard.size() > 0){
	        	siteName = giftCard.get(0).getSiteName();
	        }
			RechargeHandler handler = null;
	        if (SiteName.AMAZON.getName().equalsIgnoreCase(siteName)) {//美亚
	            handler = new RechargeAmazonHandler();
	        } else if (SiteName.AMAZON_JP.getName().equalsIgnoreCase(siteName)) {//日亚
	            handler = new RechargeAmazonJPHandler();
	        }
			if (handler != null) {
	            handler.handle(taskResult, task, giftCard);
	        }
		}catch(Exception e){
			logger.error("调用giftService出现异常",e);
		}
		return taskResult;
	}
	
	@Override
	public TaskResult burshOrderService(final Task task) {
		// TODO Auto-generated method stub
		logger.error("burshOrderService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			String mallName = (String) task.getParam("mallName");
			if(StringUtil.isEmpty(mallName)){
				logger.error("mallName is null");
				return taskResult;
			}
			logger.error("sitename = " + mallName);
			Object obj = task.getParam("brushOrderDetail");
			if(obj == null){
				logger.error("brushOrderDetail is null");
				return taskResult;
			}
			taskResult.setValue(obj);
			//开始下单
			brushOrderHandler.handle(task,taskResult,obj);
			try {
				Object obj1 = task.getParam("screentShot");
				if(obj1 != null){
					taskResult.addParam("screentShot", obj1);
				}else{
					logger.error(mallName+"调用screentShot为空");
				}
			} catch (Exception e) {
				logger.error("调用screentShot出现异常",e);
			}
			try {
				Object card = task.getParam("giftCardList");
				if(card != null){
					taskResult.addParam("giftCardList", card);
				}else{
					logger.error(mallName+"调用giftCardList为空");
				}
			} catch (Exception e) {
				logger.error("调用giftCardList出现异常",e);
			}
			
		}catch(Throwable e){
			logger.error("调用orderService出现异常",e);
		}finally{
			logger.error("下单任务结束");
		}
		return taskResult;
	}

	@Override
	public TaskResult burshShipService(Task task) {
		logger.error("burshShipService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			BrushOrderDetail brushOrderDetail =  (BrushOrderDetail) task.getParam("brushOrderDetail");
			taskResult.setValue(brushOrderDetail);
			if (brushOrderDetail == null){
				logger.error("brushOrderDetail is null");
				return taskResult;
			}
			
			//爬物流
			brushShipHandler.handle(task,taskResult,brushOrderDetail);
			Object obj = task.getParam("expressNodeList");
			if(obj != null){
				taskResult.addParam("expressNodeList", obj);
			}
		}catch(Throwable e){
			logger.error("调用shipService出现异常",e);
		}
		return taskResult;
	}

	@Override
	public TaskResult CheckGiftCard(Task task) {
		// TODO Auto-generated method stub
		logger.error("CheckGiftCard");
		TaskResult taskResult = new ClientTaskResult();
		try{
			OrderAccount account =  (OrderAccount) task.getParam("account");
	        if (account == null) {
	            logger.error("account is null");
	        }
	        //充值
	        String siteName = account.getAccountType();
	        GiftCardCheckHandler handler = null;
	        if (SiteName.AMAZON.getName().equalsIgnoreCase(siteName)) {//美亚
	            handler = new CheckGiftCardAmazonHandler();
	        } else if (SiteName.AMAZON_JP.getName().equalsIgnoreCase(siteName)) {//日亚
	            handler = new CheckGiftCardAmazonJPHandler();
	        }
			if (handler != null) {
				handler.handle(task, taskResult);
	        }
		}catch(Exception e){
			logger.error("调用giftService出现异常",e);
		}
		return taskResult;
	}

	@Override
	public TaskResult productOrderCheckService(Task task) {
		logger.error("productOrderCheckService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			String mallName = (String) task.getParam("mallName");
			if(StringUtil.isEmpty(mallName)){
				logger.error("mallName is null");
				return taskResult;
			}
			logger.error("sitename = " + mallName);
			Object obj = task.getParam("robotOrderDetails");
			if(obj == null){
				logger.error("robotOrderDetails is null");
				return taskResult;
			}
			taskResult.setValue(obj);
			//开始检测
			productOrderCheckHandler.handle(task,taskResult,obj);
		}catch(Throwable e){
			logger.error("调用orderService出现异常",e);
		}finally{
			logger.error("下单任务结束");
		}
		return taskResult;
	}

	@Override
	public TaskResult manualOrder(Task task) {
		logger.error("orderService");
		TaskResult taskResult = new ClientTaskResult();
		try{
			String mallName = (String) task.getParam("mallName");
			if(StringUtil.isEmpty(mallName)){
				logger.error("mallName is null");
				return taskResult;
			}
			logger.error("sitename = " + mallName);
			Object obj = task.getParam("robotOrderDetails");
			if(obj == null){
				logger.error("robotOrderDetails is null");
				return taskResult;
			}
			taskResult.setValue(obj);
			//开始下单
			manualOrderHandler.handle(task,taskResult,obj);
			try {
				Object obj1 = task.getParam("screentShot");
				if(obj1 != null){
					taskResult.addParam("screentShot", obj1);
				}else{
					logger.error(mallName+"调用screentShot为空");
				}
			} catch (Exception e) {
				logger.error("调用screentShot出现异常",e);
			}
			try {
				Object card = task.getParam("giftCardList");
				if(card != null){
					taskResult.addParam("giftCardList", card);
				}else{
					logger.error(mallName+"调用giftCardList为空");
				}
			} catch (Exception e) {
				logger.error("调用giftCardList出现异常",e);
			}
			
		}catch(Throwable e){
			logger.error("调用orderService出现异常",e);
		}finally{
			logger.error("下单任务结束");
		}
		return taskResult;
	}

	@Override
	public TaskResult manualShip(Task task) {
		logger.error("manualShip");
		TaskResult taskResult = new ClientTaskResult();
		try{
			List<RobotOrderDetail> orderDetails = (List<RobotOrderDetail>) task.getParam("robotOrderDetails");
			
			taskResult.setValue(orderDetails);
			if (orderDetails == null || orderDetails.size() <= 0){
				logger.error("orderDetail is null");
				return taskResult;
			}
			
			//爬物流
			manualShipHandler.handle(task,taskResult,orderDetails);
			Object obj = task.getParam("expressNodeList");
			if(obj != null){
				taskResult.addParam("expressNodeList", obj);
			}
		}catch(Throwable e){
			logger.error("调用manualShip出现异常",e);
		}
		return taskResult;
	}

	@Override
	public TaskResult externalOrder(Task task) {
		logger.error("externalOrder");
		TaskResult taskResult = new ClientTaskResult();
		try{
			String mallName = (String) task.getParam("mallName");
			if(StringUtil.isEmpty(mallName)){
				logger.error("mallName is null");
				return taskResult;
			}
			logger.error("sitename = " + mallName);
			Object obj = task.getParam("externalOrderDetails");
			if(obj == null){
				logger.error("externalOrderDetails is null");
				return taskResult;
			}
			taskResult.setValue(obj);
			//开始下单
			externalOrderHandler.handle(task,taskResult,obj);
			try {
				Object obj1 = task.getParam("screentShot");
				if(obj1 != null){
					taskResult.addParam("screentShot", obj1);
				}else{
					logger.error(mallName+"调用screentShot为空");
				}
			} catch (Exception e) {
				logger.error("调用screentShot出现异常",e);
			}
			try {
				Object card = task.getParam("giftCardList");
				if(card != null){
					taskResult.addParam("giftCardList", card);
				}else{
					logger.error(mallName+"调用giftCardList为空");
				}
			} catch (Exception e) {
				logger.error("调用giftCardList出现异常",e);
			}
			
		}catch(Throwable e){
			logger.error("调用orderService出现异常",e);
		}finally{
			logger.error("下单任务结束");
		}
		return taskResult;
	}

	@Override
	public TaskResult externalShip(Task task) {
		logger.error("externalShip");
		TaskResult taskResult = new ClientTaskResult();
		try{
			List<ExternalOrderDetail> externalOrderDetailList = (List<ExternalOrderDetail>) task.getParam("externalOrderDetails");
			taskResult.setValue(externalOrderDetailList);
			if (externalOrderDetailList == null || externalOrderDetailList.size() <= 0){
				logger.error("externalOrderDetail is null");
				return taskResult;
			}
			
			//爬物流
			externalShipHandler.handle(task,taskResult,externalOrderDetailList);
			Object obj1 = task.getParam("fedroadtext");
			if(obj1 != null){
				taskResult.addParam("fedroadtext", obj1);
			}
		}catch(Throwable e){
			logger.error("调用shipService出现异常",e);
		}
		return taskResult;
	}

}
