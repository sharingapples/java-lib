A set of java library for various tasks.

1. <code>StatusBuilder</code><br>
   A helper library for creating status snapshots on a file.
   The ThreadPool library can use the mechanism for creating
   snapshots on a user thread on periodic interval. Example:
   ```java
   // Initialize a builder with the output file and status provider
   StatusBuilder builder = new StatusBuilder(provider);

   // Call the dump method to update the status file
   builder.dump();

   // A simple usecase in the ThreadPool below
   // Do all the meat stuff
   // ...

   // and for dumping the status of the pool once in a while
   pool.join(pool::stop, builder::dump);
   ```

1. <code>ThreadPool</code><br>
   Create a pool with multiple threads to run user defined
   <code>Task</code>s. Fully thread safe. Basic Usage
   ```java
   ThreadPool pool = new ThreadPool(5); // Create 5 thread pool

   // push some tasks on the pool
   pool.push(task);
   // add more task as needed

   // start the pool
   pool.start();

   // wait on the pool with a user task, and stop if idle
   pool.join(pool::stop, () -> {
     // This is a user task that is run periodically
     // by default every 1000 ms approx
     System.out.println("Hello World - Heart Beat");
   });
   ```

2. <code>NepaliCalendar</code><br>
   A handy library for converting between Gregorian calendar
   and Nepali Calendar (Bikram Sambat)