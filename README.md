# XDMS REST API

#### END POINTS

_User Controller:_

* xdms.com/admin/users - [GET] get all users list

* xdms.com/admin/users/__{orderBy}/{direction}__ - [GET] - get all users with ordering by:
- `{orderBy}`:
  * username;
  * firstname;
  * lastname;
  * role.  
- `{direction}`: 
    * 'asc'; 
    *'desc'.
* xdms.com/admin/users/__{id}__ - [GET] - get one specific user by id.
* xdms.com/admin/users - [PUT] - Updating specific user. The request should contain User entity.
* xdms.com/admin/users - [POST] - The User entity given in the request will be persisted in DataBase.
* xdms.com/admin/users/__{id}__ - [DELETE] - Delete one specific user by id. It will return "deleted" message if user was deleted. 
You will get 404(NotFound) in case the user doesnt exist.  

* xdms.com/admin/users/userroles - [GET] - get all existing roles for users.

<a href="#user-controller">Link to Header</a>
[link](#user-controller)

_Reference Controller:_

* xdms.com/coordinator/references - [GET] get list of all references.  