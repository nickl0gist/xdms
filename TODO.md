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

12+. extract CustomSerializer into separate file and rename it

13+. add column in Excel to ~~group~~ indicate truck name from suppliers. If it is one truck to represent milkrun truck. 
This column should represent TTT in certain warehouse

14+. Check default TPA settings in XDMS, add it as default in new system without "magic" strings. 

15+. Create default tpa settings when new connection warehouse_customer created.

+16. Add condition to Excel to keep manifest codes in Manifest sheet as unique values.

+17. create mechanism to check if the manifest already existing in DB: check by manifest code;
tpa, ttt : check by name and by planned date and if they hasn't status error.
 
+18. Created DTO which will contain TPA-set ManifestSet TTT-set and ManifestReferenceSet to collect information from 
Loaded file and give it back to validation.

+19. save received Manifest, ManifestReference, TTT, TPA after validation.

+20. Adjust existing tests to new DB constraints. Make all tests to run

+21.      Test Manifest
                 √ Two Manifests with the same number
                 √ No TPA, No TTT
                 √ Not Existing Customer
                 √ Not Existing Supplier
                 √ Manifest with wrong Manifest Code
                 √ What if Supplier is not Active
                 √ What if Customer is not Active
              Test ManifestReferences
                 √ Not Existing Reference
                 √ add ManRef to not existing manifest => not possible test Case? 
                 √ add ManRef without TPA - ManifestReference will not pass the validation
                 √ What if ~~reference~~ agreement is not active
                 √ Test if forecast supplier-customer doesn't match with agreement of the reference   
             Test endpoints: 
             + GET template /manifest_upload_template.xlsx
             - POST data with file /manifests/uploadFile
                 - test validation method different cases:
                     √ ok test
                     √ nok test
                     √ partial ok/nok test
             √ SAVE(POST) parsed from Excel JSON /forecast/save 
             Test Methods +/-
                 √ manifestValidation
                     √1. manifest with same code already existing in DB
                     √2. manifest with not compliant conditions
                     √3. manifest with compliant conditions
                 ~ - tttSetValidation <private>
                 ~ - tpaSetValidation <private>
                 ~ - manifestReferenceSetValidator <private>
                 ~ - connectManiRefToManifestAndTPA ? <private>
                 ~ - connectManifestWithTpaAndTtt ? <private>
            NOK Tests:
                Test TTT
                    Not Existing 
                        √ CC, 
                        √ XD, 
                        √ TXD, 
                        √ Customer
                        √Create TTT with the same parameters as existing one
                            Create 2 similar TTT for different WH:
                                In such case the both TTT will be prepared according to provided conditions. The name will be the same as 
                                indicated in Excel file
                            Two TTT with same Name and WHCustomer combination but with different anticipated ETA to Customer Warehouse:
                                Only last calculated TTT will be saved to TTTSetDto according to Set Java Rules.
                        Create TTT:
                            √ with bad dates, 
                            √ too late dates, 
                            √ what if there no appropriate WH_CUST settings ~~-> NullPointerException ? Create Handler ?~~Doesn't matter for TTT
                Test TPA
                    Not Existing 
                        √ CC, 
                        √ XD, 
                        √ TXD, 
                        √ Customer
                        √ Create TPA with the same parameters as existing one
                            Create 2 similar TPA for different WH:
                                In such case the both TPA will be prepared according to provided conditions. The name will be the same as 
                                indicated in Excel file
                            Two TPA with same Name and WHCustomer combination but with different anticipated ETA to Customer Warehouse:
                                Only last calculated TPA will be saved to TPASetDto according to Set Java Rules. 
                        √ Create TPA: 
                                    √ with bad dates, 
                                    √ too late dates, 
                                    √ what if there no appropriate WH_CUST settings -> ~~NullPointerException ? Create Handler ?~~ TPA -> Error
+22
not done

+22 Code Refactoring for ExcelManifestController: create Validator class to move all the validations into it.

+23 TruckTimeTableController created. Endpoints created: get all TTT for certain warehouse and certain date,
 get TTT by id, creation of new TTT using usual form. 
 Tests created: 
 getAllTttByTheWarehouseAndCertainDate
 getAllTttByTheWarehouseAndCertainDateWrongRegex
 getTttByIdTest
 getTttByIdNotFound
 createNewTtt
 createNewTttStatusDelayed
 createNewTttWithNotExistingWarehouse
 createNewTttWithNullWarehouse

+24. Find Manifests with empty TTT set ot TPA set. Manifest Management page which shows all manifests with empty TTT and
 TPA list, this page allows to assign TPA or TTT

+25. TPA status before saving check the date. if day is the same change to In Progress 
    if TPA is for next Day or later change to BuFFER
    
Not done
-
-. get file for receptions with only not receipted references. Get file with all references from TTT receipted and not.

-. is it necessary to check either the TPA is closed before changing(splitting between different TPA)/deleting any manifest in it?

-. if defined by default TPA was changed, the changes should be implemented to TTT at the Warehouse where this TPA goes to.

-. STOCK - implement STOCK entity as TPA which contain goods remained in Warehouse.

-. Create mechanism which will be responsible of checking TTT and TPA status each time when get requests for particular
Warehouse and Date will be proceeded. 

-. generate TPS after the TPA was closed.

-. What if user tries to delete TPA TTT Manifest Manifest-Reference by id for warehouse he doesn't have access? 

-. check all saveAllEntities methods in controllers which implement ExcelController interface. The response entities 
should have "isActive = false". Check again before saving? To implement or not?

-. When customer is set to inactive all warehouses connections with it have to be switched to inActive.
    Customer with picked up and not delivered manifests cannot be switched to inActive. 

-. Manifest View same as real manifest template and add to it weights gross and nett, dn, sap reception number

-. manually added matrix. pick CC XD, if has references -> pick TXD, pick TTT TPA or create new according to warehouse 
and date. Pick Customer and Supplier.

-. Schedule Tasks to update TPA and TTT status from BUFFER to PENDING etc. Scheduled annotation 


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
   - manifest without TTT and TPA, without pick up from SUPPLIER and delivering to customer
   - Truck id for CC XD TXD if is empty but WH name is provided. 
   - if manifest is going only through XD it may not need to have lines in sheet Reference_Forecast 
   - check Reference Agreement if it is Properly assigned to appropriate manifest(supplier)
   - if no customer or no supplier assigned to line with manifest  
   - if customer or supplier is not active
   - the dates which are Sundays and Saturdays should be checked. Sundays to red, Saturdays to Orange
   - REFERENCE_FORECAST sheet. combination of manifest and agreement is repeated. 
   - if There is arrival date and time provided in warehouse so the TPA name should be provided also, if no => Red
   - Sequence of dates in each Row (Each previous date should be less than Each next Date and Time)
   - Duplicates in T Column Red Color
   - Dates in R N J F columns should be Ordered by Date
   
Attention!
-

- TTTService could find ambiguous TTTs in method getTttByTruckNameAndTttArrivalDatePlan if the Name and Date 
are the same for different Warehouses - add Searching by Warehouse?