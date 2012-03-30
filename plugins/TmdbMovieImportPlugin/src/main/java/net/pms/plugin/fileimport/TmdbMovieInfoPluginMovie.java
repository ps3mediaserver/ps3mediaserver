package net.pms.plugin.fileimport;

import java.util.Calendar;

import net.sf.jtmdb.Movie;

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
			Calendar cal = Calendar.getInstance();
			cal.setTime(movie.getReleasedDate());
			res = String.format("%s (%s)", movie.getName(), movie.getReleasedDate() == null ? null : cal.get(Calendar.YEAR));
		}
		return res;
	}
}
