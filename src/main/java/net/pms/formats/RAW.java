package net.pms.formats;

import java.util.ArrayList;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.InputFile;
import net.pms.encoders.Player;
import net.pms.encoders.RAWThumbnailer;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

public class RAW extends JPG {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getId() {
		return new String[] { "arw", "cr2", "crw", "dng", "raf", "mrw", "nef",
				"pef", "srf", "orf" };
	}

	/**
	 * @deprecated Use {@link #isCompatible(DLNAMediaInfo, RendererConfiguration)} instead.
	 * <p>
	 * Returns whether or not a format can be handled by the PS3 natively.
	 * This means the format can be streamed to PS3 instead of having to be
	 * transcoded.
	 * 
	 * @return True if the format can be handled by PS3, false otherwise.
	 */
	@Deprecated
	@Override
	public boolean ps3compatible() {
		return false;
	}

	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		ArrayList<Class<? extends Player>> profiles = new ArrayList<Class<? extends Player>>();
		for (String engine : PMS.getConfiguration().getEnginesAsList(PMS.get().getRegistry())) {
			if (engine.equals(RAWThumbnailer.ID)) {
				profiles.add(RAWThumbnailer.class);
			}
		}
		return profiles;
	}

	@Override
	public boolean transcodable() {
		return true;
	}

	@Override
	public void parse(DLNAMediaInfo media, InputFile file, int type, RendererConfiguration renderer) {
		try {
			OutputParams params = new OutputParams(PMS.getConfiguration());
			params.waitbeforestart = 1;
			params.minBufferSize = 1;
			params.maxBufferSize = 5;
			params.hidebuffer = true;


			String cmdArray[] = new String[4];
			cmdArray[0] = PMS.getConfiguration().getDCRawPath();
			cmdArray[1] = "-i";
			cmdArray[2] = "-v";
			if (file.getFile() != null) {
				cmdArray[3] = file.getFile().getAbsolutePath();
			}

			params.log = true;
			ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params, true, false);
			pw.runInSameThread();

			List<String> list = pw.getOtherResults();
			for (String s : list) {
				if (s.startsWith("Thumb size:  ")) {
					String sz = s.substring(13);
					media.setWidth(Integer.parseInt(sz.substring(0, sz.indexOf("x")).trim()));
					media.setHeight(Integer.parseInt(sz.substring(sz.indexOf("x") + 1).trim()));
				}
			}

			if (media.getWidth() > 0) {

				media.setThumb(RAWThumbnailer.getThumbnail(params, file.getFile().getAbsolutePath()));
				if (media.getThumb() != null) {
					media.setSize(media.getThumb().length);
				}

				media.setCodecV("jpg");
				media.setContainer("jpg");
			}

			media.finalize(type, file);
			media.setMediaparsed(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* 
	 * Force this format to be transcoded (RAW support broken in rev 409 and earlier)
	 * @see net.pms.formats.Format#skip(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean skip(String extensions, String anotherSetOfExtensions) {
		return true;
	}
}
