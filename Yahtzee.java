
/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.ArrayList;
import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		while (true) {
			nPlayers = dialog.readInt("Enter number of players");
			
			//checks if entered number is less then 5
			if (nPlayers >= 1 && nPlayers < MAX_PLAYERS)
				break;
		}

		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		categoriesMatrix = new int[nPlayers][N_CATEGORIES];
		addScoreMatrix();
		playGame();
	}

	//creates matrix that saves 
	private void addScoreMatrix() {
		scoresMatrix = new int[4][nPlayers];
	}

	//game starts here
	private void playGame() {
		for (int i = 0; i < N_SCORING_CATEGORIES; i++) {
			for (int j = 1; j <= nPlayers; j++) {
				tries(j);
				updateTotal();
			}
		}
		
		setfinalScores();
		updateTotal();		
		updateFinalLabel();
		
	}

	//prints the winner
	private void updateFinalLabel() {
		String str = "";
		createWinnersList();
		
		//string for winners' names
		for (int i = 0; i < winners.size(); i++){
			str += (playerNames[winners.get(i)] + ", ");
		}
		
		//if there is one player
		if(nPlayers == 1){
			display.printMessage("You have the total score of " + scoresMatrix[3][0]);
		}
		//different label if it's a draw
		else if(winners.size() > 1){
			display.printMessage("It's a draw! Congretulations " +  str + " you are the winners with the same total score of " + maxScore);
		}
		//if we have one winner
		else if (winners.size() == 1){
			display.printMessage("Congretulations " +  str + " you are the winner with the total score of " + maxScore);
		}
		
	}
	
	
	//creates arraylist and adds indexes of the players with the highest score
	private void createWinnersList(){
		maxScore = 0;
		for (int i = 0; i < nPlayers; i++){
			if(scoresMatrix[3][i] > maxScore){
				maxScore = scoresMatrix[3][i];
			}
		}
		for (int i = 0; i < nPlayers; i++){
			if(scoresMatrix[3][i] == maxScore){
				winners.add(i);
			}
		}
	}
	
	//this method sets upper, lower and bonus scores
	private void setfinalScores() {
		for (int i = 0; i < nPlayers; i++) {
			
			//adds bonus score if the upper score is more than 63
			if (scoresMatrix[0][i] >= 63) {
				scoresMatrix[1][i] = 35;
			}
			//updates scores on the display
			display.updateScorecard(UPPER_SCORE, i + 1, scoresMatrix[0][i]);
			display.updateScorecard(UPPER_BONUS, i + 1, scoresMatrix[1][i]);
			display.updateScorecard(LOWER_SCORE, i + 1, scoresMatrix[2][i]);
		}

	}
	

	//changer total score after each roll
	private void updateTotal() {
		for (int c = 0; c < nPlayers; c++) {
			int total = 0;
			for (int r = 0; r < 3; r++) {
				total += scoresMatrix[r][c];
			}
			display.updateScorecard(TOTAL, c + 1, total);
			scoresMatrix[3][c] = total;
		}
	}

	//player rolls the dice
	private void tries(int j) {
		display.printMessage(playerNames[j - 1] + "'s turn! Click <Roll Dice> button to roll the dice");
		display.waitForPlayerToClickRoll(j);
		int[] dice = new int[] { rgen.nextInt(1, 6), rgen.nextInt(1, 6), rgen.nextInt(1, 6), rgen.nextInt(1, 6),
				rgen.nextInt(1, 6) };
		display.displayDice(dice);
		//re-rolls the dice two times
		for (int i = 0; i < 2; i++) {
			display.printMessage("select the dice you want to re-roll and click <Roll Again>");
			display.waitForPlayerToSelectDice();
			newDice(dice);
			display.displayDice(dice);
		}
		display.printMessage("Select a category for this roll");
		changeScores(j, dice);
	}

	private void changeScores(int j, int[] dice) {
		//player chooses the category
		int category = display.waitForPlayerToSelectCategory();
		
		//checks if this category is already chosen
		while (matrixContains(j, category)) {
			//waits until the player chooses the free category
			display.printMessage("This category is already chosen. Please select another one");
			category = display.waitForPlayerToSelectCategory();
		}
		
		//counts score according to the category
		int score = score(dice, category);
		display.updateScorecard(category, j, score);
		
		//score is added to the UpperScore
		if (category >= 1 && category <= 6) {
			scoresMatrix[0][j - 1] += score;
			
			//score is added to the LowerScore
		} else if (category >= 9 && category <= 15) {
			scoresMatrix[2][j - 1] += score;
		}

		// add category to remember chosen ones
		matrixAdd(j, category);
	}

	//saves chosen category
	private void matrixAdd(int j, int category) {
		categoriesMatrix[j - 1][category - 1] = category;
	}

	
	//checks if the category is free or not
	private boolean matrixContains(int j, int category) {
		if (categoriesMatrix[j - 1][category - 1] != 0) {
			return true;
		}
		return false;
	}

	//counts the score
	private int score(int[] dice, int category) {
		
		if (category >= 1 && category <= 6) {
			return sameSums(category, dice);
		} else if (category == THREE_OF_A_KIND) {
			boolean p = checkNofAKind(dice, 3);
			if (p == true) {
				// returns sum of every die
				return sumAll(dice);
			}
		} else if (category == FOUR_OF_A_KIND) {
			boolean p = checkNofAKind(dice, 4);
			if (p == true) {
				// returns sum of every die
				return sumAll(dice);
			}
		} else if (category == FULL_HOUSE) {
			boolean p = checkForFullHouse(dice);
			if (p == true) {
				return 25;
			}
		} else if (category == SMALL_STRAIGHT) {
			boolean p = straights(dice, 4);
			if (p == true) {
				return 30;
			}
		} else if (category == LARGE_STRAIGHT) {
			boolean p = straights(dice, 5);
			if (p == true) {
				return 40;
			}
		} else if (category == YAHTZEE) {
			boolean p = checkNofAKind(dice, 5);
			if (p == true) {
				return 50;
			}
		} else if (category == CHANCE) {
			//sums up every die
			return sumAll(dice);
		}

		return 0;
	}

	// sums up every die
	private int sumAll(int[] dice) {
		int sum = 0;
		for (int i = 0; i < dice.length; i++) {
			sum += dice[i];
		}
		return sum;
	}

	// sums up specific dice
	private int sameSums(int category, int[] dice) {
		int sum = 0;
		for (int i = 0; i < dice.length; i++) {
			if (dice[i] == category) {
				sum += category;
			}
		}
		return sum;
	}

	//if the die is selected this method randomly chanages its value
	private void newDice(int[] dice) {
		for (int i = 0; i < N_DICE; i++) {
			if (display.isDieSelected(i)) {
				dice[i] = rgen.nextInt(1, 6);
			}
		}
	}

	//checks if there are n dice with same value
	private boolean checkNofAKind(int[] dice, int n) {
		for (int i = 1; i <= 6; i++) {
			int counter = 0;
			for (int j = 0; j < dice.length; j++) {
				if (dice[j] == i) {
					counter++;
				}
			}
			if (counter >= n) {
				return true;
			}
		}
		return false;
	}

	//checks if it is full house
	private boolean checkForFullHouse(int[] dice) {
		for (int i = 1; i <= 6; i++) {
			for (int j = i + 1; j <= 6; j++) {
				int ni = 0;
				int nj = 0;
				for (int n = 0; n < dice.length; n++) {
					if (dice[n] == i)
						ni++;
					if (dice[n] == j)
						nj++;
				}
				if ((nj == 3 && ni == 2) || (nj == 2 && ni == 3)) {
					return true;
				}
			}
		}
		return false;
	}

	//checks if there is small straight or large straight
	//if we check dice for small straight n is 4
	//if we chack dice for large straight n is 5
	private boolean straights(int[] dice, int n) {
		for (int i = 1; i + n <= 7; i++) {
			if (momdevnoebi(dice, i, n)) {
				return true;
			}
		}
		return false;
	}

	//checks if there are n number of following values in the values of dice
	private boolean momdevnoebi(int[] dice, int i, int n) {
		for (int j = 0; j < n; j++) {
			if (!contains(dice, i)) {
				return false;
			}
			i++;
		}
		return true;
	}

	//checks if array contains i
	private boolean contains(int[] dice, int i) {
		for (int element : dice) {
			if (element == i) {
				return true;
			}
		}
		return false;
	}

	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int[][] categoriesMatrix;
	private int[][] scoresMatrix;
	ArrayList<Integer> winners = new ArrayList<Integer>();
	private int maxScore;

}
