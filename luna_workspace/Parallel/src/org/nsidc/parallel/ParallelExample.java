package org.nsidc.parallel;
import org.nsidc.bigdata.*;

// For fork/join calls
import java.util.concurrent.*;


// Example of parallel threaded Java

public class ParallelExample { 
  
  static int procs; 

  /*---------------------------------------------------------------
  // main
  //-------------------------------------------------------------*/
  public static void main(String[] args) { 
  
    int i; 
  
    Database database1;
    Database database2;
    
    System.out.println("\nBEGIN PARALLELTEST PROGRAM");

    System.out.println(" Parallism example using threads:"); 

    String databaseName1 = "test1";
    String databaseName2 = "test2";

    if (args.length >= 1) {
      databaseName1 = args[0];
    }

    if (args.length >= 2) {
      databaseName2 = args[1];
    }


    System.out.println(" db name 1: " + databaseName1);
    System.out.println(" db name 2: " + databaseName2);

    // Open the databases
    try {
      database1 = new Database();
      database2 = new Database();
    }
    catch( Throwable t ) {
      System.err.println("Error on database open.");
      return;
    }


    // How many processors do we have available?
    procs = Runtime.getRuntime().availableProcessors(); 
    System.out.println("  Available processors = " + procs);

    // Insurance that we don't just eat all the cpus
    if (procs > 8) procs = 8;

    // Create an array to hold threads
    ParallelTask[] threads = new ParallelTask[procs]; 
  
    // Start the threads
    for (i = 0; i < procs; i++) { 

      // Calling 'start' causes the thread to start and executes the 'run' method.
      System.out.println("  Starting thread " + i);
      String queryString = "hello" + String.valueOf(i);

      // Query a different database (based on some criteria)
      if (i%2 == 0) (threads[i] = new ParallelTask(i, queryString, database2)).start(); 
      if (i%2 != 0) (threads[i] = new ParallelTask(i, queryString, database1)).start(); 
    } 
  

    double sum = 0.0;

    // Combine the output
    for (i = 0; i < procs; i++) { 
      try { 
        // Re-unites all threads; waits until each thread is done.
        threads[i].join(); 
      }
      catch (InterruptedException e) {
      } 
  
      // Retrieves the value of the 'x' variable from the thread
      sum += threads[i].x; 

      System.out.println("    Thread = " + i +
                         "  threads[i].x = " + threads[i].x +
                         "  myQuery = " + threads[i].myQuery);

    } 
  
    System.out.println("  sum = " + sum); 
    System.out.println("---------------------------"); 
    System.out.println("  "); 
    System.out.println(" Parallism example using fork/join with recursion:"); 
    

    // source image pixels are in src
    // destination image pixels are in dst
    
    int[] src = new int[1000];
    for (i = 0; i < 1000; i++) src[i] = i;
    
    int[] dst = new int[1000];
    
    BogusTask bogus = new BogusTask(src, 0, src.length, dst);

    // Create the ForkJoinPool that will run the task.

    ForkJoinPool pool = new ForkJoinPool();

    // Run the task.
    pool.invoke(bogus);
    
    System.out.println("Parallelism: " + pool.getParallelism());
    System.out.println("Active Threads: " + pool.getActiveThreadCount());
    System.out.println("Task Count: " + pool.getQueuedTaskCount());
    System.out.println("Steal Count: " + pool.getStealCount());
 }
  

 
  
  
  /*---------------------------------------------------------------
  // ParallelTask
  //
  // Class for a parallel thread.
  //-------------------------------------------------------------*/
  static class ParallelTask extends Thread { 
 
    // Variables to be retrieved by the master thread must be static 

    String myQuery;

    Database database;

    int threadNumber; 

    double x = 0.0;   //Some number, an example
  

    /*--------------------------------------------------------------------------
    // ParallelTask constructor
    //------------------------------------------------------------------------*/
    public ParallelTask(int id, String inputQuery, Database db) { 
      this.threadNumber = id;
      this.myQuery = inputQuery;
      this.database = db;
      System.out.println("    Created thread number " + threadNumber);
    }
 
  
    /*---------------------------------------------------------------
    // run
    //
    // This is where the work gets done.
    //-------------------------------------------------------------*/
    public void run() {

      System.out.println("      Thread " + threadNumber + " query: " + myQuery);

      // Some kind of work or query.
      x = threadNumber * 2.0;
    }
  } 

}