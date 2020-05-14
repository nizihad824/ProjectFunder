<html>
	<head>
	  <title>Edit Project</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
	</head>
	<body>
		<form method="post">
			Title:<br>

      <input type="hidden" name="creator" value=${loggedInUser}>
			<input type="text" name="title" value=${title}><br>
			Limit:<br>
			<input type="text" name="limit" value=${limit}><br>

      Category:<br>
			<input type="radio" name="category" value="1"> Health & Creative Works<br>
			<input type="radio" name="category" value="2"> Art & Creative Works<br>
			<input type="radio" name="category" value="3"> Education<br>
			<input type="radio" name="category" value="4"> Tech & Innovation<br>

      Predecessor:<br>
			<input type="radio" name="vorganger" value="None" checked="checked"> None<br>
      <#list projectList as p>
        <input type="radio" name="vorganger" value=${p.id}> ${p.title}<br>
      </#list>
			Description:<br>
			<input type="text" name="description" value=${description}><br>
			<input type="submit" value="Update"/>
		</form>
	</body>
</html>
