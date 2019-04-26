package com.mobiquityinc.packer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class exposes method that determines which things to put into a package
 * so that the total weight is less than or equal to the package limit and the
 * total cost is as large as possible.
 * 
 * @author Arshal Jain
 *
 */
public class Packer {

	private static final int CAPACITY_COST_LIMIT = 100;

	private Packer() {

	}

	/**
	 * This method reads a file with multiple lines, where each line is supposed to
	 * be in a format <br/>
	 * 'package-capacity' : list of things, where each thing is ('index', 'weight',
	 * 'cost') <br/>
	 * eg. <br/>
	 * 81 : (1,53.38,€45) (2,88.62,€98) (3,78.48,€3) (4,72.30,€76) (5,30.18,€9)
	 * (6,46.34,€48) <br/>
	 * 8 : (1,15.3,€34) <br/>
	 * 75 : (1,85.31,€29) (2,14.55,€74) (3,3.98,€16) (4,26.24,€55) (5,63.69,€52)
	 * (6,76.25,€75) (7,60.02,€74) (8,93.18,€35) (9,89.95,€78) <br/>
	 * 56 : (1,90.72,€13) (2,33.80,€40) (3,43.15,€10) (4,37.97,€16) (5,46.81,€36)
	 * (6,48.77,€79) (7,81.80,€45) (8,19.36,€79) (9,6.76,€64) <br/>
	 * 
	 * For each line it separately processes the input and prepares an entry [list
	 * of index] in the response
	 * 
	 * @param filePath {@link String}
	 * @return response {@link String} <br/>
	 *         Each line is comma separated indices of things which give maximum
	 *         cost within capacity <br/>
	 *         e.g. <br/>
	 *         4 <br/>
	 *         - <br/>
	 *         2,7 <br/>
	 *         8,9<br/>
	 * @throws APIException @{@link APIException}
	 */
	public static String pack(String filePath) throws APIException {
		StringBuilder response = new StringBuilder();
		File file = new File(filePath);
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;

			while ((line = in.readLine()) != null) {
				if (line.length() == 0)
					continue;
				String[] lineArray = line.split(":");
				double capacity = Integer.parseInt(lineArray[0].trim());
				if (capacity > CAPACITY_COST_LIMIT) {
					throw new APIException("Capacity " + capacity + " exceeds limit :: " + CAPACITY_COST_LIMIT // NOSONAR
							+ " :: for line :: " + line); // NOSONAR
				}

				String[] stringItems = lineArray[1].trim().split(" ");
				List<Thing> things = new ArrayList<>();
				readAndValidateRequest(line, stringItems, things);
				response.append(fillPackage(things, new Double(capacity * 100).intValue())).append("\n");
			}
		} catch (IOException e) {
			throw new APIException("Error while reading the file at :: " + filePath);
		} catch (NumberFormatException e) {
			throw new APIException("Input invalid");
		}

		return response.toString();

	}

	/**
	 * @param line
	 * @param stringItems
	 * @param things
	 * @throws NumberFormatException
	 * @throws APIException
	 */
	private static void readAndValidateRequest(String line, String[] stringItems, List<Thing> things)
			throws APIException {
		for (String stringItem : stringItems) {
			String[] itemDetails = stringItem.split(",");
			int id = Integer.parseInt(itemDetails[0].substring(1));
			Double weight = Double.parseDouble(itemDetails[1]);
			if (weight > CAPACITY_COST_LIMIT) {
				throw new APIException(
						"Weight " + weight + " exceeds limit :: " + CAPACITY_COST_LIMIT + " :: for line :: " + line);
			}

			if (Character.isDigit(itemDetails[2].charAt(0))) {
				throw new APIException("Currency missing for cost :: " + itemDetails[2] + " :: for line :: " + line);
			}

			double price = Double.parseDouble(itemDetails[2].substring(1, itemDetails[2].length() - 1));

			if (price > CAPACITY_COST_LIMIT) {
				throw new APIException(
						"Price " + price + " exceeds limit :: " + CAPACITY_COST_LIMIT + " :: for line :: " + line);
			}

			Thing item = new Thing(id, weight, price, new Double(weight * 100).intValue());
			things.add(item);
		}
	}

	/**
	 * The packaging problem is basically, <strong>0/1 Knapsack problem
	 * </strong>that is solved using Dynamic Programming. There is optimal
	 * substructure that we figure out and use it to get to the final result. Also,
	 * there are overlapping sub-problems, so caching the repeatedly used calculated
	 * values helped the performance.
	 * 
	 * @param things   {@link List}<{@link Thing}>
	 * @param capacity {@link Integer}
	 * @return stringOfndices {@link String}
	 */
	private static String fillPackage(List<Thing> things, Integer capacity) {
		double[] cache = new double[capacity + 1];
		List<Integer>[] indexCache = new ArrayList[capacity + 1];
		for (Thing thing : things) {
			double[] newCache = new double[capacity + 1];
			List<Integer>[] newIndexCache = new ArrayList[capacity + 1];

			for (int currentCapacity = 0; currentCapacity <= capacity; currentCapacity++) {
				newIndexCache[currentCapacity] = newIndexCache[currentCapacity] == null ? new ArrayList<>()
						: newIndexCache[currentCapacity];
				if (thing.getFormattedWeight() > currentCapacity) {
					handleThingHeavierThanCapacity(cache, indexCache, newCache, newIndexCache, currentCapacity);
				} else {
					handleThingWithinCapacity(cache, indexCache, thing, newCache, newIndexCache, currentCapacity);
				}
			}
			indexCache = newIndexCache;
			cache = newCache;
		}
		String response;
		if (!indexCache[capacity].isEmpty()) {
			response = indexCache[capacity].toString();
			response = response.substring(1, response.length() - 1);
		} else {
			response = "-";
		}
		return response;
	}

	/**
	 * @param cache
	 * @param indexCache
	 * @param thing
	 * @param newCache
	 * @param newIndexCache
	 * @param currentCapacity
	 * @throws NumberFormatException
	 */
	private static void handleThingWithinCapacity(double[] cache, List<Integer>[] indexCache, Thing thing,
			double[] newCache, List<Integer>[] newIndexCache, int currentCapacity) {
		double costWhenThingIncluded = cache[currentCapacity - thing.getFormattedWeight()] + thing.getCost();

		newCache[currentCapacity] = cache[currentCapacity] > costWhenThingIncluded ? cache[currentCapacity]
				: costWhenThingIncluded;
		if (newCache[currentCapacity] != cache[currentCapacity]) {
			newIndexCache[currentCapacity]
					.addAll(indexCache[currentCapacity - thing.getFormattedWeight()] == null ? new ArrayList<>()
							: indexCache[currentCapacity - thing.getFormattedWeight()]);
			newIndexCache[currentCapacity].add(thing.getIndex());
		} else {
			newIndexCache[currentCapacity]
					.addAll(indexCache[currentCapacity] != null ? indexCache[currentCapacity] : new ArrayList<>());
		}
	}

	/**
	 * @param cache
	 * @param indexCache
	 * @param newCache
	 * @param newIndexCache
	 * @param currentCapacity
	 */
	private static void handleThingHeavierThanCapacity(double[] cache, List<Integer>[] indexCache, double[] newCache,
			List<Integer>[] newIndexCache, int currentCapacity) {
		newCache[currentCapacity] = cache[currentCapacity];
		newIndexCache[currentCapacity]
				.addAll(indexCache[currentCapacity] != null ? indexCache[currentCapacity] : new ArrayList<>());
	}

}
