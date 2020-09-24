package pl.com.xdms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.xdms.domain.tpa.TpaDaysSetting;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created on 14.06.2020
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
public class TpaDaysSettingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    /**
     * Test of attempt to get schedule of outbound trucks from Warehouse to particular CUstomer
     * using Id WhCustomer and Id of workingDay
     * @throws Exception for mockMvc
     */
    @Test
    public void getTpaDaySettingsByWhCustomerAndWorkingDayTest200() throws Exception {
        mockMvc.perform(get("/warehouse/xd_std/tpa_settings/16/4"))
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    /**
     * Case when Id of given WorkingDay is out of range
     * @throws Exception for mockMvc
     */
    @Test
    public void getTpaDaySettingsByWhCustomerAndWorkingDayTest404() throws Exception {
        mockMvc.perform(get("/warehouse/xd_std/tpa_settings/16/9"))
                .andDo(print())
                .andExpect(status().is(404));
    }

    /**
     * Case when user tries to get Settings for the pare Warehouse Customer which is not active
     * @throws Exception for mockMvc
     */
    @Test
    public void getTpaDaySettingsByWhCustomerAndWorkingDayTest422() throws Exception {
        mockMvc.perform(get("/warehouse/cc_swie/tpa_settings/3/3"))
                .andDo(print())
                .andExpect(status().is(422));
    }

    /**
     * Test case of attempt to update TpaDaysSetting entity by sending JSON with proper information.
     * @throws Exception mockMvc
     * Response 200.
     */
    @Test
    public void updateTpaDaysSettingsTest200() throws Exception{
        ObjectMapper om = new ObjectMapper();
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        //Opening of Transaction
        entityManager.getTransaction().begin();
        TpaDaysSetting tpaDaysSetting = entityManager.find(TpaDaysSetting.class, 65L); //(65, '12:00', 'P1DT1H0M', 19, 3),
        entityManager.detach(tpaDaysSetting);

        tpaDaysSetting.setTransitTime("P0DT10H20M");
        tpaDaysSetting.setLocalTime("13:20");
        entityManager.close();

        String json = om.writeValueAsString(tpaDaysSetting);

        mockMvc.perform(put("/warehouse/txd_std/tpa_settings").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transitTime").value("P0DT10H20M"))
                .andExpect(jsonPath("$.localTime").value("13:20"))
                .andExpect(header().string("Message:", "TpaDaysSetting with id=65 was updated"));

    }

    /**
     * Test case of attempt to update TpaDaysSetting entity by sending JSON with Id which not existing in DB.
     * @throws Exception mockMvc
     * Response 404.
     */
    @Test
    public void updateTpaDaysSettingsTest404() throws Exception{
        TpaDaysSetting tpaDaysSetting = new TpaDaysSetting();

        tpaDaysSetting.setId(5550L);
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(tpaDaysSetting);

        mockMvc.perform(put("/warehouse/txd_std/tpa_settings").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(404))
                .andExpect(header().stringValues("Error:","TpaDaysSetting with id=5550 wasn't found"));
    }

    /**
     * Test case of attempt to update TpaDaysSetting entity by sending JSON with existing ID but with violation of
     * annotation conditions of TpaDaysSetting class.
     * @throws Exception mockMvc
     * Response 412.
     */
    @Test
    public void updateTpaDaysSettingsTest412() throws Exception{
        TpaDaysSetting tpaDaysSetting = new TpaDaysSetting();

        tpaDaysSetting.setId(50L);
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(tpaDaysSetting);

        mockMvc.perform(put("/warehouse/xd_gro/tpa_settings").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(412))
                .andExpect(header().stringValues("tpaDaysSetting-localTime_NotEmpty","must not be empty"))
                .andExpect(header().stringValues("tpaDaysSetting-localTime_NotNull","must not be null"));
    }

    /**
     * Test case of attempt to update TpaDaysSetting entity by sending JSON with null ID
     * @throws Exception mockMvc
     * Response 400.
     */
    @Test
    public void updateTpaDaysSettingsTest400() throws Exception{
        TpaDaysSetting tpaDaysSetting = new TpaDaysSetting();
        ObjectMapper om = new ObjectMapper();
        String json = om.writeValueAsString(tpaDaysSetting);

        mockMvc.perform(put("/warehouse/xd_gro/tpa_settings").contentType(MediaType.APPLICATION_JSON_UTF8).content(json))
                .andDo(print())
                .andExpect(status().is(400));
    }
}
