package com.oversea.task.handle;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.domain.AutoOrderCleanCart;
import com.oversea.task.domain.AutoOrderLogin;
import com.oversea.task.domain.AutoOrderPay;
import com.oversea.task.domain.AutoOrderSelectProduct;
import com.oversea.task.domain.GiftCard;
import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.OrderCreditCard;
import com.oversea.task.domain.OrderPayAccount;
import com.oversea.task.domain.RobotOrderDetail;
import com.oversea.task.domain.UserTradeAddress;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.ManualBuy;
import com.oversea.task.util.StringUtil;

@Component
public class ManualOrderNormalHandler implements ManualOrderHandler {
	final Logger logger = Logger.getLogger(getClass());

	@Override
	public void handle(Task task, TaskResult taskResult,Object obj) {
		// TODO Auto-generated method stub
		OrderAccount account = (OrderAccount) task.getParam("account");
		String count = (String) task.getParam("count");
		Boolean isPay = (Boolean) task.getParam("isPay");
		UserTradeAddress address = (UserTradeAddress) task.getParam("address");
		String mallName = (String) task.getParam("mallName");
		OrderPayAccount payAccount = (OrderPayAccount)task.getParam("orderPayAccount");
		OrderCreditCard orderCreditCard = (OrderCreditCard)task.getParam("orderCreditCard");
		List<GiftCard> giftCard = (List<GiftCard>) task.getParam("giftCardList");
		AutoOrderLogin autoOrderLogin = (AutoOrderLogin) task.getParam("autoOrderLogin");
		AutoOrderCleanCart autoOrderCleanCart = (AutoOrderCleanCart) task.getParam("autoOrderCleanCart");
		AutoOrderSelectProduct autoOrderSelectProduct = (AutoOrderSelectProduct) task.getParam("autoOrderSelectProduct");
		AutoOrderPay autoOrderPay = (AutoOrderPay) task.getParam("autoOrderPay");
		
		
		List<RobotOrderDetail> orderDetailList = null;
		if(obj instanceof List){
			try{
				orderDetailList = (List)obj;
			}catch(Exception e){
				orderDetailList = null;
				logger.error("orderDetailList typecast exception");
			}
		}
		
		logger.error("ManualOrderNormalHandler 开始新的一次调用");
		
		for(int i = 0;i<3;i++){
			ManualBuy manualBuy = new ManualBuy(false);
			manualBuy.setTask(task);
			manualBuy.setTaskResult(taskResult);
			manualBuy.setOrderDetailList(orderDetailList);
			try {
				AutoBuyStatus status = manualBuy.login(account.getPayAccount(), account.getLoginPwd(), autoOrderLogin);
				for(RobotOrderDetail r:orderDetailList){
					r.setStatus(status.getValue());
				}
				if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
					if(autoOrderCleanCart!=null){
						status = manualBuy.cleanCart(autoOrderCleanCart);
						for(RobotOrderDetail r:orderDetailList){
							r.setStatus(status.getValue());
						}
						if(AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
							for(RobotOrderDetail detail:orderDetailList){
								status = manualBuy.selectProduct(detail, autoOrderSelectProduct);
								detail.setStatus(status.getValue());
								if(!AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
									break;
								}
							}
							if(AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
								status = manualBuy.pay(orderDetailList, account, address, payAccount, autoOrderLogin,autoOrderPay);
								for(RobotOrderDetail r:orderDetailList){
									r.setStatus(status.getValue());
								}
								if(AutoBuyStatus.AUTO_PAY_SUCCESS.equals(status)){
									break;
								}
								if(AutoBuyStatus.AUTO_PAY_GIFTCARD_IS_TAKEOFF.equals(status) || AutoBuyStatus.AUTO_PAY_GET_MALL_ORDER_NO_FAIL.equals(status)){
									break;
								}
							}
						}
					}
					break;
				}
				
			} catch (Exception e) {
				logger.debug("ManualOrderNormalHandler.doService 碰到异常 = ", e);
			}
			finally{
				manualBuy.killFirefox();
				logger.error("=================>ManualOrderNormalHandler成功完结<======================");
			}
		}
	}
	
	private static void setOrderDetailListStatus(List<RobotOrderDetail> orderDetailList,int status){
		if(orderDetailList != null && orderDetailList.size() > 0){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setStatus(status);
				}
			}
		}
	}
	private static void setOrderDetailListTotalPrice(List<RobotOrderDetail> orderDetailList,String totalPrice){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(totalPrice)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setTotalPrice(totalPrice);
				}
			}
		}
	}
	private static void setOrderDetailListMallOrderNo(List<RobotOrderDetail> orderDetailList,String mallOrderNo){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(mallOrderNo)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setMallOrderNo(mallOrderNo);
				}
			}
		}
	}
	private static void setOrderDetailListMallFee(List<RobotOrderDetail> orderDetailList,String mallFee){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(mallFee)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setMallExpressFee(mallFee);
				}
			}
		}
	}
	private static void setOrderDetailListPromotionFee(List<RobotOrderDetail> orderDetailList,String promotionFee){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(promotionFee)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setPromotionFee(promotionFee);
				}
			}
		}
	}
	
	private static void setOrderDetailListBalancewb(List<RobotOrderDetail> orderDetailList,String balanceWb){
		if(orderDetailList != null && orderDetailList.size() > 0 && StringUtil.isNotEmpty(balanceWb)){
			for(RobotOrderDetail orderDetail : orderDetailList){
				if(orderDetail != null){
					orderDetail.setBalanceWb(balanceWb);
				}
			}
		}
	}

}
