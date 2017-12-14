package controllers;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
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
	    
	    public CompletionStage<Result> registeruserTest(String username,String password)
	    {
	        System.out.println(username);
	    	System.out.println(password);
	    	//Request request;
	    	//System.out.println(json.toString());
	        return userRepository.insert(username,password).thenApplyAsync(data -> {
	            // This is the HTTP rendering thread context
	        	return ok("ok");
	        }, httpExecutionContext.current());
	    	//return null;
	    }
}
