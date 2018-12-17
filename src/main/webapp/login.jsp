<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Page</title>


        <link rel="stylesheet" type="text/css" href="css/common.css"/>

    </head>
    <body>
        <div class="header">
            <img id="header-logo" alt="relogo"src="css/img/reLogo.png"/>
        </div>

        <br/>
        <br/>
        <br/>
        <%
			String failedLogin = request.getParameter("invalidLogin");
			if (failedLogin != null && failedLogin.equals("true")) {
		%>
	<center>
		<p class="invalidCredentials">Invalid username or password</p>
	</center>
	<br>
	<% }
	%>
	<form action="login" method="post">  
		<center>
			Username:<input type="text" name="username"/><br/><br/>  
			Password:<input type="password" name="password"/><br/><br/>  
			<input type="submit" value="login"/>  
		</center>
	</form> 
</body>
</html>
