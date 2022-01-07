package lavalink.client.player.track;

import java.util.List;

public class DefaultAudioPlaylist implements AudioPlaylist {

	private final String name;
	private final List<AudioTrack> tracks;
	private final int selectedTrack;

	public DefaultAudioPlaylist(String name, List<AudioTrack> tracks, int selectedTrack) {
		this.name = name;
		this.tracks = tracks;
		this.selectedTrack = selectedTrack;
	}

	public String getName() {
		return this.name;
	}

	public List<AudioTrack> getTracks() {
		return this.tracks;
	}

	public AudioTrack getSelectedTrack() {
		if (this.selectedTrack == -1) {
			return null;
		}
		return this.tracks.get(selectedTrack);
	}
}
