package lavalink.client.player.track;

import java.io.IOException;

public class DefaultAudioTrack implements AudioTrack {

	private String trackBase64;
	private AudioTrackInfo info;
	private Object userData;

	public DefaultAudioTrack(String trackBase64, AudioTrackInfo info) {
		this.trackBase64 = trackBase64;
		this.info = info;
	}

	public DefaultAudioTrack(String trackBase64, AudioTrackInfo info, Object userData) {
		this.trackBase64 = trackBase64;
		this.info = info;
		this.userData = userData;
	}

	public DefaultAudioTrack(DefaultAudioTrackInfo info) {
		this.info = info;
	}

	public DefaultAudioTrack(String trackBase64) {
		this.trackBase64 = trackBase64;
	}

	public DefaultAudioTrack(String trackBase64, AudioTrack oldTrack) {
		this.trackBase64 = trackBase64;
		if (oldTrack != null && oldTrack.getUserData() != null) {
			this.userData = oldTrack.getUserData();
		}
	}

	public String getTrack() {
		if (this.trackBase64 == null) {
			try {
				this.trackBase64 = AudioTrack.encode(this.info);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return this.trackBase64;
	}

	public AudioTrackInfo getInfo() {
		if (this.info == null) {
			try {
				this.info = AudioTrack.decode(this.trackBase64);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return this.info;
	}

	public void setUserData(Object userData) {
		this.userData = userData;
	}

	public Object getUserData() {
		return this.userData;
	}

	public <T> T getUserData(Class<T> klass) {
		//noinspection unchecked
		return this.userData == null ? null : (T) this.userData;
	}
}
