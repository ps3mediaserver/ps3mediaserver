package net.pms.plugin.fileimport;

import java.util.Calendar;

import com.savvasdalkitsis.jtmdb.Movie;

public class TmdbMovieInfoPluginMovie {
	private Movie movie;
	
	public TmdbMovieInfoPluginMovie(Movie movie) {
		this.movie = movie;
	}

	public Movie getMovie() {
		return movie;
	}
	
	@Override
	public String toString() {
		String res = "";
		if(movie != null) {
			String yearString = "";
			if(movie.getReleasedDate() != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(movie.getReleasedDate());
				yearString = String.format(" (%s)", cal.get(Calendar.YEAR));
			}
			res = String.format("%s%s", movie.getName(), yearString);
		}
		return res;
	}
}
