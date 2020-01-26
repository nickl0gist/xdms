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

10+. Add Transit time to tpa days settings days with hours. Implemented as part of ISO 8601 for Durations 'P0DT0H0M' 
 ~~Implement it as duration for hours adn period for a days~~. 
 link: https://en.wikipedia.org/wiki/ISO_8601#Durations

11+. TimeZone issue. Additional Column created in Warehouse and in Customer to define timezone. 

12+. extract CustomSerializator into separate file and rename it

13+. add column in Excel to ~~group~~ indicate truck name from suppliers. If it is one truck to represent milkrun truck. 
This column should represent TTT in certain warehouse

14+. Check default TPA settings in XDMS, add it as default in new system without "magic" strings. 

15+. Create default tpa settings when new connection warehouse_customer created.

+16. Add condition to Excel to keep manifest codes in Manifest sheet as unique values.

+17. create mechanism to check if the manifest already existing in DB: check by manifest code;
tpa, ttt : check by name and by planned date and if they hasnt status error.
 
+18. Created DTO which will contain TPA-set ManifestSet TTT-set and ManifestReferenceSet to collect information from Loaded 
file and give it back to validation.

not done
-

-. check all saveAllEntities methods in controllers which implement ExcelController class. The response entities 
should have "isActive = false". Check again before saving?

-. save received Manifest ManifestReference TTT TPA after validation.
   
-. When customer is set to inactive all warehouses connections with it have to be switched to inActive.
    Customer with picked up and not delivered manifests cannot be switched to inActive. 

-. Manifest View same as real manifest template and add to it weights gross and nett, dn, sap reception number

-. get file for receptions with only not receipted references. Get file with all references from TTT receipted and not.

-. manually added or by Excel XD-matrix 


2.1.0
-

-. add Comments to manifest and manifest Reference 

-. IS and CO tables and instances

-. Transport Plan for each groupage.   

-. E-mail notifications. Define what kind of notifications has to be done.

2.1.1
-
-. Reports

Excel Matrix Template
-

-. add super conditions in the template of excel for manifest loading: ???? 
   - arrival dates of Customer truck names
