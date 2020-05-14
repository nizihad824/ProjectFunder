<html>
  <head>
    <title>Project Details</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>
  <body>

    <h2>Information</h2>
    <img src=${icon} height=24 width=24 ><br>
    Title: ${title} <br>
    Created by: <a href="/view_profile?u=${creatorMail}">${creatorName} </a><br>
    ${description}<br>
    Limit: ${limit} <br>
    Current sum: ${sum} <br>
    Status: ${status}<br>
    <#if vorganger>
      Vorgänger: <a href="view_project?id=${vid}">${vtitle}</a> <br>
    </#if>


    <h2>Actions</h2>
    <a href="/new_project_fund?id=${id}">Donate</a>
    <a href="/edit_project?id=${id}">Edit Project</a>
    <form method="post">
      <input type="hidden" name="loggedInUser" value=${loggedInUser}>
      <input type="hidden" name="projectid" value=${id}>
      <input type = "submit" value="Delete">
    </form>

    <h2>List of donors</h2>
    <#list donations as d>
      ${d.name} : ${d.amount} <br>
    </#list>

    <h2>Comments</h2>
    <#list comments as c>
      ${c.name} : ${c.text} <br>
    </#list>

    <a href="/new_comment?id=${id}"> New Comment </a>
  </body>
</html>
