package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

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
	public RemoteSetDeviceController(UserRepository userRepository, HttpExecutionContext httpExecutionContext)
			throws MqttException {
		this.mqttService = new MqttService();
		this.userRepository = userRepository;
		this.httpExecutionContext = httpExecutionContext;
		Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();
	}

	/*
	 * 手机调用接口,设置esp状态到服务器 id： 设备ID value：设备状态
	 */
	public Result setEspStatus() {
		System.out.println("Mqtt");
		JsonNode json = request().body().asJson();
		String value = json.findPath("value").textValue();
		String deviceid = json.findPath("deviceid").textValue();
		try
		{
			this.mqttService.message = new MqttMessage();
			this.mqttService.message.setQos(2);
			this.mqttService.message.setRetained(true);
			this.mqttService.message.setPayload(String.valueOf(value).getBytes());
			this.mqttService.topic = this.mqttService.client.getTopic(deviceid);
			this.mqttService.publish(this.mqttService.topic, this.mqttService.message);
			this.mqttService.client.subscribe(deviceid);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return ok("ok");
	}

	public CompletionStage<Result> checkEspStatus(String id) throws MqttException {
		this.mqttService.message = new MqttMessage();
		this.mqttService.message.setQos(2);
		this.mqttService.message.setRetained(true);
		this.mqttService.client.subscribe(id);
		String value= new String(this.mqttService.message.getPayload());
		return CompletableFuture.supplyAsync(() -> {
			return ok(value);
		});
	}
}
