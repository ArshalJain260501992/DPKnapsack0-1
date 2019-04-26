package com.mobiquityinc.packer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Packer {

//	public static void main(String[] args) throws APIException {
//		System.out.println(
//				pack("C:\\Users\\Arshal Jain\\Documents\\eclipseSpace\\DPKnapsack\\src\\main\\resources\\TestFile"));
//	}

	public static String pack(String filePath) throws APIException {
		StringBuilder response = new StringBuilder();
		File file = new File(filePath);
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = in.readLine()) != null) {
				if (line.length() == 0)
					continue;
				String[] lineArray = line.split(":");
				double capacity = Integer.parseInt(lineArray[0].trim());
				String[] stringItems = lineArray[1].trim().split(" ");
				List<Thing> things = new ArrayList<>();
				for (String stringItem : stringItems) {
					String[] itemDetails = stringItem.split(",");
					int id = Integer.parseInt(itemDetails[0].substring(1));
					Double weight = Double.parseDouble(itemDetails[1]);
					if (Character.isDigit(itemDetails[2].charAt(0))) {
						throw new APIException(
								"Currency missing for cost :: " + itemDetails[2] + " :: for line :: " + line);
					}
					double price = Double.parseDouble(itemDetails[2].substring(1, itemDetails[2].length() - 1));
					Thing item = new Thing(id, weight, price, new Double(weight * 100).intValue());
					things.add(item);
				}
				response.append(fillPackage(things, new Double(capacity * 100).intValue())).append("\n");
			}
		} catch (IOException e) {
			throw new APIException("Error while reading the file at :: " + filePath);
		} catch (NumberFormatException e) {
			throw new APIException("Input invalid");
		}

		return response.toString();

	}

	public static String fillPackage(List<Thing> things, Integer capacity) {
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
