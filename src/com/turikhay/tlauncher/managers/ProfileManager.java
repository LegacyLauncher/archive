package com.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.FileTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.turikhay.tlauncher.component.RefreshableComponent;
import com.turikhay.tlauncher.minecraft.auth.AccountListener;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import com.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import com.turikhay.tlauncher.minecraft.profiles.Profile;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;

public class ProfileManager extends RefreshableComponent {
	public static final String DEFAULT_PROFILE_NAME = "TLauncher";
	public static final String DEFAULT_PROFILE_FILENAME = "launcher_profiles.json";

	private final List<ProfileManagerListener> listeners;
	private final AccountListener accountListener;

	private final JsonParser parser = new JsonParser();
	private final Gson gson;

	private final Map<String, Profile> profiles;
	private String selectedProfile;
	private File file;
	private UUID clientToken;
	private AuthenticatorDatabase authDatabase;

	public ProfileManager(ComponentManager manager, File file) throws Exception {
		super(manager);

		if (file == null)
			throw new NullPointerException();

		this.file = file;
		this.listeners = Collections
				.synchronizedList(new ArrayList<ProfileManagerListener>());

		this.profiles = new HashMap<String, Profile>();
		this.clientToken = UUID.randomUUID();
		this.accountListener = new AccountListener() {
			@Override
			public void onAccountsRefreshed(AuthenticatorDatabase db) {
				for (AccountListener listener : listeners)
					listener.onAccountsRefreshed(db);
			}
		};
		this.authDatabase = new AuthenticatorDatabase();
		authDatabase.setListener(accountListener);

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
		builder.registerTypeAdapter(File.class, new FileTypeAdapter());
		builder.registerTypeAdapter(AuthenticatorDatabase.class,
				new AuthenticatorDatabase.Serializer());
		builder.registerTypeAdapter(UUIDTypeAdapter.class, new UUIDTypeAdapter());
		builder.setPrettyPrinting();

		this.gson = builder.create();
	}

	public ProfileManager(ComponentManager manager) throws Exception {
		this(manager, getDefaultFile());
	}

	public void recreate() {
		this.setFile(getDefaultFile());
		this.refresh();
	}

	@Override
	public boolean refresh() {
		loadProfiles();

		for (ProfileManagerListener listener : listeners)
			listener.onProfilesRefreshed(this);
		
		try{ saveProfiles(); }
		catch(IOException e) {
			return false;
		}

		return true;
	}

	private void loadProfiles() {
		log("Loading profiles from:", file);
		
		this.selectedProfile = null;
		this.profiles.clear();


		RawProfileList raw = null;

		if(file.isFile())
			try {
				raw = this.gson.fromJson(parser.parse(FileUtil.readFile(file))
						.getAsJsonObject(), RawProfileList.class);
			} catch (Exception e) {
				U.log("Cannot parse profile list! Loading an empty one.", e);
			}
		
		if(raw == null)
			raw = new RawProfileList();

		this.clientToken = raw.clientToken;
		this.selectedProfile = raw.selectedProfile;
		this.authDatabase = raw.authenticationDatabase;
		authDatabase.setListener(accountListener);

		this.profiles.putAll(raw.profiles);
	}

	public void saveProfiles() throws IOException {
		RawProfileList raw = new RawProfileList();

		raw.clientToken = clientToken;
		raw.selectedProfile = selectedProfile;
		raw.profiles = profiles;
		raw.authenticationDatabase = authDatabase;

		FileUtil.writeFile(file, this.gson.toJson(raw));
	}

	public AuthenticatorDatabase getAuthDatabase() {
		return authDatabase;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		if (file == null)
			throw new NullPointerException();

		this.file = file;

		for (ProfileManagerListener listener : listeners)
			listener.onProfileManagerChanged(this);
	}

	public UUID getClientToken() {
		return clientToken;
	}

	public void setClientToken(String uuid) {
		this.clientToken = UUID.fromString(uuid);
	}

	public void addListener(ProfileManagerListener listener) {
		if (listener == null)
			throw new NullPointerException();

		if (!this.listeners.contains(listener))
			this.listeners.add(listener);
	}

	private static File getDefaultFile() {
		return new File(MinecraftUtil.getWorkingDirectory(),
				DEFAULT_PROFILE_FILENAME);
	}

	static class RawProfileList {
		Map<String, Profile> profiles = new HashMap<String, Profile>();
		String selectedProfile;
		UUID clientToken = UUID.randomUUID();
		AuthenticatorDatabase authenticationDatabase = new AuthenticatorDatabase();
	}
}
