package controllers;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.h2.engine.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.UserRepository;
import scala.collection.immutable.StreamViewLike.EmptyView;
import controllers.Base64_;
import models.User;

public class DeviceMobileController extends Controller {
	private final UserRepository userRepository;
	private final HttpExecutionContext httpExecutionContext;
	private Random random;
	private static LoadingCache<String, String> cahceBuilder = CacheBuilder.newBuilder()
			.expireAfterWrite(60, TimeUnit.SECONDS).maximumSize(100).concurrencyLevel(10)
			.build(new CacheLoader<String, String>() {
				@Override
				public String load(String key) throws Exception {
					return key;
				}
			});

	@Inject
	public DeviceMobileController(UserRepository userRepository, HttpExecutionContext httpExecutionContext) {

		this.userRepository = userRepository;
		this.httpExecutionContext = httpExecutionContext;
		Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();

	}

	public CompletionStage<Result> registeruser() {
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		String passwordEn = json.findPath("password").textValue();
		String phone = json.findPath("phoneNumber").textValue();
		String email = json.findPath("email").textValue();
		String area = json.findPath("area").textValue();
		Boolean status= userRepository.checkuserUnique(phone,name);
		if(status)
		{
			return userRepository.insert(name, passwordEn,phone,email,area).thenApplyAsync(data -> {
				return ok("ok");
			}, httpExecutionContext.current());
		}
		else
		{
			return null;
		}
	}

	public CompletionStage<Result> mobilelogin() {
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		String passwordEn = json.findPath("password").textValue();
		return userRepository.validUserPassword(name, passwordEn).thenApplyAsync(data -> {
			if (data.get()) {
				return ok("ok");
			} else {
				return ok("wrong");
			}
		}, httpExecutionContext.current());
	}

	public CompletionStage<Result> resetpassword() {
		JsonNode json = request().body().asJson();
		String phoneNumber = json.findPath("phoneNumber").textValue();
		String code = json.findPath("code").textValue();
		String passwordEn = json.findPath("password").textValue();
		String cacheCode = cahceBuilder.getIfPresent(phoneNumber);
		if (cacheCode == null) {
			return null;
			
		} else {
			if(code.equals(cacheCode))
			{
				userRepository.update(phoneNumber, passwordEn).thenApplyAsync(data -> {
					if (data.get()) {
						return ok("ok");
					} else {
						return ok("wrong");
					}
				}, httpExecutionContext.current());
			}
			else {
				return null;
			}
		}
		return null;
	}

	public Result mobilecode() {

		JsonNode json = request().body().asJson();
		String phoneNumber = json.findPath("phoneNumber").textValue();
		try {
			random = new Random();
			int randNum = random.nextInt(100000);
			String code = String.valueOf(randNum);
			cahceBuilder.put(phoneNumber, code);
			PhoneMessage.sendSms(phoneNumber, code);
			System.out.println(code);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ok("ok");
	}
}
