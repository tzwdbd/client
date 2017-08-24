package com.oversea.task.handle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.oversea.task.domain.OrderAccount;
import com.oversea.task.domain.Product;
import com.oversea.task.enums.AutoBuyStatus;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;
import com.oversea.task.service.order.AutoBuy;
import com.oversea.task.utils.ClassUtil;
import com.oversea.task.utils.Utils;

@Component
public class ProductOrderCheckNormalHandler implements ProductOrderCheckHandler {

	private final Logger logger = Logger.getLogger(getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public void handle(Task task, TaskResult taskResult, Object obj) {
		OrderAccount account = (OrderAccount) task.getParam("account");
		String count = (String) task.getParam("count");
		String mallName = (String) task.getParam("mallName");
		Map<Long,String> skuMap = (Map<Long,String>)task.getParam("skuMap");
		List<Product> productList = (List<Product>) obj;
		
		if (account == null)
		{
			logger.error("ProductOrderCheckNormalHandler error : account is null");
			return;
		}
		if (Utils.isEmpty(count))
		{
			count = "0";
		}
		

		logger.error("ProductOrderCheckNormalHandler error : doService 开始新的一次调用");
		
		AutoBuy autoBuy = null;
		autoBuy = ClassUtil.getAutoBuy(mallName);
		if(autoBuy == null){
			logger.error("ProductOrderCheckNormalHandler error : mallName:"+mallName+"找不到对应的Autobuy");
			return;
		}else{
			logger.error("获取到autobuy:"+autoBuy.getClass().getSimpleName());
		}
		
		autoBuy.setTask(task);
		autoBuy.setTaskResult(taskResult);
		if(!autoBuy.isInitSuccess()){
			return;
		}
		try{
			AutoBuyStatus status = autoBuy.login(account.getPayAccount(), account.getLoginPwd());
			if(AutoBuyStatus.AUTO_LOGIN_SUCCESS.equals(status)){
				status = autoBuy.cleanCart();
				if (AutoBuyStatus.AUTO_CLEAN_CART_SUCCESS.equals(status)){
					boolean isSelectSkuSuccess = true;
					
					for(Product product : productList){
						if(product == null){
							continue;
						}
						Map<String, String> parmas = new HashMap<String, String>();
						String url = product.getUrl();
						if (Utils.isEmpty(url)){
							logger.error("ProductOrderCheckNormalHandler error : 商品链接url为空");
							return;
						}
						parmas.put("url", url);
						parmas.put("sku", skuMap.get(product.getId()));
						parmas.put("num", "1");
						parmas.put("productEntityId", String.valueOf(product.getDefaultEntityId()));
						
						status = autoBuy.selectProduct(parmas);
						if (!AutoBuyStatus.AUTO_SKU_SELECT_SUCCESS.equals(status)){
							isSelectSkuSuccess = false;
							break;
						}
					}
					
					if(isSelectSkuSuccess){
						Boolean checkStatus = autoBuy.productOrderCheck(taskResult);
						logger.debug("AutomanProductOrderCheckHandlerExecutor.checkStatus = "+checkStatus);
						taskResult.addParam("checkStatus", checkStatus);
					}
				}
			}
		}catch(Exception e){
			logger.debug("AutomanProductOrderCheckHandlerExecutor.doService 碰到异常 = ", e);
		}finally{
			autoBuy.logout(false);
			logger.debug("=================>AutomanProductOrderCheckHandler成功完结<======================");
			logger.debug(obj.toString());
		}
	

	}

}
