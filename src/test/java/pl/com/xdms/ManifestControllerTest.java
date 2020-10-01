package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;
import pl.com.xdms.domain.warehouse.WarehouseManifestId;
import pl.com.xdms.service.*;
import pl.com.xdms.service.truck.TruckService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 03.06.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup({
        @Sql(value = {"/sql_scripts/createValuesInDBforTestsOfTttTpaManifestControllers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = {"/sql_scripts/drops.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class ManifestControllerTest {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MockMvc mockMvc;

    private Manifest newManifest;

    @Autowired
    private ManifestService manifestService;

    @Autowired
    private ReferenceService referenceService;

    @Autowired
    private ManifestReferenceService manifestReferenceService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private TruckService truckService;

    @Before
    public void init() {
        newManifest = new Manifest();
        newManifest.setManifestCode("Manifest_Code");
    }

    /**
     * Test of searching for Abandoned Manifest, which are not relied to any existing TTT
     */
    @Test
    public void getAllAbandonedManifestsTest() throws Exception {
        mockMvc.perform(get("/warehouse/xd_gro/ttt/manifests/abandoned"))
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(status().isOk());
    }

    /**
     * Ok test for searching manifest by given id.
     */
    @Test
    public void getManifestByIdTest200() throws Exception {
        mockMvc.perform(get("/warehouse/xd_std/ttt/14/manifest/10").header("truck","ttt"))
                .andDo(print())
                .andExpect(jsonPath("$.['warehouseManifestId'].warehouseId").value(4))
                .andExpect(jsonPath("$.['warehouseManifestId'].manifestId").value(10))
                .andExpect((status().isOk()));
    }

    /**
     * NOK test for searching manifest by given id.
     */
    @Test
    public void getManifestByIdTest404() throws Exception {
        mockMvc.perform(get("/warehouse/xd_std/ttt/14/manifest/250").header("truck","ttt"))
                .andDo(print())
                .andExpect(header().stringValues("Error:", "The manifest with id=250 is not existing"))
                .andExpect((status().isNotFound()));
    }

    /**
     * Test of attempt to update manifest with Ok respond status
     */
    @Test
    public void updateManifestTestResponse200() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        Manifest manifestToUpdate = entityManager.find(Manifest.class, 5L);// (5, 'TEST-MAN-03', 1, 3, 1.2, 1000.0, 3, 3),
        manifestToUpdate.getManifestsReferenceSet().iterator();
        manifestToUpdate.getTpaSet().iterator();
        manifestToUpdate.getTruckTimeTableSet().iterator();

        entityManager.detach(manifestToUpdate);

        manifestToUpdate.setTotalWeightReal(5000.0);
        manifestToUpdate.setTotalLdmReal(5.4);
        manifestToUpdate.setPalletQtyReal(10);
        manifestToUpdate.setBoxQtyReal(500);

        WarehouseManifest warehouseManifest = new WarehouseManifest();
        warehouseManifest.setManifest(manifestToUpdate);
        warehouseManifest.setWarehouse(entityManager.find(Warehouse.class, 5L));
        warehouseManifest.setTtt(entityManager.find(TruckTimeTable.class, 5L));

        warehouseManifest.setBoxQtyReal(500);
        warehouseManifest.setPalletQty(10);
        warehouseManifest.setGrossWeight(5000.0);

        String json = om.writeValueAsString(warehouseManifest);
        entityManager.getTransaction().commit();
        entityManager.close();

        Manifest manifestToCheck = manifestService.findManifestById(5L);
        Assert.assertNull(manifestToCheck.getBoxQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());
        Assert.assertNull(manifestToCheck.getPalletQtyReal());

        Assert.assertEquals(TTTEnum.PENDING, truckService.getTttService().getTttById(5L).getTttStatus().getTttStatusName());
        Assert.assertNull(truckService.getTttService().getTttById(5L).getTttArrivalDateReal());


        mockMvc.perform(put("/warehouse/cc_arad/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The Manifest with id=5 was successfully updated"));

        mockMvc.perform(get("/warehouse/cc_arad/ttt/5/manifest/5").header("truck","ttt"))
                .andDo(print())
                .andExpect(jsonPath("$.grossWeight").value(5000.0))
                .andExpect(jsonPath("$.palletQty").value(10))
                .andExpect(jsonPath("$.boxQtyReal").value(500));

        Assert.assertEquals(TTTEnum.ARRIVED, truckService.getTttService().getTttById(5L).getTttStatus().getTttStatusName());
        Assert.assertNotNull(truckService.getTttService().getTttById(5L).getTttArrivalDateReal());
    }

    /**
     * Testcase of update manifest request within TTT which doesn't have the Manifest in it's set.
     */
    @Test
    public void updateManifestTestWithinTttWhichDoesntHaveThisManifestResponse400() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        Manifest manifestToUpdate = entityManager.find(Manifest.class, 10L);
        manifestToUpdate.getManifestsReferenceSet().iterator();
        manifestToUpdate.getTpaSet().iterator();
        manifestToUpdate.getTruckTimeTableSet().iterator();

        entityManager.detach(manifestToUpdate);

        manifestToUpdate.setTotalWeightReal(5000.0);
        manifestToUpdate.setTotalLdmReal(5.4);
        manifestToUpdate.setPalletQtyReal(10);
        manifestToUpdate.setBoxQtyReal(500);

        WarehouseManifest warehouseManifest = new WarehouseManifest();
        warehouseManifest.setManifest(manifestToUpdate);
        warehouseManifest.setWarehouse(entityManager.find(Warehouse.class, 5L));
        warehouseManifest.setTtt(entityManager.find(TruckTimeTable.class, 14L));

        String json = om.writeValueAsString(warehouseManifest);
        entityManager.getTransaction().commit();
        entityManager.close();

        Manifest manifestToCheck = manifestService.findManifestById(10L);
        Assert.assertNull(manifestToCheck.getBoxQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());
        Assert.assertNull(manifestToCheck.getPalletQtyReal());
        Assert.assertNull(manifestToCheck.getTotalLdmReal());

        mockMvc.perform(put("/warehouse/cc_arad/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("Error:", "Does not Exist"));
    }

    /**
     * Attempt to update the manifest which doesn't have id
     */
    @Test
    public void updateManifestTestResponse400() throws Exception {

        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        newManifest.setTotalWeightReal(5000.0);
        newManifest.setTotalLdmReal(5.4);
        newManifest.setPalletQtyReal(10);
        newManifest.setBoxQtyReal(500);

        WarehouseManifest warehouseManifest = new WarehouseManifest();
        warehouseManifest.setManifest(newManifest);
        warehouseManifest.setWarehouse(entityManager.find(Warehouse.class, 5L));
        warehouseManifest.setTtt(entityManager.find(TruckTimeTable.class, 5L));

        String json = om.writeValueAsString(warehouseManifest);

        mockMvc.perform(put("/warehouse/cc_arad/ttt/5/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400));
    }

    /**
     * Testcase of request when there is an attempt to update manifest in not existing TTT
     */
    @Test
    public void updateManifestWithinNotExistingTttTestResponse400() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        Manifest manifestToUpdate = entityManager.find(Manifest.class, 5L);// (5, 'TEST-MAN-03', 1, 3, 1.2, 1000.0, 3, 3),
        manifestToUpdate.getManifestsReferenceSet().iterator();
        manifestToUpdate.getTpaSet().iterator();
        manifestToUpdate.getTruckTimeTableSet().iterator();

        entityManager.detach(manifestToUpdate);

        manifestToUpdate.setTotalWeightReal(5000.0);
        manifestToUpdate.setTotalLdmReal(5.4);
        manifestToUpdate.setPalletQtyReal(10);
        manifestToUpdate.setBoxQtyReal(500);

        WarehouseManifest warehouseManifest = new WarehouseManifest();
        warehouseManifest.setManifest(manifestToUpdate);
        warehouseManifest.setWarehouse(entityManager.find(Warehouse.class, 5L));
        warehouseManifest.setTtt(entityManager.find(TruckTimeTable.class, 5L));

        String json = om.writeValueAsString(warehouseManifest);
        entityManager.getTransaction().commit();
        entityManager.close();



        mockMvc.perform(put("/warehouse/cc_arad/ttt/1000/manifest/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().string("Error:", "Does not Exist"));
    }

    /**
     * Test of case when user tries to delete the Manifest by it's given Id.
     * IN this testcase the manifest doesn't have any References related.
     * Assertions check the size of the sets of Manifests in related TPA and TTT after
     * deletion was performed.
     */
    @Test
    public void deleteDirectManifestStatus200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        Set<Manifest> tttManifestSetBefore = entityManager.find(TruckTimeTable.class, 14L).getManifestSet();
        Set<Manifest> tpaManifestSetBefore = entityManager.find(TPA.class, 14L).getManifestSet();

        Assert.assertEquals(1, tttManifestSetBefore.size());
        Assert.assertEquals(1, tpaManifestSetBefore.size());

        entityManager.getTransaction().commit();
        entityManager.close();

        mockMvc.perform(delete("/warehouse/xd_std/ttt/14/manifest/10").header("truck", "ttt"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Message:", "Manifest MAN-X-04 was removed from TTT with id=14"));

        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        entityManager2.getTransaction().begin();

        Set<Manifest> tttManifestSetAfter = entityManager2.find(TruckTimeTable.class, 14L).getManifestSet();
        Set<Manifest> tpaManifestSetAfter = entityManager2.find(TPA.class, 14L).getManifestSet();

        Assert.assertEquals(0, tttManifestSetAfter.size());
        Assert.assertEquals(0, tpaManifestSetAfter.size());

        entityManager2.getTransaction().commit();
        entityManager2.close();
    }

    /**
     * Test of the case when user tries to delete manifest from DB with References inside.
     * According to architecture the manifest_References entities should be deleted by cascade
     */
    @Test
    public void deleteManifestWithReferencesStatus200() throws Exception {

        Assert.assertNotNull(manifestReferenceService.findById(7L));

        try {
            mockMvc.perform(delete("/warehouse/xd_std/ttt/16/remove_manifest/13"))//.header("truck", "remove"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().stringValues("Message:", "Manifest MAN-X-05 was removed from TTT with id=16"));

            Assert.assertNull(manifestReferenceService.findById(7L));
        } catch (HttpMessageNotWritableException h) {
            log.info(h.getStackTrace().toString());
        }
    }

    /**
     * Testcase of attempt to delete manifest from DB with real quantities provided
     */
    @Test
    public void deleteManifestWithRealQuantitiesProvidedStatus422Test() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/1/remove_manifest/1"))//.header("truck", "remove"))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().stringValues("Message:", "Manifest with id=1 arrived already and couldn't be deleted"));
    }

    /**
     * Testcase when given id wasn't found while deleting the Manifest from DB
     */
    @Test
    public void deleteManifestWithWrongId404Test() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/1/remove_manifest/100"))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "Manifest with id=100 Not Found in DB"));
    }

    /**
     * Testcase of attempt to add new manifest in chosen TTT.
     * The provided manifest meets to conditions and snd it would be saved in DB.
     * Status response - 200 (Ok).
     */
    @Test
    public void manualCreationOfManifestTestStatus200() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();

        newManifest.setManifestCode("NEW-MANIFEST-11");
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(1L));
        newManifest.setSupplier(supplierService.getSupplierById(1L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/warehouse/xd_gro/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "The manifest NEW-MANIFEST-11 was successfully saved with id=20"));
    }

    /**
     * Testcase of attempt to add new manifest in chosen TTT.
     * The provided manifest has inactive Customer and Supplier
     * and it wouldn't be persisted in DB.
     * Response - 409 (Conflict).
     */
    @Test
    public void manualCreationOfManifestTestStatus409SupplierCustomerNotActive() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();

        newManifest.setManifestCode("NEW-MANIFEST-11");
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(4L));
        newManifest.setSupplier(supplierService.getSupplierById(4L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/warehouse/xd_gro/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(409))
                .andExpect(header().stringValues("Error:", "Manifest with code=NEW-MANIFEST-11 has Given Supplier isActive = false, Customer isActive = false"));
    }

    /**
     * Testcase of attempt to save the Manifest with break of Manifest.class annotation conditions.
     * Response - 412 (Precondition Failed).
     */
    @Test
    public void manualCreationOfManifestTestStatus412() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setBoxQtyPlanned(-10);

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/warehouse/xd_gro/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("manifest-supplier_NotNull", "must not be null"))
                .andExpect(header().stringValues("manifest-customer_NotNull", "must not be null"))
                .andExpect(header().stringValues("manifest-boxQtyPlanned_Min", "must be greater than or equal to 0"));
    }

    /**
     * Test of attempt to add manifest within not existing TTT.
     * Response - 404 (Not found).
     */
    @Test
    public void manualCreationOfManifestTestStatus404() throws Exception {
        long tttId = 280L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setBoxQtyPlanned(-10);

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/warehouse/xd_gro/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:", "The TTT with Id=280 wasn't found in Warehouse xd_gro"));
    }

    /**
     * Testcase of attempt to save Manifest with ManifestCode which already exists in DB
     * Response - - 409 (Conflict).
     */
    @Test
    public void manualCreationOfManifestTestStatus409() throws Exception {
        long tttId = 28L;
        ObjectMapper om = new ObjectMapper();
        newManifest.setManifestCode("TEST-MAN-01"); // id=3 in DB
        newManifest.setTotalWeightPlanned(500.0);
        newManifest.setTotalLdmPlanned(3.0);
        newManifest.setPalletQtyPlanned(10);
        newManifest.setBoxQtyPlanned(100);
        newManifest.setCustomer(customerService.getCustomerById(1L));
        newManifest.setSupplier(supplierService.getSupplierById(1L));

        String json = om.writeValueAsString(newManifest);

        mockMvc.perform(post("/warehouse/xd_gro/ttt/" + tttId).contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(409))
                .andExpect(header().stringValues("Error:", "Manifest with code=TEST-MAN-01 is existing in DB already"));
    }

    /**
     * Test of attempt to delete Manifest in TPA which does not exist.
     */
    @Test
    public void deleteManifestFromGivenTpaTest404() throws Exception {
        mockMvc.perform(delete("/warehouse/xd_gro/tpa/200/manifest/3").header("truck", "tpa"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues("Error:", "TPA with id=200 wasn't found in Warehouse xd_gro"));
    }

    /**
     * Attempt of removing particular Manifest from Set of given TPA by id.
     * Response 200.
     */
    @Test
    public void deleteManifestFromGivenTpaTest200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TPA tpa = entityManager.find(TPA.class, 4L);
        Assert.assertEquals(2, tpa.getManifestSet().size());
        entityManager.getTransaction().commit();

        WarehouseManifestId warehouseManifestId = new WarehouseManifestId();
        warehouseManifestId.setManifestId(6L);
        warehouseManifestId.setWarehouseId(5L);

        WarehouseManifest warehouseManifest = entityManager.find(WarehouseManifest.class, warehouseManifestId);

        Assert.assertEquals(4L, warehouseManifest.getTpa().getTpaID().longValue());
        entityManager.close();

        mockMvc.perform(delete("/warehouse/cc_arad/tpa/4/manifest/6").header("truck", "tpa"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "Manifest TEST-MAN-04 was removed from TPA with id=4"))
                .andExpect(jsonPath("$.tpaID").value(4))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(1)));

        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        WarehouseManifest warehouseManifestAfter = entityManager2.find(WarehouseManifest.class, warehouseManifestId);

        Assert.assertNull(warehouseManifestAfter.getTpa());
        entityManager2.close();
    }

    /**
     * Test of case when user tries to delete unknown manifest from existing TPA.
     */
    @Test
    public void deleteManifestFromGivenTpaTest405() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/tpa/2/manifest/300").header("truck", "tpa"))
                .andDo(print())
                .andExpect(status().is(405))
                .andExpect(header().stringValues("Error:", "Manifest with id=300 wasn't found in TPA GRO1"))
                .andExpect(jsonPath("$.tpaID").value(2));
    }

    /**
     * Test of attempt to delete Manifest in TTT which does not exist.
     */
    @Test
    public void deleteManifestFromGivenTttTestTNotfound404() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/200/manifest/3").header("truck", "ttt"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().stringValues("Error:", "TTT with id=200 wasn't found in Warehouse cc_swie"));
    }

    /**
     * Test of case when user tries to delete unknown manifest from existing TTT.
     */
    @Test
    public void deleteManifestFromGivenTttTestManifestNotFound404() throws Exception {
        mockMvc.perform(delete("/warehouse/cc_swie/ttt/2/manifest/300").header("truck", "ttt"))
                .andDo(print())
                .andExpect(status().is(405))
                .andExpect(header().stringValues("Error:", "Manifest with id=300 wasn't found in TTT TPA3"))
                .andExpect(jsonPath("$.tttID").value(2));;
    }

    /**
     * Attempt of removing particular Manifest from Set of given TTT by id.
     * Response 200.
     */
    @Test
    public void deleteManifestFromGivenTttTest200() throws Exception {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();
        TruckTimeTable ttt = entityManager.find(TruckTimeTable.class, 16L);
        Assert.assertEquals(2, ttt.getManifestSet().size());
        entityManager.getTransaction().commit();

        mockMvc.perform(delete("/warehouse/xd_std/ttt/16/manifest/7").header("truck", "ttt"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(header().stringValues("Message:", "Manifest MAN-X-01 was removed from TTT with id=16"))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(1)))
                .andExpect(jsonPath("$.tttID").value(16));
    }

    /**
     * TestCase of attempt to delete manifest from Closed TPA.
     */
    @Test
    public void deleteManifestFromGivenTpaTest400() throws Exception {
        TPA tpa = truckService.getTpaService().getTpaById(4L);
        tpa.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.CLOSED));

        log.info("TPA {}", truckService.getTpaService().save(tpa).getStatus());

        mockMvc.perform(delete("/warehouse/cc_arad/tpa/4/manifest/6").header("truck", "tpa"))
                .andDo(print())
                .andExpect(status().is(400))
                .andExpect(header().stringValues("Error:", "TPA with id=4 has bean already CLOSED"))
                .andExpect(jsonPath("$.tpaID").value(4))
                .andExpect(jsonPath("$.['manifestSet']", hasSize(2)));
    }

    /**
     * Testcase of adding the Reference to the chosen manifest.
     * Status response 200
     */
    @Test
    public void addReferenceToManifestTest200() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Manifest manifest = manifestService.findManifestById(8L);
        long customerId = manifest.getCustomer().getCustomerID();
        long supplierId = manifest.getSupplier().getSupplierID();

        List<Reference> referenceList = referenceService.getAllReferencesBySupplierAndCustomer(supplierId, customerId);
        Assert.assertFalse(referenceList.isEmpty());

        ManifestReference manifestReference = new ManifestReference();
        manifestReference.setReference(referenceList.get(0));
        manifestReference.setQtyPlanned(100);
        manifestReference.setBoxQtyPlanned(1);
        manifestReference.setGrossWeightPlanned(200);
        manifestReference.setPalletQtyPlanned(1);
        manifestReference.setPalletHeight(referenceList.get(0).getPalletHeight());
        manifestReference.setPalletWeight(referenceList.get(0).getPalletWeight());
        manifestReference.setPalletWidth(referenceList.get(0).getPalletWidth());
        manifestReference.setPalletLength(referenceList.get(0).getPalletLength());
        manifestReference.setStackability(referenceList.get(0).getStackability());

        mockMvc.perform(put("/warehouse/xd_gro/ttt/17/manifest/8/addReference").contentType(MediaType.APPLICATION_JSON_UTF8).content(om.writeValueAsString(manifestReference)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Message:", String.format("Reference %s was added to Manifest %s", referenceList.get(0).getNumber(), manifest.getManifestCode())));

    }

    /**
     * Testcase of adding the Reference to manifest which id wasn't found in DB.
     * Status response 404
     */
    @Test
    public void addReferenceToManifestTest404() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ManifestReference manifestReference = new ManifestReference();
        manifestReference.setQtyPlanned(100);
        manifestReference.setBoxQtyPlanned(1);
        manifestReference.setGrossWeightPlanned(200);
        manifestReference.setPalletQtyPlanned(1);

        mockMvc.perform(put("/warehouse/xd_gro/ttt/17/manifest/1000/addReference").contentType(MediaType.APPLICATION_JSON_UTF8).content(om.writeValueAsString(manifestReference)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(header().string("Error:", "Manifest with id=1000 wasn't found in TTT=17 in Warehouse xd_gro"));

    }

    /**
     * Testcase of adding the Reference to the chosen manifest.
     * The given ManifestReference entity violates annotation conditions.
     * Status response 422
     */
    @Test
    public void addReferenceToManifestTest422() throws Exception {
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Manifest manifest = manifestService.findManifestById(8L);
        long customerId = manifest.getCustomer().getCustomerID();
        long supplierId = manifest.getSupplier().getSupplierID();

        List<Reference> referenceList = referenceService.getAllReferencesBySupplierAndCustomer(supplierId, customerId);
        Assert.assertFalse(referenceList.isEmpty());

        ManifestReference manifestReference = new ManifestReference();
        manifestReference.setReference(referenceList.get(0));
        manifestReference.setQtyPlanned(100);


        mockMvc.perform(put("/warehouse/xd_gro/ttt/17/manifest/8/addReference").contentType(MediaType.APPLICATION_JSON_UTF8).content(om.writeValueAsString(manifestReference)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("manifestReference-stackability_Min", "must be greater than or equal to 1"));

    }
}
