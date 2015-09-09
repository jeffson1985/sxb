package org.sxb.render.report;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
/**
 * Excel 生产类
 * office2007以上版本
 * 依赖于JasperReport6.1
 * @author Sun
 *
 */
public class ReportExcelExporter implements IReportFileExporter {
	public void export(ReportPrint reportPrint, HttpServletResponse response) throws JRException {
		
		response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "inline; filename="
                        + "default.xlsx");
		long start = System.currentTimeMillis();

		JasperPrint jasperPrint = reportPrint.getJasperPrint();
		
		JRXlsxExporter exporter = new JRXlsxExporter();
		
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		try {
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
		configuration.setOnePagePerSheet(true);
		exporter.setConfiguration(configuration);
		
		exporter.exportReport();

		System.err.println("XLSX creation time : " + (System.currentTimeMillis() - start));
	}

}
