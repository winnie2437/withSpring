<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<div>
  <h2>A global community of friends and strangers spitting out their inner-most and personal thoughts on the web for everyone else to see.</h2>
  <h3>Look at what these people are spitting right now...</h3>

<ol class="entry-list">

  <c:forEach var="entry" items="${entries}">
    <s:url value="/users/{userName}" var="user_url">
      <s:param name="userName" value="${entry.user.username}" />
    </s:url>
    
  <li>
    <span class="entryListImage">
      <img src="http://s3.amazonaws.com/spitterImages/${entry.user.id}.jpg" width="48" border="0" align="middle" 
        onError="this.src='<s:url value="/resources/images"/>/user_avatar.png';"/>
    </span>

    <span class="entryListText">
      <a href="${user_url}"> 
        <c:out value="${entry.user.username}" />
      </a>
      - <c:out value="${entry.text}" /><br/>
      <small><fmt:formatDate value="${entry.when}" pattern="hh:mma MMM d, yyyy" /></small>
    </span>
  </li>
  
  </c:forEach>
 
</ol>

</div>
    
