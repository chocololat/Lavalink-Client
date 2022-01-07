package lavalink.client.player.track;

import java.io.IOException;

public class DefaultTrack implements Track {

	private String trackBase64;
	private TrackInfo info;
	private Object userData;

	public DefaultTrack(String trackBase64, TrackInfo info) {
		this.trackBase64 = trackBase64;
		this.info = info;
	}

	public DefaultTrack(String trackBase64, TrackInfo info, Object userData) {
		this.trackBase64 = trackBase64;
		this.info = info;
		this.userData = userData;
	}

	public DefaultTrack(DefaultTrackInfo info) {
		this.info = info;
	}

	public DefaultTrack(String trackBase64) {
		this.trackBase64 = trackBase64;
	}

	public DefaultTrack(String trackBase64, Track oldTrack) {
		this.trackBase64 = trackBase64;
		if (oldTrack != null && oldTrack.getUserData() != null) {
			this.userData = oldTrack.getUserData();
		}
	}

	public String getTrack() {
		if (this.trackBase64 == null) {
			try {
				this.trackBase64 = Track.encode(this.info);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return this.trackBase64;
	}

	public TrackInfo getInfo() {
		if (this.info == null) {
			try {
				this.info = Track.decode(this.trackBase64);
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
