package tomocomd.md;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import tomocomd.StartpepException;
import tomocomd.model.Peptide;

public class ComputeBatch {

  private static final Logger logger = Logger.getLogger(ComputeBatch.class.getName());

  private final ExecutorService executorService;
  private final List<Future<?>> futures;
  private final RealMatrix results;
  private final int numberOfTasks;

  public ComputeBatch(int peptidesNumber, int headingsSize) {
    this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    this.futures = new ArrayList<>();
    this.results = new Array2DRowRealMatrix(peptidesNumber, headingsSize);
    this.numberOfTasks = peptidesNumber * headingsSize;
  }

  public void computeInBatch(List<Peptide> peptides, List<String> headings)
      throws InterruptedException {
    List<Callable<Void>> tasks = new ArrayList<>();

    for (int i = 0; i < peptides.size(); i++) {
      for (int j = 0; j < headings.size(); j++) {
        int finalJ = j;
        int finalI = i;
        tasks.add(
            () -> {
              results.setEntry(
                  finalI, finalJ, ComputeMD.compute(peptides.get(finalI), headings.get(finalJ)));
              return null;
            });
      }
    }

    int[] batchSizes = getBatchSize();
    int startPos = 0;
    for (int size : batchSizes) {
      int lastPos = Math.min(startPos + size, numberOfTasks);
      List<Callable<Void>> batch = tasks.subList(startPos, lastPos);
      List<Future<Void>> futuresBatch = executorService.invokeAll(batch);
      for (Future<Void> future : futuresBatch) {
        try {
          future.get();
        } catch (Exception e) {
          cancelAll(futuresBatch);
          Thread.currentThread().interrupt();
          throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(e);
        }
      }
      startPos += size;
    }
  }

  public int[] getBatchSize() {
    int numP = Runtime.getRuntime().availableProcessors();
    int first = Math.max(numP, numberOfTasks / (2 * numP));

    int numberBatch = (int) Math.ceil((2.0 * numberOfTasks) / (first + numP));

    int scheduledNumTask = 0;
    double d = (first - numP) / (numberBatch - 1.0);
    int[] batchSizes = new int[numberBatch];
    for (int i = 0; i < numberBatch; i++) {
      int current = first - (int) (i * d);
      batchSizes[i] =
          scheduledNumTask + current > numberOfTasks ? numberOfTasks - scheduledNumTask : current;
      scheduledNumTask += batchSizes[i];
    }
    return batchSizes;
  }

  public RealMatrix getResults() throws StartpepException {
    for (Future<?> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        cancelAll();
        Thread.currentThread().interrupt();
        throw StartpepException.ExceptionType.COMPUTE_MD_EXCEPTION.get(e);
      }
    }
    return results;
  }

  public void shutdown() throws InterruptedException {
    executorService.shutdown();
    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
      executorService.shutdownNow();
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        logger.log(Level.SEVERE, "Executor did not terminate in the expected time.");
      }
    }
  }

  public void cancelAll() {
    for (Future<?> future : futures) {
      future.cancel(true);
    }
  }

  public void cancelAll(List<Future<Void>> futures) {
    for (Future<Void> future : futures) {
      future.cancel(true);
    }
  }

  public void interruptExecution(List<Future<Double>> futures) {
    for (Future<Double> future : futures) {
      if (!future.isDone()) {
        future.cancel(true);
      }
    }
  }
}
