package ru.turikhay.tlauncher;

import ru.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.console.Console.CloseAction;
import joptsimple.OptionSet;

public class TLauncherLite implements MinecraftListener {
	private final TLauncher tlauncher;
	private final OptionSet args;

	TLauncherLite(TLauncher tlauncher) {
		if (tlauncher == null)
			throw new NullPointerException();

		this.tlauncher = tlauncher;
		tlauncher.getVersionManager().startRefresh(true);
		tlauncher.getProfileManager().refreshComponent();

		this.args = tlauncher.getArguments();

		MinecraftLauncher launcher = new MinecraftLauncher(this, args);
		launcher.addListener(tlauncher.getMinecraftListener());
		launcher.addListener(this);

		if (launcher.getConsole() != null)
			launcher.getConsole().setCloseAction(CloseAction.EXIT);

		launcher.start();
	}

	public TLauncher getLauncher() {
		return tlauncher;
	}

	@Override
	public void onMinecraftPrepare() {
	}

	@Override
	public void onMinecraftAbort() {
	}

	@Override
	public void onMinecraftLaunch() {
	}

	@Override
	public void onMinecraftClose() {
		if (!args.has("console")
				&& tlauncher.getSettings().getConsoleType()
						.equals(ConsoleType.NONE))
			TLauncher.kill();
	}

	@Override
	public void onMinecraftError(Throwable e) {
	}

	@Override
	public void onMinecraftKnownError(MinecraftException e) {
	}

	@Override
	public void onMinecraftCrash(Crash crash) {
	}

}
