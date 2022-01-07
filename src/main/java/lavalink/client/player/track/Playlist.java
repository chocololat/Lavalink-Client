package lavalink.client.player.track;

import java.util.List;

public interface Playlist {

	String getName();

	List<Track> getTracks();

	Track getSelectedTrack();
}
