package es.caib.comanda.estadistica.persist.config;

//import de.flapdoodle.embed.mongo.config.Net;
//import de.flapdoodle.embed.mongo.distribution.Version;
//import de.flapdoodle.embed.mongo.transitions.Mongod;
//import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
//import de.flapdoodle.embed.process.io.ProcessOutput;
//import de.flapdoodle.reverse.TransitionWalker;
//import de.flapdoodle.reverse.transitions.Start;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.PreDestroy;


//@Slf4j
//@Configuration
public class EmbeddedMongoConfig {
//
//    private TransitionWalker.ReachedState<RunningMongodProcess> runningMongod;
//
//    @Bean
//    public void startEmbeddedMongo() {
//        Mongod mongod = Mongod.builder()
//                .processOutput(Start.to(ProcessOutput.class).initializedWith(ProcessOutput.silent()))
//                .net(Start.to(Net.class).initializedWith(Net.of("0.0.0.0", 27017, false)))
//                .build();
//
//        runningMongod = mongod.start(Version.V8_0_5);
//    }
//
//    @PreDestroy
//    public void stopEmbeddedMongo() {
//        if (runningMongod != null) {
//            runningMongod.close();
//        }
//    }
}
