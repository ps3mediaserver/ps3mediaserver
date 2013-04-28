package net.pms.plugin.fileimport.imdb;

import org.json.JSONException;
import org.json.JSONObject;

public class FileSearchObject {
	private JSONObject jsonObject;
	
	public FileSearchObject(JSONObject jsonObject) {
		setJsonObject(jsonObject);
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	
	@Override
	public String toString() {
		String res = "";
		try {
			Object elem = jsonObject.get("Title");
			if(elem != null) {
				res = elem.toString();
			}
		} catch (JSONException e) {
			//do nothing
		}
		
		try {
			Object elem = jsonObject.get("Released");
			if(elem != null) {
				String dStr = elem.toString();
				if(dStr.length() > 3) {
					res += String.format(" (%s)", dStr.substring(dStr.length() - 4, dStr.length()));
				}
			}
		} catch (JSONException e) {
			//do nothing
		}
		return res;
	}
}
