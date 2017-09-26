package com.oversea.task.handle;

import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;


public interface ExternalOrderHandler {
	public static final int RETRY_COUNT = 3;//重试次数
	public void handle(Task task,TaskResult taskResult,Object orderDetail);
}
