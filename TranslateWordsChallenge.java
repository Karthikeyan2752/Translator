package com.translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslateWordsChallenge {

	private static final String INPUT_FILE_PATH = "t8.shakespeare.txt";
	private static final String WORDS_LIST_FILE_PATH = "find_words.txt";
	private static final String DICTIONARY_FILE_PATH = "french_dictionary.csv";
	private static final String OUTPUT_FILE_PATH = "t8.shakespeare.translated.txt";
	private static final String PERFORMANCE_FILE_PATH = "performance.txt";
	private static final String FREQUENCY_FILE_PATH = "frequency.csv";

	public static void main(String[] args) {
		try {

			List<String> lines = readTextFile(INPUT_FILE_PATH);
			List<String> wordsList = readTextFile(WORDS_LIST_FILE_PATH);
			List<String[]> dictionary = readCsvFile(DICTIONARY_FILE_PATH);

			Instant startTime = Instant.now();
			Map<String, Integer> wordFrequency = new HashMap<>();
			List<String> processedLines = new ArrayList<>();
			for (String line : lines) {
				String processedLine = replaceWords(line, wordsList, dictionary, wordFrequency);
				processedLines.add(processedLine);
			}
			Instant endTime = Instant.now();

			saveTextFile(OUTPUT_FILE_PATH, processedLines);

			Duration processingTime = Duration.between(startTime, endTime);
			long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			savePerformanceMetrics(PERFORMANCE_FILE_PATH, processingTime, memoryUsed);

			saveFrequencyCSV(FREQUENCY_FILE_PATH, dictionary, wordFrequency);

			System.out.println("Task completed successfully!");
		} catch (IOException e) {
			System.err.println("An error occurred while reading the input files: " + e.getMessage());
		}
	}

	private static void saveTextFile(String filePath, List<String> lines) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (String line : lines) {
				writer.write(line);
				writer.newLine();
			}
		}
	}

	private static String replaceWords(String line, List<String> wordsList, List<String[]> dictionary,
			Map<String, Integer> wordFrequency) {
		for (String word : wordsList) {
			for (String[] entry : dictionary) {
				String englishWord = entry[0];
				String frenchWord = entry[1];
				if (word.equalsIgnoreCase(englishWord)) {
					int frequency = countWordFrequency(line, word);
					line = line.replaceAll("\\b" + word + "\\b", frenchWord);
					if (frequency > 0) {
						wordFrequency.put(englishWord, wordFrequency.getOrDefault(englishWord, 0) + frequency);
					}
					break;
				}
			}
		}
		return line;
	}

	private static int countWordFrequency(String line, String word) {
		int frequency = 0;
		String[] words = line.split("\\b");
		for (String w : words) {
			if (w.equalsIgnoreCase(word)) {
				frequency++;
			}
		}
		return frequency;
	}

	private static List<String> readTextFile(String filePath) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		return lines;
	}

	private static List<String[]> readCsvFile(String filePath) throws IOException {
		List<String[]> data = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] row = line.split(",");
				data.add(row);
			}
		}
		return data;
	}

	private static void savePerformanceMetrics(String filePath, Duration processingTime, long memoryUsed)
			throws IOException {
		String timeTaken = formatDuration(processingTime);
		String memoryUsedStr = formatMemory(memoryUsed);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write("Time to process: " + timeTaken);
			writer.newLine();
			writer.write("Memory used: " + memoryUsedStr);
		}
	}

	private static String formatDuration(Duration duration) {
		long minutes = duration.toMinutes();
		long seconds = duration.getSeconds() % 60;
		return minutes + " minutes " + seconds + " seconds";
	}

	private static String formatMemory(long bytes) {
		long kb = bytes / 1024;
		long mb = kb / 1024;
		return mb + " MB";
	}

	private static void saveFrequencyCSV(String filePath, List<String[]> dictionary, Map<String, Integer> wordFrequency)
			throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write("English word,French word,Frequency");
			writer.newLine();
			for (String[] entry : dictionary) {
				String englishWord = entry[0];
				String frenchWord = entry[1];
				int frequency = wordFrequency.getOrDefault(englishWord, 0);
				writer.write(englishWord + "," + frenchWord + "," + frequency);
				writer.newLine();
			}
		}
	}
}
