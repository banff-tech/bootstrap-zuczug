/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: Apache Tomcat/7.0.59
 * Generated at: 2016-05-06 02:00:44 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.apache.jsp.error;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.ofbiz.base.util.*;

public final class error_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      response.addHeader("X-Powered-By", "JSP/2.1");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write("\n");
      out.write("\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("<title>Open For Business Message</title>\n");
      out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n");
      out.write("</head>\n");
      out.write("\n");
 String errorMsg = (String) request.getAttribute("_ERROR_MESSAGE_"); 
      out.write("\n");
      out.write("\n");
      out.write("<body bgcolor=\"#FFFFFF\">\n");
      out.write("<div align=\"center\">\n");
      out.write("  <br/>\n");
      out.write("  <table width=\"100%\" border=\"1\" height=\"200\">\n");
      out.write("    <tr>\n");
      out.write("      <td>\n");
      out.write("        <table width=\"100%\" border=\"0\" height=\"200\">\n");
      out.write("          <tr bgcolor=\"#CC6666\">\n");
      out.write("            <td height=\"45\">\n");
      out.write("              <div align=\"center\"><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"4\" color=\"#FFFFFF\"><b>:ERROR MESSAGE:</b></font></div>\n");
      out.write("            </td>\n");
      out.write("          </tr>\n");
      out.write("          <tr>\n");
      out.write("            <td>\n");
      out.write("              <div align=\"left\"><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"2\">");
      out.print(UtilFormatOut.replaceString(errorMsg, "\n", "<br/>"));
      out.write("</font></div>\n");
      out.write("            </td>\n");
      out.write("          </tr>\n");
      out.write("        </table>\n");
      out.write("      </td>\n");
      out.write("    </tr>\n");
      out.write("  </table>\n");
      out.write("</div>\n");
      out.write("<div align=\"center\"></div>\n");
      out.write("</body>\n");
      out.write("</html>\n");
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try {
            if (response.isCommitted()) {
              out.flush();
            } else {
              out.clearBuffer();
            }
          } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
