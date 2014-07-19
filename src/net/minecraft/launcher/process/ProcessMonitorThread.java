package net.minecraft.launcher.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

class ProcessMonitorThread extends Thread {
	private final JavaProcess process;

	public ProcessMonitorThread(JavaProcess process) {
		this.process = process;
	}

	@Override
	public void run() {
		Process raw = this.process.getRawProcess();
		InputStreamReader reader = new InputStreamReader(raw.getInputStream());
		BufferedReader buf = new BufferedReader(reader);
		String line = null;

		JavaProcessListener listener = this.process.getExitRunnable();

		while (this.process.isRunning()) {
			try {
				while ((line = buf.readLine()) != null) {
					if (listener != null)
						listener.onJavaProcessLog(process, line);
					this.process.getSysOutLines().add(line);
				}
			} catch (IOException ex) {
				// Do nothing?
			} finally {
				try {
					buf.close();
				} catch (IOException ex) {
					Logger.getLogger(ProcessMonitorThread.class.getName()).log(
							Level.SEVERE, null, ex);
				}
			}
		}

		if (listener != null)
			listener.onJavaProcessEnded(this.process);

	}
}