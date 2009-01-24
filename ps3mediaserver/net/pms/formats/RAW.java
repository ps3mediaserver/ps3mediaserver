package net.pms.formats;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.encoders.Player;
import net.pms.encoders.RAWThumbnailer;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

public class RAW extends JPG {
	
	@Override
	public String [] getId() {
		return new String [] { "arw", "cr2", "crw", "dng", "raf", "mrw", "nef", "pef", "srf", "orf" }; //$NON-NLS-1$
	}
	
	@Override
	public boolean ps3compatible() {
		return false;
	}

	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		ArrayList<Class<? extends Player>> profiles = new ArrayList<Class<? extends Player>>();
		for(String engine:PMS.getConfiguration().getEnginesAsList(PMS.get().getRegistry()))
			if (engine.equals(RAWThumbnailer.ID))
				profiles.add(RAWThumbnailer.class);
		return profiles;
	}

	@Override
	public boolean transcodable() {
		return true;
	}

	@Override
	public void parse(DLNAMediaInfo media, File file, int type) {
		
		try {
			
			OutputParams params = new OutputParams(PMS.getConfiguration());
			params.waitbeforestart = 1;
			params.minBufferSize = 1;
			params.maxBufferSize = 20;
			params.hidebuffer = true;
			
			
			String cmdArray [] = new String [4];
			cmdArray[0] = PMS.getConfiguration().getDCRawPath();
			cmdArray[1] = "-i";
			cmdArray[2] = "-v";
			cmdArray[3] = file.getAbsolutePath();
			
			params.log = true;
			ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
			pw.run();
			
			List<String> list = pw.getOtherResults();
			for(String s:list) {
				if (s.startsWith("Thumb size:  ")) {
					String sz = s.substring(13);
					media.width = Integer.parseInt(sz.substring(0, sz.indexOf("x")).trim());
					media.height = Integer.parseInt(sz.substring(sz.indexOf("x")+1).trim());
				}
			}
			
		
			
			if (media.width > 0) {
				
				
				params.log = false;
				
				cmdArray = new String [4];
				cmdArray[0] = PMS.getConfiguration().getDCRawPath();
				cmdArray[1] = "-e";
				cmdArray[2] = "-c";
				cmdArray[3] = file.getAbsolutePath();
				pw = new ProcessWrapperImpl(cmdArray, params);
				pw.run();
			
			
				InputStream is = pw.getInputStream(0);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int n = -1;
				byte buffer [] = new byte [4096];
				while ((n=is.read(buffer)) > -1) {
					baos.write(buffer, 0, n);
				}
				is.close();
				media.thumb = baos.toByteArray();
				media.size = media.thumb.length;
				baos.close();
				
				
			}
			
			media.finalize(type);
			media.mediaparsed = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
}
