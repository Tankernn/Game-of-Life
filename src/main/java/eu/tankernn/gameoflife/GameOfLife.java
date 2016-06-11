package eu.tankernn.gameoflife;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import eu.tankernn.gameEngine.util.NativesExporter;

public class GameOfLife {

	private static long lastFrameTime;
	private static float delta;

	private static boolean justPressed;

	private static boolean[][] cells;

	private static int gridWidth = 50, gridHeight = 50;

	private static int displayWidth = 800, displayHeight = 600;
	private static int cellWidth = displayWidth / gridWidth, cellHeight = displayHeight / gridHeight;

	private static boolean paused = false;
	private static boolean enableGrid = true;
	private static boolean debug = true;

	private static void initDisplay() {
		NativesExporter.exportNatives();
		try {
			Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// init OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, displayWidth, 0, displayHeight, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public static void main(String[] args) {
		initDisplay();
		cells = new boolean[gridWidth][gridHeight];
		initCells();

		dumpCells();

		while (!Display.isCloseRequested()) {
			long currentFrameTime = getCurrentTime();
			delta += (currentFrameTime - lastFrameTime) / 1000f;
			lastFrameTime = currentFrameTime;

			if (!paused && delta > 0.01f) {
				cells = update();
				delta = 0;
			}
			processInput();
			dumpCells();

			Display.update();

		}

		Display.destroy();
	}

	private static void processInput() {
		if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
			int x, y;
			x = (Mouse.getX()) / cellWidth;
			y = (Mouse.getY()) / cellHeight;
			if (debug)
				System.out.println("X: " + x + ", Y: " + y);
			cells[x][y] = Mouse.isButtonDown(0);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
			if (!justPressed)
				paused = !paused;
			justPressed = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
			if (!justPressed)
				cells = new boolean[gridWidth][gridHeight];
			justPressed = true;
		} else if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
			if (!justPressed)
				enableGrid = !enableGrid;
			justPressed = true;
		} else {
			justPressed = false;
		}
	}

	private static void dumpCells() {
		// Clear the screen and depth buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// set the color of the quads (R,G,B,A)
		GL11.glColor3f(0.0f, 1.0f, 0.0f);

		for (int y = 0; y < cells[0].length; y++) {
			for (int x = 0; x < cells.length; x++) {
				if (debug)
					System.out.print(cells[x][y] ? "X" : "-");
				if (cells[x][y]) {
					// draw quad
					GL11.glBegin(GL11.GL_QUADS);
					GL11.glVertex2f(cellWidth * x, cellHeight * y);
					GL11.glVertex2f(cellWidth * x + cellWidth, cellHeight * y);
					GL11.glVertex2f(cellWidth * x + cellWidth, cellHeight * y + cellHeight);
					GL11.glVertex2f(cellWidth * x, cellHeight * y + cellHeight);
					GL11.glEnd();
				}
			}
			if (debug)
				System.out.println();
		}
		if (enableGrid) {
			// set the color of the lines (R,G,B,A)
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
			for (int y = 0; y <= gridHeight; y++) {
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2f(0, cellHeight * y);
				GL11.glVertex2f(displayWidth, cellHeight * y);
				GL11.glEnd();
			}
			for (int x = 0; x <= gridWidth; x++) {
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2f(cellWidth * x, 0);
				GL11.glVertex2f(cellWidth * x, displayHeight);
				GL11.glEnd();
			}
		}
		if (debug)
			System.out.println();
	}

	private static boolean[][] update() {
		boolean[][] newcells = new boolean[gridWidth][gridHeight];
		for (int x = 0; x < cells.length; x++)
			for (int y = 0; y < cells[0].length; y++) {
				int neighbours = 0;
				try {
					if (cells[x][y + 1])
						neighbours++;
					if (cells[x][y - 1])
						neighbours++;
					if (cells[x + 1][y + 1])
						neighbours++;
					if (cells[x + 1][y - 1])
						neighbours++;
					if (cells[x - 1][y + 1])
						neighbours++;
					if (cells[x - 1][y - 1])
						neighbours++;
					if (cells[x + 1][y])
						neighbours++;
					if (cells[x - 1][y])
						neighbours++;
				} catch (ArrayIndexOutOfBoundsException ex) {
					continue;
				}
				if (cells[x][y]) {
					if (neighbours <= 1 || neighbours >= 4) {
						newcells[x][y] = false;
					} else {
						newcells[x][y] = true;
					}
				} else {
					if (neighbours == 3) {
						newcells[x][y] = true;
					} else {
						newcells[x][y] = false;
					}
				}
			}
		return newcells;
	}

	private static void initCells() {
		// cells[25][23] = true;
		// cells[25][27] = true;
		//
		// cells[23][23] = true;
		// cells[23][24] = true;
		// cells[23][25] = true;
		// cells[23][26] = true;
		// cells[23][27] = true;
		//
		// cells[27][23] = true;
		// cells[27][24] = true;
		// cells[27][25] = true;
		// cells[27][26] = true;
		// cells[27][27] = true;
	}

	private static long getCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}
}
