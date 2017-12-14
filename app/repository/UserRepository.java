package repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import models.User;
import javax.inject.Inject;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Company;
import models.Computer;
import play.db.ebean.EbeanConfig;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UserRepository {
	    private final EbeanServer ebeanServer;
	    private final DatabaseExecutionContext executionContext;

	    @Inject
	    public UserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
	        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
	        this.executionContext = executionContext;
	    }

	    public CompletionStage<Map<String, String>> options() {
	        return supplyAsync(() -> ebeanServer.find(User.class).orderBy("username").findList(), executionContext)
	                .thenApply(list -> {
	                    HashMap<String, String> options = new LinkedHashMap<String, String>();
	                    for (User c : list) {
	                        options.put(c.id.toString(), c.username);
	                    }
	                    return options;
	                });
	    }
	    
	    public CompletionStage<Long> insert(String username,String password) {
	    	User user = new User();
	    	user.username = username;
	    	user.password =password;
	    	user.id = System.currentTimeMillis();
	        return supplyAsync(() -> {
	             ebeanServer.insert(user);
	             return user.id;
	        }, executionContext);
	    }
}
