# XDMS REST API

### END POINTS

#### _User Controller:_ ####

* xdms.com/admin/users - [GET] get all users list

* xdms.com/admin/users/orderby/__{orderBy}/{direction}__ - [GET] - get all users with ordering by:
- `{orderBy}`:
  * username;
  * firstname;
  * lastname;
  * role.  
- `{direction}`: 
    * asc; 
    * desc.
* xdms.com/admin/users/__{id}__ - [GET] - get one specific user by id.
* xdms.com/admin/users - [PUT] - Updating specific user. The request should contain User entity.
* xdms.com/admin/users - [POST] - The User entity given in the request will be persisted in DataBase.
* xdms.com/admin/users/__{id}__ - [DELETE] - Delete one specific user by id. It will return "deleted" message if user 
was deleted. 
You will get 404(NotFound) in case the user doesnt exist.  

* xdms.com/admin/users/userroles - [GET] - get all existing roles for users.


[User Controller](#user-content-user-controller)

_Reference Controller:_

* xdms.com/coordinator/references - [GET] get list of all references.  
* xdms.com/coordinator/references/orderby/__{orderBy}/{direction}__ - [GET] get list of all references ordered by: 
    - `{orderBy}`:
      * number;
      * name;
      * hscode;
      * sname;  
      * cname.  
    - `{direction}`: 
      * asc; 
      * desc. 
  * xdms.com/coordinator/references/__{id}__ - [GET] - get one specific reference by id.
  * xdms.com/coordinator/references/search/__{searchString}__ - [GET] - searching reference by it's string properties:
    - `{searchString}`:
      * number;
      * name;
      * hscode;
      * designationen;
      * designationru;
      * customer_agreement;
      * supplier_agreement;
      * supplier_name;
      * customer_name.
 * xdms.com/coordinator/references/__{id}__ - [GET] - get one specific reference by id.
 * xdms.com/coordinator/references/downloadFile/references.xlsx - [GET] - get Excel XLSX file with all references 
 in database.
 * xdms.com/coordinator/references - [POST] create and record new reference in DB.
 * xdms.com/coordinator/references - [PUT] The request must have Reference instance in RequestBody and it will 
 update reference in Database.