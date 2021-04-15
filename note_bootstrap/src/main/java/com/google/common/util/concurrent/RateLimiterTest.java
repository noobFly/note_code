package com.google.common.util.concurrent;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.RateLimiter.SleepingStopwatch;

public class RateLimiterTest {
	public static void main(String args[]) throws Exception {

		// testSmoothBursty();
		 testSmoothWarmingUp();

	}

	private static void testSmoothWarmingUp() throws Exception {
		RateLimiterWrapper limiter = new RateLimiterWrapper(RateLimiter.create(5, 1, TimeUnit.SECONDS, 4.0, SleepingStopwatch.createFromSystemTimer()));
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();

	}

	private static void testSmoothBursty() throws Exception {
		RateLimiterWrapper limiter = new RateLimiterWrapper(RateLimiter.create(2));

		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		System.out.println("Thread.sleep 2s");
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
		limiter.acquire();
	}

	static class RateLimiterWrapper {

		private RateLimiter limiter;
		private Stopwatch stopwatch;
		private String simpleName;
		private Field storedPermits;
		private Field nextFreeTicketMicros;

		public RateLimiterWrapper(RateLimiter limiter) throws Exception {
			this.limiter = limiter;

			Class<?> clas = limiter.getClass();
			simpleName = clas.getSimpleName();
			storedPermits = clas.getSuperclass().getDeclaredField("storedPermits");
			storedPermits.setAccessible(true);
			nextFreeTicketMicros = clas.getSuperclass().getDeclaredField("nextFreeTicketMicros");
			nextFreeTicketMicros.setAccessible(true);
			Field stopwatchField = clas.getSuperclass().getSuperclass().getDeclaredField("stopwatch");
			stopwatchField.setAccessible(true);
			Object sleepingStopwatch = stopwatchField.get(limiter);
			Field stopwatchFiled = (sleepingStopwatch.getClass().getDeclaredField("stopwatch"));
			stopwatchFiled.setAccessible(true);
			stopwatch = (Stopwatch) stopwatchFiled.get(sleepingStopwatch);
			System.out.println(String.format("%s -> 初始化阶段:  init-storedPermits: %s, init-nextFreeTicketMicros: %s",
					simpleName, storedPermits.get(limiter), nextFreeTicketMicros.get(limiter)));
		}

		long readMicros() {
			return stopwatch.elapsed(TimeUnit.MICROSECONDS);
		}

		double acquire() throws Exception {
			long reqTimeMirco = readMicros();
			Object beforeStoredPermits = storedPermits.get(this.limiter);
			Object beforeNextFreeTicketMicros = nextFreeTicketMicros.get(this.limiter);

			double waitMirco = this.limiter.acquire();

			Object afterStoredPermits = storedPermits.get(this.limiter);
			Object afterNextFreeTicketMicros = nextFreeTicketMicros.get(this.limiter);

			System.out.println(String.format(
					"reqTimeMirco: %s, before-storedPermits: %s, before-nextFreeTicketMicros: %s, waitSeconds: %ss, after-storedPermits: %s, after-nextFreeTicketMicros: %s",
					reqTimeMirco, convert(beforeStoredPermits), beforeNextFreeTicketMicros, convert(waitMirco),
					convert(afterStoredPermits), afterNextFreeTicketMicros));
			return waitMirco;

		}

		void acquireAsync() {
			new Thread(() -> {
				try {
					acquire();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}

	}

	public static BigDecimal convert(Object o) {
		return new BigDecimal(String.valueOf(o)).setScale(4, RoundingMode.HALF_UP);
	}
}
