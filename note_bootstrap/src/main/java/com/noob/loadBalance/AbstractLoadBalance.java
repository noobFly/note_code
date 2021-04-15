package com.noob.loadBalance;

import java.util.List;

import com.noob.loadBalance.impl.ServiceInvoker;

public abstract class AbstractLoadBalance implements LoadBalance {

	static int calculateWarmupWeight(int uptime, int warmup, int weight) {
		int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
		return ww < 1 ? 1 : (ww > weight ? weight : ww);
	}

	public ServiceInvoker select(List<ServiceInvoker> invokers, Object[] parameters) {
		if (invokers == null || invokers.isEmpty())
			return null;
		if (invokers.size() == 1)
			return invokers.get(0);
		return doSelect(invokers, parameters);
	}

	protected abstract ServiceInvoker doSelect(List<ServiceInvoker> invokers, Object[] parameters);
}
