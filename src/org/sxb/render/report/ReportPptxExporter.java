package org.sxb.render.report;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
/**
 * PowerPoint 生产类
 * office 2007以上版本
 * 依赖于JasperReport6.1
 * @author Sun
 *
 */
public class ReportPptxExporter implements IReportFileExporter {
	public void export(ReportPrint reportPrint, HttpServletResponse response) throws JRException {
		long start = System.currentTimeMillis();

		response.setContentType("application/vnd.ms-powerpoint");
		response.setHeader("Content-Disposition", "inline; filename="
				+ "default.pptx");
		JasperPrint jasperPrint = reportPrint.getJasperPrint();
		
		JRPptxExporter exporter = new JRPptxExporter();
		
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		try {
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		exporter.exportReport();

		System.err.println("PPT creation time : " + (System.currentTimeMillis() - start));
	}

}
