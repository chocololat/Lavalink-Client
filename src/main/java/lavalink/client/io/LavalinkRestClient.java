/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 * Copyright (c) 2020 Callum Jay Seabrook
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.client.io;

import edu.umd.cs.findbugs.annotations.NonNull;
import lavalink.client.player.track.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class LavalinkRestClient {

	private static final Logger log = LoggerFactory.getLogger(LavalinkRestClient.class);

	private static final String YOUTUBE_SEARCH_PREFIX = "ytsearch:";
	private static final String SOUNDCLOUD_SEARCH_PREFIX = "scsearch:";

	private final LavalinkSocket socket;
	private Consumer<HttpClientBuilder> builderConsumer;

	private final HttpClient httpClient = buildClient();

	LavalinkRestClient(final LavalinkSocket socket) {
		this.socket = socket;
	}

	public void setHttpClientBuilder(final Consumer<HttpClientBuilder> clientBuilder) {
		this.builderConsumer = clientBuilder;
	}

	/**
	 * Retrieves a search result from Lavalink's Track Loading API (which retrieves it from
	 * YouTube in this case) and uses the {@code SEARCH_TRANSFORMER search transformer} to
	 * transform it in to a list of {@code AudioTrack audio tracks}
	 *
	 * @param query the search query to give to the REST API
	 * @return a list of YouTube search results as {@code AudioTrack audio tracks}
	 */
	@NonNull
	public CompletableFuture<List<Track>> getYoutubeSearchResult(final String query) {
		return load(YOUTUBE_SEARCH_PREFIX + query)
				.thenApplyAsync(LavalinkRestClient::transformSearchResult);
	}

	/**
	 * Retrieves a search result from Lavalink's Track Loading API (which retrieves it from
	 * SoundCloud in this case) and uses the {@code SEARCH_TRANSFORMER search transformer}
	 * to transform it in to a list of {@code AudioTrack audio tracks}
	 *
	 * @param query the search query to give to the REST API
	 * @return a list of SoundCloud search results as {@code AudioTrack audio tracks}
	 */
	@NonNull
	public CompletableFuture<List<Track>> getSoundcloudSearchResult(final String query) {
		return load(SOUNDCLOUD_SEARCH_PREFIX + query)
				.thenApplyAsync(LavalinkRestClient::transformSearchResult);
	}

	/**
	 * Loads a track from Lavalink's Track Loading API and sends the results to the provided
	 * {@code AudioLoadResultHandler callback} to handle them
	 *
	 * @param identifier the identifier for the track
	 * @param callback   the result handler that will handle the result of the load
	 */
	@NonNull
	public CompletableFuture<Void> loadItem(final String identifier, final LoadResultHandler callback) {
		return load(identifier)
				.thenAcceptAsync(loadResult -> consumeCallback(loadResult, callback));
	}

	private void consumeCallback(final JSONObject loadResult, final LoadResultHandler callback) {
		if (loadResult == null) {
			callback.noMatches();
			return;
		}

		try {
			final String loadType = loadResult.getString("loadType");
			final TrackLoadResultHandler trackLoadResultHandler = new TrackLoadResultHandler(loadResult);

			switch (loadType) {
				case "TRACK_LOADED":
					callback.trackLoaded(trackLoadResultHandler.handleTrackLoaded());
					break;
				case "PLAYLIST_LOADED":
					callback.playlistLoaded(trackLoadResultHandler.handlePlaylistLoaded());
					break;
				case "NO_MATCHES":
					callback.noMatches();
					break;
				case "LOAD_FAILED":
					callback.loadFailed(trackLoadResultHandler.handleLoadFailed());
					break;
				case "SEARCH_RESULT":
					callback.searchResultLoaded(trackLoadResultHandler.handleSearchResultLoaded());
					break;
				default:
					throw new IllegalArgumentException("Invalid loadType: " + loadType);
			}
		} catch (final Exception exception) {
			callback.loadFailed(new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception));
		}
	}

	private CompletableFuture<JSONObject> load(final String identifier) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final String requestURL = buildBaseAddress() + URLEncoder.encode(identifier, "UTF-8");
				return apiGet(requestURL, socket.getPassword());
			} catch (final Throwable exception) {
				log.error("Failed to load track with identifier " + identifier, exception);
			}

			return null;
		});
	}

	private static List<Track> transformSearchResult(JSONObject loadResult) {
		return loadResult.getJSONArray("tracks").toList().stream().map(track ->
				new DefaultTrack(new JSONObject((Map<?, ?>) track).getString("track"))
		).collect(Collectors.toList());
	}

	private String buildBaseAddress() {
		return socket.getRemoteUri().toString()
				.replaceFirst("ws://", "http://")
				.replaceFirst("wss://", "https://")
				.concat("/loadtracks?identifier=");
	}

	private HttpClient buildClient() {
		final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		if (builderConsumer == null) return httpClientBuilder.build();

		builderConsumer.accept(httpClientBuilder);
		return httpClientBuilder.build();
	}

	private JSONObject apiGet(final String url, final String auth) throws IOException {
		final HttpGet request = new HttpGet(url);
		request.addHeader(HttpHeaders.AUTHORIZATION, auth);

		final HttpResponse httpResponse = httpClient.execute(request);
		final int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != 200) throw new IOException("Invalid API Request Status Code: " + statusCode);

		final HttpEntity entity = httpResponse.getEntity();
		if (entity == null) throw new IOException("Invalid API Response: No Content");

		final String response = EntityUtils.toString(entity);
		return new JSONObject(response);
	}

	private static final class TrackLoadResultHandler {

		private final JSONObject loadResult;

		private TrackLoadResultHandler(JSONObject loadResult) {
			this.loadResult = loadResult;
		}

		private Track handleTrackLoaded() {
			JSONObject trackObject = loadResult.getJSONArray("tracks").getJSONObject(0);
			String singleTrackBase64 = trackObject.getString("track");
			TrackInfo trackInfo = DefaultTrackInfo.fromJSON(trackObject.getJSONObject("info"));

			return new DefaultTrack(singleTrackBase64, trackInfo);
		}

		private Playlist handlePlaylistLoaded() {
			JSONArray trackData = loadResult.getJSONArray("tracks");
			final List<Track> tracks = new ArrayList<>();

			for (Object track : trackData) {
				String trackBase64 = ((JSONObject) track).getString("track");
				TrackInfo trackInfo = DefaultTrackInfo.fromJSON(((JSONObject) track).getJSONObject("info"));

				tracks.add(new DefaultTrack(trackBase64, trackInfo));
			}

			if (tracks.size() == 0) {
				throw new FriendlyException("Playlist is empty", FriendlyException.Severity.SUSPICIOUS, new IllegalStateException("Empty playlist"));
			}

			final JSONObject playlistInfo = loadResult.getJSONObject("playlistInfo");

			return new DefaultPlaylist(playlistInfo.getString("name"), tracks, playlistInfo.optInt("selectedTrack", -1));
		}

		private List<Track> handleSearchResultLoaded() {
			JSONArray trackData = loadResult.getJSONArray("tracks");
			List<Track> tracks = new ArrayList<>();

			for (Object track : trackData) {
				String trackBase64 = ((JSONObject) track).getString("track");
				TrackInfo trackInfo = DefaultTrackInfo.fromJSON(((JSONObject) track).getJSONObject("info"));

				tracks.add(new DefaultTrack(trackBase64, trackInfo));
			}

			if (tracks.size() == 0) {
				throw new FriendlyException("No search results found", FriendlyException.Severity.SUSPICIOUS, new IllegalStateException("No results"));
			}
			return tracks;
		}

		private FriendlyException handleLoadFailed() {
			JSONObject exception = loadResult.getJSONObject("exception");
			String message = exception.getString("message");
			FriendlyException.Severity severity = FriendlyException.Severity.valueOf(exception.getString("severity"));

			return new FriendlyException(message, severity, new Throwable());
		}
	}
}
