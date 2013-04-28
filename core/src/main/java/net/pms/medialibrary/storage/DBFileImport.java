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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pms.medialibrary.commons.dataobjects.DOFileImportTemplate;
import net.pms.medialibrary.commons.dataobjects.DOFileScannerEngineConfiguration;
import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.StorageException;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Package class used to structure code for MediaLibraryStorage
 */
class DBFileImport extends DBBase{
	
	DBFileImport(JdbcConnectionPool cp) {
	    super(cp);
    }
	
	/*********************************************
	 * 
	 * Package Methods
	 * 
	 *********************************************/

	DOFileImportTemplate getFileImportTemplate(int templateId) throws StorageException {
		List<DOFileScannerEngineConfiguration> engines = new ArrayList<DOFileScannerEngineConfiguration>();
		Map<FileType, List<String>> activeEngines = new HashMap<FileType, List<String>>();
		Map<FileType, Map<String, List<String>>> enabledTags;
		String templateName = "";
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = cp.getConnection();

			// get template
			stmt = conn.prepareStatement("SELECT NAME"
					+ " FROM FILEIMPORTTEMPLATE" + " WHERE ID = " + templateId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				templateName = rs.getString(1);
			}

			// get configured engines per FileProperty
			stmt = conn.prepareStatement("SELECT FILEPROPERTY, ENGINENAME, ISENABLED"
							+ " FROM FILEIMPORTTEMPLATEENTRY"
							+ " WHERE TEMPLATEID = ?"
							+ " ORDER BY PRIO ASC");
			stmt.setInt(1, templateId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				FileProperty fp = FileProperty.valueOf(rs.getString(1));
				String engineName = rs.getString(2);
				boolean isEnabled = rs.getBoolean(3);
				
				DOFileScannerEngineConfiguration existingEngine = null;
				for(DOFileScannerEngineConfiguration en : engines) {
					if(en.getFileProperty() == fp){
						existingEngine = en;
						break;
					}
				}
				
				if(existingEngine == null) {
					List<String> engineNames = new ArrayList<String>();
					engineNames.add(engineName);
					DOFileScannerEngineConfiguration newEngine = new DOFileScannerEngineConfiguration(isEnabled, engineNames, fp);
					engines.add(newEngine);
				} else {
					existingEngine.getEngineNames().add(engineName);
				}
			}

			//set the list of active engines per FileType
			activeEngines.put(FileType.VIDEO, getActiveEngines(FileType.VIDEO, templateId, conn, stmt, rs));
			activeEngines.put(FileType.AUDIO, getActiveEngines(FileType.AUDIO, templateId, conn, stmt, rs));
			activeEngines.put(FileType.PICTURES, getActiveEngines(FileType.PICTURES, templateId, conn, stmt, rs));
			
			//set the list of enabled tags
			enabledTags = getEnabledTags(templateId, conn, stmt, rs);
			
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to retrieve file folder for template with id=%s", templateId), se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return new DOFileImportTemplate(templateId, templateName, engines, activeEngines, enabledTags);
	}

	private Map<FileType, Map<String, List<String>>> getEnabledTags(int templateId, Connection conn, PreparedStatement stmt, ResultSet rs) throws SQLException {
		Map<FileType, Map<String, List<String>>> res = new HashMap<FileType, Map<String,List<String>>>(); //key=file type, value={key=engine name, value=tag names
		
		// get configured engines per FileProperty
		stmt = conn.prepareStatement("SELECT FILETYPE, ENGINENAME, TAGNAME"
						+ " FROM FILEIMPORTTEMPLATETAGS"
						+ " WHERE TEMPLATEID = ?");
		stmt.setInt(1, templateId);
		rs = stmt.executeQuery();

		//add the retrieved engines to the result list
		while (rs.next()) {
			FileType ft = FileType.valueOf(rs.getString(1));
			String engineName = rs.getString(2);
			String tagName = rs.getString(3);
			
			//create or resolve the map by engine name
			Map<String, List<String>> tagsMap;
			if(res.containsKey(ft)) {
				//has to be created
				tagsMap = res.get(ft);
			} else {
				//already exists
				tagsMap = new HashMap<String, List<String>>();
				res.put(ft, tagsMap);
			}

			//create or resolve tag names by file type
			List<String> tagNames;
			if(tagsMap.containsKey(engineName)) {
				//already exists
				tagNames = tagsMap.get(engineName);
			} else {
				//has to be created
				tagNames = new ArrayList<String>();
				tagsMap.put(engineName, tagNames);
			}
			tagNames.add(tagName);
		}
		
		return res;
	}

	private List<String> getActiveEngines(FileType fileType, int templateId, Connection conn, PreparedStatement stmt, ResultSet rs) throws SQLException {
		List<String> res = new ArrayList<String>();
		
		// get configured engines per FileProperty
		stmt = conn.prepareStatement("SELECT ENGINENAME"
						+ " FROM FILEIMPORTTEMPLATEACTIVEENGINE"
						+ " WHERE TEMPLATEID = ? AND FILETYPE = ?"
						+ " ORDER BY ENGINENAME ASC");
		stmt.setInt(1, templateId);
		stmt.setString(2, fileType.toString());
		rs = stmt.executeQuery();

		//add the retrieved engines to the result list
		while (rs.next()) {
			res.add(rs.getString(1));
		}
		
		return res;
	}

	List<DOFileImportTemplate> getFileImportTemplates() throws StorageException {
		ArrayList<DOFileImportTemplate> res = new ArrayList<DOFileImportTemplate>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = cp.getConnection();
			
			//get template
			stmt = conn.prepareStatement("SELECT ID, NAME"
			                + " FROM FILEIMPORTTEMPLATE");
			rs = stmt.executeQuery();
			while (rs.next()) {
				int templateId = rs.getInt(1);
				String templateName = rs.getString(2);
				
				//add template
				res.add(new DOFileImportTemplate(templateId, templateName, null, null, null));
			}
			
			for(DOFileImportTemplate template : res) {
				//get configured engines per FileProperty
				stmt = conn.prepareStatement("SELECT FILEPROPERTY, ENGINENAME, ISENABLED"
				                + " FROM FILEIMPORTTEMPLATEENTRY"
				                + " WHERE TEMPLATEID = ?"
				                + " ORDER BY PRIO ASC");
				stmt.setInt(1, template.getId());
				rs = stmt.executeQuery();

				List<DOFileScannerEngineConfiguration> engines = new ArrayList<DOFileScannerEngineConfiguration>();
				while (rs.next()) {
					FileProperty fp = FileProperty.valueOf(rs.getString(1));
					String engineName = rs.getString(2);
					boolean isEnabled = rs.getBoolean(3);
					
					DOFileScannerEngineConfiguration existingEngine = null;
					for(DOFileScannerEngineConfiguration en : engines) {
						if(en.getFileProperty() == fp){
							existingEngine = en;
							break;
						}
					}
					
					if(existingEngine == null) {
						List<String> engineNames = new ArrayList<String>();
						engineNames.add(engineName);
						DOFileScannerEngineConfiguration newEngine = new DOFileScannerEngineConfiguration(isEnabled, engineNames, fp);
						engines.add(newEngine);
					} else {
						existingEngine.getEngineNames().add(engineName);
					}
				}
				template.setEngineConfigurations(engines);

				//set the list of active engines per FileType
				Map<FileType, List<String>> activeEngines = new HashMap<FileType, List<String>>();
				activeEngines.put(FileType.VIDEO, getActiveEngines(FileType.VIDEO, template.getId(), conn, stmt, rs));
				activeEngines.put(FileType.AUDIO, getActiveEngines(FileType.AUDIO, template.getId(), conn, stmt, rs));
				activeEngines.put(FileType.PICTURES, getActiveEngines(FileType.PICTURES, template.getId(), conn, stmt, rs));
				template.setEnabledEngines(activeEngines);
				
				template.setEnabledTags(getEnabledTags(template.getId(), conn, stmt, rs));
			}
			
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed to retrieve file folder for templates"), se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return res;
	}

	public boolean isFileImportTemplateInUse(int templateId) throws StorageException {
		boolean res = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = cp.getConnection();
			
			//get template
			stmt = conn.prepareStatement("SELECT COUNT(VIDEO)"
			                + " FROM MANAGEDFOLDERS"
							+ " WHERE FILEIMPORTTEMPLATEID = ?");
			stmt.setInt(1, templateId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				int nbTemplates = rs.getInt(1);
				res = nbTemplates > 0;
			}			
		} catch (SQLException se) {
			throw new StorageException(String.format("Failed count number of managed folders using the template with id=%s", templateId), se);
		} finally {
			close(conn, stmt, rs);
		}
		
		return res;
	}

	void insertTemplate(DOFileImportTemplate template) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = cp.getConnection();
			
			//insert template
			stmt = conn.prepareStatement("INSERT INTO FILEIMPORTTEMPLATE (NAME) VALUES (?)");
			stmt.setString(1, template.getName());
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys();
			if (rs != null && rs.next()) {
				//update auto generated id
				template.setId(rs.getInt(1));
			}
			
			//insert configured engines
			insertFileImportTemplateEntries(template, conn, stmt);
			
			//insert active engines
			insertFileImportTemplateActiveEngines(template, conn, stmt);

			//insert tags
			insertFileImportTemplateTags(template, conn, stmt);		
		} catch (Exception e) {
			throw new StorageException("Failed to insert import template with name " + template.getName(), e);
		} finally {
			close(conn, stmt, rs);
		}
	}

	void updateTemplate(DOFileImportTemplate template) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();
			
			//update the template
			stmt = conn.prepareStatement("UPDATE FILEIMPORTTEMPLATE SET NAME = ?  WHERE ID = ?");
			stmt.setString(1, template.getName());
			stmt.setInt(2, template.getId());
			stmt.executeUpdate();

			//delete and insert configured engines
			deleteFileImportTemplateEntries(template.getId(), conn, stmt);
			insertFileImportTemplateEntries(template, conn, stmt);

			//delete and insert active engines
			deleteFileImportTemplateActiveEngines(template.getId(), conn, stmt);
			insertFileImportTemplateActiveEngines(template, conn, stmt);

			//delete and insert tags
			deleteFileImportTemplateTags(template.getId(), conn, stmt);
			insertFileImportTemplateTags(template, conn, stmt);
			
		} catch (SQLException ex) {
			throw new StorageException(String.format("Failed to update template '%s' with id=%s", template.getName(), template.getId()), ex);
		}
	}

	void deleteFileImportTemplate(int templateId) throws StorageException {
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = cp.getConnection();	
			
			//delete the configured engines
			deleteFileImportTemplateEntries(templateId, conn, stmt);

			//delete the active engines
			deleteFileImportTemplateActiveEngines(templateId, conn, stmt);

			//delete the active engines
			deleteFileImportTemplateTags(templateId, conn, stmt);
			
			//delete the template
			stmt = conn.prepareStatement("DELETE FROM FILEIMPORTTEMPLATE WHERE ID = ?");
			stmt.setInt(1, templateId);
			stmt.execute();
		} catch (SQLException e) {
			throw new StorageException("Failed to delete file template entry with id=" + templateId, e);
        } finally {
			close(conn, stmt);
        }
	}
	
	/*********************************************
	 * 
	 * Private Methods
	 * 
	 *********************************************/
	
	private void insertFileImportTemplateEntries(DOFileImportTemplate template, Connection conn, PreparedStatement stmt) throws SQLException {		
		for(DOFileScannerEngineConfiguration engine : template.getEngineConfigurations()) {
			int prio = 0;
			for(String engineName : engine.getEngineNames()) {
				stmt = conn.prepareStatement("INSERT INTO FILEIMPORTTEMPLATEENTRY (TEMPLATEID, FILEPROPERTY, ENGINENAME, PRIO, ISENABLED)"
		                + "VALUES (?, ?, ?, ?, ?)");
				stmt.setInt(1, template.getId());
				stmt.setString(2, engine.getFileProperty().toString());
				stmt.setString(3, engineName);	
				stmt.setInt(4, prio++);
				stmt.setBoolean(5, engine.isEnabled());
				stmt.executeUpdate();				
			}
		}		
	}
	
	private void insertFileImportTemplateActiveEngines(DOFileImportTemplate template, Connection conn, PreparedStatement stmt) throws SQLException {		
		for(FileType ft : template.getEnabledEngines().keySet()) {
			List<String> engineNames = template.getEnabledEngines().get(ft);
			for(String engineName : engineNames) {
				stmt = conn.prepareStatement("INSERT INTO FILEIMPORTTEMPLATEACTIVEENGINE (TEMPLATEID, FILETYPE, ENGINENAME)"
		                + "VALUES (?, ?, ?)");
				stmt.setInt(1, template.getId());
				stmt.setString(2, ft.toString());
				stmt.setString(3, engineName);		
				stmt.executeUpdate();				
			}
		}		
	}

	private void insertFileImportTemplateTags(DOFileImportTemplate template,
			Connection conn, PreparedStatement stmt) throws SQLException {
		for (FileType ft : template.getEnabledTags().keySet()) {
			Map<String, List<String>> engineTags = template.getEnabledTags().get(ft);
			for (String engineName : engineTags.keySet()) {
				for (String tagName : engineTags.get(engineName)) {
					stmt = conn.prepareStatement("INSERT INTO FILEIMPORTTEMPLATETAGS (TEMPLATEID, FILETYPE, ENGINENAME, TAGNAME)"
									+ "VALUES (?, ?, ?, ?)");
					stmt.setInt(1, template.getId());
					stmt.setString(2, ft.toString());
					stmt.setString(3, engineName);
					stmt.setString(4, tagName);
					stmt.executeUpdate();
				}
			}
		}
	}
	
	private void deleteFileImportTemplateEntries(int templateId, Connection conn, PreparedStatement stmt) throws SQLException{
		stmt = conn.prepareStatement("DELETE FROM FILEIMPORTTEMPLATEENTRY WHERE TEMPLATEID = ?");
		stmt.setInt(1, templateId);
		stmt.execute();
	}
	
	private void deleteFileImportTemplateActiveEngines(int templateId, Connection conn, PreparedStatement stmt) throws SQLException{
		stmt = conn.prepareStatement("DELETE FROM FILEIMPORTTEMPLATEACTIVEENGINE WHERE TEMPLATEID = ?");
		stmt.setInt(1, templateId);
		stmt.execute();
	}

	private void deleteFileImportTemplateTags(int templateId, Connection conn, PreparedStatement stmt) throws SQLException {
		stmt = conn.prepareStatement("DELETE FROM FILEIMPORTTEMPLATETAGS WHERE TEMPLATEID = ?");
		stmt.setInt(1, templateId);
		stmt.execute();
		
	}
}