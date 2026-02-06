package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Direction;
import co.eci.snake.core.GameState;
import co.eci.snake.core.Snake;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public final class SnakeRunner implements Runnable {

  private final Snake snake;
  private final Board board;
  private final AtomicReference<GameState> state;

  private final int baseSleepMs = 80;
  private final int turboSleepMs = 40;
  private int turboTicks = 0;

  public SnakeRunner(Snake snake, Board board, AtomicReference<GameState> state) {
    this.snake = snake;
    this.board = board;
    this.state = state;
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted() && state.get() != GameState.STOPPED) {

        // esperar mientras el juego esté en pausa
        while (state.get() == GameState.PAUSED) {
          Thread.sleep(50);
        }

        maybeTurn();

        var res = board.step(snake);

        if (res == Board.MoveResult.HIT_OBSTACLE) {
          randomTurn();
        } else if (res == Board.MoveResult.ATE_TURBO) {
          turboTicks = 100;
        }

        int sleep = (turboTicks > 0) ? turboSleepMs : baseSleepMs;
        if (turboTicks > 0) turboTicks--;

        Thread.sleep(sleep);
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  private void maybeTurn() {
    double p = (turboTicks > 0) ? 0.05 : 0.10;
    if (ThreadLocalRandom.current().nextDouble() < p) {
      randomTurn();
    }
  }

  private void randomTurn() {
    var dirs = Direction.values();
    snake.turn(dirs[ThreadLocalRandom.current().nextInt(dirs.length)]);
  }
}

