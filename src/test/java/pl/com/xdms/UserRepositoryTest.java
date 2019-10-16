package pl.com.xdms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.repository.RoleRepository;
import pl.com.xdms.repository.UserRepository;

/**
 * Created on 16.10.2019
 * by Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@Sql(value = {"/sql_scripts/create_roles_before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//@ActiveProfiles("test")
//@TestPropertySource(locations="classpath:application.properties")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role roleForTests;

    @Before
    public void setUpRole(){
        this.roleForTests = roleRepository.findAll().get(0);
    }

    @Test
    public void whenFindByName_thenReturnUser() {
        // given
        User alex = new User(null,"AlexO", "passwordddd", "alex", "testAlex", "email@ert.tu", roleForTests);
        entityManager.persist(alex);
        entityManager.flush();
        // when
        User found = userRepository.findByUsername(alex.getUsername());
        // then
        Assert.assertEquals(found.getUsername(), alex.getUsername());
    }

    @Test
    public void whenFindById_thenReturnUser() {
        //When
        User alex = new User(null,"AlexO", "passwordddd", "alex", "testAlex", "email@ert.tu", roleForTests);
        entityManager.persist(alex);
        entityManager.flush();
        User found = userRepository.findById(1L).get();

        Assert.assertEquals(found.getUsername(), alex.getUsername());
    }
}
