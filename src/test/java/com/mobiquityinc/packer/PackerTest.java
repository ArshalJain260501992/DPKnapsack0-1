package com.mobiquityinc.packer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

import com.mobiquityinc.packer.exceptions.APIException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PackerTest {

	@Value("classpath:test/resources")
	Resource resource;

	@Autowired
	ResourceLoader resourceLoader;

	@Test
	@Order(value = 0)
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

	@Test(expected = APIException.class)
	public void testExceedingWeight() throws APIException, IOException {
		File file = resourceLoader.getResource("exceedingWeightTestData").getFile();
		Packer.pack(file.getAbsolutePath());
	}

	@Test(expected = APIException.class)
	public void testExceedingCost() throws APIException, IOException {
		File file = resourceLoader.getResource("exceedingCostTestData").getFile();
		Packer.pack(file.getAbsolutePath());
	}

	@Test(expected = APIException.class)
	public void testExceedingCapacity() throws APIException, IOException {
		File file = resourceLoader.getResource("exceedingCapacityTestData").getFile();
		Packer.pack(file.getAbsolutePath());
	}

}
