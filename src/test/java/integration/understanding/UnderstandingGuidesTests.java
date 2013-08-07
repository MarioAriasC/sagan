package integration.understanding;

import integration.IntegrationTestBase;
import integration.stubs.StubGithubService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.site.test.FixtureLoader;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClientException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnderstandingGuidesTests extends IntegrationTestBase {

	@Autowired
	private StubGithubService stubGitHubService;

	@After
	public void tearDown() throws Exception {
		stubGitHubService.clearStubs();
	}

	@Test
	public void getExistingGuide() throws Exception {
		String readmeHtml = FixtureLoader.load("/fixtures/understanding/amqp/README.html");
		stubGitHubService.addRawFileMapping(".*/README.md", readmeHtml);

		String sidebarHtml = FixtureLoader.load("/fixtures/understanding/amqp/SIDEBAR.html");
		stubGitHubService.addRawFileMapping(".*/SIDEBAR.md", sidebarHtml);

		MvcResult response = this.mockMvc.perform(get("/understanding/amqp"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith("text/html"))
				.andReturn();

		Document html = Jsoup.parse(response.getResponse().getContentAsString());
		assertThat(html.select("h1").text(), containsString("Understanding: AMQP"));
		assertThat(html.select("aside").text(), containsString("Messaging with RabbitMQ"));
	}

	@Test
	public void nonExistentGuideReturns404() throws Exception {
		stubGitHubService.addExceptionToBeThrown(new RestClientException(""));

		this.mockMvc.perform(get("/understanding/non_existent"))
				.andExpect(status().isNotFound())
				.andReturn();

	}

}