<html>
  <head>
    <title>New Project Fund</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>

  <body>
    <form method="post">
      Donation for project ${projectTitle}<br>
      Amount: <input type="text" name="amount"> <br>
      <input type="radio" name="anonymous" value="true"> Anonymous donation <br>
      <input type="hidden" name="user" value=${loggedInUser}>
      <input type="hidden" name="id" value=${id}>
      <input type="submit" value="donate">
    </form>
  </body>
</html>
