package net.pms.update;

class Version {
	private final int[] elements;
	
	Version(String versionNumberAsString) {
		elements = parseNumbers(separateElements(versionNumberAsString));
	}

	private static int[] parseNumbers(String[] elements) {
		int[] out = new int[elements.length];
		for (int i = 0; i < elements.length; i++) {
			try {
				out[i] = Integer.parseInt(elements[i]);
			} catch (NumberFormatException e) {
				out[i] = 0;
			}
		}
		return out;
	}

	private static String[] separateElements(String versionNumberAsString) {
		if (versionNumberAsString != null) {
			return versionNumberAsString.split("\\.");
		} else {
			return new String[0];
		}
	}
	
	public boolean isGreaterThan(Version other) {
		for (int i = 0; i < Math.min(elements.length, other.elements.length); i++) {
			if (elements[i] > other.elements[i]) {
				return true;
			} else if (elements[i] < other.elements[i]) {
				return false;
			}
		}
		return elements.length > other.elements.length;
	}
	
	// TODO(tcox):  Test suite
	public static void main(String[] args) {
		assert(new Version("2").isGreaterThan(new Version("1")));
		assert(!new Version("1").isGreaterThan(new Version("2")));
		assert(new Version("1.2.3").isGreaterThan(new Version("1.2.2")));
		assert(!new Version("1.2.2").isGreaterThan(new Version("1.2.3")));
		assert(!new Version("1.2.2").isGreaterThan(new Version("1.2.2")));
		assert(new Version("1.03").isGreaterThan(new Version("1.02.1")));
		assert(!new Version("1.02.1").isGreaterThan(new Version("1.03")));
	}
	
	@Override
	public String toString() {
		String out = "";
		for (int i = 0; i < elements.length; i++) {
			out += elements[i];
			if (i != elements.length - 1) {
				out += ".";
			}
		}
		return out;
	}
}
