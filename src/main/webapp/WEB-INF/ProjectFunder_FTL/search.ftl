<html>
  <head>
    <title>Search Projects</title>
    <link rel="stylesheet" type="text/css" href="/icons/style.css">
  </head>
  <body>
      <div class="nav">
        <ul>
            <li><a href="/view_main">ViewMain</a></li>
            <li><a href="/new_project">New Project</a></li>
            <li><a class="active" href="/search">Search</a></li>
            <li style="float:right"><a href="/view_profile?u=${loggedInUser}">My Profile</a></li>
        </ul>
      </div>
    <form>
      Title:<br>
      <input type="text" name="title"/> <br>
      <input type="submit" value="search"/>
    </form>
    <#if error_detected>
      <p>SQL ERROR - No results</p>
      <#elseif resultList?size == 0>
        <p>Nothing there</p>
        <#else>
          <table class ="datatable">
            <tr>
              <th>Icon</th>  <th>Title</th> <th>Creator</th> <th>Sum</th>
            </tr>
            <#list resultList as r>
              <tr>
                <td><img src=${r.icon} height=24 width=24></td>
                <td><a href="../view_project?id=${r.id}">${r.title}</a></td>
                <td><a href="../view_profile?u=${r.userEmail}">${r.name}</a></td>
                <td>${r.sum}</td>
              </tr>
            </#list>
          </table>
        </#if>
  </body>
</html>
