package com.muzlik.data;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Manages async operations with a bounded thread pool and queue.
 * Prevents thread exhaustion and provides graceful shutdown.
 */
public class AsyncExecutor {
    
    private final JavaPlugin plugin;
    private final ExecutorService executor;
    private final BlockingQueue<Runnable> taskQueue;
    private final int maxQueueSize;
    
    private static final int THREAD_POOL_SIZE = 3; // 2-4 threads as specified
    private static final int MAX_QUEUE_SIZE = 200;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;
    
    public AsyncExecutor(JavaPlugin plugin) {
        this(plugin, THREAD_POOL_SIZE, MAX_QUEUE_SIZE);
    }
    
    public AsyncExecutor(JavaPlugin plugin, int threadPoolSize, int maxQueueSize) {
        this.plugin = plugin;
        this.maxQueueSize = maxQueueSize;
        this.taskQueue = new LinkedBlockingQueue<>(maxQueueSize);
        
        // Create thread pool with custom thread factory
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "FrostSMP-Async-" + counter++);
                thread.setDaemon(true);
                return thread;
            }
        };
        
        this.executor = new ThreadPoolExecutor(
            threadPoolSize,
            threadPoolSize,
            60L,
            TimeUnit.SECONDS,
            taskQueue,
            threadFactory,
            new ThreadPoolExecutor.AbortPolicy()
        );
        
        plugin.getLogger().info("AsyncExecutor initialized with " + threadPoolSize + " threads and queue size " + maxQueueSize);
    }
    
    /**
     * Submit a task for async execution
     * 
     * @param task The task to execute
     * @return Future representing the task
     * @throws RejectedExecutionException if queue is full
     */
    public Future<?> submit(Runnable task) {
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            plugin.getLogger().warning("AsyncExecutor queue full! Task rejected. Queue size: " + getQueueSize());
            throw e;
        }
    }
    
    /**
     * Submit a callable task for async execution
     * 
     * @param task The task to execute
     * @return Future representing the task result
     * @throws RejectedExecutionException if queue is full
     */
    public <T> Future<T> submit(Callable<T> task) {
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            plugin.getLogger().warning("AsyncExecutor queue full! Task rejected. Queue size: " + getQueueSize());
            throw e;
        }
    }
    
    /**
     * Execute a task asynchronously with error handling
     * 
     * @param task The task to execute
     * @param errorHandler Handler for exceptions
     */
    public void executeAsync(Runnable task, java.util.function.Consumer<Exception> errorHandler) {
        submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error in async task", e);
                if (errorHandler != null) {
                    errorHandler.accept(e);
                }
            }
        });
    }
    
    /**
     * Execute a task asynchronously (fire and forget)
     * 
     * @param task The task to execute
     */
    public void executeAsync(Runnable task) {
        executeAsync(task, null);
    }
    
    /**
     * Get current queue size
     * 
     * @return Number of tasks waiting in queue
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Get max queue size
     * 
     * @return Maximum queue capacity
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
    
    /**
     * Check if queue is near capacity
     * 
     * @return true if queue is >80% full
     */
    public boolean isQueueNearCapacity() {
        return getQueueSize() > (maxQueueSize * 0.8);
    }
    
    /**
     * Shutdown executor gracefully
     * Waits for tasks to complete with timeout
     */
    public void shutdown() {
        plugin.getLogger().info("Shutting down AsyncExecutor...");
        
        executor.shutdown();
        
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("AsyncExecutor tasks did not complete in time, forcing shutdown");
                executor.shutdownNow();
                
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    plugin.getLogger().severe("AsyncExecutor did not terminate");
                }
            } else {
                plugin.getLogger().info("AsyncExecutor shutdown complete");
            }
        } catch (InterruptedException e) {
            plugin.getLogger().warning("AsyncExecutor shutdown interrupted");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Get the underlying executor service
     * 
     * @return The executor service
     */
    public ExecutorService getExecutor() {
        return executor;
    }
    
    /**
     * Check if executor is shutdown
     * 
     * @return true if shutdown
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }
    
    /**
     * Check if executor is terminated
     * 
     * @return true if terminated
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }
}
