/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
