package com.noob.loadBalance;

import java.util.Iterator;
import java.util.ServiceLoader;

public class LoadBalanceSpi {
	public static void spiInfo() {
		Iterator<LoadBalance> providers = sun.misc.Service.providers(LoadBalance.class);
		ServiceLoader<LoadBalance> load = ServiceLoader.load(LoadBalance.class);
		while (providers.hasNext()) {
			LoadBalance ser = providers.next();
			System.out.println(ser.getClass());
		}
		Iterator<LoadBalance> iterator = load.iterator();
		while (iterator.hasNext()) {
			LoadBalance ser = iterator.next();
			System.out.println(ser.getClass());
		}
		System.out.println(Thread.currentThread().getContextClassLoader().getClass().getCanonicalName()); // sun.misc.Launcher.AppClassLoader


	}
}
