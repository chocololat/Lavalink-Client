package lavalink.client.player.track;

import java.util.List;

public class DefaultPlaylist implements Playlist {

	private final String name;
	private final List<Track> tracks;
	private final int selectedTrack;

	public DefaultPlaylist(String name, List<Track> tracks, int selectedTrack) {
		this.name = name;
		this.tracks = tracks;
		this.selectedTrack = selectedTrack;
	}

	public String getName() {
		return this.name;
	}

	public List<Track> getTracks() {
		return this.tracks;
	}

	public Track getSelectedTrack() {
		if (this.selectedTrack == -1) {
			return null;
		}
		return this.tracks.get(selectedTrack);
	}
}
