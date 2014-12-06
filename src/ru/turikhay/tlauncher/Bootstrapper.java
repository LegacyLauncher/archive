package ru.turikhay.tlauncher;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;

import org.apache.commons.lang3.StringUtils;

import ru.turikhay.tlauncher.ui.LoadingFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public final class Bootstrapper {

	public static void main(String[] args) {
		try {
			new Bootstrapper(args).start();
		} catch (IOException e) {
			e.printStackTrace();
			TLauncher.main(args);
		}
	}

	public static JavaProcessLauncher createLauncher(String[] args, boolean loadAdditionalArgs) {
		JavaProcessLauncher processLauncher = new JavaProcessLauncher(null, new String[0]);

		processLauncher.directory(DIRECTORY);
		processLauncher.addCommand("-Xmx"+ MAX_MEMORY +"m");
		processLauncher.addCommand("-cp", FileUtil.getRunningJar());
		processLauncher.addCommand(MAIN_CLASS);

		if(args != null && args.length > 0)
			processLauncher.addCommands(args);

		if(loadAdditionalArgs) {
			File argsFile = new File(DIRECTORY, "tlauncher.args");

			if(argsFile.isFile()) {
				String[] extraArgs = loadArgsFromFile(argsFile);

				if(extraArgs != null)
					processLauncher.addCommands(extraArgs);
			}
		}

		return processLauncher;
	}

	public static JavaProcessLauncher createLauncher(String[] args) {
		return createLauncher(args, true);
	}

	private static final String MAIN_CLASS = "ru.turikhay.tlauncher.TLauncher";
	private static final int MAX_MEMORY = 96;
	private static final File DIRECTORY = new File(".");

	private final JavaProcessLauncher processLauncher;
	private final LoadingFrame frame;
	private final BootstrapperListener listener;

	private JavaProcess process;
	private boolean started;

	public Bootstrapper(String[] args) {
		this.processLauncher = createLauncher(args);
		this.frame = new LoadingFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
				die(0);
			}
		});
		this.listener = new BootstrapperListener();
	}

	public void start() throws IOException {
		if(process != null)
			throw new IllegalStateException("Process is already started");

		log("Starting launcher...");

		process = processLauncher.start();
		process.safeSetExitRunnable(listener);

		frame.setTitle("TLauncher "+ TLauncher.getVersion());
		frame.setVisible(true);
	}

	private void die(int status) {
		log("I can be terminated now.");

		if(!started && process.isRunning()) {
			log("...started instance also will be terminated.");
			log("Ni tebe, ni mne, haha!");
			process.stop();
		}

		System.exit(status);
	}

	private class BootstrapperListener implements JavaProcessListener {
		private StringBuffer buffer = new StringBuffer();

		@Override
		public void onJavaProcessLog(JavaProcess jp, String line) {
			U.plog('>', line);
			buffer.append(line).append('\n');

			if(line.startsWith(LoadingStep.LOADING_PREFIX)) {
				if(line.length() < LoadingStep.LOADING_PREFIX.length() + 2) {
					log("Cannot parse line: content is empty.");
					return;
				}

				String content = line.substring(LoadingStep.LOADING_PREFIX.length() + 1);
				LoadingStep step = Reflect.parseEnum(LoadingStep.class, content);
				if(step == null) {
					log("Cannot parse line: cannot parse step");
					return;
				}

				frame.setProgress(step.percentage);

				if(step.percentage == 100) {
					started = true;

					frame.dispose();
					die(0);
				}
			}
		}

		@Override
		public void onJavaProcessEnded(JavaProcess jp) {
			int exit;

			if((exit = jp.getExitCode()) != 0) {
				Alert.showError(
						"Error starting TLauncher"
						, "TLauncher application was closed with illegal exit code ("+ exit+"). See console:"
						, buffer.toString());
			}

			die(exit);
		}

		@Override
		public void onJavaProcessError(JavaProcess jp, Throwable e) {
		}
	}

	public enum LoadingStep {
		INITALIZING(21),
		LOADING_CONFIGURATION(35),
		LOADING_CONSOLE(41),
		LOADING_MANAGERS(51),
		LOADING_WINDOW(62),
		PREPARING_MAINPANE(77),
		POSTINIT_GUI(82),
		REFRESHING_INFO(91),
		SUCCESS(100);

		public static final String LOADING_PREFIX = "[Loading]";
		public static final String LOADING_DELIMITER = " = ";

		private final int percentage;

		LoadingStep(int percentage) {
			this.percentage = percentage;
		}

		public int getPercentage() {
			return percentage;
		}
	}

	private static String[] loadArgsFromFile(File file) {
		log("Loading arguments from file:", file);

		String content;

		try {
			content = FileUtil.readFile(file);
		} catch(IOException ioE) {
			log("Cannot load arguments from file:", file);
			return null;
		}

		return StringUtils.split(content, ' ');
	}

	private static void log(Object... s) {
		U.log("[Bootstrapper]", s);
	}
}
