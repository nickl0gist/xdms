 TODO list:
- 

####Done
 
1+. Warehouse Controller:  - ~~Create~~, Update end points. 
  For each record or ~~for all of them at the same time~~?

2+. Create ~~or not~~ connections for every warehouse with each 
customer by default with status <tt>isActive = false</tt>?     

3+. when new customer created all Warehouses get it to connection in 
table <tt>warehouse_customer</tt> with status <tt>isActive = false</tt>?

4+. Creating new Warehouse - creating at the same 
time new customer - reflection of created warehouse. (id, code ???)

5+. Create Excel template with manifests for all
 warehouses and customers to be uploaded to XDMS.

6+. Pallet Number add 
to Manifest_real entity

7+. Warehouse types TradeXDock - CC, XD, 
TXD - for warehouse to manipulate with references.

8+. Create rest endpoint 
to download template for uploading

9+. Review interface ExcelController<T>
done



not done
-



-. Add Transit time to tpa days settings days with hours. Implement it as duration for hours adn period for a days. 
    
-. When customer is set to inactive all warehouses connections with it have to be switched to inActive.
    Customer with picked up and not delivered manifests cannot be switched to inActive. 

-. Manifest View same as real manifest template and add to it weights gross and nett, dn, sap reception number

-. Transport Plan for each groupage.   


