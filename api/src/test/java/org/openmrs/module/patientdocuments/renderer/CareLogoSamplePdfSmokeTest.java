/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.patientdocuments.renderer;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmrs.module.patientdocuments.common.Helper;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Standalone (no OpenMRS test context) smoke check that renders a sample encounter
 * report through the real FOP stylesheet, with the module's default logo attached, to
 * produce an actual PDF for visual review. Mirrors the encounter form PDF a user
 * downloads from Visits > Completed forms, minus the database lookups.
 */
public class CareLogoSamplePdfSmokeTest {

	@Test
	public void transformXmlToPdf_shouldRenderSampleEncounterWithDefaultLogo() throws Exception {
		String logoContent = Helper.getDefaultLogoAsDataUri();
		Assertions.assertNotNull(logoContent);

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<encounters>"
				+ "<encounter>"
				+ "<logo>" + logoContent + "</logo>"
				+ "<patientName>md sher kha</patientName>"
				+ "<location>Deir Al-Balah PHCC</location>"
				+ "<encounterDate>2026-08-20</encounterDate>"
				+ "<printedBy>Printed by Super User (daemon) at 2026-08-20 14:40</printedBy>"
				+ "<formName>Health Promotion Session Form</formName>"
				+ "<pages>"
				+ "<page label=\"Health Promotion Session\">"
				+ "<section label=\"Participant Information\">"
				+ "<question label=\"Participant Name\">md sher kha</question>"
				+ "<question label=\"Gender\">Male</question>"
				+ "<question label=\"Age (in Years)\">0</question>"
				+ "<question label=\"Phone Number\">3234456547</question>"
				+ "</section>"
				+ "<section label=\"Session Details\">"
				+ "<question label=\"Session Date\">No data recorded</question>"
				+ "<question label=\"Session Type\">No data recorded</question>"
				+ "<question label=\"Topic\">No data recorded</question>"
				+ "<question label=\"Location\">No data recorded</question>"
				+ "<question label=\"CHW Name\">No data recorded</question>"
				+ "<question label=\"Notes\">No data recorded</question>"
				+ "</section>"
				+ "</page>"
				+ "</pages>"
				+ "</encounter>"
				+ "</encounters>";

		// Mirrors EncounterPdfReportRenderer#transformXmlToPdf, but goes through the
		// package-private getStylesheetStream(String) overload with a blank path so the
		// default bundled stylesheet loads without needing the Initializer service (which
		// requires a full OpenMRS context this standalone test doesn't spin up).
		EncounterPdfReportRenderer renderer = new EncounterPdfReportRenderer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		FopFactory fopFactory = FopFactory.newInstance(new java.io.File(".").toURI());
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

		try (InputStream xslStream = renderer.getStylesheetStream("")) {
			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = factory.newTransformer(new StreamSource(xslStream));
			Source src = new StreamSource(new StringReader(xml));
			Result res = new SAXResult(fop.getDefaultHandler());
			transformer.transform(src, res);
		}

		byte[] pdfBytes = out.toByteArray();
		Assertions.assertTrue(pdfBytes.length > 1000, "Rendered PDF should be non-trivial, got " + pdfBytes.length + " bytes");

		try (FileOutputStream fos = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/sample_encounter_with_care_logo.pdf")) {
			fos.write(pdfBytes);
		}
	}
}
