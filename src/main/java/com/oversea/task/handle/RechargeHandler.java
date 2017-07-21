package com.oversea.task.handle;

import java.util.List;

import com.oversea.task.domain.GiftCard;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;

public interface RechargeHandler {
	
	public static final int RETRY_COUNT = 3;//重试次数
	public void handle(TaskResult taskResult,Task task,List<GiftCard> giftCard);

}
