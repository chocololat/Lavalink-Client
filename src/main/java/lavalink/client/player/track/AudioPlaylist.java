package lavalink.client.player.track;

import java.util.List;

public interface AudioPlaylist {

	String getName();

	List<AudioTrack> getTracks();

	AudioTrack getSelectedTrack();
}
