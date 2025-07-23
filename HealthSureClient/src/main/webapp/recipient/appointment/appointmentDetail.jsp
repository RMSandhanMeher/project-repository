<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:view>  <-- CHANGED TO f:view
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<h1>hellow</h1>
<h:outputText value="#{recipientAppointmentController.selectedAppointmentIdForDetail}"/>
</body>
</html>
</f:view>