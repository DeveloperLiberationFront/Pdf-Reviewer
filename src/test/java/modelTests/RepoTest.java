package modelTests;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import edu.ncsu.dlf.model.Repo;

/**
 * Tests the Repo Class
 * @author Team 19
 */
public class RepoTest {

	/**
	 * Tests the entire class
	 */
	@Test
	public void testClass() {
		Repo repo = new Repo("Nicholas Anthony", "Testing");
		
		assertTrue(repo.getRepoOwner().equals("Nicholas Anthony"));
		assertTrue(repo.getRepoName().equals("Testing"));
		
		repo.setRepoOwner("Santosh Mathew");
		repo.setRepoName("Main Project");
		
		JSONObject json = null;
		
		try {
			json = repo.toJSON();
		} catch (JSONException e) {
			fail("JSON exception");
		}
		
		try {
			assertTrue(json.get("repoOwner").equals("Santosh Mathew"));
			assertTrue(json.get("repoName").equals("Main Project"));
		} catch (JSONException e) {
			fail("Exception trying to get");
		}
	}

}
