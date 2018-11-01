//CSC316 - Project
//wsspence - W. Scott Spencer
//Spell-checker using Hash-Table

import java.util.*;
import java.io.*;

public class HashTableSpellCheck {
	
	public static HashTable table;
	//#words in dictionary
	public static int dictWords;
	//#words in input text
	public static int inputWords;
	//#Mispelled words found in text
	public static int totalMispellings;
	//#Probes used in checking phase
	public static int totalProbes;
	//#lookUp operations performed (used with total probes to calculate average probes)
	public static int totalLookUps;
	//#Probes used on average per lookUp operation
	public static int avgProbes;
	
	//scanners and printwriters for the file
	static PrintWriter outputFile;
	static Scanner dictionary;
	static Scanner input;
	
	public static void main(String[] args) {
		
		//initialize class counting variables to 0 
		dictWords = 0;
		inputWords = 0;
		totalMispellings = 0;
		totalLookUps = 0;
		avgProbes = 0;
		
		//prompt user for filenames of dictionary list, text file, and output file, and create
		//files and scanners from them
		Scanner console = new Scanner(System.in);
		String fileName;
		
		System.out.println("Please enter the name of the file containing the dictionary list: ");
		fileName = console.nextLine();
		File dictionaryFile = new File(fileName);
		try {
			dictionary = new Scanner(dictionaryFile);
		} catch(FileNotFoundException e) {
			System.out.println("Invalid file.  Please try again");
			System.exit(1);
		}
		
		System.out.println("Please enter the name of the file containing the text you'd like checked (please ensure words are on separate lines): ");
		fileName = console.nextLine();
		File inputFile = new File(fileName);
		try {
			input = new Scanner(inputFile);
		} catch(FileNotFoundException e) {
			System.out.println("Invalid file.  Please try again");
			System.exit(1);
		}
		
		System.out.println("Please enter the name of the file you'd like the output saved in: ");
		fileName = console.nextLine();
		try {
			outputFile = new PrintWriter(new FileWriter(fileName));
		} catch (IOException e) {
			System.out.println("IO error");
			System.exit(1);
		}
		
		//Construct HashTable
		table = new HashTable();
		buildHashTable(dictionary);
		
		//reset probes and lookUps values so probes during build of hashtable don't influence results
		totalProbes = 0;
		totalLookUps = 0;
		
		//Read input text word-by-word, checking the spelling of each word. If a word appears 
		//mispelled, the program will write it to output. (remember to check for suffixes and 
		//prefixes that may not appear in dictionary but still aren't mispellings. 
		String inWord;
		while (input.hasNextLine()) {
			inputWords++;
			inWord = input.nextLine();
			//if this word is a possible spelling error, print it and add it to number of 
			//mispellings
			if (inWord.length() >= 1 && !checkSpelling(inWord)) {
				//Print the mispelled word
				outputFile.println(inWord);
				//increment totalMispellings
				totalMispellings++;
			}
		}
		
		//Write the number of words in dictionary, number of words in text to be spell checked, 
		//number of mispelled words in text, number of probes during checking phase, and
		//the average number of probes per lookup operation to the output.
		outputFile.println("Total words in dictionary: " + dictWords);
		outputFile.println("Total words in input text: " + inputWords);
		outputFile.println("Total mispelled words: " + totalMispellings);
		outputFile.println("Total probes during checking phase: " + totalProbes);
		avgProbes = totalProbes / totalLookUps;
		outputFile.println("Average probes per lookUp operation (includes misspellings): " + avgProbes);
		avgProbes = totalProbes / inputWords;
		outputFile.println("Average probes per word (includes misspellings): " + avgProbes);
		//close output file print writer
		outputFile.close();
	}
	
	//Helper method to check word against hashtable and report whether or not it's a possible 
	//spelling error.  Returning true if it IS in the hashTable.
	public static boolean checkSpelling(String word) {
		//See if you can find the word in hashtable, if word is found, return true 
		//(Note: apostrophes are regarded as letters for our purposes.  Conjunctions like "don't"
		//will be considered mispellings unless they appear in their entirety in the dictionary)
		if (table.lookUp(word) != null) {
			return true;
		}
		
		//If the word is not found, check the following properties (if any of them find the word,
		//return true and short-circuit out of this process of checking.  This is by no means a 
		//comprehensive list, it will let some errors slide, but is good enough for our purposes
		//and should run quickly.
		//[e.g. "bakeer" would be regarded as correct]):

		
		
		//  !NOTE!: I would typically use "else if" statements to short-circuit these cases and 
		//  get a quicker runtime, but in the interest of attempting to find a word in the 
		//  dictionary that may fall under multiple of the cases below, I will NOT do that here.
		
		String tempWord;
		
		
		//If first letter of the word is capitalized, set it to lower-case and try to find it again
		//(e.g. Scott)
		if (Character.isUpperCase(word.charAt(0))) {
			tempWord = word.toLowerCase();
			if (table.lookUp(tempWord) != null) {
				return true;
			}
		}
		
		//If the word ends in "'s", remove the 's and try to find it again. (e.g. cook's)
		if (word.length() > 1 && word.substring(word.length() - 2).equals("'s")) {
			tempWord = word.substring(0, word.length() - 2);
			if (table.lookUp(tempWord) != null) {
				return true;
			}
		}
		
		//If the word ends in "s", remove the s and try to find it again; if the word is still not 
		//found, check if it ends in "es", then remove the es and try again. (e.g. cooks, dishes)
		if (word.charAt(word.length() - 1) == 's') {
			tempWord = word.substring(0, word.length() - 1);
			if (table.lookUp(tempWord) != null) {
				return true;
			}
			else if (tempWord.charAt(tempWord.length() - 1) == 'e') {
				tempWord = tempWord.substring(0, tempWord.length() - 1);
				if (table.lookUp(tempWord) != null) {
					return true;
				}
			}
		}
		
		//If the word ends in "ed", drop the "ed" and try again; if the word is still not found, 
		//remove ONLY the "d" and try to find it a third time. (e.g. cooker, baker)
		if (word.length() > 1 && word.substring(word.length() - 2).equals("ed")) {
			tempWord = word.substring(0, word.length() - 2);
			if (table.lookUp(tempWord) != null) {
				return true;
			}
			else {
				tempWord = tempWord + "e";  //since we already dropped "ed" just add "e" back
 				if (table.lookUp(tempWord) != null) {
					return true;
				}
			}
		}
		
		//If the word ends in "ing" remove the "ing" and try again; if the word is still not found,
		//REPLACE the ing with "e" and try again. (e.g. cooking, baking)
		if (word.length() > 2 && word.substring(word.length() - 3).equals("ing")) {
			tempWord = word.substring(0, word.length() - 3);
			if (table.lookUp(tempWord) != null) {
				return true;
			}
			else {
				tempWord = tempWord + "e"; //we already dropped "ing" so to replace just add "e"
				if (table.lookUp(tempWord) != null) {
					return true;
				}
			}
		}
		
		//If the word ends in "ly", drop the ly and try again (e.g. deliciously)
		if (word.length() > 1 && word.substring(word.length() - 2).equals("ly")) {
			tempWord = word.substring(0, word.length() - 2);
			if (table.lookUp(tempWord) != null) {
				return true;
			}
		}
		
		//ELSE (none of the ifs returned true)
		return false;
	}
	
	//Helper method to build hashtable, reading dictionary word-by-word
	public static void buildHashTable(Scanner dictionary) {
		//NOTE: if passing scanner as param doesn't work, construct scanner in this method and
		//pass either the file or filename as the parameter instead.
		String word;
		while (dictionary.hasNext()) {
			dictWords++;
			word = dictionary.next();
			//put word in hashtable class variable "table"
			table.insert(word);
		}
	}
	
	//DataItem data type used by the hashtable data structure nested class
	public static class DataType {
		//value stored in datatype
		String key;
		//reference pointer to a "next" datatype
		DataType next;

		//Constructor without next reference
		public DataType(String key) {
			this.key = key;
		}
		
		//Constructor with next reference
		public DataType(String key, DataType next) {
			this.key = key;
			this.next = next;
		}
		
		//getter method for returning the key this datatype contains
		public String getKey() {
			return this.key;
		}
	}
	
	//HashTable data structure nested class
	//Basically, we take the key of datatype and run it through a hashing function to return an 
	//index in our hashtable array.  Since the array does not waste memory by containing an index
	//for every possible value, DataType makes use of recursively calling another DataType, so 
	//every time some value returns the same index as another, it is stored as the "next" DataType
	//to the one already occupying that index.  This way we can find the key we are looking for 
	//without wasting memory on an overly large array, but also reduce the searching time 
	//necessary (or total number of probes) to find the key compared to storing them in an entirely
	//recursive data structure such as a binary search tree.
	public static class HashTable {
		//size of the array (capacity, not current size)
		//(our dictionary size is 25000, so we will go smaller because we don't need an array that 
		//large if we are using the separate chaining technique of hash table building)
		private final static int SIZE = 12000;
		//Array for storage of our hashtable
		DataType[] hashArray;

		
		//Constructor
		public HashTable() {
			//Initialize our array (each index is now null)
			hashArray = new DataType[SIZE];
		}
		
		//searches an element in the hash table
		public DataType lookUp(String key) {
			//increment variable for total lookUp operations
			totalLookUps++;
			
			int hash = hashCode(key);
			DataType temp = this.hashArray[hash];
			//loop for as long as value at table[hash] exists but is not equal to desired key
			while (temp != null && !temp.getKey().equals(key)) {
				//Increment variable for probes (because if temp.getKey() != key, there is a probe)
				totalProbes++;
				//change hash to "next" (if you want you can change this to a linked list in 
				//that index, rather than incrementing hash to find next available index
				temp = temp.next;
			}
			//if temp is not null, it is the data we are looking for
			if (temp != null && temp.getKey().equals(key)) {
				return temp;
			}
			//if temp is null, it was not among those listed at that index
			else {
				return null;
			}
		}
		
		//Inserts an element into the hash table
		public DataType insert(String key) {
			//handle collisions here. If a hashed index is already occupied, move to the next
			//index. This is called linear probing.
			//pointers can also linke collided data items...array of linked lists.  (WHICH 
			//you've already implemented once in Kruskal assignment)
			int hash = hashCode(key);
			
			//if table[hash] is null, set it to a new DataType storing the parameterized key
			if (this.hashArray[hash] == null) {
				this.hashArray[hash] = new DataType(key);
				return this.hashArray[hash];
			}
			//create variable temp which is equal to table[hash] (may be null)
			DataType temp = this.hashArray[hash];
			//loop until temp.next is equal to null (the end of the linked list stored in 
			//table[hash] (if temp.next is null, loop will not execute)
			while (temp.next != null) {
				temp = temp.next;
			}
			//set temp.next to a new DataType storing our parameterized key
			temp.next = new DataType(key);
			//return temp.next
			return temp.next;
		}
		
		//Deletes an element from the hash table
		public String delete(String key) {
			//Compute hashCode and look in that index for DataItem
			//Use linear probing (as described in insert function) to determine the correct 
			//index (keep checking next) if the DataItem is not found at the computed hashCode
			//Return the string of the node we delete
			int hash = hashCode(key);
			DataType toDelete = this.hashArray[hash];
			DataType slow = toDelete;
			//loop through linked list in table[hash] until temp's key equals our parameter
			while (toDelete.getKey() != key) {
				slow = toDelete;
				toDelete = toDelete.next;
			} 
			//if toDelete == slow, we are deleting the head of the list
			if (toDelete == slow) {
				//set table[hash] equal to the next node in our linked list
				this.hashArray[hash] = toDelete.next;
				return toDelete.getKey();
			}
			//set the next pointer of the node before the node we are deleting equal to the 
			//next pointer of the node we are deleting 
			//(making "pre-toDelete" point to "post-toDelete")
			slow.next = toDelete.next;
			return toDelete.getKey();
		}
		
		//Hashing function, used to convert range of values to range of array indices
		public int hashCode(String key) {
			//Use something to minimize collisions that will still give a value between 0 and SIZE
			//use 11,999 as cap since 12,000 is the size of our array
			int MAX = 11999;
			int hash = 7;
			//Loop through each car of key, multiplying by the value at the character and 
			//using its length value to increment hash
			for (int i = 0; i < key.length(); i++) {
				hash = hash*3 + key.charAt(i);
				if (hash > MAX) {
					hash = hash % 10000;
				}
			}
			return hash;
		}
	}
}