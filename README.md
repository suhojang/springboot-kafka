Apache Kafka
---

+ 분산 스트리밍 플랫폼
+ 데이터 파이프 라인 구성 시 주로 사용되는 오픈소스 솔루션
+ 대용량의 실시간 로그처리에 특화되어 있는 솔루션
+ 데이터를 유실없이 안전하게 전달하는 것이 주목적인 메시지 시스템
+ 클러스터링이 가능하므로 Fault-Tolerant한 안정적인 아키텍처와 빠른 퍼포먼스로 데이터를 처리
+ 수평적으로 서버의 Scale-Out이 가능함
+ pub-sub모델의 메시지 큐

> 본 예제는 Kafka에 접근하여 메시지를 넣고 메시지를 받아오는 Producer, Consumer로 구성
 
 
###  dependency 추가

```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.kafka:spring-kafka'
	testImplementation('org.springframework.boot:spring-boot-starter-test') {
		exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
	}
	testImplementation 'org.springframework.kafka:spring-kafka-test'
}
```

### application.yml 설정

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: 192.168.253.8:9092
      # Kafka 클러스터에 대한 초기 연결, global 설정이 있어도, consumer.bootstrap-servers가 존재하면 consuemer 전용으로 overring
      # Consumer는 Consumer Group이 존재하기 때문에, 유일하게 식별 가능한 Consumer Group을 작성
    # group-id를 application.yml에 지정하거나 Consumer Service의 @KafkaListener 에 지정하여 사용할 수도 있다.
    # group-id: testgroup
      # auto-offset-reset
      # Kafka 서버에 초기 offset이 없거나, 서버에 현재 offset이 더 이상 없는 경우 수행할 작업을 작성합니다.
      # Consumer Group의 Consumer는 메시지를 소비할 때 Topic내에 Partition에서 다음에 소비할 offset이 어디인지 공유를 하고 있습니다.
      # 그런데 오류 등으로 인해 이러한 offset 정보가 없어졌을 때 어떻게 offeset을 reset 할 것인지를 명시한다고 보시면 됩니다.
      # latest : 가장 최근에 생산된 메시지로 offeset reset
      # earliest : 가장 오래된 메시지로 offeset reset
      # none : offset 정보가 없으면 Exception 발생
      # 직접 Kafka Server에 접근하여 offset을 reset할 수 있지만, Spring에서 제공해주는 방식은 위와 같습니다.
      auto-offset-reset: earliest
      # Kafka에서 데이터를 받아올 때, key / value를 역직렬화 합니다
      # JSON 데이터를 넘겨줄 것이라면 JsonDeserializer도 가능
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      bootstrap-servers: 192.168.253.8:9092
      # Kafka에 데이터를 보낼 때, key / value를 직렬화
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer


# 여기서는 Producer/Consumer 설정을 application.yml에 작성했지만, bean을 통해 설정하는 방법도 있습니다.
# Producer, Consumer의 설정을 여러 개로 관리하고 싶다면 bean으로 구현하는 것도 좋은 방법일듯 합니다.
```

### Test를 위한 RestController 작성(Producer)

```java
package com.example.kafka.springbootkafka.controller;

import com.example.kafka.springbootkafka.service.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    @Autowired
    private KafkaProducer producer;

    @PostMapping
    public void sendMessage(@RequestParam("message") String message){
        this.producer.sendMessage(message);
    }
}
```

### Consumer Listener 생성

```java
package com.example.kafka.springbootkafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    @KafkaListener(topics = "test_topic", groupId = "testgroup")
    public void consumeMessage(String message){
        System.out.println("Consumer message => " + message);
    }
}
```

### Producer 생성

```java
package com.example.kafka.springbootkafka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private static final String TOPIC = "test_topic";
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message){
        System.out.println("Produce message => " + message);
        this.kafkaTemplate.send(TOPIC, message);
    }
}
```

### 실행

```groovy
오후 5:29:46: Executing task 'SpringbootKafkaApplication.main()'...

> Task :compileJava
> Task :processResources UP-TO-DATE
> Task :classes

> Task :SpringbootKafkaApplication.main()

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v2.3.10.RELEASE)

2021-05-10 17:29:48.085  INFO 25704 --- [           main] c.e.k.s.SpringbootKafkaApplication       : Starting SpringbootKafkaApplication on DESKTOP-7ECUB86 with PID 25704 (D:\Documents\GitHub\springboot-kafka\build\classes\java\main started by DEV_PC in D:\Documents\GitHub\springboot-kafka)
2021-05-10 17:29:48.088  INFO 25704 --- [           main] c.e.k.s.SpringbootKafkaApplication       : No active profile set, falling back to default profiles: default
2021-05-10 17:29:49.385  INFO 25704 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2021-05-10 17:29:49.395  INFO 25704 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2021-05-10 17:29:49.396  INFO 25704 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.45]
2021-05-10 17:29:49.472  INFO 25704 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2021-05-10 17:29:49.472  INFO 25704 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1331 ms
2021-05-10 17:29:49.701  INFO 25704 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2021-05-10 17:29:49.921  INFO 25704 --- [           main] o.a.k.clients.consumer.ConsumerConfig    : ConsumerConfig values: 
	allow.auto.create.topics = true
	auto.commit.interval.ms = 5000
	auto.offset.reset = earliest
	bootstrap.servers = [192.168.253.8:9092]
	check.crcs = true
	client.dns.lookup = default
	client.id = 
	client.rack = 
	connections.max.idle.ms = 540000
	default.api.timeout.ms = 60000
	enable.auto.commit = false
	exclude.internal.topics = true
	fetch.max.bytes = 52428800
	fetch.max.wait.ms = 500
	fetch.min.bytes = 1
	group.id = testgroup
	group.instance.id = null
	heartbeat.interval.ms = 3000
	interceptor.classes = []
	internal.leave.group.on.close = true
	isolation.level = read_uncommitted
	key.deserializer = class org.apache.kafka.common.serialization.StringDeserializer
	max.partition.fetch.bytes = 1048576
	max.poll.interval.ms = 300000
	max.poll.records = 500
	metadata.max.age.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partition.assignment.strategy = [class org.apache.kafka.clients.consumer.RangeAssignor]
	receive.buffer.bytes = 65536
	reconnect.backoff.max.ms = 1000
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retry.backoff.ms = 100
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	security.providers = null
	send.buffer.bytes = 131072
	session.timeout.ms = 10000
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2]
	ssl.endpoint.identification.algorithm = https
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLSv1.2
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	value.deserializer = class org.apache.kafka.common.serialization.StringDeserializer

2021-05-10 17:29:50.022  INFO 25704 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.5.1
2021-05-10 17:29:50.023  INFO 25704 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 0efa8fb0f4c73d92
2021-05-10 17:29:50.024  INFO 25704 --- [           main] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1620635390021
2021-05-10 17:29:50.026  INFO 25704 --- [           main] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Subscribed to topic(s): test01
2021-05-10 17:29:50.028  INFO 25704 --- [           main] o.s.s.c.ThreadPoolTaskScheduler          : Initializing ExecutorService
2021-05-10 17:29:50.047  INFO 25704 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-05-10 17:29:50.090  INFO 25704 --- [           main] c.e.k.s.SpringbootKafkaApplication       : Started SpringbootKafkaApplication in 2.354 seconds (JVM running for 2.729)
2021-05-10 17:29:50.337  INFO 25704 --- [ntainer#0-0-C-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Cluster ID: EIo1yYNoQu-iaYuB2pj03A
2021-05-10 17:29:50.338  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Discovered group coordinator 192.168.253.8:9092 (id: 2147482646 rack: null)
2021-05-10 17:29:50.340  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] (Re-)joining group
2021-05-10 17:29:50.362  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Join group failed with org.apache.kafka.common.errors.MemberIdRequiredException: The group member needs to have a valid member id before actually entering a consumer group
2021-05-10 17:29:50.363  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] (Re-)joining group
2021-05-10 17:29:50.378  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Finished assignment for group at generation 8: {consumer-testgroup-1-84cf8174-94aa-467c-a696-ae32900e6fb2=Assignment(partitions=[test01-0, test01-1, test01-2])}
2021-05-10 17:29:50.393  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Successfully joined group with generation 8
2021-05-10 17:29:50.396  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Adding newly assigned partitions: test01-0, test01-1, test01-2
2021-05-10 17:29:50.411  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Setting offset for partition test01-1 to the committed offset FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[192.168.253.8:9092 (id: 1001 rack: null)], epoch=0}}
2021-05-10 17:29:50.411  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Setting offset for partition test01-2 to the committed offset FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[192.168.253.8:9092 (id: 1001 rack: null)], epoch=0}}
2021-05-10 17:29:50.411  INFO 25704 --- [ntainer#0-0-C-1] o.a.k.c.c.internals.ConsumerCoordinator  : [Consumer clientId=consumer-testgroup-1, groupId=testgroup] Setting offset for partition test01-0 to the committed offset FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=Optional[192.168.253.8:9092 (id: 1001 rack: null)], epoch=0}}
2021-05-10 17:29:50.412  INFO 25704 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : testgroup: partitions assigned: [test01-0, test01-1, test01-2]
Consumer message => hello kwic
Consumer message => hi
```

위 로그를 보면 서버 구동 시 application.yml 에 설정 한 Consumer 의 설정들을 불러오고 Kafka 와 연동 되는 모습을 볼 수 있다.   
마지막 로그에는 Consumer의 마지막 offset 부터 메시지를 받아 온 걸 확인 할 수 있다.

### /kafka API Call

```groovy
Produce message => hello kwic
2021-05-10 17:30:52.462  INFO 25704 --- [nio-8080-exec-2] o.a.k.clients.producer.ProducerConfig    : ProducerConfig values: 
	acks = 1
	batch.size = 16384
	bootstrap.servers = [192.168.253.8:9092]
	buffer.memory = 33554432
	client.dns.lookup = default
	client.id = producer-1
	compression.type = none
	connections.max.idle.ms = 540000
	delivery.timeout.ms = 120000
	enable.idempotence = false
	interceptor.classes = []
	key.serializer = class org.apache.kafka.common.serialization.StringSerializer
	linger.ms = 0
	max.block.ms = 60000
	max.in.flight.requests.per.connection = 5
	max.request.size = 1048576
	metadata.max.age.ms = 300000
	metadata.max.idle.ms = 300000
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	receive.buffer.bytes = 32768
	reconnect.backoff.max.ms = 1000
	reconnect.backoff.ms = 50
	request.timeout.ms = 30000
	retries = 2147483647
	retry.backoff.ms = 100
	sasl.client.callback.handler.class = null
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism = GSSAPI
	security.protocol = PLAINTEXT
	security.providers = null
	send.buffer.bytes = 131072
	ssl.cipher.suites = null
	ssl.enabled.protocols = [TLSv1.2]
	ssl.endpoint.identification.algorithm = https
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.protocol = TLSv1.2
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.timeout.ms = 60000
	transactional.id = null
	value.serializer = class org.apache.kafka.common.serialization.StringSerializer

2021-05-10 17:30:52.478  INFO 25704 --- [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.5.1
2021-05-10 17:30:52.478  INFO 25704 --- [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 0efa8fb0f4c73d92
2021-05-10 17:30:52.478  INFO 25704 --- [nio-8080-exec-2] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1620635452478
2021-05-10 17:30:52.499  INFO 25704 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Cluster ID: EIo1yYNoQu-iaYuB2pj03A
Consumer message => hello kwic
```

위 로그를 보면 메시지를 전달하게 될 때 application.yml 에 설정 한 Producer 를 설정하는 것을 볼 수 있다.   
중요하게 볼 것은 acks = 1 로그이다.   
Producer가 Kakfa 서버로 메시지를 전달 하기 전 서로 신호를 받았다는 로그이다.     
이후 Kafka 서버로 메시지가 전달 되고 Consumer Listener 에서 메시지를 전달 받아 로그로 보여 주는 것을 확인 할 수 있다.