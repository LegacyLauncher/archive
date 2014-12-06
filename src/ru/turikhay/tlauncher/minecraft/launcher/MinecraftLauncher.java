package ru.turikhay.tlauncher.minecraft.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import joptsimple.OptionSet;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.json.DateTypeAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.TLauncherLite;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.Configuration.ActionOnLaunch;
import ru.turikhay.tlauncher.downloader.AbortedDownloadException;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.downloader.DownloadableContainerHandler;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.managers.AssetsManager;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.managers.ServerList.Server;
import ru.turikhay.tlauncher.managers.ServerListManager;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashDescriptor;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.stream.LinkedStringStream;
import ru.turikhay.util.stream.PrintLogger;

import com.google.gson.Gson;

public class MinecraftLauncher implements JavaProcessListener {
	private final static int OFFICIAL_VERSION = 16;
	private final static int ALTERNATIVE_VERSION = 8;
	private final static boolean[] ASSETS_PROBLEM = new boolean[2];

	private boolean working;

	private final Thread parentThread;

	private final Gson gson;
	private final DateTypeAdapter dateAdapter;

	private final Downloader downloader;
	private final Configuration settings;

	private final boolean forceUpdate, assistLaunch;

	private final VersionManager vm;
	private final AssetsManager am;
	private final ProfileManager pm;

	private final LinkedStringStream output;
	private final PrintLogger logger;

	private final Console console;
	private final ConsoleVisibility consoleVis;

	private CrashDescriptor descriptor;

	private final List<MinecraftListener> listeners;
	private final List<MinecraftExtendedListener> extListeners;
	private final List<MinecraftLauncherAssistant> assistants;

	private MinecraftLauncherStep step;

	//

	public Downloader getDownloader() {
		return downloader;
	}

	public Configuration getConfiguration() {
		return settings;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	public boolean isLaunchAssist() {
		return assistLaunch;
	}

	public LinkedStringStream getStream() {
		return output;
	}

	public PrintLogger getLogger() {
		return logger;
	}

	public ConsoleVisibility getConsoleVisibility() {
		return consoleVis;
	}

	public Console getConsole() {
		return console;
	}

	public CrashDescriptor getCrashDescriptor() {
		return descriptor;
	}

	public MinecraftLauncherStep getStep() {
		return step;
	}

	public boolean isWorking() {
		return working;
	}

	private MinecraftLauncher(ComponentManager manager, Downloader downloader,
			Configuration configuration, boolean forceUpdate, ConsoleVisibility visibility,
			boolean exit) {
		if (manager == null)
			throw new NullPointerException("Ti sovsem s duba ruhnul?");

		if (downloader == null)
			throw new NullPointerException("Downloader is NULL!");

		if (configuration == null)
			throw new NullPointerException("Configuration is NULL!");

		if (visibility == null)
			throw new NullPointerException("ConsoleVisibility is NULL!");

		this.parentThread = Thread.currentThread();

		this.gson = new Gson();
		this.dateAdapter = new DateTypeAdapter();

		this.downloader = downloader;
		this.settings = configuration;

		this.assistants = manager
				.getComponentsOf(MinecraftLauncherAssistant.class);
		this.vm = manager.getComponent(VersionManager.class);
		this.am = manager.getComponent(AssetsManager.class);
		this.pm = manager.getComponent(ProfileManager.class);

		this.forceUpdate = forceUpdate;
		this.assistLaunch = !exit;

		this.output = new LinkedStringStream();
		this.logger = new PrintLogger(output);

		this.consoleVis = visibility;
		this.console = consoleVis.equals(ConsoleVisibility.NONE) ? null : new Console(settings, logger, "Minecraft Logger", consoleVis.equals(ConsoleVisibility.ALWAYS) && assistLaunch);

		this.descriptor = new CrashDescriptor(this);

		this.listeners = Collections
				.synchronizedList(new ArrayList<MinecraftListener>());
		this.extListeners = Collections
				.synchronizedList(new ArrayList<MinecraftExtendedListener>());

		this.step = MinecraftLauncherStep.NONE;

		//

		log("Minecraft Launcher [" + OFFICIAL_VERSION + ";"
				+ ALTERNATIVE_VERSION + "] has initialized");
		log("Running under TLauncher " + TLauncher.getVersion() + " "
				+ TLauncher.getBrand());
		log("Current machine:", OS.getSummary());
	}

	public MinecraftLauncher(TLauncher t, boolean forceUpdate) {
		this(t.getManager(), t.getDownloader(), t.getSettings(), forceUpdate, t
				.getSettings().getConsoleType().getVisibility(), t
				.getSettings().getActionOnLaunch() == ActionOnLaunch.EXIT);
	}

	public MinecraftLauncher(TLauncherLite tl, OptionSet options) {
		this(
				tl.getLauncher().getManager(),
				tl.getLauncher().getDownloader(),
				tl.getLauncher().getSettings(),
				options.has("force"),
				ConsoleVisibility.NONE,
				false);
	}

	public void addListener(MinecraftListener listener) {
		if (listener == null)
			throw new NullPointerException();

		if (listener instanceof MinecraftExtendedListener)
			extListeners.add((MinecraftExtendedListener) listener);

		listeners.add(listener);
	}

	public void start() {
		checkWorking();
		this.working = true;

		try {
			collectInfo();
		} catch (Throwable e) {
			log("Error occurred:", e);

			if (e instanceof MinecraftException) {
				// Send as known error
				MinecraftException me = (MinecraftException) e;

				for (MinecraftListener listener : listeners)
					listener.onMinecraftKnownError(me);

			} else if (e instanceof MinecraftLauncherAborted) {
				// Download process has been aborted

				for (MinecraftListener listener : listeners)
					listener.onMinecraftAbort();

			} else {
				// Send as unknown error
				for (MinecraftListener listener : listeners)
					listener.onMinecraftError(e);
			}
		}

		working = false;
		this.step = MinecraftLauncherStep.NONE;

		log("Launcher exited.");
	}

	public void stop() {
		if(step == MinecraftLauncherStep.NONE)
			throw new IllegalStateException();

		if(step == MinecraftLauncherStep.DOWNLOADING)
			downloader.stopDownload();

		working = false;
	}

	//

	private String versionName;
	private VersionSyncInfo versionSync;
	private CompleteVersion version;

	private String accountName;
	private Account account;

	private File javaDir, gameDir, localAssetsDir, nativeDir;
	private File globalAssetsDir, assetsIndexesDir, assetsObjectsDir;
	private int[] windowSize;
	private boolean fullScreen;
	private int ramSize;

	private JavaProcessLauncher launcher;
	private String javaArgs, programArgs;

	private boolean minecraftWorking;
	private long startupTime;
	private int exitCode;

	private Server server;

	//

	public String getVersion() {
		return versionName;
	}

	public int getExitCode() {
		return exitCode;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		checkWorking();

		this.server = server;
	}

	private void collectInfo() throws MinecraftException {
		checkStep(MinecraftLauncherStep.NONE, MinecraftLauncherStep.COLLECTING);
		log("Collecting info...");

		for (MinecraftListener listener : listeners)
			listener.onMinecraftPrepare();

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftCollecting();

		log("Force update:", forceUpdate);

		this.versionName = settings.get("login.version");
		if (versionName == null || versionName.isEmpty())
			throw new IllegalArgumentException("Version name is NULL or empty!");

		log("Selected version:", versionName);

		this.accountName = settings.get("login.account");
		if (accountName == null || accountName.isEmpty())
			throw new IllegalArgumentException("Account is NULL or empty!");

		this.account = pm.getAuthDatabase().getByUsername(accountName);
		if (account == null)
			account = new Account(accountName);

		log("Selected account:", account.toDebugString());

		this.versionSync = vm.getVersionSyncInfo(versionName);
		if (versionSync == null)
			throw new IllegalArgumentException("Cannot find version " + version);

		log("Version sync info:", versionSync);

		try {
			this.version = versionSync.resolveCompleteVersion(vm, forceUpdate);
		} catch (IOException e) {
			throw new RuntimeException("Cannot get complete version!");
		}

		if (version == null)
			throw new NullPointerException("Complete version is NULL");

		String javaDirPath = settings.get("minecraft.javadir");
		this.javaDir = new File(javaDirPath == null ? OS.getJavaPath()
				: javaDirPath);

		log("Java path:", javaDir);

		this.gameDir = new File(settings.get("minecraft.gamedir"));
		try {
			FileUtil.createFolder(gameDir);
		} catch (IOException e) {
			throw new MinecraftException("Cannot create working directory!",
					"folder-not-found", e);
		}

		this.globalAssetsDir = new File(gameDir, "assets");
		try {
			FileUtil.createFolder(globalAssetsDir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot create assets directory!", e);
		}

		this.assetsIndexesDir = new File(globalAssetsDir, "indexes");
		try {
			FileUtil.createFolder(assetsIndexesDir);
		} catch (IOException e) {
			throw new RuntimeException(
					"Cannot create assets indexes directory!", e);
		}

		this.assetsObjectsDir = new File(globalAssetsDir, "objects");
		try {
			FileUtil.createFolder(assetsObjectsDir);
		} catch (IOException e) {
			throw new RuntimeException(
					"Cannot create assets objects directory!", e);
		}

		this.nativeDir = new File(gameDir, "versions/" + this.version.getID()
				+ "/" + "natives");
		try {
			FileUtil.createFolder(nativeDir);
		} catch (IOException e) {
			throw new RuntimeException("Cannot create native files directory!",
					e);
		}

		this.javaArgs = settings.get("minecraft.javaargs");
		if (javaArgs != null && javaArgs.isEmpty())
			javaArgs = null;

		this.programArgs = settings.get("minecraft.args");
		if (programArgs != null && programArgs.isEmpty())
			programArgs = null;

		this.windowSize = settings.getClientWindowSize();

		if (windowSize[0] < 1)
			throw new IllegalArgumentException("Invalid window width!");

		if (windowSize[1] < 1)
			throw new IllegalArgumentException("Invalid window height!");

		this.fullScreen = settings.getBoolean("minecraft.fullscreen");

		this.ramSize = settings.getInteger("minecraft.memory");

		if(ramSize < OS.Arch.MIN_MEMORY)
			throw new IllegalArgumentException("Invalid RAM size!");

		for (MinecraftLauncherAssistant assistant : assistants)
			assistant.collectInfo();

		log("Checking conditions...");

		if (version.getMinimumCustomLauncherVersion() > ALTERNATIVE_VERSION)
			throw new MinecraftException(
					"Alternative launcher is incompatible with launching version!",
					"incompatible");

		if (version.getMinimumCustomLauncherVersion() == 0
				&& version.getMinimumLauncherVersion() > OFFICIAL_VERSION)
			Alert.showLocAsyncWarning("launcher.warning.title",
					"launcher.warning.incompatible.launcher");

		if (!version.appliesToCurrentEnvironment())
			Alert.showLocAsyncWarning("launcher.warning.title",
					"launcher.warning.incompatible.environment");

		this.downloadResources();
	}

	private void downloadResources() throws MinecraftException {
		checkStep(MinecraftLauncherStep.COLLECTING,
				MinecraftLauncherStep.DOWNLOADING);

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftComparingAssets();

		final List<AssetObject> assets = compareAssets();

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftDownloading();

		DownloadableContainer execContainer;
		try {
			execContainer = vm.downloadVersion(versionSync, forceUpdate);
		} catch (IOException e) {
			throw new MinecraftException("Cannot download version!",
					"download-jar", e);
		}
		execContainer.setConsole(console);

		DownloadableContainer assetsContainer = null;
		assetsContainer = am.downloadResources(version, assets, forceUpdate);

		final boolean[] kasperkyBlocking = new boolean[1];

		if (assetsContainer != null) {
			assetsContainer.addHandler(new DownloadableContainerHandler() {

				@Override
				public void onStart(DownloadableContainer c) {
				}

				@Override
				public void onAbort(DownloadableContainer c) {
				}

				@Override
				public void onError(DownloadableContainer c, Downloadable d, Throwable e) {
				}

				@Override
				public void onComplete(DownloadableContainer c, Downloadable d) throws RetryDownloadException {
					String filename = d.getFilename();
					AssetObject object = null;

					for (AssetObject asset : assets)
						if (filename.equals(asset.getHash()))
							object = asset;

					if (object == null) {
						log("Couldn't find object:", filename);
						return;
					}

					File destination = d.getDestination();
					String hash = FileUtil.getDigest(destination, "SHA-1", 40);

					if (hash == null)
						throw new RetryDownloadException("File hash is NULL!");

					String assetHash = object.getHash();
					if (assetHash == null) {
						log("Hash of", object.getHash(), "is NULL");
						return;
					}

					if (!hash.equals(assetHash)) {

						if(hash.equals("2daeaa8b5f19f0bc209d976c02bd6acb51b00b0a"))
							kasperkyBlocking[0] = true;

						throw new RetryDownloadException("Hashes are not equal. Got: " + hash + "; expected: "+ assetHash);
					}
				}

				@Override
				public void onFullComplete(DownloadableContainer c) {
					log("Assets have been downloaded");
				}
			});
			assetsContainer.setConsole(console);
		}

		if (assetsContainer != null)
			downloader.add(assetsContainer);
		downloader.add(execContainer);

		for (MinecraftLauncherAssistant assistant : assistants)
			assistant.collectResources(downloader);

		downloader.startDownloadAndWait();

		if (execContainer.isAborted())
			throw new MinecraftLauncherAborted(new AbortedDownloadException());

		if (!execContainer.getErrors().isEmpty())
			throw new MinecraftException(
					execContainer.getErrors().size() + " error(s) occurred while trying to download executable files.\n" +
							"First error: " + U.toLog(execContainer.getErrors().get(0)) + "\nRest of stack trace:",
					"download");

		if(assetsContainer != null
				&& !assetsContainer.getErrors().isEmpty()
				&& !(assetsContainer.getErrors().get(0) instanceof AbortedDownloadException)
				)
		{
			if(kasperkyBlocking[0] && !ASSETS_PROBLEM[1]) {
				// Kaspersky blocked assets downloading and problem #1 (kaspersky issue) hasn't been shown.
				Alert.showLocAsyncWarning("launcher.warning.title", "launcher.warning.assets.kaspersky", "http://resources.download.minecraft.net/ad/ad*");
				ASSETS_PROBLEM[1] = true; // mark as shown
			} else if(!ASSETS_PROBLEM[0]) {
				// problem #0 (general assets download issue) hasn't been shown
				Alert.showLocAsyncWarning("launcher.warning.title", "launcher.warning.assets");
				ASSETS_PROBLEM[0] = true; // mark as shown
			}
		} else {
			Arrays.fill(ASSETS_PROBLEM, false);
		}

		try {
			vm.getLocalList().saveVersion(version);
		} catch (IOException e) {
			log("Cannot save version!", e);
		}

		this.constructProcess();
	}

	private void constructProcess() throws MinecraftException {
		checkStep(MinecraftLauncherStep.DOWNLOADING,
				MinecraftLauncherStep.CONSTRUCTING);

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftReconstructingAssets();

		try {
			this.localAssetsDir = this.reconstructAssets();
		} catch (IOException e) {
			throw new MinecraftException("Cannot recounstruct assets!",
					"reconstruct-assets", e);
		}

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftUnpackingNatives();

		try {
			this.unpackNatives(forceUpdate);
		} catch (IOException e) {
			throw new MinecraftException("Cannot unpack natives!",
					"unpack-natives", e);
		}

		checkAborted();

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftDeletingEntries();

		try {
			this.deleteEntries();
		} catch (IOException e) {
			throw new MinecraftException("Cannot delete entries!",
					"delete-entries", e);
		}

		checkAborted();

		log("Constructing process...");
		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftConstructing();

		launcher = new JavaProcessLauncher(javaDir.getAbsolutePath(), new String[0]);
		launcher.directory(gameDir);

		if (OS.OSX.isCurrent()) {
			File icon = null;

			try {
				icon = getAssetObject("icons/minecraft.icns");
			} catch (IOException e) {
				log("Cannot get icon file from assets.", e);
			}

			if (icon != null)
				launcher.addCommand("-Xdock:icon=\"" + icon.getAbsolutePath()
						+ "\"", "-Xdock:name=Minecraft");
		}

		launcher.addCommand("-Xmx"+ ramSize +"M");

		launcher.addCommand("-Djava.library.path="+ nativeDir.getAbsolutePath());
		launcher.addCommand("-cp", constructClassPath(version));

		launcher.addCommands(getJVMArguments());

		if (javaArgs != null)
			launcher.addSplitCommands(javaArgs);

		for (MinecraftLauncherAssistant assistant : assistants)
			assistant.constructJavaArguments();

		launcher.addCommand(version.getMainClass());

		log("Half command (characters are not escaped, without Minecraft arguments):");
		log(launcher.getCommandsAsString());

		launcher.addCommands(getMinecraftArguments());

		launcher.addCommand("--width", windowSize[0]);
		launcher.addCommand("--height", windowSize[1]);

		if(fullScreen)
			launcher.addCommand("--fullscreen");

		if(server != null) {
			ServerList serverList = new ServerList();
			serverList.add(server);

			try {
				ServerListManager.reconstructList(serverList, new File(gameDir, "servers.dat"));
			} catch(Exception e) {
				log("Couldn't reconstruct server list.", e);
			}

			String[] address = StringUtils.split(server.getAddress(), ':');

			switch(address.length) {
			case 2:
				launcher.addCommand("--port", address[1]);
			case 1:
				launcher.addCommand("--server", address[0]);
				break;
			default:
				log("Cannot recognize server:", server);
			}
		}

		if (programArgs != null)
			launcher.addSplitCommands(programArgs);

		for (MinecraftLauncherAssistant assistant : assistants)
			assistant.constructProgramArguments();

		this.launchMinecraft();
	}

	private File reconstructAssets() throws IOException {
		String assetVersion = this.version.getAssets() == null ? "legacy"
				: this.version.getAssets();
		File indexFile = new File(assetsIndexesDir, assetVersion + ".json");
		File virtualRoot = new File(new File(globalAssetsDir, "virtual"),
				assetVersion);

		if (!indexFile.isFile()) {
			log("No assets index file " + virtualRoot
					+ "; can't reconstruct assets");
			return virtualRoot;
		}

		AssetIndex index = this.gson.fromJson(FileUtil.readFile(indexFile),
				AssetIndex.class);
		if (index.isVirtual()) {
			log("Reconstructing virtual assets folder at " + virtualRoot);
			for (Map.Entry<String, AssetObject> entry : index.getFileMap().entrySet()) {

				checkAborted();

				File
				target = new File(virtualRoot, entry.getKey()),
				original = new File(new File(assetsObjectsDir, entry.getValue().getHash().substring(0, 2)), entry.getValue().getHash());

				if (!original.isFile())
					log("Skipped reconstructing:", original);
				else if (forceUpdate || !target.isFile()) {
					FileUtils.copyFile(original, target, false);
					log(original, "->", target);
				}
			}

			FileUtil.writeFile(new File(virtualRoot, ".lastused"),
					this.dateAdapter.toString(new Date()));
		}
		return virtualRoot;
	}

	private File getAssetObject(String name) throws IOException {
		String assetVersion = version.getAssets();
		File indexFile = new File(assetsIndexesDir, assetVersion + ".json");

		AssetIndex index = gson.fromJson(FileUtil.readFile(indexFile),
				AssetIndex.class);

		if (index.getFileMap() == null)
			throw new IOException("Cannot get filemap!");

		String hash = index.getFileMap().get(name).getHash();
		return new File(assetsObjectsDir, hash.substring(0, 2) + "/" + hash);
	}

	private void unpackNatives(boolean force) throws IOException {
		log("Unpacking natives...");
		Collection<Library> libraries = version.getRelevantLibraries();

		OS os = OS.CURRENT;
		ZipFile zip = null;
		BufferedOutputStream bufferedOutputStream;

		if (force)
			nativeDir.delete();

		for (Library library : libraries) {
			Map<OS, String> nativesPerOs = library.getNatives();

			if (nativesPerOs != null && nativesPerOs.get(os) != null) {
				File file = new File(MinecraftUtil.getWorkingDirectory(),
						"libraries/"
								+ library.getArtifactPath(nativesPerOs.get(os)));

				if(!file.isFile())
					throw new IOException("Required archive doesn't exist: "+ file.getAbsolutePath());

				try {
					zip = new ZipFile(file);
				} catch(IOException e) {
					throw new IOException("Error opening ZIP archive: "+ file.getAbsolutePath(), e);
				}

				ExtractRules extractRules = library.getExtractRules();
				Enumeration<? extends ZipEntry> entries = zip.entries();

				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					if (extractRules == null
							|| extractRules.shouldExtract(entry.getName())) {
						File targetFile = new File(this.nativeDir,
								entry.getName());

						if (!force && targetFile.isFile())
							continue;

						if (targetFile.getParentFile() != null)
							targetFile.getParentFile().mkdirs();

						if (!entry.isDirectory()) {
							BufferedInputStream inputStream = new BufferedInputStream(
									zip.getInputStream(entry));

							byte[] buffer = new byte[2048];
							FileOutputStream outputStream = new FileOutputStream(
									targetFile);
							bufferedOutputStream = new BufferedOutputStream(
									outputStream);

							int length;
							while ((length = inputStream.read(buffer, 0,
									buffer.length)) != -1)
								bufferedOutputStream.write(buffer, 0, length);

							inputStream.close();
							bufferedOutputStream.close();
						}
					}
				}
				zip.close();
			}
		}
	}

	private void deleteEntries() throws IOException {
		List<String> entries = this.version.getRemovableEntries();
		if (entries == null || entries.size() == 0)
			return;
		log("Removing entries...");

		File file = version.getFile(gameDir);
		FileUtil.removeFromZip(file, entries);
	}

	private String constructClassPath(CompleteVersion version)
			throws MinecraftException {
		log("Constructing classpath...");

		StringBuilder result = new StringBuilder();
		Collection<File> classPath = version.getClassPath(OS.CURRENT, gameDir);
		String separator = System.getProperty("path.separator");

		for (File file : classPath) {
			if (!file.isFile())
				throw new MinecraftException("Classpath is not found: " + file,
						"classpath", file);
			if (result.length() > 0)
				result.append(separator);
			result.append(file.getAbsolutePath());
		}

		return result.toString();
	}

	private String[] getMinecraftArguments() throws MinecraftException {
		log("Getting Minecraft arguments...");
		if (version.getMinecraftArguments() == null)
			throw new MinecraftException(
					"Can't run version, missing minecraftArguments", "noArgs");

		Map<String, String> map = new HashMap<String, String>();
		StrSubstitutor substitutor = new StrSubstitutor(map);

		String assets = version.getAssets();
		String[] split = version.getMinecraftArguments().split(" ");

		map.put("auth_username", accountName);

		if (account.isPremium()) {
			map.put("auth_session",
					String.format("token:%s:%s",
							new Object[] { account.getAccessToken(),
							account.getProfile().getId() }));
			map.put("auth_access_token", account.getAccessToken());
			map.put("user_properties", gson.toJson(account.getProperties()));

			map.put("auth_player_name", account.getDisplayName());
			map.put("auth_uuid", account.getUUID());
			map.put("user_type", "mojang");
			map.put("profile_name", account.getProfile().getName());
		} else {
			map.put("auth_session", "null");
			map.put("auth_access_token", "null");
			map.put("user_properties", "[]");

			map.put("auth_player_name", accountName);
			map.put("auth_uuid", new UUID(0L, 0L).toString());
			map.put("user_type", "legacy");
			map.put("profile_name", "(Default)");
		}

		map.put("version_name", version.getID());

		map.put("game_directory", gameDir.getAbsolutePath());
		map.put("game_assets", localAssetsDir.getAbsolutePath());

		map.put("assets_root", globalAssetsDir.getAbsolutePath());
		map.put("assets_index_name", assets == null ? "legacy" : assets);

		for (int i = 0; i < split.length; i++)
			split[i] = substitutor.replace(split[i]);

		return split;
	}

	private String[] getJVMArguments() {
		String jvmargs = version.getJVMArguments();

		if(StringUtils.isNotEmpty(jvmargs))
			return StringUtils.split(jvmargs, ' ');

		if(OS.JAVA_VERSION > 1.7)
			return new String[0];

		return StringUtils.split("-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M", ' ');
	}

	private List<AssetObject> compareAssets() {
		this.migrateOldAssets();

		log("Comparing assets...");
		long start = System.nanoTime();

		List<AssetObject> result = am.checkResources(version, !forceUpdate);

		long end = System.nanoTime(), delta = end - start;
		log("Delta time to compare assets: " + delta / 1000000L + " ms.");

		return result;
	}

	private void migrateOldAssets() {
		if (!globalAssetsDir.isDirectory())
			return;

		IOFileFilter migratableFilter = FileFilterUtils
				.notFileFilter(FileFilterUtils.or(new IOFileFilter[] {
						FileFilterUtils.nameFileFilter("indexes"),
						FileFilterUtils.nameFileFilter("objects"),
						FileFilterUtils.nameFileFilter("virtual") }));
		for (File file : new TreeSet<File>(FileUtils.listFiles(globalAssetsDir,
				TrueFileFilter.TRUE, migratableFilter))) {
			String hash = FileUtil.getDigest(file, "SHA-1", 40);
			File destinationFile = new File(assetsObjectsDir, hash.substring(0,
					2) + "/" + hash);

			if (!destinationFile.exists()) {
				log("Migrated old asset", file, "into", destinationFile);
				try {
					FileUtils.copyFile(file, destinationFile);
				} catch (IOException e) {
					log("Couldn't migrate old asset", e);
				}
			}
			FileUtils.deleteQuietly(file);
		}

		File[] assets = globalAssetsDir.listFiles();
		if (assets != null) {
			for (File file : assets) {
				if ((!file.getName().equals("indexes"))
						&& (!file.getName().equals("objects"))
						&& (!file.getName().equals("virtual"))) {
					log("Cleaning up old assets directory", file,
							"after migration");
					FileUtils.deleteQuietly(file);
				}
			}
		}
	}

	private void launchMinecraft() throws MinecraftException {
		checkStep(MinecraftLauncherStep.CONSTRUCTING,
				MinecraftLauncherStep.LAUNCHING);
		log("Launching Minecraft...");

		for (MinecraftListener listener : listeners)
			listener.onMinecraftLaunch();

		log("Starting Minecraft " + versionName + "...");
		log("Launching in:", gameDir.getAbsolutePath());

		startupTime = System.currentTimeMillis();

		try {
			JavaProcess process = launcher.start();
			process.safeSetExitRunnable(this);

			minecraftWorking = true;
		} catch (Exception e) {
			notifyClose();
			throw new MinecraftException("Cannot start the game!", "start", e);
		}

		this.postLaunch();
	}

	private void postLaunch() {
		checkStep(MinecraftLauncherStep.LAUNCHING,
				MinecraftLauncherStep.POSTLAUNCH);
		log("Processing post-launch actions. Assist launch:", assistLaunch);

		for (MinecraftExtendedListener listener : extListeners)
			listener.onMinecraftPostLaunch();

		if (assistLaunch) {
			// Wait until minecraft closes
			this.waitForClose();
		} else {
			U.sleepFor(30000);
			// Minecraft is still working well, killing TLauncher
			if (minecraftWorking)
				TLauncher.kill();
		}
	}

	private void log(Object... o) {
		U.log("[Launcher]", o);
		logger.log("[L]", o);
	}

	private void checkThread() {
		if (!Thread.currentThread().equals(parentThread))
			throw new IllegalStateException("Illegal thread!");
	}

	private void checkStep(MinecraftLauncherStep prevStep,
			MinecraftLauncherStep currentStep) {
		checkAborted();

		if (prevStep == null || currentStep == null)
			throw new NullPointerException("NULL: " + prevStep + " "
					+ currentStep);

		// Check if method is called from valid step
		if (!step.equals(prevStep))
			throw new IllegalStateException("Called from illegal step: " + step);

		// Check if method is called from a parent thread
		checkThread();

		this.step = currentStep;
	}

	private void checkAborted() {
		if(!working)
			throw new MinecraftLauncherAborted("Aborted at step: "+step);
	}

	private void checkWorking() {
		if(working)
			throw new IllegalStateException("Launcher is working!");
	}

	@Override
	public void onJavaProcessLog(JavaProcess jp, String line) {
		U.plog(">", line);
		logger.log(line);
	}

	@Override
	public void onJavaProcessEnded(JavaProcess jp) {
		this.notifyClose();

		int exit = jp.getExitCode();

		for (MinecraftListener listener : listeners)
			listener.onMinecraftClose();

		log("Minecraft closed with exit code: " + exit);

		this.exitCode = exit;
		Crash crash = descriptor.scan();

		if (crash == null) {

			if (!assistLaunch)
				TLauncher.kill();

			if (console != null)
				console.killIn(5000);

		} else {

			if (console != null)
				console.show();

			for (MinecraftListener listener : listeners)
				listener.onMinecraftCrash(crash);
		}
	}

	@Override
	public void onJavaProcessError(JavaProcess jp, Throwable e) {
		this.notifyClose();

		for (MinecraftListener listener : listeners)
			listener.onMinecraftError(e);
	}

	private synchronized void waitForClose() {
		while (minecraftWorking)
			try {
				wait();
			} catch (InterruptedException e) {
			}
	}

	private synchronized void notifyClose() {
		minecraftWorking = false;

		if(System.currentTimeMillis() - startupTime < 5000)
			U.sleepFor(1000); // GUI fix

		// Wake up parent thread.
		notifyAll();

		for (MinecraftListener listener : listeners)
			listener.onMinecraftClose();
	}

	public enum MinecraftLauncherStep {
		NONE, COLLECTING, DOWNLOADING, CONSTRUCTING, LAUNCHING, POSTLAUNCH;
	}

	public enum ConsoleVisibility {
		ALWAYS, ON_CRASH, NONE;
	}

	class MinecraftLauncherAborted extends RuntimeException {
		private static final long serialVersionUID = -3001265213093607559L;

		MinecraftLauncherAborted(String message) {
			super(message);
		}

		MinecraftLauncherAborted(Throwable cause) {
			super(cause);
		}
	}
}
