package com.oversea.task.handle;

import com.oversea.task.domain.BrushOrderDetail;
import com.oversea.task.obj.Task;
import com.oversea.task.obj.TaskResult;

public interface BrushShipHandler {
	public static final int RETRY_COUNT = 3;//重试次数
	public void handle(Task task,TaskResult taskResult,BrushOrderDetail brushOrderDetail);
}
