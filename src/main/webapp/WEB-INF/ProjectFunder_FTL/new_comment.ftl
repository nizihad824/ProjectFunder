<html>
  <head>
    <title>New Comment</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>
  <body>
    <p> Logged in user = ${loggedInUser} <br>
      Project title =  ${projectTitle} </p>
    <form method="post">
      <textarea id="comment" cols="42" rows="20" name="comment"></textarea>
      <input type="checkbox" name="anonymous" value="true">Anonymous Comment
      <input type="hidden" name="loggedInUser" value=${loggedInUser}>
      <input type="submit" value="Add comment"/>
    </form>
  </body>
</html>
