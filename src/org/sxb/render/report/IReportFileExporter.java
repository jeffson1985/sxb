package org.sxb.render.report;


import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;

public interface IReportFileExporter {
	public void export(ReportPrint reportPrint, HttpServletResponse response) throws JRException;
}
