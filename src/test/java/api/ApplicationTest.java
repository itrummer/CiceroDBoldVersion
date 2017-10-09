package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import planning.config.Config;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTest extends TestCase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MainController controller;

    @Test
    public void canMakePostRequest() throws Exception {
        TestInstance testInstance = new TestInstance();
        testInstance.setAlgorithm("naive");
        testInstance.setConfig(new Config());
        testInstance.setCsvHeader("restaurant:STRING,price:STRING");
        testInstance.setCsvBody("Cafe Cent Dix,high\nCollege Town Bagels,medium");

        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writeValueAsString(testInstance);
        System.out.println(jsonContent);
        this.mockMvc.perform(post("/test")
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void contextLoads() throws Exception {
        assertNotNull(controller);
    }
}