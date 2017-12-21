package repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import models.User;
import javax.inject.Inject;

import controllers.Base64_;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
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

	public CompletionStage<Boolean> insert(String username, String password, String phone, String email, String area) {
		User user = new User();
		user.username = username;
		user.password = password;
		user.email = email;
		user.phone = phone;
		user.register_area = area;
		user.id = System.currentTimeMillis();
		return supplyAsync(() -> {
			ebeanServer.insert(user);
			return true;
		}, executionContext);
	}

	public CompletionStage<Optional<Boolean>> update(String phoneNumber, String password) {
		return supplyAsync(() -> {
			Transaction txn = ebeanServer.beginTransaction();
			Optional<Boolean> value = Optional.empty();
			try {
				User user = ebeanServer.find(User.class).where().eq("phone", phoneNumber).findUnique();
				if (user != null) {
					user.password = password;
					user.update();
					txn.commit();
					value = Optional.of(true);
				} else {
					value = Optional.of(false);
				}
			} finally {
				txn.end();
			}
			return value;
		}, executionContext);
	}

	public Boolean checkuserUnique(String phoneNumber, String username) {
		Boolean value = false;
		try {
			User user = ebeanServer.find(User.class).where().eq("phone", phoneNumber).eq("username", username)
					.findUnique();
			if (user != null) {
				value = false;
			} else {
				value = true;
			}
		} catch (Exception e) {
			value = false;
		} finally {
		}
		return value;
	}

	public CompletionStage<Optional<Boolean>> validUserPassword(String username, String password) {
		return supplyAsync(() -> {
			Transaction txn = ebeanServer.beginTransaction();
			Optional<Boolean> value = Optional.empty();
			try {
				// String decode = Base64_.base64decode(password);
				User user = ebeanServer.find(User.class).where().eq("username", username).eq("password", password)
						.findUnique();
				if (user != null) {
					value = Optional.of(true);
				} else {
					value = Optional.of(false);
				}
			} finally {
				txn.end();
			}
			return value;
		}, executionContext);
	}
}
