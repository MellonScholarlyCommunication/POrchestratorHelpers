# ACL - world readable, owner full access

## Owner
delete /.acl
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#agent> <https://hochstenbach.inrupt.net/profile/card#me> }
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#accessTo> <./> }
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#default> <./> }
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#mode> <http://www.w3.org/ns/auth/acl#Read> }
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#mode> <http://www.w3.org/ns/auth/acl#Write> }
patch  /.acl INSERT DATA { <#owner> <http://www.w3.org/ns/auth/acl#mode> <http://www.w3.org/ns/auth/acl#Control> }
patch  /.acl INSERT DATA { <#owner> a <http://www.w3.org/ns/auth/acl#Authorization> }

## Public
patch  /.acl INSERT DATA { <#public> <http://www.w3.org/ns/auth/acl#agentClass> <http://xmlns.com/foaf/0.1/Agent> }
patch  /.acl INSERT DATA { <#public> <http://www.w3.org/ns/auth/acl#accessTo> <./> }
patch  /.acl INSERT DATA { <#public> <http://www.w3.org/ns/auth/acl#default> <./> }
patch  /.acl INSERT DATA { <#public> <http://www.w3.org/ns/auth/acl#mode> <http://www.w3.org/ns/auth/acl#Read> }
patch  /.acl INSERT DATA { <#public> <http://www.w3.org/ns/auth/acl#mode> <http://www.w3.org/ns/auth/acl#Append> }
patch  /.acl INSERT DATA { <#public> a <http://www.w3.org/ns/auth/acl#Authorization> }