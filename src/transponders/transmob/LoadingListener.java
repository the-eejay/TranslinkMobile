package transponders.transmob;

/**
 * Interface to be used by the async tasks to add Listener functionality, intended to
 * call a change when starting or finishing an asynchronous task
 * 
 *
 */
public interface LoadingListener {
	public void onStateChange (boolean state);
}
