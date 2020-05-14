<html>
  <head>
    <title>Create New Project</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>
  <body>
    <div class="nav">
      <ul>
        <li><a href="/view_main">ViewMain</a></li>
        <li><a class="active" href="/new_project">New Project</a></li>
        <li><a href="/search">Search</a></li>
        <li style="float:right"><a href="/view_profile?u=${loggedInUser}">My Profile</a></li>
      </ul>
    </div>
    <form method="post">
      Title:<br>

      <input type="hidden" name="creator" value=${loggedInUser}>
      <input type="text" name="title"><br>
      Limit:<br>
      <input type="text" name="limit"><br>

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
      <input type="text" name="description"><br>
      <input type="submit" value="Create"/>
    </form>

    <#if succ>
      <p>Created new project</p>
    </#if>
  </body>
</html>
