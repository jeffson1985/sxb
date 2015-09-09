package org.sxb.render.report;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
/**
 * Word 生产类
 * office 2007以上版本
 * 依赖于JasperReport6.1
 * @author Sun
 *
 */
public class ReportWordExporter implements IReportFileExporter {
	public void export(ReportPrint reportPrint, HttpServletResponse response)
			throws JRException {
		
		response.setContentType("application/msword");
		response.setHeader("Content-Disposition", "inline; filename="
				+ "default.docx");
		long start = System.currentTimeMillis();

		JasperPrint jasperPrint = reportPrint.getJasperPrint();

		JRDocxExporter exporter = new JRDocxExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		try {
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(
					response.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		exporter.exportReport();

		System.err.println("WORD creation time : "
				+ (System.currentTimeMillis() - start));
	}

}
