package com.mobiquityinc.packer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")

public class ThingTest {

	@Value("classpath:test/resources")
	Resource resource;

	@Autowired
	ResourceLoader resourceLoader;

	@Test
	public void testSuccess() throws APIException, IOException {
		File file = resourceLoader.getResource("successTestData").getFile();
		String expectedResponse = "4\n-\n2, 7\n6, 9\n";
		assertEquals(expectedResponse, Packer.pack(file.getAbsolutePath()));
	}

	@Test(expected = APIException.class)
	public void testInvalidCost() throws APIException, IOException {
		File file = resourceLoader.getResource("invalidCostTestData").getFile();
		Packer.pack(file.getAbsolutePath());
	}

	@Test(expected = APIException.class)
	public void testUnparsableFile() throws APIException, IOException {
		File file = resourceLoader.getResource("unparsableFileTestData").getFile();
		Packer.pack(file.getAbsolutePath());
	}

}
