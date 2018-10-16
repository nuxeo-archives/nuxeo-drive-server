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
  response.setContentType("text/html");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Nuxeo Drive startup page</title>
    <script type="text/javascript">
      setTimeout(() => {
        window.location.replace("nxdrive://token/<%= token %>/user/<%= userName %>");
      }, 2500);
    </script>
    <style type="text/css">
      body {
        font: normal 14px/18pt "Helvetica", Arial, sans-serif;
      }

      .container {
        font-weight: 1.3em;
        width: 80%;
        left: 10%;
        position: absolute;
        top: 45%
      }
    </style>
  </head>
  <body>
    <div class="container">
      <h1>Authentication successful: You'll be redirected to Drive shortly...</h1>
      <h5>Drive not opening? Click <a href="nxdrive://token/<%= token %>/user/<%= userName %>">here</a> to try and reopen it.</h5>
      <!-- Current user [<%= userName %>] acquired authentication token [<%= token %>] -->
    </div>
  </body>
</html>
<% } else {
  response.setContentType("application/json"); %>
{
  "username": "<%= userName %>",
  "token": "<%= token %>"
}
<% } %>
