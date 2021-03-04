<%--
  Created by IntelliJ IDEA.
  User: goncalo
  Date: 05-03-2017
  Time: 16:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Gonçalo Almeida</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <script src="scripts.js"></script>
      <link href="https://fonts.googleapis.com/css?family=Signika" rel="stylesheet">
      <link href="https://fonts.googleapis.com/css?family=Muli" rel="stylesheet">
    <link href="style/style.css" type="text/css" rel="stylesheet">
    <link rel="icon" type="image/x-icon" href="favicon.png">
  </head>
  <body onload="$('#content').load('aboutMe.html')">
    <div id="header">
      <div class="name">
        <div class="nameDiv">
          <a onclick="load('aboutMe.html')"> Gonçalo Almeida </a>
        </div>
      </div>
      <div class="menu">
        <div class="button" onclick="load('links.jsp')">
          Links
        </div>
        <div class="button" onclick="load('projects.jsp')">
          Projects
        </div>
      </div>
    </div>

    <div id="content"></div>

    <footer id="footer">
      <p>
        Gonçalo Almeida 2017
      </p>
    </footer>
  </body>
</html>
