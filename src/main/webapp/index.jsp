<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Extraction des donnees</title>
</head>
<body>
    <div>${success}</div><br>
    <form action="controller" method="post" enctype="multipart/form-data">
    	<input type="hidden" name="action" value="pv">
    	<label for="file">Entrer un fichier PDF</label>
    	<input type="file" name="file"><br>
    	<input type="submit" value="Uploader">
    </form>
</body>
</html>