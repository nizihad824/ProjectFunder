<html>
  <head>
    <title>Profile Details</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>
  <body>
    <div class="nav">
      <ul>
        <li><a href="/view_main">ViewMain</a></li>
        <li><a href="/new_project">New Project</a></li>
        <li><a href="/search">Search</a></li>
        <li class="active" style="float:right"><a href="/view_profile?u=${loggedInUser}">My Profile</a></li>
      </ul>
    </div>
    <p>
      ${userEmail}'s profile<br>
      Name: ${name}<br>
      Number of created projects: ${created}<br>
	    Number of supported projects: ${funding}
    </p>


    <h2> Created Projects </h2>
    <#list userCreatedProjects as p_created>
      <div class="createdProject">
        <img src=${p_created.icon} height=24 width=24 class="icon">
        <a href="../view_project?id=${p_created.id}">${p_created.title}</a>
        ${p_created.status}
        ${p_created.sum}
      </div>
    </#list>

    <h2> Supported Projects </h2>
    <#list supportedProjects as p_supported>
      <div class="supportedProject">
        <img src=${p_supported.icon} height=24 width=24 class="icon">
        <a href="../view_project?id=${p_supported.id}">${p_supported.title}</a>
        ${p_supported.status}
        ${p_supported.limit}
        ${p_supported.beitrag}
      </div>
    </#list>
  </body>
</html>
