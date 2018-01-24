package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import org.fusesource.mqtt.client.Future;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.UserRepository;

public class RemoteSetDeviceController extends Controller {
	private final UserRepository userRepository;
	private final HttpExecutionContext httpExecutionContext;
	private final MqttService mqttService;
	// 设备状态查询，大量设备接入需要优化，key为设备id，value为设备开关状态
	// 未测试大规模并发是否绝对安全
	private static Map<Long, Long> deviceStatus = new ConcurrentHashMap<Long, Long>();

	private static LoadingCache<String, String> cahceBuilder = CacheBuilder.newBuilder()
			.expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(100).concurrencyLevel(10)
			.build(new CacheLoader<String, String>() {
				@Override
				public String load(String key) throws Exception {
					return key;
				}
			});

	@Inject
	public RemoteSetDeviceController(UserRepository userRepository, HttpExecutionContext httpExecutionContext) {
		this.mqttService = new MqttService();
		this.userRepository = userRepository;
		this.httpExecutionContext = httpExecutionContext;
		Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();
	}

	/*
	 * 手机调用接口,设置esp状态到服务器 设备ID value：设备状态
	 */
	public Result setEspStatus() {
		JsonNode json = request().body().asJson();
		String value = json.findPath("value").textValue();
		String deviceid = json.findPath("deviceid").textValue();
		try {
			String message = value;
			String topicpush = deviceid+"in";
			mqttService.connection.publish(topicpush, message.getBytes(), QoS.AT_MOST_ONCE, false);
			System.out.println(
					"MQTTFutureServer.publish Message " + "Topic Title :" + topicpush + " context :" + message);
			//性能需要优化
			String topicsub =  deviceid+"out";
			Topic[] topic1 = { new Topic(topicsub, QoS.AT_MOST_ONCE) };
			mqttService.connection.subscribe(topic1);
			Future<Message> futrueMessage = mqttService.connection.receive();
			Message messagesub = futrueMessage.await();
			System.out.println("MQTTFutureClient.Receive Message " + "Topic Title :" + messagesub.getTopic()
					+ " context :" + new String(messagesub.getPayload()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ok("ok");
	}

	public Result checkEspStatus() {
		JsonNode json = request().body().asJson();
		String deviceid = json.findPath("deviceid").textValue();
		String restult = "";
		try {
			String message = "3";
			String topicpush = deviceid+"in";
			String topicsub =  deviceid+"out";
			mqttService.connection.publish(topicpush, message.getBytes(), QoS.AT_MOST_ONCE, false);
			System.out.println(
					"MQTTFutureServer.publish Message " + "Topic Title :" + topicpush + " context :" + message);
			Topic[] topic1 = { new Topic(topicsub, QoS.AT_MOST_ONCE) };
			mqttService.connection.subscribe(topic1);
			Future<Message> futrueMessage = mqttService.connection.receive();
			Message messagesub = futrueMessage.await();
			System.out.println("MQTTFutureClient.Receive Message " + "Topic Title :" + messagesub.getTopic()
					+ " context :" + new String(messagesub.getPayload()));
			return ok(new String(messagesub.getPayload()));
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ok(restult);
		// return CompletableFuture.supplyAsync(() -> {
		// return ok("ok");
		// });
	}
}
