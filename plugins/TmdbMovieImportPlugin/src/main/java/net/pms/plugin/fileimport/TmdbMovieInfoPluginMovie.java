package net.pms.plugin.fileimport;

import net.sf.jtmdb.Movie;

public class TmdbMovieInfoPluginMovie {
	private Movie movie;
	
	public TmdbMovieInfoPluginMovie(Movie movie) {
		this.movie = movie;
	}

	public Movie getMovie() {
		return movie;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public String toString() {
		String res = "";
		if(movie != null) {
			String.format("%s (%s)", movie.getName(), movie.getReleasedDate().getYear());
		}
		return res;
	}
}
