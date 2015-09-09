package org.sxb.render.report;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;

import org.sxb.core.Sxb;

public class ReportPdfExporter implements IReportFileExporter {
	private static final String contentType = "application/pdf; charset=" + Sxb.me().getConstants().getEncoding();
	public void export(ReportPrint reportPrint, HttpServletResponse response) throws JRException {
		response.setContentType(contentType);
		
		try {
			JasperExportManager.exportReportToPdfStream(reportPrint.getJasperPrint(), response.getOutputStream());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
