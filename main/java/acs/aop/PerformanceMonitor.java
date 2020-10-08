package acs.aop;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class PerformanceMonitor {
	private static Log logger = LogFactory.getLog(PerformanceMonitor.class);
	
	// use Aspect Oriented Programming to measure elapsed time of operations
		@Around("@annotation(acs.aop.PerformanceMeasuring)")
		public Object timeElapsed(ProceedingJoinPoint jp) throws Throwable {
			// pre processing - get current time
			long now = System.nanoTime();
			try {
				return jp.proceed();
			} finally {
				// post processing - get current time, calculate and print elapsed time
				long elapsedInNs = System.nanoTime() - now;
				
				long elapsedInMs = elapsedInNs / 10000000L;
				
				long elapsedInS = elapsedInMs / 1000L;
				
				// read annotation
				MethodSignature signature = (MethodSignature)jp.getSignature();
				Method method = signature.getMethod(); // Java Reflection
				PerformanceMeasuring annotation = method.getAnnotation(PerformanceMeasuring.class);
				PerformenceUnits units = annotation.units();
				
				switch (units) {
				case ns:
					logger.debug("********* elapsed time: " + elapsedInNs + "ns");
					break;
					
				default:
				case ms:
					logger.debug("********* elapsed time: " + elapsedInMs + "ms");
					break;
					
				case s:
					logger.debug("********* elapsed time: " + elapsedInS + "s");
					break;
				}
			}
		}
}
