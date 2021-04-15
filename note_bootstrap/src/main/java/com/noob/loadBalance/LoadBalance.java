package com.noob.loadBalance;

import java.util.List;

import com.noob.loadBalance.impl.ServiceInvoker;

public interface LoadBalance {

	ServiceInvoker select(List<ServiceInvoker> invokers, Object[] parameters);

}