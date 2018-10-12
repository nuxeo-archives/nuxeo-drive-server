<%@ page language="java"%>
<%@ page import="org.nuxeo.runtime.api.Framework"%>
<%@ page import="org.apache.http.HttpStatus"%>
<%@ page import="org.nuxeo.ecm.tokenauth.service.TokenAuthenticationService"%>
<%
response.setCharacterEncoding("UTF-8");
TokenAuthenticationService tokenAuthService = Framework.getService(TokenAuthenticationService.class);
String token = tokenAuthService.acquireToken(request);
if (token == null) {
    response.sendError(HttpStatus.SC_UNAUTHORIZED);
    return;
}
String userName = request.getUserPrincipal().getName();
String updateToken = request.getParameter("updateToken");
String useProtocol = request.getParameter("useProtocol");

// New login system
// If the useProtocol parameter is true, this page is opened in the user's browser
// and the token is passed back to the application with its custom nxdrive protocol URL.
// If useProtocol is false (for development purposes),this page is opened using
// WebKit and the resulting JSON is parsed by Drive.
if (Boolean.parseBoolean(useProtocol)) {
  response.sendRedirect("nxdrive://token/" + token + "/user/" + userName);
} else {
  response.setContentType("application/json"); %>
{
  "username": "<%= userName %>",
  "token": "<%= token %>"
}
<% } %>
