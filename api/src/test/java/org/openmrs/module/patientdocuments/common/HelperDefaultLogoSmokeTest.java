/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientdocuments.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.util.Base64;

/**
 * Standalone (no OpenMRS test context) smoke check that the module's bundled default
 * logo classpath resource loads correctly, without going through Spring/Hibernate
 * bootstrap. Not a substitute for a full context test; kept only to sanity-check this
 * change in environments where the full context can't come up.
 */
public class HelperDefaultLogoSmokeTest {

	@Test
	public void getDefaultLogoAsDataUri_shouldLoadBundledCareLogo() throws Exception {
		String dataUri = Helper.getDefaultLogoAsDataUri();

		Assertions.assertNotNull(dataUri);
		Assertions.assertTrue(dataUri.startsWith("data:image/png;base64,"), dataUri.substring(0, 40));

		byte[] decoded = Base64.getDecoder().decode(dataUri.substring("data:image/png;base64,".length()));
		Assertions.assertTrue(decoded.length > 1000, "Decoded logo should be a real image, got " + decoded.length + " bytes");

		// Write it out so it can be visually inspected.
		try (FileOutputStream out = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/decoded_default_logo.png")) {
			out.write(decoded);
		}
	}
}
