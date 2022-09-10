package com.jade.envelope.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncPoolConfig {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数3->线程池创建时候初始化的线程数
        executor.setCorePoolSize(3);
        // 最大线程数4->线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(4);
        // 缓冲队列100->用来缓冲执行任务的队列
        executor.setQueueCapacity(100);
        // 允许线程的空闲时间60秒->当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀->设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("async_task_");
        /*
         * 线程池对拒绝任务(无线程可用)的处理策略
         * AbortPolicy:丢弃任务,直接抛出java.util.concurrent.RejectedExecutionException异常,默认的策略
         * CallerRunsPolicy:这个策略重试添加当前的任务,他会自动重复调用 execute() 方法,直到成功
         * DiscardOldestPolicy: 丢弃队列最前面的任务,然后重新尝试执行任务,会导致被丢弃的任务无法再次被执行
         * DiscardPolicy:抛弃当前任务,会导致被丢弃的任务无法再次被执行,但是不抛出异常
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
