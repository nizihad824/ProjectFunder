<html>
  <head>
    <title>Project Funder Main Page</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css"/>
  </head>
  <body>
    <div class="nav">
      <ul>
        <li><a class="active" href="/view_main">ViewMain</a></li>
        <li><a href="/new_project">New Project</a></li>
        <li><a href="/search">Search</a></li>
        <li style="float:right"><a href="/view_profile?u=${loggedInUser}">My Profile</a></li>
      </ul>
    </div>
    <h2> Open projects </h2>

    <table class="projects">
      <tr>
        <th>Categorie</th>
        <th>Title</th>
        <th>Email</th>
        <th>Sum</th>
      </tr>
      <#list projects_open as project_open>
        <tr>
          <td><img src=${project_open.icon} height=24 width=24 class="icon"></td>
          <td><a href="../view_project?id=${project_open.id}" class="projectid">${project_open.title}</a></td>
          <td><a href="../view_profile?u=${project_open.userEmail}" class="userEmail">${project_open.name}</a></td>
          <td>${project_open.sum}</td>
        </tr>
      </#list>
    </table>

    <h2> Closed projects </h2>


    <table class="projects">
      <tr>
        <th>Categorie</th>
        <th>Title</th>
        <th>Email</th>
        <th>Sum</th>
      </tr>
      <#list projects_closed as project_closed>
        <tr>
          <td><img src=${project_closed.icon} height=24 width=24 class="icon"></td>
          <td><a href="../view_project?id=${project_closed.id}" class="projectid">${project_closed.title}</a></td>
          <td><a href="../view_profile?u=${project_closed.userEmail}" class="userEmail">${project_closed.name}</a></td>
          <td>${project_closed.sum}</td>
        </tr>
      </#list>
    </table>




  </body>
</html>
