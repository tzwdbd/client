package com.oversea.task.handle;

import java.util.List;

import com.oversea.task.domain.ExternalOrderDetail;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;

public interface ExternalShipHandler {
	public static final int RETRY_COUNT = 3;//重试次数
	public void handle(Task task,TaskResult taskResult,List<ExternalOrderDetail> externalOrderDetails);
}
