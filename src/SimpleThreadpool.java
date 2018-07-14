import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class SimpleThreadpool{
	
	private static AtomicInteger poolCount= new AtomicInteger(0);
	private ConcurrentLinkedQueue<Runnable> runnables;
	private AtomicBoolean execute;
	private List<SimpleThreadpoolThread> threads;
	
	public static void main(String [] args) {
		SimpleThreadpool thread = new SimpleThreadpool(10);
		thread.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("Starting tast A....");
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Task A Completed ...");
			}
		});
		
		thread.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Task B....");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Task B Completed....");
            }
        });
		
		
	}
	
	
	private class ThreadpoolException extends RuntimeException{
		public ThreadpoolException(Throwable cause) {
			super(cause);
		}
	}
	
	
	private class SimpleThreadpoolThread extends Thread{

		private AtomicBoolean execute;
		private ConcurrentLinkedQueue<Runnable> runnables;
		
		public SimpleThreadpoolThread(String name, AtomicBoolean execute , ConcurrentLinkedQueue<Runnable> runnables) {
			
			super(name);
			this.execute= execute;
			this.runnables = runnables;
			
		}
		
		@Override
		public void run() {
			try {
				while(execute.get() || !runnables.isEmpty()) {
					Runnable runnable;
					
					while((runnable=runnables.poll())!=null) {
						runnable.run();
					}
					Thread.sleep(10);
				}
			}
			catch(RuntimeException | InterruptedException e){
				throw new ThreadpoolException(e);
			}
		}
		
	}
	
	private SimpleThreadpool(int threadCount) {
		
		poolCount.incrementAndGet();
		this.runnables = new ConcurrentLinkedQueue<>();
		this.execute = new AtomicBoolean(true);
		this.threads = new ArrayList<>();
		
		for (int threadIndex =0;threadIndex<threadCount;threadIndex++) {
			SimpleThreadpoolThread thread = new SimpleThreadpoolThread("SimpleThreadpool" + poolCount.get() + "Thread" + threadIndex, this.execute, this.runnables);
			thread.start();
			this.threads.add(thread);
		}
		
	}
	
	public static SimpleThreadpool getinstance() {
		return getinstance(Runtime.getRuntime().availableProcessors());
	}
	
	public static SimpleThreadpool getinstance(int threadCount) {
		return new SimpleThreadpool(threadCount);
	}
	
	public void execute(Runnable runnable) {
		if (this.execute.get()) {
			runnables.add(runnable);
		} else {
			throw new IllegalStateException ("Threadpool terminating, unable to execute runnable");
		}
	}
	public void terminate() {
        runnables.clear();
        stop();
    }

    
    public void stop() {
        execute.set(false);
    }
    
    
    public void awaitTermination(long timeout) throws TimeoutException{
    	if (this.execute.get()) {
    		throw new IllegalStateException("Threadpool is not terminated before awaiting termination");
    	}
    	long startTime = System.currentTimeMillis();
    	while(System.currentTimeMillis()-startTime<=timeout) {
    		boolean flag = false;
    		for (Thread thread : threads) {
    			if (thread.isAlive()) {
    				flag=false;
    				break;
    			}
    			if (flag)
        			return;
    			
    			try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new ThreadpoolException(e);
                }
            
    		}
            
    		throw new TimeoutException("Unable to terminate threadpool within the specified timeout (" + timeout + "ms)");
    		
    	}
    		
    	}
    
    public void awaitTermination() throws TimeoutException {
        if (this.execute.get()) {
            throw new IllegalStateException("Threadpool not terminated before awaiting termination");
        }
        while (true) {
            boolean flag = true;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new ThreadpoolException(e);
            }
        }
    }
    
    
}





