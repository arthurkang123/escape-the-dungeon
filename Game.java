package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;
import byog.TileEngine.TERenderer;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

public class Game implements Serializable {

    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;
    TERenderer ter;
    static Character player;
    static Enemy enemy;
    static boolean enemyDead;
    static TETile enemyTile = Tileset.ENEMY;
    static boolean gameWin = false;
    static boolean gameLose = false;
    static int score = 101;
    static TETile[][] world;
    static String input1 = "";
    static String input2;
    private static long SEED;
    private static Random RANDOM;
    private static ArrayDeque roomList;
    private static ArrayDeque keyList;
    static boolean starting;

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] playWithInputString(String input) {
        String intInput;
        intInput = input.replaceAll("[^0-9]", "");
        try {
            SEED = Long.parseLong(intInput);
        } catch (NumberFormatException e) {
            System.out.println("No seed found");
        }
        System.out.println(SEED);
        RANDOM = new Random();
        roomList = new ArrayDeque();
        keyList = new ArrayDeque();
        enemyDead = false;
        RANDOM.setSeed(SEED);

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
        for (int a = 0; a < 20; a += 1) {
            TETile[][] newRoom = generateRooms();
            int newRoomX = newRoom.length; int newRoomY = newRoom[0].length;
            int randomXPos = RANDOM.nextInt(WIDTH - 15);
            int randomYPos = RANDOM.nextInt(HEIGHT - 10);
            if (randomXPos < 3) {
                randomXPos = 3;
            }
            if (randomYPos < 3) {
                randomYPos = 3;
            }
            Position p = new Position(randomXPos + newRoomX / 2, randomYPos + newRoomY / 2);
            Room r = new Room(newRoomX, newRoomY, p);
            roomList.addFirst(r); boolean overlap = false;
            for (int i = randomXPos; i < randomXPos + newRoomX; i += 1) {
                for (int j = randomYPos; j < randomYPos + newRoomY; j += 1) {
                    if (world[i][j] != Tileset.NOTHING) {
                        overlap = true; break;
                    }
                } }
            if (!overlap) {
                int x = 0; int y = 0;
                for (int i = randomXPos; i < (randomXPos + newRoomX); i += 1) {
                    for (int j = randomYPos; j < (randomYPos + newRoomY); j += 1) {
                        world[i][j] = newRoom[x][y]; y += 1;
                    }
                    y = 0; x += 1;
                } }
        }
        for (int i = 1; i < roomList.size(); i += 1) {
            drawCorridor(world, (Room) roomList.get(i), (Room) roomList.get(i - 1));
        }
        for (int y = 1; y < HEIGHT - 1; y += 1) {
            for (int x = 1; x < WIDTH - 1; x += 1) {
                if (world[x][y] == Tileset.FLOOR) {
                    continue;
                } else if ((world[x][y + 1] == Tileset.FLOOR) || (world[x][y - 1] == Tileset.FLOOR)
                        || (world[x - 1][y] == Tileset.FLOOR)
                        || (world[x + 1][y] == Tileset.FLOOR)) {
                    world[x][y] = Tileset.WALL;
                } else if (((world[x + 1][y + 1] == Tileset.FLOOR)
                        || (world[x - 1][y + 1] == Tileset.FLOOR))
                        && (world[x][y] == Tileset.NOTHING)) {
                    world[x][y] = Tileset.WALL;
                } else if (((world[x + 1][y - 1] == Tileset.FLOOR)
                        || (world[x - 1][y - 1] == Tileset.FLOOR))
                        && (world[x][y] == Tileset.NOTHING)) {
                    world[x][y] = Tileset.WALL;
                } else {
                    continue;
                }
            } }


        Room firstRoom = (Room) roomList.get(0); Room secondRoom = (Room) roomList.get(1);
        player.placeCharacter(world, firstRoom.center, 0, 0);
        enemy.placeEnemy(world, secondRoom.center, 0, 0);

        while (Key.number < 4) {
            Room currentRoom = (Room) roomList.get(RANDOM.nextInt(roomList.size() - 1));
            if (world[currentRoom.center.x][currentRoom.center.y] == Tileset.FLOOR) {
                Key.placeKey(world, currentRoom.center);
                starting = false;
            }
        }

        starting = true;
        char[] splitArray = input.toCharArray();
        if (splitArray.length > 2) {
            for (int i = 2; i < splitArray.length; i += 1) {
                keyMovement(splitArray[i]);
            }
        }
        starting = false;

        drawHUD();
        ter.renderFrame(world);
        return world;
    }

    /**
     * Creates rooms by adding floors, no walls
     */
    private static TETile[][] generateRooms() {
        int x = RANDOM.nextInt(10); //Max length is 10
        //Min length is 4
        if (x < 4) {
            x = 4;
        }
        int y = RANDOM.nextInt(10); //Max height is 10
        //Min height is 4
        if (y < 4) {
            y = 4;
        }
        TETile[][] room = new TETile[x][y];
        for (int i = 0; i < x; i += 1) {
            for (int j = 0; j < y; j += 1) {
                room[i][j] = Tileset.FLOOR;
            }
        }

        return room;
    }

    /**
     * Calculates x distance between centers of 2 rooms
     */
    private static int xDist(Room a, Room b) {
        int aX = a.center.x;
        int bX = b.center.x;
        return bX - aX;
    }

    //Example Main Method
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        playWithKeyboard();
    }
        /*
        TERenderer ter = new TERenderer();
        TETile[][] test = playWithInputString("n660s");
        ter.renderFrame(test);
        roomList.printDeque();
        */

    /**
     * Calculates y distance between centers of 2 rooms
     */
    private static int yDist(Room a, Room b) {
        int aY = a.center.y;
        int bY = b.center.y;
        return aY - bY;
    }

    private static void drawCorridor(TETile[][] map, Room a, Room b) {
        int xDist = xDist(a, b);
        int yDist = yDist(a, b);
        if (xDist < 0) {
            /** move right from room b*/
            for (int i = b.center.x; i <= a.center.x; i += 1) {
                map[i][b.center.y] = Tileset.FLOOR;
            }
        } else {
            /** moving left from room b*/
            for (int i = b.center.x; i >= a.center.x; i -= 1) {
                map[i][b.center.y] = Tileset.FLOOR;
            }
        }
        if (yDist < 0) {
            /** move up from room a*/
            for (int i = a.center.y; i <= b.center.y; i += 1) {
                /** call vWall method moving up */
                map[a.center.x][i] = Tileset.FLOOR;
            }
        } else {
            /** move down from room a*/
            for (int i = a.center.y; i >= b.center.y; i -= 1) {
                map[a.center.x][i] = Tileset.FLOOR;
            }
        }
    }

    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public static void playWithKeyboard() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 13);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        drawFrame("ESCAPE THE DUNGEON", WIDTH / 2, HEIGHT - 15);
        drawFrame("Begin your Escape (N)", WIDTH / 2, HEIGHT - 25);
        drawFrame("Continue your Escape (L)", WIDTH / 2, HEIGHT - 30);
        drawFrame("The History Behind the Game (H)", WIDTH / 2, HEIGHT - 35);
        drawFrame("Quit (Q)", WIDTH / 2, HEIGHT - 40);
        String start = solicitNCharsInput();
        StdDraw.clear();
        StdDraw.clear(Color.BLACK);
        if (start.equals("n")) {
            drawFrame("Please enter your seed.", WIDTH / 2, HEIGHT - 15);
            String seed = solicitNCharsInput();
            playWithInputString(seed);
        } else if (start.equals("l")) {
            String loadedSeed = loadWorld();
            input1 = loadedSeed;
            starting = true;
            playWithInputString(loadedSeed);
        } else if (start.equals("h")) {
            drawFrame("When the opportunity presented itself for your escape, you took it.",
                    WIDTH / 2, HEIGHT - 10);
            drawFrame("After months of captivity, this freedom feels odd, but you know that",
                    WIDTH / 2, HEIGHT - 12);
            drawFrame("you can’t spend too much time relishing it - one slip-up will end your chance.",
                    WIDTH / 2, HEIGHT - 14);
            drawFrame("Now, you are stumbling blindly about the corridors of the dungeon where",
                    WIDTH / 2, HEIGHT - 16);
            drawFrame("you have been kept. During your time in the damp, dark dungeon,",
                    WIDTH / 2, HEIGHT - 18);
            drawFrame("you know of the powerful magic that bounds it and its captives.",
                    WIDTH / 2, HEIGHT - 20);
            drawFrame("Your fellow detainees have regaled you with story after story of failed escapes,",
                    WIDTH /2, HEIGHT - 22);
            drawFrame("but also about this secret passage that leads straight to the outside world.",
                    WIDTH / 2, HEIGHT - 24);
            drawFrame("You know that this is your best chance to escape.",
                    WIDTH / 2, HEIGHT - 26);
            drawFrame("But, its entrance is only revealed after you collect the 4 magical keys spread around the dungeon and fight the jailer.",
                    WIDTH / 2, HEIGHT - 28);
            drawFrame("Spend too long in the dungeon, and you will just become another casualty,",
                    WIDTH / 2, HEIGHT - 35);
            drawFrame("but if you are quick enough in collecting the keys and defeating the jailer, you can leave this cursed dungeon behind forever.",
                    WIDTH / 2, HEIGHT - 37);
            drawFrame("And so the game begins…", WIDTH / 2, HEIGHT - 40);
            drawFrame("Press any key to restart the game", WIDTH / 2, HEIGHT - 45);
            while (true) {
                if (StdDraw.hasNextKeyTyped()) {
                    System.exit(0);
                }
            }
        }
        String movement = solicitNCharsInput();
    }

    public static void drawFrame(String s, int x, int y) {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.setPenColor(Color.white);
        StdDraw.text(x, y, s);
        StdDraw.show();
    }

    public static void drawHUD() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        while (true) {
            String tile = world[(int) StdDraw.mouseX()][(int) StdDraw.mouseY()].description();
            String keyLeft = Integer.toString(Key.number);
            String damageLeft = "Damage Made = Up: "
                    + Enemy.upHit + " | Down: " + Enemy.downHit + " | Left: "
                    + Enemy.leftHit + " | Right: " + Enemy.rightHit;


            StdDraw.setPenColor(Color.white);
            StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "Number of Keys Left: " + keyLeft);
            StdDraw.textLeft(9, HEIGHT - 1, "Score: " + score);
            StdDraw.line(0, HEIGHT - 2, WIDTH, HEIGHT - 2);
            StdDraw.textLeft(1, HEIGHT - 1, tile);
            if (gameWin) {
                StdDraw.clear();
                StdDraw.clear(Color.BLACK);
                drawFrame("Congratulations! You escaped! Your score is: " + score, WIDTH / 2, HEIGHT / 2);
                drawFrame("Press any key to exit the game.", WIDTH / 2, HEIGHT - 40);
                while (true) {
                    if (StdDraw.hasNextKeyTyped()) {
                        System.exit(0);
                    }
                }
            } else if (gameLose) {
                StdDraw.clear();
                StdDraw.clear(Color.BLACK);
                drawFrame("You have failed. You have been banished to the dungeon for the rest of eternity.", WIDTH / 2, HEIGHT / 2);
                drawFrame("Press any key to exit the game.", WIDTH / 2, HEIGHT - 40);
                while (true) {
                    if (StdDraw.hasNextKeyTyped()) {
                        System.exit(0);
                    }
                }
            } else {
                StdDraw.text(midWidth, HEIGHT - 1, damageLeft);
            }
            StdDraw.show();

            StdDraw.setPenColor(Color.black);
            StdDraw.textLeft(1, HEIGHT - 1, tile);

            StdDraw.textLeft(1, HEIGHT - 1, "floor");
            StdDraw.textLeft(1, HEIGHT - 1, "nothing");
            StdDraw.textLeft(1, HEIGHT - 1, "wall");
            StdDraw.textLeft(1, HEIGHT - 1, "player");
            StdDraw.textLeft(1, HEIGHT - 1, "flower");
            StdDraw.textLeft(1, HEIGHT - 1, "unlocked door");
            StdDraw.textLeft(1, HEIGHT - 1, "enemy");

            StdDraw.text(midWidth, HEIGHT - 1, damageLeft);
            StdDraw.textLeft(9, HEIGHT - 1, "Score: " + score);
            StdDraw.textRight(WIDTH - 1, HEIGHT - 1, "Number of Keys Left: " + keyLeft);
            if (StdDraw.hasNextKeyTyped() || starting) {
                return;
            }
        }
    }

    public static String solicitNCharsInput() {
        input2 = "";
        //drawFrame(input2, WIDTH / 2, HEIGHT / 2);

        while (true) {
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = StdDraw.nextKeyTyped();

            input1 += String.valueOf(key);
            input2 += String.valueOf(key);
            if (input2.length() > 1) {
                // @source Searched on Stack Overflow on to check if String contains integer
                // https://stackoverflow.com/questions/14206768/how-to-check-if-a-string-is-numeric
                String test = (input2.substring(input2.length() - 2));
                if (test.matches(".*\\d.*") && (key == 's')) {
                    return input2;
                } else if ((test.equals(":q"))) {
                    input1 = input1.substring(0, input1.length() - 2);
                    saveWorld(input1);
                    System.exit(0);
                }
            }
            if (key == 'n') {
                return input2;
            } else if (key == 'l') {
                return input2;
            } else if (key == 'h') {
                return input2;
            } else {
                keyMovement(key);
            }
        }
    }

    public static void keyMovement(char key) {
        if (key == 'w') {
            TERenderer ter = new TERenderer();
            player.moveCharacterVertical(world, 1);
            if ((world[player.position.x][player.position.y]
                    == world[enemy.position.x][enemy.position.y])
                    && (!enemyDead)) {
                enemy.downHit = true;
                System.out.println("down:" + enemy.downHit);
                score += 10;
                player.moveCharacterVertical(world, -1);
            }
            enemy.moveEnemy(world);
            score -= 1;
            ter.renderFrame(world);
            drawHUD();
        } else if (key == 's') {
            TERenderer ter = new TERenderer();
            player.moveCharacterVertical(world, -1);
            if ((world[player.position.x][player.position.y]
                    == world[enemy.position.x][enemy.position.y])
                    && (!enemyDead)) {
                enemy.upHit = true;
                System.out.println("up:" + enemy.upHit);
                score += 10;
                player.moveCharacterVertical(world, 1);
            }
            enemy.moveEnemy(world);
            score -= 1;
            ter.renderFrame(world);
            drawHUD();
        } else if (key == 'a') {
            TERenderer ter = new TERenderer();
            player.moveCharacterHorizontal(world, -1);
            if ((world[player.position.x][player.position.y]
                    == world[enemy.position.x][enemy.position.y])
                    && (!enemyDead)) {
                enemy.rightHit = true;
                System.out.println("right:" + enemy.rightHit);
                score += 10;
                player.moveCharacterHorizontal(world, 1);
            }
            enemy.moveEnemy(world);
            score -= 1;
            ter.renderFrame(world);
            drawHUD();
        } else if (key == 'd') {
            TERenderer ter = new TERenderer();
            player.moveCharacterHorizontal(world, 1);
            if ((world[player.position.x][player.position.y]
                    == world[enemy.position.x][enemy.position.y])
                    && (!enemyDead)) {
                enemy.leftHit = true;
                System.out.println("left:" + enemy.leftHit);
                score += 10;
                player.moveCharacterHorizontal(world, -1);
            }
            enemy.moveEnemy(world);
            score -= 1;
            ter.renderFrame(world);
            drawHUD();
        } else if (key == ':') {

        } else {
            //String splitInput2 = input2.replace("", " ").trim();
            StdDraw.clear();
            StdDraw.clear(Color.BLACK);
            drawFrame("Please enter your seed.", WIDTH / 2, HEIGHT - 15);
            StdDraw.text(WIDTH / 2, HEIGHT / 2, input2);
            StdDraw.show();
        }
        if (score <= 0) {
            gameLose = true;
        }
        if ((enemy.upHit) && (enemy.downHit)
                && (enemy.rightHit) && (enemy.leftHit) && (!enemyDead)) {
            enemyTile = Tileset.FLOOR;
            score += 100;
            enemyDead = true;
        }
    }

    // @source Got from provided SaveDemo
    private static String loadWorld() {
        File f = new File("./world.ser");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (String) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        System.out.println("There is no saved file.");
        return null;
    }


    private static void saveWorld(String w) {
        File f = new File("./world.ser");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(w);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }


    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Room {
        int width;
        int height;
        Position center;

        Room(int width, int height, Position p) {
            this.width = width;
            this.height = height;
            this.center = p;
        }
    }

    private static class Character {
        Position position;

        Character(Position p) {
            this.position = p;
        }

        private static void placeCharacter(TETile[][] map,
                                           Position position, int xMovement, int yMovement) {
            int xPos = position.x + xMovement;
            int yPos = position.y + yMovement;
            if (map[xPos][yPos] == Tileset.FLOOR || map[xPos][yPos] == Tileset.FLOWER
                    || map[xPos][yPos] == Tileset.UNLOCKED_DOOR) {
                map[position.x][position.y] = Tileset.FLOOR;
                map[xPos][yPos] = Tileset.PLAYER;
            }
            Position p = new Position(xPos, yPos);
            player = new Character(p);
        }

        private void moveCharacterHorizontal(TETile[][] map, int direction) {
            /** move left */
            if (Key.number == 0 && !Door.reveal && enemyDead) {
                Door.placeDoor(world);
            }
            if (direction < 0) {
                if (map[this.position.x - 1][this.position.y] == Tileset.WALL) {
                    placeCharacter(world, this.position, 0, 0);
                } else if (map[this.position.x - 1][this.position.y] == Tileset.FLOWER) {
                    placeCharacter(world, this.position, -1, 0);
                    Key.removeCharacter(world, new Position(this.position.x - 1, this.position.y));
                    score += 50;
                } else if (map[this.position.x - 1][this.position.y] == Tileset.UNLOCKED_DOOR) {
                    placeCharacter(world, this.position, -1, 0);
                    gameWin = true;
                } else {
                    placeCharacter(world, this.position, -1, 0);
                }
            } else { /** move right */
                if (map[this.position.x + 1][this.position.y] == Tileset.WALL) {
                    placeCharacter(world, this.position, 0, 0);
                } else if (map[this.position.x + 1][this.position.y] == Tileset.FLOWER) {
                    placeCharacter(world, this.position, 1, 0);
                    Key.removeCharacter(world, new Position(this.position.x + 1, this.position.y));
                    score += 50;
                } else if (map[this.position.x + 1][this.position.y] == Tileset.UNLOCKED_DOOR) {
                    placeCharacter(world, this.position, 1, 0);
                    gameWin = true;
                } else {
                    placeCharacter(world, this.position, 1, 0);
                }
            }
        }

        private void moveCharacterVertical(TETile[][] map, int direction) {
            /** move down */
            if (Key.number == 0 && !Door.reveal && enemyDead) {
                Door.placeDoor(world);
            }
            if (direction < 0) {
                if (map[this.position.x][this.position.y - 1] == Tileset.WALL) {
                    placeCharacter(map, this.position, 0, 0);
                } else if (map[this.position.x][this.position.y - 1] == Tileset.FLOWER) {
                    placeCharacter(map, this.position, 0, -1);
                    Key.removeCharacter(map, new Position(this.position.x, this.position.y - 1));
                    score += 50;
                } else if (map[this.position.x][this.position.y - 1] == Tileset.UNLOCKED_DOOR) {
                    placeCharacter(map, this.position, 0, -1);
                    gameWin = true;
                } else {
                    placeCharacter(map, this.position, 0, -1);
                }
            } else { /** move up */
                if (map[this.position.x][this.position.y + 1] == Tileset.WALL) {
                    placeCharacter(map, this.position, 0, 0);
                } else if (map[this.position.x][this.position.y + 1] == Tileset.FLOWER) {
                    placeCharacter(map, this.position, 0, 1);
                    Key.removeCharacter(map, new Position(this.position.x, this.position.y + 1));
                    score += 50;
                } else if (map[this.position.x][this.position.y + 1] == Tileset.UNLOCKED_DOOR) {
                    placeCharacter(map, this.position, 0, 1);
                    gameWin = true;
                } else {
                    placeCharacter(map, this.position, 0, 1);
                }
            }
        }
    }

    private static class Key {
        static int number = 0;
        Position position;

        Key(Position p) {
            this.position = p;
        }

        private static void placeKey(TETile[][] map, Position p) {
            map[p.x][p.y] = Tileset.FLOWER;
            keyList.addFirst(p);
            Key.number += 1;
            System.out.println(keyList.size());
        }

        private static void removeCharacter(TETile[][] map, Position p) {
            if (map[p.x - 1][p.y] == Tileset.PLAYER) {
                map[p.x - 1][p.y] = Tileset.FLOOR;
            } else if (map[p.x + 1][p.y] == Tileset.PLAYER) {
                map[p.x + 1][p.y] = Tileset.FLOOR;
            } else if (map[p.x][p.y - 1] == Tileset.PLAYER) {
                map[p.x][p.y - 1] = Tileset.FLOOR;
            } else if (map[p.x][p.y + 1] == Tileset.PLAYER) {
                map[p.x][p.y + 1] = Tileset.FLOOR;
            }
            Key.number -= 1;
            System.out.println(Key.number);
        }
    }

    private static class Door {
        static boolean reveal = false;
        Position position;

        Door(Position p) {
            this.position = p;
        }

        private static void placeDoor(TETile[][] map) {
            int count = 0;
            int distFromPlayer = 0;
            Room currentRoom = (Room) roomList.get(RANDOM.nextInt(roomList.size()));
            while ((distFromPlayer < 30 || count == 0)
                    && (map[currentRoom.center.x][currentRoom.center.y] != Tileset.FLOOR)) {
                currentRoom = (Room) roomList.get(RANDOM.nextInt(roomList.size()));
                distFromPlayer = (int) Math.sqrt(Math.pow(currentRoom.center.x
                        - player.position.x, 2)
                        + Math.pow(currentRoom.center.y - player.position.y, 2));
                count += 1;
            }
            map[currentRoom.center.x][currentRoom.center.y] = Tileset.UNLOCKED_DOOR;
            Door.reveal = true;
        }
    }

    public static class Enemy {
        static boolean upHit = false;
        static boolean downHit = false;
        static boolean leftHit = false;
        static boolean rightHit = false;
        Position position;

        Enemy(Position p) {
            this.position = p;
        }

        public static void placeEnemy(TETile[][] map, Position position,
                                      int xMovement, int yMovement) {
            int xPos = position.x + xMovement;
            int yPos = position.y + yMovement;
            if (map[xPos][yPos] == Tileset.FLOOR || map[xPos][yPos] == Tileset.FLOWER) {
                map[position.x][position.y] = Tileset.FLOOR;
                map[xPos][yPos] = enemyTile;
            }
            Position p = new Position(xPos, yPos);
            enemy = new Enemy(p);
        }

        public void moveEnemy(TETile[][] map) {
            ArrayDeque directionArray = new ArrayDeque();
            /** is left clear? */
            if ((map[this.position.x - 1][this.position.y] != Tileset.WALL)
                    && (map[this.position.x - 1][this.position.y] != Tileset.FLOWER)
                    && (map[this.position.x - 1][this.position.y] != Tileset.PLAYER)) {
                directionArray.addFirst(-1);
            }
            if ((map[this.position.x + 1][this.position.y] != Tileset.WALL)
                    && (map[this.position.x + 1][this.position.y] != Tileset.FLOWER)
                    && (map[this.position.x + 1][this.position.y] != Tileset.PLAYER)) {
                directionArray.addFirst(1);
            }
            if ((map[this.position.x][this.position.y - 1] != Tileset.WALL)
                    && (map[this.position.x][this.position.y - 1] != Tileset.FLOWER)
                    && (map[this.position.x][this.position.y - 1] != Tileset.PLAYER)) {
                directionArray.addFirst(-2);
            }
            if ((map[this.position.x][this.position.y + 1] != Tileset.WALL)
                    && (map[this.position.x][this.position.y + 1] != Tileset.FLOWER)
                    && (map[this.position.x][this.position.y + 1] != Tileset.PLAYER)) {
                directionArray.addFirst(2);
            }
            int direction = (int) directionArray.get(RANDOM.nextInt(directionArray.size()));
            /** move left */
            if ((direction == -1) || (direction == 1)) {
                moveEnemyHorizontal(map, direction);
            } else {
                moveEnemyVertical(map, direction);
            }
        }

        private void moveEnemyHorizontal(TETile[][] map, int direction) {
            /** move left */
            if (direction < 0) {
                placeEnemy(map, this.position, -1, 0);
            } else {
                placeEnemy(map, this.position, 1, 0);
            }
        }

        private void moveEnemyVertical(TETile[][] map, int direction) {
            /** move down */
            if (direction < 0) {
                placeEnemy(map, this.position, 0, -1);
            } else { /** move up */
                placeEnemy(map, this.position, 0, 1);
            }
        }
    }
}
