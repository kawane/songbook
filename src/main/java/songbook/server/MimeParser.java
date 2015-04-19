package songbook.server;

import java.util.*;

/**
 * 
 * Utility class to find the best match mime type.
 * see Header Accept chapter of rfc2616
 * @author llgcode
 *
 */
public class MimeParser {
	static class MimeTypePattern {
		final String acceptedMimeType;
		final Map<String, String> parameters;
		protected MimeTypePattern(String acceptedMimeType, Map<String, String> parameters) {
			this.acceptedMimeType = acceptedMimeType;
			this.parameters = parameters;
		}
		
	}
	/**
	 * Does the mimeType match with the mimeTypePattern
	 *  
	 * 
	 * @param mimeType
	 * @param acceptedMimeType
	 * @return
	 */
	public static boolean match(String mimeType, String acceptedMimeType) {
		final String[] typeSubType1 = mimeType.split("/");
		final String[] typeSubType2 = acceptedMimeType.split("/");
		if (typeSubType1.length == 2 && typeSubType2.length == 2) {
			final String type1 = typeSubType1[0].trim();
			final String subType1 = typeSubType1[1].trim();
			final String type2 = typeSubType2[0].trim();
			final String subType2 = typeSubType2[1].trim();
			if ("*".equals(type2) || type1.equals(type2)) {
				return "*".equals(subType2) || subType1.equals(subType2);
			}
		}
		return false;
	}
	/**
	 * Returns a list of mimetype patterns ordered by priority (see Header Accept chapter of rfc2616)
	 * 
	 * @param acceptHeaderMimeType
	 * @return
	 */
	public static String[] getMimeTypesByPriority(String acceptHeaderMimeType) {
		final String[] mimetypePatternsWithParams = acceptHeaderMimeType.split(",");
		final List<MimeTypePattern> mimeTypes = new ArrayList<MimeTypePattern>();
		for (String m : mimetypePatternsWithParams) {
			final String[] mimetypePatternWithParams = m.split(";");
			if (mimetypePatternWithParams.length > 0) {
				final String acceptedMimeType = mimetypePatternWithParams[0];
				final Map<String, String> parameters = new HashMap<String, String>();
				for (int i = 1; i < mimetypePatternWithParams.length; i++) {
					final String[] paramsPair = mimetypePatternWithParams[1].split("=");
					if (paramsPair.length == 1) {
						parameters.put(paramsPair[0].trim(), "");
					} else if (paramsPair.length == 2) {
						parameters.put(paramsPair[0].trim(), paramsPair[1].trim());
					}
				}
				if (!parameters.containsKey("q")) {
					parameters.put("q", "1");
				}
				mimeTypes.add(new MimeTypePattern(acceptedMimeType.trim(), parameters));
			}
		}
		Collections.sort(mimeTypes, new Comparator<MimeTypePattern>() {
			@Override
			public int compare(MimeTypePattern m1, MimeTypePattern m2) {
				float q1 = Float.parseFloat(m1.parameters.get("q"));
				float q2 = Float.parseFloat(m2.parameters.get("q"));
				if (q1 == q2) {
					if (m1.acceptedMimeType.equals(m2.acceptedMimeType)) {
						if (m1.parameters.size() == m2.parameters.size()) {
							return 0;
						}
						return m1.parameters.size() > m2.parameters.size() ? -1 : 1;
					}
					if (m1.acceptedMimeType.contains("*")) {
						return 1;
					}
					return 1;
				}
				return q1 < q2 ? 1 : -1;
			}
		});
		String[] mimeTypesStr= new String[mimeTypes.size()];
		for (int i = 0; i < mimeTypesStr.length; i++) {
			mimeTypesStr[i] = mimeTypes.get(i).acceptedMimeType;
		}
		return mimeTypesStr;
	}
	
	/**
	 * Returns the best match mime type comparing supported mime types and accepted mime types (see HTTP Header Accept).
	 * If no match is found return the first supported mime type of the array 
	 * @param acceptHeaderMimeType
	 * @param supportedMimeTypes
	 * @return
	 */
	public static String bestMatch(String acceptHeaderMimeType, String... supportedMimeTypes) {
		if (acceptHeaderMimeType == null || acceptHeaderMimeType.length() == 0) {
			return supportedMimeTypes[0];
		}
		String[] mimeTypesByPriority = getMimeTypesByPriority(acceptHeaderMimeType);
		for (String acceptedMimeType : mimeTypesByPriority) {
			for (String supportedMimeType : supportedMimeTypes) {
				if (match(supportedMimeType, acceptedMimeType)) {
					return supportedMimeType;
				}
			}
		}
		return supportedMimeTypes[0];
	}

}
