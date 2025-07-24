package kr.hhplus.be.server;

import com.redis.testcontainers.RedisContainer;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;
	public static final RedisContainer REDIS_CONTAINER;
	public static final ConfluentKafkaContainer KAFKA_CONTAINER;

	static {
		// MySQL 컨테이너
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());

		// Redis 컨테이너
		REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.4.4"))
			.withExposedPorts(6379);
		REDIS_CONTAINER.start();

		System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString());

		// Kafka 컨테이너
		KAFKA_CONTAINER = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0").asCompatibleSubstituteFor("apache/kafka"))
			.withExtraHost("localhost", "127.0.0.1");
		KAFKA_CONTAINER.start();
		System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());

	}

	@Bean(destroyMethod = "shutdown")
	public RedissonClient redissonClient() {
		Config config = new Config();
		String redisUrl = String.format("redis://%s:%d",
			REDIS_CONTAINER.getHost(), REDIS_CONTAINER.getFirstMappedPort());
		config.useSingleServer().setAddress(redisUrl);
		return Redisson.create(config);
	}

	@Bean(name = "testProducerFactory")
	public ProducerFactory<String, String> testProducerFactory() {
		return new DefaultKafkaProducerFactory<>(
			Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers(),
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
		);
	}

	@Bean(name = "testKafkaTemplate")
	public KafkaTemplate<String, String> testKafkaTemplate() {
		return new KafkaTemplate<>(testProducerFactory());
	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
		if (KAFKA_CONTAINER.isRunning()) {
			KAFKA_CONTAINER.stop();
		}
	}
}