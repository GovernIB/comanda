package es.caib.comanda.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("AsyncThread-");
//        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();
        return executor;
    }

//    public class ContextCopyingDecorator implements TaskDecorator {
//        @Override
//        public Runnable decorate(Runnable runnable) {
//            // Copia el context del fil actual
//            var context = ThreadLocalContextHolder.getContextSnapshot();
//            return () -> {
//                try {
//                    // Estableix el context abans d'executar
//                    ThreadLocalContextHolder.setContextSnapshot(context);
//                    runnable.run();
//                } finally {
//                    // Neteja el context un cop ha acabat
//                    ThreadLocalContextHolder.clearContext();
//                }
//            };
//        }
//    }

}
