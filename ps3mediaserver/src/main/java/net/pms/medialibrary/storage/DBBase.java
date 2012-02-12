package net.pms.medialibrary.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DBBase {
	private static final Logger log = LoggerFactory.getLogger(DBBase.class);
	JdbcConnectionPool cp;

	DBBase(JdbcConnectionPool cp) {
		this.cp = cp;
	}

	static void close(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			log.error("Error while closing result set", ex);
		} finally {
			rs = null;
		}
		
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException ex) {
			log.error("Error while closing prepared statemenet", ex);
		} finally {
			stmt = null;
		}
		
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException ex) {
			log.error("Error while closingconnection", ex);
		} finally {
			conn = null;
		}
	}
	
	static void close(Connection conn, Statement stmt) {
		close(conn, stmt, null);
	}
	
	static void close(ResultSet rs) {
		close(null, null, rs);
	}
	
	static void close(Statement stmt, ResultSet rs) {
		close(null, stmt, rs);
	}

}
