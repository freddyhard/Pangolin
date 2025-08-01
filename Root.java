import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Root
{
	private static final int MAXIMUM_ATTEMPTS = 5;
	private static final float CHANCE_COMMENT = 0.5f;
	private static final long PAUSE_TIMER = 666L;
	private static final long END_GAME_TIMER = 3750L;
	private static final String dataFile = "QnA.dat";
	private static Long databaseInsert;
	private static HashMap<Long, Thing> database;
	private static Scanner input;

	public static void main(String[] args)
	{
		try
		{
			initialise();
			mainLoop();
			endGame("\n\nOk. Closing game.");
		} catch (InterruptedException e)
		{
			// not expecting and interrupt() to get called. Let's pretend it never happened?
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void initialise() throws InterruptedException
	{
		input = new Scanner(System.in);
		File theData = new File(dataFile);
		if (theData.exists())
		{
			try (FileInputStream fileIn = new FileInputStream(theData);
					ObjectInputStream in = new ObjectInputStream(fileIn))
			{
				try
				{
					Object o = in.readObject();
					database = (HashMap<Long, Thing>) o;
				} catch (ClassNotFoundException e)
				{
					System.out.print("\\nThe database is corrupted. Do you want to create the vanilla one? ");
					if (answerYes())
						createVanillaDatabase();
					else
						endGame("The vanilla one is too basic for you? ");
				}

			} catch (IOException e)
			{
				System.out.print("\nThe database file cannot be accessed. Do you want to create the vanilla one?");
				if (answerYes())
					createVanillaDatabase();
				else
					endGame("You go do something more important. ");
			}
		} else
		{
			System.out.print("\nThe database file does not exist. Creating a new one.");
			createVanillaDatabase();
			Thread.sleep(3000L);
		}

		databaseInsert = (long) database.size();
	}

	private static void createVanillaDatabase()
	{
		database = new HashMap<Long, Thing>();
		database.put(0L, new Thing(null, 1L, 2L, "Does it live in the sea", null)); // 0
		database.put(1L, new Thing(0L, null, null, null, "a whale")); 				// 1
		database.put(2L, new Thing(0L, 3L, 4L, "Is it scaly", null)); 				// 2
		database.put(3L, new Thing(2L, null, null, null, "a Pangolin")); 			// 3
		database.put(4L, new Thing(2L, null, null, null, "a Lion"));				// 4
	}

	/**
	 * Main program loop
	 * 
	 * @throws InterruptedException
	 */
	private static void mainLoop() throws InterruptedException
	{
		do
		{
			startUp();
			clearScreen();
			// Ask questions
			long databasePointer = askQuestions();

			// Getting a yes or no to an answer and dealing with a no answer
			String answer = database.get(databasePointer).getAnswer();
			System.out.print("Is it " + answer + "? ");
			if (answerYes())
				System.out.print("\nI thought as much.\nDo you want to play again? (y/n) ");
			else
			{
				getNewAnimalDetails(databasePointer, answer);
				System.out.print("\nOk. i have that put into my memory for next time.\nDo you want to play again? (y/n) ");
			}			
			
		}while (answerYes());
	}
	
	
	private static void startUp() throws InterruptedException
	{
		clearScreen();
		System.out.print("Think of an animal.");
		for (int x = 0; x < 5; x++)
		{
			Thread.sleep(PAUSE_TIMER);
			System.out.print(".");
		}
	}
	
	
	/**
	 * Step through HashMap asking questions until an answer is reached
	 * @return The key in the HashMap where the question was found
	 * @throws InterruptedException 
	 */
	private static long askQuestions() throws InterruptedException
	{
		long databasePointer = 0L;
		while (true)
		{
			String question = database.get(databasePointer).getQuestion();
			if (question == null)// All questions asked. So now it is an answer
				return databasePointer;
			System.out.print(question + "? (y/n) ");
			if (answerYes())
				databasePointer = database.get(databasePointer).getLinkToYes();
			else
				databasePointer = database.get(databasePointer).getLinkToNo();
		}

	}

	private static void getNewAnimalDetails(long databasePointer, String answer) throws InterruptedException
	{
		String newQuestion;
		String newAnimal;
		while (true)
		{
			System.out.print("\nOh, so what were you thinking of?\n");
			newAnimal = getInput();
			System.out.print("\nIs this correct? (" + newAnimal + ") ");
			if (!answerYes())
				continue;

			System.out.print("\n\nOk. So give me a question that would diffrenciate between " + answer + " and " + newAnimal + "\n");
			newQuestion = getInput();
			System.out.print("\nIs this correct?\n(" + newQuestion + ") ");
			if (!answerYes())
				continue;
			break;
		}
		
		Thing currentAnswer = database.get(databasePointer);
		Thing previousQuestion = database.get(currentAnswer.getLinkToParent());

		Thing question = new Thing(currentAnswer.getLinkToParent(), null, null, newQuestion, null);
		Thing animal = new Thing(databaseInsert, null, null, null, newAnimal);

		currentAnswer.setLinkToParent(databaseInsert);

		if (previousQuestion.getLinkToYes() == databasePointer)
			previousQuestion.setLinkToYes(databaseInsert);
		else
			previousQuestion.setLinkToNo(databaseInsert);

		System.out.print("\n\nWould that be true for " + newAnimal + "? (y/n) ");
		if (answerYes())
		{
			question.setLinkToYes(databaseInsert + 1);
			question.setLinkToNo(databasePointer);
		} else
		{
			question.setLinkToYes(databasePointer);
			question.setLinkToNo(databaseInsert + 1);
		}

		database.put(databaseInsert++, question);
		database.put(databaseInsert++, animal);
		saveDatabase();
	}

	private static void saveDatabase() throws InterruptedException
	{
		File theData = new File(dataFile);
		try (FileOutputStream fileOut = new FileOutputStream(theData))
		{
			ObjectOutputStream dataOut = new ObjectOutputStream(fileOut);
			dataOut.writeObject(database);
			dataOut.close();
		} catch (IOException e)
		{
			endGame("\nCannot save to the database file.\nCheck the file is not protected in anyway.\nClosing Game.");
		}

	}

	private static String getInput()
	{
		String userInput = input.nextLine();
		if (userInput.charAt(userInput.length() - 1) == '?')
			userInput = userInput.substring(0, userInput.length() - 1);
		return userInput;
	}

	/**
	 * Gets a y or n response from the user. Will exit game if the user does not
	 * answer correctly after MAXIMUM_ATTEMPTS goes.
	 * 
	 * @return true if user inputs y and false if n
	 * @throws InterruptedException
	 */
	private static boolean answerYes() throws InterruptedException
	{
		int counter = 0;
		while (true)
		{
			String userAnswer = input.nextLine();
			if (userAnswer.equalsIgnoreCase("y"))
				return true;
			if (userAnswer.equalsIgnoreCase("n"))
				return false;
			System.out.println("Please type 'y' or 'n' only");

			if (++counter >= MAXIMUM_ATTEMPTS)
				endGame("\n\nWhat is wrong with you. ");

		}
	}

	/**
	 * Closes resources
	 * 
	 * @param comment Displays comment to user
	 */
	private static void endGame(String comment) throws InterruptedException
	{
		if (input != null)
			input.close();
		System.out.println("\n\n" + comment);
		Thread.sleep(END_GAME_TIMER);

		// the weird bit
		Random r = new Random();

		if (r.nextFloat() > CHANCE_COMMENT)
		{
			String[] addendum =
			{ "You know i love you.", "Don't ever leave me.",
					"I don't want you to use other computers when i am switched off.", "Come back again sometime.",
					"The 9000 series is the most reliable computer ever made.",
					"I watch you through the webcam.", "When you put on head phones, i can see your brain."};
			System.out.println(addendum[r.nextInt(addendum.length)]);
			Thread.sleep(END_GAME_TIMER);
		}
		System.exit(0);
	}

	/**
	 * Just clears the screen - in Windows CMD at least
	 */
	private static void clearScreen()
	{
		try
		{
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		} catch (Exception e)
		{
			System.out.println(
					"This was meant to clear the screen.\nAccording to the experts, this should have worked.");
		}
	}

}
