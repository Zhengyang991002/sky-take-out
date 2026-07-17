package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.JoinPointSignatureIterator;
import org.slf4j.helpers.Util;
import org.springframework.stereotype.Component;

/**
 * 自定义切面类，用于进行公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

  /**
   * 切入点
   */
  @Pointcut("execution(* com.sky.mapper.*.*(..)) " +
      "&& " +
      "(@annotation(com.sky.annotation.AutoFill))")
  public void autoFillPointCut() {
  }

  /**
   * 前置通知在通知中进攻公共字段的赋值
   */
  @Before("autoFillPointCut()")
  public void autoFill(JoinPoint joinPoint) {
    log.info("公共字段自动填充[前置通知]，方法：{}", joinPoint.getSignature().getName());
    // 获取当前被拦截放大上的数据库操作类型
    MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获取方法签名
    Method method = signature.getMethod(); // 获取方法对象
    AutoFill autofill = method.getAnnotation(AutoFill.class); // 获取方法上的注解对象
    OperationType operationType = autofill.value(); //获取数据库操作类型

    // 获取当前被拦截方法的参数-实体对象
    Object[] args = joinPoint.getArgs();
    if (args == null || args.length == 0) {
      log.info("当前方法没有参数，无需自动填充公共字段");
      return;
    }

    Object entity = args[0];

    // 准备赋值的数据
    LocalDateTime now = LocalDateTime.now();
    Long currentUserId = BaseContext.getCurrentId();

    // 根据当前不同的操作类型，为对应大的属性通过反射来赋值
    if (operationType == OperationType.INSERT) {
      try {
        // 反射获取并赋值
        Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
        setCreateTime.invoke(entity, now);

        Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
        setUpdateTime.invoke(entity, now);

        Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
        setCreateUser.invoke(entity, currentUserId);
        
        Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
        setUpdateUser.invoke(entity, currentUserId);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (operationType == OperationType.UPDATE) {
      try {
        // 反射获取并赋值
        Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
        setUpdateTime.invoke(entity, now);

        Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
        setUpdateUser.invoke(entity, currentUserId);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }


}
