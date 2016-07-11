//Program:      Teeling
//Course:       COSC470
//Description:  Permits two programs, each using this control structure (but each with additional
//              customized classes and/or methods)to play Teeling (i.e, against each other).
//Author:       
//Revised:      

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
//***************************************************************************************************
//***************************************************************************************************
//Class:        Teeling
//Description:  Main class for the program. Allows set-up and plays one side.

public class Teeling {

    public static char myColor = '?';           //B (black) or W (white) - ? means not yet selected
    public static char opponentColor = '?';     //ditto but opposite
    public static String[][] spaces;//this will be the same rows and columns as the board and will hold the two letters corresponding
    //to the space in the board. index # will match up correctly with letters (0,0 holds AA, 0,1 holds AB, 2,2 holds CC, etc.)
    //populate after asking for board size and use a nested loop and String next = String.valueOf( (char) (charValue + 1)) to increment letters;
    //INSERT ANY ADDITIONAL GLOBAL VARIABLES HERE
    //===========================================
    //===========================================
    ______________________________________________________________________________________________________________________________________
    public static int desiredDepth, origDesiredDepth;
    ______________________________________________________________________________________________________________________________________
    //===========================================
    //===========================================
    //***************************************************************************************************
    //Method:		main
    //Description:	Calls routines to play Teeling Othello
    //Parameters:	none
    //Returns:		nothing
    //Calls:        loadBoard, saveBoard, showBoard, constructor in Board class
    //              getCharacter, getInteger, getKeyboardInput, constructor in KeyboardInputClass
    //              getNextValidMoves, getScore

    public static void main(String args[]) {
        //INSERT ANY ADDITIONAL CONTROL VARIABLES HERE
        //============================================
        //============================================
        //____________________________________________________________________________________________________________________________________
        ArrayList<Object[]> nextValidMoves;//arraylist of moves. one list is a move. it holds 2 index arrays which hold the rows and columns to be changed to my color
        int mode;
        TreeBoard rootBoard;
        long moveProcessStartTime, moveProcessEndTime;
               //____________________________________________________________________________________________________________________________________
        //============================================
        //============================================
        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        int pollDelay = 250;
        long moveStartTime, moveEndTime, moveGraceTime = 10000;     //times in milliseconds
        Board currentBoard = Board.loadBoard();
        String myMove = "", myColorText = "";
        System.out.println("--- Othello ---");
        System.out.println("Player: Brandon\n");
        if (currentBoard != null) {                                 //board found, make sure it can be used
            if (currentBoard.status == 1) {                          //is a game in progress?   
                if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "A game appears to be in progress. Abort it? (Y/N (default = Y)") == 'Y') {
                    currentBoard = null;
                } else {
                    System.out.println("Exiting program. Try again later...");
                    System.exit(0);
                }
            }
        }
        if ((currentBoard == null) || (currentBoard.status == 2)) {   //create a board for a new game
            int rows = 8;
            int cols = 8;
            if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "Use standard board? (Y/N: default = Y):") == 'N') {
                rows = keyboardInput.getInteger(true, rows, 4, 26, "Specify the number of rows for the board (default = " + rows + "):");
                cols = keyboardInput.getInteger(true, cols, 4, 26, "Specify the number of columns for the board (default = " + cols + "):");
            }
            int maxTime = 60;
            maxTime = keyboardInput.getInteger(true, maxTime, 10, 600, "Max time (seconds) allowed per move (Default = " + maxTime + "):");
            currentBoard = new Board(rows, cols, maxTime);
            while (currentBoard.saveBoard() == false) {
            }               //try until board is saved (necessary in case of access conflict)
        }

        //INSERT CODE HERE FOR ANY ADDITIONAL SET-UP OPTIONS
        //==================================================
        //____________________________________________________________________________________________________________________________________
        mode = keyboardInput.getInteger(true, 3, 1, 3, "Choose mode of play (Enter 1-3)\n1. Random\n2. Manual\n3. Intelligent (Default)");
        if (mode == 3) {
            desiredDepth = keyboardInput.getInteger(true, 8, 1, 100, "Enter depth limit for intelligent search. (1-100. Default = 8 (best))");
            origDesiredDepth = desiredDepth;
        }//end of if
        //____________________________________________________________________________________________________________________________________
        //==================================================
        spaces = new String[currentBoard.boardRows][currentBoard.boardCols];
        char rowLetter = 'A', colLetter;
        String space;
        for (int i = 0; i < spaces.length; i++) {
            colLetter = 'A';
            for (int j = 0; j < spaces[0].length; j++) {
                space = "";
                space += rowLetter;
                space += colLetter;
                spaces[i][j] = space;
                colLetter++;
            }//end of loop
            rowLetter++;
        }//end of loop
        //==================================================
        //==================================================
        //At this point set-up must be in progress so colors can be assigned
        if (currentBoard.colorSelected == '?') {                    //if no one has chosen a color yet, choose one (player #1)
            myColor = keyboardInput.getCharacter(true, 'B', "BW", 1, "Select color: B=Black; W=White (Default = Black):");
            currentBoard.colorSelected = myColor;

            while (currentBoard.saveBoard() == false) {
            }               //try until the board is saved
            System.out.println("You may now start the opponent's program...");
            while (currentBoard.status == 0) {                      //wait for other player to join in
                currentBoard = null;                                //get the updated board
                while (currentBoard == null) {
                    currentBoard = Board.loadBoard();
                }
            }
        } else {                                                      //otherwise take the other color (this is player #2)
            if (currentBoard.colorSelected == 'B') {
                myColor = 'W';
            } else {
                myColor = 'B';
            }
            currentBoard.status = 1;                                //by now, both players are engaged and play can begin
            while (currentBoard.saveBoard() == false) {
            }               //try until the board is saved
        }

        if (myColor == 'B') {
            myColorText = "Black";
            opponentColor = 'W';
        } else {
            myColorText = "White";
            opponentColor = 'B';
        }
        System.out.println("This player will be " + myColorText + "\n");

        //INSERT CODE HERE FOR ANY ADDITIONAL OUTPUT OPTIONS
        //==================================================
        //==================================================
        //==================================================
        //==================================================
        //Now play can begin. (At this point each player should have an identical copy of currentBoard.)
        while (currentBoard.status == 1) {
            if (currentBoard.whoseTurn == myColor) {
                if (currentBoard.whoseTurn == 'B') {
                    System.out.println("Black's turn to move...");
                } else {
                    System.out.println("White's turn to move");
                }
                currentBoard.showBoard();
                String previousMove = currentBoard.move;
                moveStartTime = System.currentTimeMillis();

                //CALL METHOD(S) HERE TO SELECT AND MAKE A VALID MOVE
                //===================================================
                //===================================================
                //____________________________________________________________________________________________________________________________________
                rootBoard = new TreeBoard(currentBoard.board);
                rootBoard.score = -999999999;//rootBoard is max, set score to -999999999 initially because it will take the best
                nextValidMoves = getNextValidMoves(rootBoard, true);
                int validMovesNum = nextValidMoves.size();
                if (nextValidMoves.isEmpty()) {//no valid moves
                    myMove = "";
                } else {//choose a move 
                    Random random = new Random();
                    int nextMoveI = 0, curMoveScore;
                    int[] moveIndex;
                    TreeBoard moveBoard;
                    boolean differentScores = false;
                    if (mode == 3) {//intelligent mode
                        System.out.println("Choosing move....");
                        boolean past3QuartersTime = false;
                        long[] howLongMovesShouldTake = new long[validMovesNum - 1];
                        long allowedTime = currentBoard.maxMoveTime * 1000 + moveGraceTime, howLongOneMoveShouldTake = allowedTime / validMovesNum, howLongThisMoveShouldTake = 0, curTime;
                        for (int i = 0; i < validMovesNum - 1; i++) {
                            howLongThisMoveShouldTake += howLongOneMoveShouldTake;
                            howLongMovesShouldTake[i] = howLongThisMoveShouldTake;
                        }//end of loop
                        moveProcessStartTime = System.currentTimeMillis();
                        for (int i = 0; i < validMovesNum; i++) {
                            moveIndex = (int[]) nextValidMoves.get(i)[0];
                            moveBoard = (TreeBoard) nextValidMoves.get(i)[1];
                            moveBoard.parent = rootBoard;
                            curMoveScore = getScore(moveBoard, 1, false);//depth 1, MIN
                            if (curMoveScore > rootBoard.score) {
                                rootBoard.score = curMoveScore;
                                nextMoveI = i;
                            } else if (!differentScores && curMoveScore < rootBoard.score) {
                                differentScores = true;
                            }//end of if
                            if (i != validMovesNum - 1) {
                                moveProcessEndTime = System.currentTimeMillis();
                                curTime = moveProcessEndTime - moveProcessStartTime;
                                if ((moveProcessEndTime - moveProcessStartTime) >= howLongMovesShouldTake[i]) {
                                    desiredDepth--;
                                }//end of if
                                if (!past3QuartersTime && curTime >= allowedTime * .75) {
                                    desiredDepth--;
                                    past3QuartersTime = true;
                                }//end of if
                            }//end of if
                        }//end of loop
                        if (!differentScores) {//if scores were all the same choose move randomly
                            nextMoveI = random.nextInt(nextValidMoves.size());
                        }//end of if
                    } else if (mode == 2) {//manual mode
                        System.out.println("The following moves are valid: ");
                        for (int i = 0; i < nextValidMoves.size(); i++) {
                            moveIndex = (int[]) nextValidMoves.get(i)[0];
                            System.out.println((i + 1) + ". " + spaces[moveIndex[0]][moveIndex[1]]);
                        }//end of loop
                        nextMoveI = (keyboardInput.getInteger(true, 1, 1, nextValidMoves.size(), "Choose a move. (1-" + nextValidMoves.size() + ". Default = 1)") - 1);
                    } else {
                        nextMoveI = random.nextInt(nextValidMoves.size());
                    }//end of ifs and elses
                    moveIndex = (int[]) nextValidMoves.get(nextMoveI)[0];
                    moveBoard = (TreeBoard) nextValidMoves.get(nextMoveI)[1];
                    myMove = spaces[moveIndex[0]][moveIndex[1]];
                    currentBoard.board = moveBoard.boardState;
                    currentBoard.whoseTurn = opponentColor;
                    desiredDepth = origDesiredDepth;//resetting depth
                }//end of else
                //____________________________________________________________________________________________________________________________________
                //===================================================
                //===================================================
                //YOU MAY ADD NEW CLASSES AND/OR METHODS BUT DO NOT
                //CHANGE ANY EXISTING CODE BELOW THIS POINT
                moveEndTime = System.currentTimeMillis();
                if ((moveEndTime - moveStartTime) > (currentBoard.maxMoveTime * 1000 + moveGraceTime)) {
                    System.out.println("\nMaximum allotted move time exceeded--Opponent wins by default...\n");
                    keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
                    currentBoard.status = 2;
                    while (currentBoard.saveBoard() == false) {
                    }       //try until the board is saved
                    System.exit(0);
                }

                if (myMove.length() != 0) {
                    System.out.println(myColorText + " chooses " + myMove + "\n");
                    currentBoard.showBoard();
                    System.out.println("Waiting for opponent's move...\n");
                } else {
                    if (previousMove.length() == 0) {               //neither player can move
                        currentBoard.status = 2;                    //game over...
                        System.out.println("\nGame over!");
                        int blackScore = 0;
                        int whiteScore = 0;
                        for (int r = 0; r < currentBoard.boardRows; r++) {
                            for (int c = 0; c < currentBoard.boardCols; c++) {
                                if (currentBoard.board[r][c] == 'B') {
                                    blackScore++;
                                } else if (currentBoard.board[r][c] == 'W') {
                                    whiteScore++;
                                }
                            }
                        }
                        if (blackScore > whiteScore) {
                            System.out.println("Blacks wins " + blackScore + " to " + whiteScore);
                        } else if (whiteScore > blackScore) {
                            System.out.println("White wins " + whiteScore + " to " + blackScore);
                        } else {
                            System.out.println("Black and White tie with scores of " + blackScore + " each");
                        }
                    } else {
                        System.out.println("No move available. Opponent gets to move again...");
                    }
                }
                currentBoard.move = myMove;
                currentBoard.whoseTurn = opponentColor;
                while (currentBoard.saveBoard() == false) {
                }           //try until the board is saved
            } else {                                                   //wait a moment then poll again
                try {
                    Thread.sleep(pollDelay);
                } catch (Exception e) {
                }
            }
            currentBoard = null;                                    //get the updated board
            while (currentBoard == null) {
                currentBoard = Board.loadBoard();
            }
        }
        keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
    }
    //***************************************************************************************************
    //____________________________________________________________________________________________________________________________________
    //Method:      getnextValidMoves
    //Description: Returns an arraylist of object arrays representing moves. The object arrays contain 2 things:
    //             1) A 2 index array holding the row and column of where the new piece is being placed
    //             2) A 2d char array representing the state of the board after that move is made.
    //Returns:     An ArrayList of object arrays. Each object array holds a two index array of the row and colum of the move, and a TreeBoard
    //             representing the state of the board after that move if it is made.
    //Calls:       getPiecesFlippedForThisMove
    //Globals:     myColor, opponentColor

    public static ArrayList<Object[]> getNextValidMoves(TreeBoard board, boolean myMove) {
        ArrayList<Object[]> nextPossibleMoves = new ArrayList();
        LinkedList<int[]> flippedPieces;
        Iterator<int[]> it;
        TreeBoard nextMove;
        int[] flippedSpace;
        int boardRows = board.rows, boardCols = board.cols;
        char whoseMove = myColor, whoseMoveNot = opponentColor;//who to get next moves for
        char[][] boardState = board.boardState, nextMoveBoardState;
        if (!myMove) {
            whoseMove = opponentColor;
            whoseMoveNot = myColor;
        }//end of if
        for (int i = 0; i < boardRows; i++) {
            for (int j = 0; j < boardCols; j++) {
                if (boardState[i][j] == ' ') {
                    flippedPieces = getPiecesFlippedForThisMove(board, i, j, whoseMove, whoseMoveNot);
                    if (flippedPieces.size() != 0) {
                        flippedPieces.addFirst(new int[]{i, j});//add the space where a new piece is put to the beggining of the list
                        nextMove = new TreeBoard(boardState);
                        nextMoveBoardState = nextMove.boardState;
                        it = flippedPieces.iterator();
                        while (it.hasNext()) {
                            flippedSpace = it.next();
                            nextMoveBoardState[flippedSpace[0]][flippedSpace[1]] = whoseMove;
                        }//end of loop
                        nextPossibleMoves.add(new Object[]{flippedPieces.getFirst(), nextMove});
                    }//end of if
                }//end of if
            }//end of loop
        }//end of loop
        return nextPossibleMoves;
    }//end of method
    //***************************************************************************************************
    //Method:      getPiecesFlippedForThisMove
    //Description: Returns a list of the locations of pieces that would be flipped for the move.
    //Parameters:  board   Current state of the board.
    //             moveRow Row of the move.
    //             moveCol Column of the move.
    //             whoseMove    Indicates whose turn it is.
    //             whoseMoveNot Indicates whose turn it isn't.
    //Returns:     LinkedList<int[]> List of 2 index arrays which indicate the row and column of each 
    //                               space which has a piece that would be flipped for this move.
    //Calls:       getPiecesFlippedInThisDirection
    //Globals:     None

    public static LinkedList<int[]> getPiecesFlippedForThisMove(TreeBoard board, int moveRow, int moveCol, char whoseMove, char whoseMoveNot) {
        char[][] boardState = board.boardState;
        int[][] positions = {{moveRow - 1, moveCol}, {moveRow - 1, moveCol + 1}, {moveRow, moveCol + 1}, {moveRow + 1, moveCol + 1}, {moveRow + 1, moveCol}, {moveRow + 1, moveCol - 1}, {moveRow, moveCol - 1}, {moveRow - 1, moveCol - 1}};
        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};//directions for next method to go when checking for pieces that can be flipped
        int row, col, boardRows = board.rows, boardCols = board.cols;
        LinkedList<int[]> flippedPieces = new LinkedList(), nextFlipped;//will hold the positions of all pieces that will be flipped for this moved
        for (int i = 0; i < 8; i++) {
            row = positions[i][0];
            col = positions[i][1];
            if (row >= 0 && col >= 0 && row < boardRows && col < boardCols && boardState[row][col] == whoseMoveNot) {
                nextFlipped = getPiecesFlippedInThisDirection(board, row, col, directions[i], whoseMove, whoseMoveNot);
                if (nextFlipped != null) {
                    flippedPieces.addAll(nextFlipped);
                }//end of if
            }//end of if  
        }//end of loop
        return flippedPieces;
    }//end of method
    //***************************************************************************************************
    //Method:      getPiecesFlippedInThisDirection
    //Description: Returns a list of locations of pieces that would be flipped going in a certain direction from the
    //             space where move is made
    //Parameters:  board        Current state of the board.
    //             row          Row of a space next to the move where there is an enemy piece.
    //             col          Column of a space next to the move where there is an enemy piece.
    //             direction    2 index array containing the values to increment row and column by in order go in a certain direction on the board.
    //             whoseMove    Indicates whose turn it is.
    //             whoseMoveNot Indicates whose turn it isn't.
    //Returns:     LinkedList<int[]> List of 2 index arrays which indicate the row and column of each 
    //                               space which has a piece that would be flipped going in this direction for this move.
    //Calls:       None
    //Globals:     None

    public static LinkedList<int[]> getPiecesFlippedInThisDirection(TreeBoard board, int row, int col, int[] direction, char whoseMove, char whoseNotMove) {
        LinkedList<int[]> flippedPieces = new LinkedList();//will store the spaces that will be flipped in this direction   
        char[][] boardState = board.boardState;
        int boardRows = board.rows, boardCols = board.cols;
        while ((row >= 0 && row != boardRows && col >= 0 && col != boardCols) && boardState[row][col] == whoseNotMove) {
            flippedPieces.addFirst(new int[]{row, col});
            row += direction[0];
            col += direction[1];
        }//end of loop
        if ((row >= 0 && row != boardRows && col >= 0 && col != boardCols) && boardState[row][col] == whoseMove) {
            return flippedPieces;//loop terminated at whoseMove 
        } else {
            return null;//loop terminated at the end of the board or a blank space
        }//end of if and else
    }//end of method
    //***************************************************************************************************
    //Method:      getScore
    //Description: Recursive method which implements MINIMAX to get scores for moves down to the sesired depth level.
    //             Base case: curDepth = desiredDepth (calls and returns calculateScore for that board)
    //             else:      call getNextValidMoves to get children for the board and call getScore on each one with (curDepth + 1)
    //                        if MIN return the lowest score. if MAX return the highest
    //                        Also implements alpha beta pruning. The loop which makes the recursive call terminates if the 
    //                        parent of treeBoard has a backed up value better for it than treeBoard's score.
    //Parameters:  treeBoard Board at root of the current subtree
    //             curDepth  Indicates the depth level of the board
    //             max       Indicates MAX / whether or not it is my turn at a certain move. true = MAX/my turn, false = MIN/not my turn
    //Returns:     int score of the board (calculated or backed up).
    //Calls:       getNextValidMoves, calculateScore
    //Globals:     desiredDepth

    public static int getScore(TreeBoard treeBoard, int curDepth, boolean max) {
        if (curDepth >= desiredDepth) {
            return calculateScore(treeBoard);
        } else {
            ArrayList<Object[]> nextValidMoves = getNextValidMoves(treeBoard, max);
            if (!nextValidMoves.isEmpty()) {//additional valid moves and not at the desired depth
                TreeBoard curChild;
                int curChildScore, validMovesNum = nextValidMoves.size();
                if (max) {
                    treeBoard.score = -999999999;
                } else {
                    treeBoard.score = 999999999;
                }//end of if and else
                for (int i = 0; i < validMovesNum; i++) {
                    //ALPHA-BETA PRUNING
                    if (max && treeBoard.score >= treeBoard.parent.score) {//parent is min, treeBoard is max and has a score >= parent's score. parent won't take it or any other score treeBoard will take
                        break;
                    } else if (!max && treeBoard.score <= treeBoard.parent.score) {//parent is max, treeBoard is min and has a score <= parent's score. parent wont't take it or any other score treeBoard will take
                        break;
                    }//end of if and else
                    curChild = (TreeBoard) nextValidMoves.get(i)[1];
                    curChild.parent = treeBoard;//set the current child's parent
                    curChildScore = getScore(curChild, curDepth + 1, !max);
                    if ((max && curChildScore > treeBoard.score) || (!max && curChildScore < treeBoard.score)) {
                        treeBoard.score = curChildScore;
                    }//end of if
                }//end of loop
                return treeBoard.score;
            } else {//not at desired depth and no valid additional moves
                return calculateScore(treeBoard);
            }//end of if and else
        }//end of if and else
    }//end of method
    //***************************************************************************************************
    //Method:      calculateScore
    //Description: Calculates and returns the score of a board.
    //Parameters:  treeBoard Board to calculate a score for.
    //Returns:     int the calculated score.
    //Calls:       None
    //Globals:     myColor opponentColor

    public static int calculateScore(TreeBoard treeBoard) {
        char[][] boardState = treeBoard.boardState;
        int rows = treeBoard.rows, cols = treeBoard.cols, score = 0, validOppMoves = 0, totalFlippedNum = 0;
        LinkedList flippedPieces;
        for (int i = 1; i < rows - 1; i++) {//calculates score for non edge spaces
            for (int j = 1; j < cols - 1; j++) {
                if (boardState[i][j] == myColor) {
                    score++;
                } else if (boardState[i][j] == opponentColor) {
                    score--;
                } else {
                    flippedPieces = getPiecesFlippedForThisMove(treeBoard, i, j, opponentColor, myColor);
                    if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                        validOppMoves++;
                        totalFlippedNum += flippedPieces.size();
                    }//end of if
                }//end of ifs and elses
            }//end of if   
        }//end of loop
        for (int i = 1; i < cols - 1; i++) {//calculates score for top edge excluding corners
            if (boardState[0][i] == myColor) {
                score += 4;
            } else if (boardState[0][i] == opponentColor) {
                score -= 4;
            } else {
                flippedPieces = getPiecesFlippedForThisMove(treeBoard, 0, i, opponentColor, myColor);
                if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                    validOppMoves++;
                    totalFlippedNum += flippedPieces.size();
                }//end of if
            }//end of ifs and elses
        }//end of loop
        for (int i = 1; i < rows - 1; i++) {//calculates score for right edge excluding corners
            if (boardState[i][cols - 1] == myColor) {
                score += 4;
            } else if (boardState[i][cols - 1] == opponentColor) {
                score -= 4;
            } else {
                flippedPieces = getPiecesFlippedForThisMove(treeBoard, i, cols - 1, opponentColor, myColor);
                if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                    validOppMoves++;
                    totalFlippedNum += flippedPieces.size();
                }//end of if
            }//end of ifs and elses
        }//end of loop
        for (int i = 1; i < cols - 1; i++) {//calculates score for bottom edge excluding corners
            if (boardState[rows - 1][i] == myColor) {
                score += 4;
            } else if (boardState[rows - 1][i] == opponentColor) {
                score -= 4;
            } else {
                flippedPieces = getPiecesFlippedForThisMove(treeBoard, rows - 1, i, opponentColor, myColor);
                if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                    validOppMoves++;
                    totalFlippedNum += flippedPieces.size();
                }//end of if
            }//end of ifs and elses
        }//end of loop
        for (int i = 1; i < rows - 1; i++) {//calculates score for left edge excluding corners
            if (boardState[i][0] == myColor) {
                score += 4;
            } else if (boardState[i][0] == opponentColor) {
                score -= 4;
            } else {
                flippedPieces = getPiecesFlippedForThisMove(treeBoard, i, 0, opponentColor, myColor);
                if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                    validOppMoves++;
                    totalFlippedNum += flippedPieces.size();
                }//end of if
            }//end of ifs and elses
        }//end of loop
        if (boardState[0][0] == myColor) {
            score += 5;
        } else if (boardState[0][0] == opponentColor) {
            score -= 5;
        } else {
            flippedPieces = getPiecesFlippedForThisMove(treeBoard, 0, 0, opponentColor, myColor);
            if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                validOppMoves++;
                totalFlippedNum += flippedPieces.size();
            }//end of if
        }//end of ifs and elses
        if (boardState[0][cols - 1] == myColor) {
            score += 5;
        } else if (boardState[0][cols - 1] == opponentColor) {
            score -= 5;
        } else {
            flippedPieces = getPiecesFlippedForThisMove(treeBoard, 0, cols - 1, opponentColor, myColor);
            if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                validOppMoves++;
                totalFlippedNum += flippedPieces.size();
            }//end of if
        }//end of ifs and elses
        if (boardState[rows - 1][0] == myColor) {
            score += 5;
        } else if (boardState[rows - 1][0] == opponentColor) {
            score -= 5;
        } else {
            flippedPieces = getPiecesFlippedForThisMove(treeBoard, rows - 1, 0, opponentColor, myColor);
            if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                validOppMoves++;
                totalFlippedNum += flippedPieces.size();
            }//end of if
        }//end of ifs and elses
        if (boardState[rows - 1][cols - 1] == myColor) {
            score += 5;
        } else if (boardState[rows - 1][cols - 1] == opponentColor) {
            score -= 5;
        } else {
            flippedPieces = getPiecesFlippedForThisMove(treeBoard, rows - 1, cols - 1, opponentColor, myColor);
            if (flippedPieces != null && flippedPieces.size() != 0) {//if valid for opponent, add pieces that would be flipped to total and increment validOppMoves
                validOppMoves++;
                totalFlippedNum += flippedPieces.size();
            }//end of if
        }//end of ifs and elses
        if (validOppMoves != 0) {
            score -= (totalFlippedNum / validOppMoves);
        }//end of if
        return score;
    }//end of method
}//end of class
//*******************************************************************************************************
//*******************************************************************************************************
//Class:        TreeBoard
//Description:  Board objects with with to operate with

class TreeBoard {
    int rows;      //size of the board (allows for variations on the standard 8x8 board)
    int cols;
    char boardState[][];     //the board. Positions are filled with: blank = no piece; 'B'=black; 'W'=white
    TreeBoard parent;//each board will keep track of its own parent
    int score;//the score of a board. will be a backed up value if the board is not at the deepest allowable depth level
    //int depth;//depth level of board
//***************************************************************************************************
    //Method:       TreeBoard
    //Description:  Constructor to create a new TreeBoard object.
    //Parameters:   board The state of the board. 
    //Calls:	    None
    //Returns:	    None

    TreeBoard(char[][] board) {
        rows = board.length;
        cols = board[0].length;
        boardState = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                boardState[i][j] = board[i][j];
            }//end of loop
        }//end of loop
    }//end of method
    //***************************************************************************************************
}//end of class
//____________________________________________________________________________________________________________________________________
//*******************************************************************************************************
//*******************************************************************************************************

//Class:        Board
//Description:  Teeling board and related parms
class Board implements Serializable {

    char status;        //0=set-up for a new game is in progress; 1=a game is in progress; 2=game is over
    char whoseTurn;     //'?'=no one's turn yet--game has not begun; 'B'=black; 'W'=white
    String move;        //the move selected by the current player (as indicated by whoseTurn)
    char colorSelected; //'B' or 'W' indicating the color chosen by the first player to access the file
    //for a new game ('?' if neither player has yet chosen a color)
    //Note: this may or may not be the color for the player accessing the file
    int maxMoveTime;    //maximum time allotted for a move (in seconds)
    int boardRows;      //size of the board (allows for variations on the standard 8x8 board)
    int boardCols;
    char board[][];     //the board. Positions are filled with: blank = no piece; 'B'=black; 'W'=white
    //***************************************************************************************************
    //Method:       Board
    //Description:  Constructor to create a new board object
    //Parameters:	rows - size of the board
    //              cols
    //              time - maximum time (in seconds) allowed per move
    //Calls:		nothing
    //Returns:		nothing

    Board(int rows, int cols, int time) {
        int r, c;
        status = 0;
        whoseTurn = 'B';        //Black always makes the first move
        move = "*";
        colorSelected = '?';
        maxMoveTime = time;
        boardRows = rows;
        boardCols = cols;
        board = new char[boardRows][boardCols];
        for (r = 0; r < boardRows; r++) {
            for (c = 0; c < boardCols; c++) {
                board[r][c] = ' ';
            }
        }
        r = boardRows / 2 - 1;
        c = boardCols / 2 - 1;
        board[r][c] = 'W';
        board[r][c + 1] = 'B';
        board[r + 1][c] = 'B';
        board[r + 1][c + 1] = 'W';
    }

    //***************************************************************************************************
    //Method:       saveBoard
    //Description:  Saves the current board to disk as a binary file named "OthelloBoard"
    //Parameters:	none
    //Calls:		nothing
    //Returns:		true if successful; false otherwise
    public boolean saveBoard() {
        try {
            ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("OthelloBoard"));
            outStream.writeObject(this);
            outStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //***************************************************************************************************
    //Method:       loadBoard
    //Description:  Loads the current Teeling board and data from a binary file
    //Parameters:   none
    //Calls:        nothing
    //Returns:      a Board object (or null if routine is unsuccessful)
    public static Board loadBoard() {
        try {
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream("OthelloBoard"));
            Board boardObject = (Board) inStream.readObject();
            inStream.close();
            return boardObject;
        } catch (Exception e) {
        }
        return null;
    }

    //***************************************************************************************************
    //Method:       showBoard
    //Description:  Displays the current Teeling board using extended Unicode characters. Looks fine
    //               in a command window but may not display well in the NetBeans IDE...
    //Parameters:   none
    //Calls:        nothing
    //Returns:      nothing
    public void showBoard() {
        int r, c;
        System.out.print("  ");                         //column identifiers
        for (c = 0; c < boardCols; c++) {
            System.out.print(" " + (char) (c + 65));
        }
        System.out.println();

        //top border
        System.out.print("  " + (char) 9484);                   //top left corner \u250C
        for (c = 0; c < boardCols - 1; c++) {
            System.out.print((char) 9472);               //horizontal \u2500
            System.out.print((char) 9516);               //vertical T \u252C
        }
        System.out.print((char) 9472);                   //horizontal \u2500
        System.out.println((char) 9488);                 //top right corner \u2510

        //board rows
        for (r = 0; r < boardRows; r++) {
            System.out.print(" " + (char) (r + 65));         //row identifier
            System.out.print((char) 9474);               //vertical \u2502
            for (c = 0; c < boardCols; c++) {
                System.out.print(board[r][c]);
                System.out.print((char) 9474);           //vertical \u2502
            }
            System.out.println();

            //insert row separators
            if (r < boardRows - 1) {
                System.out.print("  " + (char) 9500);           //left T \u251C
                for (c = 0; c < boardCols - 1; c++) {
                    System.out.print((char) 9472);       //horizontal \u2500
                    System.out.print((char) 9532);       //+ (cross) \u253C
                }
                System.out.print((char) 9472);           //horizontal \u2500
                System.out.println((char) 9508);         //right T \u2524
            }
        }

        //bottom border
        System.out.print("  " + (char) 9492);                   //lower left corner \u2514
        for (c = 0; c < boardCols - 1; c++) {
            System.out.print((char) 9472);               //horizontal \u2500
            System.out.print((char) 9524);               //upside down T \u2534
        }
        System.out.print((char) 9472);                   //horizontal \u2500
        System.out.println((char) 9496);                 //lower right corner \u2518

        return;
    }
    //***************************************************************************************************

}
//*******************************************************************************************************
//*******************************************************************************************************

