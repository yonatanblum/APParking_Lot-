package acs.aop;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggerAspect {
	private Log logger = LogFactory.getLog(LoggerAspect.class);
	
	@Before("@annotation(acs.aop.MyLogger)")
	public void logAdvice(JoinPoint jp){
		String methodName = jp.getSignature().getName();
		String targetClassName = jp.getTarget().getClass()
			.getName(); // java reflection
		this.logger.trace("***********" + targetClassName + "." + methodName + "()");
	}
	
	@Around("@annotation(acs.aop.MyLogger)")
	public Object logAdviceProxy(ProceedingJoinPoint jp) throws Throwable{
		// pre processing
		// print information regarding method invocation on method start
		String methodName = jp.getSignature().getName();
		String targetClassName = jp.getTarget().getClass()
				.getName(); // java reflection

		// also get arguments types for printing "int, java.lang.String, double, java.lang.Double, java.lang.Boolean" 
		Object[] args = jp.getArgs();
//		String[] argsTypes = Stream.of(args)
		List<String> argsTypes = Stream.of(args)
//			.map(arg->arg.getClass())
//			.map(theClass->theClass.getName())
				
			.map(arg->arg.getClass().getName() + " (" + arg + ")")
			
			.collect(Collectors.toList());
//			.toArray(new String[0]);
			
		
		this.logger.debug("***********" + targetClassName + "." + methodName + "(" + argsTypes + ") - start");
		
		// invoke original method
		
		try {
	//		Object[] args = jp.getArgs();
	//		Object rv = jp.proceed(args);
			
			Object rv = jp.proceed();
			
			// post processing
			// on success print successful ending message and return a value
			this.logger.debug("***********" + targetClassName + "." + methodName + "(" + argsTypes + ") - done successfully");
			return rv;
		} catch (Throwable e) {
			// post processing
			// on error print error ending message and throw the error
			this.logger.error("***********" + targetClassName + "." + methodName + "(" + argsTypes + ") - done with error");
			throw e;
		}
	}
}
