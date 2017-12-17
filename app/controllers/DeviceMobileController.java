package controllers;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import repository.UserRepository;

public class DeviceMobileController extends Controller{
	private final UserRepository userRepository;
	private final HttpExecutionContext httpExecutionContext;
	 @Inject
	    public DeviceMobileController(UserRepository userRepository,HttpExecutionContext httpExecutionContext) {

	        this.userRepository = userRepository;
	        this.httpExecutionContext = httpExecutionContext;
	    }
	 
	    public CompletionStage<Result> register(String username,String password)
	    {
	        System.out.println(username);
	    	System.out.println(password);
	        return userRepository.insert(username,password).thenApplyAsync(data -> {
	            // This is the HTTP rendering thread context
	            return ok("ok");
	        }, httpExecutionContext.current());
	    }
	    
	    public CompletionStage<Result> registeruserTest()
	    {
	    	JsonNode json = request().body().asJson();
	    	String name = json.findPath("name").textValue();
	    	System.out.println(name);
	    	String passwordEn = json.findPath("password").textValue();
	    	System.out.println(name);
	    	System.out.println(passwordEn);
	    	//String str2 = Base64_.base64decode(passwordEn);
	    	//System.out.println(str2);
	        return userRepository.insert(name,passwordEn).thenApplyAsync(data -> {
	        	return ok("ok");
	        }, httpExecutionContext.current());
	    }
	    
	    public CompletionStage<Result> mobilelogin()
	    {
	    	JsonNode json = request().body().asJson();
	    	String name = json.findPath("name").textValue();
	    	String passwordEn = json.findPath("password").textValue();
	    	//System.out.println(name);
	    	//System.out.println(passwordEn);
	    	//String decode = Base64_.base64decode(passwordEn);
	    	//System.out.println("de code: "+str2);
	    	return userRepository.validUserPassword(name,passwordEn).thenApplyAsync(data -> {
	    		if(data.get())
	    		{
	    			return ok("ok");
	    		}
	    		else
	    		{
	    			return ok("wrong");
	    		}
	        }, httpExecutionContext.current());
	    }
}
