package eu.h2020.symbiote.tm;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mateuszl on 30.09.2016.
 *
 * Note: to be used by components with MongoDB
 */

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import eu.h2020.symbiote.util.RabbitConstants;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author RuggenthalerC
 * 
 *         Component config beans.
 */
@Configuration
@EnableScheduling
//@EnableMongoRepositories
class AppConfig implements SchedulingConfigurer {

//	@Value("${spring.data.mongodb.host:localhost}")
//	private String mongoHost;

//	@Override
//	protected String getDatabaseName() {
//		return "symbiote-cloud-tm-database";
//	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
	}

	@Bean(destroyMethod="shutdown")
	public Executor taskExecutor() {
		return Executors.newScheduledThreadPool(4);
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(3);
		return threadPoolTaskScheduler;
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

//	@Bean
//	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
//		return restTemplateBuilder.setConnectTimeout(1 * 1000).setReadTimeout(10 * 1000).build();
//	}

	@Bean
    public RestTemplate restTemplate() throws Exception{
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

		HttpClientBuilder builder = HttpClients
				.custom();

		HttpHost proxy = new HttpHost("icache.intracomtel.com", 80, "http");
		builder.setProxy(proxy);

		requestFactory.setHttpClient(builder.build());

		return new RestTemplate(requestFactory);
    }

	@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

//	@Bean
//	public Queue federationHistoryQueue(@Value("${rabbit.queue.federation.get_federation_history}") String queue) {
//		return new Queue(queue);
//	}
//
//	@Bean
//	public TopicExchange trustTopic(@Value("${" + RabbitConstants.EXCHANGE_TRUST_NAME_PROPERTY + "}") String exchange,
//			@Value("${" + RabbitConstants.EXCHANGE_TRUST_DURABLE_PROPERTY + "}") Boolean durable,
//			@Value("${" + RabbitConstants.EXCHANGE_TRUST_AUTODELETE_PROPERTY + "}") Boolean autoDelete) {
//		return new TopicExchange(exchange, durable, autoDelete);
//	}

//	@Override
//	public Mongo mongo() {
//		return new MongoClient();
//	}
//
//    @Bean
//    @Override
//    public MongoTemplate mongoTemplate() {
//        return new MongoTemplate(new MongoClient(mongoHost), getDatabaseName());
//    }

}