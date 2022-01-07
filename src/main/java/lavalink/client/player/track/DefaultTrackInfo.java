package lavalink.client.player.track;

import org.json.JSONObject;

public class DefaultTrackInfo implements TrackInfo {

	private final String title;
	private final String author;
	private final long length;
	private final String identifier;
	private final boolean isStream;
	private final String uri;
	private final String sourceName;

	public DefaultTrackInfo(String title, String author, long length, String identifier, boolean isStream, String uri, String sourceName) {
		this.title = title;
		this.author = author;
		this.length = length;
		this.identifier = identifier;
		this.isStream = isStream;
		this.uri = uri;
		this.sourceName = sourceName;
	}

	public static TrackInfo fromJSON(JSONObject json) {
		return new DefaultTrackInfo(json.getString("title"), json.getString("author"), json.getLong("length"), json.getString("identifier"), json.getBoolean("isStream"), json.getString("uri"), json.getString("sourceName"));
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getAuthor() {
		return this.author;
	}

	@Override
	public long getLength() {
		return this.length;
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public boolean isStream() {
		return this.isStream;
	}

	@Override
	public String getUri() {
		return this.uri;
	}

	@Override
	public String getSourceName() {
		return this.sourceName;
	}

}