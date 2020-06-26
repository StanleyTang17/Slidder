package game;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

public class SoundEffect {
	
	private static boolean MUTED;
	
	private File audioFile;
	private Clip audioClip;
	
	public SoundEffect(File audioFile) {
		this.audioFile = audioFile;
	}
	
	public void play() {
		if(MUTED) return;
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			audioClip = AudioSystem.getClip();
			audioClip.open(audioStream);
			audioClip.setFramePosition(0);
			audioClip.start();
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		if(audioClip != null) audioClip.stop();
	}
	
	//getter
	public static boolean isMuted() {
		return MUTED;
	}
	
	//setter
	public static void setMuted(boolean muted) {
		MUTED = muted;
	}
	
}
