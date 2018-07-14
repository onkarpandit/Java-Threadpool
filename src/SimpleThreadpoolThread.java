import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class SimpleThreadpool{
	
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



